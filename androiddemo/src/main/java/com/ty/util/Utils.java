package com.ty.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

/**
 * 加载图片
 * 
 * @author tangyong
 * 
 */
public  class Utils {
	
	public static final String[] names = new String[] { "杨毅伟", "李世宁", "孔令发", "雷景林", "曹世超", "周达威", "马三兵", "沈星", "区永伦", "曹石磊", "翁汉良", "贺鹏飞", "诸葛秀英", "李晶", "黄伟锋", "汤勇" };

	private static Matrix yFlipMatrix;

	static {
		yFlipMatrix = new Matrix();
		yFlipMatrix.postScale(1, -1); // flip Y axis
	}

	public static Bitmap getTextureFromBitmapResource(Context context,
			int resourceId) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeResource(context.getResources(),
					resourceId);
			return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), yFlipMatrix, false);
		} finally {
			if (bitmap != null) {
				bitmap.recycle();
			}
		}
	}
}
