package com.sny.tangyong.basic;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 当显示区比较小，原图很大的时候 对inSimplesize进行计算，减少相素
 *
 */
public class LoadImageDataActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {

        TextView tx = new TextView(getBaseContext());
        ImageInfo info = readBasicImageOptionsInfo(R.drawable.bg_one);
        String showInfo = info.show();
        tx.setText(showInfo);
        BitmapFactory.Options originalOptions = info.mOptions;

        GridLayout layout = new GridLayout(getBaseContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        layout.setLayoutParams(layoutParams);
        layout.addView(tx);

        ImageView img_100_100 = createImageView(100, 100);
        Bitmap bit_100_100 = decodeBitmapWithWH(100, 100, R.drawable.bg_one, originalOptions);
        img_100_100.setImageBitmap(bit_100_100);
        layout.addView(img_100_100);

        ImageView img_200_200 = createImageView(200, 200);
        Bitmap bit_200_200 = decodeBitmapWithWH(200, 200, R.drawable.bg_one, originalOptions);
        img_200_200.setImageBitmap(bit_200_200);
        layout.addView(img_200_200);

        ImageView img_400_400 = createImageView(400, 400);
        Bitmap bit_400_400 = decodeBitmapWithWH(400, 400, R.drawable.bg_one, originalOptions);
        img_400_400.setImageBitmap(bit_400_400);
        layout.addView(img_400_400);

        ImageView img_800_600 = createImageView(800, 600);
        Bitmap bit_800_600 = decodeBitmapWithWH(800, 600, R.drawable.bg_one, originalOptions);
        img_800_600.setImageBitmap(bit_800_600);
        layout.addView(img_800_600);

        setContentView(layout);
    }

    public Bitmap decodeBitmapWithWH(int width, int height, int res, BitmapFactory.Options optionsOriginal) {

        BitmapFactory.Options optionsNew = new BitmapFactory.Options();
        optionsNew.inJustDecodeBounds = false;
        int simpleSize = calculateInSampleSize(optionsOriginal, width, height);
        optionsNew.inSampleSize = simpleSize;

        Log.d("tyler.tang", "width:" + width + "simpleSize:\t" + simpleSize);

        return BitmapFactory.decodeResource(getResources(), res, optionsNew);
    }


    public ImageView createImageView(int width, int height) {

        ImageView imageView = null;

        imageView = new ImageView(getBaseContext());

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, height);
        imageView.setLayoutParams(layoutParams);


        return imageView;
    }



    /**
     * 读取基本信息
     * 
     * @param resId
     * @return
     */
    public ImageInfo readBasicImageOptionsInfo(int resId) {

        ImageInfo imageInfo = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(getResources(), resId, options);

        int width = options.outWidth;
        int height = options.outHeight;
        String type = options.outMimeType;

        imageInfo = new ImageInfo(type, height, width);
        imageInfo.mOptions = options;

        return imageInfo;
    }



    public int calculateInSampleSize(BitmapFactory.Options originalOptions, int reqWidth, int reqHeight) {
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


    /**
     * 封装图片尺寸和类型信息
     */
    class ImageInfo {

        public String mImageType;
        public int mWidth;
        public int mHeight;
        public BitmapFactory.Options mOptions;

        public ImageInfo(String imageType, int height, int width) {
            this.mImageType = imageType;
            this.mWidth = width;
            this.mHeight = height;
        }

        public String show() {

            StringBuffer sb = new StringBuffer();
            sb.append("type:\t" + mImageType);
            sb.append("\n");
            sb.append("height:\t" + mHeight);
            sb.append("\n");
            sb.append("width:\t" + mWidth);
            sb.append("\n");

            return sb.toString();
        }
    }
}
