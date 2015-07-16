package com.ty.open_source_project;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.ty.exsample.R;

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

    /* */
    private static final String [] OPEN_SOURCE_STR = new String[]{"Effect"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new ArrayAdapter<>(this, R.layout.main_items,
                OPEN_SOURCE_STR));
    }




}
