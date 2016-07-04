package com.graphics.engine.gl.math3d;

/**
 * 
 * <br>类描述: 4X4 矩阵类
 * <br>功能详细描述: 
 * <br><em>使用前先看{@link GeometryPools}</em>
 * <br>矩阵左上角的3X3部分，代表旋转和缩放，因此setRotation*和setScale方法会互相干扰；
 * 而右边3X1部分，代表平移。
 * <p>概念“模型视图矩阵”，是指最后一行为[0 0 0 1]的矩阵。一般来说我们用到的矩阵都是这种，
 * 而投影矩阵，还有模型视图矩阵的转置矩阵{@link #transpose()}以及逆转置矩阵
 * {@link #invertTranspose()}则不是。
 * </p>
 * 
 * @author  dengweiming
 * @date  [2013-7-9]
 */
public class Matrix {
	private static final int M00 = 0;
	private static final int M10 = 1;
	private static final int M20 = 2;
	private static final int M30 = 3;
	private static final int M01 = 4;
	private static final int M11 = 5;
	private static final int M21 = 6;
	private static final int M31 = 7;
	private static final int M02 = 8;
	private static final int M12 = 9;
	private static final int M22 = 10;
	private static final int M32 = 11;
	private static final int M03 = 12;
	private static final int M13 = 13;
	private static final int M23 = 14;
	private static final int M33 = 15;
	private static final int MC = 16;
	
	private static final int VTX = 0;
	private static final int VTY = 1;
	private static final int VTZ = 2;
	private static final int VTW = 3;
	private static final int SIZE = 4;
	private static final int SIZE2 = SIZE * 2;
	private static final int SIZE3 = SIZE * 3;
	private static final float[] TMP_VECTOR = new float[SIZE];
	private static final Matrix ROTATE_MATRIX = new Matrix();
	private static final float[] RES_VECTOR = new float[SIZE];
	
	private final static float GIMBAL_LOCK_ANGLE = 90;
	
	float[] m;	//矩阵元素按列优先存储在一维数组m中
	
	/**
	 * 创建一个单位矩阵
	 */
	public Matrix() {
		m = new float[MC];
		m[M00] = m[M11] = m[M22] = m[M33] = 1;   
	}
	
	/** raw constructor */
	Matrix(int x) {
		m = new float[MC];
	}
	
	/**
	 * 创建一个矩阵，以一个列优先存储的浮点数组初始化
	 */
	public Matrix(float[] m, int offset) {
		this(0);
		set(m, 0);
	}
	
	/**
	 * 以一个列优先存储的浮点数组重设矩阵
	 */
	public Matrix set(float[] m, int offset) {
		for (int i = 0; i < MC; ++i) {
			this.m[i] = m[offset++]; 
		}
		return this;
	}
	
	/**
	 * 以一个矩阵重设这个矩阵
	 */
	public Matrix set(Matrix matrix) {
		for (int i = 0; i < MC; ++i) {
			m[i] = matrix.m[i];
		}
		return this;
	}
	
	/**
	 * 以这个矩阵重设其他矩阵
	 */
	public Matrix setTo(Matrix matrix) {
		for (int i = 0; i < MC; ++i) {
			matrix.m[i] = m[i];
		}
		return matrix;
	}
	
	@Override
	public String toString() {
		String formatter = "%.6f \t\t %.6f \t\t %.6f \t\t %.6f\n";
		String res = "matrix {\n";
		for (int i = 0; i < 4; ++i) {
			res += String.format(formatter, m[i], m[i + 4], m[i + 8], m[i + 12]);
		}
		return res + "}";
	}
	
	/**
	 * 反序列化
	 * @param src 源数据数组
	 * @param offset 要读的数据在<var>src</var>中的开始索引
	 * @return 新的<var>offset</var>值，用于下一个调用
	 * @see {@link #toArray(float[], int)}
	 */
	public int fromArray(float[] src, int offset) {
		for (int i = 0; i < MC; ++i) {
			m[i] = src[offset++]; 
		}
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
		for (int i = 0; i < MC; ++i) {
			dst[offset++] = m[i]; 
		}
		return offset;
	}
	
	/**
	 * 重设为零矩阵
	 */
	Matrix zero() {
		for (int i = 0; i < MC; ++i) {
			m[i] = 0;
		}
		return this;
	}
	
	/**
	 * 重置为单位矩阵。修改矩阵本身内容。
	 */
	public Matrix identity() {
		for (int i = 0; i < MC; ++i) {
			m[i] = 0;
		}
		m[M00] = m[M11] = m[M22] = m[M33] = 1;
        return this;
	}
	
	/**
	 * 获取转置矩阵
	 * <br>注意：得到的矩阵可能不为模型视图矩阵了，应该避免再使用{@link #invert()}。
	 */
	public Matrix transpose() {
		Matrix res = GeometryPools.acquireRawMatrix();
		final float[] dst = res.m; 
        for (int r = 0, i = 0; r < SIZE; ++r) {
            dst[r] = m[i++];
            dst[r + SIZE] = m[i++];
            dst[r + SIZE2] = m[i++];
            dst[r + SIZE3] = m[i++];
        }
        return res;
	}
	
	/**
	 * <br>功能简述: 获取逆矩阵
	 * <br>功能详细描述:
	 * <br>注意: 
	 * <br><em>对非模型视图矩阵，计算结果会不正确。</em>
	 * <br>要逆变换单个对象，使用这些对象的transform(matrix.invert())，
	 * 如果矩阵本身没有缩放操作，那么可以使用这些对象的inverseRotateAndTranslate(matrix)。
	 * <br>因为计算量比较大，因此在局部范围内应该避免多次获取。
	 * <br>如果要对多个点或向量作逆变换，先计算一次逆矩阵，再使用这些对象的transform(invertMatrix)比较快。
	 * @return
	 */
	public Matrix invert() {
		return invert(false);
	}
	
	/**
	 * 获取逆转置矩阵
	 * <br>功能详细描述: 用于对法向量和平面进行（从局部坐标系到世界坐标系，仍是正向的）变换。
	 * <br>注意：
	 * <br>因为计算量比较大，因此在局部范围内应该避免多次获取。
	 * <br>得到的矩阵可能不为模型视图矩阵了，应该避免再使用{@link #invert()}。
	 * @see <a href="http://www.lighthouse3d.com/tutorials/glsl-tutorial/the-normal-matrix"/>The Normal Matrix</a>
	 */
	public Matrix invertTranspose() {
		return invert(true);
	}
	
	private Matrix invert(boolean transpose) {
		//假定了矩阵最后一行为[0 0 0 1]，即非投影矩阵，这对于模型视图矩阵是满足的
//		assert(m[M30] == 0 && m[M31] == 0 && m[M32] == 0 && m[M33] == 1);
		
        //计算3x3子矩阵N的代数余子式矩阵
    	float a00 = m[M11] * m[M22] - m[M12] * m[M21];
    	float a01 = m[M12] * m[M20] - m[M10] * m[M22];	//-
    	float a02 = m[M10] * m[M21] - m[M11] * m[M20];
    	
    	float det = m[M00] * a00 + m[M01] * a01 + m[M02] * a02;	//行列式
		if (det == 0) {
			det = 1;
		}
		float rDet = 1 / det;
		
		float a10 = m[M02] * m[M21] - m[M01] * m[M22];	//-
		float a11 = m[M00] * m[M22] - m[M02] * m[M20];
		float a12 = m[M01] * m[M20] - m[M00] * m[M21];	//-
		
		float a20 = m[M01] * m[M12] - m[M02] * m[M11];
		float a21 = m[M02] * m[M10] - m[M00] * m[M12];	//-
		float a22 = m[M00] * m[M11] - m[M01] * m[M10];
		
		//代数余子式矩阵的转置（即伴随矩阵）乘以行列式的倒数，就得到3x3子矩阵N的逆矩阵
		//因为矩阵M=TN，所以M的逆等于N的逆乘以T的逆，乘得的平移量为(tx, ty, tz)
		float tx = -(a00 * m[M03] + a10 * m[M13] + a20 * m[M23]); 
		float ty = -(a01 * m[M03] + a11 * m[M13] + a21 * m[M23]); 
		float tz = -(a02 * m[M03] + a12 * m[M13] + a22 * m[M23]); 
		
		Matrix res = GeometryPools.acquireRawMatrix();
		final float[] dst = res.m;
		if (!transpose) {
			dst[M00] = a00 * rDet;
			dst[M10] = a01 * rDet;
			dst[M20] = a02 * rDet;
			dst[M30] = 0;
			dst[M01] = a10 * rDet;
			dst[M11] = a11 * rDet;
			dst[M21] = a12 * rDet;
			dst[M31] = 0;
			dst[M02] = a20 * rDet;
			dst[M12] = a21 * rDet;
			dst[M22] = a22 * rDet;
			dst[M32] = 0;
			dst[M03] = tx * rDet;
			dst[M13] = ty * rDet;
			dst[M23] = tz * rDet;
			dst[M33] = 1;
		} else {
			dst[M00] = a00 * rDet;
			dst[M10] = a10 * rDet;
			dst[M20] = a20 * rDet;
			dst[M30] = tx * rDet;
			dst[M01] = a01 * rDet;
			dst[M11] = a11 * rDet;
			dst[M21] = a21 * rDet;
			dst[M31] = ty * rDet;
			dst[M02] = a02 * rDet;
			dst[M12] = a12 * rDet;
			dst[M22] = a22 * rDet;
			dst[M32] = tz * rDet;
			dst[M03] = 0;
			dst[M13] = 0;
			dst[M23] = 0;
			dst[M33] = 1;
		}
		return res;
	}
	
	/**
	 * 右乘一个矩阵
	 */
	public Matrix mul(Matrix matrix) {
		Matrix res = GeometryPools.acquireZeroMatrix();
		final float[] mb = matrix.m;
		final float[] mc = res.m;
		//http://stackoverflow.com/questions/7395556/why-does-the-order-of-loops-in-a-matrix-multiply-algorithm-affect-performance
//		for (int c = 0; c < SIZE; ++c) {
//			for (int k = 0; k < SIZE; ++k) {
//				float x = mb[k][c];
//				for (int r = 0; r < SIZE; ++r) {
//					mc[r][c] += m[r][k] * x;
//				}
//			}
//		}
		for (int ib = 0; ib < MC; ++ib) {
			final float x = mb[ib];
			//由 ia = k * 4 + r, ib = c * 4 + k, ic = c * 4 + r 得
			//ia = ib % 4 * 4 + r， ic = ib - ib % 4 + r
			int ia = (ib & 3) << 2, ic = ib & 12;	//CHECKSTYLE IGNORE
			for (int r = 0; r < SIZE; ++r) {
				mc[ic++] += m[ia++] * x;
			}
		}
		
		return res;
	}
	
	/**
	 * 设置矩阵的平移量。2D版本，注意Y轴向下。
	 */
	public Matrix setTranslation(float x, float y) {
		return setTranslation(x, -y, 0);
	}
	
	/**
	 * 设置矩阵的平移量。
	 */
	public Matrix setTranslation(float x, float y, float z) {
		m[M03] = x;
		m[M13] = y;
		m[M23] = z;
//		m[M33] = 1; 
		return this;
	}
	
	/**
	 * 右乘一个平移矩阵。2D版本，注意Y轴向下。
	 */
	public Matrix translate(float x, float y) {
		return translate(x, -y, 0);
	}
	
	/**
	 * 右乘一个平移矩阵
	 */
	public Matrix translate(float x, float y, float z) {
		Matrix res = GeometryPools.acquireRawMatrix().set(this);
		res.m[M03] += m[M00] * x + m[M01] * y + m[M02] * z;
		res.m[M13] += m[M10] * x + m[M11] * y + m[M12] * z;
		res.m[M23] += m[M20] * x + m[M21] * y + m[M22] * z;
//		res.m[M33] += m[M30] * x + m[M31] * y + m[M32] * z;
		return res;
	}
	
	/**
	 * 设置矩阵的旋转部分。
	 * @param a 绕Z轴顺时针旋转的角度
	 */
	public Matrix setRotation(float a) {
		return setRotationAxisAngle(a, 0, 0, 1);
	}
	
	/**
	 * <br>功能简述: 设置矩阵的旋转部分，以旋转轴和旋转角度
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param a 绕旋转轴逆时针旋转的角度
	 * @param x 旋转轴X分量
	 * @param y 旋转轴Y分量
	 * @param z 旋转轴Z分量
	 * @return
	 */
	public Matrix setRotationAxisAngle(float a, float x, float y, float z) {
		a *= Math3D.TO_RADIAN;
		float s = (float) Math.sin(a);
		float c = (float) Math.cos(a);
		if (1 == x && 0 == y && 0 == z) {
			m[M00] = 1;		m[M01] = 0;		m[M02] = 0;
			m[M10] = 0;		m[M11] = c;		m[M12] = -s;
			m[M20] = 0;		m[M21] = s;		m[M22] = c;    
		} else if (0 == x && 1 == y && 0 == z) {
			m[M00] = c;		m[M01] = 0;		m[M02] = s;
			m[M10] = 0;		m[M11] = 1;		m[M12] = 0;
			m[M20] = -s;	m[M21] = 0;		m[M22] = c;    
		} else if (0 == x && 0 == y && 1 == z) {
			m[M00] = c;		m[M01] = -s;	m[M02] = 0;
			m[M10] = s;		m[M11] = c;		m[M12] = 0;
			m[M20] = 0;		m[M21] = 0;		m[M22] = 1;    
		} else {
			float sqLen = x * x + y * y + z * z;
			if (1.0f != sqLen) {
				float rLen = Math3D.invSqrt(sqLen);
				x *= rLen;
				y *= rLen;
				z *= rLen;
			}
			float nc = 1 - c;
			float xy = x * y;
			float yz = y * z;
			float zx = z * x;
			float xs = x * s;
			float ys = y * s;
			float zs = z * s;
			
			m[M00] = x * x * nc + c;	m[M01] = xy * nc - zs;		m[M02] = zx * nc + ys;
			m[M10] = xy * nc + zs;		m[M11] = y * y * nc + c;	m[M12] = yz * nc - xs;
			m[M20] = zx * nc - ys;		m[M21] = yz * nc + xs;		m[M22] = z * z * nc + c;  
		}
//		m[M33] = 1; 
		return this;
	}
	
	/**
	 * <br>功能简述: 设置矩阵的旋转部分，以欧拉角
	 * <br>功能详细描述:
	 * 相当于 rotateX(x), rotateY(y), rotateZ(z) 这样三个旋转的序列
	 * <br>注意:
	 * @param x 绕X轴旋转的角度
	 * @param y 绕Y轴旋转的角度
	 * @param z 绕Z轴旋转的角度
	 * @return
	 */
	public Matrix setRotationEuler(float x, float y, float z) {
		x *= Math3D.TO_RADIAN;
		y *= Math3D.TO_RADIAN;
		z *= Math3D.TO_RADIAN;
		float cx = (float) Math.cos(x);
		float sx = (float) Math.sin(x);
		float cy = (float) Math.cos(y);
		float sy = (float) Math.sin(y);
		float cz = (float) Math.cos(z);
		float sz = (float) Math.sin(z);
		float cxsy = cx * sy;
		float sxsy = sx * sy;

		m[M00] = cy * cz;				m[M01] = -cy * sz;				m[M02] = sy;      
		m[M10] = sxsy * cz + cx * sz;	m[M11] = -sxsy * sz + cx * cz;	m[M12] = -sx * cy;
		m[M20] = -cxsy * cz + sx * sz;	m[M21] = cxsy * sz + sx * cz;	m[M22] = cx * cy; 
//		m[M33] = 1; 
		return this;
	}
	
	/**
	 * 设置矩阵的旋转部分，以一个四元数{@link Quaternion}
	 */
	public Matrix setRotationQuaternion(Quaternion q) {
		float xx2 = 2 * q.x * q.x;
		float xy2 = 2 * q.x * q.y;
		float xz2 = 2 * q.x * q.z;
		float yy2 = 2 * q.y * q.y;
		float yz2 = 2 * q.y * q.z;
		float zz2 = 2 * q.z * q.z;
		float wx2 = 2 * q.w * q.x;
		float wy2 = 2 * q.w * q.y;
		float wz2 = 2 * q.w * q.z;
		m[M00] = 1 - yy2 - zz2;	m[M01] = xy2 - wz2;		m[M02] = xz2 + wy2;
		m[M10] = xy2 + wz2;		m[M11] = 1 - xx2 - zz2;	m[M12] = yz2 - wx2;
		m[M20] = xz2 -  wy2;	m[M21] = yz2 + wx2;		m[M22] = 1 - xx2 - yy2;
//    	m[M33] = 1;
		return this;
	}
	
	/**
	 * <br>功能简述: 右乘一个旋转矩阵
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param a 绕Z轴顺时针旋转的角度
	 * @return
	 */
	public Matrix rotate(float a) {
		return rotateAxisAngle(-a, 0, 0, 1);
	}
	
	/**
	 * <br>功能简述: 右乘一个旋转矩阵，以旋转轴和旋转角
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param a 绕旋转轴逆时针旋转的角度
	 * @param x 旋转轴X分量
	 * @param y 旋转轴Y分量
	 * @param z 旋转轴Z分量
	 */
	public Matrix rotateAxisAngle(float a, float x, float y, float z) {
		Matrix mat = ROTATE_MATRIX;
		mat.setRotationAxisAngle(a, x, y, z);
		return mul(mat);
	}
	
	/**
	 * <br>功能简述: 右乘一个旋转矩阵，以欧拉角
	 * <br>功能详细描述:
	 * 相当于 rotateX(x), rotateY(y), rotateZ(z) 这样三个旋转的序列
	 * <br>注意:
	 * @param x 绕X轴旋转的角度
	 * @param y 绕Y轴旋转的角度
	 * @param z 绕Z轴旋转的角度
	 * @return
	 * @return
	 */
	public Matrix rotateEuler(float x, float y, float z) {
		Matrix mat = ROTATE_MATRIX;
		mat.setRotationEuler(x, y, z);
		return mul(mat);
	}
	
	/**
	 * <br>功能简述: 右乘一个旋转矩阵，以四元数
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param q
	 * @return
	 */
	public Matrix rotateQuaternion(Quaternion q) {
		Matrix mat = ROTATE_MATRIX;
		mat.setRotationQuaternion(q);
		return mul(mat);
	}
	
	/**
	 * <br>功能简述: 设置矩阵的缩放部分
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param x X轴的缩放比例
	 * @param y Y轴的缩放比例
	 * @return
	 */
	public Matrix setScale(float x, float y) {
		return setScale(x, y, 1);
	}
	
	/**
	 * <br>功能简述: 设置矩阵的缩放部分
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param x X轴的缩放比例
	 * @param y Y轴的缩放比例
	 * @param z Z轴的缩放比例
	 * @return
	 */
	public Matrix setScale(float x, float y, float z) {
    	m[M00] = x;		m[M01] = 0;		m[M02] = 0;
    	m[M10] = 0;		m[M11] = y;		m[M12] = 0;
    	m[M20] = 0;		m[M21] = 0;		m[M22] = z;
//    	m[M33] = 1;
		return this;
	}
	
	/**
	 * <br>功能简述: 右乘一个缩放矩阵
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param x X轴的缩放比例
	 * @param y Y轴的缩放比例
	 * @return
	 */
	public Matrix scale(float x, float y) {
		return scale(x, y, 1);
	}
	
	/**
	 * <br>功能简述: 右乘一个缩放矩阵
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param x X轴的缩放比例
	 * @param y Y轴的缩放比例
	 * @param z Z轴的缩放比例
	 * @return
	 */
	public Matrix scale(float x, float y, float z) {
		Matrix res = GeometryPools.acquireRawMatrix();
		float[] dst = res.m;
		float[] s = TMP_VECTOR;
		s[Math3D.VTX] = x;
		s[Math3D.VTY] = y;
		s[Math3D.VTZ] = z;
		s[Math3D.VTW] = 1;
		for (int i = 0; i < MC; ++i) {
			dst[i] = m[i] * s[i >> 2];
		}
		return res;
	}
	
	/**
	 * 对一个齐次向量作变换
	 * @param vector4 如果是点，w分量为1；如果是向量，w分量是0；如果是平面，w分量可以为其他。
	 * 另外，如果是法向量或者平面，应该使用逆转置矩阵 {@link #invertTranspose()} 变换到世界坐标系。
	 * @param offset
	 * @return 返回值存在临时数组中，需要尽快取出，以免被后续调用覆写了
	 */
	public float[] transform(float[] vector4, int offset) {
		float[] res = RES_VECTOR;
		for (int i = 0; i < SIZE; ++i) {
			res[i] = m[i] * vector4[offset + VTX] + m[i + SIZE] * vector4[offset + VTY]
					+ m[i + SIZE2] * vector4[offset + VTZ] + m[i + SIZE3] * vector4[offset + VTW];
		}
		return res;
	}
	
	/**
	 * 对一个齐次向量作逆变换（假设没有缩放变换）
	 * @param vector4 如果是点，w分量为1；如果是向量，w分量是0；如果是平面，w分量可以为其他。
	 * 对于法向量和平面，应该很少需要本方法，其结果未验证。
	 * @param offset
	 * @return 返回值存在临时数组中，需要尽快取出，以免被后续调用覆写了
	 * @see {@link #transform(float[])}
	 * @see {@link #inverseTRS(float[], int)}
	 */
	public float[] inverseRotateAndTranslate(float[] vector4, int offset) {
		float[] res = RES_VECTOR;
		float w = vector4[offset + VTW];
		float x = vector4[offset + VTX] - m[M03] * w;
		float y = vector4[offset + VTY] - m[M13] * w;
		float z = vector4[offset + VTZ] - m[M23] * w;
		res[VTX] = m[M00] * x + m[M10] * y + m[M20] * z;
		res[VTY] = m[M01] * x + m[M11] * y + m[M21] * z;
		res[VTZ] = m[M02] * x + m[M12] * y + m[M22] * z;
		res[VTW] = w;
		return res;
	}
	
	/**
	 * 对一个齐次向量作逆变换（假设缩放变换右边没有旋转变换，可以有其他缩放变换和平移变换）
	 * @param vector4 如果是点，w分量为1；如果是向量，w分量是0；如果是平面，w分量可以为其他。
	 * 对于法向量和平面，应该很少需要本方法，其结果未验证。
	 * @param offset
	 * @return 返回值存在临时数组中，需要尽快取出，以免被后续调用覆写了
	 * @see {@link #transform(float[])}
	 * @see {@link #inverseRotateAndTranslate(float[], int)}
	 */
	public float[] inverseTRS(float[] vector4, int offset) {
		float[] res = RES_VECTOR;
		float w = vector4[offset + VTW];
		float x = vector4[offset + VTX] - m[M03] * w;
		float y = vector4[offset + VTY] - m[M13] * w;
		float z = vector4[offset + VTZ] - m[M23] * w;
		float xx = m[M00] * m[M00] + m[M10] * m[M10] + m[M20] * m[M20];     
		float yy = m[M01] * m[M01] + m[M11] * m[M11] + m[M21] * m[M21];     
		float zz = m[M02] * m[M02] + m[M12] * m[M12] + m[M22] * m[M22];
		res[VTX] = (m[M00] * x + m[M10] * y + m[M20] * z) / xx;
		res[VTY] = (m[M01] * x + m[M11] * y + m[M21] * z) / yy;
		res[VTZ] = (m[M02] * x + m[M12] * y + m[M22] * z) / zz;
		res[VTW] = w;
		return res;
	}
	
	public float[] getValues() {
		return m;
	}
	
	/**
	 * 获取平移量
	 * @return 返回值存在临时数组中，需要尽快取出，以免被后续调用覆写了
	 */
	public float[] getTranslation() {
		float[] res = RES_VECTOR;
		res[VTX] = m[M03];
		res[VTY] = m[M13];
		res[VTZ] = m[M23];
		return res;
	}
	
	public float[] getRotationAxisAngle() {
		float[] res = RES_VECTOR;
		Math3D.todo();	//TODO
		return res;
	}
	
	/**
	 * 获取旋转部分，作为欧拉角。
	 * <br>假定3x3子矩阵是旋转矩阵（规范正交矩阵，也即不包含缩放变换）。
	 * @return 返回值存在临时数组中，需要尽快取出，以免被后续调用覆写了
	 * @see {@link #setRotationEuler(float, float, float)}
	 */
	public float[] getRotationEuler() {
		//推导方法参考《3D数学基础：图形与游戏开发》10.6.2章节，只是它的矩阵是行矩阵，欧拉角是y-x-z顺序的。
		float[] euler = RES_VECTOR;
		float m02 = m[M02];
		if (m02 < Math3D.EPSILON) {
			float m21 = m[M21];
			float m11 = m[M11];
			euler[0] = (float) Math.toDegrees(Math.atan2(m21, m11));
			euler[1] = -GIMBAL_LOCK_ANGLE;
			euler[2] = 0;
		} else if (m02 > 1 - Math3D.EPSILON) {
			float m21 = m[M21];
			float m11 = m[M11];
			euler[0] = (float) Math.toDegrees(Math.atan2(m21, m11));
			euler[1] = GIMBAL_LOCK_ANGLE;
			euler[2] = 0;
		} else {
			float m12 = m[M12];
			float m22 = m[M22];
			euler[0] = (float) Math.toDegrees(Math.atan2(-m12, m22));
			euler[1] = (float) Math.toDegrees(Math.asin(m02));
			float m01 = m[M01];
			float m00 = m[M00];
			euler[2] = (float) Math.toDegrees(Math.atan2(-m01, m00));
		}
		return euler;
	}
	
	/**
	 * 获取旋转部分，作为四元数。
	 * <br>假定3x3子矩阵是旋转矩阵（规范正交矩阵，也即不包含缩放变换）。
	 */
	public Quaternion getRotationQuaternion() {
		Quaternion q = GeometryPools.acquireQuaternion();
		getRotationQuaternion(q);
		return q;
	}
	
	void getRotationQuaternion(Quaternion q) {
		/*
		 * 从 setRotationQuaternion 方法生成的矩阵中求解四元数，其中
		 * M10 + M01 = 4xy
		 * M21 + M12 = 4yz
		 * M02 + M20 = 4xz
		 * M21 - M12 = 4wx
		 * M02 - M20 = 4wy
		 * M10 - M01 = 4wz
		 */
		float tr = m[M00] + m[M11] + m[M22] + 1; 	//矩阵的迹，等于 4ww （假定 m[M33]==1）
		if (tr > Math3D.EPSILON) {
			float s = Math3D.invSqrt(tr) * 0.5f; 	// 1/4w
			q.x = (m[M21] - m[M12]) * s;
			q.y = (m[M02] - m[M20]) * s;
			q.z = (m[M10] - m[M01]) * s;
			q.w = tr * s;
		} else if (m[M00] > m[M11] && m[M00] > m[M22]) {
			// M00 > M11 => 1 - 2yy - 2zz > 1 - 2xx - 2zz => xx > yy
			// 选取绝对值最大的分量求解，这里是 x 最大
			float t = m[M00] - m[M11] - m[M22] + 1; // 4xx
			float s = Math3D.invSqrt(t) * 0.5f; 	// 1/4x
			q.x = t * s;
			q.y = (m[M10] + m[M01]) * s;
			q.z = (m[M02] + m[M20]) * s;
			q.w = (m[M21] - m[M12]) * s;
		} else if (m[M11] > m[M22]) {
			float t = m[M11] - m[M00] - m[M22] + 1; // 4yy
			float s = Math3D.invSqrt(t) * 0.5f; 	// 1/4y
			q.x = (m[M10] + m[M01]) * s;
			q.y = t * s;
			q.z = (m[M21] + m[M12]) * s;
			q.w = (m[M02] - m[M20]) * s;
		} else {
			float t = m[M22] - m[M00] - m[M11] + 1; // 4zz
			float s = Math3D.invSqrt(t) * 0.5f; 	// 1/4z
			q.x = (m[M02] + m[M20]) * s;
			q.y = (m[M21] + m[M12]) * s;
			q.z = t * s;
			q.w = (m[M10] - m[M01]) * s;
		}
	}
}