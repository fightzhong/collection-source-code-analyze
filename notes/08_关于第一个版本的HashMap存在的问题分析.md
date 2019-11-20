## 问题引入
- 创建一个自定义类
```java
// 一个自定义类
class Key {
  private int value;

  public Key (int value) {
    this.value = value;
  }

  // 重写equals方法, 规定当value相同时则两个对象是相等的
  @Override
  public boolean equals (Object obj) {
    if ( obj == this ) return true;
    if ( obj == null && obj.getClass() != this.getClass() ) return false;

    Key key = (Key) obj;

    return value == key.value;
  }

  // 重写hashCode方法
  @Override
  public int hashCode () {
    return value / 5;
  }
}
```

- 问题引入-测试案例
```java
public static void main (String[] args) {
  HashMapV1<Key, Integer> map = new HashMapV1<>();

  for ( int i = 1; i < 5; i ++ ) {
    map.put( new Key( i ), i );
  }

  System.out.println( map.size() );

  for ( int i = 1; i < 5; i ++ ) {
    System.out.println( map.get( new Key( i ) ));
  }
}
```

## 问题分析
```
上面的案例中, 我们往自己写的map中放入了4个自定义对象, 这4个自定义对象的value是1-4的, 然后我们输出
了map的size, 发现确实是4个, 说明我们添加成功的, 继续查看自定义对象的hashCode方法, 可以得出4个对象
的hashCode都是相同的, 这意味着这4个对象会放入map底层数组的同一索引下, 然后形成一棵红黑树, 之后我们
又一次进行循环, 取出这4个对象对应的值, 通过结果可以发现, 竟然有一个是null, 这就是我们第一个版本的
compare方法引起的问题

问题具体代码执行分析:
  在我们执行代码: map.get( new Key( 1 ) ) 的时候, 首先其会执行node方法找到该key对应的节点, 在我们
  执行node方法的时候, 假设在该红黑树中的root为new Key(4), 此时需要对root和new Key(1)进行判断, 首
  先是hashCode判断, 由于两者的hashCode相同, 所以转向了equals判断, 由于两者的value不相同所以是false,
  进而转向是否实现了Comparable接口判断, 由于Key这个自定义类没有实现, 所以最终转向了内存地址判断,
  出现问题的也就是在这里, 假设我们插入的new Key(1)是在root的左边, 如果此时get方法传入的new Key(1)
  的内存地址比root大, 那么就会跑到右边去查找了, 从而一定不可能找到左边的插入的值, 所以会出现返回null
  的情况
```

## 结论
```
总结一下, 因为我们的红黑树需要对元素进行比较才能判断元素往哪边存放, 所以我们需要有一个compare方法,
compare中我们对于键key采用了hashCode => equals => Comparable => 内存地址的判断逻辑, 其实就是为了
对key进行一个比较而已, 当两个key不存在可比较性的时候我们只能采用内存地址判断了, 从而由于内存地址的
不确定性导致了上述问题的发生, 下一个版本的HashMap就是为了解决这个问题的
```
