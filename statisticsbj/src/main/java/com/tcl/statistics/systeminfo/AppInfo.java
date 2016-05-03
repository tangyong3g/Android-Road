package com.tcl.statistics.systeminfo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import com.tcl.statistics.agent.StatisticsConfig;
import com.tcl.statistics.util.LogUtils;
import com.tcl.statistics.util.MD5;
import com.tcl.statistics.util.NetUtils;
import com.tcl.statistics.util.PermissionUtil;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;


/**
 * 封装一些的基础统计信息
 */
public class AppInfo {

    private static final String TCL_STATISTICS_APP_KEY = "TCL_STATISTICS_APP_KEY";
    private static final String TCL_CHANNEL = "TCL_CHANNEL";
    private static String mOsVersion = null;
    private static String mUUID = null;
    private static String mUUID2 = null;
    private static int mScreenWidth = 0;
    private static int mScreenHeight = 0;
    private static String mUseragt = null;
    private static String mBundleId = null;
    public Display display = null;
    public int SizeType;
    private static final String SDKVERSION = "1.0.0";
    private static AppInfo sInstance = null;
    private static Context mContext;
    public static String mAppId = null;
    public static String mWidth = null;
    public static String mHeight = null;
    public static String mAdMode = null;
    public static String mLaunchActivityName = null;
    private String mMacAddress;
    public static HashMap<String, String> mConfigmap = new HashMap();
    private String mNetworkOperator;
    private static int mVersionCode;
    private String mVersionName;
    private String mSDKVersion;

    public static AppInfo getInstance() {
        synchronized (AppInfo.class) {
            if (sInstance == null)
                sInstance = new AppInfo();
        }

        return sInstance;
    }

    private void init(Context context) {
        mContext = context.getApplicationContext();

        mOsVersion = android.os.Build.VERSION.RELEASE;
        this.mSDKVersion = android.os.Build.VERSION.SDK;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.display = windowManager.getDefaultDisplay();
        mScreenWidth = this.display.getWidth();
        mScreenHeight = this.display.getHeight();
        mUseragt = android.os.Build.MODEL;
        mUseragt = mUseragt.replace(" ", "");
        if ((!(TextUtils.isEmpty(mUseragt))) && (mUseragt.length() > 30)) {
            mUseragt = mUseragt.substring(0, 30);
        }

        String serialno = getSerialNumber();

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiInfo info = wifi.getConnectionInfo();
        this.mMacAddress = info.getMacAddress();

        if (TextUtils.isEmpty(this.mMacAddress))
            this.mMacAddress = "000000000000";
        else {
            this.mMacAddress = this.mMacAddress.replace(":", "");
        }

        mUUID = serialno + "_" + getIMEI() + "_" + this.mMacAddress;

        mUUID = MD5.getMD5(mUUID);

        mUUID2 = getIMEI();

        if ((mUUID2 == null) || (mUUID2.equals(""))) {
            mUUID2 = "000000000000000";
        }

        getPackageInfo(context);
        getMCC(context);
        getVersionInfo(context);
    }

    private void getMCC(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.mNetworkOperator = telephonyManager.getNetworkOperator();
    }

    public static String getSerialNumber() {
        String serial = null;
        try {
            Class c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", new Class[]{String.class});
            serial = (String) get.invoke(c, new Object[]{"ro.serialno"});
            LogUtils.D("serialNumber:" + serial);
        } catch (Exception c) {
        }
        return serial;
    }

    public String getIMEI() {
        return ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }


    public static String getCurrentVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            mVersionCode = pi.versionCode;
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return "";
    }

    private void getVersionInfo(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            mVersionCode = pi.versionCode;
            this.mVersionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException pi) {
        }
    }

    public static String getPackageName(Context context) {
        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return info.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void getPackageInfo(Context context) {
        PackageInfo info;

        try {

            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            mBundleId = info.packageName;
            mLaunchActivityName = context.getPackageManager().getLaunchIntentForPackage(mBundleId).getComponent().getClassName();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getMetaData(Context context, String key) {
        if (context == null)
            return "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(info.packageName, 128);
            return appInfo.metaData.getString(key);
        } catch (Exception e) {
        }
        return "";
    }

    public String getChannelId(Context context) {
        return getMetaData(context, "TCL_CHANNEL");
    }

    public String getAppkey(Context context) {
        return getMetaData(context, "TCL_STATISTICS_APP_KEY");
    }

    public void setAppinfo(Context context, JSONObject appinfo) {
        init(context);
        if ((!(TextUtils.isEmpty(this.mVersionName))) && (this.mVersionName.length() > 15))
            throw new IllegalArgumentException(
                    "VersionName is too long , limit length is 15,Please modify your VersionName!!!");
        try {
            appinfo.put("tg", Build.version);
            appinfo.put("dd", getIMEI());
            appinfo.put("mc", this.mMacAddress);
            appinfo.put("bm", "");
            appinfo.put("sv", mOsVersion);
            appinfo.put("ss", System.currentTimeMillis());
            appinfo.put("d", "");
            appinfo.put("op", this.mNetworkOperator);
            appinfo.put("wl", "");
            appinfo.put("c", getChannelId(mContext));
            appinfo.put("sq", "0");
            appinfo.put("ii", "");
            appinfo.put("o", "Android");
            appinfo.put("l", NetUtils.getConnectType(context));
            appinfo.put("m", mUseragt);
            appinfo.put("k", getAppkey(mContext));
            appinfo.put("h", mScreenHeight);
            appinfo.put("i", "");
            appinfo.put("w", mScreenWidth);
            appinfo.put("v", "1.0.0");
            appinfo.put("gl", "");
            appinfo.put("t", System.currentTimeMillis());
            appinfo.put("s", this.mSDKVersion);
            appinfo.put("cl", "0_0_0");
            appinfo.put("pt", "0");
            appinfo.put("pn", mBundleId);
            appinfo.put("n", this.mVersionName);
            appinfo.put(
                    "a",
                    (StatisticsConfig.getAPPVersion(mContext) == 0) ? mVersionCode :
                            StatisticsConfig.getAPPVersion(mContext));
            StatisticsConfig.saveAPPVersion(mContext, mVersionCode);
        } catch (Exception localException) {
        }
    }
}