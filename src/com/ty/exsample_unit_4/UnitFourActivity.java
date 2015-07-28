package com.ty.exsample_unit_4;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ty.exsample.R;
import com.ty.exsample_unit_4.animation.AnimationActivity;

/**
 * 第四单元的内容主要有Android的基本知识
 *
 * @author Z61
 */
public class UnitFourActivity extends ListActivity {

    String[] units = new String[]{"SurfaceViewTest", "FullScreen", "Looper", "Animation", "Meminfo", "powerConnectd", "图形", "canvas", "blending", "bitmap",
            "Handler", "Layout", "View_save", "List", "VewCycle", "DislayMetrics", "WallpaperManager", "双缓冲", "混合", "属性动画", "ConcurrentModificationException", "异步任务",
            "DeviceInfo", "Partical", "BitMapDecode"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //設置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //取消標題
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setListAdapter(new ArrayAdapter<String>(this, R.layout.main_items, units));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);
        Class cls = null;
        String componentName = null;
        switch (position) {
            case 0:
                cls = SurfaceViewTest.class;
                break;
            case 1:
                cls = FullScreenTest.class;
                break;
            case 2:
                cls = LooperActivity.class;
                break;
            case 3:
                cls = AnimationActivity.class;
                break;
            case 4:
                cls = MeminfoActivity.class;
                break;
            case 5:

                cls = PowerConnectActivity.class;
                break;
            case 6:
                cls = GrapicActivity.class;
                break;
            case 7:
                cls = CanvasDemoActivity.class;

                break;
            case 8:
                cls = MomentTest.class;
                break;
            case 9:
                cls = BitmapActivity.class;
                break;

            case 10:

                cls = HandlerTestActivity.class;
                break;

            case 11:

                cls = LayoutActivity.class;

                break;
            case 12:

                componentName = "com.ty.exsample_unit_4.CanvaseSaveRsView";

                break;
            case 13:

                cls = List1.class;

                break;

            case 14:

                cls = ViewCycleTestActivity.class;

                break;
            case 15:

                cls = DipTestActivity.class;

                break;

            case 16:

                cls = WallpaperManagerTest.class;

                break;
            case 17:

                cls = CacheBitmapActivity.class;

                break;
            case 18:

                cls = BelendingActivity.class;

                break;

            case 19:

                cls = AttriAnimationActivity.class;

                break;
            case 20:

                cls = ConcurrentModificationExceptionActivity.class;

                break;
            case 21:

                cls = SyncTaskActivity.class;

                break;
            case 22:

                cls = DeviceInfomation.class;

                break;

            case 23:

                break;

            case 24:

                cls = BitMapDecodeTest.class;

                break;

            default:

                break;
        }

        if (cls != null) {
            intentToActivity(cls);
        }

        if (componentName != null) {

            intentToView(componentName);
        }
    }

    private void intentToView(String componentName) {
        Intent intent = new Intent();
        intent.setClass(this, BaseViewActivity.class);

        intent.putExtra(BaseViewActivity.BASE_FLAG, componentName);

        startActivity(intent);
    }

    private void intentToActivity(Class cls) {
        Intent intent = new Intent();
        intent.setClass(this, cls);

        startActivity(intent);
    }

}
