package com.sny.tangyong.basic;

import com.sny.tangyong.common.view.BaseListActivity;
import com.sny.tangyong.common.view.ItemComponentInfo;

import java.util.ArrayList;

/**
 * Created by ty_sany@163.com on 2016/6/6.
 */
public class BasicBackGroudJobActivity extends BaseListActivity {


    public BasicBackGroudJobActivity() {
        initListItems();
    }

    /**
     * 初始化Item项
     */
    public void initListItems() {

        mItemsInfo = new ArrayList<ItemComponentInfo>();

        ItemComponentInfo info;

        info = new ItemComponentInfo("Create backGround Service", CreateBackGroundServiceActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Send Work Request To Service", SendRequestToServiceActivity.class);
        mItemsInfo.add(info);


        info = new ItemComponentInfo("Reporting Work State", ReportingWorkStateActivity.class);
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
