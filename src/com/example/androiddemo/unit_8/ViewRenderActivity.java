package com.example.androiddemo.unit_8;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sny.tangyong.androiddemo.R;

/**
 * 
 * 这里要知道的问题有:
 * 
 * 
 * <li> 1: 知道普通的ＶＩＥＷ和ＶＩＥＷＧＲＯＵＰ的　mesure 的过程		mesarue
 * 					
 * 				1.1  作用　为整个View树计算实际的大小，即设置实际的高(对应属性:mMeasuredHeight)和宽(对应属性:mMeasureWidth)，每个View的控件的实际宽高都是由父视图和本身视图决定的。
 * 				1.2　　mesaure 调用　onMesaure 如果是ＶＩＥＷ　那么是通过setMeasuredDimension　来设置大小。如果是VIEWGROUP需要调用子类的 mesaure来实现　
 * 			
 * 
 * 			
 * <li> 2: 知道普通的ＶＩＥＷ和ＶＩＥＷＧＲＯＵＰ的　ＬＡＹＯＵＴ 的过程
 * 
 * 				2.1  作用： 为将整个根据子视图的大小以及布局参数将View树放到合适的位置上。
 * 				2.2  layout方法会设置该View视图位于父视图的坐标轴，即mLeft，mTop，mLeft，mBottom(调用setFrame()函数去实现) 接下来回调onLayout()方法(如果该View是ViewGroup对象，需要实现该方法，对每个子视图进行布局) 
 * 				
 * 
 * 
 * <li> 3: 知道普通的ＶＩＥＷ和ＶＩＥＷＧＲＯＵＰ的　DRAW 的过程
 * 	
 * 				3.1 绘制ＶＩＥＷ的背景
 * 				3.2 调用ＯＮＤＲＡＷ方法（每个ＶＩＥＷ都要调用这个方法来绘制视图自己，ＶＩＥＷＧＲＯＵＰ除外，因为他是他的子类去实现）
 * 				3.3 调用ＤＩＳＰＡＴＣＨＤＲＡＷ　（一个的ＶＩＥＷ不需要重写这个方法，主要是为ＶＩＥＷＧＲＯＵＰ来准备的）	
 * 				3.4 VIEWGROUP会调用每个子ＶＩＥ的ＤＲＡＷ事件。
 * 
 * 
 * @author tang
 *
 */
public class ViewRenderActivity extends Activity {

	private static final String TAG = "ViewRenderActivity";
	private MyViewGroup mContainer;

	private static StringBuffer mLog = new StringBuffer();
	
	
	@Override
	protected void onStop() {
		mLog.delete(0, mLog.length());
		super.onStop();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContainer = new MyViewGroup(this);
		setContentView(mContainer);
		
		mContainer.postDelayed(new Runnable() {

			@Override
			public void run() {
				
				String tx = mLog.toString();
				TextView txview = new TextView(ViewRenderActivity.this);
				txview.setText(tx);
				mContainer.addView(txview);
			}
		}, 1500);
	}

	/**
	 * 
	 * @author tang
	 *
	 */
	class MyViewGroup extends ViewGroup {

		private Context mContext;

		public MyViewGroup(Context context) {
			super(context);
			mContext = context;
			init();
		}

		//为MyViewGroup添加三个子View  
		private void init() {

			//child 对象一 ： Button  
			Button btn = new Button(mContext);
			btn.setText("I am Button");
			this.addView(btn);

			//child 对象二 : ImageView   
			ImageView img = new ImageView(mContext);
			img.setBackgroundResource(R.drawable.ic_launcher);
			this.addView(img);

			//child 对象三 : TextView  
			TextView txt = new TextView(mContext);
			txt.setText("Only Text");
			this.addView(txt);

			//child 对象四 ： 自定义View  
			MyView myView = new MyView(mContext);
			this.addView(myView);
		}

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {

			mLog.append("MyViewGroup:\t" + "onLayout");
			mLog.append("\n");

			int childCount = getChildCount();

			int startLeft = 0;
			int startTop = 10;

			for (int i = 0; i < childCount; i++) {
				
				View child = getChildAt(i);
				child.layout(startLeft, startTop, startLeft + child.getMeasuredWidth(), startTop + child.getMeasuredHeight());
//				startLeft = startLeft + child.getMeasuredWidth() + 10;
				startTop = startTop + child.getMeasuredHeight() + 10;
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			mLog.append("MyViewGroup:\t" + "onMeasure ");
			mLog.append("\n");
			int childCount = getChildCount();

			//获取该ViewGroup的实际长和宽  涉及到MeasureSpec类的使用  
			int specSize_Widht = MeasureSpec.getSize(widthMeasureSpec);
			int specSize_Heigth = MeasureSpec.getSize(heightMeasureSpec);

			//设置本ViewGroup的宽高  
			setMeasuredDimension(specSize_Widht, specSize_Heigth);
			
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				child.measure(50, 50);   //简单的设置每个子View对象的宽高为 50px , 50px    
				//或者可以调用ViewGroup父类方法measureChild()或者measureChildWithMargins()方法  
				//this.measureChild(child, widthMeasureSpec, heightMeasureSpec) ;  
			}
		}

		@Override
		protected void dispatchDraw(Canvas canvas) {
			super.dispatchDraw(canvas);
		}

		@Override
		protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
			return super.drawChild(canvas, child, drawingTime);
		}

	}

	/**
	 * @author tang
	 */
	class MyView extends View {

		private Paint mPaint;

		public MyView(Context context) {
			super(context);

			mPaint = new Paint();
			mPaint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			mPaint.setColor(Color.RED);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			mLog.append("MyView:\t" + "onDraw ");
			mLog.append("\n");

			canvas.drawColor(Color.BLUE);
			canvas.drawRect(0, 0, 30, 30, mPaint);
			canvas.drawText("MyView", 10, 40, mPaint);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

			mLog.append("MyView:\t" + "onMeasure ");
			mLog.append("\n");
			setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		}

	}

}
