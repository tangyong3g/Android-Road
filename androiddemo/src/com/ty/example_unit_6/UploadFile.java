package com.ty.example_unit_6;

import java.io.File;

import com.sny.tangyong.androiddemo.R;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class UploadFile extends Activity implements OnClickListener {

	LinearLayout mContainer;
	Button mbtn;
	String url = "http://192.168.217.190:8080/UploadFile/servlet/UpFileServlet";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContainer = new LinearLayout(this.getApplicationContext());
		mContainer.setOrientation(LinearLayout.HORIZONTAL);

		mbtn = new Button(this.getApplicationContext());
		mbtn.setText(R.string.center);
		mbtn.setOnClickListener(this);

		mContainer.addView(mbtn);
		setContentView(mContainer);
	}

	@Override
	public void onClick(View v) {

		if (v == mbtn) {
			AjaxParams params = new AjaxParams();
			//			params.put("username", "michael yang");
			//			params.put("password", "123456");
			params.put("email", "test@tsz.net");
			//			String filePath = Environment.getExternalStorageDirectory() + "/2222.exe";
			String filePath = Environment.getExternalStorageDirectory() + "/DCIM//Camera/123.jpg";
			try {
				params.put("profile_picture", new File(filePath)); // 上传文件
			} catch (Exception e) {
			}

			FinalHttp fh = new FinalHttp();

			AjaxCallBack<Object> ajax = new AjaxCallBack<Object>() {

				@Override
				public void onStart() {
					super.onStart();
					Log.i("pro", "start");
				}

				@Override
				public void onLoading(long count, long current) {
					super.onLoading(count, current);
					Log.i("pro", "percent" + current);
				}

				@Override
				public void onFailure(Throwable t, int errorNo, String strMsg) {
					super.onFailure(t, errorNo, strMsg);
					Log.i("pro", "error message" + strMsg);
				}
			};
			ajax.progress(true, 10);
			fh.post(url, params, ajax);
		}

	}

}
