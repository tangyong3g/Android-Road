package com.graphics.engine.gl.util;

import com.graphics.engine.gl.animation.Transformation3D;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;


/**
 * 
 * <br>类描述: 射线类
 * <br>功能详细描述:
 * @deprecated 使用{@link com.graphics.engine.gl.math3d.Ray}代替
 */
public class Ray {
	
	private static final float EPSILON = 1e-6f;

	private static FloatBuffer gBufPosition = IBufferFactory.newFloatBuffer(2 * 3);	// CHECKSTYLE IGNORE

	/**
	 * 射线原点
	 */
	public Vector3f mvOrigin = new Vector3f();
	/**
	 * 射线方向
	 */
	public Vector3f mvDirection = new Vector3f();
	
	/**
	 * 设置起点
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setOrigin(float x, float y, float z) {
		mvOrigin.x = x;
		mvOrigin.y = y;
		mvOrigin.z = z;
	}
	
	/**
	 * 设置方向，会自动归一化
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setDirection(float x, float y, float z) {
		final float square = x * x + y * y + z * z;
		if (Math.abs(square - 1) > EPSILON) {
			final float rLen = (float) (1 / Math.sqrt(square));
			x *= rLen;
			y *= rLen;
			z *= rLen;
		}
		mvDirection.x = x;
		mvDirection.y = y;
		mvDirection.z = z;
	}
	
	/**
	 * 设置终点，实际上会减掉起点位置并使用{@link #setDirection(float, float, float)} 来设置方向，
	 * 因此必须在{@link #setOrigin(float, float, float)之后调用
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setEndPoint(float x, float y, float z) {
		setDirection(x - mvOrigin.x, y - mvOrigin.y,  z - mvOrigin.z);
	}

	/**
	 * 变换射线，将结果存储到out中
	 * 
	 * @param matrix - 变换矩阵
	 * @param out - 变换后的射线
	 */
	public void transform(Matrix4f matrix, Ray out) {
		Vector3f v0 = Vector3f.TEMP;
		Vector3f v1 = Vector3f.TEMP1;
		v0.set(mvOrigin);
		v1.set(mvOrigin);
		v1.add(mvDirection);

		matrix.transform(v0, v0);
		matrix.transform(v1, v1);

		out.mvOrigin.set(v0);
		v1.sub(v0);
		v1.normalize();
		out.mvDirection.set(v1);
	}
	
	/**
	 * 将射线转换到局部坐标系
	 * 
	 * @param t
	 * @param outRay
	 */
	public void transformToLocal(Transformation3D t, Ray outRay) {
		t.inverseTransform(mvOrigin, 0, outRay.mvOrigin, 0, 1);
		t.inverseTransform(mvDirection, 0, outRay.mvDirection, 0, 0);
	}

	/**
	 * 渲染射线
	 * 
	 * @param gl
	 */
	public void draw(GL10 gl) {
		gBufPosition.position(0);

		IBufferFactory.fillBuffer(gBufPosition, mvOrigin);

		Vector3f.TEMP.set(mvDirection);
		float len = 100.0f;	// CHECKSTYLE IGNORE
		Vector3f.TEMP.scale(len);
		Vector3f.TEMP.add(mvOrigin);
		IBufferFactory.fillBuffer(gBufPosition, Vector3f.TEMP);
		gBufPosition.position(0);

//		if (AppConfig.gbTrianglePicked) {
//			gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
//		} else {
//			gl.glColor4f(0.0f, 1.0f, 0.0f, 1.0f);
//		}

		gl.glPointSize(4.0f);	// CHECKSTYLE IGNORE
		gl.glLineWidth(4.0f);	// CHECKSTYLE IGNORE
		gl.glDisable(GL10.GL_DEPTH_TEST);

//		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, gBufPosition);	// CHECKSTYLE IGNORE
		gl.glDrawArrays(GL10.GL_POINTS, 0, 2);

//		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glPointSize(1f);
		gl.glLineWidth(1.0f);
	}

	/**
	 * 检测射线是否与三角形相交
	 * 
	 * @param v0
	 *            三角形顶点0
	 * @param v1
	 *            三角形顶点1
	 * @param v2
	 *            三角形顶点2
	 * @param location
	 *            - 相交点位置，以Vector4f的形式存储。其中(x,y,z)表示相交点的具体位置，w表示相交点离射线原点的距离
	 * @return 如果相交返回true
	 */
	public boolean intersectTriangle(Vector3f v0, Vector3f v1, Vector3f v2,
			Vector4f location) {
		return intersect(v0, v1, v2, location);
	}

	private static final float MAX_ABSOLUTE_ERROR = 0.000001f;

	//CHECKSTYLE IGNORE 4 LINES
	private static Vector3f tmp0 = new Vector3f(), tmp1 = new Vector3f(),
			tmp2 = new Vector3f(), tmp3 = new Vector3f(),
			tmp4 = new Vector3f(), tmp5 = new Vector3f(),
			tmp6 = new Vector3f();

	/**
	 * 射线与三角形相交检测函数
	 * 
	 * @param v0
	 *            三角形顶点0
	 * @param v1
	 *            三角形顶点1
	 * @param v2
	 *            三角形顶点2
	 * @param loc
	 *            相交点位置，以Vector4f的形式存储。其中(x,y,z)表示相交点的具体位置，w表示相交点离射线原点的距离;
	 *            如果为null则不计算相交点
	 * @return 如果相交返回true
	 */
	private boolean intersect(Vector3f v0, Vector3f v1, Vector3f v2,
			Vector4f loc) {
		Vector3f diff = tmp0;
		Vector3f edge1 = tmp1;
		Vector3f edge2 = tmp2;
		Vector3f norm = tmp3;
		Vector3f tmp = tmp4;
		diff.sub(mvOrigin, v0);
		edge1.sub(v1, v0);
		edge2.sub(v2, v0);
		norm.cross(edge1, edge2);

		float dirDotNorm = mvDirection.dot(norm);
		float sign = 0.0f;

		if (dirDotNorm > MAX_ABSOLUTE_ERROR) {
			sign = 1;
		} else if (dirDotNorm < -MAX_ABSOLUTE_ERROR) {
			sign = -1;
			dirDotNorm = -dirDotNorm;
		} else {
			// 射线和三角形平行，不可能相交
			return false;
		}

		tmp.cross(diff, edge2);
		float dirDotDiffxEdge2 = sign * mvDirection.dot(tmp);
		if (dirDotDiffxEdge2 >= 0.0f) {
			tmp.cross(edge1, diff);
			float dirDotEdge1xDiff = sign * mvDirection.dot(tmp);
			if (dirDotEdge1xDiff >= 0.0f) {
				if (dirDotDiffxEdge2 + dirDotEdge1xDiff <= dirDotNorm) {
					float diffDotNorm = -sign * diff.dot(norm);
					if (diffDotNorm >= 0.0f) {
						// 检测到相交事件
						// 如果不需要计算精确相交点，则直接返回
						if (loc == null) {
							return true;
						}
						// 计算相交点具体位置，存储在Vector4f的x,y,z中，把距离存储在w中
						float inv = 1f / dirDotNorm;
						float t = diffDotNorm * inv;

						loc.set(mvOrigin);
						loc.add(mvDirection.x * t, mvDirection.y * t,
								mvDirection.z * t);
						loc.w = t;

						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * 射线与平面上平行四边形相交检测函数
	 * 
	 * @param v0
	 *            平行四边形顶点0
	 * @param v1
	 *            平行四边形顶点1
	 * @param v2
	 *            平行四边形顶点2
	 * @param loc
	 *            相交点位置，以Vector4f的形式存储。其中(x,y,z)表示相交点的具体位置，w表示相交点离射线原点的距离;
	 *            如果为null则不计算相交点
	 * @param locInPlane
	 * 			     相交点位置，以Vector3f的形式存储。其中(x,y)是相交点以(0,1)之间的值来表示在平行四边形的某个位置，z为相交点的深度           
	 * @return 如果相交返回true
	 * 算法参考：http://www.softsurfer.com/Archive/algorithm_0105/algorithm_0105.htm
	 */
	public boolean intersectParallelogram(Vector3f v0, Vector3f v1, Vector3f v2, Vector4f loc, Vector3f locInPlane) {
		Vector3f w0 = tmp0;
		Vector3f u = tmp1;
		Vector3f v = tmp2;
		Vector3f norm = tmp3;
		Vector3f w = tmp4;
		Vector3f tmp = tmp5;
		float r, a, b;

		//get triangle edge vectors and plane normal
		u.sub(v1, v0);
		v.sub(v2, v0);
		norm.cross(u, v);

		w0.sub(mvOrigin, v0);
		a = -norm.dot(w0);
		b = norm.dot(mvDirection);

		if (b < MAX_ABSOLUTE_ERROR && b > -MAX_ABSOLUTE_ERROR)
		{
			// 射线和三角形平行，不可能相交
			return false;
		}

		r = a / b;
		if (r < 0.0) {		// ray goes away from triangle
			return false;
		}
		
		tmp.set(mvOrigin);
		tmp.add(mvDirection.x * r, mvDirection.y * r,
				mvDirection.z * r);
		
//		Log.i("Ray", "tmp.x:"+tmp.x+" tmp.y:"+tmp.y+" tmp.z:"+tmp.z);
		
		// is loc inside rectangle
		float uu, uv, vv, wu, wv, D;		// CHECKSTYLE IGNORE
		uu = u.dot(u);
		uv = u.dot(v);
		vv = v.dot(v);
		
		w.sub(tmp, v0);
		wu = w.dot(u);
		wv = w.dot(v);
		D = uv * uv - uu * vv;
		
		//get and test parametric coords
		float s, t;
		s = (uv * wv - vv * wu) / D;
		if (s < 0.0 || s > 1.0) {
			return false;
		}
		
		t = (uv * wu - uu * wv) / D;
		if (t < 0.0 || t > 1.0) {
			return false;
		}
		
		if (loc != null) {
			loc.set(tmp);
			loc.w = a / b;
		}
		
		if (locInPlane != null) {
			locInPlane.set(t, s, tmp.z);
		}
		
		return true;
	}
	
	/**
	 * 射线与平面相交检测函数
	 * 
	 * @param v
	 *            平面上一点0
	 * @param norm
	 * 			     平面的法线
	 * @param loc
	 *            相交点位置，以Vector4f的形式存储。其中(x,y,z)表示相交点的具体位置，w表示相交点离射线原点的距离;
	 *            如果为null则不计算相交点
	 * @return 如果相交返回true
	 * 算法参考：http://www.softsurfer.com/Archive/algorithm_0105/algorithm_0105.htm
	 */
	public boolean intersectPlane(Vector3f v, Vector3f norm, Vector4f loc) {
		Vector3f w0 = tmp0;
		Vector3f tmp = tmp1;
		float r, a, b;
		
		w0.sub(mvOrigin, v);
		a = -norm.dot(w0);
		b = norm.dot(mvDirection);

		if (b < MAX_ABSOLUTE_ERROR && b > -MAX_ABSOLUTE_ERROR)
		{
			// 射线和三角形平行，不可能相交
			return false;
		}

		r = a / b;
		if (r < 0.0) { // ray goes away from triangle
			return false;
		}

		tmp.set(mvOrigin);
		tmp.add(mvDirection.x * r, mvDirection.y * r, mvDirection.z * r);

		if (loc != null) {
			loc.set(tmp);
			loc.w = a / b;
		}

		return true;
	}
	
	/**
	 * 检测射线是否与包围球相交
	 * 
	 * @param center
	 *            圆心
	 * @param radius
	 *            半径
	 * @return 如果相交返回true
	 */
	public boolean intersectSphere(Vector3f center, float radius) {
		Vector3f diff = tmp0;
		diff.sub(mvOrigin, center);
		float r2 = radius * radius;
		float a = diff.dot(diff) - r2;
		if (a <= 0.0f) {
			// 在包围球内
			return true;
		}

		float b = mvDirection.dot(diff);
		if (b >= 0.0f) {
			return false;
		}
		return b * b >= a;
	}
}
