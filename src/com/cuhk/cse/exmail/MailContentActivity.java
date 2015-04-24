package com.cuhk.cse.exmail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cuhk.cse.exmail.app.MyApplication;
import com.cuhk.cse.exmail.bean.Attachment;
import com.cuhk.cse.exmail.bean.Email;
import com.cuhk.cse.exmail.db.DBProvider;
import com.cuhk.cse.exmail.utils.IOUtil;

/**
 * email content view
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2015-3-12 下午2:24:58
 * @version V1.0
 * 
 */
public class MailContentActivity extends Activity {

	private TextView tv_addr, tv_mailsubject, tv_mailcontent;
	private ListView lv_mailattachment;
	private WebView wv_mailcontent;
	private Button btn_cancel, btn_relay;
	private ArrayList<Attachment> attachments;
	private ArrayList<String> attachmentNames;
	// private ArrayList<InputStream> attachmentsInputStreams;
	private Email email;
	private Handler handler;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.email_mailcontent);

		int uid = 0;
		uid = getIntent().getIntExtra("uid", 0);
		DBProvider.init(this);
		Object[] result = DBProvider.getInstance().queryEmailDetail(
				MyApplication.info.getUserName(), uid);
		email = (Email) result[0];
		attachments = (ArrayList<Attachment>) result[1];
		attachmentNames = new ArrayList<String>();
		Log.i("MailContentActivity", "attchList size=" + attachments.size());
		for (Attachment attachment : attachments) {
			attachmentNames.add(attachment.getFilename());
		}
		init();
	}

	private void init() {
		handler = new MyHandler(this);
		tv_addr = (TextView) findViewById(R.id.tv_addr);
		tv_mailsubject = (TextView) findViewById(R.id.tv_mailsubject);
		tv_mailcontent = (TextView) findViewById(R.id.tv_mailcontent);
		if (attachments.size() > 0) {
			lv_mailattachment = (ListView) findViewById(R.id.lv_mailattachment);
			lv_mailattachment.setVisibility(View.VISIBLE);
			lv_mailattachment.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, attachmentNames));
			lv_mailattachment
					.setOnItemClickListener(new attchListItemClickListener());
		}

		btn_cancel = (Button) findViewById(R.id.btn_cancel);
		btn_relay = (Button) findViewById(R.id.btn_relay);

		// Log.i("MailContentActivity", "add=" + email.getAddress());
		// Log.i("MailContentActivity", "content=" + email.getContent());
		// Log.i("MailContentActivity", "html=" + email.getIshtml());
		// Log.i("MailContentActivity", "sbj=" + email.getSubject());
		tv_addr.setText(email.getMailfrom());
		tv_mailsubject.setText(email.getSubject());
		if (email.getIshtml()) {
			wv_mailcontent = (WebView) findViewById(R.id.wv_mailcontent);
			wv_mailcontent.setVisibility(View.VISIBLE);
			wv_mailcontent.loadDataWithBaseURL(null, email.getContent(),
					"text/html", "utf-8", null);// utf-8
			// wv_mailcontent.getSettings().setLoadWithOverviewMode(true);
			// wv_mailcontent.getSettings().setUseWideViewPort(true);
			// zoom in/out
			wv_mailcontent.getSettings().setBuiltInZoomControls(true);

			// web page display fitting
			DisplayMetrics dm = getResources().getDisplayMetrics();
			int scale = dm.densityDpi;
			if (scale == 240) {
				wv_mailcontent.getSettings().setDefaultZoom(ZoomDensity.FAR);
			} else if (scale == 160) {
				wv_mailcontent.getSettings().setDefaultZoom(ZoomDensity.MEDIUM);
			} else {
				wv_mailcontent.getSettings().setDefaultZoom(ZoomDensity.CLOSE);
			}
			wv_mailcontent.setWebChromeClient(new WebChromeClient());
			tv_mailcontent.setVisibility(View.GONE);
		} else {
			tv_mailcontent.setText(email.getContent());
			//
			// // link of phone number
			// Pattern phone = Pattern
			// .compile("1([\\d]{10})|((\\+[0-9]{2,4})?\\(?[0-9]+\\)?-?)?[0-9]{7,8}");
			// Linkify.addLinks(tv_mailcontent, phone, email.getContent());
			//
			// // link of email
			// Pattern em = Pattern
			// .compile("^([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$");
			// Linkify.addLinks(tv_mailcontent, em, email.getContent());
			//
			// // link of time
			// Pattern time = Pattern
			// .compile(
			// "(\\d{1,4}[-|\\/|年|\\.]\\d{1,2}[-|\\/|月|\\.]\\d{1,2}([日|号])?(\\s)*(\\d{1,2}([点|时])?((:)?\\d{1,2}(分)?((:)?\\d{1,2}(秒)?)?)?)?(\\s)*(PM|AM)?)",
			// Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
			// Linkify.addLinks(tv_mailcontent, time, email.getContent());
		}

		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MailContentActivity.this.finish();
			}
		});

		btn_relay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(MailContentActivity.this,
						MailEditActivity.class).putExtra("EMAIL", email)
						.putExtra("TYPE", 1));
			}
		});
		/*
		 * btn_relay.setOnLongClickListener(new OnLongClickListener() {
		 * 
		 * @Override public boolean onLongClick(View v) { startActivity(new
		 * Intent(MailContentActivity.this,
		 * MailEditActivity.class).putExtra("EMAIL", email).putExtra("type",
		 * 2)); return true; } });
		 */
	}

	private class attchListItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				final int position, long id) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					handler.obtainMessage(
							0,
							"Start download\"" + attachmentNames.get(position)
									+ "\"").sendToTarget();
					byte[] buf = attachments.get(position).getInputstream();
					InputStream is = new ByteArrayInputStream(buf);
					String path = new IOUtil().stream2file(is, Environment
							.getExternalStorageDirectory().toString()
							+ "/temp/" + attachmentNames.get(position));
					if (path == null) {
						handler.obtainMessage(0, "Download fail!")
								.sendToTarget();
					} else {
						handler.obtainMessage(0, "Save file at:" + path)
								.sendToTarget();
					}
				}
			}).start();
		}
	}

	private static class MyHandler extends Handler {

		private WeakReference<MailContentActivity> wrActivity;

		public MyHandler(MailContentActivity activity) {
			this.wrActivity = new WeakReference<MailContentActivity>(activity);
		}

		@Override
		public void handleMessage(android.os.Message msg) {
			final MailContentActivity activity = wrActivity.get();
			switch (msg.what) {
			case 0:
				Toast.makeText(activity.getApplicationContext(),
						msg.obj.toString(), Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};

}
