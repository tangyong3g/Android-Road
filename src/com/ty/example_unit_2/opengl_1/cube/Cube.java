package com.ty.example_unit_2.opengl_1.cube;

import java.nio.ByteBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * 定义立方体的顶点，色彩
 * 
 * @author tangyong
 * 
 */
public class Cube {
	//顶点缓冲
	 ByteBuffer verticesBuffer;
	//索引缓冲
	ByteBuffer indexBuffer;
	
	public Cube(){
		//定义的顶点坐标
		 final byte [] s_cubeVertices  = {
			//前面
		    -1, 1, 1, 
			1,-1, 1,  
			1, 1, 1,  
		   -1, -1, 1,
			
		    //后面
			-1, 1, -1,
			1, -1, -1,
			1, 1, -1, 
		   -1, -1, -1,
			
		   //下面
			-1, -1, 1, 
			1, -1, -1, 
			1, -1,1,   
			-1, -1, -1,
			
			//上面
			-1, 1, 1,  
			1, 1, -1,  
			1, 1, 1,   
			-1,1, -1,  
			
			//右面
			1, -1, 1,  
			1, 1, -1,  
			1, 1, 1,   
			1, -1, -1, 
			
			//左面
			-1, -1, 1, 
			-1, 1,-1,  
			-1, 1, 1,  
			-1, -1, -1 
		};
		
		//定义的绘制顺序
		 final byte[] s_cubeIndices = {
			0, 3, 1, 2, 0, 1 ,        /* front */
			6, 5, 4, 5, 7, 4,         /* back */
			8, 11, 9, 10, 8, 9,       /* top */
			15, 12, 13, 12, 14, 13,   /* bottom */
			16, 19, 17, 18, 16, 17,   /* right */
			23, 20, 21, 20, 22, 21    /* left */
		};
		
		//把数据转化到缓冲中去
		verticesBuffer = ByteBuffer.allocateDirect(s_cubeVertices.length);
		verticesBuffer.put(s_cubeVertices);
		verticesBuffer.rewind();
		
		indexBuffer = ByteBuffer.allocateDirect(s_cubeIndices.length);
		indexBuffer.put(s_cubeIndices);
		indexBuffer.rewind(); 
	}
	
	//绘制
	public void draw(GL10 gl10){
		gl10.glVertexPointer(3, GL10.GL_BYTE, 0, verticesBuffer);
		gl10.glDrawElements(GL10.GL_TRIANGLES, 6*6, GL10.GL_UNSIGNED_BYTE, indexBuffer);
	}

}
