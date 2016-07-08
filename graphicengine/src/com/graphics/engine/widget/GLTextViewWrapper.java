package com.graphics.engine.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 
 * <br>类描述: TextView 的封装类
 * <br>功能详细描述:
 * 由于之前的 GLTextViewWrapper 命名不好，在 xml 中不能直接写 TextView，
 * 所以增加 GLTextView，然后派生出 GLTextViewWrapper。
 * 不直接改 GLTextViewWrapper 的名字是为了兼容旧版的 widget 。
 * 
 * @author  dengweiming
 * @date  [2013-9-22]
 * @deprecated 使用{@link GLTextView}代替
 */
public class GLTextViewWrapper extends GLTextView {
	
	public GLTextViewWrapper(Context context) {
		super(context);
	}

	public GLTextViewWrapper(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GLTextViewWrapper(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}


}
