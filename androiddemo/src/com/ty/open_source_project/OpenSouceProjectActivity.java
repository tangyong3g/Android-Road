package com.ty.open_source_project;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

import com.sny.tangyong.androiddemo.R;

/**
 *
 *
 * Created by Administrator on 2015/7/16.
 *
 *
 *          项目为开源项目多数是引用Github上面来的,会指出来源的地方
 *
 */
public class OpenSouceProjectActivity extends ListActivity{


    String[] units = new String[] { ""};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //設置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //取消標題
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setListAdapter(new ArrayAdapter<>(this, R.layout.main_items, units));

    }





}
