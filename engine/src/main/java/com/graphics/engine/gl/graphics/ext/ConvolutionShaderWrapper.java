package com.graphics.engine.gl.graphics.ext;


import android.content.res.Resources;
import android.opengl.GLES20;

import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.graphics.GLShaderProgram;
import com.graphics.engine.gl.graphics.GLShaderWrapper;
import com.graphics.engine.gl.graphics.RenderContext;
import com.graphics.engine.gl.graphics.StaticTextureListener;
import com.graphics.engine.gl.graphics.Texture;
import com.graphics.engine.gl.graphics.TextureShader;

/**
 * <br>类描述: 卷积滤波着色器的封装器
 * <br>功能详细描述:
 * <br>使用一个二维方形的卷积核，对于图像任意一个像素，将核的中心对齐到该像素上，
 * 然后核的每个元素都和其覆盖的像素相乘，求和，再归一化（除以元素总和），作为该像素滤波的结果。
 * <br>例如，盒装滤波器
 * <br>1 1 1
 * <br>1 1 1
 * <br>1 1 1
 * <br>的作用相当于将每个像素和周围八个像素求和，再取平均值。
 * 
 * <p> 使用{@link #getEdgeDetectionEffect(int, float)}获得一个边缘检测的效果实例。
 * </p>
 * 
 * @author  dengweiming
 * @date  [2013-10-30]
 */
public class ConvolutionShaderWrapper extends GLShaderWrapper {
	private static final int FILTER_KERNEL_SIZE = 3;
	private static final int ARRAY_LENGTH = FILTER_KERNEL_SIZE * FILTER_KERNEL_SIZE;
	
	
	private static ConvolutionShader sShader;
	private static ConvolutionShader sEdgeDetectShader;
	
	private static void initInternalShaders() {
		if (sShader == null) {
			sShader = new ConvolutionShader(TextureShader.getDefaultVertexShaderSource(), TEXTURE_CONVOLUTION_FRAG);
			sShader.registerStatic();
		}
	}
	
	private final static float[] TEMP_KERNEL = new float[ARRAY_LENGTH];
	private float[] mFilterKernel = new float[ARRAY_LENGTH];
	private float mInvSum;
	private ConvolutionShader mShader;
	
	/**
	 * <br>功能简述: 获取一个实例，可以实现边缘检测效果
	 * <br>功能详细描述:
	 * <br>注意: 和加了挖空的发光效果不一样，发光效果只处理alpha通道，边缘检测处理所有通道，并且更快，不需要帧缓冲区。
	 * @param color 边缘的绘制颜色
	 * @param edgeThreshhold 判断是否为边缘的阈值。越大则边缘越少，默认用0.5就可以。
	 * @return
	 */
	public static ConvolutionShaderWrapper getEdgeDetectionEffect(int color, float edgeThreshhold) {
		GLCanvas.convertColorToPremultipliedFormat(color, TEMP_KERNEL, 0);
		TEMP_KERNEL[4] = Math.max(0, edgeThreshhold);
		if (sEdgeDetectShader == null) {
			sEdgeDetectShader = new ConvolutionShader(TextureShader.getDefaultVertexShaderSource(), TEXTURE_EDGE_DETECT_FRAG);
			sEdgeDetectShader.registerStatic();
		}
		ConvolutionShaderWrapper wrapper = new ConvolutionShaderWrapper(TEMP_KERNEL);
		wrapper.mInvSum = 1;
		wrapper.mShader = sEdgeDetectShader;
		return wrapper;
	}
	
	/**
	 * 创建一个实例
	 * @param kernel 二维卷积核，按行优先存储。目前只支持3x3的卷积核。不需要归一化。
	 */
	public ConvolutionShaderWrapper(float[] kernel) {
        if (kernel.length < ARRAY_LENGTH) {
            throw new ArrayIndexOutOfBoundsException();
        }
        System.arraycopy(kernel, 0, mFilterKernel, 0, ARRAY_LENGTH);
		mInvSum = 0;
		for (int i = 0; i < mFilterKernel.length; ++i) {
			mInvSum += mFilterKernel[i];
		}
		mInvSum = mInvSum == 0 ? 1 : 1 / mInvSum;
        
        initInternalShaders();
        mShader = sShader;
	}

	@Override
	public void onDraw(RenderContext context) {
		
	}

	@Override
	public GLShaderProgram onRender(RenderContext context) {
		ConvolutionShader shader = mShader;
		if (shader == null || !shader.bind()) {
			return null;
		}
		Texture texture = context.texture;
		if (texture != null) {
			//TODO:按照纹理绘制时的大小
			shader.setTargetSize(texture.getWidth(), texture.getHeight());
		}
		shader.setAlpha(context.alpha * mInvSum);
		shader.setMatrix(context.matrix, 0);
		shader.setFilterKernel(mFilterKernel);
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
	 * <br>类描述: 封装的着色器
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2013-10-28]
	 */
	static class ConvolutionShader extends TextureShader implements StaticTextureListener {
		private final static String TAG = "ConvolutionShader";
		int muAlphaHandle;
		int muKernelHandle;
		int muInvTargetSizeHandle;
		
		public ConvolutionShader(Resources res, String vertexFile, String fragmentFile) {
			super(res, vertexFile, fragmentFile);
		}
		
		public ConvolutionShader(String vertexSource, String fragmentSource) {
			super(vertexSource, fragmentSource);
		}
		
		@Override
		public String toString() {
			return TAG;
		}
		
		@Override
		protected boolean onProgramCreated() {
			if (!super.onProgramCreated()) {
				return false;
			}
			muAlphaHandle = getUniformLocation("uAlpha");
			muKernelHandle = getUniformLocation("uKernel");
			muInvTargetSizeHandle = getUniformLocation("uInvTargetSize");
			return true;
		}
		
		@Override
		public void setAlpha(float alpha) {
			GLES20.glUniform1f(muAlphaHandle, alpha);
		}
		
		public void setFilterKernel(float[] kernel) {
			GLES20.glUniform1fv(muKernelHandle, kernel.length, kernel, 0);
		}
		
		public void setTargetSize(float w, float h) {
			GLES20.glUniform2f(muInvTargetSizeHandle, 1.0f / w, 1.0f / h);
		}
		
		@Override
		public void onTextureInvalidate() {
			super.onTextureInvalidate();
		}
		
	}
	
	//================ texture_convolution.frag ================
	private final static String TEXTURE_CONVOLUTION_FRAG =
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	vec2 uInvTargetSize;" + "\n" + 
		"uniform	float uKernel[9];" + "\n" + 
		"uniform	float uAlpha;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	vec4 c00 = texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2(-1., -1.));" + "\n" + 
		"	vec4 c01 = texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2( 0., -1.));" + "\n" + 
		"	vec4 c02 = texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2( 1., -1.));" + "\n" + 
		"	vec4 c10 = texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2(-1.,  0.));" + "\n" + 
		"	vec4 c11 = texture2D(sTexture, vTextureCoord);" + "\n" + 
		"	vec4 c12 = texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2( 1.,  0.));" + "\n" + 
		"	vec4 c20 = texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2(-1.,  1.));" + "\n" + 
		"	vec4 c21 = texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2( 0.,  1.));" + "\n" + 
		"	vec4 c22 = texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2( 1.,  1.));" + "\n" + 
		"	vec4 color = c00 * uKernel[0] + c01 * uKernel[1] + c02 * uKernel[2] " + "\n" + 
		"	            +c10 * uKernel[3] + c11 * uKernel[4] + c12 * uKernel[5] " + "\n" + 
		"	            +c20 * uKernel[6] + c21 * uKernel[7] + c22 * uKernel[8];" + "\n" + 
		"	gl_FragColor = color * uAlpha;" + "\n" + 
		"}" + "\n" + 
		"";
	
	//XXX:使用for循环实现，效率没差，但是效果有一点不一样，可能是mod和floor计算到的偏移值不对？
	private final static String TEXTURE_CONVOLUTION_FRAG2 =
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	vec2 uInvTargetSize;" + "\n" + 
		"uniform	float uKernel[9];" + "\n" + 
		"uniform	float uAlpha;" + "\n" + 
		"const float radius = 1.;" + "\n" + 
		"const float size = 3.;" + "\n" + 
		"const int count = 9;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"   vec4 color;" + "\n" + 
		"   for (int i = 0; i < count; ++i) {"    + "\n" + 
		"       float x = mod(float(i), size) - radius;" + "\n" +
		"       float y = floor(float(i) / size) - radius;" + "\n" +
		"       vec4 tex = texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2(x, y));" + "\n" + 
		"       color = color + tex * uKernel[i];" + "\n" + 
		"   }" + "\n" +
		"	gl_FragColor = color * uAlpha;" + "\n" + 
		"}" + "\n" + 
		"";
	
	
	//================ texture_edge_detect.frag ================
	/**
	 * 使用sobel算子进行边缘检测的shader，
	 * <a href="http://zh.wikipedia.org/wiki/%E7%B4%A2%E8%B2%9D%E7%88%BE%E7%AE%97%E5%AD%90">算法参考</a>。
	 * 里面用了<a href="http://en.wikipedia.org/wiki/Luma_%28video%29">明度</a>
	 * 而不是用<a href="http://en.wikipedia.org/wiki/Grayscale">灰度</a>。
	 */
	private final static String TEXTURE_EDGE_DETECT_FRAG =
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	vec2 uInvTargetSize;" + "\n" + 
		"uniform	float uAlpha;" + "\n" + 
		"uniform	float uKernel[9];" + "\n" + 
		"float luma(vec4 color) {" + "\n" + 
		"	return 0.2126 * color.r + 0.7152 * color.g + 0.0722 * color.b;" + "\n" + 
//		"	return 0.299 * color.r + 0.587 * color.g + 0.114 * color.b;" + "\n" + 
		"}" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	float s00 = luma(texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2(-1., -1.)));" + "\n" + 
		"	float s01 = luma(texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2( 0., -1.)));" + "\n" + 
		"	float s02 = luma(texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2( 1., -1.)));" + "\n" + 
		"	float s10 = luma(texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2(-1.,  0.)));" + "\n" + 
		"	float s11 = luma(texture2D(sTexture, vTextureCoord));" + "\n" + 
		"	float s12 = luma(texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2( 1.,  0.)));" + "\n" + 
		"	float s20 = luma(texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2(-1.,  1.)));" + "\n" + 
		"	float s21 = luma(texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2( 0.,  1.)));" + "\n" + 
		"	float s22 = luma(texture2D(sTexture, vTextureCoord + uInvTargetSize * vec2( 1.,  1.)));" + "\n" + 
		"	float sx = s00 + 2. * s10 + s20 - s02 - 2. * s12 - s22;" + "\n" + 
		"	float sy = s00 + 2. * s01 + s02 - s20 - 2. * s21 - s22;" + "\n" + 
		"	float g = sx * sx + sy * sy;" + "\n" + 
		"	float a = smoothstep(0., uKernel[4], g);" + "\n" + 
		"	gl_FragColor = vec4(uKernel[0], uKernel[1], uKernel[2], uKernel[3]) * (uAlpha * a);" + "\n" + 
		"}" + "\n" + 
		"";

}
