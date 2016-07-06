package com.sany.tangyong.engineoriginal;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.Toast;

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

/**
 * 
 * <br>类描述: 视图拖拽的简单测试
 * <br>功能详细描述: 屏幕右边1/4的区域可以停放，屏幕右边1/16的区域可以悬停
 * 
 * @author  dengweiming
 * @date  [2013-7-1]
 */
public class DragTestView extends GLFrameLayout implements GLDragListener {
	final static float SCALE = 1.21f;

	GLDragView mDragView;
	GLViewGroup mLayout;

	boolean mEnterRightSide;
	boolean mPendingHoverOnRightSide;
	ColorGLDrawable mFocusDrawable = new ColorGLDrawable(0x7fffffff);
	ColorGLDrawable mHoverDrawable = new ColorGLDrawable(0x7fff0000);

	public DragTestView(Context context) {
		super(context);

		GLLayoutInflater inflater = GLLayoutInflater.from(getContext());

		mLayout = (GLViewGroup) inflater.inflate(R.layout.test, null);
		addView(mLayout);

		mDragView = new GLDragView(context);
		addView(mDragView);
		mDragView.setVisibility(INVISIBLE);

		for (int count = mLayout.getChildCount(), i = 0; i < count; ++i) {
			GLView child = mLayout.getChildAt(i);
			child.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(GLView v) {
					v.setVisibility(INVISIBLE);
					mDragView.startDrag(DragTestView.this, v, null, null);

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
		// TODO Auto-generated method stub

	}

	@Override
	public void onDragEnd(GLDragView view) {
		// TODO Auto-generated method stub

	}

	@Override
	public int onCheckTouch(GLDragView view, float x, float y, Ray ray) {
		return x >= mFocusDrawable.getBounds().left ? GLDragView.HIT : GLDragView.MISS;
	}

	@Override
	public boolean onDragMove(GLDragView view, float x, float y, Ray ray) {
		mPendingHoverOnRightSide = x >= mHoverDrawable.getBounds().left;
		if (!mPendingHoverOnRightSide && mResumePeningRunnablePosted) {
			mResumePeningRunnablePosted = false;
			removeCallbacks(mResumePeningRunnable);
		}
		return false;
	}

	@Override
	public void onDragEnter(GLDragView view) {
		mEnterRightSide = true;
	}

	@Override
	public void onDragExit(GLDragView view) {
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
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean onDropTo(GLDragView view, float x, float y, Ray ray, GLDragListener source) {
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
		// TODO Auto-generated method stub
		return false;
	}
}
