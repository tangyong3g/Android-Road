/**
		*@Copyright:Copyright (c) 2008 - 2100
		*@Company:SJS
		*/
package com.ty.example_unit_6;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ty.abtest.ABTest;
import com.sny.tangyong.androiddemo.R;

/**
*@Title:
*@Description:
*@Author:tangyong
*@Since:2015-1-14
*@Version:1.1.0
*/
public class ABTestActivity extends Activity implements OnClickListener {

	public List<User> list = new ArrayList<User>();
	public Button mBtn;
	public Context mContext;
	public static final int mAccount = 1000;
	public TextView mTxView;
	private LinearLayout mLy;
	private AbTaskSync mSync;
	StringBuffer sb = new StringBuffer();

	/***
	 * 
			*@Title:
			*@Description:
			*@Author:tangyong
			*@Since:2015-1-14
			*@Version:1.1.0
	 */
	class User {

		String userType;

		public User() {
		}

		public User(String type) {
			this.userType = type;
		}

		public String toString() {
			return userType;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO
		super.onCreate(savedInstanceState);
		initView();
	}

	private void initView() {

		mLy = new LinearLayout(this);
		mLy.setOrientation(LinearLayout.VERTICAL);

		mBtn = new Button(this);
		mBtn.setText(R.string.initCustomer);
		mBtn.setOnClickListener(this);

		mTxView = new TextView(this);

		mLy.addView(mBtn);
		mLy.addView(mTxView);

		setContentView(mLy);
	}

	@Override
	public void onClick(View v) {

		mSync = new AbTaskSync();
		mSync.execute(mAccount);

	}

	/**
			*@Title:
			*@Description:
			*@Author:tangyong
			*@Since:2015-1-14
			*@Version:1.1.0
	 */
	class AbTaskSync extends AsyncTask<Integer, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {
			int userAccount = params[0];

			for (int i = 0; i < userAccount; i++) {

				User user = new User();
				user.userType = ABTest.getInstance(mContext).genUser();
				list.add(user);

				//				try {
				//					Thread.currentThread().sleep(5);
				//				} catch (InterruptedException e) {
				//				}
				publishProgress((float) i / userAccount + "%");
				
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			mBtn.setText(values[0] + "\t percent");
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			//检查客户端分配的 Test 样本和正比率
			int account = mAccount;
			String type = getType().toString();
			
			sb.delete(0, sb.length());
						
			sb.append("构造用户总数用户" + account + "\n");
			sb.append("\n");
			sb.append("测试用户类型:\t" + type);

			mTxView.setText(result());
		}

		public String result() {

			StringBuffer sb = new StringBuffer();
			List<String> types = getType();

			for (int i = 0; i < types.size(); i++) {
				String typeTemp = types.get(i);
				int account = calculatePercentWithType(typeTemp);
				float percent = account / (float) mAccount;

				sb.append("用户类型:\t" + typeTemp);
				sb.append("构造总数:\t" + account);
				sb.append("占用比率:\t" + percent);

				sb.append("\n");
			}
			return sb.toString();
		}

		public int calculatePercentWithType(String type) {
			int result = 0;

			for (int i = 0; i < mAccount; i++) {

				User user = list.get(i);
				String typeT = user.userType;

				if (type.equals(typeT)) {
					result++;
				}
			}
			return result;
		}

		public List<String> getType() {

			List<String> types = new ArrayList<String>();
			for (int i = 0; i < mAccount; i++) {

				User user = list.get(i);
				String type = user.userType;

				if (!isExist(type, types)) {
					types.add(type);
				}
			}
			return types;
		}

		private boolean isExist(String type, List<String> original) {

			boolean result = false;

			for (int i = 0; i < original.size(); i++) {

				String exStr = original.get(i);

				if (TextUtils.isEmpty(exStr))
					continue;

				if (!TextUtils.isEmpty(type) && exStr.equals(type)) {
					return true;
				}
			}
			return result;
		}
	}

	@Override
	protected void onPause() {
		// TODO
		super.onPause();
		mSync.cancel(true);
	}

}
