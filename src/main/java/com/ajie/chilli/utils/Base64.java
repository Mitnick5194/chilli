package com.ajie.chilli.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.util.ByteArrayBuffer;

/**
 * Base64编码解码工具
 * 
 * Base64是将二进制或不可打印（如换行符）字符转换成可打印字符，编码原理是将8位的字节转换成6位（0~63），转换后得到范围在0~63的数，
 * 再根据对照表使用对应的字符代替， 但因为计算机存储是以字节为单位，所以，最终转换后的结果其实还是8位，Base64标准规定在前面两位用0代替<br>
 * 
 * <li>
 * 例： 英文A的ascii码为65，在计算机中表现为二进制：0100 0001,每6位为一个编码：0100 00和01，<br>
 * 前面补两个0：0001 0000和0001 8位形式：0001 0000和0000 0001，其实后面的书不是0000 0001，而是0001 0000<br>
 * 转换成十进制分别是16和16，参考对照表16对应的是Q，所以是QQ，但其实结果是QQ==，因为是将8位转换成6位输出
 * 所以需要和原来的对齐，即原数据3字节（24位），转换成Base后变成了4组（4*6=24字节），如果不足，则用=输出;
 * 特别提醒，不足是后面没有数据了，而不是后面是0，0是有值输出,值是A<Br>
 * 从编码原理可知，Base64编码后的结果会是原数据的大小的4/3倍（即比原来多了3/1），这还是在原数据的大小是3和4的公倍数，如果不是，
 * 则还会会多出一个或两个==<br>
 * </li>
 * 
 * unicode最大值是57838 对应的字符是 
 *
 * @author niezhenjie
 *
 */
public class Base64 {
	/** Base64对照表，下标表示base64对应的码值（索引），值表示Base64指定码值对应的值 */
	static final String[] _Table = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
			"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c",
			"d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
			"u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+",
			"/" };

	/** 将8位切割成6位基数 */
	static final public int BASE = (1 << 6) - 1;

	/**
	 * 对字符串src进行编码
	 * 
	 * @param src
	 * @return
	 */
	public static String encrypt(String src) {
		if (null == src)
			return "";
		return encrypt(src.getBytes());
	}

	/**
	 * 对IO流进行编码
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String encrypt(InputStream is) throws IOException {
		ByteArrayBuffer data = new ByteArrayBuffer(256);
		byte[] buf = new byte[256];
		int n = is.read(buf);
		while (n > -1) {
			data.append(buf, 0, n);
			n = is.read(buf);
		}
		return encrypt(data.toByteArray());
	}

	/**
	 * 对字节流数组进行编码
	 * 
	 * @param bytes
	 * @return
	 */
	public static String encrypt(byte[] bytes) {
		String ret = "";
		if (null == bytes || bytes.length == 0)
			return ret;
		StringBuilder sb = new StringBuilder();
		int i = 0, len = bytes.length;
		for (; i < len; i++) {
			// 每三个8位二进制是一组，即对每三个字节进行一次编码，编码结果是4个字节
			if ((i + 1) % 3 == 0) {
				int merge = merge(bytes[i - 2], bytes[i - 1], bytes[i]);
				// 对完整一组进行编码
				sb.append(doEncrypt(merge));
			}
		}
		// 原字节数组可能不能按照每三个为一组分割，如[65,66,67,68,69];for循环后剩下[68,69]没有处理
		// 对剩余的数也要进行编码
		int lackcount = len % 3;
		// 剩余的一个字节或两个字节进行处理
		if (lackcount == 1) {
			int byte1 = (bytes[i - 1] & (BASE << 2)) >> 2;
			// 右移4位 而不是6位 因为前面两个是补0
			int byte2 = (bytes[i - 1] << 4) & BASE;
			sb.append(mapper(byte1));
			sb.append(mapper(byte2));
			sb.append("==");
		} else if (lackcount == 2) {
			int merge = merge(bytes[i - 2], bytes[i - 1]);
			int byte1 = (merge & (BASE << 10)) >> 10;
			int byte2 = (merge & (BASE << 4)) >> 4;
			// 右移两位而不是4位，因为前面要补两个0
			int byte3 = (merge << 2) & BASE;
			sb.append(mapper(byte1));
			sb.append(mapper(byte2));
			sb.append(mapper(byte3));
			sb.append("=");

		}
		return sb.toString();
	}

	/**
	 * 将多个字节流向高位移动进行拼接起来<br>
	 * 
	 * <li>
	 * 例：[65,66] 二进制形式：0100 0001和0100 0010<br>
	 * 第一个向左移(length-1*8)位，以此类推，拼接结果<br>
	 * 0100 0001 0100 0010
	 * 
	 * @param bytes
	 * @return
	 */
	protected static int merge(byte... bytes) {
		int merge = 0, len = bytes.length;
		for (int i = 0; i < len; i++) {
			merge |= ((bytes[i] & 0xFF) << ((len - i - 1) * 8));
		}
		return merge;
	}

	/**
	 * 对三个字节合并后的结果进行编码，注意，一定完整一组的合并，不能对只有两个字节或1个字节合并的结果编码
	 * 
	 * @param src
	 *            3个字节拼接的结果
	 * @return
	 */
	protected static String doEncrypt(int src) {
		StringBuilder sb = new StringBuilder();
		int ret1 = src & BASE << 18;
		ret1 >>= 18;// 回归低位
		int ret2 = src & BASE << 12;
		ret2 >>= 12;
		int ret3 = src & BASE << 6;
		ret3 >>= 6;
		int ret4 = src & BASE;
		sb.append(mapper(ret1));
		sb.append(mapper(ret2));
		sb.append(mapper(ret3));
		sb.append(mapper(ret4));
		return sb.toString();
	}

	/**
	 * 对编码encrypt进行解码 //TODO
	 * 
	 * @param encrypt
	 * @return
	 */
	public static byte[] decode(String encrypt) {
		if (null == encrypt)
			return new byte[0];

		return null;
	}

	protected static String mapper(int index) {
		return _Table[index];
	}

	public static void main(String[] args) throws IOException {

		String en = encrypt("心有林夕");
		System.out.println(en);
		InputStream is = new FileInputStream(new File("C:/Users/ajie/Desktop/arrow_top.png"));
		String ret = encrypt(is);
		System.out.println(ret);
	}
}
