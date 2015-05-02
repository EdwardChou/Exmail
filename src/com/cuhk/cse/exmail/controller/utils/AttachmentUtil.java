package com.cuhk.cse.exmail.controller.utils;

import java.io.File;

import android.annotation.SuppressLint;

import com.cuhk.cse.exmail.model.Attachment;

public class AttachmentUtil {

	/**
	 * convert unit of file length
	 * 
	 * @param size
	 * @return ? b / kb / mb / gb
	 */
	@SuppressLint("DefaultLocale")
	public static String convertStorage(long size) {
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;

		if (size >= gb) {
			return String.format("%.1f GB", (float) size / gb);
		} else if (size >= mb) {
			float f = (float) size / mb;
			return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
		} else if (size >= kb) {
			float f = (float) size / kb;
			return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
		} else
			return String.format("%d B", size);
	}

	/**
	 * get full path
	 * 
	 * @param filepath
	 * @return
	 */
	public static String acquireNameFromFilepath(String filepath) {
		int pos = filepath.lastIndexOf('/');
		if (pos != -1) {
			return filepath.substring(pos + 1);
		}
		return "";
	}

	/**
	 * get attachment info
	 * 
	 * @param filePath
	 * @return {@link com.cuhk.cse.exmail.model.Attachment}
	 */
	public static Attachment acquireFileInfo(String filePath) {
		File file = new File(filePath);
		if (!file.exists())
			return null;
		Attachment fileInfo = new Attachment();
		String name = acquireNameFromFilepath(filePath);
		fileInfo.setFilename(name);
		fileInfo.setFiletype(name.substring(name.indexOf(".") + 1));
		fileInfo.setFilepath(filePath);
		fileInfo.setFilesize(file.length());
		return fileInfo;
	}

}
