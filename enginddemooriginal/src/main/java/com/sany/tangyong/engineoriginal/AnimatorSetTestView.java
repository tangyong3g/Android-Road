package com.sany.tangyong.engineoriginal;

import android.content.Context;

import com.go.gl.animator.Animator;
import com.go.gl.animator.AnimatorSet;
import com.go.gl.animator.FloatValueAnimator;
import com.go.gl.animator.ValueAnimator;
import com.go.gl.animator.ValueAnimator.AnimatorUpdateListener;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;

import java.util.ArrayList;

/**
 * ValueAnimator 动画类的测试样例
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public class AnimatorSetTestView extends GLViewGroup {
	GLDrawable mDrawable;
	float mX;
	float mAngle;
	ValueAnimator mAnimatorSet;
	ArrayList<Animator> mAnimators;

	public AnimatorSetTestView(Context context) {
		super(context);
		mDrawable = GLDrawable.getDrawable(getResources(),R.mipmap.ic_launcher);
		ValueAnimator translateAnimator = ValueAnimator.ofFloat(0, 300);
		translateAnimator.setDuration(1000);
		translateAnimator.setRepeatMode(ValueAnimator.REVERSE);
		translateAnimator.setRepeatCount(1);
		translateAnimator.setName("translate");
		translateAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mX = ((FloatValueAnimator)animation).getAnimatedValue();
				invalidate();
			}
		});
		
		ValueAnimator rotateAnimator = ValueAnimator.ofFloat(0, 90);
		rotateAnimator.setDuration(1500);
		rotateAnimator.setName("rotate");
		rotateAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mAngle = ((FloatValueAnimator)animation).getAnimatedValue();
				invalidate();
			}
		});
		
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.play(translateAnimator).before(rotateAnimator);
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
		mDrawable.draw(canvas);
	}
	

}