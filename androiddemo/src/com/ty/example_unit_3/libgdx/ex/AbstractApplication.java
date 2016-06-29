package com.ty.example_unit_3.libgdx.ex;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Logger;

/**
 * 
 * @author tangyong
 * 
 */
//CHECKSTYLE:OFF
public abstract class AbstractApplication extends InputAdapter implements
		ApplicationListener {

	private static final String TAG = "AbstractApplication";
	private FPSLogger mLogger = new FPSLogger();
	private SpriteBatch mBatch;
	private BitmapFont mFont;

	Logger loger = new com.badlogic.gdx.utils.Logger(TAG, Logger.INFO);

	@Override
	public void create() {
		loger.info("create");
		mBatch = new SpriteBatch();
		mFont = new BitmapFont();
	}

	@Override
	public void resize(int width, int height) {
		loger.info("resize");
	}

	@Override
	public void render() {
		mLogger.log();
		mBatch.begin();
		mFont.draw(mBatch, "fps: " + Gdx.graphics.getFramesPerSecond(), 20, 20);
		mBatch.end();
	}

	@Override
	public void pause() {
		loger.info("pause");
	}

	@Override
	public void resume() {
		loger.info("resume");
	}

	@Override
	public void dispose() {
		loger.info("dispose");
	}

}
