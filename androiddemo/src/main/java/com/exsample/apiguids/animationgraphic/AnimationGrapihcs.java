package com.exsample.apiguids.animationgraphic;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sny.tangyong.androiddemo.R;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * 			1：Android 4.0 and Animations 
 * 			2: intruducing viewProperty and Animations
 * 			3: android 3.0 Hareware
 * 			4: display bitmap effeciently 
 * 			
 * 
 * 			
 * 
 * @author  tangyong
 * @date  [2015-1-27]
 * 
 * 
 */
public class AnimationGrapihcs extends Activity {

	LinearLayout mContainer;

	LoadLargeBitmapEfficently mBitMapEfficently;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initView();
	}

	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initView() {

		mContainer = new LinearLayout(this.getApplicationContext());
		mBitMapEfficently = new LoadLargeBitmapEfficently(this.getApplicationContext());

		mContainer.addView(mBitMapEfficently);

		setContentView(mContainer);
	}

	/********************************************************* display bitmap effeciently ********************************************************************************/

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  tangyong
	 * @date  [2015-1-27]
	 */
	class LoadLargeBitmapEfficently extends View {

		final String TAG = "LoadLargeBitmapEfficently";
		Bitmap mBitmap;
		Paint mPaint;

		/**
		 * 
		 * <br>功能简述: load bitmap Demesions and type
		 * <br>功能详细描述:
		 * <br>注意:
		 */
		private void loadBitmapDemisionAndType() {

			BitmapFactory.Options ops = new BitmapFactory.Options();
			ops.inJustDecodeBounds = true;

			BitmapFactory.decodeResource(getResources(), R.drawable.bg_one, ops);

			int width = ops.outWidth;
			int height = ops.outHeight;

			String type = ops.outMimeType;

			Log.i(TAG, "width:\t" + width + "height:\t" + height + "type:\t" + type);
		}

		
		public LoadLargeBitmapEfficently(Context context) {
			super(context);

			mPaint = new Paint();
			mBitmap = decodeSampledBitmapFromResource(getResources(), R.drawable.bg_one, 100, 100);
			loadBitmapDemisionAndType();

		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			if (mBitmap != null) {
				canvas.drawBitmap(mBitmap, 0, 0, mPaint);
			}

		}

	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 * 
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param res
	 * @param resId
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/********************************************************* process Bitmap in UI Thread  ___ load Bitmap AsyncTask  ******************************************************/

	
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * 
	 * @author  tangyong
	 * @date  [2015-1-28]
	 */
	class BitmapWorkTask extends AsyncTask<Integer, Void, Bitmap> {

		private final WeakReference<ImageView> mWeakReferenceBitmap;
		private int data = 0;

		public BitmapWorkTask(ImageView bitmap) {
			mWeakReferenceBitmap = new WeakReference<ImageView>(bitmap);
		}

		@Override
		protected Bitmap doInBackground(Integer... params) {

			int data = params[0];

			return decodeSampledBitmapFromResource(getResources(), data, 100, 100);

		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);

			if (result == null && mWeakReferenceBitmap == null) {
				ImageView img = mWeakReferenceBitmap.get();
				img.setImageBitmap(result);
			}
		}

	}

}
