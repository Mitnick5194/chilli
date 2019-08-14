package com.ajie.chilli.support;

import java.util.ArrayList;

/**
 * 集中管理所有的关闭回调
 * 
 * @author niezhenjie
 *
 */
public class ShutdownHook extends Thread {

	public static ArrayList<Destroy> hooks;
	static {
		hooks = new ArrayList<Destroy>();
	}

	public ShutdownHook() {
		this("thread-hook");
	}
	
	public ShutdownHook(String name){
		super.setName(name);
		Runtime.getRuntime().addShutdownHook(this);
	}

	public static void register(final Destroy destroy) {
		synchronized (hooks) {
			hooks.add(destroy);
			hooks.trimToSize();
		}
	}

	@Override
	public void run() {
		super.run();
		for (Destroy hook : hooks) {
			hook.destroy();
		}
	}
}
