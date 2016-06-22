package com.tcl.mailfeedback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 记录本次错误日志文件地址
 *
 * @author tyler.tang
 *
 */
public class LogRecord {

    /**
     * 读取文件内容
     * 
     * @param path 文件路径
     * @return 文件内容
     * @throws IOException
     */
    public static String readFile(String path) throws IOException {
        File file = new File(path);
        FileInputStream is = new FileInputStream(file);

        long len = file.length();
        byte[] buffer = new byte[(int) len];
        is.read(buffer);

        String result = new String(buffer);
        return result;

    }

    public static String readLogFile() {
        String filePath = CrashReportConfig.LOG_PATH + "logrecord";
        String str = null;

        try {
            str = readFile(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return str;
    }

    public static void deleteLogFile() {
        String filePath = CrashReportConfig.LOG_PATH + "logrecord";

        File file = new File(filePath);

        file.delete();
    }

    public static void writeLogFile(String str) throws IOException {
        String filePath = CrashReportConfig.LOG_PATH;

        File destDir = new File(filePath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        filePath += "logrecord";

        File file = new File(filePath);
        FileOutputStream trace = new FileOutputStream(file, true);

        file.createNewFile();

        trace.write(str.getBytes());

        trace.flush();
        trace.close();
    }
}
