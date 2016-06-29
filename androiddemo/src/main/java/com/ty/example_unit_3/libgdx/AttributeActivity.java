package com.ty.example_unit_3.libgdx;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;

/**
 * 
 * 本Demo要验证的问题有 １：　深度测试 ２：　混合方式。GL_COLOR 和GL_APL 3: 要验证的是背面裁剪的问题。
 * 
 * @author tangyong
 * 
 */
public class AttributeActivity extends AndroidApplication {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AttributeListenr listener = new AttributeListenr();
		initialize(listener, true);
	}


}
