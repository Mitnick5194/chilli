package com.ajie.chilli.remote;

import java.io.InputStream;

import com.ajie.chilli.remote.exception.RemoteException;

/**
 * 文件上传服务
 *
 * @author niezhenjie
 *
 */
public interface UploadService {

	/** 默认访问路径 */
	final static String DEFAULT_PATH = "/var/www/";

	/**
	 * 指定文件名上传至默认路径
	 * 
	 * @param stream
	 * @param fileName
	 * @throws RemoteException
	 */
	void upload(InputStream stream, String fileName) throws RemoteException;

	/**
	 * 指定文件名及路径
	 * 
	 * @param stream
	 * @param path
	 * @param fileName
	 * @throws RemoteException
	 */
	void upload(InputStream stream, String path, String fileName) throws RemoteException;

	void upload(byte[] stream, String fileName) throws RemoteException;

	void upload(byte[] stream, String path, String fileName) throws RemoteException;

}
