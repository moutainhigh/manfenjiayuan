package com.manfenjiayuan.pda_supermarket.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.manfenjiayuan.business.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IInvSkuGoodsView;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.DataSyncManager;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.scanner.PDAScanFragment;
import com.manfenjiayuan.pda_supermarket.widget.compound.EditLabelView;
import com.manfenjiayuan.pda_supermarket.widget.compound.EditQueryView;
import com.manfenjiayuan.pda_supermarket.widget.compound.TextLabelView;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.invOrder.StockApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 库存商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class GoodsFragment extends PDAScanFragment implements IInvSkuGoodsView {
    private static final String TAG = "GoodsFragment";

    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;
    @Bind(R.id.label_barcodee)
    TextLabelView labelBarcode;
    @Bind(R.id.label_productName)
    TextLabelView labelProductName;
    @Bind(R.id.label_quantity)
    TextLabelView labelQuantity;
    @Bind(R.id.label_costPrice)
    EditLabelView labelCostPrice;
    @Bind(R.id.label_upperLimit)
    EditLabelView labelUpperLimit;
    @Bind(R.id.label_lowwerLimit)
    EditLabelView labelLowerLimit;

    @Bind(R.id.button_submit)
    Button btnSubmit;

    private InvSkuGoods curGoods = null;
    private InvSkuGoodsPresenter mInvSkuGoodsPresenter = null;

    public static GoodsFragment newInstance(Bundle args) {
        GoodsFragment fragment = new GoodsFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_commodity;
    }

    @Override
    protected void onScanCode(String code) {
//        eqvBarcode.setInputString(code);
        eqvBarcode.requestFocus();
        eqvBarcode.clear();
        query(code);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInvSkuGoodsPresenter = new InvSkuGoodsPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        labelCostPrice.config(EditLabelView.INPUT_TYPE_NUMBER_DECIMAL);
        labelCostPrice.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                labelUpperLimit.requestFocusEnd();
            }

            @Override
            public void onScan() {
                refresh(null);
            }
        });
//        labelCostPrice.setSoftKeyboardEnabled(false);
        labelUpperLimit.config(EditLabelView.INPUT_TYPE_NUMBER_DECIMAL);
        labelUpperLimit.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                labelLowerLimit.requestFocusEnd();
            }

            @Override
            public void onScan() {
                refresh(null);
            }
        });

        labelLowerLimit.config(EditLabelView.INPUT_TYPE_NUMBER_DECIMAL);
        labelLowerLimit.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                submit();
            }

            @Override
            public void onScan() {
                refresh(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
            }
        });
//        labelLowerLimit.setSoftKeyboardEnabled(false);

        eqvBarcode.config(EditQueryView.INPUT_TYPE_TEXT);
        eqvBarcode.setSoftKeyboardEnabled(true);
        eqvBarcode.setInputSubmitEnabled(true);
        eqvBarcode.setOnViewListener(new EditQueryView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                eqvBarcode.requestFocus();
                eqvBarcode.clear();
                query(text);
            }
        });
        btnSubmit.setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
//        refresh(null);
    }

    /**
     * 查询商品信息
     */
    public void query(String barcode) {
        isAcceptBarcodeEnabled = false;
        if (StringUtils.isEmpty(barcode)) {
            eqvBarcode.requestFocus();
            isAcceptBarcodeEnabled = true;
            return;
        }

        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            isAcceptBarcodeEnabled = true;
            refresh(null);
            return;
        }

        if (mInvSkuGoodsPresenter != null){
            mInvSkuGoodsPresenter.getByBarcodeMust(barcode);
        }
        else{
            refresh(null);
        }

    }

    @OnClick(R.id.button_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        isAcceptBarcodeEnabled = false;
        if (curGoods == null) {
            btnSubmit.setEnabled(true);
            isAcceptBarcodeEnabled = true;
            return;
        }

        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            isAcceptBarcodeEnabled = true;
            return;
        }

        if (StringUtils.isEmpty(labelCostPrice.getEtContent())) {
            DialogUtil.showHint("销售价不能为空");
            btnSubmit.setEnabled(true);
            isAcceptBarcodeEnabled = true;
            return;
        }

        if (StringUtils.isEmpty(labelLowerLimit.getEtContent())) {
            DialogUtil.showHint("安全库存不能为空");
            btnSubmit.setEnabled(true);
            isAcceptBarcodeEnabled = true;
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在提交信息...", false);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", curGoods.getId());
        jsonObject.put("costPrice", labelCostPrice.getEtContent());
        jsonObject.put("upperLimit", labelUpperLimit.getEtContent());
        jsonObject.put("lowerLimit", labelLowerLimit.getEtContent());
        jsonObject.put("tenantId", MfhLoginService.get().getSpid());

//        animProgress.setVisibility(View.VISIBLE);

        //回调
        StockApiImpl.updateStockGoods(jsonObject.toJSONString(), updateResponseCallback);
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
                    ZLogger.d("修改成功:" + retStr);
//                        DialogUtil.showHint("修改成功");

//                    hideProgressDialog();
                    showProgressDialog(ProgressDialog.STATUS_ERROR, "修改成功", true);
                    refresh(null);

                    DataSyncManager.getInstance().notifyUpdateSku();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("修改失败：" + errMsg);
                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    btnSubmit.setEnabled(true);
                }
            }
            , String.class
            , AppContext.getAppContext()) {
    };

    /**
     * 刷新信息
     */
    private void refresh(InvSkuGoods invSkuGoods) {
        curGoods = invSkuGoods;
        if (curGoods == null) {
            labelBarcode.setTvSubTitle("");
            labelProductName.setTvSubTitle("");
            labelCostPrice.setEtContent("");
            labelCostPrice.setEnabled(false);
            labelQuantity.setTvSubTitle("");
            labelUpperLimit.setEtContent("");
            labelUpperLimit.setEnabled(false);
            labelLowerLimit.setEtContent("");
            labelLowerLimit.setEnabled(false);

            btnSubmit.setEnabled(false);

            eqvBarcode.clear();
            eqvBarcode.requestFocus();

//            DeviceUtils.hideSoftInput(getActivity(), etQuery);
        } else {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelProductName.setTvSubTitle(curGoods.getName());
            labelCostPrice.setEtContent(MUtils.formatDouble(curGoods.getCostPrice(), ""));
            labelCostPrice.setEnabled(true);
            labelQuantity.setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), "暂无数据"));
            labelUpperLimit.setEtContent(MUtils.formatDouble(curGoods.getUpperLimit(), ""));
            labelUpperLimit.setEnabled(true);
            labelLowerLimit.setEtContent(MUtils.formatDouble(curGoods.getLowerLimit(), ""));
            labelLowerLimit.setEnabled(true);

            btnSubmit.setEnabled(true);

            labelCostPrice.requestFocusEnd();
        }

        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), labelCostPrice);
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
}
