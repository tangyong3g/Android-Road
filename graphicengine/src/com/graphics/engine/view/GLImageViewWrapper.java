package com.graphics.engine.view;

import com.graphics.engine.graphics.BitmapGLDrawable;
import com.graphics.engine.graphics.BitmapRecycler;
import com.graphics.engine.graphics.BitmapTexture;
import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.graphics.GLDrawable;
import com.graphics.engine.graphics.Texture;
import com.graphics.engine.graphics.filters.GraphicsFilter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * 封装绘制2D图片转3D的视图
 * @author yangyiwei
 *
 */
public class GLImageViewWrapper extends GLViewWrapper {
	
	private boolean mNativeCacheEnable = false;
	
	public GLImageViewWrapper(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public GLImageViewWrapper(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public GLImageViewWrapper(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	protected void init(Context context) {
		mViewGroup = new ViewWrapper(context);
		if (DBG_CANVAS) {
			mCanvas = new DebugCanvas();
		} else {
			mCanvas = new Canvas();
		}

		mBitmapGLDrawable = new BitmapGLDrawable();
		if (!mNativeCacheEnable) {			
			mBitmapGLDrawable.unregister();	//让视图本身注册监听就可以了
		}
		mPixelOverlayed = false;
		
		final Resources res = getResources();
		final DisplayMetrics metrics = res == null ? null : res.getDisplayMetrics();
		if (metrics != null) {
			mDensity = metrics.densityDpi;
		}
	}
	
	public void setNativeCacheEnable(boolean enable) {		
		mNativeCacheEnable = enable;
		if (mNativeCacheEnable) {
			mBitmapGLDrawable.register();
		} else {
			mBitmapGLDrawable.unregister();
		}
	}
	
	/**
	 * 保存缓存图片数据到Native内存，这样做的目的是在OpenGL纹理失效后，再次需要绘制纹理时直接从Native中再上传纹理数据到OpenGL。
	 * 作为缓存图片通常不需要反向从Native内存中拿回图片数据，因为如果需要再次用到缓存图，直接createBitmap一张出来就可以了，所以没
	 * 有定义与之相反的方法
	 */
	public void saveCacheToNativeMemory() {
		if (!mNativeCacheEnable ||
			mBitmap == null) {
			return;
		}
		
		synchronized (mBitmap) {			
			// 转存到native
			if (BitmapTexture.saveBitmapToNativeMemory(mBitmap, true)) {
				mBitmap.recycle();
			}
		}
	}
	
	public void onRefreshDrawingCache() {
		
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		if (mView == null) {
			return;
		}

		final boolean layoutRequested = mView.isLayoutRequested();
		if (layoutRequested) {
			//mView可能被调用forceLayout而本类没法获知
			mView.measure(mOldWidthMeasureSpec, mOldHeightMeasureSpec);
			mView.layout(0, 0, getWidth(), getHeight());
			mDirtyRect.set(0, 0, getWidth(), getHeight());
			mDirty = true;
		}

		final boolean cancleUpdate = mWatingForRefreshDrawingCache || mView.getHandler() == null;
		
		if (mDelayInvalidate && !mUseDeferredInvalidate) {
			mDelayInvalidate = false;
			mDirtyRect.set(0, 0, getWidth(), getHeight());
			mDirty = true;
		}
		//使用绘图缓冲更新纹理
		if (!cancleUpdate && mDirty) {
			if (DBG) {
				Log.d(TAG, "onDraw this=" + this + " w=" + getWidth() + " h=" + getHeight() + " dirty=" + mDirty + " " + mDirtyRect);
			}
			logOnDraw();
			
			final Rect dirtyRect = mTempRect;
			dirtyRect.set(mDirtyRect);
			mDirtyRect.setEmpty();
			mDirty = false;
			final boolean reallyDirty = dirtyRect.intersect(0, 0, getWidth(), getHeight());
			if (reallyDirty) {
				boolean newBitmapCreated = false;
				if (mNativeCacheEnable) {
					mBitmapGLDrawable.setTexture(null);
					mBitmapTexture = null;
					BitmapRecycler.recycleBitmapDeferred(mBitmap);
					mBitmap = null;
					mCanvas.setBitmap(sDefaultBitmap);
				}
				
				if (mBitmap == null) {
					try {
						mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
						if (null == mBitmap || null == mCanvas) {
							return;
						}
						if (mDensity != 0) {
							mBitmap.setDensity(mDensity);
						}
					} catch (OutOfMemoryError e) {
						mBitmap = null;
//						if (DBG) {
//							Log.w(TAG, "GLViewWrapper onDraw out of memory width=" + getWidth() + " height=" + getHeight());
//						}
						if (mOnOutOfMemoryListner != null) {
							mOnOutOfMemoryListner.onOutOfMemory();
						}
						return;
					}
					newBitmapCreated = true;
				}
				mNewBitmapCreated = newBitmapCreated;
			}
			if (reallyDirty || mBitmapTexture == null) {
				if (mBitmap != null) {
					refreshDrawingCache();
					onRefreshDrawingCache();
				}
			}
		}

		if (mGraphicsFilterEnabled) {
			GraphicsFilter[] filters = mGraphicsFilters;
			boolean yield = false;
			GLDrawable drawable = mBitmapGLDrawable;
			for (GraphicsFilter graphicsFilter : filters) {
				drawable = graphicsFilter.apply(canvas, getResources(), drawable, yield);
				yield = true;
			}
			drawable.draw(canvas);
		} else {
			mBitmapGLDrawable.draw(canvas);
		}
		
		if (cancleUpdate && mDirty) {
			invalidate();	//引起重绘，到时候更新绘图缓冲
			return;
		}

	}
	
	private void refreshDrawingCache() {
		
		if (DBG) {
			Log.d(TAG, "refreshDrawingCache " + mBitmap + " " + mBitmapTexture + " new=" + mNewBitmapCreated + " this=" + this);
		}

		final boolean newBitmap = mNewBitmapCreated;
		mNewBitmapCreated = false;
		
		mWatingForRefreshDrawingCache = false;
		
		boolean newTexture = false;
		
		if (mBitmap.isRecycled()) {
			Log.d("yyw", "mBitmapTexture:" + mBitmapTexture);
//			throw new RuntimeException("mBitmap isRecycle and over refreshDrawingCache");
			return;
		}
		
		if (mBitmapTexture == null) {
			if (mNativeCacheEnable) {				
				mBitmapTexture = BitmapTexture.createSharedTexture(mBitmap);
			} else {
				mBitmapTexture = new BitmapTexture(mBitmap);
			}
			mBitmapGLDrawable.setTexture(mBitmapTexture);
			mBitmapTexture.setLoadedListener(this);
			newTexture = true;
		}
		
		final Rect dirtyRect = mTempRect;

		synchronized (mBitmap) {
			mCanvas.setBitmap(mBitmap);
			final int bgColor = getDrawingCacheBackgroundColor();
			if (bgColor != 0 && newBitmap) {
				mBitmap.eraseColor(bgColor);
			}
			int saveCount = mCanvas.save();
			if (dirtyRect.width() < getWidth() || dirtyRect.height() < getHeight()) {
				mCanvas.clipRect(dirtyRect);
				if (!newBitmap) {
					mCanvas.drawColor(bgColor, PorterDuff.Mode.SRC);
				}
			} else {
				if (!newBitmap) {
					mBitmap.eraseColor(bgColor);
				}
			}
			//mCanvas.translate(-mView.getScrollX(), -mView.getScrollY());
			//mView.draw(mCanvas);
			//上面两句不能绘制Animation
			try {
				mViewGroup.drawChild(mCanvas, mView, getDrawingTime());
			} catch (Exception e) {
//				e.printStackTrace();
			}
			mCanvas.restoreToCount(saveCount);
			mCanvas.setBitmap(sDefaultBitmap);

//				if (DBG) {
//					if(mView != null){
//						GLCanvas.saveBitmap(mBitmap, "_test/" + sSaveBitmapCount++ + ".png");	//for test
//					}
//				}
		}
		
		if (!newTexture) {
			mBitmapTexture.updateSubImage(mBitmap);
		}
		invalidateFilters();
	}
	
	@Override
	public void onTextureInvalidate() {
		if (DBG) {
			Log.d(TAG, "onTextureInvalidate " + this);
		}
		
		if (!mNativeCacheEnable) {			
			mBitmapGLDrawable.setTexture(null);
			mBitmapTexture = null;
			
			//清除了缓冲位图时（例如文字视图），需要全部刷新，否则不要破坏脏区域
			if (mBitmap == null) {
				invalidateView();
			} else {
				mDirty = true;
				invalidate();
			}
		}
	}
	
	@Override
	public void onTextureLoaded(Texture texture) {
		if (!mPersistentDrawingCache && !mWatingForRefreshDrawingCache) {
			if (texture instanceof BitmapTexture) {
				if (DBG) {
					Log.d(TAG, "onTextureLoaded " + this);
				}
				
				if (!mNativeCacheEnable) {					
					BitmapRecycler.recycleBitmapDeferred(((BitmapTexture) texture).getBitmap());
					((BitmapTexture) texture).resetBitmap();
					mBitmap = null;
				}
				//因为这个事件的添加是在GL线程的，所以有可能在cleanup之后还会回调本方法，因此要加空指针保护
				if (mCanvas != null) {
					mCanvas.setBitmap(sDefaultBitmap);
				}
			}
		}
		
	}
}
