package com.ty.example_unit_1;


import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class CanavsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置全屏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(new RenderView(this));
	}

	class RenderView extends View {
		Paint paint = new Paint();

		public RenderView(Context context) {
			super(context);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			canvas.drawRGB(255, 255, 255);
			paint.setColor(Color.RED);
			//画屏幕的对角线，因为原点在左上角,x向右，y向下
			canvas.drawLine(0, 0, canvas.getWidth() - 1,canvas.getHeight() - 1, paint);

			//用笔画，中心区域不会填充
			paint.setStyle(Style.STROKE);
			paint.setColor(0xff00ff00);
			//以中心为原点画圆
			canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2,40, paint);

			//画方形FIll表示会填充
			paint.setStyle(Style.FILL);
			paint.setColor(0x770000ff);
			canvas.drawRect(100, 100, 200, 200, paint);
			invalidate();
		}
	}

}
