package com.ajie.chilli.support;


/**
 * 分页数据
 * 
 * @author niezhenjie
 */
public interface PageData<E> extends Iterable<E> {

	/**
	 * 当前页（页码）
	 * 
	 * @return
	 */
	int getPage();

	/**
	 * 每页项数
	 * 
	 * @return
	 */
	int getPageSize();

	/**
	 * 总项数
	 * 
	 * @return
	 */
	int getCount();

	/**
	 * 总页数
	 * 
	 * @return
	 */
	int getPageCount();

	/**
	 * 跳转到指定页
	 * 
	 * @param page
	 * @return
	 */
	boolean gotoPage(int page);

	/**
	 * 是否还有下一项
	 * 
	 * @return
	 */
	boolean hasNext();

	/**
	 * 下一项
	 * 
	 * @return
	 */
	E next();

	/**
	 * 设置每页项数
	 */
	void setPageSize(int pageSize);

	/**
	 * 遍历全部
	 * 
	 * @return
	 */
	Iterable<E> toForeach();

}
