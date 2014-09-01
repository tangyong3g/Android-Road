package com.example.android_begin_gl_3d;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * 当前屏幕信息
 * 
 * @author tangyong
 * 
 */
public class ScreenInfo {

	private int mWidth;
	private int mHeight;

	public int getmWidth() {
		return mWidth;
	}

	public void setmWidth(int mWidth) {
		this.mWidth = mWidth;
	}

	public int getmHeight() {
		return mHeight;
	}

	public void setmHeight(int mHeight) {
		this.mHeight = mHeight;
	}


	public void init(Context context) {

		WindowManager w = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display d = w.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);

		mWidth = metrics.widthPixels;
		mHeight = metrics.heightPixels;

	}

}
