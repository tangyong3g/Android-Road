package com.sny.tangyong.basic;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;

/**
 * 
 * @author tang
 *
 */
public class CanvasSavelayerActivity extends Activity implements Callback {

	Handler mHander;
	private SimpleView view;
	private int index;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		view = new SimpleView(this);
		setContentView(view);

		mHander = new Handler(this);

		Thread t = new Thread() {
			public void run() {

				while (true) {
					index++;

					if (index % 20 == 0) {
						mHander.sendEmptyMessage(1);
					}

				}
			};
		};

		t.start();

	}

	class SimpleView extends View {

		private static final int LAYER_FLAGS = Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
				| Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
				| Canvas.CLIP_TO_LAYER_SAVE_FLAG;

		private Paint mPaint;
		RectF mRect;
		Xfermode model;

		public SimpleView(Context context) {
			super(context);
			setFocusable(true);

			mPaint = new Paint();
			mPaint.setAntiAlias(true);

			mRect = new RectF(0, 0, 800, 1024);
			model = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
		}

		@Override
		protected void onDraw(Canvas canvas) {

			canvas.drawColor(Color.WHITE);
			canvas.translate(10, 10);
			mPaint.setColor(Color.RED);
			canvas.drawCircle(75, 75, 75, mPaint);

			//			canvas.saveLayerAlpha(0, 0, 200, 200, 0x88, LAYER_FLAGS);
			//
			int sc = canvas.saveLayer(0, 0, 400, 400, null, Canvas.MATRIX_SAVE_FLAG
					| Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
					| Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
			//
			//			mPaint.setXfermode(model);

			mPaint.setColor(Color.BLUE);
			canvas.drawCircle(125, 125, 75, mPaint);
			//
			canvas.restoreToCount(sc);
		}

	}

	@Override
	public boolean handleMessage(Message msg) {

		view.invalidate();

		return false;
	}

}
