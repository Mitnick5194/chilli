package com.ajie.chilli.common.enums;

/**
 * 各种容量单位与字节(Byte)的关系
 *
 * @author niezhenjie
 *
 */
public enum CapacityUnit {
	unknown(0, "未知"), B(1, "B"), Kb(1024 / 8, "Kb"), K(1024, "K"), KB(CapacityUnit.K.getSize(),
			"KB"), M(1024 * CapacityUnit.KB.getSize(), "M"), Mb(CapacityUnit.M.getSize() / 8, "Mb"), MB(
			CapacityUnit.M.getSize(), "MB"), GB(CapacityUnit.M.getSize(), "GB"), TB(CapacityUnit.GB
			.getSize(), "TB"), PB(CapacityUnit.TB.getSize(), "PB");

	/** 单位对应的字节大小 */
	protected int size;
	protected String unit;

	CapacityUnit(int size, String unit) {
		this.size = size;
		this.unit = unit;
	}

	public int getSize() {
		return size;
	}

	public String getUnit() {
		return unit;
	}

}
