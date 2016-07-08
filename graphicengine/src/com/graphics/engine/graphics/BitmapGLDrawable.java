package com.graphics.engine.graphics;

import java.nio.FloatBuffer;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLES20;

/**
 * 
 * <br>类描述: 可以绘制位图{@link Bitmap}的{@link GLDrawable}
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-10-15]
 */
public class BitmapGLDrawable extends GLDrawable {
	/** 平铺时只复制边界 */
    public static final int WRAP_CLAMP = GLES20.GL_CLAMP_TO_EDGE;
    /** 平铺时重复全部 */
    public static final int WRAP_REPEAT = GLES20.GL_REPEAT;
    /** 平铺时镜像重复全部（即相邻两个复制品互为镜像） */
    public static final int WRAP_MIRRORED_REPEAT = GLES20.GL_MIRRORED_REPEAT;

	private final static int TEXCOORD_COMPONENT = 2;
	private final static int TEXCOORD_FLOAT_ELEMENTS = TEXCOORD_COMPONENT * VERTEX_COUNT;

	private Bitmap mBitmap;
	private float mAlpha = 1;
	private final float[] mSrcColor = new float[4]; // CHECKSTYLE IGNORE
	private int mPorterDuffMode = TextureShader.MODE_NONE;

	private Texture mTexture;
	private float[] mTexCoordBuffer;
	private static final float[] DEFAULT_TEXCOORD_BUFFER = new float[]{ 0, 0, 0, 1, 1, 0, 1, 1 };
	
	private GLShaderWrapper mShaderWrapper;
	
	private final float[] mWrapMode = new float[] { WRAP_CLAMP, WRAP_CLAMP };

	/**
	 * 使用位图bitmap来创建一个对象，由资源res指定位图的dpi。
	 */
	public BitmapGLDrawable(Resources res, Bitmap bitmap) {
		this(new BitmapDrawable(res, bitmap));
	}

	/**
	 * 使用BitmapDrawable来创建一个对象
	 * @see {@link GLDrawable#getDrawable(android.content.res.Resources, int)}
	 */
	public BitmapGLDrawable(BitmapDrawable drawable) {
		this();
		if (drawable == null) {
			return;
		}
		final Bitmap bitmap = drawable.getBitmap();
		mBitmap = bitmap;
		mTexture = BitmapTexture.createSharedTexture(bitmap);

		mIntrinsicWidth = drawable.getIntrinsicWidth();
		mIntrinsicHeight = drawable.getIntrinsicHeight();
		setBounds(0, 0, mIntrinsicWidth, mIntrinsicHeight);
		
		if (mTexture != null && mTexture.isMipMapEnabled()) {
			int[] paddedSize = new int[2];
			Texture.solvePaddedSize(mIntrinsicWidth, mIntrinsicHeight, paddedSize, true);
			final float sx = mIntrinsicWidth / (float) paddedSize[0];
			final float sy = mIntrinsicHeight / (float) paddedSize[1];
			mTexCoordBuffer = new float[] { 0, 0, 0, sx, sy, 0, sx, sy };
		}
		
		mOpaque = drawable.getOpacity();
	}

	/**
	 * <默认构造函数>
	 * 
	 * @hide
	 */
	public BitmapGLDrawable() {
		mTexCoordBuffer = DEFAULT_TEXCOORD_BUFFER;

		register();
	}
	
	/**
	 * <br>功能简述: 设置本质大小，应该作用于用默认构造函数构造的对象
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param width
	 * @param height
	 * @hide
	 */
	public void setIntrinsicSize(int width, int height) {
		if (width > 0) {
			mIntrinsicWidth = width;
		}
		if (height > 0) {
			mIntrinsicHeight = height;
		}
	}
	

	/**
	 * <br>功能简述: 设置顶点的纹理坐标
	 * <br>功能详细描述:
	 * <br>注意: 
	 * <br>各个边界值归一化，如果纹理本身不是[0,1]x[0,1]的（例如使用了{@link Texture#mipMapNextTexture(boolean)}，
	 * 会被填充成2的幂大小，原来的纹理占最终的纹理的比例往往就不是1），要注意缩放它们。
	 * <br>u轴向右，v轴向下，即左上角为(0,0)，右下角为(1,1)。
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	public void setTexCoord(float left, float top, float right, float bottom) {
		if (mTexCoordBuffer == DEFAULT_TEXCOORD_BUFFER) {
			mTexCoordBuffer = new float[TEXCOORD_FLOAT_ELEMENTS];
		}
		final float[] buf = mTexCoordBuffer;
		int i = 0;

		buf[i++] = left;
		buf[i++] = top;
		buf[i++] = left;
		buf[i++] = bottom;
		buf[i++] = right;
		buf[i++] = top;
		buf[i++] = right;
		buf[i++] = bottom;
	}
	
	/**
	 * <br>功能简述: 设置部分绘制
	 * <br>功能详细描述: 
	 * <br>注意: 
	 * 因为会将源区域按照当前区域映射成目标区域，并设置为新的区域，因此如果调用多次本方法就会有问题，需要在调用本方法之前先备份旧的区域，
	 * 在绘制完或者下次调用本方法前需要还原成备份的区域。同理，纹理坐标也是。如下例：
	 * <pre><code>
	 * drawable.setPartiallyDraw(srcRect, boundsBak, texcoordBak);
	 * drawable.draw(canvas);
	 * drawable.setBounds((int) boundsBak.left, (int) boundsBak.top, (int) boundsBak.right, (int) boundsBak.bottom);
	 * drawable.setTexCoord(texcoordBak.left, texcoordBak.top, texcoordBak.right, texcoordBak.bottom);
	 * </code></pre>
	 * @param srcRect 源区域，在原始尺寸{@link #getIntrinsicWidth()}和{@link #getIntrinsicHeight()}中度量
	 * @param boundsBak 由于会修改到边界，所以需要将原始边界备份到这里，可以为null
	 * @param texcoordBak 由于会修改到纹理坐标，所以将原始纹理坐标备份到这里，可以为null
	 */
	public void setPartiallyDraw(RectF srcRect, RectF boundsBak, RectF texcoordBak) {
		Rect bounds = getBounds();
		float xscale = bounds.width() / (float) mIntrinsicWidth;
		float yscale = bounds.height() / (float) mIntrinsicHeight;
		int l = (int) Math.floor(srcRect.left * xscale);
		int t = (int) Math.floor(srcRect.top * yscale);
		int r = (int) Math.ceil(srcRect.right * xscale);
		int b = (int) Math.ceil(srcRect.bottom * yscale);
		
		float u0 = mTexCoordBuffer[0];
		float v0 = mTexCoordBuffer[1];
		float u1 = mTexCoordBuffer[6];
		float v1 = mTexCoordBuffer[7];
		float uscale = (u1 - u0) / bounds.width();
		float vscale = (v1 - v0) / bounds.height();
		
		if (boundsBak != null) {
			boundsBak.set(bounds);
		}
		if (texcoordBak != null) {
			texcoordBak.set(u0, v0, u1, v1);
		}
		
		//因为bounds只能是整数，所以纹理坐标的计算要使用取整后的l,t,r,b
		setBounds(l + bounds.left, t + bounds.top, r + bounds.left, b + bounds.top);
		setTexCoord(l * uscale + u0, t * vscale + v0, r * uscale + u0, b * vscale + v0);

	}
	
	/**
	 * <br>功能简述: 设置平铺模式（也即纹理坐标的回绕模式）
	 * <br>功能详细描述: 可以实现图片在bounds内平铺的效果。
	 * <br>例如设置纹理坐标为(0,0-2,2)范围内，两个方向的模式都为repeat，就得到2x2的平铺效果。
	 * <br>注意: 
	 * @param modeS 横向的回绕模式，可以为 {@link #WRAP_CLAMP}（默认），{@link #WRAP_REPEAT}， {@link #WRAP_MIRRORED_REPEAT}，
	 * 后面两种需要图片的宽度为2的幂（除非GPU支持GL_OES_texture_npot扩展，但是最好还是统一处理）。
	 * 
	 * @param modeT	纵向的回绕模式
	 * @see {@link #setTexCoord(float, float, float, float)}
	 */
	public void setWrapMode(int modeS, int modeT) {
		mWrapMode[0] = modeS;
		mWrapMode[1] = modeT;
	}

	@Override
	@SuppressLint("WrongCall")
	public void draw(GLCanvas canvas) {
		if (mTexture == null) {
			return;
		}

		TextureShader shader = null;
		final int fadeAlpha = canvas.getAlpha();
		float alpha = mAlpha;
		if (fadeAlpha < 255) {	//CHECKSTYLE IGNORE
			alpha *= fadeAlpha * ONE_OVER_255;
		}
		if (mShaderWrapper != null) {
			RenderContext context = RenderContext.acquire();
			context.shader = mShaderWrapper;
			context.alpha = alpha;
			context.texture = mTexture;
			canvas.getFinalMatrix(context);
			mShaderWrapper.onDraw(context);
			canvas.addRenderable(sRenderable, context);
			
			// push data
			VertexBufferBlock.pushVertexData(sRenderable);
			pushBoundsVertex();
			VertexBufferBlock.pushVertexData(mTexCoordBuffer, 0, TEXCOORD_FLOAT_ELEMENTS);
			VertexBufferBlock.pushVertexData(mWrapMode, 0, mWrapMode.length);
			return;
		}
		
		if (mPorterDuffMode == TextureShader.MODE_NONE) {
			shader = TextureShader.getShader(alpha >= 1
					? TextureShader.MODE_NONE
					: TextureShader.MODE_ALPHA);
		} else {
			shader = TextureShader.getShader(mPorterDuffMode);
		}

		if (shader == null) {
			return;
		}

		RenderContext context = RenderContext.acquire();
		
		//CHECKSTYLE IGNORE 4 LINES
		context.color[0] = mSrcColor[0];
		context.color[1] = mSrcColor[1];
		context.color[2] = mSrcColor[2];
		context.color[3] = mSrcColor[3];
		context.texture = mTexture;
		context.shader = shader;
		context.alpha = alpha;
		canvas.getFinalMatrix(context);

		canvas.addRenderable(sRenderable, context);
		
		// push data
		VertexBufferBlock.pushVertexData(sRenderable);
		pushBoundsVertex();
		VertexBufferBlock.pushVertexData(mTexCoordBuffer, 0, TEXCOORD_FLOAT_ELEMENTS);
		VertexBufferBlock.pushVertexData(mWrapMode, 0, mWrapMode.length);
	}
	
	@Override
	public void drawWithoutEffect(GLCanvas canvas) {
		if (mTexture == null) {
			return;
		}

		RenderContext context = RenderContext.acquire();
		context.texture = mTexture;
		context.shader = TextureShader.getShader(TextureShader.MODE_NONE);
		canvas.getFinalMatrix(context);
		canvas.addRenderable(sRenderable, context);
		
		// push data
		VertexBufferBlock.pushVertexData(sRenderable);
		pushBoundsVertex();
		VertexBufferBlock.pushVertexData(mTexCoordBuffer, 0, TEXCOORD_FLOAT_ELEMENTS);
		VertexBufferBlock.pushVertexData(mWrapMode, 0, mWrapMode.length);
	}

	@Override
	public void onTextureInvalidate() {
		if (mTexture != null) {
			mTexture.onTextureInvalidate();
		}
	}

	@Override
	public void clear() {
		if (mReferenceCount <= 0 || --mReferenceCount > 0) {
			return;
		}
		unregister();
		if (mTexture != null) {
			mTexture.clear();
			mTexture = null;
		}
		mShaderWrapper = null;
		mBitmap = null;
	}

	@Override
	public void setColorFilter(int srcColor, Mode mode) {
		if (mode == null) {
			mPorterDuffMode = TextureShader.MODE_NONE;
			return;
		}
		//从ARGB转成(r, g, b, a)的alpha-premultiplied格式
		//CHECKSTYLE IGNORE 5 LINES
		final float a = (srcColor >>> 24) * ONE_OVER_255;
		mSrcColor[0] = (srcColor >>> 16 & 0xFF) * a * ONE_OVER_255;
		mSrcColor[1] = (srcColor >>> 8 & 0xFF) * a * ONE_OVER_255;
		mSrcColor[2] = (srcColor & 0xFF) * a * ONE_OVER_255;
		mSrcColor[3] = a;
		mPorterDuffMode = mode.ordinal();
	}

	@Override
	public void setAlpha(int alpha) {
		if (alpha == 255) {	//CHECKSTYLE IGNORE
			mAlpha = 1;
		} else {
			mAlpha = alpha * ONE_OVER_255;
		}
	}

	/**
	 * <br>功能简述: 设置新的纹理
	 * <br>功能详细描述:
	 * <br>注意: 会将当前的纹理释放
	 * @param texture
	 * 
	 */
	@Override
	public void setTexture(Texture texture) {
		if (mTexture != texture && mTexture != null) {
			mTexture.clear();
		}
		mTexture = texture;
		if (mTexture != null && mTexture instanceof BitmapTexture) {
			mBitmap = ((BitmapTexture) mTexture).getBitmap();
		} else {
			mBitmap = null;
		}
	}
	
	@Override
	public Texture getTexture() {
		return mTexture;
	}
	
	@Override
	public Bitmap getBitmap() {
		return mBitmap;
	}
	
	@Override
	public void setShaderWrapper(GLShaderWrapper shader) {
		mShaderWrapper = shader;
	}
	
	@Override
	public GLShaderWrapper getShaderWrapper() {
		return mShaderWrapper;
	}
	
	@Override
	public void yield() {
		if (mTexture != null) {
			mTexture.yield();
		}
	}
	
	@Override
	public boolean isBitmapRecycled() {
		return mBitmap != null && mBitmap.isRecycled();
	}
	
	private final static Renderable sRenderable = new Renderable() {	//CHECKSTYLE IGNORE
		final static int DATA_SIZE = POSITION_FLOAT_ELEMENTS + TEXCOORD_FLOAT_ELEMENTS + 2;
		final float[] mWrapModeBuffer = new float[2]; 
		
		@Override
		public void run(long timeStamp, RenderContext context) {
			VertexBufferBlock.popVertexData(this);
			
			if (context.texture == null || !context.texture.bind()) {
				VertexBufferBlock.popVertexData(null, 0, DATA_SIZE);
				return;
			}
			if (context.shader == null) {
				VertexBufferBlock.popVertexData(null, 0, DATA_SIZE);
				return;
			}
			GLShaderProgram shaderProgram = context.shader.onRender(context);
			if (shaderProgram == null || !(shaderProgram instanceof TextureShader)) {
				VertexBufferBlock.popVertexData(null, 0, DATA_SIZE);
				return;
			}
//			TextureShader shader = (TextureShader) context.shader;
			TextureShader shader = (TextureShader) shaderProgram;
			if (context.shader == shader) {
				shader = (TextureShader) context.shader;

				if (shader == null || !shader.bind()) {
					VertexBufferBlock.popVertexData(null, 0, DATA_SIZE);
					return;
				}
				shader.setAlpha(context.alpha);
				shader.setMaskColor(context.color);
				shader.setMatrix(context.matrix, 0);
			}

			VertexBufferBlock.rewindReadingBuffer(DATA_SIZE);
			FloatBuffer positionBuffer = VertexBufferBlock.popVertexData(POSITION_FLOAT_ELEMENTS);
			shader.setPosition(positionBuffer, POSITION_COMPONENT);
			
			FloatBuffer texCoordBuffer = VertexBufferBlock.popVertexData(TEXCOORD_FLOAT_ELEMENTS);
			shader.setTexCoord(texCoordBuffer, TEXCOORD_COMPONENT);

			VertexBufferBlock.popVertexData(mWrapModeBuffer, 0, mWrapModeBuffer.length);
			GLState.setWrapMode((int) mWrapModeBuffer[0], (int) mWrapModeBuffer[1]);
			GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
			
		}
	};
}
