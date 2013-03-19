package com.ty.example_unit_1.cube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.opengl.GLUtils;

public class Texture {
	private int mProgram;
	private int muMVPMatrixHandle;
	private int maPositionHandle;
	private int maTexCoorHandle;
	private FloatBuffer mVertexBuffer;
	private FloatBuffer mTexCoorBuffer;
	/**
	 * 纹理ID
	 */
	private int mTextureId;

	private int mCount = 0;
	/**
	 * 绘制文本纹理图的对象
	 */
	private Text mText;

	public Texture(ExerciseSurfaceView mv) {
		mText = new Text(mv.getWidth());

		initVertexData();
		initShader(mv);
	}

	private void initVertexData() {
		final int amount = 4;	//4个顶点
		final int coordSize = 3;	//3个坐标分量
		mCount = amount;
		float[] vertices = new float[mCount * coordSize];
		int idx = -1;
		vertices[++idx] = -4 * Constants.UNIT_SIZE;
		vertices[++idx] = 4 * Constants.UNIT_SIZE;
		vertices[++idx] = 0;

		vertices[++idx] = 4 * Constants.UNIT_SIZE;
		vertices[++idx] = 4 * Constants.UNIT_SIZE;
		vertices[++idx] = 0;

		vertices[++idx] = -4 * Constants.UNIT_SIZE;
		vertices[++idx] = 2 * Constants.UNIT_SIZE;
		vertices[++idx] = 0;

		vertices[++idx] = 4 * Constants.UNIT_SIZE;
		vertices[++idx] = 2 * Constants.UNIT_SIZE;
		vertices[++idx] = 0;

		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);

		//顶点纹理坐标数据的缓冲
		float[] texCoord = new float[] { 0, 0, 1, 0, 0, 1, 1, 1 };
		ByteBuffer cbb = ByteBuffer.allocateDirect(texCoord.length * 4);
		cbb.order(ByteOrder.nativeOrder());
		mTexCoorBuffer = cbb.asFloatBuffer();
		mTexCoorBuffer.put(texCoord);
		mTexCoorBuffer.position(0);
	}

	private void initShader(ExerciseSurfaceView mv) {
		String vertexSource = ShaderUtil.loadAssetsFromFile("text_vertex.sh", mv.getResources());
		String fragSource = ShaderUtil.loadAssetsFromFile("text_frag.sh", mv.getResources());
		mProgram = ShaderUtil.createProgram(vertexSource, fragSource);
		muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
		maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
	}

	private void changeTexture(int frameTime) {
		if (mTextureId != -1) {
			GLES20.glDeleteTextures(1, new int[] { mTextureId }, 0);
		}
		initTexture();
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);	//绑定纹理ID
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
				GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_CLAMP_TO_EDGE);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mText.changeText(frameTime), 0);
	}

	private void initTexture() {
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		mTextureId = textures[0];
	}

	public void drawFrame(int frameTime) {
		changeTexture(frameTime);
		
		GLES20.glUseProgram(mProgram);
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
		GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4,
				mVertexBuffer);
		GLES20.glVertexAttribPointer(maTexCoorHandle, 2, GLES20.GL_FLOAT, false, 2 * 4,
				mTexCoorBuffer);

		GLES20.glEnableVertexAttribArray(maPositionHandle);
		GLES20.glEnableVertexAttribArray(maTexCoorHandle);

		//绑定纹理
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mCount);
	}
}