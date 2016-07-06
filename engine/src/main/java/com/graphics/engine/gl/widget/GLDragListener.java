package com.graphics.engine.gl.widget;


import com.graphics.engine.gl.animation.Transformation3D;
import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.view.GLView;
import com.graphics.engine.gl.math3d.Ray;

/**
 * 
 * <br>类描述: 拖动视图{@link GLDragView}的监听者
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-7-1]
 * @see {@link GLDragView}
 */
public interface GLDragListener {
	/**
	 * <br>功能简述: 拖动开始
	 * <br>功能详细描述:
	 * <br>注意: 每个listener都会收到
	 * @param view
	 */
	public void onDragStart(GLDragView view);
	
	/**
	 * <br>功能简述: 拖动结束
	 * <br>功能详细描述:
	 * <br>注意: 每个listener都会收到
	 * @param view
	 */
	public void onDragEnd(GLDragView view);
	
	/**
	 * <br>功能简述: 检查是否触摸到
	 * <br>功能详细描述: 
	 * <br>注意: 在这里只检查是否包含触摸点或者和触摸射线相交，不要修改自己的状态
	 * @param view
	 * @param x
	 * @param y
	 * @param ray
	 * @return {@link GLDragView#MISS},{@link GLDragView#PENDING},{@link GLDragView#HIT}
	 */
	public int onCheckTouch(GLDragView view, float x, float y, Ray ray);
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return 是否更新了变换
	 */
	public boolean onDragMove(GLDragView view, float x, float y, Ray ray);
	
	/**
	 * <br>功能简述: 触摸进入，由非命中转为命中
	 * <br>功能详细描述: 
	 * <br>注意:
	 * @param view
	 */
	public void onDragEnter(GLDragView view);
	
	/**
	 * <br>功能简述:触摸离开，由命中转为非命中
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param view
	 */
	public void onDragExit(GLDragView view);
	
	/**
	 * <br>功能简述: 触摸悬停
	 * <br>功能详细描述:
	 * <br>注意:
	 * 
	 * @return 下一次悬停检测的启动延时。如果需要用动画（例如自动滚屏）来响应，则返回动画所需时间。
	 */
	public long onDragHover(GLDragView view, float x, float y, Ray ray);
	
	/**
	 * <br>功能简述: 拖动停放，对源listener的回调
	 * <br>功能详细描述:
	 * <br>注意: 这个方法会在{@link #onDropTo(GLDragView, float, float, Ray, GLDragListener)}之 <em>后</em> 调用，
	 * 当source和this是同一个对象时，如果有必要，应该是先remove被拖动的视图，再add回去，并且只在其中一个方法里面做。
	 * @param target up时检测到的停放GLDragLisener对象，null表示找不到对象
	 * @param dropped 如果是true，则拖拽被target接受，如果是false，表示找不到target或者target不接受，
	 * 可以做回归原位的动画并返回true，或者返回false让拖拽层处理
	 * @return 
	 * @see {@link #onDropTo(GLDragView, float, float, Ray, GLDragListener)}
	 */
	public boolean onDropFrom(GLDragView view, float x, float y, Ray ray, GLDragListener target, boolean dropped);
	
	/**
	 * <br>功能简述: 拖动停放，对包含目标位置的listener的回调
	 * <br>功能详细描述:
	 * <br>注意: 这个方法会在{@link #onDropFrom(GLDragView, float, float, Ray, GLDragListener)}之 <em>前</em> 调用，
	 * 当source和this是同一个对象时，如果有必要，应该是先remove被拖动的视图，再add回去，并且只在其中一个方法里面做。
	 * @param view
	 * @param x
	 * @param y
	 * @param ray
	 * @param source 
	 * @return 是否接受停放
	 * @see {@link #onDropFrom(GLDragView, float, float, Ray, GLDragListener)}
	 */
	public boolean onDropTo(GLDragView view, float x, float y, Ray ray, GLDragListener source);
	
	/**
	 * <br>功能简述: 绘制被拖动的视图
	 * <br>功能详细描述: 如果需要自定义绘制效果，例如在被拖动的视图上面再绘制其他内容，就重载本方法
	 * <br>注意:
	 * @param view
	 * @param canvas
	 * @param draggedView
	 * @param t
	 * @return
	 */
	public boolean onDrawDraggedView(GLDragView view, GLCanvas canvas, GLView draggedView, Transformation3D t);
	
	/**
	 * <br>功能简述: 是否可见
	 * <br>功能详细描述: 不可见时不会被触摸到
	 * <br>注意:
	 * @return
	 */
	public int getVisibility();
	
}