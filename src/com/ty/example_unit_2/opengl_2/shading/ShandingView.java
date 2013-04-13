package com.ty.example_unit_2.opengl_2.shading;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

/**
 * 
 * @author tangyong
 * 
 */
public class ShandingView extends GLSurfaceView {

	Renderer render ;
	
	public ShandingView(Context context)
	{
		super(context);
		this.setEGLContextClientVersion(2);
		render=new ShandingRenderer();
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

		@Override
		public void onDrawFrame(GL10 gl) {
			Log.i("tyler.tang","onDrawFrame");
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
			mTriangle.draw();
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			//设置视窗大小及位置 
        	GLES20.glViewport(0, 0, width, height); 
        	//计算GLSurfaceView的宽高比
            float ratio = (float) width / height;
            //调用此方法计算产生透视投影矩阵
            Matrix.frustumM(Triangle.mProjMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
            //调用此方法产生摄像机9参数位置矩阵
            Matrix.setLookAtM(Triangle.mVMatrix, 0, 0,0,3,0f,0f,0f,0f,1.0f,0.0f); 
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			//背面剪裁
			GLES20.glEnable(GLES20.GL_CULL_FACE);
			//卷绕方式 ,默认为 GLES20.GL_CCW 顶点绘制顺序逆时针为正 
//			GLES20.glFrontFace(GLES20.GL_CW);
			mTriangle  = new Triangle(ShandingView.this);
		}
	}

}
