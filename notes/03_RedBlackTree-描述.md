## 红黑树的五条性质
```
<1> 节点要么是红色要么是黑色
<2> 根节点必须是黑色
<3> 从任意一个节点到叶子节点经过的黑色节点的个数相同
<4> 红色节点的子节点必须是黑色(由此可以推断出, 红色节点的父节点必然是黑色的)
<5> 所有的叶子节点(null节点)都是黑色的
```

## 红黑树的等价替换
```
<1> 红黑树和4阶BTree(2-3-4树)具有等价性
<2> Black节点和它的Red子节点融合在一起, 形成一个B树节点
<3> 红黑树的Black节点的个数和B树的节点个数是相等的
<4> 红黑树相对于B树来说, 红黑树是一个黑平衡的平衡二叉树
<5> 2-3树并不能完全的匹配红黑树的全部情况
```

> 所以需要注意的是, 我们红黑树的实现全部都以4阶B树为目标进行实现的, 并且要保持红黑树的性质, 换句话说, 我们对红黑树的增删操作都是以B树的增删作为参考点的, 只不过在增删的过程中需要考虑是否满足红树而已



## 判断一棵树是否是红黑树(证明红黑树的5条性质即可)
```java
public boolean isRBTree () {
  // 根节点必须是黑色
  return root.color == BLACK && isCommonBlackNodeToLeave() && judge2( root );
}

// 红黑树判断一: 从根节点出发到任意一个子节点经过的黑色节点的个数相同
private boolean isCommonBlackNodeToLeave () {
  // 先从根节点到达最小节点判断黑色节点的个数
  int blackCount = 0;
  Node<T> curNode = root;
  while ( curNode != null ) {
    blackCount = curNode.color == BLACK ? blackCount + 1 : blackCount;
    curNode = curNode.leftChild;
  }

  // 递归判断
  return isCommonBlackNodeToLeave( root, 0, blackCount );
}

private boolean isCommonBlackNodeToLeave (Node<T> node, int count, int blackCount) {
  if ( node == null ) {
    return count == blackCount;
  }

  if ( node.color == BLACK )
    count++;

  boolean result1 = isCommonBlackNodeToLeave( node.leftChild, count, blackCount );
  boolean result2 = isCommonBlackNodeToLeave( node.rightChild, count, blackCount );

  return result1 && result2;
}

// 红黑树判断二: 红色节点的子节点必须是黑色(由此可以推断出, 红色节点的父节点必然是黑色的)
public boolean judge2 (Node<T> node) {
  if ( node == null )
    return true;

  if ( isRed( node ) && ( isRed( node.leftChild ) || isRed( node.rightChild ) ) ) {
    return false;
  }

  boolean b1 = judge2(node.leftChild);
  boolean b2 = judge2(node.rightChild);

  return b1 && b2;
}
```




















## 将二叉树以树状的结构打印( Printer的使用方法 )
- 简单使用
```
<1> 将整个printer文件夹拷贝并放到项目中
<2> 在项目中自己实现的二叉树中实现printer文件下的BinaryTreeInfo接口, 并实现四个方法
    Object root (): 返回根节点
    Object left (Object node): 返回传入的节点的左节点, 需要强转
    Object right (Object node): 返回传入的节点的右节点, 需要强转
    Object string (Object node): 返回该节点的输出形式
<3> 调用printer文件下的BinaryTrees类的println方法, 并传入一个二叉树对象即可
```


############################################
#################TreeMap代码实现#############
############################################