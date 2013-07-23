package com.ty.example_unit_2.opengl_2.shading;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.badlogic.gdx.math.Matrix4;
import com.ty.animation.Transformation;
import com.ty.util.ShaderUtil;


public class Triangle {

	public static float[] mProjMatrix = new float[16];// 4x4矩阵 投影用
	public static float[] mVMatrix = new float[16];// 摄像机位置朝向9参数矩阵
	public static float[] mMVPMatrix;// 最后起作用的总变换矩阵

	int mProgram;// 自定义渲染管线程序id
	int muMVPMatrixHandle;// 总变换矩阵引用id
	int maPositionHandle; // 顶点位置属性引用id
	int maColorHandle; // 顶点颜色属性引用id
	String mVertexShader;// 顶点着色器
	String mFragmentShader;// 片元着色器
	static float[] mMMatrix = new float[16];// 具体物体的移动旋转矩阵，旋转、平移

	FloatBuffer mVertexBuffer;// 顶点坐标数据缓冲
	FloatBuffer mColorBuffer;// 顶点着色数据缓冲
	int vCount = 0;
	float xAngle = 0;// 绕x轴旋转的角度

	public Triangle(GLSurfaceView mv) {
		initVertexData();
		initShader(mv);
	}
	
	
	private void initVertexData(){
		
		float [] vertexPosition  = 
		{
		     0f, 1.0f, 0.0f,
		    -1,-1, 0.0f,
		    1,-1, 0.0f
		};
		
		vCount = vertexPosition.length/3;
		
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexPosition.length*4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertexPosition);
        mVertexBuffer.position(0);
        
        
        float colors[]=new float[]
        {
        		1,1,1,0,
        		0,0,1,0,
        		0,1,0,0
        };
        
        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length*4);
        cbb.order(ByteOrder.nativeOrder());
        mColorBuffer = cbb.asFloatBuffer();
        mColorBuffer.put(colors);
        mColorBuffer.position(0);
	}
	
	//初始化着色器
	private void initShader(GLSurfaceView mv){
		
		//加载顶点着色器的脚本内容
        mVertexShader=ShaderUtil.loadFromAssetsFile("data/unit2/shader/shading/vertex.sh", mv.getResources());
        //加载片元着色器的脚本内容
        mFragmentShader=ShaderUtil.loadFromAssetsFile("data/unit2/shader/shading/frag.sh", mv.getResources());  
        //基于顶点着色器与片元着色器创建程序
        mProgram = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        maColorHandle= GLES20.glGetAttribLocation(mProgram, "aColor");
        //获取程序中总变换矩阵引用id
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix"); 
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, Triangle.getFianlMatrix(mMMatrix), 0); 
	}
	
	public void draw(float[] matrix){
		
		//使用某个着色程序　
		GLES20.glUseProgram(mProgram);
		 //初始化变换矩阵
//        Matrix.setRotateM(mMMatrix,0,0,0,1,0);
        //设置沿Z轴正向位移1
//        Matrix.translateM(mMMatrix,0,0,0,1);
        //设置绕x轴旋转
//        Matrix.rotateM(mMMatrix,0,xAngle,1,0,0);
		
		 mMMatrix = matrix;
		
		//三角形的顶点坐标使用了 muMVPMatrixHandle 
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, Triangle.getFianlMatrix(mMMatrix), 0); 
		
		GLES20.glVertexAttribPointer
		(
				maPositionHandle,
				3, 
				GLES20.GL_FLOAT, 
				false,3*4,
				mVertexBuffer
        );
		
	   GLES20.glVertexAttribPointer  
         (
        		maColorHandle,
         		4,
         		GLES20.GL_FLOAT,
         		false,
                4*4,
                mColorBuffer
         );
         //允许顶点位置数据数组
         GLES20.glEnableVertexAttribArray(maColorHandle);  
		 GLES20.glEnableVertexAttribArray(maPositionHandle);
		
		 GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount); 
	}
	
	public static float[] getFianlMatrix(float[] spec)
    {
    	mMVPMatrix=new float[16];
    	Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, spec, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);        
        return mMVPMatrix;
    }

}
