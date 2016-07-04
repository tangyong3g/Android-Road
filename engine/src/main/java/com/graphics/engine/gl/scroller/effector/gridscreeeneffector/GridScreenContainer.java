package com.graphics.engine.gl.scroller.effector.gridscreeeneffector;

import com.go.gl.graphics.GLCanvas;

/**
 * 网格分屏的容器
 * @author dengweiming
 *
 */
public interface GridScreenContainer {

	/**
	//	 * 绘制整屏单元格
	//	 * @param canvas
	//	 * @param index		单元格的索引
	//	 */
	void drawScreen(GLCanvas canvas, int screen);

	/**
	 * 画出翻屏中每个view
	 * @param canvas
	 * @param screen 第几屏
	 * @param index view绝对下标
	 */
	void drawScreenCell(GLCanvas canvas, int screen, int index);
	
	/**
	 * <br>功能简述:画出翻屏中每个view，用指定透明值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param canvas
	 * @param screen
	 * @param index
	 * @param alpha
	 */
	void drawScreenCell(GLCanvas canvas, int screen, int index, int alpha);

	/**
	 * 获取分屏里面单元格的行数
	 * @return
	 */
	public int getCellRow();

	/**
	 * 获取分屏里面单元格的列数
	 * @return
	 */
	public int getCellCol();

	/**
	 * 获取单元格的数目
	 * @return
	 */
	public int getCellCount();

	/**
	 * 获取单元格的宽度
	 * @return
	 */
	public int getCellWidth();

	/**
	 * 获取单元格的高度
	 * @return
	 */
	public int getCellHeight();

	/**
	 * 获取单元格的宽度
	 * @return
	 */
	public int getWidth();

	/**
	 * 获取分屏的高度
	 * @return
	 */
	public int getHeight();

	/**
	 * 获取分屏里面的左空白
	 * @return
	 */
	public int getPaddingLeft();

	/**
	 * 获取分屏里面的右空白
	 * @return
	 */
	public int getPaddingRight();

	/**
	 * 获取分屏里面的上空白
	 * @return
	 */
	public int getPaddingTop();

	/**
	 * 获取分屏里面的下空白
	 * @return
	 */
	public int getPaddingBottom();

	/**
	 * 功能简述:画背景
	 * 功能详细描述:
	 * 注意:
	 * @param canvas
	 * @param screen
	 */
	public void drawScreenBackground(GLCanvas canvas, int screen);

}