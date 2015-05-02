package com.cuhk.cse.exmail.view;

import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cuhk.cse.exmail.R;
import com.cuhk.cse.exmail.R.id;
import com.cuhk.cse.exmail.R.layout;
import com.cuhk.cse.exmail.R.string;
import com.cuhk.cse.exmail.controller.utils.EmailFormatUtil;
import com.cuhk.cse.exmail.controller.utils.HttpUtil;
import com.cuhk.cse.exmail.model.Account;
import com.cuhk.cse.exmail.model.app.MyApplication;
import com.cuhk.cse.exmail.model.db.DBProvider;

/**
 * Login, entrance activity
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2014-10-16 上午11:58:16
 * @version V1.0
 * 
 */
public class LoginActivity extends Activity implements TextWatcher,
		OnClickListener {

	private EmailAutoCompleteTextView emailAddress;
	private EditText password;
	private Button clearAddress;
	private Button emailLogin;
	private ProgressDialog dialog;
	// private SharedPreferences sp;
	private boolean isRemember;
	// whether current mode is adding new account
	private boolean isAddNewAccount;

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			dialog.dismiss();
			if (MyApplication.session == null) {
				Toast.makeText(LoginActivity.this,
						getResources().getString(R.string.err_wrong_acc_pass),
						Toast.LENGTH_SHORT).show();
				if (isRemember)
					DBProvider.getInstance().deleteAccount(
							MyApplication.info.getUserName());
			} else {
				if (!isRemember)
					rememberPassword();
				if (!isAddNewAccount) {
					Intent intent = new Intent(LoginActivity.this,
							HomeActivity.class);
					startActivity(intent);
				}
				finish();
			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// must call before setContentiew()
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.email_login);

		initView();
		isAddNewAccount = getIntent().getBooleanExtra("add", false);
		if (!isAddNewAccount) {
			isRemember = autoLogin();
		}
	}

	/**
	 * Initialize the views of login panel
	 */
	private void initView() {
		emailAddress = (EmailAutoCompleteTextView) findViewById(R.id.emailAddress);
		password = (EditText) findViewById(R.id.password);
		clearAddress = (Button) findViewById(R.id.clear_address);
		emailLogin = (Button) findViewById(R.id.login_btn);

		clearAddress.setOnClickListener(this);
		emailAddress.addTextChangedListener(this);
		emailLogin.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.clear_address) {
			emailAddress.setText("");
		} else if (id == R.id.login_btn) {
			loginEmail();
		}
	}

	private boolean autoLogin() {
		DBProvider.init(this);
		Account account = DBProvider.getInstance().queryAccount();
		if (null != account) {
			String addr = account.getAddress();
			String pwd = account.getPassword();
			if (!"".equals(addr) && !"".equals(pwd)) {
				emailAddress.setText(addr);
				password.setText(pwd);
				loginEmail();
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onStop() {
		DBProvider.closeDB();
		super.onStop();
	}

	private void rememberPassword() {
		Account account = new Account();
		account.setAddress(MyApplication.info.getUserName());
		account.setPassword(MyApplication.info.getPassword());
		account.setLastlogintime((int) new Date().getTime());
		DBProvider.getInstance().insertAccount(account);
	}

	/**
	 * login in the email
	 */
	private void loginEmail() {
		String address = emailAddress.getText().toString().trim();
		String pwd = password.getText().toString().trim();
		if (TextUtils.isEmpty(address)) {
			Toast.makeText(LoginActivity.this,
					getResources().getString(R.string.err_empty_address),
					Toast.LENGTH_SHORT).show();
			return;
		} else if (TextUtils.isEmpty(pwd)) {
			Toast.makeText(LoginActivity.this,
					getResources().getString(R.string.err_empty_pass),
					Toast.LENGTH_SHORT).show();
			return;
		}
		/**
		 * check the address format
		 */
		if (!EmailFormatUtil.emailFormat(address)) {
			Toast.makeText(LoginActivity.this,
					getResources().getString(R.string.err_wrong_add_format),
					Toast.LENGTH_SHORT).show();
		} else {
			String host = "smtp."
					+ address.substring(address.lastIndexOf("@") + 1);
			MyApplication.info.setMailServerHost(host);
			MyApplication.info.setMailServerPort("587");// 587,465
			MyApplication.info.setUserName(address);
			MyApplication.info.setPassword(pwd);
			MyApplication.info.setValidate(true);

			/**
			 * progress bar
			 */
			dialog = new ProgressDialog(LoginActivity.this);
			dialog.setMessage("Loading emails, wait for seconds...");
			dialog.show();

			/**
			 * access network
			 */
			new Thread() {
				@Override
				public void run() {
					// Login in
					HttpUtil util = new HttpUtil();
					MyApplication.session = util.login();
					Message message = handler.obtainMessage();
					message.sendToTarget();
				}

			}.start();
		}
	}

	/**
	 * listener on the text changes
	 * 
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (!TextUtils.isEmpty(s)) {
			clearAddress.setVisibility(View.VISIBLE);
		} else {
			clearAddress.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

}
