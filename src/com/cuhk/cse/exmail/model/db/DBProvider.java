package com.cuhk.cse.exmail.model.db;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pinyin4j.PinyinHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cuhk.cse.exmail.controller.utils.EncryptionUtil;
import com.cuhk.cse.exmail.controller.utils.ExMailConstant;
import com.cuhk.cse.exmail.controller.utils.PinyinUtil;
import com.cuhk.cse.exmail.model.Account;
import com.cuhk.cse.exmail.model.Attachment;
import com.cuhk.cse.exmail.model.Contact;
import com.cuhk.cse.exmail.model.Draft;
import com.cuhk.cse.exmail.model.Email;
import com.cuhk.cse.exmail.model.app.MyApplication;

/**
 * Database provider for CURD operation
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2015-4-5 下午12:22:39
 * @version V1.0
 * 
 */
public class DBProvider {

	private final String TAG = DBProvider.class.getSimpleName();

	private static DBhelper mDBhelper;
	private SQLiteDatabase db;
	private static DBProvider instance;

	public DBProvider(Context context) {
		mDBhelper = new DBhelper(context);
	}

	public static synchronized void init(Context context) {
		if (instance == null) {
			instance = new DBProvider(context);
		}
	}

	public static synchronized DBProvider getInstance() {
		if (instance == null) {
			throw new IllegalStateException(DBProvider.class.getSimpleName()
					+ "is not initialized, call init(..) method first");
		}
		return instance;
	}

	public static void closeDB() {
		mDBhelper.close();
	}

	/**
	 * basic insert, delete, query, update operation
	 */
	public int insertRecord(String table, String nullColumnHack,
			ContentValues values) {
		if (null == db || db.isReadOnly()) {
			db = mDBhelper.getWritableDatabase();
		}
		if (null == values || table == null || "".equals(table)) {
			Log.i(TAG, "insert: table or values parameters is null !");
			return -1;
		}
		return (int) db.insert(table, nullColumnHack, values);
	}

	public boolean deleteRecord(String table, String whereClause,
			String[] whereArgs) {
		if (null == db || db.isReadOnly()) {
			db = mDBhelper.getWritableDatabase();
		}
		if (null == table || null == whereClause || null == whereArgs) {
			Log.i(TAG,
					"delete: table or whereClause or whereArgs parameters is null !");
			return false;
		}
		return 0 == db.delete(table, whereClause, whereArgs) ? false : true;
	}

	public Cursor queryRecord(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		if (null == db || !db.isReadOnly()) {
			db = mDBhelper.getReadableDatabase();
		}
		if (null == table) {
			Log.i(TAG, "query: table parameter is null !");
			return null;
		}
		return db.query(table, columns, selection, selectionArgs, groupBy,
				having, orderBy);
	}

	public boolean updateRecord(String table, ContentValues values,
			String whereClause, String[] whereArgs) {
		if (null == db || db.isReadOnly()) {
			db = mDBhelper.getWritableDatabase();
		}
		if (null == table || null == values) {
			Log.i(TAG, "update: table or values parameters is null !");
			return false;
		}
		return 0 == db.update(table, values, whereClause, whereArgs);
	}

	/**
	 * account operation
	 */
	public Account queryAccount() {
		Account account = null;
		Cursor cursor = queryRecord(ExMailConstant.TABLE_ACCOUNT, null, null,
				null, null, null, ExMailConstant.COLUMN_LASTLOGINTIME + " desc");
		if (null != cursor) {
			if (cursor.moveToNext()) {
				account = new Account();
				if (null != cursor.getString(2) && null != cursor.getString(3)) {
					account.setAddress(null == cursor.getString(2) ? ""
							: cursor.getString(2));
					account.setPassword(EncryptionUtil.decrypt(cursor
							.getString(3)));
				}
			}
		}
		return account;
	}

	public boolean insertAccount(Account account) {
		if (null == account) {
			Log.i(TAG, "account info is null !");
			return false;
		}
		ContentValues values = new ContentValues();
		values.put(ExMailConstant.COLUMN_ADDRESS, account.getAddress());
		values.put("password", EncryptionUtil.encrypt(account.getPassword()));
		values.put(ExMailConstant.COLUMN_LASTLOGINTIME,
				account.getLastlogintime());
		return -1 == insertRecord(ExMailConstant.TABLE_ACCOUNT, null, values) ? false
				: true;
	}

	public boolean deleteAccount(String address) {
		if (null == address) {
			Log.i(TAG, "address info is null !");
			return false;
		}
		return deleteRecord(ExMailConstant.TABLE_ACCOUNT,
				ExMailConstant.COLUMN_ADDRESS + "=?", new String[] { address });
	}

	/**
	 * Email operation
	 */
	public List<String> queryLocalUid() {
		Cursor cursor = queryRecord(ExMailConstant.TABLE_EMAIL, null, null,
				null, null, null, null);
		List<String> uidsList = null;
		if (null != cursor) {
			uidsList = new ArrayList<String>();
			while (cursor.moveToNext()) {
				uidsList.add(cursor.getString(1) + cursor.getInt(2));
			}
		}
		return uidsList;
	}

	public List<Email> queryEmailInfo(String account, int folder) {
		List<Email> resultList = null;

		String selection = "";
		String[] selectionArgs = null;
		if (folder == ExMailConstant.FOLDER_INBOX) {
			selection = ExMailConstant.COLUMN_ADDRESS + "=?";
			selectionArgs = new String[] { account };
		} else {
			selection = ExMailConstant.COLUMN_ADDRESS + "=?" + "and hasread=?";
			selectionArgs = new String[] { account, String.valueOf(folder) };
		}

		Cursor cursor = queryRecord(ExMailConstant.TABLE_EMAIL, null,
				selection, selectionArgs, null, null, null);
		if (null != cursor) {
			resultList = new ArrayList<Email>();
			while (cursor.moveToNext()) {
				Email email = new Email();
				try {
					email.setUid(cursor.getInt(2));
					email.setSendername(cursor.getString(4));
					email.setMailfrom(cursor.getString(5));
					email.setSubject(cursor.getString(9));
					email.setSentdate(cursor.getString(10));
					email.setDigest(cursor.getString(11));
					email.setHasread(cursor.getInt(13) == 1 ? true : false);
					email.setHasattach(cursor.getInt(17) == 1 ? true : false);
					resultList.add(email);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return resultList;
	}

	public Object[] queryEmailDetail(String address, int uid) {
		Object[] object = new Object[2];
		Cursor emailCursor = queryRecord(ExMailConstant.TABLE_EMAIL, null,
				ExMailConstant.COLUMN_ADDRESS + "=? and "
						+ ExMailConstant.COLUMN_UID + "=?", new String[] {
						address, String.valueOf(uid) }, null, null, null);
		if (null != emailCursor) {
			Email email = new Email();
			if (emailCursor.moveToNext()) {
				try {
					email.setEmailid(emailCursor.getInt(0));
					email.setAddress(emailCursor.getString(1));
					email.setUid(emailCursor.getInt(2));
					email.setSendername(emailCursor.getString(4));
					email.setMailfrom(emailCursor.getString(5));
					email.setCc(emailCursor.getString(7));
					email.setBcc(emailCursor.getString(8));
					email.setSubject(emailCursor.getString(9));
					email.setSentdate(emailCursor.getString(10));
					email.setContent(emailCursor.getString(12));
					email.setHasread(emailCursor.getInt(13) == 1 ? true : false);
					email.setReplysign(emailCursor.getInt(14) == 1 ? true
							: false);
					email.setIshtml(emailCursor.getInt(15) == 1 ? true : false);
					email.setCharset(emailCursor.getString(16));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			object[0] = email;
			emailCursor.close();
			object[1] = queryInboxAttachments(uid);
		}

		return object;
	}

	public boolean insertEmail(Email email) {
		if (null == email) {
			Log.i(TAG, "email info is null !");
			return false;
		}
		ContentValues values = getEmailContentValues(email);
		return -1 == insertRecord(ExMailConstant.TABLE_EMAIL, null, values) ? false
				: true;
	}

	private ContentValues getEmailContentValues(Email email) {
		ContentValues values = new ContentValues();
		values.put(ExMailConstant.COLUMN_ADDRESS,
				MyApplication.info.getUserName());
		values.put(ExMailConstant.COLUMN_UID, email.getUid());
		values.put("messageid", email.getMessageid());
		values.put(ExMailConstant.COLUMN_SENDER_NAME, email.getSendername());
		values.put(ExMailConstant.COLUMN_MAIL_FROM, email.getMailfrom());
		values.put(ExMailConstant.COLUMN_MAIL_TO, email.getMailto());
		values.put("cc", email.getCc());
		values.put("bcc", email.getBcc());
		values.put(ExMailConstant.COLUMN_SUBJECT, email.getSubject());
		values.put("sentdate", email.getSentdate());
		values.put("digest", email.getDigest());
		values.put(ExMailConstant.COLUMN_CONTENT, email.getContent());
		values.put("hasread", email.getHasread() ? "1" : "0");
		values.put("replysign", email.getReplysign() ? "1" : "0");
		values.put("ishtml", email.getIshtml() ? "1" : "0");
		values.put("charset", email.getCharset());
		return values;
	}

	/**
	 * attachment operation
	 */
	public boolean insertAttachment(List<Attachment> attachments) {
		boolean insert = false;
		for (Attachment attachment : attachments) {
			ContentValues values = new ContentValues();
			values.put(ExMailConstant.COLUMN_ADDRESS, attachment.getAddress());
			values.put(ExMailConstant.COLUMN_UID, attachment.getUid());
			values.put("draftid", attachment.getDraftid());
			values.put("filename", attachment.getFilename());
			values.put("filesize", attachment.getFilesize());
			values.put("filetype", attachment.getFiletype());
			values.put("filepath", attachment.getFilepath());
			values.put("inputstream", attachment.getInputstream());
			insertRecord(ExMailConstant.TABLE_ATTACH, null, values);
			insert = true;
		}
		return insert;
	}

	public List<Attachment> queryDraftAttachments(int draftid) {
		List<Attachment> attachments = null;
		attachments = queryAttachments(ExMailConstant.TABLE_ATTACH, null,
				ExMailConstant.COLUMN_ADDRESS + "=? and draftid=?",
				new String[] { String.valueOf(draftid) }, null, null, null);
		return attachments;
	}

	public List<Attachment> queryInboxAttachments(int uid) {
		List<Attachment> attachments = null;
		attachments = queryAttachments(
				ExMailConstant.TABLE_ATTACH,
				null,
				ExMailConstant.COLUMN_ADDRESS + "=? and "
						+ ExMailConstant.COLUMN_UID + "=?",
				new String[] { MyApplication.info.getUserName(),
						String.valueOf(uid) }, null, null, null);
		return attachments;
	}

	public List<Attachment> queryAttachments(String table, String[] columns,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy) {
		List<Attachment> attachments = new ArrayList<Attachment>();
		Cursor attachCursor = queryRecord(table, columns, selection,
				selectionArgs, groupBy, having, orderBy);
		if (null != attachCursor) {
			while (attachCursor.moveToNext()) {
				Attachment attachment = new Attachment();
				attachment.setAttachmentid(attachCursor.getInt(0));
				attachment.setUid(attachCursor.getInt(2));
				attachment.setDraftid(attachCursor.getInt(3));
				attachment.setFilename(attachCursor.getString(4));
				attachment.setFilesize(attachCursor.getInt(5));
				attachment.setFiletype(attachCursor.getString(6));
				attachment.setFilepath(attachCursor.getString(7));
				attachment.setInputstream(attachCursor.getBlob(8));
				attachments.add(attachment);
			}
			attachCursor.close();
		}
		return attachments;
	}

	public boolean deleteAttachment(int draftId) {
		return deleteRecord(
				ExMailConstant.TABLE_ATTACH,
				ExMailConstant.COLUMN_ADDRESS + "=? and draftid=?",
				new String[] { MyApplication.info.getUserName(),
						String.valueOf(draftId) });
	}

	/**
	 * draft operation
	 */
	public int insertDraft(Draft draft) {
		ContentValues values = new ContentValues();
		values.put(ExMailConstant.COLUMN_ADDRESS, draft.getAddress());
		values.put(ExMailConstant.COLUMN_MAIL_TO, draft.getMailto());
		values.put(ExMailConstant.COLUMN_SUBJECT, draft.getSubject());
		values.put(ExMailConstant.COLUMN_CONTENT, draft.getContent());
		return insertRecord(ExMailConstant.TABLE_DRAFT, null, values);
	}

	public List<Draft> queryDrafts() {
		List<Draft> result = null;
		Cursor cursor = queryRecord(ExMailConstant.TABLE_DRAFT, null,
				ExMailConstant.COLUMN_ADDRESS + "=?",
				new String[] { MyApplication.info.getUserName() }, null, null,
				null);
		if (null != cursor) {
			result = new ArrayList<Draft>();
			while (cursor.moveToNext()) {
				Draft draft = new Draft();
				draft.setDraftid(cursor.getInt(0));
				draft.setAddress(cursor.getString(1));
				draft.setMailto(cursor.getString(2));
				draft.setSubject(cursor.getString(3));
				draft.setContent(cursor.getString(4));
				draft.setIshtml(cursor.getInt(5) == 0 ? false : true);
				result.add(draft);
			}
			cursor.close();
		}
		return result;
	}

	public Draft querySingDraft(int draftId) {
		Draft draft = new Draft();
		Cursor cursor = queryRecord(
				ExMailConstant.TABLE_DRAFT,
				null,
				ExMailConstant.COLUMN_ADDRESS + "=? and draftid=?",
				new String[] { MyApplication.info.getUserName(),
						String.valueOf(draftId) }, null, null, null);
		if (null != cursor) {
			if (cursor.moveToNext()) {
				draft.setDraftid(draftId);
				draft.setAddress(cursor.getString(1));
				draft.setMailto(cursor.getString(2));
				draft.setSubject(cursor.getString(3));
				draft.setContent(cursor.getString(4));
				draft.setIshtml(cursor.getInt(5) == 0 ? false : true);
			}
			cursor.close();
		}
		return draft;
	}

	public boolean deleteDraft(String address, int draftId) {
		return deleteRecord(ExMailConstant.TABLE_DRAFT,
				ExMailConstant.COLUMN_ADDRESS + "=? and draftid=?",
				new String[] { address, String.valueOf(draftId) });
	}

	/**
	 * contact operation
	 */
	public List<Contact> queryContacts() {
		List<Contact> result = null;
		Cursor cursor = queryRecord(ExMailConstant.TABLE_CONTACT, null,
				ExMailConstant.COLUMN_USERNAME + "=?",
				new String[] { MyApplication.info.getUserName() }, null, null,
				ExMailConstant.COLUMN_PINYIN + " asc");
		if (null != cursor) {
			result = new ArrayList<Contact>();
			while (cursor.moveToNext()) {
				Contact contact = new Contact(cursor.getString(2),
						cursor.getString(3), cursor.getString(4));
				result.add(contact);
			}
			cursor.close();
		}
		return result;
	}

	public boolean updateContact(String name, String address) {
		ContentValues values = new ContentValues();
		values.put(ExMailConstant.COLUMN_ADDRESS, address);
		return updateRecord(ExMailConstant.TABLE_CONTACT, values,
				ExMailConstant.COLUMN_USERNAME + "=? and "
						+ ExMailConstant.COLUMN_CONTACT_NAME + "=?",
				new String[] { MyApplication.info.getUserName(), name });
	}

	public boolean deleteContact(String name) {
		return deleteRecord(ExMailConstant.TABLE_CONTACT,
				ExMailConstant.COLUMN_USERNAME + "=? and "
						+ ExMailConstant.COLUMN_CONTACT_NAME + "=?",
				new String[] { MyApplication.info.getUserName(), name });
	}

	public boolean insertContact(String name, String address) {
		ContentValues values = new ContentValues();
		values.put(ExMailConstant.COLUMN_USERNAME,
				MyApplication.info.getUserName());
		values.put(ExMailConstant.COLUMN_CONTACT_NAME, name);
		values.put(ExMailConstant.COLUMN_ADDRESS, address);
		values.put(ExMailConstant.COLUMN_PINYIN,
				PinyinUtil.getStringPinYin(name));
		return -1 != insertRecord(ExMailConstant.TABLE_CONTACT, null, values);
	}
}
