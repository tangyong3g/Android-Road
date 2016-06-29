package com.ty.example_unit_6.colorselector;

import java.util.Arrays;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 圆形区域显示的顔色选择器
 * 
 * @author laojiale
 * 
 */
public class CircleColorPicker extends View {
	
	@SuppressWarnings("unused")
	private final String mLogTag = "CircleColorPicker";

	/**
	 * 色环相关
	 */
	private Paint mRingPaint; // 渐变色环画笔
	// 渐变色环参数，七彩图外加黑白色[R, RG, G, GB, B, 黑, 白, BR, R]
	private final int[] mRingColors = new int[] { 0xFFFF0000, 0xFFFFFF00,
			0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFF000000, 0xFFFFFFFF,
			0xFFFF00FF, 0xFFFF0000 };
	private RectF mRingOutRectF; // 色环的显示区域
	private RectF mRingInRectF; // 色环的内切片区域
	private float mRingOutR; // 色环外圆半径
	private float mRingInR; // 色环内圆半径
	private Path mRingPath; // 色环内圆切片

	/**
	 * 顔色预览的中心圆相关
	 */
	private Paint mCirclePaint; // 中心圆画笔
	private RectF mCircleRectF; // 中心圆的区域
	private Path mCirclePath;

	/**
	 * 顔色拾取点
	 */
	private RectF mSelectPointRectF; // 显示区域
	private Paint mSelectPointPaint; // 画笔
	private float mSelectPointR = 4; // 选取点半径,2dp
	private float mSelectPointToCenterR; // 选取点到view中心的距离

	/**
	 * 画布抗锯齿
	 */
	private PaintFlagsDrawFilter mPaintFlagsDrawFilter;

	/**
	 * 其他绘图画笔
	 */
	private Paint mCommonPaint;
	private RectF mCommonRectF;

	/**
	 * 当前选择顔色，ARGB
	 */
	private int mSelectColor;

	/**
	 * 监听器
	 */
	private OnColorChangedListener mOnColorChangedListener;

	public CircleColorPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public CircleColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CircleColorPicker(Context context) {
		super(context);
		init();
	}

	/**
	 * 初始化
	 */
	@SuppressLint("NewApi")
	private void init() {
		// 绘图中使用了clip方法，硬件加速会失效
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		float density = getContext().getResources().getDisplayMetrics().density;
		mPaintFlagsDrawFilter = new PaintFlagsDrawFilter(0,
				Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
		mCommonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCommonPaint.setStyle(Paint.Style.STROKE);
		mCommonRectF = new RectF();
		// 初始化色环相关
		// float[] positons = new float[mRingColors.length];
		// final int length = positons.length;
		// for (int i = 0; i < length; i++) {
		// positons[i] = 1.0f * i / length;
		// }
		Shader s = new SweepGradient(0, 0, mRingColors, null);
		mRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRingPaint.setShader(s);
		mRingPaint.setStyle(Paint.Style.FILL);
		mRingOutRectF = new RectF();
		mRingInRectF = new RectF();
		mRingPath = new Path();

		// 初始化中心圆相关
		mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCirclePaint.setStyle(Paint.Style.FILL);
		mCircleRectF = new RectF();
		mCirclePath = new Path();

		// 初始化选取点相关
		mSelectPointRectF = new RectF();
		mSelectPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mSelectPointPaint.setStyle(Paint.Style.STROKE);
		mSelectPointPaint.setColor(Color.WHITE);
		mSelectPointPaint.setStrokeWidth(2.0f);
		mSelectPointR = mSelectPointR * density;

		mSelectColor = Color.BLACK;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// 移动到view的中心
		canvas.save(); // save 1
		canvas.translate(getWidth() / 2, getHeight() / 2);

		// 绘制色环
		canvas.save(); // save 2
		canvas.setDrawFilter(mPaintFlagsDrawFilter);
		canvas.clipPath(mRingPath, Region.Op.XOR);
		canvas.drawOval(mRingOutRectF, mRingPaint);
		canvas.restore(); // for save 2

		// 画表示透明的底纹
		if (Color.alpha(mSelectColor) < 255) {
			canvas.save(); // save 3
			canvas.setDrawFilter(mPaintFlagsDrawFilter);
			canvas.clipPath(mCirclePath, Region.Op.REPLACE);
			float w = mCircleRectF.right - mCircleRectF.left;
			float h = mCircleRectF.bottom - mCircleRectF.top;
			float r = 4.0f;
			int xc = (int) (w / r + 0.5f);
			int yc = (int) (h / r + 0.5f);
			mCommonPaint.setColor(Color.LTGRAY);
			mCommonPaint.setStyle(Paint.Style.FILL);
			mCommonRectF.left = mCircleRectF.left;
			mCommonRectF.top = mCircleRectF.top;
			mCommonRectF.right = mCommonRectF.left + r;
			mCommonRectF.bottom = mCommonRectF.top + r;
			for (int i = 0; i < xc; i++) {
				mCommonRectF.left = mCircleRectF.left + r * i;
				mCommonRectF.right = mCommonRectF.left + r;
				for (int j = 0; j < yc; j++) {
					mCommonRectF.top = mCircleRectF.top + r * j;
					mCommonRectF.bottom = mCommonRectF.top + r;
					if (((i + 1) % 2 == 0 && (j + 1) % 2 == 0)
							|| ((i + 1) % 2 != 0 && (j + 1) % 2 != 0)) {
						canvas.drawRect(mCommonRectF, mCommonPaint);
					}
				}
			}
			canvas.restore(); // for save 3
		}

		// 画中心圆
		canvas.drawOval(mCircleRectF, mCirclePaint);

		// 画顔色选取点
		canvas.drawOval(mSelectPointRectF, mSelectPointPaint);

		// 画外圈的描边
		mCommonPaint.setStyle(Paint.Style.STROKE);
		mCommonRectF.left = mRingOutRectF.left + 3.0f;
		mCommonRectF.top = mRingOutRectF.top + 3.0f;
		mCommonRectF.right = mRingOutRectF.right - 3.0f;
		mCommonRectF.bottom = mRingOutRectF.bottom - 3.0f;
		mCommonPaint.setStrokeWidth(6.0f);
		mCommonPaint.setColor(Color.LTGRAY);
		canvas.drawOval(mCommonRectF, mCommonPaint);
		mCommonPaint.setStrokeWidth(4.0f);
		mCommonPaint.setColor(Color.WHITE);
		canvas.drawOval(mCommonRectF, mCommonPaint);

		// 画内圈描边
		mCommonPaint.setStrokeWidth(5.0f);
		mCommonPaint.setColor(Color.WHITE);
		canvas.drawOval(mRingInRectF, mCommonPaint);
		mCommonPaint.setStrokeWidth(1.0f);
		mCommonPaint.setColor(Color.LTGRAY);
		canvas.drawOval(mRingInRectF, mCommonPaint);

		// 画中心圆描边
		canvas.drawOval(mCircleRectF, mCommonPaint);

		canvas.restore(); // for save 1
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mRingOutR = (right - left - getPaddingLeft() - getPaddingRight()) / 2;
		// 绘图时，会将画布移动到view的中点
		mRingOutRectF.left = -mRingOutR;
		mRingOutRectF.top = -mRingOutR;
		mRingOutRectF.right = mRingOutR;
		mRingOutRectF.bottom = mRingOutR;

		// 默认情况下内半径是外半径的0.6倍
		mRingInR = mRingOutR * 0.6f;
		mRingInRectF.left = -mRingInR;
		mRingInRectF.top = -mRingInR;
		mRingInRectF.right = mRingInR;
		mRingInRectF.bottom = mRingInR;
		mRingPath.reset();
		mRingPath.addOval(mRingInRectF, Direction.CCW);

		// 默认情况下内圆半径是内环的半径的0.5
		float cR = mRingInR * 0.5f;
		mCircleRectF.left = -cR;
		mCircleRectF.top = -cR;
		mCircleRectF.right = cR;
		mCircleRectF.bottom = cR;
		mCirclePath.reset();
		mCirclePath.addOval(mCircleRectF, Direction.CCW);

		// 选取点离中心的固定距离
		mSelectPointToCenterR = mRingInR + (mRingOutR - mRingInR) / 2;

		moveSelectPoint();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handle = false;
		// 转换一下坐标
		PointF p = transformXY(event);
		if (isTouchRing(p)) {
			handle = true;
			int calculateColor = calculateColor(p);
			// 提取原顔色的透明度值
			calculateColor = (mSelectColor & 0xFF000000)
					| (calculateColor & 0x00FFFFFF);
			if (calculateColor != mSelectColor) {
				mSelectColor = calculateColor;
				mCirclePaint.setColor(mSelectColor);
				updateSelectPoint(p);
				invalidate();
				notifyColorChange(true);
			}
		} else {
			handle = super.onTouchEvent(event);
		}
		return handle;
	}

	/**
	 * 根据触摸点计算出选定的顔色
	 * 
	 * @param event
	 * @return
	 */
	private int calculateColor(PointF p) {
		int color = Color.BLACK;
		// 计算出角度的弧值，3点钟方向为0角度，顺时针方向为正
		double radian = Math.atan2(p.y, p.x); // 当前触点的角度
		color = calculateColorByRadian(radian);
		return color;
	}

	/**
	 * 根据弧度值计算颜色
	 * 
	 * @param radian
	 * @return
	 */
	private int calculateColorByRadian(double radian) {
		int color = Color.BLACK;
		radian = (2 * Math.PI + radian) % (2 * Math.PI); // [0, 2PI]
		double l = 2 * Math.PI / (mRingColors.length - 1); // 顔色区间的间距，起点与终点重合
		double d = radian % l; // 在间距间的移距
		int index = (int) (radian / l); // 起源顔色的index
		// Loger.d(mLogTag, "index: " + index);
		if (index == mRingColors.length - 1) {
			color = mRingColors[index];
		} else {
			int srcColor = mRingColors[index]; // 起始顔色
			int dstColor = mRingColors[(index + 1) % mRingColors.length]; // 结束顔色
			float pec = (float) (d / l);
			int red = aveRGB(Color.red(srcColor), Color.red(dstColor), pec);
			int green = aveRGB(Color.green(srcColor), Color.green(dstColor),
					pec);
			int blue = aveRGB(Color.blue(srcColor), Color.blue(dstColor), pec);
			color = Color.argb(255, red, green, blue);
		}
		return color;
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
	 * 更新定位点
	 * 
	 * @param event
	 */
	private void updateSelectPoint(PointF p) {
		// 计算出角度的弧值，3点钟方向为0角度，顺时针方向为正
		double radian = Math.atan2(p.y, p.x); // 当前触点的角度
		updateSelectPointByRadian(radian);
	}

	/**
	 * 根据弧度计更新选择点的位置
	 * 
	 * @param radian
	 */
	private void updateSelectPointByRadian(double radian) {
		radian = (2 * Math.PI + radian) % (2 * Math.PI); // [0, 2PI]
		// 选择点的中心
		double cx = mSelectPointToCenterR * Math.cos(radian);
		double cy = mSelectPointToCenterR * Math.sin(radian);
		mSelectPointRectF.left = (float) (cx - mSelectPointR);
		mSelectPointRectF.top = (float) (cy - mSelectPointR);
		mSelectPointRectF.right = (float) (cx + mSelectPointR);
		mSelectPointRectF.bottom = (float) (cy + mSelectPointR);
	}

	/**
	 * 是否触摸在色环内
	 * 
	 * @param e
	 * @return
	 */
	private boolean isTouchRing(PointF p) {
		boolean isTouch = false;
		int viewCy = 0;
		int viewCx = 0;
		float dSq = (p.y - viewCy) * (p.y - viewCy) + (p.x - viewCx)
				* (p.x - viewCx);
		// 触点离中心的距离小于外圆半径大于内圆半径，那么触摸了色环
		isTouch = dSq < mRingOutR * mRingOutR && dSq > mRingInR * mRingInR;
		return isTouch;
	}

	/**
	 * 转换一下坐标到以view的中心为原点,坐标轴x，y方向仍为向左，向下
	 * 
	 * @return
	 */
	private PointF transformXY(MotionEvent e) {
		// 转换一下坐标到以view的中心为原点,坐标轴x，y方向仍为向左，向下
		PointF p = new PointF();
		p.x = e.getX() - getWidth() / 2;
		p.y = e.getY() - getHeight() / 2;
		return p;
	}

	/**
	 * 设置监听器
	 * 
	 * @param l
	 */
	public void setOnColorChangedListener(OnColorChangedListener l) {
		if (mOnColorChangedListener != l) {
			mOnColorChangedListener = l;
		}
	}

	/**
	 * 设置选中的颜色的透明度
	 * 
	 * @param alpha
	 *            [0,1]
	 */
	public void setColorAlpha(float alpha) {
		int newColor = Color.argb((int) (alpha * 255), 0, 0, 0);
		newColor = newColor | (mSelectColor & 0x00FFFFFF);
		if (newColor != mSelectColor) {
			mSelectColor = newColor;
			mCirclePaint.setColor(mSelectColor);
			invalidate();
			notifyColorChange(false);
		}
	}

	/**
	 * 设置当前选中的顔色
	 * 
	 * @param color
	 *            ARGB
	 * @param colorOnly
	 *            是否只设置顔色，忽略透明度
	 */
	public void setSelectColor(int color, boolean colorOnly) {
		int newColor;
		if (colorOnly) {
			newColor = (mSelectColor & 0xFF000000) | (color & 0x00FFFFFF);
		} else {
			newColor = color;
		}
		if (newColor != mSelectColor) {
			mSelectColor = newColor;
			mCirclePaint.setColor(mSelectColor);
			moveSelectPoint();
			invalidate();
			notifyColorChange(false);
		}
	}

	/**
	 * 根据当前顔色计算出选中点的位置
	 */
	private void moveSelectPoint() {
		int color;
		double radian = -1;
		for (int i = 0; i < mRingColors.length; i++) {
			if (mRingColors[i] == (mSelectColor | 0xFF000000)) {
				radian = 2 * Math.PI / (mRingColors.length - 1) * i;
			}
		}
		if (radian < 0) {
			do {
				color = calculateColorByRadian(radian);
				// 顔色已经比较接近了
				if (compareTwoHSB(rgb2hsb(color), rgb2hsb(mSelectColor), 10)
						|| compareTwoRGB(color, mSelectColor, 10)
						|| compareTwoBW(color, mSelectColor, 10)) {
					break;
				}
				radian = radian + Math.PI / 360; // 递增半度
			} while (radian <= 2 * Math.PI);
		}
		// 找出了接近顔色
		if (radian <= 2 * Math.PI) {
			updateSelectPointByRadian(radian);
		}
	}

	/**
	 * 比较两个颜色
	 * 
	 * @param a
	 * @param b
	 * @param accuracy
	 *            精度
	 * @return
	 */
	private boolean compareTwoRGB(int aC, int bC, int accuracy) {
		boolean isSame = false;
		int r = Color.red(aC) - Color.red(bC);
		r = r * r;
		int g = Color.green(aC) - Color.green(bC);
		g = g * g;
		int b = Color.blue(aC) - Color.blue(bC);
		b = b * b;
		isSame = r + g + b < accuracy * accuracy;
		return isSame;
	}

	/**
	 * 比较两个颜色
	 * 
	 * @param a
	 * @param b
	 * @param accuracy
	 *            精度
	 * @return
	 */
	private boolean compareTwoHSB(Hsb a, Hsb b, float accuracy) {
		boolean isSame = false;
		float h = a.mH - b.mH;
		h = h * h;
		isSame = h < accuracy * accuracy;
		return isSame;
	}

	/**
	 * 黑白色比较
	 * 
	 * @param aC
	 * @param bC
	 * @param accuracy
	 * @return
	 */
	private boolean compareTwoBW(int aC, int bC, int accuracy) {
		boolean isSame = false;
		int r = Math.abs(Color.red(aC) - Color.red(bC));
		int g = Math.abs(Color.green(aC) - Color.green(bC));
		int b = Math.abs(Color.blue(aC) - Color.blue(bC));
		isSame = (r + g + b) / 3 < accuracy;
		return isSame;
	}

	/**
	 * RGB to HSB <br>
	 * http://blog.csdn.net/xhhjin/article/details/7020449
	 * http://www.cnblogs.com/latifrons/archive/2012/10/01/2709894.html
	 * 
	 * @param color
	 * @return
	 */
	private Hsb rgb2hsb(int color) {
		int rgbR, rgbG, rgbB;
		rgbR = Color.red(color);
		rgbG = Color.green(color);
		rgbB = Color.blue(color);
		assert 0 <= rgbR && rgbR <= 255;
		assert 0 <= rgbG && rgbG <= 255;
		assert 0 <= rgbB && rgbB <= 255;
		int[] rgb = new int[] { rgbR, rgbG, rgbB };
		Arrays.sort(rgb);
		int max = rgb[2];
		int min = rgb[0];

		float hsbB = max / 255.0f;
		float hsbS = max == 0 ? 0 : (max - min) / (float) max;

		float hsbH = 0;
		if (max == rgbR && rgbG >= rgbB) {
			hsbH = (rgbG - rgbB) * 60f / (max - min) + 0;
		} else if (max == rgbR && rgbG < rgbB) {
			hsbH = (rgbG - rgbB) * 60f / (max - min) + 360;
		} else if (max == rgbG) {
			hsbH = (rgbB - rgbR) * 60f / (max - min) + 120;
		} else if (max == rgbB) {
			hsbH = (rgbR - rgbG) * 60f / (max - min) + 240;
		}
		Hsb hsb = new Hsb();
		hsb.mB = hsbB;
		hsb.mH = hsbH;
		hsb.mS = hsbS;
		return hsb;
	}

	/**
	 * Hsb
	 * 
	 * @author laojiale
	 * 
	 */
	@SuppressWarnings("unused")
	private class Hsb {
		float mH;
		float mS;
		float mB;
	}

	/**
	 * 顔色改变
	 * 
	 * @param isFromUser
	 */
	private void notifyColorChange(boolean isFromUser) {
		if (null != mOnColorChangedListener) {
			mOnColorChangedListener.onColorChanged(isFromUser, mSelectColor);
		}
	}

	/**
	 * 监听接口
	 * 
	 * @author laojiale
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
