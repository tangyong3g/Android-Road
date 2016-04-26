package com.tcl.mig.staticssdk;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.tcl.mig.staticssdk.DBAsyncTask.AsyncCallBack;
import com.tcl.mig.staticssdk.beans.CtrlBean;
import com.tcl.mig.staticssdk.beans.OptionBean;
import com.tcl.mig.staticssdk.beans.PostBean;
import com.tcl.mig.staticssdk.connect.BaseConnectHandle;
import com.tcl.mig.staticssdk.connect.PostFactory;
import com.tcl.mig.staticssdk.database.DataBaseProvider;
import com.tcl.mig.staticssdk.scheduler.GetCtrlInfoTask;
import com.tcl.mig.staticssdk.scheduler.GetCtrlInfoTask.GetCtrlInfoCallBack;
import com.tcl.mig.staticssdk.scheduler.SchedulerManager;
import com.tcl.mig.staticssdk.scheduler.StaticPostTask;
import com.tcl.mig.staticssdk.scheduler.StaticPostTask.StaticPostCallBack;
import com.tcl.mig.staticssdk.utiltool.CpuManager;
import com.tcl.mig.staticssdk.utiltool.DrawUtils;
import com.tcl.mig.staticssdk.utiltool.Machine;
import com.tcl.mig.staticssdk.utiltool.UtilTool;
/*
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
*/

//CHECKSTYLE:OFF
/**
 * 
 * <br>
 * 类描述:统计管理 <br>
 * 功能详细描述:
 * 
 * @author rongjinsong
 * @date [2013-2-28]
 */
public class StatisticsManager {

	/***************************** Changelog **************************************/
	/**
	 * V1.20update:
	 * 
	 * @date 2014-09-24
	 * @author luozhiping <br>
	 *         1.全新的日志采集控制协议
	 *         http://wiki.3g.net.cn/pages/viewpage.action?pageId=11273569
	 *         2.修改用户概率算法，提高开关控制下发的用户比例（user_ratio）字段的判断固定性，避免用户数据单点化
	 *         3.增加了一个带选项的接口：可传入ABTEST、传入位置字符串、立即上传选项
	 *         4.增加了插入到DB的监听回调，现在可以在插入完DB再做一些事情了
	 */
	private static final String SDK_VER = "1.3";

	/**
	 * V1.10update:
	 * 
	 * @date 2014-08-22
	 * @author luozhiping <br>
	 *         1.Deprecated了老的接口使用方法。新增为统一使用一个接口：uploadStaticData(final int
	 *         logId, final int funid, final String buffer);
	 *         2.增加了开关控制和循环获取服务器开关信息的功能
	 *         3.增加了定时上传功能，定时上传的时间周期由服务器下发（不再是所有数据都实时上传）
	 *         4.新增了数据表。老的数据表中的数据将自动上传后并删除
	 */

	/**
	 * 1.01更新：1.增加了接口对外指定服务器urlupLoadStaticData(String url, String staticData,
	 * int dataOpCode) 2.修复获取北京时间部分语言乱码问题 1.02独立渠道洗白功能 2013/05/03
	 * 基本信息中增加获得是否root 1.03 2013.07.31 增加互推源记录和一五屏广告上传接口 1.04 2013.12.09
	 * 1.17和19的统计数据都拼接好后存放在data中，不再按各字段存储;
	 * 2.增加软件互推开关enableSoftManager()，对不需要此功能的产品基础信息统计不需要解析cache文件 1.05
	 * 2014-01-13 DB访问改成ContentProvider形式，对于每个项目打包时需要针对每个项目将DataContentProvider.
	 * java放到对应的包名的package中 1.06 2014-03-12 优化上传机制，统一接口数据合并上传 1.07 2014-06-09
	 * DB保存增加异步接口
	 */
	public static final String TAG = "StatisticsManager";

	protected static final long ONE_MINUTES = 1 * 60 * 1000;

	public static final int BASIC_FUN_ID = 19;

	public static final int BASIC_OPTION_FUN_ID = 17;
	public static final int CHANNEL_CONTROL_FUN_ID = 530001;
	public static final int PRODUCT_GOLAUNCHEREX = 1;

	protected static final String STATISTICS_DATA_SEPARATE_STRING = "||";
	public static boolean sDebugMode = false;
	private static final String GOLAUNCHER_MAIN_ACTION = "com.gau.go.launcherex.MAIN";

	private static StatisticsManager mSelf;
	private Context mContext;
	private volatile boolean mQuit = true;
	private PostQueue mQueue;
	private DataBaseProvider mDBProvider;
	// private LinkedList<PostBean> mBufferQueue;
	private Object mMutex;
	private long mLastImportFromDB = 0;
	private static String sGoId;

	private boolean mEnableSoftManager = false;
	
	private Lock mHashMapLock = new ReentrantLock();

	/** 2014.8 */
	/** 访问开关控制记录器 */
	public static final String CTRL_SP_NAME = "ctrl_sp_";
	private static final String CTRLINFO_LAST_GET_TIME = "ctrl_last_get_time";
	public static final String USER_FIRST_RUN_TIME = "first_run_time";
//	private static final String USER_IS_NEW = "user_is_new";
	public static final long NEW_USER_VALID_TIME = 32 * 60 * 60 * 1000;
	/** 控制开关默认获取时间间隔：8小时，其中增加的300秒是为了防止与上传统计数据同时触发导致数据错误做的延迟 */
	private static final long DEFAULT_INTEVRALTIME = 8 * 60 * 60 * 1000 + 300 * 1000;
	/** 此broadcast用于主进程从控制信息获取成功后通知子进程更新内存中的控制开关 */
	private static final String BROADCAST_GETCTRLINFO = "com.android.broadcast.ctrlinfo";
	/** 此broadcast用于子进程被调用了upload××接口后，需要通知主进程立即上传 */
	private static final String BROADCAST_UPLOADDATA = "com.android.broadcast.uploaddata";
	private static final String BROADCAST_INTENT_ID = "id";
	private static final String BROADCAST_INTENT_PKGNAME = "pkg_name";
	/**
	 * 内存中的控制开关信息
	 */
	private Map<String, CtrlBean> mCtrlMap;
	private ExecutorService mExecutor;
	private boolean mIsGetCtrlFailed = false;
	private boolean mIsGettingCtrlInfo = false;
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;
	private SchedulerManager mSchedulerManager;
	private boolean onOff = false;

	public static String sMainProcessName = null;
	public static String sCurrentProcessName;
	public static String sAndroidId = null;
	public static String sCountry = null;
	public static int sVersionCode = 0;
	public static int sOSVersionCode = 0;
	public static String sChannel = null;
	public static String sVersionName = null;
	public static String sIMEI = null;
	public static String sGADID = null;

	/**
	 * 是否为新用户
	 */
	public static boolean sIsNew = true;

	private static boolean sIsBaseInfoInit;

	private HandlerThread mStatThread;
	private Handler mStatHandler;
	private ConcurrentHashMap<String, ArrayList<String>> mBufferMap = new ConcurrentHashMap<String, ArrayList<String>>();
	private int lastGetCtrlDBCount = 0;
	private long lastGetTime = 0;
	private long lastUpdateTime = 0;
	private final static String SPERATE = ";";
	private boolean lastIsNew = false;
	private boolean stopUpload = false;
	
	private StatisticsManager(Context context) {
		if (context == null) {
			throw new NullPointerException("context can not be null");
		}
		mContext = context;

		mSelf = this;
		mStatThread = new HandlerThread("statistic-thread");
		mStatThread.start();
		mStatHandler = new Handler(mStatThread.getLooper());
		
		DrawUtils.resetDensity(context);
		mQueue = new PostQueue();
		// mBufferQueue = new LinkedList<PostBean>();
		mDBProvider = new DataBaseProvider(mContext);

		mMutex = new Object();

		/** 2014.8开关控制 */
		sCurrentProcessName = getCurProcessName(mContext);
		mCtrlMap = new Hashtable<String, CtrlBean>();

		mSharedPreferences = mContext.getSharedPreferences(CTRL_SP_NAME + mContext.getPackageName(),
				Context.MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		mExecutor = Executors.newSingleThreadExecutor();
		initUserInfo();
		if (sCurrentProcessName == null || sCurrentProcessName.equals(sMainProcessName)) {
			addTaskToExecutor(new Runnable() {
				@Override
				public void run() {
					long firstRunTime = mSharedPreferences.getLong(USER_FIRST_RUN_TIME, 0);
					if (firstRunTime == 0) {
						UtilTool.checkIsNewUser(mContext, mSharedPreferences);// 为新用户存入首次运行时间值
						firstRunTime = mSharedPreferences.getLong(USER_FIRST_RUN_TIME, 0);
					}

					if (System.currentTimeMillis() - firstRunTime > NEW_USER_VALID_TIME) {
						// 如果当前时间比第一次时间晚32小时，则该用户转变为老用户
						sIsNew = false;
					}
					lastIsNew = sIsNew;
					mSchedulerManager = SchedulerManager.getInstance(mContext); // 初始化task管理器

					IntentFilter filter = new IntentFilter();
					filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
					filter.addAction(Intent.ACTION_PACKAGE_ADDED);
					filter.addAction(BROADCAST_UPLOADDATA);
					mContext.registerReceiver(mReceiver, filter);

					getCtrlInfoFromDB(false); // 从DB中取老的控制开关
					lastGetTime = System.currentTimeMillis() - mSharedPreferences.getLong(CTRLINFO_LAST_GET_TIME, 0);
					GetCtrlInfoTask task = null;
					if (lastGetTime == 0 || lastGetTime >= DEFAULT_INTEVRALTIME) {
						// 如果距离上次获取开关的时间大于默认时间（8小时）
						task = new GetCtrlInfoTask(mContext, 0, DEFAULT_INTEVRALTIME);
					} else {
						task = new GetCtrlInfoTask(mContext, DEFAULT_INTEVRALTIME - lastGetTime, DEFAULT_INTEVRALTIME);
						addPostDataSchedule();
					}

					// pushOldData();
					addCtrlTask(task);
				}
			});

		} else {
			addTaskToExecutor(new Runnable() {
				@Override
				public void run() {
					IntentFilter filter = new IntentFilter();
					filter.addAction(BROADCAST_GETCTRLINFO);
					mContext.registerReceiver(mReceiver, filter);

					getCtrlInfoFromDB(false);
				}
			});
		}
	}

	public long getFirstRunTime() {
		return mSharedPreferences.getLong(USER_FIRST_RUN_TIME, System.currentTimeMillis());
	}

//	/**
//	 * 上传老版本(Version < 1.10)统计sdk保存的老数据（将老数据转移到新数据库中）
//	 */
//	private void pushOldData() {
//		addTaskToExecutor(new Runnable() {
//			@Override
//			public void run() {
//				LinkedList<PostBean> beanList = mDBProvider.queryOldSDKVersionData();
//				if (beanList != null && beanList.size() > 0) {
//					UtilTool.logStatic("find old data count:" + beanList.size());
//					for (PostBean postBean : beanList) {
//						postBean.mNetwork = Machine.NETWORKTYPE_ALL;
//						postBean.mIsOld = true;
//						ayncStartTask(postBean, true, null);
//					}
//					mDBProvider.deleteOldData(beanList);
//				}
//			}
//		});
//	}

	/**
	 * 给线程池中加入队列
	 * 
	 * @param runnable
	 */
	private void addTaskToExecutor(Runnable runnable) {
		DBAsyncTask task = new DBAsyncTask();
		task.addTask(runnable);
		try {
			if (!mExecutor.isShutdown()) {
				mExecutor.execute(task);
			}
		} catch (Exception e) {
			UtilTool.printException(e);
		}
	}

	/**
	 * 获取相应功能点（funid）当前时间统计开关状态
	 * 
	 * @param funid
	 * @return 开true;关false
	 */
	public boolean getCtrlInfo(final int funid) {
		CtrlBean bean = null;
		if (mCtrlMap.get(String.valueOf(funid)) != null) {
			bean = mCtrlMap.get(String.valueOf(funid));
		}
		if (bean != null) {
			if (System.currentTimeMillis() > bean.getValidTime()) {
				onOff = false;
			} else {
				onOff = true;
			}
		} else {
			onOff = false;
		}
		return onOff;
	}

	/**
	 * 启动循环获取开关信息的task
	 * 
	 * @param task
	 */
	private void addCtrlTask(final GetCtrlInfoTask task) {
		task.setmCallBack(new GetCtrlInfoCallBack() {
			@Override
			public void onFinish(Map<String, CtrlBean> map, boolean connectOK) {
				// 访问控制开关服务器完毕回调onFinish
				if (connectOK) {
					lastGetTime = System.currentTimeMillis();
					mDBProvider.deleteOldCtrlInfo();
					// 在sharedPreference中存入相应信息
					mEditor.putLong(CTRLINFO_LAST_GET_TIME, lastGetTime);
					mEditor.commit();

					// 更新DB控制信息表(ctrlInfo)
					mHashMapLock.lock();
					try {
						mCtrlMap.clear();
						if (map != null && map.size() != 0) {
							mCtrlMap.putAll(map);
							refreshCtrlInfoDB(map);
						}
					} finally {
						mHashMapLock.unlock();
					}
					// 在这里做一些漏传数据的保护
					mDBProvider.setAllDataOld();
					lastGetCtrlDBCount = mDBProvider.queryDataCount();
					refreshCtrlString();
					// 通知子进程控制开关更新完毕，刷新内存中的控制开关
					Intent intent = new Intent(BROADCAST_GETCTRLINFO);
					intent.putExtra(BROADCAST_INTENT_PKGNAME, mContext.getPackageName());
					mContext.sendBroadcast(intent);
					mIsGetCtrlFailed = false;
					mIsGettingCtrlInfo = false;
				} else {
					addPostDataSchedule();
					mDBProvider.setAllDataOld();
					mIsGetCtrlFailed = true;
					mIsGettingCtrlInfo = false;
				}
			}

			@Override
			public void onStart() {
				// 获取开关之前判断是否是新用户
				if (sIsNew) {
					long firstRunTime = mSharedPreferences.getLong(USER_FIRST_RUN_TIME, 0);
					if (System.currentTimeMillis() - firstRunTime > NEW_USER_VALID_TIME) {
						sIsNew = false;
					}
				}
				lastIsNew = sIsNew;

				// 访问服务器前调用onStart
				mIsGettingCtrlInfo = true;
				if (Machine.getNetworkType(mContext) != Machine.NETWORKTYPE_INVALID) {
					task.mIsNetWorkOK = true;
				} else {
					task.mIsNetWorkOK = false;
					mIsGetCtrlFailed = true;
				}
			}
		});
		mSchedulerManager.executeTask(task);
	}

	protected void refreshCtrlString() {

	}

	/**
	 * 按开关的上传周期启动上传数据TASK
	 */
	private void addPostDataSchedule() {
		addTaskToExecutor(new Runnable() {
			@Override
			public void run() {
				if (Machine.getNetworkType(mContext) == Machine.NETWORKTYPE_INVALID) {
					return;
				}

				if (mCtrlMap.size() > 0) {
					mSchedulerManager.stopPostDataTask();
					// 相同间隔时间的CtrlBean合并起来只启动一个task
					HashMap<Long, ArrayList<String>> taskMap = new HashMap<Long, ArrayList<String>>();
					HashMap<String, CtrlBean> ctrl = new HashMap<String, CtrlBean>();
					ctrl.putAll(mCtrlMap);
					for (String key : ctrl.keySet()) {
						CtrlBean bean = ctrl.get(key);
						if (bean == null) {
							continue;
						}
						Long intervalTime = bean.getIntervalTime();
						if (intervalTime == 0) {
							continue;
						}
						if (taskMap.containsKey(intervalTime)) {
							taskMap.get(intervalTime).add("" + bean.getFunID());
						} else {
							ArrayList<String> funId = new ArrayList<String>();
							funId.add("" + bean.getFunID());
							taskMap.put(intervalTime, funId);
						}
					}
					if (taskMap.isEmpty()) {
						return;
					}
					int i = 0;
					for (Long interval : taskMap.keySet()) {
						if (taskMap.get(interval).size() > 0) {
							StringBuffer buffer = new StringBuffer();
							for (String key : taskMap.get(interval)) {
								buffer.append(key + ",");
							}
							buffer.deleteCharAt(buffer.length() - 1);
							StaticPostTask task = new StaticPostTask(mContext, interval, buffer.toString(), i);
							task.setCallBack(new StaticPostCallBack() {
								@Override
								public void onFinish(String key) {
									postData(key);
								}
							});
							i++;
							mSchedulerManager.executeTask(task);
						}
					}
				}
			}
		});
	}

	private void postData(final String funId) {
		final LinkedList<PostBean> postBeanList = mDBProvider.queryPostDatas(funId);
		if (postBeanList != null && postBeanList.size() > 0) {
			mStatHandler.post(new Job() {

				@Override
				public void invoke() {
					for (PostBean postBean : postBeanList) {
						pushTOQueue(postBean);
					}
					startTask(false);
				}
			});
		}
	}

	/**
	 * 从DB中取现有的控制信息
	 * 
	 * @param async
	 *            是否需要异步操作
	 */
	private void getCtrlInfoFromDB(boolean async) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				mCtrlMap.clear();
				if (mDBProvider != null) {
					mCtrlMap = mDBProvider.queryCtrlInfo();
				}
			}
		};
		if (async) {
			addTaskToExecutor(runnable);
		} else {
			mHashMapLock.lock();
			try {
				runnable.run();
			} finally {
				mHashMapLock.unlock();
			}
		}
	}

	/**
	 * 更新最新的控制信息到数据库中
	 * 
	 * @param map
	 */
	private void refreshCtrlInfoDB(final Map<String, CtrlBean> map) {
		addTaskToExecutor(new Runnable() {

			@Override
			public void run() {
				mDBProvider.insertCtrlInfoAsync(map, new AsyncCallBack() {
					@Override
					public void onFinish() {
						addPostDataSchedule();
						Intent intent = new Intent(BROADCAST_GETCTRLINFO);
						intent.putExtra(BROADCAST_INTENT_PKGNAME, mContext.getPackageName());
						mContext.sendBroadcast(intent);
					}
				});
			}
		});

	}

	/**
	 * 初始化部分用户基本信息
	 * 
	 * @param context
	 */
	private void initUserInfo() {
		sAndroidId = Machine.getAndroidId(mContext);
		sCountry = Machine.getSimCountryIso(mContext);
		sOSVersionCode = android.os.Build.VERSION.SDK_INT;
		PackageManager manager = mContext.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
			sVersionCode = info.versionCode;
			sVersionName = info.versionName;
		} catch (Exception e) {
			sVersionCode = 1;
			sVersionName = "1.0";
		}
	}

	public static void initBasicInfo(String mainProcessName, String channel, String IMEI,
			String authority) {
		if (sIsBaseInfoInit) {
			return;
		}
		sMainProcessName = mainProcessName;
		sChannel = channel;
		sIMEI = IMEI;
		StaticDataContentProvider.init(authority);
		sIsBaseInfoInit = true;
	}

	public static synchronized StatisticsManager getInstance(Context context) {
		if (!sIsBaseInfoInit) {
			throw new IllegalStateException("Must invoke \"StatisticsManager.initBasicInfo\" method first!");
		}
		if (mSelf == null && context != null) {
			Context appContext = context.getApplicationContext();
			if (appContext != null) {
				mSelf = new StatisticsManager(appContext);
			} else {
				mSelf = new StatisticsManager(context);
			}
		}
		return mSelf;
	}

	/**
	 * 重要！！需要在进程退出的时候调用
	 */
	public void destory() {
		if (UtilTool.isEnableLog()) {
			UtilTool.logStatic("destroy sdk");
		}
		mQuit = true;
		try {
			if (mContext != null) {
				mContext.unregisterReceiver(mReceiver);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			if (mDBProvider != null) {
				mDBProvider.destory();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (sCurrentProcessName == null || sCurrentProcessName.equals(sMainProcessName)) {
			SchedulerManager.destory();
		}
		// if (ExecutorFactory.getExecutor() != null
		// && !ExecutorFactory.getExecutor().isShutdown()) {
		// ExecutorFactory.getExecutor().shutdown();
		// }

		mSelf = null;

	}

	/**
	 * 设置是否打印log
	 * 
	 * @param onOff
	 */
	public void enableLog(boolean onOff) {
		UtilTool.enableLog(onOff);
	}

	/**
	 * <br>
	 * 功能简述:测试模式，上传到测试服务器 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public void setDebugMode() {
		sDebugMode = true;
		enableLog(true);
	}

	/**
	 * <br>
	 * 功能简述:测试模式，上传到测试服务器 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param url
	 */
	public boolean getDebugMode() {
		return sDebugMode;
	}

	/**
	 * <br>
	 * 功能简述:启动任务队列 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void startTask(boolean isOld) {
		try {
			if (mContext != null) {
				if (Machine.getNetworkType(mContext) != Machine.NETWORKTYPE_INVALID) {
					if (mQuit) {
						mQuit = false;
						postDataInQueue();
						if (UtilTool.isEnableLog()) {
							UtilTool.log(TAG, "start loop task");
						}
					} else {
						if (UtilTool.isEnableLog()) {
							UtilTool.log(TAG, "task already running");
						}
					}
				} else {
					if (!isOld) {
						PostBean bean = mQueue.pop();
						while (bean != null) {
							mDBProvider.setDataOld(bean);
							bean = mQueue.pop();
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 　 统计19接口
	 * 
	 * @param produceId
	 *            产品对应的id
	 * @param channel
	 *            　产品所属渠道
	 * @param isPay
	 *            是否付费用户　false：否；true：是
	 * @param needRootInfo
	 *            是否需要root信息
	 * @param key
	 * @param 是否新用户
	 */
	public void upLoadBasicInfoStaticData(String channel, boolean isPay, boolean needRootInfo,
			String key, boolean isNew) {
		upLoadBasicInfoStaticData(channel, isPay, needRootInfo, key, isNew, -1, null);
	}

	/**
	 * 　 统计19接口
	 * 
	 * @param produceId
	 *            产品对应的id
	 * @param channel
	 *            　产品所属渠道
	 * @param isPay
	 *            是否付费用户　false：否；true：是
	 * @param needRootInfo
	 *            是否需要root信息
	 * @param key
	 * @param 是否新用户
	 * @param versionCode
	 *            自定义版本号
	 * @param versionName
	 *            自定义版本名
	 */
	public void upLoadBasicInfoStaticData(String channel, boolean isPay, boolean needRootInfo,
			String key, boolean isNew, int versionCode, String versionName) {
		upLoadBasicInfoStaticData(channel, isPay, needRootInfo, key, isNew, versionCode,
				versionName, null);
	}

	/**
	 * 　 统计19接口
	 * 
	 * @param produceId
	 *            产品对应的id
	 * @param channel
	 *            　产品所属渠道
	 * @param isPay
	 *            是否付费用户　false：否；true：是
	 * @param needRootInfo
	 *            是否需要root信息
	 * @param key
	 * @param 是否新用户
	 * @param versionCode
	 *            自定义版本号
	 * @param versionName
	 *            自定义版本名
	 * @param appendData
	 *            附加信息
	 */
	public void upLoadBasicInfoStaticData(String channel, boolean isPay, boolean needRootInfo,
			String key, boolean isNew, int versionCode, String versionName, String appendData) {
		String productID = mContext.getPackageName();
		upLoadBasicInfoStaticData(productID, channel, isPay, needRootInfo, key, isNew, versionCode,
				versionName, appendData);
	}

	/**
	 * 　 统计19接口：http://wiki.3g.net.cn/pages/viewpage.action?pageId=6914524
	 * 
	 * @param channel
	 *            　产品所属渠道
	 * @param pay
	 *            付费状况，各产品自己定义
	 * @param needRootInfo
	 *            是否需要root信息
	 * @param key
	 * @param 是否新用户
	 */
	public void upLoadBasicInfoStaticData(String channel, boolean isPay, boolean needRootInfo,
			String key, boolean isNew, String appendData) {
		String productID = mContext.getPackageName();
		upLoadBasicInfoStaticData(productID, channel, isPay, needRootInfo, key, isNew, appendData);
	}

	/**
	 * 　 统计19接口：http://wiki.3g.net.cn/pages/viewpage.action?pageId=6914524
	 * 
	 * @param produceId
	 *            产品对应的id
	 * @param channel
	 *            　产品所属渠道
	 * @param pay
	 *            付费状况，各产品自己定义
	 * @param needRootInfo
	 *            是否需要root信息
	 * @param key
	 * @param 是否新用户
	 */
	public void upLoadBasicInfoStaticData(String productID, String channel, boolean isPay,
			boolean needRootInfo, String key, boolean isNew, String appendData) {
		upLoadBasicInfoStaticData(productID, channel, isPay, needRootInfo, key, isNew, -1, null, appendData);
	}
	
	/**
	 * 　 统计19接口：http://wiki.3g.net.cn/pages/viewpage.action?pageId=6914524
	 * 
	 * @param produceId
	 *            产品对应的id
	 * @param channel
	 *            　产品所属渠道
	 * @param pay
	 *            付费状况，各产品自己定义
	 * @param needRootInfo
	 *            是否需要root信息
	 * @param key
	 * @param 是否新用户
	 */
	public void upLoadBasicInfoStaticData(String productID, String channel, boolean isPay,
			boolean needRootInfo, String key, boolean isNew, int versionCode, String versionName, String appendData) {
		final PostBean bean = new PostBean();
		bean.mIsOld = true;
		bean.mTimeStamp = UtilTool.getBeiJinTime(System.currentTimeMillis());
		bean.mNetwork = Machine.NETWORKTYPE_ALL;
		bean.mFunId = BASIC_FUN_ID;
		bean.mId = getUniqueID();
		bean.mChannel = channel;
		bean.mPayType = String.valueOf(UtilTool.boolean2Int(isPay));
		bean.mProductID = productID;
		bean.mNeedRootInfo = needRootInfo;
		bean.mKey = key;
		bean.mIsNew = isNew;
		bean.mDataOption = PostBean.DATAHANDLECODE_ENCODE_ZIP;
		StringBuffer buffer = getBasicStatisticsInfo(bean, versionCode, versionName);
		if (appendData != null) {
			buffer.append(STATISTICS_DATA_SEPARATE_STRING);
			buffer.append(appendData);
		}
		buffer.append("||||" + getUserRatio());
		buffer.append(getMachineInfoExtend());
		bean.mData = buffer.toString();
		uploadStaticData(101, 207, "207||||gl_dc||1||||||||" + userStatus());
		ayncStartTask(bean, true, null);
	}

	private String getMachineInfoExtend() {
		StringBuffer buffer = new StringBuffer(STATISTICS_DATA_SEPARATE_STRING);
		// 像素密度
		buffer.append(DrawUtils.sDensityDpi);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// cpu型号
		buffer.append(CpuManager.getCpuName());
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// cpu频率
		try {
			double freq = Double.parseDouble(CpuManager.getMaxCpuFreq());
			freq = freq / 1000 / 1000;
			String cpuFreq = freq + "GHZ";
			buffer.append(cpuFreq);
		} catch (Exception e) {
			buffer.append("unknown GHZ");
		}
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// cpu核数
		buffer.append(CpuManager.getNumCores());
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// ram容量
		String[] ramTotal = Machine.fileSize(CpuManager.getTotalInternalMemorySize());
		String[] ramAvailable = Machine.fileSize(CpuManager.getAvailableInternalMemorySize(mContext));
		buffer.append(ramTotal[0] + ramTotal[1] + "," + ramAvailable[0] + ramAvailable[1]);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// rom容量
		buffer.append(Machine.getROMStorage());
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// sd卡容量
		long[] sdCardInfo = Machine.getSDCardMemory();
		String[] cardTotal = Machine.fileSize(sdCardInfo[0]);
		String[] cardAvailable = Machine.fileSize(sdCardInfo[1]);
		buffer.append(cardTotal[0] + cardTotal[1] + "," + cardAvailable[0] + cardAvailable[1]);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		return buffer.toString();
	}

	/**
	 * <br>
	 * 功能简述:用于17和19以外的统计协议，sdk只负责传送 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public void upLoadStaticData(String staticData) {
		if (staticData != null) {
			upLoadStaticData(null, staticData, PostBean.DATAHANDLECODE_ENCODE_ZIP);
		}
	}

	/**
	 * <br>
	 * 功能简述: <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param url
	 *            指定url，
	 * @param staticData
	 *            传输的数据
	 * @param opCode
	 *            0 不做任何处理，1 压缩，2 加密，3,压缩加密
	 */
	public void upLoadStaticData(final String url, final String staticData, final int dataOpCode) {
		addTaskToExecutor(new Runnable() {
			@Override
			public void run() {
				if (staticData != null) {
					PostBean bean = new PostBean();
					bean.mDataOption = dataOpCode;
					bean.mUrl = url;
					bean.mId = getUniqueID();
					bean.mData = staticData;
					bean.mIsOld = true;
					bean.mTimeStamp = UtilTool.getBeiJinTime(System.currentTimeMillis());
					bean.mNetwork = Machine.NETWORKTYPE_ALL;
					ayncStartTask(bean, true, null);
				}
			}
		});
	}

	private void ayncStartTask(final PostBean bean, final boolean isOld, final OnInsertDBListener insertDBListener) {
		if (insertDBListener != null) {
			insertDBListener.onBeforeInsertToDB();
		}
		mDBProvider.insertPostDataAsync(bean, new AsyncCallBack() {
			@Override
			public void onFinish() {
				if (insertDBListener != null) {
					insertDBListener.onInsertToDBFinish();
				}
				mStatHandler.post(new Job() {

					@Override
					public void invoke() {
						pushTOQueue(bean);
						// 上传
						startTask(isOld);
					}
				});
			}
		});
	}

	@Deprecated
	public void upLoadAdStaticData(String staticData) {
		if (staticData != null) {
			staticData = "37||" + staticData;
			upLoadStaticData(null, staticData, PostBean.DATAHANDLECODE_ENCODE_ZIP);
		}
	}

	/**
	 * <br>
	 * 功能简述:协议19拼装 <br>
	 * 功能详细描述:日志序列||产品 ID||Android ID||日志打印时间||OS||ROM||机型||语言地区 <br>
	 * ||渠道||版本号||版本名||产品客户端类型||手机号码||手机运营商||是否付费用户||是否GO桌面用户 <br>
	 * ||goid||是否已root||是否新用户||来源产品包名||来源产品id||正版标识 <br>
	 * 注意:
	 * 
	 * @param buffer
	 */
	@Deprecated
	public StringBuffer getBasicStatisticsInfo(PostBean bean, int versionCode, String versionName) {
		if (mContext == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		// 日志序列号
		buffer.append(bean.mFunId);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		// 产品 ID
		buffer.append(bean.mProductID);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		// Android ID
		buffer.append(Machine.getAndroidId(mContext));
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		buffer.append(bean.mTimeStamp);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		buffer.append(android.os.Build.VERSION.RELEASE);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		buffer.append(android.os.Build.VERSION.INCREMENTAL);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		buffer.append(android.os.Build.MODEL);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		if (sCountry == null || sCountry.trim().equals("")) {
			sCountry = Machine.getSimCountryIso(mContext);
		}
		buffer.append(Machine.getSimCountryIso(mContext));
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		if (bean.mChannel != null) {
			bean.mChannel = bean.mChannel.replaceAll("\r\n", "");
			bean.mChannel = bean.mChannel.replaceAll("\n", "");
		}
		buffer.append(bean.mChannel);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		PackageManager pm = mContext.getPackageManager();
		if (versionCode == -1 || versionName == null) {
			PackageInfo info = getVersionInfo();
			if (info != null) {
				versionCode = info.versionCode;
				versionName = info.versionName;
			}
		}
		buffer.append(versionCode);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		buffer.append(versionName);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		int isTablet = Machine.isTablet(mContext) ? 2 : 1;
		buffer.append(isTablet);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		buffer.append(-1);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		buffer.append(Machine.getSimOperator(mContext));
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		buffer.append(bean.mPayType);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		Intent intent = new Intent(GOLAUNCHER_MAIN_ACTION);
		//
		List<ResolveInfo> infos = null;
		try {
			infos = pm.queryIntentActivities(intent, 0);

		} catch (Exception e) {
			// TODO: handle exception
		}
		if (infos != null && infos.size() > 0) {
			buffer.append(1);
		} else {
			buffer.append(0);
		}

		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		if (sGoId == null) {
			sGoId = UtilTool.getGOId(mContext);
		}
		buffer.append(sGoId);

//		if (bean.mNeedRootInfo) {
			buffer.append(STATISTICS_DATA_SEPARATE_STRING);
			buffer.append(Machine.isRootSystem());
//		} else {
//			buffer.append(STATISTICS_DATA_SEPARATE_STRING);
//			buffer.append("-1");
//		}

		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		buffer.append(UtilTool.boolean2Int(bean.mIsNew));

		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// buffer.append(getSrcPkg());

		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// buffer.append(getSrcPid());

		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		buffer.append(bean.mKey);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		buffer.append(Build.CPU_ABI);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		buffer.append(DrawUtils.sWidthPixels);
		buffer.append("*");
		buffer.append(DrawUtils.sHeightPixels);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		buffer.append(mContext.getPackageName());
//		UtilTool.log(TAG, buffer.toString());
		return buffer;

	}

	/**
	 * <br>
	 * 上传普通统计
	 * 
	 * @param logId
	 *            protocol ID
	 * @param funid
	 *            功能点ID
	 * @param buffer:传入你自己拼装的从【功能点ID】开始的数据，详情见wiki 例如：
	 * <br>
	 *            101
	 *            拼装好的统计数据格式:功能点ID||统计对象||操作代码||操作结果||入口||Tab分类||位置||关联对象||备注<br>
	 *            102 拼装好的统计数据格式:功能点ID||布局信息||类型||位置||备注信息
	 *            
	 *            @param insertDBListener db插入监听
	 */
	public void uploadStaticData(final int logId, final int funid, final String buffer,
			final OnInsertDBListener insertDBListener) {
		uploadStaticDataForOptions(logId, funid, buffer, insertDBListener);
	}
	
	/**
	 * <br>
	 * 上传普通统计
	 * 
	 * @param logId
	 *            protocol ID
	 * @param funid
	 *            功能点ID
	 * @param buffer:传入你自己拼装的从【功能点ID】开始的数据，详情见wiki 例如：
	 * <br>
	 *            101
	 *            拼装好的统计数据格式:功能点ID||统计对象||操作代码||操作结果||入口||Tab分类||位置||关联对象||备注<br>
	 *            102 拼装好的统计数据格式:功能点ID||布局信息||类型||位置||备注信息
	 */
	public void uploadStaticData(final int logId, final int funid, final String buffer) {
		uploadStaticDataForOptions(logId, funid, buffer, null);
	}

	public final static int URL_RQUEST_FUNID = 1030;

	public void uploadRequestUrl(String url) {
		// 先判断是否立即上传
		PostBean bean = new PostBean();
		bean.mFunId = URL_RQUEST_FUNID;
		bean.mTimeStamp = UtilTool.getBeiJinTime(System.currentTimeMillis());
		bean.bn = "20150112";
		bean.mId = getUniqueID();
		bean.mData = url;
		bean.mIsOld = true;
		bean.mNetwork = Machine.NETWORKTYPE_ALL;
		ayncStartTask(bean, true, null);
	}

	/**
	 * 带选项的统计接口
	 * 
	 * @param logId
	 *            协议号
	 * @param funid
	 *            功能号
	 * @param buffer
	 *            拼装好的数据，格式见wiki,
	 *            http://wiki.3g.net.cn/pages/viewpage.action?pageId=11273639
	 * @param options
	 *            可选选项：<br>
	 *            {@link com.tcl.mig.staticssdk.beans.OptionBean#OPTION_INDEX_IMMEDIATELY_CARE_SWITCH}
	 *            为是否立即上传的标识符(在开关限制下立即上传)，传入true or false;<br>
	 *            {@link com.tcl.mig.staticssdk.beans.OptionBean#OPTION_INDEX_IMMEDIATELY_ANYWAY}
	 *            为是否立即上传的标识符(不管开关，直接、立即上传)，传入true or false;<br>
	 *            {@link com.tcl.mig.staticssdk.beans.OptionBean#OPTION_INDEX_POSITION
	 *            OPTION_INDEX_POSITION} 为位置信息；类型为字符串，如"105.23,155.88"<br>
	 *            {@link com.tcl.mig.staticssdk.beans.OptionBean#OPTION_INDEX_ABTEST
	 *            OPTION_INDEX_ABTEST} 为ABTest值；类型为字符串,如"A"
	 * @param insertDBListener
	 *            插入DB的监听
	 */
	public void uploadStaticDataForOptions(final int logId, final int funid, final String buffer,
			final OnInsertDBListener insertDBListener, final OptionBean... options) {
		if (UtilTool.isStringNoValue(buffer)) {
			throw new NullPointerException("Static data buffer can not be null");
		}
		if (stopUpload) {
			return;
		}

		addTaskToExecutor(new Runnable() {
			public void run() {
				if (isImmediatelyData(logId, funid) || checkOptionImmediatelyAnyway(options)) {
					// 先判断是否立即上传
					CtrlBean ctrlBean = new CtrlBean(System.currentTimeMillis() + 1000000, 0, "2014103", "", funid,
							System.currentTimeMillis(), Machine.NETWORKTYPE_ALL, 0);
					dataHandle(ctrlBean, logId, funid, buffer, insertDBListener, options);
				} else if (mCtrlMap.get(String.valueOf(funid)) != null) {
					// 取内存中的控制开关进行相应操作
					CtrlBean ctrlBean = mCtrlMap.get(String.valueOf(funid));
					dataHandle(ctrlBean, logId, funid, buffer, insertDBListener, options);
				} else {
					if (UtilTool.isEnableLog()) {
						// 开关为关，不进行任何操作
						UtilTool.logStatic("this funid's switch is closed, funid=" + funid
								+ ", please make sure the switch has been opened.");
					}
				}
			}
		});
	}

	/**
	 * <br>
	 * 这是一个可以自定义参数的方法。上传普通统计
	 * 
	 * @param logId
	 *            protocol ID
	 * @param funid
	 *            功能点ID
	 * @param buffer:传入你自己拼装的从【功能点ID】开始的数据，详情见wiki 例如：
	 * <br>
	 *            101
	 *            拼装好的统计数据格式:功能点ID||统计对象||操作代码||操作结果||入口||Tab分类||位置||关联对象||备注<br>
	 *            102 拼装好的统计数据格式:功能点ID||布局信息||类型||位置||备注信息
	 *            
	 *            
	 *            备注：其他参数可以自定义
	 */
	public void uploadStaticDataForDiy(final int logId, final int funid, final String buffer,
			final OnInsertDBListener insertDBListener, final String versionCode, final String versionName,
			final String channel, final OptionBean... options) {
		if (UtilTool.isStringNoValue(buffer)) {
			throw new NullPointerException("Static data buffer can not be null");
		}

		if (stopUpload) {
			return;
		}
		
		addTaskToExecutor(new Runnable() {
			public void run() {
				if (isImmediatelyData(logId, funid) || checkOptionImmediatelyAnyway(options)) {
					// 先判断是否立即上传
					CtrlBean ctrlBean = new CtrlBean(System.currentTimeMillis() + 1000000, 0, "2014103", "", funid,
							System.currentTimeMillis(), Machine.NETWORKTYPE_ALL, 0);
					dataHandleForDiy(ctrlBean, logId, funid, buffer, insertDBListener, versionCode, versionName,
							channel, options);
				} else if (mCtrlMap.get(String.valueOf(funid)) != null) {
					// 取内存中的控制开关进行相应操作
					CtrlBean ctrlBean = mCtrlMap.get(String.valueOf(funid));
					dataHandleForDiy(ctrlBean, logId, funid, buffer, insertDBListener, versionCode, versionName,
							channel, options);
				} else {
					if (UtilTool.isEnableLog()) {
						// 开关为关，不进行任何操作
						UtilTool.logStatic("this funid's switch is closed, funid=" + funid
								+ ", please make sure the switch has been opened.");
					}
				}
			}
		});
	}

	/**
	 * 103协议和功能id为207的统计数据为强制立即上传
	 * 
	 * @param logId
	 * @param funid
	 * @return
	 */
	private boolean isImmediatelyData(int logId, int funid) {
		if (logId == 103 || funid == 207) {
			return true;
		}
		return false;
	}

	/**
	 * 处理数据并上传（或保存在数据库）
	 * 
	 */
	private void dataHandle(CtrlBean ctrlBean, int logId, int funid, String buffer,
			final OnInsertDBListener insertDBListener, OptionBean[] options) {
		dataHandleForDiy(ctrlBean, logId, funid, buffer, insertDBListener, null, null, null, options);
	}

	private void dataHandleForDiy(CtrlBean ctrlBean, int logId, int funid, String buffer,
			final OnInsertDBListener insertDBListener, final String versionCode, final String versionName,
			final String channel, OptionBean[] options) {
		if (ctrlBean.getValidTime() > System.currentTimeMillis()) {
			PostBean bean = new PostBean();
			bean.mFunId = funid;
			bean.mTimeStamp = UtilTool.getBeiJinTime(System.currentTimeMillis());
			bean.bn = ctrlBean.getBn();
			bean.mId = getUniqueID();
			final String mID = bean.mId;
			StringBuffer stringBuffer = getBasicInfo(logId, bean, versionCode, versionName, channel);

			appendOptionABTest(stringBuffer, options);
			appendOptionPosition(stringBuffer, options);

			bean.mData = stringBuffer.append(buffer).toString();
			if (isImmediatelyData(logId, funid) || ctrlBean.getIntervalTime() == 0) {
				bean.mIsOld = true;
			} else {
				bean.mIsOld = false;
			}
			bean.mNetwork = ctrlBean.getNetwork();
			if (ctrlBean.getIntervalTime() == 0 || checkOptionImmediately(options)) {
				if (sCurrentProcessName == null || sCurrentProcessName.equals(sMainProcessName)) {
					ayncStartTask(bean, true, insertDBListener);
				} else {
					if (insertDBListener != null) {
						insertDBListener.onBeforeInsertToDB();
					}
					mDBProvider.insertPostDataAsync(bean, new AsyncCallBack() {
						@Override
						public void onFinish() {
							if (insertDBListener != null) {
								insertDBListener.onInsertToDBFinish();
							}
							Intent intent = new Intent(BROADCAST_UPLOADDATA);
							intent.putExtra(BROADCAST_INTENT_ID, mID);
							intent.putExtra(BROADCAST_INTENT_PKGNAME, mContext.getPackageName());
							mContext.sendBroadcast(intent);
						}
					});
				}
			} else {
				if (insertDBListener != null) {
					insertDBListener.onBeforeInsertToDB();
				}
				mDBProvider.insertPostDataAsync(bean, new AsyncCallBack() {

					@Override
					public void onFinish() {
						if (insertDBListener != null) {
							insertDBListener.onInsertToDBFinish();
						}
					}
				});
			}
		}
	}

	/**
	 * 获取当前用户的随机数值
	 * @return
	 */
	public int getUserRatio() {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences(
				StatisticsManager.CTRL_SP_NAME + mContext.getPackageName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		int mUserRatio = sharedPreferences.getInt(GetCtrlInfoTask.SP_USER_RATIO, -1);
		if (mUserRatio == -1) {
			Random random = new Random();
			mUserRatio = random.nextInt(GetCtrlInfoTask.USER_RATIO_MAX);
			editor.putInt(GetCtrlInfoTask.SP_USER_RATIO, mUserRatio);
			editor.commit();
		}
		return mUserRatio;
	}

	/**
	 * check是否选项中带有立即上传选项
	 * 
	 * @param options
	 * @return
	 */
	private boolean checkOptionImmediatelyAnyway(OptionBean[] options) {
		if (options.length > 0) {
			for (OptionBean optionBean : options) {
				if (optionBean.getOptionID() == OptionBean.OPTION_INDEX_IMMEDIATELY_ANYWAY) {
					return (Boolean) optionBean.getOptionContent();
				}
			}
		}
		return false;
	}

	/**
	 * check是否选项中带有立即上传选项
	 * 
	 * @param options
	 * @return
	 */
	private boolean checkOptionImmediately(OptionBean[] options) {
		if (options.length > 0) {
			for (OptionBean optionBean : options) {
				if (optionBean.getOptionID() == OptionBean.OPTION_INDEX_IMMEDIATELY_CARE_SWITCH) {
					return (Boolean) optionBean.getOptionContent();
				}
			}
		}
		return false;
	}

	private void appendOptionABTest(StringBuffer buffer, OptionBean[] options) {
		if (options.length > 0) {
			for (OptionBean optionBean : options) {
				if (optionBean.getOptionID() == OptionBean.OPTION_INDEX_ABTEST) {
					buffer.append((String) optionBean.getOptionContent());
					break;
				}
			}
		}
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
	}

	private void appendOptionPosition(StringBuffer buffer, OptionBean[] options) {
		if (options.length > 0) {
			for (OptionBean optionBean : options) {
				if (optionBean.getOptionID() == OptionBean.OPTION_INDEX_POSITION) {
					if (!UtilTool.isStringNoValue((String) optionBean.getOptionContent())) {
						buffer.append(getCPUInfo((String) optionBean.getOptionContent()));
						return;
					}
				}
			}
		}
		buffer.append(getCPUInfo(null));
	}

	private String getUniqueID() {
		long id = Math.abs(System.nanoTime());
		String ID = String.valueOf(id);
		return ID;
		// String buffer = String.valueOf(hashCode) +
		// String.valueOf(System.currentTimeMillis());
		// try {
		// MessageDigest md5 = MessageDigest.getInstance("MD5");
		// byte[] srcBytes;
		// srcBytes = buffer.getBytes("utf8");
		// md5.update(srcBytes);
		// byte[] resultBytes = md5.digest();
		// StringBuilder ret = new StringBuilder(resultBytes.length << 1);
		// for (int i = 0; i < resultBytes.length; i++) {
		// ret.append(Character.forDigit((resultBytes[i] >> 4) & 0xf, 16));
		// ret.append(Character.forDigit(resultBytes[i] & 0xf, 16));
		// }
		// if (UtilTool.isStringNoValue(ret.toString())) {
		// } else {
		// return ret.toString();
		// }
		// } catch (NoSuchAlgorithmException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// return buffer;
	}

	/**
	 * <br>
	 * 功能描述：2014-8新统计协议SDK信息<br>
	 * SDK内部提供拼装信息：日志序列||Android ID||GOID||IMIE||日志打印时间||<br>
	 * 国家||渠道||版本号||版本名||GADID||日志采集批次||ABTest标识||<br>
	 * 静态信息||动态信息||<br>
	 * 
	 * @param logId
	 *            日志序列 GO桌面元素布局统计（102） <br>
	 *            GO桌面操作统计协议（101） <br>
	 * 
	 * @return
	 */
	private StringBuffer getBasicInfo(int logId, PostBean bean, final String versionCode, final String versionName,
			final String channel) {
		StringBuffer buffer = new StringBuffer();
		// 日志序列号
		buffer.append(logId);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		// Android ID
		buffer.append(Machine.getAndroidId(mContext));
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		// GO ID
		if (sGoId == null) {
			sGoId = UtilTool.getGOId(mContext);
		}
		buffer.append(sGoId);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		// IMEI
		buffer.append(sIMEI);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);

		// logTime
		buffer.append(bean.mTimeStamp);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// COuntry
		buffer.append(sCountry);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// Channel
		if (UtilTool.isStringNoValue(channel)) {
			buffer.append(sChannel);
		} else {
			buffer.append(channel);
		}
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// VersionCode
		if (UtilTool.isStringNoValue(versionCode)) {
			buffer.append(sVersionCode);
		} else {
			buffer.append(versionCode);
		}
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// VersionName
		if (UtilTool.isStringNoValue(versionName)) {
			buffer.append(sVersionName);
		} else {
			buffer.append(versionName);
		}
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// GADID
		if (sGADID == null) {
			sGADID = getGoogleAdvertisingId();
		}
		buffer.append(sGADID);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// bn
		buffer.append(bean.bn);
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// ABtest
		if (mEnableSoftManager) {

		}

		return buffer;
	}
	
	private String getGoogleAdvertisingId() {
		/*
		try {
			Info adInfo = null;
			adInfo = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
			if (adInfo != null) {
				return adInfo.getId();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return "UNABLE-TO-RETRIEVE";
		*/
		return null;
	}

	/**
	 * 获取CPU信息
	 * 
	 * @param position
	 * @return
	 */
	private String getCPUInfo(String position) {
		StringBuffer buffer = new StringBuffer();
		String maxCpuFormat = "0";
		String maxMenFormat = "0";
		double pCpu = 0;
		double pMen = 0;
		DecimalFormat df = new DecimalFormat("#.00");
		try {
			double maxCpu = 0;
			double curCpu = 0;
			if (!TextUtils.isEmpty(CpuManager.getMaxCpuFreq()) && !TextUtils.isEmpty(CpuManager.getCurCpuFreq())) {
				maxCpu = Double.parseDouble(CpuManager.getMaxCpuFreq());
				curCpu = Double.parseDouble(CpuManager.getCurCpuFreq());
				if (maxCpu != 0) {
					pCpu = curCpu / maxCpu * 100;
				}
			}
			double maxMen = CpuManager.getTotalInternalMemorySize() / 1024.0 / 1024.0;
			double curMen = (CpuManager.getTotalInternalMemorySize() - CpuManager
					.getAvailableInternalMemorySize(mContext)) / 1024.0 / 1024.0;
			maxCpuFormat = df.format(maxCpu / 1024.0 / 1024.0);
			maxMenFormat = df.format(CpuManager.getTotalInternalMemorySize() / 1024.0 / 1024.0);
			// 动态信息
			if (maxMen != 0) {
				pMen = curMen / maxMen * 100;
			}
		} catch (Exception e) {
			// TODO: handle exception
			maxCpuFormat = "0";
			maxMenFormat = "0";
			pCpu = 0;
			pMen = 0;
		}
		buffer.append("{\"cpu\":\"" + maxCpuFormat + "GHz\",\"men\":\"" + maxMenFormat + "MB\"}");
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		if (position == null) {
			buffer.append("{\"cpu\":\"" + df.format(pCpu) + "%\",\"men\":\"" + df.format(pMen) + "%\"");
		} else {
			buffer.append("{\"cpu\":\"" + df.format(pCpu) + "%\",\"men\":\"" + df.format(pMen) + "%\",\"position\":\""
					+ position + "\"");
		}

		buffer.append(",\"net\":\"" + Machine.getNetworkType(mContext) + "\"}");
		buffer.append(STATISTICS_DATA_SEPARATE_STRING);
		return buffer.toString();
	}

	/**
	 * <br>
	 * 功能简述:网络发送接口 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param bean
	 */
	private void postData(PostBean bean) {
		if (bean != null && mContext != null) {
			BaseConnectHandle handle = PostFactory.produceHandle(mContext, bean.mFunId);
			handle.postData(bean);
			if (bean.mState == PostBean.STATE_POSTED) {
				if (UtilTool.isEnableLog()) {
					UtilTool.log(TAG, "a request has been posted");
				}
			} else {
				if (UtilTool.isEnableLog()) {
					UtilTool.log(TAG, "post fundid:" + bean.mFunId + " failed!");
				}
			}
		}
	}

	private void quitPost() {
		// saveDataTODB();
		if (mQuit) {
			mQueue.clear();
		}

	}

	private void postDataInQueue() {
		HashSet<String> funidList = new HashSet<String>();
		for (;;) {
			try {
				if (mQuit) {
					lastUpdateTime = System.currentTimeMillis();
					if (UtilTool.isEnableLog()) {
						UtilTool.log(TAG, "quit post!");
					}
					quitPost();
					return;
				}
				Thread.yield();
				PostBean bean = mQueue.pop();
				// 隊列空,查看buffer是否有,buffer也无的话退出
				if (bean == null) {
					if (getPostDataFromDBTask(funidList)) {
						if (UtilTool.isEnableLog()) {
							UtilTool.log(TAG, "now push data from DB!");
						}
					} else {
						if (UtilTool.isEnableLog()) {
							UtilTool.log(TAG, "no data quit!");
						}
						mQuit = true;
					}
					continue;
				}
				funidList.add(String.valueOf(bean.mFunId));

				if (bean.mReTryCount < PostBean.MAX_RETRY_COUNT) {
					postData(bean);
				}

				if (bean.mState == PostBean.STATE_POSTED) {
					mDBProvider.deletePushData(bean);
				} else {
					bean.mReTryCount++;
					if (bean.mReTryCount < PostBean.MAX_RETRY_COUNT) {
						mQueue.push(bean);
						//									sleep(3 * 60 * 1000);
					} else {
						bean.mIsOld = true;
						mDBProvider.setDataOld(bean);
						mQuit = true;
						if (UtilTool.isEnableLog()) {
							UtilTool.logStatic("quit loop");
						}
						break;
					}
				}
			} catch (Exception e) {
				UtilTool.printException(e);
			}
		}
	}
	
	private void checkPostTask() {
		mStatHandler.post(new Job() {
			
			@Override
			public void invoke() {
				if (getOldDataFromDB()) {
					startTask(true);
				}
			}
		});
	}

	/**
	 * <br>
	 * 功能简述:检查db中是否有未成功发送的数据 <br>
	 * 功能详细描述: <br>
	 * 注意:上次检查如果与本次时间差大于10s则检查，否则跳过
	 */
	private boolean getPostDataFromDBTask(HashSet<String> funid) {
		synchronized (mMutex) {
			LinkedList<PostBean> list = mDBProvider.queryPostDatas(funid);
			if (list.isEmpty()) {
				list.addAll(mDBProvider.queryOldData());
			}
			if (list != null && !list.isEmpty()) {
				for (PostBean postBean : list) {
					if (postBean.mNetwork <= Machine.getNetworkType(mContext)) {
						mQueue.push(postBean);
					}
				}
				return true;
			}
			return false;
		}
	}

	/**
	 * 功能简述:检查db中是否有未成功发送的数据
	 * 
	 * @return
	 */
	private boolean getOldDataFromDB() {
		synchronized (mMutex) {
			long now = System.currentTimeMillis();
			if (now - mLastImportFromDB > 10000) {
				mLastImportFromDB = now;
				LinkedList<PostBean> list = mDBProvider.queryOldData();
				if (list != null && !list.isEmpty()) {
					for (PostBean postBean : list) {
						if (postBean.mNetwork <= Machine.getNetworkType(mContext)) {
							mQueue.push(postBean);
						}
					}
					return true;
				}
			}
			return false;
		}
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
//			UtilTool.logStatic("receive action:" + action + "MAIN:" + sMainProcessName + ", "
//					+ intent.getStringExtra(BROADCAST_INTENT_PKGNAME));
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				if (Machine.getNetworkType(context) != Machine.NETWORKTYPE_INVALID) {
					if (UtilTool.isEnableLog()) {
						UtilTool.log(TAG, "net connection ok , check post queue!");
					}
					if (sCurrentProcessName == null
							|| sCurrentProcessName.equals(sMainProcessName)) {
						checkPostTask(); // 查询需要上传但未上传成功的数据
						if (mIsGetCtrlFailed && !mIsGettingCtrlInfo) { // 未成功获取开关信息则开始获取开关信息
							GetCtrlInfoTask ctrlTask = new GetCtrlInfoTask(mContext, 0,
									DEFAULT_INTEVRALTIME);
							addCtrlTask(ctrlTask);
						}
					}
				} else {
					if (UtilTool.isEnableLog()) {
						UtilTool.log(TAG, "lost network,quit!");
					}
					mQuit = true;
				}
				return;
			} else if (intent.getStringExtra(BROADCAST_INTENT_PKGNAME) != null
					&& intent.getStringExtra(BROADCAST_INTENT_PKGNAME).equals(mContext.getPackageName())) {
				if (action.equals(BROADCAST_GETCTRLINFO)) {
					getCtrlInfoFromDB(true);
				} else if (action.equals(BROADCAST_UPLOADDATA)) {
					final PostBean bean = mDBProvider.queryPostData(intent
							.getStringExtra(BROADCAST_INTENT_ID));
					if (bean != null) {
						mStatHandler.post(new Job() {

							@Override
							public void invoke() {
								pushTOQueue(bean);
								startTask(true);
							}
						});
					}
				}
			}
		}
	};

	public void uploadAllData() {
		synchronized (mMutex) {
			long now = System.currentTimeMillis();
			if (now - mLastImportFromDB > 10000) {
				mLastImportFromDB = now;
				final LinkedList<PostBean> list = mDBProvider.queryAllData();
				if (list != null && !list.isEmpty()) {
					mStatHandler.post(new Job() {
						
						@Override
						public void invoke() {
							for (PostBean postBean : list) {
								if (postBean.mNetwork <= Machine.getNetworkType(mContext)) {
									mQueue.push(postBean);
								}
							}
							startTask(true);
						}
					});
				}
			}
		}
	}

	/**
	 * <br>
	 * 功能简述:获得GOID <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param context
	 * @return
	 */
	public static String getGOID(Context context) {
		if (sGoId == null) {
			sGoId = UtilTool.getGOId(context);
		}
		return sGoId;
	}

	// /**
	// * <br>
	// * 功能简述: <br>
	// * 功能详细描述: <br>
	// * 注意:获得软件源产品ID
	// *
	// * @return
	// */
	// public String getSrcPid() {
	// if (mEnableSoftManager) {
	// return mSoftManager.getSrcPid();
	// } else {
	// return "-1";
	// }
	// }
	//
	// /**
	// * <br>
	// * 功能简述:获得软件源包名 <br>
	// * 功能详细描述: <br>
	// * 注意:
	// *
	// * @return
	// */
	// public String getSrcPkg() {
	// if (mEnableSoftManager) {
	// return mSoftManager.getSrcPkg();
	// } else {
	// return "-1";
	// }
	// }

	private PackageInfo getVersionInfo() {
		PackageManager pm = mContext.getPackageManager();
		PackageInfo info;
		try {
			info = pm.getPackageInfo(mContext.getPackageName(), 0);
			return info;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

//	public void enableSoftManager() {
//		mEnableSoftManager = true;
//	}

	private void pushTOQueue(PostBean bean) {
		if (bean.mNetwork <= Machine.getNetworkType(mContext)) {
			mQueue.push(bean);
		} else {
			mDBProvider.setDataOld(bean);
		}
	}

	public String getVerionName() {
		return SDK_VER;
	}

	public static String getCurProcessName(Context context) {
		int pid = android.os.Process.myPid();
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
			if (appProcess.pid == pid) {
				sCurrentProcessName = appProcess.processName;
				return appProcess.processName;
			}
		}
		return null;
	}

	public void restoreDefault() {
		mEditor.clear();
		mEditor.commit();
	}

	// public OnInsertDBListener getInsertDBListener() {
	// return mInsertDBListener;
	// }
	//
	// public void setInsertDBListener(OnInsertDBListener mInsertDBListener) {
	// this.mInsertDBListener = mInsertDBListener;
	// }

	/**
	 * 察看在统计sdk中用户是否是最新用户
	 * @return
	 */
	public boolean userIsNew() {
		// long firstRunTime = mSharedPreferences.getLong(USER_FIRST_RUN_TIME,
		// 0);
		// boolean userNew = sIsNew;
		// if (firstRunTime == 0) {
		// UtilTool.checkIsNewUser(mContext, mSharedPreferences);//
		// 为新用户存入首次运行时间值
		// firstRunTime = mSharedPreferences.getLong(USER_FIRST_RUN_TIME, 0);
		// }
		//
		// if (System.currentTimeMillis() - firstRunTime > NEW_USER_VALID_TIME)
		// {
		// // 如果当前时间比第一次时间晚32小时，则该用户转变为老用户
		// return false;
		// }
		// return true;
		return sIsNew;
	}

	private String userStatus() {
		StringBuffer buffer = new StringBuffer();
		HashMap<String, CtrlBean> map = new HashMap<String, CtrlBean>();
		map.putAll(mCtrlMap);
		for (String key : map.keySet()) {
			CtrlBean bean = map.get(key);
			if (bean != null) {
				buffer.append(bean.getFunID() + "_" + bean.getBn() + "_" + bean.getValidTime() + ",");
			}
		}
		if (buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		buffer.append(SPERATE + lastGetCtrlDBCount);
		buffer.append(SPERATE + mDBProvider.queryDataCount());
		buffer.append(SPERATE + getUserRatio());
		buffer.append(SPERATE + UtilTool.getBeiJinTime(lastGetTime));
		buffer.append(SPERATE + UtilTool.getBeiJinTime(lastUpdateTime));
		if (lastIsNew) {
			buffer.append(SPERATE + "1");
		} else {
			buffer.append(SPERATE + "0");
		}
		return buffer.toString();
	}
	
	public void setStop(boolean stop) {
		stopUpload = stop;
	}
	
	public void uploadStaticDataByTranscation(final int logId, final int funId, final String buffer) {
		String key = String.valueOf(logId) + "_" + String.valueOf(funId);
		ArrayList<String> bufferList = mBufferMap.get(key);
		if (bufferList == null) {
			bufferList = new ArrayList<String>();
			mBufferMap.put(key, bufferList);
		}
		synchronized (bufferList) {
			bufferList.add(buffer);
		}
	}

	public void commitAllData(final int logId, final int funId) {
		String key = String.valueOf(logId) + "_" + String.valueOf(funId);
		final ArrayList<String> bufferList = mBufferMap.get(key);
		if (bufferList != null && !bufferList.isEmpty()) {
			final BaseConnectHandle handle = PostFactory.produceHandle(mContext, funId);
			mStatHandler.post(new Job() {

				@Override
				protected void invoke() {
					synchronized (bufferList) {
						Iterator<String> it = bufferList.iterator();
						while (it.hasNext()) {
							Thread.yield();
							String buffer = it.next();
							boolean success = handle.postData(funId, buffer);
							if (success) {
								it.remove();
							}
						}
					}
				}
			});
		}
	}
	
	private abstract class Job implements Runnable {

		protected abstract void invoke();

		@Override
		public void run() {
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND
					+ android.os.Process.THREAD_PRIORITY_LESS_FAVORABLE);
			invoke();
		}
	}
}
