package com.ty.example_unit_3.libgdx;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.ty.crashreport.Application;

/**
 * 
 * 解决平面中任意路径问题
 * 
 * @author tangyong
 * 
 */
public class PathActivity extends Activity implements OnClickListener {

	LinearLayout mContainer;
	Array<Vector2> mArray = new Array<Vector2>();
	PathSimpleView mPathView;
	int[] mColorArray = new int[] { Color.RED, Color.BLUE, Color.CYAN,
			Color.GREEN, Color.MAGENTA, Color.YELLOW };

	private CatmullRomSpline<Vector2> mBsLine;
	public static final int LEFT_ID = 1000001;
	public static final int RIGHT_ID = 1000002;

	private float mPercent = 0.0f;
	private long mStart = 0;
	private boolean mAnimationFlag = false;
	private long total = 3000;

	Thread mThread;

	private Vector2 mTemp = new Vector2();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LinearLayout.LayoutParams paramsContainer = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		LinearLayout.LayoutParams topContainerLyParams = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		LinearLayout topContainer = new LinearLayout(this);
		topContainer.setOrientation(LinearLayout.HORIZONTAL);
		topContainer.setLayoutParams(topContainerLyParams);

		Button btnLeft = new Button(this);
		Button btnRight = new Button(this);

		btnLeft.setText("reset");
		btnRight.setText("start");

		btnLeft.setId(LEFT_ID);
		btnRight.setId(RIGHT_ID);

		btnLeft.setOnClickListener(this);
		btnRight.setOnClickListener(this);

		topContainer.addView(btnLeft);
		topContainer.addView(btnRight);

		mPathView = new PathSimpleView(this);

		mContainer = new LinearLayout(this);
		mContainer.setLayoutParams(paramsContainer);
		mContainer.setOrientation(LinearLayout.VERTICAL);

		mContainer.addView(topContainer);
		mContainer.addView(mPathView);

		setContentView(mContainer);

		initPosition();
	}

	private void initPosition() {

		mArray.clear();

		// Runnable positionRun = new Runnable() {
		//
		// @Override
		// public void run() {
		//
		int screenWidth = Application.getInstance().getScreenInfo().getmWidth();
		int screenHeight = Application.getInstance().getScreenInfo()
				.getmHeight();

		for (int i = 0; i < 5; i++) {

			int y = (int) com.ty.animation.AnimationUtils.randomMaxAndMin(
					screenHeight, 0);
			int x = (int) com.ty.animation.AnimationUtils.randomMaxAndMin(
					screenWidth, 0);

			Vector2 point = new Vector2(x, y);

			mArray.add(point);

		}

		// }
		// };
		//
		// Application application = Application.getInstance();
		// application.postRunnableToLightThread(positionRun);
	}

	class PathSimpleView extends View {

		Paint mPaint;

		public PathSimpleView(Context context) {
			super(context);
			mPaint = new Paint();

		}

		@Override
		public void draw(Canvas canvas) {
			super.draw(canvas);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			if (mArray == null || mArray.size == 0) {
				return;
			}

			// 绘制固定的顶点
			for (int i = 0; i < mArray.size; i++) {
				mPaint.setColor(mColorArray[i]);

				canvas.drawText((i + 1) + "", mArray.get(i).x,
						mArray.get(i).y - 5, mPaint);
				canvas.drawCircle(mArray.get(i).x, mArray.get(i).y, 5, mPaint);
			}

			// 绘制线条
			if (mAnimationFlag) {

				long duration = AnimationUtils.currentAnimationTimeMillis()
						- mStart;
				float percent = (float) duration / total;

				if (Math.min(percent, 1) == 1) {
					mAnimationFlag = false;
				}

				mBsLine.valueAt(mTemp, Interpolation.exp5In.apply(percent));
				canvas.drawCircle(mTemp.x, mTemp.y, 6, mPaint);

				invalidate();
			}

		}
	}

	@Override
	public void onClick(View v) {

		int id = v.getId();

		switch (id) {

		case LEFT_ID:

			reset();

			break;

		case RIGHT_ID:

			start();

			break;

		default:
			break;
		}

	}

	private void reset() {

		initPosition();

		int size = mArray.size;
		Vector2[] datas = new Vector2[size];

		for (int i = 0; i < size; i++) {
			datas[i] = mArray.get(i);
		}

		if (mBsLine == null) {
			mBsLine = new CatmullRomSpline<Vector2>(datas, true);
		}

		mBsLine.set(datas, true);

		mPathView.invalidate();

	}

	private void start() {

		if (mBsLine == null) {

			initPosition();

			int size = mArray.size;
			Vector2[] datas = new Vector2[size];

			for (int i = 0; i < size; i++) {
				datas[i] = mArray.get(i);
			}

			mBsLine = new CatmullRomSpline<Vector2>(datas, true);
		}

		mStart = AnimationUtils.currentAnimationTimeMillis();
		mAnimationFlag = true;

		mPathView.invalidate();
	}

}
