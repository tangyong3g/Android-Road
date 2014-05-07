package com.ty.exsample_unit_4;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * 这个demo要证明的有
 * 
 * <li> {@link DisplayMetrics} 用法
 * 
 * 				density:           dp/px  dp和像素的比率　
 * 				densityDpi         每英寸有多少个像素　
 * 				heightPixels       像素的高度
 * 				widthPixels        宽度用像素表示
 * 				xdip               
 * 				ydip
 * 
 * 			标题栏 + 状态栏 + 下面View可显示的高度　＝　heightPixels 　以HTC　Ｇ11为例 就是 800 
 * <li>
 * 
 * 
 * 
 * @author tang
 *
 */
public class DipTestActivity extends Activity implements Callback {
	
	private FrameLayout mContainer;
	
	private Handler mHander;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mHander = new Handler(this);

		//設置全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//取消標題
		requestWindowFeature(Window.FEATURE_NO_TITLE);


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
		
		//TODO 这里有问题，不知道为什么　
//		rs.append("标题栏的高度");
//		rs.append("\t");
//		rs.append(titleBarHeight);
//		rs.append("\n");

		return rs.toString();
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
		protected void dispatchDraw(Canvas canvas) {
			super.dispatchDraw(canvas);

			mPaint.setColor(Color.RED);
			
			int height = canvas.getHeight();
			int width = canvas.getWidth();

			canvas.drawRect(0, 0, width, height, mPaint);
			
			mPaint.setColor(Color.WHITE);
			mPaint.setStrokeWidth(1);
			
			for(int i = 0 ; i < 10 ; i++){
				
				float startX =  0 ;
				float startY  = i * 100;
				float stopX =   startX + width ;
				float stopY =   i * 100 ;
				
				canvas.drawLine(startX, startY-3, stopX, stopY-3, mPaint);
				
				canvas.drawText(i+"",startX+5, startY+15, mPaint);
			}

		}

	}

	@Override
	public boolean handleMessage(Message msg) {
		int what = msg.what;
		switch (what) {
			case 1 :
				
				String value  = String.valueOf(msg.obj);
				
				/*构建显示信息TXView*/
				final	ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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
