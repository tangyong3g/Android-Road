package com.graphics.engine.gl.animator.motionfiler;


import com.graphics.engine.gl.ICleanup;
import com.graphics.engine.gl.animation.Transformation3D;

/**
 * 
 * <br>类描述: 运动过滤器
 * <br>功能详细描述:
 * <p>
 * <br>可以设置给视图，让视图做动画，替代Animation类，见{@link GLView#setMotionFilter(MotionFilter)}
 * 以及{@link GLView#startMotionFilter(MotionFilter)}。
 * <br>视图不需要监听动画更新，运动过滤器会自动控制视图的变换或者不透明度。
 * <br>支持对视图当前实际的区域响应触摸事件，而不是视图布局的区域。然而，对于3D变换，需要Z轴变化不能太多，
 * 因为没有使用射线检测，只使用简单的逆变换。
 * </p>
 * <ul> 使用者关注以下几个方法即可：
 * 	<li>{@link #start()}
 * 	<li>{@link #reverse()}
 * 	<li>{@link #relativeReverse()}
 * 	<li>{@link #cancle()}
 * </ul>
 * 使用{@link MotionFilterSet} 或者 {@link AnimatorSet}来调度多个运动过滤器。
 * 
 * @author  dengweiming
 * @date  [2013-10-9]
 */
public interface MotionFilter extends ICleanup {
	/**
	 * 指定的尺寸的单位为像素
	 */
	int ABSOLUTE = 0;

	/**
	 * 指定的尺寸其实是一个比例值，会与相应的视图的尺寸相乘得到实际的尺寸
	 */
	int RELATIVE_TO_SELF = 1;

	/**
	 * 指定的尺寸其实是一个比例值，会与相应的视图的父容器的尺寸相乘得到实际的尺寸
	 */
	int RELATIVE_TO_PARENT = 2;

	/**
	 * <br>功能简述: 如果需要，初始化动画的参数值
	 * <br>功能详细描述:
	 * <br>注意: 仅由框架调用
	 * @param width	视图宽度
	 * @param height 视图高度
	 * @param parentWidth 父容器宽度
	 * @param parentHeight 父容器高度
	 */
	void initializeIfNeeded(int width, int height, int parentWidth, int parentHeight);

	/**
	 * <br>功能简述: 计算当前动画的变换
	 * <br>功能详细描述:
	 * <br>注意: 仅由框架调用
	 * @param t 计算结果保存到这里
	 */
	void getTransformation(Transformation3D t);

	/**
	 * <br>功能简述: 获得动画缓存的变换
	 * <br>功能详细描述: 框架会对运动过滤器（或集合的根节点）缓存变换，在计算逆矩阵时用到。
	 * <br>注意: 仅由框架调用
	 * @return
	 * @see {@link #getInverseTransformation()}
	 */
	Transformation3D getTransformation();

	/**
	 * <br>功能简述: 设置逆变换矩阵为脏的，需要重新计算
	 * <br>功能详细描述: 用于检测触摸点是否位于变换后的区域中
	 * <br>注意: 仅由框架调用
	 */
	void setInverseTransformationDirty();

	/**
	 * <br>功能简述: 获得逆变换
	 * <br>功能详细描述:
	 * <br>注意: 仅由框架调用
	 * @return
	 */
	Transformation3D getInverseTransformation();
	
	/**
	 * <br>功能简述: 是否正在动画（包括开始的延时阶段）
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	boolean isRunning();

	/**
	 * <br>功能简述: 开始动画
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	void start();
	
	/**
	 * <br>功能简述: 取消动画
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	void cancel();

	/**
	 * <br>功能简述: 逆向播放动画
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	void reverse();

	/**
	 * <br>功能简述: 以当前相反的方向播放动画
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	void relativeReverse();

	/**
	 * <br>功能简述: 是否会改变变换矩阵
	 * <br>功能详细描述:
	 * <br>注意: 子类重载
	 * @return
	 */
	boolean willChangeTransformationMatrix();

	/**
	 * <br>功能简述: 是否会改变视图边界
	 * <br>功能详细描述:
	 * <br>注意: 子类重载
	 * @return
	 */
	boolean willChangeBounds();

	/**
	 * <br>功能简述: 是否改变不透明度
	 * <br>功能详细描述:
	 * <br>注意: 子类重载
	 * @return
	 */
	boolean hasAlpha();

}
