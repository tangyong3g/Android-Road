package com.sny.tangyong.basic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by ty_sany@163.com on 2016/6/6.
 */
public class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

    WeakReference<ImageView> mWeakReferenceImageView;
    int data = 0;
    Context mContext;

    public BitmapWorkerTask(ImageView imageView, Context context) {
        mWeakReferenceImageView = new WeakReference<ImageView>(imageView);
        mContext = context.getApplicationContext();
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
        data = params[0];

        BitmapFactory.Options originalOptions = new BitmapFactory.Options();
        originalOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bg_one, originalOptions);

        try {
            Thread.sleep(500);
        } catch (InterruptedException it) {
            it.printStackTrace();
        }

        Bitmap bitmap = GraphicUtil.decodeBitmapWithWH(mContext.getResources(), 100, 100, data, originalOptions);

        //加入到LRUCache中去
        if(bitmap!=null) {
            String key = String.valueOf(data);
            BasicGraphicLruCache.getInstance().addBitmapToMemoryCache(key, bitmap);
        }

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if (isCancelled()) {
            bitmap = null;
        }

        if (mWeakReferenceImageView != null && bitmap != null) {

            final ImageView imageView = mWeakReferenceImageView.get();

            if(bitmap!=null){

                imageView.setImageBitmap(bitmap);

            }
        }
    }

}
