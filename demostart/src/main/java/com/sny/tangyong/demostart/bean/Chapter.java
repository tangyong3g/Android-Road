package com.sny.tangyong.demostart.bean;

import android.content.Intent;


/**
 * Created by T540P on 2015/8/28.
 */
public class Chapter {


    public static final String BASIC_NAME = "chapter";

    private String mKey;

    //章节下面的子章节
    private Chapter[] mChilds;

    //响应的IntentClass
    private Class mActionIntentClass;

    public Chapter[] getChilds() {
        return mChilds;
    }

    public Class getActionIntentClass() {
        return mActionIntentClass;
    }

    public void setChapterKey(String key) {
        mKey = key;
    }

    public void setmActionIntentClass(String className) throws ClassNotFoundException {
        mActionIntentClass = Class.forName(className);
    }


    public String getKey() {
        return mKey;
    }
}
