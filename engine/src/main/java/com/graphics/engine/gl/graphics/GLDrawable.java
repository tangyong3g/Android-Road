package com.graphics.engine.gl.graphics;


import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import com.graphics.engine.gl.util.NdkUtil;

/**
 * 
 * <br>类描述: 可以使用OpenGL来绘制的对象，继承自SDK的{@link Drawable}，但变为抽象类。
 * <br>功能详细描述:
 * <ul> 一些常用的方法：
 * 	<li>{@link #getDrawable(Resources, int)} 静态方法，从资源中通过id去获取对象
 * 	<li>{@link #getIntrinsicWidth()}, {@link #getIntrinsicHeight()} 获取对象本身的大小
 * 	<li>{@link #setBounds(Rect)}, {@link #setBounds(int, int, int, int)} 设置对象绘制的范围为一个矩形区域
 * 	<li>{@link #setBounds3D(float[], int, int, int, boolean, boolean)} 设置对象绘制的范围为一个3D的平行四边形
 * 	<li>{@link #draw(GLCanvas)}，{@link #drawWithoutEffect(GLCanvas)} 绘制对象
 * 	<li>{@link #setAlpha(int)} 设置对象的不透明度
 * 	<li>{@link #setColorFilter(int, android.graphics.PorterDuff.Mode)} 设置对象的混合颜色
 * 	<li>{@link #clear()} 清除对象占用的资源
 * </ul>
 */
public abstract class GLDrawable extends Drawable implements TextureListener {
	protected final static float ONE_OVER_255 = 1 / 255.0f; // CHECKSTYLE IGNORE

	protected final static int VERTEX_COUNT = 4;
	protected final static int POSITION_COMPONENT = 3;

	protected int mIntrinsicWidth = -1;
	protected int mIntrinsicHeight = -1;

	private boolean mRect2DMode = true;
	private boolean mBoundsChanged;

    protected int mReferenceCount = 1;
    protected int mOpaque = PixelFormat.UNKNOWN;
    
    protected final static int POSITION_FLOAT_ELEMENTS = POSITION_COMPONENT * VERTEX_COUNT;
    protected final static float[] TEMP_BUFFER = new float[POSITION_FLOAT_ELEMENTS];
    
    float[] mVertexBuffer;
	
	/**
	 * <br>功能简述: 从资源中获取 GLDrawable 对象
	 * <br>功能详细描述: 从资源中获取 Drawable 对象再转化为 GLDrawable 对象
	 * <br>注意: 
	 * <br>只支持 BitmapDrawable 和 NinePatchDrawable
	 * @param res
	 * @param id	资源 id
	 * @return
	 * @throws NotFoundException
	 */
	public static GLDrawable getDrawable(Resources res, int id) throws NotFoundException {
		Drawable drawable = null;
		if (NdkUtil.SAVE_RESOURCE_TO_NDK) {
			drawable = ResourceManager.getDrawable(res, id);
		} else {
			drawable = res.getDrawable(id);
			drawable = getDrawable(drawable);
		}
		if (drawable instanceof GLDrawable) {
			return (GLDrawable) drawable;
		}

		throw new IllegalArgumentException("This resource (id=" + id
				+ "） cannot be convert to GLDrawable and drawable = "
				+ drawable + ".");
	}

	/**
	 * <br>功能简述: 将 Drawable 对象再转化为 GLDrawable 对象
	 * <br>功能详细描述:
	 * <br>注意: 
	 * <br>只支持 BitmapDrawable 和 NinePatchDrawable
	 * @param drawable
	 * @return
	 */
	public static GLDrawable getDrawable(Drawable drawable) {
		if (drawable == null) {
			throw new IllegalArgumentException("drawable == null");
		}
		if (drawable instanceof BitmapDrawable) {
			return new BitmapGLDrawable((BitmapDrawable) drawable);
		}
		if (drawable instanceof NinePatchDrawable) {
			return new NinePatchGLDrawable((NinePatchDrawable) drawable);
		}
		if (drawable instanceof GLDrawable) {
			return (GLDrawable) drawable;
		}
		
		throw new IllegalArgumentException("This drawable cannot be convert to GLDrawable.");
	}
	
	@Override
	public int getIntrinsicWidth() {
		return mIntrinsicWidth;
	}

	@Override
	public int getIntrinsicHeight() {
		return mIntrinsicHeight;
	}
	
	/**
	 * <br>功能简述: 从一个drawable对象中复制本质尺寸
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param drawable
	 * @return
	 */
	protected boolean setIntrinsicSize(Drawable drawable) {
		if (drawable == null) {
			return false;
		}
		mIntrinsicWidth = drawable.getIntrinsicWidth();
		mIntrinsicHeight = drawable.getIntrinsicHeight();
		if (mIntrinsicWidth <= 0 || mIntrinsicHeight <= 0) {
			Bitmap bitmap = null;

			if (drawable instanceof BitmapDrawable) {
				bitmap = ((BitmapDrawable) drawable).getBitmap();
			} else if (drawable instanceof BitmapGLDrawable) {
				bitmap = ((BitmapGLDrawable) drawable).getBitmap();
			} else if (drawable instanceof NinePatchGLDrawable) {
				bitmap = ((NinePatchGLDrawable) drawable).getBitmap();
			} else {
				throw new RuntimeException("unsuported drawable type: " + drawable);
			}

			if (bitmap != null) {
				mIntrinsicWidth = bitmap.getWidth();
				mIntrinsicHeight = bitmap.getHeight();
			} else {
				return false;
			}
			mIntrinsicWidth = Math.max(mIntrinsicWidth, 1);
			mIntrinsicHeight = Math.max(mIntrinsicHeight, 1);
		}
		return true;
	}

	@Override
	public void draw(Canvas canvas) {
	}

	/**
	 * 设置对象的不透明度
	 * @param alpha	0表示完全透明，255表示完全不透明
	 */
	@Override
	public void setAlpha(int alpha) {
	}

	/**
	 * @Deprecated 使用{@link #setColorFilter(int, android.graphics.PorterDuff.Mode)} 代替
	 */
	@Override
	public final void setColorFilter(ColorFilter cf) {
		if (cf == null) {
			setColorFilter(0, null);
			return;
		}
		throw new UnsupportedOperationException(
				"use setColorFilter(int srcColor, PorterDuff.Mode mode) instead");
	}

	@Override
	public int getOpacity() {
		return mOpaque;
	}

	@Override
	public void setColorFilter(int srcColor, PorterDuff.Mode mode) {

	}

	@Override
	public void setBounds(int left, int top, int right, int bottom) {
		super.setBounds(left, top, right, bottom);
		if (mRect2DMode && !mBoundsChanged) {
			return;
		}
		mRect2DMode = true;
		mBoundsChanged = false;
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		mBoundsChanged = true;
	}

	/**
	* 设置一个3D的矩形边框(其实可以为平行四边形)
	* @param pts			存放顶点位置的浮点数组，如果为null，将恢复为2D的边框
	* @param offsetLT		左上角在 <var>pts</var> 的位置
	* @param offsetLB		左下角在 <var>pts</var> 的位置
	* @param offsetRT		右上角在 <var>pts</var> 的位置
	* @param extPaddingX	横向的padding区域置于边框外，只对 NinePatch 有效
	* @param extPaddingY	纵向的padding区域置于边框外，只对 NinePatch 有效
	*/
	public void setBounds3D(float[] pts, int offsetLT, int offsetLB, int offsetRT,
			boolean extPaddingX, boolean extPaddingY) {
		if (pts == null) {
			mRect2DMode = true;
			mBoundsChanged = true;
			return;
		}
		mRect2DMode = false;

		if (mVertexBuffer == null) {
			mVertexBuffer = new float[POSITION_FLOAT_ELEMENTS];
		}
		
		final float[] buf = mVertexBuffer;
		int i = 0;

		buf[i++] = pts[offsetLT];
		buf[i++] = pts[offsetLT + 1];
		buf[i++] = pts[offsetLT + 2];

		buf[i++] = pts[offsetLB];
		buf[i++] = pts[offsetLB + 1];
		buf[i++] = pts[offsetLB + 2];

		buf[i++] = pts[offsetRT];
		buf[i++] = pts[offsetRT + 1];
		buf[i++] = pts[offsetRT + 2];

		buf[i++] = pts[offsetLB] + pts[offsetRT] - pts[offsetLT];
		buf[i++] = pts[offsetLB + 1] + pts[offsetRT + 1] - pts[offsetLT + 1];
		buf[i++] = pts[offsetLB + 2] + pts[offsetRT + 2] - pts[offsetLT + 2];
	}
	
	/**
	 * <br>功能简述: 将边界顶点传入默认的数据缓冲区中
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	protected void pushBoundsVertex() {
		if (!mRect2DMode) {
			VertexBufferBlock.pushVertexData(mVertexBuffer, 0, mVertexBuffer.length);
			return;
		}
		
		final Rect rect = getBounds();
		final float[] buf = TEMP_BUFFER;
		int i = 0;
		buf[i++] = rect.left;
		buf[i++] = -rect.top;
		buf[i++] = 0;
		
		buf[i++] = rect.left;
		buf[i++] = -rect.bottom;
		buf[i++] = 0;
		
		buf[i++] = rect.right;
		buf[i++] = -rect.top;
		buf[i++] = 0;
		
		buf[i++] = rect.right;
		buf[i++] = -rect.bottom;
		buf[i++] = 0;
		
		VertexBufferBlock.pushVertexData(buf, 0, i);
	}

	/**
	 * <br>功能简述: 使用OpenGL绘制对象
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param canvas
	 */
	public void draw(GLCanvas canvas) {

	}
	
	/**
	 * <br>功能简述: 绘制时忽略掉 {@link #setAlpha(int)}, {@link #setColorFilter(int, PorterDuff.Mode)},
	 * {@link #setShaderWrapper(GLShaderWrapper)} 以及{@link GLCanvas#setAlpha(int)} 的影响
	 * <br>功能详细描述: 例如在图像处理时，先要将图片复制到帧缓冲区上，需要忽略这些影响，而不用修改状态
	 * <br>注意: 
	 * <br>仍然要注意{@link #setBounds(Rect)} 的影响
	 * @param canvas
	 */
	public void drawWithoutEffect(GLCanvas canvas) {
		
	}

	/**
	 * <br>功能简述: 清除对象占用的资源（可能有延迟）
	 * <br>功能详细描述: 无论是局部生成还是成员变量，在不再需要使用对象时，就要调用本方法清除对象。
	 * <br>注意:
	 * <br>对象有多个引用计数时，只会减少一个计数，直到计数为0时，才会真正清除。
	 * @see {@link #duplicate()}
	 */
	public void clear() {

	}
	
	/**
	 * <br>功能简述: 暂时释放显存资源（有延迟），和{@link #clear()}不同的是不会释放内存
	 * <br>功能详细描述:
	 * <br>注意: 
	 * <br>如果对象有多个引用计数，则本方法不会起作用
	 * @see {@link #duplicate()}
	 */
	public void yield() {
		
	}
    
	/**
	 * <br>功能简述: 增加一个引用计数
	 * <br>功能详细描述: 
	 * 使用引用计数的方式，可以实现多个模块共享使用同一个GLDrawable
	 * <br>注意: 
	 * <br>继承类要真正支持引用计数的话，需要在{@link #clear()}的时候对{@link #mReferenceCount}作判断：
	 * <pre>
	 * 	if (mReferenceCount <= 0 || --mReferenceCount > 0) {
	 * 		return;
	 * 	}
	 * </pre>
	 */
	public void duplicate() {
		++mReferenceCount;
	}

	@Override
	public void onTextureInvalidate() {

	}

	/**
	 * 注册监听纹理失效的事件。默认在创建对象时就会注册。注意要在不需要时反注册。
	 * @see {@link #unregister()}
	 */
	public void register() {
		TextureManager.getInstance().registerTextureListener(this);
	}

	/**
	 * 反注册监听纹理失效的事件，避免内存泄露。默认在清除对象时就会反注册。
	 * @see {@link #clear()}
	 */
	public void unregister() {
		TextureManager.getInstance().unRegisterTextureListener(this);
	}
	
	/**
	 * <br>功能简述: 获取封装的位图
	 * <br>功能详细描述:
	 * <br>注意: 
	 * @return
	 */
	public Bitmap getBitmap() {
		return null;
	}
	
	/**
	 * <br>功能简述: 封装的位图是否已被回收
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isBitmapRecycled() {
		return false;
	}
	
	/**
	 * <br>功能简述: 设置Shader封装器，用于扩展绘制行为。
	 * <br>功能详细描述:
	 * 默认的绘制行为包括：普通，透明度，混合颜色等等，使用Shader封装器可以实现改变色调等效果。
	 * <br>注意:
	 * @param shader
	 */
	public void setShaderWrapper(GLShaderWrapper shader) {
	}
	
	/**
	 * <br>功能简述: 获取设置的Shader封装器
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public GLShaderWrapper getShaderWrapper() {
		return null;
	}
	
	/**
	 * <br>功能简述: 设置纹理
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param texture
	 */
	public void setTexture(Texture texture) {
		
	}
	
	/**
	 * <br>功能简述: 获取纹理
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public Texture getTexture() {
		return null;
	}
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param info
	 */
	public void setDrawableInfo(DrawableInfo info) {

	}
}
