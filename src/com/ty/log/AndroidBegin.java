/*
 * 文 件 名:  LogUtil.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  caoshilei
 * 修改时间:  2013-10-14
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.ty.log;


/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  tangyong
 * @date  [2013-10-14]
 */
public final class AndroidBegin {
	
	private static final String TAG_ANDROID = "tag_android";
	
	public final static void d(Class<?> cls, String function, String msg) {
		LogUtil.d(TAG_ANDROID, cls, function, msg);
	}
	
	public final static void i(Class<?> cls, String function, String msg) {
		LogUtil.i(TAG_ANDROID, cls, function, msg);
	}
	
	public final static void w(Class<?> cls, String function, String msg) {
		LogUtil.w(TAG_ANDROID, cls, function, msg);
	}
	
	public final static void e(Class<?> cls, String function, String msg) {
		LogUtil.e(TAG_ANDROID, cls, function, msg);
	}
	
	public final static void e(Class<?> cls, String function, String msg, Throwable tr) {
		LogUtil.e(TAG_ANDROID, cls, function, msg, tr);
	}
	
	public final static boolean isLogOpen() {
		return LogUtil.isLogOpen();
	}
}
