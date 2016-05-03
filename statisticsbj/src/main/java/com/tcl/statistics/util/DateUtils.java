package com.tcl.statistics.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期转化工具类
 */
public class DateUtils {

    /**
     * 得到当前日期 yyyy-MM-dd 的格式
     *
     * @return
     */
    public static String getCurrentDay() {
        String str = null;
        str = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return str;
    }


    public static String getLastDay() {
        String str = null;
        str = new SimpleDateFormat("yyyy-MM-dd").format(new Date(new Date().getTime() - 8640000L));
        return str;
    }

    public static String getCurrentTime() {
        String str = null;
        str = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        return str;
    }

    public static String getLastDayTime() {
        String str = null;
        str = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(new Date().getTime() - 86400000L));
        return str;
    }
}