package com.ty.example_unit_2.opengl_2.cube;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class Render implements Renderer {
	Context mContext;

	public Render(Context context) {
		mContext = context;
		Shader shader = new Shader();
		shader.initShader("data/shaders/unit2/cube/vertex.sh","data/shaders/unit2/cube/frag.sh", mContext);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		Log.i("tyler.tang","draw");
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
//		shader.initShader(mContext);
	}
}
