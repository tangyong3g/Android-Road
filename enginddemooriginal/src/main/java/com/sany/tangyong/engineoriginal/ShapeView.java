package com.sany.tangyong.engineoriginal;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.os.SystemClock;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLShaderProgram;
import com.go.gl.graphics.RenderContext;
import com.go.gl.graphics.Renderable;
import com.go.gl.graphics.TextureListener;
import com.go.gl.graphics.TextureManager;
import com.go.gl.graphics.Triple;
import com.go.gl.util.IBufferFactory;
import com.go.gl.util.NdkUtil;
import com.go.gl.view.GLView;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2012-9-6]
 */
public class ShapeView extends GLView implements TextureListener {
	private FloatBuffer mTexCoord;
	private ShapeShader mShader;
	private static final int VERTEX_COUNT = 4;
	private static final int POSITION_COMPONENT = 2;
	private static final int TEXCOORD_COMPONENT = 2;

	long mStartTime = -1;
	final long mDuration = 5000;
	boolean mCycleBack;

	Triple mVertexTriple = new Triple();
	
	RectF mRectF = new RectF();

	public ShapeView(Context context) {
		super(context);

		mTexCoord = IBufferFactory.newFloatBuffer(TEXCOORD_COMPONENT * VERTEX_COUNT);

		float[] uv = { 0, 0, 0, 1, 1, 0, 1, 1 };
		mTexCoord.put(uv);
		mTexCoord.position(0);

		mShader = new ShapeShader(getResources(), "shape.vert", "shape.frag");

		for (int i = 0; i < Triple.BC; ++i) {
			mVertexTriple.setData(i,
					IBufferFactory.newFloatBuffer(POSITION_COMPONENT * VERTEX_COUNT));
		}

		TextureManager.getInstance().registerTextureListener(this);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		w -= 10; //CHECKSTYLE IGNORE
		h = w;

		FloatBuffer vertex = (FloatBuffer) mVertexTriple.getDataForUpdate();
		vertex.put(0);
		vertex.put(0);
//		vertex.put(0);

		vertex.put(0);
		vertex.put(-h);
//		vertex.put(0);

		vertex.put(w);
		vertex.put(0);
//		vertex.put(0);

		vertex.put(w);
		vertex.put(-h);
//		vertex.put(0);

		vertex.position(0);
	}

	@Override
	protected void onDraw(GLCanvas canvas) {

		if (mStartTime == -1) {
			mStartTime = SystemClock.uptimeMillis();
		}
		long timeElapsed = SystemClock.uptimeMillis() - mStartTime;
		float t = timeElapsed / (float) mDuration;
		if (t >= 1) {
			t = 1;
			mStartTime = -1;
			t = mCycleBack ? 1 - t : t;
			mCycleBack = !mCycleBack;
		} else {
			t = mCycleBack ? 1 - t : t;
		}

		canvas.translate(5, getHeight() / 2 - getWidth() / 2); //CHECKSTYLE IGNORE

		RenderContext context = RenderContext.acquire();
		context.shader = mShader;
		context.color[0] = t;
		
		canvas.getFinalMatrix(context);
		canvas.addRenderable(mRenderable, context);
		

		invalidate();
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2012-9-6]
	 */
	private static class ShapeShader extends GLShaderProgram {
		int muMVPMatrixHandle; //总变换矩阵引用id
		int maPositionHandle; //顶点位置属性引用id  
		int maTexCoordHandle; //顶点纹理坐标属性引用id  
		int muTime;

		public ShapeShader(Resources res, String vertexFile, String fragmentFile) {
			super(res, vertexFile, fragmentFile);
		}

		public ShapeShader(String vertexSource, String fragmentSource) {
			super(vertexSource, fragmentSource);
		}

		@Override
		public boolean onProgramCreated() {
			maPositionHandle = getAttribLocation("aPosition");
			maTexCoordHandle = getAttribLocation("aTexCoord");
			muMVPMatrixHandle = getUniformLocation("uMVPMatrix");
			muTime = getUniformLocation("uTime");
			return true;
		}

		@Override
		public void onProgramBind() {
			GLES20.glEnableVertexAttribArray(maPositionHandle);
			GLES20.glEnableVertexAttribArray(maTexCoordHandle);
		}

		public void setMatrix(float[] m, int offset) {
			GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, m, offset);
		}

		public void setTime(float time) {
			GLES20.glUniform1f(muTime, time);
		}

		public void setPosition(Buffer ptr, int component) {
			GLES20.glVertexAttribPointer(maPositionHandle, component, GLES20.GL_FLOAT, false, 0,
					ptr);
		}

		public void setPosition(int offset, int component) {
			//			GLES20.glVertexAttribPointer(maPositionHandle, component, GLES20.GL_FLOAT,
			//					false, 0, offset);   //API Level 9
			NdkUtil.glVertexAttribPointer(maPositionHandle, component, GLES20.GL_FLOAT, false, 0,
					offset);
		}

		public void setTexCoord(Buffer ptr, int component) {
			GLES20.glVertexAttribPointer(maTexCoordHandle, component, GLES20.GL_FLOAT, false, 0,
					ptr);
		}

		public void setTexCoord(int offset, int component) {
			//			GLES20.glVertexAttribPointer(maTexCoordHandle, component, GLES20.GL_FLOAT,
			//					false, 0, offset);   //API Level 9
			NdkUtil.glVertexAttribPointer(maTexCoordHandle, component, GLES20.GL_FLOAT, false, 0,
					offset);
		}

		@Override
		public String toString() {
			return "ShapeShader";
		}
	}

	@Override
	public void onTextureInvalidate() {
		mShader.onTextureInvalidate();
	}

	private final Renderable mRenderable = new Renderable() {
		
		@Override
		public void run(long timeStamp, RenderContext context) {
			ShapeShader shader = (ShapeShader) context.shader;
			if (shader == null || !shader.bind()) {
				return;
			}
			shader.setTime(context.color[0]);
			shader.setMatrix(context.matrix, 0);

			FloatBuffer vertex = (FloatBuffer) mVertexTriple.getDataForRender(timeStamp);
			shader.setPosition(vertex, POSITION_COMPONENT);
			shader.setTexCoord(mTexCoord, TEXCOORD_COMPONENT);

			GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
			
		}
	};
}
