package com.tcl.statistics.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class MetaUtils {
    public static final String SERVER_DOMAIN = "SERVER_DOMAIN";

    public static String getServerDomain(Context context){
        return getMetaData(context, SERVER_DOMAIN);
    }


    public static String getMetaData(Context context, String name) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo;
        Object value = null;
        try {

            applicationInfo = packageManager.getApplicationInfo(
                    context.getPackageName(), 128);
            if (applicationInfo != null && applicationInfo.metaData != null) {
                value = applicationInfo.metaData.get(name);
            }

        } catch (NameNotFoundException e) {
            e.printStackTrace();
            LogUtils.W("MetaUtils:Could not read the name"+name+"in the manifest file.");
            return null;
        }

        return value == null ? null : value.toString();
    }
}
