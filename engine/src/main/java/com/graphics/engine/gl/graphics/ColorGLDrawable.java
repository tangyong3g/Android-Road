package com.graphics.engine.gl.graphics;

import java.nio.FloatBuffer;

import android.opengl.GLES20;

/**
 * 
 * <br>类描述:绘制颜色的{@link GLDrawable}
 * <br>功能详细描述:
 * <br>不支持{@link #setColorFilter(int, android.graphics.PorterDuff.Mode)}。
 * <br>暂不支持{@link #drawWithoutEffect(GLCanvas)}。
 * 
 * @author  dengweiming
 * @date  [2012-9-6]
 */
public class ColorGLDrawable extends GLDrawable {
	private final static int VERTEX_COUNT = 4;
	private final static int POSITION_COMPONENT = 3;

	int mColor;
	int mFadeAlpha;
	final float[] mColors = new float[4];	//CHECKSTYLE IGNORE

	/**
	 * 使用指定颜色创建一个实例
	 */
	public ColorGLDrawable(int color) {
		setColor(color, 255);	//CHECKSTYLE IGNORE
	}

	/**
	 * 设置颜色以及淡化因子
	 */
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

	@Override
	public void setAlpha(int alpha) {
		setColor(mColor, alpha);
	}

	@Override
	public void draw(GLCanvas canvas) {
		ColorShader shader = ColorShader.getShader();
		if (shader == null) {
			return;
		}

		RenderContext context = RenderContext.acquire();
		context.shader = shader;

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
		canvas.getFinalMatrix(context);

		canvas.addRenderable(sRenderable, context);
		
		VertexBufferBlock.pushVertexData(sRenderable);
		pushBoundsVertex();
	}

	private final static Renderable sRenderable = new Renderable() {	//CHECKSTYLE IGNORE
		
		@Override
		public void run(long timeStamp, RenderContext context) {
			VertexBufferBlock.popVertexData(this);
			
			ColorShader shader = (ColorShader) context.shader;
			if (shader == null || !shader.bind()) {
				VertexBufferBlock.popVertexData(POSITION_FLOAT_ELEMENTS);
				return;
			}
			shader.setColor(context.color);
			shader.setMatrix(context.matrix, 0);

			VertexBufferBlock.rewindReadingBuffer(POSITION_FLOAT_ELEMENTS);
			FloatBuffer positionBuffer = VertexBufferBlock.popVertexData(POSITION_FLOAT_ELEMENTS);
			shader.setPosition(positionBuffer, POSITION_COMPONENT);

			GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
			
		}
	};
}
