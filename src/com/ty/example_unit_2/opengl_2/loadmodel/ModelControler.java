package com.ty.example_unit_2.opengl_2.loadmodel;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.opengl.GLSurfaceView;
import android.util.Log;


/**
 * 
 * @author tangyong
 *
 */
public class ModelControler implements SensorEventListener{

	LoadedObjectVertexOnly model;
	ModelView view ;
	private float mCurValue;
	private float lastValue;
	private boolean flag;
	
	
	public ModelControler(LoadedObjectVertexOnly model) {
		this.model = model;
	}
	
	public ModelControler(LoadedObjectVertexOnly model,ModelView view) {
		this.model = model;
		this.view = view;
	}
	
	public ModelControler() {
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		
		float x = 	event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		
	
		Constant.y = x;
		Constant.z = x;
		
		StringBuffer sb  = new StringBuffer();
		
		sb.append("X方向加速度:\t");
		sb.append(x);
		sb.append("\n");
		sb.append("Y方向加速度:\t");
		sb.append(y);
		sb.append("\n");
		sb.append("Z方向加速度:\t");
		sb.append(z);
		sb.append("\n");
		
		
		
		view.queueEvent(new Runnable() {
			
			@Override
			public void run() {
//					view.getRender().lovo.senorRatio();
			}
		});
		
		/**/
		mCurValue = x;
		if(Math.abs(mCurValue - lastValue) > 0.02){
			Constant.x = x;
			flag = true;
		}
		lastValue = mCurValue;
		
		if(flag){
//			LoadedObjectVertexOnly.angleZ = (float)-180*x/9.8f;	
		}
		
		Log.i("tyler.tang",sb.toString());
		flag = false;
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	
}
