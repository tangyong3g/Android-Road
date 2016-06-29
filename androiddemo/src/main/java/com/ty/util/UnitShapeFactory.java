package com.ty.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.View;

import com.sny.tangyong.androiddemo.AndroidApplication;

import java.util.jar.Attributes;


/**
 * 		pro: 
 * 
 * 				1: 自定义的view的高度在什么时候生成 
 * 
 * 
 * 
		*@Title:
		*@Description:
		*@Author:tangyong
		*@Since:2014-11-26
		*@Version:1.1.0
 */
public class UnitShapeFactory {

	private Context mContext;
	public static UnitShapeFactory sInstance;
	public static final int LINE_TYPE = 0;
	public static Integer LINE_Y_KEY = 1000;

	public static UnitShapeFactory getInstance() {

		if (sInstance == null) {
			sInstance = new UnitShapeFactory();
			sInstance.mContext = AndroidApplication.getInstance().getApplicationContext();
		}

		return sInstance;
	}

	public View getSimpleShapeView(int type, int color, Attributes attr) {

		View result = null;

		switch (type) {
			case LINE_TYPE :

				int width = AndroidApplication.getInstance().getScreenInfo().getmWidth();
				Integer y = 0;
				y = 20;
				LineAbsView lineAbsView = new LineAbsView(color, new Point(0, y), new Point(width, y));
				result = new AbsView(mContext, lineAbsView);

				break;

			default :
				break;
		}

		return result;
	}

	/**
	 * 
			*@Title:
			*@Description:
			*@Author:tangyong
			*@Since:2014-11-26
			*@Version:1.1.0
	 */
	@SuppressLint("WrongCall")
	class AbsView extends View {

		IAbsView mAbsView;

		public AbsView(Context context) {
			super(context);
		}

		public AbsView(Context context, IAbsView absViewImpl) {
			super(context);
			mAbsView = absViewImpl;
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			// TODO
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			int type = mAbsView.getType();

			switch (type) {
				case LINE_TYPE :
					
					setMeasuredDimension(getLayoutParams().width, LineAbsView.HEIGHT);
					
					break;

				default :
					break;
			}
			
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			// TODO
			super.onLayout(changed, left, top, right, top + 1);
			//			Log.i("data","left:\t"+left+"top:\t"+top+"right:\t"+right+"bottom:\t"+bottom);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			mAbsView.draw(canvas);
		}

	}

	/**
	 * 
			*@Title:
			*@Description:
			*@Author:tangyong
			*@Since:2014-11-26
			*@Version:1.1.0
	 */
	interface IAbsView {

		/**
				* @param canvas
				* @Description:
		 */
		void draw(Canvas canvas);

		void onMeasure();

		int getType();

	}

	/**
	 * 
			*@Title:
			*@Description:
			*@Author:tangyong
			*@Since:2014-11-26
			*@Version:1.1.0
	 */
	static class LineAbsView implements IAbsView {

		Point mStart;
		Point mEnd;
		int mColor;
		static final int HEIGHT = 5;

		public LineAbsView(int color, Point pointStart, Point pointEnd) {
			mColor = color;
			mStart = pointStart;
			mEnd = pointEnd;
		}

		@Override
		public void draw(Canvas canvas) {

			Paint paint = new Paint();

			paint.setColor(mColor);
			canvas.drawLine(mStart.x, mStart.y, mEnd.x, mEnd.y, paint);
		}

		@Override
		public void onMeasure() {
			// TODO

		}

		@Override
		public int getType() {
			// TODO
			return LINE_TYPE;
		}

	}

}
