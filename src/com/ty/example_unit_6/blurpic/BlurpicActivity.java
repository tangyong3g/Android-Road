package com.ty.example_unit_6.blurpic;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.enrique.stackblur.StackBlurManager;
import com.ty.exsample.R;

/**
 * 
 * @author tang
 *
 */
public class BlurpicActivity extends Activity implements Callback, Runnable {

	Handler mHandler;
	Resources mRes;
	private FrameLayout mFrameLayout;
	private Bitmap mBlurBm;
	private static final int MSG_FINSHED = 1;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRes = getResources();
		mHandler = new Handler(this);
		mFrameLayout = new FrameLayout(this);
		setContentView(mFrameLayout);

		mContext = this;
		
		this.run();

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();

		switch (action) {
			case MotionEvent.ACTION_DOWN :

				break;
			case MotionEvent.ACTION_MOVE :

				break;
			case MotionEvent.ACTION_UP :

				break;

			default :
				break;
		}

		return true;
	}

	@Override
	public boolean handleMessage(Message msg) {
		
		
		ImageView imageView = new ImageView(mContext);
		Drawable drawable = new BitmapDrawable(mBlurBm);
		
		imageView.setBackgroundDrawable(drawable);

		mFrameLayout.addView(imageView);

		return true;
	}

	@Override
	public void run() {

		final int radius = 10;
		Bitmap originalBitmap = BitmapFactory.decodeResource(mRes, R.drawable.bg_one);

		StackBlurManager manager = new StackBlurManager(originalBitmap);
		mBlurBm = manager.processNatively(10);

		mHandler.sendEmptyMessage(MSG_FINSHED);
	}

}
