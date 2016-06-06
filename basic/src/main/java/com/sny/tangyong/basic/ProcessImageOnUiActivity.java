package com.sny.tangyong.basic;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import java.lang.ref.WeakReference;


/**
 * 1：用异步任务加载图片 : 2： 使用weakReference 来存储 imageView。
 *
 * 这里是编码出来的100*100的图片，原图是 800 * 1000 所以看起来有点模糊
 *
 */
public class ProcessImageOnUiActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageView imageView = new ImageView(getBaseContext());
        loadBitmap(R.drawable.bg_one, imageView);
        setContentView(imageView);
    }

    /**
     * 加载图片的异步任务
     */
    class BitmapWorkTask extends AsyncTask<Integer, Void, Bitmap> {

        WeakReference<ImageView> mWeakReferenceImageView;
        int data = 0;

        public BitmapWorkTask(ImageView imageView) {
            mWeakReferenceImageView = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            data = params[0];

            BitmapFactory.Options originalOptions = new BitmapFactory.Options();
            originalOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.bg_one, originalOptions);

            try {
                Thread.sleep(500);
            } catch (InterruptedException it) {
                it.printStackTrace();
            }

            Bitmap bitmap = GraphicUtil.decodeBitmapWithWH(getResources(), 100, 100, data, originalOptions);

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

                BitmapWorkTask task = getBitmapWorkerTask(imageView);

                if (this == task && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    public void loadBitmap(int resId, ImageView imageView) {
        BitmapWorkTask task = new BitmapWorkTask(imageView);
        task.execute(resId);
        imageView.setImageDrawable(new AsyncDrable(getResources(),null,task));
    }



    public static boolean cancelPotentialWork(int data, ImageView imageView) {
        final BitmapWorkTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final int bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == 0 || bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }



    static class AsyncDrable extends BitmapDrawable {

        private WeakReference<BitmapWorkTask> bitmapWorkTaskWeakReference = null;

        public AsyncDrable(Resources res, Bitmap bitmap, BitmapWorkTask bitmapWorkTask) {
            super(res, bitmap);
            bitmapWorkTaskWeakReference = new WeakReference<BitmapWorkTask>(bitmapWorkTask);
        }

        public BitmapWorkTask getBitmapWorkerTask() {

            if (bitmapWorkTaskWeakReference != null) {
                return bitmapWorkTaskWeakReference.get();
            }
            return null;
        }
    }


    /**
     *  得到与ImageView相绑定的 BitmapWorkTask 任务。
     *  中间又通过Drawble来完成任务，bitmap ,ImageVIew的关系
     *
     * @param imageView
     * @return
     */

    private static BitmapWorkTask getBitmapWorkerTask(ImageView imageView) {

        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrable) {
                final AsyncDrable asyncDrawable = (AsyncDrable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }


}
