package com.ty.example_unit_2;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sny.tangyong.androiddemo.R;
import com.ty.example_unit_2.opengl_1.OpenGL1Activity;
import com.ty.example_unit_2.opengl_2.OpenGL2Activity;

/**
 * 
 * @author tangyong
 * 
 */
public class UnitTwoActivity extends ListActivity {

	String[] units = new String[] { "openGL1.x" ,"openGL2.x"};

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
			cls = OpenGL1Activity.class;
			break;
		case 1:
			cls = OpenGL2Activity.class;
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
