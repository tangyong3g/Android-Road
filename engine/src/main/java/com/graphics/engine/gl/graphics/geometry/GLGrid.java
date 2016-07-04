package com.graphics.engine.gl.graphics.geometry;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.graphics.engine.gl.graphics.ColorShader;
import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.graphics.GLVBO;
import com.graphics.engine.gl.graphics.IndexBufferBlock;
import com.graphics.engine.gl.graphics.RenderContext;
import com.graphics.engine.gl.graphics.Renderable;
import com.graphics.engine.gl.graphics.VertexBufferBlock;

import android.graphics.RectF;
import android.opengl.GLES20;

/**
 * <br>类描述: 纵横分割的网格
 * <br>功能详细描述:
 * 默认是一个Z轴正朝向的矩形，纵横均匀分割成若干份。但是通过修改顶点位置，可以构造出复杂的形状，例如
 * 球体{@link GLSphere}，圆柱体{@link GLCylinder}。未来可以支持山形。
 * 
 * @author  dengweiming
 * @date  [2013-10-22]
 */
public class GLGrid extends GLObject {
	private static final int MAX_DIV = (1 << 15) - 1; //CHECKSTYLE IGNORE
	
	int mDivX;
	int mDivY;
	boolean mFill;
	final float[] mLineColors = new float[4]; //CHECKSTYLE IGNORE
	int mPositionArrayStride;
	final RectF mBounds = new RectF();
	
	boolean mUseVBO;
	GLVBO mIndexVBO;
	GLVBO mPositionVBO;
	GLVBO mTexcoordVBO;
	GLVBO mNormalVBO;

	/**
	 * 创建一个纵横分割的网格
	 * <br>注意：分割的份数不宜过多，否则会影响效率，或者可能会超出Native内存，
	 * 一般24~64就足够，跟屏幕上显示尺寸以及dpi相关。
	 * @param xDiv 横向分割的份数，即有 xDiv+1 个点
	 * @param yDiv 竖向分割的份数，即有 yDiv+1 个点
	 * @param fill 是否填充，否则只是线框
	 * @see {@link #drawInLineMode(GLCanvas)}
	 */
	public GLGrid(int xDiv, int yDiv, boolean fill) {
		if (xDiv < 1 || xDiv > MAX_DIV) {
			throw new IllegalArgumentException("xDiv");
		}
		if (yDiv < 1 || yDiv > MAX_DIV) {
			throw new IllegalArgumentException("yDiv");
		}
		if ((xDiv + 1) * (yDiv + 1) > MAX_DIV) {
			throw new IllegalArgumentException("(xDiv + 1) * (yDiv + 1) > " + MAX_DIV);
		}

		mDivX = xDiv;
		mDivY = yDiv;
		mFill = fill;
		mVertexCount = (mDivX + 1) * (mDivY + 1);
		mPositionElements = mVertexCount * mPositionComponent;
		mPositionArrayStride = (mDivX + 1) * mPositionComponent;
		
		mPositionArray = new float[mPositionElements];
		
		if (mFill) {
			mMode = TRIANGLE_STRIP;
			//总共yDiv个三角形带，每两个yDiv三角形带之间需要补充两个顶点（共2*yDiv-2个）来连接
			mIndexCount = (xDiv + 1) * 2 * yDiv + 2 * (yDiv - 1);
			mIndexArray = new short[mIndexCount];
			final short[] array = mIndexArray;
			int loc = 0;
			short index1 = 0;
			short index2 = (short) (xDiv + 1);
			
			for (int y = 0; y < yDiv; ++y) {
				for (int x = 0; x <= xDiv; ++x) {
					array[loc++] = index1++;
					array[loc++] = index2++;
				}
				if (y < yDiv - 1) {
					array[loc++] = (short) (index2 - 1);
					array[loc++] = index1;
				}
			}
			
		} else {
			mMode = LINES;
			mIndexCount = xDiv * 2 * (yDiv + 1) + yDiv * 2 * (xDiv + 1);
			mIndexArray = new short[mIndexCount];
			final short[] array = mIndexArray;
			int loc = 0;
			short index1 = 0;
			short index2 = 1;
			
			for (int y = 0; y <= yDiv; ++y) {
				for (int x = 0; x < xDiv; ++x) {
					array[loc++] = index1++;
					array[loc++] = index2++;
				}
				index1++;
				index2++;
			}
			
			final int offset = xDiv + 1;
			for (int x = 0; x <= xDiv; ++x) {
				index1 = (short) x;
				index2 = (short) (index1 + offset);
				for (int y = 0; y < yDiv; ++y) {
					array[loc++] = index1;
					array[loc++] = index2;
					index1 = index2;
					index2 += offset;
				}
			}
			
			setLineColor(0xFFFFFFFF);
		}
	}
	
	/**
	 * 获取横向分割的份数，也即横向顶点个数减一。
	 */
	public final int getDivX() {
		return mDivX;
	}
	
	/**
	 * 获取纵向分割的份数，也即纵向顶点个数减一。
	 */
	public final int getDivY() {
		return mDivY;
	}
	
	/**
	 * 获取顶点位置二维数组每行的跨距
	 */
	protected final int getPositionArrayStride() {
		return mPositionArrayStride;
	}
	
	/**
	 * 设置线框图的颜色
	 * @see {@link #drawInLineMode(GLCanvas)}
	 */
	public void setLineColor(int color) {
		GLCanvas.convertColorToPremultipliedFormat(color, mLineColors, 0);
	}
	
	/**
	 * 绘制线框图。
	 * <br>注意构造方法{@link #GLMesh(int, int, boolean)}中需要指定第三个参数 fill 为 false
	 * @see {@link #setLineColor(int)}
	 */
	public boolean drawInLineMode(GLCanvas canvas) {
		if (mFill) {
			return false;
		}
		if (mLineModeRenderable == null) {
			mLineModeRenderable = new LineModeRenderable();
		}
		
		ColorShader shader = ColorShader.getShader();
		if (shader == null) {
			return true;
		}
		
		RenderContext context = RenderContext.acquire();
		context.shader = shader;

		final int fadeAlpha = canvas.getAlpha();
		//CHECKSTYLE IGNORE 12 LINES
		if (fadeAlpha < 255) {
			final float a = fadeAlpha * GLCanvas.OneOver255;
			context.color[0] = mLineColors[0] * a;
			context.color[1] = mLineColors[1] * a;
			context.color[2] = mLineColors[2] * a;
			context.color[3] = mLineColors[3] * a;
		} else {
			context.color[0] = mLineColors[0];
			context.color[1] = mLineColors[1];
			context.color[2] = mLineColors[2];
			context.color[3] = mLineColors[3];
		}
		canvas.getFinalMatrix(context);

		canvas.addRenderable(mLineModeRenderable, context);
		
		VertexBufferBlock.pushVertexData(mLineModeRenderable);
		VertexBufferBlock.pushVertexData(mPositionArray, 0, mVertexCount * mPositionComponent);
		IndexBufferBlock.pushVertexData(mIndexArray, 0, mIndexCount);
		
		return true;
	}
	
	@Override
	public final void setBounds(float left, float top, float right, float bottom) {
		if (mBounds.left != left 
			|| mBounds.top != top 
			|| mBounds.right != right 
			|| mBounds.bottom != bottom) {
			mBounds.set(left, top, right, bottom);
			onBoundsChange(left, top, right, bottom);
		}
	}
	
	protected void onBoundsChange(float left, float top, float right, float bottom) {
		final float dx = (right - left) / mDivX;
		final float dy = -(bottom - top) / mDivY;
		final float[] pos = mPositionArray;
		
		float posX = left, posY = -top;
		int loc = 0;
		
		for (int y = 0; y <= mDivY; ++y) {
			posX = left;
			for (int x = 0; x <= mDivX; ++x) {
				pos[loc++] = posX;
				pos[loc++] = posY;
				pos[loc++] = 0;
				posX += dx;
			}
			posY += dy;
		}
		
		if (mUseVBO) {
			mPositionVBO.setData(mPositionArray);
		}
	}
	
	@Override
	public void setTexcoords(float u1, float v1, float u2, float v2) {
		if (mTexcoordArray == null) {
			mTexcoordElements = mTexcoordComponent * mVertexCount;
			mTexcoordArray = new float[mTexcoordElements];
		}
		
		final float du = (u2 - u1) / mDivX;
		final float dv = (v2 - v1) / mDivY;
		final float[] tex = mTexcoordArray;
		
		float u = u1, v = v1;
		int loc = 0;
		
		for (int y = 0; y <= mDivY; ++y) {
			u = u1;
			for (int x = 0; x <= mDivX; ++x) {
				tex[loc++] = u;
				tex[loc++] = v;
				u += du;
			}
			v += dv;
		}
		
		if (mUseVBO) {
			mTexcoordVBO.setData(mTexcoordArray);
		}
	}
	
	/**
	 * <br>功能简述: 设置某个顶点的位置
	 * <br>功能详细描述:
	 * <br>注意: 由于有边界检测，因此不适用于频繁调用
	 * @param locX	顶点的横向索引
	 * @param locY	顶点的纵向索引
	 * @param x	位置X
	 * @param y	位置Y
	 * @param z	位置Z
	 */
	public void setPosition(int locX, int locY, float x, float y, float z) {
		if (locX < 0 || locY < 0 || locX > mDivX || locY > mDivY) {
			throw new IndexOutOfBoundsException("loc(" + locX + ", " + locY +
					") is out of [0, " + mDivX + "]x[0, " + mDivY + "]");
		}
		int index = mPositionArrayStride * locY + locX * mPositionComponent;
		if (mPositionComponent > 0) {
			mPositionArray[index++] = x;
		}
		if (mPositionComponent > 1) {
			mPositionArray[index++] = y;
		}
		if (mPositionComponent > 2) {
			mPositionArray[index++] = z;
		}
		
		if (mUseVBO) {
			mPositionVBO.setData(mPositionArray);
		}
	}
	
	/**
	 * 获取设置的边界。
	 * @see {@link #setBounds(float, float, float, float)}
	 */
	public final RectF getBounds() {
		return mBounds;
	}
	
	@Override
	public void updateNormal() {
		//TODO
	}

	@Override
	public boolean setUseVBO(boolean use, boolean clear) {
		if (mUseVBO == use) {
			return true;
		}
		mUseVBO = use;
		if (!mUseVBO) {
			if (clear) {
				clearVBO();
			}
			return true;
		}
		
		mIndexVBO = new GLVBO(true);
		mIndexVBO.setData(mIndexArray);
		
		mPositionVBO = new GLVBO(false);
		mPositionVBO.setData(mPositionArray);
		
		mTexcoordVBO = new GLVBO(false);
		if (mTexcoordArray != null) {
			mTexcoordVBO.setData(mTexcoordArray);
		}
		
		mNormalVBO = new GLVBO(false);
		if (mNormalArray != null) {
			mNormalVBO.setData(mNormalArray);
		}
		return true;
	}
	
	@Override
	public GLVBO getIndexVBO() {
		return mIndexVBO;
	}
	
	@Override
	public GLVBO getPositionVBO() {
		return mPositionVBO;
	}
	
	@Override
	public GLVBO getTexcoordVBO() {
		return mTexcoordVBO;
	}
	
	@Override
	public GLVBO getNormalVBO() {
		return mNormalVBO;
	}
	
	private void clearVBO() {
		if (mIndexVBO != null) {
			mIndexVBO.clear();
			mIndexVBO = null;
		}
		if (mPositionVBO != null) {
			mPositionVBO.clear();
			mPositionVBO = null;
		}
		if (mTexcoordVBO != null) {
			mTexcoordVBO.clear();
			mTexcoordVBO = null;
		}
		if (mNormalVBO != null) {
			mNormalVBO.clear();
			mNormalVBO = null;
		}
	}
	
	@Override
	public void clear() {
		super.clear();
		clearVBO();
	}
	
	private Renderable mLineModeRenderable;

	private class LineModeRenderable implements Renderable {	//CHECKSTYLE IGNORE
		
		@Override
		public void run(long timeStamp, RenderContext context) {
			VertexBufferBlock.popVertexData(this);
			ColorShader shader = (ColorShader) context.shader;
			if (shader == null || !shader.bind()) {
				VertexBufferBlock.popVertexData(mPositionElements);
				IndexBufferBlock.popVertexData(mIndexCount);
				return;
			}
			shader.setColor(context.color);
			shader.setMatrix(context.matrix, 0);

			VertexBufferBlock.rewindReadingBuffer(mPositionElements);
			FloatBuffer positionBuffer = VertexBufferBlock.popVertexData(mPositionElements);
			shader.setPosition(positionBuffer, mPositionComponent);
			
			IndexBufferBlock.rewindReadingBuffer(mIndexCount);
			ShortBuffer indexBuffer = IndexBufferBlock.popVertexData(mIndexCount);

			GLES20.glDrawElements(MODE[mMode], mIndexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer);
			
		}
	};
	
	
}
