package com.manfenjiayuan.pda_supermarket.ui.invlabel;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.QueryBarcodeFragment;
import com.manfenjiayuan.pda_supermarket.ui.goods.IScGoodsSkuView;
import com.manfenjiayuan.pda_supermarket.ui.goods.ScGoodsSkuPresenter;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.InvSkuLabelApi;
import com.mfh.framework.api.invIoOrder.InvIoOrderApi;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.List;

import butterknife.Bind;


/**
 * 打印价签
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvLabelFragment extends QueryBarcodeFragment implements IScGoodsSkuView {

    public static final String EXTRA_KEY_BARCODE = "EXTRA_KEY_BARCODE";

    @Bind(R.id.label_barcode)
    TextLabelView labelBarcode;
    @Bind(R.id.label_productName)
    TextLabelView labelProductName;
    @Bind(R.id.label_shortname)
    TextLabelView labelShortName;
    @Bind(R.id.label_costPrice)
    TextLabelView labelCostPrice;
    @Bind(R.id.label_costScore)
    TextLabelView labelCostScore;
    @Bind(R.id.label_prodArea)
    TextLabelView labelProdArea;
    @Bind(R.id.label_prodLevel)
    TextLabelView labelProdLevel;

    private ScGoodsSku curGoods = null;
    private ScGoodsSkuPresenter chainGoodsSkuPresenter;


    public static InvLabelFragment newInstance(Bundle args) {
        InvLabelFragment fragment = new InvLabelFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_invlabel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chainGoodsSkuPresenter = new ScGoodsSkuPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        super.createViewInner(rootView, container, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            String barcode = args.getString(EXTRA_KEY_BARCODE, null);

            queryByBarcode(barcode);
        }
    }


    /**
     * 查询商品
     * */
    @Override
    public void sendQueryReq(String barcode) {
        super.sendQueryReq(barcode);

        if (StringUtils.isEmpty(barcode)){
            onQueryError("条码无效");
            return;
        }

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            onQueryError(getString(R.string.toast_network_error));
        }
        else{
            // TODO: 7/30/16 执行查询动作
            chainGoodsSkuPresenter.getByBarcode(barcode);
        }
    }

    @Override
    public void submit() {
        super.submit();

        if (curGoods == null ) {
            onSubmitError("商品无效");
            return;
        }

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            onSubmitError(getString(R.string.toast_network_error));
            return;
        }

        JSONObject jsonStr = new JSONObject();
        jsonStr.put("proSkuId", curGoods.getProSkuId());
//        jsonStr.put("storeType", curGoods.getStoreType());
        jsonStr.put("storeType", InvIoOrderApi.STORE_TYPE_RETAIL);
        jsonStr.put("barcode", curGoods.getBarcode());
        jsonStr.put("skuName", curGoods.getSkuName());
        jsonStr.put("shortName", curGoods.getShortName());
        jsonStr.put("unit", curGoods.getUnit());
        jsonStr.put("priceType", curGoods.getPriceType());
        jsonStr.put("costPrice", curGoods.getCostPrice());
        jsonStr.put("costScore", curGoods.getCostScore());
        //customPrice目前就用costPrice代替
        jsonStr.put("customPrice", curGoods.getCostPrice());
        jsonStr.put("prodArea", curGoods.getProdArea());
        jsonStr.put("prodLevel", curGoods.getProdLevel());

        InvSkuLabelApi.create(jsonStr.toJSONString(), responseCallback);
    }


    @Override
    public void onSubmitSuccess() {
        super.onSubmitSuccess();

        refreshPackage(null);
    }

    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    onSubmitError(errMsg);
                }

                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}
                    onSubmitSuccess();
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };

    /**
     * 刷新信息
     * */
    private void refreshPackage(ScGoodsSku goods){
        curGoods = goods;
        if (curGoods == null){
            labelBarcode.setTvSubTitle("");
            labelProductName.setTvSubTitle("");
            labelShortName.setTvSubTitle("");
            labelCostPrice.setTvSubTitle("");
            labelCostScore.setTvSubTitle("");
            labelProdArea.setTvSubTitle("");
            labelProdLevel.setTvSubTitle("");

            btnSubmit.setEnabled(false);
        }
        else {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelProductName.setTvSubTitle(curGoods.getSkuName());
            labelShortName.setTvSubTitle(curGoods.getShortName());
            labelCostPrice.setTvSubTitle(MUtils.formatDouble(null, null,
                    curGoods.getCostPrice(), "", "/", curGoods.getUnit()));
            labelCostScore.setTvSubTitle(MUtils.formatDouble(curGoods.getCostScore(), ""));
            labelProdArea.setTvSubTitle(curGoods.getProdArea());
            labelProdLevel.setTvSubTitle(curGoods.getProdLevel());

            btnSubmit.setEnabled(true);
        }

        refresh();
    }

    @Override
    public void onIScGoodsSkuViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询商品...", false);
    }

    @Override
    public void onIScGoodsSkuViewError(String errorMsg) {
        onQuerySuccess();
        refreshPackage(null);
    }

    @Override
    public void onIScGoodsSkuViewSuccess(List<ScGoodsSku> scGoodsSkus) {

    }

    @Override
    public void onIScGoodsSkuViewSuccess(ScGoodsSku goodsSku) {
        onQuerySuccess();

        if (goodsSku == null) {
            DialogUtil.showHint("未找到商品");
        }

        refreshPackage(goodsSku);
    }
}
