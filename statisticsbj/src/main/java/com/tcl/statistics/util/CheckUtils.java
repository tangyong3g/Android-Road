package com.tcl.statistics.util;

import android.text.TextUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基础类
 */
public class CheckUtils {

    /**
     * 判断名字合法性
     *
     * @param eventName
     * @return
     */
    public static boolean isLegalEventName(String eventName) {
        if (TextUtils.isEmpty(eventName)) {
            return false;
        }
        if (eventName.length() > 50) {
            throw new RuntimeException("eventName is beyond the limits,please set eventName less than 50.");
        }

        return isLegalString(eventName);
    }

    /**
     * 判断参数的合法性
     *
     * @param map
     * @return
     */
    public static boolean isLegalParamKeyAndValue(Map<String, String> map) {
        if (map == null || map.size() == 0) {
            throw new RuntimeException("error!params map is empty or size is 0!");
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (TextUtils.isEmpty(key)) {
                throw new RuntimeException("error!param key is empty");
            } else if (TextUtils.isEmpty(value)) {
                throw new RuntimeException("error!param value is empty");
            } else if (key.length() > 50) {
                throw new RuntimeException("param key:" + key + " is beyond the limits,please set key less than 50.");
            } else if (value.length() > 100) {
                throw new RuntimeException("param value:" + value + "  is beyond the limits,please set value less than 100.");
            }

            if (!isLegalString(key)) {
                throw new RuntimeException("error!key:" + key + " is not legal,only letter,number and underline is valid");
            }
        }

        return true;
    }

    /**
     * 字符串的合法性
     *
     * @param str
     * @return
     */
    private static boolean isLegalString(String str) {
        String strPattern = "[a-zA-Z_0-9]+";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(str);
        return m.matches();
    }
}
