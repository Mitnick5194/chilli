package com.ajie.chilli.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.ajie.chilli.support.impl.ListPageData;

/**
 * 分页数据构造提供
 * 
 * @author niezhenjie
 */
public class PageDatas {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <E> PageData<E> toPageData(List<E> data) {
		if (null == data) {
			data = Collections.emptyList();
		}
		return new ListPageData(data);
	}

	public static final Empty<?> EMPTY = new Empty<Object>();

	@SuppressWarnings("unchecked")
	public static <E> PageData<E> empty() {
		return (PageData<E>) EMPTY;
	}

	public static class Empty<E> implements PageData<E> {

		@Override
		public Iterator<E> iterator() {
			return null;
		}

		@Override
		public int getPage() {
			return 0;
		}

		@Override
		public int getPageSize() {
			return 0;
		}

		@Override
		public int getCount() {
			return 0;
		}

		@Override
		public int getPageCount() {
			return 0;
		}

		@Override
		public boolean gotoPage(int page) {
			return false;
		}

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public E next() {
			return null;
		}

		@Override
		public void setPageSize(int pageSize) {

		}

		@Override
		public Iterable<E> toForeach() {
			return this;
		}

	}

	public static void main(String[] args) {
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");
		list.add("e");
		PageData<String> page = PageDatas.toPageData(list);
		page.setPageSize(2);
		System.out.println(page.getPage() + ";" + page.getPageCount() + ";"
				+ page.getPageSize());
		page.gotoPage(1);
		int i = 1;
		while (page.gotoPage(i++)) {
			System.out.println("当前页：" + page.getPage());
			for (String str : page) {
				System.out.println(str);
			}
		}
	}
}
