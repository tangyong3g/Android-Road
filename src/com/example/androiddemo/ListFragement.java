package com.example.androiddemo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.internal.widget.ListViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import com.sny.tangyong.androiddemo.AndroidApplication;
import com.sny.tangyong.androiddemo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/7/27.
 */
public class ListFragement extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Context context = container.getContext();
        ListViewCompat viewCompat = new ListViewCompat(container.getContext(), null, 0);
        SimpleAdapter simpleAdapter = initAdapter(context);

        viewCompat.setAdapter(simpleAdapter);
        return viewCompat;
    }


    /**
     * 初始化数据
     *
     * @return
     */
    private List<HashMap<String, Object>> prepareData() {

        List<HashMap<String, Object>> datas = new ArrayList<HashMap<String, Object>>();

        String[] array = AndroidApplication.getInstance().getApplicationContext().getResources().getStringArray(R.array.base_android);

        for (String str : array) {

            HashMap<String, Object> data = new HashMap<String, Object>();

            data.put("id", str);
            data.put("name", str);

            datas.add(data);
        }

        return datas;
    }


    /**
     * 初始化 SimpleAdapter
     *
     * @param context
     * @return
     */
    private SimpleAdapter initAdapter(Context context) {

        SimpleAdapter simpleAdapter = new SimpleAdapter(context, prepareData(), R.layout.item, new String[]{"name"}, new int[]{R.id.name});

        return simpleAdapter;
    }

}
