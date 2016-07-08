package com.graphics.engine.graphics.geometry;

/**
 * 
 * <br>类描述: 使用{@link GLGrid}实现的球体
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-7-11]
 */
public class GLSphere extends GLGrid {
	float mRadius;
	float mCenterX;
	float mCenterY;
	float mCenterZ;
	float mStartLongitude;


	/**
	 * 创建一个按经纬度分割的球体
	 * <br>注意：分割的份数不宜过多，否则会影响效率，或者可能会超出Native内存，
	 * 一般24~64就足够，跟屏幕上显示尺寸以及dpi相关。
	 * @param xDiv 横向（沿经度）分割的份数，即有 xDiv+1 个点
	 * @param yDiv 竖向（沿维度）分割的份数，即有 yDiv+1 个点
	 * @param fill 是否填充，否则只是线框
	 * @param startLongitude 网格第一列（一般来说，也即对应纹理u=0）的点的经度(以正前方为0度）。
	 * 		假如纹理左边对应东经180度，而我们想让本初子午线在屏幕左边(-90度），那么这个参数应为-90+180
	 */
	public GLSphere(int xDiv, int yDiv, boolean fill, float startLongitude) {
		super(xDiv, yDiv, fill);
		mStartLongitude = (float) Math.toRadians(startLongitude);
	}
	
	@Override
	protected void onBoundsChange(float left, float top, float right, float bottom) {
		super.onBoundsChange(left, top, right, bottom);
		mRadius = Math.min(right - left, bottom - top) * 0.5f;
		mCenterX = (left + right) * 0.5f;
		mCenterY = -(top + bottom) * 0.5f;
		mCenterZ = -mRadius;
		
		updatePositions();
	}
	
	private void updatePositions() {
		final int xDiv = getDivX();
		final int yDiv = getDivY();
		final float deltaPhi = (float) Math.PI / yDiv;
		final float deltaTheta = (float) Math.PI * 2 / xDiv;
		final float[] pos = getPositionArray(); 
		
		float phi = 0;
		for (int j = 0, index = 0; j <= yDiv; ++j) {
			float sinPhi = (float) Math.sin(phi);
			float cosPhi = (float) Math.cos(phi);
			float y = mRadius * cosPhi + mCenterY;
			float r = mRadius * sinPhi;
			float theta = mStartLongitude;
			for (int i = 0; i <= xDiv; ++i) {
				float sin = (float) Math.sin(theta);
				float cos = (float) Math.cos(theta);
				float z = r * cos + mCenterZ;
				float x = r * sin + mCenterX;
				pos[index++] = x;
				pos[index++] = y;
				pos[index++] = z;
				theta += deltaTheta;
			}
			phi += deltaPhi;
		}
	}
	
	/**
	 * 设置半径
	 * @param radius	半径, 应该不小于0
	 */
	public void setRadius(float radius) {
		mRadius = radius;
		mCenterZ = -mRadius;
		updatePositions();
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
}
