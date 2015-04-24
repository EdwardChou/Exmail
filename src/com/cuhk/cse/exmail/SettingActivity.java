package com.cuhk.cse.exmail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

/** 
 * Setting
 * 
 * @author EdwardChou edwardchou_gmail_com 
 * @date 2015-3-26 PM1:23:33 
 * @version V1.0   
 *  
 */
public class SettingActivity extends Activity implements OnClickListener {

	Button backButton;
	RelativeLayout addAccountLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.setting);
		
		backButton = (Button) findViewById(R.id.setting_back_btn);
		backButton.setOnClickListener(this);
		
		addAccountLayout = (RelativeLayout) findViewById(R.id.add_account_rl);
		addAccountLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.setting_back_btn) {
			finish();
		}
		if (v.getId() == R.id.add_account_rl) {
			Intent mIntent = new Intent(SettingActivity.this, LoginActivity.class);
			mIntent.putExtra("add", true);
			startActivity(mIntent);
		}
	}
	
	

}
