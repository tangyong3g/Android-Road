package com.graphics.enginedemo.simple;

import com.graphics.engine.GLActivity;
import com.graphics.engine.view.GLContentView;
import com.graphics.engine.view.GLLayoutInflater;
import com.graphics.engine.view.GLView;
import com.graphics.engine.view.GLViewGroup;
import com.graphics.enginedemo.R;

import android.os.Bundle;
import android.widget.FrameLayout;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  chaoziliang
 * @date  [2012-9-7]
 */
public class GoLauncher3DTestActivity extends GLActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GLContentView glContentView = new GLContentView(this, true);
		glContentView.setOverlayedViewGroup(new FrameLayout(this));
		setSurfaceView(glContentView, true);
		

		GLViewGroup group = new ScribbleView(this);
		setContentGlView(group);

		GLLayoutInflater inflater = GLLayoutInflater.from(this);

		GLView ninePatchiew = inflater.inflate(R.layout.test_ninepatch, null);
		group.addView(ninePatchiew);

		GLView view = inflater.inflate(R.layout.test, null);
		group.addView(view);

	}
}
