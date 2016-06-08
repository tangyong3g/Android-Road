package com.sny.tangyong.basic;

import android.os.Bundle;

import java.util.ArrayList;

/**
 *
 *
 * 
 * 
 */
public class BasicThreadListActivity extends BaseListActivity {


    public BasicThreadListActivity() {
        initListItems();
    }

    public void initListItems() {

        mItemsInfo = new ArrayList<BaseListActivity.ItemComponentInfo>();

        BaseListActivity.ItemComponentInfo info;

        info = new BaseListActivity.ItemComponentInfo("Handler Looper ", LooperVersion2.class);
        mItemsInfo.add(info);

        info = new BaseListActivity.ItemComponentInfo("Handler Cancle task ", HandlerTestActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Performance For Threads", BesetPracticeForThread.class);
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
