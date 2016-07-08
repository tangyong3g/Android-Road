package com.graphics.enginedemo;


import android.content.Context;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;

import com.graphics.engine.animation.Animation;
import com.graphics.engine.animation.AnimationSet;
import com.graphics.engine.animation.RotateAnimation;
import com.graphics.engine.animation.TranslateAnimation;
import com.graphics.engine.view.GLFrameLayout;
import com.graphics.engine.view.GLView;
import com.graphics.engine.widget.GLImageView;

/**
 * 
 * 
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public class ReverseAnimationTestView extends GLFrameLayout {
	Animation mAnimation;
	AnimationSet mAnimationSet;
	GLImageView mView;
	boolean mFirst = true;

	public ReverseAnimationTestView(Context context) {
		super(context);

		mView = new GLImageView(context);
		mView.setImageResource(R.drawable.apple);
		addView(mView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		long duration = 500;
		mAnimationSet = new AnimationSet(true);
		
		mAnimation = new RotateAnimation(0, 45);
		mAnimation.setDuration(duration);
		mAnimationSet.addAnimation(mAnimation);
		
		mAnimation = new TranslateAnimation(0, 300, 0, 300);
		mAnimation.setDuration(duration);
		mAnimationSet.addAnimation(mAnimation);

		mAnimation = mAnimationSet;
		mAnimation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				Log.d("DWM", "onAnimationEnd");

			}
		});
		mAnimation.setFillAfter(true);

		mView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(GLView v) {
				if (mFirst) {
					mFirst = false;
					mView.startAnimation(mAnimation);
				} else {
					mAnimation.reverse(mView);
				}

			}
		});

	}

}
