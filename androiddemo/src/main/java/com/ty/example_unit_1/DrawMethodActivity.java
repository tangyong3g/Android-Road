package com.ty.example_unit_1;

import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.ty.util.DemoWapper;

/**
 * 
 * 运用索引法来处理顶点的绘制
 * 
 * 画方形的时候不知道为什么有些面有问题。等做了旋转的时候再处理
 * 
 * @author Z61
 * 
 */
public class DrawMethodActivity extends AndroidApplication {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//初始化并且设置不使用GL2.0
		initialize(new DrawMethodRender(), false);
	}

	class DrawMethodRender extends DemoWapper {
		
		//网格对象引用
		Mesh mMesh = null;
		//正交投影像机
		OrthographicCamera camera = null;
		//纹理 
		Texture texture = null;

		@Override
		public void create() {
			super.create();
			//初始化网格顶点
			initVertext();
		}

		
		private void initTexture(){
			texture = new Texture(Gdx.files.internal("data/unit_1/ic_launcher.png"));
		}

		private void initVertext() {
			mMesh = new Mesh(true, 8, 36, 
					new VertexAttribute(Usage.Position, 3,"a_position"),
					new VertexAttribute(Usage.Color, 4,"a_color")
//					new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord")
			);
			
			mMesh.setVertices(new float[] {
					
					//前面 4个点
					-0.5f, -0.5f, 0,  0.1f,0.1f,0.2f,1, 
					0.5f, -0.5f, 0,   0.4f,0.4f,0.4f,1, 
					0.5f, 0.5f, 0,    0.2f,0.1f,0.2f,1, 
					-0.5f, 0.5f, 0  , 0.6f,0.1f,0.2f,1, 
					
					//后面  4个点
					-0.5f, -0.5f, -1,  0.1f,0.8f,0.2f,1, 
					0.5f, -0.5f, -1,   0.4f,0.7f,0.5f,1, 
					0.5f, 0.5f, -1,    0.1f,0.5f,0.8f,1, 
					-0.5f, 0.5f, -1  , 0.4f,0.2f,0.8f,1, 
					});
			
			mMesh.setIndices(new short[] {
					//前面
					0,1,2,2,3,0,
					//后面
					4,5,6,6,7,4,
					//左面
					4,0,3,3,7,4,
					//右面
					1,5,6,6,2,1,
					//上面
					7,3,2,2,6,7,
					//下面
					4,0,1,1,5,4
			});
		}

		private float angle = 0;
		@Override
		public void render() {
			super.render();
			GL10 gl = Gdx.graphics.getGL10();
			gl.glClear(gl.GL_COLOR_BUFFER_BIT);

			gl.glColor4f(1, 1, 1, 1);
			gl.glPushMatrix();
//			gl.glRotatef(angle, 1, 1, 0);
			mMesh.render(gl.GL_TRIANGLES);
			gl.glPopMatrix();
			
			angle += 0.8;
		}

		@Override
		public void resize(int arg0, int arg1) {
			super.resize(arg0, arg1);
		}
		
		private int downX;
		private int downY;
		private int roalX;
		private int roalY;
		
		@Override
		public boolean touchDown(int arg0, int arg1, int arg2, int arg3) {
			downX = arg0;
			downY = arg1;
			return super.touchDown(arg0, arg1, arg2, arg3);
		}
		
		@Override
		public boolean touchDragged(int arg0, int arg1, int arg2) {
			roalX = arg0-downX;
			roalY = arg1-downY;
			return super.touchDragged(arg0, arg1, arg2);
		}
		

	}

}
