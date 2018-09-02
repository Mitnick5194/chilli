package com.ajie.support.service;

import java.util.ArrayList;

/**
 * 辅助POJO类可以使用服务功能
 * 
 * @author niezhenjie
 */
public class ServiceSupport<P> {
	protected ServiceExt serviceExt;

	public ServiceSupport(ServiceExt serviceExt) {
		this.serviceExt = serviceExt;
	}

	public  P valueOf(P p) {
		return null;
	}

	public boolean update(P p) {
		serviceExt.update(p);
		return true;
	}
	
	public static void main(String[] args) {
		ServiceSupport<String> ss = new ServiceSupport<String>(null);
		ss.update(new String());
		ss.valueOf(new ArrayList<Integer>());
	}
}
