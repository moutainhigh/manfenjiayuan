package com.manfenjiayuan.pda_supermarket.ui.invreceive;

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
import com.manfenjiayuan.business.bean.InvSendIoOrderItemBrief;
import com.manfenjiayuan.business.bean.InvSendOrder;
import com.manfenjiayuan.business.presenter.InvSendOrderPresenter;
import com.manfenjiayuan.business.view.IInvSendOrderView;
import com.manfenjiayuan.pda_supermarket.Constants;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.entity.DistributionSignEntity;
import com.manfenjiayuan.pda_supermarket.database.logic.DistributionSignService;
import com.manfenjiayuan.pda_supermarket.presenter.InvSendIoOrderPresenter;
import com.manfenjiayuan.pda_supermarket.ui.IInvSendIoOrderView;
import com.manfenjiayuan.pda_supermarket.ui.activity.SecondaryActivity;
import com.manfenjiayuan.pda_supermarket.ui.adapter.DistributionSignAdapter;
import com.manfenjiayuan.pda_supermarket.ui.dialog.SelectInvCompanyInfoDialog;
import com.manfenjiayuan.pda_supermarket.utils.SharedPreferencesHelper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderItem;
import com.mfh.framework.api.invSendIoOrder.InvSendOrderItem;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.UIHelper;
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
public class CreateInvReceiveOrderFragment extends BaseReceiveOrderFragment
        implements IInvSendOrderView, IInvSendIoOrderView {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.rl_scan_sendioorder)
    RelativeLayout rlScanSendIoOrder;

    @Bind(R.id.providerView)
    NaviAddressView mProviderView;
    @Bind(R.id.office_list)
    RecyclerViewEmptySupport addressRecyclerView;
    private DistributionSignAdapter goodsAdapter;
    private ItemTouchHelper itemTouchHelper;

    @Bind(R.id.empty_view)
    View emptyView;

    private SelectInvCompanyInfoDialog selectPlatformProviderDialog = null;

    /*供应商*/
    private CompanyInfo companyInfo = null;//当前私有供应商

    private InvSendOrderPresenter invSendOrderPresenter;
    private InvSendIoOrderPresenter invSendIoOrderPresenter;

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
        DistributionSignService.get().clear();

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
    }

    @Override
    public void onResume() {
        super.onResume();

        if (companyInfo == null) {
            selectInvCompProvider();
        }

        isAcceptBarcodeEnabled = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.ARC_DISTRIBUTION_INSPECT: {
                isAcceptBarcodeEnabled = true;
                goodsAdapter.setEntityList(DistributionSignService.get().queryAll());
            }
            break;
            case Constants.ARC_SENDORDER_LIST: {
                // TODO: 8/2/16  
                if (resultCode == Activity.RESULT_OK){
                    importInvSendOrder((InvSendOrder) data.getSerializableExtra("sendOrder"));
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onBackPressed() {
//        DialogUtil.showHint("onBackPressed");
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
//        DistributionSignService.get().clear();
    }

    @Override
    public void onReceiveOrderSucceed(String orderId) {
        super.onReceiveOrderSucceed(orderId);
    }

    @Override
    public void onReceiveOrderInterrupted(String message) {
        super.onReceiveOrderInterrupted(message);
    }

    /**
     * 导入发货单
     */
    public void fetchSendIoOrder() {
        // TODO: 8/2/16 扫描发货单条码， 显示一个扫描对话框
        DialogUtil.showHint("扫描发货单条码");
        setScanEnabled(true);
    }

    public void setScanEnabled(boolean enabled){
        isAcceptBarcodeEnabled = enabled;
        if (enabled){
            rlScanSendIoOrder.setVisibility(View.VISIBLE);
        }
        else{
            rlScanSendIoOrder.setVisibility(View.GONE);
        }
    }
    /**
     * 扫描到发货单条码后，加载订单明细
     * */
    private void importSendIoOrder(String barcode){
        if (!NetWorkUtil.isConnect(MfhApplication.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            isAcceptBarcodeEnabled = true;
            return;
        }

        invSendIoOrderPresenter.loadOrderItemsByBarcode(barcode);
    }

    @OnClick(R.id.rl_scan_sendioorder)
    public void hideScanSendIoOrder(){
        setScanEnabled(false);
    }

    /**
     * 导入采购单
     */
    public void fetchSendOrder() {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FRAGMENT_TYPE_INV_SENDORDER);

        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, Constants.ARC_SENDORDER_LIST);
    }


    private void importInvSendOrder(InvSendOrder invSendOrder){
        if (invSendOrder == null){
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

        List<DistributionSignEntity> goodsList = goodsAdapter.getEntityList();
        if (goodsList == null || goodsList.size() < 1) {
            onReceiveOrderInterrupted("商品不能为空");
            return;
        }

        final JSONArray itemsArray = new JSONArray();
        Double amount = 0D;
        for (DistributionSignEntity goods : goodsList) {
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
        extras.putString(DistributionInspectFragment.EXTRA_KEY_BARCODE, barcode);

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

        goodsAdapter = new DistributionSignAdapter(getActivity(), null);
        goodsAdapter.setOnAdapterListener(new DistributionSignAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                DistributionSignEntity entity = goodsAdapter.getEntityList().get(position);
                inspect(entity.getBarcode());
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                final DistributionSignEntity entity = goodsAdapter.getEntityList().get(position);
                if (operateDialog == null) {
                    operateDialog = new CommonDialog(getActivity());
                    operateDialog.setCancelable(true);
                }
                operateDialog.setMessage(String.format("%s\n%s", entity.getBarcode(), entity.getProductName()));
                operateDialog.setPositiveButton("拒收", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        DistributionSignService.get().reject(entity);

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
        if (selectPlatformProviderDialog == null) {
            selectPlatformProviderDialog = new SelectInvCompanyInfoDialog(getActivity());
            selectPlatformProviderDialog.setCancelable(true);
            selectPlatformProviderDialog.setCanceledOnTouchOutside(false);
        }
        selectPlatformProviderDialog.init(new SelectInvCompanyInfoDialog.OnDialogListener() {
            @Override
            public void onItemSelected(CompanyInfo companyInfo) {
                changeSendCompany(companyInfo);
            }

        });
        selectPlatformProviderDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        if (!selectPlatformProviderDialog.isShowing()) {
            selectPlatformProviderDialog.show();
        }
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
        if (data != null){
            List<InvSendIoOrderItem> sendIoOrderItems = data.getItems();
            new SendIoOrderAsyncTask().execute(sendIoOrderItems);
        }

        isAcceptBarcodeEnabled = true;
    }


    private class SendOrderAsyncTask extends AsyncTask<List<InvSendOrderItem>, Integer,
            List<DistributionSignEntity>> {

        @Override
        protected List<DistributionSignEntity> doInBackground(List<InvSendOrderItem>... params) {
            List<InvSendOrderItem> items = params[0];

            if (items != null && items.size() > 0) {
                for (InvSendOrderItem entity : items) {
                    DistributionSignService.get().saveInvSendOrderItem(entity);
                }
            }

            return DistributionSignService.get().queryAll();
        }

        @Override
        protected void onPostExecute(List<DistributionSignEntity> distributionSignEntities) {
            super.onPostExecute(distributionSignEntities);
            goodsAdapter.setEntityList(distributionSignEntities);

            hideProgressDialog();
        }
    }

    private class SendIoOrderAsyncTask extends AsyncTask<List<InvSendIoOrderItem>, Integer,
            List<DistributionSignEntity>> {

        @Override
        protected List<DistributionSignEntity> doInBackground(List<InvSendIoOrderItem>... params) {
            List<InvSendIoOrderItem> items = params[0];

            if (items != null && items.size() > 0) {
                for (InvSendIoOrderItem entity : items) {
                    DistributionSignService.get().saveInvSendIoOrderItem(entity);
                }
            }


            return DistributionSignService.get().queryAll();
        }

        @Override
        protected void onPostExecute(List<DistributionSignEntity> distributionSignEntities) {
            super.onPostExecute(distributionSignEntities);
            goodsAdapter.setEntityList(distributionSignEntities);

            hideProgressDialog();
        }
    }
}
