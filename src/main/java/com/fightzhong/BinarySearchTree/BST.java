package com.fightzhong.BinarySearchTree;

import com.fightzhong.printer.BinaryTreeInfo;

import java.util.Comparator;

public class BST<E> implements BinaryTreeInfo {
	private Node<E> root; // 根节点
	private Comparator<E> cmp; // 用户可以传入一个比较器来比较两个元素


	public BST () {
		this( null );
	}

	public BST (Comparator<E> cmp) {
		this.cmp = cmp;
	}

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

	// 删除一个元素
	public void remove (E ele) {
		Node<E> node = node(ele);
		if ( node == null ) // 如果被删除的元素不存在, 直接返回即可
			return;

		remove( node );
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
		return ( (Node<E>)node ).ele;
	}

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
}
