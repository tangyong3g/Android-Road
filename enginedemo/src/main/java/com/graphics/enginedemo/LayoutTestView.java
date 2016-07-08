package com.graphics.enginedemo;

import com.graphics.engine.animation.Animation;
import com.graphics.engine.animation.InterpolatorFactory;
import com.graphics.engine.animation.Transformation3D;
import com.graphics.engine.view.GLFrameLayout;
import com.graphics.engine.view.GLLayoutInflater;
import com.graphics.engine.view.GLView;
import com.graphics.engine.view.GLViewGroup;
import com.graphics.engine.widget.GLListAdapter;
import com.graphics.engine.widget.GLListView;
import com.graphics.engine.widget.GLTextViewWrapper;

import android.content.Context;
import android.database.DataSetObserver;


/**
 * 
 */
public class LayoutTestView extends GLFrameLayout {

	public LayoutTestView(Context context) {
		super(context);

		GLViewGroup group = new ScribbleView(getContext());
		addView(group);

		GLLayoutInflater inflater = GLLayoutInflater.from(getContext());

		GLView ninePatchiew = inflater.inflate(R.layout.test_ninepatch, null);
		group.addView(ninePatchiew);

		GLView view = inflater.inflate(R.layout.test, null);
		group.addView(view);

		group.addView(new ShapeView(getContext()));
		group.addView(new AppIconTestView(getContext()));
		group.addView(createListView(getContext()));

		GLView textView = findViewById(R.id.textview_wrapper);
		textView.startAnimation(createAnimation());

	}

	private GLListView createListView(Context context) {
		GLListView listView = new GLListView(context);
		listView.setDivider(getResources().getDrawable(R.drawable.listview_divider));
		//		listView.setScrollingCacheEnabled(true);
		listView.setAdapter(new GLListAdapter() {

			private final String[] mStrings = new String[] { "a", "bb", "ccc", "dddd", "a", "bb", "ccc", "dddd", "a", "bb", "ccc", "dddd",
					"a", "bb", "ccc", "dddd", "a", "bb", "ccc", "dddd", "a", "bb", "ccc", "dddd", };

			@Override
			public void unregisterDataSetObserver(DataSetObserver observer) {

			}

			@Override
			public void registerDataSetObserver(DataSetObserver observer) {

			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public boolean hasStableIds() {
				return false;
			}

			@Override
			public int getViewTypeCount() {
				return 1;
			}

			@Override
			public GLView getView(int position, GLView convertView, GLViewGroup parent) {
				if (position < 0 || position >= getCount()) {
					return null;
				}
				if (convertView == null) {
					GLLayoutInflater inflater = GLLayoutInflater.from(getContext());
					convertView = inflater.inflate(R.layout.list_item, null);
				}
				GLTextViewWrapper textView = (GLTextViewWrapper) convertView.findViewById(R.id.list_item_text);
				textView.setPersistentDrawingCache(true);
				textView.setText(mStrings[position]);
				textView.setBackgroundColor(position % 2 == 0 ? 0x7fff0000 : 0x7f00ff00);
				return convertView;
			}

			@Override
			public int getItemViewType(int position) {
				return 1;
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

			@Override
			public Object getItem(int position) {
				return null;
			}

			@Override
			public int getCount() {
				return mStrings.length;
			}

			@Override
			public boolean isEnabled(int position) {
				return position >= 2;
			}

			@Override
			public boolean areAllItemsEnabled() {
				return false;
			}
		});

		return listView;
	}

	private Animation createAnimation() {
		//以视图中心为原点旋转的3D动画
		final float angleFrom = 0, angleTo = 30, axisX = 0, axisY = 1, axisZ = 0; //构造参数
		Animation animation = new Animation() {
			float mCenterX;
			float mCenterY;
			Transformation3D mTempTransformation3d = new Transformation3D();

			@Override
			protected void applyTransformation(float interpolatedTime, Transformation3D t) {
				final float angle = angleFrom + (angleTo - angleFrom) * interpolatedTime;
				t.setTranslate(mCenterX, mCenterY, 0);
				mTempTransformation3d.clear();
				mTempTransformation3d.setRotateAxisAngle(angle, axisX, axisY, axisZ);
				t.compose(mTempTransformation3d);
				mTempTransformation3d.clear();
				mTempTransformation3d.setTranslate(-mCenterX, -mCenterY, 0);
				t.compose(mTempTransformation3d);
			}

			@Override
			public void initialize(int width, int height, int parentWidth, int parentHeight) {
				super.initialize(width, height, parentWidth, parentHeight);
				mCenterX = width * 0.5f;
				mCenterY = height * -0.5f; //用于3D变换，Y轴和2D的相反
			}
		};
		animation.setInterpolator(InterpolatorFactory.getInterpolator(InterpolatorFactory.CYCLE_FORWARD, 0));

		animation.setDuration(1500);
		animation.setRepeatCount(Animation.INFINITE);
		animation.setFillAfter(true);

		return animation;
	}
}
