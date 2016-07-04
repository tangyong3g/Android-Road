package com.graphics.engine.gl.graphics;

import java.nio.Buffer;

import com.go.gl.util.NdkUtil;

import android.content.res.Resources;
import android.opengl.GLES20;

/**
 * 
 * <br>类描述: 绘制渐变色的着色器
 * <br>功能详细描述:
 * <ul> 一些常用方法：
 * 	<li>{@link #getShader()} 获得一个实例
 * 	<li>{@link #setMatrix(float[], int)} 设置MVP矩阵
 * 	<li>{@link #setPosition(Buffer, int)} 设置顶点位置
 * 	<li>{@link #setColor(Buffer, int)} 设置顶点颜色
 * </ul>
 */
public class ColorAttributeShader extends GLShaderProgram {
	static ColorAttributeShader sShader;

	/** @hide */
	static void initInternalShaders() {
		if (sShader == null) {
			sShader = new ColorAttributeShader(ShaderStrings.COLOR_ATTRIBUTE_VERT, ShaderStrings.COLOR_ATTRIBUTE_FRAG);
			sShader.registerStatic();
		}
	}

	/**
	 * <br>功能简述: 获取一个静态实例
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public static ColorAttributeShader getShader() {
		return sShader;
	}

	int muMVPMatrixHandle;	//总变换矩阵引用id
	int maPositionHandle;	//顶点位置属性引用id
	int maColorHandle;		//顶点颜色属性引用id

	private ColorAttributeShader(String vertexSource, String fragmentSource) {
		super(vertexSource, fragmentSource);
	}

	private ColorAttributeShader(Resources res, String vertexFile, String fragmentFile) {
		super(res, vertexFile, fragmentFile);
	}

	@Override
	protected boolean onProgramCreated() {
		muMVPMatrixHandle = getUniformLocation("uMVPMatrix");
		maPositionHandle = getAttribLocation("aPosition");
		maColorHandle = getAttribLocation("aColor");
		return true;
	}

	@Override
	protected void onProgramBind() {
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		GLES20.glEnableVertexAttribArray(maColorHandle);
	}

	/**
	 * <br>功能简述: 设置MVP矩阵
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param m
	 * @param offset
	 */
	public void setMatrix(float[] m, int offset) {
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, m, offset);
	}

	/**
	 * <br>功能简述: 设置顶点位置数据
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param ptr
	 * @param component 位置分量个数（2或者3)
	 */
	public void setPosition(Buffer ptr, int component) {
		GLES20.glVertexAttribPointer(maPositionHandle, component, GLES20.GL_FLOAT, false, 0, ptr);
	}

	/**
	 * <br>功能简述: 在绑定VBO时，设置顶点位置数据
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param offset	数据在VBO中的偏移量
	 * @param component 位置分量个数（2或者3)
	 */
	public void setPosition(int offset, int component) {
		//		GLES20.glVertexAttribPointer(maPositionHandle, component, GLES20.GL_FLOAT,
		//				false, 0, offset);   //API Level 9
		NdkUtil.glVertexAttribPointer(maPositionHandle, component, GLES20.GL_FLOAT, false, 0,
				offset);
	}

	/**
	 * <br>功能简述: 设置顶点颜色数据
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param ptr
	 * @param component	颜色分量个数（3或者4)
	 */
	public void setColor(Buffer ptr, int component) {
		GLES20.glVertexAttribPointer(maColorHandle, component, GLES20.GL_FLOAT, false, 0, ptr);
	}

	/**
	 * <br>功能简述: 在绑定VBO时，设置顶点颜色数据
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param ptr
	 * @param component	颜色分量个数（3或者4)
	 */
	public void setColor(int offset, int component) {
		//		GLES20.glVertexAttribPointer(maPositionHandle, component, GLES20.GL_FLOAT,
		//				false, 0, offset);   //API Level 9
		NdkUtil.glVertexAttribPointer(maColorHandle, component, GLES20.GL_FLOAT, false, 0, offset);
	}

	@Override
	public String toString() {
		return "ColorAttributeShader";
	}
}
