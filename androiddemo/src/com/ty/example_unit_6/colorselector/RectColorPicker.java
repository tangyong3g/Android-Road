package com.ty.example_unit_6.colorselector;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * 
 * @author tang
 * 
 */
public class RectColorPicker extends View {

	private Paint mRectPaint;// 渐变方块画笔
	private Shader rectShader;// 渐变方块渐变图像

	private float rectLeft;// 渐变方块左x坐标 以canvas为基准
	private float rectTop;// 渐变方块右x坐标
	private float rectRight;// 渐变方块上y坐标
	private float rectBottom;// 渐变方块下y坐标

	private int[] mRectColors;// 渐变方块颜色

	private int mWindowWidth;
	private int mWindowHeight;

	private Paint mCirPaint;// 点中效果的画笔
	private Paint mCirDemoPaint;// 点中效果的画笔

	private float mX;
	private float mY;

	private ArrayList<OnColorChangedListener> mListeners;

	public void addListener(OnColorChangedListener lis) {
		if (mListeners == null) {
			mListeners = new ArrayList<RectColorPicker.OnColorChangedListener>();
		}
		mListeners.add(lis);
	}

	public RectColorPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public RectColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RectColorPicker(Context context) {
		super(context);
		init();
	}

	private void init() {

		// 渐变参数
		mRectColors = new int[] { 0xFFFF0000, 0xFFFFFF00, 0xFF00FF00,
				0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000 };

		// 初始化
		mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRectPaint.setStrokeWidth(1);

		// 初始化点中效果的画笔
		mCirPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCirPaint.setStrokeWidth(1);
		mCirPaint.setStyle(Style.STROKE); // 设置画笔为空心
		mCirPaint.setColor(Color.BLACK);

		mCirDemoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		Log.i("data", "宽:\t" + getWidth());

		rectLeft = -200;
		rectTop = -40;
		rectRight = 200;
		rectBottom = 40;
	}

	public RectColorPicker(Context context, int width, int height) {
		super(context);
		init();
	}
	
	

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		mWindowWidth = getWidth();
		mWindowHeight = getHeight();
		
//		rectLeft =  - 100;
//		rectRight =  100;
		
		
		// 这个坐标系的标准是parent的区
		canvas.translate(mWindowWidth / 2, mWindowHeight / 2);

		// 表示线性渐变
		rectShader = new LinearGradient(rectLeft, 0, rectRight, 0, mRectColors,
				null, Shader.TileMode.MIRROR);
		// 设置 方形的shader
		mRectPaint.setShader(rectShader);
		// 绘制方形
		canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, mRectPaint);

		canvas.drawCircle(mX, mY, 5, mCirPaint);

		canvas.drawCircle(0, -100, 30, mCirDemoPaint);

	}

	/**
	 * 转换一下坐标到以view的中心为原点,坐标轴x，y方向仍为向左，向下
	 * 
	 * @return
	 */
	private PointF transformXY(MotionEvent e) {
		// 转换一下坐标到以view的中心为原点,坐标轴x，y方向仍为向左，向下
		PointF p = new PointF();
		p.x = e.getX() - mWindowWidth / 2;
		p.y = e.getY() - mWindowHeight / 2;
		return p;
	}

	/**
	 * 是否触摸在色环内
	 * 
	 * @param e
	 * @return
	 */
	private boolean isTouchRing(PointF p) {

		float x = p.x;
		float y = p.y;

		Log.i("position", "p.x" + p.x + "\t" + "p.y" + p.y);

		boolean r_1 = (x > rectLeft && x < rectRight);
		boolean r_2 = (y < rectBottom && y > rectTop);

		Log.i("position", "p.x" + p.x + "\t" + "p.y" + p.y + r_1 + r_2 + "");

		return r_1 && r_2;
	}

	public void updateSelectPoint(PointF p) {

		mX = p.x;
		mY = p.y;
	}

	/**
	 * 计算顔色
	 * 
	 * @param src
	 * @param dst
	 * @param p
	 * @return
	 */
	private int aveRGB(int src, int dst, float pec) {
		return src + Math.round(pec * (dst - src));
	}

	/**
	 * 根据位置计算色
	 * 
	 * @param radian
	 * @return
	 */
	private int calculateColorByRadian(PointF point) {

		float d = point.x - rectLeft;
		float l = rectRight - rectLeft;
		float perDistrict = l / 6.0f; // 7种色彩分成6个区间
		// 计算当前在第几个区间
		int index = (int) (d / perDistrict);

		int color = Color.BLACK;
		int srcColor = mRectColors[index]; // 起始顔色
		int disIndex = index + 1;

		Log.i("dis", "区间:\t" + disIndex);
		disIndex = disIndex <= 6 ? disIndex : 0;
		int dstColor = mRectColors[disIndex]; // 结束顔色
		float pec = (float) (d % perDistrict / perDistrict);

		int red = aveRGB(Color.red(srcColor), Color.red(dstColor), pec);
		int green = aveRGB(Color.green(srcColor), Color.green(dstColor), pec);
		int blue = aveRGB(Color.blue(srcColor), Color.blue(dstColor), pec);
		color = Color.argb(255, red, green, blue);
		return color;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handle = false;
		// 转换一下坐标
		PointF p = transformXY(event);
		if (isTouchRing(p)) {
			handle = true;
			updateSelectPoint(p);
			int calculateColor = calculateColorByRadian(p);
			mCirDemoPaint.setColor(calculateColor);
			notifyColorChange(calculateColor);
			invalidate();
		} else {
			handle = super.onTouchEvent(event);
		}
		return handle;
	}

	/**
	 * 通知监听器
	 * 
	 * @param b
	 */
	private void notifyColorChange(int color) {
		if (mListeners != null) {

			for (OnColorChangedListener lis : mListeners) {
				lis.onColorChanged(true, color);
			}
		}
	}

	/**
	 * 监听接口
	 * 
	 * @author tangyong
	 * 
	 */
	public interface OnColorChangedListener {
		/**
		 * 当顔色改变时调用
		 * 
		 * @param isFromUser
		 *            是否用户手动设置
		 * @param color
		 *            ARGB
		 */
		void onColorChanged(boolean isFromUser, int color);
	}

}
