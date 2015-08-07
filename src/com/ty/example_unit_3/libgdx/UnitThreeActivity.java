package com.ty.example_unit_3.libgdx;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ty.example_unit_3.libgdx.loadmode.MaterialOpenGL2Activity;
import com.ty.example_unit_3.libgdx.timetunnel.TunnelActivity;
import com.sny.tangyong.androiddemo.R;

/**
 * @author tangyong
 */
public class UnitThreeActivity extends ListActivity {

    String[] units = new String[]{"MaterialOpenGL2.x", "path", "Tunnel", "Attribute",
            "MeshShader", "SimpleAnimation", "Translate", "可施动的Actor", "任意路径问题"};

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
                cls = MaterialOpenGL2Activity.class;
                break;
            case 1:
                cls = PatchActivity.class;
                break;
            case 2:
                cls = TunnelActivity.class;
                break;
            case 3:
                cls = AttributeActivity.class;
                break;
            case 4:
                cls = MeshShaderActivity.class;
                break;
            case 5:
//				cls = SimpleAnimationActivity.class;
                break;
            case 6:
                cls = TranslateActivity.class;
                break;
            case 7:
                cls = ActorFPSAnimationActivity.class;
                break;
            case 8:

                cls = PathActivity.class;

                break;
            default:
                break;
        }
        intentToActivity(cls);
    }

    private void intentToActivity(Class cls) {
        Intent intent = new Intent();
        intent.setClass(this, cls);

        startActivity(intent);
    }

}
