package com.ty.example_unit_1;

import com.example.android_begin_gl_3d.R;
import com.ty.example_unit_1.coordiatesystem.CoordiateSystemActivity;
import com.ty.example_unit_1.cube.CubeActivity;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class UnitOneActivity extends ListActivity {
	
	String[] units = new String[] { "viewPort" ,"isoMap","drawMethod","SpritchMove","Canvas","Animation","Cube","CoordiateSystem"};

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
			cls = ViewPortActivity.class;
			break;
		case 1:
			cls = ISOMapActivity.class;
			break;
		case 2:
			cls = DrawMethodActivity.class;
			break;
		case 3:
			cls = SpritchMoveActivity.class;
			break;
		case 4:
			cls = CanavsActivity.class;
			break;
		case 5:
			cls = AnimationActivity.class;
			break;
		case 6:
			cls = CubeActivity.class;
			break;
		case 7:
			cls = CoordiateSystemActivity.class;
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
