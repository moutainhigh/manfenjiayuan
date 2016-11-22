/*
 * Copyright (c) 2016. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.mfh.litecashier.ui.fragment.components;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manfenjiayuan.business.bean.CompanyInfo;
import com.manfenjiayuan.business.bean.InvSendOrderItem;
import com.manfenjiayuan.business.bean.InvSendOrderItemBrief;
import com.manfenjiayuan.business.bean.ScGoodsSku;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.api.InvOrderApi;
import com.mfh.framework.api.constant.IsPrivate;
import com.mfh.framework.api.invOrder.StockApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.compound.OptionalLabel;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.PurchaseShopcartSyncEvent;
import com.mfh.litecashier.presenter.InventoryGoodsPresenter;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.ui.adapter.IntelligentGoodsAdapter;
import com.mfh.litecashier.ui.dialog.SelectWholesalerDialog;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseGoodsDetailFragment;
import com.mfh.litecashier.ui.view.IInventoryView;
import com.mfh.litecashier.utils.PurchaseShopcartHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * <h1>智能订货</h1>
 * <p>
 * <p/>
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class IntelligentOrderFragment extends BaseProgressFragment implements IInventoryView {

    public static final String EXTRA_KEY_CANCELABLE = "cancelable";
    public static final String EXTRA_KEY_DATETIME = "datetime";

    @Bind(R.id.tv_header_title)
    TextView tvHeaderTitle;

    @Bind(R.id.label_company)
    OptionalLabel labelCompany;
    private SelectWholesalerDialog selectPlatformProviderDialog = null;
    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private IntelligentGoodsAdapter goodsListAdapter;
    private LinearLayoutManager mRLayoutManager;
    @Bind(R.id.empty_view)
    TextView emptyView;

    @Bind(R.id.animProgress)
    ProgressBar mProgressBar;

    @Bind(R.id.button_header_close)
    ImageButton btnClose;
    @Bind(R.id.button_footer_positive)
    Button btnConfirm;

    private CompanyInfo mCompanyInfo = null;
    private boolean isLoadingMore;
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 30;
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private List<ScGoodsSku> goodsList = new ArrayList<>();

    private InventoryGoodsPresenter inventoryGoodsPresenter;


    public static IntelligentOrderFragment newInstance(Bundle args) {
        IntelligentOrderFragment fragment = new IntelligentOrderFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_components_intelligentorder;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inventoryGoodsPresenter = new InventoryGoodsPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
//        }
        tvHeaderTitle.setText("智能订货");
        initGoodsRecyclerView();

        labelCompany.setOnViewListener(new OptionalLabel.OnViewListener() {
            @Override
            public void onClickDel() {
                goodsListAdapter.setEntityList(null);//清空商品
                changeCompany(null);
            }
        });
        btnConfirm.setEnabled(false);
        if (mCompanyInfo == null){
            selectPlatformProvider();
        }


//        DialogUtil.showHint("开发君失踪了...");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @OnClick(R.id.button_header_close)
    public void finishActivity() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    /**
     * 选择平台供应商
     */
    @OnClick(R.id.label_company)
    public void selectPlatformProvider() {
        //TODO,判断商品是否存在多个供应链，若存在多个，则提示选择供应链
        if (selectPlatformProviderDialog == null) {
            selectPlatformProviderDialog = new SelectWholesalerDialog(getActivity());
            selectPlatformProviderDialog.setCancelable(false);
            selectPlatformProviderDialog.setCanceledOnTouchOutside(false);
        }
        selectPlatformProviderDialog.init(new SelectWholesalerDialog.OnDialogListener() {
            @Override
            public void onItemSelected(CompanyInfo companyInfo) {
                changeCompany(companyInfo);
            }

        });
        if (!selectPlatformProviderDialog.isShowing()) {
            selectPlatformProviderDialog.show();
        }
    }

    @OnClick(R.id.button_search)
    public void load() {
        if (mCompanyInfo == null){
            DialogUtil.showHint("请先选择批发商");
            return;
        }

        loadIntelligentOrder();

//        //初始化
//        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
//
//        inventoryGoodsPresenter.loadPurchaseGoods(mPageInfo, "", 0L, "", "", 0, "");
//        mPageInfo.setPageNo(1);
        // TODO: 4/18/16 查询订货商品
    }

    /**
     * 日结确认(支付成功后才能确认)
     */
    @OnClick(R.id.button_footer_positive)
    public void submit() {

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        onLoadProcess("正在下单...");

        JSONObject jsonObject = new JSONObject();
        JSONArray items = new JSONArray();

        //status的值不要传了，后台自动判断（因为生鲜类要求是0，其他类是1）
//        jsonObject.put("status", OrderStatus.STATUS_CONFIRM);
        jsonObject.put("bizType", InvOrderApi.BIZTYPE_PURCHASE);
        jsonObject.put("receiveNetId", MfhLoginService.get().getCurOfficeId());
        jsonObject.put("contact", MfhLoginService.get().getHumanName());
        jsonObject.put("receiveMobile", MfhLoginService.get().getTelephone());
        //TODO,这里需要填写地址，暂时使用网点名称
        jsonObject.put("receiveAddr", MfhLoginService.get().getCurOfficeName());

        // TODO: 4/18/16 加载采购商品
        List<InvSendOrderItem> goodsList = goodsListAdapter.getEntityList();
        if (goodsList != null && goodsList.size() > 0) {
            for (InvSendOrderItem goods : goodsList) {
                JSONObject item = new JSONObject();
                item.put("chainSkuId", goods.getChainSkuId());
                item.put("proSkuId", goods.getProSkuId());
                item.put("providerId", goods.getProviderId());
                item.put("isPrivate", goods.getIsPrivate());
                item.put("productName", goods.getProductName());
                item.put("askTotalCount", goods.getTotalCount());
                item.put("price", goods.getPrice());
                item.put("amount", goods.getAmount());
                item.put("barcode", goods.getBarcode());
                if (goods.getIsPrivate().equals(IsPrivate.PRIVATE)) {
                    item.put("controlType", "1");
                } else if (goods.getIsPrivate().equals(IsPrivate.UNIFORM)) {
                    item.put("controlType", "0");
                }

                items.add(item);
            }
        }
        jsonObject.put("items", items);

        InvOrderApiImpl.askSendOrder(jsonObject.toJSONString(), sendOrderRespCallback);
    }

    NetCallBack.NetTaskCallBack sendOrderRespCallback = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("采购订单创建失败: " + errMsg);
                    //查询失败
//                        animProgress.setVisibility(View.GONE);
                    onLoadError(errMsg);
                }

                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"新增成功!","version":"1","data":{"val":"158"}}
                    /**
                     10           * 新建订单成功,清空购物车
                     * */
                    PurchaseShopcartHelper.getInstance().clear();

                    showProgressDialog(ProgressDialog.STATUS_DONE, "下单成功", true);
                    //刷新购物车
                    EventBus.getDefault().post(new PurchaseShopcartSyncEvent(PurchaseShopcartSyncEvent.EVENT_ID_ORDER_SUCCESS));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onLoadFinished();
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                        }
                    }, 1000);
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };


    private void changeCompany(CompanyInfo companyInfo){
        mCompanyInfo = companyInfo;
        labelCompany.setLabelText(mCompanyInfo != null ? mCompanyInfo.getName() : "");

        if (mCompanyInfo != null){
            loadIntelligentOrder();
        }
    }
    /**
     * 智能订货
     * */
    public void loadIntelligentOrder() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        goodsListAdapter.setEntityList(null);

        onLoadProcess("正在为您智能订货...");
        StockApiImpl.autoAskSendOrder(mCompanyInfo.getId(), intelligentRespCallback);
    }

    NetCallBack.NetTaskCallBack intelligentRespCallback = new NetCallBack.NetTaskCallBack<InvSendOrderItemBrief,
            NetProcessor.Processor<InvSendOrderItemBrief>>(
            new NetProcessor.Processor<InvSendOrderItemBrief>() {
                @Override
                public void processResult(IResponseData rspData) {
                    onLoadFinished();

                    List<InvSendOrderItem> orderItems = new ArrayList<>();

                    if (rspData != null) {
                        //com.mfh.comn.net.data.RspBean cannot be cast to com.mfh.comn.net.data.RspValue
                        RspBean<InvSendOrderItemBrief> retValue = (RspBean<InvSendOrderItemBrief>) rspData;
                        InvSendOrderItemBrief orderDetail = retValue.getValue();
                        orderItems = orderDetail.getItems();
                    }

                    goodsListAdapter.setEntityList(orderItems);
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    ZLogger.d("智能订货失败：" + errMsg);

                    onLoadError(errMsg);
                }
            }
            , InvSendOrderItemBrief.class
            , CashierApp.getAppContext()) {
    };



    @Override
    public void onLoadProcess(String description) {
        super.onLoadProcess(description);
        btnConfirm.setEnabled(false);
    }

    @Override
    public void onLoadFinished() {
        super.onLoadFinished();
//        btnConfirm.setEnabled(true);
    }

    private void initGoodsRecyclerView() {
        mRLayoutManager = new LinearLayoutManager(getActivity());
        mRLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
//        //添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(emptyView);
        goodsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem = mRLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = mRLayoutManager.getItemCount();
                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
                // dy>0 表示向下滑动
//                ZLogger.d(String.format("%s %d(%d)", (dy > 0 ? "向上滚动" : "向下滚动"), lastVisibleItem, totalItemCount));
                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        goodsListAdapter = new IntelligentGoodsAdapter(getActivity(), null);
        goodsListAdapter.setOnAdapterListener(new IntelligentGoodsAdapter.OnAdapterListener() {

                                                  @Override
                                                  public void onShowDetail(InvSendOrderItem goods) {
                                                      Bundle extras = new Bundle();
                                                      extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
                                                      extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FRAGMENT_TYPE_PURCHASE_GOODSDETAIL);
                                                      extras.putString(PurchaseGoodsDetailFragment.EXTRA_KEY_SKU_NAME, goods.getProductName());
                                                      extras.putString(PurchaseGoodsDetailFragment.EXTRA_KEY_BARCODE, goods.getBarcode());
                                                      extras.putString(PurchaseGoodsDetailFragment.EXTRA_KEY_IMAGE_URL, goods.getImgUrl());
                                                      SimpleDialogActivity.actionStart(getActivity(), extras);
                                                  }

                                                  @Override
                                                  public void onDataSetChanged() {
                                                      if (goodsListAdapter != null && goodsListAdapter.getItemCount() > 0){
                                                          btnConfirm.setEnabled(true);
                                                      }
                                                      else{
                                                          btnConfirm.setEnabled(false);
                                                      }
                                                      onLoadFinished();
                                                  }
                                              }

        );
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }

    /**
     * 翻页加载更多数据
     */
    private void loadMore() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载商品列表。");
            onLoadFinished();
            return;
        }

//        if (bSyncInProgress) {
//            ZLogger.d("正在加载线上订单订单流水。");
//            onLoadFinished();
//            return;
//        }

        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            inventoryGoodsPresenter.loadPurchaseGoods(mPageInfo, "", 0L, "", "", 0, "");
        } else {
            ZLogger.d("加载商品列表，已经是最后一页。");
            onLoadFinished();
        }
    }


    @Override
    public void onProcess() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(String errorMsg) {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onData(ScGoodsSku data) {

        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onList(PageInfo pageInfo, List<ScGoodsSku> dataList) {
        mProgressBar.setVisibility(View.GONE);

    }
}
