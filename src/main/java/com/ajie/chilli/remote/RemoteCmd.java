package com.ajie.chilli.remote;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;

/**
 * 远程指令
 *
 * @author niezhenjie
 *
 */
public class RemoteCmd {

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
				System.out.println(new String(out.toByteArray(),"utf-8"));
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
		}
	}

}
