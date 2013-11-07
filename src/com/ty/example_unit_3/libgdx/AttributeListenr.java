package com.ty.example_unit_3.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.materials.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Logger;
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

	private Vector3 mStart = new Vector3(0, 50, 100);
	private Vector3 mEnd = new Vector3(0, 25, 50);
	private Vector3 mDerectUnitUp;
	private Vector3 mDerectUnitDown;

	private Vector3 mCurPosition = new Vector3();
	Logger loger = new Logger("test");

	
	@Override
	public void create() {
		super.create();
		initModelInstance();
		initModelInstanceTwo();
		initUnit();
	}

	private void initModelInstance() {

		Vector3 position_one = new Vector3(0.0f, 0, -10);
		Vector3 position_two = new Vector3(25.0f, 0, -10);
		Vector3 position_three = new Vector3(25.0f, 25, -10);
		Vector3 position_four = new Vector3(0.0f, 25, -10);

		Material material = new Material();

		Texture texture = new Texture(Gdx.files.internal("data/bobargb8888-32x32.png"));
		TextureAttribute txAttri = TextureAttribute.createDiffuse(texture);
		// IntAttribute intAttr = new IntAttribute(IntAttribute.CullFace, 0);

		BlendingAttribute blendingAttributeCube = new BlendingAttribute(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		blendingAttributeCube.opacity = 0.8f;

		material.set(txAttri);
		// material.set(intAttr);
		material.set(blendingAttributeCube);

		ModelBuilder builder = new ModelBuilder();
		Model model = builder.createRect(position_one.x, position_one.y, position_one.z, position_two.x, position_two.y, position_two.z, position_three.x,
				position_three.y, position_three.z, position_four.x, position_four.y, position_four.z, 0, 0, 1, material, Usage.Position | Usage.Normal
						| Usage.TextureCoordinates);

		mModelOne = new ModelInstance(model);

	}

	private void initModelInstanceTwo() {

		Vector3 position_one = new Vector3(0.0f, 0, -10.001f);
		Vector3 position_two = new Vector3(35.0f, 0, -10.001f);
		Vector3 position_three = new Vector3(35.0f, 35, -10.001f);
		Vector3 position_four = new Vector3(0.0f, 35, -10.001f);

		Material material = new Material();

		Texture texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		TextureAttribute txAttri = TextureAttribute.createDiffuse(texture);
		// IntAttribute intAttr = new IntAttribute(IntAttribute.CullFace, 0);

		BlendingAttribute blendingAttributeCube = new BlendingAttribute(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		material.set(txAttri);
		// material.set(intAttr);
		material.set(blendingAttributeCube);

		ModelBuilder builder = new ModelBuilder();
		Model model = builder.createRect(position_one.x, position_one.y, position_one.z, position_two.x, position_two.y, position_two.z, position_three.x,
				position_three.y, position_three.z, position_four.x, position_four.y, position_four.z, 0, 0, 1, material, Usage.Position | Usage.Normal
						| Usage.TextureCoordinates);

		mModelTwo = new ModelInstance(model);

	}

	@Override
	protected void render(ModelBatch batch, Array<ModelInstance> instances) {

		
		batch.begin(mCamera);
		batch.render(mModelTwo);
		batch.render(mModelOne);
		batch.end();

		updateCameraPosition();

	}

	Vector3 director = null;

	private void updateCameraPosition() {

		
		
//		Gdx.app.log("test", "position:\t" + mCurPosition.toString()+"end:\t"+mEnd.toString());

		if ((int)mCurPosition.x == mStart.x && (int)mCurPosition.y == mStart.y && (mCurPosition.z)== mStart.z) {
			
			director  = mDerectUnitDown;
			
		}
		
		boolean xE = (int)mCurPosition.x == mEnd.x ;
		boolean yE = (int)mCurPosition.y == mEnd.y ;
		boolean zE = (int)mCurPosition.z == mEnd.z ;
		
//		Gdx.app.log("test", "x==x?"+xE+""+":\ty==y?"+yE+":\tz==z?"+zE);

		
		Gdx.app.log("test", ""+mDerectUnitUp.toString());
		if(xE && yE && zE){
//			Gdx.app.log("test", "com................");
			director  = mDerectUnitUp;
		}
		
		mCurPosition.add(director);
		mCamera.position.set(mCurPosition);
		mCamera.update();
	}

	private void initUnit() {

		mDerectUnitUp = mStart.cpy().sub(mEnd.cpy()).nor().scl(0.1f);
		mDerectUnitDown = mEnd.cpy().sub(mStart.cpy()).nor().scl(0.1f);
		
		Gdx.app.log("test", "position:\t"+mDerectUnitUp.toString());
		

		mCurPosition.set(0, 50.0f, 100.0f);
	}

	@Override
	protected void update(float delaTime) {

	}

	

}
