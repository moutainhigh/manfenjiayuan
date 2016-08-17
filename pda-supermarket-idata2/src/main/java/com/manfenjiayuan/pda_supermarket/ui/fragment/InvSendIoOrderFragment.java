package com.manfenjiayuan.pda_supermarket.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.widget.ScanBar;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.bean.InvSendIoOrderItemBrief;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.presenter.InvSendIoOrderPresenter;
import com.manfenjiayuan.pda_supermarket.ui.IInvSendIoOrderView;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 导入发货单页面
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvSendIoOrderFragment extends PDAScanFragment implements IInvSendIoOrderView {

    @Bind(R.id.toolbar)
    public Toolbar mToolbar;
    @Bind(R.id.scanBar)
    public ScanBar mScanBar;
    @Bind(R.id.label_barcode)
    TextLabelView labelBarcode;
    @Bind(R.id.label_totalFee)
    TextLabelView labelTotalFee;
    @Bind(R.id.label_totalCount)
    TextLabelView labelTotalCount;
    @Bind(R.id.label_sendCompanyName)
    TextLabelView labelSendCompanyName;
    @Bind(R.id.label_orderName)
    TextLabelView labelOrderName;
    @Bind(R.id.label_status)
    TextLabelView labelStatus;
    @Bind(R.id.fab_submit)
    public FloatingActionButton btnSubmit;


    private InvSendIoOrderItemBrief mInvSendIoOrderItemBrief = null;
    private InvSendIoOrderPresenter invSendIoOrderPresenter;

    public static InvSendIoOrderFragment newInstance(Bundle args) {
        InvSendIoOrderFragment fragment = new InvSendIoOrderFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_invsendio_query;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        invSendIoOrderPresenter = new InvSendIoOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        if (mToolbar != null) {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
            mToolbar.setNavigationOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActivity().onBackPressed();
                        }
                    });
        } else {
            ZLogger.d("mToolbar is null");
        }

        if (mScanBar != null) {
            mScanBar.setSoftKeyboardEnabled(true);
            mScanBar.setOnScanBarListener(new ScanBar.OnScanBarListener() {
                @Override
                public void onKeycodeEnterClick(String text) {
                    mScanBar.reset();
                    queryByBarcode(text);
                }

                @Override
                public void onAction1Click(String text) {
                    mScanBar.reset();
                    queryByBarcode(text);
                }
            });
        } else {
            ZLogger.d("mScanBar is null");
        }
    }

    /**
     * 查询商品信息
     */
    public void queryByBarcode(String barcode) {
        isAcceptBarcodeEnabled = false;
        if (StringUtils.isEmpty(barcode)) {
            mScanBar.reset();
            isAcceptBarcodeEnabled = true;
            return;
        }

        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            refresh(null);
            return;
        }

        invSendIoOrderPresenter.loadOrderItemsByBarcode(barcode);
    }

    @OnClick(R.id.fab_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        isAcceptBarcodeEnabled = false;

        if (mInvSendIoOrderItemBrief != null) {
            Intent intent = new Intent();
            intent.putExtra("orderBrief", mInvSendIoOrderItemBrief);
            getActivity().setResult(Activity.RESULT_OK, intent);
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED);
        }

        getActivity().finish();
    }


    @Override
    protected void onScanCode(String code) {
        if (!isAcceptBarcodeEnabled) {
            return;
        }
        isAcceptBarcodeEnabled = false;
        mScanBar.reset();
        queryByBarcode(code);
    }

    @Override
    public void onIInvSendIoOrderViewProcess() {
        isAcceptBarcodeEnabled = false;
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "加载订单...", false);
    }

    @Override
    public void onIInvSendIoOrderViewError(String errorMsg) {
        isAcceptBarcodeEnabled = true;
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
    }

    @Override
    public void onIInvSendIoOrderViewSuccess(InvSendIoOrderItemBrief data) {
        refresh(data);
    }

    private void refresh(InvSendIoOrderItemBrief orderItemBrief){
        isAcceptBarcodeEnabled = true;
        mScanBar.reset();

        if (orderItemBrief == null){
            labelBarcode.setTvSubTitle("");
            labelOrderName.setTvSubTitle("");
            labelTotalFee.setTvSubTitle("");
            labelTotalCount.setTvSubTitle("");
            labelSendCompanyName.setTvSubTitle("");
            labelStatus.setTvSubTitle("");
        }
        else{
            labelBarcode.setTvSubTitle(orderItemBrief.getBarcode());
            labelOrderName.setTvSubTitle(orderItemBrief.getOrderName());
            labelTotalFee.setTvSubTitle(MUtils.formatDouble(orderItemBrief.getTotalFee(), ""));
            labelTotalCount.setTvSubTitle(MUtils.formatDouble(orderItemBrief.getCommitGoodsNum(), ""));
            labelSendCompanyName.setTvSubTitle(orderItemBrief.getSendCompanyName());
            labelStatus.setTvSubTitle(orderItemBrief.getCaptionStatus());
        }
        mInvSendIoOrderItemBrief = orderItemBrief;
        hideProgressDialog();
    }

}
