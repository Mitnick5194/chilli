package com.ajie.chilli.utils;

/**
 * html过滤，防止xss注入
 *
 * @author niezhenjie
 *
 */
final public class HtmlFilter {
	/** < ascii码 */
	public final static byte MARK_LEFT = 0x3C;
	/** > ascii码 */
	public final static byte MARK_RIGHT = 0x3E;

	private HtmlFilter() {

	}

	/**
	 * 过滤html标签，如 <div>abc</div>过滤后的结果：abc<br>
	 * 注意，如果只有一个标签，照样会被过滤 如只出现<div>没有</div>一样会被过滤<br>
	 * 对<img />类型的标签也起作用，但是如果是<div class='dv' ,没有结束符> ，这种是不会被过滤的
	 * 
	 * @param content
	 * @return
	 */
	public static String filterHtml(String content, StringBuilder sb) {
		if (null == sb)
			sb = new StringBuilder();
		if (null == content)
			return sb.toString();
		// <x> 最少长度3
		if (content.length() < 3)
			return content;
		int preIdx = 0;// 上一个标签的结束位置
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (c == MARK_LEFT) { // 发现左标签<
				if (i == content.length() - 1) {
					// 我靠，最后一个字符是<
					sb.append(c);
					break;
				}
				int j = i + 1;
				boolean find = false;
				for (; j < content.length(); j++) {
					char right = content.charAt(j);
					if (right == MARK_LEFT) {// 又一个<那么上一个肯定不是标签的开始了
						i = --j;
						break;
					}
					if (right == MARK_RIGHT) {
						// 找到有标签>的位置了
						find = true;
						break;
					}
				}
				if (!find) {
					// 遍历整个都没有右标签，只有左标签可以退出了
					break;
				}

				if (j - i < 2) {
					// 可能是<>这不是标签，不需过滤
					sb.append(content.substring(preIdx, j + 1));
				} else {
					// 把前面的放进结果里
					sb.append(content.substring(preIdx, i));
				}
				i = j++;
				preIdx = j;
			}
		}
		// 如果content不是以</xxx>结尾，那么最后还会有一组数据没有被加进来，需要添加一下
		sb.append(content.substring(preIdx, content.length()));
		return sb.toString();
	}

	/**
	 * 对html标签<和>转义，防止xss注入
	 * 
	 * @param content
	 * @return
	 */
	public static String escape(String content) {
		StringBuilder sb = new StringBuilder();
		if (null == content)
			return sb.toString();
		if (content.length() < 3) {
			return content;
		}
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (c == MARK_LEFT) {
				sb.append("&lt;");
			} else if (c == MARK_RIGHT) {
				sb.append("&gt;");
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		String html = "<p>123<div>abc</div>a<img class='img'/>vd<></p><code class='haha' ";
		String ret = filterHtml(html, null);
		// String escape = escape(html);
		System.out.println(ret);
		// System.out.println(escape);
	}

}
