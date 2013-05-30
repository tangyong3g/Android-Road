package com.example.android_begin_gl_3d;

import com.ty.example_unit_1.UnitOneActivity;
import com.ty.example_unit_3.libgdx.UnitThreeActivity;
import com.ty.exsample_unit_4.UnitFourActivity;
import com.ty.exsample_unit_5.UnitFiveActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 
 * @author tangyong
 *
 */
public class MainActivity extends ListActivity {

	String[] units = new String[] { "unit_1", "unit_2---[OpenGL1.x/2.x]", "unit_3---[LibGDX]", "unit_4",
			"unit_5[Android游戏 开发案例]", "unit_6", "unit_7", };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setListAdapter(new ArrayAdapter<String>(this, R.layout.main_items,
				units));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Class cls = null;
		switch (position) {
		case 0:
			cls = UnitOneActivity.class;
			break;
		case 1:
			cls = com.ty.example_unit_2.UnitTwoActivity.class;
			break;
		case 2:
			cls = UnitThreeActivity.class;
			break;
		case 3:
			cls = UnitFourActivity.class;
			break;
		case 4:
			cls = UnitFiveActivity.class;
			break;
		case 5:
			intentToUnit(cls);
			break;

		default:
			break;
		}
		
		intentToUnit(cls);
	}

	private void intentToUnit(Class cls) {

		Intent intent = new Intent();
		intent.setClass(this, cls);

		startActivity(intent);
	}

}
