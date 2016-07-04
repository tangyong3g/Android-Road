package com.graphics.engine.gl.graphics.geometry;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.graphics.engine.gl.graphics.ColorShader;
import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.graphics.GLShaderProgram;
import com.graphics.engine.gl.graphics.IndexBufferBlock;
import com.graphics.engine.gl.graphics.RenderContext;
import com.graphics.engine.gl.graphics.Renderable;
import com.graphics.engine.gl.graphics.VertexBufferBlock;

import android.opengl.GLES20;

/**
 * 
 * <br>类描述: 纯色的物体渲染器{@link GLObjectRender}
 * <br>功能详细描述:
 * 使用{@link #setColor(int)}设置颜色，使用{@link #setAlpha(int)}设置淡化因子
 * 
 * @author  dengweiming
 * @date  [2013-7-15]
 */
public class ColorGLObjectRender extends BaseGLObjectRender {
	protected final static int WHITE = 0xFFFFFFFF;
	
	int mColor;
	int mFadeAlpha;
	final float[] mColors = new float[4];	//CHECKSTYLE IGNORE

	public ColorGLObjectRender() {
		setColor(WHITE, FULL_ALPHA);
	}
	
	void setColor(int color, int alpha) {
		if (mColor == color && mFadeAlpha == alpha) {
			return;
		}
		mColor = color;
		mFadeAlpha = alpha;
		//CHECKSTYLE IGNORE 5 LINES
		final float a = (color >>> 24) * ONE_OVER_255 * alpha * ONE_OVER_255;
		mColors[0] = (color >>> 16 & 0xFF) * a * ONE_OVER_255;
		mColors[1] = (color >>> 8 & 0xFF) * a * ONE_OVER_255;
		mColors[2] = (color & 0xFF) * a * ONE_OVER_255;
		mColors[3] = a;
	}
	
	/**
	 * <br>功能简述: 设置颜色
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param color
	 */
	public void setColor(int color) {
		setColor(color, mFadeAlpha);
	}

	/**
	 * <br>功能简述: 设置额外的淡化因子
	 * <br>功能详细描述:
	 * <br>注意:  {@link GLCanvas#setAlpha(int)} 设置的淡化因子会累乘进去
	 * @param alpha [0..255]，0表示淡化至完全透明，255表示完全不淡化。
	 */
	public void setAlpha(int alpha) {
		setColor(mColor, alpha);
	}

	@Override
	public void draw(GLCanvas canvas, GLObject object) {
		GLShaderProgram shader = ColorShader.getShader();
		if (shader == null) {
			return;
		}
		
		if (!putData(object, sRenderable)) {
			return;
		}
		
		RenderContext context = RenderContext.acquire();
		context.shader = shader;
		canvas.getFinalMatrix(context);
		
		final int fadeAlpha = canvas.getAlpha();
		//CHECKSTYLE IGNORE 12 LINES
		if (fadeAlpha < 255) {
			final float a = fadeAlpha * ONE_OVER_255;
			context.color[0] = mColors[0] * a;
			context.color[1] = mColors[1] * a;
			context.color[2] = mColors[2] * a;
			context.color[3] = mColors[3] * a;
		} else {
			context.color[0] = mColors[0];
			context.color[1] = mColors[1];
			context.color[2] = mColors[2];
			context.color[3] = mColors[3];
		}
		canvas.addRenderable(sRenderable, context);
	}
	
	private final static Renderable sRenderable = new Renderable() {	//CHECKSTYLE IGNORE

		@Override
		public void run(long timeStamp, RenderContext context) {
			VertexBufferBlock.popVertexData(this);
			float[] info = TEMP_FLOAT_BUFFER_GL;
			VertexBufferBlock.popVertexData(info, 0, INDEX_LAST);
			if (info[INDEX_MARK] != MARK) {
				throw new RuntimeException("mark incorrect!");
			}
			final int mode = GLObject.MODE[(int) info[INDEX_DRAW_MODE]];
			final int indexCount = (int) info[INDEX_INDEX_COUNT];
			final int vertexCount = (int) info[INDEX_VERTEX_COUNT];
			final int totalVertexData = (int) info[INDEX_TOTAL_VERTEX_DATA];
			
			if (context.shader == null || !context.shader.bind()) {
				VertexBufferBlock.popVertexData(null, 0, totalVertexData);
				IndexBufferBlock.popVertexData(null, 0, indexCount);
				return;
			}

			ColorShader shader = (ColorShader) context.shader;
			shader.setColor(context.color);
			shader.setMatrix(context.matrix, 0);

			VertexBufferBlock.rewindReadingBuffer(totalVertexData);
			FloatBuffer positionBuffer = VertexBufferBlock.popVertexData((int) info[INDEX_POSITION_ELEMENTS]);
			shader.setPosition(positionBuffer, (int) info[INDEX_POSITION_COMPONENT]);
			
			if (indexCount > 0) {
				IndexBufferBlock.rewindReadingBuffer(indexCount);
				ShortBuffer indexBuffer = IndexBufferBlock.popVertexData(indexCount);
				GLES20.glDrawElements(mode, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
			} else {
				GLES20.glDrawArrays(mode, 0, vertexCount);
			}

		}
	};

}
