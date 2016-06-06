package com.sny.tangyong.basic;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

/**
 *
 * @author tang
 *
 */
public class CacheBitmapActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {

        ImageView imageView = new ImageView(getBaseContext());
        BasicGraphicLruCache.getInstance().loadBitmapToImageView(R.drawable.bg_one, imageView);

        setContentView(imageView);
    }
}
