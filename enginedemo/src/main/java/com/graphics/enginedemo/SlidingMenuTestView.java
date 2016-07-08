package com.graphics.enginedemo;


import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.FrameLayout.LayoutParams;

import com.graphics.engine.animation.InterpolatorFactory;
import com.graphics.engine.animator.Animator;
import com.graphics.engine.animator.AnimatorListenerAdapter;
import com.graphics.engine.animator.AnimatorSet;
import com.graphics.engine.animator.FloatValueAnimator;
import com.graphics.engine.animator.ValueAnimator;
import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.interpolator.Bezier3Interpolator;
import com.graphics.engine.view.GLLinearLayout;
import com.graphics.engine.view.GLView;
import com.graphics.engine.widget.GLButton;
import com.graphics.engine.widget.GLImageView;

/**
 * ValueAnimator 动画类的测试样例
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public class SlidingMenuTestView extends GLLinearLayout {
	private GLView mMenu;
	private GLView mCloseButton;
	private GLView mOpenButton;

	private FloatValueAnimator mSlidingAnimator;
	private FloatValueAnimator mCloseButtonRotateAnimator;
	private FloatValueAnimator mCloseButtonAlphaAnimator;
	private AnimatorSet mOpenMenuAnimatorSet;
	
	private Interpolator mFastOutInterpolator = new Bezier3Interpolator(0.5f, 2.5f);

	public SlidingMenuTestView(Context context) {
		super(context);
		setPadding(0, 200, 0, 0);

		//========init child views========
		GLButton button = new GLButton(context);
		button.setBackgroundColor(0x7fffffff);
		button.setText("A");
		addView(button, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mOpenButton = button;

		GLButton menu = new GLButton(context);
		menu.setBackgroundColor(0x7fffffff);
		menu.setText("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		addView(menu, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mMenu = menu;

		GLImageView closeButton = new GLImageView(context);
		closeButton.setImageResource(R.drawable.close_button);
		addView(closeButton, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mCloseButton = closeButton;

		//========init animators========
		mSlidingAnimator = ValueAnimator.ofFloat(0, 1);
		mSlidingAnimator.setDuration(300);
		mSlidingAnimator.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationStart(Animator animation) {
				if (!((ValueAnimator) animation).isPlayingBackwards()) {
					animation.setInterpolator(InterpolatorFactory.getInterpolator(
							InterpolatorFactory.QUADRATIC, InterpolatorFactory.EASE_OUT));
				} else {
					animation.setInterpolator(mFastOutInterpolator);
				}
			}
		});

		mCloseButtonRotateAnimator = ValueAnimator.ofFloat(-45, 90);
		mCloseButtonRotateAnimator.setDuration(400);
		mCloseButtonRotateAnimator.setInterpolator(InterpolatorFactory.getInterpolator(
				InterpolatorFactory.QUADRATIC, InterpolatorFactory.EASE_OUT));
		
		mCloseButtonAlphaAnimator = ValueAnimator.ofFloat(0, 255);
		mCloseButtonAlphaAnimator.setDuration(250);
		mCloseButtonAlphaAnimator.setInterpolator(InterpolatorFactory.getInterpolator(
				InterpolatorFactory.QUADRATIC, InterpolatorFactory.EASE_OUT));

		
		mOpenMenuAnimatorSet = new AnimatorSet();
		mOpenMenuAnimatorSet.play(mSlidingAnimator).before(mCloseButtonAlphaAnimator);
		mOpenMenuAnimatorSet.play(mCloseButtonAlphaAnimator).with(mCloseButtonRotateAnimator);
		
		ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				invalidate();	//必须通知更新绘制
			}
		};
		//因为在关闭菜单时，只使用mSlidingAnimator，所以不对整个mOpenMenuAnimatorSet设置更新监听器
		mSlidingAnimator.addUpdateListener(updateListener);
		mCloseButtonRotateAnimator.addUpdateListener(updateListener);
		

		//========init onClick listeners========
		mOpenButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(GLView v) {
				if (mSlidingAnimator.isStoppedOnFirstFrame()) {
					mOpenMenuAnimatorSet.start();
				}
			}
		});
		

		mCloseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(GLView v) {
				//不是判断mCloseButtonRotateAnimator.isStoppedOnLastFrame()，可以在按钮旋转时就关闭
				if (mSlidingAnimator.isStoppedOnLastFrame()) {
					//并且关闭菜单时，按钮不做动画，只是向左平移回去
					//因为平移动画来回不对称，所以不允许 relativeReverse()
					mSlidingAnimator.reverse();
				}
			}
		});

	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		//排版后才能决定平移动画的取值范围
		mSlidingAnimator.setValues(-mCloseButton.getRight(), 0);
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		long drawingTime = getDrawingTime();

		if (!mSlidingAnimator.isStoppedOnFirstFrame()) {
			float tx = mSlidingAnimator.getAnimatedValue();
			canvas.save();
			canvas.translate(tx, 0);
			drawChild(canvas, mMenu, drawingTime);
			canvas.restore();

			if (!mCloseButtonRotateAnimator.isStoppedOnFirstFrame()) {
				canvas.save();
				canvas.translate(tx, 0);
				float angle = mCloseButtonRotateAnimator.getAnimatedValue();
				canvas.rotate(angle, mCloseButton.getLeft() + mCloseButton.getWidth() * 0.5f,
						mCloseButton.getTop() + mCloseButton.getHeight() * 0.5f);

				int alpha = (int) mCloseButtonAlphaAnimator.getAnimatedValue();
				int savedAlpha = canvas.getAlpha();
				canvas.multiplyAlpha(alpha);
				drawChild(canvas, mCloseButton, drawingTime);
				canvas.setAlpha(savedAlpha);
				canvas.restore();
			}
		}

		drawChild(canvas, mOpenButton, drawingTime);

	}

	@Override
	public void cleanup() {
		//因为使用的动画没有被外部模块引用，因此不用cleanup它们
		super.cleanup();
	}

}