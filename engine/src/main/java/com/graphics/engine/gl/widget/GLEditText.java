package com.graphics.engine.gl.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.EditText;
import android.widget.TextView.OnEditorActionListener;

import com.graphics.engine.gl.util.FontStyleManager;
import com.graphics.engine.gl.util.OSVersion;
import com.graphics.engine.gl.view.GLContentView;
import com.graphics.engine.gl.view.GLView;

import java.lang.ref.WeakReference;

/**
 * 
 * <br>类描述: 文字编辑视图的封装器
 * <br>功能详细描述: 如果Activity设置了LayoutInScreen，则需要放置到屏幕上半部分，否则会被输入法界面挡住
 * 
 * @author  dengweiming
 * @date  [2013-6-18]
 */
public class GLEditText extends GLTextView {
	/** 设为true的时候，将使用重载的EditText，可以在里面重载方法，加断点或者打印信息，以便调试*/
	private static final boolean DBG_EDITTEXT = false;
	
	private EditText mEditText;
	private MyOnCreateContextMenuListener mOnCreateContextMenuListener;

	public GLEditText(Context context) {
		super(context);
		
		mOnCreateContextMenuListener = new MyOnCreateContextMenuListener(this);
		mEditText.setOnCreateContextMenuListener(mOnCreateContextMenuListener);
	}

	public GLEditText(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.editTextStyle);
	}

	public GLEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mOnCreateContextMenuListener = new MyOnCreateContextMenuListener(this);
		mEditText.setOnCreateContextMenuListener(mOnCreateContextMenuListener);
	}
	
	void initTextView(Context context) {
		if (DBG_EDITTEXT) {
			mEditText = new DebugEditText(context);
		} else {
			mEditText = new EditText(context);
		}
		mTextView = mEditText;
		mTextView.setTypeface(FontStyleManager.getFontStyle());
	}

	void initTextView(Context context, AttributeSet attrs, int defStyle) {
		if (DBG_EDITTEXT) {
			mEditText = new DebugEditText(context, attrs, defStyle);
		} else {
			mEditText = new EditText(context, attrs, defStyle);
		}
		mTextView = mEditText;
		mTextView.setTypeface(FontStyleManager.getFontStyle());
	}

	/**
	 * <br>功能简述: 获取EditText视图
	 * <br>功能详细描述:
	 * <br>注意: 如果cleanup了，返回null
	 * @return
	 */
	public EditText getEditText() {
		return mEditText;
	}

	/**
	 * <br>功能简述: 获取文字
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public Editable getText() {
		return mEditText == null ? null : mEditText.getText();
	}
	
	public void setText(CharSequence text) {
		if (mEditText != null) {
			mEditText.setText(text);
		}
	}
	
	public void addTextChangedListener(TextWatcher watcher) {
		if (mEditText != null) {
			mEditText.addTextChangedListener(watcher);
		}
	}
	
	public void setOnEditorActionListener(OnEditorActionListener l) {
		if (mEditText != null) {
			mEditText.setOnEditorActionListener(l);
		}
	}
	
	public CharSequence getHint() {
		return mEditText == null ? "" : mEditText.getHint();
	}
	
	public void setHint(CharSequence hint) {
		if (mEditText != null) {
			mEditText.setHint(hint);
		}
	}

	@Override
	public void cleanup() {
		mEditText.setOnCreateContextMenuListener(null);
		mOnCreateContextMenuListener.mWrapperRef.clear();
		mEditText = null;
		super.cleanup();
	}

	/**
	 * <br>类描述: 弹出菜单事件的监听者
	 * <br>功能详细描述:
	 * 
	 * @author  dengweiming
	 * @date  [2013-6-18]
	 */
	public static class MyOnCreateContextMenuListener implements View.OnCreateContextMenuListener {
		private final static String TAG = "DWM";
		private final static String TARGET_VIEW_NAME = "ActionBarContextView";
		private final static boolean DBG = false;
		private final static int MAX_DEPTH = 5;
		private final static int DEFAULT_STATUSBAR_HEIGHT = 25;	//25dp

		static int sStatusBarHeight = -1;
		int mStatusBarHeight;

		WeakReference<GLView> mWrapperRef;

		public MyOnCreateContextMenuListener(GLView view) {
			if (sStatusBarHeight < 0) {
				DisplayMetrics metrics = view.getContext().getResources().getDisplayMetrics();
				sStatusBarHeight = Math.round(DEFAULT_STATUSBAR_HEIGHT * metrics.density);
			}

			mWrapperRef = new WeakReference<GLView>(view);
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, final View v, ContextMenuInfo menuInfo) {
			//4.0以上系统，菜单出现在屏幕顶部，会被状态栏盖住，需要调整菜单位置
			if (!OSVersion.hasIceCreamSandwich()) {
				return;
			}
			
			mStatusBarHeight = sStatusBarHeight;
			GLView glView = mWrapperRef.get();
			if (glView != null) {
				GLContentView rootView = glView.getGLRootView();
				if (rootView != null) {
					ViewGroup overlayViewGroup = rootView.getOverlayedViewGroup();
					if (overlayViewGroup != null) {
						mStatusBarHeight = overlayViewGroup.getTop();
					}
				}
			}

			//非全屏状态下无需调整菜单位置
			if (mStatusBarHeight <= 0) {
				return;
			}

			v.post(new Runnable() {
				public void run() {
					View view = v.getRootView();
					if (view != null) {
						adjustContextView(view, 0, MAX_DEPTH);
					}
				}
			});

		}

		boolean adjustContextView(View view, int depth, int maxDepth) {
			if (view == null) {
				return false;
			}
			if (view.getTag() == GLContentView.OVERLAY_VIEWGROUP_TAG) {
				return false;
			}
			
			String info = view.toString();
			if (DBG) {
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < depth; ++i) {
					builder.append("\t");
				}
				builder.append(info);
				Log.d(TAG, builder.toString());
			}

			if (info.contains(TARGET_VIEW_NAME)) {
				//found it, and make it downwards
				LayoutParams layoutParams = view.getLayoutParams();
				if (layoutParams instanceof MarginLayoutParams) {
					MarginLayoutParams marginLayoutParams = (MarginLayoutParams) layoutParams;
					marginLayoutParams.topMargin = mStatusBarHeight;
				}
				return true;
			}

			if (depth < maxDepth && view instanceof ViewGroup) {
				ViewGroup viewGroup = (ViewGroup) view;
				for (int i = 0; i < viewGroup.getChildCount(); ++i) {
					if (adjustContextView(viewGroup.getChildAt(i), depth + 1, maxDepth)) {
						return true;
					}
				}
			}
			
			return false;
		}
	}
	
    /**
     * 
     * <br>类描述: 用于调试的EditText
     * <br>功能详细描述:
     * 
     * @author  dengweiming
     * @date  [2013-4-26]
     */
    private static class DebugEditText extends EditText {

		public DebugEditText(Context context) {
			super(context);
		}

		public DebugEditText(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}
		
		//需要重载的方法写在下面，调试完删掉
		
    }

}
