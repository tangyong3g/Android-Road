package com.sny.tangyong.demostart.service;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.example.tangyong.demostart.R;
import com.sny.tangyong.demostart.bean.Chapter;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by T540P on 2015/8/28.
 */
public class LoadDataServiceImpl implements ILoaderDataService {


    @Override
    public Chapter[] getChilds(String id) {


        return new Chapter[0];
    }


    @Override
    public ArrayList<Chapter> loadChapterData(Context context) throws Exception {

        XmlResourceParser xrp = context.getResources().getXml(R.xml.initcontent);

        ArrayList<Chapter> chapters = null;
        while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {

            if (xrp.getEventType() == XmlResourceParser.START_TAG) {

                String name = xrp.getName();

                if (name.equals(Chapter.BASIC_NAME)) {

                    if (chapters == null) {
                        chapters = new ArrayList<Chapter>();
                    }

                    Chapter chapter = new Chapter();

                    String id = xrp.getAttributeValue(0);
                    String attrName = xrp.getAttributeValue(1);
                    String attrAction = xrp.getAttributeValue(2);

                    //chapter.setmActionIntentClass(attrAction);
                    chapter.setChapterKey(id);

                    chapters.add(chapter);

                }
                Log.i("tyler.tnag", name);
            }
            xrp.next();
        }

        return chapters;
    }

    @Override
    public Intent getIntentById(String id) {
        return null;
    }


    @Override
    public void onSetup() {

    }

    @Override
    public void onDestory() {

    }
}
