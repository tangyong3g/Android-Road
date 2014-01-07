package com.ty.example_unit_6.colorselector;

import com.example.android_begin_gl_3d.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;

/**
 * 
 * @author tang
 * 
 */
public class RectColorPickDialog extends Dialog {

	private final boolean debug = true;
	private final String TAG = "ColorPicker";

	Context context;
	private String title;// 标题
	private int mInitialColor;// 初始颜色

	/**
	 * 初始颜色黑色
	 * 
	 * @param context
	 * @param title
	 *            对话框标题
	 * @param listener
	 *            回调
	 */
	public RectColorPickDialog(Context context, String title) {
		this(context, Color.BLACK, title);
	}

	/**
	 * 
	 * @param context
	 * @param initialColor
	 *            初始颜色
	 * @param title
	 *            标题
	 * @param listener
	 *            回调
	 */
	public RectColorPickDialog(Context context, int initialColor, String title) {
		super(context);
		this.context = context;
		mInitialColor = initialColor;
		this.title = title;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager manager = getWindow().getWindowManager();
		int height = (int) (manager.getDefaultDisplay().getHeight() * 0.5f);
		int width = (int) (manager.getDefaultDisplay().getWidth() * 0.7f);
//		RectColorPicker myView = new RectColorPicker(context, height, width);
//		setContentView(myView);
		
		setContentView(R.layout.coloradjust);
		
		setTitle(title);
	}

	public RectColorPickDialog(Context context) {
		super(context);
	}

}
