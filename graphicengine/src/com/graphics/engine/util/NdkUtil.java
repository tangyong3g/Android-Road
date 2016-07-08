package com.graphics.engine.util;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;


/**
 * 
 * <br>类描述: Native工具类
 * <br>功能详细描述:
 * 
 */
//CHECKSTYLE:OFF
public class NdkUtil {
	static {
		System.loadLibrary("glndkutil");
//		init();	// 目前阶段暂时屏蔽中断信号的捕获
	}
	
	/** 将所有资源的位图存放到NDK*/
	public static final boolean SAVE_RESOURCE_TO_NDK = false;
	/** 将图标的位图存放到NDK*/
	public static final boolean SAVE_ICON_TO_NDK = true;
	
	private static UncaughtExceptionHandler sUncaughtExceptionHandler;
	
	/**
	 * <br>功能简述: 设置Native代码崩溃时的处理者
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param handler
	 */
	public static void setOnNativeCrashedHandler(UncaughtExceptionHandler handler) {
		sUncaughtExceptionHandler = handler;
	}
	
	// 初始化中断信号的捕获函数
	private static native void init();
	
	/**
	 * @hide
	 */
	public static void onNativeCrashed(int signal) {
		Log.e("NdkUtil", "NdkUtil.onNativeCrashed(" + signal + ")");
		Exception ex = new RuntimeException("NdkUtil.onNativeCrashed(" + signal + ")");
		ex.printStackTrace();
		if (sUncaughtExceptionHandler != null) {
			sUncaughtExceptionHandler.uncaughtException(Thread.currentThread(), ex);
		}
	}

	//====================================================v
	//以下方法封装了那些没有提供java封装的OpenGL API，往后兼容至Android SDK API 8
	
	public static native void glVertexAttribPointer(int indx, int size, int type,
			boolean normalized, int stride, int offset);

	public static native void glDrawElements(int mode, int count, int type, int offset);
	
    public static native void glTexImage2D(
            int target,
            int level,
            int internalformat,
            int width,
            int height,
            int border,
            int format,
            int type,
            int pixels
        );
    
    public static native void glTexSubImage2D(
            int target,
            int level,
            int xoffset,
            int yoffset,
            int width,
            int height,
            int format,
            int type,
            int pixels
        );
    
    //====================================================^
    
	/**
	 * @hide
	 */
	public static native void saveScreenshotTGA(int x, int y, int w, int h, String fileName);
	
	/**
	 * @hide
	 */
	public static native void saveScreenshot(int x, int y, int w, int h, int[] buffer);
	
	/**
	 * @hide
	 */
	public static native void saveScreenshotBitmap(int x, int y, int w, int h, Bitmap bitmap);
	
	/**
	 * <br>功能简述: 将图片转化为HSV内部存储格式
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bitmap
	 * @param optimized 图片是否为HSV格式优化过，即满足色调为0，也即对于每个像素R>=G=B，这样优化过的图片只能设置统一的的色调值
	 * @return
	 */
	public static boolean convertToHSV(Bitmap bitmap, boolean optimized) {
		if (bitmap == null || bitmap.isRecycled() || bitmap.getConfig() != Config.ARGB_8888) {
			return false;
		}
		
		/*
		// java代码可能会比较慢，并且如果 bitmap.isMutable() 为 false 会导致 bitmap.setPixels 异常,需要复制一份
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		final int size = width * height;
		int[] pixels = new int[size * 4];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		if (!optimized) {
			final float[] hsv = new float[3];
			for (int index = 0; index < size; ++index) {
				final int color = pixels[index];
				final int alpha = Color.alpha(color);
				final int red = Color.red(color);
				final int green = Color.green(color);
				final int blue = Color.blue(color);
				Color.RGBToHSV(red, green, blue, hsv);
				final int r = (int) (hsv[0] * (255 * 255 / 360.0f) / alpha);	// h / a
				final int g = (int) (hsv[1] * hsv[2] * 255);					// s * v
				final int b = (int) ((1 - hsv[1]) * hsv[2] * 255);				// (1 - s) * v
				pixels[index] = Color.argb(alpha, r, g, b);
			}
		} else {
			for (int index = 0; index < size; ++index) {
				final int color = pixels[index];
				final int alpha = Color.alpha(color);
				final int red = Color.red(color);
				final int green = Color.green(color);
				final int blue = Color.blue(color);
				pixels[index] = Color.argb(alpha, red, red - green, blue);
			}
		}
		try {
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		} catch (Exception e) {
			return false;
		}
		return true;
		*/
		return convertToHSVInternal(bitmap, optimized);
		
	}
	
	private static native boolean convertToHSVInternal(Bitmap bitmap, boolean optimized);
	
	public static native void detectGLES20(Context context);	//盗版检测的代码，伪装了方法名
	
	public static int getLibVersion() {
		try {
			return getLibVersionInternal();
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static native int getLibVersionInternal();
	
	static int sBitmapSaved;
	static int sBytesSaved;
	static int sBitmapCleared;
	
	/**
	 * <br>功能简述: 将位图像素保存到NDK内存中
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bitmap
	 * @return 保存的NDK内存的指针
	 */
	public static int saveBitmap(Bitmap bitmap) {
		if (bitmap == null || bitmap.isRecycled()) {
			return 0;
		}
		if (bitmap.getConfig() != Config.ARGB_8888) {
			return 0;
		}
		++sBitmapSaved;
		sBytesSaved += bitmap.getWidth() * bitmap.getHeight() * 4;
		int kbSaved = sBytesSaved / 1024;
//		Log.d("DWM", "bitmapSaved=" + sBitmapSaved + " kBSaved=" + kbSaved + " cleared=" + sBitmapCleared);
		int pixelPtr = saveBitmapInternal(bitmap);
		Log.d("yyw", "saveBitmap :" + pixelPtr);
		return pixelPtr;
	}
	
	private static native int saveBitmapInternal(Bitmap bitmap);
	
	/**
	 * <br>功能简述: 释放NDK内存
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param pixels 内存指针
	 */
	public static void releaseBitmap(int pixels) {
		++sBitmapCleared;
//		Log.d("DWM", "releaseBitmap " + sBitmapCleared);
		Log.d("yyw", "releaseBitmap :" + pixels);
		releasePixelsInternal(pixels);
	}
	
	private static native void releasePixelsInternal(int pixels);
	
	/**
	 * <br>功能简述: 将NDK内存中的像素数据读进一张位图里
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bitmap 
	 * @param pixels
	 * @return
	 */
	public static int restorePixels(Bitmap bitmap, int pixels) {
		if (bitmap == null || bitmap.isRecycled() || pixels == 0) {
			return 0;
		}
		return restorePixelsInternal(bitmap, pixels);
	}
	
	private static native int restorePixelsInternal(Bitmap bitmap, int pixels);
	
	public static native int getPixelInternal(int pixelsPtr, int offset);
	
}