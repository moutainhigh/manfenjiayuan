package com.manfenjiayuan.pda_supermarket.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.PDAScanFragment;
import com.bingshanguxue.pda.bizz.invrecv.InvRecvGoodsAdapter;
import com.bingshanguxue.pda.bizz.invrecv.InvRecvInspectFragment;
import com.bingshanguxue.pda.bizz.InvSendOrderListFragment;
import com.bingshanguxue.pda.database.entity.InvRecvGoodsEntity;
import com.bingshanguxue.pda.database.service.InvRecvGoodsService;
import com.manfenjiayuan.business.bean.InvSendIoOrderItemBrief;
import com.mfh.framework.api.invSendOrder.InvSendOrder;
import com.manfenjiayuan.business.dialog.AccountQuickPayDialog;
import com.manfenjiayuan.business.presenter.InvSendOrderPresenter;
import com.manfenjiayuan.business.view.IInvSendOrderView;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.Constants;
import com.manfenjiayuan.pda_supermarket.DataSyncManager;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.presenter.InvSendIoOrderPresenter;
import com.manfenjiayuan.pda_supermarket.ui.IInvSendIoOrderView;
import com.manfenjiayuan.pda_supermarket.ui.activity.SecondaryActivity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.InvOrderApi;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.constant.StoreType;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApiImpl;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderItem;
import com.mfh.framework.api.invSendOrder.InvSendOrderItem;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.compound.NaviAddressView;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 新建采购收货单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CreateInvReceiveOrderFragment extends PDAScanFragment
        implements IInvSendOrderView, IInvSendIoOrderView {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.rl_scan_sendioorder)
    RelativeLayout rlScanSendIoOrder;

    @Bind(R.id.providerView)
    NaviAddressView mProviderView;
    @Bind(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private InvRecvGoodsAdapter goodsAdapter;
    private ItemTouchHelper itemTouchHelper;

    @Bind(R.id.empty_view)
    View emptyView;

    /*供应商*/
    private CompanyInfo companyInfo = null;//当前私有供应商

    private InvSendOrderPresenter invSendOrderPresenter;
    private InvSendIoOrderPresenter invSendIoOrderPresenter;

    private AccountQuickPayDialog payDialog = null;
    protected Double totalAmount = 0D;

    public static CreateInvReceiveOrderFragment newInstance(Bundle args) {
        CreateInvReceiveOrderFragment fragment = new CreateInvReceiveOrderFragment();

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
        return R.layout.fragment_create_inv_receiveorder;
    }

    @Override
    protected void onScanCode(String code) {
        if (!isAcceptBarcodeEnabled) {
            return;
        }
        isAcceptBarcodeEnabled = false;
//        inspect(code);
        importSendIoOrder(code);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //清空签收数据库
        InvRecvGoodsService.get().clear();

        invSendOrderPresenter = new InvSendOrderPresenter(this);
        invSendIoOrderPresenter = new InvSendIoOrderPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
//            invSendOrder = (InvSendOrder) args.getSerializable("sendOrder");
//        }

        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        // Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_submit) {
                    submit();
                } else if (id == R.id.action_sendioorder) {
                    fetchSendIoOrder();
                } else if (id == R.id.action_sendorder) {
                    fetchSendOrder();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_inv_recv);

        initRecyclerView();

        if (companyInfo == null) {
            selectInvCompProvider();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        isAcceptBarcodeEnabled = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_DISTRIBUTION_INSPECT: {
                isAcceptBarcodeEnabled = true;
                goodsAdapter.setEntityList(InvRecvGoodsService.get().queryAll());
            }
            break;
            case Constants.ARC_SENDORDER_LIST: {
                // TODO: 8/2/16  
                if (resultCode == Activity.RESULT_OK) {
                    importInvSendOrder((InvSendOrder) data.getSerializableExtra("sendOrder"));
                }
            }
            break;
            case Constants.ARC_INVCOMPANY_LIST: {
                if (resultCode == Activity.RESULT_OK) {
                    CompanyInfo companyInfo = (CompanyInfo) data.getSerializableExtra("companyInfo");
                    if (companyInfo != null){
                        changeSendCompany(companyInfo);
                    }
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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


    /**
     * 切换发货方
     */
    private void changeSendCompany(CompanyInfo companyInfo) {
        this.companyInfo = companyInfo;
//        this.mLabelProvider.setLabelText(companyInfo != null ? companyInfo.getName() : "");
        this.mProviderView.setText(companyInfo != null ? companyInfo.getName() : "");

//        goodsAdapter.setEntityList(null);//清空商品
//        InvRecvGoodsService.get().clear();
    }


    /**
     * 导入发货单
     */
    public void fetchSendIoOrder() {
        // TODO: 8/2/16 扫描发货单条码， 显示一个扫描对话框
        DialogUtil.showHint("扫描发货单条码");
        setScanEnabled(true);
    }

    public void setScanEnabled(boolean enabled) {
        isAcceptBarcodeEnabled = enabled;
        if (enabled) {
            rlScanSendIoOrder.setVisibility(View.VISIBLE);
        } else {
            rlScanSendIoOrder.setVisibility(View.GONE);
        }
    }

    /**
     * 扫描到发货单条码后，加载订单明细
     */
    private void importSendIoOrder(String barcode) {
        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            isAcceptBarcodeEnabled = true;
            return;
        }

        invSendIoOrderPresenter.loadOrderItemsByBarcode(barcode);
    }

    @OnClick(R.id.rl_scan_sendioorder)
    public void hideScanSendIoOrder() {
        setScanEnabled(false);
    }

    /**
     * 导入采购单
     */
    public void fetchSendOrder() {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FRAGMENT_TYPE_INV_SENDORDER);
        extras.putString(InvSendOrderListFragment.EXTRA_KEY_STATUS,
                String.format("%d,%d",
                        InvOrderApi.ORDER_STATUS_CONFIRM,
                        InvOrderApi.ORDER_STATUS_SENDED));
        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_SENDORDER_LIST);
    }


    private void importInvSendOrder(InvSendOrder invSendOrder) {
        if (invSendOrder == null) {
            return;
        }

        //保存批发商信息
        companyInfo = new CompanyInfo();
        companyInfo.setTenantId(invSendOrder.getSendTenantId());
        companyInfo.setName(invSendOrder.getSendCompanyName());
        this.mProviderView.setText(companyInfo != null ? companyInfo.getName() : "");

        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        //加载订单明细
        invSendOrderPresenter.loadOrderItems(invSendOrder.getId());
    }

    /**
     * 签收
     */
    public void submit() {
        if (companyInfo == null) {
            DialogUtil.showHint("请选择发货方！");
            hideProgressDialog();
            selectInvCompProvider();
            return;
        }

        List<InvRecvGoodsEntity> goodsList = goodsAdapter.getEntityList();
        if (goodsList == null || goodsList.size() < 1) {
            onReceiveOrderInterrupted("商品不能为空");
            return;
        }

        final JSONArray itemsArray = new JSONArray();
        Double amount = 0D;
        for (InvRecvGoodsEntity goods : goodsList) {
            if (goods.getReceivePrice() == null) {
                ZLogger.d("未设置价格不允许收货");
                continue;
            }
            JSONObject item = new JSONObject();
            item.put("chainSkuId", goods.getChainSkuId());//查询供应链
            item.put("proSkuId", goods.getProSkuId());
            String productName = goods.getProductName();
            // TODO: 6/10/16  商品名字太长，后台不允许提交,这里增加一层过滤
            if (!StringUtils.isEmpty(productName) && productName.length() > 10) {
                item.put("productName", productName.substring(0, 10));
            } else {
                item.put("productName", productName);
            }
            item.put("quantityCheck", goods.getReceiveQuantity());
            item.put("price", goods.getReceivePrice());
            item.put("amount", goods.getReceiveAmount());
            item.put("barcode", goods.getBarcode());
            item.put("providerId", goods.getProviderId());
            item.put("isPrivate", goods.getIsPrivate());//（0：不是 1：是）

            itemsArray.add(item);
            amount += goods.getReceiveAmount();
        }

        final Double finalAmount = amount;
        showConfirmDialog(String.format("总金额：%.2f \n请确认已经查验过所有商品。", amount),
                "签收", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        //由于商品明细可以修改，所以这里不直接对订单做收货，而是新建一个收货单
//                        doSignWork(itemsArray, finalAmount, invSendOrder.getId(),
//                                invSendOrder.getSendTenantId(), invSendOrder.getIsPrivate());
                        doSignWork(itemsArray, finalAmount, null,
                                companyInfo.getTenantId(), IsPrivate.PLATFORM);
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }


    public void doSignWork(JSONArray itemsArray, Double amount, Long otherOrderId,
                           Long sendTenantId, Integer isPrivate) {
        onReceiveOrderProcess();

        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            onReceiveOrderInterrupted(getString(R.string.toast_network_error));
            return;
        }
        final JSONObject jsonStrObject = new JSONObject();
        if (sendTenantId != null) {
            jsonStrObject.put("sendTenantId", sendTenantId);
        }
        jsonStrObject.put("sendStoreType", StoreType.WHOLESALER);
        jsonStrObject.put("isPrivate", isPrivate);
        jsonStrObject.put("receiveNetId", MfhLoginService.get().getCurOfficeId());
        jsonStrObject.put("tenantId", MfhLoginService.get().getSpid());
        jsonStrObject.put("remark", "");
        jsonStrObject.put("items", itemsArray);
        totalAmount = amount;

//        ZLogger.d("jsonStr:\n " + JSON.toJSONString(jsonStrObject));
        InvSendIoOrderApiImpl.createInvSendIoRecOrder(otherOrderId, true,
                jsonStrObject.toJSONString(), signResponseCallback);
    }

    private NetCallBack.NetTaskCallBack signResponseCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //parser:{"code":"1","msg":"收货时发送方租户不能为空!","data":null,"version":1}
                    //查询失败
//                        animProgress.setVisibility(View.GONE);
                    onReceiveOrderInterrupted("新建收货单失败" + errMsg);
                }

                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"新增成功!","version":"1","data":""}
                    /**
                     * 新增采购单成功，更新采购单列表
                     * */
                    RspValue<String> retValue = (RspValue<String>) rspData;
                    onReceiveOrderSucceed(retValue.getValue());
                }
            }
            , String.class
            , AppContext.getAppContext()) {
    };

    public void onReceiveOrderProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "正在处理订单，请稍后...", false);
    }


    /**
     * 支付中断，取消&失败
     */
    public void onReceiveOrderInterrupted(String message) {
        DialogUtil.showHint(message);
        hideProgressDialog();
    }


    /**
     * 支付成功
     */
    public void onReceiveOrderSucceed(String orderId) {
        hideProgressDialog();
        ZLogger.d("新建收货单成功: " + orderId);

        DataSyncManager.getInstance().notifyUpdateSku();

//        支付收货订单
        doPayWork(orderId, totalAmount);
    }

    /**
     * 支付订单
     */
    public void doPayWork(String orderId, Double amount) {
        if (amount <= 0) {
            onOrderPaySucceed();
            return;
        }

        if (StringUtils.isEmpty(orderId)) {
            ZLogger.d("订单无效");
            onOrderPayInterrupted();
            return;
        }

        //支付
        if (payDialog == null) {
            payDialog = new AccountQuickPayDialog(getActivity());
            payDialog.setCancelable(false);
            payDialog.setCanceledOnTouchOutside(false);
        }
        payDialog.init(orderId, amount, new AccountQuickPayDialog.DialogClickListener() {
            @Override
            public void onPaySucceed() {
                //支付成功
                onOrderPaySucceed();
            }

            @Override
            public void onPayFailed() {

            }

            @Override
            public void onPayCanceled() {
                onOrderPayInterrupted();
            }
        });
        payDialog.show();
    }


    /**
     * 支付成功
     */
    public void onOrderPaySucceed() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();

    }

    /**
     * 支付中断，取消&失败
     */
    public void onOrderPayInterrupted() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    /**
     * 验货
     */
    @OnClick(R.id.fab_add)
    public void inspect() {
        inspect("");
    }

    private void inspect(String barcode) {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FRAGMENT_TYPE_DISTRIBUTION_INSPECT);
        extras.putString(InvRecvInspectFragment.EXTRA_KEY_BARCODE, barcode);

        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_DISTRIBUTION_INSPECT);
    }

    private CommonDialog operateDialog = null;

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
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

        goodsAdapter = new InvRecvGoodsAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new InvRecvGoodsAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                InvRecvGoodsEntity entity = goodsAdapter.getEntityList().get(position);
                inspect(entity.getBarcode());
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                final InvRecvGoodsEntity entity = goodsAdapter.getEntity(position);
                if (operateDialog == null) {
                    operateDialog = new CommonDialog(getActivity());
                    operateDialog.setCancelable(true);
                }
                operateDialog.setMessage(String.format("%s\n%s", entity.getBarcode(), entity.getProductName()));
                operateDialog.setPositiveButton("拒收", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        InvRecvGoodsService.get().reject(entity);

                        goodsAdapter.notifyItemChanged(position);
                    }
                });
                operateDialog.setNegativeButton("删除", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        goodsAdapter.removeEntity(position);
                    }
                });
                if (!operateDialog.isShowing()) {
                    operateDialog.show();
                }

            }

            @Override
            public void onDataSetChanged() {
//                isLoadingMore = false;
            }
        });

        addressRecyclerView.setAdapter(goodsAdapter);

        ItemTouchHelper.Callback callback = new MyItemTouchHelper(goodsAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(addressRecyclerView);
    }

    /**
     * 选择批发商
     */
    @OnClick(R.id.providerView)
    public void selectInvCompProvider() {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_INV_COMPANYLIST);
        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_INVCOMPANY_LIST);
    }


    @Override
    public void onIInvSendOrderViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "加载订单...", false);
    }

    @Override
    public void onIInvSendOrderViewError(String errorMsg) {
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);

    }

    @Override
    public void onIInvSendOrderViewSuccess(PageInfo pageInfo, List<InvSendOrder> dataList) {
        hideProgressDialog();
    }

    @Override
    public void onIInvSendOrderViewItemsSuccess(List<InvSendOrderItem> items) {
        new SendOrderAsyncTask().execute(items);
    }

    @Override
    public void onIInvSendIoOrderViewProcess() {
        isAcceptBarcodeEnabled = false;
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "加载订单...", false);
    }

    @Override
    public void onIInvSendIoOrderViewError(String errorMsg) {
        isAcceptBarcodeEnabled = true;
        showProgressDialog(ProgressDialog.STATUS_ERROR, errorMsg, true);
    }

    @Override
    public void onIInvSendIoOrderViewSuccess(InvSendIoOrderItemBrief data) {
        if (data != null) {
            List<InvSendIoOrderItem> sendIoOrderItems = data.getItems();
            new SendIoOrderAsyncTask().execute(sendIoOrderItems);
        }

        isAcceptBarcodeEnabled = true;
    }


    private class SendOrderAsyncTask extends AsyncTask<List<InvSendOrderItem>, Integer,
            List<InvRecvGoodsEntity>> {

        @Override
        protected List<InvRecvGoodsEntity> doInBackground(List<InvSendOrderItem>... params) {
            InvRecvGoodsService.get().saveSendOrderItems(params[0]);

            return InvRecvGoodsService.get().queryAll();
        }

        @Override
        protected void onPostExecute(List<InvRecvGoodsEntity> distributionSignEntities) {
            super.onPostExecute(distributionSignEntities);
            goodsAdapter.setEntityList(distributionSignEntities);

            hideProgressDialog();
        }
    }

    private class SendIoOrderAsyncTask extends AsyncTask<List<InvSendIoOrderItem>, Integer,
            List<InvRecvGoodsEntity>> {

        @Override
        protected List<InvRecvGoodsEntity> doInBackground(List<InvSendIoOrderItem>... params) {
            InvRecvGoodsService.get().saveSendIoOrderItems(params[0]);

            return InvRecvGoodsService.get().queryAll();
        }

        @Override
        protected void onPostExecute(List<InvRecvGoodsEntity> distributionSignEntities) {
            super.onPostExecute(distributionSignEntities);
            goodsAdapter.setEntityList(distributionSignEntities);
            showProgressDialog(ProgressDialog.STATUS_DONE, "加载发货单明细成功", true);
            setScanEnabled(false);
        }
    }

}
