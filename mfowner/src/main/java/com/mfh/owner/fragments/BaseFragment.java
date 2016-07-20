package com.mfh.owner.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;

/**
 * Fragment基类
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

        //Inflate the layout for this fragment
        rootView = inflater.inflate(getLayoutResId(), container, false);

        ButterKnife.bind(this, rootView);

        createViewInner(rootView, container, savedInstanceState);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getSimpleName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
