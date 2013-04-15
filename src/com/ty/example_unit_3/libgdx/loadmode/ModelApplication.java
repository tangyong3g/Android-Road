package com.ty.example_unit_3.libgdx.loadmode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.ty.util.DemoWapper;

/**
 * 
 * @author tangyong
 * 
 */
public class ModelApplication extends DemoWapper {
	
	private final float FIELD_OF_VIEW = 67.0f;
	private PerspectiveCamera mPerspectiveCamera;
	private FPSLogger logger = new FPSLogger();
	private BitmapFont mFont = null;
	private SpriteBatch mSpriteBatch;
	
	//模型纹理
	private Texture mTexture;
	private static final String MODEL_FILE_PATH = "model.obj";
	
	
		
	//初始化相机
	private void initCamare(){
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		
		mPerspectiveCamera  = new PerspectiveCamera(FIELD_OF_VIEW, width, height);
	}
	
	//创建模型
	private StillModel initModelData(){
		StillModel model = null;
		model = ModelLoaderRegistry.loadStillModel(Gdx.files.internal(MODEL_FILE_PATH));
		mTexture = new Texture(Gdx.files.internal("model.png"));
		
		MaterialAttribute attribute = new TextureAttribute(mTexture, 0, TextureAttribute.diffuseTexture);
		Material material = new Material("model", attribute);
		
		model.setMaterial(material);
		return model;
	}
	
	
	@Override
	public void create() {
		super.create();
		initCamare();
		initModelData();
		
		mSpriteBatch = new SpriteBatch();
		mFont = new BitmapFont();
	}
	
	
	
	@Override
	public void resize(int arg0, int arg1) {
		super.resize(arg0, arg1);
		
		Gdx.gl.glEnable(Gdx.gl20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(Gdx.gl20.GL_CULL_FACE);
	}
	
	
	@Override
	public void render() {
		super.render();
		Gdx.gl.glClear(Gdx.gl20.GL_DEPTH_BUFFER_BIT | Gdx.gl20.GL_COLOR_BUFFER_BIT);
		
		
		
		
		
		
		//显示出来当前的 fps 
		mSpriteBatch.begin();
		mFont.draw(mSpriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond(), 20, 20);
		mSpriteBatch.end();
		
		logger.log();
	}
	
	@Override
	public void pause() {
		super.pause();
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}

	
}
