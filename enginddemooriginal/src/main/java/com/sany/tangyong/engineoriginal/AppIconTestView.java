package com.sany.tangyong.engineoriginal;

import java.util.ArrayList;
import java.util.List;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import com.go.gl.graphics.BitmapGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLView;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2012-9-6]
 */
public class AppIconTestView extends GLView {

	int mIconSize = 72; //CHECKSTYLE IGNORE
	final int mRow = 5;
	final int mCol = 4;

	final static float HALF = 0.5f;

	int mIconCount;
	public List<BitmapGLDrawable> mIcons;

	BitmapGLDrawable mBg;

	/** @formatter:off */
	Mode[] mModes = { Mode.SRC_OVER, Mode.DST_OVER, Mode.SRC_IN, Mode.DST_IN, Mode.SRC_OUT,
			Mode.DST_OUT, Mode.SRC_ATOP, Mode.DST_ATOP, Mode.MULTIPLY, null };
	/** @formatter:on */

	public AppIconTestView(Context context) {
		super(context);

		final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		int width = metrics.widthPixels;
		mIconSize = Math.round(width / (float) mCol * 0.6f);	//CHECKSTYLE IGNORE

		mIcons = loadAppsIcons(getResources(), getContext().getPackageManager());

		WallpaperManager manager = WallpaperManager.getInstance(context);
		Drawable d = manager.getDrawable();
		if (d instanceof BitmapDrawable) {
			mBg = new BitmapGLDrawable((BitmapDrawable) d);
		}

	}

	List<BitmapGLDrawable> loadAppsIcons(Resources res, PackageManager manager) {
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
		List<BitmapGLDrawable> icons = new ArrayList<BitmapGLDrawable>();
		mIconCount = apps.size();
		Canvas canvas = new Canvas();
		mIconCount = Math.min(mIconCount, mRow * mCol);
		for (int i = 0; i < mIconCount; ++i) {
			ResolveInfo info = apps.get(i);
			Drawable drawable = info.activityInfo.loadIcon(manager);
			Bitmap bitmap = Bitmap.createBitmap(mIconSize, mIconSize, Config.ARGB_8888);
			canvas.setBitmap(bitmap);
			drawable.setBounds(0, 0, mIconSize, mIconSize);
			drawable.draw(canvas);
			BitmapGLDrawable d = new BitmapGLDrawable(res, bitmap);
			icons.add(d);
			d.setColorFilter(0x7FFF0000, mModes[i % mModes.length]);	//CHECKSTYLE IGNORE
		}
		apps.clear();
		return icons;
	}

	long mStartTime = -1;
	long mLastTime;
	float mStartAngle;
	float mCurAngle;

	@Override
	protected void onDraw(GLCanvas canvas) {
		if (mBg != null) {
			//mBg.draw(canvas);
		}

		if (mStartTime == -1) {
			mStartTime = getDrawingTime();
			mLastTime = mStartTime;
			mStartAngle = mCurAngle;
		}

		long curTime = getDrawingTime();
		mCurAngle = mStartAngle + (float) ((curTime - mStartTime) * 360 / 3000); //CHECKSTYLE IGNORE
		//Log.d("DWM", "delta time=" + (curTime - lastTime));
		mLastTime = curTime;

		final int w = getWidth();
		final int h = getHeight();
		final float iconW = w / (float) mCol;
		final float padLeft = (iconW - mIconSize) / 2;
		final float iconH = h / (float) mRow;
		final float padTop = (iconH - mIconSize) / 2;
		canvas.translate(mTouchX - mTouchDownX, mTouchY - mTouchDownY);
		for (int i = 0; i < mRow * mCol; ++i) {
			float x = iconW * (i % mCol) + padLeft;
			float y = iconH * (i / mCol) + padTop;
			canvas.save();
			canvas.translate(x, y);
			//			canvas.rotate(mCurAngle, mIconSize * HALF, mIconSize * HALF);
			canvas.translate((float) Math.sin(Math.toRadians(mCurAngle + i * 7)) * mIconSize / 2, 0);
			mIcons.get(i).draw(canvas);
			canvas.restore();
		}
		if (curTime - mStartTime < 9000) { //CHECKSTYLE IGNORE
			invalidate();
		} else {
			invalidate();
		}

	}

	float mTouchX;
	float mTouchY;
	float mTouchDownX;
	float mTouchDownY;

	//	@Override
	//	public boolean onTouchEvent(MotionEvent event) {
	//		mTouchX = event.getX();
	//		mTouchY = event.getY();
	//		//    	Log.d("DWM", "onTouchEvent " + mTouchX + " " + mTouchY);
	//		if (event.getAction() == MotionEvent.ACTION_DOWN) {
	//			mStartTime = -1;
	//			mTouchDownX = mTouchX;
	//			mTouchDownY = mTouchY;
	//		}
	//		invalidate();
	//		return true;
	//	}

}