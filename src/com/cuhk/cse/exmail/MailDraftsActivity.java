package com.cuhk.cse.exmail;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cuhk.cse.exmail.bean.Draft;
import com.cuhk.cse.exmail.db.DBProvider;

public class MailDraftsActivity extends Activity {
	private ListView lv;
	private List<Draft> drafts = new ArrayList<Draft>();
	private MyAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.email_draft);
		drafts = getDrafts();
		lv = (ListView) findViewById(R.id.caogaoxiang);

		adapter = new MyAdapter();
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Draft draft = drafts.get(position);
				Intent intent = new Intent(MailDraftsActivity.this,
						MailEditActivity.class);
				intent.putExtra("draftid", draft.getDraftid());
				startActivity(intent);
				finish();

			}

		});
	}

	/**
	 * retrieve all draft
	 * 
	 * @return List<Draft>
	 */
	private List<Draft> getDrafts() {
		DBProvider.init(this);
		List<Draft> drafts = DBProvider.getInstance().queryDrafts();
		return drafts;
	};

	/**
	 * adapter
	 * 
	 * @author EdwardChou edwardchou_gmail_com
	 * @date 2015-4-10 下午3:51:01
	 * @version V1.0
	 * 
	 */
	private class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return drafts.size();
		}

		@Override
		public Object getItem(int position) {
			return drafts.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View item = View.inflate(MailDraftsActivity.this,
					R.layout.email_draft_item, null);
			TextView mailto = (TextView) item.findViewById(R.id.tv_mailto);
			TextView mailsubject = (TextView) item
					.findViewById(R.id.tv_mailsubject);

			Draft draft = drafts.get(position);
			mailto.setText(draft.getMailto());
			mailsubject.setText(draft.getSubject());

			return item;
		}

	}

	/**
	 * return
	 * 
	 * @param v
	 */
	public void back(View v) {
		finish();
	}

}
