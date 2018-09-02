package com.ajie.support;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ajie.utils.Toolkits;

/**
 * 支持外部ID接口的抽象实现
 * 
 * @author niezhenjie
 */
public abstract class AbstractOuterIdSupport implements OuterIdSupport {
	protected static final Logger logger = LoggerFactory.getLogger(AbstractOuterIdSupport.class);

	/**
	 * 真实ID 不一定是数值类型，但一般是数值类型
	 * 
	 * @return
	 */
	protected abstract String getRealId();

	/**
	 * 返回继承本抽象类的实体的外部ID
	 * 
	 * @return
	 */
	protected abstract String getEntityOuterId();

	protected abstract void setOuterId(String outerId);

	public String getOuterId() {
		String outerId = getEntityOuterId();
		if (null == outerId) {
			try {
				outerId = genOuterId();
			} catch (OuterIdException e) {
				logger.error(e.getMessage());
				outerId = "";
			}
		}
		return outerId;

	}

	@Override
	public String genOuterId() throws OuterIdException {
		String name = getClassType();
		if (null == name) {
			logger.error("生成外部ID失败，无法获取业务对象的类型");
			throw new OuterIdException("生成外部ID失败，无法获取业务对象的类型");
		}
		int len = name.length();
		if (len > OuterIdSupport.PRE_LENGTH) {
			name = "Object";
		}
		// 前32位，类型名+随机数
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		int lack = OuterIdSupport.PRE_LENGTH - len;
		while (lack-- > 0) {
			sb.append(Toolkits.getRandomRange(0, 9));
		}
		// 中间真实ID藏身处
		String realId = getRealId();
		if (null == realId) {
			logger.error("生成外部ID失败，无法获取业务对象的真实ID");
			throw new OuterIdException("生成外部ID失败，无法获取业务对象的真实ID");
		}
		int realIdLen = realId.length();
		if (realIdLen > IDPART_LENGTH) {
			logger.error("生成外部ID失败，业务对象真实ID长度大于 " + OuterIdSupport.IDPART_LENGTH);
			throw new OuterIdException("生成外部ID失败，业务对象真实ID长度大于 " + OuterIdSupport.IDPART_LENGTH);
		}
		lack = IDPART_LENGTH - realIdLen;
		while (lack-- > 0) {
			sb.append((char) Toolkits.getRandomRange(97, 122));
		}
		sb.append(realId);
		// 最后部分：时间戳+随机数
		String timestamp = String.valueOf(new Date().getTime());
		if (timestamp.length() > OuterIdSupport.LAST_LENGTH) {
			logger.error("生成外部ID失败，时间戳长度大于 " + OuterIdSupport.LAST_LENGTH);
			throw new OuterIdException("生成外部ID失败，时间戳长度大于 " + OuterIdSupport.LAST_LENGTH);
		}
		sb.append(timestamp);
		lack = OuterIdSupport.LAST_LENGTH - timestamp.length();
		while (lack-- > 0) {
			sb.append(Toolkits.getRandomRange(0, 9));
		}
		setOuterId(sb.toString());
		return sb.toString();
	}

	@Override
	public String getIdFromOuterId() throws OuterIdException {
		String outerId = getOuterId();
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

	/**
	 * 获取业务对象类型名
	 * 
	 * @return
	 */
	protected String getClassType() {
		return getClass().getSimpleName();
	}

	public static void main(String[] args) {
		String outerId = "asafdwasdf1243";
		int len = outerId.length();
		int hight = len - 1;
		int low = 0;
		int index = 0;
		while (hight >= low) {
			if (hight == low) {
				char c = outerId.charAt(hight);
				if (c > 57) {
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
		System.out.println(outerId);
	}

}
