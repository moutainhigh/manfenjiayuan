package com.manfenjiayuan.pda_wholesaler.ui.fragment.goods;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.bizz.goods.ScGoodsSkuEvent;
import com.bingshanguxue.vector_uikit.widget.EditLabelView;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_wholesaler.R;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.invSkuStore.InvSkuStoreApiImpl;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
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
//    @Bind(R.id.label_costPrice)
    EditLabelView labelCostPrice;
//    @Bind(R.id.label_quantity)
    TextLabelView labelQuantity;
//    @Bind(R.id.label_upperLimit)
    EditLabelView labelRackNo;

//    @Bind(R.id.fab_submit)
    public FloatingActionButton btnSubmit;

    private InvSkuGoods curGoods = null;
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
        labelCostPrice = (EditLabelView) rootView.findViewById(R.id.label_costPrice);
        labelQuantity = (TextLabelView) rootView.findViewById(R.id.label_quantity);
        labelRackNo = (EditLabelView) rootView.findViewById(R.id.label_rackno);
        btnSubmit = (FloatingActionButton) rootView.findViewById(R.id.fab_submit);

        labelCostPrice.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER},
                new EditLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                            labelRackNo.requestFocusEnd();
                        }
                    }
                });
        labelRackNo.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER},
                new EditLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                            submit();
                        }
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
                InvSkuGoods sku = (InvSkuGoods) args.getSerializable(ScGoodsSkuEvent.EXTRA_KEY_SCGOODSSKU);
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

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
            onSubmitError(getString(R.string.toast_network_error));
            return;
        }

        if (StringUtils.isEmpty(labelCostPrice.getInput())) {
            onSubmitError("销售价不能为空");
            return;
        }

        if (StringUtils.isEmpty(labelRackNo.getInput())) {
            onSubmitError("货架编号不能为空");
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
        jsonObject.put("upperLimit", labelRackNo.getInput());
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

//                    DataSyncManager.getInstance().notifyUpdateSku();
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
    private void refresh(InvSkuGoods invSkuGoods) {
        curGoods = invSkuGoods;
        if (curGoods == null) {
            labelBarcode.setTvSubTitle("");
            labelProductName.setTvSubTitle("");
            labelCostPrice.setInput("");
            labelQuantity.setTvSubTitle("");
            labelRackNo.setInput("");

            labelCostPrice.setInputEnabled(false);
            labelRackNo.setInputEnabled(false);
            btnSubmit.setEnabled(false);
            btnSubmit.setVisibility(View.GONE);

//            DeviceUtils.hideSoftInput(getActivity(), etQuery);
        } else {
            labelProductName.setTvSubTitle(curGoods.getName());
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelCostPrice.setInput(MUtils.formatDouble(curGoods.getCostPrice(), ""));
            labelQuantity.setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), "暂无数据"));
            labelRackNo.setInput(MUtils.formatDouble(curGoods.getUpperLimit(), ""));

            labelCostPrice.setInputEnabled(isEditable);
            labelRackNo.setInputEnabled(isEditable);
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
