package com.ajie.chilli.collection.utils;

import java.util.AbstractList;
import java.util.List;

/**
 * 列表由V到E的转换，因为继承了AbstractList，所以返回值可以使用List接受
 * 
 * @author niezhenjie
 * 
 */
public abstract class TransList<E, V> extends AbstractList<E> {

	protected List<V> v;

	public TransList(List<V> v) {
		if (null != v) {
			this.v = v;
		}
	}

	/**
	 * 在遍历调用get时 最终返回的是E 从而实现了转换
	 * 
	 * @param v
	 * @return
	 */
	public abstract E trans(V v);

	@Override
	public E get(int index) {
		return trans(v.get(index));
	}

	@Override
	public int size() {
		return v.size();
	}

}