package com.fightzhong.hashmap;

import java.util.Objects;

public class TestClass {
	public static void main (String[] args) {
		HashMapV2<Key, Integer> map = new HashMapV2<>();

		for ( int i = 1; i < 100; i ++ ) {
			map.put( new Key( i ), i );
		}
		
		System.out.println( map.size() );

		for ( int i = 1; i < 100; i ++ ) {
			System.out.println( map.get( new Key( i ) ));
		}
	}
}

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
		return value / 100;
	}
}
