package com.tcl.statistics.bean;

import com.tcl.statistics.util.LogUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StatisticsResult
        implements Serializable {
    private ArrayList<StatisticsItem> statisticItems = new ArrayList();
    private List<EventItem> hasParamEvents;
    private List<EventItem> noParamEvents;
    private long startTime;
    private long endTime;

    public StatisticsResult(List<EventItem> noParamEvents, List<EventItem> hasParamEvents, long startTime) {
        this.noParamEvents = noParamEvents;
        this.hasParamEvents = hasParamEvents;
        init(startTime);
    }

    private void init(long startTime) {
        this.startTime = (this.endTime = startTime);
        LogUtils.D("启动新Session，statTime:" + startTime + ",endTime" + this.endTime);
    }

    public List<EventItem> getNoParamEvents() {
        return this.noParamEvents;
    }

    public List<EventItem> getHasParamEvents() {
        return this.hasParamEvents;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public ArrayList<StatisticsItem> getStatisticItems() {
        return this.statisticItems;
    }

    public void setStatisticItems(ArrayList<StatisticsItem> statisticItems) {
        this.statisticItems = statisticItems;
    }
}