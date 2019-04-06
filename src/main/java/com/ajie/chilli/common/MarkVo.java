package com.ajie.chilli.common;

import java.util.List;

/**
 * markSupport辅助类，一个Integer表示32个boolean
 *
 * @author niezhenjie
 *
 */
public class MarkVo {
	private int mark;

	public MarkVo(int mark) {
		this.mark = mark;
	}

	public Integer getMark() {
		return mark;
	}

	/**
	 * 设置标志，负数表示取出标志
	 * 
	 * @param mark
	 */
	public MarkVo setMark(int mark) {
		if (mark == 0) {
			this.mark = 0;
		} else if (mark > 0) {
			this.mark |= mark;
		} else {
			mark = (-mark);
			this.mark &= ~(mark);
		}
		return this;
	}

	/**
	 * 批量添加标志
	 * 
	 * @param marks
	 * @return
	 */
	public MarkVo setMarks(List<Integer> marks) {
		if (null == marks)
			return this;
		for (int i : marks) {
			setMark(i);
		}
		return this;
	}

	public MarkVo removeMark(int mark) {
		setMark(-mark);
		return this;
	}

	public boolean isMark(int mark) {
		return mark == (mark & this.mark);
	}
}
