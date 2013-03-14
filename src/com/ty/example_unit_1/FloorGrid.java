package com.ty.example_unit_1;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/*
 * 绘制方式时有点问题。需要讨论。
 */
public class FloorGrid {
	
	//横数量 ,深度的数量
	int xCount , zCount;
	
	//网络
	Mesh mMesh;
	//  色彩失量
	Vector3 mColor = new Vector3(0.1f,0.1f,1f);
	
	//初始化的单个格子的位置，以及地板要画多大的面
	public FloorGrid(Vector2 filed){
		mMesh = oneTitle(1.0f, 0.0f, 1.0f);
//		xCount = (int)filed.x;
//		zCount = (int)filed.y;
		xCount = 1;
		zCount = 8;
	}
	
	public FloorGrid() {
	}
	
	public Mesh oneTitle(float x, float y ,float z ){
		
		//4 顶点数，5表示index数量
		mMesh = new Mesh(true, 4, 4, new VertexAttribute(Usage.Position, 3,
				"a_position"));
		
		//设置顶点
		mMesh.setVertices(new float[]{
				0,0,0,
				1,0,0,
				1,0,1,
				0,0,1
		});
		
		//设置绘制顺序
		mMesh.setIndices(new short[]{0,1,2,3});
		return mMesh;
	}
	
	//设置色彩 
	public void setColor(float r, float g, float b){
		mColor = new Vector3(r,g,b);
	}
	
	public void render(GL10 gl, int renderType) {
		gl.glPushMatrix();
		for(int x=0; x<xCount; x++) {
			for(int z=0; z<zCount; z++) {
				gl.glPushMatrix();
				gl.glTranslatef(x, 0, z);
				mMesh.render(renderType);
				gl.glPopMatrix();
			}
		}
		gl.glPopMatrix();
	}
	
	
	//绘制 
	public void render_(GL10 gl, int renderType){
		gl.glPushMatrix();
		
		//一行一行的绘制每人上图元 
		for(int x = 0; x< xCount; x++){
			for(int z = 0; z< zCount; z++){
				gl.glPushMatrix();
				//一列一列的画
				gl.glTranslatef(x, 0, z);
				mMesh.render(renderType);
				gl.glPopMatrix();
			}
		}
		gl.glPopMatrix();
	}
	
	
	//绘制图形
	public void renderWireframe(GL10 gl) {
		gl.glColor4f(mColor.x, mColor.y, mColor.z, 1);
		render(gl, GL10.GL_LINE_LOOP);
	}

}
