package com.graphics.engine.gl.graphics.filters;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.graphics.GLShaderProgram;
import com.graphics.engine.gl.graphics.GLShaderWrapper;
import com.graphics.engine.gl.graphics.RenderContext;
import com.graphics.engine.gl.graphics.Renderable;
import com.graphics.engine.gl.graphics.TextureShader;


/**
 * 
 * <br>类描述: 纯色发光效果的着色器
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-4-1]
 */
class GlowShaderWrapper extends GLShaderWrapper {
	BlurShader mShader;
	int mKernelRadius;
	SetGlowColorRenderable mSetGlowColorRenderable = new SetGlowColorRenderable();
	SetGlowInvTargetSizeRenderable mSetGlowInvTargetSizeRenderable = new SetGlowInvTargetSizeRenderable();
	
	private GlowShaderWrapper(BlurShader shader, int radius) {
		super();
		mShader = shader;
		mKernelRadius = radius;
	}

	private static final int[] KERNEL_RADIUS = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, };
	private static final int[] RADIUS_MAP = new int[30]; //CHECKSTYLE IGNORE
	private static BlurShader[] sBlurShaders;
	private static GlowShaderWrapper[] sWrappers;
	private static boolean sInstanced;
	
	static GlowShaderWrapper getInstance(Resources res, float radius) {
		if (!sInstanced) {
			sInstanced = true;

			final int count = KERNEL_RADIUS.length;
			sBlurShaders = new BlurShader[count];
			sWrappers = new GlowShaderWrapper[count];
			for (int i = 1; i < count; ++i) {
				int r = KERNEL_RADIUS[i];
//				sBlurShaders[i] = new BlurShader(res, "blur" + r + ".vert", "blur" + r + ".frag");
				sBlurShaders[i] = new BlurShader(VERTEX_SHADER_STRINGS[i], FRAGMENT_SHADER_STRINGS[i]);
				sBlurShaders[i].registerStatic();
				sWrappers[i] = new GlowShaderWrapper(sBlurShaders[i], r);
			}
			
			int maxStep = 4; //CHECKSTYLE IGNORE
			for (int r = 1; r < RADIUS_MAP.length; ++r) {
				int bestIndex = KERNEL_RADIUS.length - 1;
				float bestErr = r;
				for (int i = bestIndex; i > 0; --i) {
					int kr = KERNEL_RADIUS[i];
					int step1 = (int) Math.floor(r / kr);
					float err1 = Math.abs(step1 * kr - r);
					int step2 = (int) Math.ceil(r / kr);
					float err2 = Math.abs(step2 * kr - r);
					if (err1 < err2) {
						if (step1 <= maxStep && err1 <= bestErr) {
							bestIndex = i;
							bestErr = err1;
						}
					} else {
						if (step2 <= maxStep && err2 <= bestErr) {
							bestIndex = i;
							bestErr = err2;
						}
					}
				}
				RADIUS_MAP[r] = bestIndex;
			}
			RADIUS_MAP[0] = 1;
			RADIUS_MAP[5] = 3;	//特殊处理 //CHECKSTYLE IGNORE
			RADIUS_MAP[23] = 6;	//特殊处理 //CHECKSTYLE IGNORE
		}
		
		int index = RADIUS_MAP[Math.min(RADIUS_MAP.length - 1, Math.round(radius))];
		return sWrappers[index];
	}

	@Override
	public void onDraw(RenderContext context) {
	}
	
	@Override
	public GLShaderProgram onRender(RenderContext context) {
		BlurShader shader = getShader();
		if (shader == null) {
			return null;
		}
		shader.setMatrix(context.matrix, 0);

		return shader;
	}

	BlurShader getShader() {
		if (mShader == null || !mShader.bind()) {
			return null;
		}
		return mShader;
	}
	
	void setGlowColor(GLCanvas canvas, float strength, float[] alphaPremultipliedColor) {
		RenderContext context = RenderContext.acquire();
		context.shader = this;
		context.alpha = strength;
		context.color[0] = alphaPremultipliedColor[0];
		context.color[1] = alphaPremultipliedColor[1];
		context.color[2] = alphaPremultipliedColor[2];
		context.color[3] = alphaPremultipliedColor[3];	//CHECKSTYLE IGNORE
		canvas.addRenderable(mSetGlowColorRenderable, context);
	}
	
	void setGlowInvTargetSize(GLCanvas canvas, float u, float v) {
		RenderContext context = RenderContext.acquire();
		context.shader = this;
		context.color[0] = u;
		context.color[1] = v;
		canvas.addRenderable(mSetGlowInvTargetSizeRenderable, context);
	}
	
	int getKernalRadius() {
		return mKernelRadius;
	}

	/**
	 * 类描述: 实现纯色发光效果的着色器
	 * 功能详细描述:
	 * 
	 * @author dengweiming
	 * @date [2012-11-21]
	 */
	static class BlurShader extends TextureShader {
		private final static String TAG = "BlurShader2";

		public BlurShader(Resources res, String vertexFile, String fragmentFile) {
			super(res, vertexFile, fragmentFile);
		}

		public BlurShader(String vertexSource, String fragmentSource) {
			super(vertexSource, fragmentSource);
		}

		int muInvTargetSizeHandle;
		int muColorHandle;
		int muStrengthHandle;

		@Override
		protected boolean onProgramCreated() {
			if (!super.onProgramCreated()) {
				return false;
			}
			muInvTargetSizeHandle = getUniformLocation("uInvTargetSize");
			muColorHandle = getUniformLocation("uColor");
			muStrengthHandle = getUniformLocation("uStrength");
			return true;
		}

		@Override
		public String toString() {
			return TAG;
		}

		void setInvTargetSize(float u, float v) {
			GLES20.glUniform2f(muInvTargetSizeHandle, u, v);
		}

		void setColor(float[] color) {
			GLES20.glUniform4fv(muColorHandle, 1, color, 0);
		}

		void setStrength(float strength) {
			GLES20.glUniform1f(muStrengthHandle, strength);
		}
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
	 * <br>类描述: 设置发光时目标大小的倒数的Renderable
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2013-4-1]
	 */
	class SetGlowInvTargetSizeRenderable implements Renderable {

		@Override
		public void run(long timeStamp, RenderContext context) {
			GlowShaderWrapper shaderWrapper = (GlowShaderWrapper) context.shader;
			GlowShaderWrapper.BlurShader shader = shaderWrapper.getShader();
			if (shader == null) {
				return;
			}
			final float[] args = context.color;
			shader.setInvTargetSize(args[0], args[1]);

		}

	}

	/**
	 *
	 * <br>类描述: 设置发光颜色的Renderable
	 * <br>功能详细描述:
	 *
	 * @author  dengweiming
	 * @date  [2013-4-1]
	 */
	class SetGlowColorRenderable implements Renderable {

		@Override
		public void run(long timeStamp, RenderContext context) {
			GlowShaderWrapper shaderWrapper = (GlowShaderWrapper) context.shader;
			GlowShaderWrapper.BlurShader shader = shaderWrapper.getShader();
			if (shader == null) {
				return;
			}
			shader.setStrength(context.alpha);
			shader.setColor(context.color);

		}

	}
	
	//================ blur1.vert ================
	final static String BLUR1_VERT =
		"uniform mat4 uMVPMatrix;" + "\n" + 
		"attribute vec3 aPosition;" + "\n" + 
		"attribute vec2 aTexCoord;" + "\n" + 
		"uniform vec2 uInvTargetSize;" + "\n" + 
		"" + "\n" + 
		"varying vec2 vTexL1;" + "\n" + 
		"varying vec2 vTex;" + "\n" + 
		"varying vec2 vTexR1;" + "\n" + 
		"" + "\n" + 
		"void main() " + "\n" + 
		"{ " + "\n" + 
		"   gl_Position = uMVPMatrix * vec4(aPosition, 1);" + "\n" + 
		"   vTexL1 = aTexCoord + uInvTargetSize * -1.;" + "\n" + 
		"   vTex   = aTexCoord;" + "\n" + 
		"   vTexR1 = aTexCoord + uInvTargetSize *  1.;" + "\n" + 
		"}" + "\n" + 
		"";

	//================ blur1.frag ================
	final static String BLUR1_FRAG =
		"precision mediump float;" + "\n" + 
		"uniform sampler2D sTexture;" + "\n" + 
		"uniform vec4 uColor;" + "\n" + 
		"uniform float uStrength;" + "\n" + 
		"" + "\n" + 
		"varying vec2 vTexL1;" + "\n" + 
		"varying vec2 vTex;" + "\n" + 
		"varying vec2 vTexR1;" + "\n" + 
		"" + "\n" + 
		"void main() " + "\n" + 
		"{" + "\n" + 
		"	float aL1 = texture2D(sTexture, vTexL1).a;" + "\n" + 
		"	float a = texture2D(sTexture, vTex).a;" + "\n" + 
		"	float aR1 = texture2D(sTexture, vTexR1).a;" + "\n" + 
		"	float sum = aL1 + a * 2. + aR1;" + "\n" + 
		"" + "\n" + 
		"	gl_FragColor = uColor * (sum * 0.25 * uStrength); // r=1, (r+1)^-2=0.25" + "\n" + 
		"" + "\n" + 
		"} " + "\n" + 
		"";

	//================ blur2.vert ================
	final static String BLUR2_VERT =
		"uniform mat4 uMVPMatrix;" + "\n" + 
		"attribute vec3 aPosition;" + "\n" + 
		"attribute vec2 aTexCoord;" + "\n" + 
		"uniform vec2 uInvTargetSize;" + "\n" + 
		"" + "\n" + 
		"varying vec2 vTexL1;" + "\n" + 
		"varying vec2 vTexL2;" + "\n" + 
		"varying vec2 vTex;" + "\n" + 
		"varying vec2 vTexR1;" + "\n" + 
		"varying vec2 vTexR2;" + "\n" + 
		"" + "\n" + 
		"void main() " + "\n" + 
		"{ " + "\n" + 
		"   gl_Position = uMVPMatrix * vec4(aPosition, 1);" + "\n" + 
		"   vTexL1 = aTexCoord + uInvTargetSize * -1.;" + "\n" + 
		"   vTexL2 = aTexCoord + uInvTargetSize * -2.;" + "\n" + 
		"   vTex   = aTexCoord;" + "\n" + 
		"   vTexR1 = aTexCoord + uInvTargetSize *  1.;" + "\n" + 
		"   vTexR2 = aTexCoord + uInvTargetSize *  2.;" + "\n" + 
		"}" + "\n" + 
		"";

	//================ blur2.frag ================
	final static String BLUR2_FRAG =
		"precision mediump float;" + "\n" + 
		"uniform sampler2D sTexture;" + "\n" + 
		"uniform vec4 uColor;" + "\n" + 
		"uniform float uStrength;" + "\n" + 
		"" + "\n" + 
		"varying vec2 vTexL1;" + "\n" + 
		"varying vec2 vTexL2;" + "\n" + 
		"varying vec2 vTex;" + "\n" + 
		"varying vec2 vTexR1;" + "\n" + 
		"varying vec2 vTexR2;" + "\n" + 
		"" + "\n" + 
		"void main() " + "\n" + 
		"{" + "\n" + 
		"	float aL2 = texture2D(sTexture, vTexL2).a;" + "\n" + 
		"	float aL1 = texture2D(sTexture, vTexL1).a;" + "\n" + 
		"	float a   = texture2D(sTexture, vTex).a;" + "\n" + 
		"	float aR1 = texture2D(sTexture, vTexR1).a;" + "\n" + 
		"	float aR2 = texture2D(sTexture, vTexR2).a;" + "\n" + 
		"	float sum = aL2 + aL1 * 2. + a * 3. + aR1 * 2. + aR2;" + "\n" + 
		"" + "\n" + 
		"	gl_FragColor = uColor * (sum * 0.1111 * uStrength); // r=2, (r+1)^-2=0.1111" + "\n" + 
		"" + "\n" + 
		"} " + "\n" + 
		"";

	//================ blur3.vert ================
	final static String BLUR3_VERT =
		"uniform mat4 uMVPMatrix;" + "\n" + 
		"attribute vec3 aPosition;" + "\n" + 
		"attribute vec2 aTexCoord;" + "\n" + 
		"uniform vec2 uInvTargetSize;" + "\n" + 
		"" + "\n" + 
		"varying vec2 vTexL1;" + "\n" + 
		"varying vec2 vTexL2;" + "\n" + 
		"varying vec2 vTexL3;" + "\n" + 
		"varying vec2 vTex;" + "\n" + 
		"varying vec2 vTexR1;" + "\n" + 
		"varying vec2 vTexR2;" + "\n" + 
		"varying vec2 vTexR3;" + "\n" + 
		"" + "\n" + 
		"void main() " + "\n" + 
		"{ " + "\n" + 
		"   gl_Position = uMVPMatrix * vec4(aPosition, 1);" + "\n" + 
		"   vTexL1 = aTexCoord + uInvTargetSize * -1.;" + "\n" + 
		"   vTexL2 = aTexCoord + uInvTargetSize * -2.;" + "\n" + 
		"   vTexL3 = aTexCoord + uInvTargetSize * -3.;" + "\n" + 
		"   vTex   = aTexCoord;" + "\n" + 
		"   vTexR1 = aTexCoord + uInvTargetSize *  1.;" + "\n" + 
		"   vTexR2 = aTexCoord + uInvTargetSize *  2.;" + "\n" + 
		"   vTexR3 = aTexCoord + uInvTargetSize *  3.;" + "\n" + 
		"} " + "\n" + 
		"";

	//三星Galaxy Nexus使用texture2D得到的颜色是低精度的，乘以4就会溢出，所以最好先使用一个float转储再乘。
	
	//================ blur3.frag ================
	final static String BLUR3_FRAG =
		"precision mediump float;" + "\n" + 
		"uniform sampler2D sTexture;" + "\n" + 
		"uniform vec4 uColor;" + "\n" + 
		"uniform float uStrength;" + "\n" + 
		"" + "\n" + 
		"varying vec2 vTexL1;" + "\n" + 
		"varying vec2 vTexL2;" + "\n" + 
		"varying vec2 vTexL3;" + "\n" + 
		"varying vec2 vTex;" + "\n" + 
		"varying vec2 vTexR1;" + "\n" + 
		"varying vec2 vTexR2;" + "\n" + 
		"varying vec2 vTexR3;" + "\n" + 
		"" + "\n" + 
		"void main() " + "\n" + 
		"{" + "\n" + 
		"	float aL3 = texture2D(sTexture, vTexL3).a;" + "\n" + 
		"	float aL2 = texture2D(sTexture, vTexL2).a;" + "\n" + 
		"	float aL1 = texture2D(sTexture, vTexL1).a;" + "\n" + 
		"	float a   = texture2D(sTexture, vTex).a;" + "\n" + 
		"	float aR1 = texture2D(sTexture, vTexR1).a;" + "\n" + 
		"	float aR2 = texture2D(sTexture, vTexR2).a;" + "\n" + 
		"	float aR3 = texture2D(sTexture, vTexR3).a;" + "\n" + 
		"	float sum = aL3 + aL2 * 2. + aL1 * 3. + a * 4. + aR1 * 3. + aR2 * 2. + aR3;" + "\n" + 
		"" + "\n" + 
		"	gl_FragColor = uColor * (sum * 0.0625 * uStrength); // r=3, (r+1)^-2=0.0625" + "\n" + 
		"" + "\n" + 
		"} " + "\n" + 
		"";
	
	//XXX-------------------------------v
	//某台LG-P880的Tegra 3 GPU，使用之前的blur5和blur6的shader绘制的内容偏上，
	//应该是不支持11个varying vector，虽然使用OpenGL Extensions Viewer看到
	//可以支持15个。将两个vec2打包成一个vec4,可以减少使用的个数。
	//使用GLES20.glGetIntegerv(GLES20.GL_MAX_VARYING_VECTORS, params, 0);
	//得到的结果至少为8，因此blur4~6的shader都要修改掉。
	//但是这样会造成dependent texture reads，对于某些硬件可能会造成性能损失：
	//https://developer.apple.com/library/ios/documentation/3DDrawing/Conceptual/OpenGLES_ProgrammingGuide/BestPracticesforShaders/BestPracticesforShaders.html
	//最后一节Be Aware of Dynamic Texture Lookups。
	//----------------------------------^

	//================ blur4.vert ================
	final static String BLUR4_VERT =
		"uniform mat4 uMVPMatrix;" + "\n" + 
		"attribute vec3 aPosition;" + "\n" + 
		"attribute vec2 aTexCoord;" + "\n" + 
		"uniform vec2 uInvTargetSize;" + "\n" + 
		"" + "\n" + 
		"varying vec4 vTex1;" + "\n" + 
		"varying vec4 vTex2;" + "\n" + 
		"varying vec4 vTex3;" + "\n" + 
		"varying vec4 vTex4;" + "\n" + 
		"varying vec2 vTex;" + "\n" + 
		"" + "\n" + 
		"void main() " + "\n" + 
		"{ " + "\n" + 
		"   gl_Position = uMVPMatrix * vec4(aPosition, 1);" + "\n" + 
		"   vTex1 = vec4(aTexCoord + uInvTargetSize * -1. , aTexCoord + uInvTargetSize *  1.);" + "\n" + 
		"   vTex2 = vec4(aTexCoord + uInvTargetSize * -2. , aTexCoord + uInvTargetSize *  2.);" + "\n" + 
		"   vTex3 = vec4(aTexCoord + uInvTargetSize * -3. , aTexCoord + uInvTargetSize *  3.);" + "\n" + 
		"   vTex4 = vec4(aTexCoord + uInvTargetSize * -4. , aTexCoord + uInvTargetSize *  4.);" + "\n" + 
		"   vTex   = aTexCoord;" + "\n" + 
		"} " + "\n" + 
		"";

	//================ blur4.frag ================
	final static String BLUR4_FRAG =
		"precision mediump float;" + "\n" + 
		"uniform sampler2D sTexture;" + "\n" + 
		"uniform vec4 uColor;" + "\n" + 
		"uniform float uStrength;" + "\n" + 
		"" + "\n" + 
		"varying vec4 vTex1;" + "\n" + 
		"varying vec4 vTex2;" + "\n" + 
		"varying vec4 vTex3;" + "\n" + 
		"varying vec4 vTex4;" + "\n" + 
		"varying vec2 vTex;" + "\n" + 
		"const float s = 0.04;  // r=4, (r+1)^-2" + "\n" + 
		"void main() " + "\n" + 
		"{" + "\n" + 
		"	float a4 = texture2D(sTexture, vTex4.xy).a + texture2D(sTexture, vTex4.zw).a;" + "\n" + 
		"	float a3 = texture2D(sTexture, vTex3.xy).a + texture2D(sTexture, vTex3.zw).a;" + "\n" + 
		"	float a2 = texture2D(sTexture, vTex2.xy).a + texture2D(sTexture, vTex2.zw).a;" + "\n" + 
		"	float a1 = texture2D(sTexture, vTex1.xy).a + texture2D(sTexture, vTex1.zw).a;" + "\n" + 
		"	float a   = texture2D(sTexture, vTex).a;" + "\n" + 
		"   float sum = a * 5. + a1 * 4. + a2 * 3. + a3 * 2. + a4;" + "\n" +
		"	gl_FragColor = uColor * (sum * s * uStrength);" + "\n" + 
		"} " + "\n" + 
		"";
	
	//================ blur5.vert ================
	final static String BLUR5_VERT =
		"uniform mat4 uMVPMatrix;" + "\n" + 
		"attribute vec3 aPosition;" + "\n" + 
		"attribute vec2 aTexCoord;" + "\n" + 
		"uniform vec2 uInvTargetSize;" + "\n" + 
		"" + "\n" + 
		"varying vec4 vTex1;" + "\n" + 
		"varying vec4 vTex2;" + "\n" + 
		"varying vec4 vTex3;" + "\n" + 
		"varying vec4 vTex4;" + "\n" + 
		"varying vec4 vTex5;" + "\n" + 
		"varying vec2 vTex;" + "\n" + 
		"" + "\n" + 
		"void main() " + "\n" + 
		"{ " + "\n" + 
		"   gl_Position = uMVPMatrix * vec4(aPosition, 1);" + "\n" + 
		"   vTex1 = vec4(aTexCoord + uInvTargetSize * -1. , aTexCoord + uInvTargetSize *  1.);" + "\n" + 
		"   vTex2 = vec4(aTexCoord + uInvTargetSize * -2. , aTexCoord + uInvTargetSize *  2.);" + "\n" + 
		"   vTex3 = vec4(aTexCoord + uInvTargetSize * -3. , aTexCoord + uInvTargetSize *  3.);" + "\n" + 
		"   vTex4 = vec4(aTexCoord + uInvTargetSize * -4. , aTexCoord + uInvTargetSize *  4.);" + "\n" + 
		"   vTex5 = vec4(aTexCoord + uInvTargetSize * -5. , aTexCoord + uInvTargetSize *  5.);" + "\n" + 
		"   vTex   = aTexCoord;" + "\n" + 
		"} " + "\n" + 
		"";

	//================ blur5.frag ================
	final static String BLUR5_FRAG =
		"precision mediump float;" + "\n" + 
		"uniform sampler2D sTexture;" + "\n" + 
		"uniform vec4 uColor;" + "\n" + 
		"uniform float uStrength;" + "\n" + 
		"" + "\n" + 
		"varying vec4 vTex1;" + "\n" + 
		"varying vec4 vTex2;" + "\n" + 
		"varying vec4 vTex3;" + "\n" + 
		"varying vec4 vTex4;" + "\n" + 
		"varying vec4 vTex5;" + "\n" + 
		"varying vec2 vTex;" + "\n" + 
		"const float s = 0.0278;  // r=5, s=(r+1)^-2" + "\n" + 
		"void main() " + "\n" + 
		"{" + "\n" + 
		"	float a5 = texture2D(sTexture, vTex5.xy).a + texture2D(sTexture, vTex5.zw).a;" + "\n" + 
		"	float a4 = texture2D(sTexture, vTex4.xy).a + texture2D(sTexture, vTex4.zw).a;" + "\n" + 
		"	float a3 = texture2D(sTexture, vTex3.xy).a + texture2D(sTexture, vTex3.zw).a;" + "\n" + 
		"	float a2 = texture2D(sTexture, vTex2.xy).a + texture2D(sTexture, vTex2.zw).a;" + "\n" + 
		"	float a1 = texture2D(sTexture, vTex1.xy).a + texture2D(sTexture, vTex1.zw).a;" + "\n" + 
		"	float a   = texture2D(sTexture, vTex).a;" + "\n" + 
		"   float sum = a * 6. + a1 * 5. + a2 * 4. + a3 * 3. + a4 * 2. + a5;" + "\n" +
		"	gl_FragColor = uColor * (sum * s * uStrength);" + "\n" + 
		"}    " + "\n" + 
		"";

	//================ blur6.vert ================
	final static String BLUR6_VERT =
		"uniform mat4 uMVPMatrix;" + "\n" + 
		"attribute vec3 aPosition;" + "\n" + 
		"attribute vec2 aTexCoord;" + "\n" + 
		"uniform vec2 uInvTargetSize;" + "\n" + 
		"" + "\n" + 
		"varying vec4 vTex1;" + "\n" + 
		"varying vec4 vTex2;" + "\n" + 
		"varying vec4 vTex3;" + "\n" + 
		"varying vec4 vTex4;" + "\n" + 
		"varying vec4 vTex5;" + "\n" + 
		"varying vec4 vTex6;" + "\n" + 
		"varying vec2 vTex;" + "\n" + 
		"" + "\n" + 
		"void main() " + "\n" + 
		"{ " + "\n" + 
		"   gl_Position = uMVPMatrix * vec4(aPosition, 1);" + "\n" + 
		"   vTex1 = vec4(aTexCoord + uInvTargetSize * -1. , aTexCoord + uInvTargetSize *  1.);" + "\n" + 
		"   vTex2 = vec4(aTexCoord + uInvTargetSize * -2. , aTexCoord + uInvTargetSize *  2.);" + "\n" + 
		"   vTex3 = vec4(aTexCoord + uInvTargetSize * -3. , aTexCoord + uInvTargetSize *  3.);" + "\n" + 
		"   vTex4 = vec4(aTexCoord + uInvTargetSize * -4. , aTexCoord + uInvTargetSize *  4.);" + "\n" + 
		"   vTex5 = vec4(aTexCoord + uInvTargetSize * -5. , aTexCoord + uInvTargetSize *  5.);" + "\n" + 
		"   vTex6 = vec4(aTexCoord + uInvTargetSize * -6. , aTexCoord + uInvTargetSize *  6.);" + "\n" + 
		"   vTex   = aTexCoord;" + "\n" + 
		"}" + "\n" + 
		"";

	//================ blur6.frag ================
	final static String BLUR6_FRAG =
		"precision mediump float;" + "\n" + 
		"uniform sampler2D sTexture;" + "\n" + 
		"uniform vec4 uColor;" + "\n" + 
		"uniform float uStrength;" + "\n" + 
		"" + "\n" + 
		"varying vec4 vTex1;" + "\n" + 
		"varying vec4 vTex2;" + "\n" + 
		"varying vec4 vTex3;" + "\n" + 
		"varying vec4 vTex4;" + "\n" + 
		"varying vec4 vTex5;" + "\n" + 
		"varying vec4 vTex6;" + "\n" + 
		"varying vec2 vTex;" + "\n" + 
		"const float s = 0.0204;  // r=6, s=(r+1)^-2" + "\n" + 
		"void main() " + "\n" + 
		"{" + "\n" + 
		"	float a6 = texture2D(sTexture, vTex6.xy).a + texture2D(sTexture, vTex6.zw).a;" + "\n" + 
		"	float a5 = texture2D(sTexture, vTex5.xy).a + texture2D(sTexture, vTex5.zw).a;" + "\n" + 
		"	float a4 = texture2D(sTexture, vTex4.xy).a + texture2D(sTexture, vTex4.zw).a;" + "\n" + 
		"	float a3 = texture2D(sTexture, vTex3.xy).a + texture2D(sTexture, vTex3.zw).a;" + "\n" + 
		"	float a2 = texture2D(sTexture, vTex2.xy).a + texture2D(sTexture, vTex2.zw).a;" + "\n" + 
		"	float a1 = texture2D(sTexture, vTex1.xy).a + texture2D(sTexture, vTex1.zw).a;" + "\n" + 
		"	float a  = texture2D(sTexture, vTex).a;" + "\n" + 
		"   float sum = a * 7. + a1 * 6. + a2 * 5. + a3 * 4. + a4 * 3. + a5 * 2. + a6;" + "\n" +
		"	gl_FragColor = uColor * (sum * s * uStrength);" + "\n" + 
		"} " + "\n" + 
		"";
	
	//================ blur7.vert ================
	final static String BLUR7_VERT =
		"uniform mat4 uMVPMatrix;" + "\n" + 
		"attribute vec3 aPosition;" + "\n" + 
		"attribute vec2 aTexCoord;" + "\n" + 
		"uniform vec2 uInvTargetSize;" + "\n" + 
		"" + "\n" + 
		"varying vec4 vTex1;" + "\n" + 
		"varying vec4 vTex2;" + "\n" + 
		"varying vec4 vTex3;" + "\n" + 
		"varying vec4 vTex4;" + "\n" + 
		"varying vec4 vTex5;" + "\n" + 
		"varying vec4 vTex6;" + "\n" + 
		"varying vec4 vTex7;" + "\n" + 
		"varying vec2 vTex;" + "\n" + 
		"" + "\n" + 
		"void main() " + "\n" + 
		"{ " + "\n" + 
		"   gl_Position = uMVPMatrix * vec4(aPosition, 1);" + "\n" + 
		"   vTex1 = vec4(aTexCoord + uInvTargetSize * -1. , aTexCoord + uInvTargetSize *  1.);" + "\n" + 
		"   vTex2 = vec4(aTexCoord + uInvTargetSize * -2. , aTexCoord + uInvTargetSize *  2.);" + "\n" + 
		"   vTex3 = vec4(aTexCoord + uInvTargetSize * -3. , aTexCoord + uInvTargetSize *  3.);" + "\n" + 
		"   vTex4 = vec4(aTexCoord + uInvTargetSize * -4. , aTexCoord + uInvTargetSize *  4.);" + "\n" + 
		"   vTex5 = vec4(aTexCoord + uInvTargetSize * -5. , aTexCoord + uInvTargetSize *  5.);" + "\n" + 
		"   vTex6 = vec4(aTexCoord + uInvTargetSize * -6. , aTexCoord + uInvTargetSize *  6.);" + "\n" + 
		"   vTex7 = vec4(aTexCoord + uInvTargetSize * -7. , aTexCoord + uInvTargetSize *  7.);" + "\n" + 
		"   vTex   = aTexCoord;" + "\n" + 
		"}" + "\n" + 
		"";
	
	//================ blur7.frag ================
	final static String BLUR7_FRAG =
		"precision mediump float;" + "\n" + 
		"uniform sampler2D sTexture;" + "\n" + 
		"uniform vec4 uColor;" + "\n" + 
		"uniform float uStrength;" + "\n" + 
		"" + "\n" + 
		"varying vec4 vTex1;" + "\n" + 
		"varying vec4 vTex2;" + "\n" + 
		"varying vec4 vTex3;" + "\n" + 
		"varying vec4 vTex4;" + "\n" + 
		"varying vec4 vTex5;" + "\n" + 
		"varying vec4 vTex6;" + "\n" + 
		"varying vec4 vTex7;" + "\n" + 
		"varying vec2 vTex;" + "\n" + 
		"const float s = 0.015625;  // r=7, s=(r+1)^-2" + "\n" + 
		"void main() " + "\n" + 
		"{" + "\n" + 
		"	float a7 = texture2D(sTexture, vTex7.xy).a + texture2D(sTexture, vTex7.zw).a;" + "\n" + 
		"	float a6 = texture2D(sTexture, vTex6.xy).a + texture2D(sTexture, vTex6.zw).a;" + "\n" + 
		"	float a5 = texture2D(sTexture, vTex5.xy).a + texture2D(sTexture, vTex5.zw).a;" + "\n" + 
		"	float a4 = texture2D(sTexture, vTex4.xy).a + texture2D(sTexture, vTex4.zw).a;" + "\n" + 
		"	float a3 = texture2D(sTexture, vTex3.xy).a + texture2D(sTexture, vTex3.zw).a;" + "\n" + 
		"	float a2 = texture2D(sTexture, vTex2.xy).a + texture2D(sTexture, vTex2.zw).a;" + "\n" + 
		"	float a1 = texture2D(sTexture, vTex1.xy).a + texture2D(sTexture, vTex1.zw).a;" + "\n" + 
		"	float a  = texture2D(sTexture, vTex).a;" + "\n" + 
		"   float sum = a * 8. + a1 * 7. + a2 * 6. + a3 * 5. + a4 * 4. + a5 * 3. + a6 * 2. + a7;" + "\n" +
		"	gl_FragColor = uColor * (sum * s * uStrength);" + "\n" + 
		"} " + "\n" + 
		"";


	final static String[] VERTEX_SHADER_STRINGS = {null, BLUR1_VERT, BLUR2_VERT, BLUR3_VERT,
		BLUR4_VERT, BLUR5_VERT, BLUR6_VERT, BLUR7_VERT};
	final static String[] FRAGMENT_SHADER_STRINGS = {null, BLUR1_FRAG, BLUR2_FRAG, BLUR3_FRAG,
		BLUR4_FRAG, BLUR5_FRAG, BLUR6_FRAG, BLUR7_FRAG};

}


