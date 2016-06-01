package com.sny.tangyong.basic;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.UUID;


/**
 * @author ty_sany@163.com
 * 
 */
public class BasicUnquiueIdentifyActivity extends Activity implements View.OnClickListener {


    private TextView mTxAndroidId;
    private TextView mTxMac;
    private TextView mTxImei;
    private TextView mTxInstanceId;
    private TextView mTxRandomUUID;
    private TextView mTxAdvertisingId;
    private Handler mHander = new UnquiueIdHandler();
    private static final int MSG_WHAT_ADID = 111;

    private static final int MSG_WHAT_ERROR_GOOGLE_CONNECT = 110;
    private static final int MSG_WHAT_ERROR_GOOGLE_NOT_EXSIT = 112;
    private static final int MSG_WHAT_ERROR_IO = 113;


    Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_unquiue_identify);

        init();

    }

    private void init() {

        findViewById(R.id.btn_androidId).setOnClickListener(this);
        findViewById(R.id.btn_mac).setOnClickListener(this);
        findViewById(R.id.btn_imei).setOnClickListener(this);
        findViewById(R.id.btn_instanceId).setOnClickListener(this);
        findViewById(R.id.btn_randomUUID).setOnClickListener(this);
        findViewById(R.id.btn_advertisingId).setOnClickListener(this);


        mTxAndroidId = (TextView) findViewById(R.id.tx_androidId);
        mTxMac = (TextView) findViewById(R.id.tx_mac);
        mTxImei = (TextView) findViewById(R.id.tx_imei);
        mTxInstanceId = (TextView) findViewById(R.id.tx_instanceId);
        mTxRandomUUID = (TextView) findViewById(R.id.tx_randomUUID);
        mTxAdvertisingId = (TextView) findViewById(R.id.tx_advertisingId);

    }


    @Override
    public void onClick(View v) {

        if (v == null) {
            return;
        }

        int id = v.getId();

        switch (id) {

            case R.id.btn_androidId:

                mTxAndroidId.setText(getAndroidID());

                break;

            case R.id.btn_mac:

                String address = getMacAddress();
                mTxMac.setText(address);

                break;

            case R.id.btn_imei:

                mTxImei.setText(getImei());

                break;

            case R.id.btn_instanceId:

                mTxInstanceId.setText(getInstanceId());

                break;

            case R.id.btn_randomUUID:

                mTxRandomUUID.setText(getRandomUUID());

                break;

            case R.id.btn_advertisingId:
                getAdvertisingId();
                break;
        }
    }



    /*
     * @return String
     */
    private String getMacAddress() {

        String result = null;

        try {
            result = DeviceUtils.getMacAddress();
        } catch (Exception ex) {
            ex.printStackTrace();
            // 当出现异常时利用系统API去获取
            result = DeviceUtils.getMac(getApplicationContext());
        }
        return result;
    }


    private String getAndroidID() {

        String result = null;

        result = DeviceUtils.getAndroidId(getApplicationContext());

        return result;
    }



    private String getImei() {
        String result = null;

        try {
            result = DeviceUtils.getIMEI(getApplicationContext());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }


    private String getInstanceId() {
        String result = null;

        InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
        result = instanceID.getId();

        return result;
    }

    private String getRandomUUID() {
        String result = null;

        result = UUID.randomUUID().toString();

        return result;
    }


    private void getAdvertisingId() {

        if (mThread == null) {
            mThread = new Thread() {
                @Override
                public void run() {
                    super.run();

                    Message msg = null;
                    msg = Message.obtain();
                    try {

                        AdvertisingIdClient.Info info =
                                AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                        String adId = info.getId();

                        msg.what = MSG_WHAT_ADID;
                        msg.obj = adId;

                    } catch (IOException io) {
                        io.printStackTrace();
                        msg.what = MSG_WHAT_ERROR_IO;
                    } catch (GooglePlayServicesNotAvailableException gn) {
                        gn.printStackTrace();
                        msg.what = MSG_WHAT_ERROR_GOOGLE_NOT_EXSIT;

                    } catch (GooglePlayServicesRepairableException re) {
                        re.printStackTrace();
                        msg.what = MSG_WHAT_ERROR_GOOGLE_CONNECT;
                    }

                    mHander.sendMessage(msg);

                }
            };
        }

        mThread.start();
    }



    class UnquiueIdHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg == null) return;

            int what = msg.what;

            switch (what) {

                case MSG_WHAT_ADID:
                    mTxAdvertisingId.setText(msg.obj.toString());
                    break;
                case MSG_WHAT_ERROR_GOOGLE_CONNECT:
                    Toast.makeText(getApplicationContext(), "google play service error!", Toast.LENGTH_LONG).show();
                    mTxAdvertisingId.setText("google play service error!");
                    break;
                case MSG_WHAT_ERROR_GOOGLE_NOT_EXSIT:
                    Toast.makeText(getApplicationContext(), "google play service not exist!", Toast.LENGTH_LONG).show();
                    mTxAdvertisingId.setText("google play service not exist!");
                    break;
                case MSG_WHAT_ERROR_IO:
                    mTxAdvertisingId.setText("IO Exception");
                    break;
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();



    }
}
