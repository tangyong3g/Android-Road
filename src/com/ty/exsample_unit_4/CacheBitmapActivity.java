package com.ty.exsample_unit_4;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.example.android_begin_gl_3d.R;

/**
 * 
 * 1: 为什么要用双缓冲？
 * 		当一个动画争先显示时，程序又在改变它，前面的画面还没显示完，程序又要求重新绘制，这样屏幕就会不停闪烁。
 *      为了避免闪烁，可以使用双缓冲技术，将要处理的图片都放在内存中处理好过后，再将其显示到屏幕上
 * 
 * @author tang
 *
 */
public class CacheBitmapActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int width = 100;
		int height = 100;

		LayoutParams params = new LayoutParams(width, height);
		CacheView view = new CacheView(this, width, height);
		view.setLayoutParams(params);
		setContentView(view);

	}

	/**
	 * 
	 */
	class CacheView extends View implements Runnable {

		private Bitmap mCacheBmp;
		private Bitmap mBmp;
		private Paint mPaint;

		public CacheView(Context context, int width, int height) {
			super(context);

			mPaint = new Paint();
			mCacheBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

			Bitmap bmpDra = BitmapFactory.decodeResource(getResources(), R.drawable.ghxp);

			Paint paint = new Paint();
			Canvas canvas = new Canvas(mCacheBmp);

			canvas.drawBitmap(bmpDra, 0,0, paint);

		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			canvas.drawBitmap(mCacheBmp, 0, 0, mPaint);

		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				postInvalidate();
			}
		}
	}

}
