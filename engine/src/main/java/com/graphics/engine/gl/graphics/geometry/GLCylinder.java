package com.graphics.engine.gl.graphics.geometry;


import android.graphics.RectF;

import com.graphics.engine.gl.graphics.GLCanvas;

/**
 * 
 * <br>类描述: 使用{@link GLGrid}实现的圆柱体（只有侧面）
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-7-11]
 */
public class GLCylinder extends GLGrid {
	/** 让网格第一列在最左边所需要的起始经度 */
	public final static float ANGLE_TO_LEFT = -90;
	public final static float FULL_CIRCLE = 360;
	public final static float HALF_CIRCLE = 180;
	
	float mRadius;
	float mCenterX;
	float mCenterY;
	float mCenterZ;
	float mTop;
	float mBottom;
	float mRatioToAngle;
	float mRatioToArcLen;
	float mStartLongitude;
	float mEndLongitude;
	
	/**
	 * 创建一个按经纬度分割的圆柱体
	 * <br>注意：分割的份数不宜过多，否则会影响效率，或者可能会超出Native内存，
	 * 一般24~64就足够，跟屏幕上显示尺寸以及dpi相关。
	 * @param xDiv 横向（沿经度）分割的份数，即有 xDiv+1 个点
	 * @param yDiv 竖向（沿维度）分割的份数，即有 yDiv+1 个点
	 * @param fill 是否填充，否则只是线框
	 */
	public GLCylinder(int xDiv, int yDiv, boolean fill) {
		super(xDiv, yDiv, fill);
		mEndLongitude = (float) Math.PI * 2;
	}
	
	/**
	 * 设置经度范围，默认为[0..360]。如果小于这个范围，就是非完整的圆柱体侧面。
	 * @param start	网格第一列的经度（0度在正前方即Z轴方向）
	 * @param end	网格最后一列的经度
	 * @see {@link #ANGLE_TO_LEFT}
	 */
	public void setLongitude(float start, float end) {
		if (start > end) {
			throw new IllegalArgumentException("start=" + start + " should not greater than end=" + end);
		}
		
		float degrees = end - start;
		if (degrees > FULL_CIRCLE) {
			throw new IllegalArgumentException("start=" + start +
					" should not greater than end+360=" + (end + FULL_CIRCLE));
		}
		start %= FULL_CIRCLE;
		if (start <= -HALF_CIRCLE) {
			start += FULL_CIRCLE;
		}
		end = start + degrees;

		if (start != mStartLongitude || end != mEndLongitude) {
			mStartLongitude = start;
			mEndLongitude = end;
			RectF rect = getBounds();
			onBoundsChange(rect.left, rect.top, rect.right, rect.bottom);
		}
	}

	@Override
	protected void onBoundsChange(float left, float top, float right, float bottom) {
		mTop = -top;
		mBottom = -bottom;
		final float r = mRadius = (right - left) * 0.5f;
		final float cx = mCenterX = (left + right) * 0.5f;
		mCenterY = -(top + bottom) * 0.5f;
		float cz = mCenterZ = -mRadius;

		final int xDiv = getDivX();
		final int yDiv = getDivY();
		final float[] pos = getPositionArray();
		int index = 0;

		float y = -top;
		float dy = -(bottom - top) / yDiv;
		float startRadians = (float) Math.toRadians(mStartLongitude);
		float endRadians = (float) Math.toRadians(mEndLongitude);
		final float deltaTheta = (endRadians - startRadians) / xDiv;
		float theta = startRadians;
		//先处理第一行
		for (int i = 0; i <= xDiv; ++i) {
			float sin = (float) Math.sin(theta);
			float cos = (float) Math.cos(theta);
			float z = r * cos + cz;
			float x = r * sin + cx;
			
			pos[index++] = x;
			pos[index++] = y;
			pos[index++] = z;
			
			theta += deltaTheta;
		}
		
		//接下的每一行的x和z都和上一行的一样
		final int stride = getPositionArrayStride();
		for (int j = 1; j <= yDiv; ++j) {
			y += dy;
			for (int i = 0; i <= xDiv; ++i) {
				pos[index] = pos[index - stride];
				++index;
				pos[index] = y;
				++index;
				pos[index] = pos[index - stride];
				++index;
			}
		}

		mRatioToAngle = (float) (180 / (Math.PI * mRadius));
		mRatioToArcLen = 1 / mRatioToAngle;
	}

	/**
	 * 获取半径
	 */
	public float getRadius() {
		return mRadius;
	}

	/**
	 * 获取中心位置X
	 */
	public float getCenterX() {
		return mCenterX;
	}

	/**
	 * 获取中心位置Y
	 */
	public float getCenterY() {
		return mCenterY;
	}

	/**
	 * 获取中心位置Z
	 */
	public float getCenterZ() {
		return mCenterZ;
	}
	
	/**
	 * 获取网格第一列的经度
	 * @see {@link #setLongitude(float, float)}
	 */
	public float getStartLongitude() {
		return mStartLongitude;
	}
	
	/**
	 * 获取网格最后一列的经度
	 * @see {@link #setLongitude(float, float)}
	 */
	public float getEndLongitude() {
		return mEndLongitude;
	}

	/**
	 * 获取顶面在3D空间里的Y值（方向向上）
	 */
	public float getTop() {
		return mTop;
	}

	/**
	 * 获取底面在3D空间里的Y值（方向向上）
	 */
	public float getBottom() {
		return mBottom;
	}

	/**
	 * 获取圆柱侧面周长
	 */
	public float getPerimeter() {
		return (mEndLongitude - mStartLongitude) * mRatioToArcLen;
	}

	/**
	 * <br>功能简述: 将画布的坐标系以圆柱中心为原点，绕指定旋转轴旋转
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param canvas 画布
	 * @param a 旋转角度
	 * @param x	旋转轴X
	 * @param y 旋转轴Y
	 * @param z 旋转轴Z
	 */
	public void rotateAxisAngle(GLCanvas canvas, float a, float x, float y, float z) {
		canvas.translate(mCenterX, mCenterY, mCenterZ);
		canvas.rotateAxisAngle(a, x, y, z);
		canvas.translate(-mCenterX, -mCenterY, -mCenterZ);
	}

	/**
	 * 将弧长x转换为经度。
	 * @see {@link #angleTomToArcLen(float)}
	 */
	public float xToAngle(float x) {
		return x * mRatioToAngle + mStartLongitude;
	}

	/**
	 * 将经度转换为弧长x。
	 * <br>注意：起始角度处x=0。角度会归约到[起始角度，起始角度+360]的范围内。
	 * @see {@link #xToAngle(float)}
	 */
	public float angleTomToArcLen(float degrees) {
		degrees -= mStartLongitude;
		degrees %= FULL_CIRCLE;
		if (degrees < 0) {
			degrees += FULL_CIRCLE;
		}
		return degrees * mRatioToArcLen;
	}

}