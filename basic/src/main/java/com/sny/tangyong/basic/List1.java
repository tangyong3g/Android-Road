/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sny.tangyong.basic;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * 
 * 把一組字符顯示即可
 * 
 * @author tang
 *
 */
public class List1 extends ListActivity {

	public int mHeight;
	public int mWidth;
	public int mBar;
	public int mCount = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//設置全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//取消標題
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		getScreenInfo();

		setListAdapter(new MyAdapter(mStrings, this));
		getListView().setTextFilterEnabled(true);

	}

	private void getScreenInfo() {

		/** method one **/

		WindowManager wm = this.getWindowManager();
		mWidth = wm.getDefaultDisplay().getWidth();
		mHeight = wm.getDefaultDisplay().getHeight();

		Log.i("tyler.tang", "方法一:" + "width:" + mWidth + "\theight:\t" + mHeight);

		/** method two */

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels;     // 屏幕宽度（像素）
		int height = metric.heightPixels;   // 屏幕高度（像素）
		float density = metric.density;      // 屏幕密度（0.75 / 1.0 / 1.5）
		int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）

		Log.i("tyler.tang", "方法二:" + "width:" + width + "\theight:\t" + height + "\tdensity:\t" + density + "\tdensityDpi:\t" + densityDpi);

		getStatusBar();
		getTitleBar();
	}

	/**
	 * decorView是window中的最顶层view，可以从window中获取到decorView，
	 * 然后decorView有个getWindowVisibleDisplayFrame方法可以获取到程序显示的区域，包括标题栏，但不包括状态栏。 于是，我们就可以算出状态栏的高度了
	 * 
	 */
	private int getStatusBar() {

		/*
		Rect frame = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;

		return statusBarHeight;
		*/
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			sbar = getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.i("tyler.tang", "狀態的高度:\t" + sbar);

		mBar = sbar;
		return sbar;

	}

	/**
	 * 標題
	 */
	private void getTitleBar() {

		int contentTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
		//statusBarHeight是上面所求的状态栏的高度
		int titleBarHeight = contentTop - getStatusBar();

		Log.i("tyler.tang", "標題的高度:\t" + titleBarHeight);
	}

	/**
	 * 
	 * @author tang
	 *
	 */
	class MyAdapter extends BaseAdapter {

		String[] mArray;
		Context mContext;

		public MyAdapter(String[] array, Context context) {

			mArray = array;
			mContext = context;
		}

		@Override
		public int getCount() {

			return mArray != null ? mArray.length : 0;
		}
		@Override
		public Object getItem(int position) {

			return mArray != null ? mArray[position] : null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			MyView sv;
			if (convertView == null) {

				sv = new MyView(mContext, (position + 1) + ":\t" + mStrings[position]);

			} else {
				sv = (MyView) convertView;
				sv.setDis((position + 1) + ":\t" + mStrings[position]);
			}

			return sv;

		}
	}

	/**
	 * 
	 * @author tang
	 *
	 */
	class MyView extends LinearLayout {

		private String mDis;
		private TextView mTxDis;

		public void setDis(String dis) {
			this.mDis = dis;
			mTxDis.setText(dis);
		}

		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			super.onLayout(changed, l, t, r, b);
			Log.i("tyler.tang", "onLayout:\t" + l + ":\t" + t + ":\t" + r + ":\t" + b);
		};

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
		}

		public MyView(Context context, String dis) {
			super(context);
			this.mDis = dis;
			setOrientation(VERTICAL);

			AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			setLayoutParams(params);

			int height = (mHeight) / 30;

			AbsListView.LayoutParams txparams = new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT, height);

			mTxDis = new TextView(context);
			mTxDis.setText(dis);
			mTxDis.setLayoutParams(txparams);
			addView(mTxDis);

			Log.i("tyler.tang", "新的View" + this.toString()+"第:\t"+ (++mCount));
			

		}
	}

	private String[] mStrings = new String[] { "tangyong", "sany", "hello", "tangyong", "sany", "hello", "tangyong", "sany", "hello", "tangyong",
			"sany", "hello", "tangyong", "sany", "hello", "tangyong", "sany", "hello", "tangyong", "sany", "hello", "tangyong", "sany", "hello",
			"tangyong", "sany", "hello", "tangyong", "sany", "hello", "tangyong", "sany", "hello", "tangyong", "sany", "hello", "tangyong", "sany",
			"hello", "tangyong", "sany", "hello", "tangyong", "sany", "hello", "tangyong", "sany", "hello", "tangyong", "sany", "hello", "tangyong",
			"sany", "hello", "tangyong", "sany", "hello", "tangyong", "sany", "hello", "tangyong", "sany", "hello", "tangyong", "sany", "hello",
			"tangyong", "sany", "hello" };

}
