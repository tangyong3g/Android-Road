package com.ty.example_unit_6.colorselector;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.ty.exsample.R;

/**
 * 
 * @author tang
 * 
 */
public class RectColorActivity extends Activity implements OnClickListener {

	private Dialog mDialog;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager manager = getWindow().getWindowManager();
		int height = (int) (manager.getDefaultDisplay().getHeight());
		int width = (int) (manager.getDefaultDisplay().getWidth());

		setContentView(R.layout.colorpick);

		Button btn = (Button) findViewById(R.id.btn_choice_color);
		btn.setOnClickListener(this);
		mContext = this;
	}

	@Override
	public void onClick(View v) {

		mDialog = new RectColorPickDialog(mContext, Color.BLACK,getResources().getString(R.string.app_name));
		Window window = mDialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = 0.2f;
		window.setAttributes(lp);
		
		mDialog.show();

	}

}
