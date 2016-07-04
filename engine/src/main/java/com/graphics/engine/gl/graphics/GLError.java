package com.graphics.engine.gl.graphics;

import android.opengl.GLES20;
import android.util.Log;

import com.graphics.engine.gl.util.MutableInteger;

import java.util.HashMap;

/**
 * GL错误检测与清除的工具类
 * @author dengweiming
 *
 */
public class GLError {
	public static final String TAG = "GLERROR";
	public static final boolean THROW_EXCEPTION_WHEN_GL_ERROR = false;	//出错时是否抛出异常，调试用
	public static final boolean LOG_WHEN_GL_ERROR = true;	//出错时是否打印日志，调试用
	
    private static final char[] HEX_DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 
    };
    
	private static final char[] CharBuffer = new char[16];	// CHECKSTYLE IGNORE
	
	private static HashMap<MutableInteger, String> sErrorStringMap;
	private static final MutableInteger KEY = new MutableInteger();

	/**
	 * 检查是否有错误
	 * @param op	出错时打印的消息，一般为操作名称
	 */
	public static boolean checkGLError(String op) {
		int error;
		boolean foundError = false;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, op + ": glError " + errorToString(error));
			if (LOG_WHEN_GL_ERROR) {
//				Log.w(TAG, op + ": glError " + errorToString(error));
			} else if (THROW_EXCEPTION_WHEN_GL_ERROR) {
				throw new RuntimeException(op + ": glError " + errorToString(error));
			}
			foundError = true;
		}
		return foundError;
	}
	
	/**
	 * 检测当时是否存在指定错误
	 * @param error	指定的错误代码，例如{@link GLES20#GL_INVALID_ENUM}等。
	 */
	public static boolean checkGLError(int error) {
		return GLES20.glGetError() == error;
	}
	
	/**
	 * 清除错误
	 */
	public static void clearGLError() {
		while (GLES20.glGetError() != GLES20.GL_NO_ERROR) {
		}
	}
	
	/**
	 * 将错误代码转换为名称或者十六进制字符串
	 * @param i 错误代码
	 */
    public static String errorToString(int i) {
		if (sErrorStringMap == null) {
			sErrorStringMap = new HashMap<MutableInteger, String>();
			/** @formatter:off */
			sErrorStringMap.put(new MutableInteger(GLES20.GL_INVALID_ENUM),			"GL_INVALID_ENUM");
			sErrorStringMap.put(new MutableInteger(GLES20.GL_INVALID_VALUE),		"GL_INVALID_VALUE");
			sErrorStringMap.put(new MutableInteger(GLES20.GL_INVALID_OPERATION),	"GL_INVALID_OPERATION");
			sErrorStringMap.put(new MutableInteger(GLES20.GL_OUT_OF_MEMORY),		"GL_OUT_OF_MEMORY");
			/** @formatter:on */
		}
		KEY.setValue(i);
		String string = sErrorStringMap.get(KEY);
		if (string != null) {
			return string;
		}
    	return toHexString(i, 4);	// CHECKSTYLE IGNORE
    }
    
    /**
     * 将错误代码转换为十六进制字符串
     * @param i 错误代码
     * @param digitLimit 十六进制的位数限制。取值为4，会适应OpenGL的常量定义。
     */
    public static String toHexString(int i, int digitLimit) {
    	int bufLen = 10;  // Max number of hex digits in an int	// CHECKSTYLE IGNORE
    	char[] buf = CharBuffer;
    	int cursor = bufLen;
    	
		for (int j = 0; j < digitLimit; ++j) {
			// CHECKSTYLE IGNORE 2 LINES
			buf[--cursor] = HEX_DIGITS[i & 0xF];	
			i >>>= 4;
		}
    	buf[--cursor] = 'x';
    	buf[--cursor] = '0';
    	
    	return new String(buf, cursor, bufLen - cursor);
    }
}
