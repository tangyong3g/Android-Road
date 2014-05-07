package com.example.android_begin_gl_3d;

import com.example.android_begin_gl_3d.unit_7.Main;
import com.example.android_begin_gl_3d.unit_7.ShellEngineActivity;
import com.ty.example_unit_1.UnitOneActivity;
import com.ty.example_unit_3.libgdx.UnitThreeActivity;
import com.ty.example_unit_6.UnitSixActivity;
import com.ty.exsample_unit_4.UnitFourActivity;
import com.ty.exsample_unit_5.UnitFiveActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 
 * @author tangyong
 * 
 */
public class MainActivity extends ListActivity {

	String[] units = new String[] { "unit_1", "unit_2[OpenGL1.x/2.x]",
			"unit_3[LibGDX]", "unit_4[Android基本知识]", "unit_5[Android游戏开发案例]",
			"unit_6[重用组件]", "unit_7[Shell Engine]","unit_8[EffectJava]" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

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
			cls = UnitSixActivity.class;
			break;
		case 6:
			cls = Main.class;
			break;
		case 7:
			
			cls = EffectJavaActivity.class;

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
