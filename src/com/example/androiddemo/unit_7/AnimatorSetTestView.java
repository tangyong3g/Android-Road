package com.example.androiddemo.unit_7;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.go.gl.animator.Animator;
import com.go.gl.animator.AnimatorSet;
import com.go.gl.animator.FloatValueAnimator;
import com.go.gl.animator.ValueAnimator;
import com.go.gl.animator.ValueAnimator.AnimatorUpdateListener;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.sny.tangyong.androiddemo.R;;

/**
 * ValueAnimator 动画类的测试样例
 * 
 * @author dengweiming
 * @date [2013-7-4]
 */
public class AnimatorSetTestView extends GLViewGroup {

	GLDrawable mDrawable;
	float mX;
	float mAngle;
	float mScale;
	ValueAnimator mAnimatorSet;
	ArrayList<Animator> mAnimators;

	public AnimatorSetTestView(Context context) {
		super(context);
		mDrawable = GLDrawable.getDrawable(getResources(),
				R.drawable.ic_launcher);

		ValueAnimator translateAnimator = ValueAnimator.ofFloat(0, 300);
		// 设置动画时间
		translateAnimator.setDuration(1000);
		// 设置repeat方式
		translateAnimator.setRepeatMode(ValueAnimator.REVERSE);
		// 设置次数
		translateAnimator.setRepeatCount(1);
		// 设置名称
		translateAnimator.setName("translate");
		// 做动画的时候就会调用
		translateAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mX = ((FloatValueAnimator) animation).getAnimatedValue();
				invalidate();
				Log.i("TY", "up translate come !" + mX);
			}
		});

		ValueAnimator rotateAnimator = ValueAnimator.ofFloat(0, 90);
		rotateAnimator.setDuration(1500);
		rotateAnimator.setName("rotate");
		rotateAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mAngle = ((FloatValueAnimator) animation).getAnimatedValue();
				invalidate();
			}
		});

		ValueAnimator scaleAnimator = ValueAnimator.ofFloat(0.5f, 1.0f);
		scaleAnimator.setDuration(1000);
		// 设置repeat方式
		scaleAnimator.setRepeatMode(ValueAnimator.REVERSE);
		// 设置次数
		scaleAnimator.setRepeatCount(1);
		scaleAnimator.setName("scale");
		scaleAnimator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mScale = ((FloatValueAnimator) animation).getAnimatedValue();
				invalidate();
			}
		});
		

		AnimatorSet animatorSet = new AnimatorSet();
		
		animatorSet.play(scaleAnimator).before(rotateAnimator);
		animatorSet.setName("set");

		mAnimators = animatorSet.getChildAnimations();
		mAnimatorSet = animatorSet;

		setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(GLView v) {
				mAnimatorSet.relativeReverse();
				invalidate();

			}
		});

	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {

		canvas.translate(mDrawable.getIntrinsicWidth() + mX, 0);
		canvas.rotate(mAngle);
		canvas.scale(mScale, mScale);
		mDrawable.draw(canvas);

	}

}