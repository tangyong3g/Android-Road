package com.sny.tangyong.basic;

import android.app.ListActivity;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 
 * 属性动画
 * 
 * @author tangyong
 * 
 */
public class AttriAnimationActivity extends ListActivity {

	String[] units = new String[] { "XML属性动画", "布局动画", "", "" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setListAdapter(new ArrayAdapter<String>(this, R.layout.basic_item, units));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		Class cls = null;
		switch (position) {
			case 0 :
				cls = AttriAnimationXMLActivity.class;
				break;
			case 1 :

				cls = AttriAnimationLayoutActivity.class;
				break;
			case 2 :
				break;
			case 3 :
				break;
			case 4 :
				break;
		}
		if (cls != null) {
			intentToActivity(cls);
		}
	}

	private void intentToActivity(Class cls) {
		Intent intent = new Intent();
		intent.setClass(this, cls);
		startActivity(intent);
	}


	public static class ThreadTest implements Runnable{

		@Override
		public void run() {
			android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		}
	}

}
