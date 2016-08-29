package com.manfenjiayuan.pda_supermarket.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.bean.InvLossOrder;
import com.manfenjiayuan.business.bean.wrapper.CreateOrderItemWrapper;
import com.manfenjiayuan.business.dialog.InputNumberDialog;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.scanner.PDAScanFragment;
import com.manfenjiayuan.pda_supermarket.ui.adapter.InvLossOrderGoodsAdapter;
import com.manfenjiayuan.pda_supermarket.ui.dialog.SelectInvCompanyInfoDialog;
import com.manfenjiayuan.pda_supermarket.widget.compound.EditQueryView;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.constant.StoreType;
import com.mfh.framework.api.impl.InvOrderApiImpl;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.api.scGoodsSku.ScGoodsSkuApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.compound.NaviAddressView;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 新建报损单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CreateInvLossOrderFragment extends PDAScanFragment {

    @Bind(R.id.providerView)
    NaviAddressView mProviderView;
    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;
    @Bind(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private InvLossOrderGoodsAdapter officeAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Bind(R.id.empty_view)
    View emptyView;
    @Bind(R.id.button_submit)
    View btnSubmit;

    private SelectInvCompanyInfoDialog selectPlatformProviderDialog = null;
    private InputNumberDialog mInputNumberDialog = null;

    private InvLossOrder invLossOrder = null;
    private boolean isQueryProcessing;

    public static CreateInvLossOrderFragment newInstance(Bundle args) {
        CreateInvLossOrderFragment fragment = new CreateInvLossOrderFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected boolean isResponseBackPressed() {
        return true;
    }


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_create_inv_lossorder;
    }

    @Override
    protected void onScanCode(String code) {
        eqvBarcode.setInputString(code);
        eqvBarcode.requestFocus();
        load(code);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initRecyclerView();

//        Bundle args = getArguments();
//        if (args != null) {
////            invSendOrder = (InvSendOrder)args.getSerializable("sendOrder");
//        }


        eqvBarcode.config(EditQueryView.INPUT_TYPE_TEXT);
        eqvBarcode.setSoftKeyboardEnabled(true);
        eqvBarcode.setInputSubmitEnabled(true);
        eqvBarcode.setHoldFocusEnable(false);
        eqvBarcode.setOnViewListener(new EditQueryView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                load(text);
            }
        });

        loadLossOrder();
    }

    @Override
    public void onResume() {
        super.onResume();

//        if (companyInfo == null){
//            selectInvCompProvider();
//        }
//        else{
//            eqvBarcode.requestFocus();
//            eqvBarcode.clear();
//        }

    }

    @Override
    public boolean onBackPressed() {
//        DialogUtil.showHint("onBackPressed");
        if (officeAdapter.getItemCount() > 0) {
            showConfirmDialog("退出后商品列表将会清空，确定要退出吗？",
                    "退出", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            getActivity().setResult(Activity.RESULT_CANCELED);
                            getActivity().finish();
                        }
                    }, "点错了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }
        else{
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }

        return isResponseBackPressed();
    }


    private void loadLossOrder(){
        invLossOrder = null;

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            getActivity().finish();
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在请求报损单号...", false);

        InvOrderApiImpl.invLossOrderGetCurrentOrder(MfhLoginService.get().getCurOfficeId(),
                StoreType.SUPERMARKET, queryOrderRC);
    }

    private NetCallBack.NetTaskCallBack queryOrderRC = new NetCallBack.NetTaskCallBack<InvLossOrder,
            NetProcessor.Processor<InvLossOrder>>(
            new NetProcessor.Processor<InvLossOrder>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //{"code":"0","msg":"查询成功!","version":"1","data":null}

                    hideProgressDialog();

                    if (rspData != null){
                        RspBean<InvLossOrder> retValue = (RspBean<InvLossOrder>) rspData;
                        invLossOrder = retValue.getValue();
                    }

                    if (invLossOrder == null){
                        DialogUtil.showHint("获取报损单号失败");
                        getActivity().finish();
                    }
                    else{
                        mProviderView.setText(invLossOrder.getOrderName());
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);

                    hideProgressDialog();
                    DialogUtil.showHint("获取报损单号失败");
                    getActivity().finish();
                }
            }
            , InvLossOrder.class
            , MfhApplication.getAppContext()) {
    };

    /**
     * 签收
     */
    @OnClick(R.id.button_submit)
    public void createInvLossOrder() {
        btnSubmit.setEnabled(false);
        showProgressDialog(ProgressDialog.STATUS_PROCESSING);

        List<CreateOrderItemWrapper> goodsList = officeAdapter.getEntityList();
        if (goodsList == null || goodsList.size() < 1) {
            btnSubmit.setEnabled(true);
            DialogUtil.showHint("商品不能为空");
            hideProgressDialog();
            return;
        }

        if (invLossOrder == null){
//            DialogUtil.showHint("请点击屏幕右上角的'+'号新建盘点");
            DialogUtil.showHint("报损单号不能为空，请退出重试");

            btnSubmit.setEnabled(true);
            return;
        }

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
//            animProgress.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            hideProgressDialog();
            return;
        }

        JSONArray items = new JSONArray();
        for (CreateOrderItemWrapper goods : goodsList){
            JSONObject item = new JSONObject();
            item.put("proSkuId", goods.getProSkuId());
            item.put("barcode", goods.getBarcode());
            item.put("quantityCheck", goods.getQuantityCheck());
            item.put("updateHint", 1);
            items.add(item);
        }

        InvOrderApiImpl.invLossOrderItemBatchCommit(invLossOrder.getId(),
                items.toJSONString(), submitCallback);
    }

    private NetCallBack.NetTaskCallBack submitCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("新建退货单失败: " + errMsg);
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
                    ZLogger.d("新建退货单成功: ");
                    hideProgressDialog();
                    getActivity().finish();

//                    DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_PRODUCTS);
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };


    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        addressRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        addressRecyclerView.setHasFixedSize(true);
        //添加分割线
        addressRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        addressRecyclerView.setEmptyView(emptyView);

        officeAdapter = new InvLossOrderGoodsAdapter(getActivity(), null);
        officeAdapter.setOnAdapterListener(new InvLossOrderGoodsAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
//                CreateOrderItemWrapper entity = officeAdapter.getEntity(position);
//                inspect(entity.getBarcode());
//                changeQuantityCheck();
            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
                eqvBarcode.requestFocus();
                eqvBarcode.clear();
            }
        });

        addressRecyclerView.setAdapter(officeAdapter);
    }

    private void load(String barcode) {
        eqvBarcode.clear();
        if (isQueryProcessing || StringUtils.isEmpty(barcode)) {
            return;
        }

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        isQueryProcessing = true;
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在查询商品...", false);
        ScGoodsSkuApiImpl.getGoodsByBarCode(barcode, queryResCallback);
    }

    private NetCallBack.NetTaskCallBack queryResCallback = new NetCallBack.NetTaskCallBack<ScGoodsSku,
            NetProcessor.Processor<ScGoodsSku>>(
            new NetProcessor.Processor<ScGoodsSku>() {
                @Override
                public void processResult(IResponseData rspData) {
                    //{"code":"0","msg":"操作成功!","version":"1","data":""}
                    // {"code":"0","msg":"查询成功!","version":"1","data":null}

                    hideProgressDialog();
                    isQueryProcessing = false;

                    if (rspData == null){
                        DialogUtil.showHint("未找到商品");
                    }
                    else{
                        RspBean<ScGoodsSku> retValue = (RspBean<ScGoodsSku>) rspData;
                        changeQuantityCheck(retValue.getValue());
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
//                    ZLogger.d("查询失败: " + errMsg);
                    DialogUtil.showHint("未找到商品");

                    hideProgressDialog();
                    isQueryProcessing = false;
                }
            }
            , ScGoodsSku.class
            , MfhApplication.getAppContext()) {
    };

    public void changeQuantityCheck(final ScGoodsSku chainGoodsSku) {
        if (chainGoodsSku == null){
            DialogUtil.showHint("商品信息不完整");
            return;
        }
        if (mInputNumberDialog == null) {
            mInputNumberDialog = new InputNumberDialog(getActivity());
            mInputNumberDialog.setCancelable(true);
            mInputNumberDialog.setCanceledOnTouchOutside(false);
        }
        mInputNumberDialog.init("报损数量", new InputNumberDialog.OnDialogListener() {
            @Override
            public void onConfirm(String numberInputStr) {
                Double quantityCheck = Double.valueOf(numberInputStr);
                officeAdapter.appendStockTakeGoods(chainGoodsSku, quantityCheck);
            }
        });
        if (!mInputNumberDialog.isShowing()) {
            mInputNumberDialog.show();
        }
    }


    /**
     * 选择批发商
     */
    @OnClick(R.id.providerView)
    public void selectInvCompProvider() {
        if (invLossOrder == null){
            loadLossOrder();
        }
    }


}
