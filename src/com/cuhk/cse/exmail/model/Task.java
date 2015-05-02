package com.cuhk.cse.exmail.model;

import java.util.HashMap;

import android.content.Context;

/**
 * Network task configuration info
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2015-4-21 PM10:54:35
 * @version V1.0
 * 
 */
public class Task {

	public static final int LOGIN = 1;
	public static final int REGISTER = 2;
	public static final int LOAD_NET_INBOX_EMAIL = 3;
	public static final int LOAD_NET_SPORTS = 4;
	public static final int LOAD_DETAIL_SPORTS = 15;
	public static final int USER_INFO = 5;
	public static final int SELECT_LOC = 6;
	public static final int TO_USER_FRAG = 7;
	public static final int TO_LIST_FRAG = 8;
	public static final int TO_MAP_FRAG = 9;
	public static final int DONE_SELECT_LOC = 10;
	public static final int ADD_NEW_SPORT_TO_SERVER = 11;
	public static final int DONE_ADD_NEW_SPORT_TO_SERVER = 12;
	public static final int ATTEND_EVENTS = 13;
	public static final int QUIT_EVENT = 14;

	private int taskId;
	private HashMap<?, ?> taskParams;
	private Context context;

	public Task(int taskId, HashMap<?, ?> taskParams, Context mcontext) {
		super();
		this.taskId = taskId;
		this.taskParams = taskParams;
		this.context = mcontext;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public HashMap<?, ?> getTaskParams() {
		return taskParams;
	}

	public void setTaskParams(HashMap<?, ?> taskParams) {
		this.taskParams = taskParams;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context mcontext) {
		this.context = mcontext;
	}

}
