package com.ajie.support;

/**
 * 外部ID辅助类，主要是将一个外部ID转换成真是ID
 * 
 * @author niezhenjie
 */
public class OuterIdUtil {

	static public String getIdFromOuterId(String outerId) throws OuterIdException {
		if (null == outerId) {
			throw new OuterIdException("外部ID不存在");
		}
		if (outerId.length() != OuterIdSupport.ID_LENGTH) {
			throw new OuterIdException("外部ID不符合一般规则");
		}
		// 去前面
		outerId = outerId.substring(OuterIdSupport.PRE_LENGTH, outerId.length());
		// 去后面
		outerId = outerId.substring(0, OuterIdSupport.LAST_LENGTH);
		int len = outerId.length();
		int hight = len - 1;
		int low = 0;
		int index = 0;
		while (hight >= low) { // 二分法查找
			if (hight == low) {
				char c = outerId.charAt(hight);
				if (c > 57) { // 48-57是 0-9d ascii 数字前面的都是字母 ascii在[97-122]
					index = hight + 1;
				} else {
					index = hight;
				}
				break;
			}
			int middle = (hight + low) >> 1;
			char c = outerId.charAt(middle);
			if (c > 57) {
				low = middle + 1;
			} else {
				hight = middle - 1;
			}
		}
		outerId = outerId.substring(index, outerId.length());
		return outerId;
	}
}
