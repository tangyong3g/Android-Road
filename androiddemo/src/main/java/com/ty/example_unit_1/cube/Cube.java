package com.ty.example_unit_1.cube;

import android.opengl.Matrix;

public class Cube {
	private Plane[] mPlanes;
	/**
	 * 当前逆矩阵变换
	 */
	private float[] mCurMatrix;

	public Cube(ExerciseSurfaceView mv) {
		final int planeAmount = 6;
		mPlanes = new Plane[planeAmount];
		for (int i = 0; i < planeAmount; i++) {
			Plane plane = new Plane(mv);
			mPlanes[i] = plane;
		}
		initPlaneMatrix();
	}
	
	private void initPlaneMatrix() {
		int idx = -1;
		//后面
		MatrixState.setInitStack();
		MatrixState.pushMatrix();
		MatrixState.translate(0, 0, -1);
		MatrixState.rotate(180, -1, 0, 0);
		mPlanes[++idx].setCurMatrix(MatrixState.getCurMatrix());
		MatrixState.popMatrix();
		//前面
		MatrixState.pushMatrix();
		MatrixState.translate(0, 0, 1);
		mPlanes[++idx].setCurMatrix(MatrixState.getCurMatrix());
		MatrixState.popMatrix();
		//右面
		MatrixState.pushMatrix();
		MatrixState.translate(1, 0, 0);
		MatrixState.rotate(90, 0, 1, 0);
		mPlanes[++idx].setCurMatrix(MatrixState.getCurMatrix());
		MatrixState.popMatrix();
		//左面
		MatrixState.pushMatrix();
		MatrixState.translate(-1, 0, 0);
		MatrixState.rotate(-90, 0, 1, 0);
		mPlanes[++idx].setCurMatrix(MatrixState.getCurMatrix());
		MatrixState.popMatrix();
		//上面
		MatrixState.pushMatrix();
		MatrixState.translate(0, 1, 0);
		MatrixState.rotate(-90, 1, 0, 0);
		mPlanes[++idx].setCurMatrix(MatrixState.getCurMatrix());
		MatrixState.popMatrix();
		//下面
		MatrixState.pushMatrix();
		MatrixState.translate(0, -1, 0);
		MatrixState.rotate(90, 1, 0, 0);
		mPlanes[++idx].setCurMatrix(MatrixState.getCurMatrix());
		MatrixState.popMatrix();
	}

	public void setCurMatrix(float[] curMatrix) {
		mCurMatrix = curMatrix;
	}

	public float[] getCurMatrix() {
		return mCurMatrix;
	}

	public void drawCube() {
		int idx = -1;
		//绘制后面
		MatrixState.pushMatrix();
		MatrixState.translate(0, 0, -1);
		MatrixState.rotate(180, -1, 0, 0);
		mPlanes[++idx].drawPlane();
		MatrixState.popMatrix();
		//绘制前面
		MatrixState.pushMatrix();
		MatrixState.translate(0, 0, 1);
		mPlanes[++idx].drawPlane();
		MatrixState.popMatrix();
		//绘制右面
		MatrixState.pushMatrix();
		MatrixState.translate(1, 0, 0);
		MatrixState.rotate(90, 0, 1, 0);
		mPlanes[++idx].drawPlane();
		MatrixState.popMatrix();
		//绘制左面
		MatrixState.pushMatrix();
		MatrixState.translate(-1, 0, 0);
		MatrixState.rotate(-90, 0, 1, 0);
		mPlanes[++idx].drawPlane();
		MatrixState.popMatrix();
		//绘制上面
		MatrixState.pushMatrix();
		MatrixState.translate(0, 1, 0);
		MatrixState.rotate(-90, 1, 0, 0);
		mPlanes[++idx].drawPlane();
		MatrixState.popMatrix();
		//绘制下面
		MatrixState.pushMatrix();
		MatrixState.translate(0, -1, 0);
		MatrixState.rotate(90, 1, 0, 0);
		mPlanes[++idx].drawPlane();
		MatrixState.popMatrix();
	}

	/**
	 * 判断触点落在哪个面上
	 * @param vertices
	 */
	public void intersectWhichPlane(float[] vertices) {
		//射线起始点坐标
		float[] vectorStart = new float[4];
		vectorStart[0] = vertices[0];
		vectorStart[1] = vertices[1];
		vectorStart[2] = vertices[2];
		vectorStart[3] = 1;
		//射线终点坐标
		float[] vectorEnd = new float[4];
		vectorEnd[0] = vertices[3];
		vectorEnd[1] = vertices[4];
		vectorEnd[2] = vertices[5];
		vectorEnd[3] = 1;
		//存放射线在各个面的模型坐标下的向量
		float[] planeVectorStart = new float[4];
		float[] planeVectorEnd = new float[4];
		float[] dirVector = new float[3];
		float[] start = new float[3];
		//方向向量的参数
		float k = 0.0f;
		//射线在当前平面中的交点
		float x = 0.0f;
		float y = 0.0f;

		float[] invertMatrix = null;
		invertMatrix = MatrixState.inverteMatrix(mCurMatrix);
		Matrix.multiplyMV(vectorStart, 0, invertMatrix, 0, vectorStart, 0);
		Matrix.multiplyMV(vectorEnd, 0, invertMatrix, 0, vectorEnd, 0);
		
		int which = -1;
		float minK = Float.MAX_VALUE;
		for (int i = 0; i < mPlanes.length; i++) {
			invertMatrix = MatrixState.inverteMatrix(mPlanes[i].getCurMatrix());
			Matrix.multiplyMV(planeVectorStart, 0, invertMatrix, 0, vectorStart, 0);
			Matrix.multiplyMV(planeVectorEnd, 0, invertMatrix, 0, vectorEnd, 0);
			for (int j = 0; j < 3; j++) {
				start[j] = planeVectorStart[j];
				dirVector[j] = planeVectorEnd[j] - planeVectorStart[j];
			}
			k = -start[2] / dirVector[2];
			x = start[0] + k * dirVector[0];
			y = start[1] + k * dirVector[1];
			if (x > -Constants.UNIT_SIZE && x < Constants.UNIT_SIZE && y > -Constants.UNIT_SIZE
					&& y < Constants.UNIT_SIZE) {
				if(k < minK) {
					minK = k;
					which = i;
				}
			} 
			mPlanes[i].initColorByUnTouch();
		}
		
		if(which != -1) {
			mPlanes[which].initColorByTouch(1.0f, 0.0f, 0.0f);
		}
	}
}