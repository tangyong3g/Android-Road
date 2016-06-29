package com.ty.example_unit_1.cube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

public class Line {
	/**
	 * 着色器程序ID
	 */
	private int mProgram;
	
	/**
	 * 总变换矩阵句柄
	 */
	private int muMVPMatrixHandle;
	/**
	 * 顶点位置句柄
	 */
	private int maPositionHandle;
	/**
	 * 颜色句柄
	 */
	private int maColorHandle;
	/**	
	 * 顶点缓冲
	 */
	FloatBuffer mVertexBuffer;
	/**
	 * 颜色缓冲
	 */
	FloatBuffer mColorBuffer;

	private int mVertexAmount = 2;
	private float[] mVertices;
	
	public Line(ExerciseSurfaceView mv) {
		initVertex();
		initColors();
		initShader(mv);
	}
	
	private void initVertex() {
		mVertices = new float[6];
		int idx = -1;
		mVertices[++idx] = 1;
		mVertices[++idx] = 1;
		mVertices[++idx] = 5;
		mVertices[++idx] = -1;
		mVertices[++idx] = -1;
		mVertices[++idx] = 5;
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(mVertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asFloatBuffer();
		mVertexBuffer.put(mVertices);
		mVertexBuffer.position(0);
	}
	
	private void initColors() {
		final int amount = 2;
		final int colorSize = 4;
		float[] colors = new float[amount * colorSize];
		int idx = -1;
		//起点颜色
		colors[++idx] = 0.5f;
		colors[++idx] = 0.5f;
		colors[++idx] = 0.0f;
		colors[++idx] = 1.0f;

		//终点颜色
		colors[++idx] = 0.5f;
		colors[++idx] = 0.0f;
		colors[++idx] = 0.5f;
		colors[++idx] = 1.0f;

		ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
		cbb.order(ByteOrder.nativeOrder());
		mColorBuffer = cbb.asFloatBuffer();
		mColorBuffer.put(colors);
		mColorBuffer.position(0);
	}

	public void initVertex(float[] vertices) {
		for(int i = 0; i < vertices.length; i++) {
			mVertices[i] = vertices[i];
//			Log.d("LWQ", "Line initVertex vertices[" + i + "] = " + vertices[i]);
		}
		
		mVertexBuffer.put(mVertices);
		mVertexBuffer.position(0);
	}

	private void initShader(ExerciseSurfaceView mv) {
		String vertexSource = ShaderUtil.loadAssetsFromFile("vertex.sh", mv.getResources());
		String fragmentSource = ShaderUtil.loadAssetsFromFile("frag.sh", mv.getResources());
		mProgram = ShaderUtil.createProgram(vertexSource, fragmentSource);
		maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
		maColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
		muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
	}

	public void drawLine() {
		GLES20.glUseProgram(mProgram);
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
		GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4,
				mVertexBuffer);
		GLES20.glVertexAttribPointer(maColorHandle, 4, GLES20.GL_FLOAT, false, 4 * 4, mColorBuffer);
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		GLES20.glEnableVertexAttribArray(maColorHandle);
		GLES20.glLineWidth(1.0f);
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, mVertexAmount);
	}
	
}