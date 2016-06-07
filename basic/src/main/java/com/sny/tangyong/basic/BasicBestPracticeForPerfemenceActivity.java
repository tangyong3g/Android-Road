package com.sny.tangyong.basic;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * 性能最佳体验
 */
public class BasicBestPracticeForPerfemenceActivity extends BaseListActivity {


    public BasicBestPracticeForPerfemenceActivity() {
        initListItems();
    }

    public void initListItems() {

        mItemsInfo = new ArrayList<ItemComponentInfo>();

        ItemComponentInfo info;

        info = new ItemComponentInfo("Manager your app's Memory", LoadImageDataActivity.class);
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


}
