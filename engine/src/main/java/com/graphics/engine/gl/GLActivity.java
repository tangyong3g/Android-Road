package com.graphics.engine.gl;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;

import com.graphics.engine.gl.view.GLContentView;
import com.graphics.engine.gl.view.GLLayoutInflater;
import com.graphics.engine.gl.view.GLView;

/**
 * 
 * <br>
 * 类描述: 引擎的 Main Activity <br>
 * 功能详细描述:
 * 
 * <p>
 * 几个重要的方法：
 * <ul>
 * <li>{@link #setSurfaceView(GLContentView, boolean)}
 * <li>{@link #handleGLES20UnsupportedError()}
 * <li>{@link #reCreateSurfaceView()}
 * <li>{@link #initDefaultStatusBarHeight(int)}
 * <li>{@link #onGetStatusBarStaticHeight(int)}
 * <li>{@link #setFullScreen(boolean)}
 * </ul>
 * </p>
 * 
 * @date [2012-9-5]
 */
public class GLActivity extends Activity implements OnGlobalLayoutListener,
		GLContentView.SurfaceViewOwner {
	private static final String TAG = "GLActivity";
	private static final boolean DBG = false;

	private GLContentView mGLSurfaceView;
	private boolean mFullScreen;
	private int mStatusBarStaticHeight = 0;

	private boolean mLayoutInScreen;
	private boolean mGetStatusBarHeight;
	private int mSurfaceViewVisibilityBak = View.VISIBLE;
	private Runnable mGetStatusBarHeightAction;

	private OnGlobalLayoutListenerWrapper mGlobalLayoutListener;

	private static int sDefaultDisplayWidth; // 默认屏幕宽度
	private static int sDefaultDisplayHeight; // 默认屏幕高度
	private static int sStatusBarHeight; // 状态栏高度
	private static boolean sStatusBarHeightGot; // 是否获取过状态栏高度
	private static long sOnGlobalLayoutTime; // 接收到onGlobalLayout的时刻
	private static final int DEFAULT_STATUS_BAR_HEIGHT_IN_DP = 25; // 默认状态栏高度，目前手机与平板都使用25dp

	protected Handler mHandler = new Handler();
	private boolean mNeedPause = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		onCreate(savedInstanceState, false, false);
	}

	@Override
	protected void onDestroy() {
		if (DBG) {
			GLContentView.sLog.ins(TAG, "onDestroy " + this);
		}
		super.onDestroy();
		if (mGLSurfaceView != null) {
			mGLSurfaceView.onDestroy();
			mGLSurfaceView = null;
		}
		if (mGlobalLayoutListener != null) {
			mGlobalLayoutListener.setListener(null);
			mGlobalLayoutListener = null;
		}
		if (mGetStatusBarHeightAction != null) {
			mHandler.removeCallbacks(mGetStatusBarHeightAction);
			mGetStatusBarHeightAction = null;
		}

		// 清理Inflater
		GLLayoutInflater.remove(this);
	}

	/**
	 * <br>
	 * 功能简述: 创建实例时的初始化工作 <br>
	 * 功能详细描述:
	 * 
	 * @param savedInstanceState
	 * @param smoothFullScreenTransition
	 *            是否需要平滑的全屏切换过程，如果是，则窗口和屏幕一样大，那么View在非全屏的时候排版需要考虑通知栏的高度
	 * @param getStatusBarHeight
	 *            是否需要获取状态栏的高度
	 */
	protected void onCreate(Bundle savedInstanceState,
			boolean smoothFullScreenTransition, boolean getStatusBarHeight) {
		super.onCreate(savedInstanceState);
		if (DBG) {
			GLContentView.sLog.ins(TAG, "onCreate " + this);
		}

		Timer.setAutoUpdate(hashCode(), false);

		GLContentView.resetFrameTimeStamp();
		if (!detectGLES20()) {
			handleGLES20UnsupportedError();
		}
		GLContentView.createStaticView(this);

		mLayoutInScreen = smoothFullScreenTransition;

		if (!sStatusBarHeightGot) {
			mGetStatusBarHeight = getStatusBarHeight;

			Display display = getWindowManager().getDefaultDisplay();
			sDefaultDisplayWidth = display.getWidth();
			sDefaultDisplayHeight = display.getHeight();
		} else {
			mStatusBarStaticHeight = sStatusBarHeight;
			onGetStatusBarStaticHeight(mStatusBarStaticHeight);
		}

		if (mGetStatusBarHeight) {

		} else {
			if (mLayoutInScreen) {
				setLayoutInScreen();
			}
		}
	}

	/**
	 * <br>
	 * 功能简述: 设置在整个屏幕内排版 <br>
	 * 功能详细描述: 为了避免在全屏与非全屏状态之间切换要改变surface的大小造成卡顿，可以统一大小为屏幕大小 <br>
	 * 注意:
	 */
	private void setLayoutInScreen() {
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
						| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

		if (mGLSurfaceView != null) {
			mGLSurfaceView
					.setTranslateY(!mFullScreen && mLayoutInScreen ? mStatusBarStaticHeight
							: 0);
		}
	}

	/**
	 * <br>
	 * 功能简述: 设置OpenGL内容的根容器 <br>
	 * 功能详细描述: <br>
	 * 注意: 用户内容除了OpenGL内容外，还可以包含其他的2D内容，例如菜单，根据实际需要传入<var>setContent</var>参数。
	 * 
	 * <p>
	 * 如果在{@link #onCreate(Bundle, boolean, boolean)}
	 * 时指定要获取状态栏高度，那么会将<var>view</var> 设置为{@link View#GONE}，等到延迟获取到时(
	 * {@link #initDefaultStatusBarHeight(int)})再恢复。
	 * </p>
	 * 
	 * @param view
	 *            OpenGL场景的根容器
	 * @param setContent
	 *            是否调用{@link Activity#setContentView(View)}
	 *            ，让<var>view</var>作为用户内容的根容器
	 * 
	 * @see {@link #getSurfaceView()}
	 */
	public void setSurfaceView(GLContentView view, boolean setContent) {

		if (view == null) {
			throw new IllegalArgumentException("Argument view is null!");
		}
		if (DBG) {
			GLContentView.sLog.dns(TAG, "setSurfaceView view=" + view
					+ " content=" + setContent + " this=" + this);
		}
		view.setSurfaceViewOwner(this);
		if (setContent) {
			super.setContentView(view);
			final ViewGroup viewGroup = view.getOverlayedViewGroup();
			if (viewGroup != null) {

				// TODO 可能出现 viewGroup 存在父容器的情况 tangyong more detail LL -1060
				if (viewGroup.getParent() != null
						&& viewGroup.getParent() instanceof ViewGroup) {
					ViewGroup parent = (ViewGroup) viewGroup.getParent();
					if (parent != null) {
						parent.removeView(viewGroup);
					}
				}

				super.addContentView(viewGroup, new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			}
		}
		mGLSurfaceView = view;
		mGLSurfaceView.setTranslateY(mFullScreen && !mLayoutInScreen ? 0
				: mStatusBarStaticHeight);
		mGLSurfaceView.mContextHashCode = hashCode();

		if (mGetStatusBarHeight && !sStatusBarHeightGot) {
			final ViewTreeObserver observer = mGLSurfaceView
					.getViewTreeObserver();
			if (observer != null) {
				// 在获取状态栏高度的时候，需要将它设为GONE，以免排版使得它会resize导致闪帧
				mSurfaceViewVisibilityBak = mGLSurfaceView.getVisibility();
				mGLSurfaceView.setVisibility(View.GONE);
				mGlobalLayoutListener = new OnGlobalLayoutListenerWrapper();
				mGlobalLayoutListener.setListener(this);
				observer.addOnGlobalLayoutListener(mGlobalLayoutListener);
			}
		}

	}

	/**
	 * <br>
	 * 功能简述: 获取OpenGL内容的根容器 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 * @see {@link #setSurfaceView(GLContentView, boolean)}
	 */
	public GLContentView getSurfaceView() {
		return mGLSurfaceView;
	}

	/**
	 * <br>
	 * 功能简述: 获取OpenGL内容的根容器 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 * @deprecated 使用名称更符合意义的{@link #getSurfaceView()}代替，同时也避免和
	 *             {@link #getContentGlView()}混淆
	 */
	public GLContentView getGlContentView() {
		return mGLSurfaceView;
	}

	/**
	 * <br>
	 * 功能简述: 检测是否支持 OpenGL ES 2.0 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	private boolean detectGLES20() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ConfigurationInfo info = am.getDeviceConfigurationInfo();
		return info.reqGlEsVersion >= 0x20000; // CHECKSTYLE IGNORE
	}

	/**
	 * 用于延迟GLContentView 即 mGLSurfaceView的pause事件执行，避免不停按home键时的不停调用造成画面闪烁
	 * 在onPause里面会往Handler加入这个runnable 在onResume里面会移除这个runnable
	 */
	private Runnable mDelayContentPauseRunnable = new Runnable() {
		@Override
		public void run() {
			mNeedPause = false;
			if (mGLSurfaceView != null) {
				mGLSurfaceView.onPause();
			}
		}
	};

	@Override
	protected void onPause() {
		if (DBG) {
			GLContentView.sLog.ins(TAG, "onPause " + this);
		}
		mNeedPause = true;
		super.onPause();
		// 延迟执行
		mHandler.post(mDelayContentPauseRunnable);
	}

	// TODO: request for comment
	public void onPauseDelayed() {
		if (DBG) {
			GLContentView.sLog.ins(TAG, "onPauseDelayed " + this);
		}
		mNeedPause = true;
		super.onPause();
		// 延迟执行
		mHandler.postDelayed(mDelayContentPauseRunnable, 100);
	}

	@Override
	protected void onResume() {
		if (DBG) {
			// GLContentView.sLog.ins(TAG, "onResume " + this);
		}
		super.onResume();
		if (mNeedPause) {
			mHandler.removeCallbacks(mDelayContentPauseRunnable);
			mNeedPause = false;
		} else {
			if (mGLSurfaceView != null) {
				mGLSurfaceView.onResume();
			}
		}
		if (mGLSurfaceView != null) {
			mGLSurfaceView.setEventsEnabled(true);
		}
	}

	/**
	 * 当设备不支持 OpenGL ES 2.0时的处理
	 */
	@Override
	public void handleGLES20UnsupportedError() {
		throw new UnsupportedOperationException(
				"Your Device doesn't support OpenGL es 2.0");
	}

	/**
	 * <br>
	 * 功能简述: <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param view
	 */
	public void setContentGlView(GLView view) {
		if (mGLSurfaceView != null) {
			mGLSurfaceView.setContentView(view);
		}
	}

	public void setContentGlView(GLView view, LayoutParams params) {
		if (mGLSurfaceView != null) {
			mGLSurfaceView.setContentView(view, params);
		}
	}

	public void addContentGlView(GLView view) {
		if (mGLSurfaceView != null) {
			mGLSurfaceView.addContentView(view);
		}
	}

	public void addContentGlView(GLView view, LayoutParams params) {
		if (mGLSurfaceView != null) {
			mGLSurfaceView.addContentView(view, params);
		}
	}

	/**
	 * <br>
	 * 功能简述: <br>
	 * 功能详细描述: <br>
	 * 注意:只返回第一个ContentView
	 * 
	 * @return
	 */
	public GLView getContentGlView() {
		if (mGLSurfaceView != null) {
			return mGLSurfaceView.getContentView();
		}
		return null;
	}

	/**
	 * <br>
	 * 功能简述: 根据id查找一个GLView对象 <br>
	 * 功能详细描述: <br>
	 * 注意: 如果有重复，只返回第一个
	 * 
	 * @param id
	 * @return
	 */
	public final GLView findGLViewById(int id) {
		if (mGLSurfaceView != null) {
			return mGLSurfaceView.findGLViewById(id);
		}
		return null;
	}

	/**
	 * <br>
	 * 功能简述: 根据tag查找一个GLView对象 <br>
	 * 功能详细描述: <br>
	 * 注意: 如果有重复，只返回第一个
	 * 
	 * @param tag
	 * @return
	 */
	public final GLView findViewWithTag(Object tag) {
		if (mGLSurfaceView != null) {
			return mGLSurfaceView.findGLViewWithTag(tag);
		}
		return null;
	}

	/**
	 * <br>
	 * 功能简述: 设置是否为全屏状态 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param fullScreen
	 *            是否为全屏状态
	 * @see {@link #isFullScreen()}
	 */
	protected void setFullScreen(boolean fullScreen) {
		if (mFullScreen == fullScreen) {
			return;
		}
		if (fullScreen) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			if (mGLSurfaceView != null) {
				mGLSurfaceView.setTranslateY(0);
			}
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			if (mGLSurfaceView != null) {
				mGLSurfaceView.setTranslateY(!mLayoutInScreen ? 0
						: mStatusBarStaticHeight);
			}
		}
		mFullScreen = fullScreen;
	}

	/**
	 * <br>
	 * 功能简述: 是否为全屏状态 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 * @see {@link #setFullScreen(boolean)}
	 */
	public boolean isFullScreen() {
		return mFullScreen;
	}

	/**
	 * <br>
	 * 功能简述: 当这个Activity创建后会去获取状态栏的默认高度，这个方法是当获取到时的回调方法 <br>
	 * 功能详细描述: <br>
	 * 注意: 获取和回调之间可能是异步的
	 * 
	 * @param height
	 *            状态栏的默认高度
	 */
	protected void onGetStatusBarStaticHeight(int height) {

	}

	/**
	 * <br>
	 * 功能简述: 获取状态栏的默认高度 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return 状态栏的默认高度
	 * @see {@link #getStatusBarStaticHeight()}
	 */
	public int getStatusBarStaticHeight() {
		return mStatusBarStaticHeight;
	}

	/**
	 * <br>
	 * 功能简述: 获取状态栏当前显示的高度 <br>
	 * 功能详细描述: <br>
	 * 注意: 如果使用{@link #setFullScreen(boolean)}设置了全屏，那么返回值是0。
	 * 
	 * @return 状态栏当前显示的高度
	 * @see {@link #getStatusBarStaticHeight()}
	 */
	public int getStatusBarHeight() {
		return mFullScreen ? 0 : mStatusBarStaticHeight;
	}

	/** 响应全局排版事件，如果为了获取状态栏高度而注册了监听 */
	@Override
	public void onGlobalLayout() {
		if (sOnGlobalLayoutTime == 0) {
			sOnGlobalLayoutTime = System.currentTimeMillis();
		}
		if (DBG) {
			GLContentView.sLog.dns(TAG, "onGlobalLayout time="
					+ sOnGlobalLayoutTime + " this=" + this + " view="
					+ mGLSurfaceView);
		}

		if (mGetStatusBarHeightAction != null) {
			return;
		}

		// 获取状态栏的高度
		mGetStatusBarHeightAction = new Runnable() {
			public void run() {
				Rect frame = new Rect();
				getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
				if (DBG) {
					GLContentView.sLog.vns(TAG, "getWindowVisibleDisplayFrame "
							+ frame + " activity=" + GLActivity.this + " view="
							+ mGLSurfaceView);
				}
				// 如果是在其他全屏应用下进入桌面，则需要等待状态栏恢复再重试
				if (frame.width() == sDefaultDisplayWidth
						&& frame.height() == sDefaultDisplayHeight) {
					if (System.currentTimeMillis() - sOnGlobalLayoutTime > 500) { // CHEKCSTYLE
																					// IGNORE
						WindowManager.LayoutParams attrs = getWindow()
								.getAttributes();
						if ((attrs.flags & WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN) == WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN) {
							// 部分平板自身没有顶部状态栏，flag已经包含WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN，那么状态栏高度为0
							frame.top = 0;
							initDefaultStatusBarHeight(frame.top);
							return;
						} else {
							// 等待500毫秒了状态栏还是没恢复，强制使用25dp
							frame.top = Math.round(getResources()
									.getDisplayMetrics().density
									* DEFAULT_STATUS_BAR_HEIGHT_IN_DP);
						}
					} else {
						mHandler.postDelayed(this, 2); // CHEKCSTYLE IGNORE
						return;
					}
				}

				// 部分机型会出现frame.top突然为-10000，强制修正为默认值
				if (frame.top < -200) {
					frame.top = Math
							.round(getResources().getDisplayMetrics().density
									* DEFAULT_STATUS_BAR_HEIGHT_IN_DP);
				}

				if (DBG) {
					GLContentView.sLog.dns(TAG, "initDefaultStatusBarHeight "
							+ frame.top + " activity=" + GLActivity.this
							+ " view=" + mGLSurfaceView);
				}
				initDefaultStatusBarHeight(frame.top);
			}
		};
		mHandler.post(mGetStatusBarHeightAction);
		mGlobalLayoutListener.setListener(null);

		final ViewTreeObserver observer = mGLSurfaceView.getViewTreeObserver();
		if (observer != null) {
			observer.removeGlobalOnLayoutListener(mGlobalLayoutListener);
		}
	}

	/**
	 * <br>
	 * 功能简述: 根据状态栏默认高度设置 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param height
	 */
	protected void initDefaultStatusBarHeight(int height) {
		sStatusBarHeightGot = true;
		sStatusBarHeight = height;
		mStatusBarStaticHeight = height;

		if (mLayoutInScreen) {
			setLayoutInScreen();
		}
		if (mGLSurfaceView != null) {
			mGLSurfaceView.setVisibility(mSurfaceViewVisibilityBak);
		}
		onGetStatusBarStaticHeight(mStatusBarStaticHeight);
	}

	protected boolean needForceShowSurfaceView() {
		return mGetStatusBarHeight && !sStatusBarHeightGot;
	}

	/**
	 * <br>
	 * 功能简述: <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param setContent
	 *            将新创建的GLContentview设为ContentView
	 */
	protected final void reCreateSurfaceView(boolean setContent) {
		if (DBG) {
			GLContentView.sLog.dns(TAG, "reCreateSurfaceView");
		}
		GLContentView oldGLContentView = mGLSurfaceView;
		if (oldGLContentView == null) {
			return;
		}
		GLContentView newContentView = new GLContentView(
				getApplicationContext(), oldGLContentView.isTranslucent());
		newContentView.transferFrom(oldGLContentView);
		setSurfaceView(newContentView, setContent);
		oldGLContentView.onDestroy();
		mGLSurfaceView.onResume();
	}

	/**
	 * {@inheritDoc} 如果没有特殊情况（例如外部保持GLContentView的引用，或者将它加载到其他视图容器），可以调用
	 * {@link #reCreateSurfaceView(boolean)}这个默认实现。 如果重载了就不要调用super的本方法，避免重复。
	 */
	@Override
	public void reCreateSurfaceView() {
		reCreateSurfaceView(true);
	}

}

/**
 * 
 * <br>
 * 类描述: 全局排版事件监听者的封装者 <br>
 * 功能详细描述: 如果需要获取系统状态栏高度，那么需要注册全局排版时间监听。为了避免被系统保存了引用造成内存泄露，采用封装的方式。
 * 
 * @author dengweiming
 * @date [2012-11-5]
 */
class OnGlobalLayoutListenerWrapper implements OnGlobalLayoutListener {
	OnGlobalLayoutListener mListener;

	void setListener(OnGlobalLayoutListener listener) {
		mListener = listener;
	}

	@Override
	public void onGlobalLayout() {
		if (mListener != null) {
			mListener.onGlobalLayout();
			setListener(null);
		}
	}

}