package com.ty.exsample_unit_4.animation;

import java.util.Stack;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.android_begin_gl_3d.R;

/**
 * 
 * TranslateAnimation如果不写那么就是相对于目前的位置，移动
 * 
 * Animation用法
 * 
 * @author tangyong
 * 
 */
public class AnimationActivity extends Activity implements OnClickListener , android.view.animation.Animation.AnimationListener{

	String TAG = "AnimationTest";
	Button mStart;
	ImageView mImageView;
	AnimationSet mAnimationSet;
	RelativeLayout mParent;
	TranslateAnimation mTranslateAnimation;
	ImageView mIcon;
	Stack<Animation> mAnimationStack = new Stack<Animation>();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unit4_animation);
		initComponent();
		initAnimation();
	}
	
	
	private void initAnimation(){
		
		AlphaAnimation alpAnimation = new AlphaAnimation(1.0f, 0.0f);
		alpAnimation.setDuration(1000);
		ScaleAnimation scalAnimation =  new ScaleAnimation(0.1f, 2.5f, 0.1f, 2.5f,Animation.RELATIVE_TO_SELF, 0.5f,Animation.RELATIVE_TO_SELF, 0.5f);
		scalAnimation.setDuration(1000);
		
		mTranslateAnimation = new TranslateAnimation(0, 100, 0, 100);
		mTranslateAnimation.setFillAfter(true);
		mTranslateAnimation.setDuration(1000);
		
		TranslateAnimation tAniaOne = new TranslateAnimation(0, 100, 0, 100);
		tAniaOne.setFillAfter(true);
		tAniaOne.setDuration(1000);
		TranslateAnimation tAniaTwo = new TranslateAnimation(0, -100, 0, -100);
		tAniaTwo.setFillAfter(true);
		tAniaTwo.setDuration(1000);
//		mAnimationStack.push(tAniaOne);
		mAnimationStack.push(tAniaTwo);
	}
	
	

	private void initComponent() {
		
		mStart = (Button) findViewById(R.id.btn_start);
		mImageView = (ImageView) findViewById(R.id.img);
		mParent = (RelativeLayout)findViewById(R.id.animation_parent);
		mImageView   = (ImageView)findViewById(R.id.animation_img_one);
		
		mStart.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		
		case R.id.btn_start:
			
			Animation first  = null;
			try {
				first = mAnimationStack.pop();
			} catch (Exception e) {
				initAnimation();
				first = mAnimationStack.pop();
			}
			
			first.setAnimationListener(this);
			mImageView.startAnimation(first);
			
			break;

		default:
			break;
		}
		
	}

	@Override
	public void onAnimationStart(android.view.animation.Animation animation) {
		RelativeLayout.LayoutParams ly = (RelativeLayout.LayoutParams)mImageView.getLayoutParams();
		Log.i(TAG, "开始时Left:\t"+ly.leftMargin+":\tTop:\t"+ly.topMargin);
		
	}

	@Override
	public void onAnimationEnd(android.view.animation.Animation animation) {
		
		
		Animation next =  null;
		boolean isFinished = false;
		try {
			next = mAnimationStack.pop();
		} catch (Exception e) {
			isFinished = true;
		}
		
		RelativeLayout.LayoutParams ly = (RelativeLayout.LayoutParams)mImageView.getLayoutParams();
		Log.i(TAG, "结束时Left:\t"+ly.leftMargin+":\tTop:\t"+ly.topMargin);
		
		if(!isFinished){
			mImageView.startAnimation(next);
		}
	}

	@Override
	public void onAnimationRepeat(android.view.animation.Animation animation) {
		
	}


}
