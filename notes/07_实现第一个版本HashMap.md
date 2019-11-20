## 描述
```
该章节是对HashMap进行第一版本的实现, 主要实现put, remove, get操作, 先说一下实现的思路, 首先HashMap
底层的存储结构我们先定为数组+红黑树, 在JDK1.7之前是数组+链表, 对于JDK1.7的实现来说, 相对简单, 因为
链表是一条线的, 不用考虑元素的存放位置, 只需要equals一直比较下去就好了, 而红黑树需要考虑哈希冲突后
元素往左还是往右添加, 而我们传入HashMap的元素可能是没有实现Comparable接口的, 比如一个自定义类Car,
只实现了hashCode和equals, 所以在产生哈希冲突的时候, 对于红黑树的compare操作需要进行一定的设计, 在
该版本中我们也不去考虑扩容以及JDK8的(链表+红黑树)的问题, 仅仅实现一个数组+红黑树的版本, 目的是为了
引入compare的设计实现

我们在实现的过程中, 就是把上面实现的红黑树的代码复制过来而已, 然后进行一些小小的修改, 比如Node中由
val单属性变成key, val键值对的形式
```

## HashMap-Version1

> 源代码文件路径**\src\main\java\com\fightzhong\hashmap\HashMapV1.java**

- Node节点
```java
private static class Node<K, V> {
  // Node的键值对
  K key;
  V val;

  int color = RED;  // 红黑树的节点默认为红色的
  int hashCode;     // 键key对应的hashCode值, 在这里我们对每个节点都保存这个值, 方便之后代码进行引用

  Node<K, V> left;  // 左孩子
  Node<K, V> right; // 右孩子
  Node<K, V> parent;  // 父亲节点

  public Node (K key, V val,  Node<K, V> parent) {
    this.key = key;
    this.val = val;
    this.parent = parent;
    this.hashCode = key.hashCode(); // 对键key对应的hashCode进行一下赋值
  }
}
```

- HashMap中的成员变量及构造方法
```java
// 红黑树颜色的引用
private static int RED = 1;
private static int BLACK = 0;

/*
  HashMap底层中数组的默认容量, 根据上一节的分析, 必须是2的幂, 在JDK实现中默认长度为16
  这里我们采用左移操作, 因为2^4 = 1 << 4, 而2^5 = 1 << 5, 所以增加后面的值就可以增加2的幂
*/
private static int DEFAULT_INITIAL_CAPACITY = 1 << 4; 

// HashMap中总的元素个数
private int size;

/*
  HashMap中用于存储元素的数组+红黑树底层实现, 这里我们直接采用红黑树的根节点来代表红黑树
  而不是每个数组中放入一个RBTree对象, 因为效果都差不多, 直接将根节点放进去更方便一点
*/
private Node<K, V>[] table;

public HashMapV1 () {
  this( DEFAULT_INITIAL_CAPACITY );
}

public HashMapV1 (int initialCapacity) {
  this.table = new Node[initialCapacity];
}
```

- index方法(用于获取一个元素存放在数组的哪个地方)
```java
public int index (K key) {
  if ( key == null )
    return 0;

  int hashCode = key.hashCode();
  /*
    由于传入的key中hashCode方法计算得到的哈希值是key自带的
    我们对hashCode再进行一次计算, 这样就可以更加好的减少哈希冲突,
    在JDK中二次哈希对应的是hash方法, 而index方法没有提供, 在每次需要
    计算索引的时候都是直接进行与运算的, 即JDK没有把这一步抽出来
    */
  hashCode = hashCode ^ ( hashCode >>> 16 );
  return hashCode & ( table.length - 1 );
}
```

- put操作代码分析
```java
/*
  直接将RBTree中add方法及该方法内部用到的所有方法复制过来即可, 然后将add改成put, 参数由一个改成
  两个即(K key, V val), 在这些方法中, 还需要修改一个变量, 那就是root, 此时root应该修改成
  table[index(node.key)], 即利用当前方法中任意一个节点node的hashCode来获取存放的索引位置,
  所有该红黑树下的节点获取的索引都是相同的, 所以根据任意一个节点获取即可, 然后利用该索引获取root
  节点, 总的来说, 将所有的root替换为table[index(node.key)]
*/
public V put (K key, V val) {
  // 获取key对应的索引及该索引下的红黑树的根节点, 即当前key应该存放在数组中哪个索引下
  int index = index(key);
  Node<K, V> root = table[index];

  // 根节点为空的情况
  if ( root == null ) {
    table[index] = new Node( key, val, null );
  } else {
    Node<K, V> parent = null;
    Node<K, V> curNode = root;
    int cmp = 0;

    while ( curNode != null ) {
      cmp = compare( curNode.key, key );
      parent = curNode;

      if ( cmp > 0 ) {
        curNode = curNode.left;
      } else if ( cmp < 0 ){
        curNode = curNode.right;
      } else {
        V oldVal = curNode.val;

        curNode.key = key;
        curNode.val = val;

        return oldVal;
      }
    }

    // 到了这一步, 必定是找到了空的节点, 同时维护了空的节点的父亲节点
    // 此时根据比较的结果判断将新的节点插入到父亲节点的左边还是右边
    Node<K, V> newNode = new Node<>( key, val, parent );
    if ( cmp > 0 ) {
      parent.left = newNode;
    } else {
      parent.right = newNode;
    }

    // 维护红黑树的性质
    fixAfterInsertion( newNode );
  }

  // 维护根节点为黑色的性质
  table[index( key )].color = BLACK;

  size ++;
  return null;
}
```

- compare方法
```java
由于我们红黑树的元素节点是需要进行比较的, 在我们执行上述的操作后会发现compare还是报错的, 此时我们
来谈谈compare的实现, 首先对于我们传入HashMap中key来说, 可以是任意类型的, 那么对于这个任意类型的怎
么去比较呢?

比较的思路:
  <1> 先通过hashCode进行比较, 在同一个索引下的所有红黑树节点的哈希值可能是会不一样的, 所以利用
      hashCode也能先对两个key进行一下比较
  <2> 如果hashCode相等, 那也没办法进行比较了, 此时我们需要先看一下两个key是否equals, 如果equals
      那么就说明两个key是本身就是相等的, 此时可以直接将新的值覆盖原来的值, 在比较equals的时候, 采
      用Objects提供的equals方法进行比较,该方法进行了一定的封装(因为两个key相等可以是内存地址相等
      或者重写后的equals方法进行判断后相等)
  <3> 如果equals不相等, 则此时如果这两个key是同一种类型(getClass的值相等), 并且该类型实现了Comparable
      接口, 那么我们就可以直接利用这个该类的compareTo进行比较
  <4> 如果上面还不满足, 即两个key不具备可比较性, 此时我们采用内存地址进行比较

private int compare (K key1, K key2) {
  // 一、用hashCode进行比较
  int h1 = key1 == null ? 0 : key1.hashCode();
  int h2 = key2 == null ? 0 : key2.hashCode();

  if ( h1 - h2 != 0 )
    return h1 - h2;

  // 二、hashCode不能比较出两个的结果, 用equals进行判断是否是同一个对象
  if ( Objects.equals( key1, key2 ) )
    return 0;

  // 三、equals不能比较出来, 则判断两个对象是否具备可比较性
  if ( key1 != null && key2 != null
  && key1.getClass() == key2.getClass()
  && key1 instanceof Comparable ) {
    return ((Comparable) key1).compareTo( key2 );
  }

  // 四、如果不具备可比较性, 则利用内存地址进行比较
  int address1 = key1 == null ? 0 : System.identityHashCode( key1 );
  int address2 = key1 == null ? 0 : System.identityHashCode( key2 );

  return address1 - address2;
}
```

> 在对compare方法进行了完善后, 我们的删除操作, 以及node方法(根据key获取节点)也就跟添加操作类似了, 只需要将RBTree中对应的方法拿过来然后对一些数据进行一下修改即可, 比如root应该修改为table[index(node.key)]

- node方法(根据key获取节点)
```java
private Node<K, V> node (K key) {
  int index = index( key );
  Node<K, V> node = table[index];

  while ( node != null ) {
    int cmp = compare( key, node.key );

    if ( cmp == 0 )
      return node;

    if ( cmp > 0 ) {
      node = node.right;
    } else {
      node = node.left;
    }
  }

  return null;
}
```

- get方法(根据key获取值)
```java
public V get (K key) {
  Node<K, V> node = node(key);
  return node == null ? null : node.val;
}
```

> 关于删除的方法就不列出来了, 因为删除的方法跟添加的方法是类似的, 只需要将RBTree中对应的方法拿过来, 然后将Node<E>改为Node<K, V>, 将ele改为key, 将root改为table[index(node.key)]即可, 如果需要查看可以去源代码目录下找到HashMapV1进行查看, 而get方法和node方法之所以列出来是因为下一个版本的HashMap需要对这两个方法进行一下说明
