package com.cuhk.cse.exmail.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cuhk.cse.exmail.R;
import com.cuhk.cse.exmail.controller.utils.EmailFormatUtil;
import com.cuhk.cse.exmail.model.app.MyApplication;
import com.cuhk.cse.exmail.model.db.DBProvider;

/**
 * add contact
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2015-5-2 下午9:40:22
 * @version V1.0
 * 
 */
public class MailAddContactActivity extends Activity {
	EditText name, address;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contact);
		TextView accountTextView = (TextView) findViewById(R.id.activity_add_contact_account_name_tv);
		accountTextView.setText(MyApplication.info.getUserName());
		name = (EditText) findViewById(R.id.activity_add_contact_name_et);
		address = (EditText) findViewById(R.id.activity_add_contact_address_et);
	}

	/**
	 * return
	 * 
	 * @param v
	 */
	public void back(View v) {
		finish();
	}

	/**
	 * add contact
	 * 
	 * @param v
	 */
	public void choose(View v) {
		if ("".equals(name.getText().toString())) {
			Toast.makeText(this, "Name can't be empty", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (!EmailFormatUtil.emailFormat(address.getText().toString())) {
			Toast.makeText(this, "Error address format", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		DBProvider.init(this);
		DBProvider.getInstance().insertContact(name.getText().toString(),
				address.getText().toString());
		DBProvider.closeDB();
		finish();
	}
}
