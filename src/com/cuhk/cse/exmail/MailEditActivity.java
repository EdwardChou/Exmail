package com.cuhk.cse.exmail;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.cuhk.cse.exmail.adapter.GridViewAdapter;
import com.cuhk.cse.exmail.app.MyApplication;
import com.cuhk.cse.exmail.bean.Attachment;
import com.cuhk.cse.exmail.bean.Draft;
import com.cuhk.cse.exmail.db.DBProvider;
import com.cuhk.cse.exmail.utils.AttachmentUtil;
import com.cuhk.cse.exmail.utils.HttpUtil;

public class MailEditActivity extends Activity implements OnClickListener {
	private EditText mail_to;
	private EditText mail_from;
	private EditText mail_topic;
	private EditText mail_content;

	private Button send;
	private ImageButton add_lianxiren;
	private ImageButton attachment;
	private GridView gridView;
	private GridViewAdapter<Attachment> adapter = null;
	private int draftid = -1;

	private static final int SUCCESS = 1;
	private static final int FAILED = -1;
	private boolean isFromDraftBox = true;
	private ProgressDialog dialog;
	HttpUtil util = new HttpUtil();
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SUCCESS:
				dialog.cancel();
				// if successfully sent, don't save to draft box
				isFromDraftBox = false;
				// delete draft if sent successfully
				if (draftid > 0) {
					DBProvider.getInstance().deleteDraft(
							MyApplication.info.getUserName(), draftid);
					DBProvider.getInstance().deleteAttachment(draftid);
					// return to draft box
					Toast.makeText(getApplicationContext(), "Successful sent",
							Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(MailEditActivity.this,
							MailDraftsActivity.class);
					startActivity(intent);
					finish();
				} else {
					Toast.makeText(getApplicationContext(), "Successful sent",
							Toast.LENGTH_SHORT).show();
					// clear data if sent successfully
					mail_from.getText().clear();
					mail_to.getText().clear();
					mail_topic.getText().clear();
					mail_content.getText().clear();
					adapter = new GridViewAdapter<Attachment>(
							MailEditActivity.this);
					adapter.clear();
				}

				break;
			case FAILED:
				dialog.cancel();
				// if fail to send, save to draft box
				isFromDraftBox = true;
				Toast.makeText(getApplicationContext(), "Fail to send mail",
						Toast.LENGTH_SHORT).show();
				break;
			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.email_writer);
		DBProvider.init(getApplicationContext());
		init();
	}

	/**
	 * initialize view
	 */
	private void init() {
		mail_to = (EditText) findViewById(R.id.mail_to);
		mail_from = (EditText) findViewById(R.id.mail_from);
		mail_topic = (EditText) findViewById(R.id.mail_topic);
		mail_content = (EditText) findViewById(R.id.content);
		send = (Button) findViewById(R.id.send);
		attachment = (ImageButton) findViewById(R.id.add_att);
		add_lianxiren = (ImageButton) findViewById(R.id.add_lianxiren);
		gridView = (GridView) findViewById(R.id.pre_view);

		mail_from.setText(MyApplication.info.getUserName());
		send.setOnClickListener(this);
		attachment.setOnClickListener(this);
		add_lianxiren.setOnClickListener(this);

		adapter = new GridViewAdapter<Attachment>(this);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new MyOnItemClickListener());

		// whether enter as drafts box
		draftid = getIntent().getIntExtra("draftid", -1);
		if (draftid > -1) {
			Draft draft = DBProvider.getInstance().querySingDraft(draftid);
			if (null != draft) {
				mail_to.setText(draft.getMailto());
				mail_topic.setText(draft.getSubject());
				mail_content.setText(draft.getContent());
			}

			List<Attachment> attachments = new ArrayList<Attachment>();
			attachments = DBProvider.getInstance().queryDraftAttachments(
					draftid);

			// display attachments
			if (attachments.size() > 0) {
				for (Attachment affInfos : attachments) {
					adapter.appendToList(affInfos);
					int a = adapter.getList().size();
					int count = (int) Math.ceil(a / 4.0);
					gridView.setLayoutParams(new LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							(int) (94 * 1.5 * count)));
				}
			}

		}

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.send) {
			sendMail();
		} else if (id == R.id.add_att) {
			addAttachment();
		} else if (id == R.id.add_lianxiren) {
			Intent intent = new Intent(MailEditActivity.this,
					MailAddContacts.class);
			startActivityForResult(intent, 2);
		}

	};

	/**
	 * add Attachment
	 */
	private void addAttachment() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/");
		startActivityForResult(intent, 1);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 1:
				Uri uri = null;
				if (data != null) {
					uri = data.getData();
				}

				String path = uri.getPath();
				Attachment affInfos = AttachmentUtil.acquireFileInfo(path);
				if (null != affInfos) {
					affInfos.setAddress(MyApplication.info.getUserName());
					affInfos.setDraftid(draftid);
				}
				adapter.appendToList(affInfos);
				int a = adapter.getList().size();
				int count = (int) Math.ceil(a / 4.0);
				gridView.setLayoutParams(new LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						(int) (94 * 1.5 * count)));
				break;
			}
		}

		/**
		 * multi-contacts
		 */
		if (requestCode == 2) {
			List<String> chooseUsers = data
					.getStringArrayListExtra("chooseUsers");
			StringBuilder str = new StringBuilder();
			for (int i = 0; i < chooseUsers.size(); i++) {
				if (i == chooseUsers.size() - 1) {
					str.append("<" + chooseUsers.get(i) + ">");
				} else {
					str.append("<" + chooseUsers.get(i) + ">,");
				}
			}
			mail_to.setText(str.toString());

		}
	}

	/**
	 * configure email info
	 */
	private void sendMail() {
		MyApplication.info.setAttachmentInfos(adapter.getList());
		MyApplication.info
				.setFromAddress(mail_from.getText().toString().trim());
		MyApplication.info.setSubject(mail_topic.getText().toString().trim());
		MyApplication.info.setContent(mail_content.getText().toString().trim());
		// receivers
		String str = mail_to.getText().toString().trim();
		String[] receivers = str.split(",");
		for (int i = 0; i < receivers.length; i++) {
			if (receivers[i].startsWith("<") && receivers[i].endsWith(">")) {
				receivers[i] = receivers[i].substring(
						receivers[i].lastIndexOf("<") + 1,
						receivers[i].lastIndexOf(">"));
			}
		}
		MyApplication.info.setReceivers(receivers);

		// send email
		dialog = new ProgressDialog(this);
		dialog.setMessage("Sending...");
		dialog.show();

		/**
		 * sending thread
		 */
		new Thread() {
			@Override
			public void run() {
				boolean flag = util.sendTextMail(MyApplication.info,
						MyApplication.session);
				Message msg = new Message();
				if (flag) {
					msg.what = SUCCESS;
					handler.sendMessage(msg);
				} else {
					msg.what = FAILED;
					handler.sendMessage(msg);
				}
			}

		}.start();

	}

	/**
	 * click listener
	 * 
	 * @author EdwardChou edwardchou_gmail_com
	 * @date 2015-4-10 下午4:57:30
	 * @version V1.0
	 * 
	 */
	private class MyOnItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,
				long arg3) {
			Attachment infos = (Attachment) adapter.getItem(arg2);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					MailEditActivity.this);
			builder.setTitle(infos.getFilename());
			builder.setIcon(getResources()
					.getColor(android.R.color.transparent));
			builder.setMessage("Delete current attachment?");
			builder.setNegativeButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							adapter.clearPositionList(arg2);
							int a = adapter.getList().size();
							int count = (int) Math.ceil(a / 4.0);
							gridView.setLayoutParams(new LayoutParams(
									LinearLayout.LayoutParams.MATCH_PARENT,
									(int) (94 * 1.5 * count)));
						}
					});
			builder.setPositiveButton("Cancel", null);
			builder.create().show();
		}
	}

	/**
	 * return
	 * 
	 * @param v
	 */
	public void back(View v) {
		saveDraftDialog();
	}

	/**
	 * back button
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			saveDraftDialog();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void saveDraftDialog() {
		if (isFromDraftBox && mail_to.getText().toString().trim() != null) {
			AlertDialog.Builder builder = new Builder(MailEditActivity.this);
			builder.setMessage("Save to draft box?");
			builder.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// save to database
							save2DraftBox();
						}

					});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}

					});
			builder.show();
		} else {
			finish();
		}
	}

	/**
	 * save to draft box
	 */
	private void save2DraftBox() {
		Draft draft = new Draft();
		draft.setAddress(MyApplication.info.getUserName());
		draft.setMailto(mail_to.getText().toString().trim());
		draft.setSubject(mail_topic.getText().toString().trim());
		draft.setContent(mail_content.getText().toString().trim());
		DBProvider.getInstance().insertDraft(draft);
		// save attachments
		if (adapter.getList().size() > 0) {
			List<Attachment> attachments = adapter.getmList();
			DBProvider.getInstance().insertAttachment(attachments);
		}
		Toast.makeText(MailEditActivity.this, "Successful save.",
				Toast.LENGTH_SHORT).show();
		finish();
	};

}
