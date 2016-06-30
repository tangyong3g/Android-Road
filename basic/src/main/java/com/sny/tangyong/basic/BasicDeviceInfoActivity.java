package com.sny.tangyong.basic;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 *
 * @Title:
 * @Description:
 * @Author:ty_sany@163.com
 * @Since:2016-06
 * @Version:1.1.0
 */
public class BasicDeviceInfoActivity extends AppCompatActivity {

    public static final String ANDROID_ID = "androidID";
    public static final String ROM = "rom";
    public static final String MODELTYPE = "modeltype";
    public static final String COUNTRY = "country";
    public static final String IMEI = "imei";
    public static final String ISROOT = "isroot";
    public static final String CPUFRE = "cpufre";
    public static final String CPUTYPE = "cputype";
    public static final String CPUFRU = "cpufru";
    public static final String CPUCOUNT = "cpucount";
    public static final String RAMMB = "rammb";
    public static final String ROMMB = "rommb";
    public static final String CHANNEL = "channel";
    public static final String MAC = "mac";
    public static final String IMSI = "imsi";
    public static final String NET = "net";
    public static final String HEIGHT = "height";
    public static final String WIDTH = "width";

    public static final int REQUEST_PHONE = 999;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_device_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PHONE) {
            int i = 0;
            for (String temp : permissions) {
                Log.i("tyler.tang", temp + "\t" + grantResults[i]);
            }
        }
    }

    /**
     * 获取数据
     * @param view
     */
    public void requestDeviceInfo(View view) {
        init();
    }

    public void requesetPermission(View view) {
        // request permission
        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i("tyler.tang", "Displaying camera permission rationale to provide additional context.");
            Snackbar.make(findViewById(R.id.tx_deviceinfo), R.string.permission, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(BasicDeviceInfoActivity.this,
                                    new String[] {Manifest.permission.READ_PHONE_STATE}, REQUEST_PHONE);
                        }
                    }).show();
        } else {
            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_PHONE_STATE}, REQUEST_PHONE);
        }
    }



    private void init() {
        TextView tx = (TextView) findViewById(R.id.tx_deviceinfo);
        tx.setText(initShowDeviceInfo());
    }


    private String initShowDeviceInfo() {

        StringBuilder sb = new StringBuilder();

        sb.append(ANDROID_ID);
        sb.append(":\t");
        sb.append(DeviceUtils.getAndroidId(getApplicationContext()));
        sb.append("\n");

        sb.append(ROM);
        sb.append(":\t");
        sb.append(Build.DISPLAY);
        sb.append("\n");

        sb.append(MODELTYPE);
        sb.append(":\t");
        sb.append("device:\t" + Build.DEVICE + "borad:\t" + Build.BOARD + "brand:\t" + Build.BRAND + "display:\t"
                + Build.DISPLAY);
        sb.append("\n");

        sb.append(COUNTRY);
        sb.append(":\t");
        sb.append(DeviceUtils.getLocal(getApplicationContext()));
        sb.append("\n");

        try {
            sb.append(IMEI);
            sb.append(":\t");
             sb.append(DeviceUtils.getIMEI(getBaseContext()));
            sb.append("\n");
        } catch (Exception ex) {}

        sb.append(ISROOT);
        sb.append(":\t");
        sb.append(DeviceUtils.isRootSystem());
        sb.append("\n");

        sb.append(CPUFRE);
        sb.append(":\t");
        sb.append(CPUInfoUtils.getMaxCpuFreq());
        sb.append("\n");

        sb.append(CPUTYPE);
        sb.append(":\t");
        sb.append(CPUInfoUtils.getCpuModel());
        sb.append("\n");

        sb.append(RAMMB);
        sb.append(":\t");
        sb.append(DeviceUtils.getTotalMemory(getApplicationContext()) / 1024L / 1024L);
        sb.append("MB");
        sb.append("\n");

        sb.append(ROMMB);
        sb.append(":\t");
        sb.append(DeviceUtils.getTotalInternalMemorySize_() / 1024L / 1024L);
        sb.append("MB");
        sb.append("\n");

        sb.append(CPUCOUNT);
        sb.append(":\t");
        sb.append(CPUInfoUtils.getCpuCoreNums());
        sb.append("\n");

        sb.append(CHANNEL);
        sb.append(":\t");
        // sb.append(AppInfo.getMetaData(getApplicationContext(),"CHANNEL"));
        sb.append("\n");

        sb.append(MAC);
        sb.append(":\t");
        sb.append(DeviceUtils.getMac(getApplicationContext()));
        sb.append("\n");

        sb.append(IMSI);
        sb.append(":\t");
        sb.append(DeviceUtils.getIMSI(getApplicationContext()));
        sb.append("\n");

        sb.append(NET);
        sb.append(":\t");
        sb.append(NetUtils.getConnectType(getApplicationContext()));
        sb.append("\n");

        sb.append(WIDTH);
        sb.append(":\t");
        sb.append(DeviceUtils.getWidth(getApplicationContext()));
        sb.append("\n");

        sb.append(HEIGHT);
        sb.append(":\t");
        sb.append(DeviceUtils.getHeight(getApplicationContext()));
        sb.append("\n");

        sb.append("Build.VERSION.SDK_INT");
        sb.append(":\t");
        sb.append(Build.VERSION.SDK_INT);
        sb.append("\n");

        try {
            sb.append("Build.VERSION.BASE_OS");
            sb.append("\t");
             sb.append(Build.VERSION.BASE_OS);
            sb.append("\n");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        sb.append("Build.VERSION.RELEASE");
        sb.append("\t");
        sb.append(Build.VERSION.RELEASE);
        sb.append("\n");

        sb.append("Build.VERSION.INCREMENTAL");
        sb.append("\t");
        sb.append(Build.VERSION.INCREMENTAL);
        sb.append("\n");

        sb.append("Build.VERSION.SECURITY_PATCH");
        sb.append("\t");
        // sb.append(Build.VERSION.SECURITY_PATCH);
        sb.append("\n");

        sb.append("Build.VERSION.SDK");
        sb.append("\t");
        sb.append(Build.VERSION.SDK);
        sb.append("\n");

        sb.append("Build.VERSION.SDK_INT");
        sb.append("\t");
        sb.append(Build.VERSION.SDK_INT);
        sb.append("\n");

        sb.append("Build.VERSION.PREVIEW_SDK_INT");
        sb.append("\t");
        // sb.append(Build.VERSION.PREVIEW_SDK_INT);
        sb.append("\n");

//        Logger.d(sb.toString());

        return sb.toString();
    }
}
