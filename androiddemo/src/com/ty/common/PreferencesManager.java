/*
 * 文 件 名:  PreferencesManager.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  caoshilei
 * 修改时间:  2014-9-1
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.ty.common;

import junit.framework.Assert;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * <br>类描述:sharepreference持久化配置数据管理
 * <br>功能详细描述:
 * 
 * @author  caoshilei
 * @date  [2014-9-3]
 */
public class PreferencesManager {
	/** 桌面 Preference配置文件名称*/
	public static final String DEFAULT_PREFERENCE_NAME = "launcher_lab.setting";

	/** preference配置对象 */
	private SharedPreferences mPreference = null;
	private Editor mEditor = null;

	private static PreferencesManager sInstance;

	public static PreferencesManager getInstance(Context context) {
		if (null == sInstance) {
			synchronized (PreferencesManager.class) {
				if (null == sInstance) {
					sInstance = new PreferencesManager(context);
				}
			}
		}
		return sInstance;
	}

	private PreferencesManager(Context context) {
		mPreference = context.getSharedPreferences(DEFAULT_PREFERENCE_NAME, Context.MODE_PRIVATE);
		Assert.assertNotNull(mPreference);

		mEditor = mPreference.edit();
		Assert.assertNotNull(mEditor);
	}

	public String getString(String key, String defValue) {
		return mPreference.getString(key, defValue);
	}

	public boolean putString(String key, String value) {
		mEditor.putString(key, value);
		return mEditor.commit();
	}

	public boolean getBoolean(String key, boolean defValue) {
		return mPreference.getBoolean(key, defValue);
	}

	public boolean putBoolean(String key, boolean value) {
		mEditor.putBoolean(key, value);
		return mEditor.commit();
	}

	public int getInt(String key, int defValue) {
		return mPreference.getInt(key, defValue);
	}

	public boolean putInt(String key, int value) {
		mEditor.putInt(key, value);
		return mEditor.commit();
	}

	public long getLong(String key, long defValue) {
		return mPreference.getLong(key, defValue);
	}

	public boolean putLong(String key, long value) {
		mEditor.putLong(key, value);
		return mEditor.commit();
	}
}
