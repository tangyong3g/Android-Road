package com.ty.example_unit_6;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android_begin_gl_3d.R;
import com.ty.example_unit_6.seekbar.DockLineDialog;
import com.ty.exsample_unit_5.translate.TranslateActivity;

public class UnitSixActivity extends ListActivity {

	String[] units = new String[] { "可滑动的选择条", };

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
			showSeekBar();
			break;
		case 1:
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
	
	
	private void showSeekBar(){
		DockLineDialog dialog = new DockLineDialog(this,1,"Dialog title");
		dialog.show();
	}

	private void intentToActivity(Class cls) {
		if(cls==null)
			return;
		Intent intent = new Intent();
		intent.setClass(this, cls);

		startActivity(intent);
	}

}
