package com.ty.crashreport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;

import com.example.android_begin_gl_3d.ScreenInfo;

import dalvik.system.DexClassLoader;

/**
 * 
 * @author tangyong
 * 
 */
public class Application extends android.app.Application {

	public static Application sInstance;
	private ScreenInfo mScreenInfo;
	
	public static final String THREAD_NAME = "thread";
	private  static HandlerThread mLightThread = new HandlerThread(THREAD_NAME);
	static{
		mLightThread.start();
	}
	
	private static Handler sHandler = new Handler(mLightThread.getLooper());
	

	public HandlerThread getLightThread() {
		return mLightThread;
	}

	public ScreenInfo getScreenInfo() {
		return mScreenInfo;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		dexTool();
		new CrashReport().start(this);
		sInstance = this;
		
		//初始化各个组件的值
		initApplicationComponent();
	}

	
	/**
	 * 
	 * @Title: initApplicationComponent 
	 * @Description: TODO
	 * @return: void
	 */
	private void initApplicationComponent() {
		
		mScreenInfo = new ScreenInfo();
		mScreenInfo.init(this);

		
	}

	public static Application getInstance() {
		return sInstance;
	}

	/**
	 * Copy the following code and call dexTool() after super.onCreate() in
	 * Application.onCreate()
	 * <p>
	 * This method hacks the default PathClassLoader and load the secondary dex
	 * file as it's parent.
	 */
	private void dexTool() {

		File dexDir = new File(getFilesDir(), "dlibs");
		dexDir.mkdir();
		File dexFile = new File(dexDir, "libs.apk");
		File dexOpt = new File(dexDir, "opt");
		dexOpt.mkdir();
		try {
			InputStream ins = getAssets().open("DexJarAndroid.apk");
			if (dexFile.length() != ins.available()) {
				FileOutputStream fos = new FileOutputStream(dexFile);
				byte[] buf = new byte[4096];
				int l;
				while ((l = ins.read(buf)) != -1) {
					fos.write(buf, 0, l);
				}
				fos.close();
			}
			ins.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		ClassLoader cl = getClassLoader();
		ApplicationInfo ai = getApplicationInfo();
		String nativeLibraryDir = null;
		if (Build.VERSION.SDK_INT > 8) {
			nativeLibraryDir = ai.nativeLibraryDir;
		} else {
			nativeLibraryDir = "/data/data/" + ai.packageName + "/lib/";
		}
		DexClassLoader dcl = new DexClassLoader(dexFile.getAbsolutePath(),
				dexOpt.getAbsolutePath(), nativeLibraryDir, cl.getParent());
		try {
			Field f = ClassLoader.class.getDeclaredField("parent");
			f.setAccessible(true);
			f.set(cl, dcl);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void postRunnableToLightThread(Runnable task){
		
		
		sHandler.post(task);
		
	}

}
