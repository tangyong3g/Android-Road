package com.ty.example_unit_2.opengl_2.sensormanager;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * 重力传感器
 * 
 * @author tangyong
 * 
 */
public class AccelerometerActivity extends Activity implements SensorEventListener{
	
	TextView view = null;
	SensorManager sensorManager ;
	Sensor sensor ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = new TextView(this);
		setContentView(view);
		
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//注册传感器
		
		sensorManager.registerListener
		(
				this,         //eventListener 
				sensor,       // sensor
				SensorManager.SENSOR_DELAY_GAME   //delay type
        );
		
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
			Log.i("tyler.tang","onSensorChanged");
			float x = 	event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			
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
			
			view.setText(sb.toString());
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	
	
	

}
