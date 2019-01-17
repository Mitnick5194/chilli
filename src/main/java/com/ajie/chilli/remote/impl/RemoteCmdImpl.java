package com.ajie.chilli.remote.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ajie.chilli.remote.ConnectConfig;
import com.ajie.chilli.remote.RemoteCmd;
import com.ajie.chilli.remote.SessionExt;
import com.ajie.chilli.remote.SshClient;
import com.ajie.chilli.remote.SshSessionMgr;
import com.ajie.chilli.remote.Worker;
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
	private final static Logger logger = LoggerFactory.getLogger(RemoteCmdImpl.class);

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

	public ChannelExec getChannel(SessionExt sessionExt) throws RemoteException {
		try {
			Session session = sessionExt.getSession();
			return (ChannelExec) session.openChannel("exec");
		} catch (JSchException e) {
			sessionExt.recycle();
			throw new RemoteException("打开ChannelExec失败", e);
		}
	}

	@Override
	public String cmd(String cmd) throws RemoteException {
		ssh.execute(new Worker() {
			@Override
			public void run(SessionExt session) {
				try {
					ChannelExec channel = (ChannelExec) session.getSession().openChannel("exec");
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
						Thread.sleep(100);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		return null;
	}

	@Override
	public byte[] byteArrayResultCmd(String cmd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream streamResultCmd(String cmd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void voidResultcmd(String cmd) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws IOException {
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
		config.setBasePath("/var/www/image/");
		SshSessionMgr ssh = new SshSessionMgr(config);
		RemoteCmdImpl rci = new RemoteCmdImpl(ssh);
		rci.cmd("ls");

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
		config.setBasePath("/var/www/image/");
		SshClient client = SshClient.getClient(config);
		SessionExt sessionExt = client.getSession();

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
