package com.ajie.chilli.support.service;

/**
 * @author niezhenjie
 */
public class Test {

	public static void main(String[] args) {
		UserService userService = new UserServiceImpl();
		User user = userService.createUser("ajie", 25);
		user.setAge(24);
		user.update();
	}
}

class User extends ServiceSupport<UserPojo, UserServiceExt> {

	int age;
	String name;

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User(UserServiceExt serviceExt) {
		super(serviceExt);
	}

	public void update() {
		UserServiceExt service = getService();
		service.update(toPojo());
	}

	@Override
	public UserPojo toPojo() {
		UserPojo pojo = new UserPojo();
		pojo.setAge(age);
		pojo.setName(name);
		return pojo;
	}

}

/**
 * 向外暴露的接口
 * 
 * @author niezhenjie
 *
 */
interface UserService {
	boolean save();

	User createUser(String name, int age);
}

/**
 * 辅助User接口方法，不对外暴露
 * 
 * @author niezhenjie
 *
 */
interface UserServiceExt extends ServiceExt {
	boolean update(UserPojo user);

}

interface RoleServiceExt extends ServiceExt {
	boolean update(String role);

}

/**
 * 接口实现类 包括对外暴露的方法和不对外暴露的方法
 * 
 * @author niezhenjie
 *
 */
class UserServiceImpl implements UserService, UserServiceExt, RoleServiceExt {

	@Override
	public boolean save() {
		System.out.println("调用userservice save方法");
		return false;
	}

	@Override
	public boolean update(UserPojo user) {
		System.out.println("调用ServiceExt(UserServiceExt)的update方法");
		if (null != user) {
			System.out.println(user.getName());
			System.out.println(user.getAge());
		}
		return false;
	}

	@Override
	public boolean update(String role) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public User createUser(String name, int age) {
		User user = new User(this);
		user.setAge(age);
		user.setName(name);
		return user;
	}

}

class UserPojo {
	String name;
	int age;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}