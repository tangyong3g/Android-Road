package com.sny.tangyong.basic;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import com.sny.tangyong.basic.R;

/**
 * 
 * @author tangyong
 * 
 */
public class MeminfoActivity extends Activity {
	private Button mBtnStart;
	private Button mBtnTest;
	private Button mBtnJHRel;
	private Button mBtnGC;
	private Button mBtnNHRel;
	private Button mBtnDecode;
	private Button mBtnRecycle;
	private Jni mJni = new Jni();

	private List l = new ArrayList();
	private List n = new ArrayList();
	private List m = new ArrayList();

	private OnClickListener mListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnStart:
				l.add(new byte[1024 * 1024]);
				break;
			case R.id.btnJHRel:
				l.clear();
				break;
			case R.id.btnTest:
//				m.add(mJni.malloc());
				break;
			case R.id.btnNHRel:
				for (Object obj : m) {
//					mJni.free((Integer) obj);
				}
				m.clear();
				break;
			case R.id.btnDecode:
				try {
					n.add(BitmapFactory.decodeStream(getAssets()
							.open("IMG.jpg")));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case R.id.btnRecycle:
				for (Object obj : n) {
					Bitmap bitmap = (Bitmap) obj;
					bitmap.recycle();
				}
				n.clear();
				break;
			case R.id.btnGC:
				causeGC3();
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unit4_meminfo);
		mBtnStart = (Button) findViewById(R.id.btnStart);
		mBtnTest = (Button) findViewById(R.id.btnTest);
		mBtnJHRel = (Button) findViewById(R.id.btnJHRel);
		mBtnGC = (Button) findViewById(R.id.btnGC);
		mBtnNHRel = (Button) findViewById(R.id.btnNHRel);
		mBtnDecode = (Button) findViewById(R.id.btnDecode);
		mBtnRecycle = (Button) findViewById(R.id.btnRecycle);
		mBtnStart.setOnClickListener(mListener);
		mBtnTest.setOnClickListener(mListener);
		mBtnJHRel.setOnClickListener(mListener);
		mBtnNHRel.setOnClickListener(mListener);
		mBtnGC.setOnClickListener(mListener);
		mBtnDecode.setOnClickListener(mListener);
		mBtnRecycle.setOnClickListener(mListener);
	}

	private void causeGC3() {

		Log.i("causegc", "Send Signal");

		int myPid = android.os.Process.myPid();

		int signal = android.os.Process.SIGNAL_USR1;

		try {

			Class procClass = Class.forName("android.os.Process");
			Class parameterTypes[] = new Class[] { int.class, int.class };
			Method sendCauseGCSignal = procClass.getMethod("sendSignal",parameterTypes);

			Object arglist[] = new Object[2];

			arglist[0] = myPid;
			arglist[1] = signal;

			if (sendCauseGCSignal != null) {

				sendCauseGCSignal.invoke(null, arglist);

			}

		} catch (ClassNotFoundException e) {

			e.printStackTrace();

		} catch (SecurityException e) {

			e.printStackTrace();

		} catch (IllegalArgumentException e) {

			e.printStackTrace();

		} catch (IllegalAccessException e) {

			e.printStackTrace();

		} catch (InvocationTargetException e) {

			e.printStackTrace();

		} catch (NoSuchMethodException e) {

			e.printStackTrace();

		}

	}

}
