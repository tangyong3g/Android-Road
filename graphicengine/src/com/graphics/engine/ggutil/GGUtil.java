package com.graphics.engine.ggutil;

import android.content.Context;

//CHECKSTYLE:OFF 
public class GGUtil {
	/**
	 * 
	 */
	public static native void GGset520(Context context);

	static {
		try {
//			System.loadLibrary("ggutil");
		} catch (Exception e) {
			throw new RuntimeException("can not load libglndkutil.");
		}
	}
}