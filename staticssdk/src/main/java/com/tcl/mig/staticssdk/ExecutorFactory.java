package com.tcl.mig.staticssdk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//CHECKSTYLE:OFF
public class ExecutorFactory {
	private static ExecutorService mExecutor;
	
	public static ExecutorService getExecutor(){
		if (mExecutor == null) {
			mExecutor =  Executors.newSingleThreadExecutor();
		}
		return mExecutor;
	}
}
