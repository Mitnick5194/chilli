package com.ajie.chilli.cache;

import com.ajie.chilli.cache.simple.SimpleCacheMgr;

public class Test {

	public static void main(String[] args) {

		CacheMgr cacheMgr = new SimpleCacheMgr(1024, 512, 256);
		final Cache<String, User> cache = CacheFactory.openCache(User.class, true, cacheMgr);

		new Thread(new Runnable() {
			@Override
			public void run() {
				int i = 1;
				while (true) {
					try {
						cache.put(String.valueOf(i), new User("user-" + i++, 25));
						Thread.sleep(500);

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		}).start();

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		while (true) {
			try {
				Thread.sleep(1000);
				cacheMgr.recovery();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

	}
}

class User {
	private String name;
	private int age;

	public User(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public User() {

	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getAge() {
		return age;
	}
}