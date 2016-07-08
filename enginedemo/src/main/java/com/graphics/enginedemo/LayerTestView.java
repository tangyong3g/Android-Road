package com.graphics.enginedemo;

import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.graphics.GLDrawable;
import com.graphics.engine.view.GLView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.view.MotionEvent;

/**
 * <br>类描述: 测试GLCanvas的Layer图层
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-11-18]
 */
public class LayerTestView extends GLView {
	
	GLDrawable mGrid;	//背景的网格线，用于眼睛定位
	GLDrawable mMask;	//遮罩图
	
	int mLastX;
	int mLastY;
	int mX;
	int mY;

	public LayerTestView(Context context) {
		super(context);
		mGrid = GLDrawable.getDrawable(getResources(), R.drawable.grid_white);
		mMask = GLDrawable.getDrawable(getResources(), R.drawable.particle);
		mMask.setBounds(50, 50, 250, 250);
	}
	
	//生成网格图片的方法
	@SuppressWarnings("unused")
	private Bitmap generateGridBitmap() {
		int width = 768;
		int height = 1280;
		int dx = 100;
		int dy = 100;
		int lineWidth = 2;
		int lineColor = 0x7fffffff;	//0x7f000000;
		int textSize = 20;
		int textColor = 0x7fffffff;	//0x7f000000;

		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStrokeWidth(lineWidth);
		paint.setColor(lineColor);
		for (int x = 0; x < width; x += dx) {
			canvas.drawLine(x, 0, x, height, paint);
		}
		for (int y = 0; y < height; y += dy) {
			canvas.drawLine(0, y, width, y, paint);
		}

		paint.setTextSize(textSize);
		paint.setColor(textColor);
		paint.setTextAlign(Align.LEFT);
		for (int x = 0; x < width; x += dx) {
			canvas.drawText("" + x, x, textSize, paint);
		}
		for (int y = dy; y < height; y += dy) {
			canvas.drawText("" + y, 0, y + textSize, paint);
		}

		//GLCanvas.saveBitmap(bitmap, "/sdcard/grid_white.png");
		return bitmap;
	}
	
	@Override
	protected void onDraw(GLCanvas canvas) {
		mGrid.draw(canvas);
		
		//测试使用LAYER_LOCAL_FLAG时是否正确裁剪
		{
			float angle = 0;	//测试仅窗口裁剪
//			float angle = 30;	//测试蒙板裁剪
			canvas.rotate(angle);
			canvas.clipRect(50, 50, 950, 950);
			canvas.setDrawColor(Color.YELLOW);
			canvas.drawRect(50 + 1, 50 + 1, 950 - 1, 950 - 1);
			canvas.rotate(-angle);
		}
		
		canvas.translate(300, 300);
		canvas.rotate(30);
		
		int layerFlag = 
				GLCanvas.LAYER_CLIP_FLAG 
				| GLCanvas.LAYER_ALPHA_FLAG 
				| 196;
		canvas.saveLayer(0, 0, 300, 300, layerFlag);
		canvas.setDrawColor(Color.RED);
		canvas.fillRect(0, 0, 300, 300);
		canvas.setDrawColor(Color.GREEN);
		canvas.fillRect(250, 250, 350, 350);
		
		//测试遮罩效果
		{
			Mode mode = canvas.getBlendMode();
			canvas.setBlendMode(Mode.DST_OUT);

			//绘制遮罩，DST_OUT的混合模式只使用遮罩的alpha的补值，即遮罩不透明的地方会将Layer挖空
			canvas.rotate(-30, 150, 150);
			canvas.translate(mX, mY);
			mMask.draw(canvas);

			canvas.setBlendMode(mode);
		}

		canvas.restore();
		//可以看到绿色变半透明了，但是没有和红色混合，因为绿色覆盖了红色，再整体淡化
		
		//测试第二个Layer，以及使用LAYER_LOCAL_FLAG
		{
			canvas.translate(300, 300);
			canvas.saveLayer(0, 0, 300, 300, GLCanvas.LAYER_LOCAL_FLAG);
			canvas.setDrawColor(Color.RED);
			canvas.fillRect(0, 0, 300, 300);
			canvas.setDrawColor(Color.BLUE);
			canvas.fillRect(250, 250, 350, 350);
			canvas.restore();
		}
		
		//测试使用LAYER_LOCAL_FLAG时是否正确裁剪
		{
			canvas.reset();
			mMask.draw(canvas);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				mLastX = x;
				mLastY = y;
				break;
			case MotionEvent.ACTION_MOVE:
				mX += x - mLastX;
				mY += y - mLastY;
				mLastX = x;
				mLastY = y;
				invalidate();
			default :
				break;
		}
		return true;
	}

}
