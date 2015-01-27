package com.ty.exsample_unit_4;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.Attributes;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ty.crashreport.Application;
import com.ty.exsample.R;
import com.ty.exsample_unit_4.AttriAnimationXMLActivity.ViewWrap;
import com.ty.util.LayoutParamFactory;
import com.ty.util.UnitShapeFactory;

/**
 * 
		*@Title:
		*@Description: 
		*
		*			参考资料 How to Avoid ConcurrentModificationException when using an Iterator
		*			
		*			http://www.javacodegeeks.com/2011/05/avoid-concurrentmodificationexception.html
		*			http://www.2cto.com/kf/201403/286536.html
		*			
		*		
		*			解决ConcurrentModificationException的问题 ？
		*	
		*			典型问题一:
		*
		*			通过Iterator遍历元素，然后用List去remove。就会出现这个问题。
		*
		*			
		*		
		*			
		*				
		*
		*
		*@Author:tangyong
		*@Since:2014-11-26
		*@Version:1.1.0
 */
public class ConcurrentModificationExceptionActivity extends Activity implements OnClickListener {

	private Button mBtn;
	private Button mBtnRightOne;
	private TextView mShowRsTx;
	
	ScrollView mTopContainer;
	LinearLayout mContainer;
	Context mContext;
	
	private List<Integer> mList = new  ArrayList<Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getApplicationContext();
		
		initData();
		initView();
		
		excuteAccList();
	}
	
	
	public void excuteAccList(){
		
		ExecutorService executorService = Executors.newCachedThreadPool();
		
		for(int i = 0 ; i < 5 ; i++){
			executorService.execute(new Runnable() {
				
				@Override
				public void run() {
					
					for( Integer i : mList){
						try {
							Thread.sleep(5);
						} catch (Exception e) {
						}
						
						Log.i("tyler.tang","当前线程:"+Thread.currentThread().getId()+"访问数据:\t"+i);
					}
				}
			});
		}
		
	}

	
	private void initData() {
		
		for(int i  = 0; i < 1000 ; i++){
			mList.add(new Integer(i));
		}
	}

	private void initView() {
		
		mTopContainer = new ScrollView(mContext);
		mTopContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		//demo 1
		ImageView imageView = new ImageView(mContext);
		imageView.setLayoutParams(LayoutParamFactory.getLayoutParams(LayoutParams.MATCH_PARENT, 400));
		imageView.setBackgroundResource(R.drawable.error_1);

		mBtn = new Button(mContext);
		mBtn.setOnClickListener(this);
		mBtn.setText(R.string.error_demo_one);

		mContainer = new LinearLayout(this.getApplicationContext());
		mContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		mContainer.setOrientation(LinearLayout.VERTICAL);

		Attributes attr = new Attributes();
		//分割线
		View lineView = UnitShapeFactory.getInstance().getSimpleShapeView(UnitShapeFactory.LINE_TYPE, Color.BLACK, attr);

		// demo 2 
		mBtnRightOne = new Button(mContext);
		mBtnRightOne.setText(R.string.right_demo_one);
		mBtnRightOne.setOnClickListener(this);

		ImageView imageViewRight = new ImageView(mContext);
		imageViewRight.setLayoutParams(LayoutParamFactory.getLayoutParams(LayoutParams.MATCH_PARENT, 400));
		imageViewRight.setBackgroundResource(R.drawable.right_1);
		
		mShowRsTx = new TextView(mContext);
		mShowRsTx.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, 25));
		
		ImageView imageViewThree = new ImageView(mContext);
		imageViewThree.setLayoutParams(LayoutParamFactory.getLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		imageViewThree.setBackgroundResource(R.drawable.right_3);

		mTopContainer.addView(mContainer);

		mContainer.addView(mBtn);
		mContainer.addView(imageView);
		mContainer.addView(lineView);

		mContainer.addView(mBtnRightOne);
		mContainer.addView(imageViewRight);
		mContainer.addView(mShowRsTx);
		
		mContainer.addView(imageViewThree);
		
		setContentView(mTopContainer);
	}

	/**
			* 
			* @Description: error method 
	 */
	public void errorOne() {

		ArrayList<String> list = new ArrayList<String>();

		list.add("1");
		list.add("2");
		list.add("3");
		list.add("4");
		list.add("5");

		Iterator<String> iterator = (Iterator<String>) list.iterator();

		while (iterator.hasNext()) {

			String tempValue = iterator.next();
			if (tempValue.equals("1")) {
				list.remove(tempValue);
			}
		}
	}

	/**
	 * 
			* @return
			* @Description:
	 */
	public static String rightOne() {

		ArrayList<String> list = new ArrayList<String>();

		list.add("1");
		list.add("2");
		list.add("3");
		list.add("4");
		list.add("5");

		Iterator<String> iterator = (Iterator<String>) list.iterator();

		while (iterator.hasNext()) {
			String tempValue = iterator.next();
			if (tempValue.equals("1")) {
				// use Iterator operate
				iterator.remove();
			}
		}

		return list.toString();
	}

	@Override
	public void onClick(View v) {

		if (v == mBtn) {

			errorOne();

		} else if (v == mBtnRightOne) {

			String rs = rightOne();
			startRsAnimation(rs);
		}
	}

	public void startRsAnimation(String rs) {

		int width = Application.getInstance().getScreenInfo().getmWidth();
		
		ObjectAnimator animator = ObjectAnimator.ofInt(new ViewWrap(mShowRsTx), "width", 0, width).setDuration(5000);
		animator.setStartDelay(500);
		animator.start();
		
		mShowRsTx.setText(rs);
		mShowRsTx.requestLayout();
	}

}
