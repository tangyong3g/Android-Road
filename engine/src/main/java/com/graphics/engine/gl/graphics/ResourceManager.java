package com.graphics.engine.gl.graphics;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.go.gl.util.LongSparseArray;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * 
 * <br>类描述: 图片资源管理器
 * <br>功能详细描述:
 * 目前还没实际用上
 * 
 * @author  dengweiming
 * @date  [2013-6-5]
 */
public class ResourceManager {
	//CHECKSTYLE IGNORE 2 LINES
	private static final TypedValue sTmpTypeValue = new TypedValue();
	private static final Rect sPadRect = new Rect();
	private static Bitmap sTmpBitmap;

	private static LongSparseArray<DrawableInfo> sDrawableCache = new LongSparseArray<DrawableInfo>();
	private static LongSparseArray<DrawableInfo> sTmpDrawableCache;
	private static Set<DrawableInfo> sDrawableInfoSet = new HashSet<DrawableInfo>();
	private static Set<DrawableInfo> sTmpDrawableInfoSet;

	public static Drawable getDrawable(TypedArray a, int index) {
		synchronized (sTmpTypeValue) {
			TypedValue value = sTmpTypeValue;
			if (a.getValue(index, value)) {
				return loadDrawable(a.getResources(), value, value.resourceId);
			}
		}
		return null;
	}

	public static Drawable getDrawable(Resources res, int id) throws NotFoundException {
		synchronized (sTmpTypeValue) {
			TypedValue value = sTmpTypeValue;
			res.getValue(id, value, true);
			return loadDrawable(res, value, id);
		}
	}

	//CHECKSTYLE IGNORE 3 LINES
	@SuppressWarnings("rawtypes")
	static final Class[] sGetValueForDensityMethodParamTypes = new Class[] { int.class, int.class, TypedValue.class, boolean.class };
	static final Object[] sGetValueForDensityMethodParams = new Object[4];

	public static Drawable getDrawableForDensity(Resources res, int id, int density) throws NotFoundException {
		synchronized (sTmpTypeValue) {
			TypedValue value = sTmpTypeValue;
			Method method = null;
			try {
				method = Resources.class.getMethod("getValueForDensity", sGetValueForDensityMethodParamTypes);
			} catch (Exception e) {
				return null;
			}
			sGetValueForDensityMethodParams[0] = Integer.valueOf(id);
			sGetValueForDensityMethodParams[1] = Integer.valueOf(density);
			sGetValueForDensityMethodParams[2] = value;
			sGetValueForDensityMethodParams[3] = Boolean.valueOf(true);
			try {
				method.invoke(res, sGetValueForDensityMethodParams);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			DisplayMetrics mMetrics = res.getDisplayMetrics();
			/*
			 * Pretend the requested density is actually the display density. If
			 * the drawable returned is not the requested density, then force it
			 * to be scaled later by dividing its density by the ratio of
			 * requested density to actual device density. Drawables that have
			 * undefined density or no density don't need to be handled here.
			 */
			if (value.density > 0 && value.density != TypedValue.DENSITY_NONE) {
				if (value.density == density) {
					value.density = mMetrics.densityDpi;
				} else {
					value.density = (value.density * mMetrics.densityDpi) / density;
				}
			}

			return loadDrawable(res, value, id);
		}
	}

	private static long getResourceHashCode(Resources res) {
		return ((long) res.hashCode()) << 32;
	}

	private static Drawable loadDrawable(Resources res, TypedValue value, int id) {
		//		 if ((id >>> 24) == 0x1) {	//framework resources
		//             return res.getDrawable(id);
		//         }

		boolean isColorDrawable = false;
		if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
			isColorDrawable = true;
		}
		if (isColorDrawable) {
			return new ColorGLDrawable(value.data);
		}

		Drawable dr = null;
		DrawableInfo info = null;

		final long key = getResourceHashCode(res) | id;
		info = getCachedDrawableInfo(sDrawableCache, key);
		dr = createDrawable(info);
		if (dr != null) {
			return dr;
		}

		if (value.string == null) {
			throw new NotFoundException("Resource is not a Drawable (color or path): " + value);
		}

		String file = value.string.toString();

		if (file.endsWith(".xml")) {
			//XXX: 帧动画的情况，能否自行解析？
			return res.getDrawable(id);
		} else {
			try {
				AssetManager assets = res.getAssets();
				AssetFileDescriptor assetFd = assets.openNonAssetFd(value.assetCookie, file);
				InputStream is = assetFd.createInputStream();
				dr = createFromResourceStream(res, value, is, file, null);
				is.close();
				assetFd.close();
			} catch (Exception e) {
				sTmpBitmap = null;
				NotFoundException rnf = new NotFoundException("File " + file + " from drawable resource ID #0x" + Integer.toHexString(id));
				rnf.initCause(e);
				throw rnf;
			}
		}

		if (dr != null) {
			dr.setChangingConfigurations(value.changingConfigurations);
			info = new DrawableInfo(dr, sTmpBitmap, true);
			sDrawableCache.put(key, info);
			sDrawableInfoSet.add(info);
		}
		sTmpBitmap = null;

		return createDrawable(info);
	}

	private static Drawable createFromResourceStream(Resources res, TypedValue value, InputStream is, String srcName,
													 BitmapFactory.Options opts) {
		Rect pad = sPadRect;

		if (opts == null) {
			opts = new BitmapFactory.Options();
		}
		//TODO:
		//        opts.inScreenDensity = res != null
		//                ? res.getDisplayMetrics().noncompatDensityDpi : DisplayMetrics.DENSITY_DEVICE;
		Bitmap bm = BitmapFactory.decodeResourceStream(res, value, is, pad, opts);
		sTmpBitmap = bm;
		if (bm != null) {
			byte[] np = bm.getNinePatchChunk();
			if (np == null || !NinePatch.isNinePatchChunk(np)) {
				np = null;
				pad = null;
			}
			if (np != null) {
				return new NinePatchDrawable(res, bm, np, pad, srcName);
			} else {
				return new BitmapDrawable(res, bm);
			}
		}
		return null;
	}

	private static DrawableInfo getCachedDrawableInfo(LongSparseArray<DrawableInfo> drawableCache, long key) {
		DrawableInfo info = drawableCache.get(key);
		if (info != null) {
			if (!info.needReload()) {
				return info;
			}
			info.clear();
			drawableCache.remove(key);
		}
		return null;
	}

	static GLDrawable createDrawable(DrawableInfo info) {
		if (info == null || info.drawable == null) {
			return null;
		}

		GLDrawable drawable = GLDrawable.getDrawable(info.drawable);
		if (drawable != null) {
			Texture texture = drawable.getTexture();
			if (texture != null) {
				texture.setDrawableInfo(info);
			}
		}
		return drawable;
	}

	public static void onPreClearCache() {
		onPostClearCache();
		synchronized (sTmpTypeValue) {
			sTmpDrawableCache = sDrawableCache;
			sDrawableCache = new LongSparseArray<DrawableInfo>();
			sTmpDrawableInfoSet = sDrawableInfoSet;
			sDrawableInfoSet = new HashSet<DrawableInfo>();
		}
	}

	public static void onPostClearCache() {
		synchronized (sTmpTypeValue) {
			if (sTmpDrawableCache != null) {
				sTmpDrawableCache.clear();
				sTmpDrawableCache = null;
			}
			if (sTmpDrawableInfoSet != null) {
				Iterator<DrawableInfo> iterator = sTmpDrawableInfoSet.iterator();
				while (iterator.hasNext()) {
					DrawableInfo info = iterator.next();
					info.clear();
				}
				sTmpDrawableInfoSet.clear();
				sTmpDrawableInfoSet = null;
			}
		}
	}

	//TODO:清除？
	public static void onConfigurationChanged() {
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2013-6-5]
	 */
	static class WrappedResources {
		Resources res;	//CHECKSTYLE IGNORE

		WrappedResources(Resources res) {
			//			this.res = res;	//XXX:暂时不保存
		}

		void clear() {
			res = null;
		}

		Resources get() {
			return res;
		}

	}

}
