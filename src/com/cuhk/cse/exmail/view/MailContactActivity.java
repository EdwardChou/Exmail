package com.cuhk.cse.exmail.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cuhk.cse.exmail.R;
import com.cuhk.cse.exmail.model.Contact;
import com.cuhk.cse.exmail.model.db.DBProvider;

/**
 * contact activity
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2015-5-2 下午9:01:16
 * @version V1.0
 * 
 */
public class MailContactActivity extends Activity {
	private ListView lv;
	private List<Contact> list;
	private ProgressDialog dialog;
	private Myadapter adapter;
	boolean isSelectMod;
	List<String> chooseUserList;
	List<Boolean> state;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.email_contact);
		isSelectMod = getIntent().getBooleanExtra("select_mode", false);
		if (isSelectMod) {
			Button addButton = (Button) findViewById(R.id.email_contact_right_btn);
			addButton.setBackgroundResource(R.drawable.ic_done_white_36dp);
			chooseUserList = new ArrayList<String>();
		}
		DBProvider.init(this);
		dialog = new ProgressDialog(this);
		dialog.setMessage("Loading...");
		dialog.show();

		list = getAllContacts();
		init();

		dialog.dismiss();
		registerForContextMenu(lv);
	}

	@Override
	protected void onResume() {
		list = getAllContacts();
		adapter.notifyDataSetChanged();
		super.onResume();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.constacts_menu, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int id = (int) info.id;
		switch (item.getItemId()) {
		case R.id.update:
			updateAddress(list.get(id).getName(), list.get(id).getAddress());
			break;
		case R.id.delete:
			deleteAddress(list.get(id).getName());
			break;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * update contact
	 * 
	 * @param name
	 * @param address
	 */
	private void updateAddress(final String name, String address) {
		AlertDialog.Builder builder = new Builder(MailContactActivity.this);
		builder.setTitle("Modify email address");
		final EditText edit = new EditText(MailContactActivity.this);
		edit.setText(address);
		builder.setView(edit);
		builder.setPositiveButton("Ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				DBProvider.getInstance().updateContact(name,
						edit.getText().toString().trim());
				list = getAllContacts();
				adapter.notifyDataSetChanged();
				Toast.makeText(MailContactActivity.this, "Success modify",
						Toast.LENGTH_SHORT).show();
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}

	/**
	 * delete contact
	 * 
	 * @param name
	 * @param address
	 */
	private void deleteAddress(final String name) {
		AlertDialog.Builder builder = new Builder(MailContactActivity.this);
		builder.setMessage("Are you sure to delete this contact?");
		builder.setPositiveButton("Ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DBProvider.getInstance().deleteContact(name);

				list = getAllContacts();
				adapter.notifyDataSetChanged();
				Toast.makeText(MailContactActivity.this,
						"Successful delete contact", Toast.LENGTH_SHORT).show();
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}

	/**
	 * query all contact
	 * 
	 * @return
	 */
	private List<Contact> getAllContacts() {
		List<Contact> users = DBProvider.getInstance().queryContacts();
		return users;
	}

	/**
	 * initialization
	 */
	private void init() {
		lv = (ListView) findViewById(R.id.lv_constant);

		adapter = new Myadapter();
		lv.setAdapter(adapter);

	}

	private class Myadapter extends BaseAdapter {

		public Myadapter() {
			state = new ArrayList<Boolean>();
			for (Contact contact : list) {
				state.add(false);
			}
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(MailContactActivity.this,
						R.layout.email_contact_item, null);
				holder.alpha = (TextView) convertView
						.findViewById(R.id.tv_alpha);
				holder.name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.address = (TextView) convertView
						.findViewById(R.id.tv_address);
				holder.select = (CheckBox) convertView
						.findViewById(R.id.email_contact_item_cb);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Contact user = list.get(position);
			holder.alpha
					.setText(user.getPinyin().substring(0, 1).toUpperCase());
			if (position == 0) {
				holder.alpha.setVisibility(View.VISIBLE);
			} else if (list.get(position).getPinyin().substring(0, 1)
					.equals(list.get(position - 1).getPinyin().substring(0, 1))) {
				holder.alpha.setVisibility(View.INVISIBLE);
			} else {
				holder.alpha.setVisibility(View.VISIBLE);
			}
			if (isSelectMod) {
				holder.select.setVisibility(View.VISIBLE);
				holder.select
						.setOnCheckedChangeListener(new SearchItemOnCheckedChangeListener(
								position, state));
				holder.select.setChecked(state.get(position));
			}

			holder.name.setText(user.getName());
			holder.address.setText(user.getAddress());
			return convertView;
		}

		class ViewHolder {
			TextView alpha;
			TextView name;
			TextView address;
			CheckBox select;
		}

	}

	class SearchItemOnCheckedChangeListener implements OnCheckedChangeListener {
		private int id;
		private List<Boolean> state;

		public SearchItemOnCheckedChangeListener(int id, List<Boolean> state) {
			this.id = id;
			this.state = state;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			state.set(id, isChecked);
			// if (isChecked) {
			// checkedCount++;
			// } else {
			// checkedCount--;
			// }
			// if (checkCoutn > 0) {
			// searchButton.setVisibility(Button.INVISIBLE);
			// } else {
			// searchButton.setVisibility(Button.VISIBLE);
			// }
		}
	}

	@Override
	protected void onDestroy() {
		DBProvider.closeDB();
		super.onDestroy();
	}

	/**
	 * finish activity
	 * 
	 * @param v
	 */
	public void back(View v) {
		finish();
	}

	public void add(View v) {
		if (isSelectMod) {
			for (int i = 0; i < state.size(); i++) {
				if (state.get(i) == true) {
					chooseUserList.add(list.get(i).getName());
				}
			}
			Intent intent = new Intent();
			intent.putStringArrayListExtra("chooseUsers",
					(ArrayList<String>) chooseUserList);
			setResult(RESULT_OK, intent);
			finish();
		} else {
			Intent intent = new Intent(MailContactActivity.this,
					MailAddContactActivity.class);
			startActivity(intent);
		}
	}

}
