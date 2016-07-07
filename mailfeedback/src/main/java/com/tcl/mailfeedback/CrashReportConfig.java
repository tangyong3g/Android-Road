package com.tcl.mailfeedback;


/**
 * 错误报告配置 通过这个类配置
 *
 * @author tyler.tang
 */
public final class CrashReportConfig {


    /**
     * 通知栏用到的资源
     */
    public final static int RES_NOTIF_ICON = android.R.drawable.stat_notify_error;
    public final static int RES_NOTIF_TICKER_TEXT = R.string.crash_notif_ticker_text;
    public final static int RES_NOTIF_TITLE = R.string.crash_notif_title;
    public final static int RES_NOTIF_TEXT = R.string.crash_notif_text;

    /**
     * 对话框用到的资源
     */
    public final static int RES_DIALOG_ICON = android.R.drawable.ic_dialog_info;
    public final static int RES_DIALOG_TITLE = R.string.crash_dialog_title;
    public final static int RES_DIALOG_TEXT = R.string.crash_dialog_text;

    /**
     * 对话框布局
     */
    public final static int RES_DIALOG_LAYOUT = R.layout.report;
    /**
     * 对话确定按钮id
     */
    public final static int RES_DIALOG_YES_BTN_ID = R.id.sure_report;
    /**
     * 对话取消按钮id
     */
    public final static int RES_DIALOG_NO_BTN_ID = R.id.cancel_report;

    /**
     * 邮件标题的字符串id
     */
    public final static int RES_EMAIL_SUBJECT = R.string.crash_subject;
    /**
     * 收件邮箱
     */
    public static String sEMAIL_RECEIVER = "tyler.sany@gmail.com";


    /**
     * 路径类 所有路径相关的常量都同意放在此处
     *
     * @author tyler.tang
     */
    public static final class Path {
        /**
         * 日志文件备份目录
         */
        public final static String LOG_DIR = "/errorlog";

    }

}
