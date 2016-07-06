package com.sany.tangyong.engineoriginal;

import android.content.Context;
import android.view.MotionEvent;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.geometry.ColorGLObjectRender;
import com.go.gl.graphics.geometry.GLPath;
import com.go.gl.graphics.geometry.TextureGLObjectRender;
import com.go.gl.view.GLViewGroup;

/**
 * 
 * 
 * 
 * @author  dengweiming
 * @date  [2013-7-4]
 */
public class PathTestView extends GLViewGroup {
	GLPath mPath;
	ColorGLObjectRender mRender1 = new ColorGLObjectRender();
	TextureGLObjectRender mRender2 = new TextureGLObjectRender();
	TextureGLObjectRender mRender3 = new TextureGLObjectRender();

	public PathTestView(Context context) {
		super(context);
		mPath = new GLPath();
		mPath.setStrokeWidth(50, 50);
		mPath.setStrokeJoin(GLPath.JOIN_ROUND);
		mPath.setTouchSlop(context);
		
		mRender1.setColor(0xffff0000);
		mRender2.setTexture(getResources(), R.drawable.glow_line_frame);
		mRender3.setTexture(getResources(), R.drawable.glow_line);
		
//		mPath.lineTo(602.2577f, -1016.07117f, -325.4254f);
//		mPath.lineTo(602.2577f, -1016.07117f, -761.9408f);
//		mPath.lineTo(165.74232f, -1016.07117f, -761.9408f);
//		mPath.lineTo(165.74225f, -1016.07117f, -325.4254f);
//		
//		mPath.close();
		
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		mPath.onTouchEvent(event);
		invalidate();
		return true;
	}
	
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
//		canvas.setLookFromTop(756/2, -1280/2, -756/2, true, false);
//		canvas.setCullFaceEnabled(false);
		mPath.update(canvas);
		mPath.setDrawMode(false);
		mRender2.draw(canvas, mPath);
//		mRender3.draw(canvas, mPath);
//		mPath.setDrawMode(true);
//		mRender1.draw(canvas, mPath);
//		canvas.setDrawColor(0xffffff00);
//		mPath.draw(canvas);
	}


}