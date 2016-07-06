package com.sany.tangyong.engineoriginal;

import android.content.Context;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.geometry.GLGrid;
import com.go.gl.graphics.geometry.TextureGLObjectRender;
import com.go.gl.view.GLView;

/**
 * 
 */
public class SimpleClothTestView extends GLView {

	GLGrid mMesh;
	VerletMesh mVerletMesh;
	TextureGLObjectRender mRender = new TextureGLObjectRender();
	float mRatio = 1;

	public SimpleClothTestView(Context context) {
		super(context);
		setBackgroundColor(0xff000000);

		mVerletMesh = new VerletMesh(32, 32, false);
		mMesh = mVerletMesh;

		mRender.setTexture(getResources(), R.drawable.sunflower);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		int x = Math.min(w, h) * 3 / 4;
		int y = (int) (x / mRatio);
		mMesh.setBounds((w - x) / 2, (h - y) / 2, (w + x) / 2, (h + y) / 2);
		mMesh.setTexcoords(0, 0, 1, 1);
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		if (mVerletMesh != null) {
			mVerletMesh.update();
			invalidate();
		}

		canvas.setCullFaceEnabled(false);
		canvas.setDepthEnable(true);
		mRender.draw(canvas, mMesh);
	}

	float mLastX;
	float mLastY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (mVerletMesh != null) {
				mVerletMesh.setPosition(mVerletMesh.getDivX() / 2, mVerletMesh.getDivY() / 2, x, -y, 0);	//(float) Math.random() * 10);
				invalidate();
			}
		}

		mLastX = x;
		mLastY = y;
		return true;
	}

}

/**
 * 
 */
class VerletMesh extends GLGrid {
	int mIterateCount = 2;
	float mTimeStep = 0.5f;
	float mCellSizeX;
	float mRestLengthX2;
	float mCellSizeY;
	float mRestLengthY2;
	float mGravity = -0.75f;

	float[] mPositionArray2;

	public VerletMesh(int xDiv, int yDiv, boolean fill) {
		super(xDiv, yDiv, fill);
		mPositionArray2 = new float[mPositionArray.length];
	}

	@Override
	public void onBoundsChange(float left, float top, float right, float bottom) {
		super.onBoundsChange(left, top, right, bottom);
		mCellSizeX = (right - left) / getDivX();
		mRestLengthX2 = mCellSizeX * mCellSizeX;
		mCellSizeY = (bottom - top) / getDivY();
		mRestLengthY2 = mCellSizeY * mCellSizeY;

		clearVelocity();
	}

	public void clearVelocity() {
		System.arraycopy(mPositionArray, 0, mPositionArray2, 0, mPositionArray.length);
	}

	private void verlet() {
		final float[] pos1 = mPositionArray;
		final float[] pos2 = mPositionArray2;
		final int n = pos1.length;
		for (int i = 0; i < n; ++i) {
			pos2[i] = pos1[i] + (pos1[i] - pos2[i]) * 0.985f;
		}
		final float dy = mGravity * mTimeStep * mTimeStep;
		for (int i = 1; i < n; i += 3) {
			pos2[i] += dy;
		}

		//swap
		mPositionArray = pos2;
		mPositionArray2 = pos1;
	}

	private void satisfyConstraints() {
		final int divX = getDivX();
		final int divY = getDivY();
		final int stride = getPositionArrayStride();

		int index = 0;
		for (int i = 0; i <= divY; ++i) {
			for (int j = 0; j < divX; ++j) {
				satisfyConstraint(index, index + 3, mRestLengthX2);
				index += 3;
			}
			index += 3;
		}

		for (int j = 0; j <= divX; ++j) {
			index = j * 3;
			for (int i = 0; i < divY; ++i) {
				satisfyConstraint(index, index + stride, mRestLengthY2);
				index += stride;
			}
		}

		//对角方向也做限制，但是效果不像布料了
		//		index = 0;
		//		int index2 = 0;
		//		for(int i = 0; i < divY; ++i){
		//			index2 += 3;
		//			for(int j = 0; j < divX; ++j){
		//				satisfyConstraint(index, index + 3 + stride, mRestLengthX2 + mRestLengthY2);
		//				satisfyConstraint(index2, index2 - 3 + stride, mRestLengthX2 + mRestLengthY2);
		//				index += 3;
		//				index2 += 3;
		//			}
		//			index += 3;
		//		}
	}

	private void satisfyConstraint(int index1, int index2, float restLength2) {
		final float[] pos = mPositionArray;
		final float dx = pos[index1] - pos[index2];
		final float dy = pos[index1 + 1] - pos[index2 + 1];
		final float dz = pos[index1 + 2] - pos[index2 + 2];
		final float delta2 = dx * dx + dy * dy + dz * dz;
		//		final float delta = (float)Math.sqrt(delta2);
		//		final float halfDiff = (delta - mCellSize) / delta * 0.5f;
		final float halfDiff = 0.5f - restLength2 / (restLength2 + delta2);
		pos[index1] -= dx * halfDiff;
		pos[index1 + 1] -= dy * halfDiff;
		pos[index1 + 2] -= dz * halfDiff;
		pos[index2] += dx * halfDiff;
		pos[index2 + 1] += dy * halfDiff;
		pos[index2 + 2] += dz * halfDiff;
	}

	private void setAnchors() {
		// 固定左上角和右上角
		RectF rect = getBounds();
		setPosition(0, 0, rect.left, -rect.top, 0);
		setPosition(getDivX() / 2, 0, (rect.left + rect.right) / 2, -rect.top, 0);
		setPosition(getDivX(), 0, rect.right, -rect.top, 0);
	}

	public void update() {
		verlet();

		setAnchors();
		for (int i = 0; i < mIterateCount; ++i) {
			satisfyConstraints();
			setAnchors();
		}
	}
}
