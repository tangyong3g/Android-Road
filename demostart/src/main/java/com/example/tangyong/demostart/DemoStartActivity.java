package com.example.tangyong.demostart;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.sny.tangyong.demostart.SettingsActivity;
import com.sny.tangyong.demostart.bean.Chapter;
import com.sny.tangyong.demostart.service.ChapterDataControler;
import com.sny.tangyong.demostart.service.ILoaderDataService;
import com.sny.tangyong.demostart.service.LoadDataServiceImpl;

import java.util.ArrayList;


/**
 * @author ty_sany@163.com
 *         <p/>
 *         <p/>
 *         <p/>
 *         1: 要解决的问题，跳转不要增加和修改，里面的Intent信息也要保存在里面才行
 */
public class DemoStartActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    // NavigationView
    private NavigationView mNavigationView;
    private ArrayList<Chapter> mChapters;
    private ChapterDataControler mControler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        DrawerLayout dy = (DrawerLayout)findViewById(R.id.drawerlayout);
        Button btn = new Button(this);
        btn.setText("Click Me ");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnClick();
            }
        });

        dy.addView(btn);

        initView();
        initData();
    }


    public void btnClick() {

        Intent intent = new Intent();
//        intent.setAction("com.sny.tangyong.demostart.SettingsActivity");
        try{
            intent.setClass(this, Class.forName("com.sny.tangyong.demostart.SettingsActivity"));
        }catch (Exception e){
        }

        startActivity(intent);

    }

    private void initData() {

        ILoaderDataService loader = new LoadDataServiceImpl();

        try {

            mChapters = loader.loadChapterData(getApplicationContext());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_demo_start, menu);
        return true;
    }


    /**
     * 初始化NavigationView和事件
     */
    private void initView() {

        mNavigationView = (NavigationView) findViewById(R.id.demostart_navigation_id);
        mNavigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //  return true;
        //}

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        int id = menuItem.getItemId();
        String title = menuItem.getTitle().toString();


        return false;
    }
}
