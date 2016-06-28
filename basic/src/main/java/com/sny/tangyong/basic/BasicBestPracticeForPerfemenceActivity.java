package com.sny.tangyong.basic;

import com.sny.tangyong.common.view.BaseListActivity;
import com.sny.tangyong.common.view.ItemComponentInfo;

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

        info = new ItemComponentInfo("perfemence Tips", PerfemenceTipActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Improve layout perfemence", LayoutPerfemenceListActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Optimizing Battery life", BatteryLifeListActivity.class);
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
