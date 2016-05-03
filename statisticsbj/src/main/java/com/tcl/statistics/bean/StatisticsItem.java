package com.tcl.statistics.bean;

import java.io.Serializable;
import java.util.List;


/**
 * @author tylertang@tcl.com
 *         <p>
 *         统计信息封装类
 */
public class StatisticsItem implements Serializable {


    //页面信息
    private List<PageInfo> pageInfos;
    //异常信息
    private ExceptionInfo exceptionInfo;

    public List<PageInfo> getPageInfos() {
        return this.pageInfos;
    }

    public void setPageInfos(List<PageInfo> pageInfos) {
        this.pageInfos = pageInfos;
    }

    public ExceptionInfo getExceptionInfo() {
        return this.exceptionInfo;
    }

    public void setExceptionInfo(ExceptionInfo exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }
}