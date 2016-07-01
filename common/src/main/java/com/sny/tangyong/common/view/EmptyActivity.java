package com.sny.tangyong.common.view;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.sny.tangyong.common.R;

/**
 * @author tyler.tang
 * @date 2016-07-01
 * @project Android-best-practice
 */
public class EmptyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initComponent();
    }


    private void initComponent(){

        TextView tx = new TextView(getBaseContext());
        tx.setText(R.string.dev_tip);

        setContentView(tx);
    }
}
