package com.graphics.engine.graphics;

import android.opengl.GLES20;
import android.util.Log;

/**
 * 
 * <br>类描述:生成GL命令的工程类
 * <br>功能详细描述:
 * 使用{@link #get(int)}方法来获得命令的封装对象
 * 
 * @author  dengweiming
 * @date  [2012-9-17]
 */
final class GLCommandFactory {
	static final boolean DBG = false;
//	static boolean DBG = false;
	static final String TAG = "DWM";
	
	/** @formatter:off */
	private static final int DEFAULT					= 0;
	public static final int DEPTH_MASK_TRUE				= 1;
	public static final int DEPTH_MASK_FALSE			= 2;
	public static final int DEPTH_TEST_ENABLE			= 3;
	public static final int DEPTH_TEST_DISABLE			= 4;
	public static final int CULL_FACE_ENABLE			= 5;
	public static final int CULL_FACE_DISABLE			= 6;
	public static final int SCISSOR_TEST_DISABLE		= 7;
	public static final int CLEAR_COLOR_BUFFER			= 8;
	public static final int CLEAR_DEPTH_BUFFER			= 10;
	public static final int CLEAR_COLOR_DEPTH_BUFFER	= 11;
	public static final int CLEAR_COLOR					= 12;
	public static final int VIEWPORT					= 13;
	public static final int BLEND_ENABLE				= 14;
	public static final int BLEND_DISABLE				= 15;
	public static final int COLOR_MASK_TRUE				= 16;
	public static final int COLOR_MASK_FALSE			= 17;
	public static final int STENCIL_TEST_ENABLE			= 18;
	public static final int STENCIL_TEST_DISABLE		= 19;
	public static final int BLEND_FUNC					= 20;
	public static final int BLEND_FUNC_SEPERATE			= 21;
	public static final int CULL_BACK_FACE				= 22;
	public static final int CULL_FRONT_FACE				= 23;
	public static final int DEPTH_BUFFER_DIRTY			= 24;
	public static final int SCISSOR_TEST_ENABLE			= 25;
	public static final int FINISH						= 26;

	private static final int COUNT						= 27;
	/** @formatter:on */
	
	static boolean sDepthBufferDirty;
	static boolean sFrameBufferDepthBufferDirty;

	private static final Renderable[] sRunnables = new Renderable[COUNT];	//CHECKSTYLE IGNORE

	/**
	 * <br>功能简述: 获取一个GL命令的封装对象
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param command 命令编号，{@link #DEPTH_MASK_TRUE}, {@link #DEPTH_MASK_FALSE} 等等
	 * @return
	 */
	public static Renderable get(int command) {
		if (command < DEFAULT || command >= COUNT) {
			command = DEFAULT;
		}
		if (sRunnables[command] != null) {
			return sRunnables[command];
		}
		switch (command) {
			case DEPTH_MASK_TRUE :
				return sRunnables[command] = new Renderable() {
					@Override
					public void run(long timeStamp, RenderContext context) {
						GLES20.glDepthMask(true);
						if (DBG) {
							Log.d(TAG, "glDepthMask true");
						}
					}
				};
			case DEPTH_MASK_FALSE :
				return sRunnables[command] = new Renderable() {
					@Override
					public void run(long timeStamp, RenderContext context) {
						GLES20.glDepthMask(false);
						if (DBG) {
							Log.d(TAG, "glDepthMask false");
						}
					}
				};
			case DEPTH_TEST_ENABLE :
				return sRunnables[command] = new GLCapRenderable(GLES20.GL_DEPTH_TEST, true, "DEPTH_TEST_ENABLE");
			case DEPTH_TEST_DISABLE :
				return sRunnables[command] = new GLCapRenderable(GLES20.GL_DEPTH_TEST, false, "DEPTH_TEST_DISABLE");
			case CULL_FACE_ENABLE :
				return sRunnables[command] = new GLCapRenderable(GLES20.GL_CULL_FACE, true, "CULL_FACE_ENABLE");
			case CULL_FACE_DISABLE :
				return sRunnables[command] = new GLCapRenderable(GLES20.GL_CULL_FACE, false, "CULL_FACE_DISABLE");
			case SCISSOR_TEST_DISABLE :
				return sRunnables[command] = new GLCapRenderable(GLES20.GL_SCISSOR_TEST, false, "SCISSOR_TEST_DISABLE");
			case CLEAR_COLOR_BUFFER :
				return sRunnables[command] = new GLClearRenderable(GLES20.GL_COLOR_BUFFER_BIT);
			case CLEAR_DEPTH_BUFFER :
				return sRunnables[command] = new GLClearRenderable(GLES20.GL_DEPTH_BUFFER_BIT);
			case CLEAR_COLOR_DEPTH_BUFFER :
				return sRunnables[command] = new GLClearRenderable(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
			case CLEAR_COLOR:
				return sRunnables[command] = new Renderable() {
					@Override
					public void run(long timeStamp, RenderContext context) {
						final float[] args = context.color;
						GLState.glClearColor(args[0], args[1], args[2], args[3]);	//CHECKSTYLE IGNORE
					}
				};
			case VIEWPORT:
				return sRunnables[command] = new Renderable() {
					@Override
					public void run(long timeStamp, RenderContext context) {
						final float[] args = context.color;
						GLES20.glViewport((int) args[0], (int) args[1], (int) args[2], (int) args[3]);	//CHECKSTYLE IGNORE
						if (DBG) {
							Log.d(TAG, "glViewport " + (int) args[0] + " " + (int) args[1] + " " 
								+ (int) args[2] + " " + (int) args[3]);
						}
					}
				};
			case BLEND_ENABLE:
				return sRunnables[command] = new GLCapRenderable(GLES20.GL_BLEND, true, "BLEND_ENABLE");
			case BLEND_DISABLE:
				return sRunnables[command] = new GLCapRenderable(GLES20.GL_BLEND, false, "BLEND_DISABLE");
			case COLOR_MASK_TRUE:
				return sRunnables[command] = new Renderable() {
					@Override
					public void run(long timeStamp, RenderContext context) {
						GLES20.glColorMask(true, true, true, true);
					}
				};
			case COLOR_MASK_FALSE:
				return sRunnables[command] = new Renderable() {
					@Override
					public void run(long timeStamp, RenderContext context) {
						GLES20.glColorMask(false, false, false, false);
					}
				};
			case STENCIL_TEST_ENABLE :
				return sRunnables[command] = new GLCapRenderable(GLES20.GL_STENCIL_TEST, true, "STENCIL_TEST_ENABLE");
			case STENCIL_TEST_DISABLE :
				return sRunnables[command] = new GLCapRenderable(GLES20.GL_STENCIL_TEST, false, "STENCIL_TEST_DISABLE");
			case BLEND_FUNC :
				return sRunnables[command] = new Renderable() {
					@Override
					public void run(long timeStamp, RenderContext context) {
						final float[] args = context.color; 
						GLES20.glBlendFunc((int) args[0], (int) args[1]);	//CHECKSTYLE IGNORE
					}
				};
			case BLEND_FUNC_SEPERATE :
				return sRunnables[command] = new Renderable() {
					@Override
					public void run(long timeStamp, RenderContext context) {
						final float[] args = context.color; 
						GLES20.glBlendFuncSeparate((int) args[0], (int) args[1], (int) args[2], (int) args[3]);	//CHECKSTYLE IGNORE
					}
				};
			case CULL_BACK_FACE:
				return sRunnables[command] = new Renderable() {
					@Override
					public void run(long timeStamp, RenderContext context) {
						GLES20.glCullFace(GLES20.GL_BACK);
					}
				};
			case CULL_FRONT_FACE:
				return sRunnables[command] = new Renderable() {
					@Override
					public void run(long timeStamp, RenderContext context) {
						GLES20.glCullFace(GLES20.GL_FRONT);
					}
				};
			case DEPTH_BUFFER_DIRTY:
				return sRunnables[command] = new Renderable() {
					@Override
					public void run(long timeStamp, RenderContext context) {
						if (DBG) {
							Log.d(TAG, "DEPTH_BUFFER_DIRTY " + GLFramebuffer.getCurrentFrameBufferId());
						}
						if (GLFramebuffer.getCurrentFrameBufferId() == 0) {
							sDepthBufferDirty = true;
						}
					}
				};
			case SCISSOR_TEST_ENABLE :
				return sRunnables[command] = new GLCapRenderable(GLES20.GL_SCISSOR_TEST, true, "SCISSOR_TEST_ENABLE");
			case FINISH:
				return sRunnables[command] = new Renderable() {
					@Override
					public void run(long timeStamp, RenderContext context) {
						GLES20.glFinish();
					}
				};
			default :
				return sRunnables[command] = Renderable.sInstance;
		}
	}

}

/**
 * 
 * <br>类描述: GL功能的启用/禁用切换信息的Renderable
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2012-9-7]
 */
class GLCapRenderable implements Renderable {
	boolean mEnabled;
	int mCap;
	String mName;

	GLCapRenderable(int cap, boolean enabled, String name) {
		mEnabled = enabled;
		mCap = cap;
		mName = name;
	}

	@Override
	public void run(long timeStamp, RenderContext context) {
		if (GLCommandFactory.DBG) {
			Log.d(GLCommandFactory.TAG, "GLCapRenderable " + this);
		}
		if (mEnabled) {
			GLES20.glEnable(mCap);
		} else {
			GLES20.glDisable(mCap);
		}

	}
	
	@Override
	public String toString() {
		return mName != null ? mName : super.toString();
	}

}

/**
 * 
 * <br>类描述: 清除缓冲区的Renderable
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2012-11-7]
 */
class GLClearRenderable implements Renderable {
	int mBits;
	
	GLClearRenderable(int bits) {
		mBits = bits;
	}

	@Override
	public void run(long timeStamp, RenderContext context) {
		int bits = mBits;
		
		if (GLFramebuffer.getCurrentFrameBufferId() == 0) {
			//在没有使用frame buffer的时候，优化多余的深度缓冲区清除操作
			if (!GLCommandFactory.sDepthBufferDirty) {
				bits &= ~GLES20.GL_DEPTH_BUFFER_BIT;
			}
			if ((bits & GLES20.GL_DEPTH_BUFFER_BIT) != 0) {
				GLCommandFactory.sDepthBufferDirty = false;
			}
		} else {
			//在使用frame buffer的时候，优化多余的深度缓冲区清除操作，但有buffer切换就不作优化
			if (!GLCommandFactory.sFrameBufferDepthBufferDirty) {
				bits &= ~GLES20.GL_DEPTH_BUFFER_BIT;
			}
			if ((bits & GLES20.GL_DEPTH_BUFFER_BIT) != 0) {
				GLCommandFactory.sFrameBufferDepthBufferDirty = false;
			}
		}
		
		if (GLCommandFactory.DBG) {
			Log.d(GLCommandFactory.TAG, "GLClearRenderable " + Integer.toHexString(mBits) + " -> " 
				+ Integer.toHexString(bits) + " fbo=" + GLFramebuffer.getCurrentFrameBufferId());
		}
		
		if (bits != 0) {
			GLES20.glClear(bits);
		}
	}
	
}