package com.tcl.statistics.bean;

import java.io.Serializable;
import java.util.Map;


/**
 * 事件信息Bean
 */
public class EventItem implements Serializable {

    //事件名称
    private String eventName;
    //事件发生时间
    private String happenTime;

    private int count;
    //事件参数
    private Map<String, String> eventParamMap;
    //事件值
    private int eventValue;
    //事件开始时间
    private long startTime;


    /**
     * 构造方法
     *
     * @param startTime 开始时间
     * @param eventName 事件名称
     */
    public EventItem(long startTime, String eventName) {
        this.startTime = startTime;
        this.eventName = eventName;
        this.count = 1;
        this.happenTime = "0";
    }


    /**
     * 构造方法
     *
     * @param startTime 开始时间
     * @param eventName 事件名称
     * @param paramMap  事件参数
     */
    public EventItem(long startTime, String eventName, Map<String, String> paramMap) {
        this.startTime = startTime;
        this.eventName = eventName;
        this.eventParamMap = paramMap;
        this.count = 1;
        this.happenTime = "0";
    }

    public EventItem(long startTime, String eventName, Map<String, String> paramMap, int value) {
        this.startTime = startTime;
        this.eventName = eventName;
        this.eventParamMap = paramMap;
        this.eventValue = value;
        this.count = 1;
        this.happenTime = "0";
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getCount() {
        return this.count;
    }

    public String getEventName() {
        return this.eventName;
    }

    public String getHappenTime() {
        return this.happenTime;
    }

    public Map<String, String> getEventParamMap() {
        return this.eventParamMap;
    }

    public int getEventValue() {
        return this.eventValue;
    }

    public void eventHappen() {
        this.count += 1;
        EventItem tmp11_10 = this;
        tmp11_10.happenTime = tmp11_10.happenTime + "|" + (System.currentTimeMillis() - this.startTime);
    }
}