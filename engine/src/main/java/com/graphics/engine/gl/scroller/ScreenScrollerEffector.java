package com.graphics.engine.gl.scroller;


import com.graphics.engine.gl.graphics.GLCanvas;

/**
 * 分屏滚动器的特效器
 * 
 * @author dengweiming
 * 
 */
public interface ScreenScrollerEffector {
	/**
	 * 设置具体某种特效的类型
	 * 
	 * @param type
	 */
	void setType(int type);

	/**
	 * 使用随机选择特效的时候，选取下一个特效
	 */
	void updateRandomEffect();

	/**
	 * 绘制视图
	 * 
	 * @param canvas
	 * @return
	 */
	boolean onDraw(GLCanvas canvas);

	/**
	 * 被加载时的响应
	 * 
	 * @param container
	 */
	void onAttach(ScreenScrollerListener container);

	/**
	 * 被卸载时的响应
	 */
	void onDetach();

	/**
	 * 视图大小或者滚动方向变化时的响应
	 * 
	 * @param w
	 * @param h
	 * @param orientation
	 */
	void onSizeChanged(int w, int h, int orientation);

	/**
	 * 获取期望的最大过冲比例[0, 50)
	 * 
	 * @return 0表示不使用过冲插值器（切屏时没有回弹的效果）
	 */
	int getMaxOvershootPercent();

	/**
	 * 设置绘图质量
	 * 
	 * @param quality
	 */
	void setDrawQuality(int quality);

	/**
	 * 关闭时的处理
	 */
	void recycle();

	/**
	 * 设置屏幕之前的间隙
	 * 
	 * @param gap
	 */
	void setScreenGap(int gap);

	/**
	 * 设置上边距
	 * 
	 * @param top
	 */
	void setTopPadding(int top);

	/**
	 * 设置是否可以上下滑动
	 * 
	 * @param verticalSlide
	 */
	void setVerticalSlide(boolean verticalSlide);
	

	/**
	 * 是否需要启用Next Widget的绘图缓冲
	 * @return
	 */
	boolean isNeedEnableNextWidgetDrawingCache();
	
	/**
	 * 是否禁用壁纸滚动
	 * @return
	 */
	boolean disableWallpaperScrollDelay();
	
	/**
	 * 滚动开始
	 * @param source
	 */
	void onScrollStart();
	
	/**
	 * 滚动结束
	 * @param source
	 */
	void onScrollEnd();
	
	/**
	 * 甩动
	 */
	void onFlipStart();
	
	/**
	 * 甩动中断
	 * @param source
	 */
	void onFlipInterupted();
	
	/**
	 * 是否动画中
	 * @return
	 */
	boolean isAnimationing();
	
	/**
	 * 主题切换
	 */
	void onThemeSwitch();
	
	/**
	 * clean
	 */
	void cleanup();

	/**
	 * <br>功能简述:获取具体特效
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	Object getEffector();
}
