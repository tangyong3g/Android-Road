package com.graphics.engine.graphics;

import java.nio.ShortBuffer;

import android.util.Log;

import com.graphics.engine.util.IBufferFactory;
import com.graphics.engine.util.LinkedShortBuffer;

/**
 * 
 * <br>类描述: 按块组织的顶点索引数据缓冲区，用作数据的序列化容器。
 * <br>功能详细描述:
 * <br>在主线程上绘制图形，实际上是先将顶点的索引数据保存到一个全局的数据缓冲区中；
 * 到了GL线程执行渲染命令的时候，会从这个数据缓冲区读取数据，保存到Reading Buffer，再提交到GPU。
 * <br>跟{@link VertexBufferBlock}差不多，只是它保存的是short类型的顶点索引。
 * <br>具体使用方法可参考{@link GLNinePatch}的源代码
 * 
 * @author  dengweiming
 * @date  [2013-5-8]
 */
public final class IndexBufferBlock extends LinkedShortBuffer {
	private static final boolean DBG = false;
	private static final String TAG = "IndexBufferBlock";
	
	private volatile static IndexBufferBlock sCurWriteVertexBufferBlock;
	private volatile static IndexBufferBlock sCurReadVertexBufferBlock;
	private volatile static LinkedShortBuffer.Iterator sReadIterator;
	
	/** @hide */
	public volatile static int sWriteCount;
	/** @hide */
	public volatile static int sNativePtr;
	/** @hide */
	public volatile static int sWriteCountOnGLFrame;
	
	public static final int PER_DATA_SIZE_LIMIT = 128 * 128 * 8 * 2;	//CHECKSTYLE IGNORE
	private static int sNativeCapacity = 1024 * 64 * 2;					//CHECKSTYLE IGNORE
	private static ShortBuffer sNativeBuffer = IBufferFactory.newShortBuffer(sNativeCapacity);
	
	/**
	 * @hide
	 */
	public static void startWritingVertexBuffer(IndexBufferBlock block) {
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
	public static void startReadingVertexBuffer(IndexBufferBlock block) {
		if (DBG) {
			Log.d(TAG, "startReadingVertexBuffer block=" + block + " --------");
		}
		sNativePtr = 0;
		sReadIterator = block.iterator();
		sNativeBuffer.position(0);
		sCurReadVertexBufferBlock = block;
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
	 * @param count 本次渲染的顶点索引数据的数目总和。
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
				sNativeBuffer = IBufferFactory.newShortBuffer(cap);
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
				throw new RuntimeException("rewindReadingBuffer cap=" + sNativeCapacity + " > limit=" + PER_DATA_SIZE_LIMIT);
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
	public static void pushVertexData(short[] src, int start, int count) {
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
	 * <br>功能简述: 取出顶点数据（位置，纹理坐标等）
	 * <br>功能详细描述:
	 * <br>注意: Renderable对象如果某一分支需要读取数据，那么其他分支也要读取，保持一致。
	 * 如果读取多份数据，那么每次取出一份数据时要马上使用，不然下一次的读取会把前一次读取到的指针覆盖。
	 * <br>在GL线程调用
	 * @param count
	 * @return
	 */
	public static ShortBuffer popVertexData(int count) {
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
	 * <br>功能简述: 取出顶点数据到short[]数组中
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程调用
	 * @param dst
	 * @param offset
	 * @param count
	 */
	public static void popVertexData(short[] dst, int offset, int count) {
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
	 * @hide
	 */
	public IndexBufferBlock() {
		super(TYPE_LARGE);
	}
	
	/**
	 * @hide
	 */
	public void reset() {
		removeAll();
	}
	
}