package com.ajie.chilli.remote.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ajie.chilli.remote.ConnectConfig;
import com.ajie.chilli.remote.RemoteCmd;
import com.ajie.chilli.remote.SessionExt;
import com.ajie.chilli.remote.SshSessionMgr;
import com.ajie.chilli.remote.Worker;
import com.ajie.chilli.remote.exception.RemoteException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 *
 *
 * @author niezhenjie
 *
 */
public class RemoteCmdImpl implements RemoteCmd {
	public final static Logger logger = LoggerFactory.getLogger(RemoteCmdImpl.class);

	private SshSessionMgr ssh;

	public RemoteCmdImpl(SshSessionMgr ssh) {
		this.ssh = ssh;
	}

	public void setSsh(SshSessionMgr ssh) {
		this.ssh = ssh;
	}

	public SshSessionMgr getSsh() {
		return ssh;
	}

	public SessionExt getSession() throws RemoteException {
		return ssh.getSession();
	}

	@Override
	public String cmd(String cmd) throws RemoteException {
		byte[] ret = byteArrayResultCmd(cmd);
		try {
			return new String(ret, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return new String(ret);
		}
	}

	@Override
	public byte[] byteArrayResultCmd(String cmd) throws RemoteException {
		OutputStream stream = streamResultCmd(cmd);
		if (stream instanceof ByteArrayOutputStream) {
			ByteArrayOutputStream out = (ByteArrayOutputStream) stream;
			return out.toByteArray();
		}
		return null;
	}

	@Override
	public OutputStream streamResultCmd(final String cmd) throws RemoteException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		Worker worker = new Worker() {
			@Override
			public void run(SessionExt session) throws RemoteException {
				try {
					ChannelExec channel = getChannel(session);
					channel.setInputStream(null);
					channel.setErrStream(out);
					channel.setCommand(cmd);
					InputStream in = channel.getInputStream();
					channel.connect();
					byte[] buf = new byte[1024];
					while (true) { // 因为是异步的，数据不一定能及时获取到，所以需要轮询
						while (in.available() > 0) {
							in.read(buf);
							out.write(buf);
						}
						if (channel.isClosed()) { // channel关闭了，但是还有数据在流中，继续读
							if (in.available() > 0)
								continue;
							break;
						}
						Thread.sleep(100);
					}
				} catch (Exception e) {
					logger.info("指令执行失败", e);
					throw new RemoteException("指令执行失败", e);
				}
			}
		};
		ssh.execute(worker);
		return out;
	}

	@Override
	public void voidResultcmd(String cmd) throws RemoteException {
		streamResultCmd(cmd);
	}

	private ChannelExec getChannel(SessionExt session) throws RemoteException {
		try {
			// 不能使用sessionExt打开channel,否则channel会被过早关闭，导致结果没有输出，具体原因不详
			Session sess = session.getSession();
			return (ChannelExec) sess.openChannel("exec");
		} catch (JSchException e) {
			throw new RemoteException("打开ChannelExec失败", e);
		}
	}

	@Override
	public String execCmd(String cmd) {
		if (null == cmd)
			return "";
		BufferedReader reader = null;
		StringBuilder sb = new StringBuilder();
		try {
			Process proc = Runtime.getRuntime().exec("cmd /c start /b " + cmd);
			reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return sb.toString();
	}

	public static void main(String[] args) throws IOException, RemoteException {
		Properties prop = new Properties();
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("server.properties");
		prop.load(is);
		String host = prop.getProperty("host");
		String passwd = prop.getProperty("passwd");
		String name = prop.getProperty("name");
		ConnectConfig config = ConnectConfig.valueOf(name, passwd, host, 22);
		config.setMax(5);
		config.setCore(2);
		SshSessionMgr ssh = new BlockSshSessionMgr(config);
		RemoteCmdImpl rci = new RemoteCmdImpl(ssh);
		String ret = rci.cmd("echo " + passwd + " | sudo -S netstat -anpt | grep :22");
		System.out.println(ret);

		/*Properties prop = new Properties();
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("server.properties");
		prop.load(is);
		String host = prop.getProperty("host");
		String passwd = prop.getProperty("passwd");
		String name = prop.getProperty("name");
		ConnectConfig config = ConnectConfig.valueOf(name, passwd, host, 22);
		config.setMax(5);
		config.setCore(2);
		SshClient client = SshClient.getClient(config);
		SshSessionMgr ssh = new BlockSshSessionMgr(config);
		SessionExt sessionExt = ssh.getSession();

		try {
			Session session = sessionExt.getSession();
			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			channel.setInputStream(null);
			channel.setErrStream(System.err);
			channel.setCommand("ls");
			InputStream in = channel.getInputStream();
			channel.connect();
			byte[] buf = new byte[1024];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while (true) { // 因为是异步的，数据不一定能及时获取到，所以需要轮询
				while (in.available() > 0) {
					in.read(buf);
					out.write(buf);
				}
				System.out.println(new String(out.toByteArray(), "utf-8"));
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;// 还有数据，继续读
					System.out.println("exit status: " + channel.getExitStatus());
					break;
				}
				Thread.sleep(1000);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

}
