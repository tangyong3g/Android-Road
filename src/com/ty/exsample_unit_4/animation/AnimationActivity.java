package com.ty.exsample_unit_4.animation;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;

import com.example.android_begin_gl_3d.R;
import com.ty.animation.Animation;

/**
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
		
		mAnimationSet = new AnimationSet(false);
		
		mAnimationSet.addAnimation(alpAnimation);
		mAnimationSet.addAnimation(scalAnimation);
		mAnimationSet.setAnimationListener(this);
	}			

	private void initComponent() {
		mStart = (Button) findViewById(R.id.btn_start);
		mImageView = (ImageView) findViewById(R.id.img);
		mStart.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		
		case R.id.btn_start:
			
			mImageView.startAnimation(mAnimationSet);
			
			break;

		default:
			break;
		}
		
	}

	@Override
	public void onAnimationStart(android.view.animation.Animation animation) {
		
	}

	@Override
	public void onAnimationEnd(android.view.animation.Animation animation) {
		
	}

	@Override
	public void onAnimationRepeat(android.view.animation.Animation animation) {
		
	}


}
