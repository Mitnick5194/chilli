package com.ajie.chilli.common;

/**
 * markSupport辅助类
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

	public void setMark(int mark) {
		if (mark == 0) {
			this.mark = 0;
		} else if (mark > 0) {
			this.mark |= mark;
		} else {
			mark = (-mark);
			this.mark &= ~(mark);
		}
	}

	public boolean isMark(int mark) {
		return mark == (mark & this.mark);
	}
}
