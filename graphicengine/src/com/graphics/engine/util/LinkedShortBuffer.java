package com.graphics.engine.util;

import java.nio.ShortBuffer;

/**
 * 
 * <br>类描述: 短整型数缓冲区
 * <br>功能详细描述:
 * 使用短整型数组块的链表实现，并且使用对象池去缓存这些数组块，释放后可以重用，避免GC。
 * 
 * @author  dengweiming
 * @date  [2012-9-18]
 */
public class LinkedShortBuffer {
	public static final int TYPE_SMALL = 0;
	public static final int TYPE_MEDIUM = 1;
	public static final int TYPE_LARGE = 2;
	
	private final static ShortBlock SOLDIER_BLOCK = new ShortBlock(TYPE_MEDIUM);
	public static int sTotalCapacity = 0;
	
	ShortBlock mHead;
	ShortBlock mTail;
	int mBlockSize;
	int mSize;
	int mType;
	short[] mData;
	Iterator mIterator;
	Iterator mIterator2;

	/**
	 * 迭代器
	 */
	public static interface Iterator {
		
		public int position();
		
		public void position(int index);
		
		public short get();
		
		public void set(short x);

		public boolean hasNext();

		public short next();

		public int next(short[] buffer, int offset, int count);
		
		public int next(ShortBuffer buffer, int count);
	}

	/**
	 * @param type 分块的类型，可选参数为 {@link #TYPE_SMALL} （一般用这个就可以了）, {@link #TYPE_MEDIUM}, {@link #TYPE_LARGE}
	 */
	public LinkedShortBuffer(int type) {
		if (type < TYPE_SMALL || type > TYPE_LARGE) {
			throw new IllegalArgumentException("type=" + type 
					+ " is not in range[" + TYPE_SMALL + ", " + TYPE_LARGE + "]");
		}
		mType = type;
		mBlockSize = ShortBlock.BLOCK_SIZE[mType];
		mIterator = new MyIterator();
		mIterator2 = new MyIterator();
		sTotalCapacity += mBlockSize * 2;
	}

	public int size() {
		return mSize;
	}

	public Iterator iterator() {
		mIterator.position(0);
		return mIterator;
	}
	
	public Iterator iterator2() {
		mIterator2.position(0);
		return mIterator2;
	}
	
	public short[] getTempBuffer() {
		return SOLDIER_BLOCK.data;
	}

	public void pushBack(short x) {
		if (mTail == null || mTail.wc >= mBlockSize) {
			acquireTail();
		}
		mData[mTail.wc++] = x;
		++mSize;
	}

	public void pushBack(short[] src, int start, int count) {
		if (start < 0 || count <= 0 || start + count > src.length) {
			throw new IndexOutOfBoundsException();
		}
		if (mTail == null || mTail.wc >= mBlockSize) {
			acquireTail();
		}
		int cap = mBlockSize - mTail.wc;
		while (count > 0) {
			int countToCopy = Math.min(count, cap);
			System.arraycopy(src, start, mData, mTail.wc, countToCopy);
			mTail.wc += countToCopy;
			mSize += countToCopy;
			start += countToCopy;
			count -= countToCopy;
			cap = mBlockSize;
			if (count > 0) {
				acquireTail();
			}
		}
	}

	public void popBack(int count) {
		if (mSize <= 0) {
			return;
		}
		if (count > mSize) {
			count = mSize;
		}
		while (count > 0) {
			int countToRemove = Math.min(mTail.wc, count);
			mTail.wc -= countToRemove;
			mSize -= countToRemove;
			count -= countToRemove;
			if (mTail.wc == 0) {
				removeTail();
			}
		}
	}

	public void removeAll() {
		if (mHead != null) {
			mHead.release();
		}
		mHead = null;
		mTail = null;
		mData = null;
		mSize = 0;
	}

	private void acquireTail() {
		if (mHead == null) {
			mHead = ShortBlock.acquire(mType);
			mHead.sn = 0;
			mTail = mHead;
			mData = mTail.data;
		} else {
			ShortBlock next = ShortBlock.acquire(mType);
			mTail.mNext = next;
			next.mPrev = mTail;
			next.sn = mTail.sn + 1;
			mTail = next;
			mData = next.data;
		}
	}

	private void removeTail() {
		if (mTail != null) {
			ShortBlock prev = mTail.mPrev;
			mTail.release();
			mTail = prev;
			if (prev != null) {
				prev.mNext = null;
				mData = prev.data;
			} else {
				mHead = null;
				mData = null;
			}
		}
	}

	//CHECKSTYLE IGNORE 1 LINES
	private class MyIterator implements Iterator {
		private ShortBlock mBlock;
		private int mRc;
		private int mPos;

		@Override
		public short next() {
			final short res = mBlock.data[mRc]; 
			if (mPos >= mSize - 1) {
				mPos = mSize;
				return res;
			}
			++mPos;
			if (++mRc >= mBlock.wc) {
				mBlock = mBlock.mNext;
				mRc = 0;
			}
			return res;
		}
		
		@Override
		public boolean hasNext() {
			return mPos < mSize;
		}
		
		@Override
		public int position() {
			return mPos;
		}

		@Override
		public void position(int index) {
			if (index <= 0 || mSize <= 0) {
				mPos = 0;
				mRc = 0;
				mBlock = mHead != null ? mHead : SOLDIER_BLOCK;
				return;
			}
			index = Math.max(0, Math.min(index, mSize - 1));
			mPos = index;
			if (index <= mSize / 2) {
				// search forward from head
				mBlock = mHead;
				int i = 0;
				while (i + mBlock.wc <= index) {
					i += mBlock.wc;
					mBlock = mBlock.mNext;
				}
				mRc = index - i;
			} else {
				// search backward from tail
				mBlock = mTail;
				int i = mSize - mBlock.wc;	// index of the first element of this block 
				while (i > index) {
					mBlock = mBlock.mPrev;
					i -= mBlock.wc;
				}
				mRc = index - i;
			}
		}

		@Override
		public short get() {
			return mBlock.data[mRc];
		}

		@Override
		public void set(short x) {
			mBlock.data[mRc] = x;
		}

		@Override
		public int next(short[] buffer, int offset, int count) {
			if (mPos >= mSize - 1) {
				mPos = mSize;
				return 0;
			}

			mPos += count;
			int res = 0;
			while (count > 0) {
				int countToCopy = Math.min(count, mBlock.wc - mRc);
				if (buffer != null) {
					System.arraycopy(mBlock.data, mRc, buffer, offset, countToCopy);
				}
				offset += countToCopy;

				count -= countToCopy;
				mRc += countToCopy;
				res += countToCopy;
				if (mRc >= mBlock.wc) {
					mBlock = mBlock.mNext;
					mRc = 0;
					if (mBlock == null) {
						break;
					}
				}
			}

			if (mPos >= mSize) {
				mPos = mSize;
				mBlock = mTail;
				mRc = mBlock.wc - 1;
			}
			return res;
		}
		
		@Override
		public int next(ShortBuffer buffer, int count) {
			if (mPos >= mSize - 1) {
				mPos = mSize;
				return 0;
			}

			mPos += count;
			int res = 0;
			while (count > 0) {
				int countToCopy = Math.min(count, mBlock.wc - mRc);
				buffer.put(mBlock.data, mRc, countToCopy);

				count -= countToCopy;
				mRc += countToCopy;
				res += countToCopy;
				if (mRc >= mBlock.wc) {
					mBlock = mBlock.mNext;
					mRc = 0;
					if (mBlock == null) {
						break;
					}
				}
			}

			if (mPos >= mSize) {
				mPos = mSize;
				mBlock = mTail;
				mRc = mBlock.wc - 1;
			}
			return res;
		}

	};
	
	/**
	 * 使用内存池来缓存的浮点数组块
	 */
	@SuppressWarnings("unchecked")
	private static class ShortBlock implements Poolable<ShortBlock> {
		
		static final int[] BLOCK_SIZE = {128, 1024, 1024 * 32};
		static final int[] POOL_LIMIT = {1024, 512, 32};
		static final Pool<ShortBlock>[] POOLS;
		
		static {
			POOLS = new Pool[BLOCK_SIZE.length];
			
			for (int i = 0; i < BLOCK_SIZE.length; ++i) {
				final int type = i;
				final int limit = POOL_LIMIT[i];
				POOLS[i] = Pools.finitePool(new PoolableManager<ShortBlock>() {
					public ShortBlock newInstance() {
						return new ShortBlock(type);
					}

					public void onAcquired(ShortBlock element) {
					}

					public void onReleased(ShortBlock element) {
						element.wc = 0;
						element.mPrev = null;
					}
				}, limit);
			}
		}
		
		
		static ShortBlock acquire(int type) {
			return POOLS[type].acquire();
		}
		
	    ShortBlock mNext;
	    ShortBlock mPrev;
	    //CHECKSTYLE IGNORE 4 LINES
	    final short[] data;
	    int wc;
	    int sn;
	    final int type;
	    
	    ShortBlock(int type) {
			this.type = type;
			data = new short[BLOCK_SIZE[type]];
		}
	    
	    public void setNextPoolable(ShortBlock element) {
	        mNext = element;
	    }

	    public ShortBlock getNextPoolable() {
	        return mNext;
	    }
	    
		void release() {
			ShortBlock block = this;
			while (block != null) {
				ShortBlock next = block.mNext;
				POOLS[type].release(block);
				block = next;
			}
		}
	    
	}

}
