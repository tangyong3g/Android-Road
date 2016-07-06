package com.graphics.engine.gl.graphics.geometry;


import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.graphics.engine.gl.graphics.GLCanvas;
import com.graphics.engine.gl.math3d.GeometryPools;
import com.graphics.engine.gl.math3d.Math3D;
import com.graphics.engine.gl.math3d.Matrix;
import com.graphics.engine.gl.math3d.Point;
import com.graphics.engine.gl.math3d.Vector;
import com.graphics.engine.gl.util.FloatArrayList;
import com.graphics.engine.gl.util.LinkedFloatBuffer;

/**
 * 
 * <br>类描述: 3D路径类
 * <br>功能详细描述:
 * 不是很完善
 * 
 * @author  dengweiming
 * @date  [2013-7-23]
 */
public class GLPath extends GLObject {
	private final static boolean DBG = false;
	private final static String TAG = "DWM";
	private final static float[] CAMERA_POSITIOIN = new float[3];
	
	public final static int JOIN_MITER = 0; 
	public final static int JOIN_ROUND = 1; 
	public final static int JOIN_BEVEL = 2; 
	private int mJoin;
	private final Vector mViewVector = new Vector();
	private boolean mUseFixedViewVector;
	
	private LinkedFloatBuffer mPoints;
	private FloatArrayList mVertices;
	private FloatArrayList mTexcoords;

	private float mStrokeWidthStart = 1;
	private float mStrokeWidthEnd = 1;
	private boolean mTouchEventHistoryEnabled;	// = true;
	private boolean mBezierEnabled;
	private boolean mLoop;
	private int mLastPointCountsOnUpdate;

	private boolean mTouchDown;
	private float mTouchSlop;
	private float mLastTouchX;
	private float mLastTouchY;
	private float mLastMidX;
	private float mLastMidY;
	private float mSqDistThreshold;
	
	private float mTexU0;
	private float mTexU1 = 1;
	private float mTexV0;
	private float mTexV1 = 1;
	private float mTexVmid = (mTexV0 + mTexV1) * 0.5f;
	private float mLengthLimit = 3000;

	int mVerticesOffset;
	int mTexcoordsOffset;

	public GLPath() {
		mPoints = new LinkedFloatBuffer(LinkedFloatBuffer.TYPE_SMALL);
		mVertices = new FloatArrayList();
		mTexcoords = new FloatArrayList();
		mTouchSlop = ViewConfiguration.getTouchSlop() * 1.5f;	//CHECKSTYLE IGNORE
		mMode = TRIANGLE_STRIP;
		reset();
	}

	public void setStrokeWidth(float start, float end) {
		mStrokeWidthStart = start;
		mStrokeWidthEnd = end;
		float strokeSize = Math.max(start, end);
		mSqDistThreshold = Math.max(strokeSize * strokeSize, mTouchSlop * mTouchSlop) * 0.25f;
	}
	
	public void setTouchSlop(Context context) {
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}
	
	public void setTouchSlop(float touchSlop) {
		mTouchSlop = touchSlop;
	}

	public void onTouchEvent(MotionEvent event) {
		final int action = event.getAction() & MotionEvent.ACTION_MASK;

		float eventX = event.getX();
		float eventY = event.getY();

		switch (action) {
			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
				mTouchDown = false;
				if (mBezierEnabled) {
					lineTo(eventX, eventY);
				}
				break;
			case MotionEvent.ACTION_MOVE :
				if (!mTouchDown) {
					mTouchDown = true;
					mLastTouchX = mLastMidX = eventX;
					mLastTouchY = mLastMidY = eventY;
					reset();
					lineTo(eventX, eventY);
					break;
				}

				if (mTouchEventHistoryEnabled) {
					// When the hardware tracks events faster than they are delivered,
					// the event will contain a history of those skipped points.
					int historySize = event.getHistorySize();
					for (int i = 0; i < historySize; i++) {
						float historicalX = event.getHistoricalX(i);
						float historicalY = event.getHistoricalY(i);
						if (isTooClosed(mLastTouchX, mLastTouchY, historicalX, historicalY)) {
							continue;
						}
						if (!mBezierEnabled) {
							lineTo(historicalX, historicalY);
						} else {
							mLastMidX = (mLastTouchX + historicalX) * 0.5f;
							mLastMidY = (mLastTouchY + historicalY) * 0.5f;
							quadTo(mLastTouchX, mLastTouchY, mLastMidX, mLastMidY);
						}
						mLastTouchX = historicalX;
						mLastTouchY = historicalY;
					}
				}
				// After replaying history, connect the line to the touch point.
				if (isTooClosed(mLastTouchX, mLastTouchY, eventX, eventY)) {
					break;
				}
				if (!mBezierEnabled) {
					lineTo(eventX, eventY);
				} else {
					mLastMidX = (mLastTouchX + eventX) * 0.5f;
					mLastMidY = (mLastTouchY + eventY) * 0.5f;
					quadTo(mLastTouchX, mLastTouchY, mLastMidX, mLastMidY);
				}
				mLastTouchX = eventX;
				mLastTouchY = eventY;
				break;

			default :
				break;
		}
	}
	
	public void setDrawMode(boolean points) {
		mMode = points ? POINTS : TRIANGLE_STRIP;
	}
	
	public void setStrokeJoin(int join) {
		if (join < JOIN_MITER || join > JOIN_BEVEL) {
			return;
		}
		mJoin = join;
	}
	
	public void setFixedViewVector(float x, float y, float z) {
		mUseFixedViewVector = x != 0 || y != 0 || z != 0;
		mViewVector.set(x, y, z).normalize();
	}

	public void update(GLCanvas canvas) {
		//TODO: 转到本地坐标系
		int count = mPoints.size() / mPositionComponent;
		if (mLastPointCountsOnUpdate == count) {
			return;
		}
		mLastPointCountsOnUpdate = count;
		if (count <= 2) {
			return;
		}

		LinkedFloatBuffer.Iterator pntIterator = mPoints.iterator();
		{
			LinkedFloatBuffer.Iterator pntIterator2 = mPoints.iterator2();
			int index1 = 2;
			int index2 = count - 1;
			if (mLoop) {
				index1 = count - 2;
				index2 = 2;
			}
			//在开始处修改哨兵为第 index1 个点
			pntIterator.position(mPositionComponent * index1);
			for (int i = 0; i < mPositionComponent; ++i) {
				pntIterator2.set(pntIterator.next());
				pntIterator2.next();
			}
			//在末尾处将第 index2 个点添加为哨兵
			pntIterator.position(mPositionComponent * index2);
			int end = count * mPositionComponent;
			for (int i = 0; i < mPositionComponent; ++i) {
				mPoints.pushBack(pntIterator.next());
			}
			
			pntIterator.position(0);
		}

		final boolean miter = mJoin == JOIN_MITER;
		final boolean round = mJoin == JOIN_ROUND;
		mVertexCount = (count - 1) * 2;
		mPositionElements = mPositionComponent * mVertexCount;
		mVertices.removeAll();
		mVertices.grow(mPositionElements * (round ? 40 : 10) | 0x3FF);
		float[] vertices = mVertices.array();
		mPositionArray = vertices;
		
		mTexcoordElements = mTexcoordComponent * mVertexCount;
		mTexcoords.removeAll();
		mTexcoords.grow(mTexcoordElements * (round ? 40 : 10) | 0x3FF);
		float[] texcoords = mTexcoords.array();
		mTexcoordArray = texcoords;

		int oldSaveCount = GeometryPools.saveStack();
		int saveCount = oldSaveCount;

		Point cam = GeometryPools.acquirePoint().set(0, 0, 0);
		if (!mUseFixedViewVector && canvas != null) {
			canvas.getCameraWorldPosition(CAMERA_POSITIOIN);
			cam = GeometryPools.acquirePoint();
			cam.fromArray(CAMERA_POSITIOIN, 0);
			Matrix matrix = GeometryPools.acquireMatrix();
			canvas.getMatrix(matrix.getValues(), 0);
			cam = cam.transform(matrix.invert());
		}
		if (DBG) {
			Log.d(TAG, "=================================count=" + count);
		}

		mVerticesOffset = 0;
		mTexcoordsOffset = 0;
		Point p0 = GeometryPools.acquirePoint();
		setPoint(p0, pntIterator);
		Point p1 = GeometryPools.acquirePoint();
		setPoint(p1, pntIterator);
		Point p2 = GeometryPools.acquirePoint();
		Vector d1 = p1.sub(p0).normalize();
		Vector d2 = GeometryPools.acquireVector();
		if (!mLoop) {
			d1 = d1.neg();
		}
		Vector lastY = GeometryPools.acquireVector().set(0, 0, 0);

		float r = mStrokeWidthStart * 0.5f;
		float dr = (mStrokeWidthEnd - mStrokeWidthStart) * 0.5f / (count - 2);	//TODO:根据总长度计算
		float u = mTexU0;
		float du = (mTexU1 - mTexU0) / (count - 2);
		float totalLength = 0;
		for (int i = 1; i < count; ++i) {
			if (i % 8 == 1) {
				if (i > 1) {
					GeometryPools.restoreStackToCount(saveCount);
				}
				saveCount = GeometryPools.saveStack();
			}
			
			setPoint(p2, pntIterator);
			if (DBG) {
				Log.d(TAG, "        ");
				Log.d(TAG, "i=" + i + " p0=" + p0 + " p1=" + p1 + " p2=" + p2);
			}
			float dist = p1.dist(p0);
			totalLength += dist;
//			if(mLengthLimit > 0 && totalLength > mLengthLimit) {
//				
//			}

			Vector z = mUseFixedViewVector ? mViewVector : cam.sub(p1).normalize();
			p2.sub(p1).normalize().setTo(d2);
			Vector n = d1.sub(d2);
			float sina = d1.cross(d2).length();
			if (DBG) {
				Log.d(TAG, "           d1=" + d1 + " d2=" + d2 + " z=" + z + " n=" + n);
			}
			Point a = p1, b = p1, c = p1;
			if (sina > Math3D.EPSILON) {
				Vector va = z.cross(d1).mul(r);
				Vector vb = z.cross(d2).mul(r);
				Vector vc = n.mul(-r / sina);
				a = va.dot(vc) < 0 ? p1.add(va) : p1.sub(va);
				b = vb.dot(vc) < 0 ? p1.add(vb) : p1.sub(vb);
				c = p1.add(vc);
				
				if (miter) {
					float cosa = d1.dot(d2);
					float tanhoa = sina / (1 + cosa);
					float k = r * tanhoa * 1;
					a = a.add(d1.mul(k));
					b = b.sub(d2.mul(k));
				}
				
			} else {
				Vector v = z.cross(d1).mul(r);
				a = p1.add(v);
				c = p1.sub(v);
			}
			
			if (DBG) {
				drawPoint(canvas, p1, 0xffff00ff);
				drawPoint(canvas, a, 0xff00ff00);
				drawPoint(canvas, b, 0xff00ff00);
				drawPoint(canvas, c, 0xff00ffff);
			}
			
			Vector y = a.sub(c);
			boolean inverse = y.perp(d1).dot(lastY.perp(d1)) < 0;
			boolean extra = sina > Math3D.EPSILON && !(i == count - 1 && mLoop);
			if (DBG) {
				Log.d(TAG, "      a=" + a + " b=" + b + " c=" + c
					+ " n=" + n + " r=" + r + " sina=" + sina + " inv=" + inverse);
			}
			if (!inverse) {
				addPoint(a, u, mTexV0, vertices, texcoords);
				addPoint(c, u, mTexV1, vertices, texcoords);
				
				if (extra) {
					addPoint(p1, u, mTexVmid, vertices, texcoords);
					
					addPoint(p1, u, mTexVmid, vertices, texcoords);
					addPoint(p1, u, mTexVmid, vertices, texcoords);
					addPoint(a, u, mTexV0, vertices, texcoords);
					
					addPoint(a, u, mTexV0, vertices, texcoords);
					if (round) {
						addRoundJoin(p1, a, b, u, mTexVmid, mTexV0, vertices, texcoords);
					}
					addPoint(p1, u, mTexVmid, vertices, texcoords);
					addPoint(b, u, mTexV0, vertices, texcoords);
					addPoint(c, u, mTexV1, vertices, texcoords);
					
					mVertexCount += 8;
					y = b.sub(c);
				}
			} else {
				y = y.neg();
				
				addPoint(c, u, mTexV0, vertices, texcoords);
				addPoint(a, u, mTexV1, vertices, texcoords);
				
				if (extra) {
					if (round) {
						addRoundJoin(p1, a, b, u, mTexVmid, mTexV1, vertices, texcoords);
					}
					addPoint(p1, u, mTexVmid, vertices, texcoords);
					addPoint(b, u, mTexV1, vertices, texcoords);
					addPoint(c, u, mTexV0, vertices, texcoords);
					
					addPoint(c, u, mTexV0, vertices, texcoords);
					
					addPoint(c, u, mTexV0, vertices, texcoords);
					addPoint(b, u, mTexV1, vertices, texcoords);
					
					mVertexCount += 6;
					y = c.sub(b);
				}
			}

			lastY.set(y);
			Point tmpPoint = p0;
			p0 = p1;
			p1 = p2;
			p2 = tmpPoint;
			Vector tmpVector = d1;
			d1 = d2;
			d2 = tmpVector;
			r += dr;
			u += du;
		}
		GeometryPools.restoreStackToCount(oldSaveCount);

		mPoints.popBack(mPositionComponent);

		mPositionElements = mPositionComponent * mVertexCount;
		mTexcoordElements = mTexcoordComponent * mVertexCount;
		
		if (DBG) {
			for (int i = 0; i < mVertexCount; ++i) {
				Log.d("DWM", "i=" + i + "\tx=" + vertices[i * 3]
						+ " y=" + vertices[i * 3 + 1] + " z=" + vertices[i * 3 + 2] 
						+ " u=" + texcoords[i * 2] + " v=" + texcoords[i * 2 + 1]);
			}
		}
	}
	
	
	void addPoint(Point p, float u, float v, float[] vertices, float[] texcoords) {
		mVerticesOffset = p.toArray(vertices, mVerticesOffset);
		texcoords[mTexcoordsOffset++] = u;
		texcoords[mTexcoordsOffset++] = v;
	}
	
	void addRoundJoin(Point o, Point a, Point b, float u, float vCenter, float vOuter, 
			float[] vertices, float[] texcoords) {
		int div = 11;	// odd > 1	//TODO:LOD自适应
		Vector v1 = a.sub(o);
		float len1 = v1.length();
		Vector v2 = b.sub(o);
		float len2 = v2.length();
		v1.normalize();
		v2.normalize();
		float t = 0;
		float dt = 1.0f / div;
		for (int j = 0; j + 2 < div; j += 2) {
			float len3 = len1 + (len2 - len1) * (t += dt);
			Vector v3 = v1.slerp(v2, t, false).mul(len3);
			float len4 = len1 + (len2 - len1) * (t += dt);
			Vector v4 = v1.slerp(v2, t, false).mul(len4);
			Point p3 = o.add(v3);
			Point p4 = o.add(v4);

//			if (DBG) {
//				drawPoint(canvas, p3, 0xffffffff);
//				drawPoint(canvas, p4, 0xffffffff);
//			}

			addPoint(o, u, vCenter, vertices, texcoords);
			addPoint(p3, u, vOuter, vertices, texcoords);
			addPoint(o, u, vCenter, vertices, texcoords);
			addPoint(p4, u, vOuter, vertices, texcoords);
			mVertexCount += 4;
		}
	}
	
	private void drawPoint(GLCanvas canvas, Point p, int color) {
		canvas.setDrawColor(color);
		canvas.save();
		canvas.translate(p.x, p.y, p.z);
		canvas.setLookAsBillboardNoScaling();
		canvas.fillRect(-5, -5, 5, 5);
		canvas.restore();
	}
	
	private void setPoint(Point p, LinkedFloatBuffer.Iterator iterator) {
		p.x = iterator.next();
		p.y = iterator.next();
		p.z = iterator.next();
	}

	public void draw(GLCanvas canvas) {
		final int vertexCount = mVertexCount;
		if (vertexCount <= 1) {
			return;
		}
		//TODO
//		canvas.drawVertex(GLCanvas.POINTS, mPoints.array(), 0, vertexCount, true);
	}

	public void reset() {
		mPoints.removeAll();
		//在开始处添加哨兵
		lineTo(0, 0);
		mLoop = false;
		mLastPointCountsOnUpdate = 0;
	}

	public void lineTo(float x, float y) {
//		Log.d(TAG, "lineTo " + x + " " + y);
		mPoints.pushBack(x);
		mPoints.pushBack(-y);
		mPoints.pushBack(0);
	}
	
	public void lineTo(float x, float y, float z) {
//		Log.d(TAG, "lineTo " + x + " " + y + " " + z);
		mPoints.pushBack(x);
		mPoints.pushBack(y);
		mPoints.pushBack(z);
	}
	
	/**
	 * <br>功能简述: 回绕到第一个点
	 * <br>功能详细描述: 路径至少已经有3个点，并且第一个点和最后一个点不重合，否则不作修改
	 * <br>注意:
	 */
	public void close() {
		int size = mPoints.size();
		if (size <= mPositionComponent * 2) {
			return;
		}
		LinkedFloatBuffer.Iterator iterator = mPoints.iterator();
		iterator.position(mPositionComponent);
		float x1 = iterator.next();
		float y1 = iterator.next();
		float z1 = iterator.next();
		iterator.position(size - mPositionComponent);
		float x2 = iterator.next();
		float y2 = iterator.next();
		float z2 = iterator.next();
		if (x1 != x2 || y1 != y2 || z1 != z2) {
			mPoints.pushBack(x1);
			mPoints.pushBack(y1);
			mPoints.pushBack(z1);
		}
		mLoop = true;
	}

	/**
	 * Add a quadratic bezier from the last point, approaching control point
	 * (x1,y1), and ending at (x2,y2). If no moveTo() call has been made for
	 * this contour, the first point is automatically set to (0,0).
	 *
	 * @param x1 The x-coordinate of the control point on a quadratic curve
	 * @param y1 The y-coordinate of the control point on a quadratic curve
	 * @param x2 The x-coordinate of the end point on a quadratic curve
	 * @param y2 The y-coordinate of the end point on a quadratic curve
	 */
	public void quadTo(float x1, float y1, float x2, float y2) {
		//TODO
	}

	private boolean isTooClosed(float x1, float y1, float x2, float y2) {
//		Log.d(TAG, "isTooClosed " + x1 + " " + y1 + " - " + x2 + " " + y2 + 
//				" -> " + ((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) < mSqDistThreshold;
	}

}
