package com.graphics.engine.gl.scroller;

import com.go.gl.graphics.GLCanvas;

/**
 * 
 * <br>类描述: 滚动特效接口
 * <br>功能详细描述:
 * 
 * @author  songsiyu
 * @date  [2012-9-5]
 */
public interface ScrollerEffector {
	/**
	 * 绘制视图
	 * 
	 * @param canvas
	 * @return
	 */
	public boolean onDraw(GLCanvas canvas);

	/**
	 * 被加载时的响应
	 * 
	 * @param scroller
	 * @param container
	 */
	public void onAttach(Scroller scroller, ScrollerListener container);

	/**
	 * 被卸载时的响应
	 */
	public void onDetach();

	/**
	 * 视图大小或者滚动方向变化时的响应
	 * 
	 * @param w
	 * @param h
	 * @param orientation
	 */
	public void onSizeChanged(int w, int h, int orientation);

	/**
	 * 设置绘图质量
	 * 
	 * @param quality
	 */
	public void setDrawQuality(int quality);
}
