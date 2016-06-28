package com.graphics.enginedemo;

import com.go.gl.animation.Animation;
import com.go.gl.animation.ScaleAnimation;
import com.go.gl.animation.Transformation3D;
import com.go.gl.graphics.ColorGLDrawable;
import com.go.gl.graphics.GLCanvas;
import com.go.gl.math3d.Ray;
import com.go.gl.view.GLFrameLayout;
import com.go.gl.view.GLLayoutInflater;
import com.go.gl.view.GLView;
import com.go.gl.view.GLViewGroup;
import com.go.gl.widget.GLDragListener;
import com.go.gl.widget.GLDragView;
import com.sny.tangyong.androiddemo.R;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * 
 * <li>這裏要搞明白的問題有
 * 
 * <li>  DragView的工作原理
 * 					<div>
 * 								onInterceptTouchEvent 中把 MontionEvent傳入 DragView中 ， 
 * 					<div>
 * 					<li>　停区是怎么定义的？怎么样认为是进入的这个区？为什么进入这个区后位置不会回到原来的地方?
 * 
 * 
 * 					2014-05-08
 * 	
 * 					这么去理解，DLDragView里面在dispatchDraw里面就去绘制了　GlDragView.startDrag(params,params_2);params_2 并且在里面响应　GlDragListener 回调相应的状态，比如开始绘制，正在绘制等
 * 
 * 					不理解的地方: 
 * 											<li> 是否达到那些停的区域怎么处理呢？ 是在客户端还是在GlGragView里面去处理的。还是定义好区域，然后注入到GlDragView中？
 * 
 * 											<li> {@link GLDragListener} 里面的相应的接口方法，代表是什么意思呢?
 * 
 * 													1:OnDragStar()	 在OnStartDrag()时给调用。表示开始拖动。
 * 													2:onDragEnd();   在手指离开时会触发
 * 													3:onChckTouch(); 在客户端中实现，用来表示是否进入此区域　
 * 													4:onDragMove();	 在移动时会调用　
 * 													5:onDragEnter(); 进入相应的区域　
 * 													6:onDragExist(); 出了相应的区域　	
 * 													7:onDragHover(); 
 * 													8:onDropFrom();
 * 													9:onDropTo();
 * 												   10:onDrawDraggedView();
 * 
 * 											问题　一
 * 												在　GLDragView中的Move事件中，会调用GlDragListener的OnCheckTcouch事件。如果是进入的相应的区域，那么就会调用listener的Enter事件。
 * 
 * 											问题二
 * 												是在客户端来定义相应的检测规则。
 * 
 * 											
 * 				
 * 
 * <li>  GLView在 GlFrameLayout中的佈局
 * <li>  拖拽懸停區是怎麼用？
 * 
 * 
 * 
 * 
 * <br>类描述: 视图拖拽的简单测试
 * <br>功能详细描述: 屏幕右边1/4的区域可以停放，屏幕右边1/16的区域可以悬停
 * 
 * @author  dengweiming
 * @date  [2013-7-1]
 */
public class DragTestView extends GLFrameLayout implements GLDragListener {
	final static float SCALE = 1.21f;
	
	//可动的View
	GLDragView mDragView;
	// View Group
	GLViewGroup mLayout;
	//标记位 是否进入右边
	boolean mEnterRightSide;
	//是否进入停区　
	boolean mPendingHoverOnRightSide;
	
	//聚焦区
	ColorGLDrawable mFocusDrawable = new ColorGLDrawable(0x7fffffff);
	//停下的区域
	ColorGLDrawable mHoverDrawable = new ColorGLDrawable(0x7fff0000);

	
	public DragTestView(Context context) {
		super(context);
		
		
		GLLayoutInflater inflater = GLLayoutInflater.from(getContext());

		//转化xml成GlViewGroup
		mLayout = (GLViewGroup) inflater.inflate(R.layout.test, null);
		
		//添加进来当前的View
		addView(mLayout);

		//可施动的View
		mDragView = new GLDragView(context);
		addView(mDragView);
		mDragView.setVisibility(INVISIBLE);

		
		for (int count = mLayout.getChildCount(), i = 0; i < count; ++i) {
			// 给当前布局中的子view 注册长按事件，
			GLView child = mLayout.getChildAt(i);
			child.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(GLView v) {
					v.setVisibility(INVISIBLE);
					
					//把要 拖拽的view放入DragView中
					mDragView.startDrag(DragTestView.this, v, null, null);

					// 施动开始的时候就开始一个300毫秒的动画，放大的动画
					Animation a = new ScaleAnimation(1, SCALE, 1, SCALE, 
							Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
					a.setDuration(300);
					mDragView.startDragAnimation(a);

					return true;
				}
			});
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		//右边1/4的区域作为可交互的
		mFocusDrawable.setBounds((int) (w * 3 / 4), 0, w, h);
		//右边1/16的区域作为可悬停的，应该被包含与可交互区域内
		mHoverDrawable.setBounds((int) (w * 15 / 16), 0, w, h);
	}
	
	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		
		
		if (mEnterRightSide && !mPendingHoverOnRightSide && !mResumePeningRunnablePosted) {
			mFocusDrawable.draw(canvas);
		}
		if (mPendingHoverOnRightSide) {
			mHoverDrawable.draw(canvas);
		}
		super.dispatchDraw(canvas);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		//拖动发生后将事件自动发送到拖动层，不用让被拖动的视图发送
		if (mDragView.isInDrag()) {
			mDragView.dispatchTouchEvent(ev);
			return false;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public void onDragStart(GLDragView view) {
		Log.i("cycle","onDragStart");
	}

	@Override
	public void onDragEnd(GLDragView view) {
		Log.i("cycle","onDragEnd");
	}

	@Override
	public int onCheckTouch(GLDragView view, float x, float y, Ray ray) {
		Log.i("cycle","onCheckTouch"+x +"y:\t"+y);
		return x >= mFocusDrawable.getBounds().left ? GLDragView.HIT : GLDragView.MISS;
	}

	@Override
	public boolean onDragMove(GLDragView view, float x, float y, Ray ray) {
		
		Log.i("cycle","onDragMove"+x +"y:\t"+y);
		mPendingHoverOnRightSide = x >= mHoverDrawable.getBounds().left;
		if (!mPendingHoverOnRightSide && mResumePeningRunnablePosted) {
			mResumePeningRunnablePosted = false;
			removeCallbacks(mResumePeningRunnable);
		}
		return false;
	}

	@Override
	public void onDragEnter(GLDragView view) {
		Log.i("cycle","onDragEnter");
		mEnterRightSide = true;
	}

	@Override
	public void onDragExit(GLDragView view) {
		Log.i("cycle","onDragExit");
		mEnterRightSide = false;
		mPendingHoverOnRightSide = false;
	}

	Runnable mResumePeningRunnable = new Runnable() {
		@Override
		public void run() {
			mPendingHoverOnRightSide = true;
			mResumePeningRunnablePosted = false;
		}
	};
	boolean mResumePeningRunnablePosted;

	@Override
	public long onDragHover(GLDragView view, float x, float y, Ray ray) {
		
		Log.i("cycle","onDragMove"+x +"y:\t"+y);
		
		//		if (mHoverOnRightSide) {	//使用这个条件会依赖onDragMove，如果没有触摸移动则不会响应下一次hover
		if (x >= mHoverDrawable.getBounds().left) {
			mPendingHoverOnRightSide = false;
			//可选地做一个动画
			Toast.makeText(getContext(), "onHover", 1000).show();
			//希望在动画结束后，恢复高亮hover区域，以便支持连续hover
			postDelayed(mResumePeningRunnable, 2000);
			mResumePeningRunnablePosted = true;
			return 2000;
		}
		return 0;
	}

	@Override
	public boolean onDropFrom(GLDragView view, float x, float y, Ray ray, GLDragListener target, boolean isHandled) {
		Log.i("cycle","onDropFrom"+x +"y:\t"+y);
		
		return false;
	}
	
	@Override
	public boolean onDropTo(GLDragView view, float x, float y, Ray ray, GLDragListener source) {
		
		Log.i("cycle","onDropTo"+x +"y:\t"+y);
		
		if (source == this) {
			final float[] center = view.getDraggedViewCenterPosition();
			GLView v = view.getDraggedView();
			v.offsetLeftAndRight((int) (center[0] - v.getLeft() - v.getWidth() * 0.5f));
			v.offsetTopAndBottom((int) (-center[1] - v.getTop() - v.getHeight() * 0.5f));
			mLayout.bringChildToFront(v);
			return true;
		}
		return false;
	}

	@Override
	public boolean onDrawDraggedView(GLDragView view, GLCanvas canvas, GLView draggedView, Transformation3D t) {
		Log.i("cycle","onDrawDraggedView");
		return false;
	}
}
