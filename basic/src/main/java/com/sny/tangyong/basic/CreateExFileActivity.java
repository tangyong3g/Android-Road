package com.sny.tangyong.basic;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.sromku.simple.storage.SimpleStorage;
import com.sromku.simple.storage.Storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.logging.Logger;

/**
 *
 * get more information plz see the following url
 *
 *
 * <p/>
 * https://developer.android.com/training/basics/data-storage/files.html
 * <p/>
 * 内部存储： 它始终可用。 默认情况下只有您的应用可以访问此处保存的文件。 当用户卸载您的应用时，系统会从内部存储中删除您的应用的所有文件。
 * 当您希望确保用户或其他应用均无法访问您的文件时，内部存储是最佳选择。
 * <p/>
 * <p/>
 * 外部存储：
 * <p/>
 * 它并非始终可用，因为用户可采用 USB 存储的形式装载外部存储，并在某些情况下会从设备中将其删除。 它是全局可读的，因此此处保存的文件可能不受您控制地被读取。 当用户卸载您的应用时，只有在您通过
 * getExternalFilesDir() 将您的应用的文件保存在目录中时，系统才会从此处删除您的应用的文件。
 * 对于无需访问限制以及您希望与其他应用共享或允许用户使用电脑访问的文件，外部存储是最佳位置。
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
     * 在内存存储空间写入文件
     *
     * @param v
     */
    public void createInternalFile(View v) {
        saveFileInfoInternalStorage("hello.txt");
    }


    public void saveFileInfoInternalStorage(String fileName) {


        File folder = getFilesDir();

        File file = new File(folder.getAbsolutePath() + File.separator + fileName);

        boolean success = false;
        if (!file.exists()) {
            try {
                success = file.createNewFile();
            } catch (IOException io) {
                io.printStackTrace();
            }
        }

        if (success) {
            Logger.getLogger("tyler.tang").info(file.getAbsolutePath() + "create success!");
        }
    }


    private void write() {
        String filename = "external.txt";
        String string = "Hello world! external txt";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 在外部存储空间存储文件
     *
     * @param v
     */
    public void createExternalFile(View v) {

        saveFileToExternalFile("external.txt");
    }


    /**
     * 读写 ExternalFile
     *
     * @param fileName
     * @return
     */
    private boolean saveFileToExternalFile(String fileName) {

        if (isExternalStorageWritable() && isExternalStorageReadable()) {

            /*
             * 逻辑上分类，所有应用都可以访问 SD卡上面
             *
             * 涵盖以下分类
             *
             * @param type The type of storage directory to return. Should be one of {@link
             * #DIRECTORY_MUSIC}, {@link #DIRECTORY_PODCASTS}, {@link #DIRECTORY_RINGTONES}, {@link
             * #DIRECTORY_ALARMS}, {@link #DIRECTORY_NOTIFICATIONS}, {@link #DIRECTORY_PICTURES},
             * {@link #DIRECTORY_MOVIES}, {@link #DIRECTORY_DOWNLOADS}, or {@link #DIRECTORY_DCIM}.
             * May not be null.
             *
             */
            File externalRoot = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            Logger.getLogger("tyler.tang").info(externalRoot.getAbsolutePath());


            // --TODO 无法在external这里创建文件，为何？在nexus 6.0 上不行。
            File externalFile = new File(externalRoot, "external.png");
            boolean createExternalFileSuccess = false;
            if (!externalFile.exists()) {
                try {
                    createExternalFileSuccess = externalFile.createNewFile();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }

            Logger.getLogger("tyler.tang").info("create external file state:" + createExternalFileSuccess);


            /**
             * 应用专属的。获取外部卡的根目录，不传入任何的类型的时候,
             */
            File externalRootNull = getExternalFilesDir(null);

            Logger.getLogger("tyler.tang")
                    .info("create external null file path :" + externalRootNull.getAbsolutePath());


            File myAppFile = new File(externalRootNull + "/basictest");
            boolean externalRootNullFileRs = false;
            if (!myAppFile.exists()) {
                externalRootNullFileRs = myAppFile.mkdirs();
            }
            Logger.getLogger("tyler.tang").info("create external null file :" + externalRootNullFileRs);

            /**
             *
             * 应用范围内分类,应用卸载后信息会被删除
             *
             * {@link android.os.Environment#DIRECTORY_MUSIC},
             * {@link android.os.Environment#DIRECTORY_PODCASTS},
             * {@link android.os.Environment#DIRECTORY_RINGTONES},
             * {@link android.os.Environment#DIRECTORY_ALARMS},
             * {@link android.os.Environment#DIRECTORY_NOTIFICATIONS},
             * {@link android.os.Environment#DIRECTORY_PICTURES}, or
             * {@link android.os.Environment#DIRECTORY_MOVIES}.
             */
            File externalContext = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            Logger.getLogger("tyler.tang").fine(externalContext.getAbsolutePath());


        }

        return false;
    }



    /**
     *
     * 得到内部存储Dir文件
     *
     * @param view
     */
    public void getInternalFileDir(View view) {

        File file = getFilesDir();

        com.orhanobut.logger.Logger.i("internal file dir" + file.getAbsolutePath());

    }


    /**
     *
     * 得到内部存储Cache文件
     *
     * @param view
     */
    public void getInternalFileCacheDir(View view) {

        File file = getCacheDir();

        com.orhanobut.logger.Logger.i("internal cache file dir" + file.getAbsolutePath());

    }



    /**
     *
     * 得到外部存储文件公开目录
     *
     * @param view
     */
    public void getExternalFilePublic(View view) {

        if (isExternalStorageReadable()) {

            /**
             * {@link #DIRECTORY_MUSIC}, {@link #DIRECTORY_PODCASTS}, {@link #DIRECTORY_RINGTONES},
             * {@link #DIRECTORY_ALARMS}, {@link #DIRECTORY_NOTIFICATIONS},
             * {@link #DIRECTORY_PICTURES}, {@link #DIRECTORY_MOVIES}, {@link #DIRECTORY_DOWNLOADS},
             * or {@link #DIRECTORY_DCIM}. May not be null.
             */
            File externalPublic = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            com.orhanobut.logger.Logger.i(externalPublic.getAbsolutePath());


        }
    }


    /**
     *
     * 得到外部存储文件目录 应用内
     *
     * note:
     *
     * 1: 实际上会定义与程序相同名字的文件夹方便管理 2:
     *
     * @param view
     */
    public void getExternalFileRoot(View view) {

        if (isExternalStorageReadable()) {
            /**
             * {@link #DIRECTORY_MUSIC}, {@link #DIRECTORY_PODCASTS}, {@link #DIRECTORY_RINGTONES},
             * {@link #DIRECTORY_ALARMS}, {@link #DIRECTORY_NOTIFICATIONS},
             * {@link #DIRECTORY_PICTURES}, {@link #DIRECTORY_MOVIES}, {@link #DIRECTORY_DOWNLOADS},
             * or {@link #DIRECTORY_DCIM}. May not be null.
             */
            File file = getExternalFilesDir(null);
            com.orhanobut.logger.Logger.i("getExternalFileRoot" + file.getAbsolutePath());
        }
    }


    /**
     * 在SD卡根目录创建文件夹
     * 
     * <p>
     * target在22或者以下的机型才能生效
     * </p>
     *
     * @param view
     */
    public void createSDCardRootFolder(View view) {

        if (isExternalStorageReadable() && isExternalStorageWritable()) {

            String newFileDir = getExtStorageDirPath();

            com.orhanobut.logger.Logger.i(newFileDir);

            File folder = new File(newFileDir + File.separator + "test");

            if (!folder.exists()) {
                folder.mkdirs();
            }
            com.orhanobut.logger.Logger.i(folder.getAbsolutePath() + File.separator + folder.exists());
        }
    }


    public File getTempFile(Context context, String url) {
        File file = null;
        try {
            String fileName = Uri.parse(url).getLastPathSegment();
            file = File.createTempFile(fileName, null, context.getCacheDir());
        } catch (IOException e) {
            // Error while creating file
        }
        return file;
    }


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }


    /**
     * <br>
     * 功能简述:获取手机SD卡根路径 <br>
     * 功能详细描述: <br>
     * 注意:
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
     * <br>
     * 功能简述:获取手机SD卡根路径文件夹 <br>
     * 功能详细描述: <br>
     * 注意:
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


    /**
     * 利用 Storage来处理SD卡读写
     */
    private void useStorage() {

        Storage storage = null;

        if (SimpleStorage.isExternalStorageWritable()) {
            storage = SimpleStorage.getExternalStorage();
        } else {
            storage = SimpleStorage.getInternalStorage(getBaseContext());
        }

        String folder = "tangyonghello";
        storage.createDirectory(folder);

        boolean isExists = storage.isDirectoryExists(folder);

        storage.createFile(folder, "hello.txt", "some file content");

        File file = storage.getFile(folder, "hello.txt");

        Logger.getLogger("tyler.tang").info(file.getAbsolutePath());

    }

    public void createSDCardRootFolderFile(View view) {

        // useStorage();

        // File sd = Environment.getExternalStorageDirectory();
        // String path = sd.getPath() + "/tangyong";
        // File file = new File(path);
        // if (!file.exists()) file.mkdirs();
        //
        // try {
        //
        // String filePath = file.getAbsoluteFile() + "/helloddd.txt";
        // File f = createFile(filePath);
        //
        // Logger.getLogger("tyler.tang").info("absolutePath" + f.getAbsolutePath());
        // Logger.getLogger("tyler.tang").info("path" + f.getPath());
        //
        // } catch (IOException io) {
        // io.printStackTrace();
        // }


        String newFileDir = getExtStorageDirPath() + File.separator + APP_PRO_PATH;
        long timestamp = System.currentTimeMillis();
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
                Logger.getLogger("tyler.tang").info("createSuccess " + createSuccess);
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
    }


}
