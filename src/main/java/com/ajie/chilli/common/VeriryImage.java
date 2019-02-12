package com.ajie.chilli.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;

import com.ajie.chilli.utils.Toolkits;

/**
 * 验证码图片
 *
 * @author niezhenjie
 *
 */
public class VeriryImage {

	/**
	 * 获取一张验证码图片
	 * 
	 * @param key
	 * @return
	 */
	static public BufferedImage getImage(String key) {
		return getImage(key, 60, 20);
	}

	/**
	 * 获取一张验证码，使用指定的宽高和默认的字体大小
	 * 
	 * @param key
	 * @param width
	 * @param height
	 * @return
	 */
	public static BufferedImage getImage(String key, int width, int height) {
		return getImage(key, 60, 20, 18);
	}

	/**
	 * 指定宽高和字体大小的验证码图片
	 * 
	 * @param key
	 * @param width
	 * @param height
	 * @param fontsize
	 * @return
	 */
	public static BufferedImage getImage(String key, int width, int height, int fontsize) {
		// 在内存中创建图片
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
		// 获取画笔
		Graphics graphics = image.getGraphics();

		// 设置背景色
		graphics.setColor(getColor(200, 250));
		graphics.fillRect(0, 0, width, height);

		// 设置字体
		graphics.setFont(new Font("Times New Roman", Font.ITALIC, fontsize));

		// 画150条干扰线，防止被嗅探
		graphics.setColor(getColor(160, 200));
		for (int i = 0; i < 150; i++) {
			int x1 = Toolkits.getRandomRange(0, width);
			int x2 = Toolkits.getRandomRange(0, width);
			int y1 = Toolkits.getRandomRange(0, height);
			int y2 = Toolkits.getRandomRange(0, height);
			graphics.drawLine(x1, y1, x2, y2);
		}
		// 画内容
		char[] chars = key.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			graphics.setColor(getColor(10, 140));
			graphics.drawChars(chars, i, 1, i * 13, 16);
		}
		// 输出一下
		graphics.dispose();
		return image;
	}

	/**
	 * 
	 * @param min
	 *            rgb最小值
	 * @param max
	 *            最大值
	 * @return
	 */
	private static Color getColor(int min, int max) {
		int r = Toolkits.getRandomRange(min, max);
		int g = Toolkits.getRandomRange(min, max);
		int b = Toolkits.getRandomRange(min, max);
		Color color = new Color(r, g, b);
		return color;
	}

	public static void main(String[] args) throws Exception {
		File f = new File("C:\\Users\\ajie\\Desktop\\boke\\ret.png");
		System.out.println(f.exists());
		BufferedImage image = getImage("ABCd");
		OutputStream os = new FileOutputStream(f);
		Imaging.writeImage(image, os, ImageFormats.PNG, null);
		System.out.println("done");
	}
}
