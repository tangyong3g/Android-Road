package com.sny.tangyong.basic;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import java.util.Random;

/**
 * 
 * <li>
 * 
 * 04-23 19:50:08.944: I/tyler.tang(14694): 初始化!
 04-23 19:50:08.954: I/tyler.tang(14694): onMeasure: 完成！
04-23 19:50:08.964: I/tyler.tang(14694): onSizeChanged: 完成！
04-23 19:50:08.964: I/tyler.tang(14694): onLayout 完成！
04-23 19:50:08.974: I/tyler.tang(14694): onMeasure: 完成！
04-23 19:50:08.974: I/tyler.tang(14694): onLayout 完成！
04-23 19:50:08.984: I/tyler.tang(14694): onDraw: 完成！
04-23 19:50:08.984: I/tyler.tang(14694): dispatchDraw: 完成！
<li>
 * 
 * 
 * 不知道爲什麼下面有些地方沒有
 * 
 * @author tang
 *
 */
public class ViewCycleTestActivity extends Activity implements OnClickListener {

	public int mWidth;
	public int mHeight;

	public static final int ROW = 10;
	public static final int COL = 10;

	public int mRowUnit;
	public int mColUnit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//設置全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//取消標題
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		DisplayMetrics metri = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metri);

		mWidth = metri.widthPixels;
		mHeight = metri.heightPixels;

		
		mRowUnit = mHeight / ROW;
		mColUnit = mWidth / COL;

		SimpleView view = new SimpleView(this);
		setContentView(view);
		view.setOnClickListener(this);
	}

	/**
	 * 
	 * @author tang
	 *
	 */
	class SimpleView extends View {

		Paint mPaint;
		Rect mTemp = new Rect();
		int[] mColorArray = new int[] { Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.GRAY, Color.MAGENTA, Color.CYAN };
		Random mRandom = new Random();

		public SimpleView(Context context) {
			super(context);
			mPaint = new Paint();
			Log.i("tyler.tang", "初始化!");
		}

		@Override
		protected void onFinishInflate() {
			super.onFinishInflate();
			Log.i("tyler.tang", "onFinishInflate 完成！");
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			super.onLayout(changed, left, top, right, bottom);

			Log.i("tyler.tang", "onLayout 完成！" + "參數:\t" + left + ":\t" + top + ":\t" + right + ":\t" + bottom);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			Log.i("tyler.tang", "onMeasure: 完成！");
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			Log.i("tyler.tang", "onSizeChanged: 完成！");
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {

			int action = event.getAction();

			switch (action) {
				case MotionEvent.ACTION_DOWN :

//					this.invalidate();

					break;

				default :
					break;
			}

			return super.onTouchEvent(event);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			Log.i("tyler.tang", "onDraw: 完成！" + "col:\t" + COL + "row:\t" + ROW + "colUnit:\t" + mColUnit + "rowUnit:\t" + mRowUnit);

			mPaint.setColor(Color.RED);

			for (int i = 0; i < ROW; i++) {

				for (int j = 0; j < COL; j++) {

					int left = j * mColUnit;
					int top = i * mRowUnit;
					int right = left + mColUnit;
					int bottom = top + mRowUnit;

					mTemp.set(left, top, right, bottom);

					Log.i("tyler.tang", "繪製Params:\t" + mTemp.toShortString());

					int colorIndex = mRandom.nextInt(mColorArray.length - 1);
					int color = mColorArray[colorIndex];

					mPaint.setColor(color);

					canvas.drawRect(mTemp, mPaint);
				}
			}
			
			if(mAmi){
				this.invalidate();
			}
		}

		@Override
		protected void dispatchDraw(Canvas canvas) {
			super.dispatchDraw(canvas);

			Log.i("tyler.tang", "dispatchDraw: 完成！");
		}

	}

	@Override
	public void onClick(View v) {
		
		
		
		
	}
	
	boolean mAmi = false;
	
	int i = 0; 

	private void startAnimation() {
		mAmi = true;
		
		
	}

}
