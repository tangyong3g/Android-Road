package com.ty.exsample_unit_4;

import android.app.Activity;
import android.mtp.MtpConstants;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.android_begin_gl_3d.R;

/**
 * 
 * 
 * 说明:
 * 
 * 本Demo用来说明Looper的用法和Handler的关系
 * 
 * 实现主线程给子线程发送消息，结果是在Log里面可以看到在子线程里面处理消息。
 * 
 * 
 * 
 * @author 师爷GBK[ty_sany@163.com] 2013-7-17
 * 
 * 
 * 
 */
public class LooperActivity extends Activity implements OnClickListener{

	public static final int MSG_MAIN_TO_SUB = 1;
	String TAG  = "LooperActivity"; 
	LooperThread mLooperThread;
	
	
	/**
	 * 代码来自于Android源码 Looper 里面有
	 *	
	 *  说明: 
	 *
	 * @author 师爷GBK[ty_sany@163.com]
	 * 2013-7-17
	 *
	 */
	class LooperThread extends Thread {

		Handler mHandler;

		@Override
		public void run() {
			Looper.prepare();
			
			//此时Hand与当前线程中的Looper绑定了。因为在此线程中完成的实例化
			mHandler = new Handler(){
				public void handleMessage(android.os.Message msg) {
					
					int what = msg.what;
					
					switch (what) {
					case MSG_MAIN_TO_SUB:
						
						Log.i(TAG,"从主线程发来了消息!");
						break;

					default:
						break;
					}
					
				};
			};
			Looper.loop();
		}
		
		public Handler getThreadHandler(){
			return mHandler;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.looper_layout);
		Button btn = (Button)findViewById(R.id.send_msg_id);
		btn.setOnClickListener(this);
		
		
		//开启线程
		mLooperThread = new LooperThread();
		mLooperThread.start();
	}

	@Override
	public void onClick(View v) {
		
		Message msg = new Message();
		msg.what = MSG_MAIN_TO_SUB;
		
		Handler mHandler = mLooperThread.getThreadHandler();
		mHandler.sendMessageDelayed(msg, 0);
	}
	

}
