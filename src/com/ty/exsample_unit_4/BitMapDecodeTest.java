package com.ty.exsample_unit_4;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ty.exsample.R;

/**
 * 
 * 
 *      1： 如果只是得到尺寸 这样可以节省不少内存  opts.inJustDecodeBounds = true;
 * 
 * 
		*@Title:
		*@Description:
		*@Author:tangyong
		*@Since:2015-1-6
		*@Version:1.1.0
 */
public class BitMapDecodeTest extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO
		super.onCreate(savedInstanceState);

		setContentView(new BitmapView(this));

	}

	/**
	 * 
			*@Title:
			*@Description:
			*@Author:tangyong
			*@Since:2015-1-6
			*@Version:1.1.0
	 */
	class BitmapView extends View {

		Bitmap mbitmap;
		Bitmap mBitMapTwo;
		Matrix matrix;

		public BitmapView(Context context) {
			super(context);

			BitmapFactory.Options ops = new BitmapFactory.Options();
			ops.inJustDecodeBounds = true;
			mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_one, ops);
			Log.i("data", "picture width: " + ops.outWidth + "\t height:" + ops.outHeight);

			mBitMapTwo = BitmapFactory.decodeResource(getResources(), R.drawable.bg_one);

			matrix = new Matrix();

		}

		@Override
		protected void onDraw(Canvas canvas) {
			// TODO
			super.onDraw(canvas);

			Paint paint = new Paint();

			matrix.reset();
			matrix.setRotate(30);
//			matrix.setTranslate(10, 0);
			canvas.drawBitmap(mBitMapTwo, matrix, paint);
		}

	}

}
