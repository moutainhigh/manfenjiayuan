package com.manfenjiayuan.pda_wholesaler.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.widget.EditLabelView;
import com.bingshanguxue.pda.widget.EditQueryView;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.manfenjiayuan.business.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IInvSkuGoodsView;
import com.manfenjiayuan.pda_wholesaler.AppContext;
import com.manfenjiayuan.pda_wholesaler.R;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.invSkuStore.InvSkuStoreApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ChainGoodsFragment extends PDAScanFragment implements IInvSkuGoodsView {

    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;
    private final static int LABELVIEW_INDEX_BARCODE = 0;
    private final static int LABELVIEW_INDEX_NAME = 1;
    private final static int LABELVIEW_INDEX_QUANTITY = 2;
    private final static int LABELVIEW_INDEX_RACK_NO = 3;

    @Bind({R.id.label_barcode, R.id.label_productName, R.id.label_quantity,
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
    private InvSkuGoodsPresenter mInvSkuGoodsPresenter = null;

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
//        eqvBarcode.setInputString(code);
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
                submit();
            }

            @Override
            public void onScan() {

                refresh(null);
//                eqvBarcode.clear();
//                eqvBarcode.requestFocus();
            }
        });
//        labelCostPrice.setSoftKeyboardEnabled(false);
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
                query(text);
            }
        });
        btnSubmit.setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    /**
     * 查询商品信息
     */
    public void query(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            eqvBarcode.requestFocus();
            return;
        }

        eqvBarcode.clear();

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }
        mInvSkuGoodsPresenter.getByBarcodeMust(barcode);
    }

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

        if (StringUtils.isEmpty(labelCostPrice.getInput())) {
            DialogUtil.showHint("销售价不能为空");
            btnSubmit.setEnabled(true);
            return;
        }

        if (StringUtils.isEmpty(labelLowerLimit.getInput())) {
            DialogUtil.showHint("安全库存不能为空");
            btnSubmit.setEnabled(true);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在提交信息...", false);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", curGoods.getId());
        jsonObject.put("costPrice", labelCostPrice.getInput());
//        jsonObject.put("upperLimit", curGoods.getUpperLimit());
        jsonObject.put("lowerLimit", labelLowerLimit.getInput());
        jsonObject.put("tenantId", MfhLoginService.get().getSpid());

//        animProgress.setVisibility(View.VISIBLE);

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

//                    hideProgressDialog();
                    showProgressDialog(ProgressDialog.STATUS_ERROR, "修改成功", true);
//                    refresh(null);
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
            labelViews.get(LABELVIEW_INDEX_BARCODE).setTvSubTitle("");
            labelViews.get(LABELVIEW_INDEX_NAME).setTvSubTitle("");
            labelCostPrice.setInput("");
            labelViews.get(LABELVIEW_INDEX_QUANTITY).setTvSubTitle("");
            labelLowerLimit.setInput("");
            labelViews.get(LABELVIEW_INDEX_RACK_NO).setTvSubTitle("");

            btnSubmit.setEnabled(false);

            eqvBarcode.clear();
            eqvBarcode.requestFocus();

//            DeviceUtils.hideSoftInput(getActivity(), etQuery);
        } else {
            labelViews.get(LABELVIEW_INDEX_BARCODE).setTvSubTitle(curGoods.getBarcode());
            labelViews.get(LABELVIEW_INDEX_NAME).setTvSubTitle(curGoods.getName());
            labelCostPrice.setInput(MUtils.formatDouble(curGoods.getCostPrice(), ""));
            labelViews.get(LABELVIEW_INDEX_QUANTITY).setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), "无"));
            labelLowerLimit.setInput(MUtils.formatDouble(curGoods.getLowerLimit(), ""));
            labelViews.get(LABELVIEW_INDEX_RACK_NO).setTvSubTitle(curGoods.getRackNo());

            btnSubmit.setEnabled(true);

            labelCostPrice.requestFocusEnd();
        }

        DeviceUtils.hideSoftInput(getActivity(), labelCostPrice);
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
    public void onIInvSkuGoodsViewSuccess(InvSkuGoods data) {
        hideProgressDialog();

        refresh(data);
    }
}
