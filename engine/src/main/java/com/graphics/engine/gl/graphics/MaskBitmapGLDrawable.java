package com.graphics.engine.gl.graphics;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLES20;

/**
 * <br>类描述: 实现蒙板效果的{@link GLDrawable}
 * <br>功能详细描述:
 * <br>使用{@link #setFilterBitmap(BitmapDrawable)} 设置蒙板图片，它的alpha值会与原图对应像素相乘。
 * <br>默认蒙板是缩放到和原图一样大小并重叠的，可以使用{@link #setMashTexCoord(float[])}设置蒙板的变换。
 * 
 * @author  oulingmei
 */
public class MaskBitmapGLDrawable extends GLDrawable {

	private final static int TEXCOORD_COMPONENT = 2;
	private final static int TEXCOORD_FLOAT_ELEMENTS = TEXCOORD_COMPONENT * VERTEX_COUNT;

	private BitmapTexture mBitmapTexture;
	private BitmapTexture mFilterTexture;
	private float[] mTexCoordBuffer;
	private float[] mMaskTexCoordBuffer;

	/**
	 * 使用位图bitmap来创建一个对象，由资源res指定位图的dpi。
	 */
	public MaskBitmapGLDrawable(Resources res, Bitmap bitmap) {
		this(new BitmapDrawable(res, bitmap));
	}

	/**
	 * 使用BitmapDrawable来创建一个对象
	 */ 
	public MaskBitmapGLDrawable(BitmapDrawable drawable) {
		this();
		if (drawable == null) {
			return;
		}
		final Bitmap bitmap = drawable.getBitmap();
		mBitmapTexture = BitmapTexture.createSharedTexture(bitmap);

		mIntrinsicWidth = drawable.getIntrinsicWidth();
		mIntrinsicHeight = drawable.getIntrinsicHeight();
		setBounds(0, 0, mIntrinsicWidth, mIntrinsicHeight);
	}

	/**
	 * @hide
	 */
	public MaskBitmapGLDrawable() {
		mTexCoordBuffer = new float[] { 0, 0, 0, 1, 1, 0, 1, 1 };
		mMaskTexCoordBuffer = new float[] { 0, 0, 0, 1, 1, 0, 1, 1 };

		register();
	}

	@Override
	public void draw(GLCanvas canvas) {
		if (mBitmapTexture == null) {
			return;
		}

		TextureShader shader = null;
		shader = TextureShader.getShader(TextureShader.MODE_BITMAP_FILTER);
		if (shader == null) {
			return;
		}

		RenderContext context = RenderContext.acquire();
		context.texture = mBitmapTexture;
		context.shader = shader;
		canvas.getFinalMatrix(context);

		canvas.addRenderable(mRenderable, context);
		
		VertexBufferBlock.pushVertexData(mRenderable);
		pushBoundsVertex();
		VertexBufferBlock.pushVertexData(mTexCoordBuffer, 0, TEXCOORD_FLOAT_ELEMENTS);
		VertexBufferBlock.pushVertexData(mMaskTexCoordBuffer, 0, TEXCOORD_FLOAT_ELEMENTS);
	}

	@Override
	public void onTextureInvalidate() {
		if (mBitmapTexture != null) {
			mBitmapTexture.onTextureInvalidate();
		}
		if (mFilterTexture != null) {
			mFilterTexture.onTextureInvalidate();
		}
	}

	@Override
	public void clear() {
		if (mReferenceCount <= 0 || --mReferenceCount > 0) {
			return;
		}
		unregister();
		if (mBitmapTexture != null) {
			mBitmapTexture.clear();
			mBitmapTexture = null;
		}
		if (mFilterTexture != null) {
			mFilterTexture.clear();
			mFilterTexture = null;
		}

	}

	/**
	 * 功能简述: 设置新的原图
	 * <br>功能详细描述:
	 * <br>注意: 会将当前的纹理释放
	 * 
	 * @param texture
	 * 
	 * @hide
	 */
	public void setTexture(BitmapTexture texture) {
		if (mBitmapTexture != texture && mBitmapTexture != null) {
			mBitmapTexture.clear();
		}
		mBitmapTexture = texture;
	}

	/**
	 * <br>功能简述: 设置用作蒙板的位图，它的alpha值会与原图对应像素相乘。
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param drawable
	 */
	public void setFilterBitmap(BitmapDrawable drawable) {
		final Bitmap bitmap = drawable.getBitmap();
		mFilterTexture = BitmapTexture.createSharedTexture(bitmap);
	}

	/**
	 * 绑定纹理跟模板纹理
	 */
	boolean bindTexture(RenderContext context) {
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		if (!(context.texture.bind())) {
			return false;
		}
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		return !(mFilterTexture == null || !mFilterTexture.bind());

	}

	/**
	 * <br>功能简述: 获取蒙板纹理
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public Texture getFilterTexture() {
		return mFilterTexture;
	}

	/**
	 * <br>功能简述: 设置纹理坐标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param texCoord  长度为8，分别是左上角，左下角，右上角，右下角的顶点纹理坐标
	 * @see {@link #setMashTexCoord(float[])}
	 */
	public void setTexCoord(float[] texCoord) {
		System.arraycopy(texCoord, 0, mTexCoordBuffer, 0, TEXCOORD_FLOAT_ELEMENTS);
	}

	/**
	 * <br>功能简述: 设置蒙板纹理坐标
	 * <br>功能详细描述: 如果需要设置蒙板和原图的相对位置，可以对蒙板相对与原图做一个变换，
	 * 然后将变换矩阵的逆变换乘以默认的纹理坐标，得到新的坐标，用作参数。以下例子可以将掩码图
	 * 绕着中心顺时针旋转90度：
	 * <pre><code>
	 * 	android.graphics.Matrix matrix = new android.graphics.Matrix();
	 * 	matrix.preRotate(90, 0.5f, 0.5f);
	 * 	matrix.invert(matrix);
	 * 	float[] maskTexcoord = new float[] { 0, 0, 0, 1, 1, 0, 1, 1 };
	 * 	matrix.mapPoints(maskTexcoord);
	 * 	mDrawable.setMashTexCoord(maskTexcoord);
	 * </code></pre>
	 * 注意平移变换或者变换中心要按原图尺寸归一化，否则，需要增加额外的两个缩放变换：
	 * <pre><code>
	 * 	matrix.preRotate(90, mDrawable.getIntrinsicWidth() * 0.5f, mDrawable.getIntrinsicHeight() * 0.5f);
	 * 	matrix.preScale(mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
	 * 	matrix.postScale(1f / mDrawable.getIntrinsicWidth(), 1f / mDrawable.getIntrinsicHeight());
	 * </code></pre>
	 * 
	 * <br>注意: 如果需要对蒙板做动画，那么上面的代码就不要每次都new出对象，避免造成内存垃圾。
	 * <br>另外，也可以不用invert方法求逆的，可以将变换顺序和变换参数都反过来，即利用矩阵逆的性质：
	 * <br>(A * B)^-1 = B^-1 * A^-1。
	 * @param texCoord 长度为8，分别是左上角，左下角，右上角，右下角的顶点纹理坐标
	 * @see {@link #setTexCoord(float[])}
	 */
	public void setMashTexCoord(float[] texCoord) {
		//XXX: 函数名称拼写错了
		System.arraycopy(texCoord, 0, mMaskTexCoordBuffer, 0, TEXCOORD_FLOAT_ELEMENTS);
	}

	private final Renderable mRenderable = new Renderable() {
		final static int DATA_SIZE = POSITION_FLOAT_ELEMENTS + TEXCOORD_FLOAT_ELEMENTS * 2;

		@Override
		public void run(long timeStamp, RenderContext context) {
			VertexBufferBlock.popVertexData(this);
			
			if (context.texture == null || !bindTexture(context)) {
				VertexBufferBlock.popVertexData(null, 0, DATA_SIZE);
				return;
			}
			TextureShader shader = (TextureShader) context.shader;
			if (shader == null || !shader.bind()) {
				VertexBufferBlock.popVertexData(null, 0, DATA_SIZE);
				return;
			}
			shader.setAlpha(context.alpha);
			shader.setMaskColor(context.color);
			shader.setMatrix(context.matrix, 0);
			((FilterShader) shader).bindTextures(0, 1);

			VertexBufferBlock.rewindReadingBuffer(DATA_SIZE);
			FloatBuffer positionBuffer = VertexBufferBlock.popVertexData(POSITION_FLOAT_ELEMENTS);
			shader.setPosition(positionBuffer, POSITION_COMPONENT);
			FloatBuffer texCoordBuffer = VertexBufferBlock.popVertexData(TEXCOORD_FLOAT_ELEMENTS);
			shader.setTexCoord(texCoordBuffer, TEXCOORD_COMPONENT);
			FloatBuffer maskTexCoordBuffer = VertexBufferBlock.popVertexData(TEXCOORD_FLOAT_ELEMENTS);
			((FilterShader) shader).setMaskTexCoord(maskTexCoordBuffer, TEXCOORD_COMPONENT);
			
			GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
			//绘制完取消绑定纹理单元1，激活纹理单元0.
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		}
	};
}

/**
 * <br>类描述: 实现蒙板效果的着色器
 * <br>功能详细描述:
 * 
 * @author  oulingmei
 */
class FilterShader extends TextureShader {
	int muTexHandle;			//纹理引用id
	int muMaskTexHandle;		//模板纹理引用id
	int muMMatrixHandle;		//MVP矩阵
	int maMaskTexCoordHandle;	//蒙板纹理坐标引用id
	BitmapTexture mFilterTexture;
	BitmapTexture mBitmapTexture;

	public FilterShader(Resources res, String vertexFile, String fragmentFile) {
		super(res, vertexFile, fragmentFile);
	}

	public FilterShader(String vertexSource, String fragmentSource) {
		super(vertexSource, fragmentSource);
	}

	@Override
	protected boolean onProgramCreated() {
		if (!super.onProgramCreated()) {
			return false;
		}
		muMaskTexHandle = getUniformLocation("sTextureTemplate");
		muTexHandle = getUniformLocation("sTexture");
		maMaskTexCoordHandle = getAttribLocation("aMaskTexCoord");

		return true;
	}

	@Override
	protected void onProgramBind() {
		super.onProgramBind();
		GLES20.glEnableVertexAttribArray(maMaskTexCoordHandle);
	}

	/**
	 * 
	 * @param maskTexCoordTextureNum
	 *            作为蒙板的纹理使用的纹理块
	 * @param texCoordTextureNum
	 *            效果纹理自身使用的纹理块
	 * 
	 */
	public void bindTextures(int maskTexCoordTextureNum, int texCoordTextureNum) {
		GLES20.glUniform1i(muMaskTexHandle, maskTexCoordTextureNum);
		GLES20.glUniform1i(muTexHandle, texCoordTextureNum);
	}

	public void setMaskTexCoord(Buffer ptr, int component) {
		GLES20.glVertexAttribPointer(maMaskTexCoordHandle, component,
				GLES20.GL_FLOAT, false, 0, ptr);
	}

	@Override
	public String toString() {
		return "TextureShader#FilterShader";
	}
}
