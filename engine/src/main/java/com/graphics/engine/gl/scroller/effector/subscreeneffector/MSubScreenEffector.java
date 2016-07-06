package com.graphics.engine.gl.scroller.effector.subscreeneffector;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;

import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.graphics.GLDrawable;
import com.graphics.engine.gl.scroller.ScreenScroller;

/**
 * 
 * 类描述:整屏类滑动特效的顶层父类
 * 功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-3]
 */
public abstract class MSubScreenEffector {
	
	/** 低质量绘图 */
	public final static int DRAW_QUALITY_LOW = 0;
	/** 中等质量绘图 */
	public final static int DRAW_QUALITY_MID = 1;
	/** 高等质量绘图 */
	public final static int DRAW_QUALITY_HIGH = 2;
	
	protected static final float PI2 = (float) Math.PI / 2;
	protected static final float PI4 = PI2 / 2;

	protected static final float FULL_ANGLE = 360;
	protected static final float HALF_ANGLE = 180;
	protected static final float RIGHT_ANGLE = 90;
	protected static final float HALF = 0.5f;
	protected static final int ALPHA = 255;

	protected static final float VERTICAL_SENSITIVITY = 1.0f; //上下偏移灵敏度
	protected static final float MAX_VERTICAL_ANGLE = 45; //上下偏移最大角度
	protected static PaintFlagsDrawFilter sLowQuality = null;
	protected static PaintFlagsDrawFilter sMidQuality = new PaintFlagsDrawFilter(0,
			Paint.ANTI_ALIAS_FLAG);
	protected static PaintFlagsDrawFilter sHighQuality = new PaintFlagsDrawFilter(0,
			Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

	public int mOrientation;
	protected int mScreenSize;
	public int mWidth;
	public int mHeight;
	protected int mScroll;
	protected float mCenterX;
	protected float mCenterY;
	protected boolean mCombineBackground;
	protected int mOvershootPercent = 0;
	protected ScreenScroller mScroller;
	protected SubScreenContainer mContainer;
	protected int mQuality;
	protected boolean mNeedQuality = true;
	protected int mAlpha = ALPHA;
	
	protected boolean mReverse = false;

	protected boolean mVerticalSlide;
	protected float mVerticalX;
	
	private GLDrawable mBackground;
	private boolean mBackgroundEnable = false;

	protected void drawView(GLCanvas canvas, int screen, int offset, boolean first) {
		if (screen == ScreenScroller.INVALID_SCREEN) {
			//			if(!bgDrawn 
			//			   && mCombineBackground 
			//			   && !mScroller.mCycloid
			//			   && screen >= 0 
			//			   && screen < mScroller.getScreenCount()){
			//				// 作变换，并绘制背景
			//			}
			return;
		}
		//		final DrawFilter filter  = canvas.getDrawFilter();
		//		if(mNeedQuality)
		//		{
		//			requestQuality(canvas, SubScreenEffector.DRAW_QUALITY_HIGH);
		//		}
		int saveCount = canvas.save();
		if (onDrawScreen(canvas, screen, offset, first)) {
//			canvas.clipRect(0, 0, mWidth, mHeight);
			if (mCombineBackground) {
				mScroller.drawBackgroundOnScreen(canvas, screen);
			}
			if (isBackgroundEnabled()) {
				int alpha = canvas.getAlpha();
				int save = canvas.save();
				onDrawBackground(canvas, offset, first);
				canvas.setAlpha(alpha);
				canvas.restoreToCount(save);
			}
			if (mAlpha == ALPHA) {
				mContainer.drawScreen(canvas, screen);
			} else if (mAlpha > 0) {

				mContainer.drawScreen(canvas, screen, mAlpha);

			}
			//			mContainer.drawScreen(canvas, screen);
		}
		canvas.restoreToCount(saveCount);
		//		canvas.setDrawFilter(filter);
	}

	public void onSizeChanged() {
		mScreenSize = mScroller.getScreenSize();
		mOrientation = mScroller.getOrientation();
		mWidth = mScroller.getScreenWidth();
		mHeight = mScroller.getScreenHeight();
		mCenterX = mWidth * HALF;
		mCenterY = mHeight * HALF;
	}

	/**
	 *  是否后画第first屏
	 * @return
	 */
	protected boolean toReverse() {
		return mReverse;
	}

	public int getMaxOvershootPercent() {
		return mOvershootPercent;
	}

	public void onScrollChanged(int scroll, int offset) {
		mScroll = scroll;
	}

	public void onAttach(SubScreenContainer container, ScreenScroller scroller) {
		mContainer = container;
		mScroller = scroller;
		mScroller.setOvershootPercent(mOvershootPercent);
		onSizeChanged();
	}

	public void onDetach() {
		mScroller = null;
		mContainer = null;
	}

	public boolean isCombineBackground() {
		return mCombineBackground;
	}

	public void setDrawQuality(int quality) {
		mQuality = quality;
	}

	/**
	 * 
	 * @param canvas		假设当前canvas已经是低质量的，并且调用者在用完canvas之后要负责还原质量
	 * @param quality
	 */
	final protected void requestQuality(Canvas canvas, int quality) {
		quality = Math.min(quality, mQuality);
		switch (quality) {
			case SubScreenEffector.DRAW_QUALITY_MID :
				canvas.setDrawFilter(sMidQuality);
				break;
			case SubScreenEffector.DRAW_QUALITY_HIGH :
				canvas.setDrawFilter(sHighQuality);
				break;
		}
	}

	/**
	 * 
	 * @param canvas		画布
	 * @param offset		绘制的屏的偏移量
	 * @param first			是否是索引较小的屏（在左边或者上边）
	 */
	abstract protected boolean onDrawScreen(GLCanvas canvas, int screen, int offset, boolean first);
	
	/**
	 * <br>功能简述:绘制背景（非壁纸）
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param canvas
	 */
	protected void onDrawBackground(GLCanvas canvas, int offset, boolean first) {
		if (mBackground != null) {
			float t = (float) Math.sin((float) offset / mScreenSize * Math.PI);
			int alpha = (int) (255 - 255 * (1 - Math.abs(t)));
			mBackground.setAlpha(alpha);
			mBackground.draw(canvas);
		}
	}
	
	static void drawView(SubScreenContainer container, ScreenScroller scroller, GLCanvas canvas,
			int screen, float offset, boolean bgDrawn, boolean verticalSlide) {
		if (screen == ScreenScroller.INVALID_SCREEN) {
			return;
		}
		final int orientation = scroller.getOrientation();
		final int scroll = scroller.getScroll();
		final int width = scroller.getScreenWidth();
		final int height = scroller.getScreenHeight();
		canvas.save();
		if (orientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(scroll + offset, 0);
		} else {
			canvas.translate(0, scroll + offset);
		}
//		canvas.clipRect(0, 0, width, height);

		if (verticalSlide) {

			final float yOffsetRatio = 2.1f;
			final int four = 4;
			float t = Math.min((1.0f / width) * Math.abs(scroller.getCurrentScreenOffset()) * 2, 1);
			final float absT = Math.abs(t);
			//初始阶段影响角度值是x轴的偏移量，t2会快速到达1，以后影响上下角度值就取决于y轴偏移量
			float t2 = Math.min(Math.max(absT * four, scroller.getCurrentDepth()), 1);
			//滑动系数，越大越灵敏
			float yOffset = scroller.getTouchDeltaY() / (float) scroller.getScreenHeight()
					* VERTICAL_SENSITIVITY;
			float rotateX = Math.max(-1, Math.min(yOffset * yOffsetRatio, 1)) * MAX_VERTICAL_ANGLE;
			float angleX = 0;
			angleX = interpolate(angleX, rotateX, t2);
			canvas.translate(0, height / 2);
			canvas.rotateAxisAngle(angleX * t2, 1, 0, 0);
			canvas.translate(0, -height / 2);

		}

		if (!bgDrawn) {
			scroller.drawBackgroundOnScreen(canvas, screen);
		}
		container.drawScreen(canvas, screen);
		canvas.restore();
	}

	public void setVerticalSlide(boolean verticalSlide) {
		mVerticalSlide = verticalSlide;
	}

	protected void transformVertical(GLCanvas canvas, int offset) {
		final float rotateXOffset = -30f;
		final float yOffsetRatio = 2.1f;
		float yOffset = mScroller.getTouchDeltaY() / (float) mScroller.getScreenHeight();
		float rotateX = Math.max(-1, Math.min(yOffset * yOffsetRatio, 1)) * rotateXOffset;
		canvas.translate(mCenterX, mCenterY);
		canvas.rotateAxisAngle(rotateX, 1, 0, 0);
		canvas.translate(-mCenterX, -mCenterY);
	}

	protected static float interpolate(float start, float end, float t) {
		return (end - start) * t + start;
	}

	protected float getAngleX(float t) {
		return getAngleX(t, true);
	}
	
	protected float getAngleX(float t, boolean isOnFling) {
		final float yOffsetRatio = 2.1f;
		final int four = 4;
		final float absT = Math.abs(t);
		//初始阶段影响角度值是x轴的偏移量，t2会快速到达1，以后影响上下角度值就取决于y轴偏移量
		float t2 = isOnFling ? Math.min(Math.max(absT * four, mScroller.getCurrentDepth()), 1) : 1;
		//滑动系数，越大越灵敏
		float yOffset = mScroller.getTouchDeltaY() / (float) mScroller.getScreenHeight()
				* VERTICAL_SENSITIVITY;
		float rotateX = Math.max(-1, Math.min(yOffset * yOffsetRatio, 1)) * MAX_VERTICAL_ANGLE;
		mVerticalX = interpolate(mVerticalX, rotateX, t2);
		return mVerticalX * t2;
	}
	
	
	/**
	 * 是否需要启用Next Widget的绘图缓冲
	 * @return
	 */
	public boolean isNeedEnableNextWidgetDrawingCache() {
		return true;
	}
	
	/**
	 * 是否禁用壁纸滚动
	 * @return
	 */
	public boolean disableWallpaperScrollDelay() {
		return false;
	}
	
	/**
	 * 滚动开始
	 * @param source
	 */
	public void onScrollStart() {
		
	}
	
	/**
	 * 滚动结束
	 * @param source
	 */
	public void onScrollEnd() {
		
	}
	
	/**
	 * 甩动
	 */
	public void onFlipStart() {
		
	}
	
	/**
	 * 甩动中断
	 * @param source
	 */
	public void onFlipInterupted() {
		
	}
	
	/**
	 * 是否动画中
	 * @return
	 */
	public boolean isAnimationing() {
		return false;
	}
	
	/**
	 * 主题切换
	 */
	public void onThemeSwitch() {
		
	}
	
	/**
	 * clean
	 */
	public void cleanup() {
		mBackground = null;
	}

	public void setBackgroundEnable(boolean enable) {
		mBackgroundEnable = enable;
	}
	
	public boolean isBackgroundEnabled() {
		return mBackgroundEnable;
	}

	public void setBackgroundDrawable(GLDrawable d) {
		mBackground = d;
	}

}
