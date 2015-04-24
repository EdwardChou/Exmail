package com.cuhk.cse.exmail.utils;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pinyin4j.PinyinHelper;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.cuhk.cse.exmail.bean.Contact;

/**
 * Add, update, delete, query of contacts emails
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date Dec 12, 2014 4:37:57 PM
 * @version V1.0
 * 
 */
public class ContactUtil {

	private static final String[] PROJECTION = new String[] {
			ContactsContract.CommonDataKinds.Email.CONTACT_ID,
			ContactsContract.Contacts.DISPLAY_NAME,
			ContactsContract.CommonDataKinds.Email.DATA };

	/**
	 * get Email contact from system contacts
	 * 
	 * @param @param context
	 * @param @return
	 * @return List<EmailContact>
	 * @throws
	 */
	public static List<Contact> getEmailContact(Context context) {
		List<Contact> res = new ArrayList<Contact>();

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(
				ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION,
				null, null, null);
		if (cursor != null) {
			try {
				final int contactIdIndex = cursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID);
				final int displayNameIndex = cursor
						.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
				final int emailIndex = cursor
						.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
				long contactId;
				String displayName, address;
				while (cursor.moveToNext()) {
					contactId = cursor.getLong(contactIdIndex);
					displayName = cursor.getString(displayNameIndex);
					address = cursor.getString(emailIndex);

					Contact ec = new Contact((int) contactId, displayName, address);
					ec.setInitial(ContactUtil.getFirstChar(displayName));

					res.add(ec);
				}
			} finally {
				cursor.close();
			}
		}
		return res;
	}

	/**
	 * Update contact email
	 * 
	 * @param @param context
	 * @param @param name
	 * @param @param email
	 * @param @return
	 * @return int the number of rows updated
	 * @throws
	 */
	public int updateEmail(Context context, final String name, String email) {
		int result = 0;
		String where = ContactsContract.Data.DISPLAY_NAME + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ? AND "
				+ String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE)
				+ " = ? ";
		String[] params = new String[] {
				name,
				ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
				String.valueOf(ContactsContract.CommonDataKinds.Email.TYPE_HOME) };

		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.Email.DATA, email);

		ContentResolver cr = context.getContentResolver();
		result = cr.update(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
				values, where, params);
		return result;
	}

	/**
	 * Delete Contact email
	 * 
	 * @param @param context
	 * @param @param name
	 * @param @param email
	 * @param @return
	 * @return int the number of rows updated
	 * @throws
	 */
	public int deleteEmail(Context context, final String name, String email) {
		int result = 0;
		String where = ContactsContract.Data.DISPLAY_NAME + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ? AND "
				+ String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE)
				+ " = ? ";
		String[] params = new String[] {
				name,
				ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
				String.valueOf(ContactsContract.CommonDataKinds.Email.TYPE_HOME) };

		ContentValues values = new ContentValues();
		values.put(ContactsContract.CommonDataKinds.Email.DATA, email);

		ContentResolver cr = context.getContentResolver();
		result = cr.delete(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
				where, params);
		return result;
	}

	/**
	 * get initial of english or chinese string
	 * 
	 * @param @param value input string
	 * @param @return
	 * @return String initial
	 * @throws
	 */
	public static String getFirstChar(String value) {
		// initial
		char firstChar = value.charAt(0);
		// initial type
		String first = null;
		// is Chinese Character
		String[] print = PinyinHelper.toHanyuPinyinStringArray(firstChar);

		if (print == null) {
			// Uppercase the character
			if ((firstChar >= 97 && firstChar <= 122)) {
				firstChar -= 32;
			}
			if (firstChar >= 65 && firstChar <= 90) {
				first = String.valueOf((char) firstChar);
			} else {
				// number or special characters
				first = "#";
			}
		} else {
			// Chinese Character
			first = String.valueOf((char) (print[0].charAt(0) - 32));
		}
		if (first == null) {
			first = "?";
		}
		return first;
	}

}
