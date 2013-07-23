package com.ty.example_unit_2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;

import com.example.android_begin_gl_3d.R;
import com.ty.util.Constant;
import com.ty.util.ImageUtil;
import com.ty.util.MatrixState;
import com.ty.util.ShaderUtil;

/**
 * 
 *	
 *  说明: 
 *
 * @author 师爷GBK[ty_sany@163.com]
 * 2013-7-24
 *
 */
public class SquareActivity extends Activity{
	
	
	Context mContext;
	GLSurfaceView mSurfaceView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
		
		mSurfaceView = new GLSurfaceView(mContext);
		mSurfaceView.setEGLContextClientVersion(2);
		mSurfaceView.setRenderer(new SquareRender());
		mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		
		setContentView(mSurfaceView);
	}
	
	
	
	class SquareView extends GLSurfaceView{

		public SquareView(Context context) {
			super(context);
		}
	}
	
	
	
	
	class SquareRender implements Renderer{
		
		Square mSquare;
		private int mTextureId;
		
		
		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			mSquare =new Square(mContext);
			mTextureId  = ImageUtil.initTexture(R.drawable.cube_simple, mContext);
			MatrixState.setInitStack();
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			
			
			float ratio = (float)width/height;
			//设置视口
			GLES20.glViewport(0, 0, width, height);
			MatrixState.setProjectFrustum(-ratio, ratio, -1, 1, 2, 100);
			//相机位置
			MatrixState.setCamera(0,0, 0, 0, -1, 0, 0, 1, 0);
			
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			GLES20.glEnable(GLES20.GL_CULL_FACE);
			
			MatrixState.pushMatrix();
			MatrixState.translate(0, 0, -3);
			mSquare.drawSelf(mTextureId);
			
			MatrixState.popMatrix();
			
		}
		
	}
	
	class Square
	{
		
		int mProgram;//自定义渲染管线着色器程序id
	    int muMVPMatrixHandle;//总变换矩阵引用
	    int maPositionHandle; //顶点位置属性引用  
	    int maColorHandle; //顶点颜色属性引用 
	    int maTexCoorHandle;
	    String mVertexShader;//顶点着色器代码脚本  
	    String mFragmentShader;//片元着色器代码脚本
		
		FloatBuffer   mVertexBuffer;//顶点坐标数据缓冲
		FloatBuffer   mColorBuffer;//顶点着色数据缓冲
		FloatBuffer   mTextureBuffer;
	    int vCount=0;  
	    
	    public Square(Context context)
	    {    	
	    	//初始化顶点坐标与着色数据
	    	initVertexData();
	    	//初始化shader        
	    	initShader(context);
	    }
	    
	    //初始化顶点坐标与着色数据的方法
	    public void initVertexData()
	    {
	    	//顶点坐标数据的初始化================begin============================
	        
	        float vertices[]=new float[]
	        {
	        	//前面
			   -Constant.UNIT_SIZE,Constant.UNIT_SIZE,Constant.UNIT_SIZE,
	    	   -Constant.UNIT_SIZE,-Constant.UNIT_SIZE,Constant.UNIT_SIZE,
	    	   Constant.UNIT_SIZE,Constant.UNIT_SIZE,Constant.UNIT_SIZE,
	        		
	    	   -Constant.UNIT_SIZE,-Constant.UNIT_SIZE,Constant.UNIT_SIZE,
			    Constant.UNIT_SIZE,-Constant.UNIT_SIZE,Constant.UNIT_SIZE,
			    Constant.UNIT_SIZE,Constant.UNIT_SIZE,Constant.UNIT_SIZE,
			    
			    /*
	        	//后面
			    -Constant.UNIT_SIZE,Constant.UNIT_SIZE, -Constant.UNIT_SIZE,
	            -Constant.UNIT_SIZE,-Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
		    	 Constant.UNIT_SIZE,Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
		        		
		    	-Constant.UNIT_SIZE,-Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
				 Constant.UNIT_SIZE,-Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
				 Constant.UNIT_SIZE,Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
		    	
	        	//左面
		     	-Constant.UNIT_SIZE,Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
		    	-Constant.UNIT_SIZE,-Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
		    	 -Constant.UNIT_SIZE,Constant.UNIT_SIZE,Constant.UNIT_SIZE,
		    	 
		    	 -Constant.UNIT_SIZE,-Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
			    -Constant.UNIT_SIZE,-Constant.UNIT_SIZE,Constant.UNIT_SIZE,
			    -Constant.UNIT_SIZE,Constant.UNIT_SIZE,Constant.UNIT_SIZE,
		    	
	        	//右面
				Constant.UNIT_SIZE,Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
		    	Constant.UNIT_SIZE,-Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
		    	 Constant.UNIT_SIZE,Constant.UNIT_SIZE,Constant.UNIT_SIZE,
		    	 
		    	 Constant.UNIT_SIZE,-Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
			    Constant.UNIT_SIZE,-Constant.UNIT_SIZE,Constant.UNIT_SIZE,
			    Constant.UNIT_SIZE,Constant.UNIT_SIZE,Constant.UNIT_SIZE,
		    
	        	//上面
		    	-Constant.UNIT_SIZE,Constant.UNIT_SIZE, -Constant.UNIT_SIZE,
		    	-Constant.UNIT_SIZE,Constant.UNIT_SIZE,Constant.UNIT_SIZE,
		    	Constant.UNIT_SIZE,Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
		    	
		    	-Constant.UNIT_SIZE,Constant.UNIT_SIZE,Constant.UNIT_SIZE,
		    	Constant.UNIT_SIZE,Constant.UNIT_SIZE,Constant.UNIT_SIZE,
		    	Constant.UNIT_SIZE,Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
		    	
		    	
	        	//下面
		    	-Constant.UNIT_SIZE,-Constant.UNIT_SIZE, -Constant.UNIT_SIZE,
		    	-Constant.UNIT_SIZE,-Constant.UNIT_SIZE,Constant.UNIT_SIZE,
		    	Constant.UNIT_SIZE, -Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
		    	
		    	-Constant.UNIT_SIZE,-Constant.UNIT_SIZE,Constant.UNIT_SIZE,
		    	Constant.UNIT_SIZE,-Constant.UNIT_SIZE,Constant.UNIT_SIZE,
		    	Constant.UNIT_SIZE,-Constant.UNIT_SIZE,-Constant.UNIT_SIZE,
		    	*/
	        };
	        
	        vCount = 6*1;
	        
	        //创建顶点坐标数据缓冲
	        //vertices.length*4是因为一个整数四个字节
	        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
	        vbb.order(ByteOrder.nativeOrder());//设置字节顺序
	        mVertexBuffer = vbb.asFloatBuffer();//转换为Float型缓冲
	        mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
	        mVertexBuffer.position(0);//设置缓冲区起始位置
	        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
	        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
	        //顶点坐标数据的初始化================end============================
	        
	    	//顶点颜色值数组，每个顶点4个色彩值RGBA
	        float textures[]=new float[]{
	        		//前面        
	        		0,0,
	        		0,1,
	        		1,0,
	        		
	        		0,1,
	        		1,1,
	        		1,0,
	        		
	        		/*
	        		//后面
	        		0,0,
	        		0,1,
	        		1,0,
	        		
	        		0,1,
	        		1,1,
	        		1,0,
	        		
	        		//左面
	        		0,0,
	        		0,1,
	        		1,0,
	        		
	        		0,1,
	        		1,1,
	        		1,0,
	        	
	        		//右面
	        		0,0,
	        		0,1,
	        		1,0,
	        		
	        		0,1,
	        		1,1,
	        		1,0,
	        		
	        		//上面
	        		0,0,
	        		0,1,
	        		1,0,
	        		
	        		0,1,
	        		1,1,
	        		1,0,
	        			
	        	
	        		//下面
	        		0,0,
	        		0,1,
	        		1,0,
	        		
	        		0,1,
	        		1,1,
	        		1,0,	
	        		*/
	        };
	        //创建顶点着色数据缓冲
	        ByteBuffer cbb = ByteBuffer.allocateDirect(textures.length*4);
	        cbb.order(ByteOrder.nativeOrder());//设置字节顺序
	        mTextureBuffer = cbb.asFloatBuffer();//转换为Float型缓冲
	        mTextureBuffer.put(textures);//向缓冲区中放入顶点着色数据
	        mTextureBuffer.position(0);//设置缓冲区起始位置
	        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
	        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
	        //顶点着色数据的初始化================end============================
	    }
	    
	    
		
		//初始化着色器
		private void initShader(Context mv){
			
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
		}
	    
	    
	    public void drawSelf(int textuid)
	    {        
	    	
	    	 //制定使用某套shader程序
	    	 GLES20.glUseProgram(mProgram);
	         //将最终变换矩阵传入shader程序
	         GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
	         //为画笔指定顶点位置数据
	         GLES20.glVertexAttribPointer  
	         (
	         		maPositionHandle,   
	         		3, 
	         		GLES20.GL_FLOAT, 
	         		false,
	                3*4,   
	                mVertexBuffer
	         );       
	         //为画笔指定顶点着色数据
	         GLES20.glVertexAttribPointer(
	        		 maTexCoorHandle, 
	        		 2, 
	        		 GLES20.GL_FLOAT,
	 				 false, 
	 				 2 * 4, 
	 				mTextureBuffer
	         );
	         //允许顶点位置数据数组
	         GLES20.glEnableVertexAttribArray(maPositionHandle);
	     	GLES20.glEnableVertexAttribArray(maTexCoorHandle);
	     	GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textuid);
	         
	         //绘制立方体         
	         GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0, vCount); 
	 
	    }
		
	}

}
