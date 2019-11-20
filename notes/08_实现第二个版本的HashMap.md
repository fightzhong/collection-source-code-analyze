## node方法改进
- 描述
```
在第一个版本中, 由于内存地址的不确定性, 会导致我们查找一个元素的时候使用内存地址存在不确定性, 对于
该问题, 我们改进的方式是, node方法在面对不具备可比较性的对象时, 直接采用扫描的方式对整个红黑树进行
扫描(JDK中HashMap也是这样实现的), 这样就一定能够等知是否存在该元素了, 通过递归的方式来进行扫描, 在
改进的版本中, 我们抛弃了compare方法, 直接将该方法的部分实现写在node方法中, 仅仅将不具备可比较性的
情况进行了修改, 下面我们直接看代码, 里面有注释进行说明
```

```java
private Node<K, V> node (K key) {
  // 获取key应该存放在table的哪个索引下
  int index = index( key );
  Node<K, V> root = table[index];

  if ( root == null )
    return null;

  return node( root, key );
}

// 在以node为子树的红黑树中查找是否存在key
private Node<K, V> node (Node<K, V> node, K key) {
  /*
    key1: 需要被查找的key
    hashCode1: 需要被查找的key对应的哈希值

    key2: 当前比较的key
    hashCode1: 当前比较的key对应的哈希值
  */
  K key1 = key;
  int hashCode1 = key.hashCode();

  while ( node != null ) {
    K key2 = node.key;
    int hashCode2 = node.hashCode;

    int cmp = 0; // 比较的结果值
    // 先用hashCode进行查找
    if ( hashCode1 > hashCode2 ) { // 去该节点的右边查找
      cmp = 1;
    } else if ( hashCode1 < hashCode2 ) { // 去该节点的左边查找
      cmp = -1;
    } else { // hashCode相同, 利用equals进行判断
      if ( Objects.equals( key1, key2 ) ) { // 相同, 返回node
        return node;
      }

      // 不相同, 则看看是否具备可比较性
      if ( key1 != null && key2 != null
        && key1.getClass() == key2.getClass()
        && key1 instanceof Comparable ) { // 具备可比较性

        cmp = ((Comparable) key1).compareTo( key2 );
      } else {
        // 不具备可比较性, 则去当前节点的左边和右边进行全盘扫描
        Node<K, V> left = node( node.left, key );
        Node<K, V> right = node( node.right, key );

        return left == null ? right : left;
      }
    }

    if ( cmp > 0 ) { // key1 > key2
      node = node.right;
    } else if ( cmp < 0 ) {
      node = node.left;
    }
  }

  return null;
}
```

## put方法改进
> put方法的改进跟node类似, 不同的是在不具备可比较性的情况下, 我们需要先利用node方法来进行全盘扫描, 如果扫描到了key, 则更新val, 如果没有扫描到key, 则采用内存地址来进行判断应该在左边还是右边进行插入

```java
public V put (K key, V val) {
  // 获取key对应的索引及该索引下的红黑树的根节点
  int index = index(key);
  Node<K, V> root = table[index];

  // 根节点为空的情况
  if ( root == null ) {
    table[index] = new Node( key, val, null );
  } else {
    Node<K, V> parent = null;
    Node<K, V> curNode = root;
    int cmp = 0;


    /************************分割线内的代码即进行了改进的代码*******************************/
    K key1 = key;
    int hashCode1 = key == null ? 0 : key.hashCode();
    while ( curNode != null ) {
      K key2 = curNode.key;
      int hashCode2 = curNode.hashCode;

      // 先进行hashCode比较
      if ( hashCode1 > hashCode2 ) {
        cmp = 1;
      } else if ( hashCode1 < hashCode2 ) {
        cmp = -1;
      } else { // hashCode相等, 利用equals进行判断
        if ( Objects.equals( key1, key2 ) ) { // 相等则需要将新值替换旧值
          cmp = 0;
        } else { // equals不相等, 则需要判断是否具备可比较性
          // 具有可比较性
          if ( key1 != null && key2 != null
              && key1.getClass() == key2.getClass()
              && key1 instanceof Comparable ) {
            cmp = ((Comparable) key1).compareTo( key2 );
          } else {
            // 不具有可比较性, 去curNode所在的子树递归查找是否存在该元素
            Node<K, V> target = node( curNode, key );

            if ( target == null ) { // 不存在, 才利用内存地址去判断往哪边添加
              cmp = System.identityHashCode( key1 ) - System.identityHashCode( key2 );
            } else { // 存在, 则cmp应该为0, 之后可以替换该值
              cmp = 0;
              // curNode需要置换为target, 这样才能在下面cmp=0的判断中进行值的替换
              curNode = target;
            }
          }
        }
      }

      parent = curNode;
      if ( cmp > 0 ) { // key1 > key2
        curNode = curNode.right;
      } else if ( cmp < 0 ){
        curNode = curNode.left;
      } else {
        V oldVal = curNode.val;

        curNode.key = key;
        curNode.val = val;

        return oldVal;
      }
    }
    /************************分割线内的代码即进行了改进的代码*******************************/

    // 到了这一步, 必定是找到了空的节点, 同时维护了空的节点的父亲节点
    // 此时根据比较的结果判断将新的节点插入到父亲节点的左边还是右边
    Node<K, V> newNode = new Node<>( key, val, parent );
    if ( cmp > 0 ) {
      parent.right = newNode;
    } else {
      parent.left = newNode;
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





















key为null
哈希值相减的问题, 导致溢出, 从而成为负数
打印一下结果进行验证
