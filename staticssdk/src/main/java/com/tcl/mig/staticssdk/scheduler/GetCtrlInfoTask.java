package com.tcl.mig.staticssdk.scheduler;

import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.tcl.mig.staticssdk.StatisticsManager;
import com.tcl.mig.staticssdk.beans.CtrlBean;
import com.tcl.mig.staticssdk.utiltool.UtilTool;

//CHECKSTYLE:OFF

/**
 * 循环获取控制开关信息的任务，每8小时获取一次
 * 
 * @author luozhiping
 * 
 */
public class GetCtrlInfoTask extends SchedulerTask {
	private static final String KEY = "get_ctrl_info_task";
	private static final String CTRLINFO_THREAD_NAME = "get_ctrlinfo_thread";
	private static final int MILL_PER_HOUR = 60 * 1000 * 60;
	// private static final int MILL_PER_HALFMIN = 10 * 1000;

	private static final String COMMA = ",";

	private static final String REQUEST_KEY_PKGNAME = "prd_id";
	private static final String REQUEST_KEY_ANDROID_ID = "android_id";
	private static final String REQUEST_KEY_VERSION = "version";
	/**产品id：http://wiki.3g.net.cn/pages/viewpage.action?pageId=6914524*/
	public static final int USER_RATIO_MAX = 100;
	private static final int USER_TYPE_ALL = 0;
	private static String CTRLINFO_DEBUG_URL = "http://61.145.124.212:8083/GOClientData/DC";
	private static final String CTRLINFO_URL = "http://goupdate.3g.cn/GOClientData/DC";
	// private static final String CTRLINFO_URL =
	// "http://192.168.214.167:8080/GOClientData/DC";
	private GetCtrlInfoCallBack mCallBack;
	private static final String VERSION_NAME_V120 = "20150723";
	private int mUserRatio = -1;
	public static final String SP_USER_RATIO = "user_ratio";

	private static final String DEBUG_CTRL_INFO = "{"
			+ "\"ctrl_info\":"
			+ "["
			+ "{\"bn\":\"20140808001\",\"country\":\"us,cn\",\"channel\":\"\",\"version_code\":\"\",\"os_version_code\":\"\","
			+ "\"network\":\"0\",\"duration\":\"720\",\"user_type\":\"0\",\"user_ratio\":"
			+ "\"100\",\"stat_id\":\"201\",\"upload_cycle\":\"1\",\"valid_time\":\"1411603200\"},"
			+ "]"

			+ "}";
	private Context mContext;

	/**
	 * 
	 * @param startTime
	 *            任务开始时间
	 * @param intervalTime
	 *            任务间隔时间
	 */
	public GetCtrlInfoTask(Context context, long startTime, long intervalTime) {
		setKey(context, KEY);
		mContext = context;
		initUserRatio(context);
		setIntervalTime(intervalTime);
		setStartTime(System.currentTimeMillis() + startTime);
		if (UtilTool.isEnableLog()) {
			UtilTool.log(StatisticsManager.TAG, "Get ctrlInfo task constructed!:" + getStartTime());
		}
	}

	public int initUserRatio(Context context) {
		SharedPreferences spOld = context.getSharedPreferences("ctrl_sp",
				Context.MODE_PRIVATE);
		boolean oldRatio = spOld.contains(SP_USER_RATIO);
		if (!oldRatio) {
			SharedPreferences sharedPreferences = context.getSharedPreferences(
					StatisticsManager.CTRL_SP_NAME + context.getPackageName(),
					Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			mUserRatio = sharedPreferences.getInt(SP_USER_RATIO, -1);
			if (mUserRatio == -1) {
				Random random = new Random();
				mUserRatio = random.nextInt(USER_RATIO_MAX);
				editor.putInt(SP_USER_RATIO, mUserRatio);
				editor.commit();
			}
		} else {
			mUserRatio = spOld.getInt(SP_USER_RATIO, -1);

			SharedPreferences sharedPreferences = context.getSharedPreferences(
					StatisticsManager.CTRL_SP_NAME + context.getPackageName(),
					Context.MODE_PRIVATE);
			if (mUserRatio != -1) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putInt(SP_USER_RATIO, mUserRatio);
				editor.commit();
				spOld.edit().remove(SP_USER_RATIO).commit();
			}
		}

		return mUserRatio;
	}

	public void setmCallBack(GetCtrlInfoCallBack mCallBack) {
		this.mCallBack = mCallBack;
	}

	private boolean httpConnectedOK = false;

	@Override
	public void execute() {
		if (UtilTool.isEnableLog()) {
			UtilTool.log(StatisticsManager.TAG, "Execute getCtrlInfoTask!");
		}
		Thread thread = new Thread(CTRLINFO_THREAD_NAME) {
			@Override
			public void run() {
				if (mCallBack != null) {
					mCallBack.onStart();
				}

				if (mIsNetWorkOK) {
					String ctrlInfo = null;
					// ctrlInfo = DEBUG_CTRL_INFO;
					ctrlInfo = getCtrlInfo();

					Map<String, CtrlBean> map = null;
					if (UtilTool.isStringNoValue(ctrlInfo)) {
						httpConnectedOK = false;
					} else {
						httpConnectedOK = true;
						if (UtilTool.isEnableLog()) {
							UtilTool.log(StatisticsManager.TAG, "NewCtrlInfo:" + ctrlInfo);
						}
						map = parsedCtrlInfo(ctrlInfo);
						writeCtrlInfoToFile(ctrlInfo, map);
					}
					mCallBack.onFinish(map, httpConnectedOK);

				} else {
					if (UtilTool.isEnableLog()) {
						UtilTool.logStatic("Get ctrl info network is not ok and quit");
					}
					httpConnectedOK = false;
					mCallBack.onFinish(null, httpConnectedOK);
				}
			}
		};

		thread.start();
	}

	private void writeCtrlInfoToFile(String ctrlInfo, Map<String, CtrlBean> map) {
		final String huanHang = "\r\n";
		String fileName = "CtrlInfoLog.txt";
		String date = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
				.format(new java.util.Date(System.currentTimeMillis()));
		StringBuffer message = new StringBuffer(huanHang + huanHang + huanHang
				+ "Time:" + date + huanHang + "Connect uri:" + lastUri
				+ huanHang + ctrlInfo + huanHang);
		message.append("RequestInfo:" + requestInfo + huanHang);
		message.append("UserInfo:" + " userRatio:" + mUserRatio
				+ ", userContry:" + StatisticsManager.sCountry + huanHang);

		message.append("Newest ctrlBean In DB:");

		for (String key : map.keySet()) {
			message.append(huanHang);
			CtrlBean ctrlBean = map.get(key);
			String bn = ctrlBean.getBn();
			String updateTime = ctrlBean.getUpdateTime();
			int funId = ctrlBean.getFunID();
			int priority = ctrlBean.getPriority();
			long interval = ctrlBean.getIntervalTime();
			long valid = ctrlBean.getValidTime();
			message.append("[ctrlBean: funid:" + funId + ", bn:" + bn
					+ ", updateTime:" + updateTime + ", priority:" + priority
					+ ", interval:" + interval + ", validTime:" + valid + "]");
		}
		try {
			FileOutputStream fout = mContext.openFileOutput(fileName,
					Context.MODE_APPEND);
			byte[] bytes = message.toString().getBytes();
			fout.write(bytes);

			fout.close();
		} catch (Exception e) {
			UtilTool.printException(e);
		}
	}

	/***
	 * json解析
	 * 
	 * @param ctrlInfo
	 *            json字符串
	 */
	private Map<String, CtrlBean> parsedCtrlInfo(String ctrlInfo) {
		Map<String, CtrlBean> map = new HashMap<String, CtrlBean>();
		try {
			// 待添加如果为空则为符合所有的代码
			JSONArray jsonArray = new JSONObject(ctrlInfo)
					.getJSONArray("ctrl_info");

			for (int i = 0; i < jsonArray.length(); i++) {
				// 获取数据
				JSONObject json = (JSONObject) jsonArray.opt(i);
				String bn = json.getString("bn");
				String country = json.getString("country");
				String channel = json.getString("channel");
				String versionCode = json.getString("version_code");
				String osVersionCode = json.getString("os_version_code");
				int network = json.getInt("network");
				// String duration = json.getString("duration");
				long validTime = json.getLong("valid_time");
				int userType = json.getInt("user_type");
				int userRatio = json.getInt("user_ratio");
				String statId = json.getString("stat_id");
				String uploadCycle = json.getString("upload_cycle");
				String updateTime = json.getString("update_time");
				int priority = json.getInt("priority");
				// 检查该条控制信息是否符合本机
				if (isUserRatioFit(userRatio) && isCountryFit(country)
						&& isChannelFit(channel)
						&& isVersionCodeFit(versionCode)
						&& isOsVersionCodeFit(osVersionCode)
						&& isUserTypeFit(userType)) {
					long intervalTimeLong = Long.valueOf(uploadCycle);
					intervalTimeLong *= MILL_PER_HOUR;
					if (!UtilTool.isStringNoValue(statId)) {
						for (String funId : statId.split(COMMA)) {
							if (UtilTool.isStringNoValue(funId)) {
								continue;
							}
							CtrlBean bean = new CtrlBean(validTime,
									intervalTimeLong, bn, updateTime,
									Integer.valueOf(funId.trim()),
									System.currentTimeMillis(), network,
									priority);
							putCtrlBean(map, bean); // 检查优先级
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return map;
	}

	private void putCtrlBean(Map<String, CtrlBean> map, CtrlBean bean) {
		CtrlBean oldBean = map.get(String.valueOf(bean.getFunID()));
		if (oldBean != null && oldBean.getPriority() >= bean.getPriority()) {
			return;
		}
		map.put(String.valueOf(bean.getFunID()), bean);
		if (UtilTool.isEnableLog()) {
			UtilTool.logStatic("ctrlBeanfunId:" + bean.getFunID() + ", validtime:"
					+ bean.getValidTime() + ", " + "intervaltime:" + bean.getIntervalTime());
		}
	}

	private boolean isUserTypeFit(int userType) {
		return userType == USER_TYPE_ALL ? true : StatisticsManager.sIsNew;
	}

	private boolean isUserRatioFit(int userRatio) {
//		UtilTool.logStatic("userRatio:" + mUserRatio + ", ratio:" + userRatio);
		if (mUserRatio == -1) {
			initUserRatio(mContext);
		}
		if (mUserRatio < userRatio) {
			return true;
		}
		return false;
	}

	private boolean isOsVersionCodeFit(String osVersionCode) {
		return isInfoFit(String.valueOf(StatisticsManager.sOSVersionCode),
				osVersionCode);
	}

	private boolean isVersionCodeFit(String versionCode) {
		return isInfoFit(String.valueOf(StatisticsManager.sVersionCode),
				versionCode);
	}

	private boolean isChannelFit(String channel) {
		return isInfoFit(StatisticsManager.sChannel, channel);
	}

	private boolean isCountryFit(String country) {
		return isInfoFit(StatisticsManager.sCountry, country);
	}

	/**
	 * 判断开关控制信息是否与本机信息温和
	 * 
	 * @param localInfo
	 *            本机信息
	 * @param requestInfo
	 *            控制信息
	 * @return
	 */
	private boolean isInfoFit(String localInfo, String requestInfo) {
		if (UtilTool.isStringNoValue(requestInfo)
				|| UtilTool.isStringNoValue(localInfo)) {
			return true;
		}
		for (String requestInfoArray : requestInfo.split(COMMA)) {
			if (UtilTool.isEnableLog()) {
				UtilTool.logStatic("serverInfo:" + requestInfoArray + ", localInfo:" + localInfo);
			}
			if (requestInfoArray.trim().equalsIgnoreCase(localInfo)) {
				return true;
			}
		}
		return false;
	}

	private String lastUri = "";
	private String requestInfo = "";

	/**
	 * 网络操作：获取开关控制信息
	 * 
	 * @return
	 */
	private String getCtrlInfo() {
		HttpPost httpPost = null;
		if (StatisticsManager.sDebugMode) {
			httpPost = new HttpPost(CTRLINFO_DEBUG_URL);
		} else {
			httpPost = new HttpPost(CTRLINFO_URL);
		}
		HttpResponse httpResponse = null;
		try {
			requestInfo = getCtrlInfoParams();
			if (UtilTool.isEnableLog()) {
				UtilTool.logStatic("Client info to Server:" + requestInfo + " where:"
						+ httpPost.getURI());
			}
			lastUri = httpPost.getURI().toString();
			httpPost.setEntity(new StringEntity(requestInfo));
			httpResponse = new DefaultHttpClient().execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
				String result = EntityUtils.toString(httpResponse.getEntity());
				return result;
			} else {
				return null;
			}
		} catch (Exception e) {
			UtilTool.printException(e);
		}
		return null;
	}

	/**
	 * 获取post json请求字符串
	 * 
	 * @return
	 */
	private String getCtrlInfoParams() {
		JSONArray jsonArray = new JSONArray();
		JSONObject json = new JSONObject();
		try {
			json.put(REQUEST_KEY_PKGNAME, mContext.getPackageName());
			json.put(REQUEST_KEY_ANDROID_ID, StatisticsManager.sAndroidId);
			json.put(REQUEST_KEY_VERSION, VERSION_NAME_V120);
			jsonArray.put(json);
		} catch (Exception e) {
		}
		return jsonArray.toString();
	}

	public boolean mIsNetWorkOK = false;

	public interface GetCtrlInfoCallBack {
		/**
		 * 访问服务器前调用函数
		 */
		public void onStart();

		/**
		 * 访问服务器完毕回调函数
		 * 
		 * @param map
		 *            新的控制信息map
		 * @param connectOk
		 *            是否成功访问服务器
		 */
		public void onFinish(Map<String, CtrlBean> map, boolean connectOk);
	}

}
