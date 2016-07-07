/*
 * Copyright 2010 Emmanuel Astier & Kevin Gaudin
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.tcl.mailfeedback;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.orhanobut.logger.Logger;
import com.tcl.mailfeedback.ErrorReporter.ReportsSenderWorker;

import java.io.File;


/**
 * This is the dialog Activity used by ACRA to get authorization from the user to send reports.
 * Requires android:theme="@android:style/Theme.Dialog" and android:launchMode="singleInstance" in
 * your AndroidManifest to work properly.
 * 
 * @author Kevin Gaudin
 * 
 */
public class CrashReportDialog extends Activity {
    private final static String TAG = CrashReport.LOG_TAG;
    /**
     * Default left title icon.
     */

    String mReportFileName = null;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SettingScreenInfo settingInfo =
        // LauncherApplication.getSettings().getInstanceScreenInfo();
        // if (settingInfo.isHideStatusBar()) {
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // } else if (!settingInfo.isHideStatusBar()) {
        // getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(CrashReportConfig.RES_DIALOG_LAYOUT);
        // --TODO delefile and readfile 不知道为什么这里要这么处理 ，两种试获取Folder Name会产生错误
//         mReportFileName = getIntent().getStringExtra(ErrorReporter.EXTRA_REPORT_FILE_NAME);

//        mReportFileName = LogRecord.readLogFile();
//        LogRecord.deleteLogFile();

        String[] filesList = ErrorReporter.getInstance().getCrashReportFilesList();
        if(filesList != null && filesList.length >0){
            String fileName = filesList[filesList.length-1];
            mReportFileName = ErrorReporter.getInstance().getCrashReportFilePath()+ File.separator + fileName;
        }
        Logger.i("消息到达Diloag中"+mReportFileName);

        if (mReportFileName == null) {
            Log.i(TAG, "CrashReportDialog return");
            finish();
        }

        Button btnYes = (Button) findViewById(CrashReportConfig.RES_DIALOG_YES_BTN_ID);
        Button btnNo = (Button) findViewById(CrashReportConfig.RES_DIALOG_NO_BTN_ID);
        btnYes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onYes();
            }
        });
        btnNo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                onNo();
            }
        });

        cancelNotification();
    }

    /**
     * Disable the notification in the Status Bar.
     */
    protected void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(ErrorReporter.ID_ERROR_REPORT);
    }

    private void onYes() {
        try {
            ErrorReporter err = ErrorReporter.getInstance();
            ReportsSenderWorker worker = err.new ReportsSenderWorker();
            worker.setCommentReportFileName(mReportFileName);
            worker.start();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        finish();
    }

    private void onNo() {
        finish();
    }
}
