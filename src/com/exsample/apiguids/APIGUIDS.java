package com.exsample.apiguids;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.exsample.apiguids.animationgraphic.AnimationGrapihcs;
import com.exsample.apiguids.userinterface.UserInterfaceMain;
import com.ty.exsample.R;
import com.ty.exsample_unit_4.BaseViewActivity;
import com.ty.exsample_unit_4.DeviceInfomation;

/**
 * 
		*@Title:
		*@Description:
		*   
		*      from android api guids
		*
		*@Author:tangyong
		*@Since:2014-12-26
		*@Version:1.1.0
 */
public class APIGUIDS extends ListActivity {

	String[] units = new String[] { "UserInterface", "Animation and Graphics" };

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
				cls = UserInterfaceMain.class;
				break;
				
			case 1:
				
				cls = AnimationGrapihcs.class;
				break;

			case 23 :
				cls = DeviceInfomation.class;

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
		intent.setClass(this, BaseViewActivity.class);

		intent.putExtra(BaseViewActivity.BASE_FLAG, componentName);

		startActivity(intent);
	}

	private void intentToActivity(Class cls) {
		Intent intent = new Intent();
		intent.setClass(this, cls);

		startActivity(intent);
	}

}
