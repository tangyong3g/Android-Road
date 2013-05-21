package com.ty.example_unit_3.libgdx.loadmode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.ty.util.DemoWapper;

/**
 * 
 * @author tangyong ty_sany@163.com
 *
 */
public class MaterialOpenGL2 extends DemoWapper{
	
	//模型
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
	
	private float mAngle;
	
	@Override
	public void create () {
		super.create();
		
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
		
		//初始化相机
		camera = new PerspectiveCamera(45, 4, 4);
		camera.position.set(3, 3, 3);
		camera.direction.set(-1, -1, -1);
		
		//设置响应输入的接口
		Gdx.input.setInputProcessor(this);
	}
	
	@Override
	public void render () {
		super.render();
		
		GL20 gl = Gdx.graphics.getGL20();
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		if(material.getNumberOfAttributes() == 1)
			gl.glEnable(GL20.GL_TEXTURE_2D);
		
		if(material.getNumberOfAttributes() == 3){
			gl.glDisable(GL20.GL_DEPTH_TEST);
			gl.glEnable(GL20.GL_BLEND);
		}
		else
			gl.glEnable(GL20.GL_DEPTH_TEST);
		
		camera.update();
		
		mAngle += 30 * Gdx.graphics.getDeltaTime();
		
		mModel.render();
	}
	
	@Override
	public void resize (int arg0, int arg1) {
		super.resize(arg0, arg1);
		GL20 gl = Gdx.graphics.getGL20();
		gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	@Override
	public void dispose () {
		super.dispose();
	}
	
	
	@Override
	public boolean touchDown (int arg0, int arg1, int arg2, int arg3) {
		return super.touchDown(arg0, arg1, arg2, arg3);
	}

}
