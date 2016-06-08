package com.sny.tangyong.basic;

import android.os.Bundle;

import java.util.ArrayList;


/**
 * 内存方面解决方案
 */
public class BasicMemListActivity extends BaseListActivity {

    public BasicMemListActivity() {
        initListItems();
    }

    public void initListItems() {

        mItemsInfo = new ArrayList<BaseListActivity.ItemComponentInfo>();

        BaseListActivity.ItemComponentInfo info;

        info = new BaseListActivity.ItemComponentInfo("Mem consume Test", MeminfoActivity.class);
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
