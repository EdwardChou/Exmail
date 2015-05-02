package com.cuhk.cse.exmail.model;

import java.io.Serializable;

/**
 * email entity
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2015-4-11 下午5:38:41
 * @version V1.0
 * 
 */
public class Email implements Serializable {

	private static final long serialVersionUID = 1L;

	private int emailID;
	private String address;
	private int uid;
	private String messageID;
	private String sendername;
	private String mailfrom;
	private String mailto;
	private String cc;
	private String bcc;
	private String subject;
	private String sentdate;
	private String digest;
	private String content;
	private boolean hasread;
	private boolean replysign;
	private boolean ishtml;
	private String charset;
	private boolean hasattach;

	// private ArrayList<String> attachments;

	public boolean getHasattach() {
		return hasattach;
	}

	public void setHasattach(boolean hasattach) {
		this.hasattach = hasattach;
	}

	public int getEmailid() {
		return emailID;
	}

	public void setEmailid(int emailID) {
		this.emailID = emailID;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getMessageid() {
		return messageID;
	}

	public void setMessageid(String messageID) {
		this.messageID = messageID;
	}

	public String getSendername() {
		return sendername;
	}

	public void setSendername(String sendername) {
		this.sendername = sendername;
	}

	public String getMailfrom() {
		return mailfrom;
	}

	public void setMailfrom(String from) {
		this.mailfrom = from;
	}

	public String getMailto() {
		return mailto;
	}

	public void setMailto(String to) {
		this.mailto = to;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getBcc() {
		return bcc;
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSentdate() {
		return sentdate;
	}

	public void setSentdate(String sentdate) {
		this.sentdate = sentdate;
	}

	public String getDigest() {
		return digest;
	}

	public void setDigest(String digest) {
		this.digest = digest;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean getHasread() {
		return hasread;
	}

	public void setHasread(boolean hasread) {
		this.hasread = hasread;
	}

	public boolean getReplysign() {
		return replysign;
	}

	public void setReplysign(boolean replysign) {
		this.replysign = replysign;
	}

	public boolean getIshtml() {
		return ishtml;
	}

	public void setIshtml(boolean html) {
		this.ishtml = html;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

}
