package com.sny.tangyong.basic;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Window;
import android.view.WindowManager;


/**
 * 
 * 本Demo 说明全屏使用以及让屏幕一直亮着
 * 
 * @author tangyong
 * 
 *
 * @date 2013-5-14
 * 
 *
 */
public class FullScreenTest extends Activity{
	
	private WakeLock mWakeLock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		PowerManager mManager = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
		mWakeLock  =mManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "my lock");
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mWakeLock.acquire();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mWakeLock.release();
	}
	

}
