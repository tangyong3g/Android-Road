/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphics.engine.gl.graphics;


import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.graphics.engine.gl.util.FastQueue;
import com.graphics.engine.gl.util.NdkUtil;

/**
 * 
 * <br>类描述: OpenGL纹理(Texture)的封装类
 * <br>功能详细描述:
 * <ul> 一些常用方法：
 * 	<li>{@link #bind()} 在GL线程上绑定使用纹理。无需解除绑定。
 * 	<li>{@link #updateSubImage(Bitmap)} 部分更新纹理内容
 * 	<li>{@link #mipMapNextTexture(boolean)} 使下一张创建的纹理使用MipMap。MipMap会让纹理在缩小显示时效果较好。
 * </ul>
 * 
 * @author  luopeihuan
 * @date  [2012-9-5]
 */
public abstract class Texture implements TextureListener, GLClearable {
	private static final String TAG = "DWM";
	private static final boolean DBG = false;
//	public boolean DBG;
	
	//CHECKSTYLE IGNORE 3 LINES
	private static final Bitmap sBitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
	private static final Canvas sCanvas = new Canvas();
	private static final Paint sPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	
	/** 最大的纹理尺寸，默认2048，第一次渲染时会去获取实际值 */
	public static int sMaxTextureSize = 2048;	//CHECKSTYLE IGNORE
	/** 至少能支持的最大纹理尺寸，默认2048 */
	public static final int MAX_TEXTURE_SIZE_LOWERBOUND = 2048;
	private static boolean sMaxTextureSizeGot = false;
	
    public static final int STATE_UNLOADED = 0;
    public static final int STATE_QUEUED = 1;
    public static final int STATE_LOADING = 2;
    public static final int STATE_LOADED = 3;
    public static final int STATE_ERROR = 4;
    
    private static boolean sMipMapNextTexture;
    private static int sNextTextureMinFilter = GLES20.GL_LINEAR;
    
    public static final int WRAP_CLAMP = GLES20.GL_CLAMP_TO_EDGE;
    public static final int WRAP_REPEAT = GLES20.GL_REPEAT;
    public static final int WRAP_MIRRORED_REPEAT = GLES20.GL_MIRRORED_REPEAT;
    
    private static boolean sGLFlushed;	//本帧是否调用过glFlush()
    
    //CHECKSTYLE IGNORE 1 LINES
    private static final FastQueue<Texture> sLoadedTextureQueue = new FastQueue<Texture>(1024);
    //CHECKSTYLE IGNORE 1 LINES
    private static final FastQueue.Processor<Texture> sLoadedTextureProcessor = new FastQueue.Processor<Texture>() {
		@Override
		public void process(Texture object) {
			if (object != null) {
				if (object.mLoadedListener != null) {
					object.mLoadedListener.onTextureLoaded(object);
				}
			}
		}
	};
	
	/**
	 * 对已经上传完毕的纹理，通知其监听者
	 * @hide
	 */
	public static void processLoadedTextures() {
		sLoadedTextureQueue.process(sLoadedTextureProcessor);
	}
	
	/**
	 * 是否需要通知纹理上传完成的监听者
	 * @hide
	 */
	public static boolean needToProcessLoadedTextures() {
		return !sLoadedTextureQueue.isEmpty();
	}
    
    private static final int[] ID = new int[1];

    int mState = STATE_UNLOADED;
    int mId;
    int mWidth;
    int mHeight;
    float mU0, mV0, mU1, mV1;	//uv坐标的left, top, right, bottom
    float mNormalizedWidth;
    float mNormalizedHeight;
    int mPaddedWidth;	//填充成2的幂大小的宽度
    int mPaddedHeight;	//填充成2的幂大小的高度
    
    Bitmap mBitmap;
    Bitmap mResizedBitmap;
    DrawableInfo mDrawableInfo;
    
    boolean mToUpdate;
    Bitmap mBitmapForUpdate;
    
    TextureLoadedListener mLoadedListener;
    int mReferenceCount = 1;
    
    boolean mMipMap;
    int mMinFilter = GLES20.GL_LINEAR;
    int mWrapS = GLES20.GL_CLAMP_TO_EDGE;
    int mWrapT = GLES20.GL_CLAMP_TO_EDGE;
    
    private final Object mLock = new Object();
    
    public Texture() {
    	mMipMap = sMipMapNextTexture;
    	mMinFilter = sNextTextureMinFilter;
    	
    	sMipMapNextTexture = false;
    	sNextTextureMinFilter = GLES20.GL_LINEAR;
    }
    
    public boolean isCached() {
        return false;
    }

    /**
     * <br>功能简述: 设置纹理的回绕模式
     * <br>功能详细描述:
     * <br>注意: 没做线程保护，应在非渲染期间调用（例如初始化时）或者同步到GL线程。
     * <br>另外由于纹理可能是共享的，所以尽量使用{@link BitmapGLDrawable#setWrapMode(int, int)}，对不同的实例是独立的
     * 回绕模式，并且是线程安全的。
     * @param modeS 可用值为 {@link #WRAP_CLAMP}(默认), {@link #WRAP_REPEAT}, {@link #WRAP_MIRRORED_REPEAT}，
     * 后面这两种需要纹理尺寸大小为2的幂（除非GPU支持GL_OES_texture_npot扩展，但是最好还是统一处理）
     * @param modeT 同上
     */
    public void setWrapMode(int modeS, int modeT) {
    	mWrapS = modeS;
    	mWrapT = modeT;
    }
    
    @Override
    public void onTextureInvalidate() {
		mId = 0;
		mState = STATE_UNLOADED;
//		mWidth = 0;
//		mHeight = 0;
//		mNormalizedWidth = 0;
//		mNormalizedHeight = 0;
//		mU0 = mU1 = mV0 = mV1 = 0;
//		mPaddedWidth = 0;
//		mPaddedHeight = 0;
		recycleBitmap(mBitmap);
		mBitmap = null;
    }

    @Override
    public final void clear() {
    	//Log.d("DWM", "texture clear " + this + " count=" + mReferenceCount);
		if (mReferenceCount <= 0 || --mReferenceCount > 0) {
			return;
		}

    	TextureRecycler.recycleTextureDeferred(this);
    }
    
    @Override
    public void onClear() {
		//Log.d("DWM", "texture onClear " + this + " id=" + mId);
		if (mResizedBitmap != null) {
			recycleBitmap(mResizedBitmap);
			mResizedBitmap = null;
		}
		mBitmapForUpdate = null;
    	mLoadedListener = null;
    	unregister();

    	TextureManager.getInstance().deleteTexture(mId);
    	onTextureInvalidate();
		if (mDrawableInfo != null) {
			if (mDrawableInfo.autoReleaseByTexture) {
				mDrawableInfo.clear();
			}
			mDrawableInfo = null;
		}
    }
    
    @Override
    public void yield() {
		if (mReferenceCount > 1) {
			return;
		}
    	TextureRecycler.yieldTextureDeferred(this);
    }
    
    @Override
    public void onYield() {
    	TextureManager.getInstance().deleteTexture(mId);
    	onTextureInvalidate();
    }
    
	/**
	 * <br>功能简述: 增加一个引用计数
	 * <br>功能详细描述: 
	 * 使用引用计数的方式，可以实现多个模块共享使用同一个GLDrawable
	 * <br>注意: 
	 */
	public void duplicate() {
		//Log.d("DWM", "texture duplicate " + this + " count=" + mReferenceCount);
		++mReferenceCount;
	}

	/**
	 * 是否已被清除
	 */
	public boolean isCleared() {
		return mReferenceCount <= 0;
	}

	/**
	 * 是否已经上传完成
	 */
    public final boolean isLoaded() {
        return mState == STATE_LOADED;
    }

    /**
     * 获取纹理状态
     * @hide
     */
    public final int getState() {
        return mState;
    }

    /**
     * 获取纹理有效宽度
     */
    public final int getWidth() {
        return mWidth;
    }

    /**
     * 获取纹理有效高度
     */
    public final int getHeight() {
        return mHeight;
    }

//    public final float getNormalizedWidth() {
//        return mNormalizedWidth;
//    }
//
//    public final float getNormalizedHeight() {
//        return mNormalizedHeight;
//    }
    
    /**
     * 获取有效纹理坐标的左边界值
     */
    public final float getTexCoordLeft() {
    	return mU0;
    }
    
    /**
     * 获取有效纹理坐标的上边界值
     */
    public final float getTexCoordTop() {
    	return mV0;
    }
    
    /**
     * 获取有效纹理坐标的右边界值
     */
    public final float getTexCoordRight() {
    	return mU1;
    }
    
    /**
     * 获取有效纹理坐标的下边界值
     */
    public final float getTexCoordBottom() {
    	return mV1;
    }

    /** Returns a bitmap, or null if an error occurs. */
    protected abstract Bitmap onLoad();
    
    /**
     * 计算填充成2的幂后的大小，受{@link #sMaxTextureSize}的限制
     * @param w
     * @param h
     * @param dst	将写入[paddedW, paddedH]
     * @param square 是否需要填充成正方形，如果使用mipmap，部分机型例如LG-P970的glGenerateMipmap不支持非正方形的纹理
     */
	public static void solvePaddedSize(int w, int h, int[] dst, boolean square) {
		float scale = 1;
		if (w > h) {
			if (w > sMaxTextureSize) {
				scale = sMaxTextureSize / (float) w;
				w = sMaxTextureSize;
				h = (int) (h * scale);
			}
		} else {
			if (h > sMaxTextureSize) {
				scale = sMaxTextureSize / (float) h;
				w = (int) (w * scale);
				h = sMaxTextureSize;
			}
		}
		if (scale == 1) {
			w = Shared.nextPowerOf2(w);
			h = Shared.nextPowerOf2(h);
		}
		if (square) {
			w = h = Math.max(w, h);
		}
		dst[0] = w;
    	dst[1] = h;
    }
    
	/**
	 * <br>功能简述: 将图片尺寸适配到2的幂的大小
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bitmap
	 * @param maxSize
	 * @param forcePadding
	 * @return
	 */
    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize, boolean forcePadding) {
		if (bitmap == null || bitmap.isRecycled()) {
			return null;
		}
        final int srcWidth = bitmap.getWidth();
        final int srcHeight = bitmap.getHeight();
        mWidth = srcWidth;
        mHeight = srcHeight;
        mNormalizedWidth = 1;
        mNormalizedHeight = 1;
        mPaddedWidth = Shared.nextPowerOf2(mWidth);
        mPaddedHeight = Shared.nextPowerOf2(mHeight);
        float scale = 1;
        if (srcWidth > srcHeight) {
            if (srcWidth > maxSize) {
				scale = maxSize / (float) srcWidth;
				mWidth = maxSize;
				mHeight = (int) (srcHeight * scale);
				mPaddedWidth = maxSize;
				mPaddedHeight = Shared.nextPowerOf2(mHeight);
			}
		} else {
			if (srcHeight > maxSize) {
				scale = maxSize / (float) srcHeight;
				mWidth = (int) (srcWidth * scale);
				mHeight = maxSize;
				mPaddedHeight = maxSize;
				mPaddedWidth = Shared.nextPowerOf2(mWidth);
			}
		}
		if (scale != 1 || (forcePadding && (mWidth != mPaddedWidth || mHeight != mPaddedHeight))) {
			try {
				Config config = bitmap.getConfig();
				if (config == null) {
					config = Config.ARGB_8888;
				}
				Bitmap buffer = null;
				if (forcePadding) {
					buffer = Bitmap.createBitmap(mPaddedWidth, mPaddedHeight, config);
				} else {
					buffer = Bitmap.createBitmap(mWidth, mHeight, config);
				}
				Canvas canvas = sCanvas;
				canvas.setBitmap(buffer);
				Paint paint = null;
				if (scale < 1) {
					canvas.save();
					canvas.scale(scale, scale);
					paint = sPaint;
				}
				canvas.drawBitmap(bitmap, 0, 0, paint);
				if (scale < 1) {
					canvas.restore();
				}
				canvas.setBitmap(sBitmap);
				mNormalizedWidth = mWidth / (float) mPaddedWidth;
				mNormalizedHeight = mHeight / (float) mPaddedHeight;
				return buffer;
			} catch (OutOfMemoryError e) {
				mPaddedWidth = mWidth;
				mPaddedHeight = mHeight;
				return null;
			}
		} else {
			return bitmap;
		}
    }
    
    private void load() {
    	mResizedBitmap = null;
    	mBitmap = null;
    	Bitmap bitmap = null;
        try {
            bitmap = onLoad();
        } catch (Exception e) {
        } catch (OutOfMemoryError eMem) {
//            handleLowMemory();
            eMem.printStackTrace();
        }
        if (bitmap != null) {
        	mBitmap = bitmap;
        	synchronized (bitmap) {
        		mResizedBitmap = resizeBitmap(bitmap, sMaxTextureSize, false);
			}
        }
    }
    
    /**
     * <br>功能简述: 设置纹理上传完成的监听者
     * <br>功能详细描述:
     * <br>注意:
     * @param listener
     */
    public void setLoadedListener(TextureLoadedListener listener) {
    	mLoadedListener = listener;
    }
    
	void setDrawableInfo(DrawableInfo info) {
		mDrawableInfo = info;
		if (info == null) {
			return;
		}
		mWidth = info.width;
		mHeight = info.height;
		mNormalizedWidth = 1;
		mNormalizedHeight = 1;
		mPaddedWidth = Shared.nextPowerOf2(mWidth);
		mPaddedHeight = Shared.nextPowerOf2(mHeight);
	}
    
	/**
	 * 使用Native内存中的像素数据来生成纹理
	 */
    private int generateTextureWithPixelPointer(int pixels) {
		if (DBG) {
			Log.d(TAG, "generateTexture this=" + this + " pixelPointer=" + pixels);
		}
		GLError.clearGLError();
		GLES20.glGenTextures(1, ID, 0);
		mId = ID[0];
		if (mId == 0) {
			mState = Texture.STATE_ERROR;
			return 0;
		}
		if (DBG) {
			Log.d(TAG, "generateTexture id=" + mId);
		}
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mId);
		++TextureManager.sCountTexture;
		//Log.d(TAG, "++TextureCount=" + TextureManager.sCountTexture);

		boolean needToCopyBitmap = false;
		boolean needToPadBitmap = mMipMap;


		if (!needToCopyBitmap) {
			boolean texImage = false;
			if (needToPadBitmap) {
				//部分机型例如LG-P970的glGenerateMipmap不支持非正方形的纹理
				mPaddedWidth = mPaddedHeight = Math.max(mPaddedWidth, mPaddedHeight);
				
				mNormalizedWidth = mWidth / (float) mPaddedWidth;
				mNormalizedHeight = mHeight / (float) mPaddedHeight;
				if (mWidth != mPaddedWidth || mHeight != mPaddedHeight) {
					texImage = true;
					if (clearTextureMemoryViaFrameBuffer(mId, mPaddedWidth, mPaddedHeight)) {
						NdkUtil.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mWidth, mHeight,
								GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixels);
					} else {
						needToCopyBitmap = true;
					}
				}
			}
			if (!texImage) {
				// 非mipmap，或者mipmap但是大小已经是POT
				NdkUtil.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidth, mHeight,
						0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixels);
			}
		}

		//needCopyBitmap |= GLError.checkGLError(GLES20.GL_INVALID_ENUM);
		if (needToCopyBitmap) {
			mState = Texture.STATE_ERROR;
			GLError.clearGLError();
			return 0;
		}

		if (mMipMap) {
			GLES20.glFinish();
			GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
		}

		final int wrapS = mWrapS;
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrapS);
		final int wrapT = mWrapT;
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrapT);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, mMinFilter);

		boolean error = GLError.checkGLError("generateTexture");
		mState = error ? STATE_ERROR : STATE_LOADED;

		mU0 = 0;
		mV0 = 0;
		mU1 = mNormalizedWidth;
		mV1 = mNormalizedHeight;
		return ID[0];
    }
    
    private int generateTexture() {
		if (mDrawableInfo != null && mDrawableInfo.pixels != 0) {
			return generateTextureWithPixelPointer(mDrawableInfo.pixels);
		}
    	
		final Bitmap bitmap = mResizedBitmap != null ? mResizedBitmap : mBitmap;
		if (DBG) {
			Log.d(TAG, "generateTexture this=" + this + " bitmap=" + bitmap);
		}
		if (bitmap == null) {
			mState = Texture.STATE_ERROR;
			return 0;
		}
		synchronized (bitmap) {
			if (bitmap.isRecycled()) {
				if (DBG) {
					Log.w(TAG, "Texture.generateTexture: " + bitmap + " is recycled.");
				}
				mState = Texture.STATE_ERROR;
				return 0;
			}
		}
		GLError.clearGLError();
		GLES20.glGenTextures(1, ID, 0);
		mId = ID[0];
		if (mId == 0) {
			mState = Texture.STATE_ERROR;
			return 0;
		}
		if (DBG) {
			Log.d(TAG, "generateTexture id=" + mId);
		}
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mId);
		++TextureManager.sCountTexture;
		//Log.d(TAG, "++TextureCount=" + TextureManager.sCountTexture);

		boolean needToCopyBitmap = false;
		boolean needToPadBitmap = mMipMap;

		synchronized (bitmap) {
			if (!bitmap.isRecycled()) {
				//XXX:例如，RGB_565格式且宽度为奇数的位图，贴图会产生倾斜的问题，这是因为texImage2D函数以字(4字节)为单位处理像素的
				//所以将图片拷贝为ARGB_8888格式的，这样会占用较多内存，所以尽量不要用奇数宽度的图片（注意图片资源目录的dpi的影响）
				//另外一种方案是利用needToPadBitmap=true;但是，纹理坐标就不是[0, 1]了，目前shader没有自动支持它，需要使用者使用mipMap。
				needToCopyBitmap = bitmap.getConfig() == null || bitmap.getRowBytes() % 4 != 0;	//CHECKSTYLE IGNORE
			}
		}

		if (!needToCopyBitmap) {
			boolean texImage = false;
			if (needToPadBitmap) {
				//部分机型例如LG-P970的glGenerateMipmap不支持非正方形的纹理
				mPaddedWidth = mPaddedHeight = Math.max(mPaddedWidth, mPaddedHeight);
				
				mNormalizedWidth = mWidth / (float) mPaddedWidth;
				mNormalizedHeight = mHeight / (float) mPaddedHeight;
				if (mWidth != mPaddedWidth || mHeight != mPaddedHeight) {
					texImage = true;
					if (clearTextureMemoryViaFrameBuffer(mId, mPaddedWidth, mPaddedHeight)) {
						synchronized (bitmap) {
							if (!bitmap.isRecycled()) {
								GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);
							}
						}
					} else {
						needToCopyBitmap = true;
					}
				}
			}
			if (!texImage) {
				// 非mipmap，或者mipmap但是大小已经是POT
				synchronized (bitmap) {
					if (!bitmap.isRecycled()) {
						GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
					}
				}
			}
		}

		//needCopyBitmap |= GLError.checkGLError(GLES20.GL_INVALID_ENUM);
		if (needToCopyBitmap) {
			//不支持的图片格式，需要转换，再重试
			try {
				Bitmap newBitmap;
				if (needToPadBitmap) {
					synchronized (bitmap) {
						newBitmap = resizeBitmap(bitmap, sMaxTextureSize, true);
					}
				} else {
					int w = bitmap.getWidth() <= 0 ? 1 : bitmap.getWidth();
					int h = bitmap.getHeight() <= 0 ? 1 : bitmap.getHeight();
					newBitmap = Bitmap.createBitmap(w, h,
							Config.ARGB_8888);
				}
				if (newBitmap != null) {
					if (newBitmap != bitmap) {
						final Canvas canvas = sCanvas;
						canvas.setBitmap(newBitmap);
						synchronized (bitmap) {
							if (!bitmap.isRecycled()) {
								canvas.drawBitmap(bitmap, 0, 0, null);
							}
						}
						canvas.setBitmap(sBitmap);
					}
					GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, newBitmap, 0);
					newBitmap.recycle();
				}
			} catch (OutOfMemoryError e) {
				//XXX:这时默认是黑色的纹理，有必要生成透明的？
			}
			GLError.clearGLError();
		}

		if (mMipMap) {
			GLES20.glFinish();
			GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
		}

		final int wrapS = GLState.sWrapS = mWrapS;
		final int wrapT = GLState.sWrapT = mWrapT;
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrapS);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrapT);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, mMinFilter);

		boolean error = GLError.checkGLError("generateTexture");
		mState = error ? STATE_ERROR : STATE_LOADED;

		if (mResizedBitmap != mBitmap) {
			recycleBitmap(mResizedBitmap);
		}
		mResizedBitmap = null;
		recycleBitmap(mBitmap);
		mBitmap = null;
		mU0 = 0;
		mV0 = 0;
		mU1 = mNormalizedWidth;
		mV1 = mNormalizedHeight;
		return ID[0];
    }
    
    /**
     * <br>功能简述: 绑定纹理
     * <br>功能详细描述:
     * <br>注意: 在GL线程上调用
     * @return
     */
    public boolean bind() {
		if (DBG) {
			Log.d(TAG, "bind texture " + this + " state=" + mState);
		}
    	boolean toPush = false;
    	final int state = mState;
    	int id = mId;
        switch (state) {
            case Texture.STATE_UNLOADED:
            	load();
            	id = generateTexture();
            	GLError.clearGLError();
            	toPush = true;
            	break;
            case Texture.STATE_LOADED:
            	GLState.setWrapMode(mWrapS, mWrapT);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
                break;
            default:
            	return false;
        }
        boolean toUpdate = false;
        Bitmap bitmap = null;
		synchronized (mLock) {
			toUpdate = mToUpdate;
			mToUpdate = false;
			bitmap = mBitmapForUpdate;
			mBitmapForUpdate = null;
		}
		if (toUpdate && bitmap != null) {
			synchronized (bitmap) {
				if (!bitmap.isRecycled()) {
					GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
					if (!sGLFlushed) {
						sGLFlushed = true;
						//HTC G11, G12等部分机型在更新纹理会出现斜纹，需要先强制刷新一下
//						GLES20.glFlush();	//部分机器用glFlush还是不行
						GLES20.glFinish();
					}
					GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);
				}
//				else{
//					Log.w(TAG, "Bitmap is recylced");
//				}
			}
			toPush = true;
			GLError.clearGLError();
		}
        if (toPush) {
        	sLoadedTextureQueue.pushBack(this);
        }
        return true;
    }
    
    /**
     * <br>功能简述: 部分更新纹理内容
     * <br>功能详细描述:
     * <br>注意: 因为更新上传的操作会等到GL线程上执行，在其他线程修改<var>bitmap</var>的内容要用synchronized关键字锁住它。
     * @param bitmap
     */
	public void updateSubImage(Bitmap bitmap) {
		synchronized (mLock) {
			mBitmapForUpdate = bitmap;
			mToUpdate = true;
		}
    }
    
	/**
	 * 生成一张空的纹理，不上传像素数据
	 */
    boolean generateVoidTexture(boolean translucent, int width, int height) {
		if (width <= 0 || height <= 0) {
			throw new RuntimeException("Texture.generateVoidTexture error: size=" + width + "x"
					+ height);
		}
		if (mState == STATE_LOADED) {
    		return true;
    	}
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mId = textures[0];
		if (mId == 0) {
			return false;
		}
		++TextureManager.sCountTexture;
		//Log.d(TAG, "++TextureCount=" + TextureManager.sCountTexture);
		
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mId);
        final int format = translucent ? GLES20.GL_RGBA : GLES20.GL_RGB;
        //final int type = translucent ? GLES20.GL_UNSIGNED_BYTE : GLES20.GL_UNSIGNED_SHORT_5_6_5;
        final int type = GLES20.GL_UNSIGNED_BYTE;	//不透明时使用RGB888格式而不是RGB565格式以提高质量
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, format, width, height, 0,
        		format, type, null);

		final int wrapS = GLState.sWrapS = mWrapS;
		final int wrapT = GLState.sWrapT = mWrapT;
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrapS);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrapT);
        
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, mMinFilter);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        mState = STATE_LOADED;
        return true;
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
     * 回收指定位图
     */
    protected void recycleBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			BitmapRecycler.recycleBitmapDeferred(bitmap);
		}
	}
    
    /**
     * <br>功能简述: 是否启用了MipMap
     * <br>功能详细描述: 如果启用，则其有效的纹理坐标可能就不是[0, 1]范围
     * <br>注意:
     * @return
     */
    public boolean isMipMapEnabled() {
    	return mMipMap;
    }
    
    /**
     * <br>功能简述: 下一张创建的纹理使用MipMap
     * <br>功能详细描述: 可以在缩小的时候提高绘制质量，防止锯齿和像素闪烁等走样现象
     * <br>注意: 目前使用的是硬件自动生成MipMap，需要图片大小为2的幂，因此实际上会复制出一张符合大小的图片，因为纹理坐标可能不是[0, 1]。
     * 
     * @param best	是否使用最好的绘制效果，在适应缩小比例相对固定的情况下可以使用false，计算量较小。
     * @see {@link #cancleMipMapNextTexture()}
     */
    public static void mipMapNextTexture(boolean best) {
    	/*
    	GL_NEAREST_MIPMAP_NEAREST
    	GL_LINEAR_MIPMAP_NEAREST 
    	GL_NEAREST_MIPMAP_LINEAR 
    	GL_LINEAR_MIPMAP_LINEAR
    	*/  
		sMipMapNextTexture = true;
		sNextTextureMinFilter = best ? GLES20.GL_LINEAR_MIPMAP_LINEAR : GLES20.GL_LINEAR_MIPMAP_NEAREST;
    }
    
    /**
     * <br>功能简述:取消 {@link #mipMapNextTexture(boolean)} 的作用，如果还没有创建纹理
     * <br>功能详细描述: 
     * <br>注意:
     */
    public static void cancleMipMapNextTexture() {
    	sMipMapNextTexture = false;
    	sNextTextureMinFilter = GLES20.GL_LINEAR;
    }
    
    static void resetStatic() {
    	sGLFlushed = false;
    	
		if (!sMaxTextureSizeGot) {
			final int[] tmp = new int[1];
			GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, tmp, 0);
			sMaxTextureSize = Math.max(MAX_TEXTURE_SIZE_LOWERBOUND, tmp[0]);
			sMaxTextureSizeGot = true;
		}
    }
    
    /**
     * <br>功能简述: 使用frameBuffer的方式将纹理的显存清除为0，避免杂色
     * <br>功能详细描述:
     * <br>注意:一般是使用自动生成mipmap的时候，需要2的幂大小的纹理，但是贴图大小不足，有部分显存没被覆盖
     * @param textureId
     * @param width
     * @param height
     * @return
     */
    private static boolean clearTextureMemoryViaFrameBuffer(int textureId, int width, int height) {
		if (textureId == 0) {
			return false;
		}
		GLError.clearGLError();

		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width,
				height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
		
		int[] framebuffers = new int[1];
		GLES20.glGenFramebuffers(1, framebuffers, 0);
		final int framebuffer = framebuffers[0];
		if (framebuffer == 0) {
			return false;
		}
		
		int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        final int newTextureId = textures[0];
		if (newTextureId == 0) {
			GLES20.glDeleteFramebuffers(1, framebuffers, 0);
			GLError.clearGLError();
			return false;
		}
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, newTextureId);
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
				0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer);
		//直接绑定textureId即旧纹理到帧缓冲区来清除颜色，更加节省效率，但是在一台HUAWEI T9510E上有问题
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
				GLES20.GL_TEXTURE_2D, newTextureId, 0);

		boolean res = false;
		int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		if (res = status == GLES20.GL_FRAMEBUFFER_COMPLETE) {
			//backup glClearColor
			final float r = GLState.sClearRed;
			final float g = GLState.sClearGreen;
			final float b = GLState.sClearBlue;
			final float a = GLState.sClearAlpha;

			GLState.glClearColor(0, 0, 0, 0);
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

			//restore glClearColor
			GLState.glClearColor(r, g, b, a);
		} else {
			GLError.clearGLError();
		}
		
		GLES20.glCopyTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 0, 0, width, height, 0);
		GLES20.glFinish();
		GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
				GLES20.GL_TEXTURE_2D, 0, 0);
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		GLES20.glDeleteFramebuffers(1, framebuffers, 0);
		GLES20.glDeleteTextures(1, textures, 0);
		return res;
    }

}