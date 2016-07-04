package com.graphics.engine.gl.graphics.ext;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;

import com.graphics.engine.gl.graphics.BitmapTexture;
import com.graphics.engine.gl.graphics.GLDrawable;
import com.graphics.engine.gl.graphics.GLShaderProgram;
import com.graphics.engine.gl.graphics.GLShaderWrapper;
import com.graphics.engine.gl.graphics.RenderContext;
import com.graphics.engine.gl.graphics.TextureShader;
import com.graphics.engine.gl.util.NdkUtil;

/**
 * 
 * <br>类描述: 支持设置色调的着色器
 * <br>功能详细描述: 
 * 先使用{@link #setDrawableHsvEnabled(GLDrawable)}将着色器设置给图片，
 * 再调用{@link #setHue(float)} 改变图片的色调。
 * 
 * @author  dengweiming
 * @date  [2012-11-21]
 */
public class HSVShaderWrapper extends GLShaderWrapper {
	
	public HSVShaderWrapper() {
		super();
		initInternalShaders();
	}

	private final static float CIRCLE_DEGREES = 360.0f;
	private final static float SCALE = 6.0f / CIRCLE_DEGREES; //CHECKSTYLE IGNORE
	private float mHue;

	private static HSVShader sHSVShader;
	
	static void initInternalShaders() {
		if (sHSVShader == null) {
			sHSVShader = new HSVShader(TextureShader.getDefaultVertexShaderSource(), TEXTURE_SET_HUE_FRAG);
			sHSVShader.registerStatic();
		}
	}
	
	/**
	 * <br>功能简述: 将图片转化为HSV内部存储格式（预处理），并启用色调着色器
	 * <br>功能详细描述:
	 * 会利用{@link NdkUtil#convertToHSV(android.graphics.Bitmap, boolean)} 转化为HSV内部存储格式。
	 * <br>注意: 原图片有可能被修改掉内容。
	 * @param drawable
	 * @return	是否成功
	 */
	public boolean setDrawableHsvEnabled(GLDrawable drawable) {
		Bitmap bitmap = drawable.getBitmap();
		if (bitmap == null) {
			return true;
		}
		if (!bitmap.isMutable()) {
			try {
				bitmap = bitmap.copy(Config.ARGB_8888, true);
				NdkUtil.convertToHSV(bitmap, false);
			} catch (OutOfMemoryError e) {
				return false;
			}
			drawable.setShaderWrapper(this);
			drawable.setTexture(new BitmapTexture(bitmap));
		} else {
			NdkUtil.convertToHSV(bitmap, false);
			drawable.setShaderWrapper(this);
		}
		return true;
	}

	private static float saturate(float x) {
		return Math.max(0, Math.min(x, 1));
	}

	/**
	 * <br>功能简述: 设置色调
	 * <br>功能详细描述: 色调是循环的360度的，其中0和360是红色，120是绿色，240是蓝色。
	 * <br>注意: 色调是绘制的时候作用到像素上的（在线，非预处理），不会修改图片内容。
	 * @param hue [0..360)，超出范围会被自动规约到这个范围。
	 */
	public void setHue(float hue) {
		hue %= CIRCLE_DEGREES;
		if (hue < 0) {
			hue += CIRCLE_DEGREES;
		}
		mHue = hue * SCALE;
	}

	@Override
	public void onDraw(RenderContext context) {
		final float[] color = context.color;
		final float alpha = context.alpha;
		//CHECKSTYLE IGNORE 3 LINES
		color[0] = saturate(Math.abs(mHue - 3) - 1) * alpha;
		color[1] = saturate(2 - Math.abs(mHue - 2)) * alpha;
		color[2] = saturate(2 - Math.abs(mHue - 4)) * alpha;
	}

	@Override
	public GLShaderProgram onRender(RenderContext context) {
		HSVShader shader = sHSVShader;
		if (shader == null || !shader.bind()) {
			return null;
		}
		shader.setAlpha(context.alpha);
		shader.setHues(context.color);
		shader.setMatrix(context.matrix, 0);
		return sHSVShader;
	}

	/**
	 * 
	 * <br>类描述: 实现改变色调的着色器
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2012-11-21]
	 */
	private static class HSVShader extends TextureShader {
		private final static String TAG = "HSVShader";
		
		public HSVShader(Resources res, String vertexFile, String fragmentFile) {
			super(res, vertexFile, fragmentFile);
		}
		
		public HSVShader(String vertexSource, String fragmentSource) {
			super(vertexSource, fragmentSource);
		}

		int muAlphaHandle;
		int muHuesHandle;

		@Override
		protected boolean onProgramCreated() {
			if (!super.onProgramCreated()) {
				return false;
			}
			muAlphaHandle = getUniformLocation("uAlpha");
			muHuesHandle = getUniformLocation("uHues");
			return true;
		}

		@Override
		public void setAlpha(float alpha) {
			GLES20.glUniform1f(muAlphaHandle, alpha);
		}

		public void setHues(float[] hues) {
			GLES20.glUniform3fv(muHuesHandle, 1, hues, 0);
		}
		
		@Override
		public String toString() {
			return TAG;
		}
		
	}

	@Override
	protected boolean onProgramCreated() {
		return true;
	}

	@Override
	protected void onProgramBind() {

	}
	
	//================ texture_set_hue.frag ================
	final static String TEXTURE_SET_HUE_FRAG =
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	vec3 uHues;" + "\n" + 
		"uniform	float uAlpha;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	vec4 dst = texture2D(sTexture, vTextureCoord);" + "\n" + 
		"	gl_FragColor = vec4(uHues * dst.g + dst.b * uAlpha, dst.a * uAlpha);" + "\n" + 
		"}" + "\n" + 
		"";

}
