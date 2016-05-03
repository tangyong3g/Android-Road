package com.tcl.statistics.util;

import android.content.Context;

import com.tcl.statistics.bean.StatisticsResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.HashMap;

public class FileSerializableUtils {
    public static final String STATISTICS_LOG_NAME = "statistics.text";
    private static final String LOG_DIR = "log";
    private static FileSerializableUtils fileSerializableUtils;

    public static FileSerializableUtils getInstence() {
        if (fileSerializableUtils == null)
            synchronized (FileSerializableUtils.class) {
                if (fileSerializableUtils == null)
                    fileSerializableUtils = new FileSerializableUtils();
            }


        return fileSerializableUtils;
    }

    public boolean saveStatisticsResultToFile(Context context, StatisticsResult statisticsResult)
            throws IOException {
        File filesDir = context.getFilesDir();
        File logFileDir = new File(filesDir, "log");
        if (!(logFileDir.exists()))
            logFileDir.mkdir();

        File logFile = new File(logFileDir, DateUtils.getCurrentDay() + "_" + "statistics.text");
        FileOutputStream fos = new FileOutputStream(logFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(statisticsResult);
        oos.flush();
        oos.close();

        return true;
    }

    public Serializable getObjectFromFile(Context context)
            throws StreamCorruptedException, IOException, ClassNotFoundException {
        File filesDir = context.getFilesDir();
        File logFileDir = new File(filesDir, "log");
        if (!(logFileDir.exists()))
            logFileDir.mkdir();

        File logFile = new File(logFileDir, DateUtils.getCurrentDay() + "_" + "statistics.text");
        if (logFile.exists()) {
            FileInputStream fis = new FileInputStream(logFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

            Serializable obj = (Serializable) ois.readObject();
            ois.close();
            return obj;
        }
        return null;
    }

    public HashMap<Serializable, String> getHistoryLogs(Context context) {
        HashMap historyLogs = null;

        File logFileDir = new File(context.getFilesDir(), "log");
        File[] logFiles = logFileDir.listFiles();
        if ((logFiles != null) && (logFiles.length > 0)) {
            File[] arrayOfFile1;
            int j = (arrayOfFile1 = logFiles).length;
            for (int i = 0; i < j; ++i) {
                File file = arrayOfFile1[i];

                if ((file.getName().endsWith("statistics.text")) && (!(file.getName().contains(DateUtils.getCurrentDay()))))
                    try {
                        FileInputStream fis = new FileInputStream(file.getAbsolutePath
                                ());
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        Serializable log = (Serializable) ois.readObject();
                        if (historyLogs == null)
                            historyLogs = new HashMap();

                        historyLogs.put(log, file.getAbsolutePath());
                        ois.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }

        }

        return historyLogs;
    }

    public void deleteTodayLogFile(Context context) {
        File filesDir = context.getFilesDir();
        File logFileDir = new File(filesDir, "log");
        if (!(logFileDir.exists()))
            logFileDir.mkdir();

        File logFile = new File(logFileDir, DateUtils.getCurrentDay() + "_" + "statistics.text");
        if ((logFile != null) && (logFile.exists()))
            logFile.delete();
    }

    public boolean saveStatisticsResultToLastDayFile(Context context, StatisticsResult statisticsResult)
            throws IOException {
        File filesDir = context.getFilesDir();
        File logFileDir = new File(filesDir, "log");
        if (!(logFileDir.exists()))
            logFileDir.mkdir();

        File logFile = new File(logFileDir, DateUtils.getLastDayTime() + "_" + "statistics.text");
        FileOutputStream fos = new FileOutputStream(logFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(statisticsResult);
        oos.flush();
        oos.close();

        return true;
    }
}