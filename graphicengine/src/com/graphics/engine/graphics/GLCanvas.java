package com.graphics.engine.graphics;

import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Canvas.EdgeType;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.graphics.drawable.NinePatchDrawable;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;

import com.graphics.engine.animation.Transformation3D;
import com.graphics.engine.util.FastQueue;
import com.graphics.engine.util.IBufferFactory;
import com.graphics.engine.util.Ray;
import com.graphics.engine.view.GLContentView;

/**
 * <br>类描述: 画布类
 * <br>功能详细描述:
 * <p>跟SDK的{@link Canvas}类似，提供各种变换方法，裁剪功能，绘制简单的几何图形以及图片的功能。</p>
 * 
 * <p>一些变换方法有2D版本和3D版本：对于2D版本，Y轴向下，角度为正时顺时针旋转；对于3D版本，Y轴向上，Z轴向屏幕外，
 * 旋转方向遵从右手定则，即大拇指指向旋转轴时其他手指弯曲的方向为旋转方向，角度为证是逆时针旋转。</p>
 * 
 * <p>变换分为投影变换（正交投影和默认的透视投影），视图变换（框架用来调整世界坐标系的位置），模型变换。
 * 提供了多种方法来控制模型变换，例如{@link #reset()}, {@link #translate(float, float)} 等等。
 * <br>在绘制物体时，需要计算三种变换的复合变换，使用{@link #getFinalMatrix()}或者
 * {@link #getFinalMatrix(RenderContext)}。
 * </p>
 * 
 * <p>使用{@link #matrixToString()}得到当前模型矩阵的格式化字符串以便打印调试。</p>
 * 
 * @author  dengweiming
 * @date  [2013-10-16]
 */
public final class GLCanvas {
	private final static String TAG = "DWM";
	private final static boolean DBG = false;
	/** 蒙板裁剪区域的绘制颜色，为0时则不绘制，调试用 */
	private final static int STENCIL_DBG_COLOR = 0;	//0xafff0000;
	public static String sMatrixTag = null;

	//===============================for matrix =============v

	private static final float[] sTmpMatrix = new float[16]; 					//CHECKSTYLE IGNORE
	private static final StringBuilder sStringBuilder = new StringBuilder(128); //CHECKSTYLE IGNORE

	/** default filed of view */
	private static float sDEFAULT_FOV = 45;
	public static final float DEFAULT_Z_RANGE = 9000;
	
	private static final float EPSILON = 1e-6f;

	/** @formatter:off */
	//CHECKSTYLE IGNORE 1 LINES
	private static double sDEFAULT_FOV_SCALE_FACTOR = 0.5 / Math.tan(Math.toRadians(sDEFAULT_FOV) * 0.5);
	/** @formatter:on */
	
	/** 不透明度最大值，即255 */
	public static final int FULL_ALPHA = 255;
	/** 不透明度最大值的倒数，即1./255，用于归一化 */
	public static final float INV_ALPHA = 1.0f / FULL_ALPHA;
	/** 秒转为毫秒时的比率 */
	public static final long SECOND_TO_MILLISECONDS = 1000;
	/** 毫秒转为秒时的比率 */
	public static final float MILLISECONDS_TO_SECOND = 0.001f;

	//矩阵元素按列优先存储
	private static final int M00 = 0;
	private static final int M10 = 1;
	private static final int M20 = 2;
	private static final int M30 = 3;
	private static final int M01 = 4;
	private static final int M11 = 5;
	private static final int M21 = 6;
	private static final int M31 = 7;
	private static final int M02 = 8;
	private static final int M12 = 9;
	private static final int M22 = 10;
	private static final int M32 = 11;
	private static final int M03 = 12;
	private static final int M13 = 13;
	private static final int M23 = 14;
	private static final int M33 = 15;
	private static final int MC = 16;

	/** 向量x索引 */
	public static final int VTX = 0;
	/** 向量y索引 */
	public static final int VTY = 1;
	/** 向量z索引 */
	public static final int VTZ = 2;
	/** 向量w索引 */
	public static final int VTW = 3;
	/** 向量长度 */
	public static final int VECTOR_LENGTH = 4;
	
	/** 矩阵平移量x索引 */
	public static final int MTX = M03;
	/** 矩阵平移量y索引 */
	public static final int MTY = M13;
	/** 矩阵平移量z索引 */
	public static final int MTZ = M23;
	/** 矩阵长度 */
	public static final int MATRIX_LENGTH = MC;

	final float[] mModelMatrix;		//模型矩阵
	final float[] mViewMatrix;		//视图矩阵
	final float[] mProjMatrix;		//投影矩阵
	final float[] mVPMatrix;		//视图和投影矩阵的乘积
	final float[] mMVPMatrix;		//模型视图和投影矩阵的乘积

	private int mWindowWidth;
	private int mWindowHeight;

	public static final int ViewportX = 0;		//CHECKSTYLE IGNORE
	public static final int ViewportY = 1;		//CHECKSTYLE IGNORE
	public static final int ViewportW = 2;		//CHECKSTYLE IGNORE
	public static final int ViewportH = 3;		//CHECKSTYLE IGNORE
	/** 视口参数个数，值为4 */
	public  static final int ViewportArgc  = 4;	//CHECKSTYLE IGNORE
	final int[] mViewport;			//视口
	final float[] mProjectCoffs;	//将3D点投影到窗口上的2D点时需要的系数
	final float[] mUnprojectCoffs;	//将窗口上的2D点反投影时需要的系数
	boolean mClipRectMapToViewport;

	public static final int FrustumL = 0;		//CHECKSTYLE IGNORE
	public static final int FrustumR = 1;		//CHECKSTYLE IGNORE
	public static final int FrustumB = 2;		//CHECKSTYLE IGNORE
	public static final int FrustumT = 3;		//CHECKSTYLE IGNORE
	public static final int FrustumN = 4;		//CHECKSTYLE IGNORE
	public static final int FrustumF = 5;		//CHECKSTYLE IGNORE
	/** 视锥体参数个数，值为6 */
	public static final int FrustumArgc = 6;	//CHECKSTYLE IGNORE
	final float[] mFrustum;			//视锥体

	final float[] mDefaultCameraPos;	//默认相机的位置，使得原点的物体和其投影一样大
	final float[] mCameraPos;		//相机的位置
	int mWorldX;
	int mWorldY;

	int mSize;						//模型矩阵堆栈大小
	private int mSaveCount;
	private int mPtr;				//栈顶矩阵的位置
	private int mBias;				//由于有时候作变换不能在该内存位置上原地写入结果，
									//所以栈顶矩阵其实是两个矩阵在轮流交替,
									//mBias可以取值为0或者MC(16)

	private final float[] mTmpVector1;
	private final float[] mTmpVector2;

	private final float[] mTmpMatrix;
    private final RectF mTmpRect = new RectF();
	private StringBuilder mStringBuilder = sStringBuilder;

	private int mGLStateFlag;
	/** @formatter:off */
	private static final int DEPTH_MASK		= 1;
	private static final int COLOR_MASK		= DEPTH_MASK 	<< 1;
	private static final int DEPTH_TEST		= COLOR_MASK 	<< 1;
	private static final int CULL_FACE		= DEPTH_TEST 	<< 1;
	private static final int SCISSOR_TEST	= CULL_FACE  	<< 1;
	private static final int BLEND			= SCISSOR_TEST  << 1;
	private static final int STENCIL_TEST	= BLEND  		<< 1;
	private static final int CULL_BACK		= STENCIL_TEST	<< 1;
	/** @formatter:on */
	
	private int mCurClearColor;
	private boolean mClearColorChanged;
	private int mBackgroundClearColor;
	
	private final static Mode DEFAULT_BLEND_MODE = Mode.SRC_OVER;
	private Mode mBlendMode = DEFAULT_BLEND_MODE;
	
	/** @hide */
	public boolean mLastFrameDropped;	//上几帧被丢弃了
	/** @hide */
	public long mDrawingTime;
	/** @hide */
	public long mDeltaDrawingTime;
	
	public static long sFrameTime;
	/** 日志打印的频率，打日志的地方加上这个判断 if (DBG && sFrameTime % sFpsForLog == 0) */
	public static long sFpsForLog = 60;
	private static long sFrameTimeOnGL;

	//============declare for layer =============v
	private GLFramebuffer[] mLayers = new GLFramebuffer[1];	//非调试时仅使用1个图层
	private GLFramebuffer mLayer;	//当前使用的图层，是mLayers的元素
	private int mLayerIndex;
	private RectF mLayerBox = new RectF();
	private float[] mLayerMatrix = new float[MC]; 
	private RectF mLayerClipRect = new RectF();
	private boolean mLayerToDisableStencil;
	
	private RectF mLayerSavedClipRect = new RectF();
	private int mLayerSavedAlpha;
	private boolean mLayerSavedStencilEnabled;
	private int mLayerSavedCount = LAYER_UNSAVE_COUNT;
	private int mLayerFlag;
	
	/**
	 * 淡化Layer区域。
	 * <br>需要与淡化因子作位或运算，即：
	 * <br>&nbsp;&nbsp;int layerFlag = LAYER_ALPHA_FLAG | fadeAlpha;
	 */
	public final static int LAYER_ALPHA_FLAG = 1 << 8;
	/** 裁剪Layer区域。
	 * <br>在这两个条件满足时，可能需要设置这个标记：
	 * <br>&nbsp;&nbsp;1)绘制内容会超出Layer区域；
	 * <br>&nbsp;&nbsp;2)Layer区域投影到屏幕后不是AABB（轴对齐包围盒）。
	 * <br>因为如果是AABB，会自动使用窗口裁剪。
	 * */
	public final static int LAYER_CLIP_FLAG = 1 << 9;
	/**
	 * 转化到局部坐标系中。
	 * <br>将当前的坐标系映射到世界坐标系中，即假如在旋转的情况下使用图层，
	 * 那么图层在绘图缓冲中是没有旋转的。当restore的时候，整个图层再整体
	 * 旋转。这样图层的AABB就是它本身区域，不会有多余的填充部分，另外也会
	 * 在3D变换的情况下改变了投影效果。
	 * <br>这个标志已经包含了{@link #LAYER_CLIP_FLAG}的功能。
	 */
	public final static int LAYER_LOCAL_FLAG = 1 << 10;
	private final static int LAYER_ALPHA_MASK = LAYER_ALPHA_FLAG - 1;
	private final static int LAYER_UNSAVE_COUNT = -1;

	//============declare for layer =============^
	
	/**
	 * <默认构造函数>
	 * @param 变换堆栈的大小
	 * @hide
	 */
	public GLCanvas(int size) {
		mSize = size;
		mModelMatrix = new float[MC * (mSize + 1)];
		reset();

		mViewMatrix = new float[MC];
		Matrix.setIdentityM(mViewMatrix, 0);

		mProjMatrix = new float[MC];
		Matrix.setIdentityM(mProjMatrix, 0);

		mVPMatrix = new float[MC];
		Matrix.setIdentityM(mVPMatrix, 0);

		mMVPMatrix = new float[MC];
		Matrix.setIdentityM(mMVPMatrix, 0);
		
		mClipRectStack = new RectF[size + 1];
		mStencilClipRectStack = new RectF[size + 1];
		for (int i = 0; i <= size; ++i) {
			mClipRectStack[i] = new RectF();
			mStencilClipRectStack[i] = new RectF(); 
		}
		mStencilIdStack = new int[size + 1];

		//CHECKSTYLE:OFF
		mViewport = new int[4];
		mProjectCoffs = new float[4];
		mUnprojectCoffs = new float[4];
		mFrustum = new float[6];

		mDefaultCameraPos = new float[3];
		mCameraPos = new float[3];

		mTmpMatrix = new float[MC];
		mTmpVector1 = new float[4];
		mTmpVector2 = new float[4];
		//CHECKSTYLE:ON

		mFakeCanvas = new FakeCanvas();
		mBitmapDrawableMap = new HashMap<BitmapDrawable, BitmapGLDrawable>();
		mNinePatchDrawableMap = new HashMap<NinePatchDrawable, NinePatchGLDrawable>();
		mColorDrawableMap = new HashMap<ColorDrawable, ColorGLDrawable>();

	}

	/**
	 * 给临时字符缓冲（打印数据用）分配单独空间，避免线程问题
	 * @hide
	 */
	public void allocateStringBuilder() {
		if (mStringBuilder == sStringBuilder) {
			mStringBuilder = new StringBuilder(128); //CHECKSTYLE IGNORE
		}
	}

	/**
	 * 设置窗口的大小。从屏幕触摸点投射射线的时候需要用到（因为视口不一定和窗口一致）。
	 * @hide
	 */
	public void setWindowSize(int width, int height) {
		mWindowWidth = width;
		mWindowHeight = height;
	}

	/**
	 * 设置视口以及默认的视锥体
	 * 
	 * @param width
	 * @param height
	 */
	public void setDefaultViewportFrustum(int width, int height) {
		if (height < 1) {
			height = 1;
		}
		setViewport(0, 0, width, height);
		final float cameraZ = (float) (height * sDEFAULT_FOV_SCALE_FACTOR);
		final float near = Math.max(1, cameraZ / 2 - 1);
		// 解决大分辨率手机屏幕预览拐弯特效被裁剪问题
		final float far = near + DEFAULT_Z_RANGE * (Math.max(width, 800) / 800f); //CHECKSTYLE IGNORE
		setProjection(sDEFAULT_FOV, width / (float) height, near, far);
	}

	/**
	 * 设置视口
	 * 
	 * @param x
	 * @param y 视口的<u><strong>下</strong></u>边界
	 * @param width
	 * @param height
	 */
	public void setViewport(int x, int y, int width, int height) {
		width = Math.max(width, 1);
		height = Math.max(height, 1);
		mViewport[ViewportX] = x;
		mViewport[ViewportY] = y;
		mViewport[ViewportW] = width;
		mViewport[ViewportH] = height;

		computeProjectCoffs();
//		computeUnprojectCoffs();
		
		if (mCurRenderInfoNode != null) {	//TODO:在addRenderable判断是否开始绘制
			RenderContext context = RenderContext.acquire();
			final float[] args = context.color;
			args[0] = x;
			args[1] = y;
			args[2] = width;
			args[3] = height;	//CHECKSTYLE IGNORE
			Renderable renderable = GLCommandFactory.get(GLCommandFactory.VIEWPORT);
			addRenderable(renderable, context);
		}
		
	}

	/**
	 * 设置视口
	 * @param viewport	长度为{@link #ViewportArgc}
	 */
	public void setViewport(int[] viewport) {
		setViewport(viewport[ViewportX], viewport[ViewportY], viewport[ViewportW], viewport[ViewportH]);
	}

	/**
	 * 获取视口
	 * @param viewport	长度为{@link #ViewportArgc}
	 */
	public void getViewport(int[] viewport) {
		viewport[ViewportX] = mViewport[ViewportX];
		viewport[ViewportY] = mViewport[ViewportY];
		viewport[ViewportW] = mViewport[ViewportW];
		viewport[ViewportH] = mViewport[ViewportH];
	}

	/**
	 * 获取视口宽度
	 */
	public int getViewportWidth() {
		return mViewport[ViewportW];
	}

	/**
	 * 获取视口高度
	 */
	public int getViewportHeight() {
		return mViewport[ViewportH];
	}
	
	/**
	 * <br>功能简述: 设置裁剪区域是否映射到视口上
	 * <br>功能详细描述: 例如使用渲染到纹理时，裁剪区域在新的视口上位置需要作映射（因为纹理可能跟默认窗口不一样大，视口原点也可能不在当前窗口原点）
	 * <br>注意:
	 * @param mapped
	 */
	public void setClipRectMapToViewport(boolean mapped) {
		mClipRectMapToViewport = mapped;
		final RectF savedRect = mClipRectStack[mSaveCount];
		setClipRect(savedRect);
	}

	/**
	 * 设置投影方式为透视投影
	 * 
	 * @param left	视锥体的左边界
	 * @param right 视锥体的右边界
	 * @param bottom 视锥体的下边界（Y轴向上）
	 * @param top 视锥体的上边界（Y轴向上，即要比<var>bottom</var>大）
	 * @param near	近裁面。正值。
	 * @param far	远裁面。正值。
	 */
	public void setProjection(float left, float right, float bottom, float top, float near,
			float far) {
		Matrix.frustumM(mProjMatrix, 0, left, right, bottom, top, near, far);
		Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mViewMatrix, 0);

		mFrustum[FrustumL] = left;
		mFrustum[FrustumR] = right;
		mFrustum[FrustumB] = bottom;
		mFrustum[FrustumT] = top;
		mFrustum[FrustumN] = near;
		mFrustum[FrustumF] = far;

		final float z = mViewport[ViewportH] * mFrustum[FrustumN] * 0.5f / mFrustum[FrustumT]; //CHECKSTYLE IGNORE
		mDefaultCameraPos[0] = -z * mFrustum[FrustumL] / mFrustum[FrustumN];
		mDefaultCameraPos[1] = -z * mFrustum[FrustumT] / mFrustum[FrustumN];
		mDefaultCameraPos[2] = z;

		computeProjectCoffs();
//		computeUnprojectCoffs();
	}

	/**
	 * 设置投影方式为透视投影
	 * 
	 * @param fovy Y轴方向上的视角大小（眼睛张开的角度）。单位为角度，默认为45比较符合人眼。
	 * @param aspect 宽高比
	 * @param near	近裁面。正值。
	 * @param far	远裁面。正值。
	 */
	public void setProjection(float fovy, float aspect, float near, float far) {
		final float top = near * (float) Math.tan(fovy * (Math.PI / 360.0)); //CHECKSTYLE IGNORE
		final float bottom = -top;
		final float left = bottom * aspect;
		final float right = top * aspect;
		setProjection(left, right, bottom, top, near, far);
	}

	/**
	 * 设置投影矩阵，用于恢复备份
	 * @param frustum	长度为{@link #FrustumArgc}
	 * @see {@link #getProjection(float[])}
	 */
	public void setProjection(float[] frustum) {
		setProjection(frustum[FrustumL], frustum[FrustumR], frustum[FrustumB], frustum[FrustumT],
				frustum[FrustumN], frustum[FrustumF]);
	}

	/**
	 * 获取投影矩阵，用于备份
	 * @param frustum	长度为{@link #FrustumArgc}
	 * @see {@link #setProjection(float[])
	 */
	public void getProjection(float[] frustum) {
		System.arraycopy(mFrustum, 0, frustum, 0, FrustumArgc);
	}
	
	/**
	 * <br>功能简述: 设置为正交投影方式（同时会修改视口大小）
	 * <br>功能详细描述: 例如在对尺寸大于视口的图片做处理（模糊之类）时，需要更大的视口，而且不需要透视投影
	 * <br>注意: 需要在调用本方法之前使用{@link #getProjection(float[])}, {@link #getViewport(int[])}备份，
	 * 最后使用{@link #setProjection(float[])}, {@link #setViewport(int[])}还原。
	 * @param width 视口宽度
	 * @param height 视口高度
	 */
	public void setOtho(int width, int height) {
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException("width and height should be positive.");
		}
		float left = -mCameraPos[0];
		float top = -mCameraPos[1];
		float right = left + width;
		float bottom = top - height;
		Matrix.orthoM(mProjMatrix, 0, left, right, bottom, top, mFrustum[FrustumN], mFrustum[FrustumF]);
		Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mViewMatrix, 0);
		setViewport(0, 0, width, height);
	}

	private void computeProjectCoffs() {
		//CHECKSTYLE IGNORE 4 LINES
		mProjectCoffs[0] = mViewport[ViewportW] * 0.5f;
		mProjectCoffs[1] = mProjectCoffs[0] + mViewport[ViewportX];
		mProjectCoffs[2] = mViewport[ViewportH] * 0.5f;
		mProjectCoffs[3] = mProjectCoffs[2] + mViewport[ViewportY];
	}

	private void computeUnprojectCoffs() {
		final float invW = 1.0f / mViewport[ViewportW];
		final float invP00 = 1.0f / mProjMatrix[M00];
		final float invH = 1.0f / mViewport[ViewportH];
		final float invP11 = 1.0f / mProjMatrix[M11];

		/** @formatter:off */
		mUnprojectCoffs[0] = 2 * invW * invP00;
		mUnprojectCoffs[1] = (mProjMatrix[M02] - mProjMatrix[M03] - 2 * mViewport[ViewportX] * invW - 1) * invP00;
		mUnprojectCoffs[2] = 2 * invH * invP11;
		mUnprojectCoffs[3] = (mProjMatrix[M12] - mProjMatrix[M13] - 2 * mViewport[ViewportY] * invH - 1) * invP11; //CHECKSTYLE IGNORE
		/** @formatter:on */
	}

	/**
	 * 获取近裁面位置
	 * @return
	 */
	public float getZNear() {
		return mFrustum[FrustumN];
	}
	
	/**
	 * 获取远裁面位置
	 * @return
	 */
	public float getZFar() {
		return mFrustum[FrustumF];
	}

	/**
	 * <br>功能简述: 设置世界坐标系原点在窗口中的位置
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param windowX
	 * @param windowY
	 * @hide
	 */
	public void setWorldPosition(int windowX, int windowY) {
		mWorldX = windowX;
		mWorldY = windowY;
		
		mCameraPos[0] = mDefaultCameraPos[0] - windowX;
		mCameraPos[1] = mDefaultCameraPos[1] + windowY;
		mCameraPos[2] = mDefaultCameraPos[2];
		
		Matrix.setIdentityM(mViewMatrix, 0);
		Matrix.translateM(mViewMatrix, 0, -mCameraPos[0], -mCameraPos[1], -mCameraPos[2]);
		Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mViewMatrix, 0);
	}
	
	/**
	 * <br>功能简述: 计算指定深度的物体投影到屏幕后的缩放比例
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param depth 指定深度，和Z轴同向，即垂直屏幕向外为正方向
	 * @return
	 */
	public float getProjectScale(float depth) {
		// z / (z - d) = s
		final float z = mCameraPos[2];
		return z / (z - depth);
	}
	
	/**
	 * <br>功能简述: 计算深度，使得该深度的物体投影到屏幕后的缩放比例为指定比例
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param scale 指定比例
	 * @return 深度，和Z轴同向，即垂直屏幕向外为正方向
	 */
	public float getDepthForProjectScale(float scale) {
		// z / (z - d) = s
		final float z = mCameraPos[2];
		return z - z / scale;
	}

	/**
	 * 获取默认相机的深度，正数
	 * @return 
	 */
	public float getCameraZ() {
		return mCameraPos[2];
	}

	/**
	 * 获取相机的世界坐标
	 * @param pos 长度为3
	 */
	public void getCameraWorldPosition(float[] pos) {
		pos[0] = mCameraPos[0];
		pos[1] = mCameraPos[1];
		pos[2] = mCameraPos[2];
	}

	/**
	 * 获取相机在当前坐标系的坐标，因为涉及到求逆矩阵，假设当前坐标系的变换矩阵中只有平移和旋转。
	 * 否则，使用{@link #getCameraWorldPosition(float[]) 来自行求解
	 * @param pos 长度为3
	 */
	public void getCameraLocalPosition(float[] pos) {
		inverseTranslateAndRotatePosition(mModelMatrix, mPtr + mBias, mCameraPos, 0, pos, 0);
	}

	/**
	 * 设置模型矩阵，从指定眼睛位置，向指定中心点观察，并指定大致的向上的方向
	 *
	 * @param eyeX 眼睛位置 X
	 * @param eyeY 眼睛位置 Y
	 * @param eyeZ 眼睛位置 Z
	 * @param centerX 观察的中心点 X
	 * @param centerY 观察的中心点 Y
	 * @param centerZ 观察的中心点 Z
	 * @param upX 大致的向上的方向 X
	 * @param upY 大致的向上的方向 Y
	 * @param upZ 大致的向上的方向 Z
	 */
	public void setLookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY,
			float centerZ, float upX, float upY, float upZ) {
		final int ptr = mPtr + mBias;
		Matrix.setLookAtM(mModelMatrix, ptr, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
		//左乘以视图矩阵T(-camX, -camY, -camZ)的逆，以抵消其影响，可以简单地添加平移量实现
		mModelMatrix[ptr + MTX] += mCameraPos[VTX];
		mModelMatrix[ptr + MTY] += mCameraPos[VTY];
		mModelMatrix[ptr + MTZ] += mCameraPos[VTZ];
	}
	
	/**
	 * <br>功能简述: 设置模型矩阵，从顶部俯视场景
	 * <br>功能详细描述: 产生从中心点上方默认距离
	 * （在 <var>frustum</var> 为 false 时为 {@link #getCameraZ()}）的地方垂直往下观察的效果
	 * <br>注意: 中心点是在当前坐标系的。如果当前坐标系是世界坐标系，则坐标原点的位置在窗口左上方以及Y轴方向是向下。
	 * @param cx 观察的中心点 X
	 * @param cy 观察的中心点 Y
	 * @param cz 观察的中心点 Z
	 * @param rightToX X轴方向是否指向屏幕右方（而Z轴向下），否则X轴向上（而Z轴向右，即标准的ZoX平面坐标系，相当于前一种情况再向右 roll 90度）
	 * @param frustum 是否要远离到观察到整个视锥体
	 * @see {@link #setLookAt(float, float, float, float, float, float, float, float, float)}
	 */
	public void setLookFromTop(float cx, float cy, float cz, boolean rightToX, boolean frumstum) {
		float ey = cy + mCameraPos[VTZ];
		if (rightToX) {
			if (frumstum) {
				// (CamZ - cz) / (ey - CamY) = CamY / CamZ
				ey = Math.max((mCameraPos[VTZ] - cz) * mCameraPos[VTZ] / -mCameraPos[VTY] + mCameraPos[VTY], ey);
			}
			setLookAt(cx, ey, cz, cx, cy, cz, 0, 0, -1);
		} else {
			if (frumstum) {
				// (CamZ - cz) / (ey - CamY) = CamX / CamZ
				ey = Math.max((mCameraPos[VTZ] - cz) * mCameraPos[VTZ] / mCameraPos[VTX] + mCameraPos[VTY], ey);
			}
			setLookAt(cx, ey, cz, cx, cy, cz, 1, 0, 0);
		}
	}
	
	/**
	 * <br>功能简述: 设置模型矩阵，从右边向左观察场景
	 * <br>功能详细描述: 产生从中心点右方默认距离
	 * （在 <var>frustum</var> 为 false 时为 {@link #getCameraZ()}）的地方向左观察的效果
	 * <br>注意: 中心点是在当前坐标系的。如果当前坐标系是世界坐标系，则坐标原点的位置在窗口左上方以及Y轴方向是向下。
	 * @param cx 观察的中心点 X
	 * @param cy 观察的中心点 Y
	 * @param cz 观察的中心点 Z
	 * @param upToY Y轴方向是否指向屏幕上方（而Z轴向左），否则Y轴向右（而Z轴向上，即标准的YoZ平面坐标系，相当于前一种情况再向左 roll 90度）
	 * @param frustum 是否要远离到观察到整个视锥体
	 * @see {@link #setLookAt(float, float, float, float, float, float, float, float, float)}
	 */
	public void setLookFromRight(float cx, float cy, float cz, boolean upToY, boolean frustum) {
		float ex = cx + mCameraPos[VTZ];
		if (upToY) {
			if (frustum) {
				// (CamZ - cz) / (ex - CamX) = CamX / CamZ
				ex = Math.max((mCameraPos[VTZ] - cz) * mCameraPos[VTZ] / mCameraPos[VTX] + mCameraPos[VTX], ex);
			}
			setLookAt(ex, cy, cz, cx, cy, cz, 0, 1, 0);
		} else {
			if (frustum) {
				// (CamZ - cz) / (ex - CamX) = -CamY / CamZ
				ex = Math.max((mCameraPos[VTZ] - cz) * mCameraPos[VTZ] / -mCameraPos[VTY] + mCameraPos[VTX], ex);
			}
			setLookAt(ex, cy, cz, cx, cy, cz, 0, 0, 1);
		}
	}
	
	/**
	 * <br>功能简述: 设置公告板效果，忽略物体的旋转使得物体始终对着屏幕外垂直方向（但这个方法同时也忽略了物体的缩放）
	 * <br>功能详细描述: 修改模型矩阵，只保留平移部分。一个应用例子是用一张精灵图模拟光球粒子，树木等。
	 * <br>注意:
	 */
	public void setLookAsBillboardNoScaling() {
		final int ptr = mPtr + mBias;
		float tx = mModelMatrix[ptr + MTX];
		float ty = mModelMatrix[ptr + MTY];
		float tz = mModelMatrix[ptr + MTZ];
		reset();
		mModelMatrix[ptr + MTX] = tx;
		mModelMatrix[ptr + MTY] = ty;
		mModelMatrix[ptr + MTZ] = tz;
	}

	/**
	 * 获取投影矩阵和模型视图矩阵的乘积（MVP矩阵），注意如果没有调用translate这类变换的方法，应该避免重复调用本方法。
	 * 
	 * @return
	 */
	public float[] getFinalMatrix() {
		Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mModelMatrix, mPtr + mBias);
		return mMVPMatrix;
	}

	/**
	 * 获取投影矩阵和模型视图矩阵的乘积（MVP矩阵），保存到context中
	 */
	public void getFinalMatrix(RenderContext context) {
		Matrix.multiplyMM(context.matrix, 0, mVPMatrix, 0, mModelMatrix, mPtr + mBias);
	}
	
	/**
	 * 获取模型视图矩阵，应该避免重复调用本方法。
	 * @param t
	 */
	@Deprecated
	public void getMatrix(Transformation3D t) {
		//TODO
		Matrix.multiplyMM(t.getMatrix(), 0, mViewMatrix, 0, mModelMatrix, mPtr + mBias);
	}

	/**
	 * <br>功能简述: 计算3D点投影在屏幕上的位置
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param x
	 * @param y
	 * @param z
	 * @param modelMatrix 如果为null将使用当前的矩阵
	 * @param mOffset
	 * @param res
	 * @param rOffset
	 */
	public void projectToWindow(float x, float y, float z, float[] modelMatrix, int mOffset,
			float[] res, int rOffset) {
		if (modelMatrix == null) {
			modelMatrix = mModelMatrix;
			mOffset = mPtr + mBias;
		}
		final float[] v1 = mTmpVector1;
		final float[] v2 = mTmpVector2;
		v1[0] = x;
		v1[1] = y;
		v1[2] = z;
		v1[3] = 1; //CHECKSTYLE IGNORE
		Matrix.multiplyMV(v2, 0, modelMatrix, mOffset, v1, 0);
		Matrix.multiplyMV(v1, 0, mVPMatrix, 0, v2, 0);
		final float rw = 1.0f / v1[3]; //CHECKSTYLE IGNORE
		res[rOffset] = (v1[0] * rw) * mProjectCoffs[0] + mProjectCoffs[1];
		final float tmpY = (v1[1] * rw) * mProjectCoffs[2] + mProjectCoffs[3];
		res[rOffset + 1] = mWindowHeight - tmpY;

	}

	/**
	 * <br>功能简述: 计算一组3D点投影在屏幕上的位置
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param vertices 3D点，每个点为x,y,z，紧密排列
	 * @param offset
	 * @param count
	 * @param modelMatrix 如果为null将使用当前的矩阵
	 * @param mOffset
	 * @param res 结果保存位置，可以和<var>vertices</var>重合，每个点为x,y,紧密排列
	 * @param rOffset
	 */
	public void projectToWindow(float[] vertices, int offset, int count, float[] modelMatrix,
			int mOffset, float[] res, int rOffset) {
		if (modelMatrix == null) {
			modelMatrix = mModelMatrix;
			mOffset = mPtr + mBias;
		}
		Matrix.multiplyMM(mTmpMatrix, 0, mVPMatrix, 0, modelMatrix, mOffset);
		final float[] v1 = mTmpVector1;
		final float[] v2 = mTmpVector2;
		for (int i = 0; i < count; ++i) {
			v1[0] = vertices[offset++];
			v1[1] = vertices[offset++];
			v1[2] = vertices[offset++];
			v1[3] = 1; //CHECKSTYLE IGNORE
			Matrix.multiplyMV(v2, 0, mTmpMatrix, 0, v1, 0);
			final float rw = 1.0f / v2[3]; //CHECKSTYLE IGNORE
			res[rOffset++] = (v2[0] * rw) * mProjectCoffs[0] + mProjectCoffs[1];
			final float tmpY = (v2[1] * rw) * mProjectCoffs[2] + mProjectCoffs[3];
			res[rOffset++] = mWindowHeight - tmpY;

		}
	}

	/**
	 * 计算摄像机到窗口上2D点对应的3D点的射线
	 * @param windowX
	 * @param windowY 原点在窗口左上角，Y轴向下
	 * @param ray
	 */
	private void unprojectFromWindow(float windowX, float windowY, Ray ray) {
		ray.mvOrigin.set(mCameraPos[0], mCameraPos[1], mCameraPos[2]);
		ray.mvDirection.set(mUnprojectCoffs[0] * windowX + mUnprojectCoffs[1], mUnprojectCoffs[2]
				* (mWindowHeight - windowY) + mUnprojectCoffs[3], -1); //CHECKSTYLE IGNORE
		ray.mvDirection.normalize();
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
		final float[] c = mCameraPos;
		final float scale = c[2] / (c[2] - worldZ);
		res[0] = (worldX - c[0]) * scale + c[0];
		res[1] = (worldY - c[1]) * scale + c[1];
	}

	/**
	 * 将参考平面上的点反投影到指定z平面上
	 * @param planeX
	 * @param planeY
	 * @param dstZ
	 * @param res 长度为2
	 */
	public void unprojectFromReferencePlaneToWorld(float planeX, float planeY, float dstZ,
			float[] res) {
		final float[] c = mCameraPos;
		final float scale = (c[2] - dstZ) / c[2];
		res[0] = (planeX - c[0]) * scale + c[0];
		res[1] = (planeY - c[1]) * scale + c[1];
	}
	
	/**
	 * <br>功能简述: 将一个矩形投影到屏幕上，计算包围盒
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param inRect
	 * @return 结果的包围盒，存储于临时变量中，需要尽快取出，避免被后续调用更改
	 */
	public RectF projectRectToAABB(RectF inRect) {
		projectRectToAABB(inRect, mTmpRect);
		return mTmpRect;
	}
	
	/**
	 * <br>功能简述: 将一个矩形投影到屏幕上，计算包围盒
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return 结果的包围盒，存储于临时变量中，需要尽快取出，避免被后续调用更改
	 */
	public RectF projectRectToAABB(float left, float top, float right, float bottom) {
		mInClipRect.set(left, top, right, bottom);
		return projectRectToAABB(mInClipRect);
	}
	
	private void projectRectToAABB(RectF inRect, RectF outRect) {
    	final float[] buf = mShapeBuffer;
    	final int count = 4;
		int i = 0;
		buf[i++] = inRect.left;
		buf[i++] = -inRect.top;
		buf[i++] = 0;
		buf[i++] = inRect.left;
		buf[i++] = -inRect.bottom;
		buf[i++] = 0;
		buf[i++] = inRect.right;
		buf[i++] = -inRect.top;
		buf[i++] = 0;
		buf[i++] = inRect.right;
		buf[i++] = -inRect.bottom;
		buf[i++] = 0;
    	projectToWindow(buf, 0, count, null, 0, buf, 0);
    	float xMin = buf[0], xMax = buf[0];
    	float yMin = buf[1], yMax = buf[1];
		for (i = 1; i < count; ++i) {
			xMin = Math.min(xMin, buf[i * 2]);
			xMax = Math.max(xMax, buf[i * 2]);
			yMin = Math.min(yMin, buf[i * 2 + 1]);
			yMax = Math.max(yMax, buf[i * 2 + 1]);
		}
		outRect.set(xMin - mWorldX, yMin - mWorldY, xMax - mWorldX, yMax - mWorldY);
	}
	
	/**
	 * 保存当前的模型矩阵和裁剪状态到堆栈中
	 * <br>注意：需要调用{@link #restore()}或者{@link #restoreToCount(int)}恢复
	 * @return	返回值可用作{@link #restoreToCount()}的参数，以恢复到本次保存之前的状态 
	 */
	public int save() {
		if (mSaveCount + 1 >= mSize) {
			throw new RuntimeException("stack overflow.");
		}
		System.arraycopy(mModelMatrix, mPtr + mBias, mModelMatrix, mPtr + MC - mBias, MC);
		mPtr += MC;
		mBias = 0;
		
		mClipRectStack[mSaveCount + 1].set(mClipRectStack[mSaveCount]);
		mStencilClipRectStack[mSaveCount + 1].set(mStencilClipRectStack[mSaveCount]);
		mStencilIdStack[mSaveCount + 1] = mStencilIdStack[mSaveCount];
		
		++mSaveCount;
		return mSaveCount - 1;
	}
	
	/**
	 * 设置临时图层
	 * <br>{@link #saveLayer(int, int, int, int, int)}的辅助版本
	 */
	public int saveLayer(Rect rect, int flag) {
		return saveLayer(rect.left, rect.top, rect.right, rect.bottom, flag);
	}
	
	/**
	 * <br>功能简述: 在{@link #save()}的同时，设置临时图层
	 * <br>功能详细描述: 
	 * <br>注意: 需要与{@link #restore()}或者{@link #restoreToCount(int)}配对使用。
	 * <p>
	 * v========关于嵌套调用的问题：
	 * <br>{@link Canvas#saveLayer(RectF, Paint, int)}嵌套调用时，其实是共用一张位图，包含了两次
	 * 调用的区域的并集，当带有alpha淡出效果时，restore时会对区域作淡出。
	 * <br>下面代码可以看到红色区域和蓝色区域的交集会被减淡两次，而红色区域超出了第一次save时的区域部分
	 * 并没有被裁剪也没有被减淡。
	 * <pre><code>
	 * protected void onDraw(Canvas canvas) {
	 * 	Paint paint = new Paint();
	 * 	int count = canvas.saveLayerAlpha(0, 0, 300, 300, 128, 0);
	 * 	canvas.saveLayerAlpha(200, 200, 500, 500, 128, 0);
	 * 	paint.setColor(Color.RED);
	 * 	canvas.drawRect(0, 0, 400, 400, paint);
	 * 	paint.setColor(Color.BLUE);
	 * 	canvas.drawRect(200, 200, 500, 500, paint);
	 * 	canvas.restoreToCount(count);
	 * }
	 * </code></pre>
	 * 鉴于此，并且为了简化，这个方法就不支持嵌套调用了，即在restore前不能再次调用本方法。
	 * <br>如果确实需要，可以自行使用{@link GLFramebuffer}。
	 * <br>^========
	 * </p>
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param flag 标志量（{@link #LAYER_ALPHA_FLAG}，{@link #LAYER_CLIP_FLAG}，
	 * 		{@link #LAYER_LOCAL_FLAG}等），以及淡化因子的位或。
	 * @return	返回值可用作{@link #restoreToCount()}的参数，以恢复到本次保存之前的状态 
	 */
	public int saveLayer(int left, int top, int right, int bottom, int flag) {
		if (mLayerSavedCount > LAYER_UNSAVE_COUNT) {
			throw new UnsupportedOperationException("Multiple-Layers is not supported.");
		}
		mLayerFlag = flag;
		
		mLayerSavedCount = save();
		mLayerToDisableStencil = false;
		if ((mLayerFlag & LAYER_LOCAL_FLAG) == LAYER_LOCAL_FLAG) {
			mLayerFlag &= ~LAYER_CLIP_FLAG;	//LAYER_LOCAL_FLAG覆盖LAYER_CLIP_FLAG标志
			mLayerBox.set(left, top, right, bottom);
		} else {
			if ((mLayerFlag & LAYER_CLIP_FLAG) == LAYER_CLIP_FLAG) {
				//记录裁剪矩形，restore的时候用来裁剪layer
				mLayerClipRect.set(left, top, right, bottom);
				System.arraycopy(mModelMatrix, mPtr + mBias, mLayerMatrix, 0, MC);
			}
			RectF aabb = projectRectToAABB(left, top, right, bottom);
			mLayerBox.set(aabb);
		}
		//回到世界坐标系中修改窗口裁剪区域
		tempSaveMatrix();
		reset();
		mLayerSavedClipRect.set(mClipRectStack[mSaveCount]);
		final int stencilId = mStencilIdStack[mSaveCount];
		mStencilIdStack[mSaveCount] = 0;	//避免已经启用了蒙板裁剪时clipRect方法不能修改窗口裁剪
		clipRect(mLayerBox);
		mStencilIdStack[mSaveCount] = stencilId;
		tempRestoreMatrix();
		
		mLayerSavedCount = save();	//额外的一次save，可以恢复图层内的绘制操作导致的状态修改
		if ((mLayerFlag & LAYER_LOCAL_FLAG) == LAYER_LOCAL_FLAG) {
			reset();	//“摆正”坐标系，即在局部坐标系中绘制图层
		}
		
		mLayerBox.intersect(-mWorldX, -mWorldY, mWindowWidth - mWorldX, mWindowHeight - mWorldY);
		mLayerToDisableStencil = true;

		mLayer = mLayers[mLayerIndex];
		if (mLayer != null) {
			if (mLayer.getWidthLimit() != mWindowWidth 
				|| mLayer.getHeightLimit() != mWindowHeight) {
				mLayer.clear();
				mLayer = null;
			}
		}
		if (mLayer == null) {
			mLayer = new GLFramebuffer(mWindowWidth, mWindowHeight, true, 16, 0, false);
			mLayer.register();
			mLayers[mLayerIndex] = mLayer;
		}
		mLayerIndex = (mLayerIndex + 1) % mLayers.length;
		mLayer.setCaptureRectPosition(mWorldX, mWorldY);
		mLayer.bind(this);
		mLayerSavedAlpha = getAlpha();
		setAlpha(FULL_ALPHA);
		mLayerSavedStencilEnabled = isStencilEnable();
		setStencilEnable(false);
		
		final int savedClearColor = getClearColor();
		setClearColor(0);
		clearBuffer(true, true, false, true);
		setClearColor(savedClearColor);
		
		return mLayerSavedCount;
	}

	/**
	 * 恢复到上次{@link #save()}之前的模型矩阵和裁剪状态
	 */
	public void restore() {
		if (mSaveCount <= 0) {
			throw new RuntimeException("stack underflow.");
		}
		
		if (mLayerSavedCount >= mSaveCount - 1) {
			restoreToCount(mSaveCount - 2);
			return;
		}
		
		mPtr -= MC;
		mBias = 0;
		--mSaveCount;
		restoreClipRect(mSaveCount + 1, mSaveCount);
	}

	/**
	 * 以比{@link #restore()} 更高效的方式，恢复到<var>saveCount</var>对应的{@link #save()}之前的模型矩阵和裁剪状态
	 * reached saveCount.
	 * 
	 * @param saveCount
	 * @see {@link #save()}
	 */
	public void restoreToCount(int saveCount) {
		if (saveCount < 0) {
			throw new RuntimeException("stack underflow.");
		}
		if (saveCount >= mSaveCount) {
			throw new RuntimeException("saveCount " + saveCount + " is not less than current count.");
		}
		
		if (mLayerSavedCount >= saveCount) {
			if (mLayerSavedCount + 1 < mSaveCount) {
				restoreToCount(mLayerSavedCount + 1);
			}
			restoreLayer();
		}
		
		mPtr = MC * saveCount;
		mBias = 0;
		final int curCount = mSaveCount;
		mSaveCount = saveCount;
		restoreClipRect(curCount, mSaveCount);
	}
	
	/**
	 * 移除临时图层，并将其内容绘制出来
	 */
	private void restoreLayer() {
		mLayerSavedCount = LAYER_UNSAVE_COUNT;
		mLayer.unbind(this);
		restore();
		if ((mLayerFlag & LAYER_LOCAL_FLAG) != LAYER_LOCAL_FLAG) {
			//非local情况下，要把图层“摆正”来绘制
			reset();
		}
		mLayerToDisableStencil = false;
		setAlpha(mLayerSavedAlpha);
		setStencilEnable(mLayerSavedStencilEnabled);
		setClipRect(mLayerSavedClipRect);
		if (mLayerBox.width() > 0 && mLayerBox.height() > 0) {
			if ((mLayerFlag & LAYER_ALPHA_FLAG) == LAYER_ALPHA_FLAG) {
				multiplyAlpha(mLayerFlag & LAYER_ALPHA_MASK);
			}
			if ((mLayerFlag & LAYER_CLIP_FLAG) == LAYER_CLIP_FLAG) {
				System.arraycopy(mLayerMatrix, 0, mModelMatrix, mPtr + mBias, MC);
				clipRect(mLayerClipRect);
				reset();
			}
			
			BitmapGLDrawable drawable = mLayer.getDrawable();
//			drawable.draw(this);	//把整个图层都绘制出来，自动裁剪
			//只绘制mLayerBox部分，如果对Layer设置了后期处理，那么会减少不必要的区域处理
			//在使用LAYER_LOCAL_FLAG的情况下，可以对旋转的图层正确地做后期处理
			RectF boundsBak = mInClipRect;
			RectF texcoordBak = mOutClipRect;
			drawable.setPartiallyDraw(mLayerBox, boundsBak, texcoordBak);
			drawable.draw(this);
			drawable.setBounds((int) boundsBak.left, (int) boundsBak.top, (int) boundsBak.right, (int) boundsBak.bottom);
			drawable.setTexCoord(texcoordBak.left, texcoordBak.top, texcoordBak.right, texcoordBak.bottom);
			
			if ((mLayerFlag & LAYER_ALPHA_FLAG) == LAYER_ALPHA_FLAG) {
				setAlpha(mLayerSavedAlpha);
			}
		}

        /**
         * @date 2013-12-26
         * @author dengweiming
         * 使用了GLCanvas.saveLayer()功能后，Nexus 4进入屏幕预览如果绘制了顶部的提示文字就会闪烁，
         * 不知怎么就污染了深度缓冲区，但是dock栏清除深度缓冲区的操作被优化掉了，所以后续使用深度测试
         * 就有问题，但是也不知怎么的会影响到屏幕预览的区域。这里强制设置深度缓冲区为脏的保证后续不会
         * 优化掉清除操作。
         * <br>另外，一台LG P970在屏幕预览里添加第二个全屏面板（使用了saveLayer）时，就会导致dock栏花掉，
         * 或者是有两个全屏面板时横竖屏切换两次也会，同样这样也可以修复，但是对每个面板使用一个图层也会没问题。
         */
		addRenderable(GLCommandFactory.get(GLCommandFactory.DEPTH_BUFFER_DIRTY), null);
	}

	/**
	 * 修改模型矩阵为单位矩阵，即 M' = I
	 */
	public void reset() {
		Matrix.setIdentityM(mModelMatrix, mPtr + mBias);
	}

	/**
	 * 修改模型矩阵为指定矩阵，即 M' = m
	 * 
	 * @param m
	 * @param offset
	 */
	public void setMatrix(float[] m, int offset) {
		System.arraycopy(m, offset, mModelMatrix, mPtr + mBias, MC);
	}

	/**
	 * 获取模型变换矩阵，即 m = M
	 * 
	 * @param m
	 * @param offset
	 */
	public void getMatrix(float[] m, int offset) {
		System.arraycopy(mModelMatrix, mPtr + mBias, m, offset, MC);
	}

	/**
	 * 连接一个2D矩阵，注意，如果是从{@link android.graphics.Camera}中获取的，其投影效果会被忽略
	 * @param matrix
	 */
	public void concat(android.graphics.Matrix matrix) {
		convertMatrix2DTo3D(matrix, mTmpMatrix, 0);
		concat(mTmpMatrix, 0);
	}

	/**
	 * 模型矩阵右乘以指定矩阵，即 M' = M * m
	 * 
	 * @param m
	 * @param offset
	 */
	public void concat(float[] m, int offset) {
		Matrix.multiplyMM(mModelMatrix, mPtr + MC - mBias, mModelMatrix, mPtr + mBias, m, offset);
		mBias = MC - mBias;
	}

	/**
	 * 模型矩阵左乘以指定矩阵，即 M' = m * M
	 * 
	 * @param m
	 * @param offset
	 */
	public void postConcat(float[] m, int offset) {
		Matrix.multiplyMM(mModelMatrix, mPtr + MC - mBias, m, offset, mModelMatrix, mPtr + mBias);
		mBias = MC - mBias;
	}

	/**
	 * 平移当前坐标系。
	 * <br>实际上，模型矩阵右乘以一个平移矩阵，即 M' = M * T(x, y, z)
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void translate(float x, float y, float z) {
		Matrix.translateM(mModelMatrix, mPtr + mBias, x, y, z);
	}

	/**
	 * 2D版本的平移当前坐标系。
	 * <br>实际上，模型矩阵右乘以一个平移矩阵，即 M' = M * T(x, -y, 0)
	 * 
	 * @param x
	 * @param y Y轴方向向下
	 */
	public void translate(float x, float y) {
		Matrix.translateM(mModelMatrix, mPtr + mBias, x, -y, 0);
	}

	/**
	 * 将物体在摄像机空间（右手性）上移动(x, y, z)，物体的朝向对其无影响
	 * M' = T(x, y, z) * M，比{@link #postConcat(float[], int)高效些
	 * @param x
	 * @param y
	 * @param z
	 */
	public void postTranslate(float x, float y, float z) {
		int offset = mPtr + mBias + M03;
		mModelMatrix[offset++] += x;
		mModelMatrix[offset++] += y;
		mModelMatrix[offset++] += z;
	}

	/**
	 * M' = M * S(sx, sy, sz)
	 * 
	 * @param sx
	 * @param sy
	 * @param sz
	 */
	public void scale(float sx, float sy, float sz) {
		Matrix.scaleM(mModelMatrix, mPtr + mBias, sx, sy, sz);
	}

	/**
	 * M' = M * S(sx, sy, 1)
	 * 
	 * @param sx
	 * @param sy
	 * @param sz
	 */
	public void scale(float sx, float sy) {
		Matrix.scaleM(mModelMatrix, mPtr + mBias, sx, sy, 1);
	}

	/**
	 * 以(px, py)为中心进行2D缩放
	 * @param sx
	 * @param sy
	 * @param px
	 * @param py
	 */
	public void scale(float sx, float sy, float px, float py) {
		Matrix.translateM(mModelMatrix, mPtr + mBias, px, -py, 0);

		Matrix.scaleM(mModelMatrix, mPtr + mBias, sx, sy, 1);

		Matrix.translateM(mModelMatrix, mPtr + mBias, -px, py, 0);
	}

	/**
	 * <p>Rotates by angle a (in degrees) around the axis (x, y, z) <u>counterclockwise</u></p>
	 * M' = M * R(a, x, y, z)
	 * 
	 * @param a angle to rotate in degrees
	 * @param x scale factor x
	 * @param y scale factor y
	 * @param z scale factor z
	 */
	public void rotateAxisAngle(float a, float x, float y, float z) {
		Matrix.setRotateM(mTmpMatrix, 0, a, x, y, z);
		Matrix.multiplyMM(mModelMatrix, mPtr + MC - mBias, mModelMatrix, mPtr + mBias, mTmpMatrix,
				0);
		mBias = MC - mBias;
	}

	/**
	 * <p>Rotates by Euler angles <u>counterclockwise</u></p>
	 * M' = M * R(x, 1, 0, 0) * R(y, 0, 1, 0) * R(z, 0, 0, 1)
	 * 
	 * @param x angle of rotation, in degrees
	 * @param y angle of rotation, in degrees
	 * @param z angle of rotation, in degrees
	 */
	public void rotateEuler(float x, float y, float z) {
		setRotateEulerM(mTmpMatrix, 0, x, y, z); //Matrix.setRotateEulerM的实现是错的
		Matrix.multiplyMM(mModelMatrix, mPtr + MC - mBias, mModelMatrix, mPtr + mBias, mTmpMatrix,
				0);
		mBias = MC - mBias;
	}

	/**
	 * <p>Rotates by angle a (in degrees) around the Z-axis <u>clockwise</u></p>
	 * M' = M * R(-a, 0, 0, 1)
	 * 
	 * @param a angle to rotate in degrees
	 */
	public void rotate(float a) {
		Matrix.setRotateM(mTmpMatrix, 0, -a, 0, 0, 1);
		Matrix.multiplyMM(mModelMatrix, mPtr + MC - mBias, mModelMatrix, mPtr + mBias, mTmpMatrix,
				0);
		mBias = MC - mBias;
	}

	/**
	 * <p>Rotates by angle a (in degrees) around the Z-axis through center (px, py) <u>clockwise</u></p>
	 * @param a
	 * @param px
	 * @param py
	 */
	public void rotate(float a, float px, float py) {
		Matrix.translateM(mModelMatrix, mPtr + mBias, px, -py, 0);

		Matrix.setRotateM(mTmpMatrix, 0, -a, 0, 0, 1);
		Matrix.multiplyMM(mModelMatrix, mPtr + MC - mBias, mModelMatrix, mPtr + mBias, mTmpMatrix,
				0);
		mBias = MC - mBias;

		Matrix.translateM(mModelMatrix, mPtr + mBias, -px, py, 0);
	}
	
	/**
	 * <br>功能简述: 获取总的水平平移量
	 * <br>功能详细描述:
	 * <br>注意:获取的当前的模型变换矩阵中的水平平移量，如果变换中包含了非平移的变换，那么返回值就不准确了。
	 * @return
	 */
	public float getTotalTranslateX() {
		return mModelMatrix[mPtr + mBias + M03];
	}
	
	/**
	 * <br>功能简述: 获取总的垂直平移量
	 * <br>功能详细描述:
	 * <br>注意:获取的当前的模型变换矩阵中的垂直平移量，如果变换中包含了非平移的变换，那么返回值就不准确了。
	 * @return
	 */
	public float getTotalTranslateY() {
		return mModelMatrix[mPtr + mBias + M13];
	}
	
	/**
	 * <br>功能简述: 获取总的深度平移量
	 * <br>功能详细描述:
	 * <br>注意:获取的当前的模型变换矩阵中的深度平移量，如果变换中包含了非平移的变换，那么返回值就不准确了。
	 * @return
	 */
	public float getTotalTranslateZ() {
		return mModelMatrix[mPtr + mBias + M23];
	}
	
	
	public boolean mapRect(RectF inRect, RectF outRect) {
		return mapRect(mModelMatrix, mPtr + mBias, inRect, outRect);
	}
    
    /**
     * <br>功能简述:计算一个矩形区域变换并投影后的矩形区域
     * <br>功能详细描述:
     * <br>注意:
     * @param m
     * @param offset
     * @param inRect
     * @param outRect
     * @return	变换后是否还是轴对齐的矩形区域，为false的时候，<var>outRect</var>的值无效（不会被修改）
     */
    public boolean mapRect(float[] m, int offset, RectF inRect, RectF outRect) {
//		if (m[offset + M20] != 0 || m[offset + M21] != 0) {
		if (!equalToZero(m[offset + M20]) || !equalToZero(m[offset + M21])) {	//允许一点误差
			//这样会使点(x, y, 0, 1)变换成(x', y', z, 1)，其中z=f(x,y)是变值，例如绕y轴旋转的时候
			return false;
		}
		final float m00 = m[offset + M00];
		final float m10 = m[offset + M10];
		final float m01 = m[offset + M01];
		final float m11 = m[offset + M11];
		/** formatter:off */
		//综合对绕z轴旋转0,90,180,270度时（并可以缩放）的分析，矩阵左上角四个元素必须满足以下条件
		if (equalToZero(m00 * m10) 
			&& equalToZero(m00 * m01) 
			&& equalToZero(m10 * m11) 
			&& equalToZero(m01 * m11) 
			&& !equalToZero(m00 * m00 + m10 * m10)
			&& !equalToZero(m01 * m01 + m11 * m11)) {
			
	    	final float[] c = mCameraPos;
	    	final float scale = c[2] / (c[2] - m[offset + M23]);
	    	
			float x1 = (m00 * inRect.left  + m01 * -inRect.top    + m[offset + M03] - c[0]) * scale + c[0];
			float y1 = (m10 * inRect.left  + m11 * -inRect.top    + m[offset + M13] - c[1]) * scale + c[1];
			float x2 = (m00 * inRect.right + m01 * -inRect.bottom + m[offset + M03] - c[0]) * scale + c[0];
			float y2 = (m10 * inRect.right + m11 * -inRect.bottom + m[offset + M13] - c[1]) * scale + c[1];
			if (x1 > x2) {
				final float tmp = x1;
				x1 = x2;
				x2 = tmp;
			}
			if (y1 < y2) {
				final float tmp = y1;
				y1 = y2;
				y2 = tmp;
			}
			outRect.left   = Math.round(x1)  + mWorldX;
			outRect.right  = Math.round(x2)  + mWorldX;
			outRect.top    = Math.round(-y1) + mWorldY;
			outRect.bottom = Math.round(-y2) + mWorldY;
			return true;
		}
		/** formatter:on */
    	return false;
    }
    
	/**
	 * 将当前模型矩阵转化为字符串以便打印
	 * 
	 * @return
	 */
	public String matrixToString() {
		return matrixToString(mStringBuilder, mModelMatrix, mPtr + mBias);
	}

	/**
	 * 将指定矩阵转化为字符串以便打印
	 * 
	 * @param m
	 * @param offset
	 * @return
	 */
	public static String matrixToString(float[] m, int offset) {
		return matrixToString(sStringBuilder, m, offset);
	}

	private static String matrixToString(StringBuilder sb, float[] m, int offset) {
		final int p = offset;
		sb.delete(0, sb.length());
		sb.append("{[");
		/** @formatter:off */
        sb.append(m[p + M00]); sb.append(", "); sb.append(m[p + M01]); sb.append(", ");
        sb.append(m[p + M02]); sb.append(", "); sb.append(m[p + M03]); sb.append("] [");
        
        sb.append(m[p + M10]); sb.append(", "); sb.append(m[p + M11]); sb.append(", ");
        sb.append(m[p + M12]); sb.append(", "); sb.append(m[p + M13]); sb.append("] [");
        
        sb.append(m[p + M20]); sb.append(", "); sb.append(m[p + M21]); sb.append(", ");
        sb.append(m[p + M22]); sb.append(", "); sb.append(m[p + M23]); sb.append("] [");
        
        sb.append(m[p + M30]); sb.append(", "); sb.append(m[p + M31]); sb.append(", ");
        sb.append(m[p + M32]); sb.append(", "); sb.append(m[p + M33]); sb.append("]}");
        /** @formatter:on */
		return sb.toString();
	}

	public static void copyMatrix(float[] src, int srcOffset, float[] dst, int dstOffset) {
		System.arraycopy(src, srcOffset, dst, dstOffset, MC);
	}

	/**
	 * 将2D的矩阵转成3D的矩阵
	 * @param matrix
	 * @param m
	 * @param offset
	 * @return 2D矩阵是否具有投影效果，如果是，那么会被忽略
	 */
	public static boolean convertMatrix2DTo3D(android.graphics.Matrix matrix, float[] m, int offset) {
		/*
		 * 给Matrix2D添加第3列[0 0 0 0], 以及第4行[0 0 0 1]，
		 * 注意Matrix2D按行优先存储, Matrix3D按列优先存储。
		 * Matrix2D其实也是一个右手3D坐标系（使用android.graphics.Camera时可以实现z轴上的变化），
		 * y轴向下，z轴向屏幕里面，相当于标准3D坐标系绕x轴旋转180度，或者将y轴和z轴缩放-1倍，
		 * 即X=Scale(1, -1, -1)，那么变换m从Camera的空间变换到标准空间为m'=Xm(X^-1)。
		 * 
		 * 矩阵乘以某轴上的缩放矩阵，等价于将该轴对应的列元素全部乘以缩放比(-1)，
		 * 某轴上的缩放矩阵乘以一个矩阵，等价于将该轴对应的行元素全部乘以缩放比(-1)，
		 * 综合起来就是将第2,3行/列的元素都各取反一次，注意中间的2x2子矩阵的元素取反两次相当于不用操作。
		 */
		final float[] tm = sTmpMatrix;
		matrix.getValues(tm);
		m[offset + M00] = tm[0];
		m[offset + M01] = -tm[1];
		m[offset + M02] = 0;
		m[offset + M03] = tm[2];
		
		m[offset + M10] = -tm[3];	//CHECKSTYLE IGNORE
		m[offset + M11] = tm[4];	//CHECKSTYLE IGNORE
		m[offset + M12] = 0;
		m[offset + M13] = -tm[5];	//CHECKSTYLE IGNORE
		
		m[offset + M20] = 0;
		m[offset + M21] = 0;
		m[offset + M22] = 1;
		m[offset + M23] = 0;
		
		m[offset + M30] = 0;
		m[offset + M31] = 0;
		m[offset + M32] = 0;
		m[offset + M33] = 1;
		
		return tm[6] != 0 || tm[7] != 0;	//CHECKSTYLE IGNORE
		
	}
    
    /**
     * <br>功能简述:判断一个浮点数在误差{@link #EPSILON}之内等于0
     * <br>功能详细描述:
     * <br>注意:
     * @param value
     * @return
     */
    public static boolean equalToZero(float value) {
    	return value > -EPSILON && value < EPSILON;
    }

	//===============================for matrix =============^
    
    //===============================for clipping =============v
    private final RectF[] mClipRectStack;
    /** 使用蒙板测试来裁剪时的裁剪区域形状，目前只支持矩形，未来有需要可以扩展为其他形状 */
    private final RectF[] mStencilClipRectStack;
    private final int[] mStencilIdStack;
    private final RectF mInClipRect = new RectF();
    private final RectF mOutClipRect = new RectF();
    
    private static final int STENCIL_BUFFER_BITS_MASK = (1 << 8) - 1; //一般都只有8位蒙板缓冲区	//CHECKSTYLE IGNORE
    private static final int STENCIL_MAX_CLIP_DEPTH = 64;	//使用蒙板缓冲区时最多的裁剪层次
	private static final int STENCIL_ID_MASK = 0xFFFF;
    private static final int STENCIL_RESTORE_MASK = 1 << 16;	//CHECKSTYLE IGNORE
    private static final int STENCIL_CLEAR_MASK = 1 << 17;	//CHECKSTYLE IGNORE
    private int mStencilIdAllocated;
    private int mLastStencilIdHasClearMask;
    private boolean mClipRegionStarted;
    
    private final Renderable mClipRectRenderable = new Renderable() {
		
		@Override
		public void run(long timeStamp, RenderContext context) {
			//CHECKSTYLE IGNORE 4 LINES
			final int x = Math.round(context.color[ViewportX]);
			final int y = Math.round(context.color[ViewportY]);
			final int w = Math.round(context.color[ViewportW]);
			final int h = Math.round(context.color[ViewportH]);
			
			GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
			GLES20.glScissor(x, y, w, h);
		}
	};

	/**
	 * 在当前坐标系裁剪区域
	 * 裁剪区域的状态保存在状态堆栈中，即可以使用 {@link #save()} 和 {@link #restore()} 来保存和恢复。
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return
	 */
	public boolean clipRect(float left, float top, float right, float bottom) {
		mInClipRect.set(left, top, right, bottom);
		return clipRect(mInClipRect);
	}

	/**
	 * 在当前坐标系裁剪区域
	 * 裁剪区域的状态保存在状态堆栈中，即可以使用 {@link #save()} 和 {@link #restore()} 来保存和恢复。
	 * @param rect
	 * @return
	 */
	public boolean clipRect(Rect rect) {
		mInClipRect.set(rect.left, rect.top, rect.right, rect.bottom);
		return clipRect(mInClipRect);
	}
	
	
	/**
	 * 在当前坐标系裁剪区域
	 * 裁剪区域的状态保存在状态堆栈中，即可以使用 {@link #save()} 和 {@link #restore()} 来保存和恢复。
	 * @param rect
	 * @return
	 */
	public boolean clipRect(RectF rect) {
		final int stencilId = mStencilIdStack[mSaveCount];
		final boolean canClip = stencilId == 0 && mapRect(rect, mOutClipRect);
		if (canClip) {
			//裁剪区域在屏幕上的投影是矩形，使用scissor test来裁剪
			final RectF curClipRect = mClipRectStack[mSaveCount];
			if (!curClipRect.intersect(mOutClipRect)) {
				//全部裁剪
				curClipRect.right = curClipRect.left;
				curClipRect.bottom = curClipRect.top;
			}
			setClipRect(curClipRect);
			return true;
		} else {
			if (mClipRegionStarted) {
				throw new RuntimeException("Clip region has already started.");
			}
			//裁剪区域在屏幕上的投影不是矩形，而是普通的凸四边形，使用（并且后续都使用）stencil test实现裁剪
			mStencilClipRectStack[mSaveCount].set(rect);
			return setStencilClipRect(rect, updateStencilId(stencilId));
		}
	}
	
	/**
	 * <br>功能简述: 开始裁剪区域
	 * <br>功能详细描述: 要和{@link #finishClipRegion()} 配合使用，中间调用绘制图形的代码，
	 * 例如fillTriangle等等，绘制的图形的并集作为此次裁剪的区域。
	 * <br>注意: 必须要和{@link #finishClipRegion()} 配合使用，中间不能嵌套调用（否则会抛出异常），
	 * 而且不要调用save, restore等（结果未定义）。
	 */
	public void startClipRegion() {
		if (mClipRegionStarted) {
			throw new RuntimeException("Clip region has already started.");
		}
		mClipRegionStarted = true;
		
		ColorShader shader = ColorShader.getShader();
		if (shader == null) {
			return;
		}
		setStencilEnable(true);
		
		int stencilId = mStencilIdStack[mSaveCount];
		if (stencilId == 0) {
			//使用一个足够大的矩形使得restore之后clipRegion还有效
			float infinity = Integer.MAX_VALUE / 2 - 1;
			mStencilClipRectStack[mSaveCount].set(-infinity, -infinity, infinity, infinity);
		}
		int stencilValue = updateStencilId(stencilId);
		
		RenderContext context = RenderContext.acquire();
		context.shader = shader;
		context.alpha = stencilValue;
		getFinalMatrix(context);
		
		addRenderable(mStartClipRegionRenderable, context);
	}
	
	/**
	 * <br>功能简述: 完成裁剪区域
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void finishClipRegion() {
		if (!mClipRegionStarted) {
			throw new RuntimeException("Clip region has not yet started.");
		}
		mClipRegionStarted = false;
		addRenderable(mFinishClipRegionRenderable, null);
	}
	
	private int updateStencilId(int stencilId) {
		mStencilIdStack[mSaveCount] = ++mStencilIdAllocated;
		if (stencilId == 0) {
			//为了避免后续的裁剪id低8位产生回绕，导致大小判断错误，需要将当前id低8位重设为1
			if ((mStencilIdAllocated & STENCIL_BUFFER_BITS_MASK) 
					+ STENCIL_MAX_CLIP_DEPTH >= STENCIL_BUFFER_BITS_MASK) {
				mStencilIdAllocated |= STENCIL_BUFFER_BITS_MASK;
				mStencilIdAllocated += 2;
				mStencilIdStack[mSaveCount] = mStencilIdAllocated;
			}
			
			if (mStencilIdAllocated >= STENCIL_ID_MASK) {
				throw new RuntimeException("GLCanvas clipRect (with stencil) too much times.");
			}
			
			mLastStencilIdHasClearMask = mStencilIdAllocated;
			return mStencilIdStack[mSaveCount] | STENCIL_CLEAR_MASK;
		} else {
			if (mStencilIdAllocated >= STENCIL_ID_MASK) {
				throw new RuntimeException("GLCanvas clipRect (with stencil) too much times.");
			}
			if (mStencilIdAllocated - mLastStencilIdHasClearMask >= STENCIL_MAX_CLIP_DEPTH) {
				throw new RuntimeException("GLCanvas clipRect (with stencil) stack overflow.");
			}
			
			return mStencilIdStack[mSaveCount];
		}
	}
	
	/**
	 * <br>功能简述: 设置窗口裁剪
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param rect
	 */
	private void setClipRect(RectF rect) {
		if (rect.left == 0 
				&& rect.top == 0 
				&& rect.right == mWindowWidth
				&& rect.bottom == mWindowHeight) {
			if ((mGLStateFlag & SCISSOR_TEST) != 0) {
				mGLStateFlag &= ~SCISSOR_TEST;
				addRenderable(GLCommandFactory.get(GLCommandFactory.SCISSOR_TEST_DISABLE), null);
			}
			return;
		}
		mGLStateFlag |= SCISSOR_TEST;
		RenderContext context = RenderContext.acquire();
		final float[] box = context.color;
		
		if (!mClipRectMapToViewport) {
			box[ViewportX] = rect.left;
			box[ViewportY] = mWindowHeight - rect.bottom;
			box[ViewportW] = rect.right - rect.left;
			box[ViewportH] = rect.bottom - rect.top;
		} else {
			float sx = mViewport[ViewportW] / (float) mWindowWidth;
			float sy = mViewport[ViewportH] / (float) mWindowHeight;
			box[ViewportX] = rect.left * sx + mViewport[ViewportX];
			box[ViewportY] = (mWindowHeight - rect.bottom) * sy + mViewport[ViewportY];
			box[ViewportW] = Math.max((rect.right - rect.left) * sx, 0);
			box[ViewportH] = Math.max((rect.bottom - rect.top) * sy, 0);
		}
		
		addRenderable(mClipRectRenderable, context);
	}
	
	private void restoreClipRect(int curCount, int savedCount) {
		final int stencilId = mStencilIdStack[savedCount];
		if (stencilId == 0) {
			final RectF savedRect = mClipRectStack[savedCount];
			final RectF curRect = mClipRectStack[curCount];
			if (curRect.left != savedRect.left || curRect.top != savedRect.top
					|| curRect.right != savedRect.right
					|| curRect.bottom != savedRect.bottom) {
				setClipRect(savedRect);
			}
		}
		
		if (stencilId != mStencilIdStack[curCount]) {
			setStencilClipRect(mStencilClipRectStack[savedCount], stencilId | STENCIL_RESTORE_MASK);
		}
	}
	
	
	/**
	 * save currrent matrix to tmp
	 */
	private void tempSaveMatrix() {
		System.arraycopy(mModelMatrix, mPtr + mBias, mTmpMatrix, 0, MC);
	}
	
	/**
	 * restore currrent matrix from tmp
	 */
	private void tempRestoreMatrix() {
		System.arraycopy(mTmpMatrix, 0, mModelMatrix, mPtr + mBias, MC);
	}
	
	private boolean setStencilClipRect(RectF rect, int stencilValue) {
		if ((stencilValue & STENCIL_ID_MASK) == 0) {
			setStencilEnable(false);
		} else {
			if ((stencilValue & STENCIL_RESTORE_MASK) != 0) {
				//在使用蒙板的情况下多次调用clipRect，需要使mStencilIdAllocated持续增长
				//而restore时应该恢复，使得下一层的ref值仅仅增加1,以保证glStencilOp中使用GL_INC是正确的
				mStencilIdAllocated = stencilValue & STENCIL_ID_MASK;
			}
			
			//在使用临时图层时禁用蒙板裁剪，参见saveLayer()方法
			if (mLayerSavedCount > LAYER_UNSAVE_COUNT && mLayerToDisableStencil) {
				return false;
			}
			
			ColorShader shader = ColorShader.getShader();
			if (shader == null) {
				return false;
			}
			setStencilEnable(true);
			
			RenderContext context = RenderContext.acquire();
			context.shader = shader;
			context.alpha = stencilValue;
			tempSaveMatrix();
			//裁剪的时候使用了一个单位正方形（以避免更改顶点数据），使用模型变换来实现矩形的效果
			translate(rect.left, rect.top);
			scale(rect.width(), rect.height());
			getFinalMatrix(context);
			tempRestoreMatrix();
			addRenderable(mStencilClipRenderable, context);
		}
		return true;
	}
	
	private final Renderable mStencilClipRenderable = new Renderable() {
		//CHECKSTYLE IGNORE 1 LINES
		final FloatBuffer vertexBuffer = IBufferFactory.newFloatBuffer(new float[] {
				0, 0, 0, // left-top
				0, -1, 0, // left-bottom
				1, 0, 0, // right-top
				1, -1, 0, // right-bottom
		});
		
		@Override
		public void run(long timeStamp, RenderContext context) {
			mStartClipRegionRenderable.run(timeStamp, context);
			
			//draw clip region
			ColorShader shader = (ColorShader) context.shader;
			if (shader != null && shader.bind()) {
				if (STENCIL_DBG_COLOR != 0) {
					shader.setColor(STENCIL_DBG_COLOR);
				}
				shader.setMatrix(context.matrix, 0);
				shader.setPosition(vertexBuffer, GLDrawable.POSITION_COMPONENT);
				GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, GLDrawable.VERTEX_COUNT);
			}
			
			mFinishClipRegionRenderable.run(timeStamp, context);
		}
	};
	
	private int mTempStencilIdOnGLThread;
	
	private final Renderable mStartClipRegionRenderable = new Renderable() {
		@Override
		public void run(long timeStamp, RenderContext context) {
			int stencilValue = (int) context.alpha;
			final boolean restore = (stencilValue & STENCIL_RESTORE_MASK) != 0;
			final boolean clear = (stencilValue & STENCIL_CLEAR_MASK) != 0;
			final int stencilId = stencilValue & STENCIL_ID_MASK;
			if (!restore) {
				if (clear) {
					if ((stencilId & STENCIL_BUFFER_BITS_MASK) == 1) {
						//如果蒙板缓冲区的位数不够大时，会产生回绕，导致大小判断错误，避免这种情况，清除蒙板缓冲区中的脏数据
						//XXX: 如果在一帧中裁剪总次数过多，会导致清除的次数（约等于裁剪总次数/200）也变多，影响效率
						if (stencilId > 1) {
							//每帧初始时已经清除过一次，避免多余的清除
							GLES20.glClear(GLES20.GL_STENCIL_BUFFER_BIT);
						}
					} 
					GLES20.glStencilFunc(GLES20.GL_ALWAYS, stencilId, STENCIL_BUFFER_BITS_MASK);
					GLES20.glStencilOp(GLES20.GL_REPLACE, GLES20.GL_REPLACE, GLES20.GL_REPLACE);
				} else {
					//当且仅当蒙板值 s<=stencilId-1 时才通过测试，即实现 stencilId-1 与 stencilId 对应的区域的求交
					GLES20.glStencilFunc(GLES20.GL_LEQUAL, stencilId - 1, STENCIL_BUFFER_BITS_MASK);
					GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_INCR, GLES20.GL_INCR);
				}
			} else {
				GLES20.glStencilFunc(GLES20.GL_LEQUAL, stencilId, STENCIL_BUFFER_BITS_MASK);
				GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_REPLACE, GLES20.GL_REPLACE);
			}
			if (STENCIL_DBG_COLOR == 0) {
				GLES20.glColorMask(false, false, false, false);
			}
			
			mTempStencilIdOnGLThread = stencilId;
		}
	};
	
	private final Renderable mFinishClipRegionRenderable = new Renderable() {
		@Override
		public void run(long timeStamp, RenderContext context) {
			final int stencilId = mTempStencilIdOnGLThread;
			GLES20.glColorMask(true, true, true, true);
			GLES20.glStencilFunc(GLES20.GL_EQUAL, stencilId, STENCIL_BUFFER_BITS_MASK);
			GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_KEEP);
		}
	};

    //===============================for clipping =============^

	public final static float OneOver255 = 1 / 255.0f; //CHECKSTYLE IGNORE

	int mAlpha = 255; //CHECKSTYLE IGNORE
	private FakeCanvas mFakeCanvas;
	private ColorGLDrawable mColorGLDrawable;
	private HashMap<BitmapDrawable, BitmapGLDrawable> mBitmapDrawableMap;
	private HashMap<NinePatchDrawable, NinePatchGLDrawable> mNinePatchDrawableMap;
	private HashMap<ColorDrawable, ColorGLDrawable> mColorDrawableMap;

	public boolean quickReject(float left, float top, float right, float bottom, EdgeType type) {
		//TODO
		return false;
	}

	/** 没有实现 */
	@Deprecated
	public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
	}

	/** 没有实现 */
	@Deprecated
	public void drawBitmap(Bitmap bitmap, float x, float y, Paint paint) {
	}

	/** 没有实现 */
	@Deprecated
	public void drawText(CharSequence text, float x, float y, Paint paint) {
	}
	
	/**
	 * <br>功能简述: 设置背景的清除颜色
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param color
	 * @hide
	 */
	public void setBackgroundClearColor(int color) {
		mBackgroundClearColor = color;
	}
	
	/**
	 * <br>功能简述: 获取清除颜色
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public int getClearColor() {
		return mCurClearColor;
	}

	/**
	 * <br>功能简述: 设置清除颜色
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param color
	 */
	public void setClearColor(int color) {
		if (mCurClearColor == color) {
			return;
		}
		mCurClearColor = color;
		mClearColorChanged = true;
	}
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:需要保证本方法在GL线程被调用
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 * @hide
	 */
	public void setClearColorOnGLThread(float r, float g, float b, float a) {
		GLState.glClearColor(r, g, b, a);
	}

	/**
	 * 清除OpenGL缓冲区
	 * @param color 是否清除颜色缓冲区
	 * @param depth 是否清除深度缓冲区。如果当前禁止了写深度缓冲区，会自动允许，清除完再禁止。
	 * 	为了在未污染深度缓冲区时，优化掉多余的清除操作，最好是先调用本方法在调用
	 * 	{@link #setDepthEnable(boolean)}或{@link #setDepthMask(boolean)}。
	 * @param stencil 是否清除蒙板缓冲区（暂时未支持）
	 */
	public void clearBuffer(boolean color, boolean depth, boolean stencil) {
		clearBuffer(color, depth, stencil, false);
	}
	
	/**
	 * @param disableScissor	是否禁用裁剪，以保证整个缓冲区都会被清除
	 * @hide
	 */
	public void clearBuffer(boolean color, boolean depth, boolean stencil, boolean disableScissor) {
		final boolean isColorMask = isColorMask();
		if (color && !isColorMask) {
			addRenderable(GLCommandFactory.get(GLCommandFactory.COLOR_MASK_TRUE), null);
		}
		
		final boolean isDepthMask = isDepthMask();
		if (depth && !isDepthMask) {
			addRenderable(GLCommandFactory.get(GLCommandFactory.DEPTH_MASK_TRUE), null);
		}
		
		final boolean scissor = (mGLStateFlag & SCISSOR_TEST) != 0;
		if (scissor && disableScissor) {
			addRenderable(GLCommandFactory.get(GLCommandFactory.SCISSOR_TEST_DISABLE), null);
		}
		
		if (color) {
			if (mClearColorChanged) {
				mClearColorChanged = false;
				RenderContext context = RenderContext.acquire();
				convertColor(mCurClearColor, context);
				Renderable renderable = GLCommandFactory.get(GLCommandFactory.CLEAR_COLOR);
				addRenderable(renderable, context);
			}
			
			if (depth) {
				addRenderable(GLCommandFactory.get(GLCommandFactory.CLEAR_COLOR_DEPTH_BUFFER), null);
			} else {
				addRenderable(GLCommandFactory.get(GLCommandFactory.CLEAR_COLOR_BUFFER), null);
			}
		} else {
			if (depth) {
				addRenderable(GLCommandFactory.get(GLCommandFactory.CLEAR_DEPTH_BUFFER), null);
			}
		}
		
		if (depth && !isDepthMask) {
			addRenderable(GLCommandFactory.get(GLCommandFactory.DEPTH_MASK_FALSE), null);
		}
		
		if (color && !isColorMask) {
			addRenderable(GLCommandFactory.get(GLCommandFactory.COLOR_MASK_FALSE), null);
		}

		if (scissor && disableScissor) {
			addRenderable(GLCommandFactory.get(GLCommandFactory.SCISSOR_TEST_ENABLE), null);
		}
	}
	
	/**
	 * <br>功能简述: 获取颜色缓冲区是否可写
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 * @hide
	 */
	public boolean isColorMask() {
		return (mGLStateFlag & COLOR_MASK) == COLOR_MASK;
	}
	
	/**
	 * <br>功能简述: 设置颜色缓冲区是否可写
	 * <br>功能详细描述:
	 * <br>注意: 默认可写，在禁用了之后注意恢复为可写
	 * @param enabled
	 * @hide
	 */
	public void setColorMask(boolean enabled) {
		if (enabled) {
			if ((mGLStateFlag & COLOR_MASK) != COLOR_MASK) {
				mGLStateFlag |= COLOR_MASK;
				addRenderable(GLCommandFactory.get(GLCommandFactory.COLOR_MASK_TRUE), null);
			}
		} else {
			if ((mGLStateFlag & COLOR_MASK) == COLOR_MASK) {
				mGLStateFlag &= ~COLOR_MASK;
				addRenderable(GLCommandFactory.get(GLCommandFactory.COLOR_MASK_FALSE), null);
			}
		}
	}
	
	/**
	 * <br>功能简述: 获取深度缓冲区是否可写
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isDepthMask() {
		return (mGLStateFlag & DEPTH_MASK) == DEPTH_MASK;
	}

	/**
	 * <br>功能简述: 设置深度缓冲区是否可写
	 * <br>功能详细描述:
	 * <br>注意: 默认不可写，在启用了之后要注意恢复为不可写
	 * @param enabled
	 */
	public void setDepthMask(boolean enabled) {
		if (enabled) {
			if ((mGLStateFlag & DEPTH_MASK) != DEPTH_MASK) {
				mGLStateFlag |= DEPTH_MASK;
				addRenderable(GLCommandFactory.get(GLCommandFactory.DEPTH_MASK_TRUE), null);
				//使用者在启用写深度缓冲区，都认为一定会污染深度缓冲区
				//注意在清除深度缓冲区的时候虽然GLCanvas自动启用写深度缓冲区然后关闭，没有调用到以下这句
				addRenderable(GLCommandFactory.get(GLCommandFactory.DEPTH_BUFFER_DIRTY), null);
			}
		} else {
			if ((mGLStateFlag & DEPTH_MASK) == DEPTH_MASK) {
				mGLStateFlag &= ~DEPTH_MASK;
				addRenderable(GLCommandFactory.get(GLCommandFactory.DEPTH_MASK_FALSE), null);
			}
		}
	}
	
	/**
	 * <br>功能简述: 获取深度测试是否启用（隐含了深度缓冲区是否可写）
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isDepthEnabled() {
		return (mGLStateFlag & DEPTH_TEST) == DEPTH_TEST;
	}

	/**
	 * <br>功能简述: 设置是否启用深度测试和写深度缓冲区。
	 * <br>功能详细描述:
	 * <br>注意: 默认是禁用的，在启用后绘制了3D物体要注意恢复为禁用。
	 * @param enabled
	 */
	public void setDepthEnable(boolean enabled) {
		if (enabled) {
			if ((mGLStateFlag & DEPTH_TEST) != DEPTH_TEST) {
				mGLStateFlag |= DEPTH_TEST;
				addRenderable(GLCommandFactory.get(GLCommandFactory.DEPTH_TEST_ENABLE), null);
			}
		} else {
			if ((mGLStateFlag & DEPTH_TEST) == DEPTH_TEST) {
				mGLStateFlag &= ~DEPTH_TEST;
				addRenderable(GLCommandFactory.get(GLCommandFactory.DEPTH_TEST_DISABLE), null);
			}
		}
		setDepthMask(enabled);
	}
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 * @hide
	 */
	public boolean isStencilEnable() {
		return (mGLStateFlag & STENCIL_TEST) == STENCIL_TEST;
	}
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param enabled
	 * @hide
	 */
	public void setStencilEnable(boolean enabled) {
		if (enabled) {
			if ((mGLStateFlag & STENCIL_TEST) != STENCIL_TEST) {
				mGLStateFlag |= STENCIL_TEST;
				addRenderable(GLCommandFactory.get(GLCommandFactory.STENCIL_TEST_ENABLE), null);
			}
		} else {
			if ((mGLStateFlag & STENCIL_TEST) == STENCIL_TEST) {
				mGLStateFlag &= ~STENCIL_TEST;
				addRenderable(GLCommandFactory.get(GLCommandFactory.STENCIL_TEST_DISABLE), null);
			}
		}
	}

	/**
	 * <br>功能简述: 获取是否启用了背面剔除
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isCullFaceEnabled() {
		return (mGLStateFlag & CULL_FACE) == CULL_FACE;
	}
	
	/**
	 * <br>功能简述: 设置是否启用背面剔除。
	 * <br>功能详细描述:
	 * <br>注意: 默认是启用的，在禁用后绘制了物体后要注意恢复为启用。
	 * @param enabled
	 */
	public void setCullFaceEnabled(boolean enabled) {
		if (enabled) {
			if ((mGLStateFlag & CULL_FACE) != CULL_FACE) {
				mGLStateFlag |= CULL_FACE;
				addRenderable(GLCommandFactory.get(GLCommandFactory.CULL_FACE_ENABLE), null);
			}
		} else {
			if ((mGLStateFlag & CULL_FACE) == CULL_FACE) {
				mGLStateFlag &= ~CULL_FACE;
				addRenderable(GLCommandFactory.get(GLCommandFactory.CULL_FACE_DISABLE), null);
			}
		}
	}
	
	/**
	 * <br>功能简述: 获取是否是剔除背面
	 * <br>功能详细描述: 
	 * <br>注意: {@link #isCullFaceEnabled()} 用来判断是否启用剔除，本方法用来判断剔除的是正面还是背面
	 */
	public boolean isCullBackFace() {
		return (mGLStateFlag & CULL_BACK) == CULL_BACK;
	}
	
	/**
	 * <br>功能简述: 设置剔除的是背面还是正面
	 * <br>功能详细描述:
	 * <br>注意: 默认是背面，如果设置了正面要注意恢复为背面
	 * @param back
	 */
	public void setCullFaceSide(boolean back) {
		if (back) {
			if ((mGLStateFlag & CULL_BACK) != CULL_BACK) {
				mGLStateFlag |= CULL_BACK;
				addRenderable(GLCommandFactory.get(GLCommandFactory.CULL_BACK_FACE), null);
			}
		} else {
			if ((mGLStateFlag & CULL_BACK) == CULL_BACK) {
				mGLStateFlag &= ~CULL_BACK;
				addRenderable(GLCommandFactory.get(GLCommandFactory.CULL_FRONT_FACE), null);
			}
		}
	}
	
	/**
	 * <br>功能简述: 获取是否启用颜色混合
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean isBlendEnabled() {
		return (mGLStateFlag & BLEND) == BLEND;
	}

	/**
	 * <br>功能简述: 设置颜色混合是否启用
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param enabled
	 */
	public void setBlend(boolean enabled) {
		if (enabled) {
			if ((mGLStateFlag & BLEND) != BLEND) {
				mGLStateFlag |= BLEND;
				addRenderable(GLCommandFactory.get(GLCommandFactory.BLEND_ENABLE), null);
			}
		} else {
			if ((mGLStateFlag & BLEND) == BLEND) {
				mGLStateFlag &= ~BLEND;
				addRenderable(GLCommandFactory.get(GLCommandFactory.BLEND_DISABLE), null);
			}
		}
	}
	
	/**
	 * <br>功能简述:获取颜色混合模式
	 * <br>功能详细描述:
	 * <br>注意:
	 * @see #setBlendMode(Mode)
	 * @return
	 */
	public Mode getBlendMode() {
		return mBlendMode;
	}
	
	/** @formatter:off */
	private final static int[] BLEND_FUNC_FACTORS = new int[] {
			GLES20.GL_ZERO, 				GLES20.GL_ZERO, 				//CLEAR
			GLES20.GL_ONE,					GLES20.GL_ZERO,					//SRC
			GLES20.GL_ZERO,					GLES20.GL_ONE, 					//DST
			GLES20.GL_ONE,					GLES20.GL_ONE_MINUS_SRC_ALPHA, 	//SRC_OVER
			GLES20.GL_ONE_MINUS_DST_ALPHA, 	GLES20.GL_ONE, 					//DST_OVER
			GLES20.GL_DST_ALPHA, 			GLES20.GL_ZERO, 				//SRC_IN
			GLES20.GL_ZERO, 				GLES20.GL_SRC_ALPHA, 			//DST_IN
			GLES20.GL_ONE_MINUS_DST_ALPHA, 	GLES20.GL_ZERO, 				//SRC_OUT
			GLES20.GL_ZERO, 				GLES20.GL_ONE_MINUS_SRC_ALPHA, 	//DST_OUT
			GLES20.GL_DST_ALPHA, 			GLES20.GL_ONE_MINUS_SRC_ALPHA, 	//SRC_ATOP
			GLES20.GL_ONE_MINUS_DST_ALPHA,	GLES20.GL_SRC_ALPHA, 			//DST_ATOP
			-1, -1, //XOR
			-1, -1, //DARKEN
			-1, -1, //LIGHTEN
			GLES20.GL_ZERO, 				GLES20.GL_SRC_COLOR, 			//MULTIPLY
			GLES20.GL_ONE, 					GLES20.GL_ONE_MINUS_SRC_COLOR, 	//SCREEN
	};
	/** @formatter:on */
	
	/**
	 * <br>功能简述: 设置要绘制的（前景）颜色和缓冲区的（背景）颜色混合的方式，类似与{@link Paint#setXfermode(android.graphics.Xfermode)}
	 * <br>功能详细描述:
	 * <br>注意: 这个与{@link Drawable#setColorFilter(int, android.graphics.PorterDuff.Mode)} 的作用不同，
	 * 后者中的前景颜色是filterColor，背景颜色则是drawable本身的颜色
	 * @param mode 默认是{@link Mode#SRC_OUT}
	 */
	public void setBlendMode(Mode mode) {
		if (mode == null) {
			mode = DEFAULT_BLEND_MODE;
		}
		if (mBlendMode == mode) {
			return;
		}
		mBlendMode = mode;
		final int value = mode.ordinal();
		final int sFactor = BLEND_FUNC_FACTORS[value * 2];
		if (sFactor == -1) {
			throw new UnsupportedOperationException("Mode " + this + " is not supported.");
		}
		final int dFactor = BLEND_FUNC_FACTORS[value * 2 + 1];
		setBlendFunc(sFactor, dFactor);
	}
	
	/**
	 * <br>功能简述: {@link GLES20#glBlendFunc(int, int)}
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param srcRGB
	 * @param dstRGB
	 */
	private void setBlendFunc(int srcRGB, int dstRGB) {
//		setBlendFunc(srcRGB, dstRGB, srcRGB, dstRGB);	//这个方法不行，glBlendFuncSeperate部分机型未硬件实现
		RenderContext context = RenderContext.acquire();
		final float[] args = context.color;
		args[0] = srcRGB;
		args[1] = dstRGB;
		addRenderable(GLCommandFactory.get(GLCommandFactory.BLEND_FUNC), context);
	}
	
	/**
	 * <br>功能简述: {@link GLES20#glBlendFuncSeparate(int, int, int, int)}
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param srcRGB
	 * @param dstRGB
	 * @param srcAlpha
	 * @param dstAlpha
	 * 
	 * @hide glBlendFuncSeperate部分机型未硬件实现。还是把它隐藏起来好了。 2013-12-10 by dengweiming.
	 */
	public void setBlendFunc(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		RenderContext context = RenderContext.acquire();
		final float[] args = context.color;
		args[0] = srcRGB;
		args[1] = dstRGB;
		args[2] = srcAlpha;
		args[3] = dstAlpha;	//CHECKSTYLE IGNORE
		addRenderable(GLCommandFactory.get(GLCommandFactory.BLEND_FUNC_SEPERATE), context);
	}

	/**
	 * 获取alpha
	 * @return [0..255]
	 */
	public int getAlpha() {
		return mAlpha;
	}

	/**
	 * 设置alpha。
	 * 注意要设置成一个小于255的alpha的时候，要先用{@link #getAlpha()}备份当前alpha值，
	 * 绘制完要恢复为该备份值，以免影响后续绘制。
	 * @param alpha [0, 255]
	 */
	public void setAlpha(int alpha) {
		alpha = Math.max(0, Math.min(alpha, 255)); //CHECKSTYLE IGNORE
		if (alpha == mAlpha) {
			return;
		}
		mAlpha = alpha;
	}

	/**
	 * 在当前的alpha设置上累乘新的alpha，变得更透明。
	 * 注意要先用{@link #getAlpha()}备份当前alpha值，绘制完使用{@link #setAlpha(int)}恢复为该备份值，以免影响后续绘制。
	 * @param alpha	[0, 255]
	 */
	public void multiplyAlpha(int alpha) {
		//CHECKSTYLE IGNORE 5 LINES
		alpha = Math.max(0, Math.min(alpha, 255));
		if (alpha == 255) {
			return;
		}
		mAlpha = mAlpha * alpha / 255;
	}
	
	/**
	 * <br>功能简述: 获取当前绘制时间（单位毫秒）
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return 
	 */
	public long getDrawingTime() {
		return mDrawingTime;
	}
	
	/**
	 * <br>功能简述: 获取和上一帧的时间差（单位毫秒）
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public long getDeltaDrawingTime() {
		return mDeltaDrawingTime;
	}

	/** 没有实现 */
	@Deprecated
	public void drawColor(int color) {
	}

	/** 没有实现 */
	@Deprecated
	public void drawRect(Rect bounds, Paint paint) {
	}

	/**
	 * 
	 * @param drawable	之后如果要释放这个对象的引用需要调用{@link #releaseDrawableReference(Drawable)}，
	 * 或者{@link GLContentView#releaseDrawableReference(Drawable)}
	 */
	public void drawDrawable(Drawable drawable) {
		if (drawable == null) {
			return;
		}
		if (drawable instanceof GLDrawable) {
			((GLDrawable) drawable).draw(this);
		} else if (drawable instanceof BitmapDrawable) {
			BitmapGLDrawable d = mBitmapDrawableMap.get((BitmapDrawable) drawable);
			if (d == null) {
				d = new BitmapGLDrawable((BitmapDrawable) drawable);
				mBitmapDrawableMap.put((BitmapDrawable) drawable, d);
			}
			d.setBounds(drawable.getBounds());
			d.draw(this);
		} else if (drawable instanceof NinePatchDrawable) {
			NinePatchGLDrawable d = mNinePatchDrawableMap.get((NinePatchDrawable) drawable);
			if (d == null) {
				d = new NinePatchGLDrawable((NinePatchDrawable) drawable);
				mNinePatchDrawableMap.put((NinePatchDrawable) drawable, d);
			}
			d.setBounds(drawable.getBounds());
			d.draw(this);
		} else if (drawable instanceof ColorDrawable) {
			ColorGLDrawable d = mColorDrawableMap.get((ColorDrawable) drawable);
			if (d == null) {
				d = new ColorGLDrawable(0);
				mColorDrawableMap.put((ColorDrawable) drawable, d);
			}
			d.setBounds(drawable.getBounds());
			mColorGLDrawable = d;
			drawable.draw(mFakeCanvas);
			mColorGLDrawable = null;
		} else if (drawable instanceof DrawableContainer) {
			drawDrawable(drawable.getCurrent());
		}
	}

	/**
	 * 作为{@link #drawDrawable(Drawable)}参数被调用过的Drawable对象需要通过本方法来释放引用
	 * @param drawable
	 */
	public void releaseDrawableReference(Drawable drawable) {
		if (drawable == null) {
			return;
		}
		if (drawable instanceof BitmapDrawable) {
			BitmapGLDrawable d = mBitmapDrawableMap.get((BitmapDrawable) drawable);
			if (d != null) {
				d.clear();
				mBitmapDrawableMap.remove(drawable);
			}
		} else if (drawable instanceof NinePatchDrawable) {
			NinePatchGLDrawable d = mNinePatchDrawableMap.get((NinePatchDrawable) drawable);
			if (d != null) {
				d.clear();
				mNinePatchDrawableMap.remove(drawable);
			}
		} else if (drawable instanceof ColorDrawable) {
			ColorGLDrawable d = mColorDrawableMap.get((ColorDrawable) drawable);
			if (d != null) {
				d.clear();
				mColorDrawableMap.remove(drawable);
			}
		} 
		//如果为DrawableContainer，如StateListDrawable，那么应该对每个对应状态的Drawable释放引用，避免引起内存泄漏
		else if (drawable instanceof DrawableContainer) {
			try {
				@SuppressWarnings("rawtypes")
				Class containerClass = Class.forName("android.graphics.drawable.DrawableContainer");
				Field stateField = containerClass.getDeclaredField("mDrawableContainerState");
				stateField.setAccessible(true);
				DrawableContainerState state = (DrawableContainerState) stateField.get(drawable);
				Drawable[] children = state.getChildren();
				for (Drawable child : children) {
					releaseDrawableReference(child);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void clearCachedGLDrawablesInMap(Map map) {
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry entry = (Map.Entry) iter.next();
		    Object val = entry.getValue();
		    ((GLDrawable) val).clear();
		}
	}
		
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @hide
	 */
	public void cleanup() {
		clearCachedGLDrawablesInMap(mBitmapDrawableMap);
		clearCachedGLDrawablesInMap(mNinePatchDrawableMap);
		clearCachedGLDrawablesInMap(mColorDrawableMap);
		
		mBitmapDrawableMap.clear();
		mNinePatchDrawableMap.clear();
		mColorDrawableMap.clear();
		
		for (int i = 0; i < mLayers.length; ++i) {
			if (mLayers[i] != null) {
				mLayers[i].clear();
				mLayers[i] = null;
			}
		}
		mLayer = null;
	}

	/**
	 * 假的Canvas
	 * @author dengweiming
	 *
	 */
	private class FakeCanvas extends android.graphics.Canvas {
		@Override
		public void drawColor(int color) {
			mColorGLDrawable.setColor(color, 255); //CHECKSTYLE IGNORE
			mColorGLDrawable.draw(GLCanvas.this);
		}

		@Override
		public void drawRect(Rect r, Paint paint) {
			if (paint != null) {
				drawColor(paint.getColor());
			}
		}
	}

	public static void saveBitmap(Bitmap bitmap, String fileName) {
		FileOutputStream fos;
		try {
			if (fileName.charAt(0) != '/') {
				fileName = Environment.getExternalStorageDirectory().getPath() + "/" + fileName;
			}
			fos = new FileOutputStream(fileName);
			bitmap.compress(CompressFormat.PNG, 100, fos); //CHECKSTYLE IGNORE
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final float PI_OVER_180 = (float) (Math.PI / 180); //CHECKSTYLE IGNORE

	/**
	 * Converts Euler angles to a rotation matrix
	 * @param m returns the result
	 * @param offset index into rm where the result matrix starts
	 * @param x angle of rotation, in degrees
	 * @param y angle of rotation, in degrees
	 * @param z angle of rotation, in degrees
	 */
	public static void setRotateEulerM(float[] m, int offset, float x, float y, float z) {
		x *= PI_OVER_180;
		y *= PI_OVER_180;
		z *= PI_OVER_180;
		float cx = (float) Math.cos(x);
		float sx = (float) Math.sin(x);
		float cy = (float) Math.cos(y);
		float sy = (float) Math.sin(y);
		float cz = (float) Math.cos(z);
		float sz = (float) Math.sin(z);
		float cxsy = cx * sy;
		float sxsy = sx * sy;

		m[offset + M00] = cy * cz;
		m[offset + M10] = sxsy * cz + cx * sz;
		m[offset + M20] = -cxsy * cz + sx * sz;
		m[offset + M30] = 0;

		m[offset + M01] = -cy * sz;
		m[offset + M11] = -sxsy * sz + cx * cz;
		m[offset + M21] = cxsy * sz + sx * cz;
		m[offset + M31] = 0;

		m[offset + M02] = sy;
		m[offset + M12] = -sx * cy;
		m[offset + M22] = cx * cy;
		m[offset + M32] = 0;

		m[offset + M03] = 0;
		m[offset + M13] = 0;
		m[offset + M23] = 0;
		m[offset + M33] = 1;
	}
	
	private final static float GIMBAL_LOCK_ANGLE = 90;
	
	/**
	 * <br>功能简述: 将旋转矩阵转换成欧拉角
	 * <br>功能详细描述:
	 * <br>注意: 矩阵m必须为规范正交矩阵，即不包含缩放变换
	 * @param m
	 * @param offset
	 * @param euler 输出的欧拉角，长度必须至少为3,存放依次绕x,y,z轴旋转的角度
	 */
	public static void convertMatrixToEulerAngle(float[] m, int offset, float[] euler) {
		final float err = 1e-5f;
		float m02 = m[offset + M02];
		if (m02 < err - 1) {
			float m21 = m[offset + M21];
			float m11 = m[offset + M11];
			euler[0] = (float) Math.toDegrees(Math.atan2(m21, m11));
			euler[1] = -GIMBAL_LOCK_ANGLE;
			euler[2] = 0;
		} else if (m02 > 1 - err) {
			float m21 = m[offset + M21];
			float m11 = m[offset + M11];
			euler[0] = (float) Math.toDegrees(Math.atan2(m21, m11));
			euler[1] = GIMBAL_LOCK_ANGLE;
			euler[2] = 0;
		} else {
			float m12 = m[offset + M12];
			float m22 = m[offset + M22];
			euler[0] = (float) Math.toDegrees(Math.atan2(-m12, m22));
			euler[1] = (float) Math.toDegrees(Math.asin(m02));
			float m01 = m[offset + M01];
			float m00 = m[offset + M00];
			euler[2] = (float) Math.toDegrees(Math.atan2(-m01, m00));
		}
	}
	
	/**
	 * <br>功能简述: 将角-轴对转换成欧拉角
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param degrees 旋转角度
	 * @param x	旋转轴x分量
	 * @param y
	 * @param z
	 * @param euler 输出的欧拉角，长度必须至少为3,存放依次绕x,y,z轴旋转的角度
	 */
	public static void convertAxisAngleToEulerAngle(float degrees, float x, float y, float z, float[] euler) {
		/**
		 * 推导方法参考《3D数学基础：图形与游戏开发》10.6.2章节，只是它的矩阵是行矩阵，欧拉角是y-x-z顺序的。
		 * 按照该方法，对照 {@link #setRotateEulerM(float[], int, float, float, float)} 和 
		 * {@link #rotateAxisAngle(float, float, float, float)} 计算得到的矩阵，来推出适当的解法。 
		 */
		final double err = 1e-5;
		final double err2 = 1e-10;
		double len2 = x * x + y * y + z * z;
		if (len2 < err2) {
			throw new IllegalArgumentException("axis too short");
		}
		if (Math.abs(len2 - 1) > err2) {
			double recipLen = 1 / Math.sqrt(len2);
			x *= recipLen;
			y *= recipLen;
			z *= recipLen;
		}
		
		double rad = Math.toRadians(degrees);
		double sin = Math.sin(rad);
		double cos = Math.cos(rad);
		double ncs = 1 - cos;
		double m02 = x * z * ncs + y * sin;
		if (m02 < err - 1) {
			double m21 = y * z * ncs + x * sin;
			double m11 = cos + y * y * ncs;
			euler[0] = (float) Math.toDegrees(Math.atan2(m21, m11));
			euler[1] = -GIMBAL_LOCK_ANGLE;
			euler[2] = 0;
		} else if (m02 > 1 - err) {
			double m21 = y * z * ncs + x * sin;
			double m11 = cos + y * y * ncs;
			euler[0] = (float) Math.toDegrees(Math.atan2(m21, m11));
			euler[1] = GIMBAL_LOCK_ANGLE;
			euler[2] = 0;
		} else {
			double m12 = y * z * ncs - x * sin;
			double m22 = cos + z * z * ncs;
			euler[0] = (float) Math.toDegrees(Math.atan2(-m12, m22));
			euler[1] = (float) Math.toDegrees(Math.asin(m02));
			double m01 = x * y * ncs - z * sin;
			double m00 = cos + x * x * ncs;
			euler[2] = (float) Math.toDegrees(Math.atan2(-m01, m00));
		}
	}

	public static void inverseTranslateAndRotatePosition(float[] m, int mOffset, float[] src,
			int srcOffset, float[] dst, int dstOffset) {
		final float x = src[srcOffset++] - m[mOffset + M03];
		final float y = src[srcOffset++] - m[mOffset + M13];
		final float z = src[srcOffset++] - m[mOffset + M23];
		dst[dstOffset++] = m[mOffset + M00] * x + m[mOffset + M10] * y + m[mOffset + M20] * z;
		dst[dstOffset++] = m[mOffset + M01] * x + m[mOffset + M11] * y + m[mOffset + M21] * z;
		dst[dstOffset++] = m[mOffset + M02] * x + m[mOffset + M12] * y + m[mOffset + M22] * z;
	}

	public static void inverseTranslateAndRotateVector(float[] m, int mOffset, float[] src,
			int srcOffset, float[] dst, int dstOffset) {
		final float x = src[srcOffset++];
		final float y = src[srcOffset++];
		final float z = src[srcOffset++];
		dst[dstOffset++] = m[mOffset + M00] * x + m[mOffset + M10] * y + m[mOffset + M20] * z;
		dst[dstOffset++] = m[mOffset + M01] * x + m[mOffset + M11] * y + m[mOffset + M21] * z;
		dst[dstOffset++] = m[mOffset + M02] * x + m[mOffset + M12] * y + m[mOffset + M22] * z;
	}

	DrawFilter mDrawFilter;

	public void setDrawFilter(DrawFilter filter) {
		//TODO
		mDrawFilter = filter;
	}

	public DrawFilter getDrawFilter() {
		return mDrawFilter;
	}

	RenderInfoNode mCurRenderInfoNode;
	
	/**
	 * @hide
	 */
	public static void resetOnFrameStart() {
		//for debugging
		++sFrameTimeOnGL;
//		if (sTimeFrameOnGL % sFpsForLog == 0) {
//			GLCommandFactory.DBG = true;
//			GLFramebuffer.DBG = true;
//			Log.d(TAG, "===========resetOnFrameStart ==========v " + sTimeFrameOnGL);
//		} else {
//			GLCommandFactory.DBG = false;
//			GLFramebuffer.DBG = false;
//		}
		
		GLCommandFactory.sDepthBufferDirty = false;
		GLCommandFactory.sFrameBufferDepthBufferDirty = false;
		GLFramebuffer.resetStackOnRenderStart();
		GLVBO.resetStackOnRenderStart();
		Texture.resetStatic();
		GLState.setWrapMode(GLES20.GL_CLAMP_TO_EDGE, GLES20.GL_CLAMP_TO_EDGE);
	}

	/**
	 * @hide
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param rootNode
	 * @return 当前的结点，在{@link #finishDisplayList(RenderInfoNode)}时恢复
	 */
	public RenderInfoNode startRootDisplayList(RenderInfoNode rootNode) {
		rootNode.reset();
		mCurRenderInfoNode = rootNode;
		sFrameTime++;
		if (DBG && sFrameTime % sFpsForLog == 0) {
			Log.d(TAG, "===========startRootDisplayList " + rootNode + " ==========v");
		}

		//重设主线程中GL的状态，和GL线程的一致
		final int setFlag = COLOR_MASK | CULL_FACE | BLEND | CULL_BACK;
		final int clearFlag = DEPTH_MASK | DEPTH_TEST | SCISSOR_TEST | STENCIL_TEST;
		mGLStateFlag = setFlag & (~clearFlag);
		mClipRectStack[mSaveCount].set(0, 0, mWindowWidth, mWindowHeight);
		mStencilClipRectStack[mSaveCount].set(0, 0, mWindowWidth, mWindowHeight);
		mStencilIdStack[mSaveCount] = 0;
		mStencilIdAllocated = 0;
		mLastStencilIdHasClearMask = 0;
		mClipRegionStarted = false;
		mLayerSavedCount = LAYER_UNSAVE_COUNT;
		if (mLayer != null && mLayer.isBinding()) {
			mLayer.unbind(this);
		}
		mLayerIndex = 0;
		mClearColorChanged = true;
		setClearColor(mBackgroundClearColor);
		setViewport(0, 0, mWindowWidth, mWindowHeight);
		setBlendMode(DEFAULT_BLEND_MODE);
		mColorShape.setColor(DEFAULT_DRAW_COLOR);
		
		GLFramebuffer.sBindDepth = 0;
		return null;
	}

	/**
	 * @hide
	 * @param headNode
	 * @return 当前的结点，在{@link #finishDisplayList(RenderInfoNode)}时恢复
	 */
	public RenderInfoNode startDisplayList(RenderInfoNode headNode) {
		if (mCurRenderInfoNode.getForkNode() != null) {
			addRenderable(Renderable.sInstance, null);	//添加一个空结点,避免覆盖掉之前 fork 的结点
		}
		//Log.d(TAG, "===========startDisplayList " + mCurRenderInfoNode + " -> " + headNode);
		final RenderInfoNode savedRenderInfoNode = mCurRenderInfoNode;
		mCurRenderInfoNode.setForkNode(headNode);
		mCurRenderInfoNode = headNode;
		return savedRenderInfoNode;
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param renderInfoTriple
	 * @param needMatrix	是否需要使用当前的模型视图投影矩阵（绘制图元时为true）,该矩阵会传递给{@link RenderInfo#render(long, float[])}
	 */
	public void addRenderable(Renderable renderable, RenderContext context) {
		//TODO:处理还没开始绘制就调用本方法的情况
		RenderInfoNode nextNode = mCurRenderInfoNode.getNextNode();
		if (nextNode == null) {
			nextNode = mCurRenderInfoNode.acquireNext();
		} else {
			nextNode.reset();
		}
		mCurRenderInfoNode = nextNode;
		//Log.d(TAG, "===========appendRenderInfo " + renderable + " " + context);

		mCurRenderInfoNode.mRenderable = renderable;
		mCurRenderInfoNode.mContext = context;

	}

	/**
	 * @hide
	 */
	public void finishDisplayList(RenderInfoNode savedRenderInfoNode) {
		//释放后面多余的节点（前几帧留下来的） //XXX: 应该在startRootDisplayList时清掉了，nextNode应该为null，这里不需要再处理了
		RenderInfoNode nextNode = mCurRenderInfoNode.getNextNode();
		mCurRenderInfoNode.setNextNode(null);
		mCurRenderInfoNode = savedRenderInfoNode;
		if (nextNode != null) {
			nextNode.release();
		}

		//Log.d(TAG, "===========finishDisplayList " + savedRenderInfoNode);
	}

	/**
	 * @hide
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param headNode
	 */
	public void forkDisplayList(RenderInfoNode headNode) {
		if (mCurRenderInfoNode.getForkNode() != null) {
			addRenderable(Renderable.sInstance, null);	//添加一个空结点,避免覆盖掉之前 fork 的结点
		}
		//Log.d(TAG, "===========forkDisplayList " + headNode);
		mCurRenderInfoNode.setForkNode(headNode);
	}
	
	/**
	 * <br>功能简述: 是否丢帧了（一到两帧）
	 * <br>功能详细描述: 在其他程序横竖屏切换回来的时候可能会引起丢帧（为了避免横屏时绘制了竖屏的内容或者反之），而对于某些依赖于帧状态的应用就需要注意了，
	 * 例如分步模糊图片的时候，第一帧就是先把原图拷贝，后面几帧才开始模糊，那么只要有一帧丢失，就要从头开始，避免根本没拷贝到或者模糊步数不够。
	 * <br>注意:
	 */
	public boolean isLastFrameDropped() {
		return mLastFrameDropped;
	}
	
	/**
	 * <br>功能简述: 将整数值的颜色转成[0..1]范围内的alpha预乘浮点格式
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param color
	 * @param colors	will store r, g, b, a who's value in [0..1]
	 * @param offset
	 */
	public static void convertColorToPremultipliedFormat(int color, float[] colors, int offset) {
		//CHECKSTYLE IGNORE 5 LINES
		final float a = (color >>> 24) * OneOver255;
		colors[offset] = (color >>> 16 & 0xFF) * a * OneOver255;
		colors[offset + 1] = (color >>> 8 & 0xFF) * a * OneOver255;
		colors[offset + 2] = (color & 0xFF) * a * OneOver255;
		colors[offset + 3] = a;
	}
	
	/**
	 * <br>功能简述:将整数值的颜色转成[0..1]范围内的alpha预乘浮点格式
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param color
	 * @param context
	 */
	public static void convertColor(int color, RenderContext context) {
		convertColorToPremultipliedFormat(color, context.color, 0);
	}
	
	/**
	 * <br>功能简述:设置视角张开的角度
	 * <br>功能详细描述:同等深度的情况下，视角大小（看到的东西的多少）跟sDEFAULT_FOV正相关
	 * <br>注意:
	 * <br>在GLActivity.onCreate()调用。
	 * <br>必须保证在GLContentView.onSizeChanged()之前，之后不允许再设置！
	 * @param fov
	 */
	public static void setDefaultFov(float fov) {
		sDEFAULT_FOV = fov;
		sDEFAULT_FOV_SCALE_FACTOR = 0.5 / Math.tan(Math.toRadians(sDEFAULT_FOV) * 0.5);
	}

	public static float getDefaultFov() {
		return sDEFAULT_FOV;
	}

	public static double getDefaultFovScaleFactor() {
		return sDEFAULT_FOV_SCALE_FACTOR;
	}
	

	//==================绘制几何图形=============================v
	private final static int DEFAULT_DRAW_COLOR = 0xFFFFFFFF;
	private ColorShape mColorShape = new ColorShape(DEFAULT_DRAW_COLOR);
	public final static int POINTS = 0;
	public final static int LINE_STRIP = 1;
	public final static int LINE_LOOP = 2;
	public final static int LINES = 3;
	public final static int TRIANGLE_STRIP = 4;
	public final static int TRIANGLE_FAN = 5;
	public final static int TRIANGLES = 6;
	
	private final float[] mShapeBuffer = new float[16];			//CHECKSTYLE IGNORE
	private final float[] mTempFloatArray = new float[1024];	//CHECKSTYLE IGNORE
	
	/**
	 * <br>功能简述: 设置绘制几何图形时的颜色
	 * <br>功能详细描述:
	 * <br>注意: 此颜色还会受到{@link #setAlpha(int)} 的影响。默认为白色。该设置是全局的。
	 * @param color
	 */
	public void setDrawColor(int color) {
		mColorShape.setColor(color);
	}
	
	/**
	 * @see {@link #setDrawColor(int)}
	 */
	public void drawPoint(float x, float y) {
		final float[] buf = mShapeBuffer;
		int i = 0;
		buf[i++] = x;
		buf[i++] = -y;
		
		mColorShape.draw(this, POINTS, buf, 0, 1, false);
	}
	
	/**
	 * @see {@link #setDrawColor(int)}
	 */
	public void drawLine(float x1, float y1, float x2, float y2) {
		final float[] buf = mShapeBuffer;
		int i = 0;
		buf[i++] = x1;
		buf[i++] = -y1;
		buf[i++] = x2;
		buf[i++] = -y2;
		
		mColorShape.draw(this, LINES, buf, 0, 2, false);
	}
	
	/**
	 * <br>功能简述: 绘制三角形线框
	 * @see {@link #setDrawColor(int)}
	 * @see {@link #fillTriangle(float, float, float, float, float, float)}
	 */
	public void drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3) {
		final float[] buf = mShapeBuffer;
		int i = 0;
		buf[i++] = x1;
		buf[i++] = -y1;
		buf[i++] = x2;
		buf[i++] = -y2;
		buf[i++] = x3;
		buf[i++] = -y3;
		
		mColorShape.draw(this, LINE_LOOP, buf, 0, 3, false);	//CHECKSTYLE IGNORE
	}
	
	/**
	 * <br>功能简述: 填充一个三角形
	 * <br>注意: 三角形逆时针绕序时是正向，否则如果开启了背面剔除就会画不出来
	 * @see {@link #setDrawColor(int)}
	 * @see {@link #drawTriangle(float, float, float, float, float, float)}
	 */
	public void fillTriangle(float x1, float y1, float x2, float y2, float x3, float y3) {
		final float[] buf = mShapeBuffer;
		int i = 0;
		buf[i++] = x1;
		buf[i++] = -y1;
		buf[i++] = x2;
		buf[i++] = -y2;
		buf[i++] = x3;
		buf[i++] = -y3;
		
		mColorShape.draw(this, TRIANGLES, buf, 0, 3, false);	//CHECKSTYLE IGNORE
	}
	
	/**
	 * <br>功能简述: 绘制矩形线框
	 * @see {@link #setDrawColor(int)}
	 * @see {@link #fillRect(float, float, float, float, float, float)}
	 */
	public void drawRect(float left, float top, float right, float bottom) {
		final float[] buf = mShapeBuffer;
		int i = 0;
		buf[i++] = left;
		buf[i++] = -top;
		buf[i++] = left;
		buf[i++] = -bottom;
		buf[i++] = right;
		buf[i++] = -bottom;
		buf[i++] = right;
		buf[i++] = -top;
		
		mColorShape.draw(this, LINE_LOOP, buf, 0, 4, false);	//CHECKSTYLE IGNORE
	}
	
	/**
	 * <br>功能简述: 填充一个矩形
	 * @see {@link #setDrawColor(int)}
	 * @see {@link #drawRect(float, float, float, float, float, float)}
	 */
	public void fillRect(float left, float top, float right, float bottom) {
		final float[] buf = mShapeBuffer;
		int i = 0;
		buf[i++] = left;
		buf[i++] = -top;
		buf[i++] = left;
		buf[i++] = -bottom;
		buf[i++] = right;
		buf[i++] = -top;
		buf[i++] = right;
		buf[i++] = -bottom;
		
		mColorShape.draw(this, TRIANGLE_STRIP, buf, 0, 4, false);	//CHECKSTYLE IGNORE
	}
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param mode 绘制模式，包括{@link #POINTS}, {@link #LINE_STRIP}, {@link #LINE_LOOP}, {@link #LINES}, 
	 * {@link #TRIANGLE_STRIP},  {@link #TRIANGLE_FAN} {@link #TRIANGLES}，意义和OPENGL一致。
	 * @param vertex 顶点数据，在小规模时可以使用{@link #getTempFloatArray()} 来避免动态分配
	 * @param offset <var>vertex</var>的有效数据的起始索引
	 * @param vertexCount 顶点数目
	 * @param is3D 每个顶点是否包含x,y,z三个分量，否则只包含x,y两个分量。注意三角形逆时针绕序时是正向，否则如果开启了背面剔除就会画不出来。
	 */
	public void drawVertex(int mode, float[] vertex, int offset, int vertexCount, boolean is3D) {
		if (vertexCount <= 0) {
			return;
		}
		if (!is3D) {
			tempSaveMatrix();
			scale(1, -1);
		}
		mColorShape.draw(this, mode, vertex, offset, vertexCount, is3D);
		if (!is3D) {
			tempRestoreMatrix();
		}
	}
	
	/**
	 * <br>功能简述: 获取一个预分配的float数组供临时使用
	 * <br>功能详细描述:
	 * <br>注意: 其容量为1024。另外注意它是全局的，在使用过程中可能会被子方法调用修改掉内容。
	 * @return
	 */
	public float[] getTempFloatArray() {
		return mTempFloatArray;
	}
	
	/**
	 * <br>功能简述: 绘制视景体的线框
	 * <br>功能详细描述: 因为正面看只能看到跟窗口一样大的线框，而且需要后绘制以避免被覆盖。
	 * 所以一般流程应该是：
	 * <br>（如果当前模型矩阵不为单位矩阵则需要{@link #getMatrix(float[], int)}）备份），
	 * 调用{@link #reset()}回到世界坐标系中，再调用setLook*这几个方法来设置观察角度，
	 * （有备份的情况需要再{@link #concat(float[], int)}回去），再绘制场景。
	 * <br>再次调用{@link #reset()}回到世界坐标系中并设置同样的观察角度，再调用本方法。
	 * <br>另外在绘制场景时写深度缓冲区，调用本方法时先启用深度检测，可以达到消隐的效果，
	 * 避免线框总是在场景之上。
	 * <br>
	 * <br>注意: 此方法会覆写{@link #getTempFloatArray()}
	 * @see {@link #setDrawColor(int)}
	 */
	public void drawViewFrustum() {
		float cx = mCameraPos[VTX];
		float cy = mCameraPos[VTY];
		float cz = mCameraPos[VTZ];
		
		float l = mFrustum[FrustumL];
		float t = mFrustum[FrustumT];
		float r = mFrustum[FrustumR];
		float b = mFrustum[FrustumB];
		float n = mFrustum[FrustumN];
		float f = mFrustum[FrustumF];
		
		float[] vertex = mTempFloatArray;
		int offset = 0;
		//四条侧棱
		offset += putVertex(vertex, offset, cx, cy, cz, 0);
		offset += putVertex(vertex, offset, l, t, n, f / n);
		offset += putVertex(vertex, offset, cx, cy, cz, 0);
		offset += putVertex(vertex, offset, l, b, n, f / n);
		offset += putVertex(vertex, offset, cx, cy, cz, 0);
		offset += putVertex(vertex, offset, r, t, n, f / n);
		offset += putVertex(vertex, offset, cx, cy, cz, 0);
		offset += putVertex(vertex, offset, r, b, n, f / n);
		
		offset += putRect(vertex, offset, l, t, r, b, n, 1);		//近裁面
		offset += putRect(vertex, offset, l, t, r, b, n, cz / n);	//参考面
		
		drawVertex(GLCanvas.LINES, vertex, 0, offset / 3, true);	//CHECKSTYLE IGNORE
	}
	
	/**
	 * 对射线(mCamX, mCamY, mCamZ)+k(x, y, n)上的点存放到vertex[offset]位置上
	 */
	private int putVertex(float[] vertex, int offset, float x, float y, float n, float k) {
		vertex[offset++] = x * k + mCameraPos[VTX];
		vertex[offset++] = y * k + mCameraPos[VTY];
		vertex[offset++] = -n * k + mCameraPos[VTZ];
		return 3;	//CHECKSTYLE IGNORE
	}
	
	/**
	 * 将距离摄像机距离n*k处的截面存放到vertex[offset]位置上
	 */
	private int putRect(float[] vertex, int offset, float l, float t, float r, float b, float n, float k) {
		final int oldOffset = offset;
		offset += putVertex(vertex, offset, l, t, n, k);
		offset += putVertex(vertex, offset, l, b, n, k);
		offset += putVertex(vertex, offset, l, b, n, k);
		offset += putVertex(vertex, offset, r, b, n, k);
		offset += putVertex(vertex, offset, r, b, n, k);
		offset += putVertex(vertex, offset, r, t, n, k);
		offset += putVertex(vertex, offset, r, t, n, k);
		offset += putVertex(vertex, offset, l, t, n, k);
		return offset - oldOffset;
	}
	
	
	//==================绘制几何图形=============================^
	
	private static FastQueue<String> sGLLogMsgQueue;
	private static Renderable sGLLogger;
	public static final boolean DBG_GL_LOG = false;

	/**
	 * <br>功能简述: 在GL线程上打印日志
	 * <br>功能详细描述:
	 * <br>注意: 需要将{@link #DBG_GL_LOG}标志打开
	 * @param msg
	 */
	public void logOnGLThread(String msg) {
		if (DBG_GL_LOG) {
			if (sGLLogMsgQueue == null) {
				sGLLogMsgQueue = new FastQueue<String>(1024 * 4);
				sGLLogger = new Renderable() {
					
					@Override
					public void run(long timeStamp, RenderContext context) {
						String msg = sGLLogMsgQueue.popFront();
						if (msg != null) {
							if (sFrameTimeOnGL % sFpsForLog == 0) {
								Log.d(TAG, msg);
							}
						}
					}
				};
			}
			sGLLogMsgQueue.pushBack(msg);
			addRenderable(sGLLogger, null);
		}
	}
	
}

//class Matrix extends android.opengl.Matrix {
//	
//    public static void multiplyMM(float[] result, int resultOffset,
//            float[] lhs, int lhsOffset, float[] rhs, int rhsOffset){
//		if (GLCanvas.sMatrixTag != null) {
////			Log.d(GLCanvas.sMatrixTag, "multiplyMM ");
//		}
//    	android.opengl.Matrix.multiplyMM(result, resultOffset, lhs, lhsOffset, rhs, rhsOffset);
//    }
//}
