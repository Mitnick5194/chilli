package com.ajie.support;

/**
 * 实现此接口表示持久对象可以获得唯一的对外开放的ID，以避免数据库ID直接暴露出去<br>
 * 实现过程：根据业务对象的真实ID，按照一定的规则生成一个唯一的ID，需要根据对外开放的ID获得<br>
 * 真实的ID其实就是该过程的逆向过程<br>
 * <br>
 * 一般规则：outerId由64位组成，前32位由业务对象的对象类型+随机数补足32位组成，如果业务对象类型名大于16位，<br>
 * 则默认使用Object代替， 中间16位是真实ID， 如ID不足位数，则 用a-z随机字母填补，后16位由当前时间戳（13位）+3位随机数组成<br>
 * <br>
 * 由外部ID推算出真正的ID：截取中间16位，再去除第一个非数字
 * 
 * @author niezhenjie
 */
public interface OuterIdSupport {

	public static final int ID_LENGTH = 1 << 6;

	public static final int PRE_LENGTH = 1 << 5;

	public static final int IDPART_LENGTH = 1 << 4;

	public static final int LAST_LENGTH = 1 << 4;

	/**
	 * 生成唯一对外开放的id，一般情况下是依赖业务对象的实际id
	 * 
	 * @return
	 */
	String genOuterId() throws OuterIdException;

	/**
	 * 获取业务对象的唯一对外开放的id
	 * 
	 * @return
	 */
	String getOuterId();

	/**
	 * 根据对外开放的id逆向推出真是的id
	 * 
	 * @return
	 */
	String getIdFromOuterId() throws OuterIdException;

}
