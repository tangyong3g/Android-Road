package com.sny.tangyong.basic;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tylertang@tcl.com on 2016/5/12.
 *
 * 参考信息 http://blog.sina.com.cn/s/blog_74c22b210100ypfd.html
 */
public class CPUInfoUtils {

    public final static String CPU_FILE_DIR = "/sys/devices/system/cpu/";
    public final static String CAT_DIR = "/system/bin/cat";
    private static int sCpuCount = -1;
    private static String sCpuModel;
    private static String sCpuMaxFreq;


    /**
     * @return 型号
     */
    public static String getCpuModel() {
        if (sCpuModel != null) {
            return sCpuModel;
        }
        String value = "";
        byte[] perByte = new byte[24];
        ProcessBuilder cmd = new ProcessBuilder();
        String[] dir = new String[] {CAT_DIR, "/proc/cpuinfo"};
        cmd.command(dir);
        InputStream in = null;
        try {
            Process process = cmd.start();
            in = process.getInputStream();
            while (in != null && in.read(perByte) != -1) {
                value = value + new String(perByte);
            }
            String[] split = value.split("\n");
            if (split != null && split.length > 0) {
                for (String str : split) {
                    if (str != null && str.toLowerCase().contains("hardware")) {
                        String[] split2 = str.split(":");
                        if (split2 != null && split2.length > 1) {
                            if (split2 != null) {
                                sCpuModel = split2[1].trim();
                                return sCpuModel;
                            }
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     *
     * @return 频率
     */
    public static String getMaxCpuFreq() {
        if (sCpuMaxFreq != null) {
            return sCpuMaxFreq;
        }
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {CAT_DIR, CPU_FILE_DIR + "cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        sCpuMaxFreq = result.trim();
        return sCpuMaxFreq;
    }


    /**
     *
     * @return int 得到CPU核数
     */
    public static int getCpuCoreNums() {
        if (sCpuCount != -1) {
            return sCpuCount;
        }
        sCpuCount = Runtime.getRuntime().availableProcessors();
        return sCpuCount;
    }



}
