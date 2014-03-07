package com.ty.exsample_unit_4;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

/**
 * 
 * @author tang
 * 
 *         　这个demo 需求是
 * 
 *         　１:　编码一张图片用不同的方式.RBG_888 RGB_444 , 
 *         　2: 在上面显示一张gif的图.
 * 
 * 
 *         这个Demo要解决的问题有:
 * 
 * 
 *         1: 把文字流变成 bitmap 的方式. 
 *         2: bitMap 只种不同的方式所产生内存的占用的差异. 
 *         3: Paint 和Cavas
 *         的理解.
 * 
 * 
 * 
 */
public class BitMapDecodeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SampleView view = new SampleView(this);
		
		setContentView(view);
	}
	
	
	/**
	 * 把输入流转化成byte数组
	 * 
	 * @param is
	 * @return
	 */
	private static byte[] streamToBytes(InputStream is) {
		ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
		byte[] buffer = new byte[1024];
		int len;
		try {
			while ((len = is.read(buffer)) >= 0) {
				os.write(buffer, 0, len);
			}
		} catch (java.io.IOException e) {
		}
		return os.toByteArray();
	}

	/**
	 * 
	 * @author tang
	 * 
	 */
	class SampleView extends View {

		Bitmap mBitMapOne;
		Bitmap mBitMapTwo;
		Bitmap mBitMapThree;
		Bitmap mBitMapFour;

		public SampleView(Context context) {
			super(context);
			
			
		   java.io.InputStream is;
		   is = context.getResources().openRawResource(com.example.android_begin_gl_3d.R.drawable.cube_texture);
		   
		   //BitmapFactory.Options 类,  允许我们定义图片以何种方式如何读到内存，
		   BitmapFactory.Options opts = new BitmapFactory.Options();
           Bitmap bm;

           //这样设置后.就不会产生真实的bitMap回来.而只有高度和宽度.为了解决大图的问题 opts.outHeight 
           opts.inJustDecodeBounds = true;
           
           bm = BitmapFactory.decodeStream(is, null, opts);
		   
           opts.inJustDecodeBounds = false;    // 这样才能真正的得到图
           opts.inSampleSize = 1;             // 变成原来图的1/4的大小
           bm = BitmapFactory.decodeStream(is, null, opts);

           
           //第一张方块的图
		   mBitMapOne = bm;
		   
//		   int heigt = opts.outHeight;
//		   int width = opts.outWidth;
//		   
//		   Gdx.app.log("info", "height:\t"+heigt+"width:\t"+width);
		   
		   is = context.getResources().openRawResource(com.example.android_begin_gl_3d.R.drawable.ic_launcher);
           mBitMapTwo = BitmapFactory.decodeStream(is);
           
           // create a deep copy of it using getPixels() into different configs
           int w = mBitMapTwo.getWidth();
           int h = mBitMapTwo.getHeight();
           
           
           int[] pixels = new int[w*h];
           mBitMapTwo.getPixels(pixels, 0, w, 0, 0, w, h);
           
           float scaleInt = w * 0.5f;
           int   scaleIntI = (int)scaleInt;
           
           mBitMapThree = Bitmap.createBitmap(pixels, 0, w,scaleIntI, (int)(h*0.5f),Bitmap.Config.ARGB_8888);
           mBitMapFour = Bitmap.createBitmap(pixels, 0, w, w, h,Bitmap.Config.ARGB_4444);
           

		}
	
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			setFocusable(true);
			
			canvas.drawColor(0xFFCCCCCC);
			  
			Paint p = new Paint();
			p.setAntiAlias(true);
			
			canvas.drawBitmap(mBitMapOne, 10, 10, null);
			canvas.drawBitmap(mBitMapTwo, 10, 170, null);
			canvas.drawBitmap(mBitMapThree, 110, 170, null);
			canvas.drawBitmap(mBitMapFour, 210, 170, null);
			
			
		}

	}

}

