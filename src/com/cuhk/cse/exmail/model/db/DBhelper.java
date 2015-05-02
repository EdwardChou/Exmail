package com.cuhk.cse.exmail.model.db;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cuhk.cse.exmail.controller.utils.ExMailConstant;
import com.cuhk.cse.exmail.model.Email;
import com.cuhk.cse.exmail.model.app.MyApplication;

/**
 * Create database and tables
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2015-1-29 下午6:09:14
 * @version V1.0
 * 
 */
public class DBhelper extends SQLiteOpenHelper {

	private final static String TAG = DBhelper.class.getSimpleName();

	// multi-thread security for opening and closing database
	private AtomicInteger mOpenCounter = new AtomicInteger();
	private SQLiteDatabase mDatabase;

	public DBhelper(Context context) {
		super(context, ExMailConstant.DATABASE_NAME, null,
				ExMailConstant.DATABASE_VERSION);
	}

	public DBhelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(ExMailConstant.SQL_CREATE_TABLE_INBOX);
		db.execSQL(ExMailConstant.SQL_CREATE_TABLE_DRAFT);
		db.execSQL(ExMailConstant.SQL_CREATE_TABLE_ATTACH);
		db.execSQL(ExMailConstant.SQL_CREATE_TABLE_ACCOUNT);
		db.execSQL(ExMailConstant.SQL_CREATE_TABLE_CONTACT);
	}

	@Override
	public synchronized void close() {
		if (mOpenCounter.decrementAndGet() == 0) {
			// Closing database
			super.close();
			mDatabase.close();
		}
	}

	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		return super.getReadableDatabase();
	}

	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		if (mOpenCounter.incrementAndGet() == 1) {
			// Opening new database
			mDatabase = super.getWritableDatabase();
		}
		return mDatabase;
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// db.execSQL("DROP TABLE IF EXISTS " + ExMailConstant.TABLE_INBOX);
		db.execSQL("DROP TABLE IF EXISTS " + ExMailConstant.TABLE_DRAFT);
		db.execSQL("DROP TABLE IF EXISTS " + ExMailConstant.TABLE_ATTACH);
		db.execSQL("DROP TABLE IF EXISTS " + ExMailConstant.TABLE_ACCOUNT);
		Log.i(TAG, "Upgrade database success!");
		onCreate(db);
	}

	public List<Email> retrieveEmail() {
		List<Email> result = new ArrayList<Email>();
		return result;
	}

	public boolean addEmail(int uid, Email email) {
		if (email != null) {
			ContentValues values = new ContentValues();
			SQLiteDatabase sq = this.getWritableDatabase();
			Cursor cursor = sq.query(ExMailConstant.TABLE_EMAIL, new String[] {
					"msgid", "uid" }, "uid=?", new String[] { uid + "" }, null,
					null, null);
			cursor.moveToNext();
			sq.close();

			if (cursor.getCount() == 0) {
				values.put("address", MyApplication.info.getUserName());
				values.put("uid", uid);
				values.put("msgid", email.getMessageid());
				values.put("from", email.getMailfrom());
				values.put("to", email.getMailto());
				values.put("cc", email.getCc());
				values.put("bcc", email.getBcc());
				values.put("subject", email.getSubject());
				values.put("sentdata", email.getSentdate());
				values.put("content", email.getContent());
				values.put("news", 1);
				values.put("hasread", 0);
				this.getWritableDatabase().insert(ExMailConstant.TABLE_EMAIL,
						null, values);
				this.getWritableDatabase().close();
				return true;
			}
		}
		return false;
	}

}
