package com.graphics.engine.util;


/**
 * 
 * <br>类描述: 浮点数动态数组
 * <br>功能详细描述:
 * 替代ArrayList<Float>，更高效率。
 * 
 * @author  dengweiming
 * @date  [2013-7-23]
 */
public class FloatArrayList {
	private static final int ARRAY_INITIAL_CAPACITY = 256;
	private static final int ARRAY_CAPACITY_INCREMENT = 256;
	float[] mArray;
	int mSize;

	public FloatArrayList() {
		mArray = new float[ARRAY_INITIAL_CAPACITY];
	}

	public FloatArrayList(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("capacity=" + capacity + " should greater than 0.");
		}
		mArray = new float[capacity];
	}
	
	public final float[] array() {
		return mArray;
	}
	
	public final int length() {
		return mSize;
	}
	
	public final int capacity() {
		return mArray.length;
	}
	
	public float get(int index) {
		return mArray[index];
	}

	public void add(float value, int index) {
		float[] a = mArray;
		final int size = mSize;
		final int capacity = a.length;
		if (index == size) {
			if (capacity == size) {
				mArray = new float[capacity + ARRAY_CAPACITY_INCREMENT];
				System.arraycopy(a, 0, mArray, 0, capacity);
				a = mArray;
			}
			a[mSize++] = value;
		} else if (index < size) {
			if (capacity == size) {
				mArray = new float[capacity + ARRAY_CAPACITY_INCREMENT];
				System.arraycopy(a, 0, mArray, 0, index);
				System.arraycopy(a, index, mArray, index + 1, size - index);
				a = mArray;
			} else {
				System.arraycopy(a, index, a, index + 1, size - index);
			}
			a[index] = value;
			mSize++;
		} else {
			throw new IndexOutOfBoundsException("index=" + index + " count=" + size);
		}
	}
	
	public void append(float value) {
		float[] a = mArray;
		final int size = mSize;
		final int capacity = a.length;
		if (capacity == size) {
			mArray = new float[capacity + ARRAY_CAPACITY_INCREMENT];
			System.arraycopy(a, 0, mArray, 0, capacity);
			a = mArray;
		}
		a[mSize++] = value;
	}

	public void remove(int index) {
		final float[] a = mArray;
		final int size = mSize;
		if (index == size - 1) {
//			a[index].cleanup();
			--mSize;
//			a[mSize] = null;
		} else if (index >= 0 && index < size) {
//			a[index].cleanup();
			System.arraycopy(a, index + 1, a, index, size - index - 1);
			--mSize;
//			a[mSize] = null;
		} else {
			throw new IndexOutOfBoundsException();
		}
	}
	
    public void remove(int start, int count) {
		final float[] a = mArray;
		final int size = mSize;

        start = Math.max(0, start);
        final int end = Math.min(size, start + count);

        if (start == end) {
            return;
        }
        
        mSize -= end - start;

        if (end == size) {
//            for (int i = start; i < end; i++) {
//            	a[i].cleanup();
//              a[i] = null;
//            }
        } else {
//            for (int i = start; i < end; i++) {
//            	//a[i].cleanup();
//            }

            // Since we're looping above, we might as well do the copy, but is arraycopy()
            // faster than the extra 2 bounds checks we would do in the loop?
            System.arraycopy(a, end, a, start, size - end);

//            for (int i = size - (end - start); i < size; i++) {
//                a[i] = null;
//            }
        }

    }
    
    public void removeAll() {
//		final float[] a = mArray;
//		final int size = mSize;
//		for (int i = 0; i < size; i++) {
//			a[i].cleanup();
//			a[i] = null;
//		}
		mSize = 0;
    }
    
	public void grow(int capacity) {
		if (mArray.length >= capacity) {
			return;
		}
		float[] a = mArray;
		mArray = new float[capacity];
		System.arraycopy(a, 0, mArray, 0, mSize);
	}
}
