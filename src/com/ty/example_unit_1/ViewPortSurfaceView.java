package com.ty.example_unit_1;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;


/**
 * 　问题:关于视口 
 *       1：视口会影响成像的大小吗？
 *       2：视口会影响成像的位置吗？
 *       
 *       如下： 很显然显示区的大小变了，成像的大小也会变。位置受 x, y的影响 .
 *       
 *       
 * 在GLES20中
 * GLES20.glViewport(0, 0, width/2, height/2);
 * 
 * param_1     x 指的是在视口区的左下角屏幕坐标系中x的位置. 「屏幕坐标系原点在左下角」
 * param_2     y 指的是在视口区的左下角屏幕坐标系中y的位置. 「屏幕坐标系原点在左下角」
 * param_3     width  指的是在屏幕坐标系宽度用的是 象素为单位 。
 * param_4     heigh  指的是在屏幕坐标系宽度用的是 象素为单位 。
 * 
 * 
 * @author tangyong
 *
 */
public class ViewPortSurfaceView extends GLSurfaceView {
	
	ViewPortRender render = null;

	public ViewPortSurfaceView(Context context) {
		super(context);
		this.setEGLContextClientVersion(2);
		render=new ViewPortRender();
		this.setRenderer(render);
		this.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}

	/**
	 * 
	 * @author tangyong
	 * 
	 */
	class ViewPortRender implements Renderer {

		Triangle tle;
		private float angle = 0f;
		
		@Override
		public void onDrawFrame(GL10 gl) {
			//清除深度缓冲与颜色缓冲
            GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            //绘制三角形对
            tle.drawSelf(0);
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
			//设置屏幕背景色RGBA
            GLES20.glClearColor(0,0,0,1.0f);  
            //创建三角形对对象 
            tle=new Triangle(ViewPortSurfaceView.this);        
            //打开深度检测
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		}

	}

}
