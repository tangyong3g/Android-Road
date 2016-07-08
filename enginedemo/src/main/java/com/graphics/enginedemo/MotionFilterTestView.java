package com.graphics.enginedemo;

import com.graphics.engine.animator.ValueAnimator;
import com.graphics.engine.animator.motionfilter.AlphaMotionFilter;
import com.graphics.engine.animator.motionfilter.MotionFilter;
import com.graphics.engine.animator.motionfilter.MotionFilterSet;
import com.graphics.engine.animator.motionfilter.RotateMotionFilter;
import com.graphics.engine.animator.motionfilter.ScaleMotionFilter;
import com.graphics.engine.view.GLFrameLayout;
import com.graphics.engine.view.GLView;
import com.graphics.engine.widget.GLImageView;

import android.content.Context;
import android.view.ViewGroup.LayoutParams;


/**
 * 
 * 
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public class MotionFilterTestView extends GLFrameLayout {
	ValueAnimator mMotionFilter;
	MotionFilterSet mMotionFilterSet = new MotionFilterSet();
	GLImageView mView;
	boolean mFirst = true;

	public MotionFilterTestView(Context context) {
		super(context);

		mView = new GLImageView(context);
		mView.setImageResource(R.drawable.sunflower);
		addView(mView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		mMotionFilter = new ScaleMotionFilter(1, 0.8f, 1, 1.25f,
				MotionFilter.RELATIVE_TO_SELF, 0.5f, MotionFilter.RELATIVE_TO_SELF, 0.5f);
		mMotionFilter.setDuration(1000);
		mMotionFilterSet.addMotionFilter(mMotionFilter);
		mView.setMotionFilter(mMotionFilter);
		
		mMotionFilter = new RotateMotionFilter(0, 30,
				MotionFilter.RELATIVE_TO_SELF, 0.5f, MotionFilter.RELATIVE_TO_SELF, 0.5f);
		mMotionFilter.setDuration(1000);
		mMotionFilterSet.addMotionFilter(mMotionFilter);
		
//		mMotionFilter = new TranslateMotionFilter(0, 300, 0, 300);
//		mMotionFilter.setDuration(1000);
//		mMotionFilterSet.addMotionFilter(mMotionFilter);
		
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

}
