package com.graphics.engine.gl.util;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;

import android.opengl.Matrix;

/**
 * 
 * <br>类描述: 绘制发光线条的类
 * <br>功能详细描述:
 * @deprecated
 */
public class DrawFlameLine {
    private GLDrawable mGLDrawable;
	
    /**
     * @param drawable 画线所用的drawable
     * @param lineCount 最大支持的线段数
     */
	public DrawFlameLine(GLDrawable drawable, int lineCount) {
		mGLDrawable = drawable;
	}
	
	// CHECKSTYLE IGNORE 5 LINES
	int[] mViewport = new int[4];					
	float[] mTempBuffer = new float[16 + 4 + 4];
	final int M_OFFSET = 0; // 0..15
	final int V_OFFSET = 16; // 16..19
	final int V2_OFFSET = 20; // 20..23
	
	
	/** 
	 * @param canvas
	 * @param pointCount	点的数目，每两个点作为一条线段
	 * @param vertices	线的顶点，3d坐标（x,y,z）。第n条线的顶点为（vertices[6*n], vertices[6*n+1], vertices[6*n+2]）
	 * 			和（vertices[6*n+3], vertices[6*n+4], vertices[6*n+5]）
	 * @param lineWidth 线的宽度。第n条线的宽度为lineWidth[n]，为null则使用图片高度
	 * @param alpha	线的alpha值。取值范围0~255。第n条线的alpha值为alpha[n]，为null时则使用255
	 */
	public void draw(GLCanvas canvas, int pointCount, float[] vertices, int[] lineWidth, int[] alpha) {
		System.arraycopy(canvas.getFinalMatrix(), 0, mTempBuffer, 0, 16);	// CHECKSTYLE IGNORE
		
        final int oldAlpha = canvas.getAlpha();
        int curAlpha = oldAlpha;
        int saveCount = canvas.save();
        canvas.reset();
        
        canvas.getViewport(mViewport);
		final float left = mViewport[0];
		final float bottom = mViewport[1];
		final float width = mViewport[2];
		final float height = mViewport[3];		// CHECKSTYLE IGNORE
		int j = 0;
		final float[] m = mTempBuffer;
		for (int i = 0; i + 6 <= vertices.length;) {	// CHECKSTYLE IGNORE
			m[V_OFFSET + 0] = vertices[i++];
			m[V_OFFSET + 1] = vertices[i++];
			m[V_OFFSET + 2] = vertices[i++];
			m[V_OFFSET + 3] = 1.0f;			// CHECKSTYLE IGNORE
			Matrix.multiplyMV(m, V2_OFFSET, m, M_OFFSET, m, V_OFFSET);
			// CHECKSTYLE IGNORE 3 LINES
			final float rw = 1.0f / m[V2_OFFSET + 3];
			float x0 = (m[V2_OFFSET + 0] * rw + 1) * width * 0.5f + left + 0.5f;
			float y0 = (m[V2_OFFSET + 1] * rw + 1) * height * 0.5f + bottom + 0.5f;
			y0 = height - y0;
			
			m[V_OFFSET + 0] = vertices[i++];
			m[V_OFFSET + 1] = vertices[i++];
			m[V_OFFSET + 2] = vertices[i++];
			m[V_OFFSET + 3] = 1.0f;	// CHECKSTYLE IGNORE
			Matrix.multiplyMV(m, V2_OFFSET,	m, M_OFFSET, m, V_OFFSET);
			// CHECKSTYLE IGNORE 3 LINES
			final float rw2 = 1.0f / m[V2_OFFSET + 3];
			float x1 = (m[V2_OFFSET + 0] * rw2 + 1) * width * 0.5f + left - 0.5f;
			float y1 = (m[V2_OFFSET + 1] * rw2 + 1) * height * 0.5f + bottom - 0.5f;
			y1 = height - y1;
			
			int newAlpha = curAlpha;
			if (alpha != null && j < alpha.length) {
				newAlpha = oldAlpha * alpha[j] / 255;	// CHECKSTYLE IGNORE
				if (curAlpha != newAlpha) {
					curAlpha = newAlpha;
					canvas.setAlpha(curAlpha);
				}
			}
			final int lw = (lineWidth != null && j < lineWidth.length)
				? lineWidth[j] : mGLDrawable.getIntrinsicHeight();

			canvas.save();
			canvas.translate(x0, y0);
			canvas.rotate(getAngle(x0, y0, x1, y1));
			// CHECKSTYLE IGNORE 2 LINES
			canvas.translate(-0.5f, -lw / 2.0f + lw / 2);
			mGLDrawable.setBounds(0, -lw / 2, (int) (getLength(x0, y0, x1, y1) + 1.5f), lw - lw / 2);
			mGLDrawable.draw(canvas);
			canvas.restore();
		}

        canvas.setAlpha(oldAlpha);
        canvas.restoreToCount(saveCount);
	}
	
	public static float getLength(float x0, float y0, float x1, float y1) {
		return (float) Math.hypot(x1 - x0, y1 - y0);
	}

	//以（x0,y0)为原点，（x1,y1）相对于它的角度
	public static float getAngle(float x0, float y0, float x1, float y1) {
		return (float) Math.toDegrees(Math.atan2(y1 - y0, x1 - x0));
	}
}
