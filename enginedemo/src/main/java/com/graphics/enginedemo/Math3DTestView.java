package com.graphics.enginedemo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.widget.TextView;

import com.graphics.engine.math3d.GeometryPools;
import com.graphics.engine.math3d.Matrix;
import com.graphics.engine.math3d.Quaternion;

/**
 * 
 * <br>类描述: Math3D库函数测试
 * <br>功能详细描述: 每点击一次调用一次 test()
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public class Math3DTestView extends TextView {
	//CHECKSTYLE IGNORE 3 LINES
	float[] m = new float[16];
	float[] n = new float[16];
	float[] r = new float[16];

	public Math3DTestView(Context context) {
		super(context);
		setTextSize(16);

		setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				test();
			}
		});

		((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	void testMatrixMul() {
		for (int i = 0; i < 16; ++i) {
			m[i] = (float) ((Math.random() * 2 - 1) * 100);
		}

		Matrix a = new Matrix(m, 0);
		
		for (int i = 0; i < 16; ++i) {
			n[i] = (float) ((Math.random() * 2 - 1) * 100);
		}
		
		Matrix b = new Matrix(n, 0);
		
		GeometryPools.saveStack();
		Matrix c = a.mul(b);
		
		android.opengl.Matrix.multiplyMM(r, 0, m, 0, n, 0);

		setText("testMatrixMul\n");
		String formatter = "%.4f \t\t %.4f \t\t %.4f \t\t %.4f\n";
		append("---------------m*n=\n");
		for (int i = 0; i < 4; ++i) {
			append(String.format(formatter, r[i], r[i + 4], r[i + 8], r[i + 12]));
		}
		append("---------------c=\n");
		append(c.toString());
		GeometryPools.restoreStack();
	}
	
	void testQuaternionToMatrix() {
		setText("testQuaternionToMatrix\n");
		Quaternion q = new Quaternion();
		GeometryPools.saveStack();
		q.set((float) Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random());
		Matrix matrix = q.toMatrix();
		Quaternion q2 = matrix.getRotationQuaternion();
		append("q=" + q + "\n");
		append("p=" + q2 + "\n");
		append("----------------\n");
		
		q.set(0, (float) Math.random(), (float) Math.random(), (float) Math.random());
		matrix = q.toMatrix();
		q2 = matrix.getRotationQuaternion();
		append("q=" + q + "\n");
		append("p=" + q2 + "\n");
		append("----------------\n");
		
		q.set((float) Math.random(), 0, (float) Math.random(), (float) Math.random());
		matrix = q.toMatrix();
		q2 = matrix.getRotationQuaternion();
		append("q=" + q + "\n");
		append("p=" + q2 + "\n");
		append("----------------\n");
		
		q.set((float) Math.random(), (float) Math.random(), 0, (float) Math.random());
		matrix = q.toMatrix();
		q2 = matrix.getRotationQuaternion();
		append("q=" + q + "\n");
		append("p=" + q2 + "\n");
		append("----------------\n");
		
		q.set(0, 0, 0, 1);
		matrix = q.toMatrix();
		q2 = matrix.getRotationQuaternion();
		append("q=" + q + "\n");
		append("p=" + q2 + "\n");
		append("----------------\n");
		
		
		GeometryPools.restoreStack();
	}
	void testInvertVector() {
		n[0] = (float) (Math.random() * 2 - 1);
		n[1] = (float) (Math.random() * 2 - 1);
		n[2] = (float) (Math.random() * 2 - 1);
		n[3] = Math.random() > 0.5 ? 1 : 0;
		
		GeometryPools.saveStack();
		Matrix m = new Matrix();
		m.translate((float) (Math.random() * 2 - 1), (float) (Math.random() * 2 - 1), (float) (Math.random() * 2 - 1))
		 .rotateAxisAngle((float) (Math.random() * 720 - 360), (float) (Math.random() * 2 - 1), (float) (Math.random() * 2 - 1), (float) (Math.random() * 2 - 1))
		 .scale((float) (Math.random() * 2 - 1), (float) (Math.random() * 2 - 1), (float) (Math.random() * 2 - 1))
		 .setTo(m);
		
		float[] r = m.inverseTRS(n, 0);
		setText("testInvertVector\n");
		String formatter = "%.6f \t\t %.6f \t\t %.6f \t\t %.6f\n";
		append("---------------m=\n" + m + "\n");
		append("---------------v=\n");
		append(String.format(formatter, n[0], n[1], n[2], n[3]));
		append("---------------inverseTRS\n");
		append(String.format(formatter, r[0], r[1], r[2], r[3]));
		
		Matrix m2 = m.invert();
		r = m2.transform(n, 0);
		append("---------------invert\n");
		append(String.format(formatter, r[0], r[1], r[2], r[3]));
		
		android.opengl.Matrix.invertM(this.m, 0, m.getValues(), 0);
		android.opengl.Matrix.multiplyMV(r, 0, this.m, 0, n, 0);
		append("---------------sdk invert\n");
		append(String.format(formatter, r[0], r[1], r[2], r[3]));
		GeometryPools.restoreStack();
		
	}
	
	void testEulerToQuaternoin() {
		GeometryPools.saveStack();
		float x = (float) (Math.random() * 720 - 360);
		float y = (float) (Math.random() * 720 - 360);
		float z = (float) (Math.random() * 720 - 360);
		Quaternion q = new Quaternion();
		q.fromEuler(x, y, z);
		setText("testEulerToQuaternoin\n");
		append("euler: " + x + " " + y + " " + z + "\n");
		append("q=" + q + "\n");
		append("m1=" + q.toMatrix() + "\n");
		Matrix matrix = new Matrix();
		matrix.setRotationEuler(x, y, z);
		append("m2=" + matrix + "\n");
		GeometryPools.restoreStack();
	}
	
	void test() {
		testEulerToQuaternoin();
	}
}