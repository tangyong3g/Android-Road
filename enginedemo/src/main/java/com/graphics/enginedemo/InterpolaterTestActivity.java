package com.graphics.enginedemo;

import com.graphics.engine.animation.InterpolatorFactory;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;


/**
 * 
 */
public class InterpolaterTestActivity extends Activity
		implements
			AdapterView.OnItemSelectedListener,
			SeekBar.OnSeekBarChangeListener,
			RadioGroup.OnCheckedChangeListener {
	/** @formater:off */
	private static final String[] INTERPOLATORS = { "Linear", "Elastic", "Bounce", "Accelerate", "Decelerate", "Accelerate/Decelerate",
			"Anticipate", "Overshoot", "Anticipate/Overshoot", };
	/** @formater:on */

	private int mDuration = 1000;
	private int mRepeatMode;
	private Interpolator mInterpolator;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_interpolater);

		Spinner spinner = (Spinner) findViewById(R.id.interpolater_spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, INTERPOLATORS);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);

		SeekBar seekBar = (SeekBar) findViewById(R.id.duration_seekbar);
		seekBar.setOnSeekBarChangeListener(this);
		seekBar.setProgress(mDuration);

		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.repeat_mode_radio);
		radioGroup.setOnCheckedChangeListener(this);
	}

	public void onItemSelected(AdapterView parent, View v, int position, long id) {
		switch (position) {
			case 0 :
				mInterpolator = AnimationUtils.loadInterpolator(this, android.R.anim.linear_interpolator);
				break;
			case 1 :
				mInterpolator = InterpolatorFactory.getInterpolator(InterpolatorFactory.ELASTIC, 0, new float[] { 0.25f, 0.25f });
				break;
			case 2 :
				mInterpolator = AnimationUtils.loadInterpolator(this, android.R.anim.bounce_interpolator);
				break;
			case 3 :
				mInterpolator = AnimationUtils.loadInterpolator(this, android.R.anim.accelerate_interpolator);
				break;
			case 4 :
				mInterpolator = AnimationUtils.loadInterpolator(this, android.R.anim.decelerate_interpolator);
				break;
			case 5 :
				mInterpolator = AnimationUtils.loadInterpolator(this, android.R.anim.accelerate_decelerate_interpolator);
				break;
			case 6 :
				mInterpolator = AnimationUtils.loadInterpolator(this, android.R.anim.anticipate_interpolator);
				break;
			case 7 :
				mInterpolator = AnimationUtils.loadInterpolator(this, android.R.anim.overshoot_interpolator);
				break;
			case 8 :
				mInterpolator = AnimationUtils.loadInterpolator(this, android.R.anim.anticipate_overshoot_interpolator);
				break;
		}

		startAnimation();
	}

	public void onNothingSelected(AdapterView parent) {
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		mDuration = Math.max(250, progress);
		TextView textView = (TextView) findViewById(R.id.duration_text);
		textView.setText("Durantion: " + mDuration + " ms");
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		startAnimation();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (checkedId == R.id.reverse_mode_button) {
			mRepeatMode = Animation.REVERSE;
		} else {
			mRepeatMode = Animation.RESTART;
		}

		startAnimation();
	}

	private void startAnimation() {
		final View target = findViewById(R.id.target);
		final View targetParent = (View) target.getParent();

		Animation a = new TranslateAnimation(0.0f, targetParent.getWidth() - target.getWidth() - targetParent.getPaddingLeft()
				- targetParent.getPaddingRight(), 0.0f, 0.0f);
		a.setStartOffset(500);
		a.setRepeatCount(Animation.INFINITE);
		a.setDuration(mDuration);
		a.setRepeatMode(mRepeatMode);
		a.setInterpolator(mInterpolator);
		target.startAnimation(a);
	}
}
