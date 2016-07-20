package com.mfh.buyers.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;

/**
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public abstract class BaseFragment extends Fragment {

    protected View rootView;

    protected int getLayoutResId(){return 0;}

    protected abstract void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

//        ZLogger.d("onCreateView()");
        //Inflate the layout for this fragment
        rootView = inflater.inflate(getLayoutResId(), container, false);

        ButterKnife.bind(this, rootView);

        createViewInner(rootView, container, savedInstanceState);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ZLogger.d("onViewCreated()");
    }

    @Override
    public void onResume() {
        super.onResume();
//        ZLogger.d("onResume()");
        MobclickAgent.onPageStart(getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        super.onPause();
//        ZLogger.d("onPause()");
        MobclickAgent.onPageEnd(getClass().getSimpleName());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        ZLogger.d("onAttach()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        ZLogger.d("onDetach()");
    }


}
