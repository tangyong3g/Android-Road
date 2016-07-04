package com.graphics.engine.gl.util;


/**
 * 
 * <br>类描述: 支持设置value的Interger类
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2012-11-21]
 */
public class MutableInteger {
	private int mValue;
	
	public MutableInteger() {
		
	}
	
	public MutableInteger(int value) {
		mValue = value;
	}

	public void setValue(int value) {
		mValue = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MutableInteger)) {
			return false;
		}

		return ((MutableInteger) o).mValue == mValue;
	}

	@Override
	public int hashCode() {
		return mValue;
	}
}
