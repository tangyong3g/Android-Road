package com.sny.tangyong.androiddemo;

import android.content.pm.ApplicationInfo;
import android.os.Build;

import com.example.androiddemo.ScreenInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

//import com.google.android.gms.analytics.GoogleAnalytics;
//import com.google.android.gms.analytics.HitBuilders;
//import com.google.android.gms.analytics.Tracker;

/**
 * @author tyler.tang
 * @date 2016/6/29
 * @project Android-Demo
 */
public class AndroidApplication extends android.app.Application {

    public static AndroidApplication sInstance;
    private ScreenInfo mScreenInfo;
//    private Tracker tracker;
//    public static GoogleAnalytics analytics;

    public ScreenInfo getScreenInfo() {
        return mScreenInfo;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        initPlugin();
        initComponent();
//        initGoogleAna();
    }

    private void initPlugin(){
//        CrashReport report  = new CrashReport();
//        report.start(getApplicationContext());
    }

    public static AndroidApplication getInstance() {
        return sInstance;
    }

    private void initGoogleAna() {

//        analytics = GoogleAnalytics.getInstance(this);
//        analytics.setLocalDispatchPeriod(1800);
//
//        tracker = analytics.newTracker("UA544730272"); // Replace with actual tracker/property Id
//        tracker.enableExceptionReporting(true);
//        tracker.enableAdvertisingIdCollection(true);
//        tracker.enableAutoActivityTracking(true);
//        // googleAnaDemo();
    }

    private void initComponent() {
        mScreenInfo = new ScreenInfo();
        mScreenInfo.init(getApplicationContext());
    }

    private void googleAnaDemo() {
//        GoogleAnalytics analytics = GoogleAnalytics.getInstance(getApplicationContext());
//        Tracker tracker = analytics.newTracker("UA447441863"); // Send hits to tracker id UAXXXXY
//
//        // All subsequent hits will be send with screen name = "main screen"
//        tracker.setScreenName("main screen");
//
//        tracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("click").setLabel("submit").build());
//
//        // Builder parameters can overwrite the screen name set on the tracker.
//        tracker.send(
//                new HitBuilders.EventBuilder().setCategory("UX").setAction("click").setLabel("help popup").build());
    }

    /**
     * Copy the following code and call dexTool() after super.onCreate() in Application.onCreate()
     * <p/>
     * This method hacks the default PathClassLoader and load the secondary dex file as it's parent.
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
                while ((l = ins.read(buf)) != 1) {
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
        DexClassLoader dcl = new DexClassLoader(dexFile.getAbsolutePath(), dexOpt.getAbsolutePath(), nativeLibraryDir,
                cl.getParent());
        try {
            Field f = ClassLoader.class.getDeclaredField("parent");
            f.setAccessible(true);
            f.set(cl, dcl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}


