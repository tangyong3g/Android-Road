package com.sany.tangyong.engineoriginal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.go.gl.graphics.BitmapGLDrawable;
import com.go.gl.view.GLLinearLayout;
import com.go.gl.widget.GLImageView;
import com.go.gl.widget.GLTextViewWrapper;

/**
 * 
 * <br>类描述: 测试图片平铺绘制效果
 * <br>功能详细描述:
 * 
 * @author  dengweiming
 * @date  [2013-7-29]
 */
public class WrapModeTestView extends GLLinearLayout {
	
	public WrapModeTestView(Context context) {
		super(context);
		setOrientation(GLLinearLayout.VERTICAL);
		
		Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.apple)).getBitmap();
		bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);	//CHECKSTYLE IGNORE
		
		GLTextViewWrapper textViewWrapper1 = new GLTextViewWrapper(context);
		textViewWrapper1.setText("clamp to edge");
		addView(textViewWrapper1);
		GLImageView imageView1 = new GLImageView(context);
		BitmapGLDrawable drawable1 = new BitmapGLDrawable(getResources(), bitmap);
		imageView1.setImageDrawable(drawable1);
		addView(imageView1);
		
		
		GLTextViewWrapper textViewWrapper2 = new GLTextViewWrapper(context);
		textViewWrapper2.setText("repeat");
		addView(textViewWrapper2);
		GLImageView imageView2 = new GLImageView(context);
		BitmapGLDrawable drawable2 = new BitmapGLDrawable(getResources(), bitmap);
		drawable2.setWrapMode(BitmapGLDrawable.WRAP_REPEAT, BitmapGLDrawable.WRAP_REPEAT);	//
		drawable2.setTexCoord(0, 0, 2, 2);	//
		imageView2.setImageDrawable(drawable2);
		addView(imageView2);
		
		
		GLTextViewWrapper textViewWrapper3 = new GLTextViewWrapper(context);
		textViewWrapper3.setText("mirrored repeat");
		addView(textViewWrapper3);
		GLImageView imageView3 = new GLImageView(context);
		BitmapGLDrawable drawable3 = new BitmapGLDrawable(getResources(), bitmap);
		drawable3.setWrapMode(BitmapGLDrawable.WRAP_MIRRORED_REPEAT, BitmapGLDrawable.WRAP_MIRRORED_REPEAT);	//
		drawable3.setTexCoord(0, 0, 2, 2);	//
		imageView3.setImageDrawable(drawable3);
		addView(imageView3);
	}
	

}
