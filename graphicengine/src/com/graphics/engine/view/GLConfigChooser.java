package com.graphics.engine.view;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import android.opengl.GLSurfaceView;
import android.util.Log;

/**
 * 
 * <br>类描述: OpenGL配置的选择器
 * <br>功能详细描述:
 * 选择颜色缓冲区RGBA分量的位深，以及深度缓冲区和蒙板缓冲区的位深
 * 
 */
class GLConfigChooser implements GLSurfaceView.EGLConfigChooser {
	static final String TAG = "DWM";
	private static final boolean DBG = false;
	private final int[] mIntBuffer = new int[1];
	private int mAlpha;
	private int mDepth;
	private int mStencil;

	private int[] mChoosenConfig;
	private int[][] mConfigDetails;
	private boolean mGLES20Supported;

	/** @formatter:off */
	final static int INDEX_RED_SIZE = 0;		//EGL10.EGL_RED_SIZE
	final static int INDEX_GREEN_SIZE = 1;		//EGL10.EGL_GREEN_SIZE
	final static int INDEX_BLUE_SIZE = 2;		//EGL10.EGL_BLUE_SIZE
	final static int INDEX_ALPHA_SIZE = 3;		//EGL10.EGL_ALPHA_SIZE
	final static int INDEX_DEPTH_SIZE = 4;		//EGL10.EGL_DEPTH_SIZE
	final static int INDEX_STENCIL_SIZE = 5;	//EGL10.EGL_STENCIL_SIZE
	final static int INDEX_RENDERABLE_TYPE = 6;	//EGL10.EGL_RENDERABLE_TYPE
	final static int INDEX_SURFACE_TYPE = 7;	//EGL10.EGL_SURFACE_TYPE
	final static int INDEX_LAST = 8;

	final static int[] EGL_ATTRIBS = { 
		EGL10.EGL_RED_SIZE, 
		EGL10.EGL_GREEN_SIZE,
		EGL10.EGL_BLUE_SIZE, 
		EGL10.EGL_ALPHA_SIZE, 
		EGL10.EGL_DEPTH_SIZE,
		EGL10.EGL_STENCIL_SIZE, 
		EGL10.EGL_RENDERABLE_TYPE, 
		EGL10.EGL_SURFACE_TYPE, 
		};

	final static String[] ATTRIB_STRINGS = { 
		"r", 
		"g", 
		"b", 
		"a", 
		"d", 
		"s",
		"rType", 
		"sType", 
		};
	/** @formatter:on */

	private static final int EGL_OPENGL_ES2_BIT = 4;

	public GLConfigChooser() {
	}

	public GLConfigChooser(int alpha, int depth, int stencil) {
		mAlpha = alpha;
		mDepth = depth;
		mStencil = stencil;
	}

	public void setConfigure(int alpha, int depth, int stencil) {
		mAlpha = alpha;
		mDepth = depth;
		mStencil = stencil;
	}

	private boolean containFlag(int value, int flag) {
		return (value & flag) == flag;
	}

	/**
	 * <br>功能简述: 获取config的详细属性
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param config
	 * @param egl
	 * @param display
	 * @param value
	 * @param resValus 长度至少为{@link #INDEX_LAST}
	 */
	static void getConfigDetail(EGLConfig config, EGL10 egl, EGLDisplay display, int[] value,
			int[] configDetail) {
		for (int i = 0; i < INDEX_LAST; ++i) {
			egl.eglGetConfigAttrib(display, config, EGL_ATTRIBS[i], value);
			configDetail[i] = value[0];
		}
	}

	static String configToString(int[] configDetail) {
		String string = "";
		for (int i = 0; i < INDEX_LAST; ++i) {
			string = string + ATTRIB_STRINGS[i] + "=" + configDetail[i] + " ";
		}
		string += "\n";
		return string;
	}

	@Override
	public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
		if (DBG) {
			Log.d(TAG, "GLConfigChooser chooseConfig egl=" + egl + " display=" + display);
		}
		
		mGLES20Supported = false;
		
		// Querying number of configurations
		final int[] numConf = mIntBuffer;
		egl.eglGetConfigs(display, null, 0, numConf);	// if configuration array
														// is null it still
														// returns the number of
														// configurations
		final int configCount = numConf[0];

		// Querying actual configurations
		EGLConfig[] config = new EGLConfig[configCount];
		egl.eglGetConfigs(display, config, configCount, numConf);

		final int alpha = mAlpha;
		final int depth = mDepth;
		final int stencil = mStencil;

		final int[][] configDetails = mConfigDetails = new int[configCount][];
		int[] result = null;

		for (int i = 0; i < configCount; ++i) {
			configDetails[i] = new int[INDEX_LAST + 1];
			getConfigDetail(config[i], egl, display, mIntBuffer, configDetails[i]);
			if (DBG) {
				Log.v(TAG, "config " + i + ":\t" + configToString(configDetails[i]));
			}
			configDetails[i][INDEX_LAST] = i;
			mGLES20Supported |= containFlag(configDetails[i][INDEX_RENDERABLE_TYPE], EGL_OPENGL_ES2_BIT);
		}

		//尝试alpha, depth, stencil
		if (DBG) {
			Log.v(TAG, "GLConfigChooser: try alpha depth stencil");
		}
		for (int i = 0; i < configCount; i++) {
			if (filterConfig(configDetails[i], alpha, depth, stencil)) {
				result = better(result, configDetails[i]);
			}
		}

		//没有stencil的时候，尝试alpha, depth
		if (result == null) {
			if (DBG) {
				Log.v(TAG, "GLConfigChooser: try alpha depth");
			}
			for (int i = 0; i < configCount; i++) {
				if (filterConfig(configDetails[i], alpha, depth)) {
					result = better(result, configDetails[i]);
				}
			}
		}

		//没有alpha, stencil的时候，尝试depth
		if (result == null) {
			if (DBG) {
				Log.v(TAG, "GLConfigChooser: try depth");
			}
			for (int i = 0; i < configCount; i++) {
				if (filterConfig(configDetails[i], depth)) {
					result = better(result, configDetails[i]);
				}
			}
		}

		if (result == null) {
			String info = "EGLConfig: No EGL config chosen. alpha=" + alpha + " depth=" + depth
					+ " stencil=" + stencil + " numConfig=" + configCount + "\n";
			StringBuilder builder = new StringBuilder(info);
			for (int i = 0; i < configCount; ++i) {
				builder.append(configToString(configDetails[i]));
			}
			throw new IllegalArgumentException(builder.toString());
		}
		if (DBG) {
			Log.v(TAG, "====Choose this EGLConfig: " + configToString(result));
		}
		final int index = result[INDEX_LAST];
		mChoosenConfig = configDetails[index];
		return config[index];
	}

	private boolean filterConfig(int[] configDetail, int alpha, int depth, int stencil) {
		if (configDetail[INDEX_STENCIL_SIZE] < stencil) {
			return false;
		}

		return filterConfig(configDetail, alpha, depth);
	}

	private boolean filterConfig(int[] configDetail, int alpha, int depth) {
		if (configDetail[INDEX_ALPHA_SIZE] != alpha) {
			return false;
		}

		if (alpha == 0) { // check if RGB_565 format
			if (configDetail[INDEX_RED_SIZE] != 5) {	// CHECKSTYLE IGNORE
				return false;
			}
			if (configDetail[INDEX_GREEN_SIZE] != 6) {	// CHECKSTYLE IGNORE
				return false;
			}
			if (configDetail[INDEX_BLUE_SIZE] != 5) {	// CHECKSTYLE IGNORE
				return false;
			}
		}

		return filterConfig(configDetail, depth);
	}

	private boolean filterConfig(int[] configDetail, int depth) {
		if (configDetail[INDEX_DEPTH_SIZE] < depth) {
			return false;
		}

		return filterConfig(configDetail);
	}

	private boolean filterConfig(int[] configDetail) {
		if (mGLES20Supported && !containFlag(configDetail[INDEX_RENDERABLE_TYPE], EGL_OPENGL_ES2_BIT)) {
			return false;
		}

		//EGL_WINDOW_BIT | EGL_PBUFFER_BIT | EGL_PIXMAP_BIT
		if (!containFlag(configDetail[INDEX_SURFACE_TYPE], EGL10.EGL_WINDOW_BIT)) {
			return false;
		}

		return true;
	}

	/**
	 * Returns the best of the two EGLConfig passed according to depth and
	 * colours
	 * 
	 * @param a
	 *            The first candidate
	 * @param b
	 *            The second candidate
	 * @return The chosen candidate
	 */
	private int[] better(int[] a, int[] b) {
		if (a == null) {
			return b;
		}

		int[] result = null;

		int depthA = a[INDEX_DEPTH_SIZE];

		int depthB = b[INDEX_DEPTH_SIZE];

		if (depthA > depthB) {
			result = a;
		} else if (depthA < depthB) {
			result = b;
		} else // if depthA == depthB
		{
			int redA = a[INDEX_RED_SIZE];

			int redB = b[INDEX_RED_SIZE];

			if (redA > redB) {
				result = a;
			} else if (redA < redB) {
				result = b;
			} else // if redA == redB
			{
				// Don't care
				result = a;
			}
		}

		return result;
	}

	public int getColorBits() {
		return mChoosenConfig == null ? 0 : mChoosenConfig[INDEX_RED_SIZE]
				+ mChoosenConfig[INDEX_GREEN_SIZE] + mChoosenConfig[INDEX_BLUE_SIZE]
				+ mChoosenConfig[INDEX_ALPHA_SIZE];
	}

	public int getDepthBits() {
		return mChoosenConfig == null ? 0 : mChoosenConfig[INDEX_DEPTH_SIZE];
	}

	public int getStencilBits() {
		return mChoosenConfig == null ? 0 : mChoosenConfig[INDEX_STENCIL_SIZE];
	}
	
	public boolean isGLES20Supported() {
		return mGLES20Supported;
	}

}
