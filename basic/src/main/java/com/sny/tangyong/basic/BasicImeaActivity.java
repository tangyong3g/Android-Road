package com.sny.tangyong.basic;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * @author ty_sany@163.com
 *
 *         获取IMEA
 *
 * 
 */
public class BasicImeaActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CODE_ASK_READ_PHONE = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_imea);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null)
                        .show();
            }
        });
        init();
    }


    private void init() {

        Button btn = (Button) findViewById(R.id.btn_imea);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.btn_imea) {

            String Imei = null;
            String androidId = null;

            TextView txAndroidId = (TextView) findViewById(R.id.tx_androidid);
            TextView txImea = (TextView) findViewById(R.id.tx_imea);

            if (Build.VERSION.SDK_INT >= 23) {

                int readPhoneStatePermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
                if (readPhoneStatePermission != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_ASK_READ_PHONE);
                    return;

                } else {

                    Imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
                    androidId = getAndroidId(getApplicationContext());
                }
            } else {

                Imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
                androidId = getAndroidId(getApplicationContext());
            }

            txImea.setText("AndroidId \t+" + androidId);
            txAndroidId.setText("Imea \t" + Imei);

            Toast.makeText(BasicImeaActivity.this, "Imea" + Imei + ":\t androidId :\t" + androidId, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * <br>
     * 功能简述:获取Android ID的方法 <br>
     * 功能详细描述: <br>
     * 注意:
     * 
     * @return
     */
    public static String getAndroidId(Context context) {
        String androidId = null;
        if (context != null) {
            androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return androidId;
    }
}
