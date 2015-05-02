package com.cuhk.cse.exmail.model;

import java.io.Serializable;

public class Attachment implements Serializable {

	private static final long serialVersionUID = 1L;

	private int attachmentid;
	private String address;
	private int uid;
	private int draftid;
	private String fileName;
	private long fileSize;
	private String filetype;
	private String filePath;
	// binary attachment
	private byte[] inputStream;

	public Attachment() {
		super();
	}

	public Attachment(String filePath, String fileName, long fileSize) {
		super();
		this.filePath = filePath;
		this.fileName = fileName;
		this.fileSize = fileSize;
	}

	public int getAttachmentid() {
		return attachmentid;
	}

	public void setAttachmentid(int attachmentid) {
		this.attachmentid = attachmentid;
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

	public int getDraftid() {
		return draftid;
	}

	public void setDraftid(int draftid) {
		this.draftid = draftid;
	}

	public String getFilename() {
		return fileName;
	}

	public void setFilename(String fileName) {
		this.fileName = fileName;
	}

	public long getFilesize() {
		return fileSize;
	}

	public void setFilesize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFiletype() {
		return filetype;
	}

	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}

	public String getFilepath() {
		return filePath;
	}

	public void setFilepath(String filePath) {
		this.filePath = filePath;
	}

	public byte[] getInputstream() {
		return inputStream;
	}

	public void setInputstream(byte[] inputStream) {
		this.inputStream = inputStream;
	}

}
