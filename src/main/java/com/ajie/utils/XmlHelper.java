package com.ajie.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * xml辅助类，加载默认路径为classpath
 * 
 * @author niezhenjie
 */
public class XmlHelper {
	private static final Logger logger = LoggerFactory.getLogger(XmlHelper.class);

	/**
	 * 将xml加载为io流 ，注意，不能在这里关闭流，所以这个方法不太好
	 * 
	 * @param xml
	 * @return
	 * @throws IOException
	 */
	public static InputStream parseInputStream(String xml) throws IOException {
		if (null == xml) {
			return null;
		}
		URL url;
		try {
			// 先从当前线程的加载器路径读取（appClassLoader，其实就是项目的路径）
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			url = loader.getResource(xml);
		} catch (SecurityException e) {
			// 从系统资源路径中查找
			url = ClassLoader.getSystemResource(xml);
		}
		InputStream in = null;
		try {
			if (null == url) { // 还为空，只能从用户文件夹中找一下了
				String path = System.getProperty("user.dir", "");
				if (null != path && path.length() > 0) {
					if (File.separatorChar == path.charAt(path.length() - 1)) {
						xml = path + xml;
					} else {
						xml = path + File.separator + xml;
					}
					in = new FileInputStream(xml);
				}
			} else {
				in = url.openStream();
			}
			return in;
		} catch (FileNotFoundException e) {
			logger.error(xml + "加载失败：" + Toolkits.printTrace(e));
		}
		return null;
	}

	public static Document parseDocument(String xml) throws IOException {
		InputStream is = parseInputStream(xml);
		SAXReader reader = new SAXReader();
		try {
			Document doc = reader.read(is);
			return doc;
		} catch (DocumentException e) {
			logger.error(xml + "文件加载失败" + Toolkits.printTrace(e));
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (Exception e2) {
					// Ignore
				}
			}
		}
		return null;
	}

}
