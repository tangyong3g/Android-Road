
package com.ty.example_unit_3.libgdx.ex;

import com.badlogic.gdx.math.Matrix4;

public class Matrix4Ex extends Matrix4 {

	/** <br>
	 * 功能简述: 将旋转矩阵转换成欧拉角 <br>
	 * 功能详细描述: <br>
	 * 注意: 矩阵m必须为规范正交矩阵，即不包含缩放变换
	 * @param m
	 * @param offset
	 * @param euler 输出的欧拉角，长度必须至少为3,存放依次绕x,y,z轴旋转的角度 */
	public static void convertMatrixToEulerAngle (float[] m, int offset, float[] euler) {
		final float err = 1e-5f;
		float m02 = m[offset + M02];
		if (m02 < err - 1) {
			float m21 = m[offset + M21];
			float m11 = m[offset + M11];
			euler[0] = (float)Math.toDegrees(Math.atan2(m21, m11));
			euler[1] = -90;
			euler[2] = 0;
		} else if (m02 > 1 - err) {
			float m21 = m[offset + M21];
			float m11 = m[offset + M11];
			euler[0] = (float)Math.toDegrees(Math.atan2(m21, m11));
			euler[1] = 90;
			euler[2] = 0;
		} else {
			float m12 = m[offset + M12];
			float m22 = m[offset + M22];
			euler[0] = (float)Math.toDegrees(Math.atan2(-m12, m22));
			euler[1] = (float)Math.toDegrees(Math.asin(m02));
			float m01 = m[offset + M01];
			float m00 = m[offset + M00];
			euler[2] = (float)Math.toDegrees(Math.atan2(-m01, m00));
		}
	}

}
