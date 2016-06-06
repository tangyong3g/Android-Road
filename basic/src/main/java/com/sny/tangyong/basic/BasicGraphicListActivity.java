package com.sny.tangyong.basic;

import android.os.Bundle;

import java.util.ArrayList;


/**
 *
 * <br>
 * 解决如何加载大的图片。 <br>
 * 在异步线程中处理图片的编码。 <br>
 * 缓存图片的方法。 <br>
 * 管理图片内存. <br>
 * 显示图片.
 *
 */
public class BasicGraphicListActivity extends BaseListActivity {


    public BasicGraphicListActivity() {
        initListItems();
    }

    public void initListItems() {

        mItemsInfo = new ArrayList<ItemComponentInfo>();

        ItemComponentInfo info;

        info = new ItemComponentInfo("LoadImageData", LoadImageDataActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Process Image off the Thread", ProcessImageOnUiActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Cache Bitmap", CacheBitmapActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Manager Bitmap memory", ManagerBitmapActivity.class);
        mItemsInfo.add(info);

        info = new ItemComponentInfo("Display bitmap on UI", DisplayBitmapActivity.class);
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
