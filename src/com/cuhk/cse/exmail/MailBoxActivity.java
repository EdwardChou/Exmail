package com.cuhk.cse.exmail;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cuhk.cse.exmail.app.MyApplication;
import com.cuhk.cse.exmail.bean.Email;
import com.cuhk.cse.exmail.db.DBProvider;
import com.cuhk.cse.exmail.service.MailHelperService;
import com.cuhk.cse.exmail.utils.ExMailConstant;

/**
 * Mailbox, includes all/read/unread email
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2014-10-12 上午10:19:06
 * @version V1.0
 * 
 */
public class MailBoxActivity extends Activity {

	private ArrayList<Email> mailslist = new ArrayList<Email>();
	private int folderType;
	private MyAdapter myAdapter;
	private ListView lv_box;
	private ProgressDialog dialog;

	private MailHelperService service;
	private ServiceConnection conn;
	private MyBroadcastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		// type = getIntent().getStringExtra("TYPE");
		folderType = getIntent().getIntExtra("status", -1);

		setContentView(R.layout.email_mailbox);
		initView();

		boolean hasEmail = getLocalEmail(folderType);

		conn = new MyServiceConn();
		bindService(new Intent(MailBoxActivity.this, MailHelperService.class),
				conn, BIND_AUTO_CREATE);
		mReceiver = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter(
				"com.cuhk.cse.exmail.email.RECEIVER");
		registerReceiver(mReceiver, filter);
		// service.getMailByIMAP();
		if (service == null) {
			Log.e("MailBoxActivity", "service is null");
		}

		if (!MailHelperService.download) {
			if (MyApplication.info.getMailServerHost().contains("gmail")) {
				// err_google_auth
				handler.sendEmptyMessage(2);
			} else if (!hasEmail) {
				// err_network_timeout
				handler.sendEmptyMessage(4);
			} else {
				// err_imap_unavailable
				handler.sendEmptyMessage(3);
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initView() {
		lv_box = (ListView) findViewById(R.id.lv_box);
		myAdapter = new MyAdapter();
		lv_box.setAdapter(myAdapter);

		dialog = new ProgressDialog(this);
		dialog.setMessage("Loading...");
		dialog.show();
	}

	/**
	 * query email from local database
	 * 
	 * @return whether email box is empty
	 */
	private boolean getLocalEmail(int mode) {
		boolean hasEmail = false;
		mailslist.clear();
		DBProvider.init(this);
		mailslist = (ArrayList<Email>) DBProvider.getInstance().queryEmailInfo(
				MyApplication.info.getUserName(), mode);
		int size = mailslist.size();
		if (size > 0) {
			hasEmail = true;
			handler.sendEmptyMessage(0);
			if (size > 7) {
				handler.sendEmptyMessage(1);
			}
		}
		return hasEmail;
	}

	/**
	 * listview adapter
	 * 
	 * @author EdwardChou edwardchou_gmail_com
	 * @date 2015-3-12 下午1:56:56
	 * @version V1.0
	 * 
	 */
	private class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return mailslist.size();
		}

		@Override
		public Object getItem(int position) {
			return mailslist.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint({ "InflateParams", "ViewHolder" })
		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			convertView = LayoutInflater.from(MailBoxActivity.this).inflate(
					R.layout.email_mailbox_item, null);
			TextView tv_new = (TextView) convertView.findViewById(R.id.tv_new);
			if (!mailslist.get(position).getHasread()) {
				tv_new.setVisibility(View.VISIBLE);
			} else {
				tv_new.setVisibility(View.GONE);
			}
			TextView tv_from = (TextView) convertView
					.findViewById(R.id.tv_from);
			if (mailslist.get(position).getSendername().equals("Unknown")) {
				tv_from.setText(mailslist.get(position).getMailfrom());
			} else {
				tv_from.setText(mailslist.get(position).getSendername());
			}
			TextView tv_sentdate = (TextView) convertView
					.findViewById(R.id.tv_sentdate);
			tv_sentdate.setText(mailslist.get(position).getSentdate());
			TextView tv_subject = (TextView) convertView
					.findViewById(R.id.tv_subject);
			tv_subject.setText(mailslist.get(position).getSubject());
			TextView tv_digest = (TextView) convertView
					.findViewById(R.id.tv_digest);
			tv_digest.setText(mailslist.get(position).getDigest());
			ImageView iv_attach = (ImageView) convertView
					.findViewById(R.id.iv_attach);
			if (mailslist.get(position).getHasattach()) {
				iv_attach.setVisibility(View.VISIBLE);
			} else
				iv_attach.setVisibility(View.GONE);

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// update hasread status
					Email email = mailslist.get(position);
					int uid = email.getUid();
					email.setHasread(true);
					ContentValues values = new ContentValues();
					values.put("hasread", "1");
					DBProvider.getInstance().updateRecord(
							ExMailConstant.TABLE_EMAIL,
							values,
							ExMailConstant.COLUMN_ADDRESS + "=? and uid=?",
							new String[] { MyApplication.info.getUserName(),
									String.valueOf(uid) });

					final Intent intent = new Intent(MailBoxActivity.this,
							MailContentActivity.class).putExtra("uid", uid);
					Log.i("MailBoxActivity", "uid= " + uid);
					startActivity(intent);
				}
			});
			return convertView;
		}

	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				myAdapter.notifyDataSetChanged();
				break;
			case 1:
				dialog.dismiss();
				myAdapter.notifyDataSetChanged();
				break;
			case 2:
				dialog.dismiss();
				Toast.makeText(MailBoxActivity.this,
						getResources().getString(R.string.err_google_auth),
						Toast.LENGTH_LONG).show();
				break;
			case 3:
				dialog.dismiss();
				Toast.makeText(
						MailBoxActivity.this,
						getResources().getString(R.string.err_imap_unavailable),
						Toast.LENGTH_LONG).show();
				break;
			case 4:
				dialog.dismiss();
				Toast.makeText(MailBoxActivity.this,
						getResources().getString(R.string.err_network_timeout),
						Toast.LENGTH_SHORT).show();
				break;
			case 5:
				finish();
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(conn);
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
	}

	/**
	 * finish activity
	 * 
	 * @param v
	 */
	public void back(View v) {
		finish();
	}

	public final class MyServiceConn implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			service = ((MailHelperService.LocalBinder) binder).getService();
			if (service == null) {
				Log.e("MailBoxActivity", "service is null");
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			service = null;
		}
	}

	public class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e("MailBoxActivity", "receive broadcast");
			getLocalEmail(folderType);
		}
	}
}
