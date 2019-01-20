package com.ajie.chilli.remote;

import java.io.OutputStream;

import com.ajie.chilli.remote.exception.RemoteException;

/**
 * 远程指令
 *
 * @author niezhenjie
 *
 */
public interface RemoteCmd {

	/**
	 * 执行指令
	 * 
	 * @param cmd
	 *            指令
	 * @return 返回控制台显示结果
	 * @throws RemoteException
	 */
	String cmd(String cmd) throws RemoteException;

	/**
	 * 执行指令，返回字节数组结果
	 * 
	 * @param cmd
	 *            指令
	 * @return 控制台显示结果以字节数组形式返回
	 */
	byte[] byteArrayResultCmd(String cmd) throws RemoteException;

	/**
	 * 执行指令，返回流结果
	 * 
	 * @param cmd
	 *            指令
	 * @return 控制台显示结果以流的形式返回
	 */
	OutputStream streamResultCmd(String cmd) throws RemoteException;

	/**
	 * 执行指令，不返回结果
	 * 
	 * @param cmd
	 *            指令
	 * @throws RemoteException 
	 */
	void voidResultcmd(String cmd) throws RemoteException;

}
