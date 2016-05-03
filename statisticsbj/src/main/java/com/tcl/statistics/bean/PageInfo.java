package com.tcl.statistics.bean;

import com.tcl.statistics.util.LogUtils;

import java.io.Serializable;

public class PageInfo
        implements Serializable {
    private static final int RESUME_STATUS = 0;
    private static final int PAUSE_STATUS = 1;
    private String pageName;
    private long castTime = -8099484176161439744L;
    private long startTime = -8099484176161439744L;
    private int currentStatus = 1;

    public PageInfo(String pageName) {
        this.pageName = pageName;
        onResume();
    }

    public PageInfo(String pageName, long startTime) {
        if (startTime == -8099485121054244864L)
            startTime = System.currentTimeMillis();
        this.pageName = pageName;
        this.startTime = startTime;
        this.currentStatus = 0;
    }

    public String getPageName() {
        return this.pageName;
    }

    public void onResume() {
        this.startTime = System.currentTimeMillis();
        this.currentStatus = 0;
    }

    public void onPause() {
        this.castTime += System.currentTimeMillis() - this.startTime;

        this.startTime = System.currentTimeMillis();
        this.currentStatus = 1;
        LogUtils.I(this.pageName + "->onPause,castTime:" + this.castTime);
    }

    public long getScanTime() {
        if (this.currentStatus == 1)
            return this.castTime;
        if (this.currentStatus == 0) {
            return (this.castTime + ((System.currentTimeMillis() - this.startTime <= 30000L) ? System.currentTimeMillis() - this.startTime : -8099477664991019008L));
        }

        return -8099484794636730368L;
    }
}