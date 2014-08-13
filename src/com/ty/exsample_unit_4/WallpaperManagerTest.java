package com.ty.exsample_unit_4;

import android.app.Activity;
import android.app.WallpaperManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.ty.exsample.R;

/**
 * 
 * 要清楚的问题有:
 * 
 *    wallpaperManger 用法 
 * 1: 设置壁纸
 * 2: 得到壁纸应该的尺寸.
 * 
 * 
 * 
 * 
 * @author tang
 *
 */
public class WallpaperManagerTest extends Activity implements OnClickListener {

	LinearLayout mParament = null;
	WallpaperManager mWallpaperManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		mParament = new LinearLayout(this);
		mParament.setOrientation(LinearLayout.VERTICAL);

		Button btn = new Button(this);
		btn.setText("setWallpaper");
		btn.setLayoutParams(initLayout());

		btn.setOnClickListener(this);

		mParament.addView(btn);

		setContentView(mParament);

	}

	@Override
	public void onClick(View v) {

		mWallpaperManager = WallpaperManager.getInstance(this);
		
		//设置壁纸
		try {
			mWallpaperManager.setResource(R.drawable.cube_simple);
		} catch (Exception e) {
			e.printStackTrace();
		}

		StringBuffer sb = new StringBuffer();
		
		
		
		//得到尺寸
		int minHeight = mWallpaperManager.getDesiredMinimumHeight();
		int minWidth = mWallpaperManager.getDesiredMinimumWidth();
		
		
		sb.append("\n");
		sb.append("inHeight");
		sb.append("\n");
		sb.append(minHeight);
		sb.append("\n");
		sb.append("minWidth");
		sb.append("\n");
		sb.append(minWidth);
		

		TextView tx = new TextView(this);
		tx.setText(sb);
		tx.setLayoutParams(initLayout());
		mParament.addView(tx);
		
		
		System.gc();
		
		//的到壁纸
		
	}

	public ViewGroup.LayoutParams initLayout() {
		
		int width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		int height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		ViewGroup.LayoutParams params = new LayoutParams(width, height);
		
		return params;
	}

}

