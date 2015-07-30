package com.sny.tangyong.basic;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * 这个demo要证明的有
 * 
 * <li> {@link DisplayMetrics} 用法
 * 
 * 				density:           px/dip  dp和像素的比率　
 * 				densityDpi         每英寸有多少个像素　
 * 				heightPixels       像素的高度
 * 				widthPixels        宽度用像素表示
 * 				xdip               
 * 				ydip
 * 
 * 			标题栏 + 状态栏 + 下面View可显示的高度　＝　heightPixels 　以HTC　Ｇ11为例 就是 800
 * 			注意状态栏的高度可能得到的是不正确的值，在平板上面更是。 
 * <li>
 * 
 * 
 * 				问题一：如果不同的设备都搞成同样的 dip数 是否都是一样的大小呢?  不一样
 * 				问题二: 标题栏，状态栏,导航栏，高度
 * 
 * @author tang
 *
 */
public class DipTestActivity extends Activity implements Callback {

	private FrameLayout mContainer;
	private Handler mHander;
	private float mDensity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHander = new Handler(this);

		//設置全屏
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//取消標題
//		requestWindowFeature(Window.FEATURE_NO_TITLE);

		/*构建自定义的View*/
		DipView dipview = new DipView(this);
		mContainer = new FrameLayout(this);

		mContainer.addView(dipview);

		mContainer.post(new Runnable() {

			@Override
			public void run() {

				String info = showDisplayMetricsInfo();

				Message msg = new Message();
				msg.obj = info;

				msg.what = 1;

				mHander.sendMessage(msg);

			}
		});

		setContentView(mContainer);
	}
	
	
	/**
	 * 获取屏幕的高度，包括状态栏，操作栏
	 * @param context
	 * @return
	 */
	public static int getRealScreentHeight(Context context) {
		int heightPixels;
		WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display d = w.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);
		// since SDK_INT = 1;  
		heightPixels = metrics.heightPixels;
		// includes window decorations (statusbar bar/navigation bar)  
		if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
			try {
				heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
			} catch (Exception e) {
			}
		}
		// includes window decorations (statusbar bar/navigation bar)  
		else if (Build.VERSION.SDK_INT >= 17) {
			try {
				android.graphics.Point realSize = new android.graphics.Point();
				Display.class.getMethod("getRealSize", android.graphics.Point.class).invoke(d,
						realSize);
				heightPixels = realSize.y;
			} catch (Exception e) {
			}
		}
		return heightPixels;
	}
	
	/**
	 * 在运行时获取此时系统的底部操作栏是否透明
	 * @return
	 */
	public static boolean isNavigationTransparent(Context context) {
		int id = context.getResources().getIdentifier("config_enableTranslucentDecor", "bool",
				"android");
		if (id == 0) {
			return false;
		} else {
			return context.getResources().getBoolean(id);
		}
	}

	private void drawGreenDist(Canvas canvas, Paint paint) {

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		float density = displayMetrics.density;

		float px = 200 * density;

		Log.i("tyler.tang", "px的值是:\t" + px);

		paint.setColor(Color.GREEN);

		float left = 0;
		float top = 300;
		float right = left + px;
		float bottom = top + px;

		canvas.drawRect(left, top, right, bottom, paint);

		paint.setColor(Color.BLACK);
		String display = "left:" + left + "\t top" + top + "\t right:" + right + "\t bottom:" + bottom + "\t px:" + px;
		canvas.drawText(display, left, top, paint);

	}

	private String showDisplayMetricsInfo() {

		StringBuffer rs = new StringBuffer();

		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		
	
		//区域二 宽度相同，高度和view的高度加上标题栏的高度
		View frameLayout = getWindow().findViewById(Window.ID_ANDROID_CONTENT);
		Rect outRect = new Rect();
		frameLayout.getWindowVisibleDisplayFrame(outRect);

		Rect drawoutRect = new Rect();
		frameLayout.getDrawingRect(drawoutRect);

		//状态栏的高度 note: 这种方式得到状态栏高度不能在oncreate的时候调用不然无法得到。
		Rect frame = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		int statusBarHeight = frame.top;

		/*标题栏的高度*/
		int contentTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
		int titleBarHeight = contentTop - statusBarHeight;

		float density = displayMetrics.density;
		float densityDpi = displayMetrics.densityDpi;

		float heightPixels = displayMetrics.heightPixels;
		float widthPixels = displayMetrics.widthPixels;

		float xdip = displayMetrics.xdpi;
		float ydip = displayMetrics.ydpi;

		rs.append("density:");
		rs.append("\t");
		rs.append(density);
		rs.append("\n");

		rs.append("densityDpi:");
		rs.append("\t");
		rs.append(densityDpi);
		rs.append("\n");

		rs.append("heightPixels:");
		rs.append("\t");
		rs.append(heightPixels);
		rs.append("\n");

		rs.append("widthPixels:");
		rs.append("\t");
		rs.append(widthPixels);
		rs.append("\n");

		rs.append("xdip:");
		rs.append("\t");
		rs.append(xdip);
		rs.append("\n");

		rs.append("ydip:\t");
		rs.append(ydip);
		rs.append("\n");

		rs.append("frameLayoutHeight");
		rs.append("\t");
		rs.append(outRect.toShortString());
		rs.append("\n");

		rs.append("frameLayoutHeightDrawing");
		rs.append("\t");
		rs.append(drawoutRect.toShortString());
		rs.append("\n");

		rs.append("状态栏的高度");
		rs.append("\t");
		rs.append(statusBarHeight);
		rs.append("\n");

		rs.append("状态栏的高度方法二:");
		rs.append("\t");
		rs.append(getStateBar());
		rs.append("\n");

		
		rs.append("底部操作栏是否透明:\t");
		rs.append("\t");
		rs.append(isNavigationTransparent(DipTestActivity.this));
		rs.append("\n");
		
		
		rs.append("通过反射得到的完整尺寸");
		rs.append("\t");
		rs.append(getRealScreentHeight(DipTestActivity.this));
		rs.append("\n");
		
		
		/** 标题栏 **/
		rs.append("标题栏寸");
		rs.append("\t");
		rs.append(outRect.bottom -drawoutRect.bottom );
		rs.append("\n");
		
		
		/** 导航栏*/
		rs.append("导航栏寸");
		rs.append("\t");
		rs.append(getRealScreentHeight(DipTestActivity.this) - outRect.bottom);
		rs.append("\n");
		

		//TODO 这里有问题，不知道为什么　
		//		rs.append("标题栏的高度");
		//		rs.append("\t");
		//		rs.append(titleBarHeight);
		//		rs.append("\n");

		return rs.toString();
	}

	/**
	 * 得到状态栏的高度
	 * 
	 * @return float 
	 */
	private float getStateBar() {

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

		return sbar;
	}

	/**
	 * 
	 * @author tang
	 *
	 */
	class DipView extends View {

		Paint mPaint;

		public DipView(Context context) {
			super(context);

			mPaint = new Paint();
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			
			Log.i("tyler.tang", showDisplayMetricsInfo());

			
		}

		@Override
		protected void dispatchDraw(Canvas canvas) {
			super.dispatchDraw(canvas);

			mPaint.setColor(Color.RED);

			int height = canvas.getHeight();
			int width = canvas.getWidth();

			canvas.drawRect(0, 0, width, height, mPaint);

			mPaint.setColor(Color.WHITE);
			mPaint.setStrokeWidth(1);
			
			
			String tx = "[ height: "+height + " width: "+width +" ]";
			

			for (int i = 0; i < 10; i++) {

				float startX = 0;
				float startY = i * 100;
				float stopX = startX + width;
				float stopY = i * 100;

				canvas.drawLine(startX, startY - 3, stopX, stopY - 3, mPaint);

				canvas.drawText(i + "", startX + 5, startY + 15, mPaint);
			}

			//绘制一个为 200 dip * 200 dip的绿色区
//			drawGreenDist(canvas, mPaint);
			
			canvas.drawText(tx, 0, 300+15, mPaint);
		}

	}

	@Override
	public boolean handleMessage(Message msg) {
		int what = msg.what;
		switch (what) {
			case 1 :

				String value = String.valueOf(msg.obj);

				/*构建显示信息TXView*/
				final LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				final TextView view = new TextView(this);

				view.setText(value);

				mContainer.addView(view);

				break;

			default :
				break;
		}
		return false;
	}

}
