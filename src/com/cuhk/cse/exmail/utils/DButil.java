package com.cuhk.cse.exmail.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DButil extends SQLiteOpenHelper {
	public DButil(Context context) {
		super(context, "emailconstants.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// db.execSQL("create table email(id INTEGER PRIMARY KEY AUTOINCREMENT,mailfrom varchar(20),name varchar(20),address varchar(20))");
		// db.execSQL("create table caogaoxiang(id INTEGER PRIMARY KEY AUTOINCREMENT,mailfrom varchar(20),mailto varchar(20),subject varchar(20),content text)");
		// db.execSQL("create table attachment(id INTEGER PRIMARY KEY AUTOINCREMENT,filename varchar(20),filepath varchar(100),filesize varchar(20),mailid varchar(20))");
		// db.execSQL("create table emailstatus(id INTEGER PRIMARY KEY AUTOINCREMENT,mailfrom varchar(20),messageid varchar(100))");
		db.execSQL("create table email(emid INTEGER PRIMARY KEY AUTOINCREMENT,uid varchar(100),messageid varchar(100),"
				+ "mailfrom varchar(20),mailto varchar(20),cc varchar(20),bcc varchar(20),subject varchar(20),sentdate varchar(20),"
				+ "content text,flagread varchar(20),flagstate varchar(20),mark varchar(20),useraccount varchar(20),ishtml varchar(20),"
				+ "digest text,attach,blob)");
		db.execSQL("create table emailuser(usid INTEGER PRIMARY KEY AUTOINCREMENT,useraccount varchar(20),address varchar(20),"
				+ "name varchar(20),password varchar(100))");
		db.execSQL("create table draft(drid INTEGER PRIMARY KEY AUTOINCREMENT,mailfrom varchar(20),mailto varchar(20),subject varchar(20),"
				+ "content text)");
		db.execSQL("create table contact(coid INTEGER PRIMARY KEY AUTOINCREMENT,name varchar(20),address varchar(20),birthday varchar(20),"
				+ "tel varchar(20),cgid varchar(20),remarks varchar(100),useraccount varchar(20))");
		db.execSQL("create table contactgroup(cgid INTEGER PRIMARY KEY AUTOINCREMENT,groupname varchar(20))");
		db.execSQL("create table attachment(atid INTEGER PRIMARY KEY AUTOINCREMENT,filename varchar(20),filepath varchar(100),"
				+ "filesize varchar(20),filetype varchar(20),emid varchar(20))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
