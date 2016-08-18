package com.bingshanguxue.pda.bizz.goods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.DataSyncManager;
import com.bingshanguxue.pda.R;
import com.bingshanguxue.pda.widget.EditLabelView;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.invSkuStore.InvSkuStoreApiImpl;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import de.greenrobot.event.EventBus;


/**
 * 库存商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class GoodsInfoFragment extends BaseFragment {

//    @Bind(R.id.label_productName)
    TextLabelView labelProductName;
//    @Bind(R.id.label_barcodee)
    TextLabelView labelBarcode;
//    @Bind(R.id.label_buyprice)
    TextLabelView labelBuyprice;
//    @Bind(R.id.label_costPrice)
    EditLabelView labelCostPrice;
//    @Bind(R.id.label_grossProfit)
    TextLabelView labelGrossProfit;
//    @Bind(R.id.label_quantity)
    TextLabelView labelQuantity;
//    @Bind(R.id.label_upperLimit)
    EditLabelView labelUpperLimit;
//    @Bind(R.id.label_sellNumber)
    TextLabelView labelSellNum7;
//    @Bind(R.id.label_avgSellNum)
    TextLabelView labelSellNum30;

//    @Bind(R.id.fab_submit)
    public FloatingActionButton btnSubmit;

    private ScGoodsSku curGoods = null;
    private boolean isEditable = true;//网店商品档案允许被修改，平台商品档案不允许被修改。

    public static GoodsInfoFragment newInstance(Bundle args) {
        GoodsInfoFragment fragment = new GoodsInfoFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_goods_info;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        labelProductName = (TextLabelView) rootView.findViewById(R.id.label_productName);
        labelBarcode = (TextLabelView) rootView.findViewById(R.id.label_barcodee);
        labelBuyprice = (TextLabelView) rootView.findViewById(R.id.label_buyprice);
        labelCostPrice = (EditLabelView) rootView.findViewById(R.id.label_costPrice);
        labelGrossProfit = (TextLabelView) rootView.findViewById(R.id.label_grossProfit);
        labelQuantity = (TextLabelView) rootView.findViewById(R.id.label_quantity);
        labelUpperLimit = (EditLabelView) rootView.findViewById(R.id.label_upperLimit);
        labelSellNum7 = (TextLabelView) rootView.findViewById(R.id.label_sellNum7);
        labelSellNum30 = (TextLabelView) rootView.findViewById(R.id.label_sellNum30);
        btnSubmit = (FloatingActionButton) rootView.findViewById(R.id.fab_submit);

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
        labelCostPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (curGoods != null) {
                    //计算毛利率:(costPrice-buyPrice) / buyPrice
                    String grossProfit = MUtils.retrieveFormatedGrossMargin(curGoods.getCostPrice(),
                            (calInputCostPrice() - curGoods.getBuyPrice()));
                    labelGrossProfit.setTvSubTitle(grossProfit);
                }
            }
        });
//        labelCostPrice.setSoftKeyboardEnabled(false);
        labelUpperLimit.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onKeycodeEnterClick(String text) {
                submit();
//                labelLowerLimit.requestFocusEnd();
            }

            @Override
            public void onScan() {
                refresh(null);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    /**
     * 验证
     */
    public void onEventMainThread(ScGoodsSkuEvent event) {
        int eventId = event.getEventId();
        Bundle args = event.getArgs();

        ZLogger.d(String.format("ScGoodsSkuEvent(%d)", eventId));
        switch (eventId) {
            case ScGoodsSkuEvent.EVENT_ID_SKU_UPDATE: {
                ScGoodsSku sku = (ScGoodsSku) args.getSerializable(ScGoodsSkuEvent.EXTRA_KEY_SCGOODSSKU);
                isEditable = args.getBoolean(ScGoodsSkuEvent.EXTRA_KEY_ISEDITABLE, true);
                refresh(sku);
            }
            break;

        }
    }

    private Double calInputCostPrice() {
        String price = labelCostPrice.getInput();
        if (StringUtils.isEmpty(price)) {
            return 0D;
        }

        return Double.valueOf(price);
    }

//    @OnClick(R.id.fab_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        onSubmitProcess();

        if (curGoods == null) {
            onSubmitError("商品无效");
            return;
        }

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            onSubmitError(getString(R.string.toast_network_error));
            return;
        }

        if (StringUtils.isEmpty(labelCostPrice.getInput())) {
            onSubmitError("销售价不能为空");
            return;
        }

        if (StringUtils.isEmpty(labelUpperLimit.getInput())) {
            onSubmitError("排面库存不能为空");
            return;
        }

//        if (StringUtils.isEmpty(labelLowerLimit.getInput())) {
//            DialogUtil.showHint("安全库存不能为空");
//            btnSubmit.setEnabled(true);
//            isAcceptBarcodeEnabled = true;
//            return;
//        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在提交信息...", false);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", curGoods.getId());
        jsonObject.put("costPrice", labelCostPrice.getInput());
        jsonObject.put("upperLimit", labelUpperLimit.getInput());
//        jsonObject.put("lowerLimit", labelLowerLimit.getInput());
        jsonObject.put("tenantId", MfhLoginService.get().getSpid());

        //回调
        InvSkuStoreApiImpl.update(jsonObject.toJSONString(), updateResponseCallback);
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
                    onSubmitSuccess();

                    DataSyncManager.getInstance().notifyUpdateSku();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    onSubmitError(errMsg);
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
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
        btnSubmit.setEnabled(true);
    }

    public void onSubmitSuccess() {
        showProgressDialog(ProgressDialog.STATUS_DONE, "操作成功", true);
        btnSubmit.setEnabled(true);
        //修改商品信息成功后，清空商品信息
//        refresh(null);
    }

    /**
     * 刷新信息
     */
    private void refresh(ScGoodsSku invSkuGoods) {
        curGoods = invSkuGoods;
        if (curGoods == null) {
            labelBarcode.setTvSubTitle("");
            labelProductName.setTvSubTitle("");
            labelBuyprice.setTvSubTitle("");
            labelCostPrice.setInput("");
            labelGrossProfit.setTvSubTitle("");
            labelQuantity.setTvSubTitle("");
            labelUpperLimit.setInput("");
            labelSellNum30.setTvSubTitle("");
            labelSellNum7.setTvSubTitle("");

            labelCostPrice.setInputEnabled(false);
            labelUpperLimit.setInputEnabled(false);
            btnSubmit.setEnabled(false);
            btnSubmit.setVisibility(View.GONE);

//            DeviceUtils.hideSoftInput(getActivity(), etQuery);
        } else {
            labelProductName.setTvSubTitle(curGoods.getSkuName());
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelCostPrice.setInput(MUtils.formatDouble(curGoods.getCostPrice(), ""));
            labelQuantity.setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), "暂无数据"));
            labelUpperLimit.setInput(MUtils.formatDouble(curGoods.getUpperLimit(), ""));
            labelSellNum7.setTvSubTitle("暂无数据");
            labelSellNum30.setTvSubTitle(MUtils.formatDouble(curGoods.getAvgSellNum(), ""));

            //计算毛利率:(costPrice-buyPrice) / costPrice
            String grossProfit = MUtils.retrieveFormatedGrossMargin(curGoods.getCostPrice(),
                    (curGoods.getCostPrice() - curGoods.getBuyPrice()));
            labelGrossProfit.setTvSubTitle(grossProfit);
            labelBuyprice.setTvSubTitle(MUtils.formatDouble(curGoods.getBuyPrice(), ""));

            labelCostPrice.setInputEnabled(isEditable);
            labelUpperLimit.setInputEnabled(isEditable);
            if (isEditable){
                labelCostPrice.requestFocusEnd();
                btnSubmit.setVisibility(View.VISIBLE);
                btnSubmit.setEnabled(true);
            }
            else{
                btnSubmit.setVisibility(View.GONE);
            }
        }
    }
}
