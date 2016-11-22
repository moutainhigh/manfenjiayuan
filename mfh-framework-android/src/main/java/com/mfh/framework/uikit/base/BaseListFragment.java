package com.mfh.framework.uikit.base;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public abstract class BaseListFragment<T> extends Fragment {
    public static final String EXTRA_KEY_ANIM_TYPE = "animationType";

    protected View rootView;

    public static final int ANIM_TYPE_DEFAULT = -1;//默认动画
    public static final int ANIM_TYPE_NEW_FLOW = 0;//新流程，底部弹出
    protected int animType = ANIM_TYPE_DEFAULT;

    protected static final int STATE_NONE = 0;
    protected static final int STATE_REFRESH = 1;
    protected static final int STATE_LOADMORE = 2;
    protected static final int STATE_NOMORE = 3;
    protected static final int STATE_PRESSNONE = 4;// 正在下拉但还没有到刷新的状态
    protected static int mState = STATE_NONE;

    protected boolean isLoadingMore;
    protected boolean bSyncInProgress = false;//是否正在同步
    protected static int MAX_SYNC_PAGESIZE = 20;
    protected static final int MAX_PAGE = 20;
    protected PageInfo mPageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, MAX_SYNC_PAGESIZE);
    protected List<T> entityList = new ArrayList<>();

    //进度对话框
    private ProgressDialog mProgressDialog = null;
    //确认对话框
    private CommonDialog confirmDialog = null;


    protected int getLayoutResId() {
        return 0;
    }


    protected void initViews(View rootView) {
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
    public void reload() {

    }

    /**
     * 加载更多数据
     */
    public void loadMore() {

    }

    /**
     * 显示同步数据对话框
     */
    public void showProgressDialog(int status, String processText, boolean isAutoHideEnabled) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.setProgress(status, processText, isAutoHideEnabled);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    /**
     * 隐藏进度对话框
     */
    public void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * 显示确认对话框
     */
    public void showConfirmDialog(String message, String positive,
                                  DialogInterface.OnClickListener positiveListener,
                                  String negative,
                                  DialogInterface.OnClickListener negativelistener) {
        if (confirmDialog == null) {
            confirmDialog = new CommonDialog(getActivity());
        }

        confirmDialog.setMessage(message);
        confirmDialog.setPositiveButton(positive, positiveListener);
        confirmDialog.setNegativeButton(negative, negativelistener);
        if (!confirmDialog.isShowing()) {
            confirmDialog.show();
        }
    }

    /**
     * 隐藏确认对话框
     */
    public void hideConfirmDialog() {
        if (confirmDialog != null) {
            confirmDialog.dismiss();
        }
    }

}
