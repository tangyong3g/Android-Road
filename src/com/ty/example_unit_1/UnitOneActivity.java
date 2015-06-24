package com.ty.example_unit_1;

import java.io.File;
import java.util.List;

import com.ty.exsample.R;
import com.ty.example_unit_1.coordiatesystem.CoordiateSystemActivity;
import com.ty.example_unit_1.cube.CubeActivity;
import com.ty.example_unit_1.service.ServiceActivity;
import com.ty.util.MD5Util;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class UnitOneActivity extends ListActivity {
	
	String[] units = new String[] { "viewPort" ,"isoMap","drawMethod","SpritchMove","Canvas","Animation","Cube","CoordiateSystem","Service"};

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
		case 8:
			cls = ServiceActivity.class;
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
		getApplicationMD5(getApplicationContext());
	}
	

	/**
	 * <br>
	 * 功能简述:获取应用程序的md5 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	public String getApplicationMD5(Context context) {
		String md5String = null;
		Intent intent = new Intent("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		intent.addCategory("android.intent.category.DEFAULT");

		List<ResolveInfo> pkgAppsList = context.getPackageManager().queryIntentActivities(intent, 0);
		try {
			for (ResolveInfo info : pkgAppsList) {

				String pkgNameString = "com.gtp.nextlauncher";

				if (info.activityInfo.packageName.equals(pkgNameString)) {
					File file = new File(info.activityInfo.applicationInfo.sourceDir);
					md5String = MD5Util.getFileMD5String(file);
				}
			}
		} catch (Exception e) {
			md5String = null;
		}

		Log.i("test", "MD5 ID " + md5String);
		return md5String;
	}


}
