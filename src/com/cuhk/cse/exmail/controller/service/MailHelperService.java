package com.cuhk.cse.exmail.controller.service;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.cuhk.cse.exmail.controller.utils.MailHelper;
import com.cuhk.cse.exmail.model.Notify;
import com.cuhk.cse.exmail.model.Result;
import com.cuhk.cse.exmail.model.Task;
import com.cuhk.cse.exmail.model.db.DBProvider;

/**
 * download service
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2015-4-21 下午10:48:37
 * @version V1.0
 * 
 */
public class MailHelperService extends Service implements Runnable {

	private static final String TAG = "MailHelperService";
	// service running flags
	public static boolean isRun = false;
	// notify interface list
	private static ArrayList<Notify> allActivity = new ArrayList<Notify>();
	// task configuration info
	private static ArrayList<Task> allTasks = new ArrayList<Task>();

	private final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		public MailHelperService getService() {
			return MailHelperService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		DBProvider.init(getApplicationContext());
		isRun = true;
		// start new thread
		new Thread(this).start();
		Log.i(TAG, "MainService onCreate()");
	}

	@Override
	public void onDestroy() {
		isRun = false;
		super.onDestroy();
		DBProvider.closeDB();
	}

	/**
	 * add new task to new thread list
	 * 
	 * @param task
	 */
	public static void addNewTask(Task t) {
		Log.i(TAG, "newTask");
		allTasks.add(t);
	}

	/**
	 * add new task and interface to new thread list
	 * 
	 * @param task
	 * @param notify
	 */
	public static void addNewTask(Task t, Notify n) {
		Log.i(TAG, "newTask");
		allTasks.add(t);
		allActivity.add(n);
	}

	/**
	 * add new activity notify interface
	 * 
	 * @param notify
	 */
	public static void addActivity(Notify n) {
		Log.i(TAG, "addActivity");
		allActivity.add(n);
	}

	/**
	 * remove activity notify interface from the list
	 * 
	 * @param notify
	 */
	public static void removeActivity(Notify n) {
		allActivity.remove(n);
	}

	/**
	 * get activity notify interface by class name
	 * 
	 * @param className
	 * @return Notify
	 */
	public static Notify getActivityByName(String name) {
		for (Notify n : allActivity) {
			if (n.getClass().getName().indexOf(name) >= 0) {
				return n;
			}
		}
		return null;
	}

	// task opertaion end

	@Override
	public void run() {
		// if service is running
		while (isRun) {
			try {
				if (allTasks.size() > 0) {
					doTask(allTasks.get(0));
				} else {
					try {
						Thread.sleep(2000);
					} catch (Exception e) {
					}
				}
			} catch (Exception e) {
				if (allTasks.size() > 0)
					allTasks.remove(allTasks.get(0));
				Log.e(TAG, e.toString());
			}
		}
	}

	private void doTask(Task task) {
		Message msg = handler.obtainMessage();
		msg.what = task.getTaskId();

		switch (task.getTaskId()) {
		case Task.LOAD_NET_INBOX_EMAIL:
			int status = Result.SUCCESS;
			if (!MailHelper.getMailByIMAP(task.getContext()))
				status = Result.FAIL;
			msg.obj = new Result(task.getTaskId(), status, null);
			break;
		default:
			break;
		}
		allTasks.remove(task);
		handler.sendMessage(msg);
	}

	private static Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case Task.LOAD_NET_INBOX_EMAIL:
				MailHelperService.getActivityByName("MailBoxActivity").refresh(
						msg.obj);
				break;
			default:
				break;
			}
		}

	};

}
