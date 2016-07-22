package com.manfenjiayuan.pda_supermarket.ui.goods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.widget.EditLabelView;
import com.bingshanguxue.pda.widget.EditQueryView;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.DataSyncManager;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.activity.PrimaryActivity;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.impl.StockApiImpl;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 库存商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class GoodsFragment extends PDAScanFragment implements IGoodsView {
    private static final String TAG = "GoodsFragment";

    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;
    @Bind(R.id.label_productName)
    TextLabelView labelProductName;
    @Bind(R.id.label_barcodee)
    TextLabelView labelBarcode;
    @Bind(R.id.label_buyprice)
    TextLabelView labelBuyprice;
    @Bind(R.id.label_costPrice)
    EditLabelView labelCostPrice;
    @Bind(R.id.label_sellMonthNum)
    TextLabelView labelSellMonthNum;
    @Bind(R.id.label_grossProfit)
    TextLabelView labelGrossProfit;
    @Bind(R.id.label_quantity)
    TextLabelView labelQuantity;
    @Bind(R.id.label_upperLimit)
    EditLabelView labelUpperLimit;
    @Bind(R.id.label_provider)
    SettingsItem labelProvider;

    @Bind(R.id.button_submit)
    Button btnSubmit;

    private ScGoodsSku curGoods = null;
    private GoodsPresenter mGoodsPresenter = null;

    public static GoodsFragment newInstance(Bundle args) {
        GoodsFragment fragment = new GoodsFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_goods;
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

        mGoodsPresenter = new GoodsPresenter(this);
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
        labelCostPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (curGoods != null){
                    //计算毛利率:(costPrice-buyPrice) / buyPrice
                    String grossProfit = MUtils.retrieveFormatedGrossMargin(curGoods.getBuyPrice(),
                            (calInputCostPrice() - curGoods.getBuyPrice()));
                    labelGrossProfit.setTvSubTitle(grossProfit);
                }
            }
        });
//        labelCostPrice.setSoftKeyboardEnabled(false);
        labelUpperLimit.config(EditLabelView.INPUT_TYPE_NUMBER_DECIMAL);
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

    private Double calInputCostPrice(){
        String price = labelCostPrice.getEtContent();
        if (StringUtils.isEmpty(price)) {
            return 0D;
        }

         return Double.valueOf(price);
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

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            isAcceptBarcodeEnabled = true;
            refresh(null);
            return;
        }

        if (mGoodsPresenter != null) {
            mGoodsPresenter.findGoodsList(barcode);
        } else {
            refresh(null);
        }
    }

    @OnClick(R.id.label_provider)
    public void queryProvider() {
        if (curGoods == null) {
            return;
        }

        Bundle extras = new Bundle();
//                    extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(PrimaryActivity.EXTRA_KEY_SERVICE_TYPE, PrimaryActivity.FRAGMENT_TYPE_GOODS_PROVIDER);
        extras.putString(ChainGoodsSkuFragment.EXTRA_KEY_BARCODE, curGoods.getBarcode());
        PrimaryActivity.actionStart(getActivity(), extras);
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

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
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

//        if (StringUtils.isEmpty(labelLowerLimit.getEtContent())) {
//            DialogUtil.showHint("安全库存不能为空");
//            btnSubmit.setEnabled(true);
//            isAcceptBarcodeEnabled = true;
//            return;
//        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在提交信息...", false);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", curGoods.getId());
        jsonObject.put("costPrice", labelCostPrice.getEtContent());
        jsonObject.put("upperLimit", labelUpperLimit.getEtContent());
//        jsonObject.put("lowerLimit", labelLowerLimit.getEtContent());
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
    private void refresh(ScGoodsSku invSkuGoods) {
        curGoods = invSkuGoods;
        if (curGoods == null) {
            labelBarcode.setTvSubTitle("");
            labelProductName.setTvSubTitle("");
            labelBuyprice.setTvSubTitle("");
            labelCostPrice.setEtContent("");
            labelCostPrice.setEnabled(false);
            labelGrossProfit.setTvSubTitle("");
            labelSellMonthNum.setTvSubTitle("");
            labelQuantity.setTvSubTitle("");
            labelUpperLimit.setEtContent("");
            labelUpperLimit.setEnabled(false);
//            labelLowerLimit.setEtContent("");
//            labelLowerLimit.setEnabled(false);
            eqvBarcode.clear();
            eqvBarcode.requestFocus();
            labelProvider.setEnabled(false);

            btnSubmit.setEnabled(false);


//            DeviceUtils.hideSoftInput(getActivity(), etQuery);
        } else {
            labelProductName.setTvSubTitle(curGoods.getSkuName());
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelCostPrice.setEtContent(MUtils.formatDouble(curGoods.getCostPrice(), ""));
            labelCostPrice.setEnabled(true);
            labelQuantity.setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), "暂无数据"));
            labelUpperLimit.setEtContent(MUtils.formatDouble(curGoods.getUpperLimit(), ""));
            labelUpperLimit.setEnabled(true);
//            labelLowerLimit.setEtContent(MUtils.formatDouble(curGoods.getLowerLimit(), ""));
//            labelLowerLimit.setEnabled(true);


            //计算毛利率:(costPrice-buyPrice) / buyPrice
            String grossProfit = MUtils.retrieveFormatedGrossMargin(curGoods.getBuyPrice(),
                    (curGoods.getCostPrice() - curGoods.getBuyPrice()));
            labelGrossProfit.setTvSubTitle(grossProfit);
            labelSellMonthNum.setTvSubTitle(MUtils.formatDouble(curGoods.getSellMonthNum(), ""));
            labelBuyprice.setTvSubTitle(MUtils.formatDouble(curGoods.getBuyPrice(), ""));

            labelCostPrice.requestFocusEnd();

            labelProvider.setEnabled(true);
            btnSubmit.setEnabled(true);
        }

        isAcceptBarcodeEnabled = true;
        DeviceUtils.hideSoftInput(getActivity(), labelCostPrice);
    }


    @Override
    public void onIGoodsViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在搜索商品...", false);
    }

    @Override
    public void onIGoodsViewError(String errorMsg) {
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);

        DialogUtil.showHint("未找到商品");
        refresh(null);
    }

    @Override
    public void onIGoodsViewSuccess(List<ScGoodsSku> scGoodsSkus) {
        if (scGoodsSkus != null && scGoodsSkus.size() > 0) {
            ScGoodsSku scGoodsSku = scGoodsSkus.get(0);
            refresh(scGoodsSku);
        } else {
            DialogUtil.showHint("未找到商品");
            refresh(null);
        }

        hideProgressDialog();

    }
}
