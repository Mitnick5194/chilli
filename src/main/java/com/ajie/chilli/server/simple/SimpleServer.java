package com.ajie.chilli.server.simple;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import com.ajie.chilli.server.Server;

/**
 * 服务器信息
 *
 * @author niezhenjie
 *
 */
public class SimpleServer implements Server {
	
	

	protected Runtime runtime;

	protected OperatingSystemMXBean operatingSystemMXBean;

	public SimpleServer() {
		
		runtime = Runtime.getRuntime();
		operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
	}

	@Override
	public String getArch() {
		return operatingSystemMXBean.getArch();
	}

	@Override
	public String getSystemName() {
		return operatingSystemMXBean.getName();
	}

	@Override
	public String getVersion() {
		return operatingSystemMXBean.getVersion();
	}

	@Override
	public long getMaxMemory() {
		return runtime.maxMemory();
	}

	@Override
	public long getTotalMemory() {
		return runtime.totalMemory();
	}

	@Override
	public int getAvailableProcessors() {
		return runtime.availableProcessors();
	}

	@Override
	public long getUseMemory() {

		return getTotalMemory() - getFreeMemory();
	}

	@Override
	public long getFreeMemory() {
		return runtime.freeMemory();
	}

	@Override
	public long getRealFreeMeory() {
		return getMaxMemory() - getUseMemory();
	}

	@Override
	public double getSystemLoadAverage() {
		return operatingSystemMXBean.getSystemLoadAverage();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{name:").append(getSystemName()).append(",");
		sb.append("arch:").append(getArch()).append(",");
		sb.append("maxMemory:").append(getMaxMemory()).append(",");
		sb.append("totalMemory:").append(getTotalMemory()).append(",");
		sb.append("availableProcessors:").append(getAvailableProcessors()).append(",");
		sb.append("useMemory:").append(getUseMemory()).append(",");
		sb.append("freeMemory:").append(getFreeMemory()).append(",");
		sb.append("realFreeMeory:").append(getRealFreeMeory()).append(",");
		sb.append("systemLoadAverage:").append(getSystemLoadAverage()).append("}");
		return sb.toString();
	}

	public static void main(String[] args) {
		Server server = new SimpleServer();
		System.out.println(server.getArch());
		System.out.println(server.getSystemName());
		System.out.println(server.getMaxMemory() / 1024 / 1024);
		System.out.println(server.getTotalMemory() / 1024 / 1024);
		System.out.println(server.getAvailableProcessors());
		System.out.println(server.getUseMemory() / 1024 / 1024);
		System.out.println(server.getFreeMemory() / 1024 / 1024);
		System.out.println(server.getRealFreeMeory() / 1024 / 1024);
		System.out.println(server.getSystemLoadAverage());
		System.out.println(server.toString());

	}

}
