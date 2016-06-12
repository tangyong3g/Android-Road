package com.sny.tangyong.basic;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.TextView;


/**
 *

 1. Monitor network connections (Wi-Fi, GPRS, UMTS, etc.)
 2. Send broadcast intents when network connectivity changes
 3. Attempt to "fail over" to another network when connectivity to a network is lost
 4. Provide an API that allows applications to query the coarse-grained or fine-grained state of the available networks
 5. Provide an API that allows applications to request and select networks for their data traffic

 *
 */
public class ReduceBatteryDrainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tx = new TextView(getBaseContext());
        tx.setText(getConnect());

        setContentView(tx);

    }

    private String getConnect(){

        ConnectivityManager conManager =  (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = conManager.getActiveNetworkInfo();
        String result = info.toString();
        return result;
    }
}
