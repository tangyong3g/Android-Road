package com.graphics.engine.gl.graphics;

import java.nio.FloatBuffer;

import android.opengl.GLES20;

/**
 * 
 * <br>类描述:绘制纯色几何形状的类
 * <br>功能详细描述:
 * 支持OpenGL ES默认的几何形状：点，线，三角形。
 * <ul> 一些常用的方法：
 * 	<li>{@link #setColor(int)} 设置颜色
 * 	<li>{@link #setAlpha(int)} 设置不透明度
 * 	<li>{@link #draw(GLCanvas, int, float[], int, int, boolean)} 绘制图形
 * </ul>
 * 
 * @author  dengweiming
 * @date  [2012-9-6]
 */
public class ColorShape {
	/** 绘制模式：点，每个顶点独立 */
	public final static int POINTS = 0;
	/** 绘制模式：线段带，每个顶点依次连接 */
	public final static int LINE_STRIP = 1;
	/** 绘制模式：线段圈，每个顶点依次连接，首尾两个顶点连接 */
	public final static int LINE_LOOP = 2;
	/** 绘制模式：线段，每两个顶点连接 */
	public final static int LINES = 3;
	/** 绘制模式：填充的三角形带，第一二三个顶点连接成逆时针的三角形，第二三四个顶点连接成顺时针的三角形，依次类推。 */
	public final static int TRIANGLE_STRIP = 4;
	/** 绘制模式：填充的三角形扇，第一个顶点为公共顶点，其余每两个顶点和公共顶点连接 */
	public final static int TRIANGLE_FAN = 5;
	/** 绘制模式：填充的三角形，每三个顶点连接 */
	public final static int TRIANGLES = 6;
	
	private final static int[] MODE = {
		GLES20.GL_POINTS,
		GLES20.GL_LINE_STRIP,
		GLES20.GL_LINE_LOOP,
		GLES20.GL_LINES,
		GLES20.GL_TRIANGLE_STRIP,
		GLES20.GL_TRIANGLE_FAN,
		GLES20.GL_TRIANGLES,
	};
	
	private final static float ONE_OVER_255 = 1 / 255.0f;
	private final static int[] POSITION_COMPONENT = {2, 3};
	
	//绘制时的顶点位置是否为3的标志，模式，以及顶点数量，按以下规则编码：
	//private final static int POSITION_COMPONENT_SHIFT = 0;
	private final static int MODE_SHIFT = 1;
	private final static int COUNT_SHIFT = 4;
	private final static int POSITION_COMPONENT_MASK = 1;
	private final static int MODE_MASK = (1 << COUNT_SHIFT) - 1 & ~POSITION_COMPONENT_MASK;
	private final static int COUNT_MASK = ~POSITION_COMPONENT_MASK & ~MODE_MASK;
	private final static int MAX_COUNT = 1 << 16;

	int mColor;
	int mFadeAlpha;
	final float[] mColors = new float[4];	//CHECKSTYLE IGNORE

	/**
	 * 使用指定颜色创建一个实例
	 */
	public ColorShape(int color) {
		setColor(color, 255);	//CHECKSTYLE IGNORE
	}

	/**
	 * 设置颜色和不透明度
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
	 * 设置颜色
	 */
	public void setColor(int color) {
		setColor(color, mFadeAlpha);
	}

	/**
	 * 设置不透明度
	 * @param alpha 0表示完全透明，255表示完全不透明
	 */
	public void setAlpha(int alpha) {
		setColor(mColor, alpha);
	}

	/**
	 * <br>功能简述: 绘制几何图形
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param canvas 画布
	 * @param mode	图形模式：{@link #POINTS}, {@link #LINES}, {@link #LINE_STRIP}, {@link #LINE_LOOP}, 
	 * {@link #TRIANGLES}, {@link #TRIANGLE_STRIP}, {@link #TRIANGLE_FAN}
	 * @param vertex	顶点位置数组，各个顶点的位置 (x, y {,z}) 依次排列
	 * @param offset	<var>vertex</var>的有效数据的偏移量
	 * @param vertexCount	顶点个数
	 * @param is3D	顶点位置是否为3D的。如果是则需要z分量,Y轴方向向下；否则只需要x和y分量,且Y轴方向向上。
	 */
	public void draw(GLCanvas canvas, int mode, float[] vertex, int offset, int vertexCount, boolean is3D) {
		if (vertexCount <= 0) {
			throw new RuntimeException("Too less vertex to draw.");
		}
		if (vertexCount > MAX_COUNT) {
			throw new RuntimeException("Too many vertex to draw.");
		}
		int elements = vertexCount * (is3D ? 3 : 2);
		if (offset + elements > vertex.length) {
			throw new IndexOutOfBoundsException("Too many vertex to draw.");
		}
		
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
		context.alpha = encode(is3D, mode, vertexCount);
		canvas.getFinalMatrix(context);

		VertexBufferBlock.pushVertexData(mRenderable);
		canvas.addRenderable(mRenderable, context);
		VertexBufferBlock.pushVertexData(vertex, offset, elements);
	}
	
	/** 将几个绘制的参数组合成一个整数以便传递到GL线程 */
	private static int encode(boolean is3D, int mode, int count) {
		int code = (count << COUNT_SHIFT) | (mode << MODE_SHIFT);
		return is3D ? code | 1 : code;
	}

	/** 从参数组合代码中获取顶点位置分量 */
	private static int decodeVertexComponent(int code) {
		return POSITION_COMPONENT[code & POSITION_COMPONENT_MASK];
	}
	
	/** 从参数组合代码中获取顶点图形模式 */
	private static int decodeMode(int code) {
		return MODE[(code & MODE_MASK) >>> MODE_SHIFT];
	}
	
	/** 从参数组合代码中获取顶点数目 */
	private static int decodeCount(int code) {
		return (code & COUNT_MASK) >>> COUNT_SHIFT;
	}

	private final Renderable mRenderable = new Renderable() {	//CHECKSTYLE IGNORE
		
		@Override
		public void run(long timeStamp, RenderContext context) {
			VertexBufferBlock.popVertexData(this);
			
			final int code = (int) context.alpha;
			final int positionComponent = decodeVertexComponent(code);
			final int mode = decodeMode(code);
			final int count = decodeCount(code);
			final int elements = positionComponent * count;
			
			VertexBufferBlock.rewindReadingBuffer(elements);
			ColorShader shader = (ColorShader) context.shader;
			if (shader == null || !shader.bind()) {
				VertexBufferBlock.popVertexData(null, 0, elements);
				return;
			}
			shader.setColor(context.color);
			shader.setMatrix(context.matrix, 0);

			FloatBuffer positionBuffer = VertexBufferBlock.popVertexData(elements);
			shader.setPosition(positionBuffer, positionComponent);

			GLES20.glDrawArrays(mode, 0, count);
			
		}
	};
}
