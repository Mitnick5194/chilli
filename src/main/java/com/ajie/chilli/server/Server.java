package com.ajie.chilli.server;

/**
 * 服务器信息<br>
 * 注意：jvm的最大内存可以从配置文件更改，eclipse可以通过eclipse.ini修改JAVA_OPTS='-Xms256m -Xmx512m'
 * 　　（表示初始化内存为256MB，可以使用的最大内存为512MB）。其他web容器（如tomcat、jetty）
 * 可以通过启动容器的线程配置加上上面的配置进行修改
 *
 * @author niezhenjie
 *
 */
public interface Server {

	/**
	 * 操作系统架构
	 * 
	 * @return
	 */
	String getArch();

	/**
	 * 操作系统名称
	 * 
	 * @return
	 */
	String getSystemName();

	/**
	 * 操作系统版本
	 * 
	 * @return
	 */
	String getVersion();

	/**
	 * JVM可以从宿主机获取到最大内存，注意与totalMemory的区别，操作系统不是一次性将jvm所需要最大的内存都分配给它，
	 * 而是等到jvm需要的时候再适量的分配
	 * 
	 * @return
	 */
	long getMaxMemory();

	/**
	 * 此刻宿主机分配给JVM的内存（注意，并不是最大内存）
	 * 
	 * @return
	 */
	long getTotalMemory();

	/**
	 * cpu数量
	 * 
	 * @return
	 */
	int getAvailableProcessors();

	/**
	 * 已使用内存（totalMemory - freeMemory）
	 * 
	 * @return
	 */
	long getUseMemory();

	/**
	 * JVM空闲内存（totalMemory - useMemory）
	 * 
	 * @return
	 */
	long getFreeMemory();

	/**
	 * 真正的剩余内存（maxMemory - useMemory）
	 * 
	 * @return
	 */
	long getRealFreeMeory();

	/**
	 * 最后一分钟内系统加载平均值
	 * 
	 * @return
	 */
	double getSystemLoadAverage();
}
