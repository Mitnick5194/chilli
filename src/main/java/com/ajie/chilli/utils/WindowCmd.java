package com.ajie.chilli.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * window cmd命令工具
 *
 * @author niezhenjie
 *
 */
public class WindowCmd {

	static public String execCmd(String cmd) throws IOException {
		if (null == cmd)
			return "";
		BufferedReader reader = null;
		StringBuilder sb = new StringBuilder();
		Process proc = Runtime.getRuntime().exec("cmd /c " + cmd);
		reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		try {
			String ret = execCmd("mvn -version");
			System.out.println(ret);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
