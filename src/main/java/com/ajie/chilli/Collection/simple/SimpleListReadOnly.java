package com.ajie.chilli.Collection.simple;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 超级简单的只读list封装 基与ArrayList
 * 
 * @author niezhenjie
 */
public class SimpleListReadOnly<E> extends ArrayList<E> {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException("只读列表不支持添加");
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException("只读列表不支持添加");
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException("只读列表不支持添加");
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException("只读列表不支持添加");
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException("只读列表不支持添加");
	}

}
