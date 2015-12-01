package com.ty.crashreport;

import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import com.example.androiddemo.ScreenInfo;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

/**
 * @author tangyong
 */
public class Application extends android.app.Application {

    public static Application sInstance;
    private ScreenInfo mScreenInfo;

    public static final String THREAD_NAME = "thread";
    private static HandlerThread mLightThread = new HandlerThread(THREAD_NAME);

    private Tracker tracker;
    public static GoogleAnalytics analytics;

    static {
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
        //初始化GA
        initGoogleAna();
    }


    private void initGoogleAna() {

        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-54473027-2"); // Replace with actual tracker/property Id
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

//        googleAnaDemo();

    }

    private void googleAnaDemo() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(getApplicationContext());
        Tracker tracker = analytics.newTracker("UA-44744186-3"); // Send hits to tracker id UA-XXXX-Y

        // All subsequent hits will be send with screen name = "main screen"
        tracker.setScreenName("main screen");

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("UX")
                .setAction("click")
                .setLabel("submit")
                .build());

        // Builder parameters can overwrite the screen name set on the tracker.
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("UX")
                .setAction("click")
                .setLabel("help popup")
                .build());
    }


    /**
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
     * <p/>
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

    public void postRunnableToLightThread(Runnable task) {

        sHandler.post(task);

    }

}
