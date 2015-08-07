package com.example.android_begin_gl_3d.unit_8;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sny.tangyong.androiddemo.R;

/**
 * 
 * @author tang
 *
 */
public class UnitEight extends ListActivity {

	String[] units = new String[] { "View的绘制过程" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setListAdapter(new ArrayAdapter<String>(this, R.layout.main_items, units));
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
			case 0 :
				cls = ViewRenderActivity.class;
				break;

			default :
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
