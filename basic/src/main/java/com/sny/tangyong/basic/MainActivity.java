package com.sny.tangyong.basic;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Administrator on 2015/7/29.
 */
public class MainActivity extends ListActivity {

    String[] units = new String[]{

            "SurfaceViewTest",
            "FullScreen",
            "Looper",
            "Animation",
            "Meminfo",
            "powerConnectd",
            "graphic",
            "canvas",
            "blending",
            "bitmap",
            "Handler",
            "Layout",
            "View_save",
            "List",
            "VewCycle",
            "DislayMetrics",
            "WallpaperManager",
            "cache",
            "blending",
            "attribute animation",
            "ConcurrentModificationException",
            "ansy task",
            "DeviceInfo",
            "Partical",
            "BitMapDecode",
            "LooperVersion2.0",
            "Fragment",
            "Best Practice for performance threads ",
            "Imea and android id ",
            "basic device information",
            "UnCatchException",
            "Https/Http",
            "HttpsTwo",
            "Identifying ID唯一性解决方案",
            "Fragment"

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getActionBar();
        setListAdapter(new ArrayAdapter<String>(this, R.layout.basic_item, units));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);
        Class cls = null;
        String componentName = null;
        switch (position) {
            case 0:
                cls = GLSurfaceViewActivity.class;
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
            case 25:
                cls = LooperVersion2.class;
                break;

            case 26:

                cls = FragmentTest.class;

                break;
            case 27:
                cls = BesetPracticeForThread.class;
                break;

            case 28:
                cls = BasicImeaActivity.class;
                break;
            case 29:
                cls = BasicDeviceInfoActivity.class;
                break;
            case 30:

                cls = UnCatchExceptionActivity.class;
                break;
            case 31:

                cls = BasicHttpActivity.class;
                break;
            case 32:
                cls = BasicHttpTwoActivity.class;
                break;
            case 33:

                cls = BasicUnquiueIdentifyActivity.class;

                break;

            case 34:

                cls = BasicFragment.class;

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
