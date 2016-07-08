package com.graphics.engine.graphics;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.NinePatch;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.opengl.GLES20;
import android.util.Log;

/**
 * <pre>
 * NinePatch的OpenGL实现
 * 
 * 来自源码中ResourceTypes.h中的文档:
 * This chunk specifies how to split an image into segments for
 * scaling.
 *
 * There are J horizontal and K vertical segments.  These segments divide
 * the image into J*K regions as follows (where J=4 and K=3):
 *
 *      F0   S0    F1     S1
 *   +-----+----+------+-------+
 * S2|  0  |  1 |  2   |   3   |
 *   +-----+----+------+-------+
 *   |     |    |      |       |
 *   |     |    |      |       |
 * F2|  4  |  5 |  6   |   7   |
 *   |     |    |      |       |
 *   |     |    |      |       |
 *   +-----+----+------+-------+
 * S3|  8  |  9 |  10  |   11  |
 *   +-----+----+------+-------+
 *
 * Each horizontal and vertical segment is considered to by either
 * stretchable (marked by the Sx labels) or fixed (marked by the Fy
 * labels), in the horizontal or vertical axis, respectively. In the
 * above example, the first is horizontal segment (F0) is fixed, the
 * next is stretchable and then they continue to alternate. Note that
 * the segment list for each axis can begin or end with a stretchable
 * or fixed segment.
 *
 * The relative sizes of the stretchy segments indicates the relative
 * amount of stretchiness of the regions bordered by the segments.  For
 * example, regions 3, 7 and 11 above will take up more horizontal space
 * than regions 1, 5 and 9 since the horizontal segment associated with
 * the first set of regions is larger than the other set of regions.  The
 * ratios of the amount of horizontal (or vertical) space taken by any
 * two stretchable slices is exactly the ratio of their corresponding
 * segment lengths.
 *
 * xDivs and yDivs point to arrays of horizontal and vertical pixel
 * indices.  The first pair of Divs (in either array) indicate the
 * starting and ending points of the first stretchable segment in that
 * axis. The next pair specifies the next stretchable segment, etc. So
 * in the above example xDiv[0] and xDiv[1] specify the horizontal
 * coordinates for the regions labeled 1, 5 and 9.  xDiv[2] and
 * xDiv[3] specify the coordinates for regions 3, 7 and 11. Note that
 * the leftmost slices always start at x=0 and the rightmost slices
 * always end at the end of the image. So, for example, the regions 0,
 * 4 and 8 (which are fixed along the X axis) start at x value 0 and
 * go to xDiv[0] and slices 2, 6 and 10 start at xDiv[1] and end at
 * xDiv[2].
 *
 * The array pointed to by the colors field lists contains hints for
 * each of the regions.  They are ordered according left-to-right and
 * top-to-bottom as indicated above. For each segment that is a solid
 * color the array entry will contain that color value; otherwise it
 * will contain NO_COLOR.  Segments that are completely transparent
 * will always have the value TRANSPARENT_COLOR.
 *
 * enum {
        // The 9 patch segment is not a solid color.
        NO_COLOR = 0x00000001,

        // The 9 patch segment is completely transparent.
        TRANSPARENT_COLOR = 0x00000000
    };
    </pre>
 */
class GLNinePatch implements TextureListener {
	private static final int NINEPATCH_CHUNCK_HEAD_SIZE = 32; //Res_png_9patch结构体的头为32个字节
	private static final String TAG = "GLNinePatch";
	private Bitmap mBitmap;
	private byte[] mChunk;

	private Texture mTexture;
	private boolean mDecodeSucceed;

	private int mWidth;
	private int mHeight;
	private byte mNumXDivs, mNumYDivs, mNumColors;
	//可缩放区域的示意图：0  1--2  3--4 （即1和2，3和4标明了一段可缩放区域）
	private int[] mXDivs; //x方向上的分段位置，在计算纹理坐标后，将其转化为与前一个位置的距离
	private int[] mYDivs; //y方向上的分段位置，在计算纹理坐标后，将其转化为与前一个位置的距离
	//	private int[] colors;		//暂时不使用这些数据

	private int mXScaleArea;
	private int mYScaleArea;

	private float[] mTexCoordBuffer;
	private short[] mIndexBuffer;
	private int mIndexArraySize;

	private float[] mVertexArray;
	private float[] mVertexXArray;
	private float[] mVertexYArray;

	private final static float ONE_OVER_255 = 1 / 255.0f;	//CHECKSTYLE IGNORE

	private final static int POSITION_COMPONENT = 3;
	private final static int TEXCOORD_COMPONENT = 2;

	private float mAlpha = 1;
	private final float[] mSrcColor = new float[4];	//CHECKSTYLE IGNORE
	private int mPorterDuffMode = TextureShader.MODE_NONE;

	private Rect mPadding = new Rect();
	
	private GLShaderWrapper mShaderWrapper;

	public GLNinePatch(NinePatchDrawable drawable) {
		try {
			Field field = NinePatchDrawable.class.getDeclaredField("mNinePatch");
			field.setAccessible(true);
			NinePatch ninePatch = (NinePatch) field.get(drawable);

			field = NinePatch.class.getDeclaredField("mBitmap");
			field.setAccessible(true);
			mBitmap = (Bitmap) field.get(ninePatch);
			

//			field = NinePatch.class.getDeclaredField("mChunk");
//			field.setAccessible(true);
//			mChunk = (byte[]) field.get(ninePatch);
			mChunk = mBitmap.getNinePatchChunk();

			mWidth = mBitmap.getWidth();
			mHeight = mBitmap.getHeight();

			mTexture = BitmapTexture.createSharedTexture(mBitmap);

			mDecodeSucceed = deserialize() && computeArrays();
			mBitmap = null;
			mChunk = null;

			drawable.getPadding(mPadding);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//for test
	/*
	 public  GLNinePatch(Bitmap bitmap, byte[] chunk) {
		mBitmap = bitmap;
		mChunk = chunk;
		
		mWidth = mBitmap.getWidth();
		mHeight = mBitmap.getHeight();
		
		mTexture = new BitmapTexture(mBitmap);
		
		mDecodeSucceed = deserialize() && computeArrays();
		mBitmap = null;
		mChunk = null;
	}
	 */

	@SuppressLint("WrongCall")
	public void draw(GLCanvas canvas) {
		if (!mDecodeSucceed) {
			return;
		}
		if (mTexture == null) {
			return;
		}

		final int fadeAlpha = canvas.getAlpha();
		float alpha = mAlpha;
		if (fadeAlpha < 255) {	//CHECKSTYLE IGNORE
			alpha *= fadeAlpha * ONE_OVER_255;
		}
		
		if (mShaderWrapper != null) {
			RenderContext context = RenderContext.acquire();
			context.shader = mShaderWrapper;
			context.alpha = alpha;
			context.texture = mTexture;
			canvas.getFinalMatrix(context);
			mShaderWrapper.onDraw(context);
			canvas.addRenderable(mRenderable, context);

			VertexBufferBlock.pushVertexData(mRenderable);
			VertexBufferBlock.pushVertexData(mVertexArray, 0, mVertexArray.length);
			VertexBufferBlock.pushVertexData(mTexCoordBuffer, 0, mTexCoordBuffer.length);
			IndexBufferBlock.pushVertexData(mIndexBuffer, 0, mIndexArraySize);
			return;
		}
		
		
		TextureShader shader = null;
		if (mPorterDuffMode == TextureShader.MODE_NONE) {
			shader = TextureShader.getShader(alpha >= 1
					? TextureShader.MODE_NONE
					: TextureShader.MODE_ALPHA);
		} else {
			shader = TextureShader.getShader(mPorterDuffMode);
		}

		if (shader == null) {
			return;
		}
		
		RenderContext context = RenderContext.acquire();
		
		context.shader = shader;
		context.alpha = alpha;
		context.texture = mTexture;
		//CHECKSTYLE IGNORE 4 LINES
		context.color[0] = mSrcColor[0];
		context.color[1] = mSrcColor[1];
		context.color[2] = mSrcColor[2];
		context.color[3] = mSrcColor[3];
		canvas.getFinalMatrix(context);

		canvas.addRenderable(mRenderable, context);

		VertexBufferBlock.pushVertexData(mRenderable);
		VertexBufferBlock.pushVertexData(mVertexArray, 0, mVertexArray.length);
		VertexBufferBlock.pushVertexData(mTexCoordBuffer, 0, mTexCoordBuffer.length);
		IndexBufferBlock.pushVertexData(mIndexBuffer, 0, mIndexArraySize);
	}
	
	public void drawWithoutEffect(GLCanvas canvas) {
		if (!mDecodeSucceed) {
			return;
		}
		if (mTexture == null) {
			return;
		}
		
		RenderContext context = RenderContext.acquire();
		context.shader = TextureShader.getShader(TextureShader.MODE_NONE);
		context.texture = mTexture;
		canvas.getFinalMatrix(context);

		canvas.addRenderable(mRenderable, context);

		VertexBufferBlock.pushVertexData(mRenderable);
		VertexBufferBlock.pushVertexData(mVertexArray, 0, mVertexArray.length);
		VertexBufferBlock.pushVertexData(mTexCoordBuffer, 0, mTexCoordBuffer.length);
		IndexBufferBlock.pushVertexData(mIndexBuffer, 0, mIndexArraySize);
	}

	private boolean deserialize() {
		if (null == mBitmap) {
			return false;
		}
		if (null == mChunk || mChunk.length < NINEPATCH_CHUNCK_HEAD_SIZE) {
			Log.e(TAG, "ninePatch head is broken!");
			return false;
		}
		
		//CHECKSTYLE IGNORE 3 LINES
		mNumXDivs = mChunk[1];
		mNumYDivs = mChunk[2];
		mNumColors = mChunk[3];
		if (mChunk.length < NINEPATCH_CHUNCK_HEAD_SIZE + (mNumXDivs + mNumYDivs + mNumColors) * 4) {	//CHECKSTYLE IGNORE
			Log.e(TAG, "ninePatch head is broken!");
			return false;
		}

		mXDivs = new int[mNumXDivs + 2];
		mYDivs = new int[mNumYDivs + 2];
		//		colors = new int[numColors];

		int bias = NINEPATCH_CHUNCK_HEAD_SIZE - 1;

		mXDivs[0] = 0;
		for (int i = 1; i <= mNumXDivs; ++i) {
			mXDivs[i] = byte2Int(mChunk[++bias], mChunk[++bias], mChunk[++bias], mChunk[++bias]);
		}
		mXDivs[mNumXDivs + 1] = mWidth;

		mYDivs[0] = 0;
		for (int i = 1; i <= mNumYDivs; ++i) {
			mYDivs[i] = byte2Int(mChunk[++bias], mChunk[++bias], mChunk[++bias], mChunk[++bias]);
		}
		mYDivs[mNumYDivs + 1] = mHeight;

		//		for(int i = 0; i < numColors; ++i){
		//			colors[i] = byte2Int(mChunk[++bias], mChunk[++bias], mChunk[++bias], mChunk[++bias]);
		//		}		
		return true;
	}

	private boolean computeArrays() {
		//=========计算纹理坐标数组
		int texCoordArraySize = (mNumXDivs + 2) * (mNumYDivs + 2) * 2;
		float[] texCoordArray = new float[texCoordArraySize];

		int[] paddedSize = { mWidth, mHeight };
		if (mTexture != null && mTexture.isMipMapEnabled()) {
			Texture.solvePaddedSize(mWidth, mHeight, paddedSize, true);
		}
		final float sx = 1.0f / paddedSize[0];
		final float sy = 1.0f / paddedSize[1];
		for (int i = 0, index = 0; i < mNumYDivs + 2; ++i) {
			final float yCords = mYDivs[i] * sy;
			for (int j = 0; j < mNumXDivs + 2; ++j) {
				texCoordArray[index++] = mXDivs[j] * sx;
				texCoordArray[index++] = yCords;
			}
		}
		mTexCoordBuffer = texCoordArray;

		//=========计算可缩放区域大小
		//将分段位置转化为与前一个位置的距离
		for (int i = mNumXDivs + 1; i > 0; --i) {
			mXDivs[i] -= mXDivs[i - 1];
		}
		for (int i = mNumYDivs + 1; i > 0; --i) {
			mYDivs[i] -= mYDivs[i - 1];
		}
		mXScaleArea = 0;
		mYScaleArea = 0;
		//可缩放区域的示意图：0  1--2  3--4 （即1和2，3和4标明了一段可缩放区域）
		for (int i = 2; i < mNumXDivs + 1; i += 2) {
			mXScaleArea += mXDivs[i];
		}
		for (int i = 2; i < mNumYDivs + 1; i += 2) {
			mYScaleArea += mYDivs[i];
		}
		//如果没有可缩放区域，认为是整体均匀缩放的
		if (mXScaleArea <= 0) {
			mXScaleArea = mWidth;
		}
		if (mYScaleArea <= 0) {
			mYScaleArea = mHeight;
		}

		//=========计算顶点索引数组
		mIndexArraySize = (mNumXDivs + 1) * (mNumYDivs + 1) * 6;	//CHECKSTYLE IGNORE
		short[] indexArray = new short[mIndexArraySize];
		mIndexBuffer = indexArray;

		//将各块区域分解成左上和右下两个三角形，计算三角形的顶点索引数组
		final int w = mNumXDivs + 2;
		for (int row = 0, index = 0, lt = 0; row < mNumYDivs + 1; ++row, ++lt) {
			for (int col = 0; col < mNumXDivs + 1; ++col, ++lt) {
				/**
				 * (row,   col)---------(row, col+1)
				 *   |                       |
				 *   |                       |
				 *   |                       |
				 *   |                       |
				 * (row+1, col)---------(row+1, col+1)
				 * 
				 * lt表示点(row,col)的索引
				 */
				indexArray[index++] = (short) lt; //lt
				indexArray[index++] = (short) (lt + w); //lb
				indexArray[index++] = (short) (lt + 1); //rt
				indexArray[index++] = (short) (lt + 1); //rt
				indexArray[index++] = (short) (lt + w); //lb
				indexArray[index++] = (short) (lt + w + 1); //rb
			}
		}

		//=========创建顶点坐标数组
		mVertexXArray = new float[mNumXDivs + 2];
		mVertexYArray = new float[mNumYDivs + 2];
		int vertexCordSize = (mNumXDivs + 2) * (mNumYDivs + 2) * POSITION_COMPONENT;
		mVertexArray = new float[vertexCordSize];

		return true;
	}

	/** 
	 * 4个字节（从低到高排列）转换为整型 
	 * @return 
	 */
	private static int byte2Int(byte b0, byte b1, byte b2, byte b3) {
		//注意要先和0xFF相与，为了避免转成int的时候保留了负号
		return (b0 & 0xFF) | ((b1 & 0xFF) << 8) | ((b2 & 0xFF) << 16) | ((b3 & 0xFF) << 24);	//CHECKSTYLE IGNORE
	}

	public void setBounds(int left, int top, int right, int bottom) {
		if (!mDecodeSucceed) {
			return;
		}
		//重新计算顶点数组
		float sx = ((right - left) - (mWidth - mXScaleArea)) / (float) mXScaleArea;
		mVertexXArray[0] = left;
		if (sx >= 0) {
			for (int j = 1; j < mNumXDivs; j += 2) {
				mVertexXArray[j] = mXDivs[j] + mVertexXArray[j - 1];
				mVertexXArray[j + 1] = mXDivs[j + 1] * sx + mVertexXArray[j];
			}
		} else {
			sx = (right - left) / (float) (mWidth - mXScaleArea); //当实际宽度比不可缩放区域的宽度还要小时，均匀缩放
			for (int j = 1; j < mNumXDivs; j += 2) {
				mVertexXArray[j] = mXDivs[j] * sx + mVertexXArray[j - 1];
				mVertexXArray[j + 1] = mVertexXArray[j];
			}
		}
		mVertexXArray[mNumXDivs + 1] = right;

		float sy = ((bottom - top) - (mHeight - mYScaleArea)) / (float) mYScaleArea;
		mVertexYArray[0] = -top;
		if (sy >= 0) {
			for (int i = 1; i < mNumYDivs; i += 2) {
				mVertexYArray[i] = -mYDivs[i] + mVertexYArray[i - 1];
				mVertexYArray[i + 1] = -mYDivs[i + 1] * sy + mVertexYArray[i];
			}
		} else {
			sy = (bottom - top) / (float) (mHeight - mYScaleArea);
			for (int i = 1; i < mNumYDivs; i += 2) {
				mVertexYArray[i] = -mYDivs[i] * sy + mVertexYArray[i - 1];
				mVertexYArray[i + 1] = mVertexYArray[i];
			}
		}
		mVertexYArray[mNumYDivs + 1] = -bottom;

		for (int i = 0, index = 0; i < mNumYDivs + 2; ++i) {
			final float y = mVertexYArray[i];
			for (int j = 0; j < mNumXDivs + 2; ++j) {
				mVertexArray[index++] = mVertexXArray[j];
				mVertexArray[index++] = y;
				mVertexArray[index++] = 0;
			}
		}
	}

	/**
	 * 设置一个3D的矩形边框(其实可以为平行四边形)
	 * @param pts
	 * @param offsetLT		左上角在 pts 的位置
	 * @param offsetLB		左下角在 pts 的位置
	 * @param offsetRT		右上角在 pts 的位置
	 * @param extPaddingX	横向的padding区域置于边框外
	 * @param extPaddingY	纵向的padding区域置于边框外
	 */
	public void setBounds3D(float[] pts, int offsetLT, int offsetLB, int offsetRT,
			boolean extPaddingX, boolean extPaddingY) {
		if (!mDecodeSucceed) {
			return;
		}
		float x0 = pts[offsetLT];
		float x1 = pts[offsetLT + 1];
		float x2 = pts[offsetLT + 2];
		float u0 = pts[offsetRT] - x0;
		float u1 = pts[offsetRT + 1] - x1;
		float u2 = pts[offsetRT + 2] - x2;
		float v0 = pts[offsetLB] - x0;
		float v1 = pts[offsetLB + 1] - x1;
		float v2 = pts[offsetLB + 2] - x2;
		float uLen = (float) Math.sqrt(u0 * u0 + u1 * u1 + u2 * u2);
		float vLen = (float) Math.sqrt(v0 * v0 + v1 * v1 + v2 * v2);

		if (extPaddingX) {
			float tu = -mPadding.left / uLen;
			x0 += u0 * tu;
			x1 += u1 * tu;
			x2 += u2 * tu;
			tu = (mPadding.left + mPadding.right) / uLen + 1;
			u0 *= tu;
			u1 *= tu;
			u2 *= tu;
			uLen *= tu;
		}
		if (extPaddingY) {
			float tv = -mPadding.top / vLen;
			x0 += v0 * tv;
			x1 += v1 * tv;
			x2 += v2 * tv;
			tv = (mPadding.top + mPadding.bottom) / vLen + 1;
			v0 *= tv;
			v1 *= tv;
			v2 *= tv;
			vLen *= tv;
		}

		//重新计算顶点数组
		float su = (uLen - (mWidth - mXScaleArea)) / (float) mXScaleArea;
		mVertexXArray[0] = 0;
		if (su >= 0) {
			final float oneOverULen = 1 / uLen;
			final float suOverULen = su / uLen;
			for (int j = 1; j < mNumXDivs; j += 2) {
				mVertexXArray[j] = mXDivs[j] * oneOverULen + mVertexXArray[j - 1];
				mVertexXArray[j + 1] = mXDivs[j + 1] * suOverULen + mVertexXArray[j];
			}
		} else {
			su = 1 / (mWidth - mXScaleArea);
			for (int j = 1; j < mNumXDivs; j += 2) {
				mVertexXArray[j] = mXDivs[j] * su + mVertexXArray[j - 1];
				mVertexXArray[j + 1] = mVertexXArray[j];
			}
		}
		mVertexXArray[mNumXDivs + 1] = 1;

		float sv = (vLen - (mHeight - mYScaleArea)) / (float) mYScaleArea;
		mVertexYArray[0] = 0;
		if (sv >= 0) {
			final float oneOverVLen = 1 / vLen;
			final float svOverULen = sv / vLen;
			for (int i = 1; i < mNumYDivs; i += 2) {
				mVertexYArray[i] = mYDivs[i] * oneOverVLen + mVertexYArray[i - 1];
				mVertexYArray[i + 1] = mYDivs[i + 1] * svOverULen + mVertexYArray[i];
			}
		} else {
			sv = 1 / (mHeight - mYScaleArea);
			for (int i = 1; i < mNumYDivs; i += 2) {
				mVertexYArray[i] = mYDivs[i] * sv + mVertexYArray[i - 1];
				mVertexYArray[i + 1] = mVertexYArray[i];
			}
		}
		mVertexYArray[mNumYDivs + 1] = 1;

		for (int i = 0, index = 0; i < mNumYDivs + 2; ++i) {
			final float v = mVertexYArray[i];
			for (int j = 0; j < mNumXDivs + 2; ++j) {
				final float u = mVertexXArray[j];
				mVertexArray[index++] = x0 + u0 * u + v0 * v;
				mVertexArray[index++] = x1 + u1 * u + v1 * v;
				mVertexArray[index++] = x2 + u2 * u + v2 * v;
			}
		}
	}

	public void setColorFilter(int srcColor, Mode mode) {
		if (mode == null) {
			mPorterDuffMode = TextureShader.MODE_NONE;
			return;
		}
		//从ARGB转成(r, g, b, a)的alpha-premultiplied格式
		//CHECKSTYLE IGNORE 5 LINES
		final float a = (srcColor >>> 24) * ONE_OVER_255;
		mSrcColor[0] = (srcColor >>> 16 & 0xFF) * a * ONE_OVER_255;
		mSrcColor[1] = (srcColor >>> 8 & 0xFF) * a * ONE_OVER_255;
		mSrcColor[2] = (srcColor & 0xFF) * a * ONE_OVER_255;
		mSrcColor[3] = a;
		mPorterDuffMode = mode.ordinal();
	}

	public void setAlpha(int alpha) {
		if (alpha == 255) {	//CHECKSTYLE IGNORE
			mAlpha = 1;
		} else {
			mAlpha = alpha * ONE_OVER_255;
		}
	}

	@Override
	public void onTextureInvalidate() {
		if (mTexture != null) {
			mTexture.onTextureInvalidate();
		}

	}

	public void clear() {
		if (mTexture != null) {
			mTexture.clear();
			mTexture = null;
		}
		mShaderWrapper = null;
		mBitmap = null;
	}

	public void getPadding(Rect padding) {
		padding.set(mPadding);
	}
	
	Bitmap getBitmap() {
		if (mTexture != null && mTexture instanceof BitmapTexture) {
			return ((BitmapTexture) mTexture).getBitmap();
		}
		return null;
	}
	
	/**
	 * <br>功能简述: 设置新的纹理
	 * <br>功能详细描述:
	 * <br>注意: 会将当前的纹理释放
	 * @param texture
	 * 
	 * @hide
	 */
	void setTexture(Texture texture) {
		if (mTexture != texture && mTexture != null) {
			mTexture.clear();
		}
		mTexture = texture;
	}
	
	Texture getTexture() {
		return mTexture;
	}
	
	void setShaderWrapper(GLShaderWrapper shader) {
		mShaderWrapper = shader;
	}
	
	GLShaderWrapper getShaderWrapper() {
		return mShaderWrapper;
	}
	
	public void yield() {
		if (mTexture != null) {
			mTexture.yield();
		}
	}
	
	public boolean isBitmapRecycled() {
		return mBitmap != null && mBitmap.isRecycled();
	}
	
	private final Renderable mRenderable = new Renderable() {

		@Override
		public void run(long timeStamp, RenderContext context) {
			VertexBufferBlock.popVertexData(this);
			
			final int vertexLen = mVertexArray.length;
			final int texcoordLen = mTexCoordBuffer.length;
			final int dataSize = vertexLen + texcoordLen;
			final int indexSize = mIndexArraySize;
			
			if (context.texture == null || !context.texture.bind()) {
				VertexBufferBlock.popVertexData(null, 0, dataSize);
				IndexBufferBlock.popVertexData(null, 0, indexSize);
				return;
			}
			if (context.shader == null) {
				VertexBufferBlock.popVertexData(null, 0, dataSize);
				IndexBufferBlock.popVertexData(null, 0, indexSize);
				return;
			}
			GLShaderProgram shaderProgram = context.shader.onRender(context);
			if (shaderProgram == null || !(shaderProgram instanceof TextureShader)) {
				VertexBufferBlock.popVertexData(null, 0, dataSize);
				IndexBufferBlock.popVertexData(null, 0, indexSize);
				return;
			}
			TextureShader shader = (TextureShader) shaderProgram;
			if (context.shader == shader) {	//不是 GLShaderWrapper 的情况
				shader = (TextureShader) context.shader;
				if (shader == null || !shader.bind()) {
					VertexBufferBlock.popVertexData(null, 0, dataSize);
					IndexBufferBlock.popVertexData(null, 0, indexSize);
					return;
				}
				shader.setAlpha(context.alpha);
				shader.setMaskColor(context.color);
				shader.setMatrix(context.matrix, 0);
			}

			VertexBufferBlock.rewindReadingBuffer(dataSize);
			FloatBuffer positionBuffer = VertexBufferBlock.popVertexData(vertexLen);
			shader.setPosition(positionBuffer, POSITION_COMPONENT);
			FloatBuffer texCoordBuffer = VertexBufferBlock.popVertexData(texcoordLen);
			shader.setTexCoord(texCoordBuffer, TEXCOORD_COMPONENT);

			IndexBufferBlock.rewindReadingBuffer(indexSize);
			ShortBuffer indexBuffer = IndexBufferBlock.popVertexData(indexSize);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexSize, GLES20.GL_UNSIGNED_SHORT,
					indexBuffer);
		}

	};

}