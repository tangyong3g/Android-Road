package com.ty.example_unit_1.cube;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

/**
 * 加载着色器
 * @author liuwenqin
 *
 */
public class ShaderUtil {
	private static final String LOG_TAG = "ES20_ERROR";

	/**
	 * 创建着色器
	 * @param vertexSource 顶点着色器
	 * @param fragmentSource 片元着色器
	 * @return
	 */
	public static int createProgram(String vertexSource, String fragmentSource) {
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
		if (vertexShader == 0) {
			return 0;
		}
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
		if (fragmentShader == 0) {
			return 0;
		}
		//创建着色器程序
		int program = GLES20.glCreateProgram();
		if (program != 0) {
			//绑定顶点着色器
			GLES20.glAttachShader(program, vertexShader);
			checkGLError("glAttachShader");
			//绑定片元着色器
			GLES20.glAttachShader(program, fragmentShader);
			checkGLError("glAttachShader");
			//编译程序
			GLES20.glLinkProgram(program);
			int[] linkStatus = new int[1];
			//获取着色器程序编译状态
			GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
			if(linkStatus[0] != GLES20.GL_TRUE) {
				Log.e(LOG_TAG, "Could not create program: ");
				Log.e(LOG_TAG, GLES20.glGetProgramInfoLog(program));
				//删除创建失败的着色器程序
				GLES20.glDeleteProgram(program);
				program = 0;
			}
		}
		return program;
	}
	
	/**
	 * 检查每一步操作是否有错
	 * @param op
	 */
	public static void checkGLError(String op) {
		int error;
		while((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(LOG_TAG, op + ": glError " + error);
			throw new RuntimeException(op + ": glError " + error);
		}
	}

	/**
	 * 加载脚本
	 * @param shaderType 脚本类型
	 * @param source 脚本内容
	 * @return 脚本ID。成功，返回值不为0
	 */
	public static int loadShader(int shaderType, String source) {
		//创建shader
		int shader = GLES20.glCreateShader(shaderType);
		if (shader != 0) {	//创建成功
			//加载脚本内容
			GLES20.glShaderSource(shader, source);
			//编译脚本内容
			GLES20.glCompileShader(shader);
			int[] compiled = new int[1];
			//获取编译脚本状态
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {	//编译失败
				Log.e(LOG_TAG, "Could not compile " + shaderType + ":");
				Log.e(LOG_TAG, GLES20.glGetShaderInfoLog(shader));
				//删除脚本
				GLES20.glDeleteShader(shader);
				shader = 0;
			}
		}
		return shader;
	}

	/**
	 * 读取脚本内容
	 * @param file 脚本名
	 * @param r 系统资源
	 * @return 脚本内容
	 */
	public static String loadAssetsFromFile(String file, Resources r) {
		String result = null;
		try {
			InputStream inputStream = r.getAssets().open(file);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			int ch = -1;
			while ((ch = inputStream.read()) != -1) {
				byteArrayOutputStream.write(ch);
			}
			byte[] bytes = byteArrayOutputStream.toByteArray();
			result = new String(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}