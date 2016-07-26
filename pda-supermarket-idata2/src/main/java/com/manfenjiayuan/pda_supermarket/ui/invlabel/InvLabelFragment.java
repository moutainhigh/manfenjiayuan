package com.manfenjiayuan.pda_supermarket.ui.invlabel;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.widget.EditQueryView;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.goods.IScGoodsSkuView;
import com.manfenjiayuan.pda_supermarket.ui.goods.ScGoodsSkuPresenter;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.InvSkuLabelApi;
import com.mfh.framework.api.invIoOrder.InvIoOrderApi;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 打印价签
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvLabelFragment extends PDAScanFragment implements IScGoodsSkuView {

    private static final String TAG = "InvReturnGoodsInspectFragment";
    public static final String EXTRA_KEY_COMPANYID = "companyId";
    public static final String EXTRA_KEY_BARCODE = "EXTRA_KEY_BARCODE";

    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;

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

    @Bind(R.id.button_submit)
    Button btnSubmit;

    private ScGoodsSku curGoods = null;
    private ScGoodsSkuPresenter chainGoodsSkuPresenter;
    private boolean isQueryProcessing;


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
    protected void onScanCode(String code) {
//        eqvBarcode.setInputString(code);
        query(code);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chainGoodsSkuPresenter = new ScGoodsSkuPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        eqvBarcode.config(EditQueryView.INPUT_TYPE_TEXT);
        eqvBarcode.setSoftKeyboardEnabled(true);
        eqvBarcode.setInputSubmitEnabled(true);
        eqvBarcode.setOnViewListener(new EditQueryView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                query(text);
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            String barcode = args.getString(EXTRA_KEY_BARCODE, null);

//            eqvBarcode.setInputString(barcode);
            query(barcode);
        }
    }

    /**
     * 查询包裹信息
     * */
    public void query(String barcode){
        eqvBarcode.clear();

        if (isQueryProcessing || StringUtils.isEmpty(barcode)){
            return;
        }

        chainGoodsSkuPresenter.getGoodsByBarCode(barcode);
    }

    @OnClick(R.id.button_submit)
    public void submit() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);

        if (curGoods == null ) {
            btnSubmit.setEnabled(true);
            DialogUtil.showHint("商品无效");
            hideProgressDialog();
            return;
        }

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
//            animProgress.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            hideProgressDialog();
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

    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.df("创建待打印价签失败: " + errMsg);
//                    {"code":"1","msg":"132079网点有仓储单正在处理中...","version":"1","data":null}
                    //查询失败
//                        animProgress.setVisibility(View.GONE);
//                    DialogUtil.showHint("新建退货单失败" + errMsg);
                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    btnSubmit.setEnabled(true);
                }

                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}
//                        animProgress.setVisibility(View.GONE);
                    /**
                     * 新建退货单成功，更新采购单列表
                     * */
                    ZLogger.df("创建待打印价签成功:");
                    showProgressDialog(ProgressDialog.STATUS_DONE, "上传成功", true);
                    refreshPackage(null);
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

            eqvBarcode.clear();
            eqvBarcode.requestFocus();
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

        DeviceUtils.hideSoftInput(getActivity(), eqvBarcode);
    }

    @Override
    public void onIScGoodsSkuViewProcess() {

        isQueryProcessing = true;
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询商品...", false);
    }

    @Override
    public void onIScGoodsSkuViewError(String errorMsg) {
        hideProgressDialog();
        isQueryProcessing = false;
        refreshPackage(null);
    }

    @Override
    public void onIScGoodsSkuViewSuccess(List<ScGoodsSku> scGoodsSkus) {

    }

    @Override
    public void onIScGoodsSkuViewSuccess(ScGoodsSku goodsSku) {
        hideProgressDialog();
        isQueryProcessing = false;

        if (goodsSku == null) {
            DialogUtil.showHint("未找到商品");
        }

        refreshPackage(goodsSku);
    }
}
