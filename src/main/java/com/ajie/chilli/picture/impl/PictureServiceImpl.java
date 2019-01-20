package com.ajie.chilli.picture.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ajie.chilli.picture.Picture;
import com.ajie.chilli.picture.PictureException;
import com.ajie.chilli.picture.PictureService;
import com.ajie.chilli.remote.ConnectConfig;
import com.ajie.chilli.remote.SshSessionMgr;
import com.ajie.chilli.remote.UploadService;
import com.ajie.chilli.remote.exception.RemoteException;
import com.ajie.chilli.remote.impl.AsynSshSessionMgr;
import com.ajie.chilli.remote.impl.UploadServiceImpl;
import com.ajie.chilli.utils.Toolkits;
import com.ajie.chilli.utils.common.StringUtils;

/**
 * 图片服务，所有上传的图片保存路径都是相对于图片服务器
 *
 * @author niezhenjie
 *
 */
public class PictureServiceImpl implements PictureService {
	private final static Logger logger = LoggerFactory.getLogger(PictureServiceImpl.class);
	/** 上传服务 */
	private UploadService uploadService;

	/** 图片服务器访问链接对应的目录，如/var/www/images/ */
	private final String rootpath;

	/** 图片服务器url (即访问上面的rootpath链接) */
	private final String serverURL;

	/** 图片名字前缀 */
	private String prefix;

	public PictureServiceImpl(UploadService uploadService, String path, String serverURL) {
		this.uploadService = uploadService;
		this.rootpath = path;
		this.serverURL = serverURL;
		prefix = DEFAULT_PREFIX;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Picture create(InputStream stream) throws PictureException {
		return create(null, null, stream);
	}

	@Override
	public Picture create(String address, InputStream stream) throws PictureException {
		return create(address, null, stream);
	}

	@Override
	public Picture get(String address) throws PictureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Picture createForName(String fileName, InputStream stream) throws PictureException {
		return create(null, fileName, stream);
	}

	@Override
	public Picture create(String address, String fileName, InputStream stream)
			throws PictureException {
		try {
			String uniqueKey = Toolkits.uniqueKey(32);
			String path = genPath(address);
			String name = prefix + uniqueKey; // 服务器保存名字
			Picture picture = genPicture(stream);
			if (null == picture)
				return null;
			name += "." + picture.getType();
			picture.setAddress(genURL(address, name));
			picture.setOriginName(fileName);
			picture.setName(name);// 服务器显示名字
			ByteArrayOutputStream out = picture.getByteArrayOutputStream();
			uploadService.upload(out.toByteArray(), path, name);
			return picture;
		} catch (RemoteException e) {
			logger.error("图片上传失败", e);
			throw new PictureException("图片上传失败", e);
		} catch (ImageReadException e) {
			logger.error("图片上传失败", e);
			throw new PictureException("图片上传失败", e);
		} catch (IOException e) {
			logger.error("图片上传失败", e);
			throw new PictureException("图片上传失败", e);
		}
	}

	/**
	 * 根据传入的路径生成绝对路径
	 * 
	 * @param address
	 * @return
	 */
	private String genPath(String address) {
		StringBuilder sb = new StringBuilder();
		sb.append(rootpath);
		if (!rootpath.endsWith("/")) {
			sb.append("/");
		}
		if (StringUtils.isEmpty(address))
			return sb.toString();
		if (address.startsWith("/")) // 防止绝地路径
			address = address.substring(1);
		sb.append(address);
		if (!address.endsWith("/"))
			sb.append("/");
		return sb.toString();
	}

	private String genURL(String address, String fileName) {
		StringBuilder sb = new StringBuilder();
		sb.append(serverURL);
		if (!serverURL.endsWith("/")) {
			sb.append("/");
		}
		if (null == address) {
			sb.append(fileName);
			return sb.toString();
		}
		if (address.startsWith("/"))
			address.substring(1);
		if (!address.endsWith("/")) {
			sb.append("/");
		}
		sb.append(address);
		sb.append(fileName);
		return sb.toString();
	}

	private Picture genPicture(InputStream stream) throws IOException, ImageReadException {
		Picture pic = new Picture(stream);
		pic.setSize(stream.available());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int size = 1024;
		byte[] buf = new byte[size];
		int n = 0, available = 0;
		do {
			available = stream.available();
			n = stream.read(buf);
			out.write(buf, 0, n);
		} while (available > size);
		byte[] bytes = out.toByteArray();
		ImageInfo info = Imaging.getImageInfo(bytes);
		pic.setType(info.getFormat().getName());
		pic.setWidth(info.getWidth());
		pic.setHeight(info.getHeight());
		pic.setByteArrayOutputStream(out);// 保存流，因为上面已经把stream读取过了
		return pic;
	}

	public static void main(String[] args) throws IOException {
		InputStream stream = new FileInputStream(new File(
				"C:/Users/ajie/Desktop/arrow_top.png"));
		Properties prop = new Properties();
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("server.properties");
		prop.load(is);
		String host = prop.getProperty("host");
		String passwd = prop.getProperty("passwd");
		String name = prop.getProperty("name");
		ConnectConfig config = ConnectConfig.valueOf(name, passwd, host, 22);
		// timeout一般来说需要设置大一点，否则会出现各种超时
		config.setTimeout(3000);
		config.setMax(10);
		config.setCore(0);
		// final AsynSshSessionMgr mgr = new AsynSshSessionMgr(config);
		final SshSessionMgr mgr = new AsynSshSessionMgr(config);
		final UploadService upload = new UploadServiceImpl(mgr);
		PictureService pictureService = new PictureServiceImpl(upload, "/var/www/image", host
				+ "/image");
		try {
			Picture pic = pictureService.create(stream);
			System.out.println(pic.getAddress());
		} catch (PictureException e) {
			e.printStackTrace();
		}
	}

}
