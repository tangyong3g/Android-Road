package com.graphics.engine.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.graphics.engine.util.FontStyleManager;
import com.graphics.engine.view.GLViewWrapper;

/**
 * 
 * <br>类描述: TextView的封装器
 * <br>功能详细描述:
 * <br>对于较少用到的接口，可以调用 {@link #getTextView()} 获得封装的 TextView，再调用。
 * <br>绘图缓冲默认不常驻内存，以节省内存，如果要绘制大块的文字，或者经常更新文字，
 * 可以使用{@link #setPersistentDrawingCache(boolean)}。
 * <br>需要交互式编辑，请使用子类{@link GLEditText}。
 * 
 * @author  dengweiming
 * @date  [2013-9-22]
 */
public class GLTextView extends GLViewWrapper {
	/** 设为true的时候，将使用重载的TextView，可以在里面重载方法，加断点或者打印信息，以便调试*/
	static final boolean DBG_TEXTVIEW = false;

	TextView mTextView;

	/**
	 * 与文字阴影相关的常量
	 */
	private static final float SHADOW_LARGE_RADIUS = 1.5f;
	private static final float SHADOW_X_OFFSET = 0.2f;
	private static final float SHADOW_Y_OFFSET = 0.5f;
	private static final int SHADOW_LARGE_COLOR = 0xFF000000;

	public GLTextView(Context context) {
		super(context);
		initTextView(context);
		setView(mTextView, null);
		mTextView.getPaint().setAntiAlias(true);
	}

	public GLTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GLTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initTextView(context, attrs, defStyle);
		setView(mTextView, null);
		// 支持独立语言包
		int textResId = attrs == null ? 0 : attrs.getAttributeResourceValue(
				"http://schemas.android.com/apk/res/android", "text", 0);
		if (textResId > 0) {
			setText(textResId);
		}
		mTextView.getPaint().setAntiAlias(true);
	}
	
	void initTextView(Context context) {
		setPersistentDrawingCache(false);
		if (DBG_TEXTVIEW) {
			mTextView = new DebugTextView(context);
		} else {
			mTextView = new TextView(context);
		}
		mTextView.setTypeface(FontStyleManager.getFontStyle());
	}

	void initTextView(Context context, AttributeSet attrs, int defStyle) {
		setPersistentDrawingCache(false);
		if (DBG_TEXTVIEW) {
			mTextView = new DebugTextView(context, attrs, defStyle);
		} else {
			mTextView = new TextView(context, attrs, defStyle);
		}
		int visibility = mTextView.getVisibility();
		if (visibility != VISIBLE) {
			mTextView.setVisibility(VISIBLE);
			setVisibility(visibility);
		}
		mTextView.setTypeface(FontStyleManager.getFontStyle());
	}

	public void setText(CharSequence text) {
		if (null != getText() && getText().equals(text)) {
			return;
		}
		mTextView.setText(text);
	}
	
	public void setText(int resId) {
		mTextView.setText(resId);
	}

	public CharSequence getText() {
		return mTextView.getText();
	}

	public void setTextSize(float size) {
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
	}

	public void setTextColor(int color) {
		mTextView.setTextColor(color);
	}
	
	public void setTextColor(ColorStateList colors) {
		mTextView.setTextColor(colors);
	}

	public void setMaxLines(int maxlines) {
		mTextView.setMaxLines(maxlines);
	}
	
	public void setMinLines(int minLines) {
		mTextView.setMinLines(minLines);
	}

	public void setSingleLine() {
		mTextView.setSingleLine();
	}

	public void setGravity(int gravity) {
		mTextView.setGravity(gravity);
	}
	
	public void setBold(boolean isBold) {
		TextPaint paint = mTextView.getPaint();
		if (paint.isFakeBoldText() != isBold) {
			mTextView.getPaint().setFakeBoldText(isBold);
			mTextView.getPaint().setTypeface(
					Typeface.create(Typeface.DEFAULT, isBold ? Typeface.BOLD : Typeface.NORMAL));
			invalidateView();
		}
	}

	/**
	 * 
	 * @param width	以px为单位，调用者可能需要将dp转成px
	 */
	public void setMaxWidth(int width) {
		mTextView.setMaxWidth(width);
	}

	public void setEllipsize(TruncateAt where) {
		mTextView.setEllipsize(where);
	}

	/**
	 * 显示文字的阴影
	 */
	public void showTextShadow() {
		mTextView.setShadowLayer(SHADOW_LARGE_RADIUS, SHADOW_X_OFFSET, SHADOW_Y_OFFSET,
				SHADOW_LARGE_COLOR);
	}
	
	/**
	 * <br>功能简述: 关闭文字阴影
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void hideTextShadow() {
		mTextView.setShadowLayer(0, SHADOW_X_OFFSET, SHADOW_Y_OFFSET, SHADOW_LARGE_COLOR);
	}
	
	@Override
	public void cleanup() {
//		mTextView = null;
		super.cleanup();
	}
	
	/**
	 * 设置文字的背景drawable
	 * @param d
	 */
	public void setTextBackgroundDrawable(Drawable d) {
		mTextView.setBackgroundDrawable(d);
	}
	
	/**
	 * 设置padding
	 * */
	public void setTextPadding(int left, int top, int right, int bottom) {
		mTextView.setPadding(left, top, right, bottom);
	}
	
	/**
	 * <br>功能简述: 设置启用走马灯效果
	 * <br>功能详细描述:
	 * <br>注意: 这个方法会调用{@link #setPersistentDrawingCache(boolean)} 使绘图缓冲常驻，避免动画期间频繁申请和释放位图
	 * @param repeatLimt 滚动次数，-1表示无限次
	 */
	public void setMarqueeEnabled(int repeatLimit) {
		if (mTextView != null) {
			if (mTextView.getEllipsize() != TruncateAt.MARQUEE) {
				mTextView.setEllipsize(TruncateAt.MARQUEE);
			}
			mTextView.setFocusable(true);
			mTextView.setFocusableInTouchMode(true);
			mTextView.setHorizontallyScrolling(true);
			mTextView.setMarqueeRepeatLimit(repeatLimit);
			mTextView.setSingleLine(true);

			setPersistentDrawingCache(true);
			setFocusable(true);
			setFocusableInTouchMode(true);
		}
	}
	
	/**
	 * <br>功能简述: 返回内部封装的TextView
	 * <br>功能详细描述: 为了避免增加过多不常用的接口，还是将内部封装的TextView提供出来给使用者调用。
	 * 例如设置文字周围的位图的接口，但是不建议在需要频繁重绘时使用，可能会导致慢，因为使用了整个TextView的绘图缓冲来更新纹理的。
	 * <br>注意: 在调用过{{@link #cleanup()} 之后会返回null
	 * @return
	 */
    public TextView getTextView() {
    	return mTextView;
    }
    
    /**
     * 
     * <br>类描述: 用于调试的TextView
     * <br>功能详细描述:
     * 
     * @author  dengweiming
     * @date  [2013-4-26]
     */
    private static class DebugTextView extends TextView {

		public DebugTextView(Context context) {
			super(context);
		}

		public DebugTextView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public DebugTextView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}
		
		//需要重载的方法写在下面，调试完删掉
    }

}
