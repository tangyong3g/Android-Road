package com.ty.example_unit_6;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ty.component.blure.Blur2Activity;
import com.ty.example_unit_6.blurpic.BlurpicActivity;
import com.ty.example_unit_6.colorselector.ColorSelectorActivity;
import com.ty.example_unit_6.colorselector.RectColorActivity;
import com.ty.example_unit_6.seekbar.DockLineDialog;
import com.sny.tangyong.androiddemo.R;

public class UnitSixActivity extends ListActivity {

    String[] units = new String[]{"可滑动的选择条", "颜色选择器", "色相亮度饱和度", "方形", "图片模糊", "读取Excele", "上传下载", "SVG", "AbTest", "图片模糊2.0"};

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
        switch (position) {
            case 0:
                showSeekBar();
                break;
            case 1:
                cls = ColorSelectorActivity.class;
                break;
            case 2:
                break;
            case 3:
                cls = RectColorActivity.class;
                break;
            case 4:
                cls = BlurpicActivity.class;
                break;
            case 5:
                cls = ReadExcelActivity.class;
                break;
            case 6:
                cls = UploadFile.class;

                break;
            case 7:

                cls = SVGACtivityTest.class;

                break;

            case 8:

                cls = ABTestActivity.class;

                break;
            case 9:

                cls = Blur2Activity.class;

                break;
            default:
                break;
        }
        intentToActivity(cls);
    }

    private void showSeekBar() {
        DockLineDialog dialog = new DockLineDialog(this, 1, "Dialog title");
        dialog.show();
    }

    private void intentToActivity(Class cls) {
        if (cls == null)
            return;
        Intent intent = new Intent();
        intent.setClass(this, cls);

        startActivity(intent);
    }

}
