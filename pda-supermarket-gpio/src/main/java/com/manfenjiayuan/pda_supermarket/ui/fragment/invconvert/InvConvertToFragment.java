package com.manfenjiayuan.pda_supermarket.ui.fragment.invconvert;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.bean.InvSkuGoods;
import com.manfenjiayuan.business.presenter.InvSkuGoodsPresenter;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.view.IInvSkuGoodsView;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.bean.wrapper.ChangeSkuStoreItem;
import com.manfenjiayuan.pda_supermarket.scanner.PDAScanFragment;
import com.manfenjiayuan.pda_supermarket.widget.compound.EditLabelView;
import com.manfenjiayuan.pda_supermarket.widget.compound.EditQueryView;
import com.manfenjiayuan.pda_supermarket.widget.compound.TextLabelView;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.impl.StockApiImpl;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.dialog.ProgressDialog;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 库存转换
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvConvertToFragment extends PDAScanFragment implements IInvSkuGoodsView {
    private static final String TAG = "GoodsFragment";

    public static final String EXTRA_KEY_CONVERT_SKUGOODS = "convertSkuGoods";

    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;
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

    @Bind(R.id.button_submit)
    Button btnSubmit;

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
    protected void onScanCode(String code) {
        eqvBarcode.setInputString(code);
        query(code);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInvSkuGoodsPresenter = new InvSkuGoodsPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

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

        Bundle args = getArguments();
        if (args != null) {
            convertSkuGoods = (ChangeSkuStoreItem) args.getSerializable(EXTRA_KEY_CONVERT_SKUGOODS);
        }

        if (convertSkuGoods == null) {
            DialogUtil.showHint("转换商品无效");
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }

        eqvBarcode.requestFocus();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh(curGoods);
    }

    /**
     * 查询包裹信息
     */
    public void query(String barcode) {
        if (StringUtils.isEmpty(barcode)) {
            eqvBarcode.requestFocus();
            return;
        }

        eqvBarcode.clear();
        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            refresh(null);
            return;
        }

        mInvSkuGoodsPresenter.getByBarcodeMust(barcode);
    }

    @OnClick(R.id.button_submit)
    public void submit() {
        btnSubmit.setEnabled(false);
        if (curGoods == null) {
            DialogUtil.showHint("请扫描商品");
            btnSubmit.setEnabled(true);
            return;
        }

        if (!NetWorkUtil.isConnect(getActivity())) {
            DialogUtil.showHint(R.string.toast_network_error);
            btnSubmit.setEnabled(true);
            return;
        }

        if (StringUtils.isEmpty(labelQuantityCheck.getEtContent())) {
            DialogUtil.showHint("库存数不能为空");
            btnSubmit.setEnabled(true);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在提交信息...", false);
        JSONArray sendItemsJsonArray = new JSONArray();
        sendItemsJsonArray.add(convertSkuGoods);
        JSONObject receiveItemJson = new JSONObject();
        receiveItemJson.put("id", curGoods.getId());
        receiveItemJson.put("quantity", labelQuantityCheck.getEtContent());

        //回调
        StockApiImpl.changeSkuStore(sendItemsJsonArray.toJSONString(),
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
                    DialogUtil.showHint("转换成功");

                    hideProgressDialog();
//                    showProgressDialog(ProgressDialog.STATUS_ERROR, "转换成功", true);
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("转换失败：" + errMsg);
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
            labelQuantity.setTvSubTitle("");
            labelQuantityCheck.setEtContent("");
            tvUnit.setText("");

            btnSubmit.setEnabled(false);

            eqvBarcode.clear();
            eqvBarcode.requestFocus();

//            DeviceUtils.hideSoftInput(getActivity(), etQuery);
        } else {
            labelBarcode.setTvSubTitle(curGoods.getBarcode());
            labelProductName.setTvSubTitle(curGoods.getName());
            labelQuantity.setTvSubTitle(MUtils.formatDouble(curGoods.getQuantity(), "暂无数据"));
            labelQuantityCheck.setEtContent("");
            tvUnit.setText(curGoods.getUnit());

            btnSubmit.setEnabled(true);

            labelQuantityCheck.requestFocusEnd();
        }

        DeviceUtils.hideSoftInput(getActivity(), labelQuantityCheck);
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
