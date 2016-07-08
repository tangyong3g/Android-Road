package com.graphics.enginedemo;

import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.graphics.geometry.GLGrid;
import com.graphics.engine.graphics.geometry.TextureGLObjectRender;
import com.graphics.engine.view.GLView;

import android.content.Context;


/**
 * 贝塞尔曲面的测试程序
 */
public class BezierPatchTestView extends GLView {

	GLGrid mMesh;
	BezierMesh mBezierMesh;
	TextureGLObjectRender mRender = new TextureGLObjectRender();
	float mRatio = 1;

	public BezierPatchTestView(Context context) {
		super(context);
		setBackgroundColor(0xff000000);

		mBezierMesh = new BezierMesh(32, 32, true);
		mMesh = mBezierMesh;
		mRatio = 0.75f;

		mRender.setTexture(getResources(), R.mipmap.ic_launcher);
		
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		int x = Math.min(w, h) * 3 / 4;
		int y = (int) (x / mRatio);
//		mMesh.setBounds((w - x) / 2, (h - y) / 2, (w + x) / 2, (h + y) / 2);
		mMesh.setTexcoords(0, 0, 1, 1);
		
		//控制点，来自于NeHe教程对应章节
		float[] a = {
        		-0.75f,	-0.75f,	-0.50f, 
        		-0.25f,	-0.75f,	 0.00f, 
        		 0.25f,	-0.75f,	 0.00f, 
        		 0.75f,	-0.75f,	-0.50f, 
        		-0.75f,	-0.25f,	-0.75f, 
        		-0.25f,	-0.25f,	 0.50f, 
        		 0.25f,	-0.25f,	 0.50f, 
        		 0.75f,	-0.25f,	-0.75f, 
        		-0.75f,	 0.25f,	 0.00f, 
        		-0.25f,	 0.25f,	-0.50f, 
        		 0.25f,	 0.25f,	-0.50f, 
        		 0.75f,	 0.25f,	 0.00f, 
        		-0.75f,	 0.75f,	-0.50f, 
        		-0.25f,	 0.75f,	-1.00f, 
        		 0.25f,	 0.75f,	-1.00f, 
        		 0.75f,	 0.75f,	-0.50f, 
        };
		float[] cp = mBezierMesh.getControlPoints();
		for (int i = 0; i < cp.length; ++i) {
			cp[i] = a[i] * x / 2;
		}
		mBezierMesh.makeBezierPatch();
	}
	
	float mTime = 0;

	@Override
	protected void onDraw(GLCanvas canvas) {
		final boolean cullFaceEnabled = canvas.isCullFaceEnabled();
		final boolean depthEnabled = canvas.isDepthEnabled();
		canvas.setCullFaceEnabled(false);
		canvas.setDepthEnable(true);
		
		canvas.translate(getWidth() / 2, getHeight() / 2);
		canvas.rotateAxisAngle(mTime, 0, 1, 0);
		canvas.rotateAxisAngle(-90, 1, 0, 0);
		mRender.draw(canvas, mMesh);
		mBezierMesh.drawControlPoints(canvas, 0xffffffff);
		
		canvas.setCullFaceEnabled(cullFaceEnabled);
		canvas.setDepthEnable(depthEnabled);
		
		mTime += 0.5f;
		invalidate();
	}

}

/**
 * 贝塞尔曲面类
 */
class BezierMesh extends GLGrid {
	private final static int POS_COMP = 3; 
	private final static int FOUR = 4;
	private final static float[] TEMP_VECTOR = new float[FOUR];

	float[] mControlPoints;
	float[] mTmpControlPoints;
	
	GLGrid mControlPointMesh;
	
	
	public BezierMesh(int xDiv, int yDiv, boolean fill) {
		super(xDiv, yDiv, fill);
		mControlPoints = new float[FOUR * FOUR * POS_COMP];
		mTmpControlPoints = new float[FOUR * POS_COMP];
	}

	@Override
	protected void onBoundsChange(float left, float top, float right, float bottom) {
		final float dx = (right - left) / (FOUR - 1);
		final float dy = -(bottom - top) / (FOUR - 1);
		final float[] pos = mControlPoints;
		
		float posX = left, posY = -top;
		int loc = 0;
		
		for (int y = 0; y < FOUR; ++y) {
			posX = left;
			for (int x = 0; x < FOUR; ++x) {
				pos[loc++] = posX;
				pos[loc++] = posY;
				pos[loc++] = 0;
				posX += dx;
			}
			posY += dy;
		}
		makeBezierPatch();
	}
	
	private static void bernstein(float u, float[] ctrlPnts, int sOff, float[] dst, int dOff) {
		final float uu = u * u;
		final float n = 1 - u;
		final float nn = n * n;
		final float[] c = TEMP_VECTOR;
		c[0] = nn * n;		//(1-u)^3
		c[1] = 3 * nn * u;	//3u(1-u)^2
		c[2] = 3 * n * uu;	//3(1-u)u^2
		c[3] = uu * u;		//u^3

		//dst=sum(ctrlPnts[p] * c[p]), p=[0..4]
		for (int i = 0; i < POS_COMP; ++i) {
			float sum = 0;
			int j = sOff + i;
			for (int p = 0; p < FOUR; ++p) {
				sum += ctrlPnts[j] * c[p];
				j += POS_COMP;
			}
			dst[dOff + i] = sum;
		}
	}
	
	/**
	 * 更新曲面网格顶点位置
	 */
	public void makeBezierPatch() {
		final int divX = getDivX();
		final int divY = getDivY();
		final int stride = getPositionArrayStride();
		float u = 0, v = 0, du = 1.0f / divX, dv = 1.0f / divY;
		for (int x = 0; x <= divX; ++x, u += du) {
			//先在每行对控制点按u插值
			for (int i = 0; i < FOUR; ++i) {
				bernstein(u, mControlPoints, i * FOUR * POS_COMP, mTmpControlPoints, i * POS_COMP);
			}
			v = 0;
			int index = x * POS_COMP;
			for (int y = 0; y <= divY; ++y, v += dv) {
				//再将各行的插值点再按v插值
				bernstein(v, mTmpControlPoints, 0, mPositionArray, index);
				index += stride;
			}
		}
	}
	
	/**
	 * 获取控制点网格的数组，一共4行4列，每个顶点3个分量(x, y, z)。
	 * 注意更新该数组后需要调用 {@link #makeBezierPatch()} 来更新曲面网格顶点位置
	 */
	public float[] getControlPoints() {
		return mControlPoints;
	}
	
	/**
	 * 把控制点网格绘制出来
	 */
	public void drawControlPoints(GLCanvas canvas, int color) {
		if (mControlPointMesh == null) {
			mControlPointMesh = new GLGrid(FOUR - 1, FOUR - 1, false);
		}
		System.arraycopy(mControlPoints, 0, mControlPointMesh.getPositionArray(), 0, mControlPoints.length);
		mControlPointMesh.setLineColor(color);
		mControlPointMesh.drawInLineMode(canvas);
		
	}

}
