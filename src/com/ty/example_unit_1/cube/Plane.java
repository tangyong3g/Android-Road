package com.ty.example_unit_1.cube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

public class Plane {

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
	/**
	 * 顶点索引缓冲
	 */
	ByteBuffer mIndexBuffer;

	private int mIndexAmount;
	/**
	 * 逆变换矩阵
	 */
	private float[] mCurMatrix;

	private float[] mColors;

	public Plane(ExerciseSurfaceView mv) {
		initVertex();
		initShader(mv);
	}

	public void setCurMatrix(float[] curMatrix) {
		mCurMatrix = curMatrix;
	}

	public float[] getCurMatrix() {
		return mCurMatrix;
	}

	private void initVertex() {
		final int amount = 4;
		final int coordSize = 3;
		float[] vertices = new float[amount * coordSize];
		int idx = -1;
		vertices[++idx] = 1f;
		vertices[++idx] = 1f;
		vertices[++idx] = 0f;

		vertices[++idx] = -1f;
		vertices[++idx] = 1f;
		vertices[++idx] = 0f;

		vertices[++idx] = -1f;
		vertices[++idx] = -1f;
		vertices[++idx] = 0f;

		vertices[++idx] = 1f;
		vertices[++idx] = -1f;
		vertices[++idx] = 0f;

		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);

		final int colorSize = 4;
		mColors = new float[amount * colorSize];
		idx = -1;
		mColors[++idx] = 1f;
		mColors[++idx] = 1f;
		mColors[++idx] = 0f;
		mColors[++idx] = 1f;

		mColors[++idx] = 1f;
		mColors[++idx] = 0f;
		mColors[++idx] = 1f;
		mColors[++idx] = 1f;

		mColors[++idx] = 0f;
		mColors[++idx] = 1f;
		mColors[++idx] = 1f;
		mColors[++idx] = 1f;

		mColors[++idx] = 0.5f;
		mColors[++idx] = 0.5f;
		mColors[++idx] = 0.5f;
		mColors[++idx] = 1f;

		ByteBuffer cbb = ByteBuffer.allocateDirect(mColors.length * 4);
		cbb.order(ByteOrder.nativeOrder());
		mColorBuffer = cbb.asFloatBuffer();
		mColorBuffer.put(mColors);
		mColorBuffer.position(0);

		final int indexAmount = 6;
		byte[] indexes = new byte[indexAmount];
		idx = -1;
		indexes[++idx] = 0;
		indexes[++idx] = 1;
		indexes[++idx] = 2;
		indexes[++idx] = 0;
		indexes[++idx] = 2;
		indexes[++idx] = 3;

		mIndexBuffer = ByteBuffer.allocateDirect(indexes.length);
		mIndexBuffer.put(indexes);
		mIndexBuffer.position(0);
		mIndexAmount = indexes.length;
	}

	public void initColorByTouch(float red, float green, float blue) {
		int idx = -1;
		for (int i = 0; i < 4; i++) {
			mColors[++idx] = red;
			mColors[++idx] = green;
			mColors[++idx] = blue;
			mColors[++idx] = 1.0f;
		}
		mColorBuffer.put(mColors);
		mColorBuffer.position(0);
	}
	
	public void initColorByUnTouch() {
		int idx = -1;
		mColors[++idx] = 1f;
		mColors[++idx] = 1f;
		mColors[++idx] = 0f;
		mColors[++idx] = 1f;

		mColors[++idx] = 1f;
		mColors[++idx] = 0f;
		mColors[++idx] = 1f;
		mColors[++idx] = 1f;

		mColors[++idx] = 0f;
		mColors[++idx] = 1f;
		mColors[++idx] = 1f;
		mColors[++idx] = 1f;

		mColors[++idx] = 0.5f;
		mColors[++idx] = 0.5f;
		mColors[++idx] = 0.5f;
		mColors[++idx] = 1f;
		
		mColorBuffer.put(mColors);
		mColorBuffer.position(0);
	}

	private void initShader(ExerciseSurfaceView mv) {
		String vertexSource = ShaderUtil.loadAssetsFromFile("vertex.sh", mv.getResources());
		String fragmentSource = ShaderUtil.loadAssetsFromFile("frag.sh", mv.getResources());
		mProgram = ShaderUtil.createProgram(vertexSource, fragmentSource);
		maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
		maColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
		muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
	}

	public void drawPlane() {
		GLES20.glUseProgram(mProgram);
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
		GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4,
				mVertexBuffer);
		GLES20.glVertexAttribPointer(maColorHandle, 4, GLES20.GL_FLOAT, false, 4 * 4, mColorBuffer);
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		GLES20.glEnableVertexAttribArray(maColorHandle);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndexAmount, GLES20.GL_UNSIGNED_BYTE,
				mIndexBuffer);
	}
}