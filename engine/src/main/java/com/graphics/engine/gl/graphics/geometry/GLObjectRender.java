package com.graphics.engine.gl.graphics.geometry;


import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.graphics.GLClearable;

/**
 * <br>类描述: 物体渲染器
 * <br>功能详细描述:
 * 使用{@link #draw(GLCanvas, GLObject)}方法绘制{@link GLObject}。
 * <ul>实现类包括但不限于：
 * 	<li>{@link ColorGLObjectRender} 用于绘制纯色物体</li> 
 * 	<li>{@link TextureGLObjectRender} 用于绘制单贴图的物体</li>
 * </ul>
 * 
 * @author  dengweiming
 * @date  [2013-10-22]
 */
public interface GLObjectRender extends GLClearable {
	
	/**
	 * <br>功能简述: 绘制物体
	 * <br>功能详细描述:
	 * <br>注意: 支持{@link GLCanvas#setAlpha(int)}设置的淡化因子
	 * @param canvas
	 * @param object
	 */
	public void draw(GLCanvas canvas, GLObject object);
	
}
