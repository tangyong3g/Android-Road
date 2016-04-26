package com.tcl.mig.staticssdk.utiltool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.content.Context;

/**
 * CPU信息读取
 * 
 * @author jiangxuwen http://blog.sina.com.cn/s/blog_74c22b210100ypfd.html
 */
public class CpuManager {
	public final static String CPU_FILE_DIR = "/sys/devices/system/cpu/";

	public final static String CAT_DIR = "/system/bin/cat";

	private static int sCpuCount = -1;

	private static final int LENGTH = 24;

	// 获取CPU最大频率（单位KHZ）

	// "/system/bin/cat" 命令行

	// "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" 存储最大频率的文件的路径

	public static String getMaxCpuFreq() {
		String result = "";
		ProcessBuilder cmd;
		InputStream in = null;
		try {
			String[] args = { CAT_DIR, CPU_FILE_DIR + "cpu0/cpufreq/cpuinfo_max_freq" };
			cmd = new ProcessBuilder(args);
			Process process = cmd.start();
			in = process.getInputStream();
			byte[] re = new byte[LENGTH];
			while (in.read(re) != -1) {
				result = result + new String(re);
			}
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			result = "N/A";
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		if (result.trim().equals("")) {
			return "0";
		}
		return result;
	}

	// 获取CPU最小频率（单位KHZ）
	public static String getMinCpuFreq() {
		String result = "";
		ProcessBuilder cmd;
		InputStream in = null;
		try {
			String[] args = { CAT_DIR, CPU_FILE_DIR + "cpu0/cpufreq/cpuinfo_min_freq" };
			cmd = new ProcessBuilder(args);
			Process process = cmd.start();
			in = process.getInputStream();
			byte[] re = new byte[LENGTH];
			while (in.read(re) != -1) {
				result = result + new String(re);
			}
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			result = "N/A";
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		return result.trim();
	}

	// 实时获取CPU当前频率（单位KHZ）
	public static String getCurCpuFreq() {
		String result = "N/A";
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(CPU_FILE_DIR + "cpu0/cpufreq/scaling_cur_freq");
			br = new BufferedReader(fr);
			String text = br.readLine();
			if (text != null) {
				result = text.trim();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fr != null) {
					fr.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
			}
		}
		return result;
	}

	// 获取CPU名字
	public static String getCpuName() {
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader("/proc/cpuinfo");
			br = new BufferedReader(fr);
			String text = br.readLine();
			String[] array = text.split(":\\s+", 2);
			for (int i = 0; i < array.length; i++) {
			}
			return array[1];
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fr != null) {
					fr.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
			}
		}
		return "unknown";
	}

	/**
	 * Calculates the free memory of the device. This is based on an inspection
	 * of the filesystem, which in android devices is stored in RAM.
	 * 
	 * @return Number of bytes available.
	 */
	public static long getAvailableInternalMemorySize(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(mi);
		return mi.availMem;
	}

	/**
	 * Calculates the total memory of the device. This is based on an inspection
	 * of the filesystem, which in android devices is stored in RAM.
	 * 
	 * @return Total number of bytes.
	 */
	public static long getTotalInternalMemorySize() {
		String str1 = "/proc/meminfo";
		String str2 = "0";
		FileReader fr = null;
		BufferedReader localBufferedReader = null;
		try {
			fr = new FileReader(str1);
			localBufferedReader = new BufferedReader(fr, 8192);
			while ((str2 = localBufferedReader.readLine()) != null) {
				// Log.i(TAG, "---" + str2);
				Pattern p = Pattern.compile("(\\d+)");
				Matcher m = p.matcher(str2);
				if (m.find()) {
					str2 = m.group(1);
				}
				return Long.valueOf(str2) * 1000;
			}
			localBufferedReader.close();
		} catch (IOException e) {

		} finally {
			try {
				if (fr != null) {
					fr.close();
				}
				if (localBufferedReader != null) {
					localBufferedReader.close();
				}
			} catch (IOException e) {
			}
		}
		return 0;
	}

	/**
	 * 获取cpu核心数
	 * 
	 * @return
	 */
	public static int getNumCores() {
		// Private Class to display only CPU devices in the directory listing
		/**
		 * 
		 * @author luozhiping
		 * 
		 */
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}

		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			// Return the number of cores (virtual CPU devices)
			return files.length;
		} catch (Exception e) {
			// Print exception
			e.printStackTrace();
			// Default to return 1 core
			return 1;
		}
	}

}
