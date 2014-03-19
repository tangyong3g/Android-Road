package com.ty.exsample_unit_4;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

		Bitmap mBitMapTemp;

		MaskFilter blurMaskFilter;

		public SampleView(Context context) {
			super(context);

			blurMaskFilter = new BlurMaskFilter(1000, Blur.NORMAL);

			java.io.InputStream is;
			is = context.getResources().openRawResource(
					com.example.android_begin_gl_3d.R.drawable.bg_one);

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

			mBitMapTemp = BoxBlurFilter(mBitMapOne);
			//		   
			//		   Gdx.app.log("info", "height:\t"+heigt+"width:\t"+width);

			is = context.getResources().openRawResource(
					com.example.android_begin_gl_3d.R.drawable.ic_launcher);
			mBitMapTwo = BitmapFactory.decodeStream(is);

			// create a deep copy of it using getPixels() into different configs
			int w = mBitMapTwo.getWidth();
			int h = mBitMapTwo.getHeight();

			int[] pixels = new int[w * h];
			mBitMapTwo.getPixels(pixels, 0, w, 0, 0, w, h);

			float scaleInt = w * 0.5f;
			int scaleIntI = (int) scaleInt;

			mBitMapThree = Bitmap.createBitmap(pixels, 0, w, scaleIntI, (int) (h * 0.5f),
					Bitmap.Config.ARGB_8888);
			mBitMapFour = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_4444);

		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			setFocusable(true);

			canvas.drawColor(0xFFCCCCCC);

			Paint p = new Paint();
			//			p.setMaskFilter(blurMaskFilter);
			p.setAlpha(255);

			canvas.drawBitmap(mBitMapOne, 10, 10, null);
			canvas.drawBitmap(mBitMapTemp, 10, 10, p);
			
			
			//			canvas.drawBitmap(mBitMapTwo, 10, 170, p);
			//			canvas.drawBitmap(mBitMapThree, 110, 170, p);
			//			canvas.drawBitmap(mBitMapFour, 210, 170, p);

		}

	}

	/**
	 * 高斯模糊
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap convertToBlur(Bitmap bmp) {
		// 高斯矩阵
		int[] gauss = new int[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 };
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		int pixR = 0;
		int pixG = 0;
		int pixB = 0;
		int pixColor = 0;
		int newR = 0;
		int newG = 0;
		int newB = 0;
		int delta = 64; // 值越小图片会越亮，越大则越暗
		int idx = 0;
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 1, length = height - 1; i < length; i++) {
			for (int k = 1, len = width - 1; k < len; k++) {
				idx = 0;
				for (int m = -1; m <= 1; m++) {
					for (int n = -1; n <= 1; n++) {
						pixColor = pixels[(i + m) * width + k + n];
						pixR = Color.red(pixColor);
						pixG = Color.green(pixColor);
						pixB = Color.blue(pixColor);
						newR = newR + pixR * gauss[idx];
						newG = newG + pixG * gauss[idx];
						newB = newB + pixB * gauss[idx];
						idx++;
					}
				}
				newR /= delta;
				newG /= delta;
				newB /= delta;
				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));
				pixels[i * width + k] = Color.argb(255, newR, newG, newB);
				newR = 0;
				newG = 0;
				newB = 0;
			}
		}
		bmp.recycle();
		newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
		return newBmp;
	}

	/** 水平方向模糊度 */
	private static float hRadius = 5;
	/** 竖直方向模糊度 */
	private static float vRadius = 5;
	/** 模糊迭代度 */
	private static int iterations = 7;

	public static Bitmap BoxBlurFilter(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
		for (int i = 0; i < iterations; i++) {
			blur(inPixels, outPixels, width, height, hRadius);
			blur(outPixels, inPixels, height, width, vRadius);
		}
		blurFractional(inPixels, outPixels, width, height, hRadius);
		blurFractional(outPixels, inPixels, height, width, vRadius);
		bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
//		Drawable drawable = new BitmapDrawable(bitmap);
//		return drawable;
		return bitmap;
	}

	public static void blur(int[] in, int[] out, int width, int height, float radius) {
		int widthMinus1 = width - 1;
		int r = (int) radius;
		int tableSize = 2 * r + 1;
		int divide[] = new int[256 * tableSize];

		for (int i = 0; i < 256 * tableSize; i++)
			divide[i] = i / tableSize;

		int inIndex = 0;

		for (int y = 0; y < height; y++) {
			int outIndex = y;
			int ta = 0, tr = 0, tg = 0, tb = 0;

			for (int i = -r; i <= r; i++) {
				int rgb = in[inIndex + clamp(i, 0, width - 1)];
				ta += (rgb >> 24) & 0xff;
				tr += (rgb >> 16) & 0xff;
				tg += (rgb >> 8) & 0xff;
				tb += rgb & 0xff;
			}

			for (int x = 0; x < width; x++) {
				out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8)
						| divide[tb];

				int i1 = x + r + 1;
				if (i1 > widthMinus1)
					i1 = widthMinus1;
				int i2 = x - r;
				if (i2 < 0)
					i2 = 0;
				int rgb1 = in[inIndex + i1];
				int rgb2 = in[inIndex + i2];

				ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
				tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
				tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
				tb += (rgb1 & 0xff) - (rgb2 & 0xff);
				outIndex += height;
			}
			inIndex += width;
		}
	}

	public static void blurFractional(int[] in, int[] out, int width, int height, float radius) {
		radius -= (int) radius;
		float f = 1.0f / (1 + 2 * radius);
		int inIndex = 0;

		for (int y = 0; y < height; y++) {
			int outIndex = y;

			out[outIndex] = in[0];
			outIndex += height;
			for (int x = 1; x < width - 1; x++) {
				int i = inIndex + x;
				int rgb1 = in[i - 1];
				int rgb2 = in[i];
				int rgb3 = in[i + 1];

				int a1 = (rgb1 >> 24) & 0xff;
				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >> 8) & 0xff;
				int b1 = rgb1 & 0xff;
				int a2 = (rgb2 >> 24) & 0xff;
				int r2 = (rgb2 >> 16) & 0xff;
				int g2 = (rgb2 >> 8) & 0xff;
				int b2 = rgb2 & 0xff;
				int a3 = (rgb3 >> 24) & 0xff;
				int r3 = (rgb3 >> 16) & 0xff;
				int g3 = (rgb3 >> 8) & 0xff;
				int b3 = rgb3 & 0xff;
				a1 = a2 + (int) ((a1 + a3) * radius);
				r1 = r2 + (int) ((r1 + r3) * radius);
				g1 = g2 + (int) ((g1 + g3) * radius);
				b1 = b2 + (int) ((b1 + b3) * radius);
				a1 *= f;
				r1 *= f;
				g1 *= f;
				b1 *= f;
				out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
				outIndex += height;
			}
			out[outIndex] = in[width - 1];
			inIndex += width;
		}
	}

	public static int clamp(int x, int a, int b) {
		return (x < a) ? a : (x > b) ? b : x;
	}

}
