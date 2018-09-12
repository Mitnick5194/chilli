package com.ajie.chilli.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * 工具箱
 * 
 * @author niezhenjie
 */
public class Toolkits {

	private Toolkits() {
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
	 * 当前时间戳+从6为随机数里截取的三位数，经测试，同时开100个线程生成的id没有出现过重复，但是当<br>
	 * 线程数到达1000个时，会出现几个重复，在并发不是很大的情况下，可以使用
	 * 
	 * @return
	 */
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
	 * 0x开头的十六进制转十进制
	 * 
	 * @param hex
	 * @return
	 */
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

	public static final Random _Random = new Random();

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
	public static void main(String[] args) {
		final HashSet<String> set = new HashSet<String>();
		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < 1000; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					synchronized (set) {
						String id = Toolkits.gen16UniqueId();
						// set.add(id);
						list.add(id);
					}
				}
			}).start();
		}
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(set.size());
		System.out.println(list.size());

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
		System.out.println("================================");
		/*	for (int i = 0; i < list.size(); i++) {
				System.out.println(i + "： " + list.get(i));
			}*/

		/*int i = Toolkits.Hex2Deci("0x100");
		System.out.println(i);*/
	}
}
