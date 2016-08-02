package com.manfenjiayuan.pda_supermarket.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.widget.ScanBar;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 查询条码
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public abstract class QueryBarcodeFragment extends PDAScanFragment{

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.scanBar)
    public ScanBar mScanBar;
    @Bind(R.id.iv_search)
    ImageView ivSearch;


    @Bind(R.id.fab_cancel)
    public FloatingActionButton fabCancel;
    @Bind(R.id.fab_submit)
    public FloatingActionButton btnSubmit;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_query_barcode;
    }

    @Override
    protected void onScanCode(String code) {
        if (!isAcceptBarcodeEnabled){
            return;
        }
        isAcceptBarcodeEnabled = false;
        mScanBar.reset();
        queryByBarcode(code);
    }


    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        if (mToolbar != null){
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
            mToolbar.setNavigationOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActivity().onBackPressed();
                        }
                    });
        }
        else{
            ZLogger.d("mToolbar is null");
        }

        if (mScanBar != null){
            mScanBar.setSoftKeyboardEnabled(true);
            mScanBar.setOnViewListener(new ScanBar.OnViewListener() {
                @Override
                public void onKeycodeEnterClick(String text) {
                    mScanBar.reset();
                    queryByBarcode(text);
                }

            });
        }else{
            ZLogger.d("mScanBar is null");
        }
    }


    /**
     * 刷新
     * */
    public void refresh(){
        mScanBar.reset();
        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), mScanBar);
    }

    /**
     * 搜索条码
     */
    public void queryByBarcode(String barcode) {
        isAcceptBarcodeEnabled = false;
        if (StringUtils.isEmpty(barcode)) {
            mScanBar.reset();
            isAcceptBarcodeEnabled = true;
            return;
        }

        sendQueryReq(barcode);
    }

    /**
     * 查询商品信息
     */
    public void queryByName(String name) {
        isAcceptBarcodeEnabled = false;
        if (StringUtils.isEmpty(name)) {
            mScanBar.requestFocus();
            isAcceptBarcodeEnabled = true;
            return;
        }

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            onQueryError(getString(R.string.toast_network_error));
        }

        // TODO: 7/30/16 执行查询动作
    }

    @OnClick(R.id.iv_search)
    public void onSerarch(){
        DialogUtil.showHint("点击了搜索");
    }

    @OnClick(R.id.fab_cancel)
    public void cancel() {
        fabCancel.setEnabled(false);
        // TODO: 7/30/16 具体提交代码
    }

    @OnClick(R.id.fab_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        isAcceptBarcodeEnabled = false;
        
        onSubmitProcess();

        // TODO: 7/30/16 具体提交代码
    }

    /**
     * 发送查询请求
     * */
    public void sendQueryReq(String barcode){
        onQueryProcess();
    }

    /**
     * 查询处理中
     * */
    public void onQueryProcess() {
        isAcceptBarcodeEnabled = false;
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
    }

    /**
     * 查询失败
     * */
    public void onQueryError(String errorMsg) {
        ZLogger.df(errorMsg);
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
        isAcceptBarcodeEnabled = true;
    }
    /**
     * 查询成功
     * */
    public void onQuerySuccess() {
//        showProgressDialog(ProgressDialog.STATUS_DONE, "操作成功", true);
        hideProgressDialog();
        isAcceptBarcodeEnabled = true;
    }

    /**
     * 提交处理中
     * */
    public void onSubmitProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
    }

    /**
     * 提交失败
     * */
    public void onSubmitError(String errorMsg) {
        if (!StringUtils.isEmpty(errorMsg)){
            showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
            ZLogger.df(errorMsg);
        }
        else{
            hideProgressDialog();
        }
        isAcceptBarcodeEnabled = true;
        btnSubmit.setEnabled(true);
    }

    /**
     * 提交成功
     * */
    public void onSubmitSuccess() {
        showProgressDialog(ProgressDialog.STATUS_DONE, "操作成功", true);
//        hideProgressDialog();
    }

}
