package com.graphics.engine.gl.graphics.ext;

import com.graphics.engine.gl.graphics.BitmapGLDrawable;
import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.graphics.GLDrawable;
import com.graphics.engine.gl.graphics.GLFramebuffer;
import com.graphics.engine.gl.graphics.GLShaderWrapper;
import com.graphics.engine.gl.graphics.NinePatchGLDrawable;

import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

/**
 * 
 * <br>类描述: 用于模糊图片的{@link GLDrawable}
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-1-17]
 */
public class BlurGLDrawable extends GLDrawable {
	boolean mOwnDrawable;
	GLDrawable mDrawable;
	GLFramebuffer mFramebuffer1;
	GLFramebuffer mFramebuffer2;
	int mBufferWidth;
	int mBufferHeight;

	int[] mViewportBak = new int[GLCanvas.ViewportArgc];
	float[] mFrustumBak = new float[GLCanvas.FrustumArgc];

	int mTotalBlurSteps = DEFAULT_TOTAL_BLUR_STEPS;
	int mStepsOnEveryFrame = DEFAULT_STEPS_ON_EVERY_FRAME;
	int mCurSteps;
	boolean mToFinishBluring;	//失效后下次绘制时需要一次性模糊好
	boolean mLastFrameDropped;
	
	boolean mSourceGLDrawableEffectEnabled;
	boolean mSourceGLDrawableYieldOnFilterDone;
	
	private final static int BUFFER_INV_SCALE = 2;	//原图大小比例于第一个buffer
	private final static int SAMPLE_SIZE = 2;		//第一个buffer大小比例于第二个buffer
	private final static int FULL_ALPHA = 255;
	
	public final static float  DEFAULT_DENSITY = 1.5f;	//屏幕像素密度为1.5（240dpi）时，默认模糊4步
	public final static int  DEFAULT_TOTAL_BLUR_STEPS = 4;
	public final static int  DEFAULT_STEPS_ON_EVERY_FRAME = 1;

	/**
	 * <默认构造函数>
	 * @param drawalbe 如果是{@link GLDrawable}则需要外部使用者负责清除
	 * @param translucent 图片是否半透明的，如果不肯定，可以使用 drawable.getOpacity() != PixelFormat.OPAQUE。
	 * 	在不透明时可以优化掉不必要的清除操作。
	 */
	public BlurGLDrawable(Drawable drawable, boolean translucent) {
		if (drawable == null) {
			throw new IllegalArgumentException("drawable == null");
		}
		if (drawable instanceof GLDrawable) {
			mDrawable = (GLDrawable) drawable;
		} else if (drawable instanceof BitmapDrawable) {
			mDrawable = new BitmapGLDrawable((BitmapDrawable) drawable);
			mOwnDrawable = true;
		} else if (drawable instanceof NinePatchDrawable) {
			mDrawable = new NinePatchGLDrawable((NinePatchDrawable) drawable);
			mOwnDrawable = true;
		}
		
		setIntrinsicSize(drawable);
		mBufferWidth = Math.max(1, mIntrinsicWidth / BUFFER_INV_SCALE);
		mBufferHeight = Math.max(1, mIntrinsicHeight / BUFFER_INV_SCALE);
		mFramebuffer1 = new GLFramebuffer(mBufferWidth, mBufferHeight, translucent, 0, 0, false);
		mFramebuffer2 = new GLFramebuffer(mBufferWidth / SAMPLE_SIZE, mBufferHeight / SAMPLE_SIZE, translucent, 0, 0, false);
		//因为尺寸不变，自定义视口初始化一次，不需要再更改，对应后续的canvas.setOtho()
		mFramebuffer1.setCustomViewport(0, 0, mIntrinsicWidth, mIntrinsicHeight);
		mFramebuffer2.setCustomViewport(0, 0, mIntrinsicWidth, mIntrinsicHeight);

		if (translucent) {
			mFramebuffer1.setClearColorOnBind(0);
			mFramebuffer2.setClearColorOnBind(0);
		}
		
		register();
		Rect bounds = drawable.getBounds();
		if (bounds.width() == 0 || bounds.height() == 0) {
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		}
		setBounds(drawable.getBounds());
	}
	
	/**
	 * <br>功能简述: 设置模糊的总步数（步数越多就计算量越大），以及每帧的步数（分多帧来模糊可以避免卡顿）
	 * <br>功能详细描述:
	 * <br>注意: 屏幕像素密度越高，模糊步数则越多，可以使用{@link #getDesiredBlurStep(float)}来获得
	 * @param totalSteps
	 * @param stepsOnEveryFrame
	 */
	public void setBlurStep(int totalSteps, int stepsOnEveryFrame) {
		mTotalBlurSteps = Math.max(1, totalSteps);
		mStepsOnEveryFrame = Math.max(1, Math.min(stepsOnEveryFrame, totalSteps));
	}
	
	/**
	 * <br>功能简述: 获取理想的模糊步数
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param density
	 * @return
	 * <pre> 几个样例：
	 * 1.0 -> 2    
	 * 1.5 -> 4   
	 * 2.0 -> 7   
	 * 2.5 -> 11  
	 * 3.0 -> 16
	 * </pre>  
	 */
	public static int getDesiredBlurStep(float density) {
		return (int) Math.round(Math.pow(density / DEFAULT_DENSITY, 2) * DEFAULT_TOTAL_BLUR_STEPS);
	}
	
	/**
	 * 是否达到模糊的总次数，如果否，则外部调用者需要要求重绘
	 */
	public boolean isBlurDone() {
		return mCurSteps >= mTotalBlurSteps;
	}
	
	/**
	 * <br>功能简述: 
	 * <br>功能详细描述: 如果是使用外部的{@link GLDrawable}作为图片源，
	 * 设置是否将它的alpha, colorFilter, shaderWrapper的效果以进行处理
	 * <br>注意: 
	 */
	public void setSourceGLDrawableEffectEnabled() {
		mSourceGLDrawableEffectEnabled = true;
	}
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述: 如果是使用外部的{@link GLDrawable}作为图片源，
	 * 那么是否在处理完暂时释放它的资源
	 * <br>注意:
	 */
	public void setSourceGLDrawableYieldOnFilterDone() {
		mSourceGLDrawableYieldOnFilterDone = true;
	}

	@Override
	public void draw(GLCanvas canvas) {
		if (mLastFrameDropped) {
			mLastFrameDropped = false;
			mCurSteps = 0;
			mToFinishBluring = true;
		}
		if (canvas.isLastFrameDropped()) {
			mLastFrameDropped = true;
			mCurSteps = 0;
			mToFinishBluring = true;
		}
		
		final int width = mDrawable.getIntrinsicWidth();
		final int height = mDrawable.getIntrinsicHeight();
		
		if (mCurSteps < mTotalBlurSteps) {

			//设置视口和正交投影方式（注意先备份当前设置，最后还要还原），以及重置模型矩阵，还有透明度
			canvas.getViewport(mViewportBak);
			canvas.getProjection(mFrustumBak);
			canvas.setOtho(width, height);
			canvas.save();
			canvas.reset();
			final int oldAlpha = canvas.getAlpha();
			canvas.setAlpha(FULL_ALPHA);
			final boolean blend = canvas.isBlendEnabled();
			canvas.setBlend(false);

			//第一次模糊之前先要将原图绘制到帧缓冲区1中
			if (mCurSteps <= 0) {
				mFramebuffer1.bind(canvas);
				canvas.clearBuffer(true, false, false);
				
				canvas.save();
				final Rect rect = mDrawable.getBounds();
				canvas.scale(mBufferWidth / (float) rect.width(), mBufferHeight
						/ (float) rect.height());
				canvas.translate(-rect.left, -rect.top);
				
				if (mSourceGLDrawableEffectEnabled) {
					mDrawable.draw(canvas);
				} else {
					mDrawable.drawWithoutEffect(canvas);
				}
				mFramebuffer1.unbind(canvas);
				
				canvas.restore();
				
				if (mOwnDrawable || mSourceGLDrawableYieldOnFilterDone) {
					mDrawable.yield();	//释放mDrawable的显存
				}
			}

			int step = Math.max(1, Math.min(mStepsOnEveryFrame, mTotalBlurSteps - mCurSteps));
			if (mToFinishBluring) {
				step = mTotalBlurSteps;
			}
			mCurSteps += step;

			final GLDrawable drawable1 = mFramebuffer1.getDrawable();
			final GLDrawable drawable2 = mFramebuffer2.getDrawable();
			
			final float smallWidth = mBufferWidth / (float) SAMPLE_SIZE;
			final float smallHeight = mBufferHeight / (float) SAMPLE_SIZE;
			for (int i = 0; i < step; ++i) {
				float w = Math.max(1, smallWidth - i);
				float h = Math.max(1, smallHeight - i);
				
				//将帧缓冲区1的内容缩小绘制到帧缓冲区2中
				canvas.reset();
				canvas.scale(w / (float) mBufferWidth, h / (float) mBufferHeight);
				mFramebuffer2.bind(canvas);
				drawable1.drawWithoutEffect(canvas);
				mFramebuffer2.unbind(canvas);

				//将帧缓冲区2的内容放大绘制回帧缓冲区1中
				canvas.reset();
				canvas.scale(mBufferWidth / (float) w, mBufferHeight / (float) h);
				mFramebuffer1.bind(canvas);
				drawable2.draw(canvas);
				mFramebuffer1.unbind(canvas);

			}

			canvas.setBlend(blend);
			canvas.setAlpha(oldAlpha);
			canvas.restore();
			canvas.setViewport(mViewportBak);
			canvas.setProjection(mFrustumBak);
			
			if (mCurSteps >= mTotalBlurSteps) {
				mToFinishBluring = false;
				mFramebuffer2.yield();	//释放mFrameBuffer2的显存
			}
			
		}
		
		canvas.save();
		final Rect rect = getBounds();
		canvas.translate(rect.left, rect.top);
		canvas.scale(rect.width() / (float) mBufferWidth, rect.height()
				/ (float) mBufferHeight);
		mFramebuffer1.getDrawable().draw(canvas);
		canvas.restore();
	}
	
	/**
	 * <br>功能简述: 更新绘制内容
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void invalidate() {
		mCurSteps = 0;
		mToFinishBluring = true;
	}
	
	@Override
	public void onTextureInvalidate() {
		super.onTextureInvalidate();
		mFramebuffer1.onTextureInvalidate();
		mFramebuffer2.onTextureInvalidate();
		mCurSteps = 0;
		mToFinishBluring = true;
	}

	@Override
	public void clear() {
		if (mReferenceCount <= 0 || --mReferenceCount > 0) {
			return;
		}
		if (mOwnDrawable) {
			mDrawable.clear();
		}
		mDrawable = null;
		mFramebuffer1.clear();
		mFramebuffer2.clear();
		unregister();
	}
	
	@Override
	public void yield() {
		if (mOwnDrawable) {
			mDrawable.yield();
		}
		mFramebuffer1.yield();
		mFramebuffer2.yield();
	}
	
	@Override
	public void setAlpha(int alpha) {
		mFramebuffer1.getDrawable().setAlpha(alpha);
	}
	
	@Override
	public void setColorFilter(int srcColor, Mode mode) {
		mFramebuffer1.getDrawable().setColorFilter(srcColor, mode);
	}
	
	@Override
	public void setShaderWrapper(GLShaderWrapper shader) {
		mFramebuffer1.getDrawable().setShaderWrapper(shader);
	}
	
	/**
	 * <br>功能简述:  源 drawable 改变，需要修改模糊内容
	 * 
	 * 注意： 传入的新drawable的内容，如果尺寸和现有的一致，则返回修改后的内容返回true，否则返回false，调用方需要看是否更新成功进行后续处理
	 * <br>另外，忽略了图片是否半透明。
	 * @param drawable
	 * @return 是否修改成功
	 */
	public boolean onDrawableChanged(GLDrawable drawable) {
		if (drawable != null 
				&& drawable.getIntrinsicWidth() == mIntrinsicWidth 
				&& drawable.getIntrinsicHeight() == mIntrinsicHeight) {
			mDrawable = drawable;
			invalidate();
			return true;
		}
		return false;
	}
}
