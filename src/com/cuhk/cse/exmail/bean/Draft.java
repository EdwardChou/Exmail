package com.cuhk.cse.exmail.bean;

import java.io.Serializable;

public class Draft implements Serializable {

	private static final long serialVersionUID = 1L;
	private int draftid;

	private String address;
	private String mailto;
	private String subject;
	private String content;
	private boolean isHtml;

	public Draft() {

	}

	public Draft(String address, String mailto, String subject, String content,
			boolean isHtml) {
		super();
		this.address = address;
		this.mailto = mailto;
		this.subject = subject;
		this.content = content;
		this.isHtml = isHtml;
	}

	public int getDraftid() {
		return draftid;
	}

	public void setDraftid(int draftid) {
		this.draftid = draftid;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getMailto() {
		return mailto;
	}

	public void setMailto(String mailto) {
		this.mailto = mailto;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean getIshtml() {
		return isHtml;
	}

	public void setIshtml(boolean isHtml) {
		this.isHtml = isHtml;
	}
}
