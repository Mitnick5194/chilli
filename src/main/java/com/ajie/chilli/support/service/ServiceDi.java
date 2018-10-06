package com.ajie.chilli.support.service;

/**
 * 服务注入
 * 
 * 因pojo就是一堆属性组成的bean，没有任何的调用能力，需对pojo进一步加工，使其能访问服务接口，<Br>
 * 这样就能通过服务接口，调用需要的服务，这样会更体现出面向对象
 * 
 * 泛型P是业务对象对应的pojo，E是业务对象对应的service扩展如： ServiceSupport<TbUser , UserServiceExt>
 * 其中UserService继承UserServiceExt
 * 
 * @author niezhenjie
 */
public abstract class ServiceDi<P, E extends ServiceExt> {
	protected transient E serviceExt;

	public ServiceDi(E serviceExt) {
		this.serviceExt = serviceExt;
	}

	protected E getService() {
		return serviceExt;
	}

	public abstract P toPojo();

}
