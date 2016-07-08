package com.graphics.gowidget.core;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.graphics.engine.view.GLView;
import com.graphics.engine.view.GLViewGroup;

/**
 * Next桌面屏幕小装饰挂件视图
 * @author yangyiwei
 * @date 2014-08-26
 */
public class NextPetView extends GLViewGroup implements IGoWidget3D {
	
	/**
	 * 小挂件回调接口 （待扩展）
	 * @author yangyiwei
	 * 
	 */
	public interface INextPetDelegate {
		/**
		 * 点击小挂件的回调
		 * @param adornment	小挂件
		 */
		public void onPetClick(NextPetView pet);
		
		/**
		 * 小挂件需要重新布局
		 * @param adornment
		 * @param left
		 * @param top
		 * @param right
		 * @param bottom
		 */
		public void onPetRequestLayout(NextPetView pet, int left, int top, int right, int bottom);
	}
	
	public static final String DEBUG_TAG = "NextPetView";

	protected INextPetDelegate mDelegate = null;
	
	public NextPetView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public NextPetView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public NextPetView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 指定回调委托
	 * @param delegate
	 */
	public void setDelegate(INextPetDelegate delegate) {
		mDelegate = delegate;
	}
	
	/**
	 * 横竖屏切换
	 */
	public void onOrientationChanged() {
		
	}
	
	/**
	 * 复原操作
	 */
	public void reset() {
		
	}
	
	/**
	 * 限定绘图区域（小挂件能全画布绘制，不受视图大小限制）
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	public void clipDrawRect(int left, int top, int right, int bottom) {
		
	}
	
	/**
	 * 是否支持摆动
	 * @return
	 */
	public boolean canShake() {
		return false;
	}
	
	/**
	 * 
	 * @param event
	 */
	public void onCellLayoutTouched(MotionEvent event) {
		
	}

	/**
	 * 版本号
	 * code 4 支持元素摆动
	 */
	@Override
	public int getVersion() {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "getVersion:4");
		return 4;
	}

	@Override
	public GLView getContentView() {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "getContentView:" + this.toString());
		return this;
	}

	@Override
	public void setWidgetCallback(WidgetCallback callback) {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "setWidgetCallback:" + (callback == null ? "null" : callback.toString()));
	}

	@Override
	public void onStart(Bundle data) {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "onStart:" + (data == null ? "null" : data.toString()));
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "onStop");
	}

	@Override
	public void onDelete() {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "onDelete");
	}

	@Override
	public void onRemove() {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "onRemove");
	}

	@Override
	public boolean onApplyTheme(Bundle data) {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "onApplyTheme:" + (data == null ? "null" : data.toString()));
		return false;
	}

	@Override
	public void onEnter() {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "onEnter");
	}

	@Override
	public void onLeave() {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "onLeave");
	}

	@Override
	public boolean onActivate(boolean animate, Bundle data) {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "onActivate:" + animate + ", " + (data == null ? "null" : data.toString()));
		return false;
	}

	@Override
	public boolean onDeactivate(boolean animate, Bundle data) {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "onDeactivate:" + animate + ", " + (data == null ? "null" : data.toString()));
		return false;
	}

	@Override
	public void onClearMemory() {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "onClearMemory");
	}

	@Override
	public void onEnableInvalidate() {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "onEnableInvalidate");
	}

	@Override
	public void onDisableInvalidate() {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "onDisableInvalidate");
	}

	@Override
	public int getBackgroundAnimationType() {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "getBackgroundAnimationType");
		return 0;
	}

	@Override
	public GLView getKeepView() {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "getKeepView");
		return null;
	}

	@Override
	public boolean isSupportDisableInvalidate() {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "isSupportDisableInvalidate");
		return false;
	}

	@Override
	public Object action(int actionId, int param, boolean flag, Object... objs) {
		// TODO Auto-generated method stub
		Log.d(DEBUG_TAG, "action");
		return null;
	}
}
