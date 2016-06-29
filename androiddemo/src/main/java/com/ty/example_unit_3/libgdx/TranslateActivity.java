
package com.ty.example_unit_3.libgdx;

import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.materials.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.ty.example_unit_3.libgdx.ex.Base3D;

/** @author tangyong ty_sany@163.com */
public class TranslateActivity extends AndroidApplication {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		initialize(new TranslateListener(), true);
		super.onCreate(savedInstanceState);
	}

	class TranslateListener extends Base3D {

		ModelInstance modelInstance;
		float mAngle = 0.0f;
		private Vector3 mVector = new Vector3(0, 1, 0);
		private Vector3 mOne = new Vector3(100,50,50);
		private Vector3 mTwo = new Vector3(0,0,0);
		private Vector3 mThree = new Vector3(0,50,0);

		@Override
		public void create () {

			ModelBuilder modelBulBuilder = new ModelBuilder();

			float width = 10.0f;
			float height = 10.0f;
			float depth = 10.0f;

			Material material = new Material();
			ColorAttribute colorAttri = ColorAttribute.createDiffuse(0.0f, 1.0f, 0.0f, 1.0f);
			material.set(colorAttri);

			Model model = modelBulBuilder.createBox(width, height, depth, material, 1);

			modelInstance = new ModelInstance(model);

			super.create();
		}

		@Override
		protected void render (ModelBatch batch, Array<ModelInstance> instances) {

			batch.begin(mCamera);
			translate();
			batch.render(modelInstance);

			batch.end();

		}

		private void translate () {
			Matrix4 matrix4 = modelInstance.transform;
			
			Vector3 direct = mOne.sub(mTwo);
			
			Vector3 result = direct.crs(mThree);

			matrix4.idt();
			
//			matrix4.translate(mOne);
			matrix4.rotate(result, mAngle);
			
			Log.i("data","result:\t"+result.toString());
//			matrix4.translate(-mOne.x,-mOne.y, -mOne.z);
			mAngle += 0.8f;
		}

		@Override
		protected void update (float delaTime) {

		}

	}

}
