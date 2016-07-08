package com.graphics.engine.graphics;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.graphics.engine.graphics.ResourceManager.WrappedResources;
import com.graphics.engine.util.NdkUtil;

/**
 * 
 * <br>类描述: 为实现将图片像素数据保存到Native内存，所需要的信息记录
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-6-5]
 */
public class DrawableInfo {

	//CHECKSTYLE IGNORE 8 LINES
	Drawable drawable;
	Bitmap bitmap;
	WrappedResources res;
	int resId;
	int pixels;
	int width;
	int height;
	boolean autoReleaseByTexture;

	DrawableInfo(Drawable drawable, Bitmap bitmap, boolean autoReleaseByTexture) {
		this.drawable = drawable;
		this.bitmap = bitmap;
		this.autoReleaseByTexture = autoReleaseByTexture;
		width = bitmap.getWidth();
		height = bitmap.getHeight();
		pixels = NdkUtil.saveBitmap(bitmap);
//		bitmap.recycle();
	}

	void clear() {
		drawable = null;
		if (bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}
		res = null;
		clearPixels();
	}

	void clearPixels() {
		if (pixels != 0) {
			NdkUtil.releaseBitmap(pixels);
			pixels = 0;
		}
	}

	boolean needReload() {
		return pixels == 0 && (bitmap == null || bitmap.isRecycled());
	}
}
