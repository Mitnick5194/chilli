package com.ajie.chilli.collection;

/**
 * 列表辅助工厂
 *
 * @author niezhenjie
 *
 */
public final class CollectionFactory {

	private CollectionFactory() {

	}

	public static <E> SwitchUnmodifiableList<E> createUnmodifiableList() {
		return new SwitchUnmodifiableList<E>();
	}
}
