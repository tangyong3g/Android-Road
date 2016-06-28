package com.sny.tangyong.basic;

import android.os.Bundle;

import com.sny.tangyong.common.view.BaseListActivity;
import com.sny.tangyong.common.view.ItemComponentInfo;

import java.util.ArrayList;


/**
 * @author ty_sany@163.com
 */
public class AndroidNPreViewListActivity extends BaseListActivity {

    public AndroidNPreViewListActivity() {
        initListItems();
    }

    public void initListItems() {

        mItemsInfo = new ArrayList<ItemComponentInfo>();
        ItemComponentInfo info;

        info = new ItemComponentInfo("BackGround Optization for Android N", BackGroundOptizationActivity.class);
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
