package com.ty.exsample_unit_4;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;


/**
 * 
 * @author tangyong
 *
 */
public class PowerConnectActivity extends Activity implements Callback{
	
	private String mState = "normal";
	private Handler mHandler ;
	private TextView view;
	private ConnectedReciver reciever;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		view = new TextView(this);
		setContentView(view);
		view.setText(mState);
		mHandler = new Handler(this);
		registerRe();
		sendMsg();
	}
	
	
	public void registerRe(){
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		
		reciever = new ConnectedReciver();
		
		registerReceiver(reciever, filter);
	}
	
	private void sendMsg (){
		
		Message msgTmp = new Message();
		mHandler.sendMessageDelayed(msgTmp, 1500);
		
	}
	
	private void updateTx(){
		view.setText(mState);
	}
	
	class ConnectedReciver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
			Log.i("test",""+action);
			if(action.equals(Intent.ACTION_POWER_CONNECTED)){
				
				mState = "connected";
			}
			if(action.equals(Intent.ACTION_POWER_DISCONNECTED)){
				
				mState = "disconnected";
			}
		}
	}

	
	@Override

	protected void onPause() {
		
		try {
			unregisterReceiver(reciever);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		super.onPause();
	}

	@Override
	public boolean handleMessage(Message msg) {
		sendMsg();
		updateTx();
		
		
		Log.i("test","更新来了.....");
		return true;
	}
	
	

}
