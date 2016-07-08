package com.graphics.engine.animation;

import java.io.PrintWriter;

import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.util.Vector3f;

import android.graphics.RectF;
import android.opengl.Matrix;


/**
 * Defines the transformation to be applied at
 * one point in time of an Animation.
 *
 */
public class Transformation3D {
	//CHECKSTYLE:OFF
    /**
     * Indicates a transformation that has no effect (alpha = 1 and identity matrix.)
     */
    public static int TYPE_IDENTITY = 0x0;
    /**
     * Indicates a transformation that applies an alpha only (uses an identity matrix.)
     */
    public static int TYPE_ALPHA = 0x1;
    /**
     * Indicates a transformation that applies a matrix only (alpha = 1.)
     */
    public static int TYPE_MATRIX = 0x2;
    /**
     * Indicates a transformation that applies an alpha and a matrix.
     */
    public static int TYPE_BOTH = TYPE_ALPHA | TYPE_MATRIX;
	//CHECKSTYLE:ON

    private static final int MATRIX_SIZE = 16;
    
	protected static float[] sTmpMatrix = new float[MATRIX_SIZE];

    protected float[] mMatrix;
    protected float mAlpha;
    protected int mTransformationType;
    private com.graphics.engine.math3d.Matrix mMatrix3D;
    
    //矩阵元素按列优先存储
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
    
    private static final int VX = 0;
    private static final int VY = 1;
    private static final int VZ = 2;
    private static final int VW = 3;

    /**
     * Creates a new transformation with alpha = 1 and the identity matrix.
     */
    public Transformation3D() {
    	mMatrix3D = new com.graphics.engine.math3d.Matrix();
    	mMatrix = mMatrix3D.getValues();
//    	clear();
		mAlpha = 1.0f;
		mTransformationType = TYPE_BOTH;
    }

    /**
     * Reset the transformation to a state that leaves the object
     * being animated in an unmodified state. The transformation type is
     * {@link #TYPE_BOTH} by default.
     */
	public Transformation3D clear() {
		Matrix.setIdentityM(mMatrix, 0);
		mAlpha = 1.0f;
		mTransformationType = TYPE_BOTH;
		return this;
	}

    /**
     * Indicates the nature of this transformation.
     *
     * @return {@link #TYPE_ALPHA}, {@link #TYPE_MATRIX},
     *         {@link #TYPE_BOTH} or {@link #TYPE_IDENTITY}.
     */
    public int getTransformationType() {
        return mTransformationType;
    }

    /**
     * Sets the transformation type.
     *
     * @param transformationType One of {@link #TYPE_ALPHA},
     *        {@link #TYPE_MATRIX}, {@link #TYPE_BOTH} or
     *        {@link #TYPE_IDENTITY}.
     */
    public void setTransformationType(int transformationType) {
        mTransformationType = transformationType;
    }
    
    /**
     * 
     * @param matrix
     * @param offset
     */
    public void set(float[] matrix, int offset) {
    	System.arraycopy(matrix, offset, mMatrix, 0, MATRIX_SIZE);
    }

    /**
     * Clones the specified transformation.
     *
     * @param t The transformation to clone.
     */
    public void set(Transformation3D t) {
        mAlpha = t.getAlpha();
        final float[] matrix = t.getMatrix();
        System.arraycopy(matrix, 0, mMatrix, 0, MATRIX_SIZE);
        mTransformationType = t.getTransformationType();
    }
    
    /**
     * Apply this Transformation to an existing Transformation, e.g. apply
     * a scale effect to something that has already been rotated.
     * 注意当 t 和调用者本身是同一个 Transformation3D 时，结果未定义（即不保证正确）。
     * @param t
     */
    public void compose(Transformation3D t) {
        mAlpha *= t.getAlpha();
        System.arraycopy(mMatrix, 0, sTmpMatrix, 0, MATRIX_SIZE);
        Matrix.multiplyMM(mMatrix, 0, sTmpMatrix, 0, t.getMatrix(), 0);
    }
    
    /**
     * <br>功能简述: 求矩阵的逆
     * <br>功能详细描述:
     * <br>注意:
     * @param t 结果矩阵，可以和this相同
     */
    public void invert(Transformation3D t) {
    	invert(mMatrix, 0, t.mMatrix, 0);
    }
    
    /**
     * @return The 4*4 Matrix representing the transformation to apply to the
     * coordinates of the object being animated
     */
    public float[] getMatrix() {
        return mMatrix;
    }
    
    public com.graphics.engine.math3d.Matrix getMatrix3D() {
    	return mMatrix3D;
    }
    
    /**
     * Sets the degree of transparency
     * @param alpha 1.0 means fully opaqe and 0.0 means fully transparent
     */
    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }

    /**
     * @return The degree of transparency
     */
    public float getAlpha() {
        return mAlpha;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);	// CHECKSTYLE IGNORE
        sb.append("Transformation3D");
        toShortString(sb);
        return sb.toString();
    }
    
    /**
     * Return a string representation of the transformation in a compact form.
     */
    public String toShortString() {
        StringBuilder sb = new StringBuilder(128);	// CHECKSTYLE IGNORE
        toShortString(sb);
        return sb.toString();
    }
    
    /**
     * @hide
     */
    public void toShortString(StringBuilder sb) {
        sb.append("{alpha=");
        sb.append(mAlpha);
        sb.append(" matrix=");
        sb.append('[');
        sb.append(mMatrix[M00]); sb.append(", "); sb.append(mMatrix[M01]); sb.append(", ");
        sb.append(mMatrix[M02]); sb.append(", "); sb.append(mMatrix[M03]); sb.append("][");
        sb.append(mMatrix[M10]); sb.append(", "); sb.append(mMatrix[M11]); sb.append(", ");
        sb.append(mMatrix[M12]); sb.append(", "); sb.append(mMatrix[M13]); sb.append("][");
        sb.append(mMatrix[M20]); sb.append(", "); sb.append(mMatrix[M21]); sb.append(", ");
        sb.append(mMatrix[M22]); sb.append(", "); sb.append(mMatrix[M23]); sb.append(']');
        sb.append('}');
    }
    
    /**
     * Print short string, to optimize dumping.
     * @hide
     */
    public void printShortString(PrintWriter pw) {
        pw.print("{alpha=");
        pw.print(mAlpha);
        pw.print(" matrix=");
        //TODO
//        mMatrix.printShortString(pw);
        pw.print('}');
    }
    
    /**
     * 2D平移，X轴朝右，Y轴朝下 
     * Set the matrix to translate by (dx, dy). 
     * */
    public void setTranslate(float dx, float dy) {
    	// CHECKSTYLE IGNORE 2 LINES
    	mMatrix[12] = dx;
    	mMatrix[13] = -dy;
    }
    
    /**
     * 3D平移，X轴朝右，Y轴朝上，Z轴朝屏幕外 
     * Set the matrix to translate by (dx, dy). 
     * */
    public void setTranslate(float dx, float dy, float dz) {
    	// CHECKSTYLE IGNORE 3 LINES
    	mMatrix[12] = dx;
    	mMatrix[13] = dy;
    	mMatrix[14] = dz;
    }

    /**
     * Set the matrix to scale by sx and sy, with a pivot point at (px, py).
     * The pivot point is the coordinate that should remain unchanged by the
     * specified transformation.
     */
    public void setScale(float sx, float sy, float px, float py) {
    	// CHECKSTYLE IGNORE 4 LINES
    	mMatrix[0] = sx;
    	mMatrix[5] = sy;
    	mMatrix[12] = px - px * sx;
    	mMatrix[13] = sy * py - py;
    }

    /** Set the matrix to scale by sx and sy. */
    public void setScale(float sx, float sy) {
    	// CHECKSTYLE IGNORE 2 LINES
    	mMatrix[0] = sx;
    	mMatrix[5] = sy;
    }
    
    /** Set the matrix to scale by sx, sy and sz. */
    public void setScale(float sx, float sy, float sz) {
    	// CHECKSTYLE IGNORE 3 LINES
    	mMatrix[0] = sx;
    	mMatrix[5] = sy;
    	mMatrix[10] = sz;
    }
    
    /** Set the matrix to scale by sx, sy and sz. */
    public void setScale(float sx, float sy, float sz, float px, float py, float pz) {
    	// CHECKSTYLE IGNORE 3 LINES
    	mMatrix[0] = sx;
    	mMatrix[5] = sy;
    	mMatrix[10] = sz;
    	
    	//preTranslate	    	// CHECKSTYLE IGNORE 3 LINES
    	mMatrix[12] += px;
    	mMatrix[13] += py;
    	mMatrix[14] += pz;
    	
    	//after Translate
    	Matrix.translateM(mMatrix, 0, -px, -py, -pz);
    }

    /**
     * 绕Z轴顺时针旋转
     * Set the matrix to rotate by the specified number of degrees, with a pivot
     * point at (px, py). The pivot point is the coordinate that should remain
     * unchanged by the specified transformation.
     */
    public void setRotate(float degrees, float px, float py) {
    	degrees = -(float) Math.toRadians(degrees);
        float s = (float) Math.sin(degrees);
        float c = (float) Math.cos(degrees);
    	// CHECKSTYLE IGNORE 6 LINES
        mMatrix[0] = c;
        mMatrix[1] = s;
        mMatrix[4] = -s;
        mMatrix[5] = c;
        mMatrix[12] = px - c * px - s * py;
        mMatrix[13] = c * py - py - s * px;
    }

    /**
     * 绕Z轴顺时针旋转
     * Set the matrix to rotate about (0,0) by the specified number of degrees.
     */
    public void setRotate(float degrees) {
    	degrees = -(float) Math.toRadians(degrees);
        float s = (float) Math.sin(degrees);
        float c = (float) Math.cos(degrees);
    	// CHECKSTYLE IGNORE 4 LINES
        mMatrix[0] = c;
        mMatrix[1] = s;
        mMatrix[4] = -s;
        mMatrix[5] = c;
    }
    
    /**
     * 根据轴-角对旋转，旋转轴为（<var>ax</var>, <var>ay</var>, <var>az</var>），
     * <var>degrees</var>为正值则逆时针旋转
     * 
     * @param degrees The amount to rotate, in degrees, counter-clockwise if positive
     * @param ax 旋转轴的x分量
     * @param ay 旋转轴的y分量
     * @param az 旋转轴的z分量
     */
    public void setRotateAxisAngle(float degrees, float ax, float ay, float az) {
    	Matrix.setRotateM(mMatrix, 0, degrees, ax, ay, az);
    }
    
    /**
     * @param degrees
     * @param ax
     * @param ay
     * @param az
     */
    public void setRotateAxisAngle(float degrees, float ax, float ay, float az, float px, float py, float pz) {
    	Matrix.setRotateM(mMatrix, 0, degrees, ax, ay, az);
    	//preTranslate	    	// CHECKSTYLE IGNORE 3 LINES
    	mMatrix[12] += px;
    	mMatrix[13] += py;
    	mMatrix[14] += pz;
    	
    	//after Translate
    	Matrix.translateM(mMatrix, 0, -px, -py, -pz);
    }
    
    /**
     * 根据欧拉角旋转
     * @param degreesX The amount to rotate about X-axis, in degrees, counter-clockwise if positive
     * @param degreesY The amount to rotate about Y-axis, in degrees, counter-clockwise if positive
     * @param degreesZ The amount to rotate about Z-axis, in degrees, counter-clockwise if positive
     */
    public void setRotateEuler(float degreesX, float degreesY, float degreesZ) {
//    	Matrix.setRotateEulerM(mMatrix, 0, degreesX, degreesY, degreesZ);	//SDK这个实现是错的
    	GLCanvas.setRotateEulerM(mMatrix, 0, degreesX, degreesY, degreesZ);
    }
    
    /**
     * Apply this matrix to the src rectangle, and write the transformed
     * rectangle into dst. This is accomplished by transforming the 4 corners of
     * src, and then setting dst to the bounds of those points.
     *
     * @param dst Where the transformed rectangle is written.
     * @param src The original rectangle to be transformed.
     * @return the result of calling rectStaysRect()
     */
    @Deprecated
    public boolean mapRect(RectF dst, RectF src) {
        if (dst == null || src == null) {
            throw new NullPointerException();
        }
        //TODO 将变换作用到矩形后再计算包围盒
        return false;
    }

    /**
     * Apply this matrix to the rectangle, and write the transformed rectangle
     * back into it. This is accomplished by transforming the 4 corners of rect,
     * and then setting it to the bounds of those points
     *
     * @param rect The rectangle to transform.
     * @return the result of calling rectStaysRect()
     */
    @Deprecated
    public boolean mapRect(RectF rect) {
        return mapRect(rect, rect);
    }
    
    /**
     * 计算原点变换后的位置
     * @param v			结果存放的数组，保证长度至少为offset+3
     * @param offset
     */
    public void mapOrigin(float[] v, int offset) {
    	// CHECKSTYLE IGNORE 3 LINES
    	v[offset++] = mMatrix[12];
    	v[offset++] = mMatrix[13];
    	v[offset++] = mMatrix[14];
    }
    
    /**
     * 计算点或者向量变换后的值，也即transform
     * @param u
     * @param offsetU
     * @param v		可以和u相同
     * @param offsetV
     * @param w		为0表示向量，为1表示顶点
     */
    public void mapVector(float[] u, int offsetU, float[] v, int offsetV, int w) {
    	final float x = u[offsetU];
    	final float y = u[offsetU + 1];
    	final float z = u[offsetU + 2];
    	v[offsetV++] = mMatrix[M00] * x + mMatrix[M01] * y + mMatrix[M02] * z + mMatrix[M03] * w;
    	v[offsetV++] = mMatrix[M10] * x + mMatrix[M11] * y + mMatrix[M12] * z + mMatrix[M13] * w;
    	v[offsetV++] = mMatrix[M20] * x + mMatrix[M21] * y + mMatrix[M22] * z + mMatrix[M23] * w;
    }
    
    /**
     * 计算点或者向量变换后的值
     * @param u
     * @param offsetU
     * @param v		可以和u相同
     * @param offsetV
     * @param w		为0表示向量，为1表示顶点
     */
    public void mapVector(float[] u, int offsetU, Vector3f v, int w) {
    	final float x = u[offsetU];
    	final float y = u[offsetU + 1];
    	final float z = u[offsetU + 2];
    	v.x = mMatrix[M00] * x + mMatrix[M01] * y + mMatrix[M02] * z + mMatrix[M03] * w;
    	v.y = mMatrix[M10] * x + mMatrix[M11] * y + mMatrix[M12] * z + mMatrix[M13] * w;
    	v.z = mMatrix[M20] * x + mMatrix[M21] * y + mMatrix[M22] * z + mMatrix[M23] * w;
    }
    
    /**
     * 假设当前矩阵只有旋转和平移，计算点或者向量逆变换的值
     * @param u
     * @param offsetU
     * @param v		可以和u相同
     * @param offsetV
     * @param w		为0表示向量，为1表示顶点
     */
    public void inverseRotateAndTranslateVector(float[] u, int offsetU, float[] v, int offsetV, int w) {
    	final float x = u[offsetU] - mMatrix[M03] * w;
    	final float y = u[offsetU + 1] - mMatrix[M13] * w;
    	final float z = u[offsetU + 2] - mMatrix[M23] * w;
    	v[offsetV++] = mMatrix[M00] * x + mMatrix[M10] * y + mMatrix[M20] * z;
    	v[offsetV++] = mMatrix[M01] * x + mMatrix[M11] * y + mMatrix[M21] * z;
    	v[offsetV++] = mMatrix[M02] * x + mMatrix[M12] * y + mMatrix[M22] * z;
    }
    
    /**
     * <br>功能简述:计算点或者向量逆变换的值(支持缩放)
     * <br>功能详细描述:
     * <br>注意:
     * @param u
     * @param offsetU
     * @param v
     * @param offsetV
     * @param w
     */
    public void inverseTransform(Vector3f u, int offsetU, Vector3f v, int offsetV, int w) {
    	float[] m = new float[16];
    	Matrix.invertM(m, 0, mMatrix, 0);
    	float[] inV = new float[]{
    		u.x,
    		u.y,
    		u.z,
    		w
    	};
    	float[] outV = new float[4];
    	Matrix.multiplyMV(outV, 0, m, 0, inV, 0);
    	v.x = outV[0];
    	v.y = outV[1];
    	v.z = outV[2];
    }
    
    /**
     * <br>功能简述: 将矩阵的逆变换作用到点或者向量上
     * <br>功能详细描述: 如果多次调用，应该使用{@link #invert(Transformation3D)}求得逆矩阵之后再使用
     * {@link #mapVector(float[], int, float[], int, int)} 
     * <br>注意:
     * @param u
     * @param offsetU
     * @param v
     * @param offsetV
     * @param w 为0表示向量，为1表示顶点
     */
    public void inverseTransform(float[] u, int offsetU, float[] v, int offsetV, int w) {
    	float[] m = sTmpMatrix;
    	invert(mMatrix, 0, m, 0);
    	final float x = u[offsetU];
    	final float y = u[offsetU + 1];
    	final float z = u[offsetU + 2];
    	v[offsetV++] = m[M00] * x + m[M01] * y + m[M02] * z + m[M03] * w;
    	v[offsetV++] = m[M10] * x + m[M11] * y + m[M12] * z + m[M13] * w;
    	v[offsetV++] = m[M20] * x + m[M21] * y + m[M22] * z + m[M23] * w;
    	
    }
    
    public void translateVector(float[] u, int offsetU, float[] v, int offsetV, int w) {
    	final float x = u[offsetU];
    	final float y = u[offsetU + 1];
    	final float z = u[offsetU + 2];
    	v[offsetV++] = x + mMatrix[M03] * w;
    	v[offsetV++] = y + mMatrix[M13] * w;
    	v[offsetV++] = z + mMatrix[M23] * w;
    }
    
    public void inverseTranslateVector(float[] u, int offsetU, float[] v, int offsetV, int w) {
    	final float x = u[offsetU];
    	final float y = u[offsetU + 1];
    	final float z = u[offsetU + 2];
    	v[offsetV++] = x - mMatrix[M03] * w;
    	v[offsetV++] = y - mMatrix[M13] * w;
    	v[offsetV++] = z - mMatrix[M23] * w;
    }
    
    public void rotateVector(float[] u, int offsetU, float[] v, int offsetV) {
    	final float x = u[offsetU];
    	final float y = u[offsetU + 1];
    	final float z = u[offsetU + 2];
    	v[offsetV++] = mMatrix[M00] * x + mMatrix[M01] * y + mMatrix[M02] * z;
    	v[offsetV++] = mMatrix[M10] * x + mMatrix[M11] * y + mMatrix[M12] * z;
    	v[offsetV++] = mMatrix[M20] * x + mMatrix[M21] * y + mMatrix[M22] * z;
    }
    
    public void inverseRotateVector(float[] u, int offsetU, float[] v, int offsetV) {
    	final float x = u[offsetU];
    	final float y = u[offsetU + 1];
    	final float z = u[offsetU + 2];
    	v[offsetV++] = mMatrix[M00] * x + mMatrix[M10] * y + mMatrix[M20] * z;
    	v[offsetV++] = mMatrix[M01] * x + mMatrix[M11] * y + mMatrix[M21] * z;
    	v[offsetV++] = mMatrix[M02] * x + mMatrix[M12] * y + mMatrix[M22] * z;
    }
    
    /**
     * 获取摄像机位置在当前坐标系的位置，假设没有缩放变换
     * @param v
     * @param offset
     */
    public void getEyePosition(float[] v, int offset) {
    	final float x = -mMatrix[M03];
    	final float y = -mMatrix[M13];
    	final float z = -mMatrix[M23];
    	v[offset++] = mMatrix[M00] * x + mMatrix[M10] * y + mMatrix[M20] * z;
    	v[offset++] = mMatrix[M01] * x + mMatrix[M11] * y + mMatrix[M21] * z;
    	v[offset++] = mMatrix[M02] * x + mMatrix[M12] * y + mMatrix[M22] * z;
    }
    
    public void clearRotation() {
    	// CHECKSTYLE IGNORE 6 LINES
    	for (int i = 0; i < 12; ++i) {
    		mMatrix[i] = 0; 
    	}
    	mMatrix[0] = 1;
    	mMatrix[5] = 1;
    	mMatrix[10] = 1;
    }
    
    public void postTranslate(float[] v, int offset) {
    	mMatrix[M03] += v[offset++];
    	mMatrix[M13] += v[offset++];
    	mMatrix[M23] += v[offset++];
    }
    
    /**
     * <br>功能简述: 计算矩阵的逆
     * <br>功能详细描述: 限制矩阵最后一行为[0 0 0 1]，即非投影矩阵，这对于模型视图矩阵是满足的
     * <br>注意:
     * @param src
     * @param srcOffset
     * @param dst 可以跟src相同，也可以跟sTmpMatrix相同
     * @param dstOffset
     */
    public static void invert(float[] src, int srcOffset, float[] dst, int dstOffset) {
        System.arraycopy(src, srcOffset, sTmpMatrix, 0, MATRIX_SIZE);
        float[] m = sTmpMatrix;
    	
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
		
		dst[dstOffset++] = a00 * rDet;
		dst[dstOffset++] = a01 * rDet;
		dst[dstOffset++] = a02 * rDet;
		dst[dstOffset++] = 0;
		dst[dstOffset++] = a10 * rDet;
		dst[dstOffset++] = a11 * rDet;
		dst[dstOffset++] = a12 * rDet;
		dst[dstOffset++] = 0;
		dst[dstOffset++] = a20 * rDet;
		dst[dstOffset++] = a21 * rDet;
		dst[dstOffset++] = a22 * rDet;
		dst[dstOffset++] = 0;
		dst[dstOffset++] = tx * rDet;
		dst[dstOffset++] = ty * rDet;
		dst[dstOffset++] = tz * rDet;
		dst[dstOffset++] = 1;
		
    }
}