package com.sny.tangyong.basic;

import java.util.ArrayList;

/**
 * 布局优化
 */
public class LayoutPerfemenceListActivity extends BaseListActivity {


    public LayoutPerfemenceListActivity() {
        initListItems();
    }

    public void initListItems() {

        mItemsInfo = new ArrayList<ItemComponentInfo>();

        ItemComponentInfo info;

        info = new ItemComponentInfo("Optimizing Layout Hierarchies", LoadImageDataActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Re-using Layouts", LoadImageDataActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Loading Views On Demand", LoadImageDataActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Making ListView Scrolling Smooth", BasicPerfemenceListActivity.class);
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
