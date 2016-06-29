package com.ty.example_unit_1.cube;

import android.opengl.Matrix;

/**
 * OpengL拾取射线
 * @author liuwenqin
 *
 */
public class Ray {

	/**
	 * 初始化拾取射线
	 * @param left 视景体左边宽度。大于0
	 * @param right 视景体右边宽度。大于0
	 * @param top 视景体顶部高度。大于0
	 * @param bottom 视景体底部高度。大于0
	 * @paran near 视景体近平面与视点的距离。大于0
	 * @param far 视景体远平面距离视点的距离
	 * @param screenWidth 屏幕宽度
	 * @param screenHeight 屏幕高度
	 * @param screenX 触控点在屏幕上的X坐标
	 * @param screenY 触控点在屏幕上的Y坐标
	 */
	public static float[] initRay(float left, float right, float top, float bottom, float near,
			float far, float screenWidth, float screenHeight, float screenX, float screenY) {
		float[] verticeA = getVerticeA(left, top, near, screenWidth, screenHeight, screenX, screenY);
		float[] verticeB = getVerticeB(near, far, verticeA);
		float[] vectorA = new float[] { verticeA[0], verticeA[1], verticeA[2], 1 };
		float[] vectorB = new float[] { verticeB[0], verticeB[1], verticeB[2], 1 };
		float[] invVMatrix = MatrixState.inverteMatrix(MatrixState.getVMatrix());
		Matrix.multiplyMV(vectorA, 0, invVMatrix, 0, vectorA, 0);
		Matrix.multiplyMV(vectorB, 0, invVMatrix, 0, vectorB, 0);
		return new float[] { vectorA[0], vectorA[1], vectorA[2], vectorB[0], vectorB[1], vectorB[2] };
	}

	/**
	 * 将触控点A的坐标转换为3D坐标
	 * 注意：视景体的left == right，top == bottom
	 * @param left 视景体左边宽度。大于0
	 * @param top 视景体顶部高度。大于0
	 * @paran near 视景体近平面与视点的距离。大于0
	 * @param screenWidth 屏幕宽度
	 * @param screenHeight 屏幕高度
	 * @param screenX 触控点在屏幕上的X坐标
	 * @param screenY 触控点在屏幕上的Y坐标
	 * @return 触控点在当前摄像机坐标系中的坐标，该触控点为拾取射线在视景体近平面上的交点A
	 */
	private static float[] getVerticeA(float left, float top, float near, float screenWidth,
			float screenHeight, float screenX, float screenY) {
		final int coordSize = 3;
		float[] vertice = new float[coordSize];
		final float half = 0.5f;
		vertice[0] = (screenX - screenWidth * half) * left / (screenWidth * half);
		vertice[1] = (screenHeight * half - screenY) * top / (screenHeight * half);
		vertice[2] = -near;
		return vertice;
	}

	/**
	 * 获取拾取射线在视景体远平面上的交点B
	 * @param near 视景体近平面距离视点的距离
	 * @param far 视景体远平面距离视点的距离
	 * @param verticeA 拾取射线在视景体近平面上的交点
	 * @return 拾取射线在视景体远平面上的交点B
	 */
	private static float[] getVerticeB(float near, float far, float[] verticeA) {
		final int coordSize = 3;
		float[] verticeB = new float[coordSize];
		float ratio = far / near;
		verticeB[0] = verticeA[0] * ratio;
		verticeB[1] = verticeA[1] * ratio;
		verticeB[2] = -far;
		return verticeB;
	}
}