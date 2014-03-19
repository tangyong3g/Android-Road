package com.ty.exsample_unit_4;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.android_begin_gl_3d.R;
import com.ty.util.BitmapUtil;

/**
 * 
 * @author tang
 * 
 * 
 *         <li>第一个要未,要把view转化成一个bitmap再画到下面的自定义view中去
 * 
 */
public class BitmapActivity extends Activity {

	
	LinearLayout mParent;
	
	ImageView mFirstImg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unit_4_bitmap);
		
		
		mParent = (LinearLayout) findViewById(R.id.ly_container);
		mFirstImg = (ImageView) findViewById(R.id.img_show_view);
		
		//转化imageView成bitmap
		Bitmap bitmap = BitmapUtil.viewToBitmap(mFirstImg);
		//传到view中去
		SimpleView simpleView  = new SimpleView(getApplicationContext(), bitmap);
		mParent.addView(simpleView);
		
	}
	
	
	
	class SimpleView extends View{
		
		
		Bitmap mBitmap;
		Paint mPaint;

		public SimpleView(Context context) {
			super(context);
		}
		
		public SimpleView(Context context,Bitmap bitmap) {
			super(context);
			this.mBitmap = bitmap;
			mPaint = new Paint();
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			
			if(mBitmap!=null){
				canvas.drawBitmap(mBitmap, 100,25, mPaint);
			}
		}
		
		
	}

}
