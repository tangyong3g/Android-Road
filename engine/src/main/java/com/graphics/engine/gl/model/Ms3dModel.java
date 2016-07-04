package com.graphics.engine.gl.model;


import com.graphics.engine.gl.animator.FloatValueAnimator;
import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.graphics.GLClearable;
import com.graphics.engine.gl.graphics.GLDrawable;
import com.graphics.engine.gl.graphics.RenderContext;
import com.graphics.engine.gl.graphics.Renderable;
import com.graphics.engine.gl.graphics.Texture;
import com.graphics.engine.gl.graphics.TextureListener;
import com.graphics.engine.gl.graphics.TextureManager;
import com.graphics.engine.gl.graphics.TextureRecycler;
import com.graphics.engine.gl.graphics.TextureShader;
import com.graphics.engine.gl.graphics.VertexBufferBlock;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

/**
 * 
 * <br>类描述: Ms3d模型与动画类
 * <br>功能详细描述:
 * 移植了以前Go锁屏虫子主题的NDK代码
 * 
 * @author  dengweiming
 * @date  [2013-11-25]
 */
public class Ms3dModel implements TextureListener, GLClearable {
	static {
		System.loadLibrary("ms3d");
	}

	private final static String TAG = "DWM";
	private final static boolean DBG = false;

	private String mModelName;
	private String mAnimationName;
	private int mModelPointer;
	private int mGroupCount;
	private Texture[] mTextures;
	private int[] mTextureIndices;	//mTextures[ mTextureIndex[i] ] 为第i个group的纹理
	
	FloatValueAnimator mAnimator;
	private int mAnimationPointer;

	/**
	 * 默认构造函数，从assets目录下夹在ms3d格式的文件
	 * @param context
	 * @param ms3dFileName 如果不是带".mp3"后缀的，那么需要在build.xml中配置不压缩
	 * @param psaFileName
	 */
	public Ms3dModel(Context context, String ms3dFileName) {
		if (DBG) {
			Log.d(TAG, "load ms3d model: " + ms3dFileName);
		}

		mModelName = "MS3DModel(" + ms3dFileName + ")";
		Resources res = context.getResources();
		String packageName = context.getPackageName();
		OpenAssetFileResult ms3dRes = AssetsUtil.openAssetFile(res.getAssets(), ms3dFileName);
		if (ms3dRes != null && ms3dRes.len > 0) {
			mModelPointer = loadModel(ms3dRes.descriptor, ms3dRes.offset, ms3dRes.len);
		}
		if (mModelPointer == 0) {
			Log.w(TAG, "Failed to load model " + ms3dFileName + ", cause: " + getErrorMessage());
		}
		mGroupCount = getGroupCount(mModelPointer);
		
		mTextureIndices = new int[mGroupCount];
		for (int i = 0; i < mGroupCount; ++i) {
			mTextureIndices[i] = getGroupTextureIndex(mModelPointer, i);
		}

		String textureNames = getTextureNames(mModelPointer);
		String[] textureNameArrays = textureNames.split("\n");
		int textureCount = textureNameArrays.length;
		mTextures = new Texture[textureCount];
		for (int i = 0; i < textureCount; ++i) {
			String textureName = textureNameArrays[i];
			if (DBG) {
				Log.v(TAG, "load texture: i=" + i + " name=" + textureName);
			}
			int index = textureName.indexOf('.');
			if (index >= 0) {
				textureName = textureName.substring(0, index);
			}
			int id = res.getIdentifier(textureName, "drawable", packageName);
			if (id != 0) {
				GLDrawable drawable = GLDrawable.getDrawable(res, id);
				if (drawable != null) {
					mTextures[i] = drawable.getTexture();
					mTextures[i].duplicate();
					drawable.clear();
				}
			}
		}

		TextureManager.getInstance().registerTextureListener(this);

	}
	
	/**
	 * <br>功能简述: 从assets目录加载psa格式的动画文件
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param psaFileName 如果不是带".mp3"后缀的，那么需要在build.xml中配置不压缩
	 */
	public void loadAnimation(Context context, String psaFileName) {
		if (DBG) {
			Log.d(TAG, "load ms3d animation: " + mModelName);
		}
		mAnimationName = psaFileName;
		if (mModelPointer != 0) {
			int pointer = 0;
			OpenAssetFileResult psaRes = AssetsUtil.openAssetFile(context.getResources().getAssets(), psaFileName);
			if (psaRes != null && psaRes.len > 0) {
				pointer = loadAnimation(mModelPointer, psaRes.descriptor, psaRes.offset, psaRes.len);
			}
			if (mAnimationPointer != 0) {
				releaseAnimation(mAnimationPointer);
			}
			mAnimationPointer = pointer;
		}
	}
	
	/**
	 * <br>功能简述: 修正动画首尾两帧之间的平移量（由于制作动画时数据设置不精确，这里适当修正）
	 * @param xyzMask 如果设置了1,则dx有效，如果设置了2,则dy有效，如果设置了4,则dz有效
	 */
	public void fixAnimationTranslation(int animId, int xyzMask, float dx, float dy, float dz) {
		if (mModelPointer != 0) {
			fixAnimationTranslation(mModelPointer, animId, xyzMask, dx, dy, dz);
		}
	}
	
	/**
	 * <br>功能简述: 修正动画首尾两帧之间的旋转量（由于制作动画时数据设置不精确，这里适当修正）
	 * @param x (x, y, z, w) 为首尾两帧之间的旋转量的四元数{@link com.go.gl.math3d.Quaternion}，(0, 0, 0, 1) 表示没有旋转
	 */
	public void fixAnimationRotation(int animId, float x, float y, float z, float w) {
		if (mModelPointer != 0) {
			fixAnimationRotation(mModelPointer, animId, x, y, z, w);
		}
	}
	
	/**
	 * <br>功能简述: 添加动画更新的监听者
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param listener
	 */
	public void addUpdateListener(AnimatorUpdateListener listener) {
		if (mAnimator == null) {
			mAnimator = new FloatValueAnimator();
			mAnimator.setValues(0, 1);
			mAnimator.addListener(new MyAnimtorListener());
			mAnimator.addUpdateListener(new MyUpdateListener());
		}
		if (listener != null) {
			mAnimator.addUpdateListener(listener);
		}
	}
	
	/**
	 * <br>功能简述: 移除动画更新的监听者
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param listener
	 */
	public void removeUpdateListener(AnimatorUpdateListener listener) {
		if (mAnimator != null && listener != null) {
			mAnimator.removeUpdateListener(listener);
		}
	}
	
	/**
	 * <br>功能简述: 添加动画事件的监听者
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param listener
	 */
	public void addListener(AnimatorListener listener) {
		addUpdateListener(null);
		if (listener != null) {
			mAnimator.addListener(listener);
		}
	}

	/**
	 * <br>功能简述: 移除动画事件的监听者
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param listener
	 */
	public void removeListener(AnimatorListener listener) {
		if (mAnimator != null && listener != null) {
			mAnimator.removeListener(listener);
		}
	}

	/**
	 * <br>功能简述: 播放动画
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param animId 动画序号
	 * @param accumulate 是否累积从第一帧到最后一帧的变换
	 * @param speed 动画播放速度倍数，1表示正常速度
	 * @param repeatCount 重复次数，-1表示无限
	 * @return
	 */
	public boolean playAnimation(int animId, boolean accumulate, float speed, int repeatCount) {
		if (mAnimationPointer == 0) {
			return false;
		}
		int duration = playAnimation(mAnimationPointer, animId, accumulate, speed);
		if (mAnimator == null) {
			addUpdateListener(null);	//可以用来创建mAnimator的实例
		}
		mAnimator.setDuration(duration);
		mAnimator.setRepeatCount(repeatCount);
		mAnimator.start();
		return true;
	}

	/**
	 * <br>功能简述: 绘制模型
	 * <br>功能详细描述:
	 * <br>注意: 由外部控制深度缓冲区的使用
	 * @param canvas
	 */
	public void draw(GLCanvas canvas) {
		if (mModelPointer == 0) {
			return;
		}

		TextureShader shader = null;
		final int fadeAlpha = canvas.getAlpha();
		float alpha = fadeAlpha * GLCanvas.INV_ALPHA;
		shader = TextureShader.getShader(alpha >= 1 ? TextureShader.MODE_NONE : TextureShader.MODE_ALPHA);
		if (shader == null) {
			return;
		}

		float[] mvpMatrix = canvas.getFinalMatrix();
		for (int i = 0; i < mGroupCount; ++i) {
			VertexBufferBlock.pushVertexData(mRenderable);
			RenderContext context = RenderContext.acquire();
			context.texture = mTextures[i];
			context.shader = shader;
			context.alpha = alpha;
			context.color[0] = i;
			System.arraycopy(mvpMatrix, 0, context.matrix, 0, context.matrix.length);
			canvas.addRenderable(mRenderable, context);
		}
	}

	/**
	 * <br>功能简述: 获取网格组数
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public int getGroupCount() {
		return mGroupCount;
	}

	@Override
	public void onTextureInvalidate() {
		if (mTextures != null) {
			for (int i = 0; i < mTextures.length; ++i) {
				if (mTextures[i] != null) {
					mTextures[i].onTextureInvalidate();
				}
			}
		}

	}

	@Override
	public void clear() {
		TextureRecycler.recycleTextureDeferred(this);

		if (mTextures != null) {
			for (int i = 0; i < mTextures.length; ++i) {
				if (mTextures[i] != null) {
					mTextures[i].clear();
					mTextures[i] = null;
				}
			}
		}
		
		if (mAnimator != null) {
			mAnimator.cleanup();
			mAnimator = null;
		}
	}

	@Override
	public void onClear() {
		if (mModelPointer != 0) {
			releaseModel(mModelPointer);
			mModelPointer = 0;
		}
		if (mAnimationPointer != 0) {
			releaseAnimation(mAnimationPointer);
			mAnimationPointer = 0;
		}

		TextureManager.getInstance().unRegisterTextureListener(this);
	}

	@Override
	public void yield() {
		if (mTextures != null) {
			for (int i = 0; i < mTextures.length; ++i) {
				if (mTextures[i] != null) {
					mTextures[i].yield();
				}
			}
		}

	}

	@Override
	public void onYield() {

	}
	
	@Override
	public String toString() {
		return mModelName;
	}

	
	private native static int loadModel(int descriptor, long offset, long len);
	
	private native static int loadAnimation(int modelPointer, int descriptor, long offset, long len);

	private native static String getErrorMessage();

	private native static void releaseModel(int modelPointer);
	
	private native static void releaseAnimation(int animPointer);

	private native static int getGroupCount(int modelPointer);

	private native static String getTextureNames(int modelPointer);

	private native static int getGroupTextureIndex(int modelPointer, int group);

	private native static void renderGroup(int modelPointer, int group, int positionHandle, int texcoordHandle);
	
	/**
	 * @return duration, in milliseconds
	 */
	private native static int playAnimation(int animPointer, int animId, boolean accumulate, float speed);
	
	private native static void onAnimationRepeat(int animPointer);
	
	private native static void onAnimationEnd(int animPointer);
	
	private native static void onAnimationUpdate(int animPointer, float normalizedTime);
	
	private native static void fixAnimationTranslation(
			int modelPointer, int animId, int xyzMask, float dx, float dy, float dz);
	
	private native static void fixAnimationRotation(
			int modelPointer, int animId, float x, float y, float z, float w);

	Renderable mRenderable = new Renderable() {

		@Override
		public void run(long timeStamp, RenderContext context) {
			VertexBufferBlock.popVertexData(this);

			if (context.texture == null || !context.texture.bind()) {
				return;
			}
			if (!(context.shader instanceof TextureShader) || !context.shader.bind()) {
				return;
			}
			TextureShader shader = (TextureShader) context.shader;
			shader.setAlpha(context.alpha);
			shader.setMatrix(context.matrix, 0);
			int group = (int) context.color[0];
			renderGroup(mModelPointer, group, shader.getPositionHandle(), shader.getTexcoordHandle());
		}
	};
	
	//CHECKSTYLE IGNORE 1 LINES
	class MyAnimtorListener implements AnimatorListener {

		@Override
		public void onAnimationStart(Animator animation) {
			
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			if (mAnimationPointer != 0) {
				Ms3dModel.onAnimationEnd(mAnimationPointer);
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			if (mAnimationPointer != 0) {
				Ms3dModel.onAnimationRepeat(mAnimationPointer);
			}
		}
		
	}
	
	//CHECKSTYLE IGNORE 1 LINES
	class MyUpdateListener implements AnimatorUpdateListener {

		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			if (mAnimationPointer != 0) {
				float normalizedTime = animation.getAnimatedFraction();
				Ms3dModel.onAnimationUpdate(mAnimationPointer, normalizedTime);
			}
			
		}
		
	}


}
