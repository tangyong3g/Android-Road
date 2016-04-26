package com.tcl.mig.staticssdk.utiltool;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.text.DecimalFormat;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

// CHECKSTYLE:OFF
public class Machine {
	private static boolean sCheckTablet = false;
	private static boolean sIsTablet = false;

	private final static int KSYSTEMROOTSTATEUNKNOW = -1;
	private final static int KSYSTEMROOTSTATEDISABLE = 0;
	private final static int KSYSTEMROOTSTATEENABLE = 1;
	private static int SYSTEMROOTSTATE = KSYSTEMROOTSTATEUNKNOW;

	public static String getPhoneNumber(Context context) {
		String number = "";
		// try {
		// TelephonyManager manager = (TelephonyManager) context
		// .getSystemService(Context.TELEPHONY_SERVICE);
		//
		// // SIM卡状态
		// boolean simCardEnable = manager.getSimState() ==
		// TelephonyManager.SIM_STATE_READY;
		//
		// if (simCardEnable) {
		// number = manager.getLine1Number();
		// }
		//
		// } catch (Exception e) {
		// // TODO: handle exception
		// }
		return number;
	}

	/**
	 * 因为主题2.0新起进程，无法获取GoLauncher.getContext()， 所以重载此方法，以便主题2.0调用
	 * 
	 * @param context
	 * @return
	 */
	public static String getSimOperator(Context context) {
		String simOperator = "";
		try {
			// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
			TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			// SIM卡状态
			simOperator = manager.getSimOperator();

		} catch (Exception e) {
			// TODO: handle exception
		}
		return simOperator;
	}

	/**
	 * 因为主题2.0新起进程，无法获取GoLauncher.getContext()， 所以重载此方法，以便主题2.0调用
	 * 
	 * @param context
	 * @return
	 */
	public static String getSimCountryIso(Context context) {
		String simCountryIso = "";
		try {
			// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
			TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			// SIM卡状态
			simCountryIso = manager.getSimCountryIso();

		} catch (Exception e) {
			// TODO: handle exception
		}
		if (simCountryIso == null || simCountryIso.trim().equals("")) {
			return getCountry(context);
		} else if (simCountryIso.contains(",")) {
			String[] simCountryIsoArrary = simCountryIso.split(",");
			if (simCountryIsoArrary != null && simCountryIsoArrary.length > 1) {
				if (simCountryIsoArrary[0] != null && !simCountryIsoArrary[0].trim().equals("")) {
					return simCountryIsoArrary[0];
				} else if (simCountryIsoArrary[1] != null && !simCountryIsoArrary[1].trim().equals("")) {
					return simCountryIsoArrary[1];
				} else {
					return getCountry(context);
				}
			}
		}
		return simCountryIso;
	}

	// 判断当前设备是否为平板
	private static boolean isPad() {
		if (DrawUtils.sDensity >= 1.5 || DrawUtils.sDensity <= 0) {
			return false;
		}
		if (DrawUtils.sWidthPixels < DrawUtils.sHeightPixels) {
			if (DrawUtils.sWidthPixels > 480 && DrawUtils.sHeightPixels > 800) {
				return true;
			}
		} else {
			if (DrawUtils.sWidthPixels > 800 && DrawUtils.sHeightPixels > 480) {
				return true;
			}
		}
		return false;
	}

	public static boolean isTablet(Context context) {
		if (sCheckTablet == true) {
			return sIsTablet;
		}
		sCheckTablet = true;
		sIsTablet = isPad();
		return sIsTablet;
	}

	public static final int NETWORKTYPE_ALL = 0;
	public static final int NETWORKTYPE_INVALID = -1;
	public static final int NETWORKTYPE_WIFI = 4;
	public static final int NETWORKTYPE_2G = 1;
	public static final int NETWORKTYPE_3G = 2;
	public static final int NETWORKTYPE_4G = 3;

	// public static final int NETWORKTYPE_WAP = 4;

	/**
	 * 获取当前网络类型
	 * 
	 * @author huyong
	 * 
	 * @param context
	 * @return -1无网络连接；0 所有网络状态;1wifi;2 2G;3 3G;4 4G;
	 */
	public static int getNetworkType(Context context) {
		int result = NETWORKTYPE_INVALID;
		try {
			if (context != null) {
				ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				if (cm != null) {
					NetworkInfo networkInfo = cm.getActiveNetworkInfo();
					if (networkInfo != null && networkInfo.isConnected()) {
						String type = networkInfo.getTypeName();
						if (type.equalsIgnoreCase("WIFI")) {
							result = NETWORKTYPE_WIFI;
						} else if (type.equalsIgnoreCase("MOBILE")) {
							// String proxyHost =
							// android.net.Proxy.getDefaultHost();
							result = isFastMobileNetwork(context) ? NETWORKTYPE_3G : NETWORKTYPE_2G;
						}
					}
				}
			}
		} catch (Exception e) {
			result = NETWORKTYPE_INVALID;
		}
		return result;
	}

	/**
	 * 获取当前网络类型
	 * 
	 * @author luozhiping
	 * @param context
	 * @return
	 */
	private static boolean isFastMobileNetwork(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		switch (telephonyManager.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return false; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return false; // ~ 14-64 kbps
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return false; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return true; // ~ 400-1000 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return true; // ~ 600-1400 kbps
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return false; // ~ 100 kbps
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return true; // ~ 2-14 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return true; // ~ 700-1700 kbps
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return true; // ~ 1-23 Mbps
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return true; // ~ 400-7000 kbps
		case TelephonyManager.NETWORK_TYPE_EHRPD:
			return true; // ~ 1-2 Mbps
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
			return true; // ~ 5 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			return true; // ~ 10-20 Mbps
		case TelephonyManager.NETWORK_TYPE_IDEN:
			return false; // ~25 kbps
		case TelephonyManager.NETWORK_TYPE_LTE:
			return true; // ~ 10+ Mbps
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return false;
		default:
			return false;
		}
	}

	/**
	 * 获得手机内存的可用空间大小
	 * 
	 * @author kingyang
	 */
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * 获得手机内存的总空间大小
	 * 
	 * @author kingyang
	 */
	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 获得手机sdcard的可用空间大小
	 * 
	 * @author kingyang
	 */
	public static long getAvailableExternalMemorySize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * 获得手机sdcard的总空间大小
	 * 
	 * @author kingyang
	 */
	public static long getTotalExternalMemorySize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 是否存在SDCard
	 * 
	 * @author chenguanyu
	 * @return
	 */
	public static boolean isSDCardExist() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取当前的语言
	 * 
	 * @author zhoujun
	 * @param context
	 * @return
	 */
	public static String getLanguage(Context context) {
		String language = context.getResources().getConfiguration().locale.getLanguage();
		return language;
	}

	/**
	 * 获取当前的语言
	 * 
	 * @author zhoujun
	 * @param context
	 * @return
	 */
	public static String getCountry(Context context) {
//		String language = context.getResources().getConfiguration().locale.getCountry();
		String language = Locale.getDefault().getCountry().toLowerCase();
		return language;
	}

	/**
	 * 判断应用软件是否运行在前台
	 * 
	 * @param context
	 * @param packageName
	 *            应用软件的包名
	 * @return
	 */
	public static boolean isTopActivity(Context context, String packageName) {
		try {
			ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
			if (tasksInfo.size() > 0) {
				// 应用程序位于堆栈的顶层
				if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * <br>
	 * 功能简述: 获取真实的imei号。 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param context
	 * @return
	 */
	public static String getIMEI(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();
		return imei;
	}

	/**
	 * <br>
	 * 功能简述:获取Android ID的方法 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	public static String getAndroidId(Context context) {
		String androidId = null;
		if (context != null) {
			androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		}
		return androidId;
	}

	/**
	 * <br>
	 * 功能简述: <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	public static int isRootSystem() {
		if (SYSTEMROOTSTATE == KSYSTEMROOTSTATEENABLE) {
			return 1;
		} else if (SYSTEMROOTSTATE == KSYSTEMROOTSTATEDISABLE) {

			return 0;
		}
		File f = null;
		final String kSuSearchPaths[] = { "/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/" };
		try {
			for (int i = 0; i < kSuSearchPaths.length; i++) {
				f = new File(kSuSearchPaths[i] + "su");
				if (f != null && f.exists()) {
					SYSTEMROOTSTATE = KSYSTEMROOTSTATEENABLE;
					return 1;
				}
			}
		} catch (Exception e) {
		}
		SYSTEMROOTSTATE = KSYSTEMROOTSTATEDISABLE;
		return 0;
	}

	public static boolean isCNUser(Context context) {
		boolean result = false;

		if (context != null) {
			// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
			TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			// SIM卡状态
			boolean simCardUnable = manager.getSimState() != TelephonyManager.SIM_STATE_READY;
			String simOperator = manager.getSimOperator();

			if (simCardUnable || TextUtils.isEmpty(simOperator)) {
				// 如果没有SIM卡的话simOperator为null，然后获取本地信息进行判断处理
				// 获取当前国家或地区，如果当前手机设置为简体中文-中国，则使用此方法返回CN
				String curCountry = Locale.getDefault().getCountry();
				if (curCountry != null && curCountry.contains("CN")) {
					// 如果获取的国家信息是CN，则返回TRUE
					result = true;
				} else {
					// 如果获取不到国家信息，或者国家信息不是CN
					result = false;
				}
			} else if (simOperator.startsWith("460")) {
				// 如果有SIM卡，并且获取到simOperator信息。
				/**
				 * 中国大陆的前5位是(46000) 中国移动：46000、46002 中国联通：46001 中国电信：46003
				 */
				result = true;
			}
		}
		return result;
	}

	/**
	 * sd卡大小
	 * 
	 * @return
	 */
	public static long[] getSDCardMemory() {
		long[] sdCardInfo = new long[2];
		try {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				File sdcardDir = Environment.getExternalStorageDirectory();
				StatFs sf = new StatFs(sdcardDir.getPath());
				long bSize = sf.getBlockSize();
				long bCount = sf.getBlockCount();
				long availBlocks = sf.getAvailableBlocks();

				sdCardInfo[0] = bSize * bCount;// 总大小
				sdCardInfo[1] = bSize * availBlocks;// 可用大小
			}
		} catch (Exception e) {
			sdCardInfo[0] = 0;// 总大小
			sdCardInfo[1] = 0;// 可用大小
		}
		
		return sdCardInfo;
	}

	public static String getROMStorage() {
		long blockSize = 0;
		long totalBlocks = 0;
		long availableBlocks = 0;
		long usedBlocks = 0;
		try {
			String path = Environment.getDataDirectory().getPath();
			StatFs statFs = new StatFs(path);
			blockSize = statFs.getBlockSize();
			totalBlocks = statFs.getBlockCount();
			availableBlocks = statFs.getAvailableBlocks();
			usedBlocks = totalBlocks - availableBlocks;
		} catch (Exception e) {
		}
		// 处理存储容量格式
		String[] total = fileSize(totalBlocks * blockSize);
		String[] available = fileSize(availableBlocks * blockSize);
		String[] used = fileSize(usedBlocks * blockSize);

		// int ss=Integer.parseInt(used[0]);
		// int mm=Integer.parseInt(total[0]);
		// int tt=ss*100/mm;

		return total[0] + total[1] + "," + available[0] + available[1];
	}

	public static String[] fileSize(long size) {
		String str = "";
		if (size >= 1024) {
			str = "KB";
			size /= 1024;
			if (size >= 1024) {
				str = "MB";
				size /= 1024;
				if (size >= 1024) {
					str = "GB";
					size /= 1024;
				}
			}

		}
		DecimalFormat formatter = new DecimalFormat();
		formatter.setGroupingSize(3);
		String[] result = new String[2];
		result[0] = formatter.format(size);
		result[1] = str;

		return result;
	}
}
