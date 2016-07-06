package com.graphics.enginedemo;

import android.content.Context;
import android.view.ViewGroup.LayoutParams;

import com.graphics.engine.gl.animator.ValueAnimator;
import com.graphics.engine.gl.animator.motionfiler.AlphaMotionFilter;
import com.graphics.engine.gl.animator.motionfiler.MotionFilter;
import com.graphics.engine.gl.animator.motionfiler.MotionFilterSet;
import com.graphics.engine.gl.animator.motionfiler.RotateMotionFilter;
import com.graphics.engine.gl.animator.motionfiler.ScaleMotionFilter;
import com.graphics.engine.gl.view.GLFrameLayout;
import com.graphics.engine.gl.view.GLView;
import com.graphics.engine.gl.widget.GLImageView;


/**
 * 
 * @author dengweiming
 * @date [2013-7-4]
 */
public class MotionFilterTestView extends GLFrameLayout {

	ValueAnimator mMotionFilter;
	MotionFilterSet mMotionFilterSet = new MotionFilterSet();
	GLImageView mView;
	boolean mFirst = true;

	public MotionFilterTestView(Context context) {
		super(context);

		initImageView(context);
	}

	private void initOriginal(Context context) {

		mView = new GLImageView(context);
		mView.setImageResource(R.mipmap.ic_launcher);
		addView(mView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		mMotionFilter = new ScaleMotionFilter(1, 0.8f, 1, 1.25f, MotionFilter.RELATIVE_TO_SELF,
				0.5f, MotionFilter.RELATIVE_TO_SELF, 0.5f);
		mMotionFilter.setDuration(1000);
		mMotionFilterSet.addMotionFilter(mMotionFilter);
		mView.setMotionFilter(mMotionFilter);

		mMotionFilter = new RotateMotionFilter(0, 30, MotionFilter.RELATIVE_TO_SELF, 0.5f,
				MotionFilter.RELATIVE_TO_SELF, 0.5f);
		mMotionFilter.setDuration(1000);
		mMotionFilterSet.addMotionFilter(mMotionFilter);

		mMotionFilter = new AlphaMotionFilter(1, 0.5f);
		mMotionFilter.setDuration(1000);
		mMotionFilterSet.addMotionFilter(mMotionFilter);

		mView.startMotionFilter(mMotionFilterSet);
		mView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(GLView v) {
				mMotionFilterSet.relativeReverse();
				invalidate();
			}
		});

	}

	private void initImageView(Context context) {

		mView = new GLImageView(context);
		mView.setImageResource(R.mipmap.ic_launcher);
		addView(mView, new LayoutParams(200, 200));

	}

}
