package com.google.android.exoplayer.demo.com.google.android.exoplayer.bean;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * 用于解析从服务器返回的数据
 * 
 * @author Chuanhang.Gu
 * 
 */
public class ResponseBean {
	public static final String SUCCESS_CODE="0";
	public static final String ERROR_CODE="-1";

	@SerializedName("ErrorCode")
	private String errorCode;
	@SerializedName("ErrorString")
	private String errorString;
	/**
	 * 当返回的数据合法的而是，才有意义
	 */
	@SerializedName("ResponseObject")
	private JsonObject responseObject;

	@Override
	public String toString() {
		return "ErrorCode:" + errorCode + "	ErrorString:" + errorString
				+ "	ResponseObject" + responseObject;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorString() {
		return errorString;
	}

	public void setErrorString(String errorString) {
		this.errorString = errorString;
	}

	public JsonObject getResponseObject() {
		return responseObject;
	}

	public void setResponseObject(JsonObject responseObject) {
		this.responseObject = responseObject;
	}

}
