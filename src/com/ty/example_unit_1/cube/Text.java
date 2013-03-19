package com.ty.example_unit_1.cube;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;

/**
 * 绘制文本纹理图
 * @author liuwenqin
 *
 */
public class Text {
	/**
	 * 绘制文本的纹理图
	 */
	private Bitmap mTextureBmp;
	/**
	 * 纹理图的内存画布
	 */
	private Canvas mBufferCanvas;
	private int mWidth;
	private int mHeight;
	private Paint mPaint;
	private int mLastFrameTime;

	public Text(int width) {
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(0xff000000);
		mPaint.setTextSize(16);
		FontMetrics fontMetrics = mPaint.getFontMetrics();
		int fontHeight = (int) (Math.ceil(fontMetrics.descent - fontMetrics.ascent) + 2);
		mTextureBmp = Bitmap.createBitmap(width, fontHeight, Bitmap.Config.ARGB_8888);
		mBufferCanvas = new Canvas(mTextureBmp);
		mWidth = width;
		mHeight = fontHeight;
		mLastFrameTime = 0;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	/**
	 * 刷新内存纹理图
	 * @param text 当前绘制的文本
	 * @return 新的文本纹理图
	 */
	public Bitmap changeText(int frameTime) {
		if (frameTime != mLastFrameTime) {
			String text = "绘制一帧耗时：" + frameTime + "毫秒";
			mPaint.setColor(0xffffffff);
			mBufferCanvas.drawRect(0, 0, mWidth, mHeight, mPaint);
			mPaint.setColor(0xff000000);
			mBufferCanvas.drawText(text, 0, 0, mPaint);
			mLastFrameTime = frameTime;
		}
		return mTextureBmp;
	}

	/**
	 * 回收图片资源
	 */
	public void release() {
		mTextureBmp.recycle();
		mTextureBmp = null;
	}
}