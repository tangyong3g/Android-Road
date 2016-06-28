package com.sny.tangyong.basic;

import android.os.Bundle;

import com.sny.tangyong.common.view.BaseListActivity;
import com.sny.tangyong.common.view.ItemComponentInfo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/7/29.
 */
public class MainActivity extends BaseListActivity {

    public MainActivity() {
        initListItems();
    }


    public void initListItems() {

        mItemsInfo = new ArrayList<ItemComponentInfo>();

        ItemComponentInfo info;

        info = new ItemComponentInfo("Meminfo", BasicMemListActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Graphic", BasicGraphicListActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Thread", BasicThreadListActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Layout", LayoutActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("performance", BasicBestPracticeForPerfemenceActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Other", BasicOtherListActivity.class);
        mItemsInfo.add(info);

        initDisplayList();
    }


    private void initDisplayList() {
        if (mItemsInfo != null && mItemsInfo.size() > 0) {
            mUnits = new String[mItemsInfo.size()];

            for (int i = 0; i < mItemsInfo.size(); i++) {
                mUnits[i] = mItemsInfo.get(i).mDisplayName;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getActionBar();
    }

}
