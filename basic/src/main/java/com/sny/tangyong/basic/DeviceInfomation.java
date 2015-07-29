package com.sny.tangyong.basic;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * 
		*@Title:
		*@Description:
		*@Author:tangyong
		*@Since:2014-12-25
		*@Version:1.1.0
 */
public class DeviceInfomation extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO
		super.onCreate(savedInstanceState);

		long size = getAvailableInternalMemorySize();
		Log.i("data", "获取手机内部剩余存储空间:\t" + formatFileSize(size, false));

		size = getTotalInternalMemorySize();
		Log.i("data", "获取手机内部总的存储空间:\t" + formatFileSize(size, false));

		size = getTotalExternalMemorySize();
		Log.i("data", "获取SDCARD总的存储空间:\t" + formatFileSize(size, false));

		size = getAvailableExternalMemorySize();
		Log.i("data", "获取SDCARD剩余存储空间:\t" + formatFileSize(size, false));
		
		
		size = getTotalExternalStoSizeV();
		Log.i("data", "获取SDCARD总的存储空间 Tyler:\t" + formatFileSize(size, false));
		
		size = getNextExternalStoSizeV();
		Log.i("data", "获取SDCARD剩余存储空间 tyler :\t" + formatFileSize(size, false));

	}

	/**
	  * @return
	  * @Description:
	 */
	public boolean isAvaForSD() {

		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

	}

	private static final int ERROR = -1;

	/**
	 * SDCARD是否存
	 */
	public static boolean externalMemoryAvailable() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取手机内部剩余存储空间
	 * 
	 * @return
	 */
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();

		String pathStr = path.getPath();
		StatFs stat = new StatFs(pathStr);
		Log.i("path", "获取手机内部剩余存储空间:\t" + pathStr);

		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * 获取手机内部总的存储空间
	 * 
	 * @return
	 */
	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();

		String pathStr = path.getPath();
		StatFs stat = new StatFs(pathStr);
		Log.i("path", "获取手机内部总的存储空间:\t" + pathStr);

		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 获取SDCARD剩余存储空间
	 * 
	 * @return
	 */
	public static long getAvailableExternalMemorySize() {
		if (externalMemoryAvailable()) {

			File path = Environment.getExternalStorageDirectory();

			String pathStr = path.getPath();
			StatFs stat = new StatFs(pathStr);
			Log.i("path", "获取SDCARD剩余存储空间:\t" + pathStr);

			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;

		} else {
			return ERROR;
		}
	}

	/**
	 * 获取SDCARD总的存储空间
	 * 
	 * @return
	 */
	public static long getTotalExternalMemorySize() {

		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();

			String pathStr = path.getPath();
			StatFs stat = new StatFs(pathStr);
			Log.i("path", "获取SDCARD总的存储空间:\t" + pathStr);

			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			return totalBlocks * blockSize;
		} else {
			return ERROR;
		}
	}

	/**
	 * 获取SDCARD总的存储空间 VErsion II 
	 * 
	 * @return
	 */
	public static long getTotalExternalStoSizeV() {

		if (externalMemoryAvailable()) {
			
			File path = getDirectory("EXTERNAL_STORAGE", "/storage/ext_sd");
			String pathStr = path.getPath();
			StatFs stat = new StatFs(pathStr);

			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			
			return totalBlocks * blockSize;
			
		} else {
			
			return ERROR;
		}
	}
	
	/**
	 * 获取SDCARD  NExt 的存储空间 VErsion II 
	 * 
	 * @return
	 */
	public static long getNextExternalStoSizeV() {

		if (externalMemoryAvailable()) {
			
			File path = getDirectory("EXTERNAL_STORAGE", "/storage/ext_sd");
			String pathStr = path.getPath();
			StatFs stat = new StatFs(pathStr);

			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getAvailableBlocks();
			
			return totalBlocks * blockSize;
		} else {
			return ERROR;
		}
	}
	
	

	/**
	 * 获取系统总内存
	 * 
	 * @param context 可传入应用程序上下文。
	 * @return 总内存大单位为B。
	 */
	public static long getTotalMemorySize(Context context) {
		String dir = "/proc/meminfo";
		try {
			FileReader fr = new FileReader(dir);
			BufferedReader br = new BufferedReader(fr, 2048);
			String memoryLine = br.readLine();
			String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
			br.close();
			return Integer.parseInt(subMemoryLine.replaceAll("\\D+", "")) * 1024l;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 获取当前可用内存，返回数据以字节为单位。
	 * 
	 * @param context 可传入应用程序上下文。
	 * @return 当前可用内存单位为B。
	 */
	public static long getAvailableMemory(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(memoryInfo);
		return memoryInfo.availMem;
	}

	private static DecimalFormat fileIntegerFormat = new DecimalFormat("#0");
	private static DecimalFormat fileDecimalFormat = new DecimalFormat("#0.#");

	/**
	 * 单位换算
	 * 
	 * @param size 单位为B
	 * @param isInteger 是否返回取整的单位
	 * @return 转换后的单位
	 */
	public static String formatFileSize(long size, boolean isInteger) {
		DecimalFormat df = isInteger ? fileIntegerFormat : fileDecimalFormat;
		String fileSizeString = "0M";
		if (size < 1024 && size > 0) {
			fileSizeString = df.format((double) size) + "B";
		} else if (size < 1024 * 1024) {
			fileSizeString = df.format((double) size / 1024) + "K";
		} else if (size < 1024 * 1024 * 1024) {
			fileSizeString = df.format((double) size / (1024 * 1024)) + "M";
		} else {
			fileSizeString = df.format((double) size / (1024 * 1024 * 1024)) + "G";
		}
		return fileSizeString;
	}

	static File getDirectory(String variableName, String defaultPath) {
		
		String path = System.getenv(variableName);
		return path == null ? new File(defaultPath) : new File(path);
	}
}
