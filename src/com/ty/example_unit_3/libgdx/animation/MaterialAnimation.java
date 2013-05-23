package com.ty.example_unit_3.libgdx.animation;


import android.opengl.Matrix;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.ty.example_unit_3.libgdx.ex.Matrix4Ex;
import com.ty.util.DemoWapper;

public class MaterialAnimation extends DemoWapper{
	
	// 模型
	private StillModel mModel;
	//纹理属性
	private TextureAttribute textureAttribute;
	//颜色属性
	private ColorAttribute colorAttribute;
	//混合属性
	private BlendingAttribute blendingAttribute;
	
	//物料
	private Material material;
	//纹理 
	private Texture texture;
	//相机 
	private Camera camera;

	@Override
	public void create () {
		//加载模型 
		mModel =  ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/cube.obj"));
		//生成纹理 
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"), true);
		//创建物料属性
		textureAttribute = new TextureAttribute(texture, 0, "ic");
		//创建颜色属性
		colorAttribute = new ColorAttribute(Color.ORANGE, "Orange");
		//创建混合属性
		blendingAttribute =  new BlendingAttribute("Additive", GL10.GL_ONE, GL10.GL_ONE);
		
		//创建物料
		material = new Material();
		mModel.setMaterial(material);
		
		material.addAttribute(textureAttribute);
		material.addAttribute(colorAttribute);
		material.addAttribute(blendingAttribute);
		
		//初始化相机
		camera = new PerspectiveCamera(45, 4, 4);
		camera.position.set(3, 3, 3);
		camera.direction.set(-1, -1, -1);
		
		//设置响应输入的接口
		Gdx.input.setInputProcessor(this);
	}
	
	@Override
	public void resize (int width, int height) {
		
		GL10 gl = Gdx.graphics.getGL10();
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glEnable(GL10.GL_BLEND);
		gl.glEnable(GL10.GL_DEPTH_TEST);
	
	}
	
	private float angleY = 0.0f;
	Matrix4 temp = new Matrix4();
	
	@Override
	public void render () {
		
		GL10 gl = Gdx.graphics.getGL10();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	
		camera.update();
		camera.apply(gl);
		
		 temp.idt();
		 gl.glMatrixMode(GL10.GL_MODELVIEW);
		
		 gl.glPushMatrix();
		 float[] modelview = new float[16];
       Gdx.gl11.glGetFloatv( GL11.GL_MODELVIEW_MATRIX, modelview, 0);      //获取当前矩阵     
       float[] x_axis = { 1, 0, 0, 0 };
       float[] y_axis = { 0, 1, 0, 0 };
       
       Matrix.invertM(modelview, 0, modelview, 0);                                    //求逆矩阵
       Matrix.multiplyMV(x_axis, 0, modelview, 0, x_axis, 0);                   //获取世界x轴在模型坐标系里的指向（w轴）
       Matrix.multiplyMV(y_axis, 0, modelview, 0, y_axis, 0);
       
       Matrix4 matric_x = new Matrix4();
       matric_x.rotate(new Vector3(y_axis[0], y_axis[1], y_axis[2]), mAngleX);
       
       Matrix4 matric_y = new Matrix4();
       matric_y.rotate(new Vector3(x_axis[0], x_axis[1], x_axis[2]), -mAngleY);
       
       //这一句和上面两句是等价的
//		 gl.glRotatef(mAngleX, y_axis[0], y_axis[1], y_axis[2]);
//       gl.glRotatef(-mAngleY, x_axis[0],x_axis[1], x_axis[2]);
       
       gl.glMultMatrixf(matric_x.val, 0);
       gl.glMultMatrixf(matric_y.val, 0);
       
     
       temp.mul(matric_x);
       temp.mul(matric_y);
       
		// That's it. Materials are bound automatically on render
		mModel.render();
		
		gl.glPopMatrix();
	}
	
	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		
		float [] euler = new float[3];
		
		Matrix4Ex.convertMatrixToEulerAngle(temp.val, 0, euler);
		
		Log.i("tyler.tang","得出来的欧拉角呢 x"+euler[0]+"y"+euler[1]+"z"+euler[2]);
		
		return super.touchUp(screenX, screenY, pointer, button);
	}

	private float mAngleX;
	private float mAngleY;
	
	private float mPreY;
	private float mPreX;
	
	private static final float  TOUCH_SCALE_FACTOR  = 180/320.0f;

	@Override
	public boolean touchDragged (int x, int y, int arg2) {
		
		float dy = y - mPreY;//计算触控笔Y位移
		float dx = x - mPreX;//计算触控笔X位移
		  
		mAngleX += dx * TOUCH_SCALE_FACTOR;//设置沿Y轴旋转角度
		mAngleY += dy * TOUCH_SCALE_FACTOR;//设置沿X轴旋转角度
		  
		mPreY = y;
		mPreX = x;
		
		return super.touchDragged(x, y, arg2);
	}
	

}
