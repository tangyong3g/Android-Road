package com.graphics.engine.gl.graphics.filters;


import com.graphics.engine.gl.graphics.BitmapGLDrawable;
import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.graphics.GLDrawable;
import com.graphics.engine.gl.graphics.GLFramebuffer;
import com.graphics.engine.gl.graphics.GLShaderWrapper;
import com.graphics.engine.gl.graphics.NinePatchGLDrawable;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

/**
 * <br>类描述: 实现纯色发光效果的{@link GLDrawable}
 * <br>功能详细描述: 
 * <ul>具体效果的设置相关方法： 
 * 	<li>{@link #setGlowColor(int)}
 * 	<li>{@link #setGlowStrength(int)}
 * 	<li>{@link #setSourceGLDrawableEffectEnabled()}
 * 	<li>{@link #setSourceGLDrawableYieldOnFilterDone()}
 * </ul>
 * 
 * @author dengweiming
 * @date [2013-4-1]
 */
public class GlowGLDrawable extends GLDrawable {
	boolean mOwnDrawable;
	GLDrawable mDrawable;
	GLFramebuffer mFramebuffer1;
	GLFramebuffer mFramebuffer2;

	int[] mViewportBak = new int[GLCanvas.ViewportArgc];
	float[] mFrustumBak = new float[GLCanvas.FrustumArgc];

	int mTotalBlurSteps;
	int mStepsOnEveryFrame;
	int mCurSteps;
	boolean mToFinishBluring; // 失效后下次绘制时需要一次性模糊好
	boolean mLastFrameDropped;
	
	boolean mSourceGLDrawableEffectEnabled;
	boolean mSourceGLDrawableYieldOnFilterDone;

	private final static int FULL_ALPHA = 255;

	GlowShaderWrapper mBlurShaderWrapper;
	int mGlowColor;
	final float[] mColor = new float[4];	//CHECKSTYLE IGNORE
	float mStrength = 1;
	int mBoarderSize;
	boolean mInner;
	boolean mKnockOut;

	int mAlpha = FULL_ALPHA;
	int mSrcColor;
	PorterDuff.Mode mMode;
	GLShaderWrapper mShaderWrapper;



	/**
	 * <默认构造函数>
	 * @param res			用于从assets读取shader源文件
	 * @param drawalbe		如果是{@link GLDrawable}则需要外部使用者负责清除
	 * @param glowRadius 	发光的半径（单位是px，不同dpi下要预先将dp转到px），值越大则传播发光范围越大越模糊
	 * @param inner 		是否为内发光
	 * @param knockOut		是否镂空
	 * 例如创建一个勾勒轮廓的效果，可以使用(具体数值可以再细调)glowRadius=1*density, inner=false, knockOut=true。
	 * 如果是普通发光效果，可以使用glowRadius=6*density, inner=false, knockOut=false, 并且setGlowStrength(12*density)。
	 * 如果是内发光效果，可以使用glowRadius=8*density, inner=true, knockOut=true
	 */
	public GlowGLDrawable(Resources res, Drawable drawable, float glowRadius, boolean inner, boolean knockOut) {
		if (drawable == null) {
			throw new IllegalArgumentException("drawable == null");
		}
		if (glowRadius <= 0) {
			throw new IllegalArgumentException("glow Radius <= 0");
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
		
		mInner = inner;
		mKnockOut = knockOut;
		
		mBlurShaderWrapper = GlowShaderWrapper.getInstance(res, glowRadius);
		int kernalRadius = mBlurShaderWrapper.getKernalRadius();
		
		mTotalBlurSteps = Math.round(glowRadius / kernalRadius);
		mStepsOnEveryFrame = mTotalBlurSteps;	//XXX: 如果!mInner&&!mKnockOut，可以设置为1,使得逐步模糊，避免一次完成造成卡帧

		if (!mInner) {
			mBoarderSize = (int) Math.ceil(kernalRadius * mTotalBlurSteps);
		}

		setIntrinsicSize(drawable);
		int w = mIntrinsicWidth += mBoarderSize * 2;
		int h = mIntrinsicHeight += mBoarderSize * 2;
		mFramebuffer1 = new GLFramebuffer(w, h, true, 0, 0, false);
		mFramebuffer1.setCaptureRectSize(w, h, false);

		mFramebuffer2 = new GLFramebuffer(w, h, true, 0, 0, false);
		mFramebuffer2.setCaptureRectSize(w, h, false);
		
		//因为尺寸不变，自定义视口初始化一次，不需要再更改，对应后续的canvas.setOtho()
		mFramebuffer1.setCustomViewport(0, 0, mIntrinsicWidth, mIntrinsicHeight);
		mFramebuffer2.setCustomViewport(0, 0, mIntrinsicWidth, mIntrinsicHeight);

		register();

		setGlowColor(0xFFFFFFFF);
		Rect bounds = drawable.getBounds();
		if (bounds.width() == 0 || bounds.height() == 0) {
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		}
		setBounds(drawable.getBounds());
	}
	
	/**
	 * <br>
	 * 功能简述: 设置发光颜色 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param color
	 */
	public void setGlowColor(int color) {
		if (mGlowColor != color) {
			mGlowColor = color;
			GLCanvas.convertColorToPremultipliedFormat(color, mColor, 0);
			mCurSteps = 0;
			mToFinishBluring = true;
		}
	}

	/**
	 * <br>
	 * 功能简述: 设置发光强度 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param strength [-255..255]，默认为0
	 */
	public void setGlowStrength(int strength) {
		mStrength = 1 + strength * ONE_OVER_255;
		mCurSteps = 0;
		mToFinishBluring = true;
	}

	/**
	 * <br>功能简述: 获取边框的大小
	 * <br>功能详细描述: 如果包含了外发光，那么具有边框
	 * <br>注意:
	 * @return
	 */
	public int getBoardSize() {
		return mBoarderSize;
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
	
	GLDrawable apply(GLCanvas canvas) {
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
		final boolean innerAndKnockOut = mInner && mKnockOut;
		final boolean innerOrKnockOut = mInner || mKnockOut;
		mToFinishBluring |= innerOrKnockOut;

		final int extWidth = getIntrinsicWidth();
		final int extHeight = getIntrinsicHeight();
		final int width = extWidth - mBoarderSize * 2;
		final int height = extHeight - mBoarderSize * 2;
		final boolean blendEnabledSaved = canvas.isBlendEnabled();
		final Mode blendModeSaved = canvas.getBlendMode();
		
		if (mCurSteps < mTotalBlurSteps) {
			// 设置视口和正交投影方式（注意先备份当前设置，最后还要还原），以及重置模型矩阵，还有透明度
			canvas.getViewport(mViewportBak);
			canvas.getProjection(mFrustumBak);
			canvas.setOtho(extWidth, extHeight);
			canvas.save();
			canvas.reset();
			final int oldAlpha = canvas.getAlpha();
			canvas.setAlpha(FULL_ALPHA);

			// 第一次模糊之前先要将原图绘制到帧缓冲区1中
			if (mCurSteps <= 0) {
				mFramebuffer1.bind(canvas);
				canvas.save();
				canvas.translate(mBoarderSize, mBoarderSize);
				
				int savedClearColor = canvas.getClearColor();
				int clearColor = 0;
				if (innerAndKnockOut) {
					canvas.setBlend(true);
					canvas.setBlendMode(Mode.DST_OUT);
					clearColor = mGlowColor;
				}
				
				canvas.setClearColor(clearColor);
				canvas.clearBuffer(true, false, false);

				if (!mOwnDrawable) {
					final Rect rect = mDrawable.getBounds();
					canvas.scale(width / (float) rect.width(), height / (float) rect.height());
					canvas.translate(-rect.left, -rect.top);
				}
				if (mSourceGLDrawableEffectEnabled) {
					mDrawable.draw(canvas);
				} else {
					mDrawable.drawWithoutEffect(canvas);
				}
				
				canvas.restore();
				mFramebuffer1.unbind(canvas);
				
				canvas.setClearColor(savedClearColor);

				mFramebuffer2.bind(canvas);
				canvas.clearBuffer(true, false, false);
				mFramebuffer2.unbind(canvas);
			}

			int step = Math.max(1, Math.min(mStepsOnEveryFrame, mTotalBlurSteps - mCurSteps));
			if (mToFinishBluring) {
				step = mTotalBlurSteps;
			}
			mCurSteps += step;

			mBlurShaderWrapper.setGlowColor(canvas, mStrength, mColor);

			final GLDrawable drawable1 = mFramebuffer1.getDrawable();
			final GLDrawable drawable2 = mFramebuffer2.getDrawable();

			drawable1.setAlpha(FULL_ALPHA);
			drawable1.setColorFilter(0, null);
			drawable1.setShaderWrapper(null);

			drawable1.setShaderWrapper(mBlurShaderWrapper);
			drawable2.setShaderWrapper(mBlurShaderWrapper);
			// 因为模糊过程中下一帧总是比上一帧覆盖像素多，所以通过禁用混合的方式避免清除缓冲区（只需初始化时清除一次）
			canvas.setBlend(false);

			for (int i = 0; i < step; ++i) {
				// 将帧缓冲区1的内容横向模糊绘制到帧缓冲区2中
				mFramebuffer2.bind(canvas);
				// canvas.clearBuffer(true, false, false);
				mBlurShaderWrapper.setGlowInvTargetSize(canvas, 1.0f / width, 0);
				drawable1.draw(canvas);
				mFramebuffer2.unbind(canvas);

				// 将帧缓冲区2的内容纵向模糊绘制回帧缓冲区1中
				mFramebuffer1.bind(canvas);
				// canvas.clearBuffer(true, false, false);
				mBlurShaderWrapper.setGlowInvTargetSize(canvas, 0, 1.0f / height);
				drawable2.draw(canvas);
				mFramebuffer1.unbind(canvas);
			}


			drawable1.setShaderWrapper(null);
			drawable2.setShaderWrapper(null);

			drawable1.setAlpha(mAlpha);
			drawable1.setColorFilter(mSrcColor, mMode);
			drawable1.setShaderWrapper(mShaderWrapper);

			if (mCurSteps >= mTotalBlurSteps) {
				mToFinishBluring = false;

				if (innerOrKnockOut) {
					canvas.setBlend(true);
					mFramebuffer1.bind(canvas);
					canvas.save();
					canvas.translate(mBoarderSize, mBoarderSize);

					canvas.setBlendMode(mInner ? Mode.DST_IN : Mode.DST_OUT);
					if (!mOwnDrawable) {
						final Rect rect = mDrawable.getBounds();
						canvas.scale(width / (float) rect.width(), height / (float) rect.height());
						canvas.translate(-rect.left, -rect.top);
					}
					if (mSourceGLDrawableEffectEnabled) {
						mDrawable.draw(canvas);
					} else {
						mDrawable.drawWithoutEffect(canvas);
					}
					
					canvas.restore();
					mFramebuffer1.unbind(canvas);
				}
				
				mFramebuffer2.yield(); // 释放mFrameBuffer2的显存
				
				if (mOwnDrawable || mSourceGLDrawableYieldOnFilterDone) {
					mDrawable.yield();	//释放mDrawable的显存
				}
			}

			canvas.setBlendMode(blendModeSaved);
			canvas.setBlend(blendEnabledSaved);
			canvas.setAlpha(oldAlpha);
			canvas.restore();
			canvas.setViewport(mViewportBak);
			canvas.setProjection(mFrustumBak);

		}
		return mFramebuffer1.getDrawable();
	}
	
	@Override
	public void draw(GLCanvas canvas) {
		apply(canvas);
		
		final int extWidth = getIntrinsicWidth();
		final int extHeight = getIntrinsicHeight();
		final int width = extWidth - mBoarderSize * 2;
		final int height = extHeight - mBoarderSize * 2;
		
		canvas.save();
		final Rect rect = getBounds();
		canvas.translate(rect.left, rect.top);
		canvas.scale(rect.width() / (float) width, rect.height() / (float) height);
		canvas.translate(-mBoarderSize, -mBoarderSize);
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
		invalidate();
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

		mShaderWrapper = null;
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
		mAlpha = alpha;
		mFramebuffer1.getDrawable().setAlpha(alpha);
	}

	@Override
	public void setColorFilter(int srcColor, Mode mode) {
		mSrcColor = srcColor;
		mMode = mode;
		mFramebuffer1.getDrawable().setColorFilter(srcColor, mode);
	}

	@Override
	public void setShaderWrapper(GLShaderWrapper shader) {
		mShaderWrapper = shader;
		mFramebuffer1.getDrawable().setShaderWrapper(shader);
	}

}
