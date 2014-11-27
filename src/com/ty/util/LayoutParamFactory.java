package com.ty.util;

import android.view.ViewGroup.LayoutParams;

/**
 * 
		*@Title:
		*@Description:
		*@Author:tangyong
		*@Since:2014-11-26
		*@Version:1.1.0
 */
public class LayoutParamFactory {

	public static LayoutParams getLayoutParams(int width, int height) {
		LayoutParams sLayoutParmsWrapContent = new LayoutParams(width, height);
		return sLayoutParmsWrapContent;
	}
	

}
