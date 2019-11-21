# collection-source-code-analyze(Java集合类源代码分析)

> 对集合类源代码进行分析主要对增删改查操作的源代码进行分析, 因为这四个操作才是集合中最重要的, 其它的操作示情况进行分析, 分析的流程我采用的是自己仿照JDK的思想实现这些数据结构, 而不是直接对源码一句句解释意思, 因为JDK源码的命名过于简略, 不容易被理解, 但是通过我所实现的这些集合就能知道JDK中大概是怎么实现的了, 如果笔记做得有错误, 大家可以给我提issuses =,=

## 注意点一
```
本仓库为Java集合类进行源代码的分析笔记, 同时对于红黑树的实现从二分搜索树 => B树 => 4阶B树 => 红黑树
的思路进行了完整的描述和实现, 从而方便了对底层为红黑树的集合类进行说明, 在对集合类如(TreeMap)等进
行说明前, 需要对红黑树的实现代码能够看懂, 所以将红黑树的实现作为源码分析的开头曲, 先解决一个大家的
疑惑, 网上大部分的资料是通过2-3树来等价红黑树的, 但其实有些红黑树的情况不能概述, 我学习的资料中是采
用2-3-4树即4阶B树来等价的, 能够完整的等价红黑树的情况
```

## 注意点二
```
所有的源代码均存放在/src/main/java/com/fightzhong目录下, 所有的笔记均存放在/notes/下, 在集合源码
分析笔记完成之后会整理成gitbook的形式来方便阅读
```

## 注意点三
```
关于TreeMap的源码和TreeSet的源码就不进行分析了, 原因是TreeMap其实就是一棵红黑树, 如果把我写的红黑
树看懂了, 那么TreeMap就没问题了, TreeMap和我写的红黑树的差别在于, 我写的红黑树中节点是Node, 只存储
一个value, 而TreeMap的节点是Entry<K, V>, 即存放了键值对, 但是其在进行比较的时候是对key进行比较的,
所以即使增加一个V属性也没有影响, 而TreeSet就更简单了, 其里面就内置了一个TreeMap, 然后所有的操作都是
通过TreeMap实现的, 在put操作的时候, 其V会被默认填入Object对象
```

## 注意点四
```
HashMap的源码过于复杂, 我会自己先实现一个HashMap, 并通过不断的优化, 从而实现一个等价于JDK自带的
HashMap, 在我的实现中, 是仿照JDK的HashMap去实现的, 举个例子, JDK8的HashMap会采用链表加红黑树去实
现, 对于红黑树来说, 其元素必定是具备可比较性的, 而我们在日常使用HashMap的时候可以知道, 我们传入的
key有时候并不具备可比较性, 如一个自定义对象Parent, 那么在红黑树进行比较的时候, 我的做法是采用了JDK
中的做法的(比较hashCode => equals => Comparable => 内存地址), 所以我的做法是通过自己实现一个HashMap
来达到对JDK自带的HashMap有一个更加清晰的了解(需要注意的是, JDK中在比较内存地址之前还会利用类名进行
比较一次, 我的实现中省略了这一步, 不过效果都是类似的, 都是为了使得key能够比较出一个结果)
```

## 目录
  * 从零实现红黑树
    * [二分搜索树非递归实现(等同于TreeMap无红黑树操作的实现)](notes/01_BinarySearchTree.md)
    * [B树](notes/02_BTree.md)
    * [红黑树的描述以及一些工具的使用](notes/03_RedBlackTree-描述.md)
    * [红黑树的添加操作](notes/04_RedBlackTree-添加操作.md)
    * [红黑树的删除操作](notes/05_RedBlackTree-删除操作.md)
  * 聊聊HashMap
    * [HashMap前置基础知识](notes/06_HashMap前置基础知识.md)
    * [version1-HashMap](notes/07_实现第一个版本HashMap.md)
    * [version1-HashMap存在的问题分析](notes/08_关于第一个版本的HashMap存在的问题分析.md)
    * [version2-HashMap](notes/09_实现第二个版本的HashMap.md)
    * [version3-HashMap解决compareTo问题](notes/10_实现第三个版本的HashMap解决compareTo问题.md)
    * [version4-HashMap实现扩容机制](notes/11_实现第四个版本的HashMap实现扩容机制.md)

