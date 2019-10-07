package com.ajie.chilli.encrypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * 超级简单的对称加密，加密后的内容长度和加密前一样
 *
 * @author niezhenjie
 *
 */
public class SimpleEncrypt {

	/** 偏移量，消除assic字符 */
	public static final int OFFSET = 256;

	/**
	 * 加密
	 * 
	 * @param content
	 *            加密内容
	 * @param password
	 *            密码
	 * @return 密文
	 */
	public static String encrypt(String content, String password) {
		String encryptPasswd = encryptPassword(password);
		StringBuilder sb = new StringBuilder();
		char[] cons = content.toCharArray();
		char[] pws = encryptPasswd.toCharArray();
		for (int i = 0; i < cons.length; i++) {
			sb.append((char) (cons[i] + pws[i % (pws.length - 1)] + OFFSET));
		}
		return sb.toString();
	}

	/**
	 * 解密
	 * 
	 * @param encrypt
	 *            加密内容
	 * @param password
	 *            密码
	 * @return 解密后的内容
	 */
	public static String decrypt(String encrypt, String password) {
		String encryptPasswd = encryptPassword(password);
		char[] cons = encrypt.toCharArray();
		char[] pws = encryptPasswd.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < cons.length; i++) {
			sb.append((char) (cons[i] - pws[i % (pws.length - 1)] - OFFSET));
		}
		return sb.toString();
	}

	/**
	 * 先对密码进行加密 为了让加密依赖密码长度，避免密码比内容长时，密码后面的没有参与加密
	 * 
	 * @param password
	 * @return
	 */
	public static String encryptPassword(String password) {
		StringBuilder sb = new StringBuilder();
		int pl = password.length();
		for (char c : password.toCharArray()) {
			sb.append((char) (c + pl + OFFSET));
		}
		return sb.toString();
	}

	/**
	 * 对文件内容进行编码，注意，文件要保存为utf-8编码格式（可手动修改）
	 * 
	 * @param in
	 * @param out
	 * @param password
	 */
	public static void encrypt(InputStream in, OutputStream out, String password) {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in, "utf-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String line = null;
		try {
			while (null != (line = br.readLine())) {
				// 对每一行进行加密
				sb.append(encrypt(line, password));
				sb.append("\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 写入文件
		try {
			out.write(sb.toString().getBytes("utf-8"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != in)
					in.close();
				if (null != out)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 对文件内容进行编码，输出utf-8解密内容
	 * 
	 * @param in
	 * @param out
	 * @param password
	 */
	public static void decrypt(InputStream in, OutputStream out, String password) {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line = null;
		try {
			while (null != (line = br.readLine())) {
				// 对每一行进行加密
				sb.append(decrypt(line, password));
				sb.append("\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 写入文件
		try {
			out.write(sb.toString().getBytes("utf-8"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != in)
					in.close();
				if (null != out)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		try {
			InputStream in = new FileInputStream(new File(
					"C:\\Users\\ajie\\Desktop\\test.txt"));
			OutputStream out = new FileOutputStream(new File(
					"C:\\users\\ajie\\Desktop\\out.txt"));
			encrypt(in, out, "123");
			in.close();
			out.close();
			InputStream din = new FileInputStream(new File(
					"C:\\Users\\ajie\\Desktop\\out.txt"));
			OutputStream dout = new FileOutputStream(new File(
					"C:\\users\\ajie\\Desktop\\out1.txt"));
			decrypt(din, dout, "123");
			din.close();
			dout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
