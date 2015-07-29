package com.sny.tangyong.basic;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class CanvasDemoActivity extends Activity {

	GameView mGameView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGameView = new GameView(this);
		
		setContentView(mGameView);

	}

	/**
	 * 接口
	 * @author tang
	 *
	 */
	public interface DrawGrapic {

		public void draw(Canvas canvas);

	}

	/**
	 * 
	 * @author tang
	 *
	 */
	class GameView extends View implements Runnable {

		//画笔对象
		private Paint mPaint;
		//绘制接口
		private DrawGrapic mDrawGrapic;

		public GameView(Context context) {
			super(context);

			mPaint = new Paint();
			
//			new Thread(this).start();

		}

		@Override
		public void run() {

			while (!Thread.currentThread().isInterrupted()) {

				try {

					Thread.sleep(1000);

				} catch (InterruptedException e) {

					// TODO: handle exception 

					Thread.currentThread().interrupt();

				}

				//使用postInvalidate 可以直接在线程中更新界面 

				postInvalidate();

			}

		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
			Log.i("tyler.tang","game ondraw");

			//设计画布为黑色背景
			canvas.drawColor(Color.BLACK);
			//抗锯齿
			mPaint.setAntiAlias(true);

			//设计图形为空心
			mPaint.setStyle(Paint.Style.STROKE);

			//绘制空心几何图形 

			mDrawGrapic = new DrawCircle();

			mDrawGrapic.draw(canvas);

			mDrawGrapic = new DrawLine();

			mDrawGrapic.draw(canvas);

			mDrawGrapic = new DrawRect();

			mDrawGrapic.draw(canvas);

		}
	}

	/**
	 * 画圆
	 * 
	 * @author tang
	 *
	 */
	class DrawCircle implements DrawGrapic {

		private Paint paint = null;
		private Paint paint_eye = null;

		public DrawCircle() {

			paint = new Paint();
			paint_eye = new Paint();
		}

		@Override
		public void draw(Canvas canvas) {

			// TODOAuto-generated method stub 

			//绘制圆形(圆心x，圆心y，半径r，画笔p) 

			paint_eye.setAntiAlias(true);

			paint.setAntiAlias(true);

			RectF rectF = new RectF(120, 60, 370, 240);

			paint_eye.setColor(Color.WHITE);

			paint.setColor(Color.GREEN);

			canvas.drawCircle(190, 110, 18, paint_eye);

			canvas.drawCircle(300, 110, 18, paint_eye);

			canvas.drawArc(rectF, 180, 180, true, paint);
		}

	}

	class DrawLine implements DrawGrapic {

		private Paint paint = null;

		public DrawLine() {

			paint = new Paint();

		}

		@Override
		public void draw(Canvas canvas) {

			// TODOAuto-generated method stub 

			paint.setAntiAlias(true);

			//绘制直线 

			paint.setColor(Color.GREEN);

			//设置线条粗细 

			paint.setStrokeWidth(12);

			canvas.drawLine(120, 40, 170, 90, paint);

			canvas.drawLine(320, 90, 370, 40, paint);

		}

	}

	public class DrawRect implements DrawGrapic {

		private Paint paint = null;

		public DrawRect() {

			paint = new Paint();

		}

		@Override
		public void draw(Canvas canvas) {

			// TODOAuto-generated method stub 

			//定义圆角矩形对象 

			RectF rectF1 = new RectF(120, 170, 370, 500);

			RectF rectF2 = new RectF(40, 150, 90, 400);

			RectF rectF3 = new RectF(390, 150, 440, 400);

			RectF rectF4 = new RectF(140, 520, 200, 650);

			RectF rectF5 = new RectF(290, 520, 350, 650);

			paint.setAntiAlias(true);

			//设置画笔颜色为BLUE 

			paint.setColor(Color.GREEN);

			//在画布上绘制圆角矩形/圆弧/直线 

			canvas.drawRoundRect(rectF1, 20, 20, paint);

			canvas.drawRoundRect(rectF2, 20, 20, paint);

			canvas.drawRoundRect(rectF3, 20, 20, paint);

			canvas.drawRoundRect(rectF4, 20, 20, paint);

			canvas.drawRoundRect(rectF5, 20, 20, paint);

		}

	}

}
