package com.ty.example_unit_2.opengl_2.cube;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.ty.exsample.R;
import com.ty.util.ImageUtil;
import com.ty.util.MatrixState;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;
import android.view.SurfaceView;

/**
 * 
 * @author tangyong
 * 
 */
public class CubeView extends GLSurfaceView {

	private Context mContext;
	
	public CubeView(Context context) {
		super(context);
		this.mContext = context;
		this.setEGLContextClientVersion(2);
		this.setRenderer(new CubeRender());
	}
	
	/**
	 * 
	 * @author tangyong
	 * 
	 */
	class CubeRender implements Renderer {

		Cube mCube = null;
		private int mTextureId;
		
		@Override
		public void onDrawFrame(GL10 gl) {
			GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
			MatrixState.pushMatrix();
//			mCube.drawSelf(mTextureId);
			MatrixState.popMatrix();
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			
			GLES20.glViewport(0, 0, width, height);
			float ratio =  (float)width/height;
			MatrixState.setProjectFrustum(-ratio, ratio, -1, 1, 2, 100);
			MatrixState.setCamera
			(
				0, 0, 0, 
				0f, 0f, -1f,
				0f, 1.0f, 0.0f
	        );
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			GLES20.glEnable(GLES20.GL_FRONT_FACE);
			
			mCube = new Cube(mContext);
			mTextureId = ImageUtil.initTexture(R.drawable.cube_simple, mContext);
		}
	}

}
