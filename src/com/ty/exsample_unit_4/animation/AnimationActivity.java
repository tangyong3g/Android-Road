package com.ty.exsample_unit_4.animation;

import android.app.Activity;
import android.graphics.Rect;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ty.exsample.R;

/**
 * 
 * 2013-07-26
 * 
 * TranslateAnimation 和 AlpAnimation的用法，
 * 
 * 问题 1: 当一个view设置的某个动物，并启动后。如果其它的view也使用了同样的一动画对象那他会同样的做动画，
 * Animation和View之前就像是监听者模式一样
 * 
 * 
 * 
 * Animation用法
 * 
 * @author tangyong
 */
public class AnimationActivity extends Activity implements OnClickListener,
		android.view.animation.Animation.AnimationListener {

	String TAG = "AnimationTest";
	Button mStart;
	private Button mBtnAnimationTwo;
	ImageView mImageView;
	AnimationSet mAnimationSet;
	RelativeLayout mParent;
	ImageView mIcon;
	private ImageView mImageOne;

	private LinearLayout mAnimation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.unit4_animation);
		initComponent();
	}

	private void initComponent() {

		mStart = (Button) findViewById(R.id.btn_start);
		mBtnAnimationTwo = (Button) findViewById(R.id.btn_animation_two);
		mImageOne = (ImageView) findViewById(R.id.animation_img_one);

		mImageView = (ImageView) findViewById(R.id.img);
		mParent = (RelativeLayout) findViewById(R.id.animation_parent);

		mStart.setOnClickListener(this);
		mBtnAnimationTwo.setOnClickListener(this);

		mAnimation = (LinearLayout) findViewById(R.id.ly_animation);
		mAnimation.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {

		case R.id.btn_start:

			playAnimationOne();

			break;

		case R.id.btn_animation_two:

			playAnimationTwo();

			break;
			
		case R.id.ly_animation:
		
			Log.i("tyler.tang","点中我了");
		break;

		default:
			break;
		}
	}

	TranslateAnimation mTranAnimation;
	TranslateAnimation mUpTranAnimation;
	
	int mCount = 0;
	
	
	private void playAnimationOne() {
		Rect rect = new Rect();
		mAnimation.getHitRect(rect);
		Log.i("tyler.tang",rect.toString());
		
		mCount ++;
		if (mAnimationSet == null) {
			mAnimationSet = new AnimationSet(false);
		}
		if(mTranAnimation == null){
			mTranAnimation = new TranslateAnimation(0, 0, 0, 100);
		}
		if(mUpTranAnimation == null){
			mUpTranAnimation  = new TranslateAnimation(0,0,100,0);
		}
		mAnimation.clearAnimation();
		mAnimationSet.getAnimations().clear();
		
		if(mCount %2 == 0){
			mAnimationSet.addAnimation(mUpTranAnimation);
		}else{
			mAnimationSet.addAnimation(mTranAnimation);
		}
		
		mAnimationSet.setDuration(7);
		mAnimationSet.setFillAfter(true);
		mAnimationSet.setAnimationListener(this);

		mAnimation.startAnimation(mAnimationSet);

	}

	private void playAnimationTwo() {

		if (mAnimationSet == null) {
			mAnimationSet = new AnimationSet(false);
		}
		mImageOne.clearAnimation();
		mAnimationSet.getAnimations().clear();

		AlphaAnimation alpAnimation = new AlphaAnimation(1.0f, 0.0f);
		mAnimationSet.addAnimation(alpAnimation);
		ScaleAnimation scalAnimation = new ScaleAnimation(0.05f, 2.0f, 0.1f,
				2.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
		mAnimationSet.addAnimation(scalAnimation);

		mAnimationSet.setDuration(2000);
		mAnimationSet.setRepeatCount(10);
		mAnimationSet.setFillAfter(false);

		mImageView.startAnimation(mAnimationSet);
	}

	@Override
	public void onAnimationStart(android.view.animation.Animation animation) {
	}

	@Override
	public void onAnimationEnd(android.view.animation.Animation animation) {

		if (mAnimation != null) {

//			android.widget.RelativeLayout.LayoutParams params = 	(android.widget.RelativeLayout.LayoutParams)mAnimation.getLayoutParams();

//			params.topMargin = 100;
//			params.topMargin = params.topMargin;
			
			
//			mAnimation.setLayoutParams(params);
			
//			Matrix matrix  = mAnimation.getMatrix();
//			matrix.postTranslate(0, 100);
			
//			mAnimation.setTranslationY(100);
			Rect rect = new Rect();
			
			mAnimation.getHitRect(rect);
			
			Log.i("tyler.tang",rect.toString());
		}

	}

	@Override
	public void onAnimationRepeat(android.view.animation.Animation animation) {
		Log.i(TAG, "Repeat");
	}

}
