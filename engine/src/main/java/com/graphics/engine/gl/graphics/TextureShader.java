package com.graphics.engine.gl.graphics;

import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.opengl.GLES20;

import com.graphics.engine.gl.util.NdkUtil;

import java.nio.Buffer;

/**
 * <br>类描述: 从纹理取色并可以和遮蔽颜色混合的着色器
 * <br>功能详细描述:
 * {@link #getShader(int)}
 * 
 * @author  dengweiming
 * @date  [2013-10-16]
 */
public class TextureShader extends GLShaderProgram {
	private static TextureShader[] sShaders;
	
	public final static int MODE_CLEAR       = 0;
	public final static int MODE_SRC         = 1;
	public final static int MODE_DST         = 2;
	public final static int MODE_SRC_OVER    = 3;
	public final static int MODE_DST_OVER    = 4;
	public final static int MODE_SRC_IN      = 5;
	public final static int MODE_DST_IN      = 6;
	public final static int MODE_SRC_OUT     = 7;
	public final static int MODE_DST_OUT     = 8;
	public final static int MODE_SRC_ATOP    = 9;
	public final static int MODE_DST_ATOP    = 10;
	public final static int MODE_XOR         = 11;
	public final static int MODE_DARKEN      = 12;
	public final static int MODE_LIGHTEN     = 13;
	public final static int MODE_MULTIPLY    = 14;
	public final static int MODE_SCREEN      = 15;
    public final static int MODE_ALPHA		 = 16;
	public final static int MODE_BITMAP_FILTER = 17;
	private final static int PORTER_DUFF_MODE_COUNT = 18;

	/** 忽略遮蔽颜色 */
    public final static int MODE_NONE	 = MODE_DST;
	
    static void initInternalShaders() {
		if (sShaders == null) {
    		sShaders = new TextureShader[PORTER_DUFF_MODE_COUNT];
    		
    		/** @formatter:off */
    		sShaders[MODE_DST] = 
    			new TextureShader(ShaderStrings.TEXTURE_VERT, ShaderStrings.TEXTURE_FRAG);
    		sShaders[MODE_SRC_OVER] = 
    			new PorterDuffShader(ShaderStrings.TEXTURE_VERT, ShaderStrings.TEXTURE_SRC_OVER_FRAG, Mode.SRC_OVER, true, true);
    		sShaders[MODE_DST_OVER] = 
    			new PorterDuffShader(ShaderStrings.TEXTURE_VERT, ShaderStrings.TEXTURE_DST_OVER_FRAG, Mode.DST_OVER, true, false);
    		sShaders[MODE_SRC_IN] = 
    			new PorterDuffShader(ShaderStrings.TEXTURE_VERT, ShaderStrings.TEXTURE_SRC_IN_FRAG, Mode.SRC_IN, false, false);
    		sShaders[MODE_DST_IN] = 
    			new PorterDuffShader(ShaderStrings.TEXTURE_VERT, ShaderStrings.TEXTURE_DST_IN_FRAG, Mode.DST_IN, false, false);
    		sShaders[MODE_SRC_OUT] = 
    			new PorterDuffShader(ShaderStrings.TEXTURE_VERT, ShaderStrings.TEXTURE_SRC_OUT_FRAG, Mode.SRC_OUT, false, false);
    		sShaders[MODE_DST_OUT] = 
    			new PorterDuffShader(ShaderStrings.TEXTURE_VERT, ShaderStrings.TEXTURE_DST_OUT_FRAG, Mode.DST_OUT, true, false);
    		sShaders[MODE_SRC_ATOP] = 
    			new PorterDuffShader(ShaderStrings.TEXTURE_VERT, ShaderStrings.TEXTURE_SRC_ATOP_FRAG, Mode.SRC_ATOP, true, true);
    		sShaders[MODE_DST_ATOP] = 
    			new PorterDuffShader(ShaderStrings.TEXTURE_VERT, ShaderStrings.TEXTURE_DST_ATOP_FRAG, Mode.DST_ATOP, false, false);
			sShaders[MODE_MULTIPLY] = 
				new PorterDuffShader(ShaderStrings.TEXTURE_VERT, ShaderStrings.TEXTURE_MULTIPLY_FRAG, Mode.MULTIPLY, false, false);
			sShaders[MODE_BITMAP_FILTER] = 
				new FilterShader(ShaderStrings.BITMAP_FILTER_VERT, ShaderStrings.BITMAP_FILTER_FRAG);

			sShaders[MODE_ALPHA] = 
				new AlphaShader(ShaderStrings.TEXTURE_VERT, ShaderStrings.TEXTURE_ALPHA_FRAG);
			/** @formatter:on */
			
			for (int i = 0; i < sShaders.length; ++i) {
				if (sShaders[i] != null) {
					sShaders[i].registerStatic();
				}
			}
    	}
    }
    
	/**
	 * 获取静态的着色器实例
	 * @param mode	纹理和遮蔽颜色的混合方式，可选{@link #MODE_NONE}, 或者{@link #MODE_SRC_OVER}这些和{@link Mode}对应的值
	 * @return
	 */
	public static TextureShader getShader(int mode) {
		final TextureShader shader = sShaders[mode];
		if (shader == null) {
			throw new UnsupportedOperationException("mode " + mode + " is not supported");
		}
		return shader;
	}
	
	/**
	 * <br>功能简述: 获取默认的顶点着色器代码，方便继承类使用
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public static String getDefaultVertexShaderSource() {
		return ShaderStrings.TEXTURE_VERT;
	}
	
	/**
	 * <br>功能简述: 获取默认的片断着色器代码，方便继承类使用
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public static String getDefaultFragmentShaderSource() {
		return ShaderStrings.TEXTURE_FRAG;
	}
	
    int muMVPMatrixHandle;	//总变换矩阵引用id
    int maPositionHandle;	//顶点位置属性引用id  
    int maTexCoordHandle;	//顶点纹理坐标属性引用id 
    int mStride;			//顶点属性的跨距，0表示属性紧密排列
    
	/**
	 * 从资源中按照指定的文件名读取源代码并创建一个实例
	 * @param res 资源
	 * @param vertexFile 顶点着色程序的源代码文件名
	 * @param fragmentFile 片段着色程序的源代码文件名
	 */
    public TextureShader(Resources res, String vertexFile, String fragmentFile) {
		super(res, vertexFile, fragmentFile);
	}
    
	/**
	 * 从指定的源代码创建一个实例
	 * @param vertexSource 顶点着色程序的源代码
	 * @param fragmentSource 片段着色程序的源代码
	 */
	public TextureShader(String vertexSource, String fragmentSource) {
		super(vertexSource, fragmentSource);
	}

	@Override
	protected boolean onProgramCreated() {
        maPositionHandle = getAttribLocation("aPosition");
        maTexCoordHandle = getAttribLocation("aTexCoord");
        muMVPMatrixHandle = getUniformLocation("uMVPMatrix");
		return true;
	}
	
	@Override
	protected void onProgramBind() {
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glEnableVertexAttribArray(maTexCoordHandle);
	}
	
	/**
	 * <br>功能简述: 设置整体的不透明度
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用。
	 * @param alpha [0, 1]
	 */
	public void setAlpha(float alpha) {
		
	}
	
	/**
	 * <br>功能简述: 设置掩码颜色
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用。要在{@link #setAlpha(float)} 后调用。
	 * @param color	 r,g,b,a in [0, 1], alpha-premultiplied
	 * @param mode
	 */
	public void setMaskColor(float[] color) {
		
	}

	/**
	 * <br>功能简述: 设置MVP矩阵
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param m
	 * @param offset
	 */
	public void setMatrix(float[] m, int offset) {
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, m, offset);
	}
	
	/**
	 * <br>功能简述: 设置顶点位置数据
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param ptr
	 * @param component 位置分量个数（2或者3)
	 */
	public void setPosition(Buffer ptr, int component) {
		GLES20.glVertexAttribPointer(maPositionHandle, component, GLES20.GL_FLOAT,
				false, mStride, ptr);   
	}
	
	/**
	 * <br>功能简述: 在绑定VBO时，设置顶点位置数据
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param offset	数据在VBO中的偏移量
	 * @param component 位置分量个数（2或者3)
	 */
	public void setPosition(int offset, int component) {
//		GLES20.glVertexAttribPointer(maPositionHandle, component, GLES20.GL_FLOAT,
//				false, 0, offset);   //API Level 9
		NdkUtil.glVertexAttribPointer(maPositionHandle, component, GLES20.GL_FLOAT,
				false, mStride, offset);
	}
	
	/**
	 * <br>功能简述: 设置顶点纹理坐标数据
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param ptr
	 * @param component 位置分量个数（2或3)
	 */
	public void setTexCoord(Buffer ptr, int component) {
		GLES20.glVertexAttribPointer(maTexCoordHandle, component, GLES20.GL_FLOAT,
				false, mStride, ptr);   
	}
	
	/**
	 * <br>功能简述: 在绑定VBO时，设置顶点纹理坐标数据
	 * <br>功能详细描述:
	 * <br>注意: 在GL线程上调用
	 * @param offset	数据在VBO中的偏移量
	 * @param component 位置分量个数（2或3)
	 */
	public void setTexCoord(int offset, int component) {
//		GLES20.glVertexAttribPointer(maTexCoordHandle, component, GLES20.GL_FLOAT,
//				false, 0, offset);   //API Level 9
		NdkUtil.glVertexAttribPointer(maTexCoordHandle, component, GLES20.GL_FLOAT,
				false, mStride, offset);
	}
	
	/**
	 * <br>功能简述: 设置顶点属性在数组中存储的跨距
	 * <br>功能详细描述: 顶点位置和纹理坐标使用了该跨距，如果子类扩展属性的跨距不想等，要注意在
	 * 调用{@link GLES20#glVertexAttribPointer(int, int, int, boolean, int, Buffer)}时指定正确的跨距。
	 * <br>注意: 在GL线程上或者初始化时调用
	 * @param stride 默认为0，表示属性在数组中紧密排列。对于数组中间隔地存储多组属性时，跨距应为这些属性的元素字节数总和。
	 */
	public final void setVertexAttributeStride(int stride) {
		mStride = stride;
	}
	
	@Override
	public String toString() {
		return "TextureShader";
	}
	
	/**
	 * <br>功能简述: 获取顶点位置属性的句柄
	 * <br>功能详细描述: 用于自行调用glVertexAttribPointer方法时的第一个参数
	 * <br>注意:
	 * @return
	 */
	public int getPositionHandle() {
		return maPositionHandle;
	}
	
	/**
	 * <br>功能简述: 获取顶点纹理坐标属性的句柄
	 * <br>功能详细描述: 用于自行调用glVertexAttribPointer方法时的第一个参数
	 * <br>注意:
	 * @return
	 */
	public int getTexcoordHandle() {
		return maTexCoordHandle;
	}
}

/**
 * 从纹理取色并进行alpha淡化的着色器
 * @author dengweiming
 *
 */
class AlphaShader extends TextureShader {
	int muAlphaHandle;
	
	public AlphaShader(Resources res, String vertexFile, String fragmentFile) {
		super(res, vertexFile, fragmentFile);
	}
	
	public AlphaShader(String vertexSource, String fragmentSource) {
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
	
	@Override
	public void setAlpha(float alpha) {
		GLES20.glUniform1f(muAlphaHandle, alpha);
	}
	
	@Override
	public String toString() {
		return "TextureShader#AlphaShader";
	}
}

/**
 * 从纹理取色并与指定颜色按照指定模式混合的着色器
 * @author dengweiming
 *
 */
class PorterDuffShader extends TextureShader {
	int muAlphaHandle;
    int muSrcColorHandle;
    int mMode;
    boolean mSetAlpha;
    boolean mReverseAlpha;
    float mAlpha = 1;
    
    /**
     * @param setAlpha 是否将alpha传递给shader（即shader源码中是否包含uAlpha）
     * @param reverseAlpha 是否把整体的alpha减掉掩码颜色的alpha，以提高绘制效率
     */
	public PorterDuffShader(Resources res, String vertexFile, String fragmentFile,
							Mode mode, boolean setAlpha, boolean reverseAlpha) {
		super(res, vertexFile, fragmentFile);
		mMode = mode.ordinal();
		mSetAlpha = setAlpha;
		mReverseAlpha = reverseAlpha;
	}
    
	/**
	 * @param setAlpha 是否将alpha传递给shader（即shader源码中是否包含uAlpha）
	 * @param reverseAlpha 是否把整体的alpha减掉掩码颜色的alpha，以提高绘制效率
	 */
    public PorterDuffShader(String vertexSource, String fragmentSource,
							Mode mode, boolean setAlpha, boolean reverseAlpha) {
    	super(vertexSource, fragmentSource);
    	mMode = mode.ordinal();
    	mSetAlpha = setAlpha;
    	mReverseAlpha = reverseAlpha;
    }
	
	@Override
	protected boolean onProgramCreated() {
		if (!super.onProgramCreated()) {
			return false;
		}
		if (mSetAlpha) {
			muAlphaHandle = getUniformLocation("uAlpha");
		}
        muSrcColorHandle = getUniformLocation("uSrcColor");
        return true;
	}
	
	
	@Override
	public void setAlpha(float alpha) {
		mAlpha = alpha;
		if (mSetAlpha) {
			GLES20.glUniform1f(muAlphaHandle, alpha);
		}
	}
	
	@Override
	public void setMaskColor(float[] color) {
		color[0] *= mAlpha;
		color[1] *= mAlpha;
		color[2] *= mAlpha;
		color[3] *= mAlpha;	//CHECKSTYLE IGNORE
		
		if (mReverseAlpha && mSetAlpha) {
			GLES20.glUniform1f(muAlphaHandle, mAlpha - color[0]);
		}
		
		GLES20.glUniform4fv(muSrcColorHandle, 1, color, 0);
	}
	
	@Override
	public String toString() {
		return "TextureShader#PorterDuffShader(" + mMode + ")";
	}

}
