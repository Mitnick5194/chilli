package com.ajie.chilli.Collection.simple;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 简单的只读list封装 基与ArrayList
 * 
 * @author niezhenjie
 */
public class SwitchUnmodifiableList<E> extends ArrayList<E> {

	private static final long serialVersionUID = 1L;

	/** 不可修改标识 , 默认为false，为true时标识列表只读 */
	protected boolean unmodifiable;

	/**
	 * 无参构造
	 */
	public SwitchUnmodifiableList() {
		super();
	}

	public SwitchUnmodifiableList(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * 通过传入的集合构造，并可以根据unmodifiable参数控制是否构造完成后立刻转换为只读
	 * 
	 * @param c
	 * @param unmodifiable
	 */
	public SwitchUnmodifiableList(Collection<? extends E> c, boolean unmodifiable) {
		super(c);
		if (unmodifiable)
			swithUnmodifiable();
	}

	public SwitchUnmodifiableList(Collection<? extends E> c) {
		this(c, false);
	}

	/**
	 * 只读转换
	 * 
	 * <p>
	 * 我要再提醒你一次。 调用这个方法之后，你就再也不是个凡人。<br>
	 * 人世间的增、删、改不能再沾半点， 否则你就会接收到一堆异常， 苦不堪言。<br>
	 * 在转换之前，你还有什么话要说。
	 *
	 * 
	 * 曾经，有一份优秀的源码摆在我面前，我没有珍惜，等我失去的时候我才后悔莫及<br>
	 * 人世间最痛苦的事莫过于此。 如果上天能够给我一个再来一次的机会，<br>
	 * 我会对那个作者说三个字：拷一份。 <br>
	 * 如果非要上加上一个范围，我希望是…… 完整的
	 * </p>
	 * 
	 */
	public void swithUnmodifiable() {
		unmodifiable = true;
	}

	public void assertModifiable() {
		if (unmodifiable)
			throw new UnsupportedOperationException(
					"hei man , what are you fucking doing , i am readonly you know");
	}

	@Override
	public boolean add(E e) {
		assertModifiable();
		return super.add(e);
	}

	@Override
	public void add(int index, E element) {
		assertModifiable();
		super.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		assertModifiable();
		return super.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		assertModifiable();
		return super.addAll(index, c);
	}

	@Override
	public E set(int index, E element) {
		assertModifiable();
		return super.set(index, element);
	}

	public static void main(String[] args) {
		SwitchUnmodifiableList<String> list = new SwitchUnmodifiableList<String>();
		list.add("a");
		list.add("b");
		list.add("c");
		System.out.println(list.size());
		list.swithUnmodifiable();
		list.set(1, "df");

	}

}
