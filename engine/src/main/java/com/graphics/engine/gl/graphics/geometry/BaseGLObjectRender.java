package com.graphics.engine.gl.graphics.geometry;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLVBO;
import com.go.gl.graphics.IndexBufferBlock;
import com.go.gl.graphics.Renderable;
import com.go.gl.graphics.VertexBufferBlock;

/**
 * 
 * <br>类描述: 一个比较基础的物体渲染器
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-7-15]
 */
public abstract class BaseGLObjectRender implements GLObjectRender {
	protected static final int INDEX_INDEX_COUNT = 0;
	protected static final int INDEX_VERTEX_COUNT = 1;
	protected static final int INDEX_POSITION_COMPONENT = 2;
	protected static final int INDEX_TEXCOORD_COMPONENT = 3;
	protected static final int INDEX_NORMAL_COMPONENT = 4;
	protected static final int INDEX_VERTEX_DATA_START = 5;
	protected static final int INDEX_POSITION_ELEMENTS = 6;
	protected static final int INDEX_TEXCOORD_ELEMENTS = 7;
	protected static final int INDEX_NORMAL_ELEMENTS = 8;
	protected static final int INDEX_TOTAL_VERTEX_DATA = 9;
	protected static final int INDEX_DRAW_MODE = 10;
	protected static final int INDEX_MARK = 11;
	protected static final int INDEX_LAST = 12;
	
	protected static final int MARK = 0xdeadbeaf;	//用于检查数据传输错误的标记

	protected static final float[] TEMP_FLOAT_BUFFER = new float[INDEX_LAST];
	protected final static float[] TEMP_FLOAT_BUFFER_GL = new float[INDEX_LAST];

	protected final static float ONE_OVER_255 = 1 / 255.0f; // CHECKSTYLE IGNORE
	protected final static int FULL_ALPHA = 255;
	
	protected boolean mHasTexcoord;
	protected boolean mHasNormal;

	public BaseGLObjectRender() {
	}

	/**
	 * <br>功能简述: 绘制半透明的物体
	 * <br>功能详细描述: 由于半透明绘制的算法复杂，这里只实现最简单的两趟绘制，要求物体是简单的凸多面体（如果只是半透明的平面图形反而就有浪费了）。
	 * <br>注意: 先画不透明的物体，再画半透明的物体才有颜色混合效果。如果绘制多个半透明物体，要按照遮挡关系先绘制离摄像机最远的
	 * @param canvas
	 * @param object
	 * @param frontAlpha 正向面的透明度因子 [0..255]
	 * @param backAlpha  背向面的透明度因子 [0..255]
	 */
	//TODO:改名为drawTranslucentConvexObject
	public void drawTranslucentObject(GLCanvas canvas, GLObject object, int frontAlpha, int backAlpha) {
		final boolean cullFaceEnabled = canvas.isCullFaceEnabled();
		final boolean cullBack = canvas.isCullBackFace();
		final boolean depthMask = canvas.isDepthMask();
		final int oldAlpha = canvas.getAlpha();
		canvas.setDepthMask(false);
		canvas.setCullFaceEnabled(true);

		//TODO:两次draw操作，会传递两次顶点数据，可以优化掉第二次
		canvas.setCullFaceSide(false);
		canvas.multiplyAlpha(backAlpha);
		draw(canvas, object);

		canvas.setCullFaceSide(true);
		canvas.setAlpha(oldAlpha);
		canvas.multiplyAlpha(frontAlpha);
		draw(canvas, object);

		canvas.setAlpha(oldAlpha);
		canvas.setCullFaceSide(cullBack);
		canvas.setCullFaceEnabled(cullFaceEnabled);
		canvas.setDepthMask(depthMask);
	}

	@Override
	public void clear() {

	}

	@Override
	public void onClear() {

	}

	@Override
	public void yield() {

	}

	@Override
	public void onYield() {

	}
	
	protected boolean putData(GLObject object, Renderable renderable) {
		if (object.getVertexCount() <= 0) {
			return false;
		}
		
		float[] positionArray = object.getPositionArray();
		if (positionArray == null) {
			return false;
		}

		float[] texcoordArray = null;
		if (mHasTexcoord) {
			texcoordArray = object.getTexcoordArray();
			if (texcoordArray == null) {
				return false;
			}
		}
		
		float[] normalArray = null;
		if (mHasNormal) {
			normalArray = object.getNormalArray();
			if (normalArray == null) {
				return false;
			}
		}
		
		short[] indexArray = object.getIndexArray();

		final float[] info = TEMP_FLOAT_BUFFER;

		info[INDEX_INDEX_COUNT] = indexArray == null ? 0 : object.getIndexCount();
		info[INDEX_VERTEX_COUNT] = object.getVertexCount();
		info[INDEX_POSITION_COMPONENT] = object.getPositionComponent();
		info[INDEX_TEXCOORD_COMPONENT] = object.getTexcoordComponent();
		info[INDEX_NORMAL_COMPONENT] = object.getNormalComponent();
		info[INDEX_POSITION_ELEMENTS] = object.getPositionElements();
		info[INDEX_TEXCOORD_ELEMENTS] = texcoordArray == null ? 0 : object.getTexcoordElements();
		info[INDEX_NORMAL_ELEMENTS] = normalArray == null ? 0 : object.getNormalElements();
		info[INDEX_TOTAL_VERTEX_DATA] = 0;
		info[INDEX_DRAW_MODE] = object.getDrawMode();
		info[INDEX_MARK] = MARK;

		for (int i = INDEX_VERTEX_DATA_START + 1; i < INDEX_TOTAL_VERTEX_DATA; ++i) {
			info[INDEX_TOTAL_VERTEX_DATA] += info[i];
		}
		VertexBufferBlock.pushVertexData(renderable);
		VertexBufferBlock.pushVertexData(info, 0, INDEX_LAST);

		if (indexArray != null) {
			IndexBufferBlock.pushVertexData(indexArray, 0, (int) info[INDEX_INDEX_COUNT]);
		}

		VertexBufferBlock.pushVertexData(positionArray, 0, (int) info[INDEX_POSITION_ELEMENTS]);
		if (texcoordArray != null) {
			VertexBufferBlock.pushVertexData(texcoordArray, 0, (int) info[INDEX_TEXCOORD_ELEMENTS]);
		}
		if (normalArray != null) {
			VertexBufferBlock.pushVertexData(normalArray, 0, (int) info[INDEX_NORMAL_ELEMENTS]);
		}
		
		return true;
	}
	
	protected boolean putDataWithVBO(GLObject object, Renderable renderable, GLCanvas canvas) {
		if (object.getVertexCount() <= 0) {
			return false;
		}
		
		GLVBO positionArray = object.getPositionVBO();
		if (positionArray == null) {
			return false;
		}
		
		GLVBO texcoordArray = null;
		if (mHasTexcoord) {
			texcoordArray = object.getTexcoordVBO();
			if (texcoordArray == null) {
				return false;
			}
		}
		
		GLVBO normalArray = null;
		if (mHasNormal) {
			normalArray = object.getNormalVBO();
			if (normalArray == null) {
				return false;
			}
		}

		GLVBO indexArray = object.getIndexVBO();
		
		final float[] info = TEMP_FLOAT_BUFFER;
		
		info[INDEX_INDEX_COUNT] = indexArray == null ? 0 : object.getIndexCount();
		info[INDEX_VERTEX_COUNT] = object.getVertexCount();
		info[INDEX_POSITION_COMPONENT] = object.getPositionComponent();
		info[INDEX_TEXCOORD_COMPONENT] = object.getTexcoordComponent();
		info[INDEX_NORMAL_COMPONENT] = object.getNormalComponent();
		info[INDEX_POSITION_ELEMENTS] = object.getPositionElements();
		info[INDEX_TEXCOORD_ELEMENTS] = texcoordArray == null ? 0 : object.getTexcoordElements();
		info[INDEX_NORMAL_ELEMENTS] = normalArray == null ? 0 : object.getNormalElements();
		info[INDEX_TOTAL_VERTEX_DATA] = 0;
		info[INDEX_DRAW_MODE] = object.getDrawMode();
		info[INDEX_MARK] = MARK;
		
		for (int i = INDEX_POSITION_ELEMENTS; i < INDEX_TOTAL_VERTEX_DATA; ++i) {
			info[INDEX_TOTAL_VERTEX_DATA] += info[i];
		}
		
		// bind VBOs
		if (info[INDEX_INDEX_COUNT] > 0) {
			indexArray.bindOnUIThread(canvas);
		}
		if (info[INDEX_NORMAL_ELEMENTS] > 0) {
			normalArray.bindOnUIThread(canvas);
		}
		if (info[INDEX_TEXCOORD_ELEMENTS] > 0) {
			texcoordArray.bindOnUIThread(canvas);
		}
		positionArray.bindOnUIThread(canvas);
		
		
		VertexBufferBlock.pushVertexData(renderable);
		VertexBufferBlock.pushVertexData(info, 0, INDEX_LAST);
		
		return true;
	}

}