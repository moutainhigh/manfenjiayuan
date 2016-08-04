package com.mfh.framework.uikit.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.comn.bean.PageInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public abstract class BaseListFragment<T> extends Fragment {

    protected View rootView;

    protected static final int STATE_NONE       = 0;
    protected static final int STATE_REFRESH    = 1;
    protected static final int STATE_LOADMORE   = 2;
    protected static final int STATE_NOMORE     = 3;
    protected static final int STATE_PRESSNONE  = 4;// 正在下拉但还没有到刷新的状态
    protected static int mState = STATE_NONE;

    protected boolean isLoadingMore;
    protected boolean bSyncInProgress = false;//是否正在同步
    protected static final int MAX_SYNC_PAGESIZE = 20;
    protected static final int MAX_PAGE = 20;
    protected PageInfo mPageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, MAX_SYNC_PAGESIZE);
    protected List<T> entityList = new ArrayList<>();

    protected int getLayoutResId(){return 0;}


    protected void initViews(View rootView){
        ButterKnife.bind(this, rootView);
    }

    protected abstract void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

//        ZLogger.d("onCreateView()");
        //Inflate the layout for this fragment
        rootView = inflater.inflate(getLayoutResId(), container, false);

        initViews(rootView);

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
//        MobclickAgent.onPageStart(getClass().getSimpleName());
    }

    @Override
    public void onPause() {
        super.onPause();
//        ZLogger.d("onPause()");
//        MobclickAgent.onPageEnd(getClass().getSimpleName());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        ZLogger.d("onDetach()");
    }

    /**
     * 开始加载
     */
    public void onLoadStart() {
        isLoadingMore = true;
        bSyncInProgress = true;
        setRefreshing(true);
    }

    /**
     * 加载完成
     */
    public void onLoadFinished() {
        bSyncInProgress = false;
        isLoadingMore = false;
        setRefreshing(false);
    }

    /**
     * 设置刷新状态
     */
    public void setRefreshing(boolean refreshing) {
        if (refreshing) {
            mState = STATE_REFRESH;
        } else {
            mState = STATE_NONE;
        }
    }

    /**
     * 重新加载数据
     */
    public void reload(){

    }

    /**
     * 加载更多数据
     */
    public void loadMore(){

    }

}
