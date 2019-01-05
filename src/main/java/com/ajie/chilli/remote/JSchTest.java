package com.ajie.chilli.remote;

import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 *
 *
 * @author niezhenjie
 *
 */
public class JSchTest {
	public static void main(String[] args) {
		JSch jsch = new JSch();
		try {
			Session session = jsch.getSession("ajie", "www.ajie18.top", 22);
			if (null == session) {
				System.out.println("打开失败 ，session为空");
			}
			//session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword("niezhenjie22");
			session.connect(30000);
			System.out.println("连接成功");
		} catch (JSchException e) {
			e.printStackTrace();
		}
		/*InputStream in = null;
		try {
			in = new FileInputStream(new File("C:/Users/ajie/Desktop/origin_image006.png"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			uploadBySftp("www.ajie18.top", "ajie", "niezhenjie22", 22, "/var/www/", "image/",
					"test.png", in);
			System.out.println("上传成功");
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	public static boolean uploadBySftp(String ip, String username, String password, int port,
			String basePath, String filePath, String fileName, InputStream is) throws Exception {
		boolean result = false;
		Session session = null;
		Channel channel = null;
		String fullPath = null;

		JSch jsch = new JSch();
		// jsch.setKnownHosts(new FileInputStream(new
		// File("C:/Users/ajie/.ssh/known_hosts")));
		// 采用指定的端口连接服务器
		session = jsch.getSession(username, ip, port);

		// 如果服务器连接不上，则抛出异常
		if (session == null) {
			throw new Exception("session is null");
		}

		// 设置登陆主机的密码
		session.setPassword(password); // 设置密码
		// 设置第一次登陆的时候提示，可选值：(ask | yes | no)
		session.setConfig("StrictHostKeyChecking", "no");
		// 设置登陆超时时间
		session.connect(10000);

		try {
			// 创建sftp通信通道
			channel = (Channel) session.openChannel("sftp");
			channel.connect(1000);
			ChannelSftp sftp = (ChannelSftp) channel;

			fullPath = basePath + filePath;

			// 判断存放目录是否存在，如不存在就创建目录
			// try {
			// sftp.ls(fullPath);
			// } catch (Exception e) {
			// sftp.mkdir(fullPath);
			// }
			// 核心：使用mkdir方法远程创建目录只能一级一级创建，cd方法进入目录会被下一个cd方法的路径覆盖
			// 创建目录必须先进入到要创建目录的上一级位置
			// 使用JSch远程创建递归目录，最后得到可以直接进入且存放图片的path
			String[] folders = fullPath.split("/");
			// 返回的结果数组比预计的数组多了一个元素，第一个元素为空
			System.out.println(folders.length);
			String path = "";
			// 第一个元素是空字符串，所以从第二个元素开始遍历，即i=1
			for (int i = 1; i < folders.length; i++) {
				String folder = folders[i];
				path = path + "/" + folder;
				try {
					// 用是否能进入该目录来判断该目录是否存在
					sftp.cd(path);
				} catch (SftpException e) {
					// 上面不能进入则报错，执行以下的创建命令
					sftp.mkdir(path);
				}
			}
			System.out.println(path);

			// 进入服务器指定的文件夹
			sftp.cd(path);

			// 以下代码实现从本地上传一个文件到服务器，如果要实现下载，对换以下流就可以了
			OutputStream os = sftp.put(fileName);
			// InputStream instream = new FileInputStream(new
			// File("c:/print.txt"));

			byte b[] = new byte[1024];
			int n;
			while ((n = is.read(b)) != -1) {
				os.write(b, 0, n);
			}
			result = true;
			os.flush();
			os.close();
			is.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return result;
		} finally {
			session.disconnect();
			channel.disconnect();
		}
	}

}
