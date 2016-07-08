package com.graphics.engine.graphics.geometry;

import com.graphics.engine.graphics.GLClearable;
import com.graphics.engine.graphics.GLVBO;

import android.opengl.GLES20;

/**
 * <br>类描述: 通用的物体类
 * <br>功能详细描述:
 * 物体由若干顶点组成，每个顶点具有一组属性，默认的有位置，纹理坐标，法线（没有使用光照则不需要）。
 * 子类可以增加一些扩展属性。
 * <ul> 顶点按一定的绘制模式来组织：
 * 	<li>{@link #POINTS}</li>
 * 	<li>{@link #LINE_STRIP}</li>
 * 	<li>{@link #LINE_LOOP}</li>
 * 	<li>{@link #LINES}</li>
 * 	<li>{@link #TRIANGLE_STRIP}</li>
 * 	<li>{@link #TRIANGLE_FAN}</li>
 * 	<li>{@link #TRIANGLES}</li>
 * </ul>
 * 顶点绘制的时候可以采用顶点索引的方式，避免同一个顶点会有多个拷贝，浪费内存以及影响效率，如果子类
 * 采用这种方式，则{@link #getIndexArray()}返回结果不为null，这意味着子类需要负责该返回结果的创建。
 * <ul>常用的一些方法：
 * 	<li>{@link #setBounds(float, float, float, float)}</li>
 * 	<li>{@link #setTexcoords(float, float, float, float)}</li>
 * </ul>
 * 物体可以使用渲染器{@link GLObjectRender}来绘制，见
 * {@link GLObjectRender#draw(com.graphics.engine.graphics.GLCanvas, GLObject)}。
 * 
 * @author  dengweiming
 * @date  [2013-10-22]
 */
public abstract class GLObject implements GLClearable {
	/** 绘制模式：点，每个顶点独立 */
	public final static int POINTS = 0;
	/** 绘制模式：线段带，每个顶点依次连接 */
	public final static int LINE_STRIP = 1;
	/** 绘制模式：线段圈，每个顶点依次连接，首尾两个顶点连接 */
	public final static int LINE_LOOP = 2;
	/** 绘制模式：线段，每两个顶点连接 */
	public final static int LINES = 3;
	/** 绘制模式：填充的三角形带，第一二三个顶点连接成逆时针的三角形，第二三四个顶点连接成顺时针的三角形，依次类推。 */
	public final static int TRIANGLE_STRIP = 4;
	/** 绘制模式：填充的三角形扇，第一个顶点为公共顶点，其余每两个顶点和公共顶点连接 */
	public final static int TRIANGLE_FAN = 5;
	/** 绘制模式：填充的三角形，每三个顶点连接 */
	public final static int TRIANGLES = 6;
	
	/** 绘制模式对应的GL常量数组 */
	public final static int[] MODE = {
		GLES20.GL_POINTS,
		GLES20.GL_LINE_STRIP,
		GLES20.GL_LINE_LOOP,
		GLES20.GL_LINES,
		GLES20.GL_TRIANGLE_STRIP,
		GLES20.GL_TRIANGLE_FAN,
		GLES20.GL_TRIANGLES,
	};
	
	protected int mPositionComponent = 3;
	protected int mTexcoordComponent = 2;
	protected int mNormalComponent = 3;

	protected int mMode;
	protected int mVertexCount;
	protected int mIndexCount;
	protected short[] mIndexArray;
	
	protected int mPositionElements;
	protected float[] mPositionArray;
	protected int mTexcoordElements;
	protected float[] mTexcoordArray;
	protected int mNormalElements;
	protected float[] mNormalArray;
	
	/**
	 * 获取顶点数目
	 */
	public final int getVertexCount() {
		return mVertexCount;
	}
	
	/**
	 * 获取顶点索引数目
	 */
	public final int getIndexCount() {
		return mIndexCount;
	}
	
	/**
	 * 获取顶点位置分量数目，一般是2或者3
	 */
	public final int getPositionComponent() {
		return mPositionComponent;
	}
	
	/**
	 * 获取顶点纹理坐标分量数目，一般是2
	 */
	public final int getTexcoordComponent() {
		return mTexcoordComponent;
	}
	
	/**
	 * 获取顶点法线分量数目，一般是3
	 */
	public final int getNormalComponent() {
		return mNormalComponent;
	}
	
	/**
	 * 获取顶点位置数组元素个数，等于分量乘以顶点个数
	 */
	public final int getPositionElements() {
		return mPositionElements;
	}
	
	/**
	 * 获取顶点纹理坐标数组元素个数，等于分量乘以顶点个数
	 */
	public final int getTexcoordElements() {
		return mTexcoordElements;
	}
	
	
	/**
	 * 获取顶点法线数组元素个数，等于分量乘以顶点个数
	 */
	public final int getNormalElements() {
		return mNormalElements;
	}
	
	/**
	 * <br>功能简述: 获取顶点索引数组
	 * <br>功能详细描述:
	 * <br>注意: 子类如果采用顶点索引的方式，则需要创建顶点索引数组，渲染时使用
	 * {@link GLES20#glDrawElements(int, int, int, java.nio.Buffer)}方法
	 * @return 顶点索引数组。可以为null。
	 * @see {@link #getIndexCount()}
	 */
	public final short[] getIndexArray() {
		return mIndexArray;
	}
	
	/**
	 * 获取顶点位置数组
	 * @see {@link #getPositionElements()}
	 */
	public final float[] getPositionArray() {
		return mPositionArray;
	}
	
	/**
	 * 获取顶点纹理坐标数组
	 * @see {@link #getTexcoordElements()}
	 */
	public final float[] getTexcoordArray() {
		return mTexcoordArray;
	}
	
	/**
	 * 获取顶点法线数组
	 * @see {@link #getNormalElements()}
	 */
	public final float[] getNormalArray() {
		return mNormalArray;
	}
	
	/**
	 * 获取名字对应的顶点扩展属性的分量数目
	 * @param name 顶点扩展属性的名字
	 */
	public int getArrayComponentByName(String name) {
		return 0;
	}
	
	/**
	 * 获取名字对应的顶点扩展属性数组的元素数目，等于分量乘以顶点个数
	 * @param name 顶点扩展属性的名字
	 */
	public int getArrayElementsByName(String name) {
		return 0;
	}
	
	/**
	 * 获取名字对应的顶点扩展属性数组
	 * @param name 顶点扩展属性的名字
	 * @see {@link #getArrayElementsByName(String)}
	 */
	public float[] getArrayByName(String name) {
		return null;
	}
	
	/**
	 * <br>功能简述: 获取绘制模式（也即顶点组织的模式，点，线，折线，三角形等）
	 * <br>功能详细描述:
	 * <br>注意: 
	 * @return {@link #POINTS}, {@link #LINES}, {@link #TRIANGLES}等
	 */
	public int getDrawMode() {
		return mMode;
	}
	
	/**
	 * <br>功能简述: 设置边界
	 * <br>功能详细描述: 边界是2D的，Z轴上的边界如何设置由子类实现
	 * <br>注意:
	 * @param left		左边界
	 * @param top		上边界
	 * @param right		右边界
	 * @param bottom	下边界
	 */
	public void setBounds(float left, float top, float right, float bottom) {
	}
	
	/**
	 * <br>功能简述: 设置纹理坐标
	 * <br>功能详细描述: 具体来说，对每个顶点怎样分配纹理坐标，由子类实现。
	 * <br>注意:
	 * @param u1 纹理范围的左边界，[0..1]
	 * @param v1 纹理范围的上边界，[0..1]
	 * @param u2 纹理范围的右边界，[0..1]
	 * @param v2 纹理范围的下边界，[0..1]
	 */
	public void setTexcoords(float u1, float v1, float u2, float v2) {
	}
	
	/**
	 * <br>功能简述: 更新顶点法线
	 * <br>功能详细描述: 如果需要光照的话，并且顶点间相对位置改变了，就需要重新计算顶点法线
	 * <br>注意:
	 */
	public void updateNormal() {
	}
	
	/**
	 * 设置是否使用VBO保存数据
	 * @param use 是否使用
	 * @param clear	如果不使用，则是否清除当前的VBO
	 * @return	该操作是否成功
	 * @see {@link GLVBO}
	 */
	public boolean setUseVBO(boolean use, boolean clear) {
		return !use;	//默认不支持VBO
	}
	
	/**
	 * 获取顶点索引的VBO
	 * @see {@link #setUseVBO(boolean, boolean)}
	 */
	public GLVBO getIndexVBO() {
		return null;
	}
	
	/**
	 * 获取顶点位置的VBO
	 * @see {@link #setUseVBO(boolean, boolean)}
	 */
	public GLVBO getPositionVBO() {
		return null;
	}
	
	/**
	 * 获取顶点纹理坐标的VBO
	 * @see {@link #setUseVBO(boolean, boolean)}
	 */
	public GLVBO getTexcoordVBO() {
		return null;
	}
	
	/**
	 * 获取顶点法线的VBO
	 * @see {@link #setUseVBO(boolean, boolean)}
	 */
	public GLVBO getNormalVBO() {
		return null;
	}
	
	/**
	 * 获取名字对应的顶点扩展属性的VBO
	 * @param name 顶点扩展属性的名字
	 * @see {@link #setUseVBO(boolean, boolean)}
	 */
	public GLVBO getVBOByName(String name) {
		return null;
	}
	
	@Override
	public void clear() {
		
	}
	
	@Override
	public void onClear() {
		
	}
	
	@Override
	public void yield() {
		
	}
	
	@Override
	public void onYield() {
		
	}
}
