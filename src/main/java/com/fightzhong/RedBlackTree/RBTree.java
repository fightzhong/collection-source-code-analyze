package com.fightzhong.RedBlackTree;

import com.fightzhong.printer.BinaryTreeInfo;

import java.util.Comparator;

public class RBTree<E> implements BinaryTreeInfo {
	private Node<E> root; // 根节点
	private Comparator<E> cmp; // 用户可以传入一个比较器来比较两个元素
	private static int RED = 1;
	private static int BLACK = 0;

	public RBTree () {
		this( null );
	}

	public RBTree (Comparator<E> cmp) {
		this.cmp = cmp;
	}

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

	// 将一个节点的颜色设置为传入的颜色
	private void color (Node<E> node, int color) {
		assertNotNull( node );
		node.color = color;
	}

	private boolean isRed (Node<E> node) {
		return node != null && node.color == RED;
	}

	private boolean isBlack (Node<E> node) {
		return node == null || node.color == BLACK;
	}

	private boolean isLeftChild (Node<E> node) {
		assertNotNull( node );

		Node<E> parent = node.parent;
		if ( parent == null )
			throw new IllegalStateException( "根节点, 不存在父亲节点" );

		return parent.left == node;
	}

	private boolean isRightChild (Node<E> node) {
		Node<E> parent = node.parent;
		if ( parent == null )
			throw new IllegalStateException( "根节点, 不存在父亲节点" );

		return parent.right == node;
	}

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

	// 判断一个节点是否是度为2的节点
	private boolean hasTwoChild (Node<E> node) {
		return node.left != null && node.right != null;
	}

	// 获取一个节点的前驱
	private Node<E> predecessor (Node<E> node) {
		assertNotNull( node );

		if ( node.left != null ) { // 左孩子不为空的情况, 则一直node.left.right.right...即可
			Node<E> p = node.left;
			while ( p.right != null ) {
				p = p.right;
			}

			return p;
		} else { // 左孩子为空的情况, 往上查找, 直到当前节点是parent的右孩子位置
			Node<E> parent = node.parent;
			Node<E> curNode = node;

			// 如果parent为空, 则说明找到了根节点, 直接返回null, 即没有前驱
			while ( parent != null && parent.right != curNode ) {
				curNode = parent;
				parent = curNode.parent;
			}

			return parent;
		}
	}

	// 获取一个节点的后继
	private Node<E> successor (Node<E> node) {
		assertNotNull( node );

		if ( node.right != null ) { // 右孩子不为空的情况, 则一直node.right.left.left...即可
			Node<E> p = node.right;
			while ( p.left != null ) {
				p = p.left;
			}

			return p;
		} else { // 右孩子为空的情况, 往上查找, 直到当前节点是parent的左孩子位置
			Node<E> parent = node.parent;
			Node<E> curNode = node;

			// 如果parent为空, 则说明找到了根节点, 直接返回null, 即没有前驱
			while ( parent != null && parent.left != curNode ) {
				curNode = parent;
				parent = curNode.parent;
			}

			return parent;
		}
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
			throw new IllegalStateException( "插入的元素必须实现Comparable接口或者在构造方法中传入一个Comparator接口实现类" );
		}
	}

	// 如果传入的对象为空, 则抛出一个异常
	private void assertNotNull (Object obj) {
		if ( obj == null )
			throw new NullPointerException();
	}

	@Override
	public Object root () {
		return root;
	}

	@Override
	public Object left (Object node) {
		return ( (Node<E>)node ).left;
	}

	@Override
	public Object right (Object node) {
		return ( (Node<E>)node ).right;
	}

	@Override
	public Object string (Object node) {
		Node<E> n = ( (Node<E>)node );
		return (n.color == RED ? n.ele + "_R" : n.ele);
		// return (n.color == RED ? n.ele + "_R" : n.ele) + "_" + ( n.parent == null ? null : n.parent.ele );
		// return n.ele + "_" + ( n.parent == null ? null : n.parent.ele );
	}

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
}
