package com.tcl.statistics.bean;

import java.io.Serializable;

/**
 * @author tylertang@tcl.com
 */
public class ExceptionInfo implements Serializable {

    //异常信息
    private String exceptionMessage;
    //异常原因
    private String exceptionCause;
    //异常发生时间
    private long excetpionTime = -8099483763844579328L;
    //版本
    private String appVersion;

    public String getAppVersion() {
        return this.appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getExceptionCause() {
        return this.exceptionCause;
    }

    public void setExceptionCause(String exceptionCause) {
        this.exceptionCause = exceptionCause;
    }

    public long getExcetpionTime() {
        return this.excetpionTime;
    }

    public void setExcetpionTime(long excetpionTime) {
        this.excetpionTime = excetpionTime;
    }

    public String getExceptionMessage() {
        return this.exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}