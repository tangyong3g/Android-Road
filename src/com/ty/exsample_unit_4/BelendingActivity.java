package com.ty.exsample_unit_4;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

/**
 * 本单元内容是要掌握多种混合的方法
 * 
 * 
 * 
 * @author tangyong
 * 
 * 
 * 
 * 
 */

public class BelendingActivity extends Activity {

	XfModelSimpleView mXfView;

	LinearLayout mContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		mXfView = new XfModelSimpleView(this);
		mXfView.setLayoutParams(params);

		mContainer = new LinearLayout(this);
		mContainer.setOrientation(LinearLayout.VERTICAL);

		mContainer.addView(mXfView);

		setContentView(mContainer);
	}

	/**
	 * @author tangyong
	 */
	class XfModelSimpleView extends View {

		private static final int W = 64;
		private static final int H = 64;
		private static final int ROW_MAX = 4; // number of samples per row

		private Bitmap mSrcB;
		private Bitmap mDstB;
		private Shader mBG; // background checker-board pattern

		private final Xfermode[] sModes = {
				new PorterDuffXfermode(PorterDuff.Mode.CLEAR),
				new PorterDuffXfermode(PorterDuff.Mode.SRC),
				new PorterDuffXfermode(PorterDuff.Mode.DST),
				new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER),
				new PorterDuffXfermode(PorterDuff.Mode.DST_OVER),
				new PorterDuffXfermode(PorterDuff.Mode.SRC_IN),
				new PorterDuffXfermode(PorterDuff.Mode.DST_IN),
				new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT),
				new PorterDuffXfermode(PorterDuff.Mode.DST_OUT),
				new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP),
				new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP),
				new PorterDuffXfermode(PorterDuff.Mode.XOR),
				new PorterDuffXfermode(PorterDuff.Mode.DARKEN),
				new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN),
				new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY),
				new PorterDuffXfermode(PorterDuff.Mode.SCREEN) };

		private final String[] sLabels = { "Clear", "Src", "Dst", "SrcOver",
				"DstOver", "SrcIn", "DstIn", "SrcOut", "DstOut", "SrcATop",
				"DstATop", "Xor", "Darken", "Lighten", "Multiply", "Screen" };

		public XfModelSimpleView(Context context) {
			super(context);

			mSrcB = makeSrc(W, H);
			mDstB = makeDst(W, H);

			// make a ckeckerboard pattern
			Bitmap bm = Bitmap.createBitmap(new int[] { 0xFFFFFFFF, 0xFFCCCCCC,
					0xFFCCCCCC, 0xFFFFFFFF }, 2, 2, Bitmap.Config.RGB_565);
			mBG = new BitmapShader(bm, Shader.TileMode.REPEAT,
					Shader.TileMode.REPEAT);
			Matrix m = new Matrix();
			m.setScale(6, 6);
			mBG.setLocalMatrix(m);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			Paint paint = new Paint();
			
			paint.setColor(Color.rgb(255, 0, 0));
			canvas.drawRect(0, 0, 100, 100, paint);
			paint.setXfermode(sModes[3]);
			paint.setColor(Color.rgb(0, 255, 0));
			canvas.drawRect(0, 0, 50, 50, paint);
			
			
		}

		/**
		 * 
		 * @author tangyong
		 * 
		 */
		class OpenGLSimpleView extends GLSurfaceView {

			public OpenGLSimpleView(Context context) {
				super(context);
			}

			/**
			 * @author tangyong
			 */
			class openglSimpleRender implements Renderer {

				@Override
				public void onSurfaceCreated(GL10 gl, EGLConfig config) {

				}

				@Override
				public void onSurfaceChanged(GL10 gl, int width, int height) {

				}

				@Override
				public void onDrawFrame(GL10 gl) {

				}
			}
		}

		// create a bitmap with a circle, used for the "dst" image
		Bitmap makeDst(int w, int h) {
			Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(bm);
			Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

			p.setColor(0xFFFFCC44);
			c.drawOval(new RectF(0, 0, w * 3 / 4, h * 3 / 4), p);
			return bm;
		}

		// create a bitmap with a rect, used for the "src" image
		Bitmap makeSrc(int w, int h) {
			Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(bm);
			Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

			p.setColor(0xFF66AAFF);
			c.drawRect(w / 3, h / 3, w * 19 / 20, h * 19 / 20, p);
			return bm;
		}
	}

}
