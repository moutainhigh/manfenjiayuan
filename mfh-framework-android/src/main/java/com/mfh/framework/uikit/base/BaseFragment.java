package com.mfh.framework.uikit.base;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.framework.uikit.BackHandledInterface;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import butterknife.ButterKnife;

/**
 * Fragment基类
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public abstract class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";
    protected View rootView;

    //确认对话框
    private CommonDialog confirmDialog = null;
    //进度对话框
    private ProgressDialog mProgressDialog = null;


    protected BackHandledInterface mBackHandledInterface = null;
    /**
     * 所有继承BackHandledFragment的子类都将在这个方法中实现物理Back键按下后的逻辑
     * FragmentActivity捕捉到物理返回键点击事件后会首先询问Fragment是否消费该事件
     * 如果没有Fragment消息时FragmentActivity自己才会消费该事件
     */
    public boolean onBackPressed(){
        return isResponseBackPressed();
    }
    protected boolean isResponseBackPressed(){return false;}

    protected int getLayoutResId(){return 0;}

    protected void initViews(View rootView){
        ButterKnife.bind(this, rootView);
    }

    protected abstract void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //Inflate the layout for this fragment
        rootView = inflater.inflate(getLayoutResId(), container, false);

        initViews(rootView);

        createViewInner(rootView, container, savedInstanceState);
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity() instanceof BackHandledInterface){
            this.mBackHandledInterface = (BackHandledInterface)getActivity();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        //告诉FragmentActivity，当前Fragment在栈顶
        if (isResponseBackPressed()){
            mBackHandledInterface.setSelectedFragment(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        MobclickAgent.onPageStart(TAG);
    }

    @Override
    public void onPause() {
        super.onPause();
//        MobclickAgent.onPageEnd(TAG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        hideProgressDialog();

//        DeviceUtils.hideSoftInput(getActivity());
    }


    /**
     * 显示确认对话框
     * */
    public void showConfirmDialog(int message, int positive,
                                  DialogInterface.OnClickListener positiveListener,
                                  int negative,
                                  DialogInterface.OnClickListener negativelistener){
        if (confirmDialog == null){
            confirmDialog = new CommonDialog(getActivity());
        }

        confirmDialog.setMessage(message);
        confirmDialog.setPositiveButton(positive, positiveListener);
        confirmDialog.setNegativeButton(negative, negativelistener);
        if (!confirmDialog.isShowing()){
            confirmDialog.show();
        }
    }

    /**
     * 显示确认对话框
     * */
    public void showConfirmDialog(String message, String positive,
                                  DialogInterface.OnClickListener positiveListener,
                                  String negative,
                                  DialogInterface.OnClickListener negativelistener){
        if (confirmDialog == null){
            confirmDialog = new CommonDialog(getActivity());
        }

        confirmDialog.setMessage(message);
        confirmDialog.setPositiveButton(positive, positiveListener);
        confirmDialog.setNegativeButton(negative, negativelistener);
        if (!confirmDialog.isShowing()){
            confirmDialog.show();
        }
    }

    /**
     * 隐藏确认对话框
     */
    public void hideConfirmDialog(){
        if (confirmDialog != null) {
            confirmDialog.dismiss();
        }
    }

    public void initProgressDialog(String processText, String doneText, String errorText) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.init(processText, doneText, errorText);
    }

    /**
     * 显示同步数据对话框
     */
    public void showProgressDialog(int status) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.setProgress(status);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
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

    public void showProgressDialog(int status, String processText,
                                   boolean isCancelAble, boolean isAutoHideEnabled) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(isCancelAble);
        }
        mProgressDialog.setProgress(status, processText, isAutoHideEnabled);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    /**
     * 隐藏进度对话框
     */
    public void hideProgressDialog(){
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

}
