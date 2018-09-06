package com.ajie.chilli.support.service;

/**
 * 因pojo就是一堆属性组成的bean，没有任何的调用能力，需对pojo进一步加工，使其能访问服务接口，<Br>
 * 这样就能通过服务接口，调用需要的服务，这样会更体现出面向对象
 * 
 * @author niezhenjie
 */
public abstract class ServiceSupport<P, E extends ServiceExt> {
	protected E serviceExt;

	public ServiceSupport(E serviceExt) {
		this.serviceExt = serviceExt;
	}

	public E getService() {
		return serviceExt;
	}

	public abstract P toPojo();

}
