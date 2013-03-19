package com.ty.example_unit_1.cube;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class ExerciseSurfaceView extends GLSurfaceView {
	private SceneRenderer mRenderer;
	private Cube mCube;
	private Line mLine;
	private Texture mTexture;
	private static final float ANGLE_SPAN = 0.3f;
	private float mPrevX;
	private float mPrevY;
	private int mFrameTime = 0;
	private float mXAngle = 0;
	private float mYAngle = 0;

	private float mWidth;
	private float mHeight;

	public ExerciseSurfaceView(Context context) {
		super(context);
		//使用OpenglEs2.0
		this.setEGLContextClientVersion(2);
		mRenderer = new SceneRenderer();
		setRenderer(mRenderer);
		//设置为主动渲染模式
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

		mPrevX = -1.0f;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mPrevX = event.getX();
				mPrevY = event.getY();

				float[] vertices = Ray.initRay(Constants.sRatio, Constants.sRatio, 1, 1, 10f, 30f,
						mWidth, mHeight, mPrevX, mPrevY);
				mLine.initVertex(vertices);
				mCube.intersectWhichPlane(vertices);
				break;
			case MotionEvent.ACTION_MOVE :
				float deltX = event.getX() - mPrevX;
				float deltY = event.getY() - mPrevY;
				mXAngle += deltX * ANGLE_SPAN;
				mXAngle = mXAngle % 360;
				mYAngle += deltY * ANGLE_SPAN;
				mYAngle = mYAngle % 360;
				mPrevX = event.getX();
				mPrevY = event.getY();
				break;
			case MotionEvent.ACTION_UP :
				mPrevX = -1;
				mPrevY = -1;
				break;
			default :
				;
		}
		return true;	//必须返回true，才能处理所有的触摸事件
	}

	private class SceneRenderer implements GLSurfaceView.Renderer {

		@Override
		public void onDrawFrame(GL10 gl) {
			long startTime = System.currentTimeMillis();
			//清除深度缓冲与颜色缓冲
			GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);	//每次绘制前必须清除深度缓冲位和颜色缓冲位，否则屏幕花屏
			MatrixState.pushMatrix();

			MatrixState.pushMatrix();
			MatrixState.rotate(45, 1, 1, 1);
			MatrixState.rotate(mXAngle, 0, 1, 0);
			MatrixState.rotate(mYAngle, 1, 0, 0);
			mCube.setCurMatrix(MatrixState.getCurMatrix());
			mCube.drawCube();
			MatrixState.popMatrix();

			//			if (mLine != null) {
			//				MatrixState.pushMatrix();
			//				mLine.drawLine();
			//				MatrixState.popMatrix();
			//			}

			MatrixState.popMatrix();

			mFrameTime = (int) (System.currentTimeMillis() - startTime);
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			mWidth = width;
			mHeight = height;
			GLES20.glViewport(0, 0, width, height);
			Constants.sRatio = (float) width / height;

			MatrixState.setProjectFrustum(-Constants.sRatio, Constants.sRatio, -1, 1, 10f, 30);
			MatrixState.setCamera(0, 0, 20f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

			MatrixState.setInitStack();
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			//清除颜色缓冲区
			GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
			mCube = new Cube(ExerciseSurfaceView.this);
			mLine = new Line(ExerciseSurfaceView.this);
			//打开深度检测
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			//打开背面裁剪
			GLES20.glEnable(GLES20.GL_CULL_FACE);

		}
	}

}