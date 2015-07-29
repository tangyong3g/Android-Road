package com.sny.tangyong.basic;

import android.app.Activity;


/**
 * @author tang
 *         <p/>
 *         <p/>
 *         <li>第一个要未,要把view转化成一个bitmap再画到下面的自定义view中去
 *         <p/>
 *         <p/>
 *         --TODO bitmap 的common 工程没有弄好，所以后面再来处理好了。
 */
public class BitmapActivity extends Activity {


    /*
    LinearLayout mParent;

    ImageView mFirstImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unit_4_bitmap);

        mParent = (LinearLayout) findViewById(R.id.ly_container);
        mFirstImg = (ImageView) findViewById(R.id.img_show_view);

        Bitmap bitmap = BitMapUtil.viewToBitmap(mFirstImg);
        SimpleView simpleView = new SimpleView(getApplicationContext(), bitmap);
        mParent.addView(simpleView);

    }


    class SimpleView extends View {


        Bitmap mBitmap;
        Paint mPaint;

        public SimpleView(Context context) {
            super(context);
        }

        public SimpleView(Context context, Bitmap bitmap) {
            super(context);
            this.mBitmap = bitmap;
            mPaint = new Paint();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, 100, 25, mPaint);
            }
        }
    }
*/
}
