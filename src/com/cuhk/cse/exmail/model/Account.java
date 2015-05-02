package com.cuhk.cse.exmail.model;

import java.io.Serializable;

/**
 * JavaBean for Email user account info
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2015-4-5 下午2:15:00
 * @version V1.0
 * 
 */
public class Account implements Serializable {

	private static final long serialVersionUID = 1L;

	private int accountid = 0;
	private String username = "";
	private String address = "";
	private String password = "";
	private int lastLoginTime = 0;

	public int getAccountid() {
		return accountid;
	}

	public void setAccountid(int accountid) {
		this.accountid = accountid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getLastlogintime() {
		return lastLoginTime;
	}

	public void setLastlogintime(int lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

}
