package com.ajie.chilli.remote;

import java.io.InputStream;
import java.rmi.RemoteException;

import org.apache.commons.net.ftp.FTPFile;

/**
 * FTPClient包装服务类
 *
 * @author niezhenjie
 *
 */
public interface JschService {

	public static final int MODE_FORCE = 1 << 1;

	public static final int MODE_SOFT = 1 << 2;

	boolean upload(InputStream stream) throws RemoteException;

	boolean upload(InputStream stream, String name) throws RemoteException;

	FTPFile[] listFiles() throws RemoteException;

}
