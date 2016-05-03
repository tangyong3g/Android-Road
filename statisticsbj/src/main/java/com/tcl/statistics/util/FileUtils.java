package com.tcl.statistics.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;


/**
 * 统计日志本地文件
 */
public class FileUtils {

    public static File getStatisticsFile(Context context, String name) {
        String absolutePath = getAbsolutePath(context);
        File dir = new File(absolutePath + File.separator + "statistics");
        if (!(dir.exists()))
            dir.mkdirs();
        return new File(dir, name);
    }

    public static File getLogFile(Context context, String name) {
        String absolutePath = getAbsolutePath(context);
        File dir = new File(absolutePath + File.separator + "statistics" + File.separator + "log");
        if (!(dir.exists()))
            dir.mkdirs();
        return new File(dir, name);
    }

    public static String getAbsolutePath(Context context) {
        if (Environment.getExternalStorageState().equals("mounted"))
            return Environment.getExternalStorageDirectory().getAbsolutePath();

        return context.getFilesDir().getAbsolutePath();
    }
}