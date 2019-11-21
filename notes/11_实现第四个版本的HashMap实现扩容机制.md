## 扩容机制实现思路
```
扩容机制指的是当哈希表中的元素总个数达到一定大小的时候, 对哈希表数组的长度进行扩容, 然后再将元素分散
到新的哈希表中, 目的是为了减少哈希冲突以及减少红黑树的高度, 提高HashMap的性能, 在JDK中, 会维持一个
负载因子(加载因子)LoadFactor, 当哈希表中总的元素个数除以哈希表的长度的值大于该负载因子的值的时候就
会进行扩容, 即:
  LoadFactor(默认为0.75) < size / table.length

需要注意的是, 在哈希表中同一索引下的红黑树节点, 其哈希值是不一定相同的, 所以在扩容后其索引也不会相同,
所以我们需要将这些节点一个个取出来, 然后放到新的哈希表中, 扩容操作只会出现在put方法中, 只有增加元素
才会导致扩容
```

## 扩容机制实现代码
- put方法及静态参数
```java
private static float DEFAULT_LOAD_FACTOR = 0.75f; // 负载因子

public V put (K key, V val) {
  // 在增加之前判断是否需要进行扩容
  resize();

  // 下面的代码跟之前的版本的代码一样
}
```

- resize方法
```java
private void resize () {
  if ( size / table.length <= DEFAULT_LOAD_FACTOR )
    return;

  // 保存旧的哈希表, 使得table属性指向新的哈希表(大小为原来的两倍)
  Node<K, V>[] oldTable = table;
  table = new Node[oldTable.length * 2];

  // 遍历table中的每一个根节点(等价于遍历每一棵红黑树)
  for ( Node<K, V> node: oldTable ) {
    if ( node == null )
      continue;

    // 利用层序遍历获取红黑树中的所有元素
    Queue<Node<K, V>> queue = new LinkedList<>();
    queue.add( node );
    while ( !queue.isEmpty() ) {
      Node<K, V> head = queue.remove();

      if ( head.left != null )
        queue.add( head.left );

      if ( head.right != null )
        queue.add( head.right );

      // 将该元素放入新的哈希表中
      move( head );
    }
  }
}
```

- move方法
```java
/*
  说一下修改的地方, 首先传入的node应该被当作一个新的节点, 然后只需要复制put方法逻辑进行添加就好了
  <1> 作为一个新的节点, 里面的参数应该被重置:
      node.left = null;
      node.right = null;
      node.parent = null;
      node.color = RED;
  <2> 不用再进行相等判断, 因为原来的红黑树中节点肯定是唯一的, 所以新的哈希表中一定没有重复的元素,
      只需要大小比较就好了, 一定不会出现相等的情况, 所以代码可以简化, 剔除相等情况的逻辑
  <3> 在找到添加的位置后, 需要维护被移动节点(也是被当作新增节点的那个节点)的parent属性
*/
private void move (Node<K, V> node) {
  // 重置node, 将其当作一个新增的节点
  node.left = null;
  node.right = null;
  node.parent = null;
  node.color = RED;

  // 执行增加操作
  // 获取key对应的索引及该索引下的红黑树的根节点
  int index = index( node.key );
  Node<K, V> root = table[index];

  // 根节点为空的情况
  if ( root == null ) {
    table[index] = node;
  } else {
    Node<K, V> parent = null;
    Node<K, V> curNode = root;
    int cmp = 0;

    K key1 = node.key;
    int hashCode1 = node.hashCode;
    while ( curNode != null ) {
      K key2 = curNode.key;
      int hashCode2 = curNode.hashCode;

      if ( hashCode1 > hashCode2 ) {
        cmp = 1;
      } else if ( hashCode1 < hashCode2 ) {
        cmp = -1;
      } else { // hashCode1 == hashCode2, 不用判断两者是否相等
        // 具有可比较性
        if ( key1 != null && key2 != null
          && key1.getClass() == key2.getClass()
          && key1 instanceof Comparable
          && ((Comparable) key1).compareTo( key2 ) != 0 ) {
          cmp = ((Comparable) key1).compareTo( key2 );
        } else {
          cmp = System.identityHashCode( key1 ) - System.identityHashCode( key2 );
        }
      }

      parent = curNode;
      if ( cmp > 0 ) { // key1 > key2
        curNode = curNode.right;
      } else if ( cmp < 0 ){
        curNode = curNode.left;
      }
    }

    // 到了这一步, 必定是找到了空的节点, 同时维护了空的节点的父亲节点
    // 此时根据比较的结果判断将新的节点插入到父亲节点的左边还是右边
    node.parent = parent;
    if ( cmp > 0 ) {
      parent.right = node;
    } else {
      parent.left = node;
    }

    // 维护红黑树的性质
    fixAfterInsertion( node );
  }

  // 维护根节点为黑色的性质
  table[index].color = BLACK;
}
```
