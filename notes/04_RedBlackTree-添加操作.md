
## 添加情况分析
- 下面来说一下4阶B树与红黑树节点等价的情况
  ```
  <1> B树中, 新元素必定是添加到叶子节点中, 同样的, 我们红黑树的节点添加也是添加到叶子节点
  <2> 4阶B树所有节点的元素个数x都符合1 <= x <= 3
  <3> 建议新添加的节点默认是RED, 这样能够让红黑树的性质尽快得到满足(1,2,3,5满足, 4不一定), 可以对比
      着红黑树的五条性质进行查看, 红黑树的添加就是要对这5条性质进行维护
  <4> 如果添加的是根节点, 染成Black即可, 其实这一步很简单, 那就是在节点添加完成后, 即将退出函数时将
      根节点设置为黑色就好了, 这个之后我们会在代码中说明

  4阶B树中, 叶子节点一共有三种情况, 分别是该节点的元素个数为1个, 2个, 3个, 之前我们说过, 要让一棵红
  黑树与4阶B树进行等价的条件是: [Black节点和它的Red子节点融合在一起, 形成一个B树节点], 也就是说, 每
  个4阶B树中必须有一个黑色节点, 之后该黑色节点的左右子节点可以有, 也可以没有, 从而一棵4阶B树的三种情
  况就会分化成4种情况, 因为元素个数为2个的情况会被分成两种(左节点存在或者右节点存在), 如下图就是一棵
  B树与红黑树的等价
  ```

  <img src="photos\RBTree/添加1.png" />

- 4阶B树和红黑树在增加时的节点等价
  ```
  由上可得, 我们新增的元素是必须在叶子节点的, 如图一所示, 是一棵红黑树, 我们将该红黑树看成一棵四阶B树
  后, 新增节点在B树就有12种, 分别是上面四种情况的左右增加节点, 或者中间缝隙中增加, 如下图所示, 我用绿
  色的线标识的就是能够添加的情况, 接下来我们就是要对这12种情况进行分类了
  ```

  <img src="photos\RBTree/添加2.png" />

- 情况1: parent为black
  ```
  在我们之前的描述中, 我们知道当我们的添加的节点为红色的时候, 直接就满足了性质1,2,3,5, 但是不满足性质
  4, 而红黑树的添加就是要对这5条性质进行维护, 这样才能保证是红黑树, 也就是说我们只需要想办法维护性质4
  就好了, 对于父节点为black的情况, 直接就满足了性质4, 所以不必做任何的操作, 在12种情况中, 有四种情况
  是parent为black的, 如下图所示, 我们不必对这四种情况做任何的操作
  ```

  <img src="photos\RBTree/添加3.png" />

- 情况2: parent为red, 该情况会出现两个红色节点连在一起, 不满足红黑树的性质4, 如下图所示, 即除了上面4种情况的另外8种情况, 对于这8种情况, 我们需要一一说明

  <img src="photos\RBTree/添加4.png" />

  - 情况2.1: 四阶B树中, 未产生上溢的情况, 即元素个数在添加后仍然少于4个的情况, 换成红黑树来说, 其实就是parent为red, uncle不为red(null也为黑色), uncle指的是父亲节点的兄弟节点
    - 情况2.1.1: 在未产生上溢的情况下, LL\RR的情况, 如下图所示
      ```
      对于这种情况, 我们知道当我们添加完节点后, 需要保证的是仍然能够组成一个四阶B树的节点, 而对于
      红黑树来说, 要使得能够组成B树节点, 则是Black节点和它的Red子节点融合在一起, 形成一个B树节点,
      那么对于下图情况来说, 已经变成了一个black red red, 新增的red节点不是black节点的子节点了, 这
      时, 我们需要进行旋转, 进行左旋转或者右旋转, 然后对颜色进行维护, 并且在旋转时一定要维护parent
      属性,保证是red black red的情况, 这样才满足红黑树等价于四阶B树的情况
      ```

      <img src="photos\RBTree/添加5.png" />

    - 情况2.1.2: 在未产生上溢的情况下, LR\RL的情况, 如下图所示
      ```
      同上面的情况是类似的, 但是我们需要对这些情况进行两次旋转而已, 第一次旋转则将当前情况转换为
      2.1.1情况, 然后利用2.1.1情况进行处理
      ```

      <img src="photos\RBTree/添加6.png" />

  - 情况2.2: 四阶B树中, 产生上溢的情况, 换成红黑树来说, 其实就是parent为red, uncle不为red(null也为黑色), uncle指的是父亲节点的兄弟节点
    ```
    在四阶B树中产生上溢, 则需要将四阶B树的中心元素进行向上合并,并且该节点的左右两边的元素进行分离
    成两个B树节点, 需要注意的是, 将中心元素左右的元素分离出去之后, 需要对这些分离的元素保持红黑树到
    四阶B树转换的条件, 即Black节点和它的Red子节点融合在一起, 对于上溢的情况, 只需要将中心节点两边
    的节点的颜色转换为黑色就可以了, 然后将中心节点向上合并, 但是向上合并的过程仍然可能会造成上溢,
    所以如果上面的节点仍然是上溢的情况下, 那么我们可以这样来理解, 对于这个向上合并的元素, 我们把它
    看作为新增的元素, 这样对于上面的节点来说, 就可以同样进行上溢处理了, 即一个递归的过程, 对于一个
    新增的节点来说, 必然是红色的, 所以我们需要把向上合并的这个元素的颜色改为红色即可, 对于这
    ```
    <img src="photos\RBTree/添加7.png" />
    <img src="photos\RBTree/添加8.png" />
    <img src="photos\RBTree/添加9.png" />
    <img src="photos\RBTree/添加10.png" />


- 注意
  ```
  通过上述的分析, 我们对红黑树的增加情况进行总结, 一共有3大种情况, 下面用伪代码来说明一下:
  public void fixAfterInsertion (Node<E> node) {
    if ( node的父亲是黑色 ) {
      return; 不做任何操作
    }

    // 此时父亲一定是红色的
    if ( 叔叔是黑色 ) { // 叔叔是黑色, 说明不是上溢的情况
      // 不是上溢的情况又分为四种
      if ( LL ) {
        // 爷爷进行右旋转
      } else if ( LR ) {
        // 父亲先进行左旋转, 爷爷再进行右旋转
      } else if ( RR ) {
        // 爷爷进行左旋转
      } else if ( RL ) {
        // 父亲先进行右旋转, 爷爷再进行左旋转
      }

      // 旋转完成后, 此时父亲可能变成了爷爷的身份, 此时需要对父亲的parent的属性进行维护
    } else { // 叔叔是红色, 一定是上溢的情况
      // 在B树中需要将爷爷的左右孩子进行分离成独立的B树节点即可
      // 在红黑树中只需要将左右孩子的颜色设置为黑色, 爷爷设置为红色向上递归调用(爷爷作为上面节点新增加的节点)
    }
  }
  ```

- 注意2
  ```
  我们对红黑树的增加节点的逻辑跟二分搜索树的逻辑是一模一样的, 只不过在增加完成后, 需要调用
  fixAfterInsertion(node)方法对增加后的节点进行红黑树性质的维护而已
  ```


## Node节点的代码
```java
private static class Node<E> {
  E ele; // 用户往二叉树中添加的元素可以是任意类型(泛型指定)
  Node<E> left;  // 节点的左孩子
  Node<E> right; // 节点的右孩子
  Node<E> parent; // 节点的父亲节点
  int color = RED;

  public Node (E ele, Node<E> parent) {
    this.ele = ele;
    this.parent = parent;
  }
}
```

## 辅助代码
```java
// 判断一个节点是否是红色
private boolean isRed (Node<E> node) {
  return node != null && node.color == RED;
}

// 判断一个节点是否是黑色
private boolean isBlack (Node<E> node) {
  return node == null || node.color == BLACK;
}

// 将一个节点的颜色设置为传入的颜色
private void color (Node<E> node, int color) {
  assertNotNull( node );
  node.color = color;
}
```

## 左旋转&&右旋转&&颜色反转
```java
private Node<E> rightRotate (Node<E> node) {
  Node<E> left = node.left;
  node.left = left.right;
  left.right = node;

  // 旋转后parent发生了改变, 需要进行维护

  // node.parent的左右指向需要更改为left
  if ( node.parent == null ){
    root = left;
  } else {
    if ( node.parent.left == node ) {
      node.parent.left = left;
    } else {
      node.parent.right = left;
    }
  }

  left.parent = node.parent;
  node.parent = left;
  if ( node.left != null )
    node.left.parent = node;

  return left;
}

private Node<E> leftRotate (Node<E> node) {
  Node<E> right = node.right;
  node.right = right.left;
  right.left = node;

  // 旋转后parent发生了改变, 需要进行维护

  // node.parent的左右指向需要更改为node
  if ( node.parent == null ){
    root = right;
  } else {
    if ( node.parent.left == node ) {
      node.parent.left = right;
    } else {
      node.parent.right = right;
    }
  }

  right.parent = node.parent;
  node.parent = right;
  if ( node.right != null )
    node.right.parent = node;

  return right;
}

private void flipColor (Node<E> node) {
  node.left.color = RED;
  node.right.color = RED;
  node.color = BLACK;
}
```

## 添加代码实现
```java
// 增加一个元素
public void add (E ele) {
  assertNotNull( ele );

  // 根节点为空的情况
  if ( root == null ) {
    root = new Node( ele, null );
  } else {
    Node<E> parent = null;
    Node<E> curNode = root;
    int cmp = 0;

    while ( curNode != null ) {
      cmp = compare(curNode.ele, ele);
      parent = curNode;

      if ( cmp > 0 ) {
        curNode = curNode.left;
      } else if ( cmp < 0 ){
        curNode = curNode.right;
      } else {
        return;
      }
    }

    // 到了这一步, 必定是找到了空的节点, 同时维护了空的节点的父亲节点
    // 此时根据比较的结果判断将新的节点插入到父亲节点的左边还是右边
    Node<E> newNode = new Node<>( ele, parent );
    if ( cmp > 0 ) {
      parent.left = newNode;
    } else {
      parent.right = newNode;
    }

    // 维护红黑树的性质
    fixAfterInsertion( newNode );
  }

  // 维护根节点是黑色的
  root.color = BLACK;
}

// 红黑树的插入操作完成后, 需要维持红黑树的性质
private void fixAfterInsertion (Node<E> node) {
  // 如果父亲节点是黑色的, 那么就直接添加就可以了, 不用维持平衡
  if ( isBlack( node.parent ) ) {
    return;
  }

  // 父亲节点是红色的
  Node<E> parent = node.parent; // 父亲节点
  Node<E> grandParent = parent.parent; // 爷爷节点
  Node<E> sibling = isLeftChild( parent ) ? grandParent.right : grandParent.left; // 叔叔节点

  // 未发生上溢的情况, 即当前节点的叔叔节点是黑色的, 那么就说明没有和爷爷节点一起组成一个B树节点, 所以没有溢出
  if ( isBlack( sibling ) ) {
    if ( isLeftChild( node ) && isLeftChild( parent ) ) { // LL

      grandParent = rightRotate( grandParent );
      flipColor( grandParent );
    } else if ( isLeftChild( node ) && isRightChild( parent ) ) { // RL

      rightRotate( parent );
      grandParent = leftRotate( grandParent );
      flipColor( grandParent );
    } else if ( isRightChild( node ) && isRightChild( parent ) ) { // RR

      grandParent = leftRotate( grandParent );
      flipColor( grandParent );
    } else if ( isRightChild( node ) && isLeftChild( parent ) ) { // LR

      leftRotate( parent );
      grandParent = rightRotate( grandParent );
      flipColor( grandParent );
    }

  } else { // 发生上溢的情况
    color( sibling, BLACK );
    color( parent, BLACK );
    color( grandParent, RED );

    // 将当前grandParent当作上一层的新增节点, 继续判断是否在上一层会发生上溢的情况
    fixAfterInsertion( grandParent );
  }
}
```