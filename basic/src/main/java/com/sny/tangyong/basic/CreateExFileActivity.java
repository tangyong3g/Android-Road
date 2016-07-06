package com.sny.tangyong.basic;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 */
public class CreateExFileActivity extends Activity implements View.OnClickListener {

    public static final String APP_PRO_PATH = "basic/";
    static final String SILENT_PREFIX = "-";
    static final String ERROR_FILE_TYPE = "_stk.txt";

    public static final String LAUNCHER_DATA_DIR_NAME = "basic";
    public static final String SDPATH = Environment.getExternalStorageDirectory() + "/";

    /**
     * 桌面数据根目录，位于SD卡上
     */
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


    public boolean checkFileExists(String filepath) {
        File file = new File(SDPATH + filepath);
        return file.exists();
    }

    public File createDIR(String dirpath) {
        File dir = new File(SDPATH + dirpath);
        dir.mkdir();
        return dir;
    }

    public File createFile(String filepath) throws IOException {
        File file = new File(SDPATH + filepath);
        file.createNewFile();
        return file;
    }


    public void createExtFile(View view) {

//        Storage storage = SimpleStorage.getExternalStorage();

//        Storage storage = SimpleStorage.getInternalStorage(getBaseContext());

        Storage storage = null;

        if (SimpleStorage.isExternalStorageWritable()) {
            storage = SimpleStorage.getExternalStorage();
        } else {
            storage = SimpleStorage.getInternalStorage(getBaseContext());
        }

        storage = SimpleStorage.getInternalStorage(getBaseContext());

        String folder = "tangyonghello";
        storage.createDirectory(folder);

        boolean isExists = storage.isDirectoryExists(folder);

        storage.createFile(folder,"hello.txt","some file content");

        File file = storage.getFile(folder,"hello.txt");

        Logger.getLogger("tyler.tang").info(file.getAbsolutePath());




        /*
        File sd = Environment.getExternalStorageDirectory();
        String path = sd.getPath() + "/tangyong";
        File file = new File(path);
        if (!file.exists())
            file.mkdir();

        try {

            File folder   = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if(!folder.exists()){
                folder.mkdirs();
            }

            String filePath = file.getAbsoluteFile()+"/hello.txt";
            File f = createFile(filePath);

            Logger.getLogger("tyler.tang").info("absolutePath"+f.getAbsolutePath());
            Logger.getLogger("tyler.tang").info("path"+f.getPath());

        } catch (IOException io) {
            io.printStackTrace();
        }


*/

         /*
        String newFileDir = getExtStorageDirPath() + File.separator + APP_PRO_PATH;
        long timestamp = System.currentTimeMillis();
        Logger.getLogger("tyler.tang").info(newFileDir);
        File fileDir = new File(newFileDir);
        boolean rs = fileDir.isDirectory();
        Logger.getLogger("tyler.tang").info(rs + "");
        if (!rs) {
            boolean result = fileDir.mkdirs();
            Logger.getLogger("tyler.tang").info("create folder :\t" + result + "");
        }

        newFileDir += (null != null ? SILENT_PREFIX : "") + "stack-" + timestamp + ERROR_FILE_TYPE;
        File tFile = new File(newFileDir);
        FileOutputStream fos = null;

        Logger.getLogger("tyler.tang").info(tFile.getAbsolutePath());

        if (!tFile.exists()) {
            try {
                boolean createSuccess = tFile.createNewFile();
                Logger.getLogger("tyler.tang").info("createSuccess "+createSuccess);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }


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
        */
    }


}
