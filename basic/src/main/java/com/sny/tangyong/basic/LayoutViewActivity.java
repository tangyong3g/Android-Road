package com.sny.tangyong.basic;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

/**
 * 
 * @author tang
 * 
 * 
 */
public class LayoutViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(initSimpleView());
	}

	public View initView() {

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.basic_item, null);

		return view;
	}

	public View initSimpleView() {

		SimpleView view = new SimpleView(this);
		return view;
	}

	/**
	 * 自定义View
	 * 
	 * @author tang
	 * 
	 */
	class SimpleView extends View {

		public SimpleView(Context context) {
			super(context);
		}

		@Override
		protected void onFinishInflate() {
			Log.i("cycle", "onFinishInflate");
			super.onFinishInflate();
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			Log.i("cycle", "onMeasure");
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right,
				int bottom) {
			Log.i("cycle", "onLayout");

			super.onLayout(changed, left, top, right, bottom);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			Log.i("cycle", "onSizeChanged");
			super.onSizeChanged(w, h, oldw, oldh);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			Log.i("cycle", "onDraw");
			super.onDraw(canvas);
		}

		@Override
		protected void onFocusChanged(boolean gainFocus, int direction,
				Rect previouslyFocusedRect) {
			Log.i("cycle", "onFocusChanged");
			super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

		}

		@Override
		public void onWindowFocusChanged(boolean hasWindowFocus) {
			Log.i("cycle", "onWindowFocusChanged");
			super.onWindowFocusChanged(hasWindowFocus);
		}

	}

}
