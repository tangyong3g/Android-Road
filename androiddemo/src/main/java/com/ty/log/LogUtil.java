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

import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.util.HashMap;

import com.ty.util.Constant;

import android.util.Log;
import android.util.TimingLogger;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  tangyong
 * @date  [2013-10-14]
 */
public final class LogUtil {
	/** 日志输出目录，位于sd卡目录下 */
	public static final String NEXT_LOG_DIR = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
			+ "androidBegin_Data/debug_log";
	/** 日志文件前缀 */
	public static final String LOG_FILE_PREFIX = "debug_log";

	/** 桌面日志默认Tag */
	public static final String TAG_DEFAULT = "nextlauncher";

	/** 功能表日志是否打开,可以设置初始为打开 */
	private static boolean sIsLogOpen = true;

	/** 是否输出日志到文件 */
	private static boolean sIsOutToFile = false;

	/** 时间计算计时器，用于统计输出任务消耗时间 */
	private static HashMap<String, TimingLogger> sTimingLoggerCache = new HashMap<String, TimingLogger>();

	/** 框架日志对象 */
	private static com.go.gl.util.Log sLog = null;

	static {
		if (sIsOutToFile) {
			sLog = new com.go.gl.util.Log(LOG_FILE_PREFIX);
		}
	}

	public static void init() {
		//设置日志开关打开
		sIsLogOpen = true;

		if (!sIsOutToFile) {
			//先清理原来系统插件日志文件
			deleteLogFile();
			//创建一个新的框架日志对象
			sLog = new com.go.gl.util.Log(LOG_FILE_PREFIX);
			//设置日志可以输出到文件
			sIsOutToFile = true;
		}
	}

	/** <br>功能简述:注销日志，注销后日志文件删除，并且不再产生日志
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public static void destory() {
		//关闭日志开关
		sIsLogOpen = false;
		//关闭日志输出到文件开关
		sIsOutToFile = false;
		//重置框架日志对象
		sLog = null;
	}

	/** <br>功能简述:获取功能表模块日志列表
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public static File[] getLogFiles() {
		File nextLogDir = new File(NEXT_LOG_DIR);
		if (!nextLogDir.exists()) {
			return null;
		}

		//获取widget_log开头的所有日志文件
		File[] widgetFiles = nextLogDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if ((null != filename) && (filename.indexOf(LOG_FILE_PREFIX) == 0)) {
					return true;
				} else {
					return false;
				}
			}
		});
		return widgetFiles;
	}

	/** <br>功能简述:删除/sd卡/next_log/目录下appdrawer_log为前缀的日志文件
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private static void deleteLogFile() {
		File[] logFiles = getLogFiles();

		//删除所有appdrawer_log开头的所有日志文件
		if (null == logFiles) {
			return;
		}
		for (File file : logFiles) {
			file.delete();
		}
	}

	public final static void d(Class<?> cls, String function, String msg) {
		d(TAG_DEFAULT, cls, function, msg);
	}

	public final static void d(String tag, Class<?> cls, String function, String msg) {
		if (!sIsLogOpen) {
			return;
		}
		if (null == msg) {
			msg = "";
		}
		msg = MessageFormat.format("[{0}]({1}):{2}", cls.getSimpleName(), function, msg);
		Log.d(tag, msg);
		if (sIsOutToFile) {
			sLog.dns(tag, msg);
		}
	}

	public final static void i(Class<?> cls, String function, String msg) {
		i(TAG_DEFAULT, cls, function, msg);
	}

	public final static void i(String tag, Class<?> cls, String function, String msg) {
		if (!sIsLogOpen) {
			return;
		}
		if (null == msg) {
			msg = "";
		}
		msg = MessageFormat.format("[{0}]({1}):{2}", cls.getSimpleName(), function, msg);
		Log.i(tag, msg);
		if (sIsOutToFile) {
			sLog.ins(tag, msg);
		}
	}

	public final static void w(Class<?> cls, String function, String msg) {
		w(TAG_DEFAULT, cls, function, msg);
	}

	public final static void w(String tag, Class<?> cls, String function, String msg) {
		if (!sIsLogOpen) {
			return;
		}
		if (null == msg) {
			msg = "";
		}
		msg = MessageFormat.format("[{0}]({1}):{2}", cls.getSimpleName(), function, msg);
		Log.w(tag, msg);
		if (sIsOutToFile) {
			sLog.wns(tag, msg);
		}
	}

	public final static void e(Class<?> cls, String function, String msg) {
		e(TAG_DEFAULT, cls, function, msg);
	}

	public final static void e(String tag, Class<?> cls, String function, String msg) {
		if (null == msg) {
			msg = "";
		}
		msg = MessageFormat.format("[{0}]({1}):{2}", cls.getSimpleName(), function, msg);
		Log.e(tag, msg);
		if (sIsOutToFile) {
			sLog.ens(tag, msg);
		}
	}

	public final static void e(Class<?> cls, String function, String msg, Throwable tr) {
		e(TAG_DEFAULT, cls, function, msg, tr);
	}

	public final static void e(String tag, Class<?> cls, String function, String msg, Throwable tr) {
		if (null == msg) {
			msg = "";
		}
		msg = MessageFormat.format("[{0}]({1}):{2}", cls.getSimpleName(), function, msg);
		Log.e(tag, msg, tr);
		if (sIsOutToFile) {
			sLog.ens(tag, msg, tr);
		}
	}

	/** <br>功能简述:启动任务计时
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param taskName
	 */
	public final static void startTimingLogger(String taskName) {
		if (!sIsLogOpen) {
			return;
		}
		sTimingLoggerCache.put(taskName, new TimingLogger(Constant.TAG_DEBUG, taskName));
	}

	public final static void addSplit(String taskName, String workName) {
		if (!sIsLogOpen) {
			return;
		}
		TimingLogger timingLogger = sTimingLoggerCache.remove(taskName);
		if (timingLogger != null) {
			timingLogger.addSplit(workName);
		}
	}

	/** <br>功能简述:停止任务计时，并输出日志
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param taskName
	 */
	public final static void stopTimingLogger(String taskName) {
		TimingLogger timingLogger = sTimingLoggerCache.remove(taskName);
		if (timingLogger != null) {
			timingLogger.dumpToLog();
		}
	}

	public final static boolean isLogOpen() {
		return sIsLogOpen;
	}
}
