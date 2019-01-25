package com.ajie.chilli.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

/**
 * 工具箱
 * 
 * @author niezhenjie
 */
final public class Toolkits {

	/** 数字字母表 */
	public final static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
			'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
			's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
			'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	/** 十六进制对照表 */
	public final static char[] hexTable = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
			'b', 'c', 'd', 'e', 'f' };

	public static final Random _Random = new Random();

	private Toolkits() {
	}

	/**
	 * 生成由0-9a-zA-z组成的长度为16的字串
	 * 
	 * @return
	 */
	static public String uniqueKey() {
		return uniqueKey(16);
	}

	/**
	 * 生成由0-9a-zA-z组成的唯一名字，经测试，此方法的唯一性最高，生成16位数，在5000个并发下测试10次没有出现过冲突
	 * 但是这个在性能上消耗大于genRandomStr，在并发很大时，优先选择此方法
	 * 
	 * @param len
	 * @return
	 */
	static public String uniqueKey(int len) {
		if (0 == len)
			return "";
		int timestamptlen = 13;// 时间戳长度
		StringBuilder sb = new StringBuilder();
		if (len < timestamptlen) { // 长度少于13，直接使用13个随机数做对照
			for (int i = 0; i < len; i++) {
				int idx = getRandomRange(0, 61);
				sb.append(digits[idx]);
			}
			return sb.toString();
		}
		len -= timestamptlen;
		for (int i = 0; i < len; i++) {
			int idx = getRandomRange(0, 61);
			sb.append(digits[idx]);
		}
		Date now = new Date();
		sb.append(now.getTime());
		return sb.toString();
	}

	/**
	 * 线程异常堆栈
	 * 
	 * @param e
	 * @return
	 */
	public static String printTrace(Throwable e) {
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return sw.toString();
		} finally {
			try {
				if (null != sw) {
					sw.close();
				}
				if (null != pw) {
					pw.close();
				}
			} catch (Exception ex) {
				// Ignore
			}
		}
	}

	/**
	 * 16位随机数 <br>
	 * 当前时间戳+从6为随机数里截取的三位数，经测试，同时开100个线程生成的id没有出现过重复<br>
	 * 
	 * @return
	 * 
	 * @Deprecated 使用genRandomStr或uniqueKey
	 */
	@Deprecated
	public static String gen16UniqueId() {
		long currentTimeMillis = System.currentTimeMillis(); // 当前时间戳
		Random random = new Random();
		int random1 = random.nextInt(999); // 3位随机数
		int random2 = random.nextInt(999); // 3位随机数
		int radom = ((random1 * random2));
		StringBuilder sb = new StringBuilder();
		sb.append(currentTimeMillis);
		sb.append(radom);
		String str = sb.toString();
		// 不足16位用0补足
		int lack = 16 - str.length();
		if (lack > 0) {
			while (lack-- > 0) {
				str += "0";
			}
		} else if (lack < 0) {
			str = str.substring(0, 16);
		}
		return str;
	}

	/**
	 * 随机生成指定长度的字串，长度越小，冲突的几率越大，经测试5000个并发生成16位冲突在10个以内，在并发不是很大时，可以使用，并发大时，
	 * 请使用uniqueKey，此方法性能高于uniqueKey
	 * 
	 * @param len
	 * @return
	 */
	public static String genRandomStr(int len) {
		StringBuilder sb = new StringBuilder();
		String rbt = genRandomByTimestamp(sb);
		if (rbt.length() > len)
			return rbt.substring(0, len);
		if (rbt.length() == len)
			return rbt;
		while (rbt.length() < len) {
			rbt = genRandomByTimestamp(sb);
		}
		return rbt.substring(0, len);
	}

	/**
	 * 当前时间戳每一个数加上一个随机数再根据对照表获得一个13位的字串
	 * 
	 * 时间戳是13位，到2286年失效
	 * 
	 * @return
	 */
	public static String genRandomByTimestamp(StringBuilder sb) {
		String now = String.valueOf(new Date().getTime());
		int rad = getRandomRange(0, 52);// 对照表最大下标为61，now单个数最大值是9
		if (null == sb)
			sb = new StringBuilder();
		for (int i = 0; i < 13; i++) {
			int dec = getIndexByChar(now.charAt(i));
			sb.append(digits[dec + rad]);
		}
		return sb.toString();
	}

	/**
	 * 字符c在对照表digits里对象的下标
	 * 
	 * @param c
	 * @return
	 */
	private static int getIndexByChar(char c) {
		for (int i = 0; i < digits.length; i++) {
			char ch = digits[i];
			if (ch == c) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 0x开头的十六进制转十进制
	 * 
	 * @param hex
	 * @return
	 * 
	 * @Deprecated请使用hex2deci
	 */
	@Deprecated
	public static int Hex2Deci(String hex) throws NumberFormatException {
		if (null == hex) {
			throw new NumberFormatException("格式错误，参数格式应为0x开头的十六进制: " + hex);
		}
		int len = hex.length();
		if (len <= 2) {
			throw new NumberFormatException("格式错误，参数格式应为0x开头的十六进制: " + hex);
		}
		String str = hex.substring(2, len);
		int ret = 0;
		try {
			ret = Integer.valueOf(str, 16);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("格式错误，参数格式应为0x开头的十六进制: " + hex);
		}
		return ret;
	}

	/**
	 * 十六进制转十进制
	 * 
	 * @param hex
	 *            十六进制 支持0x开头
	 * @return
	 */
	public static int hex2deci(String hex) {
		if (null == hex)
			throw new NumberFormatException("param is null");
		// 去除前面的0x
		if (hex.startsWith("0x"))
			hex = hex.substring(2);
		if (hex.length() == 0)
			throw new NumberFormatException("param is invalid: " + hex);
		int len = hex.length();
		int i = 0;
		int result = 0;
		while (i < len) {
			int dec = getHexChar(hex.charAt(i));
			if (-1 == dec)
				throw new NumberFormatException("param is invalid: " + hex);
			result += dec << ((len - (i++) - 1) * 4);
		}
		return result;
	}

	/**
	 * 十六进制的字符对应的十进制数字
	 * 
	 * @param c
	 * @return
	 */
	private static int getHexChar(char c) {
		for (int i = 0; i < hexTable.length; i++) {
			char hex = hexTable[i];
			if (hex <= 90 && hex >= 65) // 大写转小写
				hex += 32;
			if (hex == c)
				return i;
		}
		return -1;
	}

	/**
	 * 数字型的十六进制转十进制
	 * 
	 * @param hex
	 * @return
	 * @throws NumberFormatException
	 */
	public static int Hex2Deci(int hex) throws NumberFormatException {
		return hex2deci(String.valueOf(hex));
	}

	/**
	 * 生成32位md5码
	 * 
	 * @param password
	 * @return
	 */
	public static String md5Password(String password) {

		try {
			// 得到一个信息摘要器
			MessageDigest digest = MessageDigest.getInstance("md5");
			byte[] result = digest.digest(password.getBytes());
			StringBuffer buffer = new StringBuffer();
			// 把每一个byte 做一个与运算 0xff;
			for (byte b : result) {
				// 与运算
				int number = b & 0xff;
				String str = Integer.toHexString(number);
				if (str.length() == 1) {
					buffer.append("0");
				}
				buffer.append(str);
			}

			// 标准的md5加密后的结果
			return buffer.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "";
		}

	}

	/**
	 * 随机生成从 [min - max]随机数
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static int getRandomRange(int min, int max) {
		int ret = _Random.nextInt(max - min + 1);
		return ret + min;
	}

	// 测试
	public static void main(String[] args) throws InterruptedException {

		int hex2deci = hex2deci("0x001");
		System.out.println(hex2deci);
		System.out.println("========");

		String uniqueKey = uniqueKey(32);
		System.out.println(uniqueKey);

		System.out.println("==========");

		/*final HashSet<String> set = new HashSet<String>();
		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < 5000; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					synchronized (set) {
						String id = Toolkits.uniqueKey(16);
						// String id = gen16UniqueId();
						// System.out.println(id);
						set.add(id);
						list.add(id);
					}
				}
			}).start();
		}
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(set.size());
		System.out.println(list.size());*/
		/*
		System.out.println("================================");
		for (int i = 0; i < list.size(); i++) {
			String str1 = list.get(i);
			for (int j = i + 1; j < list.size() - i - 1; j++) {
				if (str1.equals(list.get(j))) {
					System.out.println("i=" + i + ", j=" + j);
					System.out.println(str1);
				}
			}
		}
		System.out.println("================================");*/
		/*	for (int i = 0; i < list.size(); i++) {
				System.out.println(i + "： " + list.get(i));
			}*/

		/*int i = Toolkits.Hex2Deci("0x100");
		System.out.println(i);*/
	}
}
