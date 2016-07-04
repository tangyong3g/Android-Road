package com.graphics.engine.gl.graphics.ext;


import com.graphics.engine.gl.graphics.GLShaderProgram;
import com.graphics.engine.gl.graphics.GLShaderWrapper;
import com.graphics.engine.gl.graphics.RenderContext;
import com.graphics.engine.gl.graphics.StaticTextureListener;
import com.graphics.engine.gl.graphics.TextureShader;

import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.opengl.GLES20;

/**
 * 
 * <br>类描述: 实现{@link ColorMatrixColorFilter}效果的着色器封装器
 * <br>功能详细描述:
 * <br>通过{@link GLDrawable#setShaderWrapper(GLShaderWrapper)}方法使绘制对象的颜色做线性变换。
 * <ul>
 * <li>{@link #getCombinedEffect(ColorMatrixShaderWrapper, ColorMatrixShaderWrapper)}可以
 * 组合两个效果，得到一个新效果。
 * <li>{@link #getTranslateEffect(int, int, int)} 平移颜色
 * <li>{@link #getScaleEffect(float, float, float)} 缩放颜色
 * <li>{@link #getGrayEffect()} 让绘制对象变成灰色
 * <li>{@link #getSepiaEffect()} 让绘制对象变成褐色 
 * <li>其他get*Effect方法
 * </ul>
 * 
 * <p><pre> ColorMatrix是一个4x5的矩阵，按照行优先存储成一维数组：
 *  [ a, b, c, d, e,
 *    f, g, h, i, j,
 *    k, l, m, n, o,
 *    p, q, r, s, t ]
 *
 * 当作用在一个颜色 [R, G, B, A]时, 计算结果为（再限制到[0..255]）：
 *   R' = a*R + b*G + c*B + d*A + e;
 *   G' = f*R + g*G + h*B + i*A + j;
 *   B' = k*R + l*G + m*B + n*A + o;
 *   A' = p*R + q*G + r*B + s*A + t;
 *   
 * 注意e,j,o,t是非归一化的，即取值的量级为255，并且可以为负数。
 * </pre></p>
 * 
 * @author  dengweiming
 * @date  [2013-10-28]
 */
public class ColorMatrixShaderWrapper extends GLShaderWrapper {
	private static final int ARRAY_COLUMN = 5;
	private static final int ARRAY_LENGTH = 20;
	private static final int ARRAY_SUB_MATRIX_LENGTH = 16;
	private static final float FULL_ALPHA = 255;
	private static final float INV255 = 1.0f / FULL_ALPHA;
	
	/** @formatter:off */
	
	/**
	 * 灰度化效果 
	 */
	private static final float[] GRAY_MATRIX = new float[] {
		0.299f, 0.587f, 0.114f, 0, 0,  
		0.299f, 0.587f, 0.114f, 0, 0,
		0.299f, 0.587f, 0.114f, 0, 0,
		0, 0, 0, 1, 0,
	};
	private static ColorMatrixShaderWrapper sGrayEffect;
	
	/**
	 * 褐色效果，其实为灰度化效果再组合(49, -14, -56)的偏移
	 * https://gist.github.com/pastelLab/5998928
	 */
	private static final float[] SEPIA_MATRIX = new float[] {
		0.299f, 0.587f, 0.114f, 0.000f,  0.191f * FULL_ALPHA, 
		0.299f, 0.587f, 0.114f, 0.000f, -0.054f * FULL_ALPHA, 
		0.299f, 0.587f, 0.114f, 0.000f, -0.221f * FULL_ALPHA, 
		0, 0, 0, 1, 0,
	};
	private static ColorMatrixShaderWrapper sSepiaEffect;
	
	/**
	 * 负片化效果 
	 */
	private static final float[] NEGATIVE_MATRIX = new float[] {
		-1, 0, 0, 0, 255, 
		0, -1, 0, 0, 255, 
		0, 0, -1, 0, 255, 
		0, 0, 0, 1, 0,
	};
	private static ColorMatrixShaderWrapper sNegativeEffect;
	/** @formatter:on */
	
	private static ColorMatrixShader sShader;
	private static ColorMatrixShader sShaderFast;	//alpha值不变时的优化版本
	private static final float[] TEMP_ARRAY = new float[ARRAY_LENGTH];
	
	private static void initInternalShaders() {
		if (sShader == null) {
			sShader = new ColorMatrixShader(TextureShader.getDefaultVertexShaderSource(), TEXTURE_COLOR_MATRIX_FRAG);
			sShader.registerStatic();
		}
		if (sShaderFast == null) {
			sShaderFast = new ColorMatrixShader(TextureShader.getDefaultVertexShaderSource(), TEXTURE_COLOR_MATRIX_FAST_FRAG);
			sShaderFast.registerStatic();
		}
	}
	
	private float[] mArray = new float[ARRAY_LENGTH];
	private ColorMatrixShader mShader;
	private String mString;
	
	/**
	 * <br>功能简述: 获取一个实例，组合两个已有效果
	 * <br>功能详细描述: 例如{@link #getBrightEffect(int)}和{@link #getGrayEffect()}组合可以
	 * 保持图片亮度而替换颜色。
	 * <br>注意: 因为矩阵是右乘以颜色的，因此在后面的参数的矩阵先作用于颜色
	 * @param shaderA 后起作用的效果
	 * @param shaderB 先起作用的效果
	 * @return
	 */
	public static ColorMatrixShaderWrapper getCombinedEffect(
			ColorMatrixShaderWrapper shaderA, ColorMatrixShaderWrapper shaderB) {
		ColorMatrixShaderWrapper shaderC = new ColorMatrixShaderWrapper();
		float[] a = shaderA.mArray;
		float[] b = shaderB.mArray;
		float[] c = shaderC.mArray;
		//5x5列矩阵相乘，注意最后一行[0 0 0 0 1]没有存储
		android.opengl.Matrix.multiplyMM(c, 0, a, 0, b, 0);
		int ind = ARRAY_SUB_MATRIX_LENGTH;
		for (int i = 0; i < 4; ++i) {
			c[ind + i] = a[i] * b[ind] 
							+ a[i + 4] * b[ind + 1] 
							+ a[i + 8] * b[ind + 2] 
							+ a[i + 12] * b[ind + 3] 
							+ a[i + 16];
		}
		
		initInternalShaders();
		shaderC.mShader = sShader;
        //CHECKSTYLE IGNORE 1 LINES
		if (c[15] == 0 && c[16] == 0 && c[17] == 0 && c[18] == 1 && c[19] == 0) {
			shaderC.mShader = sShaderFast;	//alpha值不变时可优化
		}
		return shaderC;
	}
	
	public static ColorMatrixShaderWrapper getTranslateEffect(int rTrans, int gTrans, int bTrans) {
		float[] array = TEMP_ARRAY;
		resetArray(array);
		array[4] = rTrans;
		array[9] = gTrans;
		array[14] = bTrans;
		return new ColorMatrixShaderWrapper(array);
	}
	
	/**
	 * <br>功能简述: 获取一个实例，将R，G，B通道分别进行缩放
	 * <br>功能详细描述: 相当于{@link ColorMatrix#setScale(float, float, float, float)}，只是alpha缩放倍数为1。
	 * <br>注意:
	 * @param rScale	红色通道缩放倍数，1表示不变，0则移除了该通道颜色，大于1会增强该通道颜色
	 * @param gScale	绿色通道缩放倍数，1表示不变，0则移除了该通道颜色，大于1会增强该通道颜色
	 * @param bScale	蓝色通道缩放倍数，1表示不变，0则移除了该通道颜色，大于1会增强该通道颜色
	 * @return
	 */
	public static ColorMatrixShaderWrapper getScaleEffect(float rScale, float gScale, float bScale) {
		float[] array = TEMP_ARRAY;
		resetArray(array);
		array[0] = rScale;
		array[6] = gScale;
		array[12] = bScale;
		return new ColorMatrixShaderWrapper(array);
	}
	
	/**
	 * <br>功能简述: 获取一个实例，具有灰度化效果
	 * <br>功能详细描述: 使用公式 R'=G'=B'= 0.299 * R + 0.587 * G + 0.114 * B
	 * <br>注意: {@link ColorMatrix#setSaturation(float)}参数为0时也是灰色效果，但是偏暗。
	 * 参考<a href="http://en.wikipedia.org/wiki/Luma_%28video%29">明度</a>和
	 * <a href="http://en.wikipedia.org/wiki/Grayscale">灰度</a>的差别。
	 */
	public static ColorMatrixShaderWrapper getGrayEffect() {
		if (sGrayEffect != null) {
			return sGrayEffect;
		}
		return sGrayEffect = new ColorMatrixShaderWrapper(GRAY_MATRIX);
	}
	
	/**
	 * <br>功能简述: 获取一个实例，具有负片（底片）化效果
	 * <br>功能详细描述: 
	 * <br>注意: 
	 */
	public static ColorMatrixShaderWrapper getNegativeEffect() {
		if (sNegativeEffect != null) {
			return sNegativeEffect;
		}
		return sNegativeEffect = new ColorMatrixShaderWrapper(NEGATIVE_MATRIX);
	}
	
	/**
	 * <br>功能简述: 获取一个实例，具有褐化效果
	 * <br>功能详细描述: 
	 * <br>其实为灰度化效果再组合(49, -14, -56)的偏移，即:
	 * <pre><code>
	 * getCombinedEffect(getTranslateEffect(49, -14, -56), getGrayEffect());
	 * </code></pre>
	 * <a href="https://gist.github.com/pastelLab/5998928">算法来源</a>
	 * <br>注意: 
	 */
	public static ColorMatrixShaderWrapper getSepiaEffect() {
		if (sSepiaEffect != null) {
			return sSepiaEffect;
		}
		return sSepiaEffect = new ColorMatrixShaderWrapper(SEPIA_MATRIX);
	}
	
	/**
	 * <br>功能简述: 获取一个实例，将R，G，B通道分别进行缩放
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param color 颜色值，其中R,G,B颜色归一化后分别代表对应通道的缩放比例
	 * @return
	 * 
	 * @see 使用{@link #getScaleEffect(float, float, float)}实现
	 */
	public static ColorMatrixShaderWrapper getBrightEffect(int color) {
		return getScaleEffect(
					(color >>> 16 & 0xFF) * INV255, 
					(color >>> 8 & 0xFF) * INV255, 
					(color & 0xFF) * INV255
				);
	}
	
	/**
	 * <br>功能简述: 获取一个实例，改变对比度
	 * <br>功能详细描述: 会对RGB分量都加上一个偏移量
	 * <br>注意:
	 * @param contrast 偏移量，量级为255，可以为负数。
	 * @return
	 * @see 使用{@link #getTranslateEffect(int, int, int)}实现
	 */
	public static ColorMatrixShaderWrapper getContrastEffect(int contrast) {
		return getTranslateEffect(contrast, contrast, contrast);
	}

	private ColorMatrixShaderWrapper() {
	}
	
	/**
	 * 使用一个{@link ColorMatrix}来创建一个实例
	 */
	public ColorMatrixShaderWrapper(ColorMatrix matrix) {
		this(matrix.getArray());
	}
	
	/**
	 * 使用一个数组来创建一个实例
	 * @param array 其含义见{@link ColorMatrixShaderWrapper}的文档说明
	 */
	public ColorMatrixShaderWrapper(float[] array) {
        if (array.length < ARRAY_LENGTH) {
            throw new ArrayIndexOutOfBoundsException();
        }
        /*
         * ColorMatrix中按照行优先顺序存储4x5的矩阵
         *  0  1  2  3  4
         *  5  6  7  8  9
         * 10 11 12 13 14
         * 15 16 17 18 19 
         * 需要转换成列优先存储成左边4x4的子矩阵有右边4x1的列向量，
         * ColorMatrixShader.setColorMatrix(float[])方法则会读取转换后的数组。
         */
		int index = 0;
		for (int i = 0; i < ARRAY_COLUMN; ++i) {
			for (int j = i; j < i + ARRAY_LENGTH; j += ARRAY_COLUMN) {
				mArray[index] = array[j];
				if (i >= ARRAY_COLUMN - 1) {
					mArray[index] *= INV255;	//最后一列平移量要归一化
				}
				index++;
			}
		}
		
        initInternalShaders();
        mShader = sShader;
        //CHECKSTYLE IGNORE 1 LINES
		if (array[15] == 0 && array[16] == 0 && array[17] == 0 && array[18] == 1 && array[19] == 0) {
			mShader = sShaderFast;	//alpha值不变时可优化
		}
	}

	@Override
	public void onDraw(RenderContext context) {
		
	}

	@Override
	public GLShaderProgram onRender(RenderContext context) {
		ColorMatrixShader shader = mShader;
		if (shader == null || !shader.bind()) {
			return null;
		}
		shader.setAlpha(context.alpha);
		shader.setMatrix(context.matrix, 0);
		shader.setColorMatrix(mArray);
		//TODO:如果shader==sShaderFast，那么可以把context.alpha预先乘进mArray中alpha那行，片段着色器就不用再乘一次
		return shader;
	}
	
	@Override
	protected boolean onProgramCreated() {
		return true;
	}

	@Override
	protected void onProgramBind() {
		
	}
	
	@Override
	public String toString() {
		if (mString == null) {
			mString = super.toString() + " {";
			for (int i = 0; i < 4; ++i) {
				mString += "\n\t";
				for (int j = 0; j < 5; ++j) {
					mString += String.format("%.3f, ", mArray[j * 4 + i]);
				}
			}
			mString += "}";
		}
		return mString;
	}
	
	/**
	 * 将一个原始的矩阵重置
	 */
    private static void resetArray(float[] a) {
        for (int i = 19; i > 0; --i) {
            a[i] = 0;
        }
        a[0] = a[6] = a[12] = a[18] = 1;
    }

	/**
	 * <br>类描述: 封装的着色器
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2013-10-28]
	 */
	static class ColorMatrixShader extends TextureShader implements StaticTextureListener {
		private final static String TAG = "ColorMatrixShader";
		int muAlphaHandle;
		int muColorSubMatrixHadle;
		int muColorTranslationHadle;
		
		public ColorMatrixShader(Resources res, String vertexFile, String fragmentFile) {
			super(res, vertexFile, fragmentFile);
		}
		
		public ColorMatrixShader(String vertexSource, String fragmentSource) {
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
			muColorSubMatrixHadle = getUniformLocation("uColorSubMatrix");
			muColorTranslationHadle = getUniformLocation("uColorTranslation");
			return true;
		}
		
		@Override
		public void setAlpha(float alpha) {
			GLES20.glUniform1f(muAlphaHandle, alpha);
		}
		
		public void setColorMatrix(float[] matrix) {
			GLES20.glUniformMatrix4fv(muColorSubMatrixHadle, 1, false, matrix, 0);
			GLES20.glUniform4fv(muColorTranslationHadle, 1, matrix, ARRAY_SUB_MATRIX_LENGTH);
		}
		
		@Override
		public void onTextureInvalidate() {
			super.onTextureInvalidate();
		}
		
	}
	
	//================ texture_color_matrix.frag ================
	private final static String TEXTURE_COLOR_MATRIX_FRAG =
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	float uAlpha;" + "\n" + 
		"uniform	mat4 uColorSubMatrix;" + "\n" + 
		"uniform	vec4 uColorTranslation;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	vec4 dst = texture2D(sTexture, vTextureCoord);" + "\n" + 
		"	vec4 color = vec4(dst.rgb * (1. / dst.a), dst.a);" + "\n" + 
		"	color = clamp(uColorSubMatrix * color + uColorTranslation, 0., 1.);" + "\n" + 
		"	gl_FragColor = vec4(color.rgb, 1.) * (color.a * uAlpha);" + "\n" + 
		"}" + "\n" + 
		"";
	
	//================ texture_color_matrix_fast.frag ================
	private final static String TEXTURE_COLOR_MATRIX_FAST_FRAG =
		"precision mediump float;" + "\n" + 
		"varying	vec2 vTextureCoord;" + "\n" + 
		"uniform	sampler2D sTexture;" + "\n" + 
		"uniform	float uAlpha;" + "\n" + 
		"uniform	mat4 uColorSubMatrix;" + "\n" + 
		"uniform	vec4 uColorTranslation;" + "\n" + 
		"void main()" + "\n" + 
		"{" + "\n" + 
		"	vec4 dst = texture2D(sTexture, vTextureCoord);" + "\n" + 
		"	gl_FragColor = clamp(uColorSubMatrix * dst + uColorTranslation * dst.a, 0., dst.a) * uAlpha;" + "\n" + 
		"}" + "\n" + 
		"";

}
