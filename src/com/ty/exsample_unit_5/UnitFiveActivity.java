package com.ty.exsample_unit_5;

import com.example.android_begin_gl_3d.R;
import com.ty.exsample_unit_4.AssetsActivity;
import com.ty.exsample_unit_4.FullScreenTest;
import com.ty.exsample_unit_4.SurfaceViewTest;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


/**
 * 
 * 本单元主要讲的是从Android 游戏开发宝典里面的知识
 * 
 * 
 * 
 * @author tangyong ty_sany@163.com
 *
 */
public class UnitFiveActivity extends ListActivity{
	
	String[] units = new String[] { "AssetManager", };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(this, R.layout.main_items,
				units));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		Class cls = null;
		switch (position) {
		case 0:
			cls = AssetsActivity.class;
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
