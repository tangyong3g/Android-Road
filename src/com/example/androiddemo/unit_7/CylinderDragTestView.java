package com.example.androiddemo.unit_7;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.sny.tangyong.androiddemo.R;
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
 * <li> 圆柱体是怎么画的 
 * <li>
 * 
 * 
 * <br>
 * 类描述: 测试在圆柱体上拖拽图标 <br>
 * 功能详细描述: 在屏幕左右两边空白处上下滑动可以旋转圆柱体
 * 
 * @author dengweiming
 * @date [2013-7-3]
 */
public class CylinderDragTestView extends GLViewGroup {

	final static float SCALE = 1.21f;

	// 使用GLGrid的方式，创建出来网格 -- GLGrid -- GLObject
	GLCylinder mMesh;
	// 用来绘制纹理的
	TextureGLObjectRender mRender = new TextureGLObjectRender();

	boolean mFillCylinderMesh = true;
	boolean mLookDown = false;

	// -- GLViewGroup -- GLView
	GLDragView mDragView;

	// GLDragView的监听者
	GLDragListener mDragListener;

	final int mIconCount = 5;

	// Ray
	Ray mTouchRay = new Ray();
	Ray mTmpRay = new Ray();

	Point mHitPoint = new Point();
	boolean mHit;
	float mHitAngle;
	float mHitY;
	float mTouchX;
	float mTouchY;
	float mLastTouchY;

	/** 记录动画一个时刻的状态 */
	Transformation3D mTransformation = new Transformation3D();
	Transformation3D mTransformationOnTouch = new Transformation3D();

	/** 封装了一个圆柱体的信息 */
	Cylinder mCylinder = new Cylinder();

	/** 上下底座的参数 */
	Point mUpperCenter = new Point();
	Point mLowerCenter = new Point();

	float mScrollAngle;

	public CylinderDragTestView(Context context) {
		super(context);

		Log.i("cycle", "构造方法");
		// 分割一个圆柱的网格
		mMesh = new GLCylinder(30, 1, mFillCylinderMesh);
		// 设置纹理坐标
		mMesh.setTexcoords(0, 0, 1, 1);

		// 设置纹理资源
		mRender.setTexture(getResources(), R.drawable.bg_one);

		// 创建 dragView
		mDragView = new GLDragView(context);
		mDragView.setVisibility(INVISIBLE);
		mDragListener = new DragListener();

		
		// TODO 这里不知道是什么意思呢？
		addView(mDragView);

		// 添加几个图标
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
					// 对照dispatchDraw方法如何绘制图标的
					m = m.translate(mMesh.getCenterX(), mMesh.getCenterY(), mMesh.getCenterZ());
					m = m.rotateAxisAngle(icon.angle, 0, 1, 0);
					m = m.translate(-mMesh.getCenterX(), -mMesh.getCenterY(), -mMesh.getCenterZ());
					m = m.translate(mMesh.getCenterX() + icon.getWidth() * -0.5f, icon.getTop());
					t.set(m.getValues(), 0);
					GeometryPools.restoreStack();

					
					/* start drag 做的事情有:
					 * 
					 * 1: 把 mDragistener 做为dragView的source,把v 做为DragView的view
					 * 2: 把dragListener 添加到dragView的listeners 集合中去
					 * 3: 回调drageStar函数 
					 * */
					mDragView.startDrag(mDragListener, v, t, null);

					return true;
				}
			});
		}

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.i("cycle", "onSizeChanged");

		mDragView.layout(0, 0, w, h);

		// 设置圆柱体边界　
		mMesh.setBounds(0, 0, w, h);
		//设置经度　
		mMesh.setLongitude(GLCylinder.ANGLE_TO_LEFT, GLCylinder.ANGLE_TO_LEFT + GLCylinder.FULL_CIRCLE);

		//		mMesh.setLongitude(0,360);

		// 设置上下底的坐标
		mLowerCenter.set(mMesh.getCenterX(), mMesh.getBottom(), mMesh.getCenterZ());
		mUpperCenter.set(mMesh.getCenterX(), mMesh.getTop(), mMesh.getCenterZ());

		//圆柱体
		mCylinder.set(mLowerCenter, mUpperCenter, mMesh.getRadius());

		//屏幕顶上面的中心世界坐标
		mHitPoint.set(w * 0.5f, 0, 0);

		// 随机指定图标在圆柱上的位置

		//得到周长
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

				icon.angle = mMesh.xToAngle(l + iconSize / 2);

			}
		}

	}

	public boolean checkTouch() {

		GeometryPools.saveStack();
		mHit = mCylinder.intersect(mTouchRay);
		if (mHit) {
			Point hitPoint = mTouchRay.getHitPoint();
			hitPoint.setTo(mHitPoint);
			Vector m = mHitPoint.sub(mLowerCenter);
			mHitAngle = (float) Math.toDegrees(Math.atan2(m.x, m.z));
			mHitY = -mHitPoint.y;
			Log.i("tyler.tang", "checkTouch:\t" + mHit + "\t" + mHitY);
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

		mTransformationOnTouch.set(mTransformation); // 记住此时的变换，以免绘制时用于调试的图形受动画影响

		getTouchRay(mTmpRay, true);
		mTmpRay.inverseRotateAndTranslate(mTransformation.getMatrix3D()).setTo(mTouchRay);
		mTouchRay.startCast();

		mHit = mCylinder.intersect(mTouchRay);
		if (mHit) {
			Point hitPoint = mTouchRay.getHitPoint();
			hitPoint.setTo(mHitPoint);
			Vector m = mHitPoint.sub(mLowerCenter);
			mHitAngle = (float) Math.toDegrees(Math.atan2(m.x, m.z));
			// if (mHitAngle < 0) {
			// mHitAngle += Math3D.FULL_DEGREES;
			// }
			float x = mMesh.angleTomToArcLen(mHitAngle);
			float y = -mHitPoint.y;
			ev.setLocation(x, y);
			invalidate(); // 更新绘制调试的图形
		}

		GeometryPools.restoreStack();

		boolean res = super.dispatchTouchEvent(ev);
		ev.setLocation(mTouchX, mTouchY);
		return res;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!mHit) {
			// event.setLocation(mTouchX, mTouchY);
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

		float[] pos = new float[3];
		canvas.getCameraLocalPosition(pos);
		Log.i("tyler.tang", "localposition 位置:\t" + pos[0] + ":\t" + pos[1] + ":\t" + pos[2]);
		canvas.getCameraWorldPosition(pos);
		Log.i("tyler.tang", "wordposition 位置:\t" + pos[0] + ":\t" + pos[1] + ":\t" + pos[2]);

		if (mLookDown) {
			canvas.setLookFromTop(mMesh.getCenterX(), 0, mMesh.getCenterZ(), false, true); // 从顶上俯视
		}
		canvas.save();

		mTransformation.clear().setRotateAxisAngle(-mScrollAngle, 0, 1, 0, mMesh.getCenterX(), mMesh.getCenterY(), mMesh.getCenterZ());

		canvas.concat(mTransformation.getMatrix(), 0);

		mRender.drawTranslucentObject(canvas, mMesh, 192, 128);

		drawIcons(canvas);
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

	// CHECKSTYLE IGNORE 3 LINES
	static class IconView extends GLView {
		float angle;
		float y;

		public IconView(Context context) {
			super(context);
		}

	}

	// CHECKSTYLE IGNORE 1 LINES
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
				v.offsetLeftAndRight((int) (newX - oldX)); // 因为存在回绕的可能，不能直接累加偏移量
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

		}

		@Override
		public long onDragHover(GLDragView view, float x, float y, Ray ray) {
			return 0;
		}

		@Override
		public boolean onDropFrom(GLDragView view, float x, float y, Ray ray, GLDragListener target, boolean isHandled) {
			return false;
		}

		@Override
		public boolean onDropTo(GLDragView view, float x, float y, Ray ray, GLDragListener source) {
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
			return 0;
		}

	}
}
