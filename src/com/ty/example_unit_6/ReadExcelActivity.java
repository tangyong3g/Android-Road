package com.ty.example_unit_6;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ty.exsample_unit_4.Hsb;

import jxl.Sheet;
import jxl.Workbook;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * 
 * @author Administrator
 * 
 */
public class ReadExcelActivity extends Activity {

	String[] names = new String[] { "杨毅伟", "李世宁", "孔令发", "雷景林", "曹世超",
			"周达威", "马三兵", "沈星", "区永伦", "曹石磊", "翁汉良", "贺鹏飞", "诸葛秀英", "李晶", "黄伟锋","汤勇" };

	Map<String, List<Person>> datas = new HashMap<String, List<Person>>();
	// 排好的队列
	List<Person> sortArray = new ArrayList<ReadExcelActivity.Person>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 設置全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 取消標題
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		final SimpleView view = new SimpleView(this);

		setContentView(view);

		Thread t = new Thread() {

			public void run() {
				readExcel();
				view.postInvalidate();
			};
		};

		t.start();

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

		public SimpleView(Context context) {
			super(context);

			mPaint = new Paint();
			mPaint.setStrokeWidth(2);
			rect = new Rect();
			
			comparator = new PersonComparetor();

			width = com.ty.crashreport.Application.getInstance()
					.getScreenInfo().getmWidth();
			height = com.ty.crashreport.Application.getInstance()
					.getScreenInfo().getmHeight();

		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

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
					float bottom = height-10;

					Log.i("rect", "");

					rect.left = (int) left;
					rect.top = (int) top;
					rect.right = (int) right;
					rect.bottom = (int) bottom;

					canvas.drawRect(rect, mPaint);
					
					String text = name+":"+workTime;
					//绘制名字和时间
					canvas.drawText(text, left, top - 10, mPaint);
					
				}
			}

		}
	}

	public void readExcel() {

		try {
			InputStream is = getResources().getAssets().open("ex.xls");
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

				ArrayList<Person> arrayData;

				// 判断就存在
				if (!isExist(name)) {

					arrayData = new ArrayList<ReadExcelActivity.Person>();
					datas.put(name, arrayData);
					
					Person t = new Person();
					t.name = name;
					
					sortArray.add(t);

				} else {
					arrayData = (ArrayList<ReadExcelActivity.Person>) datas
							.get(name);
				}

				// 每一行对应一个人的数据
				Person p = new Person();

				String nameTemp = sheet.getCell(1, i).getContents();
				String timeStr = sheet.getCell(2, i).getContents();

				p.name = nameTemp;
				p.timeStr = timeStr;

				Log.i("data", p.toString());

				// 添加进来
				arrayData.add(p);
			}
			
			for(Person p : sortArray){
				
				String name =p.name;
				p.avg =calculateAvgForPerson(name);
				
			}
			
			book.close();
		} catch (Exception e) {
			e.printStackTrace();
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

		int lenght = names.length;

		for (int i = 0; i < lenght; i++) {

			String nameTemp = names[i];

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

		ArrayList<Person> array = (ArrayList<ReadExcelActivity.Person>) datas
				.get(name);

		int size = array.size();
		float totalTime = 0.0f;

		for (Person p : array) {

			totalTime += p.getTime();

		}

		result = totalTime / size;

		return result;
	}

	class Person {

		public String name;
		public float time;
		public String date;
		public float avg;

		public String timeStr;

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

	}

	class PersonComparetor implements Comparator<Person> {

		@Override
		public int compare(Person lhs, Person rhs) {
			
			if(lhs.avg > rhs.avg){
				return 1;
			}else if(lhs.avg == rhs.avg){
				return 0;
			}else{
				return -1;
			}
		}

	}

}
