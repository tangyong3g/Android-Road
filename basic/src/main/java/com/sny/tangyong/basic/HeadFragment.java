package com.sny.tangyong.basic;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2016/6/3.
 */
public class HeadFragment extends Fragment {

    IFragmentTransToActivity mIfragmentTransTOActivity;


    public interface IFragmentTransToActivity{
        void translateState(String state);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mIfragmentTransTOActivity = (IFragmentTransToActivity)activity;
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment, container, false);
    }
}
