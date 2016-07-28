package com.manfenjiayuan.pda_wholesaler.ui.invio;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.database.entity.InvIoGoodsEntity;
import com.bingshanguxue.pda.database.service.InvIoGoodsService;
import com.bingshanguxue.pda.dialog.CommitInvIoOrderDialog;
import com.bingshanguxue.pda.widget.EditQueryView;
import com.manfenjiayuan.pda_wholesaler.Constants;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.ui.activity.SecondaryActivity;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.invIoOrder.InvIoOrderApi;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 新建出库/入库订单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CreateInvIoOrderFragment extends PDAScanFragment {
    public static final String EXTRA_KEY_ORDER_TYPE = "orderType";
    public static final String EXTRA_KEY_STORE_TYPE = "storeType";

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.eqv_barcode)
    EditQueryView eqvBarcode;
    @Bind(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private InvIoOrderGoodsAdapter goodsAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Bind(R.id.empty_view)
    View emptyView;
    @Bind(R.id.button_submit)
    Button btnSubmit;

    private int orderType = InvIoOrderApi.ORDER_TYPE_IN;
    private int storeType = InvIoOrderApi.STORE_TYPE_RETAIL;

    public static CreateInvIoOrderFragment newInstance(Bundle args) {
        CreateInvIoOrderFragment fragment = new CreateInvIoOrderFragment();

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
        return R.layout.fragment_create_inv_ioorder;
    }

    @Override
    protected void onScanCode(String code) {
        eqvBarcode.requestFocus();
        eqvBarcode.clear();
        inspect(code);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //清空签收数据库
        InvIoGoodsService.get().clear();
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initRecyclerView();

        Bundle args = getArguments();
        if (args != null) {
            orderType = args.getInt(EXTRA_KEY_ORDER_TYPE);
            storeType = args.getInt(EXTRA_KEY_STORE_TYPE);
        }

        eqvBarcode.config(EditQueryView.INPUT_TYPE_TEXT);
        eqvBarcode.setSoftKeyboardEnabled(true);
        eqvBarcode.setInputSubmitEnabled(true);
        eqvBarcode.setHoldFocusEnable(false);
        eqvBarcode.setOnViewListener(new EditQueryView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                inspect(text);
            }
        });

        eqvBarcode.requestFocus();
        eqvBarcode.clear();
        if (orderType == InvIoOrderApi.ORDER_TYPE_IN) {
            btnSubmit.setText("入库");
            mToolbar.setTitle("新建入库单");
        } else {
            mToolbar.setTitle("新建出库单");
            btnSubmit.setText("出库");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        eqvBarcode.requestFocusEnd();
        eqvBarcode.clear();
    }

    @Override
    public boolean onBackPressed() {
        if (goodsAdapter.getItemCount() > 0) {
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
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }

        return isResponseBackPressed();
    }


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

        goodsAdapter = new InvIoOrderGoodsAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new InvIoOrderGoodsAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
//                CreateOrderItemWrapper entity = goodsAdapter.getEntity(position);
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

        addressRecyclerView.setAdapter(goodsAdapter);
    }


    /**
     * 签收
     */
    @OnClick(R.id.button_submit)
    public void createInvReturnOrder() {
        btnSubmit.setEnabled(false);

        showConfirmDialog("确定要提交单据吗？",
                "确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        doCreateTask();
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        btnSubmit.setEnabled(true);
                    }
                });
    }

    /**
     * 创建订单
     */
    private void doCreateTask() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);

        List<InvIoGoodsEntity> goodsList = goodsAdapter.getEntityList();
        if (goodsList == null || goodsList.size() < 1) {
            btnSubmit.setEnabled(true);
            DialogUtil.showHint("商品不能为空");
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

        JSONArray items = new JSONArray();
        for (InvIoGoodsEntity goods : goodsList) {
            JSONObject item = new JSONObject();
            item.put("proSkuId", goods.getProSkuId());
            item.put("productName", goods.getProductName());
            item.put("barcode", goods.getBarcode());
            item.put("quantityCheck", goods.getQuantityCheck());
            item.put("quantityPack", goods.getQuantityPack());
            item.put("price", goods.getPrice());
            item.put("posId", goods.getPosId());

            items.add(item);
        }

        InvIoOrderApi.createIoOrder(orderType, storeType,
                items, responseCallback);
    }

    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.df("新建出入库订单失败: " + errMsg);
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
                    ZLogger.df("新建出入库订单成功:");
                    if (orderType == InvIoOrderApi.ORDER_TYPE_OUT) {
                        RspValue<String> retValue = (RspValue<String>) rspData;

                        doCommitTask(retValue.getValue());
                    } else {
                        DialogUtil.showHint("订单创建成功");
                        hideProgressDialog();
                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
                    }
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };

    private CommitInvIoOrderDialog commitDialog = null;
    /**
     * 提交订单
     * */
    private void doCommitTask(final String orderId){
//        hideProgressDialog();
        DialogUtil.showHint("准备提交订单");

        if (commitDialog == null) {
            commitDialog = new CommitInvIoOrderDialog(getActivity());
            commitDialog.setCancelable(true);
            commitDialog.setCanceledOnTouchOutside(false);
        }
        commitDialog.init(new CommitInvIoOrderDialog.DialogListener() {
            @Override
            public void onCancel() {
                btnSubmit.setEnabled(true);
                hideProgressDialog();
            }

            @Override
            public void onNextStep(String vehicle, String phonenumber) {
                if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
                    DialogUtil.showHint(R.string.toast_network_error);
//            animProgress.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    hideProgressDialog();
                    return;
                }
                InvIoOrderApi.commitOrder(orderId, null,
                        vehicle, commitRC);
            }

        });
        if (!commitDialog.isShowing()) {
            commitDialog.show();
        }
    }


    private NetCallBack.NetTaskCallBack commitRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.df("提交出入库订单失败: " + errMsg);
//                    {"code":"1","msg":"132079网点有仓储单正在处理中...","version":"1","data":null}
                    //查询失败
//                        animProgress.setVisibility(View.GONE);
//                    DialogUtil.showHint("新建退货单失败" + errMsg);
                    DialogUtil.showHint(errMsg);
                    hideProgressDialog();
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }

                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}
//                        animProgress.setVisibility(View.GONE);
                    /**
                     * 新建退货单成功，更新采购单列表
                     * */
                    ZLogger.df("提交出入库订单成功:");
                    DialogUtil.showHint("提交订单成功");
                    hideProgressDialog();
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }
            }
            , String.class
            , MfhApplication.getAppContext()) {
    };


    /**
     * 验货
     * */
    private void inspect(String barcode) {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_INVIO_INSPECTGOODS);
        extras.putString(InvIoGoodsInspectFragment.EXTRA_KEY_BARCODE, barcode);

        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_DISTRIBUTION_INSPECT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_DISTRIBUTION_INSPECT: {
                try{
                    eqvBarcode.requestFocusEnd();
                    goodsAdapter.setEntityList(InvIoGoodsService.get().queryAll());
                }
                catch (Exception e){
                    ZLogger.ef(e.toString());
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
