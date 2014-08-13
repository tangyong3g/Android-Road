package com.ty.exsample_unit_4;

import java.lang.reflect.Constructor;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ty.exsample.R;

/**
 * 
 * @author tang
 * 
 */
public class BaseViewActivity extends Activity {

	public static final String BASE_FLAG = "base_flag";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bun = getIntent().getExtras();

		View simpleView = null;

		if (bun != null) {
			String view_name = bun.getString(BASE_FLAG);

			if (view_name != null && view_name.equals("")) {

				try {

					Class instance_c = getClassLoader().loadClass(view_name);

					@SuppressWarnings({ "unchecked", "rawtypes" })
					Constructor constructor = instance_c
							.getConstructor(Context.class);
					Object object = constructor.newInstance(this);

					if (object instanceof View) {

						simpleView = (View) object;

					}

				} catch (Exception e) {
				}
			}
		}

		if (simpleView != null) {

			setContentView(simpleView);

		} else {

			setContentView(R.layout.report);
		}

	}
}
