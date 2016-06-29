package com.ty.exsample_unit_5;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sny.tangyong.androiddemo.R;
import com.ty.exsample_unit_5.shader.ShaderActivity;
import com.ty.exsample_unit_5.shape.CireSlideActivity;
import com.ty.exsample_unit_5.translate.TranslateActivity;


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
	
	String[] units = new String[] { "Translation[基本变化]", "顶点着色器妙用","3D基本图形的构建"};

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
			cls = TranslateActivity.class;
			break;
		case 1:
			cls = ShaderActivity.class;
			break;
		case 2:
//			cls = CireSlideActivity.class;
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
