package com.ty.example_unit_3.libgdx;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android_begin_gl_3d.R;
import com.ty.example_unit_2.opengl_1.OpenGL1Activity;
import com.ty.example_unit_3.libgdx.loadmode.LoadModeActivity;
import com.ty.example_unit_3.libgdx.loadmode.MaterialActivity;
import com.ty.example_unit_3.libgdx.loadmode.SimpleLoadActivity;

/**
 * 
 * @author tangyong
 * 
 */
public class UnitThreeActivity extends ListActivity {

	String[] units = new String[] { "Material","loadModelSimple","Material" };

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
			cls = LoadModeActivity.class;
			break;
		case 1:
			cls = SimpleLoadActivity.class;
			break;
		case 2:
			cls = MaterialActivity.class;
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
