package com.tcl.mig.staticssdk;
/**
 * 
 * 存db监听
 * @author luozhiping
 *
 */
public interface OnInsertDBListener {
	void onBeforeInsertToDB();
	void onInsertToDBFinish();
}
