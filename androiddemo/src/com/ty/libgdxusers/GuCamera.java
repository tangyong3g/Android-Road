package com.ty.libgdxusers;

import com.badlogic.gdx.math.Vector3;

/**
 * 定义相机行为接口
 * 
 * @author tangyong
 * 
 */
public interface GuCamera {

	public void push();

	public void pop();

	public void handleKeys();

	public void spin(float delta, float dir);

	public void spin(float delta, Vector3 dir);

}
