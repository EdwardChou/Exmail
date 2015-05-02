package com.cuhk.cse.exmail.controller.utils;

/**
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2015-4-5 上午11:54:34
 * @version V1.0
 * 
 */
public class ExMailConstant {

	/**
	 * database
	 */
	public static final String DATABASE_NAME = "exmail.db";
	public static final int DATABASE_VERSION = 1;
	// table
	public static final String TABLE_EMAIL = "email";
	public static final String TABLE_DRAFT = "draft";
	public static final String TABLE_ATTACH = "attachment";
	public static final String TABLE_ACCOUNT = "account";
	public static final String TABLE_CONTACT = "contact";
	// column
	public static final String COLUMN_ADDRESS = "address";
	public static final String COLUMN_CONTENT = "content";
	public static final String COLUMN_MAIL_FROM = "mailfrom";
	public static final String COLUMN_MAIL_TO = "mailto";
	public static final String COLUMN_LASTLOGINTIME = "lastlogintime";
	public static final String COLUMN_SENDER_NAME = "sendername";
	public static final String COLUMN_SUBJECT = "subject";
	public static final String COLUMN_UID = "uid";
	public static final String COLUMN_CONTACT_NAME = "contactname";
	public static final String COLUMN_PINYIN = "pinyin";
	public static final String COLUMN_USERNAME = "username";
	// sql statement
	public static final String SQL_CREATE_TABLE_INBOX = "CREATE TABLE "
			+ TABLE_EMAIL + "(emailid INTEGER PRIMARY KEY autoincrement,"
			+ COLUMN_ADDRESS + " TEXT," + COLUMN_UID + " INTEGER,"
			+ "messageid TEXT," + COLUMN_SENDER_NAME + " TEXT,"
			+ COLUMN_MAIL_FROM + " TEXT," + COLUMN_MAIL_TO + " TEXT,"
			+ "cc TEXT," + "bcc TEXT," + COLUMN_SUBJECT + " TEXT,"
			+ "sentdate TEXT," + "digest TEXT," + COLUMN_CONTENT + " TEXT,"
			+ "hasread INTEGER," + "replysign INTEGER," + "ishtml INTEGER,"
			+ "charset TEXT," + "hasattach INTEGER)";
	public static final String SQL_CREATE_TABLE_DRAFT = "CREATE TABLE "
			+ TABLE_DRAFT + "(draftid INTEGER PRIMARY KEY autoincrement,"
			+ COLUMN_ADDRESS + " TEXT," + COLUMN_MAIL_TO + " TEXT,"
			+ COLUMN_SUBJECT + " TEXT," + COLUMN_CONTENT + " TEXT,"
			+ "ishtml INTEGER)";
	public static final String SQL_CREATE_TABLE_ATTACH = "CREATE TABLE "
			+ TABLE_ATTACH + "(attachmentid INTEGER PRIMARY KEY autoincrement,"
			+ COLUMN_ADDRESS + " TEXT," + COLUMN_UID + " INTEGER,"
			+ "draftid INTEGER," + "filename TEXT," + "filesize INTEGER,"
			+ "filetype TEXT," + "filepath TEXT," + "inputstream BLOB)";
	public static final String SQL_CREATE_TABLE_ACCOUNT = "CREATE TABLE "
			+ TABLE_ACCOUNT + "(accountid INTEGER PRIMARY KEY autoincrement,"
			+ COLUMN_USERNAME + " TEXT," + COLUMN_ADDRESS + " TEXT,"
			+ "password TEXT," + COLUMN_LASTLOGINTIME + " INTEGER)";
	public static final String SQL_CREATE_TABLE_CONTACT = "CREATE TABLE "
			+ TABLE_CONTACT + "(contactid INTEGER PRIMARY KEY autoincrement,"
			+ COLUMN_USERNAME + " TEXT," + COLUMN_CONTACT_NAME + " TEXT,"
			+ COLUMN_ADDRESS + " TEXT," + "password TEXT," + COLUMN_PINYIN
			+ " TEXT)";

	/**
	 * folder
	 */
	public static final int FOLDER_INBOX = -1;
	public static final int FOLDER_READ = 1;
	public static final int FOLDER_UNREAD = 0;

	public static final String EMAIL_COUNT = "emailcount";

	/**
	 * mail server info
	 */
	public static final String STORE_IMAPS = "imaps";
	public static final String STORE_IMAP = "imap";
	public static final String STORE_POP3S = "pop3s";
	public static final String STORE_POP3 = "pop3";

}
