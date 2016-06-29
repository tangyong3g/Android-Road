package com.ty.example_unit_2.opengl_2.loadmodel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.net.ssl.HandshakeCompletedListener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.badlogic.gdx.math.Vector3;
import com.ty.util.MatrixState;
import com.ty.util.ShaderUtil;

/**
 * 
 * @author tangyong
 * 
 */
public class LoadedObjectVertexOnly {

	int mProgram; // 自定义渲染管线ID
	int muMVPMatrixHandle;// 总变换矩阵引用
	int maPositionHandle; // 顶点位置属性引用
	int maNormalHandle; // 顶点法向量属性引用

	int maTexCoorHandle; // 纹理坐标属性引用
	String mVertexShader;// 顶点着色器代码脚本
	String mFragmentShader;// 片元着色器代码脚本

	FloatBuffer mVertexBuffer;// 顶点坐标数据缓冲
	FloatBuffer mTexCoorBuffer;// 顶点纹理坐标数据缓冲
	FloatBuffer mNormalBuffer;// 顶点纹理坐标数据缓冲

	Vector3 mModelCenterPosition; // 模型的中心位置

	int vCount = 0;
	float angle = 0.0f;
	public static float angleZ = 0.0f;

	public LoadedObjectVertexOnly(ModelView mv, float[] vertices,
			float[] textureCoors, float[] normalCoors, Vector3 centerPosition) {

		mModelCenterPosition = centerPosition;

		// 初始化顶点坐标与着色数据
		initVertexData(vertices, textureCoors, normalCoors);
		// 初始化shader
		initShader(mv);
	}

	private void checkGlError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e("Render", op + ": glError " + error);
			throw new RuntimeException(op + ": glError " + error);
		}
	}

	// 初始化顶点坐标的方法
	public void initVertexData(float[] vertices, float[] textureCoor,
			float[] normalCoor) {
		// 顶点坐标数据的初始化================begin============================
		vCount = vertices.length / 3;
		Log.i("tyler.tang", "顶点数:\t" + vCount);
		// 创建顶点坐标数据缓冲
		// vertices.length*4是因为一个整数四个字节
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());// 设置字节顺序
		mVertexBuffer = vbb.asFloatBuffer();// 转换为Float型缓冲
		mVertexBuffer.put(vertices);// 向缓冲区中放入顶点坐标数据
		mVertexBuffer.position(0);// 设置缓冲区起始位置
		// 特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
		// 转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
		// 顶点坐标数据的初始化================end============================

		/**/
		ByteBuffer tbb = ByteBuffer.allocateDirect(textureCoor.length * 4);
		tbb.order(ByteOrder.nativeOrder());
		mTexCoorBuffer = tbb.asFloatBuffer();
		mTexCoorBuffer.put(textureCoor);
		mTexCoorBuffer.position(0);

		// 顶点法向量数据缓冲
		/* */
		ByteBuffer nbb = ByteBuffer.allocateDirect(normalCoor.length * 4);
		nbb.order(ByteOrder.nativeOrder());
		mNormalBuffer = tbb.asFloatBuffer();
		mNormalBuffer.put(textureCoor);
		mNormalBuffer.position(0);// 设置缓冲区起始位置

	}

	public void drawSelf(int textureId) {
		// 制定使用某套着色器程序
		GLES20.glUseProgram(mProgram);
		float xOffset = mModelCenterPosition.x;
		// float yOffset = -mModelCenterPosition.y;
		// float zOffset = mModelCenterPosition.z;
		float yOffset = 0;
		float zOffset = 0;

		// MatrixState.rotate(angleZ, 0, 0, 1);

		// MatrixState.translate(xOffset, yOffset, zOffset);
		// MatrixState.rotate(angle, 0, 1, 0);
		// MatrixState.translate(-xOffset, -yOffset, -zOffset);

		// MatrixState.rotate(angle, 1, 1, 1);
		// 将最终变换矩阵传入着色器程序
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false,
				MatrixState.getFinalMatrix(), 0);

		// 将顶点位置数据传入渲染管线
		GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT,
				false, 3 * 4, mVertexBuffer);

		// 把纹理坐标传入渲染管线中去
		/**/
		GLES20.glVertexAttribPointer(maTexCoorHandle, 2, GLES20.GL_FLOAT,
				false, 2 * 4, mTexCoorBuffer);


		// 将顶点法向量数据传入渲染管线
		/*  */
		GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT, false,
				3 * 4, mNormalBuffer);

		// GLES20.glEnableVertexAttribArray(maNormalHandle);
		GLES20.glEnableVertexAttribArray(maTexCoorHandle);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

		// 启用顶点位置数据
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		// 绘制加载的物体
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);

		// angle += 0.8f;
	}

	// 初始化shader
	public void initShader(ModelView mv) {
		// 加载顶点着色器的脚本内容
		mVertexShader = ShaderUtil.loadFromAssetsFile(
				"data/unit2/shader/vertex.sh", mv.getResources());
		// 加载片元着色器的脚本内容
		mFragmentShader = ShaderUtil.loadFromAssetsFile(
				"data/unit2/shader/frag.sh", mv.getResources());
		// 基于顶点着色器与片元着色器创建程序
		mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
		// 获取程序中顶点位置属性引用
		maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
		// 获取程序中总变换矩阵引用
		muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		// 获取程序中顶点纹理坐标属性引用
		maTexCoorHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
		maNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
	}

	public void senorRatio(float x) {
		LoadedObjectVertexOnly.angleZ = (float) -180 * x / 9.8f;
		// LoadedObjectVertexOnly.angleZ = (float)-x;
	}

}
