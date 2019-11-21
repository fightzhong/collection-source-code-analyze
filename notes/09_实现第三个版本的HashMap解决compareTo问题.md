
> **对应HashMapV3.java**

## 问题
```
实现Comparable接口则必须实现compareTo方法, 往哈希表中放入键值对, 键必须实现equals方法, 该方法的
作用是在哈希表中判断两个对象是否是相等的, 我们这里举JDK1.8之前的哈希表来进行说明, 1.8之前是采用数组
+链表实现的哈希表, hashCode方法用于确定key存放在数组的哪个位置, equals方法用于确定在该索引下的链表
中是否存在该对象, 在我们的实现中, 每个索引存放的是一个红黑树, 那么对于红黑树来说, 需要对key进行比较
才能得知放在左边还是右边, 以put操作为例, 我们采用了先比较hashCode, 在hashCode相同的情况下, 应该先
通过equals判断是否相等, 如果不相等, 则需要比较出一个大小来才能确定往哪边插入, 此时先判断是否具备可
比较性(是否实现了Comparable接口), 如果实现了该接口, 那么我们的做法就是直接调用compareTo来获取比较
结果, 这里就会出现一个问题, 当equals返回false的时候, compareTo是有可能返回true的, 因为两个方法都
是我们自己实现的, 而两个对象是否相等, 只能通过equals来判断, compareTo是不能进行判断的, 所以对于该
情况来说, 我们不应该在compareTo返回为0的情况下认为两个key相等, 而仅仅能利用不返回0的情况来判断谁大
谁小, 当compareTo返回0的时候, 我们应该采用内存地址来进行判断谁大谁小, 知道了问题所在后, 我们下面
对node方法进行重构, 其它方法修改的也是类似的
```

## node方法
- 重构前部分代码
```java
if ( Objects.equals( key1, key2 ) ) { // 相同, 返回node
  return node;
}

// 不相同, 则看看是否具备可比较性
if ( key1 != null && key2 != null
  && key1.getClass() == key2.getClass()
  && key1 instanceof Comparable ) { // 具备可比较性
  cmp = ((Comparable) key1).compareTo( key2 );
} else {
  // 不具备可比较性, 则去当前节点的左边和右边进行查找
  Node<K, V> left = node( node.left, key );
  Node<K, V> right = node( node.right, key );

  return left == null ? right : left;
}
```

- 重构后的部分代码
```java
if ( Objects.equals( key1, key2 ) ) { // 相同, 返回node
  return node;
}

// 不相同, 则看看是否具备可比较性
if ( key1 != null && key2 != null
  && key1.getClass() == key2.getClass()
  && key1 instanceof Comparable
  && ((Comparable) key1).compareTo( key2 ) != 0 ) { // 具备可比较性, 当两者比较不为0的时候才可以修改cmp
  cmp = ((Comparable) key1).compareTo( key2 );
} else {
  // 不具备可比较性, 则去当前节点的左边和右边进行查找
  Node<K, V> left = node( node.left, key );
  Node<K, V> right = node( node.right, key );

  return left == null ? right : left;
}
```
