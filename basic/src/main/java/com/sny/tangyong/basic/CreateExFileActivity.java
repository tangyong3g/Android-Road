package com.sny.tangyong.basic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 */
public class CreateExFileActivity extends Activity implements View.OnClickListener {

    public static final String APP_PRO_PATH = "basic/";
    static final String SILENT_PREFIX = "-";
    static final String ERROR_FILE_TYPE = "_stk.txt";

    public static final String LAUNCHER_DATA_DIR_NAME = "basic";

    /** 桌面数据根目录，位于SD卡上 */
    public static final String LAUNCHER_DATA_DIR = getExtStorageDirPath() + File.separator + LAUNCHER_DATA_DIR_NAME;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_filecreate_ly);
    }

    /**
     * <br>功能简述:获取手机SD卡根路径
     * <br>功能详细描述:
     * <br>注意:
     *
     * @return
     */
    public static String getExtStorageDirPath() {
        File extStgDir = getExtStorageDir();
        if (null == extStgDir) {
            return null;
        }
        return extStgDir.getAbsolutePath();
    }

    /**
     * <br>功能简述:获取手机SD卡根路径文件夹
     * <br>功能详细描述:
     * <br>注意:
     *
     * @return
     */
    public static File getExtStorageDir() {
        return android.os.Environment.getExternalStorageDirectory();
    }


    @Override
    public void onClick(View v) {


    }


    public void getExtFilePath(View view) {
        Logger.getLogger("tyler.tang").info(getExtStorageDirPath());

    }


    public void createExtFile(View view) {

        String newFileDir = getExtStorageDirPath() + File.separator + APP_PRO_PATH;
        long timestamp = System.currentTimeMillis();
        Logger.getLogger("tyler.tang").info(newFileDir);
        File fileDir = new File(newFileDir);
        boolean rs = fileDir.isDirectory();
        Logger.getLogger("tyler.tang").info(rs + "");
        if (!rs) {
            boolean result = fileDir.mkdirs();
            Logger.getLogger("tyler.tang").info(result + "");
        }

        newFileDir += (null != null ? SILENT_PREFIX : "") + "stack-" + timestamp + ERROR_FILE_TYPE;
        File tFile = new File(newFileDir);
        FileOutputStream fos = null;

        try {

            fos = new FileOutputStream(tFile);

            OutputStreamWriter write = new OutputStreamWriter(fos);

            write.write(new Date().toString());

            write.flush();
            write.close();

            fos.flush();
            fos.close();


        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        }

    }

}
