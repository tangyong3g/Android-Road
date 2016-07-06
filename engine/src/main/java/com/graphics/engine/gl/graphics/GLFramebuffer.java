package com.graphics.engine.gl.graphics;


import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.util.Log;

import com.graphics.engine.gl.util.NdkUtil;
import com.graphics.engine.gl.view.GLContentView;
import com.graphics.engine.gl.view.GLView;

/**
 * <br>类描述: OpenGL帧缓冲区(Frame Buffer)的封装类
 * <br>功能详细描述:
 * 可以用来保存截图，或者画布的临时缓冲。
 * 
 * @author  dengweiming
 * @date  [2013-10-16]
 */
public class GLFramebuffer implements TextureListener, GLClearable {
	private static final boolean DBG = false;
//	static boolean DBG = false;
	private static final String TAG = "DWM";
	
	private int mFrameBuffer;
	private int mDepthBuffer;
	private int mStencilBuffer;
	private BufferTexture mTexture;
	
	private int mState;
	private boolean mInvalidated = true;	//是否失效，不用mState是因为它在GL线程有写操作
	private boolean mTranslucent;
	private int mDepthSize;
	private int mStencilSize;
	private boolean mPOT;	//pow of two
	
	private static final int STATE_CLEARED = -1;
	private static final int STATE_NONE = 0;
	private static final int STATE_TEXTURE_CREATED = 1;
	private static final int STATE_FRAME_BUFFER_CREATED = 2;
	
	private BitmapGLDrawable mDrawable;
	private boolean mSizeChanged;
	private int mWidth;
	private int mHeight;
	private int mPaddedWidth;
	private int mPaddedHeight;

	private final Object mLock = new Object();
	private final int[] mViewportBak = new int[GLCanvas.ViewportArgc];
	private int[] mCustomViewport;
	private float mViewportScale;
	private int mCaptureLeft;
	private int mCaptureTop;
	private int mCaptureWidth;
	private int mCaptureHeight;
	private boolean mAllowScaleUp;
	private boolean mIsBinding; 
	
	private static final int DEPTH_BIT_UNIT = 16;
	private static final int DEPTH_BIT_MAX = 32;
	private static boolean sDepth32Supported = true;
	
	private float[] mClearColor;
	
	private static final int[] STACK = new int[64];	//CHECKSTYLE IGNORE
	private static int sStackPtr;
	static int sBindDepth;
	private static final int[] VIEW_PORT_NO_FBO = new int[4];
	
	/**
	 * 
	 * @param width			宽度
	 * @param height		高度
	 * @param translucent	是否半透明，即支持alpha通道
	 * @param depthSize		如果绘制的内容使用了深度缓冲区，需要设为16或者32
	 * @param stencilSize	蒙板缓冲区的位数目，暂时忽略
	 * @param pot			填充成2的幂大小，如果要生成mipmap就需要
	 */
	public GLFramebuffer(int width, int height, boolean translucent, int depthSize, int stencilSize, boolean pot) {
		if (DBG) {
			Log.d(TAG, "GLFramebuffer new: w=" + width + " h=" + height + " translucent="
					+ translucent + " depth=" + depthSize + " stencil=" + stencilSize);
		}
		mWidth = Math.max(1, width);
		mHeight = Math.max(1, height);
		if (pot) {
			mPaddedWidth = Shared.nextPowerOf2(mWidth);
			mPaddedHeight = Shared.nextPowerOf2(mHeight);
			mPOT = true;
		} else {
			mPaddedWidth = mWidth;
			mPaddedHeight = mHeight;
			mPOT = Shared.isPowerOf2(width) && Shared.isPowerOf2(mPaddedHeight);
		}
		
		mTranslucent = translucent;
		mDepthSize = (Math.max(0, Math.min(depthSize, DEPTH_BIT_MAX)) + DEPTH_BIT_UNIT - 1)
				/ DEPTH_BIT_UNIT;
		mStencilSize = stencilSize;
		mTexture = new BufferTexture();
		mTexture.setSize(mWidth, mHeight, mPaddedWidth, mPaddedHeight);
		
		setCaptureRectSize(mWidth, mHeight, false);
	}
	
	private boolean updateBuffer() {
		if (mState == STATE_CLEARED) {
			return false;
		}
		if (mState >= STATE_FRAME_BUFFER_CREATED) {
			return true;
		}
		if (DBG) {
			Log.d(TAG, this + " GLFrameBuffer: glGenTetures ");
		}
		GLError.clearGLError();
		
		int width = 0;
		int height = 0;
		synchronized (mLock) {
			width = mPaddedWidth;
			height = mPaddedHeight;
		}
		if (mTexture == null || !mTexture.generateVoidTexture(mTranslucent, width, height)) {
			return false;
		}
		mState = STATE_TEXTURE_CREATED;
		
		boolean res = createFrameBuffer(width, height, mTexture.mId);
		if (DBG) {
			Log.d(TAG, this + " GLFrameBuffer: updateBuffer result=" + res);
		}
		if (!res) {
			clear();
			return false;
		}
		mState = STATE_FRAME_BUFFER_CREATED;
		return res;
	}
	
	/**
	 * <br>功能简述: 获取宽度限制
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public int getWidthLimit() {
		return mPaddedWidth;
	}
	
	/**
	 * <br>功能简述: 获取高度限制
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public int getHeightLimit() {
		return mPaddedHeight;
	}
	
	/**
	 * 
	 * @param width		应该为2的幂
	 * @param height	应该为2的幂
	 * @param targetTextureId
	 * @return
	 */
    private boolean createFrameBuffer(int width, int height, int targetTextureId) {
    	mFrameBuffer = 0;
    	mDepthBuffer = 0;
    	mStencilBuffer = 0;
    	
    	int[] buffer = new int[1];
		if (DBG) {
			Log.d(TAG, this + " GLFrameBuffer: glGenBuffers w=" + width + " h=" + height);
		}
		
        GLES20.glGenBuffers(1, buffer, 0);
        final int framebuffer = buffer[0];
		if (framebuffer == 0) {
        	return false;
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer);
        mFrameBuffer = framebuffer;

        ++TextureManager.sCountFBO;
        
        if (DBG) {
        	Log.d(TAG, this + " GLFrameBuffer: glFramebufferTexture2D target=" + targetTextureId);
        }
        
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
        		GLES20.GL_TEXTURE_2D, targetTextureId, 0);
        
		if (mDepthSize > 0) {
			if (DBG) {
				Log.d(TAG, this + " GLFrameBuffer: glGenRenderbuffers for depth ");
			}
	        GLES20.glGenRenderbuffers(1, buffer, 0);
	        final int depthbuffer = buffer[0];
	        
			if (depthbuffer != 0) {
				++TextureManager.sCountRenderBuffer;
				
				int depthFormat = mDepthSize > DEPTH_BIT_UNIT && sDepth32Supported
						? GLES20.GL_DEPTH_COMPONENT
						: GLES20.GL_DEPTH_COMPONENT16;
				GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthbuffer);
				GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, depthFormat, width, height);
				if (GLError.checkGLError(GLES20.GL_INVALID_ENUM)) {
					sDepth32Supported = false;
					mDepthSize = DEPTH_BIT_UNIT;
					depthFormat = GLES20.GL_DEPTH_COMPONENT16;
					GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, depthFormat, width, height); //retry once
				}
				GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
						GLES20.GL_RENDERBUFFER, depthbuffer);

				mDepthBuffer = depthbuffer;
			} else {
				return false;
			}
        }
        
		//暂时未使用蒙板缓冲区
//		if (mStencilSize > 0) {
//			GLError.clearGLError();
//			GLES20.glGenRenderbuffers(1, buffer, 0);
//			final int stencilBuffer = buffer[0];
//			if (stencilBuffer != 0) {
//				++TextureManager.sCountRenderBuffer;
//				GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, stencilBuffer);
//				GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_STENCIL_INDEX8, width, height);
//				GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_STENCIL_ATTACHMENT, 
//						GLES20.GL_RENDERBUFFER, stencilBuffer);
//				mStencilBuffer = stencilBuffer;
//			} else {
//				return false;
//			}
//		}

		GLError.checkGLError("createFrameBuffer");
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
    		if (DBG) {
    			Log.w(TAG, this + " GLFrameBuffer error: glCheckFramebufferStatus status=" + status);
    		}
        	return false;
        }
        return true;
    }
    
    /**
     * <br>功能简述:设置截图区域大小
     * <br>功能详细描述:
     * <br>注意:
     * @param width
     * @param height
     * @param allowScaleUp	如果为了生成更高分辨率的截图（可以用于多重采样抗锯齿），可以设为true
     */
    public void setCaptureRectSize(int width, int height, boolean allowScaleUp) {
		if (mCaptureWidth == width && mCaptureHeight == height && mAllowScaleUp == allowScaleUp) {
			return;
		}
		if (width < 1 || height < 1) {
			throw new IllegalArgumentException("width and height should be positive.");
		}
		float scale = Math.min(mPaddedWidth / (float) width, mPaddedHeight / (float) height);
		if (!allowScaleUp) {
			scale = Math.min(1, scale);
		}
		mCaptureWidth = width;
		mCaptureHeight = height;
		mAllowScaleUp = allowScaleUp;
		width = Math.round(width * scale);
		height = Math.round(height * scale);
		
		mViewportScale = scale;
    	mSizeChanged = mWidth != width || mHeight != height;
		if (mSizeChanged) {
			synchronized (mLock) {
				mWidth = width;
				mHeight = height;
			}
			if (mTexture != null) {
				mTexture.setSize(mWidth, mHeight, mPaddedWidth, mPaddedHeight);
			}
			if (mDrawable != null) {
				getDrawable();	//更新它的状态
			}
		}
    }
    

    /**
     * <br>功能简述:设置截图区域在窗口中的位置
     * <br>功能详细描述:位置会影响投影效果
     * <br>注意:
     * @param leftInViewport
     * @param topInViewPort
     */
    public void setCaptureRectPosition(int leftInViewport, int topInViewPort) {
    	mCaptureLeft = leftInViewport;
    	mCaptureTop = topInViewPort;
    }
    
    /**
     * <br>功能简述: 设置自定义的视口
     * <br>功能详细描述:
     * <br>在{@link #bind(GLCanvas)}的时候，默认使用bind深度为0前的视口，和自己的截图区域计算得临时视口，
     * <br>见{@link #saveViewport(GLCanvas)}，这样嵌套bind的时候不会出问题。
     * <br>但是在这种情况下会有问题：
     * <br>在使用了绘图缓冲的视图中，已经bind了，视口已经记录好了（一般为窗口大小）。这时再有需要设置新的视口，
     * <br>（例如使用模糊效果等图像处理方法，在调用{@link GLCanvas#setOtho(int, int)}时会设置新的视口
     * <br>为图片的大小），这时图片缓冲的视口应该使用新的视口，这个新的视口通过本方法设置。
     * <br>注意:
     * @param x
     * @param y
     * @param w	如果为0,将禁止使用自定义视口
     * @param h
     */
	public void setCustomViewport(int x, int y, int w, int h) {
		if (mCustomViewport == null) {
			mCustomViewport = new int[GLCanvas.ViewportArgc];
		}
		mCustomViewport[GLCanvas.ViewportX] = x;
		mCustomViewport[GLCanvas.ViewportY] = y;
		mCustomViewport[GLCanvas.ViewportW] = w;
		mCustomViewport[GLCanvas.ViewportH] = h;
	}
    
    /**
     * <br>功能简述: 获取drawable对象以便把纹理绘制出来
     * <br>功能详细描述:
     * <br>注意:
     * @return
     */
    public BitmapGLDrawable getDrawable() {
		if (mDrawable == null) {
			mDrawable = new BitmapGLDrawable();
			mDrawable.unregister();
			mDrawable.setTexture(mTexture);
			mSizeChanged = true;
		}
    	if (mSizeChanged) {
    		mSizeChanged = false;
    		mDrawable.setIntrinsicSize(mCaptureWidth, mCaptureHeight);
    		mDrawable.setBounds(0, 0, mCaptureWidth, mCaptureHeight);
//			if (mTexture != null) {
//				mDrawable.setTexCoord(mTexture.mU0, mTexture.mV0, mTexture.mU1, mTexture.mV1);
//			}
			mDrawable.setTexCoord(0, mHeight / (float) mPaddedHeight, mWidth / (float) mPaddedWidth, 0);
    	}
    	return mDrawable;
    }
    
	private class BufferTexture extends Texture {		// CHECKSTYLE IGNORE
		
    	void setSize(int width, int height, int paddedWidth, int paddedHeight) {
			mWidth = width;
			mHeight = height;
			mPaddedWidth = paddedWidth;
			mPaddedHeight = paddedHeight;
			mNormalizedWidth = mWidth / (float) mPaddedWidth;
			mNormalizedHeight = mHeight / (float) mPaddedHeight;
			mU0 = 0;
			mU1 = mNormalizedWidth;
			
			mV0 = mNormalizedHeight;	//和位图纹理的V方向相反
			mV1 = 0;
    	}

		@Override
		protected Bitmap onLoad() {
			return null;
		}
		
		@Override
		public void onTextureInvalidate() {
			mId = 0;
			mState = STATE_UNLOADED;
		}
		
		@Override
		public boolean bind() {
			final int state = mState;
	    	final int id = mId;
	    	//Log.d(TAG, "glframebuffer texture bind: state=" + state + " id=" + id + " this=" + GLFramebuffer.this);
	        switch (state) {
	            case Texture.STATE_UNLOADED:
	            	return false;
	            case Texture.STATE_LOADED:
	                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
	                break;
	            default:
	            	return false;
	        }
	        return true;
		}
    }

	@Override
	public void onTextureInvalidate() {
		if (DBG) {
			Log.d(TAG, "GLFrameBuffer: onTextureInvalidate: " + this);
		}
    	mState = STATE_NONE;
    	mInvalidated = true;
		if (mTexture != null) {
			mTexture.onTextureInvalidate();
		}
    	mFrameBuffer = 0;
    	mDepthBuffer = 0;
    	mStencilBuffer = 0;
		
	}
	
	/**
	 * <br>功能简述: 是否失效了
	 * <br>功能详细描述: 如果用来作绘图缓冲，那么在失效的时候需要刷新绘图缓冲
	 * <br>注意: 需要调用{{@link #register()}让本对象能监听到失效事件
	 * @return
	 */
	public boolean isInvalidated() {
		return mInvalidated;
	}
	
	@Override
	public void clear() {
		if (DBG) {
			Log.d(TAG, "GLFrameBuffer clear: " + this);
		}
		if (mDrawable != null) {
//			mDrawable.setTexture(null);
			mDrawable.clear();
			mDrawable = null;
		}
		TextureRecycler.recycleTextureDeferred(this);
	}
	
	@Override
	public void onClear() {
		if (mTexture != null) {
			mTexture.clear();
			mTexture = null;
		}
		unregister();

		final TextureManager manager = TextureManager.getInstance();
		manager.deleteFrameBuffer(mFrameBuffer);
		manager.deleteRenderBuffer(mDepthBuffer);
		manager.deleteRenderBuffer(mStencilBuffer);
		
		onTextureInvalidate();
		mState = STATE_CLEARED;
	}
	
	@Override
	public void yield() {
		TextureRecycler.yieldTextureDeferred(this);
	}
	
	@Override
	public void onYield() {
		if (mTexture != null) {
			mTexture.onYield();
		}
		final TextureManager manager = TextureManager.getInstance();
		manager.deleteFrameBuffer(mFrameBuffer);
		manager.deleteRenderBuffer(mDepthBuffer);
		manager.deleteRenderBuffer(mStencilBuffer);
		onTextureInvalidate();
	}
	
	/**
	 * <br>功能简述: 注册监听纹理失效事件
	 * <br>功能详细描述:
	 * <br>注意: 默认没有注册。在不再需要监听的时候要调用{@link #unregister()}反注册。
	 */
	public void register() {
		TextureManager.getInstance().registerTextureListener(this);
	}
	
	/**
	 * <br>功能简述: 反注册监听纹理失效事件
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void unregister() {
		TextureManager.getInstance().unRegisterTextureListener(this);
	}
	
	/**
	 * <br>功能简述: 是否已经绑定
	 * <br>功能详细描述:
	 * <br>注意: 如果重复绑定，会抛出异常
	 * @return
	 */
	public boolean isBinding() {
		return mIsBinding;
	}
	
	/**
	 * <br>功能简述: 绑定到画布上，让画布绘制内容到自己的缓冲区里。
	 * <br>功能详细描述:
	 * <br>注意: 不同的对象可以嵌套绑定，即 a.bind, b.bind, b.unbind, a.bind 这样的顺序调用（没有检测非法的顺序）。
	 * @param canvas
	 * @see {@link #unbind(GLCanvas)} 解除绑定
	 */
	public void bind(GLCanvas canvas) {
		if (mIsBinding) {
			throw new RuntimeException("This GLFrameBuffer is already binding! (w" + mWidth + ", h=" + mHeight + ")");
		}
		if (sBindDepth == 0) {
			canvas.getViewport(VIEW_PORT_NO_FBO);
		}
		++sBindDepth;
		mIsBinding = true;
		mInvalidated = false;
		saveViewport(canvas);
		canvas.addRenderable(mBindRenderable, null);
	}
    
	/** GL线程上的绑定操作 */
    private boolean bind() {
		if (!updateBuffer()) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			if (DBG) {
				Log.d(TAG, "GLFrameBuffer bind failed: this=" + this);
			}
    		return false;
    	}

		if (sStackPtr >= STACK.length - 1) {
			throw new RuntimeException("GLFrameBuffer stack overflow (>" + STACK.length + ")");
		}
		STACK[++sStackPtr] = mFrameBuffer;
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer);
		if (DBG) {
			Log.d(TAG, "GLFrameBuffer.bind " + mFrameBuffer);
		}
        
		if (mClearColor != null) {
			//backup glClearColor
			final float r = GLState.sClearRed;
			final float g = GLState.sClearGreen;
			final float b = GLState.sClearBlue;
			final float a = GLState.sClearAlpha;

			GLState.glClearColor(mClearColor[0], mClearColor[1], mClearColor[2], mClearColor[3]);	//CHECKSTYLE IGNORE
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

			//restore glClearColor
			GLState.glClearColor(r, g, b, a);
		}
		
		GLCommandFactory.sFrameBufferDepthBufferDirty = true;
		
        return true;
    }
    
    /**
     * <br>功能简述: 解除绑定
     * <br>功能详细描述:
     * <br>注意: 要和 {@link #bind(GLCanvas)} 配对使用
     * @param canvas
     */
    public void unbind(GLCanvas canvas) {
		--sBindDepth;
    	mIsBinding = false;
    	restoreViewport(canvas);
		canvas.addRenderable(sUnbindRenderable, null);
    }
    
    /** @hide */
    static void resetStackOnRenderStart() {
    	sStackPtr = 0;
    	STACK[0] = 0;
    	GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }
    
    /** GL线程上的解除绑定操作 */
    private static void unbind() {
    	int id = sStackPtr > 0 ? STACK[--sStackPtr] : 0;
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, id);
		if (DBG) {
			Log.d(TAG, "GLFrameBuffer.unbind " + id);
		}
    }
    
    /** 保存视口 */
    private void saveViewport(GLCanvas canvas) {
    	float s = mViewportScale;
    	
		if (s == 0) {
			throw new RuntimeException("Frame buffer didn't call setCaptureRectInViewport " + this);
		}
		
		canvas.getViewport(mViewportBak);
		int[] viewport = mCustomViewport != null && mCustomViewport[GLCanvas.ViewportW] > 0 
				? mCustomViewport : VIEW_PORT_NO_FBO;
		int x0 = viewport[GLCanvas.ViewportX];
		int y0 = viewport[GLCanvas.ViewportY];
		int w0 = viewport[GLCanvas.ViewportW];
		int h0 = viewport[GLCanvas.ViewportH];
		float fx = (x0 - mCaptureLeft) * s; 
		float fy = (y0 - (h0 - (mCaptureTop + mCaptureHeight))) * s;	//视口的原点在左下角
		float fw = w0 * s;
		float fh = h0 * s;
    	
		int x = Math.round(fx);
		int y = Math.round(fy);
		int w = Math.round(fw);
		int h = Math.round(fh);
    	canvas.setViewport(x, y, w, h);
    	canvas.setClipRectMapToViewport(true);
    }
    
    /** 恢复视口 */
    private void restoreViewport(GLCanvas canvas) {
    	canvas.setViewport(mViewportBak);
    	canvas.setClipRectMapToViewport(false);
    }
    
    /**
     * <br>功能简述: 设置调用{@link #bind(GLCanvas)}后自动清除帧缓冲区的颜色
     * <br>功能详细描述:
     * <br>注意: 如果没调用过本方法，那么不会自动清除帧缓冲区。另外本方法没有做线程同步，所以仅在初始化时调用才是安全的，否则需要将操作post到GL线程。
     * @param color
     */
	public void setClearColorOnBind(int color) {
		if (mClearColor == null) {
			mClearColor = new float[4];
		}
		GLCanvas.convertColorToPremultipliedFormat(color, mClearColor, 0);
	}
	
	/**
	 * <br>功能简述: 保存内容到Bitmap中
	 * <br>功能详细描述:
	 * <br>注意: 截图是一个异步操作，期间如果程序被pause了，那么截图会中断，并且会没有回调。
	 * 可以在resume的时候判断有没有回调过以便重新截图。
	 * 另外本方法会影响帧率的，不宜频繁调用
	 * @param canvas
	 * @param listener 截图完成的监听者
	 * @param captureRect 截图区域，如果为null则表示截取全部有效区域
	 */
	public void saveToBitmap(GLCanvas canvas, GLView.OnBitmapCapturedListener listener, Rect captureRect) {
		final boolean isBinding = mIsBinding;
		if (!isBinding) {
			bind(canvas);
		}
		
		RenderContext context = RenderContext.acquire();
		float[] rect = context.color;
		if (captureRect == null) {
			rect[0] = 0;
			rect[1] = 0;
			rect[2] = mCaptureWidth;
			rect[3] = mCaptureHeight;
		} else {
			captureRect.intersect(0, 0, mCaptureWidth, mCaptureWidth);
			rect[0] = captureRect.left;
			//截出的图，padding的空白区域（如果要求了pot）在上方，而y从底部往上，即y应为截图区域之下的高度
			rect[1] = mCaptureHeight - captureRect.bottom;
			rect[2] = captureRect.width();
			rect[3] = captureRect.height();
		}
		//TODO: 处理mViewportScale不为1的情况。下面是把整个视口截取出来：
//		int[] viewport = new int[4];
//		canvas.getViewport(viewport);
//		rect[0] = viewport[0];
//		rect[1] = viewport[1];
//		rect[2] = viewport[2];
//		rect[3] = viewport[3];
		BitmapCapturer capturer = new BitmapCapturer();
		capturer.mListener = listener;
		canvas.addRenderable(capturer, context);
		
		if (!isBinding) {
			unbind(canvas);
		}
	}
	
	/**
	 * <br>功能简述: 获取当前绑定的帧缓冲区id
	 * <br>功能详细描述:
	 * <br>注意:要在GL线程调用
	 * @return
	 */
	static int getCurrentFrameBufferId() {
		return STACK[sStackPtr];
	}
	
    //CHECKSTYLE IGNORE
	private final Renderable mBindRenderable = new Renderable() {
		
		@Override
		public void run(long timeStamp, RenderContext context) {
			if (!bind()) {
				//TODO: 禁止后面的绘制
			}
		}
	};
	
    //CHECKSTYLE IGNORE 1 LINES
	private final static Renderable sUnbindRenderable = new Renderable() {
		
		@Override
		public void run(long timeStamp, RenderContext context) {
			unbind();
		}
	};
	

	/**
	 * 
	 * <br>类描述: 将缓冲的内容保存到位图的操作封装对象
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2013-6-20]
	 */
	private static class BitmapCapturer implements Renderable {
		GLView.OnBitmapCapturedListener mListener;
		Bitmap mBitmap;

		@Override
		public void run(long timeStamp, RenderContext context) {
			float[] rect = context.color;
			int x = (int) rect[0];
			int y = (int) rect[1];
			int w = Math.max(1, (int) rect[2]);
			int h = Math.max(1, (int) rect[3]);

			mBitmap = null;
			try {
				mBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
				NdkUtil.saveScreenshotBitmap(x, y, w, h, mBitmap);
			} catch (OutOfMemoryError e) {

			}

			GLContentView.postStatic(new Runnable() {

				@Override
				public void run() {
					if (mListener != null) {
						mListener.onBitmapCaptured(mBitmap);
					}
					mListener = null;
					mBitmap = null;
				}
			});
		}

	}
}
