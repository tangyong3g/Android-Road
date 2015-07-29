package com.sny.tangyong.basic;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * 
 * 
 * @author tangyong
 */
public class SyncTaskActivity extends Activity implements OnClickListener {

	LinearLayout mContainer;
	Button mBtn;
	Context mContext;
	AnsyTaskSub mTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this.getApplicationContext();

		initView();
		initComponent();
	}

	/**
	 * 
	 */
	private void initComponent() {

		mTask = new AnsyTaskSub();

	}

	private void initView() {

		mContainer = new LinearLayout(mContext);
		mContainer.setOrientation(LinearLayout.VERTICAL);

		mBtn = new Button(mContext);
		mBtn.setText(R.string.start);
		mContainer.addView(mBtn);
		mBtn.setOnClickListener(this);

		setContentView(mContainer);
	}

	/**
	 * 
	 * @author tangyong
	 *
	 */
	class AnsyTaskSub extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Runs on the UI thread before {@link #doInBackground}.
			mBtn.setText("onPreExecute");
		}
		
		@Override
		protected Integer doInBackground(Integer... params) {
			int index = 0;

			while (index < 100) {
				try {
					Thread.sleep(100);
					index++;
					//send the value to the UI thread and the UI component could show the change . 
					publishProgress(index);
				} catch (InterruptedException e) {
				}
			}
			return index;
		}
		
		

		@Override
		protected void onCancelled() {
			super.onCancelled();

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			mBtn.setText("value is :" + values[0]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			mBtn.setText("onPostExecute:\t"+result);
		}
		
	}

	@Override
	public void onClick(View v) {

		if (v == mBtn) {
			mTask.execute(100, 1, 1);
		}

	}

}
