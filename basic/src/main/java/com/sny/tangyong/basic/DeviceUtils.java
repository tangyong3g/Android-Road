package com.sny.tangyong.basic;

import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Created by lenvo on 2016/5/12.
 *
 *
 * 参考资料
 *
 * http://blog.csdn.net/hudashi/article/details/8091543
 */

public class DeviceUtils {

    private final static int kSystemRootStateUnknow = -1;
    private final static int kSystemRootStateDisable = 0;
    private final static int kSystemRootStateEnable = 1;
    private static int systemRootState = kSystemRootStateUnknow;
    private static final int REQUEST_CODE_ASK_READ_PHONE_PERMISSION = 111;

    public static boolean isRootSystem() {
        if (systemRootState == kSystemRootStateEnable) {
            return true;
        } else if (systemRootState == kSystemRootStateDisable) {

            return false;
        }
        File f = null;
        final String kSuSearchPaths[] = {"/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/"};
        try {
            for (int i = 0; i < kSuSearchPaths.length; i++) {
                f = new File(kSuSearchPaths[i] + "su");
                if (f != null && f.exists()) {
                    systemRootState = kSystemRootStateEnable;
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        systemRootState = kSystemRootStateDisable;
        return false;
    }


    /**
     * <br>
     * 功能简述:获取Android ID的方法
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
     * 获取SIM卡所在的国家
     *
     * @author tylertang@tcl.com ty_sany@163.com
     * @param context
     * @return 当前手机sim卡所在的国家，如果没有sim卡，取本地语言代表的国家
     */
    public static String getLocal(Context context) {

        String ret = null;
        Locale locale = null;

        if (locale == null) {
            locale = Locale.getDefault();
        }
        try {
            // --TODO 这里要处理一下权限问题
            TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telManager != null) {
                ret = telManager.getSimCountryIso();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (ret == null || ret.equals("")) {
            ret = locale.getCountry().toLowerCase();
        }
        return null == ret ? "error" : ret;
    }


    /**
     * 获取手机IMEA
     *
     * @param context
     * @return String IMEA
     *
     *         // --TODO 6.0的权限处理这里在哪个时间点比较好呢。因为如果不是这样那么Activity的对象会被传到统计SDK中。
     *         https://material.google.com/patterns/permissions.html#permissions-denied-permissions
     */
    public static String getIMEI(Context context) {

        String imea = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // 检查用户是否通过此权限
            String permission = android.Manifest.permission.READ_PHONE_STATE;

            if (PermissionUtil.hanPermission(context, permission)) {
                imea = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                return imea;
            } else {
                // ActivityCompat.requestPermissions(context, new
                // String[]{Manifest.permission.READ_PHONE_STATE},
                // REQUEST_CODE_ASK_READ_PHONE_PERMISSION);
            }
        } else {
            imea = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        }
        return imea;
    }

    /**
     * //--TODO 目前好像不好处理
     *
     * @return
     */
    public static boolean isRoot() {


        return false;
    }


    /**
     * Calculates the free memory of the device. This is based on an inspection of the filesystem,
     * which in android devices is stored in RAM.
     *
     * @return Number of bytes available.
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }


    /**
     * Calculates the total memory of the device. This is based on an inspection of the filesystem,
     * which in android devices is stored in RAM.
     *
     * @return Total number of bytes.
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    /**
     * @return
     */
    public static Integer getRAM() {
        return (int) (Runtime.getRuntime().maxMemory());
    }


    public static int getHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay().getHeight();
    }

    public static int getWidth(Context context) {

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay().getWidth();

    }

    /**
     * @return
     */
    public static String getIMSI(Context context) {

        try {
            TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            /**
             * 获取SIM卡的IMSI码 SIM卡唯一标识：IMSI 国际移动用户识别码（IMSI：International Mobile Subscriber
             * Identification Number）是区别移动用户的标志，
             * 储存在SIM卡中，可用于区别移动用户的有效信息。IMSI由MCC、MNC、MSIN组成，其中MCC为移动国家号码，由3位数字组成，
             * 唯一地识别移动客户所属的国家，我国为460；MNC为网络id，由2位数字组成，
             * 用于识别移动客户所归属的移动网络，中国移动为00，中国联通为01,中国电信为03；MSIN为移动客户识别码，采用等长11位数字构成。
             * 唯一地识别国内GSM移动通信网中移动客户。所以要区分是移动还是联通，只需取得SIM卡中的MNC字段即可
             */
            return telManager.getSubscriberId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 从本地文件获取MAC地址
     * 
     * @return
     * @throws IOException
     */
    public static String getMacAddress() throws IOException {
        String result = "";
        String Mac = "";
        result = callCmd("busybox ifconfig", "HWaddr");

        // 如果返回的result == null，则说明网络不可取
        if (result == null) {
            return "connect error!";
        }

        // 对该行数据进行解析
        // 例如：eth0 Link encap:Ethernet HWaddr 00:16:E8:3E:DF:67
        if (result.length() > 0 && result.contains("HWaddr") == true) {
            Mac = result.substring(result.indexOf("HWaddr") + 6, result.length() - 1);
            Log.i("test", "Mac:" + Mac + " Mac.length: " + Mac.length());

            if (Mac.length() > 1) {
                Mac = Mac.replaceAll(" ", "");
                // 不处理 :的问题。
                // result = "";
                // String[] tmp = Mac.split(":");
                // for (int i = 0; i < tmp.length; ++i) {
                // result += tmp[i];
                // }
                result = Mac;
            }
            Log.i("test", result + " result.length: " + result.length());
        }
        return result;
    }

    /**
     * 转化Mac地址
     * 
     * @param result
     * @return
     */
    public static String resolveMacAddress(String result) {

        String Mac = "";

        // 如果返回的result == null，则说明网络不可取
        if (result == null) {
            return "connect error!";
        }

        // 对该行数据进行解析
        // 例如：eth0 Link encap:Ethernet HWaddr 00:16:E8:3E:DF:67
        if (result.length() > 0 && result.contains("HWaddr") == true) {
            Mac = result.substring(result.indexOf("HWaddr") + 6, result.length() - 1);
            Log.i("test", "Mac:" + Mac + " Mac.length: " + Mac.length());

            if (Mac.length() > 1) {
                Mac = Mac.replaceAll(" ", "");
                result = "";
                String[] tmp = Mac.split(":");
                for (int i = 0; i < tmp.length; ++i) {
                    result += tmp[i];
                }
            }
            Log.i("test", result + " result.length: " + result.length());
        }
        return result;

    }


    public static String callCmd(String cmd, String filter) throws IOException {
        String result = "";
        String line = "";

        Process proc = Runtime.getRuntime().exec(cmd);
        InputStreamReader is = new InputStreamReader(proc.getInputStream());
        BufferedReader br = new BufferedReader(is);

        // 执行命令cmd，只取结果中含有filter的这一行
        while ((line = br.readLine()) != null && line.contains(filter) == false) {
            // result += line;
            Log.i("test", "line: " + line);
        }

        result = line;
        Log.i("test", "result: " + result);
        return result;
    }



    /**
     * 利用系统API去拿macAddress
     *
     * @param context
     * @return
     */
    public static String getMac(Context context) {

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();

        return info.getMacAddress();
    }


    private static final int ERROR = -1;

    /**
     * SDCARD是否存
     */
    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }


    /**
     * 获取手机内部总的存储空间
     *
     * @return
     */
    public static long getTotalInternalMemorySize_() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
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
            StatFs stat = new StatFs(path.getPath());
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
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return ERROR;
        }
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


    /**
     * 获取当前可用内存，返回数据以字节为单位。
     *
     * @param context 可传入应用程序上下文。
     * @return 当前可用内存单位为B。
     */
    public static long getTotalMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        return memoryInfo.totalMem;
    }



}
