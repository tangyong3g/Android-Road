package com.sany.tangyong.engineoriginal;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * <br>类描述: 程序主入口
 * <br>功能详细描述: 包含多个测试样例的列表
 * 
 * @author  dengweiming
 * @date  [2013-5-29]
 */
public class Main extends ListActivity {
	public final static String KEY_CONTENT_VIEW = "ContentView";
	public final static String VIEW_NAME_PREFIX = "com.go.test.shellengine.";

	private List<String> mItemTitle = new ArrayList<String>();
	private List<String> mItemViewName = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addItems();
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.main_list, mItemTitle);
		setListAdapter(fileList);
		setContentView(R.layout.main);
	}

	private void addItem(String title, String viewName) {
		mItemTitle.add(title);
		if (viewName.indexOf('.') < 0) {
			mItemViewName.add(VIEW_NAME_PREFIX + viewName);
		} else if (viewName.indexOf(' ') >= 0) {
			mItemViewName.add(viewName.substring(viewName.indexOf(' ') + 1));
		} else {
			mItemViewName.add(viewName);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position >= 0 && position < mItemViewName.size()) {
			Intent intent = new Intent(this, BaseTestActivity.class);
			intent.putExtra(KEY_CONTENT_VIEW, mItemViewName.get(position));
			startActivity(intent);
		}
	}

	/**
	 * TODO: 在这里添加具体的GLView和名称
	 */
	void addItems() {
//		addItem("Scribble", ScribbleTestView.class.toString());
		addItem("MS3D Model", MS3DTestView.class.toString());
		addItem("Layer", LayerTestView.class.toString());
		addItem("Sprite and Particle", SpriteTestView.class.toString());
		addItem("Edge Detect vs Glow Outline", EdgeDetectTestView.class.toString());
		addItem("Convolution Filter", ConvolutionFilterTestView.class.toString());
		addItem("Color Matrix", ColorMatrixTestView.class.toString());
		addItem("Motion Filter", MotionFilterTestView.class.toString());
		addItem("AnimatorSet", AnimatorSetTestView.class.toString());
		addItem("Reverse Animation", ReverseAnimationTestView.class.toString());
		addItem("Slinding menu", SlidingMenuTestView.class.toString());
		addItem("Vertex Buffer Object", VBOTestView.class.toString());
		addItem("WrapMode", WrapModeTestView.class.toString());
		addItem("Path", PathTestView.class.toString());
		addItem("Value Animator", ValueAnimatorTestView.class.toString());
		addItem("Ray Intersect", RayTestView.class.toString());
		addItem("Virtual Track Ball", VirtualTrackBallTestView.class.toString());
		addItem("Math3D", Math3DTestView.class.toString());
		addItem("Cylinder Drag", CylinderDragTestView.class.toString());
		addItem("Drag", DragTestView.class.toString());
		addItem("Capture", CaptureTestView.class.toString());
		addItem("Edit Text", EditTextTestView.class.toString());
		addItem("Bezier Patch", BezierPatchTestView.class.toString());
		addItem("Twist Grid", TwistGridTestView.class.toString());
		addItem("Simple Cloth", SimpleClothTestView.class.toString());
		addItem("Rotate", RotateTestView.class.toString());
		addItem("Stencil Clip ", ClipTestView.class.toString());
		addItem("Glow Filter", GlowTestView.class.toString());
		addItem("Layout", LayoutTestView.class.toString());
	}
}
