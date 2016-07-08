package com.graphics.engine.graphics.geometry;

/**
 * 
 * <br>类描述: 长方体盒子类
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-7-11]
 */
public class GLBox extends GLObject {
	/*
	 * 盒子有6个面，每个面4个顶点，按照左上，左下，右上，右下的顺序排列。
	 */

	private final static int FACES = 6;
	private final static int VERTEX_PER_FACE = 4;
	private final static int VECTOR_COMP = 3;

	/** @formatter:off */
	private final static int[] NORMALS = {
		-1,  0,  0,	// left
		 1,  0,  0,	// right
		 0, -1,  0,	// bottom
		 0,  1,  0,	// top
		 0,  0, -1,	// back
		 0,  0,  1,	// front
	};
	
	private final static int[] TANGENT_INDEX = {
		5, 4, 1, 1, 0, 1, 
	};
	
	private final static int[] BINORMAL_INDEX = {
		3, 3, 5, 4, 3, 3, 
	};
	
	private final static int[] TANGENT_FACTOR = {
		-1, -1, 1, 1, 
	};
	
	private final static int[] BINORMAL_FACTOR = { 
		1, -1, 1, -1, 
	};
	/** @formatter:on */
	private final static float[] CENTER = new float[3];
	private final static float[] HALF_SIZE = new float[3];

	boolean mFill;
	float mCenterX;
	float mCenterY;
	float mCenterZ;
	float mWidth;
	float mHeight;
	float mLength;

	/**
	 * 创建一个长方体盒子
	 * @param fill	是否填充各个面，否则为线框
	 */
	public GLBox(boolean fill) {
		mFill = fill;
		mVertexCount = VERTEX_PER_FACE * FACES;
		mPositionElements = mVertexCount * mPositionComponent;

		mPositionArray = new float[mPositionElements];
		if (mFill) {
			mMode = TRIANGLE_STRIP;
			mIndexCount = VERTEX_PER_FACE * FACES + 2 * (FACES - 1);
			mIndexArray = new short[mIndexCount];
			final short[] array = mIndexArray;
			int loc = 0;
			for (int f = 0; f < FACES; ++f) {
				for (int v = 0; v < VERTEX_PER_FACE; ++v) {
					array[loc++] = (short) (VERTEX_PER_FACE * f + v);
				}
				if (f < FACES - 1) {
					array[loc] = array[loc - 1];
					++loc;
					array[loc] = (short) (VERTEX_PER_FACE * (f + 1));
					++loc;
				}
			}
		} else {
			mMode = LINES;
			mIndexCount = 2 * (VERTEX_PER_FACE - 1) * FACES;
			mIndexArray = new short[mIndexCount];
			final short[] array = mIndexArray;
			int loc = 0;
			for (int f = 0; f < FACES; ++f) {
				if (f == 2 || f == 3) {
					continue;
				}
				//除了顶面和底面，每个面绘制左，上，下三条棱
				int index = VERTEX_PER_FACE * f;
				array[loc++] = (short) index;
				array[loc++] = (short) (index + 1);
				array[loc++] = (short) index;
				array[loc++] = (short) (index + 2);
				array[loc++] = (short) (index + 1);
				array[loc++] = (short) (index + 3);
			}
		}
	}

	/**
	 * <br>功能简述: 设置盒子的大小
	 * <br>功能详细描述: 
	 * <br>注意:
	 * @param w	宽
	 * @param h	高
	 * @param l	长(Z轴）
	 */
	public void setSize(float w, float h, float l) {
		float cx = w * 0.5f;
		float cy = h * -0.5f;
		float cz = l * -0.5f;
		if (mCenterX == cx && mCenterY == cy || mCenterZ == cz) {
			return;
		}

		CENTER[0] = mCenterX = cx;
		CENTER[1] = mCenterY = cy;
		CENTER[2] = mCenterZ = cz;
		HALF_SIZE[0] = w * 0.5f;
		HALF_SIZE[1] = h * 0.5f;
		HALF_SIZE[2] = l * 0.5f;

		final float[] pos = mPositionArray;
		int loc = 0;

		for (int f = 0; f < FACES; ++f) {
			int n = f * VECTOR_COMP;
			int t = TANGENT_INDEX[f] * VECTOR_COMP;
			int bn = BINORMAL_INDEX[f] * VECTOR_COMP;
			for (int v = 0; v < VERTEX_PER_FACE; ++v) {
				for (int i = 0; i < 3; ++i) {
					/** @formatter:off */
					pos[loc++] = (NORMALS[t + i] * TANGENT_FACTOR[v] 
							+ NORMALS[bn + i] * BINORMAL_FACTOR[v] 
							+ NORMALS[n + i]) 
								* HALF_SIZE[i] + CENTER[i];
					/** @formatter:on */
				}
			}
		}
	}

	/**
	 * 获取盒子中心点X坐标
	 */
	public float getCenterX() {
		return mCenterX;
	}

	/**
	 * 获取盒子中心点Y坐标
	 */
	public float getCenterY() {
		return mCenterY;
	}

	/**
	 * 获取盒子中心点Z坐标
	 */
	public float getCenterZ() {
		return mCenterZ;
	}

	@Override
	public void setTexcoords(float u1, float v1, float u2, float v2) {
		if (mTexcoordArray == null) {
			mTexcoordElements = mTexcoordComponent * mVertexCount;
			mTexcoordArray = new float[mTexcoordElements];
		}

		final float[] tex = mTexcoordArray;
		int loc = 0;
		for (int f = 0; f < FACES; ++f) {
			tex[loc++] = u1;
			tex[loc++] = v1;
			tex[loc++] = u1;
			tex[loc++] = v2;
			tex[loc++] = u2;
			tex[loc++] = v1;
			tex[loc++] = u2;
			tex[loc++] = v2;
		}
	}
}
