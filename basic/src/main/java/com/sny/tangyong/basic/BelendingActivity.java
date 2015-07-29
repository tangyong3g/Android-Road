package com.sny.tangyong.basic;

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
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 本单元内容是要掌握多种混合的方法
 * 
 * @author tangyong
 * 
 * 
 */

public class BelendingActivity extends Activity {

	XfModelSimpleView mXfView;
	LinearLayout mContainer;
	SimpleView mSimple;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutParams params = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		mXfView = new XfModelSimpleView(this);
		// mXfView.setLayoutParams(params);

		mContainer = new LinearLayout(this);
		mContainer.setOrientation(LinearLayout.VERTICAL);

		mSimple = new SimpleView(this);

		// mContainer.addView(mXfView);
		mContainer.addView(mSimple);
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

			canvas.drawColor(Color.WHITE);
			// 启用抗锯齿
			Paint labelP = new Paint(Paint.ANTI_ALIAS_FLAG);

			// 设置文本位置
			labelP.setTextAlign(Paint.Align.CENTER);

			// 画笔
			Paint paint = new Paint();
			paint.setFilterBitmap(false);

			// 移动画布
			canvas.translate(15, 35);

			int x = 0;
			int y = 0;

			for (int i = 0; i < sModes.length; i++) {

				paint.setStyle(Paint.Style.STROKE);
				paint.setShader(null);
				canvas.drawRect(x - 0.5f, y - 0.5f, x + W + 0.5f, y + H + 0.5f,
						paint);

				paint.setStyle(Paint.Style.FILL);
				paint.setShader(mBG);
				canvas.drawRect(x, y, x + W, y + H, paint);

				/*	*/
				int sc = canvas.saveLayer(x, y, x + W, y + H, null,
						Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
								| Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
								| Canvas.FULL_COLOR_LAYER_SAVE_FLAG
								| Canvas.CLIP_TO_LAYER_SAVE_FLAG);

				canvas.translate(x, y);
				canvas.drawBitmap(mDstB, 0, 0, paint);
				paint.setXfermode(sModes[i]);
				canvas.drawBitmap(mSrcB, 0, 0, paint);
				paint.setXfermode(null);
				canvas.restoreToCount(sc);

				canvas.drawText(sLabels[i], x + W / 2, y - labelP.getTextSize()
						/ 2, labelP);

				x += W + 10;

				// wrap around when we've drawn enough for one row
				if ((i % ROW_MAX) == ROW_MAX - 1) {
					x = 0;
					y += H + 30;
				}

			}
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

	}

	/**
	 * 
	 * @author tangyong
	 * 
	 */
	class SimpleView extends View {

		Bitmap mSrc;
		Bitmap mDesc;
		Matrix mMatrix = new Matrix();

		public SimpleView(Context context) {
			super(context);

			mSrc = makeSrcDemo(200, 200);
			mDesc = makeDstDemo(200, 200);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			Paint paint = new Paint();
			canvas.drawBitmap(mSrc, mMatrix, paint);
			paint.setXfermode(sModes[4]);
			canvas.drawBitmap(mDesc, mMatrix, paint);
			paint.setXfermode(null);
		}
	}

	// create a bitmap with a circle, used for the "dst" image
	Bitmap makeDstDemo(int w, int h) {
		Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bm);
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

		p.setColor(Color.argb(1, 0, 255, 0));
		c.drawOval(new RectF(0, 0, w, h), p);
		return bm;
	}

	// create a bitmap with a rect, used for the "src" image
	Bitmap makeSrcDemo(int w, int h) {
		Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bm);
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setColor(Color.argb(255, 255, 0, 0));
		c.drawRect(0, 0, w, h, p);
		return bm;
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

		p.setColor(Color.RED);
		c.drawRect(0, 0, w, h, p);
		return bm;

	}

}
