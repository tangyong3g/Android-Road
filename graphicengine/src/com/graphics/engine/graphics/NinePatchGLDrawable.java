package com.graphics.engine.graphics;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.NinePatchDrawable;

/**
 * <br>类描述: 绘制NinePatch的{@link GLDrawable}
 * <br>功能详细描述:
 * NinePatch是一种分块位图，一般分成3X3块，在作为视图背景的时候，可以自适应不同的大小。
 * 
 * @author  dengweiming
 * @date  [2013-10-16]
 */
public class NinePatchGLDrawable extends GLDrawable {
	private GLNinePatch mGlNinePatch;

	/**
	 * 使用NinePatchDrawable创建一个实例
	 * @see {@link GLDrawable#getDrawable(android.content.res.Resources, int)}
	 */
	public NinePatchGLDrawable(NinePatchDrawable drawable) {
		mGlNinePatch = new GLNinePatch(drawable);
		mIntrinsicWidth = drawable.getIntrinsicWidth();
		mIntrinsicHeight = drawable.getIntrinsicHeight();
		
		register();
		setBounds(0, 0, mIntrinsicWidth, mIntrinsicHeight);
		
		mOpaque = drawable.getOpacity();
	}
	
	@Override
	public void draw(GLCanvas canvas) {
		mGlNinePatch.draw(canvas);
	}
	
	@Override
	public void drawWithoutEffect(GLCanvas canvas) {
		mGlNinePatch.drawWithoutEffect(canvas);
	}
	
	@Override
	public void clear() {
		unregister();
		mGlNinePatch.clear();
	}

	@Override
	public void onTextureInvalidate() {
		mGlNinePatch.onTextureInvalidate();
	}
	
	@Override
	protected void onBoundsChange(Rect bounds) {
		mGlNinePatch.setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
	}
	
	@Override
	public void setBounds3D(float[] pts, int offsetLT, int offsetLB, int offsetRT, boolean extPaddingX, boolean extPaddingY) {
		super.setBounds3D(pts, offsetLT, offsetLB, offsetRT, extPaddingX, extPaddingY);
		if (pts != null) {
			mGlNinePatch.setBounds3D(pts, offsetLT, offsetLB, offsetRT, extPaddingX, extPaddingY);
		}
	}
	
	@Override
	public void setAlpha(int alpha) {
		mGlNinePatch.setAlpha(alpha);
	}
	
	@Override
	public void setColorFilter(int srcColor, Mode mode) {
		mGlNinePatch.setColorFilter(srcColor, mode);
	}
	
	@Override
	public boolean getPadding(Rect padding) {
		mGlNinePatch.getPadding(padding);
		return true;
	}
	
	@Override
	public void setTexture(Texture texture) {
		mGlNinePatch.setTexture(texture);
	}
	
	@Override
	public Texture getTexture() {
		return mGlNinePatch.getTexture();
	}
	
	@Override
	public Bitmap getBitmap() {
		return mGlNinePatch.getBitmap();
	}
	
	@Override
	public void setShaderWrapper(GLShaderWrapper shader) {
		mGlNinePatch.setShaderWrapper(shader);
	}
	
	@Override
	public GLShaderWrapper getShaderWrapper() {
		return mGlNinePatch.getShaderWrapper();
	}
	
	@Override
	public void yield() {
		mGlNinePatch.yield();
	}
	
	@Override
	public boolean isBitmapRecycled() {
		return mGlNinePatch.isBitmapRecycled();
	}
}
