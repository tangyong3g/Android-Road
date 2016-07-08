package com.graphics.engine.graphics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import android.opengl.GLES20;
import android.util.Log;

import com.graphics.engine.util.FastQueue;
import com.graphics.engine.util.FastQueue.Processor;
import com.graphics.engine.util.Pool;
import com.graphics.engine.util.Poolable;
import com.graphics.engine.util.PoolableManager;
import com.graphics.engine.util.Pools;

/**
 * 
 * <br>类描述: 纹理管理者
 * <br>功能详细描述:
 * <ul> 一些常用方法：
 * 	<li>{@link #getInstance()}
 * 	<li>{@link #registerTextureListener(TextureListener)}
 * 	<li>{@link #unRegisterTextureListener(TextureListener)}
 * 	<li>{@link #getAllInvalidateCount()}
 * </ul>
 */
public class TextureManager {
	private static final String TAG = "DWM";
	private static final boolean DBG = false;

	private HashSet<TextureListener> mTextureListeners = new HashSet<TextureListener>();
	private HashSet<StaticTextureListener> mStaticTextureListeners = new HashSet<StaticTextureListener>();
	
	private int mAllInvalidateCount;

	private static TextureManager sInstance = null;
	
	private static final int TYPE_FBO = 0;
	private static final int TYPE_RENDER_BUFFER = 1;
	private static final int TYPE_TEXTURE = 2;
	private static final int TYPE_VBO = 3;
	
	static int sCountFBO;
	static int sCountRenderBuffer;
	static int sCountTexture;
	static int sCountVBO;

	private TextureManager() {
	}

	/**
	 * 获取一个静态实例
	 */
	public static synchronized TextureManager getInstance() {
		if (sInstance == null) {
			sInstance = new TextureManager();
		}

		return sInstance;
	}

	/**
	 * 注册纹理失效事件监听者
	 * @see {@link #unRegisterTextureListener(TextureListener)}
	 */
	public boolean registerTextureListener(TextureListener l) {
		if (l == null) {
			return false;
		}
		synchronized (mTextureListeners) {
			return mTextureListeners.add(l);
		}
	}

	/**
	 * 反注册纹理失效事件监听者
	 * <br>注意：如果纹理注册了静态监听，即{@link #registerStaticTextureListener(StaticTextureListener)}，
	 * <br>那么需要先{@link #unRegisterStaticTextureListener(StaticTextureListener)}再调用本方法才有效，
	 * <br>不过一般来说，对于静态监听的对象，例如框架内部的shader，就是不需要调用本方法。
	 */
	public boolean unRegisterTextureListener(TextureListener l) {
		if (l == null) {
			return false;
		}
		
		//如果是静态纹理，不能取消监听
		synchronized (mStaticTextureListeners) {
			if (mStaticTextureListeners.contains(l)) {
				return false;
			}
		}
		
		synchronized (mTextureListeners) {
			return mTextureListeners.remove(l);
		}
	}

	/**
	 * 注册静态纹理失效事件监听者
	 * @see {@link #unRegisterTextureListener(TextureListener)}
	 */
	public boolean registerStaticTextureListener(StaticTextureListener l) {
		if (l == null) {
			return false;
		}
		
		synchronized (mStaticTextureListeners) {
			mStaticTextureListeners.add(l);
		}
		return registerTextureListener(l);
	}

	/**
	 * 反注册静态纹理失效事件监听者
	 */
	public boolean unRegisterStaticTextureListener(StaticTextureListener l) {
		if (l == null) {
			return false;
		}
		synchronized (mStaticTextureListeners) {
			mStaticTextureListeners.remove(l);
		}
		return unRegisterTextureListener(l);
	}

	/**
	 * 初始化预定义的着色器
	 * @hide
	 */
	public void initInternalShaders() {
		onGLContextLostStatic();
		
		TextureShader.initInternalShaders();
		ColorShader.initInternalShaders();
		ColorAttributeShader.initInternalShaders();
		
		//将静态纹理重新注册监听
		synchronized (mStaticTextureListeners) {
			synchronized (mTextureListeners) {
				Iterator<StaticTextureListener> iterator = mStaticTextureListeners.iterator();
				while (iterator.hasNext()) {
					StaticTextureListener listener = iterator.next();
					mTextureListeners.add(listener);
				}
			}
		}
	}
	
	/**
	 * 纹理失效时通知预定义的着色器
	 * @hide
	 */
	public static void onGLContextLostStatic() {
		GLShaderProgram.onGLContextLostStatic();
	}

	/**
	 * 通知所有的纹理失效事件监听者
	 * @hide
	 */
	public void notifyAllInvalidated() {
		++mAllInvalidateCount;
		synchronized (mTextureListeners) {
			Iterator<TextureListener> iterator = mTextureListeners.iterator();
			while (iterator.hasNext()) {
				TextureListener listener = iterator.next();
				listener.onTextureInvalidate();
			}
		}
		
		sCountFBO = 0;
		sCountRenderBuffer = 0;
		sCountTexture = 0;
		sCountVBO = 0;
	}
	
	/**
	 * 获取纹理失效事件发生的次数。
	 * <br>如果一个监听者反注册监听了，则需要使用本方法记录次数，到下次使用到纹理的时候，
	 * 需要获取新的次数，如果次数不想等，则说明中间发生了纹理失效事件，那么纹理已经失效了，
	 * 需要自行调用{@link TextureListener#onTextureInvalidate()}。
	 */
	public int getAllInvalidateCount() {
		return mAllInvalidateCount;
	}
	
	/**
	 * <br>功能简述: 删除一个FrameBuffer
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param id
	 * @hide
	 */
	public void deleteFrameBuffer(int id) {
		if (id != 0) {
			PoolableIntergerPair pair = sIntergerPairPool.acquire();
			pair.mFirst = id;
			pair.mSecond = TYPE_FBO;
			sIntergerPairQueue.pushBack(pair);
		}
	}
	
	/**
	 * <br>功能简述: 删除一个RenderBuffer（FrameBuffer的一个附件）
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param id
	 * @hide
	 */
	public void deleteRenderBuffer(int id) {
		if (id != 0) {
			PoolableIntergerPair pair = sIntergerPairPool.acquire();
			pair.mFirst = id;
			pair.mSecond = TYPE_RENDER_BUFFER;
			sIntergerPairQueue.pushBack(pair);
		}
	}
	
	/**
	 * <br>功能简述: 删除一个纹理
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param id
	 * @hide
	 */
	public void deleteTexture(int id) {
		if (id != 0) {
			PoolableIntergerPair pair = sIntergerPairPool.acquire();
			pair.mFirst = id;
			pair.mSecond = TYPE_TEXTURE;
			sIntergerPairQueue.pushBack(pair);
		}
	}
	
	/**
	 * <br>功能简述: 删除一个Vertex Buffer Object
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param id
	 * @hide
	 */
	public void deleteVBO(int id) {
		if (id != 0) {
			PoolableIntergerPair pair = sIntergerPairPool.acquire();
			pair.mFirst = id;
			pair.mSecond = TYPE_VBO;
			sIntergerPairQueue.pushBack(pair);
		}
	}
	
	/**
	 * 清空删除队列
	 */
	void clearDeleteQueue() {
		sIntergerPairQueue.cleanup();
		Arrays.fill(sIntergerPairArray, null);
		sArrayIndex = 0;
	}

	/**
	 * 删除当前队列里的纹理和VBO
	 */
	public void handleDeleteTextures() {
		sArrayIndex = 0;
		//将数据从sIntergerPairQueue移到sIntergerPairArray
		sIntergerPairQueue.process(sIntergerPairProcessor);
		int arrayCount = sArrayIndex;
		if (arrayCount <= 0) {
			return;
		}
		
		int begin = 0;
		int end = 0;
		
		GLError.clearGLError();
		
		//先清除数量相对较多的纹理，可减少后续处理数目
		for (int i = 0; i < arrayCount; ++i) {
			PoolableIntergerPair pair = sIntergerPairArray[i];
			if (pair.mSecond == TYPE_TEXTURE) {
				sIntergerArray[end++] = pair.mFirst;
				sIntergerPairArray[i--] = sIntergerPairArray[--arrayCount];
			}
		}
		if (end != begin) {
			sCountTexture -= end - begin;
			if (DBG) {
				Log.v(TAG, "delete texture: remain count=" + sCountTexture);
			}
			GLES20.glDeleteTextures(end - begin, sIntergerArray, begin);
			begin = end;
		}
		
		//清除帧缓冲区对象
		for (int i = 0; i < arrayCount; ++i) {
			PoolableIntergerPair pair = sIntergerPairArray[i];
			if (pair.mSecond == TYPE_FBO) {
				sIntergerArray[end++] = pair.mFirst;
				sIntergerPairArray[i--] = sIntergerPairArray[--arrayCount];
			}
		}
		if (end != begin) {
			sCountFBO -= end - begin;
			if (DBG) {
				Log.v(TAG, "delete FBO: remain count=" + sCountFBO);
			}
			GLES20.glDeleteFramebuffers(end - begin, sIntergerArray, begin);
			begin = end;
		}
		
		//清除渲染缓冲区
		for (int i = 0; i < arrayCount; ++i) {
			PoolableIntergerPair pair = sIntergerPairArray[i];
			if (pair.mSecond == TYPE_RENDER_BUFFER) {
				sIntergerArray[end++] = pair.mFirst;
				sIntergerPairArray[i--] = sIntergerPairArray[--arrayCount];
			}
		}
		if (end != begin) {
			sCountRenderBuffer -= end - begin;
			if (DBG) {
				Log.v(TAG, "delete render buffer: remain count=" + sCountRenderBuffer);
			}
			GLES20.glDeleteRenderbuffers(end - begin, sIntergerArray, begin);
			begin = end;
		}

		//清除顶点缓冲区对象
		for (int i = 0; i < arrayCount; ++i) {
			PoolableIntergerPair pair = sIntergerPairArray[i];
			if (pair.mSecond == TYPE_VBO) {
				sIntergerArray[end++] = pair.mFirst;
				sIntergerPairArray[i--] = sIntergerPairArray[--arrayCount];
			}
		}
		if (end != begin) {
			sCountVBO -= end - begin;
			if (DBG) {
				Log.v(TAG, "delete VBO: remain count=" + sCountVBO);
			}
			GLES20.glDeleteBuffers(end - begin, sIntergerArray, begin);
			begin = end;
		}
				
		GLError.checkGLError("handleDeleteTextures");
	}
	
	/** @hide */
	public void cleanup() {
		synchronized (mTextureListeners) {
			mTextureListeners.clear();
		}
	}
	

	private static final int INTEGER_PAIR_POOL_LIMIT = 1024;
	private static final int INTEGER_PAIR_QUEUE_LIMIT = 1024;
	
	//CHECKSTYLE IGNORE 1 LINES
	private static class PoolableIntergerPair implements Poolable<PoolableIntergerPair>{
		int mFirst;
		int mSecond;
		
		private PoolableIntergerPair mNext;
		@Override
		public void setNextPoolable(PoolableIntergerPair element) {
			mNext = element;
		}
		
		@Override
		public PoolableIntergerPair getNextPoolable() {
			return mNext;
		}
		
		public void release() {
			sIntergerPairPool.release(this);
		}
	}
	
	//CHECKSTYLE IGNORE 1 LINES
	private static final Pool<PoolableIntergerPair> sIntergerPairPool = Pools.finitePool(
			new PoolableManager<PoolableIntergerPair>() {

				@Override
				public PoolableIntergerPair newInstance() {
					return new PoolableIntergerPair();
				}

				@Override
				public void onAcquired(PoolableIntergerPair element) {
				}

				@Override
				public void onReleased(PoolableIntergerPair element) {
				}
			}, INTEGER_PAIR_POOL_LIMIT);
	
	//CHECKSTYLE IGNORE 1 LINES
	private static final FastQueue<PoolableIntergerPair> sIntergerPairQueue = new FastQueue<PoolableIntergerPair>(INTEGER_PAIR_QUEUE_LIMIT);
	
	//CHECKSTYLE IGNORE 1 LINES
	private static Processor<PoolableIntergerPair> sIntergerPairProcessor = new Processor<PoolableIntergerPair>() {

		@Override
		public void process(PoolableIntergerPair object) {
			if (object == null) {
				return;
			}
			sIntergerPairArray[sArrayIndex++] = object;
		}
	};
	
	final static PoolableIntergerPair[] sIntergerPairArray = new PoolableIntergerPair[INTEGER_PAIR_QUEUE_LIMIT];	//CHECKSTYLE IGNORE
	final static int[] sIntergerArray = new int[INTEGER_PAIR_QUEUE_LIMIT];	//CHECKSTYLE IGNORE
	static int sArrayIndex;
	
	
}