package com.seoul.kysersozelee.adbController.adb;

public class AdbResult {
	private boolean isSuccess;

	private String data;

	public AdbResult() {
		super();
	}

	public AdbResult(boolean success, String data) {
		super();
		this.isSuccess = success;
		this.data = data;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean success) {
		this.isSuccess = success;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
