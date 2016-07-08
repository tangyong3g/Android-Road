package com.graphics.engine.util;

import android.graphics.Typeface;

/** 
 * 字体管理
 * @author ouyonglun
 *
 */
public class FontStyleManager {
	
	
	private static Typeface sTypeface;
	
	public static void setFontStyle(Typeface typeface) {
		sTypeface = typeface;
	}
	
	public static Typeface getFontStyle() {
		if (sTypeface == null) {
			return Typeface.DEFAULT;
		} else {
			return sTypeface;
		}
	}
	
}
