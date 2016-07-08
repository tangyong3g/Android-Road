package com.graphics.engine.graphics.ext;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.graphics.engine.graphics.GLShaderProgram;
import com.graphics.engine.graphics.GLShaderWrapper;
import com.graphics.engine.graphics.RenderContext;
import com.graphics.engine.graphics.StaticTextureListener;
import com.graphics.engine.graphics.TextureShader;

/**
 * 
 * <br>类描述: 实现蒙板透明度过滤达到裁剪效果的着色器封装器
 * <br>功能详细描述: 
 * 使用{@link #getAlphaMaskShaderWrapper()}获得一个效果实例。
 * 
 * @author  chenjingmian
 * @date  [2014-2-19]
 */
public class AlphaMaskShaderWrapper extends GLShaderWrapper {
	
	private static AlphaMaskShader sShader;
	private AlphaMaskShader mAlphaShader;
	
	private static final float ALPHA_THRESHHOLD = 0.5f;
	
	private static void initInternalShaders() {
		if (sShader == null) {
			sShader = new AlphaMaskShader(TextureShader.getDefaultVertexShaderSource(),
					ALPHA_MASK_FRAG);
			sShader.registerStatic();
		}
	}

	/**
	 * <br>功能简述: 获取一个实例
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public static AlphaMaskShaderWrapper getAlphaMaskShaderWrapper() {
		AlphaMaskShaderWrapper wrapper = new AlphaMaskShaderWrapper();
		return wrapper;
	}
	
	/**
	 * 创建一个实例
	 */
	public AlphaMaskShaderWrapper() {
		initInternalShaders();
		mAlphaShader = sShader;
	}
	
	@Override
	public void onDraw(RenderContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public GLShaderProgram onRender(RenderContext context) {
		AlphaMaskShader shader = mAlphaShader;
		if (shader == null || !shader.bind()) {
			return null;
		}
		shader.setAlphaThreshhold(ALPHA_THRESHHOLD); //(context.alpha);
		shader.setMatrix(context.matrix, 0);
		return shader;
	}

	@Override
	protected boolean onProgramCreated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onProgramBind() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 * <br>类描述: 封装的着色器
	 * <br>功能详细描述:
	 * 
	 * @author  chenjingmian
	 * @date  [2014-2-19]
	 */
	static class AlphaMaskShader extends TextureShader implements StaticTextureListener {
		int muAlphaHandle;
		
		public AlphaMaskShader(Resources res, String vertexFile, String fragmentFile) {
			super(res, vertexFile, fragmentFile);
		}
		
		public AlphaMaskShader(String vertexSource, String fragmentSource) {
			super(vertexSource, fragmentSource);
		}
		
		@Override
		protected boolean onProgramCreated() {
			if (!super.onProgramCreated()) {
				return false;
			}
			muAlphaHandle = getUniformLocation("uAlpha");
			return  true;
		}
		
		public void setAlphaThreshhold(float alpha) {
			GLES20.glUniform1f(muAlphaHandle, alpha);
		}
		
		@Override
		public String toString() {
			return "TextureShader#AlphaMaskShader";
		}
		
		@Override
		public void onTextureInvalidate() {
			super.onTextureInvalidate();
		}
	}
	
	/**
	 * 像素的alpha小于设定的阈值，丢弃
	 */
	private final static String ALPHA_MASK_FRAG = 
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	float uAlpha;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	if (texture2D(sTexture, vTextureCoord).a < uAlpha) discard; " + "\n" + 
		"	gl_FragColor = vec4(1., 1., 1., 1.);" + "\n" + 
		"} " + "\n" + 
		"";
}
