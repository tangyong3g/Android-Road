package com.graphics.engine.gl.graphics;

import android.util.Log;

import com.graphics.engine.gl.util.IBufferFactory;
import com.graphics.engine.gl.util.LinkedFloatBuffer;

import java.nio.FloatBuffer;

/**
 * 
 * <br>类描述: 按块组织的顶点数据缓冲区，用作数据的序列化容器。
 * <br>功能详细描述:
 * <br>在主线程上绘制图形，实际上是先将顶点的位置，纹理坐标等数据保存到一个全局的数据缓冲区中；
 * 到了GL线程执行渲染命令的时候，会从这个数据缓冲区读取数据，保存到Reading Buffer，再提交到GPU。
 * <br>具体使用方法参考{@link ColorGLDrawable}的代码。
 * 
 * @author  dengweiming
 * @date  [2013-5-8]
 */
public final class VertexBufferBlock extends LinkedFloatBuffer {
	private static final boolean DBG = false;
	private static final String TAG = "VertexBufferBlock";
	
	private static VertexBufferBlock sCurWriteVertexBufferBlock;
	private static VertexBufferBlock sCurReadVertexBufferBlock;
	private static LinkedFloatBuffer.Iterator sReadIterator;
	
	/** @hide */
	public static int sWriteCount;
	/** @hide */
	public static int sNativePtr;
	/** @hide */
	public static int sWriteCountOnGLFrame;
	
	public static final int PER_DATA_SIZE_LIMIT = 128 * 128 * 8;	//CHECKSTYLE IGNORE
	private static int sNativeCapacity = 1024 * 64;					//CHECKSTYLE IGNORE
	private static FloatBuffer sNativeBuffer = IBufferFactory.newFloatBuffer(sNativeCapacity);
	
	
	static Renderable sLastRenderable;
	static Renderable sCurRenderable;
	static int sLastPos;
	static int sCurPos;
	
	/**
	 * @hide
	 */
	public static void startWritingVertexBuffer(VertexBufferBlock block) {
		if (DBG) {
			Log.d(TAG, "startWritingVertexBuffer block=" + block + " =============");
		}
		sWriteCount = 0;
		block.reset();
		sCurWriteVertexBufferBlock = block;
	}
	
	/**
	 * @hide
	 */
	public static void startReadingVertexBuffer(VertexBufferBlock block) {
		if (DBG) {
			Log.d(TAG, "startReadingVertexBuffer block=" + block + " --------");
		}
		sNativePtr = 0;
		sReadIterator = block.iterator();
		sNativeBuffer.position(0);
		sCurReadVertexBufferBlock = block;
		
		sLastRenderable = null;
		sCurRenderable = null;
		sLastPos = 0;
		sCurPos = 0;
	}
	
	/**
	 * @hide
	 */
	public static void finishReadingVertexBuffer() {
		sCurReadVertexBufferBlock = null;
		sReadIterator = null;
	}
	
	/**
	 * <br>功能简述: 清空ReadingBuffer
	 * <br>功能详细描述: 在渲染{@link Renderable#run(long, RenderContext)}时，需要从数据缓冲区读取（几组）数据到ReadingBuffer中，
	 * 然后再提交到GPU。
	 * <br>注意: 在GL线程上调用
	 * @param count 本次渲染的顶点数据（位置，纹理坐标等）的数目总和。
	 * @return
	 */
	public static boolean rewindReadingBuffer(int count) {
		sNativePtr = 0;
		sNativeBuffer.position(0);
		if (sNativeCapacity < count) {
//			try {
				int cap = Math.max(count, sNativeCapacity * 2);
				if (DBG) {
					Log.i(TAG, "rewindReadingBuffer count=" + count + " cap=" + sNativeCapacity + " -> " + cap);
				}
				sNativeBuffer = IBufferFactory.newFloatBuffer(cap);
//			} catch (OutOfMemoryError e) {
//				if (DBG) {
//					Log.w(TAG, "rewindReadingBuffer OutOfMemoryError");
//				}
//				return false;
//			}
			sNativeCapacity = sNativeBuffer.capacity();
			if (sNativeCapacity > PER_DATA_SIZE_LIMIT) {
				if (DBG) {
					Log.w(TAG, "rewindReadingBuffer cap=" + sNativeCapacity + " > limit=" + PER_DATA_SIZE_LIMIT);
				}
				throw new RuntimeException("rewindReadingBuffer cap=" + sNativeCapacity
						+ " > limit=" + PER_DATA_SIZE_LIMIT + ". Try reducing vertex count.");
			}
		}
		return true;
	}
	
	/**
	 * <br>功能简述: 提交顶点数据（位置，纹理坐标等）
	 * <br>功能详细描述:
	 * <br>注意: 每次调用 {@link GLCanvas#addRenderable(Renderable, RenderContext)}的时候，
	 * 如果该Renderable对象需要读取数据，那么就要调用本方法提交数据
	 * @param src
	 * @param start
	 * @param count
	 */
	public static void pushVertexData(float[] src, int start, int count) {
		if (DBG) {
			Log.d(TAG, "push count=" + count);
		}
		if (count > 0) {
			if (start < 0 || count <= 0 || start + count > src.length) {
				throw new IndexOutOfBoundsException();
			}
			sCurWriteVertexBufferBlock.pushBack(src, start, count);
			sWriteCount += count;
		} else {
			throw new IllegalArgumentException("count=" + count);
		}
	}
	
	/**
	 * <br>功能简述: 取出顶点数据（位置，纹理坐标等）到Readingbuffer
	 * <br>功能详细描述:
	 * <br>注意: Renderable对象如果某一分支需要读取数据，那么其他分支也要读取，保持一致。
	 * 如果读取多份数据，那么每次取出一份数据时要马上使用，不然下一次的读取会把前一次读取到的指针覆盖。
	 * @param count
	 * @return
	 */
	public static FloatBuffer popVertexData(int count) {
		if (DBG) {
			Log.d(TAG, "pop count=" + count);
		}
		if (count > 0) {
			if (!sReadIterator.hasNext()) {
				throw new IndexOutOfBoundsException("Should be pop more data than push.");
			}
			sNativeBuffer.position(sNativePtr);
			sReadIterator.next(sNativeBuffer, count);
			sNativeBuffer.position(sNativePtr);
			sNativePtr += count;
		} else {
			throw new IllegalArgumentException("count=" + count);
		}
		return sNativeBuffer;
	}
	
	/**
	 * <br>功能简述: 取出顶点数据到float[]数组中
	 * @param dst	为null时，仅仅跳过这些数据而不拷贝
	 */
	public static void popVertexData(float[] dst, int offset, int count) {
		if (DBG) {
			Log.d(TAG, "pop count=" + count);
		}
		if (count > 0) {
			if (!sReadIterator.hasNext()) {
				throw new IndexOutOfBoundsException("Should be pop more data than push.");
			}
			sReadIterator.next(dst, offset, count);
		} else {
			throw new IllegalArgumentException("count=" + count);
		}
	}
	
	/**
	 * <br>功能简述: 把一个Renderable对象的地址提交，以便后续渲染的时候校验数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param renderable
	 */
	public static void pushVertexData(Renderable renderable) {
		float x = renderable.hashCode() & 0xFFFF;
		sCurWriteVertexBufferBlock.pushBack(x);
		sWriteCount += 1;
	}
	
	/**
	 * <br>功能简述: 根据一个Renderable对象的地址校验数据在反序列化时是否出错
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param renderable
	 */
	public static void popVertexData(Renderable renderable) {
		if (DBG) {
			Log.d(TAG, "pop");
		}
		sLastRenderable = sCurRenderable;
		sCurRenderable = renderable;
		if (!sReadIterator.hasNext()) {
			throw new IndexOutOfBoundsException("Should be pop more data than push.");
		}
		int last = sLastPos;
		sLastPos = sCurPos;
		sCurPos = sReadIterator.position();
		float x = sReadIterator.next();
		final int hashCode = renderable.hashCode();
//		if(last != 0) {
		if (x != (hashCode & 0xFFFF)) {
			float[] v = new float[48];
			int pos1 = Math.max(sLastPos - 24, 0);
			sReadIterator.position(pos1);
			sReadIterator.next(v, 0, v.length);
			String values1 = "";
			for (int i = 0; i < v.length; ++i) {
				if (i % 10 == 0) {
					values1 += "\n";
				}
				values1 += "\t" + v[i];
			}
			int pos2 = Math.max(sCurPos - 24, 0);
			sReadIterator.position(pos2);
			sReadIterator.next(v, 0, v.length);
			String values2 = "";
			for (int i = 0; i < v.length; ++i) {
				if (i % 10 == 0) {
					values2 += "\n";
				}
				values2 += "\t" + v[i];
			}
			throw new RuntimeException(
					"\n====renderable last=" + sLastRenderable + "/" + (sLastRenderable.hashCode() & 0xFFFF) 
					+ " cur=" + sCurRenderable + "/" + (sCurRenderable.hashCode() & 0xFFFF) + " x=" + x + 
					" \n====pos last=" + sLastPos +	" oldPos=" + sCurPos 
					+ " \n====values1 start=" + pos1 + " : " + values1 
					+ " \n====values2 start=" + pos2 + " : " + values2);
		}
	}
	
	/**
	 * @hide
	 */
	public VertexBufferBlock() {
		super(TYPE_LARGE);
	}
	
	/**
	 * @hide
	 */
	public void reset() {
		removeAll();
	}
	
}
