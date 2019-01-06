package com.ajie.chilli.remote;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

/**
 * FTPClient包装服务类
 *
 * @author niezhenjie
 *
 */
public interface SshService {

	/** 默认端口 */
	public static final int DEFAULT_PORT = 22;

	/** 整个系统只有一个连接客户端 */
	public static final int MODE_SINGLETON = 0x100;
	/** 每一个线程创建一个连接客户端 */
	public static final int MODE_MULTI = 0x1000;

	/**
	 * 文件上传
	 * 
	 * @param stream
	 *            文件流
	 * @param name
	 *            文件名
	 * @return
	 * @throws RemoteException
	 * @throws IOException
	 */
	boolean upload(String name, InputStream stream) throws RemoteException, IOException;

	/**
	 * 文件上传至指定路径
	 * 
	 * @param stream
	 *            文件流
	 * @param path
	 *            上传路径
	 * @param name
	 *            文件名
	 * @return
	 * @throws RemoteException
	 * @throws IOException
	 */
	boolean upload(String path, String name, InputStream stream) throws RemoteException,
			IOException;

}
