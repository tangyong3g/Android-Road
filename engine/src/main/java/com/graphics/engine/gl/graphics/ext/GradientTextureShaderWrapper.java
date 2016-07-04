package com.graphics.engine.gl.graphics.ext;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import com.graphics.engine.gl.graphics.GLShaderProgram;
import com.graphics.engine.gl.graphics.GLShaderWrapper;
import com.graphics.engine.gl.graphics.RenderContext;
import com.graphics.engine.gl.graphics.TextureShader;
import com.graphics.engine.gl.util.IBufferFactory;

import android.opengl.GLES20;

/**
 * <br>类描述: 实现渐变透明贴图效果的着色器封装器
 * <br>功能详细描述:
 * 使用{@link #setDrawableVertexAlpha(int, int, int, int)}或者
 * {@link #setDrawableVertexColor(int, int, int, int)}，使贴图渐变透明。
 * 
 * @author  dengweiming
 * @date  [2013-11-15]
 */
public class GradientTextureShaderWrapper extends GLShaderWrapper {
	private static final int SINGLE_SPRITE_MODE = -1;

	static GradientTextureShader sShader;
	static FloatBuffer sBuffer;
	int[] mColors;

	public GradientTextureShaderWrapper() {
		if (sShader == null) {
			sShader = new GradientTextureShader();
		}
	}

	/** Encodes the ABGR int color as a float. The high bits are masked to avoid using floats in the NaN range, which unfortunately
	 * means the full range of alpha cannot be used. See {@link Float#intBitsToFloat(int)} javadocs. */
	public static float intToFloatColor(int color) {
		return Float.intBitsToFloat(color & 0xfeffffff);
	}

	/**
	 * <br>功能简述: 设置{@link GLDrawable}四个顶点的颜色
	 * <br>功能详细描述: 这些颜色会和{@link GLDrawable#setAlpha(int)}以及{@link GLCanvas#setAlpha(int)}相乘，
	 * 最后和纹理的颜色相乘，得到最终颜色。
	 * <br>注意:
	 * @param lt 左上角的颜色
	 * @param lb 左下角的颜色
	 * @param rt 右上角的颜色
	 * @param rb 右下角的颜色
	 * 
	 * @see {@link #setDrawableVertexAlpha(int, int, int, int)}
	 */
	public void setDrawableVertexColor(int lt, int lb, int rt, int rb) {
		if (mColors == null) {
			mColors = new int[4];
		}
		mColors[0] = lt;
		mColors[1] = lb;
		mColors[2] = rt;
		mColors[3] = rb;
	}

	/**
	 * <br>功能简述: 设置{@link GLDrawable}四个顶点的不透明度
	 * <br>功能详细描述: 这些不透明度会和{@link GLDrawable#setAlpha(int)}以及{@link GLCanvas#setAlpha(int)}相乘，
	 * 最后和纹理的颜色相乘，得到最终颜色。
	 * <br>注意:
	 * @param lt 左上角的不透明度
	 * @param lb 左下角的不透明度
	 * @param rt 右上角的不透明度
	 * @param rb 右下角的不透明度
	 * 
	 * @see {@link #setDrawableVertexColor(int, int, int, int)}
	 */
	public void setDrawableVertexAlpha(int lt, int lb, int rt, int rb) {
		/** @formatter:off */
		setDrawableVertexColor(
				(lt << 24) | 0xFFFFFF, 
				(lb << 24) | 0xFFFFFF, 
				(rt << 24) | 0xFFFFFF, 
				(rb << 24) | 0xFFFFFF);
		/** @formatter:on */
	}

	@Override
	public void onDraw(RenderContext context) {
		if (mColors != null) {
			float fadeAlpha = context.alpha;
			context.alpha = SINGLE_SPRITE_MODE;
			int[] src = mColors;
			float[] dst = context.color;
			for (int i = 0; i < src.length; ++i) {
				int color = src[i];
				if (fadeAlpha < 1) {
					color = (int) ((color >>> 24) * fadeAlpha) << 24 | (color & 0xFFFFFF);
				}
				dst[i] = intToFloatColor(color);
			}
		}
	}

	@Override
	public GLShaderProgram onRender(RenderContext context) {
		GradientTextureShader shader = sShader;
		if (shader == null || !shader.bind()) {
			return null;
		}
		shader.setMatrix(context.matrix, 0);
		if (context.alpha == SINGLE_SPRITE_MODE) {
			if (sBuffer == null) {
				sBuffer = IBufferFactory.newFloatBuffer(4);
			}
			sBuffer.position(0);
			sBuffer.put(context.color);
			sBuffer.position(0);
			shader.setColor(sBuffer, 0);
		}
		return shader;
	}

	@Override
	protected boolean onProgramCreated() {
		return true;
	}

	@Override
	protected void onProgramBind() {

	}

	/**
	 * 
	 * <br>类描述: 实现渐变透明贴图效果的着色器
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2013-11-15]
	 */
	public final static class GradientTextureShader extends TextureShader {
		int maColorHandle;	//顶点颜色属性引用id 

		public GradientTextureShader() {
			super(GRADIENT_VERT, GRADIENT_FRAG);
			registerStatic();
		}

		/**
		 * <br>功能简述: 设置顶点颜色属性
		 * <br>功能详细描述: 颜色 <em>不</em> 需要alpha-premutiplied。
		 * <br>如果<var>ptr</var>为{@link FloatBuffer}，则需要使用
		 * {@link GradientTextureShaderWrapper#intToFloatColor(int)}
		 * 将整型的颜色值转换为浮点型存储，其中掩码的作用避免产生NaN,但会导致
		 * alpha被限制为偶数值（影响可以忽略）。
		 * <br>注意: 在GL线程上调用
		 * @param ptr
		 * @param stride 相邻两个顶点颜色之间的内存距离（单位为字节）。如果为0,表示紧密排列。
		 */
		public void setColor(Buffer ptr, int stride) {
			GLES20.glVertexAttribPointer(maColorHandle, 4, GLES20.GL_UNSIGNED_BYTE, true, stride, ptr);
		}

		@Override
		protected boolean onProgramCreated() {
			maColorHandle = getAttribLocation("aColor");
			return super.onProgramCreated();
		}

		@Override
		protected void onProgramBind() {
			GLES20.glEnableVertexAttribArray(maColorHandle);
			super.onProgramBind();
		}

	}

	/** @formatter:off */
	private final static String GRADIENT_VERT =
		"uniform		mat4 uMVPMatrix;" + "\n" + 
		"attribute	vec3 aPosition;" + "\n" + 
		"attribute	vec2 aTexCoord;" + "\n" + 
		"attribute	vec4 aColor;" + "\n" + 
		"varying		vec2 vTextureCoord;" + "\n" + 
		"varying		vec4 vColor;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	gl_Position = uMVPMatrix * vec4(aPosition, 1.);" + "\n" + 
		"	vTextureCoord = aTexCoord;" + "\n" + 
		//在此转换为alpha-premultiplied格式
		"	vColor = vec4(aColor.rgb * aColor.a, aColor.a);" + "\n" + 
		"}" + "\n" + 
		"";

	private final static String GRADIENT_FRAG =
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"varying		vec4 vColor;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	gl_FragColor = texture2D(sTexture, vTextureCoord) * vColor;" + "\n" + 
		"}" + "\n" + 
		"";
	/** @formatter:on */

}
