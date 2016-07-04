package com.graphics.engine.gl.math3d;

/**
 * 
 * <br>类描述: 3D数学库
 * <br>功能详细描述:
 * 包含了一些常量定义和工具函数，是{@link Math}的扩展。
 * 
 * @author  dengweiming
 * @date  [2013-7-1]
 */
public class Math3D {

	/** 默认的浮点数误差范围 */
	public static final float EPSILON = 1e-6f;
	
	/** 完全不透明 */
	public static final float FULL_ALPHA = 255;
	
	/** 完全不透明的倒数 */
	public static final float ONE_OVER_FULL_ALPHA = 1 / FULL_ALPHA;
	
	/** 一个圆周的角度 */
	public static final float FULL_DEGREES = 360;
	
	/** 半个圆周的角度 */
	public static final float HALF_DEGREES = 180;
	
	/** 向量x索引 */
	public static final int VTX = 0;
	/** 向量y索引 */
	public static final int VTY = 1;
	/** 向量z索引 */
	public static final int VTZ = 2;
	/** 向量w索引 */
	public static final int VTW = 3;
	/** 向量长度 */
	public static final int VECTOR_LENGTH = 4;
	
	
	static final int M00 = 0;
	static final int M10 = 1;
	static final int M20 = 2;
	static final int M30 = 3;
	static final int M01 = 4;
	static final int M11 = 5;
	static final int M21 = 6;
	static final int M31 = 7;
	static final int M02 = 8;
	static final int M12 = 9;
	static final int M22 = 10;
	static final int M32 = 11;
	static final int M03 = 12;
	static final int M13 = 13;
	static final int M23 = 14;
	static final int M33 = 15;
	static final int MC = 16;
	
	/** 矩阵平移量x索引 */
	public static final int MTX = M03;
	/** 矩阵平移量y索引 */
	public static final int MTY = M13;
	/** 矩阵平移量z索引 */
	public static final int MTZ = M23;
	/** 矩阵长度 */
	public static final int MATRIX_LENGTH = MC;
	
	/** 最大值 */
	public static final float MAX_VALUE = Float.MAX_VALUE;
	/** 最小值 */
	public static final float MIN_VALUE = Float.MIN_VALUE;
	
	/** 双精度的圆周率 */
	private static final double PI_DOUBLE = Math.acos(-1);
	
	/** 圆周率 */
	public static final float PI = (float) PI_DOUBLE;
	/** 圆周率的一半 */
	public static final float HALF_PI = (float) (PI_DOUBLE * 0.5);
	/** 圆周率的两倍 */
	public static final float DOUBLE_PI = (float) (PI_DOUBLE * 2);
	
	/** 弧度转角度的比率 */
	public static final float TO_DEGREE = (float) (180 / PI_DOUBLE);
	/** 角度转弧度的比率 */
	public static final float TO_RADIAN = (float) (PI_DOUBLE / 180);
	
	/**
	 * 生成[0, 1)范围均匀分布的随机数
	 */
	public static float random() {
		return (float) Math.random();
	}
	
	/**
	 * <br>功能简述: 生成一个不透明的随机颜色
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param rgbMin R,G,B分量的最小值
	 * @param rgbMax R,G,B分量的最大值
	 * @return
	 */
	public static int randomColor(int rgbMin, int rgbMax) {
		int r = (int) ((rgbMax - rgbMin) * Math.random()) + rgbMin;
		int g = (int) ((rgbMax - rgbMin) * Math.random()) + rgbMin;
		int b = (int) ((rgbMax - rgbMin) * Math.random()) + rgbMin;
		return 0xFF000000 | (r << 16) | (g << 8) | b;	//CHECKSTYLE IGNORE
	}
	
	/**
	 * <br>功能简述: 生成一个随机颜色
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param rgbMin R,G,B分量的最小值
	 * @param rgbMax R,G,B分量的最大值
	 * @param aMin Alpha分量的最小值
	 * @param aMax Alpha分量的最大值
	 * @return
	 */
	public static int randomColor(int rgbMin, int rgbMax, int aMin, int aMax) {
		int r = (int) ((rgbMax - rgbMin) * Math.random()) + rgbMin;
		int g = (int) ((rgbMax - rgbMin) * Math.random()) + rgbMin;
		int b = (int) ((rgbMax - rgbMin) * Math.random()) + rgbMin;
		int a = (int) ((aMax - aMin) * Math.random()) + aMin;
        return (a << 24) | (r << 16) | (g << 8) | b;	//CHECKSTYLE IGNORE
	}

	/**
	 * 将一个角度归约到[0, 360)的范围内
	 */
	public static float reduceDegrees(float degrees) {
		degrees %= FULL_DEGREES;
		if (degrees < 0) {
			degrees += FULL_DEGREES;
		}
		return degrees;
	}
	
	/**
	 * 将一个弧度归约到[0, 2pi)的范围内
	 */
	public static float reduceRadians(float radians) {
		radians %= DOUBLE_PI;
		if (radians < 0) {
			radians += DOUBLE_PI;
		}
		return radians;
	}
	
	/**
	 * 判断一个浮点数是否约等于0，误差范围为{@link #EPSILON}
	 */
	public static boolean fZero(float a) {
		return a > -EPSILON && a < EPSILON;
	}
	
	/**
	 * 判断两个浮点数是否约等于，误差范围为{@link #EPSILON}
	 */
	public static boolean fEqual(float a, float b) {
		return a - b > -EPSILON && a - b < EPSILON;
	}
	
	/**
	 * 计算平方根的倒数
	 */
	public static float invSqrt(float x) {
		return (float) (1 / Math.sqrt(x));
	}
		
	static void todo() {
		throw new UnsupportedOperationException("todo");
	}
}
