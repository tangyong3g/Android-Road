package com.sny.tangyong.basic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * 
 * @author tang
 * 
 */
public class CanvaseSaveRsView extends View {

	Paint mPaint;

	public CanvaseSaveRsView(Context context) {
		super(context);

		mPaint = new Paint();

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawRect(0, 120, 50, 20, mPaint);

		mPaint.setColor(Color.RED);
		canvas.drawCircle(100, 100, 20, mPaint);
		mPaint.setColor(Color.BLUE);

	}

}
