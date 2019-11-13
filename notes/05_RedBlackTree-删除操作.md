## 删除情况分析
- 下面来说一下4阶B树与红黑树节点等价的情况
  ```
  红黑树的实现是在我们实现的二分搜索树的基础上进行的, 比二分搜索树多了一步操作, 那就是在删除后调用了
  fixAfterDeletion方法来维持红黑树的性质, 该方法的参数是node(被删除的节点), replacement(被取代的节点),
  需要注意的是, 红黑树中删除一个节点, 该节点只会出现三种情况, node有一个左孩子, node有一个右孩子,
  node没有孩子, 因为对于node有两个孩子的情况, 我们在二分搜索树中都会转换成一个孩子的情况(找到后继),
  所以下面的分析其实就是对这三种情况进行细分而已
  ```

- 情况1: 删除的node为红色(直接返回即可, 没有破坏红黑树性质)
  ```
  则其一定没有孩子节点, 因为如果其有孩子节点, 假如是红色, 则不满足性质[红色节点的子节点必须是黑色],
  假如是黑色, 那么该节点到叶子节点经过的黑色节点的个数左右就不会一致了, 即不满足红黑树的性质, 对于该
  情况只需要直接返回即可, 因为删除该节点不会破坏红黑树的性质
  ```
- 情况2: node为黑色, 有孩子节点
  ```
  如果node为黑色, 有孩子节点, 则一定是只有一边有孩子节点, 因为对于node有两个孩子的情况, 我们在二分搜
  索树中都会转换成一个孩子的情况(找到后继), 并且这个孩子节点一定是红色的, 因为如果其是黑色的, 那么另
  一边由于没有节点, 则破坏了性质[任意节点到叶子节点经过的黑色节点的个数是一致的], 所以对于该情况, 为
  了在删除node后仍然维护红黑树的性质, 只需要将孩子节点染成黑色即可
  ```
- 情况3: node为黑色, 无孩子节点
  - sibling(兄弟节点)为黑色, 并且至少有一个红色孩子节点(可以向兄弟节点借一个元素的情况)
    <img src="photos\RBTree/删除1.png" />
    <img src="photos\RBTree/删除2.png" />
    <img src="photos\RBTree/删除3.png" />
  - sibling(兄弟节点)为黑色, 并且没有红色孩子节点(不能向兄弟节点借元素的情况)
    <img src="photos\RBTree/删除4.png" />
  - sibling(兄弟节点)为红色, 但是其一定会有两个黑色子节点(不然不满足红黑树的定义)
    <img src="photos\RBTree/删除5.png" />
## 删除代码实现
```java
// 删除一个元素
public void remove (E ele) {
  Node<E> node = node(ele);
  if ( node == null ) // 如果被删除的元素不存在, 直接返回即可
    return;

  remove( node );

  if ( root != null )
    root.color = BLACK;
}

// 删除一个节点, node为待删除节点
private void remove (Node<E> node) {
  // 如果该节点有两个孩子, 则需要用后继节点的值来代替当前节点的值
  if ( hasTwoChild( node ) ) {
    Node<E> s = successor(node);
    node.ele = s.ele;

    node = s; // 使得后继节点作为待删除的节点进行删除
  }

  // 到了这一步, node一定是一个度为1或者度为0的节点(同时也说明不为空), 因为其是后继节点
  // 那么根据我们删除度为1节点的逻辑, 应该将该节点的左节点或者右节点拼接到父节点上
  // replacement则是代替被删除节点去占据父亲节点的
  Node<E> replacement = node.left == null ? node.right : node.left;
  Node<E> parent = node.parent;

  // 此时需要判断replacement应该取代parent的left还是right
  // 同时维护replacement的parent属性, 所有需要判断replacement是否为空
  if ( replacement != null ) {
    replacement.parent = parent;

    // 只有当parent不为空的情况下才去判断将replacement放在parent的left还是right
    if ( parent != null ) {
      int cmp = compare( parent.ele, node.ele );

      if ( cmp > 0 ) {
        parent.left = replacement;
      } else {
        parent.right = replacement;
      }
    } else { // parent为空, 则replacement成为新的根节点
      root = replacement;
    }

  } else {
    if ( parent != null ) {
      int cmp = compare( parent.ele, node.ele );

      if ( cmp > 0 ) {
        parent.left = null;
      } else {
        parent.right = null;
      }
    } else {
      root = null;
    }
  }

  // node一定不为空
  fixAfterDeletion( node, replacement );
}

private void fixAfterDeletion (Node<E> node, Node<E> replacement) {
  // 需要提前注意的是: 此时node是已经被删除了的, 而replacement的父亲也是已经发生了改变的
  // 但是node的左右仍然指向的是replacement, 并且node的parent仍然是其原来的parent

  /*
    由于我们已经在删除操作中对两个孩子节点的情况进行了处理, 那么到了这一步
    node一定是一个度为1或者度为0的节点, 如果这个node为红色, 那么其一定是
    一个度为0的节点, 因为在红黑树中, 度为1的红色节点是不存在的, 因为红色节点
    的孩子节点一定是黑色的, 那么如果该红色节点有黑色子节点, 那么其另一边没有
    孩子节点则是不符合红黑树的性质[从任意节点到叶子节点经过的黑色节点的个数相同]

    结论: node为红色节点, 则其一定是无孩子的叶子节点, 删除它不用做任何操作也能满足
          红黑树的性质
    */
  if ( isRed( node ) )
    return;

  /*
    删除的节点node一定是一个黑色节点, 并且最多只有一边有孩子, 因为两边都有孩子的情况会
    在remove操作中被转换为只有一边有孩子的情况, 关于这个node有两种情况:

    只有一个红色孩子节点:
        如果该边还有黑色节点则是不满足红黑树的性质[从任意节点到叶子节点经过的黑色节点的个数相同]
        如果该边有两个红色节点也是不满足红黑树的性质
        对于该种情况, 只需要将其孩子节点设置为黑色就好了, 即replacement设置为黑色, 但是在判断时
        我们尽量用颜色去判断, 即replacement为红色的情况

    没有孩子节点: 在下面进行说明
    */
  if ( isRed( replacement ) ) {
    color( replacement, BLACK );
    return;
  }


  /* 没有孩子节点的情况(node为黑色), 我们需要对node进行一下左右判断, 因为node在不同的边则之后维护平衡的旋转操作是不同的 */

  // 处理一下特殊的情况, 即node为根节点的情况

  if ( node.parent == null )
    return;

  /*
    node在parent的左边还是右边, 这里不能通过parent.left == node来判断
    因为在删除操作的时候, parent指向node的方向已经修改成了null, 所以可以
    利用parent的两边中哪一边是null则可以认为是node原来的位置, 原因是另一边
    一定不为null, 如果为null, 那么[parent -> node(黑) -> 叶子节点]与
    [parent -> node的另一边null]就不满足红黑树的定义了, 对于在左边和在右边的
    代码, 刚好是对称的, 我们先对在右边的情况进行代码的编写, 然后将右边的代码复制
    到左边, 将left-right方向互换就可以了
    */
  Node<E> parent = node.parent; // 父节点
  // boolean left = parent.left == null; // version1: 被删除节点的方向, true为在左边
  boolean left = parent.left == null || parent.left == node; // version2: 被删除节点的方向, true为在左边
  Node<E> sibling = left ? parent.right : parent.left;  // 被删除节点在删除前的兄弟节点, 应该通过left以及parent来判断

  if ( left ) {
    // 不添加注释, 与else对称, 只是将left -> right对换而已
    if ( isRed( sibling ) ) {
      color( sibling, BLACK );
      color( parent, RED );
      leftRotate( parent );
      sibling = parent.right; // todo
    }

    if ( isBlack( sibling.left ) && isBlack( sibling.right ) ) {
      boolean parentBlack = parent.color == BLACK;
      color( parent, BLACK );
      color( sibling, RED );

      if ( parentBlack )
        fixAfterDeletion( parent, null );
    } else {
      if ( isBlack( sibling.right ) ) { // LR
        sibling = rightRotate( sibling );
      }
      color( sibling, parent.color );
      color( parent, BLACK );
      color( sibling.right, BLACK );
      parent = leftRotate( parent );
    }
  } else {
    // 处理兄弟节点为红色的情况, 此时需要将parent进行右旋转, 使得兄弟节点的孩子节点与node成为兄弟
    if ( isRed( sibling ) ) {
      color( sibling, BLACK );
      color( parent, RED );
      rightRotate( parent );
      sibling = parent.left;
    }

    // 兄弟节点为黑色的情况, 需要判断兄弟节点能否借元素
    // 当兄弟节点没有一个红色子节点的时候, 则是不能借的情况
    if ( isBlack( sibling.left ) && isBlack( sibling.right ) ) {
      // 根据B树的下溢, 父亲节点应该向下合并, 则将父亲节点的颜色设为黑色, 然后将兄弟节点设置为红色
      boolean parentBlack = parent.color == BLACK;
      color( parent, BLACK );
      color( sibling, RED );

      if ( parentBlack )
        fixAfterDeletion( parent, null );

    } else { // 兄弟节点至少有一个红色子节点
      if ( isBlack( sibling.left ) ) { // LR
        sibling = leftRotate( sibling );
      }
      // LL, 兄弟节点的颜色必须设置为父亲节点的颜色, 因为父亲节点可能为红色也可能为黑色, 但旋转操作都是一样的
      color( sibling, parent.color );
      color( parent, BLACK );
      color( sibling.left, BLACK );
      parent = rightRotate( parent );
    }
  }
}
```

## 删除操作注意点
```
<1> boolean left = parent.left == null || parent.left == node;
    left表示被删除节点的方向, true为在左边, 在正常的删除黑色叶子节点的操作中, 其父亲节点在另一边一
    定是还有黑色节点的, 否则不满足性质[从任意一个节点到叶子节点经过的黑色节点的个数相同], 所以我们
    对于parent.left判断为null是则表示被删除的节点node一定是在父节点的左边, 为什么还要加一个
    parent.left == node呢? 因为在父亲节点为黑色, 兄弟节点也为黑色, 兄弟节点没有可以借的节点的情况
    下, 即:
            黑(父)
        /           \
       黑(兄弟)     黑(被删除节点node)
    此时我们是让父亲节点向下合并的, 但是此时会造成父亲节点那个位置也下溢了, 所以递归调用了
    fixAfterDeletion方法来将parent节点当作被删除节点进行情况判断, 然而此时这个父亲节点是没有被删除
    的, 只不过是在B树下溢中这样描述而已, 所以需要通过parent.left == node来判断是否在上一层节点左边

<2> 以上图片的考虑情况都是基于被删除的节点node在右侧的情况, 对于node节点在左边的情况只需要将代码复
    制一份，将right-left互换，将leftRotate-rightRotate互换即可
```