package com.cuhk.cse.exmail.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.cuhk.cse.exmail.app.MyApplication;
import com.cuhk.cse.exmail.db.DBProvider;
import com.cuhk.cse.exmail.utils.ExMailConstant;
import com.sun.mail.imap.IMAPFolder;

/**
 * Email receive via POP3 or IMAP
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2015-3-12 上午11:49:14
 * @version V1.0
 * 
 */
public class MailHelperService extends Service {

	private static final String TAG = "MailHelperService";

	public static boolean download = false;
	public static int downloadMailCount = 0;
	private Intent intent = new Intent("com.cuhk.cse.exmail.email.RECEIVER");
	// private MailHelperService instance = MailHelperService.this;

	private List<MailReceiver> mailList;
	private HashMap<String, Integer> serviceHashMap;

	public class LocalBinder extends Binder {
		public MailHelperService getService() {
			return MailHelperService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.e(TAG, "============> " + TAG + ".onBind");
		return new LocalBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e(TAG, "============> " + TAG + ".onCreate");
		new Thread(new Runnable() {
			@Override
			public void run() {
				getMailByIMAP();
			}
		}).start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "============> " + TAG + ".onDestroy");

	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		Log.e(TAG, "============> " + TAG + ".onRebind");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.e(TAG, "============> " + TAG + ".onStart");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.e(TAG, "============> " + TAG + ".onUnbind");
		return super.onUnbind(intent);
	}

	public String getUpdateUrlStr() throws Exception {
		String urlStr = null;
		if (serviceHashMap == null) {
			serviceHashMap = this.getServeHashMap();
		}
		if (serviceHashMap.get("update") == 1) {
			urlStr = mailList.get(1).getSubject();
		}
		return urlStr;
	}

	public String getUserHelp() throws Exception {
		String userandmoney = null;
		if (serviceHashMap == null) {
			serviceHashMap = this.getServeHashMap();
		}
		if (serviceHashMap.get("userhelp") == 1) {
			userandmoney = mailList.get(3).getSubject();
		}
		return userandmoney;
	}

	public int getAllUserHelp() throws Exception {
		String userandmoney = null;
		int money = 0;
		if (serviceHashMap == null) {
			serviceHashMap = this.getServeHashMap();
		}
		if (serviceHashMap.get("userhelp") == 1) {
			userandmoney = mailList.get(3).getSubject();
		}
		if (userandmoney != null && userandmoney.contains("all-user-100")) {
			money = Integer.parseInt(userandmoney.substring(
					userandmoney.lastIndexOf("-" + 1), userandmoney.length()));
		}
		return money;
	}

	public boolean getAdControl() throws Exception {
		String ad = null;
		if (serviceHashMap == null) {
			serviceHashMap = this.getServeHashMap();
		}
		if (serviceHashMap.get("adcontrol") == 1) {
			ad = mailList.get(2).getSubject();
		}
		if (ad.equals("ad=close")) {
			return false;
		}
		return true;
	}

	public HashMap<String, Integer> getServeHashMap() throws Exception {
		serviceHashMap = new HashMap<String, Integer>();
		if (mailList == null) {
			mailList = getAllMailByPOP("INBOX");
		}
		String serviceStr = mailList.get(0).getSubject();
		if (serviceStr.contains("update 1.0=true")) {
			serviceHashMap.put("update", 1);
		} else if (serviceStr.contains("update 1.0=false")) {
			serviceHashMap.put("update", 0);
		}
		if (serviceStr.contains("adcontrol 1.0=true")) {
			serviceHashMap.put("adcontrol", 1);
		} else if (serviceStr.contains("adcontrol 1.0=false")) {
			serviceHashMap.put("adcontrol", 0);
		}
		if (serviceStr.contains("userhelp 1.0=true")) {
			serviceHashMap.put("userhelp", 1);
		} else if (serviceStr.contains("userhelp 1.0=false")) {
			serviceHashMap.put("userhelp", 0);
		}
		return serviceHashMap;
	}

	/**
	 * query email via POP3 protocol
	 * 
	 * @param folderName
	 *            E.g. "INBOX"
	 * @throws MessagingException
	 * @return List<MailReceiver>
	 * @throws
	 */
	public List<MailReceiver> getAllMailByPOP(String folderName)
			throws MessagingException {
		List<MailReceiver> mailList = new ArrayList<MailReceiver>();

		Store store = connectServer(ExMailConstant.STORE_POP3S);

		// open folder
		Folder folder = store.getFolder(folderName);
		folder.open(Folder.READ_ONLY);
		// email count
		int mailCount = folder.getMessageCount();
		if (mailCount == 0) {
			folder.close(true);
			store.close();
			return null;
		} else {
			// query all email
			Message[] messages = folder.getMessages();
			for (int i = 0; i < messages.length; i++) {
				// email object
				MailReceiver receiveMail = new MailReceiver(
						(MimeMessage) messages[i]);
				mailList.add(receiveMail);// 添加到邮件列表中
			}
			return mailList;
		}
	}

	/**
	 * query email via POP3 protocol
	 * 
	 * @param context
	 * @return boolean whether successfully query new email
	 * @throws MessagingException
	 */
	public boolean getMailByPOP(String folderName) throws MessagingException {
		boolean hasEmail = false;
		List<String> messageids = DBProvider.getInstance().queryLocalUid();

		// connect server
		Store store = connectServer(ExMailConstant.STORE_POP3S);

		// open folder
		Folder folder = store.getFolder(folderName);
		folder.open(Folder.READ_ONLY);
		// email count
		int mailCount = folder.getMessageCount();
		if (mailCount == 0) {
			folder.close(true);
			store.close();
			return hasEmail;
		}
		// successfully query email
		hasEmail = true;
		// query all email
		Message[] messages = folder.getMessages();
		for (int i = 0; i < messages.length; i++) {
			download = true;
			String messageID = String.valueOf(messages[i].getMessageNumber());
			// email that hadn't stored
			if (!messageids.contains(messageID)) {// email object
				MailReceiver receiveMail = new MailReceiver(
						(MimeMessage) messages[i]);
				insertEmail(receiveMail, 0);
				// send broadcast Action: com.cuhk.cse.exmail.email.RECEIVER
				intent.putExtra("emailcount", downloadMailCount);
				sendBroadcast(intent);
			}
		}
		return hasEmail;
	}

	/**
	 * query email from server via IMAP
	 * 
	 * @return boolean whether successfully query new email
	 * @throws MessagingException
	 */
	public boolean getMailByIMAP() {
		boolean hasEmail = false;
		try {
			int emailCount = 30;
			DBProvider.init(getApplicationContext());
			List<String> uidsList = DBProvider.getInstance().queryLocalUid();

			// Store store = connectServer(ExMailConstant.STORE_IMAPS);
			Store store = null;
			store = MyApplication.session.getStore(ExMailConstant.STORE_IMAPS);
			// pop3s
			String temp = MyApplication.info.getMailServerHost();
			String host = temp.replace("smtp", ExMailConstant.STORE_IMAPS);
			MyApplication.info.setValidate(true);
			store.connect(host, MyApplication.info.getUserName(),
					MyApplication.info.getPassword());

			// open folder
			IMAPFolder folder = (IMAPFolder) connectServer(
					ExMailConstant.STORE_IMAPS).getFolder("INBOX");
			folder.open(Folder.READ_WRITE);
			// email count
			int mailCount = folder.getMessageCount();

			// receive no email, error or empty email box
			if (mailCount == 0) {
				folder.close(true);
				store.close();
				return hasEmail;
			}

			// if no error
			hasEmail = true;
			Message[] messages;
			messages = folder.getMessages();
			for (int i = messages.length - 1; i >= 0 && emailCount > 0; i--) {
				// String messageID = String.valueOf(((MimeMessage)
				// messages[i]).getMessageID());
				int uid = (int) folder.getUID(messages[i]);
				String addressAndUid = MyApplication.info.getUserName() + uid;
				Log.i("MailHelperService", "check uid=" + uid);
				download = true;
				// email that hadn't stored
				if (!uidsList.contains(addressAndUid)) {
					downloadMailCount++;
					emailCount--;
					Log.i("MailHelperService", "will retrieve " + emailCount
							+ " more emails.");
					// email object
					MailReceiver receiveMail = new MailReceiver(
							(MimeMessage) messages[i], uid);
					insertEmail(receiveMail, uid);
					Log.e("MailHelperService", "download "
							+ receiveMail.getAttachments().size() + " attachs");
					DBProvider.getInstance().insertAttachment(
							receiveMail.getAttachments());
					// send broadcast Action: com.cuhk.cse.exmail.email.RECEIVER
					intent.putExtra(ExMailConstant.EMAIL_COUNT,
							downloadMailCount);
					sendBroadcast(intent);
				}
			}
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return hasEmail;
	}

	/**
	 * insert email received from server to local database
	 * 
	 * @param receiveMail
	 * @param uid
	 * @param hasRead
	 */
	private void insertEmail(MailReceiver receiveMail, int uid) {
		ContentValues values = new ContentValues();
		values.put(ExMailConstant.COLUMN_ADDRESS,
				MyApplication.info.getUserName());
		values.put("uid", uid);
		try {
			values.put("messageid", receiveMail.getMessageID());
			values.put(ExMailConstant.COLUMN_SENDER_NAME,
					receiveMail.getSenderName());
			values.put(ExMailConstant.COLUMN_MAIL_FROM, receiveMail.getFrom());
			values.put(ExMailConstant.COLUMN_MAIL_TO,
					receiveMail.getMailAddress("TO"));
			values.put("cc", receiveMail.getMailAddress("CC"));
			values.put("bcc", receiveMail.getMailAddress("BCC"));
			values.put(ExMailConstant.COLUMN_SUBJECT, receiveMail.getSubject());
			Log.i("MailHelperService",
					"insert db, sbj= " + receiveMail.getSubject());
			values.put("sentdate", receiveMail.getSentDate());
			values.put("digest", receiveMail.getMailContent()[0]);
			values.put(ExMailConstant.COLUMN_CONTENT,
					receiveMail.getMailContent()[1]);
			values.put("hasread", receiveMail.hasRead() ? "1" : "0");
			values.put("replysign", receiveMail.getReplySign() ? "1" : "0");
			values.put("ishtml", receiveMail.isHtml() ? "1" : "0");
			values.put("charset", receiveMail.getCharset());
			values.put("hasattach",
					receiveMail.getAttachments().size() > 0 ? "1" : "0");
			DBProvider.getInstance().insertRecord(ExMailConstant.TABLE_EMAIL,
					null, values);
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Store connectServer(String protocol) {
		// connect server
		Store store = null;
		try {
			store = MyApplication.session.getStore(protocol);
			// pop3s
			String temp = MyApplication.info.getMailServerHost();
			String host = temp.replace("smtp", protocol);
			MyApplication.info.setValidate(true);
			store.connect(host, MyApplication.info.getUserName(),
					MyApplication.info.getPassword());
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return store;
	}

}
