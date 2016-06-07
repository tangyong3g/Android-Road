package com.sny.tangyong.basic;

import android.os.Bundle;

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

        info = new ItemComponentInfo("Meminfo", MeminfoActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Graphic", BasicGraphicListActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Thread", HandlerTestActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Layout", LayoutActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("ViewSave", ViewCycleTestActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("DeviceInfo", BasicDeviceInfoActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("LooperVersion2", LooperVersion2.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Performance For Threads", BesetPracticeForThread.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("UnCatchException", UnCatchExceptionActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Http/Https", BasicHttpTwoActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Identifying", BasicUnquiueIdentifyActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Best Practice for perfemence", BasicBestPracticeForPerfemenceActivity.class);
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
