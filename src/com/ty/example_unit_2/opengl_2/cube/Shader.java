package com.ty.example_unit_2.opengl_2.cube;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.ty.example_unit_2.opengl_2.shading.Triangle;
import com.ty.util.ShaderUtil;

/**
 * 
 * 
 * @author tangyong
 * 
 */
public class Shader {

	protected String mVertextShader;
	protected String mFragmentShader;
	protected int mProgram;
	protected int maPositionHandle;

	public void initShader(String vertextFilePath, String fragFilePath,
			Context context) {
		mVertextShader = ShaderUtil.loadFromAssetsFile(vertextFilePath,context.getResources());
		mFragmentShader = ShaderUtil.loadFromAssetsFile(fragFilePath,context.getResources());

		mProgram = ShaderUtil.createProgram(mVertextShader, mFragmentShader);
       maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
	}
	
	
	//初始化着色器
	public void initShader(Context mv){
		
		//加载顶点着色器的脚本内容
		mVertextShader=ShaderUtil.loadFromAssetsFile("data/unit2/shader/shading/vertex.sh", mv.getResources());
        //加载片元着色器的脚本内容
		mFragmentShader=ShaderUtil.loadFromAssetsFile("data/unit2/shader/shading/frag.sh", mv.getResources());  
        //基于顶点着色器与片元着色器创建程序
        mProgram = ShaderUtil.createProgram(mVertextShader, mFragmentShader);
        
       
     
        /*
        maColorHandle= GLES20.glGetAttribLocation(mProgram, "aColor");
        //获取程序中总变换矩阵引用id
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
       */ 
	}

}
