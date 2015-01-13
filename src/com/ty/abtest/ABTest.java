package com.ty.abtest;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.text.TextUtils;

import com.ty.common.PreferencesManager;

/**
 * ABTEST测试
 * 
 * 类名称：ABTest
 * 类描述：
 * 创建人：makai
 * 修改人：makai
 * 修改时间：2014年11月21日 上午9:58:50
 * 修改备注：
 * @version 1.0.0
 *
 */
@TestInfo(startTime = "2014-12-26 23:59:00", endTime = "2014-12-31 23:59:00")
public class ABTest {

	public static final String AUTO_USER = "auto";

	private static final String SAVE_SP_KEY = "abtest";

	private String mUser;

	private String mEndTime;

	private String mStartTime;

	private boolean mRebuild;

	private PreferencesManager mSp;

	private static ABTest sInstance;

	public ABTest(Context context) {
		mSp = PreferencesManager.getInstance(context);
		init();
	}

	public static ABTest getInstance(Context context) {
		if (null == sInstance) {
			sInstance = new ABTest(context);
		}
		return sInstance;
	}

	private void init() {
		if (getClass().isAnnotationPresent(TestInfo.class)) {
			TestInfo testInfo = getClass().getAnnotation(TestInfo.class);
			mRebuild = testInfo.rebuild();
			mStartTime = testInfo.startTime();
			mEndTime = testInfo.endTime();
		} else {
			throw new RuntimeException("the test info not exist");
		}
		String user;
		if (AUTO_USER.equals(DebugSwitchList.sABTest_USER_TYPE)) {
			user = mSp.getString("user", "");
			if (TextUtils.isEmpty(user)) {
				if (mRebuild) {
					mUser = null;
				}
				if (!isValid(mStartTime, mEndTime)) {
					user = getDefaultUser();
				} else {
					user = genUser();
				}
				mSp.putString("user", user);
			}
		} else {
			user = DebugSwitchList.sABTest_USER_TYPE;
			mSp.putString("user", user);
		}
		mUser = user;
	}

	/**
	 * 设置升级用户类型
	 * setUpgradeUser(这里用一句话描述这个方法的作用)
	 * (这里描述这个方法适用条件 – 可选) 
	 *void
	 * @exception 
	 * @since  1.0.0
	 */
	public void setUpgradeUser() {
		mUser = getUpGradeUser();
		mSp.putString("user", mUser);
	}

	/**
	 * 是否是测试用户
	 * isTestUser(这里用一句话描述这个方法的作用)
	 * (这里描述这个方法适用条件 – 可选)
	 * @param user	{@link TestUser}
	 * @return 
	 *boolean
	 * @exception 
	 * @since  1.0.0
	 */
	public boolean isTestUser(String user) {
		if (!TextUtils.isEmpty(user)) {
			return user.equals(getUser());
		}
		return false;
	}

	/**
	 * 获取当前测试用户
	 * getUser(这里用一句话描述这个方法的作用)
	 * (这里描述这个方法适用条件 – 可选)
	 * @return 
	 *String
	 * @exception 
	 * @since  1.0.0
	 */
	public String getUser() {
		if (TextUtils.isEmpty(mUser)) {
			mUser = mSp.getString("user", genUser());
		}
		return mUser;
	}

	private String genUser() {
		try {
			List<Field> userList = getUserList();
			int index = (int) (Math.random() * userList.size());
			Field field = userList.get(index);
			return field.get(null).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private List<Field> getUserList() {
		Field[] fields = TestUser.class.getDeclaredFields();
		if (fields == null) {
			throw new RuntimeException("not find test user");
		}
		List<Field> list = new ArrayList<Field>();
		for (Field field : fields) {
			if (field.isAnnotationPresent(TestUserModel.class)) {
				TestUserModel userModel = field.getAnnotation(TestUserModel.class);
				if (userModel.isTestUser()) {
					for (int i = 0; i < userModel.odds(); i++) {
						list.add(field);
					}
				}
			}
		}
		return list;
	}

	private String getDefaultUser() {
		Field[] fields = TestUser.class.getDeclaredFields();
		if (fields == null) {
			throw new RuntimeException("not find test user");
		}
		for (Field field : fields) {
			if (field.isAnnotationPresent(TestUserModel.class)) {
				TestUserModel userModel = field.getAnnotation(TestUserModel.class);
				if (userModel.isTestUser() && userModel.isDefaultUser()) {
					try {
						return field.get(null).toString();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	private String getUpGradeUser() {
		Field[] fields = TestUser.class.getDeclaredFields();
		if (fields == null) {
			throw new RuntimeException("not find test user");
		}
		for (Field field : fields) {
			if (field.isAnnotationPresent(TestUserModel.class)) {
				TestUserModel userModel = field.getAnnotation(TestUserModel.class);
				if (userModel.isUpGradeUser()) {
					try {
						return field.get(null).toString();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	private boolean isValid(String start, String end) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		try {
			Date before = df.parse(start);
			Date now = df.parse(df.format(new Date()));
			Date after = df.parse(end);
			if (now.before(after) && now.after(before)) {
				return true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static final int NOT_HAS_LOCKER = 0;
	private static final int HAS_LOCKER = 1;

	private int mHasLockerState = -1;

	public boolean isUserHasLocker(Context context) {
		return false;
		//		if (mHasLockerState == -1) {
		//			PreferencesManager pm = PreferencesManager.getSharedPreference(context.getApplicationContext(), ContentView.LOCKER_UNLOCK_ANIM, Context.MODE_PRIVATE);
		//			final boolean hasUsedLockerInPreVersion = !pm.getBoolean(ContentView.LOCKER_FIRST_START, true);
		//			mHasLockerState = hasUsedLockerInPreVersion ? HAS_LOCKER : NOT_HAS_LOCKER;
		//		}
		//		return mHasLockerState == HAS_LOCKER;
		// return isTestUser(TestUser.USER_I) && Machine.isSamsung();
		// return !(isTestUser(TestUser.USER_E) || isTestUser(TestUser.USER_A) || isTestUser(TestUser.USER_C) || isTestUser(TestUser.USER_D)
		// || isTestUser(TestUser.USER_G) || (isTestUser(TestUser.USER_J) && !Machine.isSamsung()));
	}
}