package com.graphics.engine.gl.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;

/**
 * 
 * <br>类描述: 日志类
 * <br>功能详细描述: 支持设置输出到文件的方式{@link #LOG_TYPE}，以及最低优先级{@link #LOG_LEVEL_MIN}来过滤日志。
 * 日志文件输出到sd卡目录下的{@link #mLOG_DIR_NAME}中，如果在该目录下创建文件{@link #NO_LOG_FILE_NAME}可以在当前
 * 日志文件超过限制{@link #LOG_COUNT_LIMIT_PER_FILE}后不再输出日志。
 * 
 * 默认的用法是使用各种静态方法，跟{@link android.util.Log}一致，使用者只需更改import的类为本类即可。
 * 还可以创建一个实例，通过构造方法{@link #Log(String)}指定写日志时的文件名前缀（默认为log），以区分其他模块的日志
 * （如果不想使用TAG来区分）。然后调用各种非静态的方法来输出日志，例如{@link #dns(String, String)}。
 * 
 * @author  dengweiming
 * @date  [2013-8-13]
 */
public class Log {

    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = 2;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = 3;

    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = 4;

    /**
     * Priority constant for the println method; use Log.w.
     */
    public static final int WARN = 5;

    /**
     * Priority constant for the println method; use Log.e.
     */
    public static final int ERROR = 6;

    /**
     * Priority constant for the println method.
     */
    public static final int ASSERT = 7;
    
    
    private static final String PRIORITYS_STRING[] = {
    	" ", 
    	"V", 
    	"D", 
    	"I", 
    	"W", 
    	"E", 
    	"A", 
    };
    
	/** 屏蔽日志输出 */
	private static final int DISABLED = 0;
	/** 输出到日志文件 */
	private static final int LOG_TO_FILE = 1;
	/** 按平常的方式，输出到日志缓冲区 */
	private static final int LOG_NORMAL = 2;
	
	//配置项
	private static final int LOG_TYPE = LOG_TO_FILE;			//日志输出类型
	private static final int LOG_LEVEL_MIN = VERBOSE;			//允许通过的最小日志等级
    private static final String LOG_DEFALT_DIR_NAME = "NextLauncher_Data/debug_log";	//日志输出目录，位于sd卡目录下，根据项目修改
    /** 日志输出目录(不包含sd卡的路径) */
    private String mLOG_DIR_NAME;	                                                    //日志输出目录，位于sd卡目录下，根据项目修改
	private static final String NO_LOG_FILE_NAME = "nolog";		//在日志输出目录下创建这个文件，在当前日志文件超过限制时禁止继续输出
	private static final int LOG_COUNT_LIMIT_PER_FILE = 20000;	//每个文件的日志输出限制
	
	
	private final Object mLOCK = new Object();
	private File mCurFile;
	private int mLogCount;
	private String mLogFilePrefix;
	private boolean mFirstLog = true;
	
	//CHECKSTYLE IGNORE 1 LINES
	private static final Log sInstance = new Log("log");
	
	/**
	 * @param logFilePrefix	日志文件名称前缀，以便区分
     * <br/>注意：该方法只供Next桌面调用
	 */
	public Log(String logFilePrefix) {
		this(LOG_DEFALT_DIR_NAME, logFilePrefix);
	}

    /**
     * @param logDir 日志输出目录，位于sd卡目录下，不同桌面需根据需要更改路径。(例如Next桌面的路径是:NextLauncher_Data/debug_log)
     * @param logFilePrefix	日志文件名称前缀，以便区分
     */
    public Log(String logDir, String logFilePrefix) {
        mLOG_DIR_NAME = logDir;
        mLogFilePrefix = logFilePrefix == null ? "" : logFilePrefix;
    }

	public int vns(String tag, String msg) {
		return println_native_non_static(LOG_ID_MAIN, VERBOSE, tag, msg);
	}
	
    public int dns(String tag, String msg) {
        return println_native_non_static(LOG_ID_MAIN, DEBUG, tag, msg);
    }
    
    public int dns(String tag, String msg, Throwable tr) {
    	return println_native_non_static(LOG_ID_MAIN, DEBUG, tag, msg + '\n' + getStackTraceString(tr));
    }
    
    public int ins(String tag, String msg) {
    	return println_native_non_static(LOG_ID_MAIN, INFO, tag, msg);
    }
    
    public int wns(String tag, String msg) {
    	return println_native_non_static(LOG_ID_MAIN, WARN, tag, msg);
    }
    
    public int ens(String tag, String msg) {
    	return println_native_non_static(LOG_ID_MAIN, ERROR, tag, msg);
    }
    
    public int ens(String tag, String msg, Throwable tr) {
    	return println_native_non_static(LOG_ID_MAIN, ERROR, tag, msg + '\n' + getStackTraceString(tr));
    }
	
    
    /**
     * Send a {@link #VERBOSE} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int v(String tag, String msg) {
        return println_native(LOG_ID_MAIN, VERBOSE, tag, msg);
    }

    /**
     * Send a {@link #VERBOSE} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int v(String tag, String msg, Throwable tr) {
        return println_native(LOG_ID_MAIN, VERBOSE, tag, msg + '\n' + getStackTraceString(tr));
    }

    /**
     * Send a {@link #DEBUG} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int d(String tag, String msg) {
        return println_native(LOG_ID_MAIN, DEBUG, tag, msg);
    }

    /**
     * Send a {@link #DEBUG} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int d(String tag, String msg, Throwable tr) {
        return println_native(LOG_ID_MAIN, DEBUG, tag, msg + '\n' + getStackTraceString(tr));
    }

    /**
     * Send an {@link #INFO} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int i(String tag, String msg) {
        return println_native(LOG_ID_MAIN, INFO, tag, msg);
    }

    /**
     * Send a {@link #INFO} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int i(String tag, String msg, Throwable tr) {
        return println_native(LOG_ID_MAIN, INFO, tag, msg + '\n' + getStackTraceString(tr));
    }

    /**
     * Send a {@link #WARN} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int w(String tag, String msg) {
        return println_native(LOG_ID_MAIN, WARN, tag, msg);
    }

    /**
     * Send a {@link #WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int w(String tag, String msg, Throwable tr) {
        return println_native(LOG_ID_MAIN, WARN, tag, msg + '\n' + getStackTraceString(tr));
    }

    /*
     * Send a {@link #WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param tr An exception to log
     */
    public static int w(String tag, Throwable tr) {
        return println_native(LOG_ID_MAIN, WARN, tag, getStackTraceString(tr));
    }

    /**
     * Send an {@link #ERROR} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int e(String tag, String msg) {
        return println_native(LOG_ID_MAIN, ERROR, tag, msg);
    }

    /**
     * Send a {@link #ERROR} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int e(String tag, String msg, Throwable tr) {
        return println_native(LOG_ID_MAIN, ERROR, tag, msg + '\n' + getStackTraceString(tr));
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     * @param tr An exception to log
     */
    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Low-level logging call.
     * @param priority The priority/type of this log message
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    public static int println(int priority, String tag, String msg) {
        return println_native(LOG_ID_MAIN, priority, tag, msg);
    }

    /** @hide */ public static final int LOG_ID_MAIN = 0;
    /** @hide */ public static final int LOG_ID_RADIO = 1;
    /** @hide */ public static final int LOG_ID_EVENTS = 2;
    /** @hide */ public static final int LOG_ID_SYSTEM = 3;

    /** @hide */ 
    //CHECKSTYLE IGNORE 1 LINES
    public static int println_native(int bufID, int priority, String tag, String msg) {
    	return sInstance.println_native_non_static(bufID, priority, tag, msg);
    }
    
	@SuppressLint("SimpleDateFormat")
	//CHECKSTYLE IGNORE 1 LINES
	private int println_native_non_static(int bufID, int priority, String tag, String msg) {
		switch (LOG_TYPE) {
			case DISABLED :
				return 0;
			case LOG_NORMAL:
				return android.util.Log.println(priority, tag, msg);
			default :
				break;
		}
		if (priority < VERBOSE || priority > ASSERT) {
			throw new IllegalArgumentException("wrong priority =" + priority);
		}
		if (priority < LOG_LEVEL_MIN) {
			return 0;
		}
		String sdStateString = android.os.Environment.getExternalStorageState();
		if (!sdStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			// 没有可读可写权限
			return 0;
		}
		
		if (mFirstLog) {
			mFirstLog = false;
			ins("Log", "====================\n======Log init======\n====================");
		}
		
		//格式化输出信息
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date(System.currentTimeMillis());
		String dateString = dateFormat.format(date);
		msg = dateString + ": " + PRIORITYS_STRING[priority] + "/" + tag + ": " + msg + "\n";
		
		synchronized (mLOCK) {
			if (mLogCount % LOG_COUNT_LIMIT_PER_FILE == 0) {
				//超过行数限制时创建新日志文件
				mLogCount = 0;

				String dirName = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
						+ File.separator + mLOG_DIR_NAME;
				File dir = new File(dirName);
				try {
					if (dir.exists() && !dir.isDirectory()) {
						dir.delete();
					}
					if (!dir.exists()) {
						dir.mkdirs();
					}
				} catch (SecurityException e) {
					dir = null;
					return 0;
				}
				
				if ((new File(dirName + File.separator + NO_LOG_FILE_NAME)).exists()) {
					mCurFile = null;
					return 0;
				}

				dateString = dateString.replace(':', '-').replace(' ', '_');
				String sCurFileName = dirName + File.separator + mLogFilePrefix + "_" + dateString + ".txt";

				mCurFile = new File(sCurFileName);
				if (!mCurFile.exists()) {
					try {
						mCurFile.createNewFile();
					} catch (IOException e) {
						mCurFile = null;
						e.printStackTrace();
						return 0;
					}
				}

			}
			++mLogCount;

			if (mCurFile == null) {
				return 0;
			}

			try {
				//写数据
				FileOutputStream outputStream = new FileOutputStream(mCurFile, true);
				outputStream.write(msg.getBytes());
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 1;
	}
}
