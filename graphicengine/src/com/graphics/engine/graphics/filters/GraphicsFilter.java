package com.graphics.engine.graphics.filters;

import android.content.res.Resources;

import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.graphics.GLDrawable;

/**
 * 
 * <br>类描述: 图像处理特效类接口
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-5-14]
 */
public interface GraphicsFilter {
	
	/**
	 * <br>功能简述: 处理图像
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param canvas
	 * @param res
	 * @param drawable
	 * @param yieldWhenDone
	 * @return
	 */
	public abstract GLDrawable apply(GLCanvas canvas, Resources res, GLDrawable drawable, boolean yieldWhenDone);
	
	/**
	 * <br>功能简述: 更换原始图像时的回调
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void reset();
	
	/**
	 * <br>功能简述: 原始图像内容有更新时的回调
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void invalidate();
}
