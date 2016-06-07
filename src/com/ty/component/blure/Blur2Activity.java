package com.ty.component.blure;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.sny.tangyong.androiddemo.R;


/**
 * 图片模糊升级版本来自于
 * <p/>
 * http://blog.csdn.net/crazy__chen/article/details/47027069
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * Created by Administrator on 2015/7/29.
 */
public class Blur2Activity extends Activity {

    ImageView mBlurImage;
    BlurDrawable blurDrawable;
    float mLastY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.blur);
        /*
        mBlurImage = (ImageView) findViewById(R.id.img_blur);

        Bitmap bp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        BlurDrawable blurDrawable = new BlurDrawable(this, getResources(), bp);
        mBlurImage.setImageDrawable(blurDrawable.getBlurDrawable());
    */
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mLastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                float alphaDelt = (y - mLastY) / 50;
                int alpha = (int) (blurDrawable.getBlur() + alphaDelt);

                if (alpha > 255) {
                    alpha = 255;
                } else if (alpha < 0.0) {
                    alpha = 0;
                }
                //blurDrawable.setBlur(alpha);
                Log.i("tyler", alpha + "");
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }
}
