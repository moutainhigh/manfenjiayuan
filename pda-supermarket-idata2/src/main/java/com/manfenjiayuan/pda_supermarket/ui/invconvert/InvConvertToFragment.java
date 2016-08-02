package com.manfenjiayuan.pda_supermarket.ui.invconvert;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.widget.EditLabelView;
import com.bingshanguxue.pda.widget.EditQueryView;
import com.bingshanguxue.pda.widget.TextLabelView;
import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.manfenjiayuan.business.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IInvSkuGoodsView;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.bean.wrapper.ChangeSkuStoreItem;
import com.manfenjiayuan.pda_supermarket.ui.QueryBarcodeFragment;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.invSkuStore.InvSkuStoreApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 库存转换
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvConvertToFragment extends QueryBarcodeFragment implements IInvSkuGoodsView {

    public static final String EXTRA_KEY_CONVERT_SKUGOODS = "convertSkuGoods";

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

        super.createViewInner(rootView, container, savedInstanceState);
        labelQuantityCheck.config(EditLabelView.INPUT_TYPE_NUMBER_DECIMAL);
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
        labelQuantityCheck.setSoftKeyboardEnabled(true);

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
    public void onResume() {
        super.onResume();
        refresh(curGoods);
    }

    /**
     * 查询商品信息
     */
    @Override
    public void sendQueryReq(String barcode) {
        super.sendQueryReq(barcode);
        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            refresh(null);
            return;
        }

        mInvSkuGoodsPresenter.getByBarcodeMust(barcode);
    }

    @Override
    public void onSubmitSuccess() {
//        super.onSubmitSuccess();

        hideProgressDialog();
    }

    @Override
    public void submit() {
        super.submit();

        if (curGoods == null) {
            onSubmitError("请扫描商品");
            return;
        }

        if (!NetWorkUtil.isConnect(getActivity())) {
            onSubmitError(getString(R.string.toast_network_error));
            return;
        }

        if (StringUtils.isEmpty(labelQuantityCheck.getEtContent())) {
            onSubmitError("库存数不能为空");
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在提交信息...", false);
        JSONArray sendItemsJsonArray = new JSONArray();
        sendItemsJsonArray.add(convertSkuGoods);
        JSONObject receiveItemJson = new JSONObject();
        receiveItemJson.put("id", curGoods.getId());
        receiveItemJson.put("quantity", labelQuantityCheck.getEtContent());

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
     * 刷新信息
     */
    private void refresh(InvSkuGoods invSkuGoods) {
        refresh();
        curGoods = invSkuGoods;
        if (curGoods == null) {
            labelBarcode.setTvSubTitle("");
            labelProductName.setTvSubTitle("");
            labelQuantity.setTvSubTitle("");
            labelQuantityCheck.setEtContent("");
            tvUnit.setText("");

            btnSubmit.setEnabled(false);

//            DeviceUtils.hideSoftInput(getActivity(), etQuery);
        } else {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelProductName.setTvSubTitle(curGoods.getName());
            labelQuantity.setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), "暂无数据"));
            labelQuantityCheck.setEtContent("");
            tvUnit.setText(curGoods.getUnit());

            labelQuantityCheck.requestFocusEnd();
            btnSubmit.setEnabled(true);
        }
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
