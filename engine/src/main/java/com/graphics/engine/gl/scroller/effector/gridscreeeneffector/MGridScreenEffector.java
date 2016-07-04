package com.graphics.engine.gl.scroller.effector.gridscreeeneffector;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.scroller.ScreenScroller;

import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;

/**
 * 
 * 类描述:网格特效的顶层抽象类
 * 功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-3]
 */
public abstract class MGridScreenEffector {

	public static final float HALF = 0.5f;
	public static final float HALF_ANGLE = 180f;
	public static final float FULL_ANGLE = 360f;
	public static final float RIGHT_ANGLE = 90f;
	public static final int FADE_ANGLE = 75;
	public static final float INTERPOLATE_END = 64f;
	public static final int ALPHA = 255;
	protected static final float VERTICAL_SENSITIVITY = 1.0f; //上下偏移灵敏度
	protected static final float MAX_VERTICAL_ANGLE = 45; //上下偏移最大角度
	protected static PaintFlagsDrawFilter sLowQuality = null;
	protected static PaintFlagsDrawFilter sMidQuality = new PaintFlagsDrawFilter(0,
			Paint.ANTI_ALIAS_FLAG);
	protected static PaintFlagsDrawFilter sHighQuality = new PaintFlagsDrawFilter(0,
			Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

	protected static float interpolate(float start, float end, float t) {
		return (end - start) * t + start;
	}

	protected GridScreenContainer mContainer;
	protected ScreenScroller mScroller;
	protected float mWidth;
	protected float mHeight;
	protected float mCenterX;
	protected float mCenterY;
	protected int mQuality;
	protected boolean mCombineBackground;
	protected boolean mVerticalSlide;

	protected float mVerticalX;

	public void onAttach(GridScreenContainer container, ScreenScroller scroller) {
		mContainer = container;
		mScroller = scroller;
		onSizeChanged(mScroller.getScreenWidth(), mScroller.getScreenHeight());
	}

	public void onDetach() {
		mContainer = null;
		mScroller = null;
	}

	public void onSizeChanged(int w, int h) {
		mWidth = w;
		mHeight = h;
		mCenterX = w * HALF;
		mCenterY = h * HALF;
	}

	// 是否后画当前屏
	protected boolean isCurrentScreenOnTop() {
		return false;
	}

	protected abstract void onDrawScreen(GLCanvas canvas, int screen, int offset);
	
	protected void onDrawScreen(GLCanvas canvas, int screen, float offset) {
		
	}
	
	protected void drawScreen(GLCanvas canvas, int screen, float offset, int topPadding, float scroll) {
		if (screen == ScreenScroller.INVALID_SCREEN) {
			return;
		}
		canvas.save();
		canvas.translate(offset + scroll, topPadding);
		onDrawScreen(canvas, screen, offset);
		canvas.restore();
	}
	
	

	public void drawScreen(GLCanvas canvas, int screen, int offset, int topPadding, int scroll) {
		if (screen == ScreenScroller.INVALID_SCREEN) {
			return;
		}
		canvas.save();
		canvas.translate(offset + scroll, topPadding);
		onDrawScreen(canvas, screen, offset);
		canvas.restore();
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
	final protected void requestQuality(GLCanvas canvas, int quality) {
		quality = Math.min(quality, mQuality);
		switch (quality) {
			case GridScreenEffector.DRAW_QUALITY_MID :
				canvas.setDrawFilter(sMidQuality);
				break;
			case GridScreenEffector.DRAW_QUALITY_HIGH :
				canvas.setDrawFilter(sHighQuality);
				break;
		}
	}

	public static void drawScreen(GridScreenContainer container, GLCanvas canvas, ScreenScroller scroller,
			int screen, int offset, int topPadding, int scroll) {
		//		if(screen == ScreenScroller.INVALID_SCREEN){
		//			return;
		//		}
		//		canvas.save();
		//		canvas.translate(scroll + offset, topPadding);
		//		final int row = container.getCellRow();
		//		final int col = container.getCellCol();	
		//		int index = row * col * screen;
		//		final int end = Math.min(container.getCellCount(), index + row * col);
		//		final int cellWidth = container.getCellWidth();
		//		final int cellHeight = container.getCellHeight();
		//		final int paddingLeft = container.getPaddingLeft();
		//		final int paddingTop = container.getPaddingTop();
		//		int lastX = 0, lastY = 0;
		//		for(int i = 0, cellY = paddingTop; i < row && index < end; ++i) {
		//			for(int j = 0, cellX = paddingLeft; j < col && index < end; ++j, ++index){
		//				canvas.translate(cellX - lastX, cellY - lastY);
		//				container.drawScreenCell(canvas,screen, index);
		//				lastX = cellX;
		//				lastY = cellY;
		//				cellX += cellWidth;
		//			}
		//			cellY += cellHeight;
		//		}
		//		canvas.restore();
		if (screen == ScreenScroller.INVALID_SCREEN) {
			return;
		}
		final int orientation = scroller.getOrientation();
		final int width = scroller.getScreenWidth();
		final int height = scroller.getScreenHeight();
		canvas.save();
		if (orientation == ScreenScroller.HORIZONTAL) {
			canvas.translate(scroll + offset, 0);
		} else {
			canvas.translate(0, scroll + offset);
		}
		//canvas.clipRect(0, 0, width, height);
		container.drawScreen(canvas, screen);
		canvas.restore();
	}

	/**
	 * 在桌面屏幕编辑的小屏幕状态下，画白色半透明的背景
	 * @param container
	 * @param canvas
	 * @param screen
	 * @param offset
	 * @param topPadding
	 * @param scroll
	 */
	public static void drawScreenBackground(GridScreenContainer container, GLCanvas canvas, int screen,
			int offset, int topPadding, int scroll) {
		//|| !(container instanceof Workspace)
		if (screen == ScreenScroller.INVALID_SCREEN) {
			return;
		}
		canvas.save();
		canvas.translate(scroll + offset, topPadding);
		container.drawScreenBackground(canvas, screen);
		canvas.restore();
	}

	protected float getAngleX(float t) {
		final float four = 4;
		final float yOffsetRatio = 2.1f;
		final float absT = Math.abs(t);
		//初始阶段影响角度值是x轴的偏移量，t2会快速到达1，以后影响上下角度值就取决于y轴偏移量
		float t2 = Math.min(Math.max(absT * four, mScroller.getCurrentDepth()), 1);

		//滑动系数，越大越灵敏
		float yOffset = mScroller.getTouchDeltaY() / (float) mScroller.getScreenHeight()
				* VERTICAL_SENSITIVITY;
		float rotateX = Math.max(-1, Math.min(yOffset * yOffsetRatio, 1)) * MAX_VERTICAL_ANGLE;
		mVerticalX = interpolate(mVerticalX, rotateX, t2);
		return mVerticalX * t2;
	}

	protected void rorateX(GLCanvas canvas, float angleX, float translateY) {
		//将X轴平移到屏幕中间
		canvas.translate(0, translateY);
		canvas.rotateAxisAngle(angleX, 1, 0, 0);
		canvas.translate(0, -translateY);
	}

	public void setVerticalSlide(boolean verticalSlide) {
		mVerticalSlide = verticalSlide;
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
		
	}
	
	public boolean isFloatAdapted() {
		return false;
	}
}
