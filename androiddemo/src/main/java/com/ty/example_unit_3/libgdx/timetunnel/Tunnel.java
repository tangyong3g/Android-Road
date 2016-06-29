package com.ty.example_unit_3.libgdx.timetunnel;

import android.opengl.GLES20;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.IntAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.utils.Array;
import com.ty.example_unit_3.libgdx.ex.Base3D;



/**
 * 
 * @author tangyong
 *
 */
public class Tunnel extends Base3D{

	/*模型*/
	private ModelInstance mFirstInstance;
	private ModelInstance mSecondInstance;
	private ModelInstance mStarsInstance;
	
	private static final String GD_FIRST_TEXTURE_FILENAME = "data/unit3/pic/gd_scene_3.png";
	private static final String GD_FIRST_MODEL_FILENAME = "data/unit3/model/gd_visiable.obj";
	
	private static final String GD_SECOND_MODEL_FILENAME = "data/unit3/model/gd_visiable.obj";
	
	private static final String GD_STARS_TEXTURE_FILENAME = "data/unit3/pic/star_3.png";
	private static final String GD_STARS_MODEL_FILENAME = "data/unit3/model/cubes.obj";
	
	private int mLoadedResCount = 0;
	private boolean mLoadFinished = false;
	
	
	@Override
	public void create() {
		super.create();
		
		initResource();
	}
	
	private void initResource() {
		
		assets.load(GD_FIRST_TEXTURE_FILENAME, Texture.class);
		assets.load(GD_FIRST_MODEL_FILENAME, Model.class);
		
		assets.load(GD_SECOND_MODEL_FILENAME, Model.class);
		
		assets.load(GD_STARS_TEXTURE_FILENAME, Texture.class);
		assets.load(GD_STARS_MODEL_FILENAME, Model.class);
		
	}


	@Override
	protected void onLoadPercentChange(float currentPercent) {
		super.onLoadPercentChange(currentPercent);
		initModelInstance();
	}
	
	
	private void loadThirdInstance() {
		
		mStarsInstance = new ModelInstance(assets.get(GD_STARS_MODEL_FILENAME, Model.class));
		Material material = mStarsInstance.materials.get(0);

		Texture tx = assets.get(GD_STARS_TEXTURE_FILENAME, Texture.class);
		TextureAttribute txAttr = TextureAttribute.createDiffuse(tx);
		
		BlendingAttribute blendingAttribute = new BlendingAttribute(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		material.set(blendingAttribute);
		
		IntAttribute intAttr = new IntAttribute(IntAttribute.CullFace, 0);
		
		material.set(intAttr);
		material.set(txAttr);
		
	}

	private void loadSecondInstance() {
		
		mSecondInstance = new ModelInstance(assets.get(GD_SECOND_MODEL_FILENAME, Model.class));
		Material material = mSecondInstance.materials.get(0);

		Texture tx = assets.get(GD_FIRST_TEXTURE_FILENAME, Texture.class);
		TextureAttribute txAttr = TextureAttribute.createDiffuse(tx);
		
		BlendingAttribute blendingAttribute = new BlendingAttribute(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		material.set(blendingAttribute);
		
		material.set(txAttr);
		
	}

	private void loadFirstInstance(){
		
		mFirstInstance = new ModelInstance(assets.get(GD_FIRST_MODEL_FILENAME, Model.class));
		Material material = mFirstInstance.materials.get(0);

		Texture tx = assets.get(GD_FIRST_TEXTURE_FILENAME, Texture.class);
		TextureAttribute txAttr = TextureAttribute.createDiffuse(tx);
		
		BlendingAttribute blendingAttribute = new BlendingAttribute(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		material.set(blendingAttribute);
		material.set(txAttr);

	}
	
	private void initModelInstance(){
		mLoadedResCount ++;
		
		switch (mLoadedResCount) {
		
		case 2:
			
			loadFirstInstance();
			
			break;
			
		case 3:
			loadSecondInstance();
			
			break;
		case 5:
			
			loadThirdInstance();
			
			break;
		default:
			break;
		}
	}

	
	@Override
	protected void onLoaded() {
		super.onLoaded();
		
		mLoadFinished = true;
	}
	

	@Override
	protected void render(ModelBatch batch, Array<ModelInstance> instances) {
		
	
		if(mFirstInstance != null){
			batch.begin(mCamera);
			batch.render(mFirstInstance);
			batch.end();
		}
		
		if(mSecondInstance != null){
			batch.begin(mCamera);
			batch.render(mSecondInstance);
			batch.end();
		}
		
		if(mStarsInstance != null){
			
			batch.begin(mCamera);
			DefaultShader.defaultDepthFunc  = 0;
			batch.render(mStarsInstance);
			DefaultShader.defaultDepthFunc  = GL10.GL_LEQUAL;
			batch.end();
			
		}
		
	}

	
	@Override
	protected void update(float delaTime) {
		
	}

}
