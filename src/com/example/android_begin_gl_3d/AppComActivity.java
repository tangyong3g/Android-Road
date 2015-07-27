package com.example.android_begin_gl_3d;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.ty.exsample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/7/25.
 */
public class AppComActivity extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_com);

        ViewPager viewPager = (ViewPager)findViewById(R.id.viewpager);
        initViewPage(viewPager);
    }


    /**
     *  初始化ViewPage
     *
     * @param viewPager
     */
    private void initViewPage(ViewPager viewPager){

        Adapter  adapter = new Adapter(getSupportFragmentManager());

        adapter.addFragment(new ListFragement(),"one");
        adapter.addFragment(new ListFragement(),"two");

        viewPager.setAdapter(adapter);

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
}
