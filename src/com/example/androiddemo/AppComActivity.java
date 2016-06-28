package com.example.androiddemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.sny.tangyong.androiddemo.R;
import com.ty.example_unit_1.UnitTwoActivity;
import com.ty.example_unit_3.libgdx.UnitThreeActivity;
import com.ty.example_unit_6.UnitSixActivity;
import com.ty.open_source_project.OpenSouceProjectActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试一下提交记录
 * <p/>
 * <p/>
 * Created by Administrator on 2015/7/25.
 */
public class AppComActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private NavigationView mNVView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_com);
        //initViewPage(viewPager);
        setUpView();

    }


    private void setUpView() {

        mNVView = (NavigationView) findViewById(R.id.nv_main);
        mNVView.setNavigationItemSelectedListener(this);
    }


    /**
     * 初始化ViewPage
     *
     * @param viewPager
     */
    private void initViewPage(ViewPager viewPager) {

      //  Adapter adapter = new Adapter(getSupportFragmentManager());

        //adapter.addFragment(new ListFragement(), "one");
        //adapter.addFragment(new ListFragement(), "two");

        //viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        int id = menuItem.getItemId();

        Class to = null;

        switch (id) {

            case R.id.nav_androidbasic:
            //    to = com.sny.tangyong.testaar.MainActivity.class;
                break;
            case R.id.nav_opengl:

                to = UnitTwoActivity.class;
                break;

            case R.id.nav_libgdx:
                to = UnitThreeActivity.class;
                break;
            case R.id.nav_component:

                to = UnitSixActivity.class;

                break;
            case R.id.nav_open_source:

                to = OpenSouceProjectActivity.class;

                break;
            case R.id.nav_shellengine:

//                to = Main.class;
                break;
        }

        if (to != null) {
            intentToUnit(to);
        }

        return false;
    }


    private void intentToUnit(Class cls) {

        Intent intent = new Intent();
        intent.setClass(this, cls);

        startActivity(intent);
    }

}
