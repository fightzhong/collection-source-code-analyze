package com.fightzhong.hashmap;

import java.util.Objects;

@SuppressWarnings( "unchecked" )
public class HashMapV1<K, V> {
	private static int RED = 1;
	private static int BLACK = 0;
	private static int DEFAULT_INITIAL_CAPACITY = 1 << 4; // 16, 哈希表默认的数组长度

	private int size;
	private Node<K, V>[] table;

	/*----------------------------------Node节点类--------------------------------------*/
	private static class Node<K, V> {
		K key;
		V val;
		int color = RED;
		int hashCode;
		Node<K, V> left;
		Node<K, V> right;
		Node<K, V> parent;

		public Node (K key, V val,  Node<K, V> parent) {
			this.key = key;
			this.val = val;
			this.parent = parent;
			this.hashCode = key.hashCode();
		}
	}

	/*----------------------------------构造器--------------------------------------*/
	public HashMapV1 () {
		this( DEFAULT_INITIAL_CAPACITY );
	}

	public HashMapV1 (int initialCapacity) {
		this.table = new Node[initialCapacity];
	}

	/*----------------------------------核心方法--------------------------------------*/
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

	public V get (K key) {
		Node<K, V> node = node(key);
		return node == null ? null : node.val;
	}

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

	// 删除一个元素
	public void remove (K key) {
		Node<K, V> node = node( key );
		if ( node == null ) // 如果被删除的元素不存在, 直接返回即可
			return;

		remove( node );

		int index = index( key );
		if ( table[index] != null )
			table[index].color = BLACK;
	}

	// 删除一个节点, node为待删除节点
	private void remove (Node<K, V> node) {
		if ( hasTwoChild( node ) ) {
			Node<K, V> s = successor(node);
			node.key = s.key;

			node = s; // 使得后继节点作为待删除的节点进行删除
		}

		Node<K, V> replacement = node.left == null ? node.right : node.left;
		Node<K, V> parent = node.parent;

		if ( replacement != null ) {
			replacement.parent = parent;

			if ( parent != null ) {
				int cmp = compare( parent.key, node.key );

				if ( cmp > 0 ) {
					parent.left = replacement;
				} else {
					parent.right = replacement;
				}
			} else {
				table[index( node.key )] = replacement;
			}

		} else {
			if ( parent != null ) {
				int cmp = compare( parent.key, node.key );

				if ( cmp > 0 ) {
					parent.left = null;
				} else {
					parent.right = null;
				}
			} else {
				table[index( node.key )] = null;
			}
		}

		// node一定不为空
		fixAfterDeletion( node, replacement );
	}

	/*----------------------------------红黑树相关操作--------------------------------------*/
	private void fixAfterDeletion (Node<K, V> node, Node<K, V> replacement) {
		if ( isRed( node ) )
			return;

		if ( isRed( replacement ) ) {
			color( replacement, BLACK );
			return;
		}

		if ( node.parent == null )
			return;

		Node<K, V> parent = node.parent; // 父节点
		boolean left = parent.left == null || parent.left == node; // version2: 被删除节点的方向, true为在左边
		Node<K, V> sibling = left ? parent.right : parent.left;  // 被删除节点在删除前的兄弟节点, 应该通过left以及parent来判断

		if ( left ) {
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
			if ( isRed( sibling ) ) {
				color( sibling, BLACK );
				color( parent, RED );
				rightRotate( parent );
				sibling = parent.left;
			}

			if ( isBlack( sibling.left ) && isBlack( sibling.right ) ) {
				boolean parentBlack = parent.color == BLACK;
				color( parent, BLACK );
				color( sibling, RED );

				if ( parentBlack )
					fixAfterDeletion( parent, null );

			} else {
				if ( isBlack( sibling.left ) ) { // LR
					sibling = leftRotate( sibling );
				}
				color( sibling, parent.color );
				color( parent, BLACK );
				color( sibling.left, BLACK );
				parent = rightRotate( parent );
			}
		}
	}

	// 红黑树的插入操作完成后, 需要维持红黑树的性质
	private void fixAfterInsertion (Node<K, V> node) {
		// 如果父亲节点是黑色的, 那么就直接添加就可以了, 不用维持平衡
		if ( isBlack( node.parent ) ) {
			return;
		}

		// 父亲节点是红色的
		Node<K, V> parent = node.parent; // 父亲节点
		Node<K, V> grandParent = parent.parent; // 爷爷节点
		Node<K, V> sibling = isLeftChild( parent ) ? grandParent.right : grandParent.left; // 叔叔节点

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

	private Node<K, V> rightRotate (Node<K, V> node) {
		Node<K, V> left = node.left;
		node.left = left.right;
		left.right = node;

		// 旋转后parent发生了改变, 需要进行维护

		// node.parent的左右指向需要更改为left
		if ( node.parent == null ){
			table[index(node.key)] = left;
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

	private Node<K, V> leftRotate (Node<K, V> node) {
		Node<K, V> right = node.right;
		node.right = right.left;
		right.left = node;

		// 旋转后parent发生了改变, 需要进行维护

		// node.parent的左右指向需要更改为node
		if ( node.parent == null ){
			table[index(node.key)] = right;
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

	private void flipColor (Node<K, V> node) {
		node.left.color = RED;
		node.right.color = RED;
		node.color = BLACK;
	}

	// 将一个节点的颜色设置为传入的颜色
	private void color (Node<K, V> node, int color) {
		node.color = color;
	}

	private boolean isBlack (Node<K, V> node) {
		return node == null || node.color == BLACK;
	}

	private boolean isRed (Node<K, V> node) {
		return node != null && node.color == RED;
	}

	// 判断一个节点是否是度为2的节点
	private boolean hasTwoChild (Node<K, V> node) {
		return node.left != null && node.right != null;
	}

	// 获取一个节点的后继
	private Node<K, V> successor (Node<K, V> node) {

		if ( node.right != null ) {
			Node<K, V> p = node.right;
			while ( p.left != null ) {
				p = p.left;
			}

			return p;
		} else {
			Node<K, V> parent = node.parent;
			Node<K, V> curNode = node;

			while ( parent != null && parent.left != curNode ) {
				curNode = parent;
				parent = curNode.parent;
			}

			return parent;
		}
	}

	private boolean isLeftChild (Node<K, V> node) {

		Node<K, V> parent = node.parent;
		if ( parent == null )
			throw new IllegalStateException( "根节点, 不存在父亲节点" );

		return parent.left == node;
	}

	private boolean isRightChild (Node<K, V> node) {
		Node<K, V> parent = node.parent;
		if ( parent == null )
			throw new IllegalStateException( "根节点, 不存在父亲节点" );

		return parent.right == node;
	}

	/**
	 * 比较的规则:
	 *   <1> 先通过hashCode进行比较, 在同一个索引下的所有红黑树节点的哈希值可能是会不一样的, 所以利用
	 *       hashCode也能先对两个key进行一下比较
	 *   <2> 如果hashCode相等, 那也没办法进行比较了, 此时我们需要先看一下两个key是否equals, 如果equals
	 *       那么就说明两个key是本身就是相等的, 此时可以直接将新的值覆盖原来的值, 在比较equals的时候, 采
	 *       用Objects提供的equals方法进行比较,该方法进行了一定的封装(因为两个key相等可以是内存地址相等
	 *       或者重写后的equals方法进行判断后相等)
	 *   <3> 如果equals不相等, 则此时如果这两个key是同一种类型(getClass的值相等), 并且该类型实现了Comparable
	 *       接口, 那么我们就可以直接利用这个该类的compareTo进行比较
	 *   <4> 如果上面还不满足, 即两个key不具备可比较性, 此时我们采用内存地址进行比较
	 */
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

	// 获取key对应在哈希表中的索引
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

	/*----------------------------------简单的方法--------------------------------------*/
	public int size () {
		return size;
	}

	public boolean isEmpty () {
		return size == 0;
	}
}
