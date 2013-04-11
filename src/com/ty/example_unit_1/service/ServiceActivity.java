package com.ty.example_unit_1.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 
 * @author tangyong
 * 
 */
public class ServiceActivity extends Activity implements OnClickListener{
	
	private boolean mFlag = false; 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Button btn  = new Button(this);
		btn.setOnClickListener(this);
		setContentView(btn);
	}

	@Override
	public void onClick(View v) {
		Log.i("tyler.tang","线程编号:\t"+Thread.currentThread().getId());
		mFlag = !mFlag ;
		
		Intent intent = new Intent();
		intent.setClassName(this,MyService.class.getName());
		intent.putExtra("state", mFlag);
		
		startService(intent);
	}
	
}
