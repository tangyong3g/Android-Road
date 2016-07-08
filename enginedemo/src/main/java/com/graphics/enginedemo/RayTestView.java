package com.graphics.enginedemo;


import android.content.Context;
import android.view.MotionEvent;

import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.graphics.geometry.GLBox;
import com.graphics.engine.graphics.geometry.GLObjectRender;
import com.graphics.engine.graphics.geometry.GLSphere;
import com.graphics.engine.graphics.geometry.TextureGLObjectRender;
import com.graphics.engine.math3d.AABB;
import com.graphics.engine.math3d.GeometryPools;
import com.graphics.engine.math3d.Math3D;
import com.graphics.engine.math3d.Point;
import com.graphics.engine.math3d.Ray;
import com.graphics.engine.math3d.Sphere;
import com.graphics.engine.view.GLView;

/**
 * 
 * <br>类描述: 射线检测测试样例
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-7-12]
 */
public class RayTestView extends GLView {
	boolean mLookDown = false;
	final int mCount = 12;
	final float mTouchYOffset = -100; //给触摸点Ｙ轴上的偏移，避免被手指挡到

	float[] mTemVector = new float[4];

	MySphere[] mSpheres = new MySphere[mCount];
	MyBox[] mBoxes = new MyBox[mCount];
	TextureGLObjectRender mRender1 = new TextureGLObjectRender();
	TextureGLObjectRender mRender2 = new TextureGLObjectRender();

	Ray mRay = new Ray();

	public RayTestView(Context context) {
		super(context);
		for (int i = 0; i < mCount; ++i) {
			mSpheres[i] = new MySphere();
		}
		for (int i = 0; i < mCount; ++i) {
			mBoxes[i] = new MyBox();
		}
		mRender1.setTexture(getResources(), R.drawable.earthmap1k);
		mRender2.setTexture(getResources(), R.drawable.sunflower);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		float r = Math.min(w, h) / 8;
		for (int i = 0; i < mCount; ++i) {
			mSpheres[i].set((float) Math.random() * w, (float) Math.random() * -h, 
					(float) Math.random() * -Math.min(w, h), r);
		}
		for (int i = 0; i < mCount; ++i) {
			mBoxes[i].set((float) Math.random() * w, (float) Math.random() * -h, 
					(float) Math.random() * -Math.min(w, h), rand() * r * 2,
					rand() * r * 2, rand() * r * 2);
		}
	}
	
	float rand() {
		return (float) Math.random() * 0.5f + 0.5f;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		GeometryPools.saveStack();
		//		getTouchRay(mRay, true);
		getGLRootView().getCameraWorldPosition(mTemVector);
		mRay.set(GeometryPools.acquirePoint().set(mTemVector[0], mTemVector[1], mTemVector[2]),
				GeometryPools.acquirePoint().set(event.getX(), -(event.getY() + mTouchYOffset), 0));
		mRay.startCast();
		for (int i = 0; i < mCount; ++i) {
			mSpheres[i].intersect(mRay);
		}
		for (int i = 0; i < mCount; ++i) {
			mBoxes[i].intersect(mRay);
		}
		GeometryPools.restoreStack();
		invalidate();
		return true;
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		if (mLookDown) {
			canvas.setLookFromTop(getWidth() * 0.5f, getHeight() * 0.75f, 
					getWidth() * -0.5f, true, false);	//从顶上俯视
		}

		canvas.setDepthEnable(true);
		GeometryPools.saveStack();

		for (int i = 0; i < mCount; ++i) {
			canvas.save();
			mSpheres[i].draw(canvas, mRender1);
			canvas.restore();
		}
		for (int i = 0; i < mCount; ++i) {
			canvas.save();
			mBoxes[i].draw(canvas, mRender2);
			canvas.restore();
		}
		//canvas.drawViewFrustum();
		drawRay(canvas, mRay);
		Point hp = mRay.getHitPoint();
		canvas.translate(hp.x, hp.y, hp.z);
		canvas.setLookAsBillboardNoScaling();
		float s = 10;
		canvas.drawRect(-s, -s, s, s);

		GeometryPools.restoreStack();
		canvas.setDepthEnable(false);
	}

	void drawRay(GLCanvas canvas, Ray ray) {
		Point p0 = ray.getPoint(0);
		Point p1 = ray.getPoint(canvas.getZFar());
		float[] a = canvas.getTempFloatArray();
		int i = 0;
		a[i++] = p0.x;
		a[i++] = p0.y;
		a[i++] = p0.z;
		a[i++] = p1.x;
		a[i++] = p1.y;
		a[i++] = p1.z;
		canvas.drawVertex(GLCanvas.LINES, a, 0, 2, true);
	}

}

//CHECKSTYLE IGNORE 1 LINES
class MySphere {
	final static Point CENTER = new Point();
	GLSphere mMesh = new GLSphere(24, 16, true, 0);
	Sphere mShape = new Sphere();
	float mRotX;
	float mRotY;

	public MySphere() {
		mMesh.setTexcoords(0, 0, 1, 1);
		mMesh.setBounds(0, 0, 2, 2);
		mRotX = (float) Math.random() * Math3D.FULL_DEGREES;
		mRotY = (float) Math.random() * Math3D.FULL_DEGREES;
	}

	void set(float x, float y, float z, float r) {
		CENTER.set(x, y, z);
		mShape.set(CENTER, r);
	}

	void draw(GLCanvas canvas, GLObjectRender render) {
		Point p = mShape.center();
		float r = mShape.radius();
		canvas.translate(p.x, p.y, p.z);
		canvas.rotateAxisAngle(mRotX, 1, 0, 0);
		canvas.rotateAxisAngle(mRotY, 0, 1, 0);
		canvas.scale(r, r, r);
		canvas.translate(-mMesh.getCenterX(), -mMesh.getCenterY(), -mMesh.getCenterZ());
		render.draw(canvas, mMesh);
//		((TextureGLObjectRender) render).drawTranslucentObject(canvas, mMesh, 128, 128);
	}

	void intersect(Ray ray) {
		mShape.intersect(ray);
	}
}

//CHECKSTYLE IGNORE 1 LINES
class MyBox {
	GLBox mMesh = new GLBox(true);
	AABB mShape = new AABB();
	
	public MyBox() {
		mMesh.setTexcoords(0, 0, 1, 1);
		mMesh.setSize(1, 1, 1);
	}
	
	void set(float cx, float cy, float cz, float w, float h, float l) {
		mShape.setWithCenter(cx, cy, cz, w, h, l);
	}
	
	void draw(GLCanvas canvas, GLObjectRender render) {
		Point c = mShape.center();
		float w = mShape.width();
		float h = mShape.height();
		float l = mShape.length();
		canvas.translate(c.x - w * 0.5f, c.y + h * 0.5f, c.z + l * 0.5f);
		canvas.scale(w, h, l);
		render.draw(canvas, mMesh);
	}
	
	void intersect(Ray ray) {
		mShape.intersect(ray);
	}
}
