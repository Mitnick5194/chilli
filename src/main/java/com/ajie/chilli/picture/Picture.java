package com.ajie.chilli.picture;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Picture {

	/** 服务器保存名 */
	private String name;

	/** 图片原名字，可能没有 */
	private String originName;

	/** 保存地址 */
	private String address;

	/** 类型 如jpg png */
	private String type;

	/** 图片大小 单位字节 */
	private double size;

	/** 图片宽 */
	private double width;

	/** 图片高 */
	private double height;

	/** 绑定额外信息 */
	private String extra;

	/** 流 */
	private InputStream stream;

	/** 保存流，防止多次读取时stream丢失问题 */
	private ByteArrayOutputStream outStream;

	public Picture(InputStream stream) {
		this.stream = stream;
	}

	public Picture() {

	}

	public void setByteArrayOutputStream(ByteArrayOutputStream out) {
		outStream = out;
	}

	public ByteArrayOutputStream getByteArrayOutputStream() {
		return outStream;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String extra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public InputStream getInputStream() {
		return stream;
	}

	public void setInputStream(InputStream stream) {
		this.stream = stream;
	}

	public String getOriginName() {
		return originName;
	}

	public void setOriginName(String originName) {
		this.originName = originName;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public InputStream getStream() {
		return stream;
	}

	public void setStream(InputStream stream) {
		this.stream = stream;
	}

	public String getExtra() {
		return extra;
	}

}
