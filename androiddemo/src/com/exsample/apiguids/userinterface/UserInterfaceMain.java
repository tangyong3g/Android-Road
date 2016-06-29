package com.exsample.apiguids.userinterface;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sny.tangyong.androiddemo.R;

/**
 * 
		*@Title:
		*@Description:
		*@Author:tangyong
		*@Since:2014-12-26
		*@Version:1.1.0
 */
public class UserInterfaceMain extends ListActivity {

	String[] units = new String[] { "Linear Layout", "Relative Layout", "List View", "Grid View" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(this, R.layout.main_items, units));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		Class cls = null;
		String componentName = null;

		switch (position) {

			case 0 :
				cls = LinearLayoutTest.class;
				break;

			case 1 :
				cls = RalativeLayoutTest.class;
				break;

			case 2 :

				cls = ListViewTest.class;

				break;
			case 3 :

				cls = GridViewTest.class;

				break;
			default :
				break;
		}
		if (cls != null) {
			intentToActivity(cls);
		}
		if (componentName != null) {
			intentToView(componentName);
		}
	}
	private void intentToView(String componentName) {
		Intent intent = new Intent();


		startActivity(intent);
	}

	private void intentToActivity(Class cls) {
		Intent intent = new Intent();
		intent.setClass(this, cls);

		startActivity(intent);
	}

}
