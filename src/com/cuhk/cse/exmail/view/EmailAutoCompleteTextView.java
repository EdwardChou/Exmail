package com.cuhk.cse.exmail.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.cuhk.cse.exmail.R;

/**
 * Auto Complete Textview with email
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2015-4-15 下午5:25:04
 * @version V1.0
 * 
 */
public class EmailAutoCompleteTextView extends AutoCompleteTextView {

	private static final String TAG = EmailAutoCompleteTextView.class
			.getSimpleName();

	private String[] emailSuffixs = new String[] { "@qq.com", "@163.com",
			"@126.com", "@gmail.com", "@sina.com", "@hotmail.com",
			"@yahoo.com", "@sohu.com", "@foxmail.com", "@139.com" };

	public EmailAutoCompleteTextView(Context context) {
		super(context);
		init(context);
	}

	public EmailAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public EmailAutoCompleteTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public void setAdapterString(String[] es) {
		if (es != null & es.length > 0) {
			this.emailSuffixs = es;
		}
	}

	private void init(final Context context) {
		EmailAutoCompleteAdapter adapter = new EmailAutoCompleteAdapter(
				context, R.layout.email_auto_complete_textview_item,
				emailSuffixs);
		this.setAdapter(adapter);
		// auto complete since 1st character
		this.setThreshold(1);
		this.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					String text = EmailAutoCompleteTextView.this.getText()
							.toString();
					// when regain focus, restart auto complete
					if (!"".equals(text))
						performFiltering(text, 0);
				} else {
					// if lost focus, check email format
					EmailAutoCompleteTextView ev = (EmailAutoCompleteTextView) v;
					String text = ev.getText().toString();
					if (text != null
							&& text.matches("^[a-zA-Z0-9_]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")) {

					} else {
						Toast toast = Toast.makeText(context,
								"Error email format", Toast.LENGTH_SHORT);
						toast.show();
					}
				}
			}
		});
	}

	@Override
	protected void performFiltering(CharSequence text, int keyCode) {
		// call after input text, compare input with adapter data, if match,
		// then show up in dropdown list
		Log.i(TAG, "performFiltering() " + text.toString() + "-" + keyCode);
		String string = text.toString();
		int index = string.indexOf("@");
		if (index != -1) {
			if (string.matches("^[a-zA-Z0-9_]+$")) {
				super.performFiltering("@", keyCode);
			} else {
				this.dismissDropDown();
			}
		} else {
			super.performFiltering(text, keyCode);
		}
	}

	@Override
	protected void replaceText(CharSequence text) {
		// when we select from dropdown list, android use adapter to fill in the
		// text field, combine suffixs with user input
		Log.i(TAG, "repalce text" + text.toString());
		String string = this.getText().toString();
		int index = string.indexOf("@");
		if (index != -1) {
			string = string.substring(0, index);
		}
		super.replaceText(string + text);
	}

	private class EmailAutoCompleteAdapter extends ArrayAdapter<String> {

		Context context;

		public EmailAutoCompleteAdapter(Context context, int resId,
				String[] suffix) {
			super(context, resId, suffix);
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.i(TAG, "getView()");
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.email_auto_complete_textview_item, null);
			}
			TextView tv = (TextView) convertView
					.findViewById(R.id.email_auto_ctx_item_tv);
			String string = EmailAutoCompleteTextView.this.getText().toString();
			int index = string.indexOf("@");
			if (index != -1) {
				string = string.substring(0, index);
			}
			// combine input with email suffixs in adapter, show in dropdown
			// list
			tv.setText(string + getItem(position));
			Log.i(TAG, tv.getText().toString());
			return convertView;
		}

	}

}
