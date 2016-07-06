package com.sany.tangyong.engineoriginal;

import android.content.Context;
import android.view.MotionEvent;

import com.go.gl.animation.Transformation3D;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.geometry.GLCylinder;
import com.go.gl.graphics.geometry.TextureGLObjectRender;
import com.go.gl.math3d.Cylinder;
import com.go.gl.math3d.GeometryPools;
import com.go.gl.math3d.Math3D;
import com.go.gl.math3d.Matrix;
import com.go.gl.math3d.Point;
import com.go.gl.math3d.Ray;
import com.go.gl.math3d.Vector;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLDragListener;
import com.go.gl.widget.GLDragView;

/**
 * 
 * <br>类描述: 测试在圆柱体上拖拽图标
 * <br>功能详细描述: 在屏幕左右两边空白处上下滑动可以旋转圆柱体
 * 
 * @author  dengweiming
 * @date  [2013-7-3]
 */
public class CylinderDragTestView extends GLViewGroup {
	final static float SCALE = 1.21f;

	GLCylinder mMesh;
	TextureGLObjectRender mRender = new TextureGLObjectRender();
	boolean mFillCylinderMesh = true;
	boolean mLookDown = false;

	GLDragView mDragView;
	GLDragListener mDragListener;

	final int mIconCount = 5;

	Ray mTouchRay = new Ray();
	Ray mTmpRay = new Ray();
	Point mHitPoint = new Point();
	boolean mHit;
	float mHitAngle;
	float mHitY;
	float mTouchX;
	float mTouchY;
	float mLastTouchY;
	Transformation3D mTransformation = new Transformation3D();
	Transformation3D mTransformationOnTouch = new Transformation3D();
	Cylinder mCylinder = new Cylinder();
	Point mUpperCenter = new Point();
	Point mLowerCenter = new Point();
	float mScrollAngle;

	public CylinderDragTestView(Context context) {
		super(context);
		mMesh = new GLCylinder(30, 1, mFillCylinderMesh);
		mMesh.setTexcoords(0, 0, 1, 1);
		mRender.setTexture(getResources(), R.drawable.grunge_world_map);

		mDragView = new GLDragView(context);
		mDragView.setVisibility(INVISIBLE);
		mDragListener = new DragListener();
		addView(mDragView);

		for (int i = 0; i < mIconCount; ++i) {
			GLView view = new IconView(context);
			int color = Math3D.randomColor(128, 255, 128, 255);
			view.setBackgroundColor(color);
			addView(view, 0);
			view.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(GLView v) {
					v.setVisibility(INVISIBLE);
					
					IconView icon = (IconView) v;
					Transformation3D t = GLDragView.TMP_TRANSFORMATION;
					t.set(mTransformation);
					GeometryPools.saveStack();
					Matrix m = GeometryPools.acquireMatrix();
					m.set(t.getMatrix3D());
					//对照dispatchDraw方法如何绘制图标的
					m = m.translate(mMesh.getCenterX(), mMesh.getCenterY(), mMesh.getCenterZ());
					m = m.rotateAxisAngle(icon.angle, 0, 1, 0);
					m = m.translate(-mMesh.getCenterX(), -mMesh.getCenterY(), -mMesh.getCenterZ());
					m = m.translate(mMesh.getCenterX() + icon.getWidth() * -0.5f, icon.getTop());
					t.set(m.getValues(), 0);
					GeometryPools.restoreStack();
					
					mDragView.startDrag(mDragListener, v, t, null);

					//	Animation a = new ScaleAnimation(1, SCALE, 1, SCALE, 
					//	Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					//	a.setDuration(300);
					//	mDragView.startDragAnimation(a);

					return true;
				}
			});
		}

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mDragView.layout(0, 0, w, h);

		//设置圆柱体
		mMesh.setBounds(0, 0, w, h);
		mMesh.setLongitude(GLCylinder.ANGLE_TO_LEFT, GLCylinder.ANGLE_TO_LEFT + GLCylinder.FULL_CIRCLE);
		
		mLowerCenter.set(mMesh.getCenterX(), mMesh.getBottom(), mMesh.getCenterZ());
		mUpperCenter.set(mMesh.getCenterX(), mMesh.getTop(), mMesh.getCenterZ());
		mCylinder.set(mLowerCenter, mUpperCenter, mMesh.getRadius());

		mHitPoint.set(w * 0.5f, 0, 0);

		//随机指定图标在圆柱上的位置
		w = (int) mMesh.getPerimeter();
		int iconSize = Math.min(w, h) / 8;
		for (int i = 0; i < getChildCount(); ++i) {
			GLView view = getChildAt(i);
			if (view instanceof IconView) {
				IconView icon = (IconView) view;
				int l = (int) ((w - iconSize) * Math3D.random());
				int t = (int) ((h - iconSize) * Math3D.random());
				view.layout(l, t, l + iconSize, t + iconSize);
				icon.angle = mMesh.xToAngle(l + iconSize / 2);
			}
		}

	}

	//	@Override
	//	public boolean onInterceptTouchEvent(MotionEvent ev) {
	//		//拖动发生后将事件自动发送到拖动层，不用让被拖动的视图发送
	//		if (mDragView.isInDrag()) {
	//			mDragView.dispatchTouchEvent(ev);
	//			return false;
	//		}
	//		return super.onInterceptTouchEvent(ev);
	//	}

	public boolean checkTouch() {

		GeometryPools.saveStack();
		mHit = mCylinder.intersect(mTouchRay);
		if (mHit) {
			Point hitPoint = mTouchRay.getHitPoint();
			hitPoint.setTo(mHitPoint);
			Vector m = mHitPoint.sub(mLowerCenter);
			mHitAngle = (float) Math.toDegrees(Math.atan2(m.x, m.z));
			mHitY = -mHitPoint.y;
		}
		GeometryPools.restoreStack();
		return mHit;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mTouchX = ev.getX();
		mTouchY = ev.getY();

		if (mDragView.isInDrag()) {
			mDragView.dispatchTouchEvent(ev);
			return true;
		}

		GeometryPools.saveStack();

		mTransformationOnTouch.set(mTransformation);	//记住此时的变换，以免绘制时用于调试的图形受动画影响
		getTouchRay(mTmpRay, true);
		mTmpRay.inverseRotateAndTranslate(mTransformation.getMatrix3D()).setTo(mTouchRay);
		mTouchRay.startCast();

		mHit = mCylinder.intersect(mTouchRay);
		if (mHit) {
			Point hitPoint = mTouchRay.getHitPoint();
			hitPoint.setTo(mHitPoint);
			Vector m = mHitPoint.sub(mLowerCenter);
			mHitAngle = (float) Math.toDegrees(Math.atan2(m.x, m.z));
//			if (mHitAngle < 0) {
//				mHitAngle += Math3D.FULL_DEGREES;
//			}
			float x = mMesh.angleTomToArcLen(mHitAngle);
			float y = -mHitPoint.y;
			ev.setLocation(x, y);
			invalidate();	//更新绘制调试的图形
		}

		GeometryPools.restoreStack();

		boolean res = super.dispatchTouchEvent(ev);
		ev.setLocation(mTouchX, mTouchY);
		return res;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!mHit) {
//			event.setLocation(mTouchX, mTouchY);
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				mScrollAngle += (mTouchY - mLastTouchY) / getHeight() * 360;
				invalidate();
			}
			mLastTouchY = mTouchY;
		}
		return true;
	};

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		if (mLookDown) {
			canvas.setLookFromTop(mMesh.getCenterX(), 0, mMesh.getCenterZ(), false, true);	//从顶上俯视
		}
		canvas.save();

		mTransformation.clear().setRotateAxisAngle(-mScrollAngle, 0, 1, 0, 
				mMesh.getCenterX(), mMesh.getCenterY(), mMesh.getCenterZ());
		canvas.concat(mTransformation.getMatrix(), 0);

		mRender.drawTranslucentObject(canvas, mMesh, 192, 128);

		drawIcons(canvas);

		//绘制调试图形
//		canvas.restore();
//		canvas.save();
//		canvas.concat(mTransformationOnTouch.getMatrix(), 0);
//		drawRay(canvas, mTouchRay);	//从正面看，射线投影成为一个点（因为起点是摄像机的），试试将mLookDown设为true
//		canvas.translate(mHitPoint.x, mHitPoint.y, mHitPoint.z);
//		canvas.setLookAsBillboardNoScaling();
//		float s = 50;
//		canvas.drawRect(-s, -s, s, s);

		canvas.restore();

		if (mDragView.isInDrag()) {
			mDragView.draw(canvas);
		}
	}

	void drawIcons(GLCanvas canvas) {
		final boolean cullFace = canvas.isCullFaceEnabled();
		if (!mFillCylinderMesh) {
			canvas.setCullFaceEnabled(false);
		}

		final long drawingTime = getDrawingTime();
		for (int i = 0; i < getChildCount(); ++i) {
			GLView view = getChildAt(i);
			if (view.getVisibility() == VISIBLE && view instanceof IconView) {
				final int saveCount = canvas.save();
				IconView icon = (IconView) view;
				mMesh.rotateAxisAngle(canvas, icon.angle, 0, 1, 0);
				canvas.translate(mMesh.getCenterX(), 0);
				canvas.translate(icon.getWidth() * -0.5f, 0);
				canvas.translate(-icon.getLeft(), 0);
				drawChild(canvas, icon, drawingTime);
				canvas.restoreToCount(saveCount);
			}
		}

		canvas.setCullFaceEnabled(cullFace);
	}

	void drawRay(GLCanvas canvas, Ray ray) {
		GeometryPools.saveStack();
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
		GeometryPools.restoreStack();
	}

	//CHECKSTYLE IGNORE 3 LINES
	static class IconView extends GLView {
		float angle;
		float y;

		public IconView(Context context) {
			super(context);
		}

	}

	//CHECKSTYLE IGNORE 1 LINES
	class DragListener implements GLDragListener {
		float mLastHitAngle;
		float mLastHitX;
		float mLastHitY;

		@Override
		public void onDragStart(GLDragView view) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onDragEnd(GLDragView view) {
		}

		@Override
		public int onCheckTouch(GLDragView view, float x, float y, Ray ray) {
			mTouchRay.set(ray);
			boolean hit = checkTouch();
			return hit ? GLDragView.HIT : GLDragView.PENDING;
		}

		@Override
		public boolean onDragMove(GLDragView view, float x, float y, Ray ray) {
			GLView v = view.getDraggedView();
			if (v instanceof IconView) {
				IconView icon = (IconView) v;
				float oldX = mMesh.angleTomToArcLen(icon.angle);
				icon.angle = Math3D.reduceDegrees(icon.angle + mHitAngle - mLastHitAngle);
				float newX = mMesh.angleTomToArcLen(icon.angle);
				v.offsetLeftAndRight((int) (newX - oldX));	//因为存在回绕的可能，不能直接累加偏移量
				v.offsetTopAndBottom((int) (mHitY - mLastHitY));
				mLastHitAngle = mHitAngle;
				mLastHitY = mHitY;
			}
			return false;
		}

		@Override
		public void onDragEnter(GLDragView view) {
			mLastHitY = mHitY;
			mLastHitAngle = mHitAngle;
		}

		@Override
		public void onDragExit(GLDragView view) {
			// TODO Auto-generated method stub

		}

		@Override
		public long onDragHover(GLDragView view, float x, float y, Ray ray) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean onDropFrom(GLDragView view, float x, float y, Ray ray, GLDragListener target, boolean isHandled) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onDropTo(GLDragView view, float x, float y, Ray ray, GLDragListener source) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onDrawDraggedView(GLDragView view, GLCanvas canvas, GLView draggedView, Transformation3D t) {
			final int saveCount = canvas.save();
			canvas.concat(mTransformation.getMatrix(), 0);
			IconView icon = (IconView) draggedView;
			mMesh.rotateAxisAngle(canvas, icon.angle, 0, 1, 0);
			canvas.translate(mMesh.getCenterX(), 0);
			canvas.translate(icon.getWidth() * -0.5f, 0);
			canvas.translate(-icon.getLeft(), 0);
			drawChild(canvas, icon, getDrawingTime());
			canvas.restoreToCount(saveCount);

			return true;
		}

		@Override
		public int getVisibility() {
			// TODO Auto-generated method stub
			return 0;
		}

	}
}
