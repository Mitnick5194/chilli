package com.ajie.chilli.support.impl;

import java.util.ArrayList;
import java.util.List;

import com.ajie.chilli.support.ext.AbstractPageData;

/**
 * 基于List的分页封装
 * 
 * @author niezhenjie
 */
public class ListPageData<E> extends AbstractPageData<E> {

	private List<E> data;

	public ListPageData(List<E> data) {
		super(null != data ? data.size() : 0);
		this.data = data;
	}

	@Override
	protected E get() {
		return data.get(getPos());
	}

	public static void main(String[] args) {
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("c");
		list.add("d");
		list.add("e");
		ListPageData<String> page = new ListPageData<>(list);
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
