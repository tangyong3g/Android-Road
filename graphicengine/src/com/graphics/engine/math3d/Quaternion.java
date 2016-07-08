package com.graphics.engine.math3d;


/**
 * 
 * <br>类描述: 四元数类，用来表示旋转
 * <br>功能详细描述:
 * <br><em>使用前先看{@link GeometryPools}</em>
 * <br>四元数与复数类似，对于旋转轴 A(x, y, z) 和旋转角度 θ 的轴-角对，对应的四元数为 
 * Asinθ/2 + cosθ/2，也即 (x*sinθ/2, y*sinθ/2, z*sinθ/2, cosθ/2)。
 * <br>四元数的优势在于可以方便地对旋转动画做插值，没有欧拉角的万向锁问题。
 * 
 * @author  dengweiming
 * @date  [2013-7-9]
 */
public class Quaternion {
	//CHECKSTYLE IGNORE 4 LINES
	float x;
	float y;
	float z;
	float w;
	
	private final static Quaternion NEG_QUATERNION = new Quaternion();
	private final static Vector AXIS = new Vector();
	private static final float[] RES_VECTOR = new float[Math3D.VECTOR_LENGTH];

	public Quaternion() {
		reset();
	}
	
	public Quaternion(float x, float y, float z, float w) {
		set(x, y, z, w);
	}
	
	public Quaternion set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		normalize();
		return this;
	}

	public Quaternion set(Quaternion q) {
		x = q.x;
		y = q.y;
		z = q.z;
		w = q.w;
		return this;
	}

	public Quaternion setTo(Quaternion q) {
		q.x = x;
		q.y = y;
		q.z = z;
		q.w = w;
		return q;
	}

	/**
	 * 反序列化
	 * @param src 源数据数组
	 * @param offset 要读的数据在<var>src</var>中的开始索引
	 * @return 新的<var>offset</var>值，用于下一个调用
	 * @see {@link #toArray(float[], int)}
	 */
	public int fromArray(float[] src, int offset) {
		x = src[offset++];
		y = src[offset++];
		z = src[offset++];
		w = src[offset++];
		normalize();
		return offset;
	}

	/**
	 * 序列化
	 * @param dst 目标数据数组
	 * @param offset 要写的数据在<var>dst</var>的开始索引
	 * @return 新的<var>offset</var>值，用于下一个调用
	 * @see {@link #fromArray(float[], int)}
	 */
	public int toArray(float[] dst, int offset) {
		dst[offset++] = x;
		dst[offset++] = y;
		dst[offset++] = z;
		dst[offset++] = w;
		return offset;
	}

	@Override
	public String toString() {
		return "Quaternion(" + x + ", " + y + ", " + z + ", " + w + ")";
	}
	
	Quaternion reset() {
		x = 0;
		y = 0;
		z = 0;
		w = 1;
		return this;
	}
	
	public float length() {
		float sqLen = x * x + y * y + z * z + w * w;
		if (sqLen > Math3D.EPSILON) {
			return (float) Math.sqrt(sqLen);
		}
		return 0;
	}
	
	/**
	 * 计算长度的平方
	 */
	public float sqLength() {
		return x * x + y * y + z * z + w * w;
	}
	
	/**
	 * 规范化为单位向量
	 */
	public Quaternion normalize() {
		float sqLen = x * x + y * y + z * z + w * w;
		if (!Math3D.fZero(sqLen)) {
			float recipLen = (float) (1 / Math.sqrt(sqLen));
			x *= recipLen;
			y *= recipLen;
			z *= recipLen;
			w *= recipLen;
		}
		return this;
	}
	
	/**
	 * 四元数取反。注意轴取反并且旋转角取反后，结果是等价的，即-q==q。
	 * @see {@link #invert()}
	 */
	public Quaternion neg() {
		Quaternion res = GeometryPools.acquireRawQuaternion();
		res.x = -x;
		res.y = -y;
		res.z = -z;
		res.w = -w;
		return res;
	}
	
	/**
	 * 四元数求逆，即反向旋转。
	 */
	public Quaternion invert() {
		Quaternion res = GeometryPools.acquireRawQuaternion();
		res.x = -x;
		res.y = -y;
		res.z = -z;
		res.w = w;
//		res.normalize();	//因为我们总是使用单位四元数，所以不必再做规范化
		return res;
	}
	
	/**
	 * 四元数乘法，即旋转的累积
	 */
	public Quaternion mul(Quaternion q) {
		Quaternion res = GeometryPools.acquireRawQuaternion();
		//Q(v1, s1) * Q(v2, s2) = Q(v1*s2+v2*s1+v1Xv2, s2*s2-v1*v2)
//		Vector v1 = GeometryPools.aquireVector().set(x, y, z);
//		float s1 = w;
//		Vector v2 = GeometryPools.aquireVector().set(q.x, q.y, q.z);
//		float s2 = q.w;
//		Vector v3 = v1.mul(s2).add(v2.mul(s1)).add(v1.cross(v2));
//		res.x = v3.x;
//		res.y = v3.y;
//		res.z = v3.z;
//		res.w = s1 * s2 - v1.dot(v2);
		res.x = w * q.x + x * q.w + y * q.z - z * q.y;
		res.y = w * q.y - x * q.z + y * q.w + z * q.x;
		res.z = w * q.z + x * q.y - y * q.x + z * q.w;
		res.w = w * q.w - x * q.x - y * q.y - z * q.z;
		return res;
	}
	
	/**
	 * 四元数的差，即从 q 旋转到 this 需要的角位移（其实更接近与除法的意义）。
	 */
	public Quaternion sub(Quaternion q) {
		return q.invert().mul(this);
	}
	
	public Quaternion fromAxisAngle(float a, float x, float y, float z) {
		AXIS.set(x, y, z);
		AXIS.normalize();
		float theta = a * 0.5f * Math3D.TO_RADIAN;
		float sin = (float) Math.sin(theta);
		this.w = (float) Math.cos(theta);
		this.x = AXIS.x * sin;
		this.y = AXIS.y * sin;
		this.z = AXIS.z * sin;
		return this;
	}
	
	/**
	 * @return  返回值存在临时数组中，需要尽快取出，以免被后续调用覆写了。依次存放的是 x, y, z, a（和通常的参数顺序不一样）。
	 */
	public float[] toAxisAngle() {
		float[] res = RES_VECTOR;
		float rSin = Math3D.invSqrt(1 - w * w);
		float theta = (float) Math.acos(w) * 2 * Math3D.TO_DEGREE;
		res[Math3D.VTW] = theta;
		res[Math3D.VTX] = x * rSin;
		res[Math3D.VTY] = y * rSin;
		res[Math3D.VTZ] = z * rSin;
		return res;
	}
	
	public Quaternion fromEuler(float x, float y, float z) {
		x *= Math3D.TO_RADIAN * 0.5f;
		y *= Math3D.TO_RADIAN * 0.5f;
		z *= Math3D.TO_RADIAN * 0.5f;
		float sx = (float) Math.sin(x);
		float cx = (float) Math.cos(x);
		float sy = (float) Math.sin(y);
		float cy = (float) Math.cos(y);
		float sz = (float) Math.sin(z);
		float cz = (float) Math.cos(z);
		this.x = sx * cy * cz + cx * sy * sz;
		this.y = cx * sy * cz - sx * cy * sz;
		this.z = sx * sy * cz + cx * cy * sz;
		this.w = cx * cy * cz - sx * sy * sz;
		return this;
	}
	
	/**
	 * @return  返回值存在临时数组中，需要尽快取出，以免被后续调用覆写了
	 */
	public float[] toEuler() {
		float[] res = RES_VECTOR;
		Math3D.todo();	//TODO
		return res;
	}
	
	public Quaternion fromMatrix(Matrix matrix) {
		matrix.getRotationQuaternion(this);
		return this;
	}
	
	public Matrix toMatrix() {
		Matrix matrix = GeometryPools.acquireRawMatrix();
		matrix.setRotationQuaternion(this);
		matrix.m[Math3D.M30] = matrix.m[Math3D.M31] = matrix.m[Math3D.M32] = 0;
		matrix.m[Math3D.M33] = 1;
		return matrix;
	}
	
	/**
	 * 球型线性插值（Spherical Linear Interpolation），可以保证以最短大弧的方式进行插值
	 */
	public Quaternion slerp(Quaternion q, float t) {
		Quaternion res = GeometryPools.acquireRawQuaternion();
		float dot = x * q.x + y * q.y + z * q.z + w * q.w;
		if (dot < 0) {
			dot = -dot;
			NEG_QUATERNION.x = -q.x;
			NEG_QUATERNION.y = -q.y;
			NEG_QUATERNION.z = -q.z;
			NEG_QUATERNION.w = -q.w;
			q = NEG_QUATERNION;	// q=-q;
		}
		if (dot > 1 - Math3D.EPSILON) {
			res.x = (q.x - x) * t + x;
			res.y = (q.y - y) * t + y;
			res.z = (q.z - z) * t + z;
			res.w = (q.w - w) * t + w;
			return res;
		}
		float theta = (float) Math.acos(dot);
		//q3 = sinθ(1-t)/sinθ * q1 + sinθt/sinθ * q2
		float rSin = (float) (1 / Math.sin(theta));
		float c1 = (float) Math.sin(theta * (1 - t)) * rSin;
		float c2 = (float) Math.sin(theta * t) * rSin;
		res.x = c1 * x + c2 * q.x;
		res.y = c1 * y + c2 * q.y;
		res.z = c1 * z + c2 * q.z;
		res.w = c1 * w + c2 * q.w;
		return res;
	}

	/**
	 * 计算从向量 v1 变换到 v2 所需的四元数。
	 * <br>算法对 v1 与 v2 比较接近时是数值稳定的，但是当 v1 与 -v2 比较接近时，
	 * 由于解并不唯一，所以数值不稳定，然而这种情况在实践中不太可能出现。
	 * <br>注意：向量 v1 和 v2 需要预先规范化的，如果不能确定，可以这样调用：
	 * rotateArc(v1.normalize(), v2.normalize())。
	 */
	public static Quaternion rotateArc(Vector v1, Vector v2) {
		//算法：Game Programming Gems 1 的 2.10 章节
		Quaternion q = GeometryPools.acquireRawQuaternion();
		Vector n = v1.cross(v2);
		float d = v1.dot(v2);
		float s = (float) Math.sqrt((1 + d) * 2);
		float rs = 1 / s;
		q.x = n.x * rs;
		q.y = n.y * rs;
		q.z = n.z * rs;
		q.w = s * 0.5f;
		return q;
	}
}
