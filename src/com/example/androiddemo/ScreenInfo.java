package com.example.androiddemo;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * 当前屏幕信息
 * 
 * 
 * 怎么得到当前Activity的状态
 * 
 * @author tangyong
 * 
 */
public class ScreenInfo {

	//屏幕的宽
	private int mWidth;
	//高
	private int mHeight;
	
	//屏幕状态栏高度
	private int mStateBarHeight;
	
	//屏幕标题栏高度
	private int mTitleBarHeight;
	
	//屏幕导航栏高度
//	private int  
	

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

//		ActivityMonitor

	}

}
