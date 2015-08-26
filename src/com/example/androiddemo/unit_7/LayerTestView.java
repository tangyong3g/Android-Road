package com.example.androiddemo.unit_7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.util.Log;
import android.view.MotionEvent;

import com.sny.tangyong.androiddemo.R;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.graphics.GLDrawable;
import com.go.gl.view.GLView;

/**
 * 
 * 从上面我们可以看到PorterDuff.Mode为枚举类,一共有16个枚举值:
1.PorterDuff.Mode.CLEAR  
  所绘制不会提交到画布上。
2.PorterDuff.Mode.SRC
   显示上层绘制图片
3.PorterDuff.Mode.DST
  显示下层绘制图片
4.PorterDuff.Mode.SRC_OVER
  正常绘制显示，上下层绘制叠盖。
5.PorterDuff.Mode.DST_OVER
  上下层都显示。下层居上显示。
6.PorterDuff.Mode.SRC_IN
   取两层绘制交集。显示上层。
7.PorterDuff.Mode.DST_IN
  取两层绘制交集。显示下层。
8.PorterDuff.Mode.SRC_OUT
 取上层绘制非交集部分。
9.PorterDuff.Mode.DST_OUT
 取下层绘制非交集部分。
10.PorterDuff.Mode.SRC_ATOP
 取下层非交集部分与上层交集部分
11.PorterDuff.Mode.DST_ATOP
 取上层非交集部分与下层交集部分
12.PorterDuff.Mode.XOR
  异或：去除两图层交集部分
13.PorterDuff.Mode.DARKEN
  取两图层全部区域，交集部分颜色加深
14.PorterDuff.Mode.LIGHTEN
  取两图层全部，点亮交集部分颜色
15.PorterDuff.Mode.MULTIPLY
  取两图层交集部分叠加后颜色
16.PorterDuff.Mode.SCREEN
  取两图层全部区域，交集部分变为透明色
 * 
 * 
 * 这里要学习的内容有:
 * 
 * <li>canvas.drawLine();
 * <li>canvas.clipRect();
 * <li>canvas.save canvas.restore();
 * <li>Mode.DST_OUT 是一种什么样的混合方式呢?   Da * (1 - Sa) 为执行的公式  可以理解为 当源色为0了，就全要 DST的色，如果源色有。那么值就为0 就给空开了。
 * <li>GLView的生命周期　onMeasure onSizeChange  onLayout 
 * 
 * 
 * <li> 相機位置  x = screenWidth / 2  y = - (screenHeight / 2)  z = 每個相機不一樣，讓屏幕像素和世界單位統一的位置。
 * 
 * 
 * 
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
	

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		Log.i("cycle", "onLayout" + changed + "l:\t" + left + "t:\t" + top + "r:\t" + right + "b:\t" + bottom);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		Log.i("cycle", "onMeasure width:\t" + widthMeasureSpec + ":\tH:\t" + heightMeasureSpec);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		Log.i("cycle", "onFinishInflate");
	}

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

		float[] pos = new float[3];
		canvas.getCameraLocalPosition(pos);
		Log.i("tyler.tang", "pos 位置:\t" + pos[0] + ":\t" + pos[1] + ":\t" + pos[2]);
		canvas.getCameraWorldPosition(pos);
		Log.i("tyler.tang", "world位置:\t" + pos[0] + ":\t" + pos[1] + ":\t" + pos[2]);

		mGrid.draw(canvas);

		//测试使用LAYER_LOCAL_FLAG时是否正确裁剪
		{
			float angle = 0;	//测试仅窗口裁剪
			//			float angle = 30;	//测试蒙板裁剪
			canvas.rotate(angle);
			//			canvas.save();
			canvas.clipRect(50, 50, 950, 950);
			canvas.setDrawColor(Color.YELLOW);
			canvas.drawRect(50 + 1, 50 + 1, 950 - 1, 950 - 1);
			//canvas.restore();
			canvas.rotate(-angle);
		}

		//向右，下移动300
		canvas.translate(300, 300);
		//转动30度
		//		canvas.rotate(30);

		// 130指的是混合alp  的值 256 为最大 
		int layerFlag = GLCanvas.LAYER_CLIP_FLAG | GLCanvas.LAYER_ALPHA_FLAG | 130;

		canvas.saveLayer(0, 0, 300, 300, layerFlag);
		canvas.setDrawColor(Color.RED);

		//填充这个区域 
		canvas.fillRect(0, 0, 300, 300);

		//重新设置Color
		canvas.setDrawColor(Color.GREEN);
		//再填充 这个区域 
		canvas.fillRect(250, 250, 350, 350);

		//测试遮罩效果
		{
			/**/
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
		/**/

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
			case MotionEvent.ACTION_MOVE :
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

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		Log.i("cycle", "onSizeChange");
	}

}
