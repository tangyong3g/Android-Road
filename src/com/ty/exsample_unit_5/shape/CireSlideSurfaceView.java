package com.ty.exsample_unit_5.shape;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import com.ty.exsample.R;
import com.ty.util.MatrixState;

/**
 * 
 * @author tang
 *CireSlideRender
 */
class CireSlideSurfaceView extends GLSurfaceView {

	CireSlideRender mRender;

	public CireSlideSurfaceView(Context context) {
		super(context);

//		mRender = new CireSlideRender();
//		setEGLContextClientVersion(2);
//		setRenderer(mRender);

	}

	/**
	 * @author tang
	 */
	class CireSlideRender implements Renderer {

		private int textureId;
		private Cylinder cylinder;

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {

			//设置屏幕背景色RGBA
			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			//启用深度测试
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			//设置为打开背面剪裁
			GLES20.glEnable(GLES20.GL_CULL_FACE);
			//初始化变换矩阵
			MatrixState.setInitStack();
			//加载纹理
			textureId = initTexture(R.drawable.bg_one);
			//创建圆柱对象
			cylinder = new Cylinder(CireSlideSurfaceView.this, 1, 1.2f, 3.9f, 36, textureId, textureId, textureId);
			//创建圆柱骨架对象
			//            cylinderl= new CylinderL(CireSlideSurfaceView.this,1,1.2f,3.9f,36);

		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			//设置视窗大小及位置 
			GLES20.glViewport(0, 0, width, height);
			//计算GLSurfaceView的宽高比
			float ratio = (float) width / height;
			//调用此方法计算产生透视投影矩阵
			MatrixState.setProjectFrustum(-ratio, ratio, -1, 1, 4f, 100);
			//调用此方法产生摄像机9参数位置矩阵
			MatrixState.setCamera(0, 0, 8.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		}

		@Override
		public void onDrawFrame(GL10 gl) {

			//清除深度缓冲与颜色缓冲
			GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

			//保护现场
			MatrixState.pushMatrix();
			MatrixState.translate(0, 0, -10);
			cylinder.drawSelf();
			MatrixState.popMatrix();

		}

	}

	public int initTexture(int drawableId)//textureId
	{
		//生成纹理ID
		int[] textures = new int[1];
		GLES20.glGenTextures(1,          //产生的纹理id的数量
				textures,   //纹理id的数组
				0           //偏移量
		);
		int textureId = textures[0];
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		//通过输入流加载图片===============begin===================
		InputStream is = this.getResources().openRawResource(drawableId);
		Bitmap bitmapTmp;
		try {
			bitmapTmp = BitmapFactory.decodeStream(is);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//通过输入流加载图片===============end=====================  

		//实际加载纹理
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,   //纹理类型，在OpenGL ES中必须为GL10.GL_TEXTURE_2D
				0, 					  //纹理的层次，0表示基本图像层，可以理解为直接贴图
				bitmapTmp, 			  //纹理图像
				0					  //纹理边框尺寸
		);
		bitmapTmp.recycle(); 		  //纹理加载成功后释放图片

		return textureId;
	}
}
