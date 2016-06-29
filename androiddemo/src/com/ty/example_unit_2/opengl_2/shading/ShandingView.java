package com.ty.example_unit_2.opengl_2.shading;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.badlogic.gdx.math.Matrix4;
import com.ty.animation.Transformation;
import com.ty.animation.TranslateAnimation;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.ViewDebug.HierarchyTraceType;

/**
 * 
 * @author tangyong
 * 
 */
public class ShandingView extends GLSurfaceView {

	Renderer render;
	
	 static float sDEFAULT_FOV = 45;
	 static double sDEFAULT_FOV_SCALE_FACTOR = 0.5 / Math.tan(Math.toRadians(sDEFAULT_FOV) * 0.5);
	 public static final float DEFAULT_Z_RANGE = 9000;

	public ShandingView(Context context) {
		super(context);
		this.setEGLContextClientVersion(2);
		render = new ShandingRenderer();
		this.setRenderer(render);
		this.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}

	/**
	 * 
	 * @author tangyong
	 * 
	 */
	class ShandingRenderer implements Renderer {

		Triangle mTriangle;
		TranslateAnimation mTranslateAnimation = null;

		float deltaTime;
		long lastFrameTime = 0L;

		@Override
		public void onDrawFrame(GL10 gl) {

			long time = System.nanoTime();
			deltaTime = (time - lastFrameTime) / 1000000000.0f;
			lastFrameTime = time;

			float[] result = new float[16];
			Matrix.setRotateM(result, 0, 0, 0, 1, 0);
			Transformation tran = new Transformation();
			tran.clear();

			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT
					| GLES20.GL_DEPTH_BUFFER_BIT);
			mTriangle.draw(result);
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {

			/*
			// 设置视窗大小及位置
			GLES20.glViewport(0, 0, width , height );
			// 计算GLSurfaceView的宽高比
			float ratio = (float) height / width;
			// 调用此方法计算产生透视投影矩阵
			Matrix.frustumM(Triangle.mProjMatrix, 0, -1, 1, -ratio, ratio, 1,
					10f);
			// 调用此方法产生摄像机9参数位置矩阵
			Matrix.setLookAtM(Triangle.mVMatrix, 0, 0, 0, 1.0000001f, 0f, 0f,
					0f, 0f, 1.0f, 0.0f);
					*/
			changeSurface(width, height);
		}
		
	
		
		private void changeSurface(int width , int height){
			
			mTriangle = new Triangle(ShandingView.this,width,height);
			
			GLES20.glViewport(width/2, height/2, width/2 , height/2 );
			// 计算GLSurfaceView的宽高比
			float aspect = (float) width / height;
			
			final float cameraZ = (float) (height * sDEFAULT_FOV_SCALE_FACTOR);
			final float near = Math.max(1, cameraZ / 2 - 1);
			// 解决大分辨率手机屏幕预览拐弯特效被裁剪问题
			final float far = near + DEFAULT_Z_RANGE * (Math.max(width, 800) / 800f); //CHECKSTYLE IGNORE
			
			final float top = near * (float) Math.tan(45f * (Math.PI / 360.0)); //CHECKSTYLE IGNORE
			
			final float bottom = -top;
			final float left = bottom * aspect;
			final float right = top * aspect;
			
			
			final float z = height * near * 0.5f /top; //CHECKSTYLE IGNORE
			float 	mDefaultCameraPos[] = new float[3];
			
			mDefaultCameraPos[0] = -z * left / near;
			mDefaultCameraPos[1] = -z * top /near;
			mDefaultCameraPos[2] = z;
			

			Matrix.frustumM(Triangle.mProjMatrix, 0, left, right, bottom, top, near, far);
			Matrix.multiplyMM(Triangle.mMVPMatrix, 0, Triangle.mProjMatrix, 0, Triangle.mVMatrix, 0);
			

			// setWindowPosition
			Matrix.setIdentityM(Triangle.mVMatrix, 0);
			Matrix.translateM(Triangle.mVMatrix, 0, -mDefaultCameraPos[0], -mDefaultCameraPos[1], -mDefaultCameraPos[2] );
			Matrix.multiplyMM(Triangle.mMVPMatrix, 0, Triangle.mProjMatrix, 0,Triangle.mVMatrix, 0);
			
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			// 背面剪裁
			GLES20.glEnable(GLES20.GL_CULL_FACE);
			// 卷绕方式 ,默认为 GLES20.GL_CCW 顶点绘制顺序逆时针为正
			// GLES20.glFrontFace(GLES20.GL_CW);
//			mTriangle = new Triangle(ShandingView.this);

			mTranslateAnimation = new TranslateAnimation(0, 0, 0, -10, 0, 0);
			mTranslateAnimation.initialize(0, 0, 0, 0);
		}
	}

}
