package com.graphics.engine.graphics;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import com.graphics.engine.GLActivity;


/**
 * <br>类描述: OpenGL着色器程序(Shader)的封装类
 * <br>功能详细描述:
 * 着色器是用着色语言描述的指令集合，给开发者提供了扩展性，其分成两个部分：
 * <br>顶点着色器(Vertex shader)，控制顶点是如何变换的；
 * <br>片段着色器(Fragment shader)，控制像素颜色是如何计算的。
 * 
 * @author  dengweiming
 * @date  [2013-10-16]
 */
public abstract class GLShaderProgram implements StaticTextureListener {
	private static final boolean DBG = true;
	private static final boolean LOG_SRC_WHEN_COMPILER_ERROR = false;
	private static final String TAG = GLError.TAG;
	//shader源代码路径，如果为null，则从assets目录下读取
	private static final String SHADER_SRC_PATH = null;	//"/sdcard/shadersrc/";
	static int sCurProgram;
	
	int mProgram;
	String mVertexShaderSource;
	String mFragmentShaderSource;
	
	private String mInitProgramTag;
	private String mBindProgramTag;
	
	/**
	 * 从文件中加载shader源代码
	 * 
	 * @param res
	 * @param fileName
	 * @return
	 */
	public static String loadFromAssetsFile(Resources res, String fileName) {
		String result = null;
		InputStream in = null;
		ByteArrayOutputStream baos = null;
		
		if (SHADER_SRC_PATH == null) {
			try {
				in = res.getAssets().open(fileName);
			} catch (IOException e) {
				e.printStackTrace();
				if (DBG) {
					throw new RuntimeException("GLShaderProgram: IOException: open assets: " + fileName);
				}
			}
		} else {
			try {
				in = new FileInputStream(SHADER_SRC_PATH + fileName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				if (DBG) {
					throw new RuntimeException("GLShaderProgram: FileNotFoundException: open file: " + fileName);
				}
				return result;
			}
		}
		
		int ch = 0;
		baos = new ByteArrayOutputStream();
		try {
			while ((ch = in.read()) != -1) {
				baos.write(ch);
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (DBG) {
				throw new RuntimeException("GLShaderProgram: IOException: baos.write(ch);");
			}
		}
		
		byte[] buff = baos.toByteArray();
		try {
			result = new String(buff, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			if (DBG) {
				throw new RuntimeException("GLShaderProgram: UnsupportedEncodingException");
			}
		}
		
		result = result.replaceAll("\\r\\n", "\n");
		
		try {
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
			if (DBG) {
				throw new RuntimeException("GLShaderProgram: IOException: baos.close();");
			}
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			if (DBG) {
				throw new RuntimeException("GLShaderProgram: IOException: in.close();");
			}
		}
		
//		ShaderStrings.convertSourceCodeToHardCodingString(result, fileName);
		return result;
	}
	
	/**
	 * 加载指定shader
	 * 
	 * @param shaderType	shader的类型：{@link GLES20#GL_VERTEX_SHADER}或者{@link GLES20#GL_FRAGMENT_SHADER}
	 * @param source 		shader的脚本字符串
	 * @return
	 */
	private static int loadShader(int shaderType, String source) {
		// 创建一个新shader
		int shader = GLES20.glCreateShader(shaderType);
		// 若创建成功则加载shader
		if (shader != 0) {
			// 加载shader的源代码
			GLES20.glShaderSource(shader, source);
			// 编译shader
			GLES20.glCompileShader(shader);
			// 存放编译成功shader数量的数组
			int[] compiled = new int[1];
			// 获取Shader的编译情况
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {		// 若编译失败则显示错误日志并删除此shader
				String shaderTypeString = shaderType == GLES20.GL_VERTEX_SHADER ? "vertex shader" : "fragment shader";
				Log.e(TAG, "Could not compile " + shaderTypeString + ":");
				Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
				GLES20.glDeleteShader(shader);
				GLError.clearGLError();
				if (LOG_SRC_WHEN_COMPILER_ERROR) {
					Log.v(TAG, "shader src following:");
					Log.v(TAG, source);
				}
				shader = 0;
			}
		}
		return shader;
	}

	/**
	 * 创建shader程序
	 * 
	 * @param vertexSource
	 * @param fragmentSource
	 * @return
	 */
	private static int createProgram(String vertexSource, String fragmentSource) {
		if (vertexSource == null || fragmentSource == null) {
			return 0;
		}
		GLError.clearGLError();
		// 加载顶点着色器
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
		if (vertexShader == 0) {
			return 0;
		}

		// 加载片元着色器
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
		if (fragmentShader == 0) {
			GLES20.glDeleteShader(vertexShader);
			return 0;
		}

		// 创建程序
		int program = GLES20.glCreateProgram();
		// 若程序创建成功则向程序中加入顶点着色器与片元着色器
		if (program != 0) {
			// 向程序中加入顶点着色器
			GLES20.glAttachShader(program, vertexShader);
			GLError.checkGLError("glAttachShader");
			// 向程序中加入片元着色器
			GLES20.glAttachShader(program, fragmentShader);
			GLError.checkGLError("glAttachShader");
			// 链接程序
			GLES20.glLinkProgram(program);
			// 存放链接成功program数量的数组
			int[] linkStatus = new int[1];
			// 获取program的链接情况
			GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
			// 若链接失败则报错并删除程序
			if (linkStatus[0] != GLES20.GL_TRUE) {
				Log.e(GLError.TAG, "Could not link program: ");
				Log.e(GLError.TAG, GLES20.glGetProgramInfoLog(program));
				GLES20.glDeleteShader(vertexShader);
				GLES20.glDeleteShader(fragmentShader);
				GLES20.glDeleteProgram(program);
				GLError.clearGLError();
				program = 0;
			}
		}
		return program;
	}
	
	public GLShaderProgram() {
		
	}
	
	/**
	 * 从资源中按照指定的文件名读取源代码并创建一个实例
	 * @param res 资源
	 * @param vertexFile 顶点着色程序的源代码文件名
	 * @param fragmentFile 片段着色程序的源代码文件名
	 */
	public GLShaderProgram(Resources res, String vertexFile, String fragmentFile) {
        this(loadFromAssetsFile(res, vertexFile), 
        		loadFromAssetsFile(res, fragmentFile));
	}
	
	/**
	 * 从指定的源代码创建一个实例
	 * @param vertexSource 顶点着色程序的源代码
	 * @param fragmentSource 片段着色程序的源代码
	 */
	public GLShaderProgram(String vertexSource, String fragmentSource) {
		mVertexShaderSource = vertexSource;
		mFragmentShaderSource = fragmentSource;
		
		mInitProgramTag = "init program " + toString();
		mBindProgramTag = "bind program " + toString();
	}
	
	/**
	 * <br>功能简述: 绑定使用这个着色器
	 * <br>功能详细描述:
	 * <br>注意: 需要在GL线程调用
	 * @return
	 */
	public boolean bind() {
		if (mProgram == 0) {
			if (!initShader()) {
				return false;
			}
			if (GLError.checkGLError(mInitProgramTag)) {
				return false;
			}
		}

		if (sCurProgram != mProgram) {
			sCurProgram = mProgram;
			GLES20.glUseProgram(mProgram);
			onProgramBind();
			if (GLError.checkGLError(mBindProgramTag)) {
				if (GLError.LOG_WHEN_GL_ERROR) {
					Log.w(GLError.TAG, "mProgram=" + mProgram);
				}
				return false;
			}
		}
    	return true;
	}
	
	private boolean initShader() {
		GLError.clearGLError();
		mProgram = createProgram(mVertexShaderSource, mFragmentShaderSource);
		return mProgram != 0 && onProgramCreated();
	}
	
	/**
	 * <br>功能简述: 程序创建时的响应方法
	 * <br>功能详细描述: 一般在此时使用 getXXXLocation 方法去获取程序变量的id
	 * <br>注意: 每次纹理失效（例如从其他程序切换回来）都会重新创建，但需要调用{@link #register()}
	 * @return 是否响应成功
	 * @see {@link #getUniformLocation(String)}
	 * @see {@link #getAttribLocation(String)}
	 */
	protected abstract boolean onProgramCreated();
	
	/**
	 * <br>功能简述: 在程序绑定时的响应方法
	 * <br>功能详细描述: 一般在此时使用{@link GLES20#glEnableVertexAttribArray(int)}去启用顶点属性数组
	 * <br>注意:
	 * @see {@link #bind()}
	 */
	protected abstract void onProgramBind();
	
	static void onGLContextLostStatic() {
		sCurProgram = 0;
	}
	
	@Override
	public void onTextureInvalidate() {
		mProgram = 0;
	}
	
	/** 
	 * <br>功能简述: 查找uniform变量（统一属性）的id
	 * <br>功能详细描述:
	 * <br>注意: 在 {@link #onProgramCreated()} 时调用并记录好
	 * @param variable uniform变量的名称
	 * @return
	 */
	protected int getUniformLocation(String variable) {
		if (DBG) {
			final int handle = GLES20.glGetUniformLocation(mProgram, variable);
			if (handle == -1) {
				throw new RuntimeException("cannot find " + variable + ".");
			}
			return handle;
		}
		return GLES20.glGetUniformLocation(mProgram, variable);
	}
	
	/** 
	 * <br>功能简述: 查找attribute变量（顶点属性）的id
	 * <br>功能详细描述:
	 * <br>注意: 在 {@link #onProgramCreated()} 时调用并记录好
	 * @param variable attribute变量的名称
	 * @return
	 */
	protected int getAttribLocation(String variable) {
		if (DBG) {
			final int handle = GLES20.glGetAttribLocation(mProgram, variable);
			if (handle == -1) {
				throw new RuntimeException("cannot find " + variable + ".");
			}
			return handle;
		}
		return GLES20.glGetAttribLocation(mProgram, variable);
	}
	
	/**
	 * <br>功能简述: 为{@link GLShaderWrapper} 的扩展增加的装饰接口
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @return
	 */
	protected GLShaderProgram onRender(RenderContext context) {
		return this;
	}
	
	/**
	 * <br>功能简述: 注册监听纹理失效事件
	 * <br>功能详细描述:
	 * <br>注意: 默认没有注册。在不再需要监听的时候要调用{@link #unregister()}反注册。
	 */
	public void register() {
		TextureManager.getInstance().registerTextureListener(this);
	}
	
	/**
	 * <br>功能简述: 反注册监听纹理失效事件
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void unregister() {
		TextureManager.getInstance().unRegisterTextureListener(this);
	}
	
	/**
	 * <br>功能简述: 注册监听静态纹理失效事件
	 * <br>功能详细描述: 对于静态实例，可以使用这个方法代替{@link #register()}，
	 * 防止纹理管理器在{@link GLActivity}销毁时移除监听者。
	 * <br>注意: 默认没有注册。在不再需要监听的时候要调用{@link #unregisterStatic()}反注册。
	 */
	public void registerStatic() {
		TextureManager.getInstance().registerStaticTextureListener(this);
	}
	
	/**
	 * <br>功能简述: 反注册监听静态纹理失效事件
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void unregisterStatic() {
		TextureManager.getInstance().unRegisterStaticTextureListener(this);
	}
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	protected void onTextureManagerCleanup() {
		
	}

}
