package com.sany.tangyong.engineoriginal;

import android.content.Context;
import android.opengl.GLES20;
import android.util.AttributeSet;

import com.go.gl.graphics.ColorAttributeShader;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLVBO;
import com.go.gl.graphics.RenderContext;
import com.go.gl.graphics.Renderable;
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
public class VBOTestView extends GLView {

	private static final int POSITION_COMPONENT = 3;
	private static final int COLOR_COMPONENT = 4;

	private int mIndexCount;
	private float mRotateX;
	private float mRotateY;
	private float mRotateZ;

	
	GLVBO mVertexBO;
	GLVBO mColorBO;
	GLVBO mIndexBO;

	public VBOTestView(Context context) {
		super(context);
		init();
	}

	public VBOTestView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VBOTestView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		/** @formatter:off */
        float colors[] = {
                0, 0, 0, 1, 
                1, 0, 0, 0.25f,
                0, 1, 0, 0.5f,
                1, 1, 0, 0.25f,
                0, 0, 1, 0.5f,
                1, 0, 1, 1, 
                0, 1, 1, 0.25f,
                1, 1, 1, 0.25f,
        };
        /** @formatter:on */
		mColorBO = new GLVBO(false, GLVBO.STATIC);
		mColorBO.setData(colors);

		/** @formatter:off */
		//CHECKSTYLE IGNORE 4 LINES
        short index[] = {
                0, 4, 2, 6, 3, 7, 1, 5, 
                5, 7, 
                7, 6, 5, 4, 1, 0, 3, 2,
        };
        /** @formatter:on */
		mIndexCount = index.length;
		mIndexBO = new GLVBO(true, GLVBO.STATIC);
		mIndexBO.setData(index);
		
		mVertexBO = new GLVBO(false, GLVBO.STATIC);

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		final float s = Math.min(getWidth(), getHeight()) * 0.25f;
		/** @formatter:off */
		float vertices[] = { 
				-s, -s, -s, 
				 s, -s, -s, 
				-s,  s, -s, 
				 s,  s, -s, 
				-s, -s,  s, 
				 s, -s,  s, 
				-s,  s,  s, 
				 s,  s,  s, 
		};
		/** @formatter:on */

		mVertexBO.setData(vertices);
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		ColorAttributeShader shader = ColorAttributeShader.getShader();
		if (shader == null) {
			return;
		}
		//		canvas.setDepthEnable(true);

		canvas.translate(getWidth() / 2, getHeight() / 2);
		canvas.rotateEuler(mRotateX, mRotateY, mRotateZ);

		RenderContext context = RenderContext.acquire();
		context.shader = shader;
		canvas.getFinalMatrix(context);

		mIndexBO.bindOnUIThread(canvas);
		mColorBO.bindOnUIThread(canvas);
		mVertexBO.bindOnUIThread(canvas);
		
		canvas.setCullFaceSide(false);
		canvas.addRenderable(mRenderable, context);
		
		//绘制半透明使用了两趟绘制，第二趟之前因为mRenderable会调用unbind，所以要重新调用bind
		mIndexBO.bindOnUIThread(canvas);
		mColorBO.bindOnUIThread(canvas);
		mVertexBO.bindOnUIThread(canvas);
		
		canvas.setCullFaceSide(true);
		canvas.addRenderable(mRenderable, context);

		//		canvas.setDepthEnable(false);
		invalidate(); //不停重绘，并且computeScroll方法会在绘制前被调用以更新状态
		
	}

	@Override
	public void computeScroll() {
		//CHECKSTYLE IGNORE 3 LINES
		mRotateX += 0.5f;
		mRotateY += 0.2f;
		mRotateZ += 0.3f;
	}
	
	@Override
	public void cleanup() {
		mIndexBO.clear();
		mColorBO.clear();
		mVertexBO.clear();
		super.cleanup();
	}

	private final Renderable mRenderable = new Renderable() {

		@Override
		public void run(long timeStamp, RenderContext context) {
			ColorAttributeShader shader = (ColorAttributeShader) context.shader;
			if (shader == null || !shader.bind()) {
				return;
			}
			shader.setMatrix(context.matrix, 0);
			
			shader.setPosition(0, POSITION_COMPONENT);
			GLVBO.unbindOnGLThread();
			
			
			shader.setColor(0, COLOR_COMPONENT);
			GLVBO.unbindOnGLThread();
			
			NdkUtil.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mIndexCount, 
					GLES20.GL_UNSIGNED_SHORT, 0);

			GLVBO.unbindOnGLThread();

		}
	};
}
