package com.mfh.petitestock.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.impl.StockApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.petitestock.AppContext;
import com.mfh.petitestock.R;
import com.mfh.petitestock.bean.InvSkuGoods;
import com.mfh.petitestock.widget.compound.EditLabelView;
import com.mfh.petitestock.widget.compound.EditQueryView;
import com.mfh.petitestock.widget.compound.TextLabelView;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ChainGoodsFragment extends GpioFragment {
    public static final String EXTRA_MODULE = "module";

    private static final String TAG = "CommodityFragment";

    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;
    private final static int LABELVIEW_INDEX_NAME       = 0;
    private final static int LABELVIEW_INDEX_QUANTITY   = 1;
    private final static int LABELVIEW_INDEX_UPPERLIMIT = 2;
    private final static int LABELVIEW_INDEX_RACK_NO    = 3;

    @Bind({R.id.label_productName, R.id.label_quantity, R.id.label_upperLimit,
            R.id.label_rackno})
    List<TextLabelView> labelViews;
    @Bind(R.id.label_costPrice)
    EditLabelView labelCostPrice;
    @Bind(R.id.label_lowwerLimit)
    EditLabelView labelLowerLimit;

    @Bind(R.id.button_submit)
    Button btnSubmit;
    @Bind(R.id.animProgress)
    ProgressBar mProgressBar;

    private InvSkuGoods curGoods = null;

    public static ChainGoodsFragment newInstance(Bundle args) {
        ChainGoodsFragment fragment = new ChainGoodsFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_chain_goods;
    }

    @Override
    protected void onScanCode(String code) {
        eqvBarcode.setInputString(code);
        query(code);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        labelCostPrice.config(EditLabelView.INPUT_TYPE_NUMBER_DECIMAL);
        labelCostPrice.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onInput(String text) {
                submit();
            }

            @Override
            public void onScan() {
                init();
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
            }
        });
//        labelCostPrice.setSoftKeyboardEnabled(false);
        labelLowerLimit.config(EditLabelView.INPUT_TYPE_NUMBER_DECIMAL);
        labelLowerLimit.setOnViewListener(new EditLabelView.OnViewListener() {
            @Override
            public void onInput(String text) {
                submit();
            }

            @Override
            public void onScan() {
                init();
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
            }
        });
//        labelLowerLimit.setSoftKeyboardEnabled(false);

        eqvBarcode.config(EditQueryView.INPUT_TYPE_TEXT);
        eqvBarcode.setSoftKeyboardEnabled(true);
        eqvBarcode.setInputSubmitEnabled(false);
        eqvBarcode.setOnViewListener(new EditQueryView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                query(text);
            }
        });
        btnSubmit.setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    /**
     * 查询包裹信息
     */
    public void query(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            return;
        }

        curGoods = null;

        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            refresh();
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询数据...", false);
        StockApiImpl.getByBarcodeMust(barcode, queryRespCallback);
    }

    NetCallBack.NetTaskCallBack queryRespCallback = new NetCallBack.NetTaskCallBack<InvSkuGoods,
            NetProcessor.Processor<InvSkuGoods>>(
            new NetProcessor.Processor<InvSkuGoods>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //java.lang.ClassCastException: com.mfh.comn.net.data.RspValue cannot be cast to com.mfh.comn.net.data.RspBean
                    RspBean<InvSkuGoods> retValue = (RspBean<InvSkuGoods>) rspData;
                    curGoods = retValue.getValue();

                    hideProgressDialog();
                    refresh();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("查询商品失败：" + errMsg);

                    showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
                    refresh();
                }
            }
            , InvSkuGoods.class
            , AppContext.getAppContext()) {
    };

    NetCallBack.QueryRsCallBack queryRespCallback2 = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<InvSkuGoods>(new PageInfo(1, 20)) {
        @Override
        public void processQueryResult(RspQueryResult<InvSkuGoods> rs) {
            //此处在主线程中执行。
            int retSize = rs.getReturnNum();
            if (retSize > 0) {
                curGoods = rs.getRowEntity(0);
            } else {
                curGoods = null;
            }

            hideProgressDialog();
            refresh();
        }

        @Override
        protected void processFailure(Throwable t, String errMsg) {
            super.processFailure(t, errMsg);
            ZLogger.d("查询商品失败：" + errMsg);

            showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);
            refresh();
        }
    }, InvSkuGoods.class, AppContext.getAppContext());


    @OnClick(R.id.button_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        if (curGoods == null) {
            btnSubmit.setEnabled(true);
            return;
        }

        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }

        if (StringUtils.isEmpty(labelCostPrice.getEtContent())) {
            DialogUtil.showHint("销售价不能为空");
            btnSubmit.setEnabled(true);
            return;
        }

        if (StringUtils.isEmpty(labelLowerLimit.getEtContent())) {
            DialogUtil.showHint("安全库存不能为空");
            btnSubmit.setEnabled(true);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在提交信息...", false);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", curGoods.getId());
        jsonObject.put("costPrice", labelCostPrice.getEtContent());
//        jsonObject.put("upperLimit", curGoods.getUpperLimit());
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
                    curGoods = null;

//                    hideProgressDialog();
                    showProgressDialog(ProgressDialog.STATUS_ERROR, "修改成功", true);
                    refresh();
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


    private void init() {
        curGoods = null;
        refresh();
    }

    /**
     * 刷新信息
     */
    private void refresh() {
        if (curGoods == null) {
            labelViews.get(LABELVIEW_INDEX_NAME).setTvSubTitle("");
            labelCostPrice.setEtContent("");
            labelViews.get(LABELVIEW_INDEX_QUANTITY).setTvSubTitle("");
            labelViews.get(LABELVIEW_INDEX_UPPERLIMIT).setTvSubTitle("");
            labelLowerLimit.setEtContent("");
            labelViews.get(LABELVIEW_INDEX_RACK_NO).setTvSubTitle("");

            btnSubmit.setEnabled(false);

            eqvBarcode.clear();
            eqvBarcode.requestFocus();

//            DeviceUtils.hideSoftInput(getActivity(), etQuery);
        } else {
            labelViews.get(LABELVIEW_INDEX_NAME).setTvSubTitle(curGoods.getName());
            labelCostPrice.setEtContent(MUtils.formatDouble(curGoods.getCostPrice(), ""));
            labelViews.get(LABELVIEW_INDEX_QUANTITY).setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), "无"));
            labelViews.get(LABELVIEW_INDEX_UPPERLIMIT).setTvSubTitle(MUtils.formatDouble(curGoods.getUpperLimit(), "无"));
            labelLowerLimit.setEtContent(MUtils.formatDouble(curGoods.getLowerLimit(), ""));
            labelViews.get(LABELVIEW_INDEX_RACK_NO).setTvSubTitle(String.format("%d", curGoods.getRackNo()));

            btnSubmit.setEnabled(true);

            labelCostPrice.requestFocus();
        }

        DeviceUtils.hideSoftInput(getActivity(), labelCostPrice);
    }

}
