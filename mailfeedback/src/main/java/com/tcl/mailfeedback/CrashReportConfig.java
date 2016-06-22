package com.tcl.mailfeedback;


import android.os.Environment;

/**
 * 错误报告配置 通过这个类配置
 * 
 * @author tyler.tang
 *
 */
public final class CrashReportConfig {

    /** 通知栏用到的资源 */
    public final static int RES_NOTIF_ICON = android.R.drawable.stat_notify_error;
    public final static int RES_NOTIF_TICKER_TEXT = R.string.crash_notif_ticker_text;
    public final static int RES_NOTIF_TITLE = R.string.crash_notif_title;
    public final static int RES_NOTIF_TEXT = R.string.crash_notif_text;

    /** 对话框用到的资源 */
    public final static int RES_DIALOG_ICON = android.R.drawable.ic_dialog_info;
    public final static int RES_DIALOG_TITLE = R.string.crash_dialog_title;
    public final static int RES_DIALOG_TEXT = R.string.crash_dialog_text;

    /** 对话框布局 */
    public final static int RES_DIALOG_LAYOUT = R.layout.report;
    /** 对话确定按钮id */
    public final static int RES_DIALOG_YES_BTN_ID = R.id.sure_report;
    /** 对话取消按钮id */
    public final static int RES_DIALOG_NO_BTN_ID = R.id.cancel_report;

    /** 邮件标题的字符串id */
    public final static int RES_EMAIL_SUBJECT = R.string.crash_subject;

    /** 收件邮箱 */
    public static String sEMAIL_RECEIVER = "tyler.sany@gmail.com";
    /** 崩溃日志保存路径 */
    public final static String LOG_PATH = Path.SDCARD + Path.LOG_DIR;

    /**
     * 是否搜集额外的包信息 为ture需要配置 {@link #ADDITIONAL_TAG} 和 {@link #ADDITIONAL_PACKAGES}
     */
    public final static boolean REPORT_ADDITIONAL_INFO = true;


    /**
     * widget的签名信息
     */
    public final static boolean REPORT_ADDITIONAL_SIGNATURE = true;
    public final static String WIDGET_SIGNATURE = "XData";


    /**
     * 路径类 所有路径相关的常量都同意放在此处
     *
     * @author tyler.tang
     */
    public static final class Path {
        /**
         * sdcard head
         */
        public final static String SDCARD = Environment.getExternalStorageDirectory().getPath();

        // 存储路径 $Lite /NextLauncher_Data_Lite
        public static String sLAUNCHER_DIR = "/Launcher_Data";


        /**
         * 数据库文件备份目录
         */
        public final static String DBFILE_PATH = sLAUNCHER_DIR + "/db";

        /**
         * 主题等SharedPreferences文件备份目录
         */
        // public final static String PREF_PATH = LAUNCHER_DIR + "/pref";

        /**
         * 日志文件备份目录
         */
        public final static String LOG_DIR = sLAUNCHER_DIR + "/log/";

        /**
         * 图片文件目录
         */
        // public final static String COMMON_ICON_PATH = LAUNCHER_DIR + "/icon/";

        public final static String GOTHEMES_PATH = SDCARD + sLAUNCHER_DIR + "/nextTheme/";
        public final static String SCREEN_FOLDER = "/screen";
        public final static String DOCK_FOLDER = "/dock";
        public final static String APPDRAWER_FOLDER = "/appdrawer";

        /**
         * 特色图标路径
         */
        public final static String CUSTOM_ICON_DIR = sLAUNCHER_DIR + "/desk/customicon";

        /**
         * 主题等SharedPreferences文件备份目录
         */
        public final static String PREFERENCESFILE_PATH = sLAUNCHER_DIR + "/preferences";

        /**
         * 自定义手势－文件存储目录
         */
        public static final String DIY_GESTURE_PATH = sLAUNCHER_DIR + "/diygesture/";
    }

}
