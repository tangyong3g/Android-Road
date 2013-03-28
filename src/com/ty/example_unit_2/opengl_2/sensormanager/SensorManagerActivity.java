package com.ty.example_unit_2.opengl_2.sensormanager;

import com.example.android_begin_gl_3d.R;
import com.ty.example_unit_2.opengl_2.loadmodel.LoadModelActivity;
import com.ty.example_unit_2.opengl_2.meshcube.ＭeshCubeActivity;
import com.ty.example_unit_2.opengl_2.shading.ShadingLanguageActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 此事例子主要内容是传感器
 * 
 * 加速度传感器 
 * 磁场传感器 
 * 光传感器 
 * 温度传感器 
 * 接近传感器 
 * 姿态传感器
 * 
 * 
 * @author tangyong
 * 
 */
public class SensorManagerActivity extends ListActivity {

	String[] units = new String[] { "accelerometer" };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setListAdapter(new ArrayAdapter<String>(this, R.layout.main_items,units));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		Class cls = null;
		switch (position) {
		case 0:
			cls = AccelerometerActivity.class;
			break;
		case 1:
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			break;
		case 5:
			break;
		case 6:
			break;
		case 7:
			break;
		default:
			break;
		}
		intentToActivity(cls);
	}

	private void intentToActivity(Class cls) {
		Intent intent = new Intent();
		intent.setClass(this, cls);

		startActivity(intent);
	}
	
	

}
