package com.graphics.engine.graphics.geometry;

/**
 * <br>类描述: 使用{@link GLGrid}实现的圆
 * @author panguowei
 *
 */
public class GLCircle extends GLGrid {
	
	float mRadius;
	float mCenterX;
	float mCenterY;

	public GLCircle(int yDiv, boolean fill) {
		super(1, yDiv, fill); // 圆是一个平面，所以x只需要1。y方向上的横坐标会变化
	}
	
	@Override
	protected void onBoundsChange(float left, float top, float right,
			float bottom) {
		super.onBoundsChange(left, top, right, bottom);
		mRadius = Math.min(right - left, bottom - top) * 0.5f;
		mCenterX = (left + right) * 0.5f;
		mCenterY = -(top + bottom) * 0.5f;
		
		update();
	}
	
	private void update() {
		final int yDiv = getDivY();
		
		final float deltaPhi = (float) Math.PI / yDiv;
		final float[] pos = getPositionArray(); 

		float phi = 0;
		for (int j = 0, index = 0; j <= yDiv; ++j) {
			
			// 相当于只有 -90和90两条经线，所以就不循环了，以2个点为单位，一次修改两个点
			float sinPhi = (float) Math.sin(phi);
			float cosPhi = (float) Math.cos(phi);
			float y = mRadius * cosPhi + mCenterY;
			float r = mRadius * sinPhi;
			
			pos[index + 1] = y;
			pos[index + 4] = y;
			
			pos[index] = mCenterX - r; // X的绝对值相当于这个圆在y时候的纬线长度的一半
			pos[index + 3] = mCenterX + r;
			
			index += 6;
			
			phi += deltaPhi;
		}
	}
	
	/**
	 * 设置半径。
	 * @param radius 半径。应该不小于0.
	 */
	public void setRadius(float radius) {
		mRadius = radius;
		update();
	}

	/**
	 * 获取半径。
	 */
	public float getRadius() {
		return mRadius;
	}
}
