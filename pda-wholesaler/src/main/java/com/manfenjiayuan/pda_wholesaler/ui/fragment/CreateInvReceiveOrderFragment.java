package com.manfenjiayuan.pda_wholesaler.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.pda.bizz.ARCode;
import com.bingshanguxue.pda.bizz.InvSendOrderListFragment;
import com.bingshanguxue.pda.bizz.invrecv.InvRecvGoodsAdapter;
import com.bingshanguxue.pda.bizz.invrecv.InvRecvInspectFragment;
import com.bingshanguxue.pda.bizz.invsendio.SendIoEntryMode;
import com.bingshanguxue.pda.database.entity.InvRecvGoodsEntity;
import com.bingshanguxue.pda.database.service.InvRecvGoodsService;
import com.bingshanguxue.pda.dialog.ActionDialog;
import com.manfenjiayuan.business.presenter.InvSendOrderPresenter;
import com.manfenjiayuan.business.view.IInvSendOrderView;
import com.manfenjiayuan.pda_wholesaler.AppContext;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.ui.activity.SecondaryActivity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.invOrder.InvOrderApi;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.constant.StoreType;
import com.mfh.framework.api.invCompProvider.MyProvider;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderApiImpl;
import com.mfh.framework.api.invSendOrder.InvSendOrder;
import com.mfh.framework.api.invSendOrder.InvSendOrderItem;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.rxapi.http.InvSendIoOrderHttpManager;
import com.mfh.framework.rxapi.subscriber.MValueSubscriber;
import com.mfh.framework.uikit.base.BaseFragment;
import com.bingshanguxue.vector_uikit.widget.NaviAddressView;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.MyItemTouchHelper;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * 新建采购收货单
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class CreateInvReceiveOrderFragment extends BaseFragment
        implements IInvSendOrderView {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.providerView)
    NaviAddressView mProviderView;
    @Bind(R.id.office_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private InvRecvGoodsAdapter goodsAdapter;
    private ItemTouchHelper itemTouchHelper;
    @Bind(R.id.empty_view)
    View emptyView;

    /*供应商*/
    private MyProvider companyInfo = null;//当前私有供应商

    private InvSendOrderPresenter invSendOrderPresenter;

    private int entryMode = SendIoEntryMode.MANUAL;
    private ActionDialog mActionDialog = null;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //清空签收数据库
        InvRecvGoodsService.get().clear();

        invSendOrderPresenter = new InvSendOrderPresenter(this);
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
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_inv_recv);

        mProviderView.setEnabled(false);
        initRecyclerView();

        selectEntryMode();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ARCode.ARC_DISTRIBUTION_INSPECT: {
                goodsAdapter.setEntityList(InvRecvGoodsService.get().queryAll());
            }
            break;
            case ARCode.ARC_SENDORDER_LIST: {
                // TODO: 8/2/16  
                if (resultCode == Activity.RESULT_OK) {
                    importInvSendOrder((InvSendOrder) data.getSerializableExtra("sendOrder"));
                }else {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
                }
            }
            break;
            case ARCode.ARC_INV_COMPROVIDER_LIST: {
                if (resultCode == Activity.RESULT_OK) {
                    MyProvider myProvider = (MyProvider) data.getSerializableExtra("myProvider");
                    if (myProvider != null){
                        changeSendCompany(myProvider);
                    }
                }

                if (companyInfo == null) {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                    getActivity().finish();
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
    private void changeSendCompany(MyProvider companyInfo) {
        this.companyInfo = companyInfo;
//        this.mLabelProvider.setLabelText(companyInfo != null ? companyInfo.getName() : "");
        this.mProviderView.setText(companyInfo != null ? companyInfo.getName() : "");

    }

    /**
     * 导入采购单
     */
    public void fetchSendOrder() {
        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FRAGMENT_TYPE_INV_SENDORDER);
        extras.putString(InvSendOrderListFragment.EXTRA_KEY_STATUS,
                String.format("%d,%d,%d", InvOrderApi.ORDER_STATUS_INIT,
                        InvOrderApi.ORDER_STATUS_CONFIRM, InvOrderApi.ORDER_STATUS_SENDED));
        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_SENDORDER_LIST);
    }


    private void importInvSendOrder(InvSendOrder invSendOrder) {
        if (invSendOrder == null) {
            return;
        }

        //保存批发商信息
        companyInfo = new MyProvider();
        companyInfo.setId(invSendOrder.getSendTenantId());
        companyInfo.setName(invSendOrder.getSendCompanyName());
        this.mProviderView.setText(companyInfo != null ? companyInfo.getName() : "");

        if (!NetworkUtils.isConnect(MfhApplication.getAppContext())) {
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

//        final Double finalAmount = amount;
        showConfirmDialog(String.format("总金额：%.2f, \n请确认已经查验过所有商品。", amount),
                "签收", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        //由于商品明细可以修改，所以这里不直接对订单做收货，而是新建一个收货单
//                        doSignWork(itemsArray, finalAmount, invSendOrder.getId(),
//                                invSendOrder.getSendTenantId(), invSendOrder.getIsPrivate());
                        doSignWork(itemsArray, null, companyInfo.getId());
                    }
                }, "点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    public void doSignWork(JSONArray itemsArray, Long otherOrderId, Long sendTenantId) {
        onReceiveOrderProcess();

        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            onReceiveOrderInterrupted(getString(R.string.toast_network_error));
            return;
        }
        final JSONObject jsonStrObject = new JSONObject();
        if (sendTenantId != null) {
            jsonStrObject.put("sendTenantId", sendTenantId);
        }
        jsonStrObject.put("sendStoreType", StoreType.WHOLESALER);
        jsonStrObject.put("isPrivate", IsPrivate.PLATFORM);
        jsonStrObject.put("receiveNetId", MfhLoginService.get().getCurOfficeId());
        jsonStrObject.put("tenantId", MfhLoginService.get().getSpid());
        jsonStrObject.put("remark", "");
        jsonStrObject.put("items", itemsArray);


        Map<String, String> options = new HashMap<>();
        if (otherOrderId != null) {
            options.put("otherOrderId", String.valueOf(otherOrderId));
        }
        options.put("checkOk", "true");
        options.put("jsonStr", jsonStrObject.toJSONString());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        InvSendIoOrderHttpManager.getInstance().createRecOrder(options,
                new MValueSubscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        ZLogger.ef("新建收货单失败:" + e.toString());
                        onReceiveOrderInterrupted(e.getMessage());
                    }

                    @Override
                    public void onValue(String data) {
                        super.onValue(data);
                        onReceiveOrderSucceed();
                    }

                });
    }

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
    public void onReceiveOrderSucceed() {
        showProgressDialog(ProgressDialog.STATUS_DONE, "收货成功", false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }
        }, 1000);
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
        if (companyInfo != null){
            extras.putLong(InvRecvInspectFragment.EXTRA_KEY_TENANTID, companyInfo.getId());
        }
        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_DISTRIBUTION_INSPECT);
    }

    /**
     * 选择入口
     * */
    private void selectEntryMode(){
        if (mActionDialog == null) {
            mActionDialog = new ActionDialog(getActivity());
            mActionDialog.setCancelable(false);
            mActionDialog.setCanceledOnTouchOutside(false);
        }
        mActionDialog.init("新建收货单", "可以选择以下方式新建收货单",
                new ActionDialog.DialogClickListener() {
                    @Override
                    public void onAction1Click() {
                        entryMode = SendIoEntryMode.MANUAL;

                        if (companyInfo == null) {
                            selectInvCompProvider();
                        }
                    }

                    @Override
                    public void onAction2Click() {
//                        entryMode = SendIoEntryMode.SENDIOORDER;
//                        fetchSendIoOrder();
                    }

                    @Override
                    public void onAction3Click() {
                        entryMode = SendIoEntryMode.SENDORDER;
                        fetchSendOrder();
                    }
        });
        mActionDialog.registerActions("手动输入商品", null, "导入采购单");
        if (!mActionDialog.isShowing()){
            mActionDialog.show();
        }
    }

    private CommonDialog operateDialog = null;

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(emptyView);

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

        goodsRecyclerView.setAdapter(goodsAdapter);

        ItemTouchHelper.Callback callback = new MyItemTouchHelper(goodsAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(goodsRecyclerView);
    }

    /**
     * 选择批发商
     */
    @OnClick(R.id.providerView)
    public void selectInvCompProvider() {


        Bundle extras = new Bundle();
//                extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SecondaryActivity.EXTRA_KEY_FRAGMENT_TYPE, SecondaryActivity.FT_INV_COMPROVIDER_LIST);
        Intent intent = new Intent(getActivity(), SecondaryActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_INV_COMPROVIDER_LIST);

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
    public void onIInvSendOrderViewItemsSuccess(final List<InvSendOrderItem> items) {

        Observable.create(new Observable.OnSubscribe<List<InvRecvGoodsEntity>>() {
            @Override
            public void call(Subscriber<? super List<InvRecvGoodsEntity>> subscriber) {
                InvRecvGoodsService.get().saveSendOrderItems(items);
                List<InvRecvGoodsEntity> invRecvGoodsEntities = InvRecvGoodsService.get().queryAll();
                subscriber.onNext(invRecvGoodsEntities);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<InvRecvGoodsEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<InvRecvGoodsEntity> invRecvGoodsEntities) {
                        goodsAdapter.setEntityList(invRecvGoodsEntities);
//            showProgressDialog(ProgressDialog.STATUS_DONE, "加载发货单明细成功", true);
                        hideProgressDialog();
                    }
                });
    }


}
