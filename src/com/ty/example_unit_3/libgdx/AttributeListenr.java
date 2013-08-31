package com.ty.example_unit_3.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.IntAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.ty.example_unit_3.libgdx.ex.Base3D;

/**
 * 
 * 
 * 说明:
 * 
 * 深度测试打开时，绘制顺序不会受影响，如果是关闭的那么就应该在远处的先画出来。前面的东西会给挡住。
 * 
 * @author 师爷GBK[ty_sany@163.com] 2013-8-31
 * 
 */
public class AttributeListenr extends Base3D {

	private ModelInstance mModelOne;
	private ModelInstance mModelTwo;

	@Override
	public void create() {
		super.create();
		initModelInstance();
		initModelInstanceTwo();
	}

	private void initModelInstance() {

		Vector3 position_one = new Vector3(0.0f, 0, 0);
		Vector3 position_two = new Vector3(25.0f, 0, 0);
		Vector3 position_three = new Vector3(25.0f, 25, 0);
		Vector3 position_four = new Vector3(0.0f, 25, 0);

		Material material = new Material();

		Texture texture = new Texture(Gdx.files.internal("data/ic_launcher.png"));
		TextureAttribute txAttri = TextureAttribute.createDiffuse(texture);
		IntAttribute intAttr = new IntAttribute(IntAttribute.CullFace, 0);

		BlendingAttribute blendingAttributeCube = new BlendingAttribute(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		blendingAttributeCube.opacity = 0.8f;

		material.set(txAttri);
		material.set(intAttr);
		 material.set(blendingAttributeCube);

		ModelBuilder builder = new ModelBuilder();
		Model model = builder.createRect(position_one.x, position_one.y, position_one.z, position_two.x, position_two.y, position_two.z, position_three.x,
				position_three.y, position_three.z, position_four.x, position_four.y, position_four.z, 0, 0, 1, material, Usage.Position | Usage.Normal
						| Usage.TextureCoordinates);

		mModelOne = new ModelInstance(model);

	}

	private void initModelInstanceTwo() {

		Vector3 position_one = new Vector3(0.0f, 0, -10);
		Vector3 position_two = new Vector3(25.0f, 0, -10);
		Vector3 position_three = new Vector3(25.0f, 25, -10);
		Vector3 position_four = new Vector3(0.0f, 25, -10);

		Material material = new Material();

		Texture texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		TextureAttribute txAttri = TextureAttribute.createDiffuse(texture);
		IntAttribute intAttr = new IntAttribute(IntAttribute.CullFace, 0);

		BlendingAttribute blendingAttributeCube = new BlendingAttribute(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		material.set(txAttri);
		material.set(intAttr);
		 material.set(blendingAttributeCube);

		ModelBuilder builder = new ModelBuilder();
		Model model = builder.createRect(position_one.x, position_one.y, position_one.z, position_two.x, position_two.y, position_two.z, position_three.x,
				position_three.y, position_three.z, position_four.x, position_four.y, position_four.z, 0, 0, 1, material, Usage.Position | Usage.Normal
						| Usage.TextureCoordinates);

		mModelTwo = new ModelInstance(model);

	}

	@Override
	protected void render(ModelBatch batch, Array<ModelInstance> instances) {

		DefaultShader.defaultDepthFunc = 0;
		DefaultShader.defaultDepthFunc = GL10.GL_LEQUAL;

		batch.begin(mCamera);

		batch.render(mModelOne);

		batch.end();
		
		
		batch.begin(mCamera);
		batch.render(mModelTwo);
		
		batch.end();
		
		
	
		

	}

	@Override
	protected void update(float delaTime) {

	}

}
