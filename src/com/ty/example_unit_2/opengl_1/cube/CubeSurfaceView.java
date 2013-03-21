package com.ty.example_unit_2.opengl_1.cube;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;

/**
 * 
 * @author tangyong
 * 
 */
public class CubeSurfaceView extends GLSurfaceView {

	public CubeSurfaceView(Context context) {
		super(context);
		setRenderer(new CubeRender());
	}

	/**
	 * 
	 * @author tangyong
	 * 
	 */
	class CubeRender implements Renderer {
		
		Cube cube = null;
		private float angle = 0;
		
		public CubeRender() {
			cube = new Cube();
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			
			gl.glColor4f(1, 1, 1, 1);
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
			
			gl.glLoadIdentity();
			gl.glTranslatef(0, 0, -9);
			gl.glRotatef(angle, 0, 1, 0);
			gl.glEnable(GL10.GL_LINE_SMOOTH);
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			
			cube.draw(gl);
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glDisable(GL10.GL_LINE_SMOOTH);
			angle += 0.8f;
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			//当大小变化的时候会给调用
			float ratio = (float) width / height;
			gl.glViewport(0, 0, width, height);
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			//平面还是平滑着色
			gl.glShadeModel(GL10.GL_SMOOTH);
			//指定色彩缓冲区
			gl.glClearColor(0, 0,0, 0);
			//设置深度缓存,进行跟踪
			gl.glClearDepthf(1.0f);
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glDepthFunc(GL10.GL_LEQUAL);

			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
		}

	}

}
