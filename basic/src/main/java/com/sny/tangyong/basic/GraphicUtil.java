package com.sny.tangyong.basic;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Created by lenvo on 2016/6/6.
 */
public class GraphicUtil {

    public static Bitmap decodeBitmapWithWH(Resources res, int width, int height, int resId,
            BitmapFactory.Options optionsOriginal) {

        BitmapFactory.Options optionsNew = new BitmapFactory.Options();
        optionsNew.inJustDecodeBounds = false;
        int simpleSize = calculateInSampleSize(optionsOriginal, width, height);
        optionsNew.inSampleSize = simpleSize;

        Log.d("tyler.tang", "width:" + width + "simpleSize:\t" + simpleSize);

        return BitmapFactory.decodeResource(res, resId, optionsNew);
    }



    public static int calculateInSampleSize(BitmapFactory.Options originalOptions, int reqWidth, int reqHeight) {
        int inSampleSize = 1;

        int width = originalOptions.outWidth;
        int height = originalOptions.outHeight;

        Log.d("tyler.tang", "original width:" + width);

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


}
