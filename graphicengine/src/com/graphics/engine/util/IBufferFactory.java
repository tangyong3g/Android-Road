package com.graphics.engine.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * <br>类描述: Buffer的工厂类
 * <br>功能详细描述:
 * 提供创建Buffer的方法。 
 * <br>注意OPhone中要传入gl*Pointer()函数的Buffer对象必须要为direct模式申请的，
 * 这样可以确保缓存对象放置在Native的堆中，以免受到Java端的垃圾回收机制的影响。
 * 对于FloatBuffer,ShortBuffer,IntBuffer等多字节的缓存对象， 
 * 它们的字节顺序 必须设置为nativeOrder。
 * 
 * @author Yong
 */
public class IBufferFactory {
	private static final int SIZE_OF_FLOAT = 4;
	private static final int SIZE_OF_SHORT = 2;
	
	/**
	 * 创建新的FloatBuffer对象
	 * 
	 * @param numElements float元素的个数
	 * @return
	 */
	public static FloatBuffer newFloatBuffer(int numElements) {
		ByteBuffer bb = ByteBuffer.allocateDirect(numElements * SIZE_OF_FLOAT);
		bb.order(ByteOrder.nativeOrder());
		return bb.asFloatBuffer();
	}
	
	/**
	 * 将一个数组的元素封装成新的FloatBuffer对象
	 * @param buffer
	 * @return
	 */
	public static FloatBuffer newFloatBuffer(float[] buffer) {
		ByteBuffer bb = ByteBuffer.allocateDirect(buffer.length * SIZE_OF_FLOAT);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(buffer);
		fb.position(0);
		return fb;
	}

	/**
	 * 创建新的ShortBuffer对象
	 * 
	 * @param numElements short元素的个数
	 * @return
	 */
	public static ShortBuffer newShortBuffer(int numElements) {
		ByteBuffer bb = ByteBuffer.allocateDirect(numElements * SIZE_OF_SHORT);
		bb.order(ByteOrder.nativeOrder());
		return bb.asShortBuffer();
	}
	
	/**
	 * 将一个数组的元素封装成新的ShortBuffer对象
	 * @param buffer
	 * @return
	 */
	public static ShortBuffer newShortBuffer(short[] buffer) {
		ByteBuffer bb = ByteBuffer.allocateDirect(buffer.length * SIZE_OF_SHORT);
		bb.order(ByteOrder.nativeOrder());
		ShortBuffer sb = bb.asShortBuffer();
		sb.put(buffer);
		sb.position(0);
		return sb;
	}

	public static void read(FloatBuffer fb, Vector3f v) {
		v.x = fb.get();
		v.y = fb.get();
		v.z = fb.get();
	}

	public static void fillBuffer(FloatBuffer fb, Vector3f v) {
		fb.put(v.x);
		fb.put(v.y);
		fb.put(v.z);
	}

	public static void fillBuffer(FloatBuffer fb, Vector3f v, int limit) {
		fb.put(v.x);
		fb.put(1.0f - v.y);

		if (limit == 2) {

		} else {
			fb.put(v.z);
		}
	}

	public static void fillBuffer(FloatBuffer fb, Vector4f v) {
		fb.put(v.x);
		fb.put(v.y);
		fb.put(v.z);
		fb.put(v.w);
	}

	public static void fillBuffer(ShortBuffer sb, int[] data) {
		for (int i = 0; i < data.length; i++) {
			sb.put((short) data[i]);
		}
	}

}
