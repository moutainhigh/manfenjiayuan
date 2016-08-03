package com.manfenjiayuan.pda_supermarket.ui.invconvert;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.widget.EditLabelView;
import com.bingshanguxue.pda.widget.ScanBar;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.manfenjiayuan.business.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IInvSkuGoodsView;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.bean.wrapper.ChangeSkuStoreItem;
import com.manfenjiayuan.pda_supermarket.ui.activity.SecondaryActivity;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 库存转换
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvConvertFromFragment extends PDAScanFragment implements IInvSkuGoodsView {

    @Bind(R.id.toolbar)
    public Toolbar mToolbar;
    @Bind(R.id.scanBar)
    public ScanBar mScanBar;

    @Bind(R.id.label_barcodee)
    TextLabelView labelBarcode;
    @Bind(R.id.label_productName)
    TextLabelView labelProductName;
    @Bind(R.id.label_quantity)
    TextLabelView labelQuantity;
    @Bind(R.id.label_quantity_check)
    EditLabelView labelQuantityCheck;
    @Bind(R.id.tv_unit)
    TextView tvUnit;
    @Bind(R.id.fab_submit)
    public FloatingActionButton btnSubmit;


    private InvSkuGoods curGoods = null;
    private InvSkuGoodsPresenter mInvSkuGoodsPresenter = null;

    public static InvConvertFromFragment newInstance(Bundle args) {
        InvConvertFromFragment fragment = new InvConvertFromFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_invconvert_from;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInvSkuGoodsPresenter = new InvSkuGoodsPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        if (mToolbar != null) {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
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
        labelQuantityCheck.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                submit();
            }

            @Override
            public void onScan() {
                refresh(null);
            }
        });
        labelQuantityCheck.setSoftKeyboardEnabled(true);

        btnSubmit.setEnabled(false);
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

        mInvSkuGoodsPresenter.getByBarcodeMust(barcode);
    }

    private static final int REQUEST_CODE_INV_CONVERT_TO = 1;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_INV_CONVERT_TO: {
                //商品签收成功
                hideProgressDialog();
                if (resultCode == Activity.RESULT_OK) {
                    refresh(null);
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.fab_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        isAcceptBarcodeEnabled = false;

        onSubmitProcess();

        if (curGoods == null) {
            onSubmitError("请先扫描商品");
            return;
        }

        if (StringUtils.isEmpty(labelQuantityCheck.getInput())) {
            onSubmitError("库存数不能为空");
            return;
        }

        // TODO: 5/19/16
        ChangeSkuStoreItem itemWrapper = new ChangeSkuStoreItem();
        itemWrapper.setId(curGoods.getId());
        itemWrapper.setQuantity(Double.valueOf(labelQuantityCheck.getInput()));

        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_SKUGOODS_CONVERT_TO);
        extras.putSerializable(InvConvertToFragment.EXTRA_KEY_CONVERT_SKUGOODS, itemWrapper);

        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, REQUEST_CODE_INV_CONVERT_TO);
    }

    /**
     * 提交处理中
     */
    public void onSubmitProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
    }

    /**
     * 提交失败
     */
    public void onSubmitError(String errorMsg) {
        if (!StringUtils.isEmpty(errorMsg)) {
            showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
            ZLogger.df(errorMsg);
        } else {
            hideProgressDialog();
        }
        isAcceptBarcodeEnabled = true;
        btnSubmit.setEnabled(true);
    }

    /**
     * 提交成功
     */
    public void onSubmitSuccess() {
        showProgressDialog(ProgressDialog.STATUS_DONE, "操作成功", true);
//        hideProgressDialog();
    }

    /**
     * 刷新信息
     */
    private void refresh(InvSkuGoods invSkuGoods) {
        mScanBar.reset();
        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), mScanBar);

        curGoods = invSkuGoods;
        if (curGoods == null) {
            labelBarcode.setTvSubTitle("");
            labelProductName.setTvSubTitle("");
            labelQuantity.setTvSubTitle("");
            labelQuantityCheck.setInput("");
            tvUnit.setText("");

            btnSubmit.setEnabled(false);

//            DeviceUtils.hideSoftInput(getActivity(), etQuery);
        } else {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelProductName.setTvSubTitle(curGoods.getName());
            labelQuantity.setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), "暂无数据"));
            labelQuantityCheck.setInput("");
            tvUnit.setText(curGoods.getUnit());

            btnSubmit.setEnabled(true);

            labelQuantityCheck.requestFocusEnd();
        }
    }

    @Override
    public void onProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在搜索商品...", false);
    }

    @Override
    public void onError(String errorMsg) {

        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);

        refresh(null);
    }

    @Override
    public void onSuccess(InvSkuGoods invSkuGoods) {

        hideProgressDialog();

        refresh(invSkuGoods);
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
}
