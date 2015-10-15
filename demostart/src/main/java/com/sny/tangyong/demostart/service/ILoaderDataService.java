package com.sny.tangyong.demostart.service;

import android.content.Context;
import android.content.Intent;

import com.sny.tangyong.demostart.bean.Chapter;

import java.util.ArrayList;

/**
 * Created by T540P on 2015/8/28.
 */
public interface ILoaderDataService extends ILifeCycle {

    /**
     * 得到当前的响应
     *
     * @param id
     * @return
     */
    public Intent getIntentById(String id);

    /**
     * 得到子单元
     *
     * @param id
     * @return
     */
    public Chapter[] getChilds(String id);

    /**
     * 初始化数据
     *
     * @return
     * @throws Exception
     */
    public ArrayList<Chapter> loadChapterData(Context context
    ) throws Exception;

}
