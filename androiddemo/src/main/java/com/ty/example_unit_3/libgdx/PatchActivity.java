package com.ty.example_unit_3.libgdx;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Path;
import com.badlogic.gdx.math.Vector2;
import com.sny.tangyong.androiddemo.R;


/**
 * 
 *	
 *  说明:
 *  	本DEMO是用来说明Libgdx中path的用法和android中的多种插值器的用法
 *  
 *   
 *   
 *   需求如下: 
 *   
 *   	在一个view中，产生三个点用来作为路径的标记点。然后用不同的插值器去做运用。
 *
 * @author 师爷GBK[ty_sany@163.com]
 * 2013-7-31
 *
 */
public class PatchActivity extends Activity implements OnClickListener ,Callback{	
	
	private ImageView mImageView;
	private Path<Vector2> mPositions;
	private Button mBtn;
	private static String TAG = "PatchActivity";
	private int mH;
	private int mW;
	private long mStartTime ;
	private long mDurtion = 3000;
	private Handler mHandler;
	private static final int  ANIMATION = 2000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unit3_path);
		initConponent();
	}


	private void initConponent() {
		
		mH = 800;
		mW = 480;
		mImageView = (ImageView)findViewById(R.id.unit3_path_image);
		mBtn = (Button)findViewById(R.id.unit3_path_btn);
		mHandler = new Handler(this);
		mBtn.setOnClickListener(this);
		
		initPosition();
	}
	
	
	
	private void initPosition(){
		mPositions = new Bezier<Vector2>(new Vector2(0,mH),new Vector2(mW/2.0f, mH/4.0f),new Vector2(mW,mH));
		
	}
	
	
	private void startAnimation(){
		mStartTime = AnimationUtils.currentAnimationTimeMillis();
		sendMsg();
	}
	
	private Vector2 mTemp = new Vector2();
	
	
	private Vector2 onAnimation(){

		long stepTime = AnimationUtils.currentAnimationTimeMillis()- mStartTime;
		float t = stepTime * 1.0f / mDurtion;
		t = Math.max(0, Math.min(t, 1));
		
		if(t == 1){
			return null;
		}
		
		mPositions.valueAt(mTemp, t);
		
		return mTemp;
	}


	@Override
	public void onClick(View v) {
		
		int id = v.getId();
		
		switch (id) {
		case R.id.unit3_path_btn:
			
			startAnimation();
			
			break;

		default:
			break;
		}
	}


	@Override
	public boolean handleMessage(Message msg) {
		int what = msg.what;
		
		switch (what) {
		case ANIMATION:
			
			executeAnimation(msg);
			
			break;

		default:
			break;
		}
		return false;
	}


	private void executeAnimation(Message msg) {
		
		Vector2 value = onAnimation();
		if(value != null){
			changePosition(value);
			sendMsg();
		}
	}
	
	private void sendMsg(){
		Message msg = new Message();
		msg.what = ANIMATION;
		mHandler.sendMessage(msg);
	}
	
	private RelativeLayout.LayoutParams ly  = new RelativeLayout.LayoutParams(40, 40);
	
	private void changePosition(Vector2 positionValue){
		
		float x = positionValue.x;
		float y = positionValue.y;
		
		Log.i(TAG,"positoin:x:\t"+x +"\ty:\t"+y);
		
		ly.setMargins((int)x, (int)y, (int)x+mImageView.getWidth(), (int)y+mImageView.getHeight());
		mImageView.setLayoutParams(ly);
	}
	

}
