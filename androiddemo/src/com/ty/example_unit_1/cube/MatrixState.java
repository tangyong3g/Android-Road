package com.ty.example_unit_1.cube;

import android.opengl.Matrix;

/**
 * 记录矩阵状态的堆栈
 * @author liuwenqin
 *
 */
public class MatrixState {
	private static float[][] mStack = new float[10][16];
	/**
	 * 当前变换矩阵在矩阵堆栈中的索引
	 */
	private static int mStackTop = -1;
	/**
	 * 视图矩阵
	 */
	private static float[] mMVMatrix = new float[16];
	/**
	 * 投影矩阵
	 */
	private static float[] mProjMatrix = new float[16];
	/**
	 * 获取具体物体的总变换矩阵
	 */
	private static float[] mMVPMatrix = new float[16];
	private static float[] mCurMatrix;

	public static void setInitStack() {
		mCurMatrix = new float[16];
		Matrix.setRotateM(mCurMatrix, 0, 0, 1, 0, 0);
	}

	/**
	 * 矩阵进栈
	 */
	public static void pushMatrix() {
		mStackTop++;
		for (int i = 0; i < 16; i++) {
			mStack[mStackTop][i] = mCurMatrix[i];
		}
	}

	/**
	 * 矩阵退栈
	 */
	public static void popMatrix() {
		for(int i = 0; i < 16; i++) {
			mCurMatrix[i] = mStack[mStackTop][i];
		}
		mStackTop--;
	}

	/**
	 * 绕指定向量旋转
	 * @param a 旋转角度
	 * @param x 旋转轴x分量
	 * @param y 旋转轴y分量
	 * @param z 旋转轴z分量
	 */
	public static void rotate(float a, float x, float y, float z) {
		Matrix.rotateM(mCurMatrix, 0, a, x, y, z);
	}

	/**
	 * 缩放
	 * @param x x方向缩放倍数
	 * @param y y方向缩放倍数
	 * @param z z方向缩放倍数
	 */
	public static void scale(float x, float y, float z) {
		Matrix.scaleM(mCurMatrix, 0, x, y, z);
	}

	/**
	 * 平移
	 * @param x x方向的平移量
	 * @param y y方向的平移量
	 * @param z z方向的平移量
	 */
	public static void translate(float x, float y, float z) {
		Matrix.translateM(mCurMatrix, 0, x, y, z);
	}

	/**
	 * 设置观察矩阵
	 * @param cx 观察点位置x坐标
	 * @param cy 观察点位置y坐标
	 * @param cz 观察点位置z坐标
	 * @param tx 目标位置x坐标
	 * @param ty 目标位置y坐标
	 * @param tz 目标位置z坐标
	 * @param upX 照相机向上的x分量
	 * @param upY 照相机向上的y分量
	 * @param upZ 照相机向上的z分量
	 */
	public static void setCamera(float cx, float cy, float cz, float tx, float ty, float tz,
			float upX, float upY, float upZ) {
		Matrix.setLookAtM(mMVMatrix, 0, cx, cy, cz, tx, ty, tz, upX, upY, upZ);
	}

	/**
	 * 正交投影
	 * @param left near面的左边
	 * @param right near面的右边
	 * @param bottom near面的下面
	 * @param top near面的上面
	 * @param near near面距离观察点的距离
	 * @param far far面距离观察点的距离
	 */
	public static void setProjectOrtho(float left, float right, float bottom, float top,
			float near, float far) {
		Matrix.orthoM(mProjMatrix, 0, left, right, bottom, top, near, far);
	}

	/**
	 * 透视投影
	 * @param left near面的左边
	 * @param right near面的右边
	 * @param bottom near面的下面
	 * @param top near面的上面
	 * @param near near面距离观察点的距离
	 * @param far far面距离观察点的距离
	 */
	public static void setProjectFrustum(float left, float right, float bottom, float top,
			float near, float far) {
		Matrix.frustumM(mProjMatrix, 0, left, right, bottom, top, near, far);
	}

	/**
	 * 获取最终变换矩阵
	 * @return 最终变换矩阵
	 */
	public static float[] getFinalMatrix() {
		Matrix.multiplyMM(mMVPMatrix, 0, mMVMatrix, 0, mCurMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
		return mMVPMatrix;
	}
	
	/**
	 * 获取摄像机矩阵
	 * @return 摄像机矩阵
	 */
	public static float[] getVMatrix() {
		return mMVMatrix;
	}
	
	/**
	 * 获取当前变换矩阵
	 * 注意：复制矩阵，需要生成新的矩阵，逐一复制源矩阵的元素
	 * @return
	 */
	public static float[] getCurMatrix() {
		float[] m = new float[mCurMatrix.length];
		for(int i = 0; i < m.length; i++) {
			m[i] = mCurMatrix[i];
		}
		return m;
	}
	
	/**
	 * 求逆矩阵
	 * @param matrix 源矩阵
	 * @return 逆矩阵
	 */
	public static float[] inverteMatrix(float[] matrix) {
		float[] invMatrix = new float[16];
		Matrix.invertM(invMatrix, 0, matrix, 0);
		return invMatrix;
	}
}