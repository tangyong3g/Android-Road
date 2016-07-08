/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphics.engine.graphics;

import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.graphics.engine.util.MutableInteger;
import com.graphics.engine.util.NdkUtil;

/**
 * 
 * <br>类描述:位图纹理
 * <br>功能详细描述:
 * <ul> 常用方法：
 * 	<li>{@link #createSharedTexture(Bitmap)} 创建或获取共享纹理
 * 	<li>{@link #BitmapTexture(Bitmap)} 创建纹理实例（非共享）
 * </ul>
 */
public class BitmapTexture extends Texture {
	private Bitmap mOriginalBitmap;
	private boolean mShared;

	private static final MutableInteger KEY = new MutableInteger();
	private static final int MAP_INIT_CAPACITY = 128;
	private static final HashMap<MutableInteger, BitmapTexture> MAP = new HashMap<MutableInteger, BitmapTexture>(
			MAP_INIT_CAPACITY);

	/**
	 * <br>功能简述: 创建或获取共享纹理
	 * <br>功能详细描述: 如果指定位图已经有了对应的纹理，则下一次创建只会增加该纹理的引用计数
	 * <br>注意: 每张共享纹理在不需要使用的时候仍然要调用clear()方法来清理
	 * @param bitmap
	 * @return
	 */
	public static BitmapTexture createSharedTexture(Bitmap bitmap) {
//		return new BitmapTexture(bitmap);
		if (bitmap == null) {
			return null;
		}
		BitmapTexture texture = null;
		synchronized (MAP) {
			final int hashCode = bitmap.hashCode();
			KEY.setValue(hashCode);
			texture = MAP.get(KEY);
			if (texture != null && texture.isCleared()) {
				MAP.remove(KEY);
				texture = null;
			}
			if (texture == null) {
				texture = new BitmapTexture(bitmap);
				texture.mShared = true;
				MutableInteger key = new MutableInteger();
				key.setValue(hashCode);
				MAP.put(key, texture);

				DrawableInfo info = PIXEL_MAP.get(KEY);
				texture.setDrawableInfo(info);
			} else {
				texture.duplicate();
			}
		}
		return texture;
	}
	
	private static final HashMap<MutableInteger, DrawableInfo> PIXEL_MAP = new HashMap<MutableInteger, DrawableInfo>(
			MAP_INIT_CAPACITY);
	
	/**
	 * <br>功能简述: 将位图保存到Native内存中
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bitmap
	 * @param autoReleaseByTexture 如果为false，则使用者需要在清除资源时自行调用{@link #releaseBitmapNativeMemory(Bitmap)} 
	 * @return 是否保存成功，如果成功，一般需要调用者使用{@link Bitmap#recycle()}回收该位图。
	 */
	public static boolean saveBitmapToNativeMemory(Bitmap bitmap, boolean autoReleaseByTexture) {
		if (bitmap == null || bitmap.isRecycled()) {
			return false;
		}
		final int hashCode = bitmap.hashCode();
		synchronized (MAP) {
			KEY.setValue(hashCode);
			
			DrawableInfo info = PIXEL_MAP.get(KEY);
			if (info != null && info.bitmap != bitmap) {
				info.clear();
				info = null;
			}
			if (info == null) {
				info = new DrawableInfo(null, bitmap, autoReleaseByTexture);
			}
			if (info.pixels == 0) {
				info.pixels = NdkUtil.saveBitmap(bitmap);
				if (info.pixels == 0) {
					return false;
				}
			}
			MutableInteger key = new MutableInteger();
			key.setValue(hashCode);
			PIXEL_MAP.put(key, info);
			
			BitmapTexture texture = MAP.get(KEY);
			if (texture != null /*&& !texture.isCleared()*/) {
				texture.setDrawableInfo(info);
			}
		}
		return true;
	}
	
	/**
	 * <br>功能简述: 拿到保存在Native内存中位图的指定像素点值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bitmap
	 * @param x 	像素点位置
	 * @param y		像素点位置
	 * @return 是像素点的像素值
	 */
	public static int getPixelFromNative(Bitmap bitmap, int x, int y) {
		if (bitmap == null) {
			return 0;
		}
		
		final int hashCode = bitmap.hashCode();
		synchronized (MAP) {
			KEY.setValue(hashCode);
			
			DrawableInfo info = PIXEL_MAP.get(KEY);
			if (info != null && info.pixels != 0) {
				int offset = x + bitmap.getWidth() * y;
				return NdkUtil.getPixelInternal(info.pixels, offset);
			}
		}
		
		return 0;
	}
	
	/**
	 * <br>功能简述: 将位图保存在Native内存中的数据清除
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bitmap
	 */
	public static void releaseBitmapNativeMemory(Bitmap bitmap) {
		if (bitmap == null) {
			return;
		}
		final int hashCode = bitmap.hashCode();
		BitmapTexture texture = null;
		synchronized (MAP) {
			KEY.setValue(hashCode);
			
			DrawableInfo info = PIXEL_MAP.get(KEY);
			if (info != null) {
				info.clear();
				PIXEL_MAP.remove(KEY);
			}
			
			texture = MAP.get(KEY);
			if (texture != null) {
				MAP.remove(KEY);
			}
		}
		if (texture != null) {
			texture.clear();
		}
	}
	
	/**
	 * <br>功能简述: 将一张位图的像素从Native内存中取出来构造一张新的位图
	 * <br>功能详细描述:
	 * <br>注意: 使用完返回的位图要清除它
	 * @param bitmap
	 * @return null表示失败
	 */
	public static Bitmap restoreBitmapFromNativeMemory(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		final int hashCode = bitmap.hashCode();
		synchronized (MAP) {
			KEY.setValue(hashCode);
			
			DrawableInfo info = PIXEL_MAP.get(KEY);
			if (info != null && info.pixels != 0) {
				try {
					bitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
				} catch (OutOfMemoryError e) {
				}
				
				int restored = NdkUtil.restorePixels(bitmap, info.pixels);
				if (restored != 0) {
					return bitmap;
				} else {
					bitmap.recycle();
				}
			}
		}
		return null;
	}
	
	/** @hide */
	public static void onDestroyStatic() {
		synchronized (MAP) {
			MAP.clear();
		}
	}

	/**
	 * 使用位图创建一个对象
	 */
	public BitmapTexture(Bitmap bitmap) {
		mOriginalBitmap = bitmap;
		mWidth = bitmap.getWidth();
		mHeight = bitmap.getHeight();
	}

	@Override
	protected Bitmap onLoad() {
		return mOriginalBitmap;
	}

	/**
	 * 清除对封装的位图的引用
	 */
	public void resetBitmap() {
		if (mShared && mOriginalBitmap != null) {
			synchronized (MAP) {
				final int hashCode = mOriginalBitmap.hashCode();
				KEY.setValue(hashCode);
				if (MAP.get(KEY) == this) {
					MAP.remove(KEY);
				}
			}
		}
		mOriginalBitmap = null;
		mBitmap = null;
	}

	public Bitmap getBitmap() {
		return mOriginalBitmap;
	}

	@Override
	protected void recycleBitmap(Bitmap bitmap) {
		if (bitmap != mOriginalBitmap) {
			super.recycleBitmap(bitmap);
		}
	}

	@Override
	public void onClear() {
		super.onClear();
		resetBitmap();
	}
}