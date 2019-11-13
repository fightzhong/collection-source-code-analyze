## 一些小的概念
```
前驱节点: 一个节点的前驱节点是在该二叉树中序遍历时在该节点的前一个节点, 如果是二叉搜索树, 前驱节点就是前一个比它小的节点
后继节点: 一个节点的后继节点是在该二叉树中序遍历时在该节点的后一个节点, 如果是二叉搜索树, 后继节点就是后一个比它大的节点

度为0的节点node: 表示node节点的没有一个孩子节点
度为1的节点node: 表示node节点有一个孩子节点, 可以是左节点也可以是右节点
度为2的节点node: 表示node节点有两个孩子节点

由于我们的最终目的是为了实现红黑树, 所以这个二分搜索树的实现是基于TreeMap中红黑树中的实现中进行模拟
的, 在网上有很多实现二分搜索树的方式, 并且在我的github的data-structure-implement-by-java仓库中
也对二分搜索树利用进行了完整的实现, 在本章节主要利用非递归的方式实现二分搜索树的增加和删除操作, 为
后面红黑树的实现打下基础先, 并且不维护其它变量了, 如size
```

## Node节点类
```java
private static class Node<E> {
  E ele; // 用户往二叉树中添加的元素可以是任意类型(泛型指定)
  Node<E> left;  // 节点的左孩子
  Node<E> right; // 节点的右孩子
  Node<E> parent; // 节点的父亲节点

  public Node (E ele, Node<E> parent) {
    this.ele = ele;
    this.parent = parent;
  }
}
```

## 节点之间比较的实现
````java
二分搜索树中节点必须是可以比较的, 所以我们需要构造一个int compare(Node<E> n1, Node<E> n2)方法来
对两个节点进行比较, 为了能够更加的灵活, 我们可以允许用户在构造器传入一个Comparator实现类来规定两个
元素如何进行比较, 然后在compare方法中, 如果节点中的元素是Comparable接口的实现类, 则优先采用该接口
的compareTo方法进行两个节点的比较, 如果不是则采用cmp来进行比较, 如果既不是Comparable接口的实现类,
在构造二分搜索树的时候也没有传入一个Comparator接口的实现类, 则抛出异常:

private Comparator<E> cmp; // 用户可以传入一个比较器来比较两个元素

public BST () {
  this( null );
}

// 用户可以传入一个比较器来比较两个元素
public BST (Comparator<E> cmp) {
  this.cmp = cmp;
}

// 比较两个元素, 我们规定: 如果返回值大于0, 则ele1 > ele2, 小于0, 则ele1 < ele2, 等于0, 则ele1 == ele2
private int compare (E ele1, E ele2) {
  assertNotNull( ele1 );
  assertNotNull( ele2 );

  if ( ele1 instanceof Comparable ) {
    return ( ( Comparable<E> ) ele1 ).compareTo( ele2 );
  } else if ( cmp != null ) {
    return cmp.compare( ele1, ele2 );
  } else {
    throw new IllegalStateException( "元素必须实现Comparable接口或传入一个Comparator接口实现类" );
  }
}
````

## 增加操作
- 逻辑分析
  ```
  增加一个新的节点node一定是在叶子节点上进行增加的, 我们从根节点出发, 如果比新增节点node的值小则向
  右查找, 如果比新增节点node的值大则向左查找, 直到找到null的节点后将新增加的节点放在null的位置, 并
  指定其parent为上一层节点
  ```

- 代码实现
  ```java
  // 增加一个元素
	public void add (E ele) {
		assertNotNull( ele );

		// 根节点为空的情况
		if ( root == null ) {
			root = new Node( ele, null );
			return;
		}

		// 根节点不为空的情况, 需要通过比较找到ele应该插入的位置
		// 同时在找的过程中需要维护父节点, 因为新增节点的时候需要传入一个父节点
		Node<E> parent = null;
		Node<E> curNode = root;
		int cmp = 0; // 比较的结果可以赋予一个任意的默认值, 因为curNode一定不为空, 所以一定会进入循环

		while ( curNode != null ) {
			cmp = compare(curNode.ele, ele);
			parent = curNode;

			if ( cmp > 0 ) { // 当前元素 > 新增的元素, 应该在当前元素的左边添加
				curNode = curNode.left;
			} else if ( cmp < 0 ){ // 当前元素 < 新增的元素, 应该在当前元素的右边添加
				curNode = curNode.right;
			} else { // 当前元素 == 新增的元素, 不执行添加逻辑, 直接返回
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
	}
  ```

## 前驱
```
前驱: 一个节点n1的前驱节点是在该二叉树中序遍历时在该节点的前一个节点, 有两种情况:
    <1> n1的左孩子不为空的情况下, 只需要n1.left.right.right....就可以找到了
    <2> n1的左孩子为空的情况下, 那么就要往上查找, 如果一个祖先节点node的右子树中存在n1, 那么这个
        node节点就一定为n1的前驱, 因为其是在中序遍历时比n1最小的所有节点中最大的那个节点

```

## 后继
```
后继: 一个节点的后继节点是在该二叉树中序遍历时在该节点的后一个节点, 有两种情况:
    <1> n1的右孩子不为空的情况下, 只需要n1.right.left.left....就可以找到了
    <2> n1的右孩子为空的情况下, 那么就要往上查找, 如果一个祖先节点node的左子树中存在n1, 那么这个
        node节点就一定为n1的后继, 因为其是在中序遍历时比n1最大的所有节点中最小的那个节点
```

## 获取一个元素对应在BST中的节点
```java
// 获取一个元素对应的节点
private Node<E> node (E ele) {
  Node<E> node = root;
  while ( node != null ) {
    int cmp = compare( ele, node.ele );

    if ( cmp > 0 ) {
      node = node.right;
    } else if ( cmp < 0 ) {
      node = node.left;
    } else {
      return node;
    }
  }

  return null;
}
```

## 删除操作
- 逻辑分析
  ```
  删除分为四种情况:
  <1> 删除的节点n1有一个左孩子n2没有右孩子(度为1的情况):
          该情况只需要将左孩子n2直接顶替在被删除节点n1在其父亲节点的位置(左或者右), 举个例子:
          父亲节点parent, parent.left = n1, n1.left = n2; 如果此时删除n1, 则parent.left = n2
          即可, 然后再维护一下n2的parent

  <2> 删除的节点n1有一个右孩子n2没有左孩子(度为1的情况)
          该情况只需要将右孩子n2直接顶替在被删除节点n1在其父亲节点的位置(左或者右), 举个例子:
          父亲节点parent, parent.right = n1, n1.left = n2; 如果此时删除n1, 则parent.right = n2
          即可, 然后再维护一下n2的parent

  <3> 删除的节点有两个孩子(度为2的情况)
          当我们删除一个节点n1的时候, 需要找到该节点的前驱n2或者后继n2, 在本次实现中, 我们选择找
          到后继(其实之后的逻辑是一样的, 所以选择哪个并不重要), 然后将后继节点n2的元素的值赋予给
          需要被删除的节点n1, 然后再将后继节点n2删除即可, 需要注意的是, 此时这个n2一定是一个度为1
          的情况, 因为如果不是度为1的情况的话, 其一定还有后继, 所以情况又回到了<1><2>对应的了
  <4> 删除的节点是叶子节点, 没有左右孩子(度为0的情况):
          此时只需要将该节点在父节点的指向中设置为null即可
  ```

- 代码实现
  ```java
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
	}
  ```















