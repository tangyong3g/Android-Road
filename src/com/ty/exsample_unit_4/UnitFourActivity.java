package com.ty.exsample_unit_4;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android_begin_gl_3d.R;
import com.ty.exsample_unit_4.animation.AnimationActivity;

/**
 * 
 * 第四单元的内容主要有Android的基本知识
 * 
 * 
 * @author Z61
 * 
 */
public class UnitFourActivity extends ListActivity {

	String[] units = new String[] { "AssetManager", "SurfaceViewTest",
			"FullScreen", "Looper", "Animation", "Meminfo", "powerConnectd",
			"图形", "canvas", "blending", "bitmap", "Handler", "Layout",
			"View_save" };

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
		String componentName = null;
		switch (position) {
		case 0:
			cls = AssetsActivity.class;
			break;
		case 1:
			cls = SurfaceViewTest.class;
			break;
		case 2:
			cls = FullScreenTest.class;
			break;
		case 3:
			cls = LooperActivity.class;
			break;
		case 4:
			cls = AnimationActivity.class;
			break;
		case 5:
			cls = MeminfoActivity.class;
			break;
		case 6:

			cls = PowerConnectActivity.class;
			break;
		case 7:
			cls = GrapicActivity.class;
			break;
		case 8:
			cls = CanvasDemoActivity.class;

			break;
		case 9:

			cls = MomentTest.class;
			break;
		case 10:

			cls = BitmapActivity.class;
			break;

		case 11:

			cls = HandlerTestActivity.class;

			break;

		case 12:

			cls = LayoutActivity.class;

			break;
		case 13:

			componentName = "com.ty.exsample_unit_4.CanvaseSaveRsView";

			break;
		default:

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
