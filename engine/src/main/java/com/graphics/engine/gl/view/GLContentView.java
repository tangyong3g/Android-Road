package com.graphics.engine.gl.view;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.WeakHashMap;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import com.graphics.engine.gl.ICleanup;
import com.graphics.engine.gl.Timer;
import com.graphics.engine.gl.animator.ValueAnimator;
import com.graphics.engine.gl.graphics.BitmapRecycler;
import com.graphics.engine.gl.graphics.BitmapTexture;
import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.graphics.GLError;
import com.graphics.engine.gl.graphics.IndexBufferBlock;
import com.graphics.engine.gl.graphics.RenderContext;
import com.graphics.engine.gl.graphics.RenderInfoNode;
import com.graphics.engine.gl.graphics.Renderable;
import com.graphics.engine.gl.graphics.Texture;
import com.graphics.engine.gl.graphics.TextureManager;
import com.graphics.engine.gl.graphics.TextureRecycler;
import com.graphics.engine.gl.graphics.VertexBufferBlock;
import com.graphics.engine.gl.math3d.GeometryPools;
import com.graphics.engine.gl.util.FastQueue;
import com.graphics.engine.gl.util.FpsCounter;
import com.graphics.engine.gl.util.FrameTracker;
import com.graphics.engine.gl.util.NdkUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2012-9-6]
 */
public class GLContentView extends GLSurfaceView implements GLViewParent {
	private static final boolean DBG = false;
	public static final String TAG = "DWM";
	private static final boolean SHOW_FPS = false;
	public static final String FPS_TAG = "golauncher3d";
	private static final boolean ENABLE_SCREENSHOT = false;
	public static com.graphics.engine.gl.util.Log sLog = new com.graphics.engine.gl.util.Log("ShellEngine");

	/** default filed of view */
//	public static final float DEFAULT_FOV = GLCanvas.DEFAULT_FOV;
//	public static final float DEFAULT_Z_RANGE = GLCanvas.DEFAULT_Z_RANGE;
	private static final float HALF = 0.5f;
	/** @formatter:off */
	// CHECKSTYLE IGNORE 1 LINES
//	private static final double DEFAULT_FOV_SCALE_FACTOR = 0.5 / Math.tan(Math.toRadians(DEFAULT_FOV) * 0.5);
	/** @formatter:on */

	private static final int MAX_STACK_DEPTH = 64;
	
	public static final Object OVERLAY_VIEWGROUP_TAG = new Object();

	private final float[] mRefPosition = new float[3];	// CHECKSTYLE IGNORE
	private final static float OneOver255 = 1 / 255.0f; // CHECKSTYLE IGNORE
	private final static int FullAlpha = 255;			// CHECKSTYLE IGNORE
	private float mHalfFovX;
	private float mHalfFovY;
	private int mClearAlpha = 0;
	private boolean mTranslucent; //Surface的Format为PixelFormat.TRANSLUCENT

	private GLDecorView mGlViewGroup;
	boolean mIsAnimating;
	private boolean mTraversalScheduled;

	private AttachInfo mAttachInfo;
	private GLRenderer mRenderer;
	private RendererWrapper mRendererWrapper;
	private int mSurfaceCreateCount;
	private GLCanvas mCanvas;
	private boolean mDrawing;
	private final Rect mDirty = new Rect();
	
	private final Ray mTouchRay = new Ray();
	private float mTouchX;
	private float mTouchY;
	private boolean mTouchRayInvalidated = true;
	private final static float[] TEMP_VECTOR = new float[GLCanvas.VECTOR_LENGTH];

	private FpsCounter mFpsCounter = new FpsCounter(1);

	private static View sView;
	private static GLContentView sGLViewRoot;

	private TextureManager mTextureManager;
	private ViewGroup mOverlayedViewGroup;

	private GLConfigChooser mGlConfigChooser;
	private int mMaxTexureSize; //GL_MAX_TEXTURE_SIZE;
	private int mColorBits;
	private int mDepthBits;
	private int mStencilBits;

	private boolean mRunning;
	private boolean mResumed = false;
	private	boolean mPreparedForTraversal;
	private final RenderInfoNode[] mRenderInfoNodeStack = new RenderInfoNode[MAX_STACK_DEPTH * 2];

	private Object mAttachInfoObject;	//主线程的 mAttachInfo 对象
	private Field mDrawingTimeField;	//主线程的 mAttachInfo 对象的 mDrawingTime 变量域

	private RenderData[] mRenderDatas;

	private int mMainThreadHashCode;
	private int mGLThreadHashCode;
	
	private boolean mEventsEnabled = true;

	private boolean mEventsToken = false;

	private Rect mTempRect = new Rect();
	private static final String DOT_TGA = ".tga";
	
	private FrameTracker mFrameTracker = new FrameTracker();
	
	private long mDrawingTime;
	private long mAdjustTime = 0;
	private boolean mDisableAttachGlViewGroup;
	/** 是否已经迁移到另外一个实例，即被作为参数调用过{@link #transferFrom(GLContentView)}，或者已经销毁 */
	private boolean mTransfered;
	SurfaceViewOwner mSurfaceViewOwner;
	
	private boolean mGLES20Supported;
	private GLThreadUncaughtExceptionHandler mGLThreadUncaughtExceptionHandler;
	private boolean mSurfaceInvalid;
	private int mEGLContextClientVersion = 2;

	/** @hide */
	public int mContextHashCode;

	private GLContentView(Context context) {
		super(context);
	}

	public GLContentView(Context context, boolean translucent) {
		super(context);
		init(translucent);
	}

	public GLContentView(Context context, AttributeSet attr) {
		super(context, attr);
		init(false); //默认使用16位颜色格式
	}
	
	public void setSurfaceViewOwner(SurfaceViewOwner surfaceViewOwner) {
		mSurfaceViewOwner = surfaceViewOwner;
	}

	protected void init(boolean translucent) {
		if (DBG) {
			Log.d(TAG, "GLContentView init " + this + " translucent=" + translucent);
		}


		
		createStaticView(getContext());
		// mOverlayedViewGroup = new FrameLayout(getContext());
		// mOverlayedViewGroup.setVisibility(View.GONE);
		mMainThreadHashCode = Thread.currentThread().hashCode();

		mRenderDatas = new RenderData[BUFFER_COUNT];
		for (int i = 0; i < BUFFER_COUNT; ++i) {
			mRenderDatas[i] = new RenderData();
		}

		TextureManager.getInstance().initInternalShaders();

		mTextureManager = TextureManager.getInstance();

		mCanvas = new GLCanvas(MAX_STACK_DEPTH);
		mCanvas.allocateStringBuilder();

		mRenderer = new GLRenderer();
		mRendererWrapper = new RendererWrapper();
		mRendererWrapper.setRenderer(mRenderer);
		
		mAttachInfo = new AttachInfo();
		mAttachInfo.mViewProxy = this;
		//作为代理必须自己启用这些设置
		setHapticFeedbackEnabled(true);
		setSoundEffectsEnabled(true);
		
		mDrawingTime = SystemClock.uptimeMillis();
		mAttachInfo.mDrawingTime = mDrawingTime;
//		AnimationUtils.sDrawingTime = mDrawingTime;

		mGlViewGroup = new GLDecorView(getContext());
		mGlViewGroup.assignParent(this);
		mGLThreadUncaughtExceptionHandler = new GLThreadUncaughtExceptionHandler(this);
		mGlConfigChooser = new GLConfigChooser();
		if (translucent) {
			mGlConfigChooser.setConfigure(8, 16, 4);	//CHECKSTYLE IGNORE
			getHolder().setFormat(PixelFormat.TRANSLUCENT);
			mClearAlpha = 0;
		} else {
			mGlConfigChooser.setConfigure(0, 16, 4);	//CHECKSTYLE IGNORE
			getHolder().setFormat(PixelFormat.OPAQUE);
			mClearAlpha = FullAlpha;
		}
		mCanvas.setBackgroundClearColor(mClearAlpha << 24);	//CHECKSTYLE IGNORE
		setEGLConfigChooser(mGlConfigChooser);
		mTranslucent = translucent;
		setEGLContextClientVersion(mEGLContextClientVersion);
		setEGLContextFactory(new ContextFactory(mEGLContextClientVersion, mGLThreadUncaughtExceptionHandler));
		setEGLWindowSurfaceFactory(new WindowSurfaceFactory());
		setRenderer(mRendererWrapper);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}

	/**
	 * 设置背景是否透明（仅在初始化设置为支持透明才可以）
	 * @param alpha	[0, 255]
	 * @return	是否设置成功
	 */
	public boolean setBackgroundAlpha(int alpha) {
		if (mTranslucent) {
			mClearAlpha = alpha;
			mCanvas.setBackgroundClearColor(mClearAlpha << 24);	//CHECKSTYLE IGNORE
			return true;
		}
		return false;
	}
	
	/**
	 * <br>功能简述: 横竖屏切换时的处理
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param vertical 要切换到的方向
	 */
	public void onOrientationChanged(boolean vertical) {
		if (mTransfered) {
			return;
		}
		if (DBG) {
			Log.d(TAG, "GLContentView onOrientationChanged: w=" + getWidth()
					+ " h=" + getHeight() + " -> vertical=" + vertical);
		}
		final boolean wait = mWaitForSizeChanged;
		final boolean changed = (getWidth() < getHeight()) ^ vertical;
		mWaitForSizeChanged = changed;
		if (wait && !changed) {
			// 连续两次onOrientationChanged调用，而还没有onSizeChanged调用，则取消等待
			if (mWindowVisibilityToSet >= 0) {
				onWindowVisibilityChanged(mWindowVisibilityToSet);
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (DBG) {
			Log.d(TAG, "GLContentView onSizeChanged w=" + w + " h=" + h + " running=" + mRunning);
		}
		if (mTransfered) {
			return;
		}
		float dstDepth = (float) (h * GLCanvas.getDefaultFovScaleFactor());
		mRefPosition[0] = -w * HALF;
		mRefPosition[1] = h * HALF - mTranslateY;
		mRefPosition[2] = -dstDepth;

		mHalfFovX = (float) Math.toDegrees(Math.atan(w * HALF / dstDepth));
		mHalfFovY = GLCanvas.getDefaultFov() / 2;

		mSizeChanged = true;
		
		if (mRunning) {
			removeCallbacks(mRequestRenderAction);
			forceWriteCurrentFrame();
			mDisablePredraw = true;
			forceTraversal();
			mDisablePredraw = false;
		} else {
			// XXX:
			// 有可能未onAttachToWindow就onSizeChanged而导致后续removeCallbacks没用么？保险起见还是加个判断
			if (getHandler() != null) {
				mSurfaceInvalid = false;
//				mDisablePredraw = true;
				onResume();
//				mDisablePredraw = false;
				unscheduleTraversals();
				if (mSurfaceInvalid) {
					return;
				}
				if (!mPauseActionPosted) {
					mPauseActionPosted = true;
					postOnFrameRendered(mPauseAction);
				}
			}
		}
		
		if (mWaitForSizeChanged) {
			mWaitForSizeChanged = false;
			if (mWindowVisibilityToSet >= 0) {
				onWindowVisibilityChanged(mWindowVisibilityToSet);
			}
		}
	}
	
	private void forceTraversal() {
		unscheduleTraversals();
		mTraversalsTime = -1;
		mTraversalsAction.run();
		
		//最多重试一次
		if (mDrawCancled) {
			unscheduleTraversals();
			mTraversalsTime = -1;
			mTraversalsAction.run();
		}
		finishWritingBuffer();
		if (mDrawCancled) {
			mDrawCancled = false;
			if (DBG) {
				Log.w(TAG, "cancle draw when forceTraversal");
			}
		}
	}
	
	@Override
	public void forceLayout() {
		super.forceLayout();
		mLayoutRequested = true;
		if (mGlViewGroup != null) {
			forceLayout(mGlViewGroup);
		}
	}
	
    private static void forceLayout(GLView view) {
        view.forceLayout();
        if (view instanceof GLViewGroup) {
            GLViewGroup group = (GLViewGroup) view;
            final int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                forceLayout(group.getChildAt(i));
            }
        }
    }

	@Override
	public void requestLayout() {
		GLDecorView view = mGlViewGroup;
		if (view != null && view.mRequestingLayout) {
			checkThread();
			mLayoutRequested = true;
			scheduleTraversals();
			return;
		}
		
		super.requestLayout();
	}

	@Override
	public boolean isLayoutRequested() {
		GLDecorView view = mGlViewGroup;
		if (view != null && view.mRequestingLayout) {
			return mLayoutRequested;
		}
		
		return super.isLayoutRequested();
	}
	
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (mTransfered) {
			return;
		}
		if (changed && mOverlayedViewGroup != null) {
			int oldLeft = mOverlayedViewGroup.getLeft();
			int oldTop = mOverlayedViewGroup.getTop();
			mOverlayedViewGroup.offsetLeftAndRight(left - oldLeft);
			mOverlayedViewGroup.offsetTopAndBottom(top + mTranslateY - oldTop);
		}
	}

	private void checkThread() {
		if (mMainThreadHashCode != 0 && mMainThreadHashCode != Thread.currentThread().hashCode()) {
			throw new RuntimeException(
					"Check Thread error: Only the original thread that created a view hierarchy can touch its views.");
		}
	}
	
	private void drawFrame() {
		
		startReadingBuffer();
		
		mTextureManager.handleDeleteTextures();

		final Rect dirty = mDirty;
		mIsAnimating = true; //TODO:如果注释这句会有残影，why？
		if (!dirty.isEmpty() || mIsAnimating) {
			mCanvas.setClearColorOnGLThread(0, 0, 0, mClearAlpha * OneOver255);
			//TODO:如果当前没有需要深度缓冲区的视图，则不需要清空深度缓冲区
			GLES20.glDepthMask(true); //为了清空深度缓冲区，需要临时启用和禁用写深度缓冲区
			GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT
					| GLES20.GL_STENCIL_BUFFER_BIT);
			dirty.setEmpty();
			mIsAnimating = false;
			if (SHOW_FPS) {
				boolean updated = mFpsCounter.computeFps(SystemClock.uptimeMillis());
				if (updated) {
					Log.i(FPS_TAG, "fps=" + mFpsCounter.getFps());
				}
			}

			resetGLConfig();

			//显示列表已转化为左儿子右兄弟的二叉树，使用堆栈实现其遍历
			final RenderInfoNode[] stack = mRenderInfoNodeStack;
			int count = 0;
			
			RenderData renderData;
			final long timeStamp;
			synchronized (mBufferLock) {
				renderData = mRenderDatas[mBufferReadingPtr];
				renderData.mReading = true;
				
				timeStamp = mBufferTimeStamp[mBufferReadingPtr];
				if (DBG) {
					Log.v(TAG, "draw frame " + timeStamp + " ================v " + GLContentView.this);
				}
			}
			VertexBufferBlock.startReadingVertexBuffer(renderData.mVertexBufferBlock);
			IndexBufferBlock.startReadingVertexBuffer(renderData.mIndexBufferBlock);
			RenderInfoNode rootNode = renderData.mRenderInfoNode;
			VertexBufferBlock.sWriteCountOnGLFrame = renderData.mVertexBufferWriteCount;
			IndexBufferBlock.sWriteCountOnGLFrame = renderData.mIndexBufferWriteCount;
			
			stack[count++] = rootNode;					//push
			while (count > 0) {
				RenderInfoNode node = stack[--count];	//pop
				stack[count] = null;
				node.mRenderable.run(timeStamp, node.mContext);

				//push right sibling first and will be pop last
				final RenderInfoNode nextNode = node.getNextNode();
				if (nextNode != null) {
					stack[count++] = nextNode;
				}

				//push left child last and will be pop first
				final RenderInfoNode forkNode = node.getForkNode();
				if (forkNode != null) {
					stack[count++] = forkNode;
				}
			}
			
			if (!mOnFrameRenderedActionQueue.isEmpty()) {
				queueEvent(mOnFrameRenderedActionCaller);
			}
			
			if (DBG) {
				Log.v(TAG, "draw frame " + timeStamp + " ================^");
			}
			
			VertexBufferBlock.finishReadingVertexBuffer();
			IndexBufferBlock.finishReadingVertexBuffer();
			synchronized (mBufferLock) {
				renderData.mReading = false;
			}
			finishReadingBuffer();
			
			checkQueues(false);
			
			
			if (ENABLE_SCREENSHOT) {
				if (mScreenshotInfo != null) {
					synchronized (mScreenshotLock) {
						if (mScreenshotInfo.count > 0) {
							queueEvent(mScreenshotInfo);
						}
					}
				}
			}
		}

	}
	
	/**
	 * <br>功能简述: 重设GL的配置
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void resetGLConfig() {
		GLES20.glColorMask(true, true, true, true);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glDepthMask(false);
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_STENCIL_TEST);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		
		GLCanvas.resetOnFrameStart();
		
		GLError.clearGLError();
	}

	public void setContentView(GLView view) {
		mGlViewGroup.removeAllViewsInLayout();
		mGlViewGroup.addView(view);
	}

	public void setContentView(GLView view, LayoutParams params) {
		mGlViewGroup.removeAllViewsInLayout();
		mGlViewGroup.addView(view, params);
	}

	public void addContentView(GLView view) {
		mGlViewGroup.addView(view);
	}

	public void addContentView(GLView view, LayoutParams params) {
		mGlViewGroup.addView(view, params);
	}
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意: 只返回第一个ContentView
	 * @return
	 */
	public GLView getContentView() {
		return mGlViewGroup.getChildAt(0);
	}

	/**
	 * 获取View兼容层
	 * @return
	 */
	public ViewGroup getOverlayedViewGroup() {
		return mOverlayedViewGroup;
	}

	/**
	 * 设置View兼容层
	 * @param viewGroup	应该不能为null，并且已经加到Activity的ContentView中
	 */
	public void setOverlayedViewGroup(ViewGroup viewGroup) {
		if (viewGroup == null) {
			throw new IllegalArgumentException("viewGroup cannot be null.");
		}
		mOverlayedViewGroup = viewGroup;
		mOverlayedViewGroup.setTag(OVERLAY_VIEWGROUP_TAG);
		mOverlayedViewGroup.setVisibility(View.GONE);
		/**
		 * @date 2013-06-14 by dengweiming
		 * 如果不设置大小，那么GLViewWrapper封装的EditText就不能获得backspace键
		 * 如果不够大，就显示不了选择文字的控件
		 */
		mOverlayedViewGroup.layout(0, 0, 4000, 4000);	//CHECKSTYLE IGNORE
	}

	/**
	 * 获取参考点（GLView视图根节点的左上角）在3D空间的位置
	 * @return	数组[x, y, z]，注意不要修改里面的内容
	 */
	@Deprecated
	public final float[] getRefPosition() {
		return mRefPosition;
	}

	/**
	 * 获取指定z处的截面区域（相对于参考点）
	 * @param z		相对于参考点的z
	 * @param res	存放[left, top, right, bottom]
	 */
	@Deprecated
	public void getFrustumZPlane(float z, float[] res) {
		float k = 1 + z / mRefPosition[2];
		res[0] = -getWidth() / 2 * k - mRefPosition[0];
		res[1] = getHeight() / 2 * k - mRefPosition[1];
		res[2] = getWidth() / 2 * k - mRefPosition[0];
		res[3] = -getHeight() / 2 * k - mRefPosition[1];	//CHECKSTYLE IGNORE
	}

	@Deprecated
	public float getHalfFovX() {
		return mHalfFovX;
	}

	@Deprecated
	public float getHalfFovY() {
		return mHalfFovY;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (!mEventsEnabled || mEventsToken) {
			return true;
		}
		mTouchRayInvalidated = true;
		mTouchX = event.getX();
		mTouchY = event.getY();
		if (mTranslateY != 0) {
			event.setLocation(mTouchX, mTouchY -= mTranslateY);
		}
		if (mGlViewGroup == null) {
			return super.dispatchTouchEvent(event);
		}
		return mGlViewGroup.dispatchTouchEvent(event);
	}
	
	Ray getTouchRayInWorld() {
		if (mTouchRayInvalidated) {
			mTouchRayInvalidated = false;
			final float[] cp = TEMP_VECTOR;
			GeometryPools.saveStack();
			mCanvas.getCameraWorldPosition(cp);
			com.go.gl.math3d.Point p = GeometryPools.acquirePoint().set(cp[0], cp[1], cp[2]);
			com.go.gl.math3d.Point q = GeometryPools.acquirePoint().set(mTouchX, -mTouchY, 0);
			mTouchRay.set(p, q);
			mTouchRay.startCast();
			GeometryPools.restoreStack();
		}
		return mTouchRay;
	}

	public final GLView findGLViewById(int id) {
		return mGlViewGroup.findViewById(id);
	}

	public final GLView findGLViewWithTag(Object tag) {
		return mGlViewGroup.findViewWithTag(tag);
	}

	/**
	 * 设置主线程的绘制时间，以统一动画步调
	 * @param drawingTime
	 * @return 如果返回false，那么需要调用 invalidate 来更新时间
	 */
	private boolean setMainThreadDrawingTime(long drawingTime) {
		if (mDrawingTimeField != null && mAttachInfoObject != null) {
			try {
				mDrawingTimeField.setLong(mAttachInfoObject, drawingTime);
			} catch (IllegalArgumentException e) {
				return false;
			} catch (IllegalAccessException e) {
				return false;
			}
			return true;
		}
		return false;

	}

	@Override
	protected void onAttachedToWindow() {
		if (DBG) {
			Log.d(TAG, "onAttachedToWindow " + this);
		}
		if (mTransfered) {
			return;
		}
		super.onAttachedToWindow();
		
		GLContentView oldGLViewRoot = sGLViewRoot;
		sGLViewRoot = this;
		if (oldGLViewRoot != null && oldGLViewRoot != this) {
			oldGLViewRoot.onDestroy();
		}
		
		mAttachInfo.mHandler = getHandler();
		mAttachInfo.mWindowVisibility = getWindowVisibility();

		if (!mDisableAttachGlViewGroup) {
			mGlViewGroup.dispatchAttachedToWindow(mAttachInfo, 0);
		}
		mDisableAttachGlViewGroup = false;

		//获取主线程的 mAttachInfo 对象以及其 mDrawingTime 的域
		mAttachInfoObject = null;
		mDrawingTimeField = null;
		try {
			Field attachInfoField;
			attachInfoField = View.class.getDeclaredField("mAttachInfo");
			attachInfoField.setAccessible(true);
			Object attachInfo = attachInfoField.get(this);
			attachInfoField.setAccessible(false);
			if (attachInfo != null) {
				@SuppressWarnings("rawtypes")
				Class attachInfoClass = attachInfoField.getType();
				Field drawingTimeField = attachInfoClass.getDeclaredField("mDrawingTime");
				drawingTimeField.setAccessible(true);

				mAttachInfoObject = attachInfo;
				mDrawingTimeField = drawingTimeField;
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		if (DBG) {
			Log.d(TAG, "onDetachedFromWindow " + this);
		}
		if (mTransfered) {
			return;
		}
		super.onDetachedFromWindow();
		if (mGlViewGroup != null && mGlViewGroup.mAttachInfo != null) {
			mGlViewGroup.dispatchDetachedFromWindow();
		}
		
		mAttachInfoObject = null;
		mDrawingTimeField = null;
	}
	
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		if (DBG) {
			Log.d(TAG, "GLContentView onWindowVisibilityChanged: visibility="
					+ visibility + " waitForSizeChanged=" + mWaitForSizeChanged);
		}
		if (mTransfered) {
			return;
		}
		if (!mWaitForSizeChanged) {
			mWindowVisibilityToSet = -1;
			super.onWindowVisibilityChanged(visibility);
		} else {
			mWindowVisibilityToSet = visibility;
		}
	}
	
	@Override
	public void setVisibility(int visibility) {
		if (DBG) {
			Log.d(TAG, "setVisibility " + visibility, new Throwable());
		}
		super.setVisibility(visibility);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (mTransfered) {
			return;
		}
		super.onWindowFocusChanged(hasWindowFocus);
		if (mGlViewGroup != null) {
			mGlViewGroup.dispatchWindowFocusChanged(hasWindowFocus);
		}
	}

	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		if (mGlViewGroup != null) {
			return mGlViewGroup.dispatchKeyEventPreIme(event);
		}
		return false;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (mGlViewGroup != null) {
			return mGlViewGroup.dispatchKeyEvent(event);
		}
		return false;
	}
	@Override
	public boolean dispatchKeyShortcutEvent(KeyEvent event) {
		if (mGlViewGroup != null) {
			return mGlViewGroup.dispatchKeyShortcutEvent(event);
		}
		return false;
	}

	@Override
	public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
		if (mGlViewGroup != null) {
			return mGlViewGroup.dispatchPopulateAccessibilityEvent(event);
		}
		return false;
	}

	@Override
	protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
		super.dispatchRestoreInstanceState(container);
		if (mGlViewGroup != null) {
			mGlViewGroup.dispatchRestoreInstanceState(container);
		}
	}

	@Override
	protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
		super.dispatchSaveInstanceState(container);
		if (mGlViewGroup != null) {
			mGlViewGroup.dispatchSaveInstanceState(container);
		}
	}

	@Override
	public boolean dispatchTrackballEvent(MotionEvent event) {
		if (mGlViewGroup != null) {
			return mGlViewGroup.dispatchTrackballEvent(event);
		}
		return false;
	}

	@Override
	public GLViewParent getGLParent() {
		return null;
	}

	@Override
	public void requestChildFocus(GLView child, GLView focused) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearChildFocus(GLView child) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getChildVisibleRect(GLView child, Rect r, Point offset) {
		ViewParent p = getParent();
		return p == null || p.getChildVisibleRect(this, r, offset);
	}

	@Override
	public GLView focusSearch(GLView v, int direction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void bringChildToFront(GLView child) {
		// TODO Auto-generated method stub

	}

	@Override
	public void focusableViewAvailable(GLView v) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean showContextMenuForChild(GLView originalView) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void childDrawableStateChanged(GLView child) {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean requestChildRectangleOnScreen(GLView child, Rect rectangle, boolean immediate) {
//		Log.d("DWM", "============requestChildRectangleOnScreen " + rectangle);
		
		return requestRectangleOnScreen(rectangle, immediate);
		
	}

	public void invalidateChild(GLView child, Rect dirty) {
		//TODO 部分更新
		//checkThread();
		//if (DEBUG_DRAW) Log.v(TAG, "Invalidate child: " + dirty);
		//if (mCurScrollY != 0 || mTranslator != null) {
		//    mTempRect.set(dirty);
		//    dirty = mTempRect;
		//    if (mCurScrollY != 0) {
		//       dirty.offset(0, -mCurScrollY);
		//    }
		//    if (mTranslator != null) {
		//        mTranslator.translateRectInAppWindowToScreen(dirty);
		//    }
		//    if (mAttachInfo.mScalingRequired) {
		//        dirty.inset(-1, -1);
		//    }
		//}
		mDirty.union(dirty);
		
		if (GLView.DBG_INVALIDATE) {
			Log.v(GLView.INVALIDATE_LOG_TAG, "GLContentView invalidate: dirty=" + mDirty
					+ " mWillDrawSoon=" + mWillDrawSoon);
		}
		
		if (!mWillDrawSoon) {
			mWillDrawSoon = true;
			scheduleTraversals();
		}

	}
	
	final static long INTERVAL_IN_MILLI = 14L;
	final static long NANO_PER_MILLI = 1000000L;
	final static long INTERVAL_IN_NANO = INTERVAL_IN_MILLI * NANO_PER_MILLI;
	final static long POST_DELAY_IN_MILLI = 4;

	boolean mDisablePredraw;
	boolean mDrawCancled;
	boolean mWillDrawSoon;
	long mTraversalsTime;
	boolean mLayoutRequested;
	boolean mSizeChanged;
	volatile boolean mQueueActionPosted;
	boolean mPauseActionPosted;
	
	//为避免进入其他程序切换横竖屏后回来会造成画面混乱而增加的变量
	boolean mWaitForSizeChanged;		//当横竖屏切换后需要等待大小改变的事件
	int mWindowVisibilityToSet = -1;	//延迟响应窗口可见性事件的可见性参数
	
	//为避免surfaceChanged多余调用而增加的变量
	int mLastSurfaceWidth = -1;
	int mLastSurfaceHeight = -1;
	int mLastSurfaceFormat = -1;
	
	private final Runnable mQueueAction = new Runnable() {
		
		@Override
		public void run() {
			mQueueActionPosted = false;
			Texture.processLoadedTextures();
			sCleanUpQueue.process(sCleanUpProcessor);
			TextureRecycler.doRecycle();
			BitmapRecycler.doRecycle();
			
			if (TextureRecycler.needToDoRecycle() || BitmapRecycler.needToDoRecycle()) {
				scheduleTraversals();
			}
			
		}
	};

	private final Runnable mRequestRenderAction = new Runnable() {
		@Override
		public void run() {
			if (!mRunning) {
				return;
			}

			if (!checkRequestRender()) {
				postDelayed(this, POST_DELAY_IN_MILLI);
			}
		}

	};
	
	private final Runnable mPauseAction = new Runnable() {
		@Override
		public void run() {
			if (mPauseActionPosted) {
				//这个runnable对象是通过postOnFrameRendered来异步post的，removeCallbacks时
				//可能还没post，所以之后还会可能执行，需要通过额外的变量来过滤
				mPauseActionPosted = false;
				onPause();
			}
		}
	};

	private final Runnable mTraversalsAction = new Runnable() {
		@Override
		public void run() {
			if (!mRunning) {
				return;
			}

			final long nanoTime = System.nanoTime();
			if (nanoTime - mTraversalsTime < INTERVAL_IN_NANO) {
				final long delayTime = (mTraversalsTime + INTERVAL_IN_NANO - nanoTime
						+ NANO_PER_MILLI - 1) / NANO_PER_MILLI;
				postDelayed(this, delayTime);
				return;
			}
			if (!isPreparedForTraversal() || !startWritingBuffer()) {
				postDelayed(this, POST_DELAY_IN_MILLI);
				return;
			}
			if (DBG) {
				Log.v(TAG, "performTraversals==========v " + GLContentView.this);
			}
			mTraversalScheduled = false;
			mTraversalsTime = nanoTime;
			mWillDrawSoon = false;
			mDrawCancled = false;
			if (THREAD_SEQUENCE_CALL_LOCK != null) {
				synchronized (THREAD_SEQUENCE_CALL_LOCK) {
					performTraversals();
				}
			} else {
				performTraversals();
			}
			if (DBG) {
				Log.v(TAG, "performTraversals==========^ " + GLContentView.this);
			}

		}
	};

	private void performTraversals() {
		if (mGlViewGroup == null) {
			return;
		}
		
		final int width = getWidth();
		final int height = getHeight();
		if (mGlViewGroup.mAttachInfo != null) {
			if (mSizeChanged) {
				mSizeChanged = false;
				if (mGlViewGroup.getWidth() != width
						|| mGlViewGroup.getHeight() != height) {
					mLayoutRequested = true;
				}
				mCanvas.setWindowSize(width, height);
				mCanvas.setDefaultViewportFrustum(width, height);
				mCanvas.setWorldPosition(0, mTranslateY);
			}
			
			if (mTranslateY != mRequestedTranslateY) {
				mTranslateY = mRequestedTranslateY;
				mCanvas.setWorldPosition(0, mTranslateY);
				mAttachInfo.mTranslateY = mTranslateY;
				mRefPosition[1] = getHeight() * HALF - mTranslateY;
				mLayoutRequested = true;
			}
			final boolean relayout = mLayoutRequested;
			mLayoutRequested = false;

			if (relayout) {
				final int height2 = height - Math.abs(mTranslateY);
				final int widthMeasureSpec = MeasureSpec
						.makeMeasureSpec(width, MeasureSpec.EXACTLY);
				final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(height2,
						MeasureSpec.EXACTLY);
				//Log.d("DWM", "relayout ================w=" + width + " h=" + height);
				mGlViewGroup.measure(widthMeasureSpec, heightMeasureSpec);
				mGlViewGroup.layout(0, 0, width, height2);	//左上角设为(0, 0)，可以让后继的getLocationOnWindow结果为(0, 0)
				
				if (mOverlayedViewGroup != null) {
					//为了保证封装的2D视图（View实例）在计算屏幕位置时获得正确的结果，
					//要保证mOverlayedViewGroup的top位置和mGlViewGroup绘制时一致
					final int oldTop = mOverlayedViewGroup.getTop();
					final int newTop = mTranslateY + getTop();
					mOverlayedViewGroup.offsetTopAndBottom(newTop - oldTop);
				}
			}
		}

		if (!mDisablePredraw) {
			ViewTreeObserver observer = getViewTreeObserver();
			mDrawCancled = observer != null && observer.dispatchOnPreDraw();
			if (mDrawCancled) {
				if (DBG) {
					Log.w(TAG, "=======cancle draw");
				}
				mTraversalsTime = -1;
				mTraversalScheduled = true;
				post(mTraversalsAction);
				return;
			}
		}
		//FIXME: else的情况，有可能在横竖屏切换的时候有TextView需要onPreDraw，那么会绘制不正确

		RenderData renderData;
		synchronized (mBufferLock) {
			renderData = mRenderDatas[mBufferWritingPtr];
			if (renderData.mReading) {
				throw new RuntimeException("try writing renderData while reading");
			}
		}
		RenderInfoNode savedNode = mCanvas.startRootDisplayList(renderData.mRenderInfoNode);
		VertexBufferBlock.startWritingVertexBuffer(renderData.mVertexBufferBlock);
		IndexBufferBlock.startWritingVertexBuffer(renderData.mIndexBufferBlock);
		
		final int saveCount = mCanvas.save();
		mDrawing = true;
		try {
			if (mOnFrameRenderingListenerCount > 0) {
				for (int i = 0; i < mOnFrameRenderingListenerCount; ++i) {
					mCanvas.addRenderable(mOnFrameRenderingListeners[i], null);
					mOnFrameRenderingListeners[i] = null;
				}
				mOnFrameRenderingListenerCount = 0;
			}
			
			final long uptime = SystemClock.uptimeMillis();
			if (mResumed) {
				mResumed = false;
				mAdjustTime = uptime - mDrawingTime;
			}
			// 帧率大于60帧的时候mDrawingTime改为均匀update
			long deltaTime = uptime - mAdjustTime - mDrawingTime;
//			deltaTime = deltaTime > SYSTEMTIME_UPDATE_MIN_INTERVAL
//					? deltaTime
//					: DRAWINGTIME_UPDATE_NORMAL_INTERVAL;
			deltaTime = mFrameTracker.computeFrameTime(deltaTime);
			mDrawingTime += deltaTime;
			mAdjustTime = uptime - mDrawingTime;
			Timer.setTime(mContextHashCode, mDrawingTime);
			//mDrawingTime = uptime;
			mAttachInfo.mDrawingTime = mDrawingTime;
//			AnimationUtils.sDrawingTime = mDrawingTime;
			if (!setMainThreadDrawingTime(mDrawingTime)) {
				//			postInvalidate();
			}
			
//			Log.d("DWM", "draw===================v " + mAttachInfo.mDrawingTime + " duration=" + ((uptime - mLastUptime)));
			mCanvas.mDeltaDrawingTime = deltaTime;
			mCanvas.mDrawingTime = mDrawingTime;
			
			AnimationHandler animationHandler = ValueAnimator.sAnimationHandler;
			ValueAnimator.sCurrentTime = mDrawingTime;
			animationHandler.run();
			if (animationHandler.isScheduled()) {
				scheduleTraversals();
			}
			
			mGlViewGroup.draw(mCanvas);
			mCanvas.restoreToCount(saveCount);
			mCanvas.finishDisplayList(savedNode);
			mCanvas.mLastFrameDropped = false;
			mAttachInfo.mIgnoreDirtyState = false; // TODO:设为true的时机？
			renderData.mVertexBufferWriteCount = VertexBufferBlock.sWriteCount;
			renderData.mIndexBufferWriteCount = IndexBufferBlock.sWriteCount;
		} finally {
		}
		mDrawing = false;
		finishWritingBuffer();
	}
	
	private void checkQueues(boolean postDelayed) {
		if (mQueueActionPosted) {
			return;
		}
		/** @formatter:off */
		final boolean queuing = Texture.needToProcessLoadedTextures()
				|| BitmapRecycler.needToDoRecycle()
				|| TextureRecycler.needToDoRecycle() 
				|| !sCleanUpQueue.isEmpty();
		/** @formatter:on */
		if (queuing) {
			mQueueActionPosted = true;
			if (postDelayed) {
				postDelayed(mQueueAction, INTERVAL_IN_MILLI);
			} else {
				post(mQueueAction);
			}
		}
	}

	private void scheduleTraversals() {
		if (!mTraversalScheduled) {
			mTraversalScheduled = true;
			if (DBG) {
				Log.v(TAG, "scheduleTraversals");
			}
			post(mTraversalsAction);
		} else {
			if (DBG) {
				Log.v(TAG, "scheduleTraversals ignore");
			}
		}
	}

	private void unscheduleTraversals() {
		if (mTraversalScheduled) {
			mTraversalScheduled = false;
			removeCallbacks(mTraversalsAction);
		}
	}

	public GLViewParent invalidateChildInParent(final int[] location, final Rect dirty) {
		invalidateChild(null, dirty);
		return null;
	}

	class GLRenderer implements Renderer {	//CHECKSTYLE IGNORE

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			mGLThreadHashCode = Thread.currentThread().hashCode();
			if (DBG) {
				Log.i(TAG, "onSurfaceCreated------------------------------------v thread="
						+ mGLThreadHashCode + " " + GLContentView.this);
			}
			mGLThreadUncaughtExceptionHandler.attachToThread();
			
			mGLES20Supported = mGlConfigChooser != null && mGlConfigChooser.isGLES20Supported();
			if (!mGLES20Supported) {
				final SurfaceViewOwner owner = mSurfaceViewOwner;
				post(new Runnable() {
					public void run() {
						owner.handleGLES20UnsupportedError();
					}
				});
				Log.w("NextLauncher", "OpenGL ES 2.0 is NOT supported by your hardware! Prepare to exit...");
				Thread.currentThread().interrupt();
				return;
			}

			GLError.clearGLError();
			GLES20.glEnable(GLES20.GL_CULL_FACE);
			GLES20.glCullFace(GLES20.GL_BACK);
			GLES20.glDepthFunc(GLES20.GL_LEQUAL);
			//    		GLES20.glEnable(GLES20.GL_TEXTURE_2D);		//默认启用纹理贴图
			GLES20.glEnable(GLES20.GL_BLEND); //默认启用颜色混合
			GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA); //默认使用premutiply-alpha的颜色格式

			//获取当前配置
			int[] params = new int[1];
			GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, params, 0);
			mMaxTexureSize = params[0];
			mColorBits = mGlConfigChooser == null ? 0 : mGlConfigChooser.getColorBits();
			mDepthBits = mGlConfigChooser == null ? 16 : mGlConfigChooser.getDepthBits();	//CHECKSTYLE IGNORE
			mStencilBits = mGlConfigChooser == null ? 0 : mGlConfigChooser.getStencilBits();
			if (mColorBits >= 24) {	//CHECKSTYLE IGNORE
				GLES20.glDisable(GLES20.GL_DITHER);
			} else {
				GLES20.glEnable(GLES20.GL_DITHER);
			}
			//默认禁用深度测试和写深度缓冲区
			GLES20.glDepthMask(false);
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);

			GLES20.glClearStencil(0);
			GLError.clearGLError();

			TextureManager.onGLContextLostStatic();
			++mSurfaceCreateCount;
			if (DBG) {
				Log.i(TAG, "onSurfaceCreated------------------------------------^");
			}
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			if (DBG) {
				Log.i(TAG, "onSurfaceChanged------------------------------------v thread="
						+ mGLThreadHashCode + " w=" + width + " h=" + height);
			}
//			GLES20.glViewport(0, 0, width, height);
			removeCallbacks(mRequestRenderAction);
			forceReadCurrentFrame();
			if (DBG) {
				Log.i(TAG, "onSurfaceChanged------------------------------------^");
			}
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			if (THREAD_SEQUENCE_CALL_LOCK != null) {
				synchronized (THREAD_SEQUENCE_CALL_LOCK) {
					drawFrame();
				}
			} else {
				drawFrame();
			}
		}
	}

	private static final int CLEANUP_QUEUE_LIMIT = 1024;
	private static FastQueue<ICleanup> sCleanUpQueue = new FastQueue<ICleanup>(CLEANUP_QUEUE_LIMIT);
	private static CleanUpProcessor sCleanUpProcessor = new CleanUpProcessor();
	private static class CleanUpProcessor implements FastQueue.Processor<ICleanup> {	//CHECKSTYLE IGNORE
		@Override
		public void process(ICleanup object) {
			if (object != null) {
				object.cleanup();
			}
		}
	}

	public static View getStaticView() {
		if (sView == null) {
			throw new RuntimeException("static view is not created");
		}
		return sView;
	}

	/**
	 * <br>功能简述: 创建静态的视图
	 * <br>功能详细描述:用于在其他非视图模块共享某些信息，以及视图树还没装载到窗口时保证能使用post方法
	 * <br>注意:
	 * @param context
	 */
	public static void createStaticView(Context context) {
		if (sView == null) {
			sView = new View(context.getApplicationContext());
		}
	}
	
	public static boolean postStatic(Runnable action) {
		if (sGLViewRoot != null) {
			return sGLViewRoot.post(action);
		}
		if (sView != null) {
			return sView.post(action);
		}
		return false;
	}

	public static boolean postDelayedStatic(Runnable action, long delayMillis) {
		if (sGLViewRoot != null) {
			return sGLViewRoot.postDelayed(action, delayMillis);
		}
		if (sView != null) {
			return sView.postDelayed(action, delayMillis);
		}
		return false;
	}

	public static boolean removeCallbacksStatic(Runnable action) {
		if (sGLViewRoot != null) {
			return sGLViewRoot.removeCallbacks(action);
		}
		if (sView != null) {
			return sView.removeCallbacks(action);
		}
		return false;
	}

	/**
	 * 实际上还是post到主线程的，使用postStatic代替
	 * @deprecated 
	 * */
	public static boolean postToGLThread(Runnable action) {
		return postStatic(action);
	}

	/**
	 * 实际上还是post到主线程的，使用postDelayedStatic代替
	 * @deprecated
	 */
	public static boolean postToGLThreadDelayed(Runnable action, long delayMillis) {
		return postDelayedStatic(action, delayMillis);
	}

	/**
	 * 实际上还是在主线程上remove的，使用removeCallbacksStatic代替
	 * @deprecated
	 */
	public static boolean removeCallback(Runnable action) {
		return removeCallbacksStatic(action);
	}
	
	/**
	 * <br>功能简述: 清除缓存的GLDrawable（即使用{@link GLCanvas#drawDrawable(Drawable)}时缓存起来的那些）
	 * <br>功能详细描述:
	 * <br>注意: 在必要时使用，例如更换主题
	 */
	public static void clearCachedGLDrawables() {
		if (sGLViewRoot != null) {
			sGLViewRoot.mCanvas.cleanup();
		}
	}

	public static void requestCleanUp(ICleanup object) {
		sCleanUpQueue.pushBack(object);
	}

	/**
	 * 是否32位颜色格式
	 * @return
	 */
	public boolean isTranslucent() {
		return mTranslucent;
	}

	/**
	 * 切换16位和32位颜色格式
	 * @param translucent
	 */
	public void changePixelFormat(boolean translucent) {
		if (DBG) {
			Log.i(TAG, "changePixelFormat========= " + mTranslucent + " -> " + translucent + " "
					+ this);
		}
		if (mTranslucent == translucent) {
			return;
		}
		if (mTransfered) {
			return;
		}
		final boolean isRunning = mRunning;
		if (isRunning) {
			onPause();
		}
		
		if (translucent) {
			mGlConfigChooser.setConfigure(8, 16, 4);	//CHECKSTYLE IGNORE
			getHolder().setFormat(PixelFormat.TRANSLUCENT);
			if (mClearAlpha == FullAlpha) {
				mClearAlpha = 0;	// 如果现在已经使用setBackgroundAlpha设成其他值，那么由外部还原
			}
		} else {
			mGlConfigChooser.setConfigure(0, 16, 4);	//CHECKSTYLE IGNORE
			getHolder().setFormat(PixelFormat.OPAQUE);
			if (mClearAlpha == 0) {
				mClearAlpha = FullAlpha;
			}
		}
		mTranslucent = translucent;
		mCanvas.setBackgroundClearColor(mClearAlpha << 24);	//CHECKSTYLE IGNORE

		if (isRunning) {
			onResume();
		}
	}

	/**
	 * 作为{@link GLCanvas#drawDrawable(Drawable)}参数被调用过的Drawable对象需要通过本方法来释放引用
	 * @param drawable
	 */
	public void releaseDrawableReference(Drawable drawable) {
		mCanvas.releaseDrawableReference(drawable);
	}

	public int getViewportWidth() {
		return mCanvas.getViewportWidth();
	}

	public int getViewportHeight() {
		return mCanvas.getViewportHeight();
	}

	/**
	 * <br>功能简述: 计算指定深度的物体投影到屏幕后的缩放比例
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param depth 指定深度，和Z轴同向，即垂直屏幕向外为正方向
	 * @return
	 */
	public float getProjectScale(float depth) {
		return mCanvas.getProjectScale(depth);
	}
	
	/**
	 * <br>功能简述: 计算深度，使得该深度的物体投影到屏幕后的缩放比例为指定比例
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param scale 指定比例
	 * @return 深度，和Z轴同向，即垂直屏幕向外为正方向
	 */
	public float getDepthForProjectScale(float scale) {
		return mCanvas.getDepthForProjectScale(scale);
	}
	
	public float getCameraZ() {
		return mCanvas.getCameraZ();
	}

	/**
	 * 获取相机的世界坐标
	 * @param pos 长度为3
	 */
	public void getCameraWorldPosition(float[] pos) {
		mCanvas.getCameraWorldPosition(pos);
	}

	/**
	 * 将世界坐标点投影到参考平面
	 * @param worldX
	 * @param worldY
	 * @param worldZ
	 * @param res 长度为2
	 */
	public void projectFromWorldToReferencePlane(float worldX, float worldY, float worldZ,
			float[] res) {
		mCanvas.projectFromWorldToReferencePlane(worldX, worldY, worldZ, res);
	}

	/**
	 * 将参考平面上的点反投影到指定z平面上
	 * @param planeX
	 * @param planeY
	 * @param dstZ
	 * @param res 长度为2
	 */
	public void unprojectFromReferencePlane(float planeX, float planeY, float dstZ, float[] res) {
		mCanvas.unprojectFromReferencePlaneToWorld(planeX, planeY, dstZ, res);
	}
	
	public boolean isRunning() {
		return mRunning;
	}
	
	private boolean isPreparedForTraversal() {
		if (!mPreparedForTraversal) {
			//当已经onAttachedToWindow以及onSizeChanged了才开始绘图，避免绘制了黑屏
			mPreparedForTraversal = sGLViewRoot == this && getWidth() > 0;
		}
		return mPreparedForTraversal;
	}

	@Override
	public void onPause() {
		if (DBG) {
			Log.d(TAG, "GLContentView onPause " + this);
		}
		removeCallbacks(mRequestRenderAction);
		removeCallbacks(mTraversalsAction);
		removeCallbacks(mQueueAction);
		mQueueActionPosted = false;
		if (mPauseActionPosted) {
			mPauseActionPosted = false;
			removeCallbacks(mPauseAction);
		}
		mRunning = false;
		super.onPause();
	}

	@Override
	public void onResume() {
		if (DBG) {
			Log.d(TAG, "GLContentView onResume " + this);
		}
		
		if (mPauseActionPosted) {
			mPauseActionPosted = false;
			removeCallbacks(mPauseAction);
		}
		
		if (mGLThreadUncaughtExceptionHandler.mExceptionCaught) {
			mGLThreadUncaughtExceptionHandler.mExceptionCaught = false;
			mSurfaceInvalid = true;
			onPause();
			//本方法是在GLSurfaceView.dispatchDraw里面调用的，为了避免出问题，使用post
			post(new Runnable() {
				@Override
				public void run() {
					if (mSurfaceViewOwner != null) {
						mSurfaceViewOwner.reCreateSurfaceView();
					}
				}
			});
			if (DBG) {
				Log.w(TAG, "GLContentView onResume: surface is not valid, recreate surfaceView.");
			}
			return;
		}
		
		if (!mRunning) {
			mResumed = true;
			mRunning = true;
			TextureManager.getInstance().notifyAllInvalidated();
			TextureRecycler.clearQueue();

			if (!mTransfered) {
				startResetingBuffer();
				forceTraversal();
				finishResetingBuffer();
			}
		}
		super.onResume();
	}
	
	public void onDestroy() {
		if (DBG) {
			Log.d(TAG, "GLContentView onDestroy " + this);
		}

		if (mPauseActionPosted) {
			mPauseActionPosted = false;
			removeCallbacks(mPauseAction);
		}
		removeCallbacks(mRequestRenderAction);
		removeCallbacks(mTraversalsAction);
		removeCallbacks(mQueueAction);
		mQueueActionPosted = false;
		
		if (mRunning) {
			super.onPause();
		}
		
		mRunning = false;
		mEventsEnabled = false;
		mEventsToken = false;

		mRenderer = null;
		mRendererWrapper.setRenderer(null);
				
		mOverlayedViewGroup = null;

		mGlConfigChooser = null;

		Arrays.fill(mRenderInfoNodeStack, null);

		mAttachInfoObject = null;
		mDrawingTimeField = null;
		
		Arrays.fill(mOnFrameRenderingListeners, null);
		mOnFrameRenderedActionQueue.cleanup();
		
		mSurfaceViewOwner = null;
		mGLThreadUncaughtExceptionHandler.mContentViewRef.clear();
		
		
		if (mTransfered) {
			return;
		}
		mTransfered = true;
		
		if (mAttachInfo != null) {
			mAttachInfo.mViewProxy = null;
			mAttachInfo.mHandler = null;
			mAttachInfo = null;
		}
		
		if (mCanvas != null) {
			mCanvas.cleanup();
			mCanvas = null;
		}
		
		if (sGLViewRoot == this) {
			//处理静态的内容
			ValueAnimator.sAnimationHandler.cleanup();
			sCleanUpQueue.process(sCleanUpProcessor);
			sCleanUpQueue.cleanup();
			BitmapRecycler.clearQueue();
			TextureRecycler.clearQueue();
			if (mTextureManager != null) {
				mTextureManager.cleanup();
				mTextureManager = null;
			}
			BitmapTexture.onDestroyStatic();

			//		sView = null;	// sView使用的是ApplicationContext，没必要释放，避免引起空指针问题以及post操作执行不到
			sGLViewRoot = null;

			if (GLView.sTags != null) {
				GLView.sTags.clear();
				GLView.sTags = null;
			}

			//清理View.sTags
			try {
				Field sTagsField = View.class.getDeclaredField("sTags");	//TODO: W/System.err(8074): java.lang.NoSuchFieldException: sTags
				sTagsField.setAccessible(true);
				try {
					@SuppressWarnings("unchecked")
					WeakHashMap<View, SparseArray<Object>> sTags = (WeakHashMap<View, SparseArray<Object>>) sTagsField.get(null);
					if (sTags != null) {
						sTags.clear();
						sTagsField.set(null, null);
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		
		//耗时操作放到最后，避免超时会执行不到
		final RenderData[] renderDatas;
		synchronized (mBufferLock) {
			renderDatas = mRenderDatas;
			mRenderDatas = null;
		}
		
		final GLViewGroup glViewGroup = mGlViewGroup;
		mGlViewGroup = null;
		
		if (renderDatas != null) {
			synchronized (mBufferLock) {
				for (int i = 0; i < BUFFER_COUNT; ++i) {
					RenderData data = renderDatas[i];
					data.reset();
				}
			}
		}
		
		if (glViewGroup != null) {
			glViewGroup.cleanup();
		}
	}
	
	
	/**
	 * 从另一个GLContentView中迁移
	 * @param src
	 */
	public void transferFrom(GLContentView src) {
		if (DBG) {
			Log.d(TAG, "GLContentView transfer " + src + " -> " + this);
		}
		
		if (src == null) {
			return;
		}
		
		if (src.mTransfered) {
			return;
		}
		
		if (src.isRunning()) {
			src.onPause();
		}

		//释放自己的
		mAttachInfo.mViewProxy = null;
		mGlViewGroup.assignParent(null);

		mOverlayedViewGroup = src.mOverlayedViewGroup;
		mAttachInfo = src.mAttachInfo;
		mAttachInfo.mViewProxy = this;
		mGlViewGroup = src.mGlViewGroup;
		mGlViewGroup.assignParent(null);
		mGlViewGroup.assignParent(this);
		mTextureManager = src.mTextureManager;
		mCanvas = src.mCanvas;
		mRenderDatas = src.mRenderDatas;

		//避免切换时 mGlViewGroup 多余的挂载
		if (mGlViewGroup.mAttachInfo != null) {
			mDisableAttachGlViewGroup = true;
		}

		synchronized (src.mBufferLock) {
			System.arraycopy(src.mBufferTimeStamp, 0, mBufferTimeStamp, 0, mBufferTimeStamp.length);
			System.arraycopy(src.mBufferState, 0, mBufferState, 0, mBufferState.length);
			mWriteTimeStamp = src.mWriteTimeStamp;
			mReadTimeStamp = src.mReadTimeStamp;
			mBufferWritingPtr = src.mBufferWritingPtr;
			mBufferReadingPtr = src.mBufferReadingPtr;
			mForceReadFrame = src.mForceReadFrame;
		}
		
		mTranslateY = mRequestedTranslateY = src.mRequestedTranslateY;
		layout(src.getLeft(), src.getTop(), src.getRight(), src.getBottom());
		mSizeChanged = false;
		
		//清除src对已迁移的成员的引用
		src.mGlViewGroup = null;
		src.mAttachInfo = null;
		src.mTextureManager = null;
		src.mCanvas = null;
		src.mRenderDatas = null;
		
		src.mTransfered = true;

	}
	
	//等待渲染完毕后执行的操作相关代码----------------------------------------------------------v
	
	//CHECKSTYLE IGNORE 1 LINES
	private class OnFrameRenderingListener implements Renderable {
		Runnable mAction;
		
		@Override
		public void run(long timeStamp, RenderContext context) {
			if (mAction != null) {
				mOnFrameRenderedActionQueue.pushBack(mAction);	//放到队列里，等下一帧渲染时再处理
				mAction = null;
			}
		}
		
	}
	
	//非绘制期间调用的 postOnFrameRendered 添加的 OnFrameRenderingListener 对象
	private static final int ON_FRAME_RENDERING_LISTENER_MAX_COUNT = 16;
	OnFrameRenderingListener[] mOnFrameRenderingListeners = new OnFrameRenderingListener[ON_FRAME_RENDERING_LISTENER_MAX_COUNT];
	private int mOnFrameRenderingListenerCount;
	
	private static final int ON_FRAME_RENDERED_ACTION_MAX_COUNT = 32;
	FastQueue<Runnable> mOnFrameRenderedActionQueue = new FastQueue<Runnable>(ON_FRAME_RENDERED_ACTION_MAX_COUNT);
	Processor<Runnable> mOnFrameRenderedActionProcessor = new Processor<Runnable>() {

		@Override
		public void process(Runnable object) {
			if (object != null) {
				post(object);
			}
		}
	};
	
	Runnable mOnFrameRenderedActionCaller = new Runnable() {
		@Override
		public void run() {
			mOnFrameRenderedActionQueue.process(mOnFrameRenderedActionProcessor);
		}
	};
	
	/**
	 * <br>功能简述:在本帧渲染完成后执行操作
	 * <br>功能详细描述:
	 * <br>注意:因为绘制和渲染是异步的，因此在跳转到其他Activity等情况下，需要等待渲染完成后才能执行这些操作。
	 * 需要在主线程上执行本方法，如果在另外的线程调用，需要用post方法来调用本方法。
	 * @param runnable
	 * @return
	 */
	public boolean postOnFrameRendered(Runnable runnable) {
		checkThread();
		OnFrameRenderingListener listener = new OnFrameRenderingListener();
		listener.mAction = runnable;
		if (mDrawing) {
			mCanvas.addRenderable(listener, null);
		}
		else {
			if (mOnFrameRenderingListenerCount >= ON_FRAME_RENDERING_LISTENER_MAX_COUNT) {
				throw new RuntimeException("postOnFrameRendered called exceed "
						+ ON_FRAME_RENDERING_LISTENER_MAX_COUNT + " times during this frame.");
			}
			mOnFrameRenderingListeners[mOnFrameRenderingListenerCount++] = listener;
			scheduleTraversals();
		}
		return true;
	}
	
	//等待渲染完毕后执行的操作相关代码----------------------------------------------------------^
	
	private int mTranslateY;
	private int mRequestedTranslateY;
	
	/**
	 * <br>功能简述: 设置根视图的上下偏移，主要是用于显示/隐藏通知栏
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param ty	应该大于0（向下偏移），小于0的情况未测试
	 */
	public void setTranslateY(int ty) {
		if (mRequestedTranslateY == ty) {
			return;
		}
		
		mRequestedTranslateY = ty;
		scheduleTraversals();
	}
	
	/**
	 * <br>功能简述: 设置是否处理触摸等事件
	 * <br>功能详细描述: 默认打开这个设置的。例如在打开一个activity的时候，需要等候绘制完本帧再真正打开，在此期间需要屏蔽事件。
	 * <br>注意: 在{@link #onResume()} 的时候会自动重新打开设置，避免出现问题。
	 * @param enabled
	 */
	public void setEventsEnabled(boolean enabled) {
		mEventsEnabled = enabled;
	}
	
	/**
	 * <br> 功能简述： 设置桌面事件是否被夺取
	 * <br>功能详细描述: 默认关闭的。例如便签插件在做动画的时候，为了不允许屏幕事件，所以把这里设为打开，在打开期间需要屏蔽事件
	 * 
	 * @param token true 被插件获取了事件 | false 插件没有获取事件，事件由桌面处理
	 */
	public void setEventsToken(boolean token) {
		mEventsToken = token;
	}
	
	/**
	 * <br> 功能简述： 获取是否被夺取了事件
	 */
	public boolean isEventsToken() {
		return mEventsToken;
	}
	
	//截屏相关代码----------------------------------------------------------v
	
	private final Object mScreenshotLock = new Object();
	
	//CHECKSTYLE IGNORE 1 LINES
	class ScreenshotInfo implements Runnable {
		//CHECKSTYLE IGNORE 9 LINES
		int x;
		int y;
		int w;
		int h;
		int count;
		int frameIgnore;
		String fileName;
		String fileType;
		long lastFrame = Long.MIN_VALUE / 2;

		void saveScreenshot() {
			final long timeStamp;
			synchronized (mBufferLock) {
				timeStamp = mBufferTimeStamp[mBufferReadingPtr];
			}
			if (timeStamp - lastFrame <= frameIgnore) {
				return;
			}
			--count;
			lastFrame = timeStamp;

			String file = fileName + String.format("%08X", timeStamp) + fileType;
			if (DOT_TGA.equalsIgnoreCase(fileType)) {
				NdkUtil.saveScreenshotTGA(x, y, w, h, file);
			} else {
				//如果不是.tga后缀的则当作.png处理
				int[] pixels = new int[w * h];
				NdkUtil.saveScreenshot(x, y, w, h, pixels);
				Bitmap bitmap = Bitmap.createBitmap(pixels, w, h, Config.ARGB_8888);
				GLCanvas.saveBitmap(bitmap, file);
			}
		}

		@Override
		public void run() {
			saveScreenshot();
		}
	}

	private ScreenshotInfo mScreenshotInfo;

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param fileName	例如"/sdcard/screenshot_"
	 * @param fileType	支持".png"或者".tga"
	 * @param count
	 * @frameIgnore		每两次截屏之间忽略的帧数，为0即连续截屏
	 */
	public boolean saveScreenshot(int left, int top, int right, int bottom, String fileName, String fileType, int count, int frameIgnore) {
		if (!ENABLE_SCREENSHOT) {
			return false;
		}
		if (mScreenshotInfo == null) {
			mScreenshotInfo = new ScreenshotInfo();
		}
		final int windowWidth = getWidth();
		final int windowHeight = getHeight();
		final Rect rect = mTempRect;
		rect.set(left, top, right, bottom);
		if (!rect.intersect(0, 0, windowWidth, windowHeight)) {
			return false;
		}

		final ScreenshotInfo info = mScreenshotInfo;
		synchronized (mScreenshotLock) {
			info.x = rect.left;
			info.y = windowHeight - rect.bottom;
			info.w = rect.width();
			info.h = rect.height();
			info.fileName = fileName;
			info.fileType = fileType;
			info.count = count;
			info.frameIgnore = frameIgnore;
		}
		return true;

	}
	
	//截屏相关代码----------------------------------------------------------^
	
	//主线程和GL线程的同步控制----------------------------------------------v
	private static final int BUFFER_STATE_FREE = 0;
	private static final int BUFFER_STATE_WRITING = 1;
	private static final int BUFFER_STATE_TO_READ = 2;
	private static final int BUFFER_STATE_READING = 3;
	private static final int BUFFER_COUNT = 2;

	private static final String[] BUFFER_STATE_STRINGS = { "f", "w", "tr", "r", };

	private static final boolean DBG_BUFFER = false;
	private static final String DBG_BUFFER_TAG = "DWM";

	private final long[] mBufferTimeStamp = new long[BUFFER_COUNT];
	private final int[] mBufferState = new int[BUFFER_COUNT];
	private volatile long mWriteTimeStamp = 0;
	private volatile long mReadTimeStamp = 0;
	private volatile int mBufferWritingPtr = 0;
	private volatile int mBufferReadingPtr = 0;
	private volatile boolean mForceReadFrame;
	
	private final Object mBufferLock = new Object();
	private final static Object STATIC_TIMESTAMP_LOCK = new Object();
	/** 让绘制线程和渲染线程互斥的锁，为null时不起作用，调试用*/
	private final static Object THREAD_SEQUENCE_CALL_LOCK = null;	//new Object();
	
	private static volatile long sFrameTimeStamp;
	private static volatile long sRenderTimeStamp;
	
	/** @hide */
	public static void resetFrameTimeStamp() {
		synchronized (STATIC_TIMESTAMP_LOCK) {
			sFrameTimeStamp = 0;
		}
	}
	
	public static long getFrameTimeStamp() {
		synchronized (STATIC_TIMESTAMP_LOCK) {
			return sFrameTimeStamp;
		}
	}
	
	public static long getRenderTimeStamp() {
		synchronized (STATIC_TIMESTAMP_LOCK) {
			return sRenderTimeStamp;
		}
	}

	private boolean startWritingBuffer() {
		synchronized (mBufferLock) {
			if (mBufferState[mBufferWritingPtr] == BUFFER_STATE_WRITING) {
				return true;
			}
			
			final int next = mBufferWritingPtr + 1 < BUFFER_COUNT ? mBufferWritingPtr + 1 : 0;
			if (mBufferState[next] != BUFFER_STATE_FREE) {
				//post delay
				return false;
			}
			
			mBufferWritingPtr = next;
			mBufferState[mBufferWritingPtr] = BUFFER_STATE_WRITING;
			mBufferTimeStamp[mBufferWritingPtr] = ++mWriteTimeStamp;
			synchronized (STATIC_TIMESTAMP_LOCK) {
				sFrameTimeStamp = mWriteTimeStamp;
			}
			if (mBufferReadingPtr == mBufferWritingPtr) {
				mBufferReadingPtr = mBufferReadingPtr + 1 < BUFFER_COUNT ? mBufferReadingPtr + 1 : 0;	//read next
				
				checkReadWriteCollision();
			}
			
			if (DBG_BUFFER) {
				Log.v(DBG_BUFFER_TAG, "v----startWritingBuffer wp=" + mBufferWritingPtr
					+ ":" + mWriteTimeStamp + " rp=" + mBufferReadingPtr + ":" + mReadTimeStamp 
					+ " buffers: " + mBufferTimeStamp[0] + " " 
					+ BUFFER_STATE_STRINGS[mBufferState[0]] + " / " + mBufferTimeStamp[1] 
					+ " " + BUFFER_STATE_STRINGS[mBufferState[1]]);
			}
			//draw
			return true;
		}
	}

	private void finishWritingBuffer() {
		synchronized (mBufferLock) {
			if (DBG_BUFFER) {
				Log.v(DBG_BUFFER_TAG, "^----finishWritingBuffer");
			}
			
			if (mBufferState[mBufferWritingPtr] == BUFFER_STATE_WRITING) {
				mBufferState[mBufferWritingPtr] = BUFFER_STATE_TO_READ;
				post(mRequestRenderAction);
			}
		}
	}

	private void startReadingBuffer() {
		synchronized (mBufferLock) {
			checkReadWriteCollision();
			
			mBufferState[mBufferReadingPtr] = BUFFER_STATE_READING;
			mReadTimeStamp = mBufferTimeStamp[mBufferReadingPtr];
			synchronized (STATIC_TIMESTAMP_LOCK) {
				sRenderTimeStamp = mReadTimeStamp;
			}
			
			if (DBG_BUFFER) {
				Log.v(DBG_BUFFER_TAG, "v====startReadingBuffer wp=" + mBufferWritingPtr
					+ ":" + mWriteTimeStamp + " rp=" + mBufferReadingPtr + ":" + mReadTimeStamp 
					+ " buffers: " + mBufferTimeStamp[0] + " " 
					+ BUFFER_STATE_STRINGS[mBufferState[0]] + " / " + mBufferTimeStamp[1] 
					+ " " + BUFFER_STATE_STRINGS[mBufferState[1]]);
			}
			//render
		}
	}

	private void finishReadingBuffer() {
		synchronized (mBufferLock) {
			if (DBG_BUFFER) {
				Log.v(DBG_BUFFER_TAG, "^====finishReadingBuffer");
			}
			mBufferState[mBufferReadingPtr] = BUFFER_STATE_FREE;
			checkRequestRender();
		}
	}

	private boolean checkRequestRender() {
		synchronized (mBufferLock) {
			if (mBufferState[mBufferReadingPtr] != BUFFER_STATE_READING) {
				boolean request = false;
				final int next = mBufferReadingPtr + 1 < BUFFER_COUNT ? mBufferReadingPtr + 1 : 0;
				
				if (mBufferTimeStamp[mBufferReadingPtr] <= mReadTimeStamp
						&& mBufferTimeStamp[next] > mReadTimeStamp
						&& mBufferState[next] != BUFFER_STATE_WRITING) {
					mBufferReadingPtr = next;
					request = true;
				} else if (mBufferTimeStamp[mBufferReadingPtr] > mReadTimeStamp
						&& mBufferState[mBufferReadingPtr] != BUFFER_STATE_WRITING) {
					request = true;
				}
				
				if (DBG_BUFFER) {
					Log.v(DBG_BUFFER_TAG, "checkRequestRender wp=" + mBufferWritingPtr
						+ ":" + mWriteTimeStamp + " rp=" + mBufferReadingPtr + ":" + mReadTimeStamp 
						+ " buffers: " + mBufferTimeStamp[0] + " " 
						+ BUFFER_STATE_STRINGS[mBufferState[0]] + " / " + mBufferTimeStamp[1] 
						+ " " + BUFFER_STATE_STRINGS[mBufferState[1]]);
				}
				
				if (request) {
					checkQueues(false);
					requestRender();
					if (DBG_BUFFER) {
						Log.v(DBG_BUFFER_TAG, "requestRender rp=" + mBufferReadingPtr);
					}
				}
				
				return true;
			}
			return false;
		}
	}

	private void startResetingBuffer() {
		synchronized (mBufferLock) {
			if (DBG_BUFFER) {
				Log.d(DBG_BUFFER_TAG, "startResetingBuffer");
			}
			
			for (int i = 0; i < BUFFER_COUNT; ++i) {
				mBufferState[i] = BUFFER_STATE_FREE;
				mRenderDatas[i].mReading = false;
			}
		}
	}
	
	private void finishResetingBuffer() {
		synchronized (mBufferLock) {
			if (DBG_BUFFER) {
				Log.d(DBG_BUFFER_TAG, "finishResetingBuffer");
			}
			
			mBufferReadingPtr = mBufferWritingPtr;
			checkReadWriteCollision();
		}
	}
	
	private void forceWriteCurrentFrame() {
		synchronized (mBufferLock) {
			mCanvas.mLastFrameDropped = true;
			for (int i = 0; i < BUFFER_COUNT; ++i) {
				if (i != mBufferReadingPtr) {
					mBufferState[i] = BUFFER_STATE_FREE;
				}
			}
			mBufferWritingPtr = mBufferReadingPtr;	//下一次startWritingBuffer的时候会将写指针前移
			mForceReadFrame = true;
			
			if (DBG_BUFFER) {
				Log.v(DBG_BUFFER_TAG, "forceWriteCurrentFrame wp=" + mBufferWritingPtr
					+ ":" + mWriteTimeStamp + " rp=" + mBufferReadingPtr + ":" + mReadTimeStamp 
					+ " buffers: " + mBufferTimeStamp[0] + " " 
					+ BUFFER_STATE_STRINGS[mBufferState[0]] + " / " + mBufferTimeStamp[1] 
					+ " " + BUFFER_STATE_STRINGS[mBufferState[1]]);
			}
			
			checkReadWriteCollision();
		}
	}
	
	private void forceReadCurrentFrame() {
		synchronized (mBufferLock) {
			if (!mForceReadFrame) {
				return;
			}
			if (DBG_BUFFER) {
				Log.d(DBG_BUFFER_TAG, "forceReadCurrentFrame mBufferTimeStamp: "
						+ mBufferTimeStamp[0] + " " + BUFFER_STATE_STRINGS[mBufferState[0]] + " / " 
						+ mBufferTimeStamp[1] + " " + BUFFER_STATE_STRINGS[mBufferState[1]]);
			}
			
			mForceReadFrame = false;
			long timeStamp = Long.MIN_VALUE;
			int curFrame = mBufferWritingPtr;
			for (int i = 0; i < BUFFER_COUNT; ++i) {
				if (mBufferTimeStamp[i] > timeStamp && mBufferState[i] != BUFFER_STATE_WRITING) {
					timeStamp = mBufferTimeStamp[i];
					curFrame = i;
				}
			}
			for (int i = 0; i < BUFFER_COUNT; ++i) {
				if (i != curFrame) {
					if (mBufferState[i] == BUFFER_STATE_TO_READ 
							|| mBufferState[i] == BUFFER_STATE_READING) {
						mBufferState[i] = BUFFER_STATE_FREE;

					}
				}
			}
			mBufferReadingPtr = curFrame;
			
			if (DBG_BUFFER) {
				Log.v(DBG_BUFFER_TAG, "forceReadCurrentFrame wp=" + mBufferWritingPtr
					+ ":" + mWriteTimeStamp + " rp=" + mBufferReadingPtr + ":" + mReadTimeStamp 
					+ " buffers: " + mBufferTimeStamp[0] + " " 
					+ BUFFER_STATE_STRINGS[mBufferState[0]] + " / " + mBufferTimeStamp[1] 
					+ " " + BUFFER_STATE_STRINGS[mBufferState[1]]);
			}
			
			checkReadWriteCollision();
		}
	}
	
	void checkReadWriteCollision() {
		if (mBufferState[mBufferReadingPtr] == BUFFER_STATE_WRITING) {
			throw new RuntimeException(
			//Log.e(TAG, 
					"GLContentView checkReadWriteCollision: wp=" + mBufferWritingPtr 
					+ ":" + mWriteTimeStamp + " rp=" + mBufferReadingPtr + ":" + mReadTimeStamp 
					+ " buffers: " + mBufferTimeStamp[0] + " " 
					+ BUFFER_STATE_STRINGS[mBufferState[0]] + " / " + mBufferTimeStamp[1] 
					+ " " + BUFFER_STATE_STRINGS[mBufferState[1]]);
		}
	}
	
	//主线程和GL线程的同步控制----------------------------------------------^
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
//		checkSession();
		if (!isSurfaceValid()) {
			onPause();
			//本方法是在GLSurfaceView.dispatchDraw里面调用的，为了避免出问题，使用post
			post(new Runnable() {
				@Override
				public void run() {
					if (mSurfaceViewOwner != null) {
						mSurfaceViewOwner.reCreateSurfaceView();
					}
				}
			});
			return;
		}
		mLastSurfaceWidth = -1;
		mLastSurfaceHeight = -1;
		mLastSurfaceFormat = -1;
		if (DBG) {
			Log.d(TAG, "GLContentView surfaceCreated");
		}
		super.surfaceCreated(holder);
	}

	public boolean isSurfaceValid() {
		final SurfaceHolder holder = getHolder();
		if (holder != null) {
			final Surface surface = holder.getSurface();
			return surface != null && surface.isValid();
		}
		return false;
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (format != mLastSurfaceFormat || w != mLastSurfaceWidth
				|| h != mLastSurfaceHeight) {
			if (DBG) {
				Log.d(TAG, "GLContentView surfaceChanged w=" + w + " h=" + h);
			}
			mLastSurfaceWidth = w;
			mLastSurfaceHeight = h;
			mLastSurfaceFormat = format;
			super.surfaceChanged(holder, format, w, h);
		}
	}

	/**
	 * 
	 * <br>类描述: 给SurfaceView持有者实现，用于需要重新创建SurfaceView的时候
	 * <br>功能详细描述:
	 * 
	 * @author  oulingmei
	 * @date  [2012-12-19]
	 */
	public interface SurfaceViewOwner {
		/**
		 * <br>功能简述: 重新创建{@link GLContentView} 实例
		 * <br>功能详细描述:当原来的实例的holder无效的时候会被要求重新创建实例
		 * <br>注意:
		 */
		void reCreateSurfaceView();
		
		
		/**
		 * <br>功能简述: 当检测到不支持 OpenGL ES 2.0 时的处理
		 * <br>功能详细描述:
		 * <br>注意:
		 */
		void handleGLES20UnsupportedError();
	}
	
	/**
	 * 
	 * <br>类描述: 用于捕获createContext异常的类
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2013-6-18]
	 */
    private static class ContextFactory implements EGLContextFactory {
        private final static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;	//from egl.h
        int mEGLContextClientVersion;
        GLThreadUncaughtExceptionHandler mHandler;
        
		public ContextFactory(int eglContextClientVersion, GLThreadUncaughtExceptionHandler handler) {
			mEGLContextClientVersion = eglContextClientVersion;
			mHandler = handler;
		}

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, mEGLContextClientVersion,
                    EGL10.EGL_NONE };
            
            mHandler.attachToThread();	//这里会比onSurfaceCreated更早执行到

            return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT,
                    mEGLContextClientVersion != 0 ? attrib_list : null);
        }

        public void destroyContext(EGL10 egl, EGLDisplay display,
								   EGLContext context) {
            if (!egl.eglDestroyContext(display, context)) {
            	throw new RuntimeException("eglDestroyContext: " + egl.eglGetError());
            }
        }
    }
	
	/**
	 * 
	 * <br>类描述: 用于捕获“Make sure the SurfaceView or associated SurfaceHolder has a valid Surface”这个异常的类
	 * <br>功能详细描述: 
	 * 
	 * @author  dengweiming
	 * @date  [2013-6-8]
	 */
	private static class WindowSurfaceFactory implements EGLWindowSurfaceFactory {

		public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
			EGLSurface surface = null;
			try {
				surface = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
			} catch (Exception e) {
				if (DBG) {
					Log.d(TAG, "WindowSurfaceFactory.createWindowSurface exception: " + e);
				}
			}
			return surface;
		}

		public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
			egl.eglDestroySurface(display, surface);
		}
	}
	
	/**
	 * 
	 * <br>类描述: GL线程未捕获的异常的处理者
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2013-6-8]
	 */
	private static class GLThreadUncaughtExceptionHandler implements UncaughtExceptionHandler {
		volatile boolean mExceptionCaught;
		int mAttachedThread;
		WeakReference<GLContentView> mContentViewRef;
		
		public GLThreadUncaughtExceptionHandler(GLContentView contentView) {
			mContentViewRef = new WeakReference<GLContentView>(contentView);
		}
		
		public void attachToThread() {
			Thread thread = Thread.currentThread();
			int hashCode = thread.hashCode();
			if (mAttachedThread != hashCode) {
				mAttachedThread = hashCode;
				thread.setUncaughtExceptionHandler(this);
			}
		}
		
		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			mExceptionCaught = true;
			if (DBG) {
				Log.d(TAG, "GLThreadUncaughtExceptionHandler.uncaughtException: thread=" + thread + " ex=" + ex);
			}
			
			String exString = ex.toString();
			if (!exString.contains("egl") && !exString.contains("EGL")) {
				//只处理EGL相关的异常，其他异常暴露出来以便发现并解决问题
				mExceptionCaught = false;
				final RuntimeException exception = new RuntimeException("GLThread uncaughtException.", ex);
				new Thread(new Runnable() {
					@Override
					public void run() {
						throw exception;
					}
				}).start();
				return;
			}
			
			GLContentView contentView = mContentViewRef.get();
			contentView.post(new Runnable() {
				@Override
				public void run() {
					GLContentView contentView = mContentViewRef.get();
					if (contentView != null) {
						mContentViewRef.clear();
						if (!mExceptionCaught) {
							return;	//避免重复调用
						}
						mExceptionCaught = false;
						
						SurfaceViewOwner owner = contentView.mSurfaceViewOwner;
						if (owner != null) {
							owner.reCreateSurfaceView();
						}
					}
				}
			});
		}
	};

}


/**
 * 
 * <br>类描述: 
 * <br>功能详细描述:{@link GLContentView.GLRenderer} 的封装器，避免因为内部类类实例被引用而不能释放外部类实例
 * 
 * @author  dengweiming
 * @date  [2012-10-24]
 * @hide
 */
class RendererWrapper implements Renderer {
	Renderer mRenderer;

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Renderer renderer = mRenderer;
		if (renderer != null) {
			renderer.onSurfaceCreated(gl, config);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Renderer renderer = mRenderer;
		if (renderer != null) {
			renderer.onSurfaceChanged(gl, width, height);		
		}
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		Renderer renderer = mRenderer;
		if (renderer != null) {
			renderer.onDrawFrame(gl);
		}
	}
	
	public void setRenderer(Renderer renderer) {
		mRenderer = renderer;
	}
	
}

/**
 * 渲染时用的数据
 */
class RenderData {
	RenderInfoNode mRenderInfoNode;
	VertexBufferBlock mVertexBufferBlock;
	IndexBufferBlock mIndexBufferBlock;
	volatile int mVertexBufferWriteCount;
	volatile int mIndexBufferWriteCount;
	volatile boolean mReading;
	
	RenderData() {
		mRenderInfoNode = new RenderInfoNode();
		mVertexBufferBlock = new VertexBufferBlock();
		mIndexBufferBlock = new IndexBufferBlock();
	}
	
	void reset() {
		mRenderInfoNode.reset();
		mVertexBufferBlock.reset();
		mIndexBufferBlock.reset();
	}
}

//CHECKSTYLE IGNORE 1 LINES
class GLDecorView extends GLFrameLayout {
	boolean mRequestingLayout;

	public GLDecorView(Context context) {
		super(context);
	}
	
	@Override
	public void requestLayout() {
		mRequestingLayout = true;
		super.requestLayout();
		mRequestingLayout = false;
	}
	
}