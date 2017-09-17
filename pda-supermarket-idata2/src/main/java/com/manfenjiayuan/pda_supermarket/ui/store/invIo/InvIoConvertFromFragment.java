package com.manfenjiayuan.pda_supermarket.ui.store.invIo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.PDAScanManager;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.vector_uikit.widget.ScanBar;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.mvp.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.mvp.view.IInvSkuGoodsView;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.common.SecondaryActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invSkuStore.InvSkuGoods;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.prefs.SharedPrefesManagerFactory;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 库存转换
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvIoConvertFromFragment extends PDAScanFragment implements IInvSkuGoodsView {

    @BindView(R.id.toolbar)
    public Toolbar mToolbar;
    @BindView(R.id.scanBar)
    public ScanBar mScanBar;

    @BindView(R.id.label_barcodee)
    TextLabelView labelBarcode;
    @BindView(R.id.label_productName)
    TextLabelView labelProductName;
    @BindView(R.id.label_quantity)
    TextLabelView labelQuantity;
    @BindView(R.id.label_quantity_check)
    EditLabelView labelQuantityCheck;
    @BindView(R.id.fab_submit)
    public FloatingActionButton btnSubmit;
    @BindView(R.id.fab_scan)
    FloatingActionButton btnSweep;

    private InvSkuGoods curGoods = null;
    private InvSkuGoodsPresenter mInvSkuGoodsPresenter = null;

    public static InvIoConvertFromFragment newInstance(Bundle args) {
        InvIoConvertFromFragment fragment = new InvIoConvertFromFragment();

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
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
        }
        if (animType == ANIM_TYPE_NEW_FLOW) {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        } else {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        }
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        if (mScanBar != null) {
//            mScanBar.setSoftKeyboardEnabled(true);
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
        labelQuantityCheck.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER},
                new EditLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                            submit();
                        }
                    }
                });
        btnSweep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt(PDAScanManager.ScanBarcodeEvent.KEY_EVENTID,
                        PDAScanManager.ScanBarcodeEvent.EVENT_ID_START_ZXING);
                EventBus.getDefault().post(new PDAScanManager.ScanBarcodeEvent(args));
            }
        });

        if (SharedPrefesManagerFactory.isCameraSweepEnabled()) {
            btnSweep.setVisibility(View.VISIBLE);
        } else {
            btnSweep.setVisibility(View.GONE);
        }
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

        if (!NetworkUtils.isConnect(getActivity())) {
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
                if (resultCode == Activity.RESULT_OK) {
                    getActivity().setResult(Activity.RESULT_OK, data);
                    getActivity().finish();
                } else {
                    btnSubmit.setEnabled(true);
                    hideProgressDialog();
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

        curGoods.setQuantityCheck(Double.valueOf(labelQuantityCheck.getInput()));

        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_SKUGOODS_CONVERT_TO);
        extras.putSerializable(InvConvertToFragment.EXTRA_KEY_INV_SKU_GOODS, curGoods);

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
            labelQuantityCheck.setEndText("");

            btnSubmit.setEnabled(false);

//            DeviceUtils.hideSoftInput(getActivity(), etQuery);
        } else {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelProductName.setTvSubTitle(curGoods.getName());
            labelQuantity.setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), "暂无数据"));
            labelQuantityCheck.setInput("");
            labelQuantityCheck.setEndText(curGoods.getUnit());
            labelQuantityCheck.requestFocusEnd();

            btnSubmit.setEnabled(true);
        }
    }

    @Override
    public void onIInvSkuGoodsViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在搜索商品...", false);
    }

    @Override
    public void onIInvSkuGoodsViewError(String errorMsg) {
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);

        refresh(null);
    }

    @Override
    public void onIInvSkuGoodsViewSuccess(InvSkuGoods invSkuGoods) {
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
