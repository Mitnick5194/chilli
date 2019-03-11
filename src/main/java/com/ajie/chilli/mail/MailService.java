package com.ajie.chilli.mail;

/**
 * 邮件服务
 *
 * @author niezhenjie
 *
 */
public interface MailService {

	/**
	 * 发送文本邮件
	 * 
	 * @param messenger
	 */
	void send(String messenger);

}
