package com.ty.libgdxusers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

/**
 * 处理日志　
 * 
 * 
 * @author tangyong
 * 
 */
public class Log {

	public static void out(double d) {
		Gdx.app.log("gdxdemos", String.valueOf(d));
	}

	public static void out(String str, double d) {
		Gdx.app.log("gdxdemos", str + String.valueOf(d));
	}

	public static void out(float f) {
		Gdx.app.log("gdxdemos", String.valueOf(f));
	}

	public static void out(String str, float f) {
		Gdx.app.log("gdxdemos", str + String.valueOf(f));
	}

	public static void out(float[] fArr) {
		String str = "";
		for (int i = 0; i < fArr.length; i++) {
			if (i != 0) {
				str = str.concat(", ");
			}
			str = str.concat(String.valueOf(fArr[i]));
		}
		Gdx.app.log("gdxdemos", str);
	}

	public static void out(String str, float[] fArr) {
		String str2 = "";
		for (int i = 0; i < fArr.length; i++) {
			if (i != 0) {
				str2 = str2.concat(", ");
			}
			str2 = str2.concat(String.valueOf(fArr[i]));
		}
		Gdx.app.log("gdxdemos", str + str2);
	}

	public static void out(String str) {
		Gdx.app.log("gdxdemos", str);
	}

	public static void out(String str, int x, int y) {
		Gdx.app.log("gdxdemos", str + x + ", " + y);
	}

	public static void out(String str, float x, float y) {
		Gdx.app.log("gdxdemos", str + x + ", " + y);
	}

	public static void out(String str, Vector3 v) {
		Gdx.app.log("gdxdemos", str + "[" + v.x + ", " + v.y + ", " + v.z + "]");
	}

	// FIXME doesn't accept string right way
	static void splog(String s, float... vars) {
		String s2 = String.format(s, vars);
		Log.out(s2);
	}

	public static void hr() {
		Log.out("------------------------");
	}

}
