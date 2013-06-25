package com.ty.example_unit_6.seekbar;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.android_begin_gl_3d.R;


/**
 * 
 * 设置项弹出窗
 * 
 * @author tangyong
 * 
 */
//CHECKSTYLE:OFF
public class DockLineDialog extends Dialog implements OnSeekBarChangeListener,
		android.view.View.OnClickListener {

	public Context mContext;
	//布局
	public LinearLayout mDialogLayout;
	//滑动条的值
	private TextView mValueTextView;
	//滑动条
	private SeekBar mSeekBar;
	private int mProgress;
	// 窗口标题
	private String mDialogTitle;
	// 一行代表的最大数据
	private int mMaxCount = 10;
	// 最小数据
	private int mMinCount = 1;
	private final static int DOT_COUNT = 10;
	//存储设定值的KEY值
	private String mKey = "";
	private int mSettingDockCountIndexChange;
	//弹出窗口的上一层控件
//	private DeskSettingItemBaseView mParentView;
	//默认值
	private String mDefaultValue;
	
	private ArrayList<OnValueChangeListener> mOnValueChangeListenerArray  = new ArrayList<OnValueChangeListener>();
	
	public void addListener(OnValueChangeListener listener){
		mOnValueChangeListenerArray.add(listener);
	}
	
	
	public String getmDefaultValue() {
		return mDefaultValue;
	}

	public void setmDefaultValue(String mDefaultValue) {
		this.mDefaultValue = mDefaultValue;
	}

//	public DeskSettingItemBaseView getmParentView() {
//		return mParentView;
//	}

//	public void setmParentView(DeskSettingItemBaseView mParentView) {
//		this.mParentView = mParentView;
//	}

	public String getmKey() {
		return mKey;
	}

	public void setmKey(String mKey) {
		this.mKey = mKey;
	}

	public void setmMaxCount(int mMaxCount) {
		this.mMaxCount = mMaxCount;
	}

	public void setmMinCount(int mMinCount) {
		this.mMinCount = mMinCount;
	}
	
	public DockLineDialog(Context context, int dialogType, String dialogTitle) {
		super(context, R.style.SettingDialog);
		mContext = context;
		//弹出窗标题
		this.mDialogTitle = dialogTitle;
	}

	public DockLineDialog(Context context, int dialogType, String dialogTitle,String defaultValue) {
		super(context, R.style.SettingDialog);
		mContext = context;
		//弹出窗标题
		this.mDialogTitle = dialogTitle;
		this.mDefaultValue = defaultValue;
	}


	public DockLineDialog(Context context, int theme) {
		super(context, R.style.SettingDialog);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (null == mContext) {
			return;
		}

		View view = getView();
		if (null != view) {
//			DialogBase.setDialogWidth(mDialogLayout, mContext);
			setContentView(view);
		}
	}

	public void setProgress() {
		if (null != mSeekBar) {
			// mSeekBar.setProgress(toSeekBarData(mSettingDockCountIndexChange));
		}
	}

	@Override
	public void onClick(View v) {

		int id = v.getId();
		switch (id) {
		case R.id.center:
			center();
			break;
		case R.id.cancle:
			cancle();
			break;
		default:
			break;
		}
	}
	
	

	/**
	 * 确定
	 */
	private void center() {
		dismiss();
		
		mSettingDockCountIndexChange = toRealData(mSeekBar.getProgress());
		String value = String.valueOf(mSettingDockCountIndexChange);
//		mParentView.setSummaryText(value);
//		PreferenceManager.getInstance().putString(mKey, value);
		
//		for(OnValueChangeListener listener : mOnValueChangeListenerArray){
//			listener.onSeekBarValueChange(mParentView, value);
//		}
		
	}
	

	/**
	 * 取消
	 */
	private void cancle() {
		dismiss();
	}
	
	

	
	/**
	 * 初始化的时候创建 View
	 * @return
	 */
	private View getView() {
		
		int value = 0;
		
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.unit6_seekbar_one, null);
		mDialogLayout = (LinearLayout) view.findViewById(R.id.dialog_layout);

		// 设置标题
		TextView dialogTitleTextView = (TextView) view.findViewById(R.id.desk_setting_dialog_singleormulti_title);
		dialogTitleTextView.setText(mDialogTitle);
		
		//设置滑动条
		mSeekBar = (SeekBar) view.findViewById(R.id.mRowBar);
		mSeekBar.setMax((mMaxCount - mMinCount) * DOT_COUNT);
		mSeekBar.setProgress(toSeekBarData(value));
		mSeekBar.setOnSeekBarChangeListener(this);

		//设置滑动条的数值
		mValueTextView = (TextView) view.findViewById(R.id.rowActualValue);
		mValueTextView.setText(String.valueOf(value));

		//设置确定和取消的事件监听
		Button okButton = (Button) view.findViewById(R.id.center);
		okButton.setOnClickListener(this);
		Button cancelButton = (Button) view.findViewById(R.id.cancle);
		cancelButton.setOnClickListener(this);

		return view;
	}
	
	public void setProcessValue(){
//		int value = Integer.parseInt(PreferenceManager.getInstance().getString(mKey, mDefaultValue));
		int value = 0;
		
		//设置滑动条
		if(mSeekBar!=null){
			mSeekBar.setMax((mMaxCount - mMinCount) * DOT_COUNT);
			mSeekBar.setProgress(toSeekBarData(value));
			mSeekBar.setOnSeekBarChangeListener(this);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		mValueTextView.setText(String.valueOf(toRealData(progress)));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		mProgress = seekBar.getProgress();

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		int progress = seekBar.getProgress();
		boolean toLeft = progress <= mProgress;
		int nearProgress = toNearProgress(progress, toLeft);
		mSeekBar.setProgress(nearProgress);
	}

	private int toSeekBarData(int realData) {
		return (realData - mMinCount) * DOT_COUNT;
	}

	private int toRealData(int seekBarData) {
		return seekBarData / DOT_COUNT + mMinCount;
	}

	private int toNearProgress(int currentProgress, boolean toLeft) {
		
		int nearProgress = currentProgress / DOT_COUNT * DOT_COUNT; // 先除以length取整，再乘回去
		if (!toLeft) {
			nearProgress += DOT_COUNT;
			nearProgress = Math.min(nearProgress, toSeekBarData(mMaxCount));
		}
		return nearProgress;
	}

}