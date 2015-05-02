package com.cuhk.cse.exmail.model;

/**
 * result entity for transfer info between service to activity
 * 
 * @author EdwardChou edwardchou_gmail_com
 * @date 2015-5-2 下午5:28:48
 * @version V1.0
 * 
 */
public class Result {

	public static final int SUCCESS = 0;
	public static final int FAIL = 2;
	public static final int ERROR = 4;

	private int taskId;
	private int status;
	private Object resultObj;

	public Result(int taskId, int status, Object resultObj) {
		super();
		this.taskId = taskId;
		this.status = status;
		this.resultObj = resultObj;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Object getResultObj() {
		return resultObj;
	}

	public void setResultObj(Object resultObj) {
		this.resultObj = resultObj;
	}

}
