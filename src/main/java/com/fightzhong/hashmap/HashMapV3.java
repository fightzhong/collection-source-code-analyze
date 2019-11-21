package com.fightzhong.hashmap;

import java.util.Objects;

@SuppressWarnings( "unchecked" )
public class HashMapV3<K, V> {
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
			this.hashCode = key == null ? 0 : key.hashCode();
		}
	}

	/*----------------------------------构造器--------------------------------------*/
	public HashMapV3 () {
		this( DEFAULT_INITIAL_CAPACITY );
	}

	public HashMapV3 (int initialCapacity) {
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

			K key1 = key;
			int hashCode1 = key == null ? 0 : key.hashCode();
			while ( curNode != null ) {
				K key2 = curNode.key;
				int hashCode2 = curNode.hashCode;

				if ( hashCode1 > hashCode2 ) {
					cmp = 1;
				} else if ( hashCode1 < hashCode2 ) {
					cmp = -1;
				} else { // hashCode1 == hashCode2
					if ( Objects.equals( key1, key2 ) ) { // 相等则需要将新值替换旧值
						cmp = 0;
					} else { // 不相等
						// 具有可比较性
						if ( key1 != null && key2 != null
								&& key1.getClass() == key2.getClass()
								&& key1 instanceof Comparable
								&& ((Comparable) key1).compareTo( key2 ) != 0 ) {
							cmp = ((Comparable) key1).compareTo( key2 );
						} else {
							// 不具有可比较性, 去curNode所在的子树递归查找是否存在该元素
							Node<K, V> target = node( curNode, key );

							if ( target == null ) { // 不存在, 才利用内存地址去判断往哪边添加
								cmp = System.identityHashCode( key1 ) - System.identityHashCode( key2 );
							} else { // 存在, 则cmp应该为0, 之后可以替换该值
								cmp = 0;
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

	// 删除一个元素
	public V remove (K key) {
		Node<K, V> node = node(key);
		if ( node == null ) // 如果被删除的元素不存在, 直接返回即可
			return null;

		remove( node );

		if ( table[index( key )] != null )
			table[index( key )].color = BLACK;

		return node.val;
	}

	private void remove (Node<K, V> node) {

		if ( hasTwoChild( node ) ) {
			Node<K, V> s = successor(node);
			node.val = s.val;
			node.hashCode = s.hashCode;
			node.key = s.key;

			node = s; // 使得后继节点作为待删除的节点进行删除
		}

		Node<K, V> replacement = node.left == null ? node.right : node.left;
		Node<K, V> parent = node.parent;

		if ( replacement != null ) {
			replacement.parent = parent;

			// 只有当parent不为空的情况下才去判断将replacement放在parent的left还是right
			if ( parent != null ) {
				int cmp = compare( parent.key, node.key );

				if ( cmp > 0 ) {
					parent.left = replacement;
				} else {
					parent.right = replacement;
				}
			} else { // parent为空, 则replacement成为新的根节点
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
		size --;
	}

	public int compare (K key1, K key2) {
		int cmp = 0;
		int hashCode1 = key1.hashCode();
		int hashCode2 = key2.hashCode();

		if ( hashCode1 > hashCode2 ) {
			cmp = 1;
		} else if ( hashCode1 < hashCode2 ) {
			cmp = -1;
		} else { // hashCode1 == hashCode2
			if ( Objects.equals( key1, key2 ) ) {
				cmp = 0;
			} else {
				if ( key1 != null && key2 != null
					&& key1.getClass() == key2.getClass()
					&& key1 instanceof Comparable
					&& ((Comparable) key1).compareTo( key2 ) != 0) {
					cmp = ((Comparable) key1).compareTo( key2 );
				} else {
					cmp = System.identityHashCode( key1 ) - System.identityHashCode( key2 );
				}
			}
		}

		return cmp;
	}

	public V get (K key) {
		Node<K, V> node = node(key);
		return node == null ? null : node.val;
	}

	private Node<K, V> node (K key) {
		int index = index( key );
		Node<K, V> root = table[index];

		if ( root == null )
			return null;

		return node( root, key );
	}

	private Node<K, V> node (Node<K, V> node, K key) {
		/*
			key1: 需要被查找的key
			hashCode1: 需要被查找的key对应的哈希值

			key2: 当前比较的key
			hashCode1: 当前比较的key对应的哈希值
		*/
		K key1 = key;
		int hashCode1 = key == null ? 0 : key.hashCode();

		while ( node != null ) {
			K key2 = node.key;
			int hashCode2 = node.hashCode;

			int cmp = 0; // 比较的结果值
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
					&& key1 instanceof Comparable
					&& ((Comparable) key1).compareTo( key2 ) != 0 ) { // 具备可比较性
					cmp = ((Comparable) key1).compareTo( key2 );
				} else {
					// 不具备可比较性, 则去当前节点的左边和右边进行查找
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

	/*----------------------------------红黑树相关操作--------------------------------------*/
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

			} else { // 兄弟节点至少有一个红色子节点
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
