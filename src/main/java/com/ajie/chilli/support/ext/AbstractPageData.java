package com.ajie.chilli.support.ext;

import java.util.Iterator;

import com.ajie.chilli.support.PageData;

/**
 * 分页数据抽象实现
 * 
 * @author niezhenjie
 */
public abstract class AbstractPageData<E> implements PageData<E> {
	/** 默认每页项数 */
	public static final int DEFAULT_PAGE_SIZE = 50;
	/*
	*//** 数据 */
	/*
	 * private E[] data;
	 */
	/** 当前页 */
	private int page;
	/** 每页项数 */
	private int pageSize;
	/** 总页数 */
	private int pageCount;
	/** 总项数 */
	private int count;
	/** 当前指针位置 */
	private int pos;

	protected AbstractPageData(int count) {
		pageSize = DEFAULT_PAGE_SIZE;
		this.count = count;
		page = -1;
		pos = -1;
		cal();
	}

	private void cal() {
		this.pageCount = (int) Math.ceil((double) count / (double) pageSize);
	}

	/**
	 * 获取当前指针的数据
	 * 
	 * @return
	 */
	abstract protected E get();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterator<E> iterator() {
		return new PageDataIterator(this);
	}

	@Override
	public Iterable<E> toForeach() {
		pageSize = count;
		page = 1;
		return this;
	}

	/**
	 * 指针位置
	 * 
	 * @return
	 */
	protected int getPos() {
		return pos;
	}

	@Override
	public int getPage() {
		return page;
	}

	@Override
	public int getPageSize() {
		return pageSize;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public int getPageCount() {
		return pageCount;
	}

	@Override
	public boolean gotoPage(int page) {
		if (page < 0) {
			return false;
		}
		if (page > pageCount) {
			return false;
		}
		pos = pageSize * (page - 1) - 1;// next的时候在向前走一步
		this.page = page;
		return true;
	}

	@Override
	public boolean hasNext() {
		if (page == -1) {
			return false;
		}
		// hasNext判断并还没有next，所以pos一开始在当前页的第一项的前一个位置
		return (pos + 1) >= pageSize * (page - 1)
				&& (pos + 1) < pageSize * page && (pos + 1) < count;
	}

	@Override
	public E next() {
		pos += 1;
		return get();
	}

	@Override
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
		cal();
	}

	private static class PageDataIterator<E> implements Iterator<E> {
		private PageData<E> data;

		protected PageDataIterator(PageData<E> data) {
			this.data = data;
		}

		@Override
		public boolean hasNext() {
			return data.hasNext();
		}

		@Override
		public E next() {
			return data.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("不可删除");
		}

	}

}
