package com.graphics.enginedemo;


import android.content.Context;
import android.view.MotionEvent;

import com.graphics.engine.animator.ValueAnimator;
import com.graphics.engine.graphics.GLCanvas;
import com.graphics.engine.model.Ms3dModel;
import com.graphics.engine.view.GLView;

/**
 * <br>类描述: 测试Ms3d模型和动画
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-11-18]
 */
public class MS3DTestView extends GLView {
	Ms3dModel mWoodModel;
	Ms3dModel mBugModel;

	public MS3DTestView(Context context) {
		super(context);
		mWoodModel = new Ms3dModel(getContext(), "wood.ms3d.mp3");
		mBugModel = new Ms3dModel(getContext(), "bug.ms3d.mp3");
		mBugModel.loadAnimation(getContext(), "bug.psa.mp3");

		//将第一个动画的Y轴平移量和旋转量修正
		mBugModel.fixAnimationTranslation(0, 2, 0, 0, 0);
		mBugModel.fixAnimationRotation(0, 0, 0, 0, 1);

		mBugModel.playAnimation(0, true, 1, -1);
		mBugModel.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				invalidate();
			}
		});
	}

	@Override
	protected void onDraw(GLCanvas canvas) {
		final boolean isDepthEnabled = canvas.isDepthEnabled();
		canvas.setDepthEnable(true);
		canvas.setLookFromTop(0, 0, 0, true, false);
		canvas.scale(4, 4, 4);

		mWoodModel.draw(canvas);
		canvas.translate(0, 0, -50);
		mBugModel.draw(canvas);

		canvas.setDepthEnable(isDepthEnabled);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				mBugModel.playAnimation(1, false, 2, -1);
				break;
			case MotionEvent.ACTION_UP :
				mBugModel.playAnimation(0, true, 1, -1);
				break;
			default :
				break;
		}
		invalidate();
		return true;
	}

	@Override
	public void cleanup() {
		super.cleanup();
		mWoodModel.clear();
		mBugModel.clear();
	}

}
