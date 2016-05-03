package com.tcl.statistics.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 加密工具
 */
public class MD5 {
    public static String getMD5(String content) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(content.getBytes());
            return getHashString(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getHashString(MessageDigest digest) {
        byte[] arrayOfByte;
        StringBuilder builder = new StringBuilder();
        int j = (arrayOfByte = digest.digest()).length;
        for (int i = 0; i < j; ++i) {
            byte b = arrayOfByte[i];
            builder.append(Integer.toHexString(b >> 4 & 0xF));
            builder.append(Integer.toHexString(b & 0xF));
        }
        return builder.toString();
    }
}