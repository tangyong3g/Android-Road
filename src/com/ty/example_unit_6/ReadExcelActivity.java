package com.ty.example_unit_6;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.androiddemo.ScreenInfo;
import com.sny.tangyong.androiddemo.AndroidApplication;
import com.sny.tangyong.androiddemo.R;
import com.ty.util.Utils;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;

/**
 * 
 * @author Administrator
 * 
 */
public class ReadExcelActivity extends Activity {

	

	Map<String, List<Person>> datas = new HashMap<String, List<Person>>();
	// 排好的队列
	List<Person> sortArray = new ArrayList<ReadExcelActivity.Person>();

	ReadSyncTask mReadSyncTask;
	SimpleView mSimpleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 設置全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 取消標題
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSimpleView = new SimpleView(this);
		setContentView(mSimpleView);
		
		mReadSyncTask = new ReadSyncTask();
		mReadSyncTask.execute(0, 0, 0);
	}

	/**
	 * 
	 * @author Administrator
	 * 
	 */
	class SimpleView extends View {

		Paint mPaint;
		int width;
		int height;
		Rect rect;
		Comparator<Person> comparator;

		public float mShowPercent;

		public SimpleView(Context context) {
			super(context);

			mPaint = new Paint();
			mPaint.setStrokeWidth(2);
			rect = new Rect();

			comparator = new PersonComparetor();

			width = AndroidApplication.getInstance().getScreenInfo().getmWidth();
			height =AndroidApplication.getInstance().getScreenInfo().getmHeight();

		}

		public void drawProcess(int process, Canvas canvas) {

		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			//绘制进度
			if (mReadSyncTask != null && (mReadSyncTask.getStatus() == Status.RUNNING )) {

				mPaint.setColor(Color.BLACK);
				String text = mShowPercent + "%";
				ScreenInfo info = AndroidApplication.getInstance().getScreenInfo();
				int x = info.getmWidth() / 2;
				int y = info.getmHeight() / 2;
				canvas.drawText(text, x, y, mPaint);
			}

			mPaint.setColor(Color.BLUE);
			// 绘制Y轴
			canvas.drawLine(10, 0, 10, height, mPaint);

			mPaint.setColor(Color.RED);
			// 绘制X
			canvas.drawLine(10, height - 10, width, height - 10, mPaint);

			// 绘制Y上面的标尺 2小一个单位 共 24小时
			float heightDis = height / 12;

			for (int i = 11; i >= 0; i--) {

				mPaint.setColor(Color.RED);

				int startX = 10;
				float startY = (i + 1) * heightDis;
				int endX = startX + 8;
				float endY = startY;

				canvas.drawLine(startX, startY, endX, endY, mPaint);

				mPaint.setColor(Color.BLUE);
				int tV = (11 - i) * 2;

				String text = "(" + tV + " H" + ")";
				canvas.drawText(text, endX + 5, endY + 5, mPaint);

			}

			//处理完成才开始绘制
			if (mReadSyncTask != null && mReadSyncTask.getStatus() == Status.FINISHED) {

				if (sortArray != null && sortArray.size() > 0) {

					Collections.sort(sortArray, comparator);

					int size = datas.size();
					int index = 0;
					float widhtPer = width / size;

					for (Person personTemp : sortArray) {

						String name = personTemp.name;
						float workTime = personTemp.avg;
						workTime -= 9.5;
						float resultHeight = workTime * height / 24;

						Log.i("data_a", workTime + "" + name);

						float left = (index++) * widhtPer + 10;
						float top = height - resultHeight - 10;
						float right = left + widhtPer / 2;
						float bottom = height - 10;

						Log.i("rect", "");

						rect.left = (int) left;
						rect.top = (int) top;
						rect.right = (int) right;
						rect.bottom = (int) bottom;

						canvas.drawRect(rect, mPaint);

						String text = name + ":" + workTime;
						//绘制名字和时间
						canvas.drawText(text, left, top - 10, mPaint);

					}
				}
			}

		}
	}
	public boolean isExist(String name) {

		boolean result = false;
		result = datas.containsKey(name);
		return result;
	}

	public boolean isRowAva(String timeStr) {

		if (TextUtils.isEmpty(timeStr))
			return false;

		return timeStr.contains("下午");

	}

	public boolean isPersonAva(String name) {

		if (TextUtils.isEmpty(name))
			return false;

		boolean result = false;

		int lenght = Utils.names.length;

		for (int i = 0; i < lenght; i++) {

			String nameTemp = Utils.names[i];

			if (nameTemp.equals(name)) {
				result = true;
				return result;
			}
		}

		return result;
	}

	public float calculateAvgForPerson(String name) {

		if (datas.size() == 0) {
			return 0.0f;
		}

		float result = 0;

		ArrayList<Person> array = (ArrayList<ReadExcelActivity.Person>) datas.get(name);

		int size = array.size();
		float totalTime = 0.0f;
		

		for (Person p : array) {

			totalTime += p.getTime();

		}

		result = totalTime / size;

		return result;
	}
	
	
	public float calculateMorAvgForPerson(String name) {

		if (datas.size() == 0) {
			return 0.0f;
		}

		float result = 0;

		ArrayList<Person> array = (ArrayList<ReadExcelActivity.Person>) datas.get(name);

		int size = array.size();
		float totalTime = 0.0f;
		

		for (Person p : array) {
			
			totalTime += p.getMorTime();
			
		}

		result = totalTime / size;

		return result;
	}

	class Person {

		public String name;
		public float time;
		public String date;
		public float avg;
		public float mor_avg;

		public String timeStr;
		
		
		public float getMorTime(){
			
			if (TextUtils.isEmpty(timeStr)) {
				return 0.0f;
			}
			int afterNindex = timeStr.indexOf("上午");
			String str = timeStr.substring(afterNindex + 3, timeStr.length());

			String hourStr = str.substring(0, 2);
			String minStr = str.substring(3, 5);

			int hour = Integer.parseInt(hourStr) + 12;
			float min = Integer.parseInt(minStr) / (float) 60;

			return hour + min;
		}

		public float getTime() {

			if (TextUtils.isEmpty(timeStr)) {
				return 0.0f;
			}
			int afterNindex = timeStr.indexOf("下午");
			String str = timeStr.substring(afterNindex + 3, timeStr.length());

			String hourStr = str.substring(0, 2);
			String minStr = str.substring(3, 5);

			int hour = Integer.parseInt(hourStr) + 12;
			float min = Integer.parseInt(minStr) / (float) 60;

			return hour + min;
		}

		public String toString() {

			return name + getTime();
		}

		public boolean isWeekDay() {
			boolean result = true;

			if (TextUtils.isEmpty(timeStr)) {
				result = true;
			}

			CharSequence dataStr = timeStr.subSequence(0, timeStr.indexOf("下午"));
			Date d = StringToDate(dataStr.toString(), "yyyy-MM-dd");

			if (d.getDay() == 0 || d.getDay() == 6) {
				result = false;
			}

			Log.i("testdata", timeStr + "\t" + result);
			return result;
		}

		public Date StringToDate(String dateStr, String formatStr) {
			DateFormat sdf = new SimpleDateFormat(formatStr);
			Date date = null;
			try {
				date = sdf.parse(dateStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return date;
		}

	}

	class PersonComparetor implements Comparator<Person> {

		@Override
		public int compare(Person lhs, Person rhs) {

			if (lhs.avg > rhs.avg) {
				return 1;
			} else if (lhs.avg == rhs.avg) {
				return 0;
			} else {
				return -1;
			}
		}
	}

	class ReadSyncTask extends AsyncTask<Integer, Float, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Toast.makeText(ReadExcelActivity.this.getApplicationContext(), R.string.read_start, Toast.LENGTH_LONG).show();
		}

		@Override
		protected Integer doInBackground(Integer... params) {

			try {
				InputStream is = getResources().getAssets().open("11.xls");
				Workbook book = Workbook.getWorkbook(is);

				book.getNumberOfSheets();
				// 获得第一个工作表对象
				Sheet sheet = book.getSheet(0);
				int Rows = sheet.getRows();
				int Cols = sheet.getColumns();

				for (int i = 0; i < Rows; ++i) {

					String name = sheet.getCell(1, i).getContents();
					String time = sheet.getCell(2, i).getContents();

					// 判断用户有效性
					if (!isPersonAva(name) || !isRowAva(time)) {
						continue;
					}

					publishProgress((float) i / Rows);

					ArrayList<Person> arrayData;

					// 判断就存在
					if (!isExist(name)) {

						arrayData = new ArrayList<ReadExcelActivity.Person>();
						datas.put(name, arrayData);

						Person t = new Person();
						t.name = name;

						sortArray.add(t);

					} else {
						arrayData = (ArrayList<ReadExcelActivity.Person>) datas.get(name);
					}

					// 每一行对应一个人的数据
					Person p = new Person();

					String nameTemp = sheet.getCell(1, i).getContents();
					String timeStr = sheet.getCell(2, i).getContents();

					p.name = nameTemp;
					p.timeStr = timeStr;

					if (p.isWeekDay()) {
						// 添加进来
						arrayData.add(p);

					}
				}

				for (Person p : sortArray) {

					String name = p.name;
					p.avg = calculateAvgForPerson(name);
					p.mor_avg = calculateMorAvgForPerson(name);
				}

				book.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Float... values) {
			// TODO
			super.onProgressUpdate(values);

			if (mSimpleView != null) {

				mSimpleView.invalidate();
				mSimpleView.mShowPercent = values[0];

			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			if (mSimpleView != null) {
				mSimpleView.invalidate();
			}
			
			Toast.makeText(ReadExcelActivity.this.getApplicationContext(), R.string.read_finish, Toast.LENGTH_LONG).show();
		}

	}

}
