package com.sny.tangyong.basic;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * user:  tyler.tang(ty_sany@163.com)
 * date:  2015/7/30
 * url:   https://github.com/tangyong3g/Android-Demo
 */
public class GLSurfaceViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    class SimpleGlSrufaceView extends GLSurfaceView {

        public SimpleGlSrufaceView(Context context) {
            super(context);
        }

    }

}
