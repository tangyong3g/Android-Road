package com.sny.tangyong.basic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * 错误反馈提示
 */
public class BasicErrorReportActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button btn = new Button(getBaseContext());
        btn.setLayoutParams(layoutParams);
        btn.setText(R.string.crashOccur);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // occurs a problem .
                int i = 1 / 0;

            }
        });
        setContentView(btn);

    }
}
