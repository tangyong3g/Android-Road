package com.ty.example_unit_3.libgdx.ex;

import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * 
 * @author tangyong
 * 
 */
// CHECKSTYLE:OFF
public abstract class Base3D extends AbstractApplication implements
		GestureListener {

	// 资源管理
	public AssetManager assets;
	// 透视投影相机
	public PerspectiveCamera mCamera;
	// 相机控类
	public CameraInputController inputController;
	// 处理模型
	public ModelBatch modelBatch;
	// 参考对象
	public Model axesModel;
	public ModelInstance axesInstance;
	// 是否显示参考的世界坐标系　
	public boolean showAxes = false;
	// 要绘制的模型
	public Array<ModelInstance> instances = new Array<ModelInstance>();

	@Override
	public void create() {
		super.create();
		if (assets == null)
			assets = new AssetManager();

		modelBatch = new ModelBatch();

		mCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		
		mCamera.position.set(0f, 0f, 0f);
		mCamera.near = 2f;
		mCamera.far = 560f;
		mCamera.update();

		createAxes();

		// Gdx.input.setInputProcessor(inputController = new
		// CameraInputController(cam));

	}

	final float GRID_MIN = -1000f;
	final float GRID_MAX = 1000f;
	final float GRID_STEP = 50f;

	// 创建参考系
	private void createAxes() {
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		MeshPartBuilder builder = modelBuilder.part("grid", GL10.GL_LINES,
				Usage.Position | Usage.Color, new Material());
		builder.setColor(Color.LIGHT_GRAY);
		for (float t = GRID_MIN; t <= GRID_MAX; t += GRID_STEP) {
			builder.line(t, 0, GRID_MIN, t, 0, GRID_MAX);
			builder.line(GRID_MIN, 0, t, GRID_MAX, 0, t);
		}
		builder = modelBuilder.part("axes", GL10.GL_LINES, Usage.Position
				| Usage.Color, new Material());
		builder.setColor(Color.RED);
		builder.line(0, 0, 0, GRID_MAX * 10, 0, 0);
		builder.setColor(Color.GREEN);
		builder.line(0, 0, 0, 0, GRID_MAX * 10, 0);
		builder.setColor(Color.BLUE);
		builder.line(0, 0, 0, 0, 0, GRID_MAX * 10);
		axesModel = modelBuilder.end();
		axesInstance = new ModelInstance(axesModel);
	}

	protected abstract void render(final ModelBatch batch,
			final Array<ModelInstance> instances);

	protected abstract void update(float delaTime);

	protected boolean loading = true;

	/**
	 * 所有的资源都中载完成
	 */
	protected void onLoaded() {

	}

	// 在加载资源时调用
	protected void onLoadPercentChange(float currentPercent) {
		Log.i("cycle", "资源加载进度发生变化......" + assets.getProgress());
	}

	// 当前加载的比率
	private float mCurrentLoadPercent = 0.0f;
	private float mPreLoadPercent = 0.0f;

	@Override
	public void render() {

		mCurrentLoadPercent = assets.getProgress();

		if (mCurrentLoadPercent != mPreLoadPercent) {
			onLoadPercentChange(mCurrentLoadPercent);
		}

		mPreLoadPercent = mCurrentLoadPercent;
		if (loading && assets.update()) {
			loading = false;
			onLoaded();
		}
		// inputController.update();
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// modelBatch.begin(mCamera);
		if (showAxes)
			modelBatch.render(axesInstance);

		render(modelBatch, instances);
		// modelBatch.end();
		super.render();
	}

	@Override
	public void dispose() {
		super.dispose();

		if (modelBatch != null) {
			modelBatch.dispose();
		}
		if (assets != null) {
			assets.dispose();
		}
		if (axesModel != null) {
			assets.dispose();
		}
		assets = null;
		axesModel = null;
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void resume() {
		super.resume();
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
			Vector2 pointer1, Vector2 pointer2) {
		// TODO Auto-generated method stub
		return false;
	}

}
