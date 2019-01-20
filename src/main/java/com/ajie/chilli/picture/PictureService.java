package com.ajie.chilli.picture;

import java.io.InputStream;

public interface PictureService {

	/** 生成图片名的默认前缀 (remote-) */
	static final String DEFAULT_PREFIX = "RM-";

	/**
	 * 创建一张图片，并保存到服务器,路径为图片服务器的跟路径
	 * 
	 * @param stream
	 * @return
	 * @throws PictureException
	 */
	Picture create(InputStream stream) throws PictureException;

	/**
	 * 创建一张图片，并保存到指定服务器路径（相对于图片服务器的根路径，）
	 * 
	 * @param address
	 *            相对路径，如 blog/20190120
	 * @param stream
	 * @return
	 * @throws PictureException
	 */
	Picture create(String address, InputStream stream) throws PictureException;

	/**
	 * 指定文件名
	 * 
	 * @param fileName
	 *            原文件名
	 * @param stream
	 * @return
	 * @throws PictureException
	 */
	Picture createForName(String fileName, InputStream stream) throws PictureException;

	/**
	 * 指定路径
	 * 
	 * @param address
	 * @param fileName
	 *            原文件名
	 * @param stream
	 * @return
	 * @throws PictureException
	 */
	Picture create(String address, String fileName, InputStream stream) throws PictureException;

	/**
	 * 根据服务器路径获取一张图片
	 * 
	 * @param address
	 *            服务器路径
	 * @return
	 * @throws PictureException
	 */
	Picture get(String address) throws PictureException;
}
