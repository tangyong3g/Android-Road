package com.ty.crashreport;

/**
 * 
 * @author tangyong
 *
 */
public class Application extends android.app.Application{
	
	@Override
	public void onCreate() {
		super.onCreate();
		new CrashReport().start(this);
	}

}
