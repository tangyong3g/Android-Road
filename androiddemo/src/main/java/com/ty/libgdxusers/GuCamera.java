package com.ty.libgdxusers;

import com.badlogic.gdx.math.Vector3;

/**
 * 定义相机行为接口
 * 
 * @author tangyong
 * 
 */
public interface GuCamera {

	void push();

	void pop();

	void handleKeys();

	void spin(float delta, float dir);

	void spin(float delta, Vector3 dir);

}
