
package com.ty.exsample_unit_4.animation;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.android_begin_gl_3d.R;

/** 
 * 
 * 2013-07-26 
 * 
 *  TranslateAnimation 和 AlpAnimation的用法，
 *  
 *  问题 1: 当一个view设置的某个动物，并启动后。如果其它的view也使用了同样的一动画对象那他会同样的做动画，Animation和View之前就像是监听者模式一样
 * 
 * 
 * 
 * Animation用法
 * 
 * @author tangyong */
public class AnimationActivity extends Activity implements OnClickListener, android.view.animation.Animation.AnimationListener {

	String TAG = "AnimationTest";
	Button mStart;
	private Button mBtnAnimationTwo;
	ImageView mImageView;
	AnimationSet mAnimationSet;
	RelativeLayout mParent;
	ImageView mIcon;
	private ImageView mImageOne;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.unit4_animation);
		initComponent();
	}

	private void initComponent () {

		mStart = (Button)findViewById(R.id.btn_start);
		mBtnAnimationTwo = (Button)findViewById(R.id.btn_animation_two);
		mImageOne = (ImageView)findViewById(R.id.animation_img_one);

		mImageView = (ImageView)findViewById(R.id.img);
		mParent = (RelativeLayout)findViewById(R.id.animation_parent);

		mStart.setOnClickListener(this);
		mBtnAnimationTwo.setOnClickListener(this);
	}

	@Override
	public void onClick (View v) {
		int id = v.getId();
		switch (id) {

		case R.id.btn_start:
			
			playAnimationOne();

			break;

		case R.id.btn_animation_two:

			playAnimationTwo();

			break;

		default:
			break;
		}
	}
	
	private void playAnimationOne(){
		if (mAnimationSet == null) {
			mAnimationSet = new AnimationSet(false);
		}
		mImageView.clearAnimation();
		mAnimationSet.getAnimations().clear();
		TranslateAnimation tran = new TranslateAnimation(0,10,0,10);

		mAnimationSet.addAnimation(tran);
		mAnimationSet.setDuration(2000);
		mAnimationSet.setFillAfter(false);
		
		mImageOne.startAnimation(mAnimationSet);
		
	}

	private void playAnimationTwo () {

		if (mAnimationSet == null) {
			mAnimationSet = new AnimationSet(false);
		}
		mImageOne.clearAnimation();
		mAnimationSet.getAnimations().clear();

		AlphaAnimation alpAnimation = new AlphaAnimation(1.0f, 0.0f);
		mAnimationSet.addAnimation(alpAnimation);
		ScaleAnimation scalAnimation = new ScaleAnimation(0.05f, 2.0f, 0.1f, 2.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		mAnimationSet.addAnimation(scalAnimation);

		mAnimationSet.setDuration(2000);
		mAnimationSet.setRepeatCount(10);
		mAnimationSet.setFillAfter(false);
		
		mImageView.startAnimation(mAnimationSet);
	}

	@Override
	public void onAnimationStart (android.view.animation.Animation animation) {
	}

	@Override
	public void onAnimationEnd (android.view.animation.Animation animation) {

	}

	@Override
	public void onAnimationRepeat (android.view.animation.Animation animation) {
		Log.i(TAG,"Repeat");
	}

}
