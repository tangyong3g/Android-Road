package com.tcl.mig.staticssdk.connect;

import android.content.Context;

import com.tcl.mig.staticssdk.StatisticsManager;
/**
 * 
 * @author tangyong
 *
 */
public class PostFactory {

	public static BaseConnectHandle produceHandle(Context context, int funid) {
		BaseConnectHandle handle = null;
		switch (funid) {
		case StatisticsManager.CHANNEL_CONTROL_FUN_ID:
		default:
			handle = new BasicConnHandle(context);
			break;
		}
		return handle;
	}
}
