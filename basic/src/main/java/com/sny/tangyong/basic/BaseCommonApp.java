package com.sny.tangyong.basic;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.List;

/**
 * Created by ty_sany@163.com on 2016/6/7.
 */
public class BaseCommonApp {

    /**
     * 得到安装的应用程序
     * 
     * @return
     */
    public static List<PackageInfo> getInstalledApps() {

        int flag = PackageManager.GET_META_DATA;
        List<PackageInfo> infos = BasicApplication.getInstance().getPackageManager().getInstalledPackages(flag);

        return infos;
    }



}
