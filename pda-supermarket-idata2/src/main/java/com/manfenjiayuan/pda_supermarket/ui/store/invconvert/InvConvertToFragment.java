package com.manfenjiayuan.pda_supermarket.ui.store.invconvert;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.pda.widget.ScanBar;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.manfenjiayuan.business.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IInvSkuGoodsView;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.bean.wrapper.ChangeSkuStoreItem;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.invSkuStore.InvSkuStoreApiImpl;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 库存转换
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvConvertToFragment extends PDAScanFragment implements IInvSkuGoodsView {

    public static final String EXTRA_KEY_CONVERT_SKUGOODS = "convertSkuGoods";
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

    private ChangeSkuStoreItem convertSkuGoods;
    private InvSkuGoods curGoods;
    private InvSkuGoodsPresenter mInvSkuGoodsPresenter = null;

    public static InvConvertToFragment newInstance(Bundle args) {
        InvConvertToFragment fragment = new InvConvertToFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_invconvert_to;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInvSkuGoodsPresenter = new InvSkuGoodsPresenter(this);
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

        btnSubmit.setEnabled(false);

        Bundle args = getArguments();
        if (args != null) {
            convertSkuGoods = (ChangeSkuStoreItem) args.getSerializable(EXTRA_KEY_CONVERT_SKUGOODS);
        }

        if (convertSkuGoods == null) {
            DialogUtil.showHint("转换商品无效");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }
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
    public void onResume() {
        super.onResume();
        refresh(curGoods);
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

    @OnClick(R.id.fab_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        isAcceptBarcodeEnabled = false;

        onSubmitProcess();

        if (curGoods == null) {
            onSubmitError("请扫描商品");
            return;
        }

        if (!NetworkUtils.isConnect(getActivity())) {
            onSubmitError(getString(R.string.toast_network_error));
            return;
        }

        if (StringUtils.isEmpty(labelQuantityCheck.getInput())) {
            onSubmitError("库存数不能为空");
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在提交信息...", false);
        JSONArray sendItemsJsonArray = new JSONArray();
        sendItemsJsonArray.add(convertSkuGoods);
        JSONObject receiveItemJson = new JSONObject();
        receiveItemJson.put("id", curGoods.getId());
        receiveItemJson.put("quantity", labelQuantityCheck.getInput());

        //回调
        InvSkuStoreApiImpl.changeSkuStore(sendItemsJsonArray.toJSONString(),
                receiveItemJson.toJSONString(), updateResponseCallback);
    }

    NetCallBack.NetTaskCallBack updateResponseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                    RspValue<String> retValue = (RspValue<String>) rspData;
                    String retStr = retValue.getValue();

                    //出库成功:1-556637
                    ZLogger.d("转换成功:" + retStr);
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    onSubmitError(errMsg);
                }
            }
            , String.class
            , AppContext.getAppContext()) {
    };

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
//        showProgressDialog(ProgressDialog.STATUS_DONE, "操作成功", true);
        hideProgressDialog();
        DialogUtil.showHint("转换成功");
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
}
