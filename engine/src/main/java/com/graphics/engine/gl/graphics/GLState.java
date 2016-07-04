package com.graphics.engine.gl.graphics;

import android.opengl.GLES20;

/**
 * 
 * <br>类描述: 维护GL状态机属性的类
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-1-4]
 * @hide
 */
public class GLState {
	static float sClearRed;
	static float sClearGreen;
	static float sClearBlue;
	static float sClearAlpha;
	
	static int sWrapS;
	static int sWrapT;
	
	static void glClearColor(float red, float green, float blue, float alpha) {
		sClearRed = red;
		sClearGreen = green;
		sClearBlue = blue;
		sClearAlpha = alpha;
		GLES20.glClearColor(red, green, blue, alpha);
	}
	
	static void setWrapMode(int wrapS, int wrapT) {
		if (sWrapS != wrapS) {
			sWrapS = wrapS;
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, wrapS);
		}
		if (sWrapT != wrapT) {
			sWrapT = wrapT;
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, wrapT);
		}
	}
}
