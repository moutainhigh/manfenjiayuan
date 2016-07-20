package com.mfh.petitestock.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.serialport.api.SerialPort;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.umeng.analytics.MobclickAgent;
import com.zkc.Service.CaptureService;

import butterknife.ButterKnife;


/**
 * 有扫描功能的Fragment基类
 * Created by Nat.ZZN(bingshanguxue) on 15/11/06.
 */
public abstract class GpioFragment extends Fragment {

    private static final String TAG = "GpioFragment";

    public static final String ACTION_SCAN_CODE = "com.zkc.scancode";
    public static final String EXTRA_KEY_SCAN_CODE = "code";

    protected View rootView;

    //确认对话框
    private CommonDialog confirmDialog = null;
    //进度对话框
    private ProgressDialog mProgressDialog = null;

    private BroadcastReceiver scanBroadcastReceiver;

    //扫描到条码
    protected abstract void onScanCode(String code);

    protected int getLayoutResId(){return 0;}

    protected abstract void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //Inflate the layout for this fragment
        rootView = inflater.inflate(getLayoutResId(), container, false);

        ButterKnife.bind(this, rootView);

        //开启扫描功能
        Intent newIntent = new Intent(getActivity(), CaptureService.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startService(newIntent);

        SerialPort.CleanBuffer();
        CaptureService.scanGpio.openScan();

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

        MobclickAgent.onPageStart(TAG);

        CaptureService.scanGpio.openScan();
        registerReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();

        MobclickAgent.onPageEnd(TAG);

        if (scanBroadcastReceiver != null) {
            getActivity().unregisterReceiver(scanBroadcastReceiver);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        CaptureService.scanGpio.closeScan(); // �رյ�Դ
//        CaptureService.scanGpio.closePower();
    }


    private void registerReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SCAN_CODE);
        scanBroadcastReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                if (extras != null){
                    onScanCode(extras.getString(EXTRA_KEY_SCAN_CODE));
                }
            }
        };
        getActivity().registerReceiver(scanBroadcastReceiver, intentFilter);
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
        confirmDialog.show();
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
        confirmDialog.show();
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


    /**
     * 隐藏进度对话框
     */
    public void hideProgressDialog(){
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

}
