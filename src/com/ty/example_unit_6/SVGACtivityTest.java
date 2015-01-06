package com.ty.example_unit_6;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.ty.exsample.R;

/**
 * 
		*@Title:
		*@Description:
		*@Author:tangyong
		*@Since:2014-12-30
		*@Version:1.1.0
 */
public class SVGACtivityTest extends Activity {

	SVG mSVG;
	SVGView mView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO
		super.onCreate(savedInstanceState);
		setContentView(R.layout.svg);
		//		loadSVG();
		//
		//		//		Picture pic = loadSVG();
		//		// Create a new ImageView
		//		ImageView imageView = new ImageView(this);
		//		// Set the background color to white
		//		imageView.setBackgroundColor(Color.WHITE);
		//		// Parse the SVG file from the resource
		//		SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.gradients);
		//		// Get a drawable from the parsed SVG and set it as the drawable for the ImageView
		//		imageView.setImageDrawable(svg.createPictureDrawable());
		//		// Set the ImageView as the content view for the Activity
		//		setContentView(imageView);
		//
		//		//		SVGView view = new SVGView(this);
		//		//		setContentView(view);
		//		//		view.invalidate();

		showTwo();
	}

	/**
	 *  it just support less 3.0 version 
	  * 
	  * @Description:
	 */
	private void showTwo() {

		SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.slice2);
		BitmapDrawable drawable = svg.createBitmapDrawable(this);

		ImageView imageView = (ImageView) findViewById(R.id.img_show);
		imageView.setLayoutParams(new LayoutParams(185, 185));
		imageView.setBackgroundDrawable(drawable);

//		setContentView(imageView);

	}

	private Picture loadSVG() {

		
		try {

			mSVG = SVGParser.getSVGFromAsset(getAssets(), "data/svg/gradients.svg");

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (mSVG != null) {
			return mSVG.getPicture();
		} else {
			return null;
		}
	}

	class SVGView extends View {

		Picture mPic;

		public SVGView(Context context) {
			super(context);

		}

		public void setSVGView(Picture picture) {
			mPic = picture;
		}

		public SVGView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void draw(Canvas canvas) {
			// Parse the SVG file from the resource
			// Get the picture

			canvas.save();
			Picture picture = mSVG.getPicture();
			// Draw picture in canvas
			// Note: use transforms such as translate, scale and rotate to position the picture correctly
			canvas.drawPicture(picture);
			Log.i("cycle", "Test");
			canvas.restore();
		}

	}

}
