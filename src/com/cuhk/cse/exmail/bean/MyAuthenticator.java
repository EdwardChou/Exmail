package com.cuhk.cse.exmail.bean;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class MyAuthenticator extends Authenticator {

	String userName = null;
	String password = null;
	
	// static {
	// Security.addProvider(new JSSEProvider());
	// }

	public MyAuthenticator() {
	}

	public MyAuthenticator(String username, String password) {
		this.userName = username;
		this.password = password;
	}

	/**
	 * 登入校验
	 */
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(userName, password);
	}
}
