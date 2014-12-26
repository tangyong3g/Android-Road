package com.exsample.apiguids.userinterface;

import com.ty.exsample.R;
import com.ty.exsample_unit_4.AssetsActivity;
import com.ty.exsample_unit_4.AttriAnimationActivity;
import com.ty.exsample_unit_4.BaseViewActivity;
import com.ty.exsample_unit_4.BelendingActivity;
import com.ty.exsample_unit_4.BitmapActivity;
import com.ty.exsample_unit_4.CacheBitmapActivity;
import com.ty.exsample_unit_4.CanvasDemoActivity;
import com.ty.exsample_unit_4.ConcurrentModificationExceptionActivity;
import com.ty.exsample_unit_4.DeviceInfomation;
import com.ty.exsample_unit_4.DipTestActivity;
import com.ty.exsample_unit_4.FullScreenTest;
import com.ty.exsample_unit_4.GrapicActivity;
import com.ty.exsample_unit_4.HandlerTestActivity;
import com.ty.exsample_unit_4.LayoutActivity;
import com.ty.exsample_unit_4.List1;
import com.ty.exsample_unit_4.LooperActivity;
import com.ty.exsample_unit_4.MeminfoActivity;
import com.ty.exsample_unit_4.MomentTest;
import com.ty.exsample_unit_4.PowerConnectActivity;
import com.ty.exsample_unit_4.SurfaceViewTest;
import com.ty.exsample_unit_4.SyncTaskActivity;
import com.ty.exsample_unit_4.ViewCycleTestActivity;
import com.ty.exsample_unit_4.WallpaperManagerTest;
import com.ty.exsample_unit_4.animation.AnimationActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 
		*@Title:
		*@Description:
		*@Author:tangyong
		*@Since:2014-12-26
		*@Version:1.1.0
 */
public class UserInterfaceMain extends ListActivity {

	String[] units = new String[] { "Linear Layout", "Relative Layout", "List View" };

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
