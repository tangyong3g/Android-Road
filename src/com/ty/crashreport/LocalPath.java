package com.ty.crashreport;

import android.os.Environment;

/**
 * 
 * 类描述:SD卡存放内容的路径
 * 功能详细描述:
 * 
 * @author  wenjiaming
 * @date  [2012-9-4]
 */
//CHECKSTYLE:OFF
public class LocalPath {
	/** 根据不同的Widget修改  **/
	public final static String APP_NAME = "android_begin";

	// 存储路径
	public final static String GOLAUNCHEREX_DIR = "/android_begin";

	/**
	 * 日志文件备份目录
	 */
	public final static String LOG_DIR = Environment.getExternalStorageDirectory()
			+ GOLAUNCHEREX_DIR + "/log/";
	/**
	 * 日常记录用户行为日志
	 */
	public final static String RUNTIME_LOG_PATH = Environment.getExternalStorageDirectory()
			+ GOLAUNCHEREX_DIR + "/runtime/";

	/**
	 * PackageName
	 * 有些地方取不到PackageName，写死在此处
	 */
	public final static String PACKAGE_NAME = "com.gtp.nextlauncher.livepaper.bulbex";

}
