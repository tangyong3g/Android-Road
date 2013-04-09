package com.ty.example_unit_3.libgdx.loadmode;

import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.StillModelNode;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.ty.util.DemoWapper;

/**
 * 
 * @author tangyong
 * 
 */
public class LoadModelActivity2 extends DemoWapper {
	
	PerspectiveCamera cam;
	StillModel model;
	Texture[] textures = null;
	boolean hasNormals = false;
	BoundingBox bounds = new BoundingBox();
	String fileName;
	String[] textureFileNames;
	FPSLogger fps = new FPSLogger();
	SpriteBatch batch;
	BitmapFont font;
	//private PrototypeRendererGL20 protoRenderer;
	private StillModelNode instance;
	private StillModelNode instance2;
	private ShaderProgram shader2;
	private ShaderProgram shader1;

	
	
	
	@Override
	public void create() {
		super.create();
	}
	
	@Override
	public void render() {
		super.render();
	}
	
	@Override
	public void resume() {
		super.resume();
	}
	
	@Override
	public void resize(int arg0, int arg1) {
		super.resize(arg0, arg1);
	}

}
