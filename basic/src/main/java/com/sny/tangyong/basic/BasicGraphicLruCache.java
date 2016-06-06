package com.sny.tangyong.basic;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.ImageView;

/**
 * m LruCache缓存程序中的使用的图片
 */
public class BasicGraphicLruCache {

    private static BasicGraphicLruCache instance;
    private LruCache<String, Bitmap> mMemoryCache;

    /**
     * 得到实例
     *
     * @return BasicGraphicLruCache
     */
    public static BasicGraphicLruCache getInstance() {

        if (instance == null) {
            instance = new BasicGraphicLruCache();
        }
        return instance;
    }

    /**
     * 构造方法
     */
    private BasicGraphicLruCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;

        // 初始化运行时内存的 1/8为Image的内存空间
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }

        };
    }

    /**
     * 添加图片到缓存中去
     * 
     * @param key
     * @param bitmap
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * 从缓存中取出
     * 
     * @param key
     * @return
     */
    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * 加载图片到View中去,如果没有那么直接在线程中decode出来加入缓存
     * 
     * @param resId
     * @param mImageView
     */
    public void loadBitmapToImageView(int resId, ImageView mImageView) {

        final String imageKey = String.valueOf(resId);

        final Bitmap bitmap = getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        } else {
            mImageView.setImageResource(R.drawable.empty_photo);
            Context context = BasicApplication.getInstance().getApplicationContext();
            BitmapWorkerTask task = new BitmapWorkerTask(mImageView,context);
            task.execute(resId);
        }
    }

}
