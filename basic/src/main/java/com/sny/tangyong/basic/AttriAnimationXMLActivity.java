package com.sny.tangyong.basic;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * 
 * pro:
 * 
 * 			属性动画本质上是调用对象的set and get methond 如果没有的话可重写。或者适配
 * 
 * 	属性动画要求动画作用的对象提供该属性的get和set方法，属性动画根据你传递的该熟悉的初始值和最终值，以动画的效果多次去调用set方法，每次传递给set方法的值都不一样，确切来说是随着时间的推移，所传递的值越来越接近最终值。总结一下，你对object的属性xxx做动画，
 * 	如果想让动画生效，要同时满足两个条件：
 * 
	1. object必须要提供setXxx方法，如果动画的时候没有传递初始值，那么还要提供getXxx方法，因为系统要去拿xxx属性的初始值（如果这条不满足，程序直接Crash）
	2. object的setXxx对属性xxx所做的改变必须能够通过某种方法反映出来，比如会带来ui的改变啥的（如果这条不满足，动画无效果但不会Crash）
	
	以上条件缺一不可
	
	from csdn http://blog.csdn.net/singwhatiwanna/article/details/17841165
 * 		
 * 
 * @author tangyong
 * 
 */
public class AttriAnimationXMLActivity extends Activity implements OnClickListener {

	private ImageView mImageView;
	private Button mBtn;

	private LinearLayout mContain;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContain = new LinearLayout(this.getApplicationContext());
		mContain.setOrientation(LinearLayout.VERTICAL);

		LayoutParams ly = new LayoutParams(100, 100);
		mImageView = new ImageView(this);
		mImageView.setBackgroundResource(R.drawable.notification_template_icon_bg);
		mImageView.setLayoutParams(ly);

		mBtn = new Button(this);
		mBtn.setText("Test");
		mBtn.setLayoutParams(new LayoutParams(100, 100));
		mBtn.setOnClickListener(this);

		mContain.addView(mBtn);
		mContain.addView(mImageView);

		setContentView(mContain);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();

		switch (action) {

			case MotionEvent.ACTION_DOWN :

				startAnimation();

				break;

			case MotionEvent.ACTION_UP :

				break;

			default :
				break;
		}

		return super.onTouchEvent(event);
	}

	public void startAnimation() {

		Animator animator = AnimatorInflater.loadAnimator(this.getApplicationContext(),
				R.animator.translate);
		animator.setTarget(mImageView);

		//设置缩放点 
		mImageView.setPivotX(0);
		mImageView.setPivotY(0);

		animator.start();
	}

	/**
	 * 用适配
	 * 
	 * @author tangyong
	 * 
	 */
	public static class ViewWrap {

		private View mTarget;

		public ViewWrap(View target) {
			this.mTarget = target;
		}

		public void setTarget(View target) {
			this.mTarget = target;
		}

		public void setWidth(int width) {

			mTarget.getLayoutParams().width = width;
			mTarget.requestLayout();

			Log.i("data", "data:\t" + width);
		}

		public int getWidth() {
			return mTarget.getLayoutParams().width;
		}

	}

	@Override
	public void onClick(View v) {
		startBtnAnimation();
	}

	
	/**
	 *  start 
	 */
	public void startBtnAnimation() {

		ObjectAnimator animator = 	ObjectAnimator.ofInt(new ViewWrap(mBtn), "width", 100, 200).setDuration(2000);
		animator.setStartDelay(2000);
		animator.start();

		mBtn.requestLayout();
	}

}
