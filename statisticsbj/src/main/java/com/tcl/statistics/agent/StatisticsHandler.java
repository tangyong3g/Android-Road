package com.tcl.statistics.agent;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;

import com.tcl.statistics.bean.EventItem;
import com.tcl.statistics.bean.ExceptionInfo;
import com.tcl.statistics.bean.PageInfo;
import com.tcl.statistics.bean.StatisticsItem;
import com.tcl.statistics.bean.StatisticsResult;
import com.tcl.statistics.net.StatisticsApi;
import com.tcl.statistics.systeminfo.AppInfo;
import com.tcl.statistics.util.CrashHandler;
import com.tcl.statistics.util.FileSerializableUtils;
import com.tcl.statistics.util.LogUtils;
import com.tcl.statistics.util.NetUtils;

public class StatisticsHandler {
    private static StatisticsHandler mInstance = new StatisticsHandler();
    private static Context mContext;
    private HandlerThread mHandlerThread ;
    private Handler mHandler;

    private String mCurrentPage = "";
    private String mCurrentClassName = "";

    public final static int WHAT_ON_RESUME = 0;
    public final static int WHAT_ON_PAUSE = 1;
    public final static int WHAT_ON_EXIT = 2;
    public final static int WHAT_ON_ERROR_EXIT = 3;
    public final static int WHAT_ON_EVENT = 4;
    public final static int WHAT_ON_PAGE_START = 5;
    public final static int WHAT_ON_PAGE_END = 6;
    public final static int WHAT_ON_CATCH_EXCEPTION = 7;
    public final static int WHAT_SEND_LOG = 999;

    private long mStartTime = 0;
    private long mExitTime;

    private static StatisticsResult mStatisticsResult;
    private static StatisticsItem mStatisticsItem;
    // 统计页面信息
    private static List<PageInfo> mPageInfos = new ArrayList<PageInfo>();
    // 不带参数的事件统计
    private static List<EventItem> mNoParamEvents = new ArrayList<EventItem>();
    // 带参数的事件统计
    private static List<EventItem> mHasParamEvents = new ArrayList<EventItem>();

    public static String mExceptionMessage = null;
    public static String mExcetpionCause = null;

    private static final int DATA_MAX_SIZE = 300*1024;

    private boolean isSendLog = false;
    private boolean isResume = false;

    public static StatisticsHandler getInstance() {
        return mInstance;
    }

    private StatisticsHandler() {
        startThread();
    }

    private void startThread() {
        mHandlerThread = new HandlerThread("statisticHandler",
                Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(),
                new HandlerCallBack());
    }


    class HandlerCallBack implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            Object obj = msg.obj;
            switch (msg.what) {
                case WHAT_ON_RESUME:
                    if (obj != null) {
                        onResume((Context) obj);
                    }
                    break;
                case WHAT_ON_PAUSE:
                    if (obj != null) {
                        onPause((Context) obj);
                    }
                    break;
                case WHAT_ON_EXIT:
                    onExit();
                    break;
                case WHAT_ON_ERROR_EXIT:
                    onErrorExit();
                    break;

                case WHAT_ON_EVENT:
                    if (obj != null) {
                        onEvent(obj);
                    }
                    break;
                case WHAT_ON_PAGE_START:
                    if (obj != null) {
                        onPageStart((String) obj);
                    }
                    break;
                case WHAT_ON_PAGE_END:
                    if (obj != null) {
                        onPageEnd((String) obj);
                    }
                    break;
                case WHAT_ON_CATCH_EXCEPTION:
                    if (obj != null) {
                        catchException((Context) obj);
                    }
                    break;
                case WHAT_SEND_LOG:
                    sendLog();
                    break;
            }
            return true;
        }

    }

    //判断子线程是否死掉
    private void checkThreadAlive() {
        if (mHandlerThread == null || !mHandlerThread.isAlive()) {
            startThread();
        }
    }


    public void sendMessage(int what, Object obj) {
        checkThreadAlive();
        Message msg = mHandler.obtainMessage(what, obj);
        mHandler.sendMessage(msg);
    }


    public void sendMessage(int what) {
        checkThreadAlive();
        mHandler.sendEmptyMessage(what);
    }

    /**
     * 设置异常捕获器
     *
     * @param context
     */
    private static void catchException(Context context) {
        if(context!=null){
            mContext = context.getApplicationContext();
            // 初始化异常记录器
            CrashHandler crashHandler = CrashHandler.getInstance();
            crashHandler.init(context);
        }
    }

    /**
     * 自定义页面开始
     *
     * @param pageName
     */
    private void onPageStart(String pageName) {
        LogUtils.W("进入自定义页面:" + pageName);
        if(mStatisticsResult == null){
            LogUtils.W("未执行onResume而执行onPageStart错误");
            return;
        }

        if(mContext == null){
            LogUtils.W("进入onPageStart，context 为null:");
            return;
        }

        if(convertStatisticResultToJson(mStatisticsResult).length()>DATA_MAX_SIZE){
            //大于数据最大值，则将数据存储为昨天文件，并创建新文件存储之后的数据
            LogUtils.E("data is full");
            //执行当前Activity的onResume操作，保存浏览时间
            for (int i = mPageInfos.size() - 1; i >= 0; i--) {
                PageInfo pageTimeCalculateTool = mPageInfos.get(i);
                if (mCurrentClassName.equals(pageTimeCalculateTool
                        .getPageName())) {
                    if (pageTimeCalculateTool != null) {
                        pageTimeCalculateTool.onPause();
                    }
                    break;
                }
            }
            saveStatisticsResultToHistoryFile();
            deleteTodayCacheFile();
            startNewStatistics();
        }


        if (mStatisticsResult == null) {
            mStatisticsResult = new StatisticsResult(mNoParamEvents,mHasParamEvents,System.currentTimeMillis());

            initNewStatisticsItem();
            // 增加新的统计
            mStatisticsResult.getStatisticItems().add(mStatisticsItem);
        }

        if (!pageName.equals(mCurrentPage)) {
            mPageInfos.add(new PageInfo(pageName));
            mCurrentPage = pageName;
        } else {
            for (int i = mPageInfos.size() - 1; i >= 0; i--) {
                PageInfo pageTimeCalculateTool = mPageInfos.get(i);
                if (mCurrentPage.equals(pageTimeCalculateTool.getPageName())) {
                    if (pageTimeCalculateTool != null) {
                        pageTimeCalculateTool.onResume();
                    }
                    break;
                }
            }
        }
    }

    /**
     * 自定义页面结束
     *
     * @param pageName
     */
    private void onPageEnd(String pageName) {
        LogUtils.W("离开自定义页面:" + pageName);
        if(mStatisticsResult == null){
            LogUtils.E("执行onPageEnd时，未创建统计信息出错");
            return;
        }

        if(TextUtils.isEmpty(mCurrentPage)){
            LogUtils.D("当前页面未先执行onPageStart()，却执行了onPageEnd()");
            return;
        }

        if (mPageInfos == null || mPageInfos.size() == 0) {
            LogUtils.D("mPageInfos is null | mPageInfos.size == 0");
            return;
        }


        for (int i = mPageInfos.size() - 1; i >= 0; i--) {
            PageInfo pageInfo = mPageInfos.get(i);
            if (pageName.equals(pageInfo.getPageName())) {
                if (pageInfo != null) {
                    pageInfo.onPause();
                }
                break;
            }
        }

        mExitTime = System.currentTimeMillis();
        mStatisticsResult.setEndTime(System.currentTimeMillis());
    }

    /**
     * 增加事件统计
     *
     * @param obj
     */
    private void onEvent(Object obj) {
        EventItem event = (EventItem) obj;
        if(event.getEventParamMap()!=null){
            mHasParamEvents.add(event);
        }else{
            mNoParamEvents.add(event);
//			EventItem eventItem = mNoParamEvents.get(event.getEventName());
//			if (eventItem != null) {
//				eventItem.eventHappen();
//				event = null;
//			} else {
//				mNoParamEvents.put(event.getEventName(), event);
//			}
        }
    }

    /**
     * 进入Activity页面
     *
     * @param context
     */
    private void onResume(Context context) {
        mContext = context.getApplicationContext();
        String className = getShortClassName(context);
        LogUtils.D("进入页面:" + className);

        if(isResume){
            LogUtils.E(className+":未执行onPause而直接执行onResume出错");
            return;
        }

        isResume = true;

        if (System.currentTimeMillis() - mExitTime > StatisticsConfig
                .getSessionTimeOut(mContext)) {// 距离之前离开页面超过Session时间
            mStartTime = System.currentTimeMillis();

            mStatisticsResult = fetchStatisticsResult();
            HashMap<Serializable, String> historyLogs = FileSerializableUtils.getInstence().getHistoryLogs(context);
            if (mStatisticsResult != null
                    && mStatisticsResult.getStatisticItems().size() != 0) {
                mNoParamEvents = mStatisticsResult.getNoParamEvents();
                mHasParamEvents = mStatisticsResult.getHasParamEvents();
                LogUtils.D("准备发送今日数据");
                // 上报结果
                if (reportResult(mStatisticsResult)) {
                    deleteTodayCacheFile();
                    mNoParamEvents.clear();
                    mHasParamEvents.clear();
                    mStatisticsResult = new StatisticsResult(mNoParamEvents,mHasParamEvents,mStartTime);
                }
            }else if(historyLogs == null ||historyLogs.size()==0){ //无历史日志，并且当天日志内容为空，代表首次安装,则发送一个空数据日志
                mStatisticsResult = new StatisticsResult(mNoParamEvents,mHasParamEvents,mStartTime);
                reportResult(mStatisticsResult);
            }

            if(historyLogs != null && historyLogs.size()>0){
                //发送历史日志
                SendHistoryLogHandler.getInstance().sendHistoryLogs(context);
            }

            // 清空临时缓存
            clearCache();

            if (mStatisticsResult == null) {
                mStatisticsResult = new StatisticsResult(mNoParamEvents,mHasParamEvents,mStartTime);
            }

            initNewStatisticsItem();

            // 增加新的统计
            mStatisticsResult.getStatisticItems().add(mStatisticsItem);
        }else if(convertStatisticResultToJson(mStatisticsResult).length()>DATA_MAX_SIZE){
            //大于数据最大值，则将数据存储为昨天文件，并创建新文件存储之后的数据
            LogUtils.E("data is full");
            saveStatisticsResultToHistoryFile();
            deleteTodayCacheFile();
            startNewStatistics();
        }

        if (!className.equals(mCurrentClassName)) {
            if(mPageInfos.size()==0){//第一个页面的开始时间设置为新Session的开始时间
                mPageInfos.add(new PageInfo(className,mStartTime));
            }else{
                mPageInfos.add(new PageInfo(className));
            }
            mCurrentClassName = className;
            mCurrentPage = null;//进入另一个Activity了，则应该把最后的自定义页面置位null，
            //否则在回到刚才的Activity时，里面的自定义页面不会重新计算，而是累加时间，详见
            //onPageStart逻辑（或判断自定义页面和mCurrentPage是否一致）
        } else {
            for (int i = mPageInfos.size() - 1; i >= 0; i--) {
                PageInfo pageTimeCalculateTool = mPageInfos.get(i);
                if (mCurrentClassName.equals(pageTimeCalculateTool
                        .getPageName())) {
                    if (pageTimeCalculateTool != null) {
                        pageTimeCalculateTool.onResume();
                    }
                    break;
                }
            }
        }
    }

    private void startNewStatistics() {
        mNoParamEvents.clear();
        mHasParamEvents.clear();
        mStatisticsResult = new StatisticsResult(mNoParamEvents,mHasParamEvents,System.currentTimeMillis());
        clearCache();
        initNewStatisticsItem();
        mStatisticsResult.getStatisticItems().add(mStatisticsItem);
    }

    private void deleteTodayCacheFile() {
        if (mContext != null)
            FileSerializableUtils.getInstence().deleteTodayLogFile(mContext);
    }

    private void initNewStatisticsItem() {
        mStatisticsItem = new StatisticsItem();
        mStatisticsItem.setPageInfos(mPageInfos);
    }

    /**
     * 离开页面
     *
     * @param obj
     */
    private void onPause(Context context) {
        mContext = context.getApplicationContext();
        String className = getShortClassName(context);

        if(mStatisticsResult == null){
            LogUtils.D("执行onPause方法之前，没有创建日志信息");
            return;
        }

        if (mPageInfos == null || mPageInfos.size() == 0) {
            LogUtils.D("mPageInfos is null | mPageInfos.size == 0");
            return;
        }
        if(!isResume){
            LogUtils.E(className+":未执行onResume而直接执行onPause出错");
            return;
        }
        if(TextUtils.isEmpty(mCurrentClassName)){
            LogUtils.D("当前页面"+mCurrentClassName+":未先执行onResume，却执行了onPause()");
            return;
        }

        isResume = false;
        LogUtils.W("离开页面:" + className);

        // PageTimeCalculateTool pageTimeCalculateTool = mAllStatistics
        // .get(className);
        for (int i = mPageInfos.size() - 1; i >= 0; i--) {
            PageInfo pageTimeCalculateTool = mPageInfos.get(i);
            if (mCurrentClassName.equals(pageTimeCalculateTool.getPageName())) {
                if (pageTimeCalculateTool != null) {
                    pageTimeCalculateTool.onPause();
                }
                break;
            }
        }

        mExitTime = System.currentTimeMillis();
        mStatisticsResult.setEndTime(System.currentTimeMillis());
        saveStatisticsResult();
    }

    /**
     * 清除缓存数据
     */
    private void clearCache() {
        mPageInfos.clear();
        mExceptionMessage = null;
        mExcetpionCause = null;
        mCurrentClassName = null;
        mCurrentPage = null;
    }

    private void onExit() {
        saveStatisticsResult();
    }

    public void onErrorExit() {
        ExceptionInfo exceptionInfo = new ExceptionInfo();
        exceptionInfo.setExceptionMessage(mExceptionMessage);
        exceptionInfo.setExceptionCause(mExcetpionCause);
        exceptionInfo.setExcetpionTime(System.currentTimeMillis());
        if (mContext != null) {
            exceptionInfo.setAppVersion(AppInfo.getCurrentVersion(mContext));
        }
        mStatisticsItem.setExceptionInfo(exceptionInfo);

        //保存退出的时间
        mStatisticsResult.setEndTime(System.currentTimeMillis());
        saveStatisticsResult();
//		System.exit(1);
    }

    public void onKillProcess() {
        saveStatisticsResult();
    }

    private String getShortClassName(Context context) {
        String packageName = AppInfo.getPackageName(context);
        String className = context.getClass().getName();
        if (TextUtils.isEmpty(packageName)) {
            return className;
        }
        className = className.substring(packageName.length() + 1);
        return className;
    }

    /**
     * 上报统计结果
     *
     * @param statisticsResult
     * @return true代表发送成功,fasle代表发送失败
     */
    public boolean reportResult(StatisticsResult statisticsResult) {
        if (TextUtils.isEmpty(AppInfo.getInstance().getAppkey(mContext))||AppInfo.getInstance().getAppkey(mContext).length()!=10) {
            LogUtils.E("appKey is null or appKey is wrong,check your config");
            return false;
        }
        boolean sendResult = false;

        if (NetUtils.isNetworkEnable(mContext)) {
            LogUtils.D("net enable");

            JSONObject reportData = new JSONObject();
            JSONArray pageStatArray = new JSONArray();
            JSONArray eventArray = new JSONArray();
            JSONArray exceptionArray = new JSONArray();

            ArrayList<StatisticsItem> statisticItems = statisticsResult
                    .getStatisticItems();
            for (StatisticsItem item : statisticItems) {

                try {
                    JSONObject pageStat = new JSONObject();
                    pageStat.put("e", statisticsResult.getEndTime());
                    pageStat.put("s", statisticsResult.getStartTime());
                    pageStat.put("i", System.currentTimeMillis());
                    pageStat.put("c", statisticItems.size());
                    JSONArray pages = new JSONArray();
                    for (PageInfo pageTimeCalculateTool : item.getPageInfos()) {
//					LogUtils.E(pageTimeCalculateTool.getPageName()
//							+ "----停留时间共:"
//							+ pageTimeCalculateTool.getScanTime());
                        JSONObject page = new JSONObject();
                        page.put("n", pageTimeCalculateTool.getPageName());
                        page.put("d", pageTimeCalculateTool.getScanTime());
                        page.put("ps", 0);
                        pages.put(page);
                    }
                    ExceptionInfo exceptionInfo = item.getExceptionInfo();
                    if (exceptionInfo != null) {
                        JSONObject exception = new JSONObject();
                        exception.put("c", exceptionInfo.getExceptionMessage());
                        exception.put("v", exceptionInfo.getAppVersion());
                        exception.put("y", exceptionInfo.getExceptionCause());
                        exception.put("t", exceptionInfo.getExcetpionTime());
                        exceptionArray.put(exception);
                    }
                    pageStat.put("p", pages);
                    pageStatArray.put(pageStat);
                } catch (Exception e) {
                    sendResult = false;
                    return sendResult;
                }
            }

            try {
                //无参数的事件
                List<EventItem> noParamEvents = statisticsResult.getNoParamEvents();
                for (EventItem eventItem : noParamEvents) {
                    JSONObject event = new JSONObject();
                    event.put("d", eventItem.getEventValue());
                    event.put("t", eventItem.getStartTime());
//				event.put("s", eventItem.getHappenTime());
                    event.put("c", eventItem.getCount());
                    event.put("i", eventItem.getEventName());
                    event.put("p", new JSONArray());
                    eventArray.put(event);
                }
                //带参数的事件
                List<EventItem> hasParamEvents = statisticsResult.getHasParamEvents();
                for (EventItem eventItem : hasParamEvents) {
                    JSONObject event = new JSONObject();
                    event.put("d", eventItem.getEventValue());
                    event.put("t", eventItem.getStartTime());
//				event.put("s", eventItem.getHappenTime());
                    event.put("c", eventItem.getCount());
                    event.put("i", eventItem.getEventName());

                    if(eventItem.getEventParamMap()!=null&&eventItem.getEventParamMap().size()>0){
                        JSONArray eventParamArray = new JSONArray();
                        for (Map.Entry<String, String> eventParamEntry : eventItem.getEventParamMap().entrySet()) {
                            JSONObject eventParam = new JSONObject();
                            eventParam.put("k",eventParamEntry.getKey());
                            eventParam.put("v",eventParamEntry.getValue());
                            eventParamArray.put(eventParam);
                        }
                        event.put("p", eventParamArray);
                    }
                    eventArray.put(event);
                }

                reportData.put("pr", pageStatArray);
                reportData.put("ex", exceptionArray);
                reportData.put("ev", eventArray);

                JSONObject appinfo = new JSONObject();
                AppInfo.getInstance().setAppinfo(mContext, appinfo);
                reportData.put("he", appinfo);

            } catch (JSONException e) {
                e.printStackTrace();
                sendResult = false;
                return sendResult;
            }
            LogUtils.D("上报消息长度:"+reportData.toString().length()+"\n消息：" + reportData.toString());
            //联网上报成功，清空上报内容
            boolean result = StatisticsApi.sendLog(mContext, reportData.toString(), 30 * 1000, 30 * 1000);
//			boolean result = true;
            LogUtils.D("上报结果:" + result);
            sendResult = result;
        }else{
            LogUtils.D("net disable");
        }
        return sendResult;
    }


    private void sendLog() {
        if (isSendLog == false) {
            synchronized (StatisticsHandler.this) {
                if (isSendLog == false) {
                    isSendLog = true;
                    if (reportResult(mStatisticsResult)) {
                        // 清空缓存
                        mPageInfos.clear();
                        mExceptionMessage = null;
                        mExcetpionCause = null;
                        mCurrentPage = null;
                        mNoParamEvents.clear();
                        mHasParamEvents.clear();
                        mStatisticsResult = new StatisticsResult(mNoParamEvents,mHasParamEvents,System.currentTimeMillis());
                    }

                    if (mStatisticsResult == null) {
                        mStatisticsResult = new StatisticsResult(mNoParamEvents,mHasParamEvents,System.currentTimeMillis());
                    }

                    initNewStatisticsItem();

                    // 增加新的统计
                    mStatisticsResult.getStatisticItems().add(mStatisticsItem);

                    mPageInfos.add(new PageInfo(mCurrentClassName));

                    isSendLog = false;
                }
            }
        }
    }

    private String convertStatisticResultToJson(StatisticsResult statisticsResult){
        JSONObject reportData = new JSONObject();
        JSONArray pageStatArray = new JSONArray();
        JSONArray eventArray = new JSONArray();
        JSONArray exceptionArray = new JSONArray();

        ArrayList<StatisticsItem> statisticItems = statisticsResult
                .getStatisticItems();
        for (StatisticsItem item : statisticItems) {

            try {
                JSONObject pageStat = new JSONObject();
                pageStat.put("e", statisticsResult.getEndTime());
                pageStat.put("s", statisticsResult.getStartTime());
                pageStat.put("i", System.currentTimeMillis());
                pageStat.put("c", statisticItems.size());
                JSONArray pages = new JSONArray();
                for (PageInfo pageTimeCalculateTool : item.getPageInfos()) {
                    JSONObject page = new JSONObject();
                    page.put("n", pageTimeCalculateTool.getPageName());
                    page.put("d", pageTimeCalculateTool.getScanTime());
                    page.put("ps", 0);
                    pages.put(page);
                }
                ExceptionInfo exceptionInfo = item.getExceptionInfo();
                if (exceptionInfo != null) {
                    JSONObject exception = new JSONObject();
                    exception.put("c", exceptionInfo.getExceptionMessage());
                    exception.put("v", exceptionInfo.getAppVersion());
                    exception.put("y", exceptionInfo.getExceptionCause());
                    exception.put("t", exceptionInfo.getExcetpionTime());
                    exceptionArray.put(exception);
                }
                pageStat.put("p", pages);
                pageStatArray.put(pageStat);
            } catch (Exception e) {
            }
        }

        try {
            //无参数的事件
            List<EventItem> noParamEvents = statisticsResult.getNoParamEvents();
            for (EventItem eventItem : noParamEvents) {
                JSONObject event = new JSONObject();
                event.put("d", eventItem.getEventValue());
                event.put("t", eventItem.getStartTime());
//				event.put("s", eventItem.getHappenTime());
                event.put("c", eventItem.getCount());
                event.put("i", eventItem.getEventName());
                event.put("p", new JSONArray());
                eventArray.put(event);
            }
            //带参数的事件
            List<EventItem> hasParamEvents = statisticsResult.getHasParamEvents();
            for (EventItem eventItem : hasParamEvents) {
                JSONObject event = new JSONObject();
                event.put("d", eventItem.getEventValue());
                event.put("t", eventItem.getStartTime());
//				event.put("s", eventItem.getHappenTime());
                event.put("c", eventItem.getCount());
                event.put("i", eventItem.getEventName());

                if(eventItem.getEventParamMap()!=null&&eventItem.getEventParamMap().size()>0){
                    JSONArray eventParamArray = new JSONArray();
                    for (Map.Entry<String, String> eventParamEntry : eventItem.getEventParamMap().entrySet()) {
                        JSONObject eventParam = new JSONObject();
                        eventParam.put("k",eventParamEntry.getKey());
                        eventParam.put("v",eventParamEntry.getValue());
                        eventParamArray.put(eventParam);
                    }
                    event.put("p", eventParamArray);
                }
                eventArray.put(event);
            }

            reportData.put("pr", pageStatArray);
            reportData.put("ex", exceptionArray);
            reportData.put("ev", eventArray);

            JSONObject appinfo = new JSONObject();
            AppInfo.getInstance().setAppinfo(mContext, appinfo);
            reportData.put("he", appinfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reportData.toString();
    }

    /**
     * 保存统计结果
     */
    private static void saveStatisticsResult() {
//		LogUtils.E("准备保存数据");
        try {
            FileSerializableUtils.getInstence().saveStatisticsResultToFile(mContext,
                    mStatisticsResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存统计结果为历史文件
     */
    private static void saveStatisticsResultToHistoryFile() {
        LogUtils.D("准备保存文件为历史数据");
        try {
            FileSerializableUtils.getInstence().saveStatisticsResultToLastDayFile(mContext,
                    mStatisticsResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * 从本地文件获取统计结果
     *
     * @return
     */
    private static StatisticsResult fetchStatisticsResult() {
        if (mContext == null) {
            LogUtils.I("fetchStatisticsResult,context is null");
            return null;
        }

        StatisticsResult statisticsResult = null;
        try {
            statisticsResult = (StatisticsResult) FileSerializableUtils
                    .getInstence().getObjectFromFile(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statisticsResult;
    }
}
