package com.graphics.engine.gl.graphics;

import java.nio.Buffer;

import android.opengl.GLES20;

/**
 * 
 * <br>类描述: OpenGL顶点缓冲区对象(Vertex Buffer Object)的封装类
 * <br>功能详细描述:
 * 跟纹理保存像素数据类似，VBO可以将顶点的位置，纹理坐标，索引等数据保存到GPU，
 * 避免每次使用到的时候都从内存传送到GPU，减少带宽占用，提高效率（需要大量顶点时效果才明显）。
 * <ul> 一些常用的方法：
 * 	<li>{@link #setData(float[])}
 * 	<li>{@link #setData(short[])}
 * 	<li>{@link #bindOnUIThread(GLCanvas)}
 * 	<li>{@link #unbindOnGLThread()}
 * </ul>
 * 
 * @author  dengweiming
 * @date  [2013-8-19]
 */
public class GLVBO implements GLClearable, TextureListener {
	private final static int[] ID = new int[1];
	private final static float[] TMP = new float[1];
	private final static float[] GL_TMP = new float[1];
	
	/** 流模式，数据只修改一次，使用少次 */
	public final static int STREAM = GLES20.GL_STREAM_DRAW;
	/** 静态模式，数据只修改一次，使用多次 */
	public final static int STATIC = GLES20.GL_STATIC_DRAW;
	/** 动态模式，数据修改多次，使用多次 */
	public final static int DYNAMIC = GLES20.GL_DYNAMIC_DRAW;
	
	private static final int[] ID_STACK = new int[64];			//CHECKSTYLE IGNORE
	private static final int[] INDEX_ID_STACK = new int[64];	//CHECKSTYLE IGNORE
	private static final int[] TARGET_STACK = new int[128];		//CHECKSTYLE IGNORE
	private static int sIdStackPtr;
	private static int sIndexIdStackPtr;
	private static int sTargetStackPtr;
	
	private final int mTarget;
	private final int mDataType;
	private final int mDataTypeSize;
	private final int mUsage;
	
	private volatile int mId;
	
	private float[] mFloatData;
	private short[] mShortData;
	private int mDataCounts;
	private boolean mDataInvalidated;
	
	/**
	 * @param isIndex	是否为顶点索引
	 * 使用{@link #STATIC}方式
	 * @see {@link #GLVBO(boolean, int)}
	 */
	public GLVBO(boolean isIndex) {
		this(isIndex, STATIC);
	}
	
	/**
	 * @param isIndex	是否为顶点索引
	 * @param usage		使用模式，作为性能提示，可能会被硬件忽略。可选值：{@link #STREAM}, {@link #STATIC}, {@link #DYNAMIC}。
	 */
	public GLVBO(boolean isIndex, int usage) {
		if (isIndex) {
			mDataType = GLES20.GL_UNSIGNED_SHORT;
			mDataTypeSize = 2;
			mTarget = GLES20.GL_ELEMENT_ARRAY_BUFFER;
		} else {
			mDataType = GLES20.GL_FLOAT;
			mDataTypeSize = 4;
			mTarget = GLES20.GL_ARRAY_BUFFER;
		}
		
		if (usage != STREAM && usage != STATIC && usage != DYNAMIC) {
			throw new IllegalArgumentException("usage=" + usage + " is not STREAM or STATIC, DYNAMIC.");
		}
		mUsage = usage;

		TextureManager.getInstance().registerTextureListener(this);
	}
	
	/**
	 * <br>功能简述: 设置顶点属性数据
	 * <br>功能详细描述:
	 * <br>注意: 需要构造时的参数index为false。数据的长度必须保持一致。
	 * @param data
	 */
	public void setData(float[] data) {
		if (mDataType == GLES20.GL_FLOAT) {
			mFloatData = data;
			if (mDataCounts > 0 && mDataCounts != data.length) {
				throw new IllegalArgumentException("data length changed.");
			}
			mDataCounts = data.length;
			mDataInvalidated = true;
		} else {
			throw new IllegalArgumentException("Index VBO does not support float[].");
		}
	}
	
	/**
	 * <br>功能简述: 设置顶点索引数据
	 * <br>功能详细描述:
	 * <br>注意:需要构造时的参数index为true。数据的长度必须保持一致。
	 * @param data
	 */
	public void setData(short[] data) {
		if (mDataType != GLES20.GL_FLOAT) {
			mShortData = data;
			if (mDataCounts > 0 && mDataCounts != data.length) {
				throw new IllegalArgumentException("data length changed.");
			}
			mDataCounts = data.length;
			mDataInvalidated = true;
		} else {
			throw new IllegalArgumentException("Only index VBO support short[].");
		}
	}
	
	/**
	 * <br>功能简述: 获取封装的浮点数据
	 * <br>功能详细描述:
	 * <br>注意: 只有调用了{@link #setData(float[])} 才能再获取
	 * @return
	 */
	public float[] getFloatData() {
		return mFloatData;
	}
	
	/**
	 * <br>功能简述: 获取封装的短整型数据
	 * <br>功能详细描述:
	 * <br>注意: 只有调用了{@link #setData(short[])} 才能再获取
	 * @return
	 */
	public short[] getShortData() {
		return mShortData;
	}
	
	/**
	 * <br>功能简述: 强制刷新数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void invalidateData() {
		mDataInvalidated = true;
	}
	
	/**
	 * <br>功能简述: 在主线程上绑定（实际上还是会同步到GL线程上绑定），在堆栈上push的操作
	 * <br>功能详细描述: 
	 * <br>注意:
	 * @param canvas
	 * @see {@link #unbindOnGLThread()}
	 */
	public void bindOnUIThread(GLCanvas canvas) {
		canvas.addRenderable(mBinder, null);
		if (!mDataInvalidated) {
			return;
		}
		mDataInvalidated = false;
		if (mDataCounts <= 0) {
			return;
		}
		VertexBufferBlock.pushVertexData(mDataUploader);
		TMP[0] = mDataCounts;
		VertexBufferBlock.pushVertexData(TMP, 0, 1);
		
		if (mFloatData != null) {
			VertexBufferBlock.pushVertexData(mFloatData, 0, mFloatData.length);
		} else if (mShortData != null) {
			IndexBufferBlock.pushVertexData(mShortData, 0, mShortData.length);
		}
		canvas.addRenderable(mDataUploader, null);
	}
	
	/**
	 * <br>功能简述: 在GL线程上绑定，在堆栈上push的操作
	 * <br>功能详细描述: 
	 * <br>注意: 
	 * @param buffer 注意如果在主线程修改内容，要对它做同步控制
	 * @param count 元素数目
	 * @see {@link #unbindOnGLThread()}
	 */
	public void bindOnGLThread(Buffer buffer, int count) {
		mBinder.run(0, null);
		if (!mDataInvalidated) {
			return;
		}
		mDataInvalidated = false;
		mDataCounts = count;
		if (mDataCounts <= 0) {
			return;
		}

		GLError.clearGLError();
		GLES20.glBufferData(mTarget, mDataCounts * mDataTypeSize, buffer, mUsage);
		if (GLError.checkGLError(GLES20.GL_OUT_OF_MEMORY)) {
			//TODO:释放部分显存并重试
			GLES20.glBindBuffer(mTarget, 0);
			GLError.clearGLError();
		}
	}
	
	/**
	 * <br>功能简述: 在GL线程上解除绑定，在堆栈上pop的操作
	 * <br>功能详细描述:
	 * <br>注意:
	 * @see {@link #bindOnUIThread(GLCanvas)}
	 */
	public static void unbindOnGLThread() {
		if (sTargetStackPtr <= 0) {
			throw new RuntimeException("Target stack underflow.");
		}
		int curTarget = TARGET_STACK[sTargetStackPtr--];	// pop statck
		int preId = 0;
		if (curTarget == GLES20.GL_ARRAY_BUFFER) {
			if (sIdStackPtr <= 0) {
				throw new RuntimeException("Id statck underflow.");
			}
			preId = ID_STACK[--sIdStackPtr];				// pop statck
		} else {
			if (sIndexIdStackPtr <= 0) {
				throw new RuntimeException("Index id statck underflow.");
			}
			preId = INDEX_ID_STACK[--sIndexIdStackPtr];		// pop statck
		}
		GLES20.glBindBuffer(curTarget, preId);
	}

	@Override
	public void clear() {
    	TextureRecycler.recycleTextureDeferred(this);
	}

	@Override
	public void onClear() {
		TextureManager.getInstance().deleteVBO(mId);
		onTextureInvalidate();
		
		TextureManager.getInstance().unRegisterTextureListener(this);
		mFloatData = null;
		mShortData = null;
	}

	@Override
	public void yield() {
    	TextureRecycler.yieldTextureDeferred(this);
	}

	@Override
	public void onYield() {
		TextureManager.getInstance().deleteVBO(mId);
    	onTextureInvalidate();
	}

	@Override
	public void onTextureInvalidate() {
		mId = 0;
		mDataInvalidated = true;
	}
    
	/**
	 * @hide
	 */
    static void resetStackOnRenderStart() {
    	sIdStackPtr = 0;
    	sIndexIdStackPtr = 0;
    	sTargetStackPtr = 0;
    	ID_STACK[sIdStackPtr] = 0;
    	INDEX_ID_STACK[sIndexIdStackPtr] = 0;
    	
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    //CHECKSTYLE IGNORE 1 LINES
	private final Renderable mBinder = new Renderable() {
		
		@Override
		public void run(long timeStamp, RenderContext context) {
			int id = mId;
			if (id == 0) {
				GLES20.glGenBuffers(1, ID, 0);
				mId = id = ID[0];
			}
			GLES20.glBindBuffer(mTarget, id);
			
			if (sTargetStackPtr >= TARGET_STACK.length - 1) {
				throw new RuntimeException("Target stack overflow (>" + TARGET_STACK.length + ")");
			}
			TARGET_STACK[++sTargetStackPtr] = mTarget;		// save stack
			
			if (mTarget == GLES20.GL_ARRAY_BUFFER) {
				if (sIdStackPtr >= ID_STACK.length - 1) {
					throw new RuntimeException("Id stack overflow (>" + ID_STACK.length + ")");
				}
				ID_STACK[++sIdStackPtr] = id;				// save stack
			} else {
				if (sIndexIdStackPtr >= INDEX_ID_STACK.length - 1) {
					throw new RuntimeException("Index id stack overflow (>" + INDEX_ID_STACK.length + ")");
				}
				INDEX_ID_STACK[++sIndexIdStackPtr] = id;	// save stack
			}
		}
	};
	
	//CHECKSTYLE IGNORE 1 LINES
	private final Renderable mDataUploader = new Renderable() {

		@Override
		public void run(long timeStamp, RenderContext context) {
			VertexBufferBlock.popVertexData(this);
			VertexBufferBlock.popVertexData(GL_TMP, 0, 1);
			int dataCount = (int) GL_TMP[0];
			Buffer buffer;
			if (mDataType == GLES20.GL_FLOAT) {
				VertexBufferBlock.rewindReadingBuffer(dataCount);
				buffer = (Buffer) VertexBufferBlock.popVertexData(dataCount);
			} else {
				IndexBufferBlock.rewindReadingBuffer(dataCount);
				buffer = (Buffer) IndexBufferBlock.popVertexData(dataCount);
			}
			
			GLError.clearGLError();
			GLES20.glBufferData(mTarget, dataCount * mDataTypeSize, buffer, mUsage);
			if (GLError.checkGLError(GLES20.GL_OUT_OF_MEMORY)) {
				//TODO:释放部分显存并重试
				GLES20.glBindBuffer(mTarget, 0);
				GLError.clearGLError();
			}
		}
		
	};

}
