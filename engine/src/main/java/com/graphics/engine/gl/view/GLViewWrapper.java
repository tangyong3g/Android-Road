package com.graphics.engine.gl.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.EditText;

import com.graphics.engine.gl.graphics.BitmapGLDrawable;
import com.graphics.engine.gl.graphics.BitmapRecycler;
import com.graphics.engine.gl.graphics.BitmapTexture;
import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.graphics.GLDrawable;
import com.graphics.engine.gl.graphics.Texture;
import com.graphics.engine.gl.graphics.TextureListener;
import com.graphics.engine.gl.graphics.TextureLoadedListener;
import com.graphics.engine.gl.graphics.TextureManager;
import com.graphics.engine.gl.graphics.filters.GraphicsFilter;

import java.lang.reflect.Field;

/**
 * 
 * <br>类描述: {@link View}的封装器
 * <br>功能详细描述: 
 * <br>在OpenGL场景中显示视图的内容，原理是获取视图的绘图缓冲作为纹理绘制出来。
 * <ul> 主要可用的方法：
 * <li>使用{@link #setView(View, LayoutParams)}设置封装的视图。
 * <li>如果视图的内容经常更新，绘制效率会比较低，所以适合比较少更新绘制的视图，
 * 如果不希望它更新，使用{@link #setUseDeferredInvalidate(boolean)}。
 * <li>{@link #setPersistentDrawingCache(boolean)} 这个方法用来设置
 * 绘图缓冲的位图是否常驻内存，如果比较少更新，可以设为不常驻，以节省内存。
 * <li>对于文字，可以使用{@link com.graphics.engine.gl.widget.GLTextView}这个子类，它默认不常驻内存。
 * <li>{@link OnOutOfMemoryListner}用来处理绘图缓冲因为内存不够申请失败
 * 的情况，例如桌面挂件在此时会显示一张感叹号的图片。
 * </ul>
 * 
 * @author  dengweiming
 * @date  [2012-9-6]
 */
public class GLViewWrapper extends GLView implements TextureListener, TextureLoadedListener {
	protected static final String TAG = "DWM";
	protected static final boolean DBG_CANVAS = false;	//是否使用重载的Canvas来帮助调试
	protected static final boolean DBG = false;
//	public boolean DBG = false;

	protected ViewWrapper mViewGroup;
	protected View mView;
	protected Canvas mCanvas;
	protected Bitmap mBitmap;
	protected boolean mDirty;
	protected Rect mDirtyRect = new Rect();
	protected Rect mTempRect = new Rect();
	protected BitmapTexture mBitmapTexture;
	protected boolean mPersistentDrawingCache = true; //上传纹理后是否保留位图

	protected Runnable mRunnable = null; // 添加到2D view tree后执行的Runnable

	protected BitmapGLDrawable mBitmapGLDrawable;
	protected boolean mNewBitmapCreated;	//位图在本帧创建的
	protected boolean mWatingForRefreshDrawingCache;
	
	protected boolean mUseDeferredInvalidate = false; //延迟纹理的更新设置
	protected boolean mDelayInvalidate = false; //是否需要在延迟结果后更新纹理
	
	protected boolean mSizeChanged;
	
	protected int mDensity;
	
	protected OnOutOfMemoryListner mOnOutOfMemoryListner;
	
	protected int mRememberedAllInvalidateCount = -1;
	
	protected boolean mDispatchTouchEventEnabled = true; //是否将触摸事件分发给封装的视图

	protected final static int[] LOCATION = new int[2];
	
	public boolean mAutoAdjustInternalViewWrapperPosition;
	protected boolean mWillCallGetLocation;
	protected boolean mRegistered;
	
	/**
	 * 
	 * <br>类描述: 不够内存创建绘图缓冲的事件监听者
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2012-12-5]
	 */
	public interface OnOutOfMemoryListner {
		
		void onOutOfMemory();
	}

	//CHECKSTYLE IGNORE 1 LINES
	protected static final Bitmap sDefaultBitmap = Bitmap.createBitmap(1, 1, Config.ARGB_8888);
	
	
	public GLViewWrapper(Context context) {
		super(context);
		init(context);
	}

	public GLViewWrapper(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GLViewWrapper(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	protected void init(Context context) {
		mViewGroup = new ViewWrapper(context);
		if (DBG_CANVAS) {
			mCanvas = new DebugCanvas();
		} else {
			mCanvas = new Canvas();
		}

		mBitmapGLDrawable = new BitmapGLDrawable();
		mBitmapGLDrawable.unregister();	//让视图本身注册监听就可以了
		mPixelOverlayed = false;
		
		final Resources res = getResources();
		final DisplayMetrics metrics = res == null ? null : res.getDisplayMetrics();
		if (metrics != null) {
			mDensity = metrics.densityDpi;
		}
	}
	
	/**
	 * <br>功能简述: 检测在非监听期间，纹理是否已经失效
	 * <br>功能详细描述: 在onDetachedFromWindow的时候反注册监听了，如果纹理失效了，就收不到消息，
	 * 那么纹理保留的id就是过期的，那么后续的纹理清除操作就会影响到当前分配到这个id的其他纹理。
	 * <br>注意:
	 */
	protected void checkTextureInvalidated() {
		if (!mRegistered) {
			final int invalidateCount = TextureManager.getInstance().getAllInvalidateCount();
			// 通过记录次数的方法判断，有则让自己的纹理失效
			if (mRememberedAllInvalidateCount != invalidateCount) {
				if (mRememberedAllInvalidateCount != -1) {
					if (mBitmapTexture != null) {
						mBitmapTexture.onTextureInvalidate();
					}
					onTextureInvalidate();
				}
				mRememberedAllInvalidateCount = invalidateCount;
			}
		}
	}

	@Override
	protected void onAttachedToWindow() {
		if (DBG) {
			Log.d(TAG, "GLViewWrapper onAttachedToWindow: " + this);
		}
		super.onAttachedToWindow();
		
		checkTextureInvalidated();
		
		if (!mRegistered) {
			TextureManager.getInstance().registerTextureListener(this);
			mRegistered = true;
		}
		
		if (mViewGroup.getParent() == null) {
			//同样，这里也要防止多余的onAttachedToWindow
			GLContentView rootView = getGLRootView();
			if (rootView != null) {
				final ViewGroup viewGroup = rootView.getOverlayedViewGroup();
				if (viewGroup != null) {
					viewGroup.addView(mViewGroup);
				}
			}
		}
		
		if (mRunnable != null) {
			mViewGroup.post(new Runnable() {
				@Override
				public void run() {
					if (mRunnable != null) {
						mRunnable.run();
						mRunnable = null;
					}
				}
			});
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		if (DBG) {
			Log.d(TAG, "GLViewWrapper onDetachedFromWindow: " + this);
		}
		super.onDetachedFromWindow();
		final TextureManager textureManager = TextureManager.getInstance();
		textureManager.unRegisterTextureListener(this);
		mRegistered = false;
		mRememberedAllInvalidateCount = textureManager.getAllInvalidateCount();
		
		GLContentView rootView = getGLRootView();
		if (rootView != null) {
			final ViewGroup viewGroup = rootView.getOverlayedViewGroup();
			if (viewGroup != null) {
				viewGroup.removeViewInLayout(mViewGroup);
			}
		}
	}

	/**
	 * 获取绘图缓冲是否常驻
	 * @return
	 */
	public boolean getPersistentDrawingCache() {
		return mPersistentDrawingCache;
	}

	/**
	 * 设置绘图缓冲是否常驻
	 * <br>如果比较少更新，可以设为不常驻，以节省内存。
	 */
	public void setPersistentDrawingCache(boolean enabled) {
		mPersistentDrawingCache = enabled;
	}

	/**
	 * 设置添加到2D view tree 后执行的Runnable
	 * 例如scrollable widget需要等待view被添加到2D view tree上才发送广播通知AppWidgetProvider加载数据，
	 * 否则可能导致widget无法刷新
	 * @param runnable
	 * TODO：这个应该移到widget接口那里
	 */
	public void setRunnable(Runnable runnable) {
		mRunnable = runnable;
	}

	public void setView(View view, LayoutParams layoutParams) {
		if (mView != null && mView != view) {
			// clear
			mViewGroup.removeAllViewsInLayout();
			BitmapRecycler.recycleBitmapDeferred(mBitmap);
			mBitmap = null;
			mCanvas.setBitmap(sDefaultBitmap);
			checkTextureInvalidated();
			mBitmapGLDrawable.setTexture(null);
			mBitmapTexture = null;
		}
		mView = view;
		if (mView != null) {
			if (layoutParams == null) {
				layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);
			}
			final ViewParent parent = mView.getParent();
			if (parent != null) {
				Log.w(TAG, "GLViewWrapper setView " + view + " already has parent " + parent);
				if (parent instanceof ViewGroup) {
					((ViewGroup) parent).removeViewInLayout(mView);
				}
			}
			mViewGroup.addView(mView, layoutParams);
			mWillCallGetLocation = mView instanceof EditText;	//目前已知框架中的这些视图会调用getLoaciontxxx
			invalidate();
		}
	}

	public View getView() {
		return mView;
	}

	public View getViewById(int id) {
		return mView == null ? null : mView.findViewById(id);
	}

	public View getViewWithTag(Object tag) {
		return mView == null ? null : mView.findViewWithTag(tag);
	}

	public void invalidateView(Rect dirty) {
		if (mView != null) {
			if (mDirty) {
				mDirtyRect.union(dirty);
			} else {
				mDirtyRect.set(dirty);
				if (!mUseDeferredInvalidate) {
					mDirty = true;
				} else {
					mDelayInvalidate = true;
				}
			}
		}
		super.invalidate();
	}

	public void invalidateView(int l, int t, int r, int b) {
		if (mView != null) {
			if (mDirty) {
				mDirtyRect.union(l, t, r, b);
			} else {
				mDirtyRect.set(l, t, r, b);
				if (!mUseDeferredInvalidate) {
					mDirty = true;
				} else {
					mDelayInvalidate = true;
				}
			}
		}
		super.invalidate();
	}

	public void invalidateView() {
		if (mView != null) {
			mDirtyRect.set(0, 0, getWidth(), getHeight());
			if (!mUseDeferredInvalidate) {
				mDirty = true;
			} else {
				mDelayInvalidate = true;
			}
		}
		super.invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mView != null) {
			mView.measure(widthMeasureSpec, heightMeasureSpec);
			setMeasuredDimension(resolveSize(mView.getMeasuredWidth(), widthMeasureSpec),
					resolveSize(mView.getMeasuredHeight(), heightMeasureSpec));
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mBitmapGLDrawable.setBounds(0, 0, w, h);
		mBitmapGLDrawable.setIntrinsicSize(w, h);
		mViewGroup.layout(0, 0, w, h);
		
		mSizeChanged = true;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (DBG) {
			Log.d(TAG, "onLayout " + left + " " + top + " " + right + " " + bottom + " " + this);
		}
		final int width = right - left;
		final int height = bottom - top;
		if (changed) {
			if (mWillCallGetLocation || mAutoAdjustInternalViewWrapperPosition) {
				adjustInternalViewWrapperPosition();
			} else {
				mViewGroup.layout(left, top, right, bottom);
			}
		}
		if (mView != null) {
			if (mSizeChanged) {
				checkTextureInvalidated();
				mBitmapGLDrawable.setTexture(null);
				mBitmapTexture = null;
			}
			boolean relayout = mSizeChanged || isLayoutRequested();
			mSizeChanged = false;
			if (relayout) {
				mView.layout(0, 0, width, height);
				if (getHandler() == null) {
					//还没添加到视图树上时，ViewWrapper.invalidateChildInParent()不会被调用，需要强制刷新
					invalidateView();
				}
			}
			if (mBitmap != null) {
				if (mBitmap.getWidth() != width || mBitmap.getHeight() != height) {
					BitmapRecycler.recycleBitmapDeferred(mBitmap);
					mBitmap = null;
					mCanvas.setBitmap(sDefaultBitmap);
				}
			}
		}
	}

	static int sSaveBitmapCount = 0;
	
	protected void logOnDraw() { }
	
	protected void logInvalidateChildInParent(Rect dirty) { }

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
		
		if (mBitmapTexture == null) {
			mBitmapTexture = new BitmapTexture(mBitmap);
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
	
	@Override
	public void onTextureLoaded(Texture texture) {
		if (!mPersistentDrawingCache && !mWatingForRefreshDrawingCache) {
			if (texture instanceof BitmapTexture) {
				if (DBG) {
					Log.d(TAG, "onTextureLoaded " + this);
				}
				BitmapRecycler.recycleBitmapDeferred(((BitmapTexture) texture).getBitmap());
				((BitmapTexture) texture).resetBitmap();
				mBitmap = null;
				//因为这个事件的添加是在GL线程的，所以有可能在cleanup之后还会回调本方法，因此要加空指针保护
				if (mCanvas != null) {
					mCanvas.setBitmap(sDefaultBitmap);
				}
			}
		}
		
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (mView != null && mDispatchTouchEventEnabled) {
			boolean viewHandled = mView.dispatchTouchEvent(event);
			boolean glViewHandled = superDispatchTouchEvent(event);
			return viewHandled || glViewHandled;
		}
		return superDispatchTouchEvent(event);
	}
	
	protected final boolean superDispatchTouchEvent(MotionEvent event) {
		return super.dispatchTouchEvent(event);
	}
	
	/**
	 * <br>功能简述: 设置是否将触摸事件分发给封装的视图
	 * <br>功能详细描述:
	 * <br>注意: 默认启用
	 * @param enabled
	 */
	public final void setDispatchTouchEventEnabled(boolean enabled) {
		mDispatchTouchEventEnabled = enabled;
	}

	/**
	 * <br>功能简述: 告诉父容器事件是否已经被自己拦截
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param disallowIntercept
	 */
	public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
		GLViewParent glViewParent = getGLParent();
		if (glViewParent != null) {
			glViewParent.requestDisallowInterceptTouchEvent(disallowIntercept);
		}
	}
	
	/**
	 * 
	 * <br>类描述: 封装的视图的父容器，用于拦截重绘或排版等请求
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2012-9-6]
	 */
	protected class ViewWrapper extends ViewGroup {

		public ViewWrapper(Context context) {
			super(context);
		}

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		}

		@Override
		public void requestLayout() {
			GLViewWrapper.this.requestLayout();
		}

		@Override
		public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
			if (DBG) {
				Log.v(TAG, "invalidateChildInParent this=" + this + " dirty=" + dirty);
				debugView(ViewWrapper.this);
			}
			logInvalidateChildInParent(dirty);
			GLViewWrapper.this.invalidateView(dirty);
			return null;
		}

		@Override
		public void dispatchDraw(Canvas canvas) {
		}

		@Override
		public boolean drawChild(Canvas canvas, View child, long drawingTime) {
			return super.drawChild(canvas, child, drawingTime);
		}

		@Override
		public void invalidate() {
			GLViewWrapper.this.invalidateView();
		}

		@Override
		public void invalidate(int l, int t, int r, int b) {
			GLViewWrapper.this.invalidateView(l, t, r, b);
		}

		@Override
		public void invalidate(Rect dirty) {
			GLViewWrapper.this.invalidateView(dirty);
		}
		
		@Override
		public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
			GLViewWrapper.this.requestDisallowInterceptTouchEvent(disallowIntercept);
		}
	}
	
    /**
     * 打印视图树信息，调试用
     */
    private static void debugView(View view) {
    	debugView(view, 0);
    }
    
    private static void debugView(View view, int depth) {
        String output = debugIndent(depth - 1);

        output += "+ " + view;
        int id = view.getId();
        if (id != -1) {
            output += " (id=" + id + ")";
        }
        Object tag = view.getTag();
        if (tag != null) {
            output += " (tag=" + tag + ")";
        }
        Log.d(VIEW_LOG_TAG, output);
        
        Field field = null;
        int privateFlags = 0;
		try {
			field = View.class.getDeclaredField("mPrivateFlags");
			field.setAccessible(true);
			privateFlags = field.getInt(view);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

        if ((privateFlags & FOCUSED) != 0) {
            output = debugIndent(depth) + " FOCUSED";
            Log.d(VIEW_LOG_TAG, output);
        }

        output = debugIndent(depth);
        output += "frame={" + view.getLeft() + ", " + view.getTop() + ", " + view.getRight()
                + ", " + view.getBottom() + "} scroll={" + view.getScrollX() + ", " + view.getScrollY()
                + "} ";
        Log.d(VIEW_LOG_TAG, output);

        if (view.getPaddingLeft() != 0 || view.getPaddingTop() != 0 || view.getPaddingRight() != 0
                || view.getPaddingBottom() != 0) {
            output = debugIndent(depth);
            output += "padding={" + view.getPaddingLeft() + ", " + view.getPaddingTop()
                    + ", " + view.getPaddingRight() + ", " + view.getPaddingBottom() + "}";
            Log.d(VIEW_LOG_TAG, output);
        }

        output = debugIndent(depth);
        output += "mMeasureWidth=" + view.getMeasuredWidth() +
                " mMeasureHeight=" + view.getMeasuredHeight();
        Log.d(VIEW_LOG_TAG, output);
        
        Drawable bg = view.getBackground();
		if (bg != null) {
			output = debugIndent(depth);
			output += "mBackground=" + bg + " bounds=" + bg.getBounds();
			Log.d(VIEW_LOG_TAG, output);
		}

        output = debugIndent(depth);
        if (view.getLayoutParams() == null) {
            output += "BAD! no layout params";
        } else {
        	//XXX mLayoutParams.debug隐藏了
        	output = " [layoutParames.debug] ";
//            output = mLayoutParams.debug(output);
        }
        Log.d(VIEW_LOG_TAG, output);

        output = debugIndent(depth);
        output += "flags={";
//        output += GLView.printFlags(mViewFlags);
        output += "}";
        Log.d(VIEW_LOG_TAG, output);

        output = debugIndent(depth);
        output += "privateFlags={";
//        output += GLView.printPrivateFlags(mPrivateFlags);
        output += "0x" + Integer.toHexString(privateFlags) + " " + Integer.toBinaryString(privateFlags);
        output += "}";
        Log.d(VIEW_LOG_TAG, output);
        
		if (view instanceof ViewGroup) {
			output = "";
			int childrenCount = ((ViewGroup) view).getChildCount();
			if (childrenCount != 0) {
				output = debugIndent(depth);
				output += "{";
				Log.d(VIEW_LOG_TAG, output);
			}
			int count = childrenCount;
			for (int i = 0; i < count; i++) {
				View child = ((ViewGroup) view).getChildAt(i);
				debugView(child, depth + 1);
			}

			if (childrenCount != 0) {
				output = debugIndent(depth);
				output += "}";
				Log.d(VIEW_LOG_TAG, output);
			}
		}
    }

	@Override
	public void cleanup() {
		BitmapRecycler.recycleBitmapDeferred(mBitmap);
		mBitmap = null;
		
		checkTextureInvalidated();
		mBitmapGLDrawable.clear();
		if (mBitmapTexture != null) {
			mBitmapTexture.setLoadedListener(null);
			mBitmapTexture = null;
		}

		if (mViewGroup != null) {
			mViewGroup.removeAllViewsInLayout();
			ViewParent parent = mViewGroup.getParent();
			if (parent != null && parent instanceof ViewGroup) {
				((ViewGroup) parent).removeViewInLayout(mViewGroup);
			}
		}
		
		if (mCanvas != null) {
			mCanvas.setBitmap(sDefaultBitmap);
			mCanvas = null;
		}
		
		if (null != mView) {
			mView.setOnLongClickListener(null);
			mView.setOnClickListener(null);
			mView = null;
		}
		
		mOnOutOfMemoryListner = null;
		
		TextureManager.getInstance().unRegisterTextureListener(this);
		mRegistered = false;
		
		super.cleanup();
	}
	
	/**
	 * <br>功能简述: 设置延迟更新绘图缓冲
	 * <br>功能详细描述: 在某些情况下，为了性能，不希望更新绘图缓冲太频繁，可以使用这个方法
	 * <br>注意:
	 * @param useDeferredInvalidate 为false会暂停更新绘图缓冲，直到为true的调用来到。
	 */
	public void setUseDeferredInvalidate(boolean useDeferredInvalidate) {
		mUseDeferredInvalidate = useDeferredInvalidate;
		//XXX: 如果参数为true应该要invalidate一下？
	}
	

	
    /**
     * <br>功能简述: 设置透明度
     * <br>功能详细描述:
     * <br>注意: 背景不会受影响（不过一般都不会设置背景，而是{@link #getView()}得到封装的视图再设置背景）
     * @param alpha [0, 255]
     */
	@Override
	public void setAlpha(int alpha) {
		mBitmapGLDrawable.setAlpha(alpha);
	}
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
     * <br>注意: 背景不会受影响（不过一般都不会设置背景，而是{@link #getView()}得到封装的视图再设置背景）
	 * @param srcColor
	 * @param mode	为 null 可以清除 color filter
	 */
	@Override
	public void setColorFilter(int srcColor, PorterDuff.Mode mode) {
		mBitmapGLDrawable.setColorFilter(srcColor, mode);
	}
	
	@Override
	public void setFocusable(boolean focusable) {
		if (mView != null) {
			mView.setFocusable(true);
		}
		super.setFocusable(focusable);
	}
	
	@Override
	public void setFocusableInTouchMode(boolean focusableInTouchMode) {
		if (mView != null) {
			mView.setFocusableInTouchMode(true);
		}
		super.setFocusableInTouchMode(focusableInTouchMode);
	}

	@Override
	public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
		if (mView != null) {
			mView.requestFocus(direction, previouslyFocusedRect);
		}
		return super.requestFocus(direction, previouslyFocusedRect);
	}
	
	@Override
	public void clearFocus() {
		if (mView != null) {
			mView.clearFocus();
		}
		super.clearFocus();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (mView != null) {
			mView.onWindowFocusChanged(hasWindowFocus);
		}
		super.onWindowFocusChanged(hasWindowFocus);
	}
	
	/**
	 * <br>功能简述: 设置不够内存创建绘图缓冲的事件监听者
	 * <br>功能详细描述:
	 * <br>注意: 事件回调是在绘制期间调用的，响应的时候只能改变之后的绘制行为
	 * @param listner
	 */
	public void setOnOutOfMemoryListner(OnOutOfMemoryListner listner) {
		mOnOutOfMemoryListner = listner;
	}
	
	/**
	 * <br>功能简述: 调整内部的ViewWrapper的位置
	 * 
	 * <br>功能详细描述: 如果包装的View需要调用{@link View#getLocationInWindow(int[])} 或者
	 * {@link View#getLocationOnScreen(int[])}，则需要先调用本方法保证结果正确（特别是父容器
	 * 有滚动过），因为包装的View是直接用父容器也即内部的ViewWrapper的位置去计算自己的全局位置的。
	 * 
	 * <br>注意: 在屏幕层添加Circle Launcher的widget，点击后会在widget周围显示一个圆圈，因为此时
	 * 父容器（CellLayout）的左上角是对齐到根视图左上角的，因此不调用本方法也可以。（另外在隐藏状态栏的
	 * 时候，这个圆圈的位置是偏上的，Go桌面也有这个问题，不管了）
	 */
	public void adjustInternalViewWrapperPosition() {
		getLocationUnderStatusBar(LOCATION);
		mViewGroup.layout(LOCATION[0], LOCATION[1], LOCATION[0] + getWidth(), LOCATION[1] + getHeight());
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		super.onSaveInstanceState();
		return null;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		//如果本身和封装的 View 有一样 id，下面的调用参数为 state 就会抛出异常
		//为了防止这种情况，将参数改为 null，不需要还原状态，那么也不需要保存（默认的状态其实是空的）
		//而封装的 View 也添加到视图树上了，它的保存和还原跟这里无关
		super.onRestoreInstanceState(null);
	}
	
	/**
	 * <br>类描述: 重载的Canvas，以帮助调试，需要打开{@link #DBG_CANVAS}
	 * <br>功能详细描述:
	 * 需要调试的方法在下面重载即可
	 * 
	 * @author  dengweiming
	 * @date  [2013-12-2]
	 */
	class DebugCanvas extends Canvas {
		
		@Override
		public void translate(float dx, float dy) {
			if (DBG) {
				Log.d(TAG, "canvas translate " + dx + " " + dy);
			}
			super.translate(dx, dy);
		}
		
		@Override
		public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
			if (DBG) {
				Log.d(TAG, "canvas drawBitmap " + bitmap + " " + src + " " + dst);
			}
			super.drawBitmap(bitmap, src, dst, paint);
		}
		
	}
	
}