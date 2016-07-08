package com.graphics.enginedemo.simple;

import android.content.Context;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.util.AttributeSet;

import com.graphics.engine.graphics.ColorShader;
import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.graphics.RenderContext;
import com.graphics.engine.graphics.Renderable;
import com.graphics.engine.graphics.Triple;
import com.graphics.engine.util.IBufferFactory;
import com.graphics.engine.view.GLView;

import java.nio.FloatBuffer;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2012-9-6]
 */
public class RectGLView extends GLView {
	private static final int VERTEX_COUNT = 4;
	private static final int POSITION_COMPONENT = 2;
	
	boolean mBoundSpecified;
	int mColor;

	public RectGLView(Context context, int color) {
		super(context);
		init(color);
		updateBound(new RectF());
	}

	public RectGLView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init(0xFFFFFFFF);
		updateBound(new RectF());
	}

	public RectGLView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(0xFFFFFFFF);
		updateBound(new RectF());
	}

	public RectGLView(Context context, RectF rect, int color) {
		super(context);
		init(color);

		mBoundSpecified = true;
		updateBound(rect);
	}

	void init(int color) {
		mColor = color;

		mVertexBufferTriple = new Triple();
		for (int i = 0; i < Triple.BC; ++i) {
			FloatBuffer buffer = IBufferFactory.newFloatBuffer(POSITION_COMPONENT * VERTEX_COUNT);
			mVertexBufferTriple.setData(i, buffer);
		}

	}

	private void updateBound(RectF rect) {
		//画线需要使用像素的中点
		/** @formatter:off */
		//CHECKSTYLE IGNORE 5 LINES
        float vertices[] = {
        		rect.left + 0.5f,  -rect.top - 0.5f, 
        		rect.left + 0.5f,  -rect.bottom + 0.5f, 
        		rect.right - 0.5f, -rect.bottom + 0.5f, 
        		rect.right - 0.5f, -rect.top - 0.5f, 
        };
        /** @formatter:on */

		FloatBuffer buffer = (FloatBuffer) mVertexBufferTriple.getDataForUpdate();
		buffer.put(vertices);
		buffer.position(0);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (!mBoundSpecified) {
			updateBound(new RectF(0, 0, w, h));
		}
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		ColorShader shader = ColorShader.getShader();
		if (shader == null) {
			return;
		}

		RenderContext context = RenderContext.acquire();
		context.shader = shader;
		canvas.convertColor(mColor, context);
		canvas.getFinalMatrix(context);

		canvas.addRenderable(mRenderable, context);
	}

	private Triple mVertexBufferTriple;


	private final Renderable mRenderable = new Renderable() {
		
		@Override
		public void run(long timeStamp, RenderContext context) {
			ColorShader shader = (ColorShader) context.shader;
			if (shader == null || !shader.bind()) {
				return;
			}
			shader.setMatrix(context.matrix, 0);
			shader.setColor(context.color);
			FloatBuffer vertex = (FloatBuffer) mVertexBufferTriple.getDataForRender(timeStamp);
			shader.setPosition(vertex, POSITION_COMPONENT);
			GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, VERTEX_COUNT);
			
		}
	};
}
