package com.tcl.statistics.util;

import android.content.Context;
import android.content.pm.PackageManager;


public class PermissionUtil {

    public static boolean hanPermission(Context context, String permission) {
        boolean hasPermission = context.checkCallingOrSelfPermission(permission) != PackageManager. PERMISSION_DENIED;
        LogUtils.D("permission " + permission + ":" + hasPermission);
        return hasPermission;
    }
}