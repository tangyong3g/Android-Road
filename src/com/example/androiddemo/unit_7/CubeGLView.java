package com.example.androiddemo.unit_7;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.opengl.GLES20;
import android.util.AttributeSet;

import com.go.gl.graphics.ColorAttributeShader;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.RenderContext;
import com.go.gl.graphics.Renderable;
import com.go.gl.graphics.Triple;
import com.go.gl.util.IBufferFactory;
import com.go.gl.view.GLView;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2012-9-6]
 */
public class CubeGLView extends GLView {

	private static final int VERTEX_COUNT = 8;
	private static final int POSITION_COMPONENT = 3;
	private static final int COLOR_COMPONENT = 4;

	private FloatBuffer mColorBuffer;
	private ShortBuffer mIndexBuffer;
	private int mIndexCount;
	private float mRotateX;
	private float mRotateY;
	private float mRotateZ;

	Triple mVertexBufferTriple;

	public CubeGLView(Context context) {
		super(context);
		init();
	}

	public CubeGLView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CubeGLView(Context context, AttributeSet attrs, int defStyle) {
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
//		for (int i = 0; i < colors.length; i += 4) {
//			colors[i + 0] *= colors[i + 3];
//			colors[i + 1] *= colors[i + 3];
//			colors[i + 2] *= colors[i + 3];
//		}
		mColorBuffer = IBufferFactory.newFloatBuffer(colors);

		/** @formatter:off */
		//CHECKSTYLE IGNORE 4 LINES
        short index[] = {
                0, 4, 2, 6, 3, 7, 1, 5, 
                5, 7, 
                7, 6, 5, 4, 1, 0, 3, 2,
        };
        /** @formatter:on */
		mIndexCount = index.length;
		mIndexBuffer = IBufferFactory.newShortBuffer(index);

		mVertexBufferTriple = new Triple();
		mVertexBufferTriple.setData(0, IBufferFactory.newFloatBuffer(POSITION_COMPONENT * VERTEX_COUNT));
		mVertexBufferTriple.setData(1, IBufferFactory.newFloatBuffer(POSITION_COMPONENT * VERTEX_COUNT));

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
		FloatBuffer vertexBuffer = (FloatBuffer) mVertexBufferTriple.getDataForUpdate();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

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

		canvas.setCullFaceSide(false);
		canvas.addRenderable(mRenderable, context);
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

	private final Renderable mRenderable = new Renderable() {

		@Override
		public void run(long timeStamp, RenderContext context) {
			ColorAttributeShader shader = (ColorAttributeShader) context.shader;
			if (shader == null || !shader.bind()) {
				return;
			}
			shader.setMatrix(context.matrix, 0);
			FloatBuffer vertex = (FloatBuffer) mVertexBufferTriple.getDataForRender(timeStamp);
			shader.setPosition(vertex, POSITION_COMPONENT);
			shader.setColor(mColorBuffer, COLOR_COMPONENT);
			GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, mIndexCount, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);

		}
	};
}
