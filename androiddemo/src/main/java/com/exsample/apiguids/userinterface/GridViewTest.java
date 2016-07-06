package com.exsample.apiguids.userinterface;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.sny.tangyong.androiddemo.R;

/**
		*@Title:
		*@Description:
		*@Author:tangyong
		*@Since:2015-1-5
		*@Version:1.1.0
 */
public class GridViewTest extends Activity {

	GridView view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gridview);

		view = (GridView) findViewById(R.id.gridview_id);
		view.setAdapter(new ImageViewAdapter(this));

		view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Toast.makeText(GridViewTest.this, "" + position, Toast.LENGTH_SHORT).show();
			}

		});
	}
	/**
	 * 
			*@Title:
			*@Description:
			*@Author:tangyong
			*@Since:2015-1-5
			*@Version:1.1.0
	 */
	class ImageViewAdapter extends BaseAdapter {

		Context mContext;

		private Integer[] mThumbIds = { R.drawable.bg, R.drawable.bg_one, R.drawable.click_sq, R.drawable.dest, R.drawable.dest, R.drawable.cube_texture, R.drawable.cube_simple,
				R.drawable.dest, R.drawable.error_1, R.drawable.ground_texture, R.drawable.ghxp, R.drawable.gd, R.drawable.right_1, R.drawable.right_3,
				R.drawable.unit6_seekbar_setting_dialog_bg, R.drawable.unit6_seekbar_setting_dialog_bg, R.drawable.unit6_seekbar, R.drawable.unit6_seekbar_line2, R.drawable.mask,
				R.drawable.src };

		public ImageViewAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			return mThumbIds.length;
		}
		@Override
		public Object getItem(int position) {
			return mThumbIds[position];
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ImageView imageView;

			if (convertView == null) {

				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
				imageView.setPadding(8, 8, 8, 8);
			} else {
				imageView = (ImageView) convertView;
			}

			imageView.setImageResource(mThumbIds[position]);

			return imageView;
		}
	}

}
