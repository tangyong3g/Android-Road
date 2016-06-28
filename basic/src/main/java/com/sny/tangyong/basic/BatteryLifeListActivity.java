package com.sny.tangyong.basic;

import com.sny.tangyong.common.view.BaseListActivity;
import com.sny.tangyong.common.view.ItemComponentInfo;

import java.util.ArrayList;

/**
 * 
 */
public class BatteryLifeListActivity extends BaseListActivity {


    public BatteryLifeListActivity() {
        initListItems();
    }

    public void initListItems() {

        mItemsInfo = new ArrayList<ItemComponentInfo>();

        ItemComponentInfo info;

        info = new ItemComponentInfo("Reduce Network battery Drain", ReduceBatteryDrainActivity.class);
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
