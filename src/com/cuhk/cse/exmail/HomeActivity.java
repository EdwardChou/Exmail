package com.cuhk.cse.exmail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cuhk.cse.exmail.app.MyApplication;
import com.cuhk.cse.exmail.calendar.BaseCalendar;
import com.cuhk.cse.exmail.utils.EmailFormatUtil;
import com.cuhk.cse.exmail.utils.ExMailConstant;
import com.cuhk.cse.exmail.view.OnListItemExpandListener;

/**
 * Main activity
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2014-11-11 下午10:12:37
 * @version V1.0
 * 
 */
public class HomeActivity extends Activity {

	private Button manageAccountButton;
	private ExpandableListView expendView;
	/**
	 * record time for exit decision
	 */
	private long mExitTime = 0;

	/**
	 * record the current selected groupId
	 */
	private int selectedGroupId = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		// 绑定Service
		Intent service = new Intent("com.cuhk.cse.exmail.email.RECEIVER");
		startService(service);

		final MyExpendAdapter adapter = new MyExpendAdapter();

		manageAccountButton = (Button) findViewById(R.id.account_manage_btn);
		manageAccountButton.setOnClickListener(new onClickListen());

		expendView = (ExpandableListView) findViewById(R.id.list);
		// indicator beside each item to display the item's current state,
		// not display by default
		expendView.setGroupIndicator(null);
		expendView.setAdapter(adapter);

		// click on groups
		expendView.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {

				if (selectedGroupId == groupPosition) {
					selectedGroupId = -1;
				} else {
					selectedGroupId = groupPosition;
				}
				adapter.notifyDataSetChanged();
				return false;
			}
		});
		expendView.setOnGroupExpandListener(new OnListItemExpandListener(
				expendView));

		// click on children
		expendView.setOnChildClickListener(new OnChildClickListener() {
			@SuppressLint("InflateParams")
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				if (groupPosition == 0 && childPosition == 0) {
					// view contacts
					Intent intent = new Intent(HomeActivity.this,
							MailContactActivity.class);
					startActivity(intent);
				} else if (groupPosition == 0 && childPosition == 1) {
					// add contacts
					addContactDialog();
				} else if (groupPosition == 1 && childPosition == 0) {
					// write email
					Intent intent = new Intent(HomeActivity.this,
							MailEditActivity.class);
					startActivity(intent);
				} else if (groupPosition == 1 && childPosition == 1) {
					// drafts
					Intent intent = new Intent(HomeActivity.this,
							MailDraftsActivity.class);
					startActivity(intent);
				} else if (groupPosition == 2 && childPosition == 0) {
					// view all mail
					Intent intent = new Intent(HomeActivity.this,
							MailBoxActivity.class);
					intent.putExtra("TYPE", "INBOX");
					intent.putExtra("status", ExMailConstant.FOLDER_INBOX);
					startActivity(intent);
				} else if (groupPosition == 2 && childPosition == 1) {
					// view unread email
					Intent intent = new Intent(HomeActivity.this,
							MailBoxActivity.class);
					intent.putExtra("TYPE", "INBOX");
					intent.putExtra("status", ExMailConstant.FOLDER_UNREAD);
					startActivity(intent);
				} else if (groupPosition == 2 && childPosition == 2) {
					// view read email
					Intent intent = new Intent(HomeActivity.this,
							MailBoxActivity.class);
					intent.putExtra("TYPE", "INBOX");
					intent.putExtra("status", ExMailConstant.FOLDER_READ);
					startActivity(intent);
				} else if (groupPosition == 3 && childPosition == 0) {
					// view calendar
					Intent intent = new Intent(HomeActivity.this,
							BaseCalendar.class);
					startActivity(intent);
				} else if (groupPosition == 3 && childPosition == 1) {
					// view notes
					// TODO NOTES
				}
				adapter.notifyDataSetChanged();
				return false;
			}

		});

	}

	/**
	 * show dialog to store new contact
	 */
	@SuppressLint("InflateParams")
	private void addContactDialog() {
		AlertDialog.Builder builder = new Builder(HomeActivity.this);
		builder.setTitle(getResources().getString(R.string.add_contact));

		View view = getLayoutInflater().inflate(R.layout.email_add_address,
				null);
		final EditText name = (EditText) view.findViewById(R.id.name);
		final EditText addr = (EditText) view.findViewById(R.id.address);

		builder.setView(view);
		builder.setPositiveButton(getResources().getString(R.string.ok),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						insertAddress(name.getText().toString().trim(), addr
								.getText().toString().trim());
					}
				});
		builder.setNegativeButton(getResources().getString(R.string.cancel),
				null);
		builder.show();

	}

	/**
	 * insert contacts to database
	 * 
	 * @param user
	 *            contact's name
	 * @param address
	 *            contact's email address
	 */
	private void insertAddress(String user, String address) {
		if (user == null) {
			Toast.makeText(HomeActivity.this,
					"Contact name should not be empty", Toast.LENGTH_SHORT)
					.show();
		} else {
			if (!EmailFormatUtil.emailFormat(address)) {
				Toast.makeText(HomeActivity.this, "Address format error",
						Toast.LENGTH_SHORT).show();
			} else {
				Uri uri = Uri
						.parse("content://com.cuhk.cse.exmail.emailcontactsprovider");
				ContentValues values = new ContentValues();
				values.put("useraccount", MyApplication.info.getUserName());
				values.put("name", user);
				values.put("address", address);
				getContentResolver().insert(uri, values);

				Toast.makeText(HomeActivity.this, "Add contacts success!",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	/**
	 * adapter for expandableListView
	 * 
	 */
	private class MyExpendAdapter extends BaseExpandableListAdapter {

		/**
		 * pic state
		 */
		// int []group_state=new
		// int[]{R.drawable.group_right,R.drawable.group_down};

		/**
		 * group title
		 */
		String[] group_title = getResources().getStringArray(
				R.array.array_home_item);

		/**
		 * child text
		 */
		String[][] child_text = new String[][] {
				getResources().getStringArray(R.array.array_contact_item),
				getResources().getStringArray(R.array.array_sendemail_item),
				getResources().getStringArray(R.array.array_inbox_item),
				getResources().getStringArray(R.array.array_tool_item) };
		int[][] child_icons = new int[][] {
				{ R.drawable.listlianxiren, R.drawable.tianjia },
				{ R.drawable.xieyoujian, R.drawable.caogaoxiang },
				{ R.drawable.all, R.drawable.notread, R.drawable.hasread },
				{ R.drawable.calendar, R.drawable.notes } };

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return child_text[groupPosition][childPosition];
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@SuppressLint({ "SimpleDateFormat", "InflateParams" })
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			convertView = getLayoutInflater().inflate(R.layout.email_child,
					null);
			TextView tv = (TextView) convertView.findViewById(R.id.tv);
			tv.setText(child_text[groupPosition][childPosition]);

			ImageView iv = (ImageView) convertView
					.findViewById(R.id.child_icon);
			iv.setImageResource(child_icons[groupPosition][childPosition]);
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return child_text[groupPosition].length;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return group_title[groupPosition];
		}

		@Override
		public int getGroupCount() {
			return group_title.length;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			convertView = getLayoutInflater().inflate(R.layout.email_group,
					null);

			ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
			ImageView iv = (ImageView) convertView.findViewById(R.id.iv);
			TextView tv = (TextView) convertView.findViewById(R.id.iv_title);

			iv.setImageResource(R.drawable.group_right);
			tv.setText(group_title[groupPosition]);

			if (groupPosition == 0) {
				icon.setImageResource(R.drawable.constants);
			} else if (groupPosition == 1) {
				icon.setImageResource(R.drawable.mailto);
			} else if (groupPosition == 2) {
				icon.setImageResource(R.drawable.mailbox);
			} else if (groupPosition == 3) {
				icon.setImageResource(R.drawable.tool);
			}

			if (selectedGroupId != groupPosition) {
				iv.setImageResource(R.drawable.group_right);
			} else {
				iv.setImageResource(R.drawable.group_down);
			}

			return convertView;
		}

		/**
		 * Indicates whether the item ids are stable across changes to the
		 * underlying data
		 */
		@Override
		public boolean hasStableIds() {
			return true;
		}

		/**
		 * Whether the child at the specified position is selectable
		 */
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	/**
	 * exit app
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) < 2000) {
				android.os.Process.killProcess(android.os.Process.myPid());
			} else {
				Toast.makeText(HomeActivity.this,
						"Press one more and exit app.", Toast.LENGTH_SHORT)
						.show();
				mExitTime = System.currentTimeMillis();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	class onClickListen implements android.view.View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.account_manage_btn) {
				Intent mIntent = new Intent(HomeActivity.this,
						SettingActivity.class);
				startActivity(mIntent);
			}
		}
	}
}
