package com.example.androiddemo.unit_7;

import android.content.Context;
import android.view.ViewGroup.LayoutParams;

import com.sny.tangyong.androiddemo.R;
import com.go.gl.animator.ValueAnimator;
import com.go.gl.animator.motionfilter.AlphaMotionFilter;
import com.go.gl.animator.motionfilter.MotionFilter;
import com.go.gl.animator.motionfilter.MotionFilterSet;
import com.go.gl.animator.motionfilter.RotateMotionFilter;
import com.go.gl.animator.motionfilter.ScaleMotionFilter;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLView;
import com.go.gl.widget.GLImageView;

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
		mView.setImageResource(R.drawable.ic_launcher);
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
		mView.setImageResource(R.drawable.ic_launcher);
		addView(mView, new LayoutParams(200, 200));

	}

}
