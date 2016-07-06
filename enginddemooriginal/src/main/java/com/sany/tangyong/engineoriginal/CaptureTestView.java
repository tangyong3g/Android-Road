package com.sany.tangyong.engineoriginal;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.go.gl.graphics.GLCanvas;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.view.GLView;

/**
 * 测试将GLView截图保存到sdcard
 */
public class CaptureTestView extends GLLinearLayout {

	boolean mToSaveCapture;
	GLView mView;
	String mFileSavePath = "/capture.png";

	public CaptureTestView(Context context) {
		super(context);

		mView = new CubeGLView(context);

		//		mView = new GLImageView(context);
		//		((GLImageView)mView).setImageResource(R.drawable.apple);

		addView(mView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		mView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(GLView v) {
				mToSaveCapture = true;
				invalidate();
			}
		});

		Toast toast = Toast.makeText(context, "Click to capture...", Toast.LENGTH_SHORT);
		toast.show();
	}

	@Override
	protected void dispatchDraw(GLCanvas canvas) {
		super.dispatchDraw(canvas);
		if (mToSaveCapture) {
			mToSaveCapture = false;
			mView.setDrawingCacheEnabled(true);
			mView.saveDrawingCacheToBitmap(canvas, new GLView.OnBitmapCapturedListener() {

				@Override
				public void onBitmapCaptured(Bitmap bitmap) {
					if (bitmap != null) {
						GLCanvas.saveBitmap(bitmap, Environment.getExternalStorageDirectory() + mFileSavePath);
						Toast toast = Toast.makeText(getContext(), "Saved to /sdcard" + mFileSavePath, Toast.LENGTH_SHORT);
						toast.show();
					}

				}
			});
			mView.setDrawingCacheEnabled(false);
		}
	}

}
