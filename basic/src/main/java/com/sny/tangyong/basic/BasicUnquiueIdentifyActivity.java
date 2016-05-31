package com.sny.tangyong.basic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class BasicUnquiueIdentifyActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_unquiue_identify);

        init();
    }


    private void init() {}



    @Override
    public void onClick(View v) {

        if (v == null) {
            return;
        }

        int id = v.getId();

        switch (id) {

            case R.id.btn_androidId:

                break;

            case R.id.btn_mac:


                break;

            case R.id.btn_uuid:

                break;
            case R.id.btn_imei:
                break;

            case R.id.btn_instanceId:

                break;

            case R.id.btn_randomUUID:

                break;

            case R.id.btn_advertisingId:
                break;
        }
    }


    private String getAndroidId() {
        String result = null;

        return result;
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


    private String getUUid() {

        String result = null;

        return result;
    }


    private String getImei() {
        String result = null;

        return result;
    }


    private String getInstanceId() {
        String result = null;

        return result;
    }

    private String getRandomUUID() {
        String result = null;

        return result;
    }


    private String getAdvertisingId() {
        String result = null;

        return result;
    }


}
