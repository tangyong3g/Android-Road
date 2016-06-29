package com.ty.example_unit_2.opengl_2.loadmodel;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * 
 * @author tangyong
 * 
 */
public class ModelControler implements SensorEventListener {

	LoadedObjectVertexOnly model;
	ModelView view;
	private float mCurValue;
	private float lastValue;
	private boolean flag;
	
	private boolean loadModelFinished  = false;

	public boolean isLoadModelFinished() {
		return loadModelFinished;
	}

	public void setLoadModelFinished(boolean loadModelFinished) {
		this.loadModelFinished = loadModelFinished;
	}

	public ModelControler(LoadedObjectVertexOnly model) {
		this.model = model;
	}

	public ModelControler(LoadedObjectVertexOnly model, ModelView view) {
		this.model = model;
		this.view = view;
	}

	public ModelControler() {
	}
	
	int index = 0;
	public static volatile float kFilteringFactor = (float)0.10;

	
	float temp = 0.0f;
	@Override
	public void onSensorChanged(SensorEvent event) {

		if(!loadModelFinished){
			return;
		}
		
		float x = event.values[0];
		final float y = event.values[1];
		final float z = event.values[2];
		
		Constant.y = x;
		Constant.z = x;
		
		mCurValue  = x;
		
		/*
		if (Math.abs(mCurValue - lastValue) > 0.032) {
			flag = true;
			avgRunnable(lastValue, mCurValue);
			lastValue = mCurValue;
		}*/
		
		temp =   (float) ((x * kFilteringFactor) + (temp * (1.0 - kFilteringFactor)));
		
		if(view!=null && view.getRender().lovo!=null){
			Runnable run = new Runnable() {
				@Override
				public void run() {
					view.getRender().lovo.senorRatio(temp);
				}
			};
			view.queueEvent(run);
		}
		flag = false;
	}
	
	private void avgRunnable(final float last , float current) {
		
		final float addValue = (current-last)/1.0f;
		for(int i = 0; i <= 1 ; i++){
			final int temp = i;
			Runnable run = new Runnable() {
				@Override
				public void run() {
					view.getRender().lovo.senorRatio(last+addValue*temp);
				}
			};
			view.queueEvent(run);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

}
