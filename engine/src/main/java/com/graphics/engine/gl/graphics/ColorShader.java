package com.graphics.engine.gl.graphics;

import java.nio.Buffer;

import com.go.gl.util.NdkUtil;

import android.content.res.Resources;
import android.opengl.GLES20;

/**
 * 
 * <br>类描述: 绘制单色的着色器
 * <br>功能详细描述:
 */
public class ColorShader extends GLShaderProgram {
	static ColorShader sShader;
	private static final float ONE_OVER_255 = 1 / 255.0f;	// CHECKSTYLE IGNORE

	/** @hide */
	static void initInternalShaders() {
		if (sShader == null) {
			sShader = new ColorShader(ShaderStrings.COLOR_VERT, ShaderStrings.COLOR_FRAG);
			sShader.registerStatic();
		}
	}

	/**
	 * <br>功能简述: 获取一个静态实例
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public static ColorShader getShader() {
		return sShader;
	}

	int muMVPMatrixHandle;	//总变换矩阵引用id
	int maPositionHandle;	//顶点位置属性引用id
	int muColorHandle;		//颜色引用id
	final float[] mColors = new float[4];	// CHECKSTYLE IGNORE

	private ColorShader(String vertexSource, String fragmentSource) {
		super(vertexSource, fragmentSource);
	}

	private ColorShader(Resources res, String vertexFile, String fragmentFile) {
		super(res, vertexFile, fragmentFile);
	}

	@Override
	protected boolean onProgramCreated() {
		maPositionHandle = getAttribLocation("aPosition");
		muMVPMatrixHandle = getUniformLocation("uMVPMatrix");
		muColorHandle = getUniformLocation("uColor");
		return true;
	}

	@Override
	protected void onProgramBind() {
		GLES20.glEnableVertexAttribArray(maPositionHandle);
	}

	/**
	 * 设置颜色
	 * <br>注意: 在GL线程上调用
	 */
	public void setColor(int color) {
		//从ARGB转成(r, g, b, a)的alpha-premultiplied格式
		// CHECKSTYLE IGNORE 5 LINES
		final float a = (color >>> 24) * ONE_OVER_255;
		mColors[0] = (color >>> 16 & 0xFF) * a * ONE_OVER_255;
		mColors[1] = (color >>> 8 & 0xFF) * a * ONE_OVER_255;
		mColors[2] = (color & 0xFF) * a * ONE_OVER_255;
		mColors[3] = a;
		GLES20.glUniform4fv(muColorHandle, 1, mColors, 0);
	}

	/**
	 * 设置颜色
	 * <br>注意: 在GL线程上调用
	 * @param color	r,g,b,a in [0, 1], alpha-premultiplied
	 */
	public void setColor(float[] color) {
		GLES20.glUniform4fv(muColorHandle, 1, color, 0);
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

	@Override
	public String toString() {
		return "ColorShader";
	}
}
