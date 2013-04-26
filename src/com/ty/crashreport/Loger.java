package com.ty.crashreport;
import android.util.Log;

/**
 * 
 * @author zengyongping
 *
 */
//CHECKSTYLE:OFF
class LogConfig {
	/**
	 * 
	 * @param level
	 * @return
	 */
	final boolean isWrite(int level) {
		if (DEBUG) {
			return true;
		}
		if (level >= mLogLevel) {
			return true;
		}
		return false;
	}

	LogConfig() {
		read();
	}

	private synchronized void read() {

		String level = "INFO";
		if (level != null) {
			if (level.equals("ASSERT")) {
				mLogLevel = Log.ASSERT;
			} else if (level.equals("ERROR")) {
				mLogLevel = Log.ERROR;
			} else if (level.equals("WARN")) {
				mLogLevel = Log.WARN;
			} else if (level.equals("INFO")) {
				mLogLevel = Log.INFO;
			} else if (level.equals("DEBUG")) {
				mLogLevel = Log.DEBUG;
			} else if (level.equals("VERBOSE")) {
				mLogLevel = Log.VERBOSE;
			}
		}
	}
	final boolean isD() {
		return DEBUG;
	}
	final boolean isT() {
		return PRINT_TIME;
	}
	private static final int LOGCLOSE = 100;
	private int mLogLevel = LOGCLOSE; //配置文件中log等级	
	private static final boolean DEBUG = false;
	private static final boolean PRINT_TIME = true;
}

/**
 * @author zengyongping
 * 
 */
public final class Loger {
	private static LogConfig sLogConfig = new LogConfig();

	/**
	 * Priority constant for the println method; use Log.v.
	 */
	public static final int VERBOSE = Log.VERBOSE;

	/**
	 * Priority constant for the println method; use Log.d.
	 */
	public static final int DEBUG = Log.DEBUG;

	/**
	 * Priority constant for the println method; use Log.i.
	 */
	public static final int INFO = Log.INFO;

	/**
	 * Priority constant for the println method; use Log.w.
	 */
	public static final int WARN = Log.WARN;

	/**
	 * Priority constant for the println method; use Log.e.
	 */
	public static final int ERROR = Log.ERROR;

	/**
	 * Priority constant for the println method.
	 */
	public static final int ASSERT = Log.ASSERT;

	/**
	 * print time log use this tag    
	 */
	public static final String T_TAG = "TT";

	public static final boolean isLoggable(String tag, int level) {
		return Log.isLoggable(tag, level);
	}

	public static final boolean isD() {
		return sLogConfig.isD();
	}

	public static final boolean isT() {
		return sLogConfig.isT();
	}
	public static final int println(int priority, String tag, String msg) {
		if (sLogConfig.isWrite(priority)) {
			return Log.println(priority, createTag(tag, null), msg);
		}
		return -1;
	}

	public static final String getStackTraceString(Throwable tr) {
		return Log.getStackTraceString(tr);
	}

	/**
	 * 
	 * @param tag 标识符
	 * @param msg 打印信息
	 */
	public static final void v(String tag, String msg) {
		if (sLogConfig.isWrite(Log.VERBOSE)) {
			Log.v(createTag(tag, null), msg);
		}
	}

	/**
	 * @param tag 标识符
	 * @param msg 打印信息
	 * @param classRef 要打印对象类名所对应的对象指针
	 */
	public static final void v(String tag, String msg, Object classRef) {
		if (sLogConfig.isWrite(Log.VERBOSE)) {
			Log.v(createTag(tag, classRef), msg);
		}
	}

	/**
	 * @param tag 标识符
	 * @param msg 打印信息
	 * @param Throwable tr 抛出的异常
	 */
	public static final void v(String tag, String msg, Throwable tr) {
		if (sLogConfig.isWrite(Log.VERBOSE)) {
			Log.v(createTag(tag, null), msg, tr);
		}
	}

	/**
	 * 
	 * @param tag
	 * @param msg
	 */
	public static final void d(String tag, String msg) {
		if (sLogConfig.isWrite(Log.DEBUG)) {
			Log.d(createTag(tag, null), msg);
		}
	}

	/**
	 * @param tag 标识符
	 * @param msg 打印信息
	 * @param Throwable tr 抛出的异常
	 */
	public static final void d(String tag, String msg, Throwable tr) {
		if (sLogConfig.isWrite(Log.DEBUG)) {
			Log.d(createTag(tag, null), msg, tr);
		}
	}

	/**
	 * 
	 * @param tag
	 * @param msg
	 * @param classRef
	 */
	public static final void d(String tag, String msg, Object classRef) {
		if (sLogConfig.isWrite(Log.DEBUG)) {
			Log.d(createTag(tag, classRef), msg);
		}
	}

	/**
	 * 
	 * @param tag
	 * @param msg
	 */
	public static final void i(String tag, String msg) {
		if (sLogConfig.isWrite(Log.INFO)) {
			Log.i(createTag(tag, null), msg);
		}
	}

	/**
	 * 
	 * @param tag
	 * @param msg
	 * @param classRef
	 */
	public static final void i(String tag, String msg, Object classRef) {
		if (sLogConfig.isWrite(Log.INFO)) {
			Log.i(createTag(tag, classRef), msg);
		}
	}

	/**
	 * @param tag 标识符
	 * @param msg 打印信息
	 * @param Throwable tr 抛出的异常
	 */
	public static final void i(String tag, String msg, Throwable tr) {
		if (sLogConfig.isWrite(Log.INFO)) {
			Log.i(createTag(tag, null), msg, tr);
		}
	}

	/**
	 * 
	 * @param tag
	 * @param msg
	 */
	public static final void w(String tag, String msg) {
		if (sLogConfig.isWrite(Log.WARN)) {
			Log.w(createTag(tag, null), msg);
		}
	}
	/**
	 * 
	 * @param tag
	 * @param msg
	 * @param classRef
	 */
	public static final void w(String tag, String msg, Object classRef) {
		if (sLogConfig.isWrite(Log.WARN)) {
			Log.w(createTag(tag, classRef), msg);
		}
	}

	/**
	 * @param tag 标识符
	 * @param msg 打印信息
	 * @param Throwable tr 抛出的异常
	 */
	public static final void w(String tag, String msg, Throwable tr) {
		if (sLogConfig.isWrite(Log.WARN)) {
			Log.w(createTag(tag, null), msg, tr);
		}
	}

	/**
	 * 
	 * @param tag
	 * @param msg
	 */
	public static final void e(String tag, String msg) {
		if (sLogConfig.isWrite(Log.ERROR)) {
			Log.e(createTag(tag, null), msg);
		}
	}

	/**
	 * 
	 * @param tag
	 * @param msg
	 * @param classRef
	 */
	public static final void e(String tag, String msg, Object classRef) {
		if (sLogConfig.isWrite(Log.ERROR)) {
			Log.e(createTag(tag, classRef), msg);
		}
	}

	/**
	 * @param tag 标识符
	 * @param msg 打印信息
	 * @param Throwable tr 抛出的异常
	 */
	public static final void e(String tag, String msg, Throwable tr) {
		if (sLogConfig.isWrite(Log.ERROR)) {
			Log.e(createTag(tag, null), msg, tr);
		}
	}
	/**
	 * 
	 * @param tag
	 * @param classRef
	 * @return
	 */
	private static final String createTag(String tag, Object classRef) {
		String tagRet = "com.jb@";
		try {
			if (classRef != null) {
				tagRet += classRef.getClass().getName();
				tagRet += "::";
				tagRet += new Exception().getStackTrace()[2].getMethodName();
				tagRet += "@";
			}
			tagRet += tag;
		} catch (Exception e) {

		}
		return tagRet;
	}
}
