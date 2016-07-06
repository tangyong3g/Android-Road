package com.sany.tangyong.engineoriginal;

import android.content.Context;
import android.view.MotionEvent;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.geometry.GLSphere;
import com.go.gl.graphics.geometry.TextureGLObjectRender;
import com.go.gl.math3d.GeometryPools;
import com.go.gl.math3d.Ray;
import com.go.gl.view.GLViewGroup;
import com.go.gl.view.VirtualTrackBall;

/**
 * 
 * 
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public class VirtualTrackBallTestView extends GLViewGroup {


	GLSphere mMesh;
	TextureGLObjectRender mRender;
	VirtualTrackBall mTrackBall = new VirtualTrackBall();

	public VirtualTrackBallTestView(Context context) {
		super(context);
		
		mRender = new TextureGLObjectRender();
		mRender.setTexture(getResources(), R.drawable.earthmap1k);
		mMesh = new GLSphere(48, 24, true, -90 + 180);
		mMesh.setTexcoords(0, 0, 1, 1);

	}

	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		float r = Math.min(w, h) * 0.45f;
		mMesh.setBounds(w / 2 - r, h / 2 - r, w / 2 + r, h / 2 + r);
		mTrackBall.setBoundingSphere(mMesh.getCenterX(), mMesh.getCenterY(), 
				mMesh.getCenterZ(), mMesh.getRadius() * 1.25f);
	}
	
	
	public boolean onTouchEvent(MotionEvent event) {
		GeometryPools.saveStack();
		Ray ray = GeometryPools.acquireRay();
		getTouchRay(ray, true);
		ray.startCast();
		mTrackBall.onTouch(ray, event.getAction());
		GeometryPools.restoreStack();
		invalidate();
		return true;
	}
	
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
//		mMesh.drawInLineMode(canvas);
		mTrackBall.applyRotation(canvas, true);
		mRender.draw(canvas, mMesh);
	}


}