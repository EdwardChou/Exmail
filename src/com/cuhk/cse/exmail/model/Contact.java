package com.cuhk.cse.exmail.model;

/** 
 * contact info
 * 
 * @author EdwardChou edwardchou_gmail_com 
 * @date 2015-5-2 下午8:49:01 
 * @version V1.0   
 *  
 */
public class Contact {

	private String name;
	private String address;
	private String pinyin;

	public Contact(String name, String address, String pinyin) {
		super();
		this.name = name;
		this.address = address;
		this.pinyin = pinyin;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

}
