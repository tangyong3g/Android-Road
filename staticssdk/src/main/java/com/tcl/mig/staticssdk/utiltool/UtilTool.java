package com.tcl.mig.staticssdk.utiltool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.tcl.mig.staticssdk.StatisticsManager;
import com.tcl.mig.staticssdk.database.DataBaseHelper;

//CHECKSTYLE:OFF
public class UtilTool {

	private static boolean sEnableLog = false;
	public static final int NETTYPE_MOBILE = 0; // 中国移动
	public static final int NETTYPE_UNICOM = 1; // 中国联通
	public static final int NETTYPE_TELECOM = 2; // 中国电信
	private static final String GO_STATIC_ACTION = "com.jiubang.gau.ACTION_GOSTATICSDK";
	private static final String GO_STATIC_ID = "go_static_id";
	private static final String GOID_FILE = "goid";

	public static void log(String tag, String msg) {
		Log.d(tag == null ? StatisticsManager.TAG : tag, msg);
	}

	public static void logStatic(String msg) {
		log(StatisticsManager.TAG, msg);
	}

	public static void printException(Exception e) {
		if (sEnableLog && e != null) {
			Log.d(StatisticsManager.TAG, Log.getStackTraceString(e));
		}
	}

	public static int boolean2Int(boolean value) {
		if (value) {
			return 1;
		}
		return 0;
	}

	public static String gzip(byte[] bs) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		GZIPOutputStream gzout = null;
		try {
			gzout = new GZIPOutputStream(bout);
			gzout.write(bs);
			gzout.flush();
		} catch (Exception e) {
			throw e;

		} finally {
			if (gzout != null) {
				try {
					gzout.close();
				} catch (Exception ex) {
				}
			}
		}
		String result = null;
		if (bout != null) {
			result = bout.toString("ISO-8859-1");
		}
		return result;
	}

	public static void enableLog(boolean enable) {
		sEnableLog = enable;
	}
	
	public static boolean isEnableLog() {
		return sEnableLog;
	}

	/**
	 * 是否cmwap连接
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static boolean isCWWAPConnect(Context context) {
		boolean result = false;
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
			if (Proxy.getDefaultHost() != null
					|| Proxy.getHost(context) != null) {
				result = true;
			}
		}

		return result;
	}

	/**
	 * 获取网络类型
	 * 
	 * @author huyong
	 * @param context
	 * @return 1 for 移动，2 for 联通，3 for 电信，-1 for 不能识别
	 */
	public static int getNetWorkType(Context context) {
		int netType = -1;
		// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
		TelephonyManager manager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String simOperator = manager.getSimOperator();
		if (simOperator != null) {
			if (simOperator.startsWith("46000")
					|| simOperator.startsWith("46002")) {
				// 因为移动网络编号46000下的IMSI已经用完，
				// 所以虚拟了一个46002编号，134/159号段使用了此编号
				// 中国移动
				netType = NETTYPE_MOBILE;
			} else if (simOperator.startsWith("46001")) {
				// 中国联通
				netType = NETTYPE_UNICOM;
			} else if (simOperator.startsWith("46003")) {
				// 中国电信
				netType = NETTYPE_TELECOM;
			}
		}
		return netType;
	}

	/**
	 * 获取网关
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static String getProxyHost(Context context) {
		return Proxy.getHost(context);
	}

	public static int getProxyPort(Context context) {
		return Proxy.getPort(context);
	}

public static String getBeiJinTime() {
		return getBeiJinTime(System.currentTimeMillis());
	}

	public static String getBeiJinTime(long milliseconds) {
		String stamp = null;
		try {
			Date now = new Date(milliseconds);
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss:SSS", Locale.US);
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			stamp = dateFormat.format(now);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return stamp;
	}

	/**
	 * <br>
	 * 功能简述:获取go产品统一标识 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	public static String getGOId(Context context) {
		String goId = null;
		boolean fromeSdcard = false;
		do {
			try {
				SharedPreferences preferences = context.getSharedPreferences(
						GO_STATIC_ID + context.getPackageName(),
						Context.MODE_WORLD_READABLE);
				goId = preferences.getString(GO_STATIC_ID, null);
				if (goId == null) {
					goId = readGoidFromSdcard();
					if (goId != null) {
						fromeSdcard = true;
						break;
					}
				}
				if (goId == null) {
					PackageManager pm = context.getPackageManager();
					Intent intent = new Intent(GO_STATIC_ACTION);
					List<ResolveInfo> infos = pm.queryIntentActivities(intent,
							0);
					if (infos != null && !infos.isEmpty()) {
						for (ResolveInfo info : infos) {
							String pkg = info.activityInfo.packageName;
							if (pkg != null
									&& !pkg.equals(context.getPackageName())) {
								Context otherContext = context
										.createPackageContext(pkg,
												Context.CONTEXT_IGNORE_SECURITY);
								if (otherContext != null) {
									preferences = otherContext
											.getSharedPreferences(GO_STATIC_ID
													+ pkg,
													Context.MODE_WORLD_READABLE);
									goId = preferences.getString(GO_STATIC_ID,
											null);
									if (goId != null) {
										break;
									}
								}
							}
						}
					}
				} else {
					return goId;
				}

			} catch (Exception e) {
				// TODO: handle exception
			}
		} while (false);
		if (goId == null) {
			goId = System.currentTimeMillis() + Machine.getAndroidId(context);
		}
		SharedPreferences preferences = context.getSharedPreferences(
				GO_STATIC_ID + context.getPackageName(),
				Context.MODE_WORLD_READABLE);
		preferences.edit().putString(GO_STATIC_ID, goId).commit();
		if (!fromeSdcard) {
			saveGoidToSdcard(goId);
		}
		return goId;
	}

	private static String readGoidFromSdcard() {
		String goid = null;
		File file = new File(CACHEDIR + GOID_FILE);
		if (file.exists()) {
			byte[] buffer = new byte[1024];
			FileInputStream is = null;
			try {
				is = new FileInputStream(file);
				int len = is.read(buffer);
				if (len > 0) {
					byte[] data = new byte[len];
					for (int i = 0; i < len; i++) {
						data[i] = buffer[i];
					}
					// 生成字符串
					String dataStr = new String(data, "utf-8");
					dataStr.trim();
					if (data != null) {
						// 去掉回车键
						String replace = "\r\n";
						if (dataStr.contains(replace)) {
							dataStr = dataStr.replaceAll(replace, "");
						}
						replace = "\n";
						if (dataStr.contains(replace)) {
							dataStr = dataStr.replaceAll(replace, "");
						}
					}
					goid = dataStr;
				}
			} catch (IOException e) {
				e.printStackTrace();
				// IO异常
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return goid;
	}
	public static String CACHEDIR = Environment.getExternalStorageDirectory().getPath()
			+ "/.goproduct/";
	
	private static void saveGoidToSdcard(String goid) {
		File file = new File(CACHEDIR + GOID_FILE);
		createNewFile(file.getAbsolutePath(), false);
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(file);
			os.write(goid.getBytes("utf-8"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}


	public static File createNewFile(String path, boolean append) {
		File newFile = new File(path);
		if (!append) {
			if (newFile.exists()) {
				newFile.delete();
			}
		}
		if (!newFile.exists()) {
			try {
				File parent = newFile.getParentFile();
				createDir(parent.getAbsolutePath());
				newFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return newFile;
	}

	public static void createDir(String path) {
		File dir = new File(path);
		if (dir.exists()) {
			return;
		} else {
			dir.mkdirs();
		}
	}

	/**
	 * 
	 * @param string
	 * @return string为null或者为""或者为"null"则返回true;否则返回false;
	 */
	public static boolean isStringNoValue(String string) {
		if (string == null || string.trim().equals("")
				|| string.trim().equals("null")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 检查是否为新用户，如果是新用户，则存入新用户第一次运行的时间。
	 * @param context
	 * @param sharedPreferences
	 */
	public static synchronized void checkIsNewUser(Context context, SharedPreferences sharedPreferences) {
		SQLiteDatabase db = null;
		String dbPath = Environment.getDataDirectory() + "/data/" + context.getPackageName()
				+ "/databases/";
		String dbFilePath = dbPath + DataBaseHelper.DB_NAME;
		try {
			db = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READONLY);
			sharedPreferences.edit().putLong(StatisticsManager.USER_FIRST_RUN_TIME, System.currentTimeMillis() - StatisticsManager.NEW_USER_VALID_TIME).commit();
		} catch (Exception e) {
			sharedPreferences.edit().putLong(StatisticsManager.USER_FIRST_RUN_TIME, System.currentTimeMillis()).commit();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}
}
