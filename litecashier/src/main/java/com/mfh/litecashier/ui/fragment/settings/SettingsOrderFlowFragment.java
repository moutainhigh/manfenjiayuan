package com.mfh.litecashier.ui.fragment.settings;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.api.impl.CashierApiImpl;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.framework.api.constant.WayType;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.database.entity.PosOrderEntity;
import com.mfh.litecashier.database.entity.PosOrderItemEntity;
import com.mfh.litecashier.database.entity.PosOrderPayEntity;
import com.mfh.litecashier.database.logic.PosOrderItemService;
import com.mfh.litecashier.database.logic.PosOrderPayService;
import com.mfh.litecashier.database.logic.PosOrderService;
import com.mfh.litecashier.event.OrderflowLocalWarehouseEvent;
import com.mfh.litecashier.service.OrderSyncManager;
import com.mfh.litecashier.ui.adapter.LocalOrderflowGoodsAdapter;
import com.mfh.litecashier.ui.adapter.SettingsOrderflowAdapter;
import com.mfh.litecashier.ui.widget.InputSearchView;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 设置－－门店所有订单流水
 * 显示POS机本地的订单流水
 *
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public class SettingsOrderFlowFragment extends BaseFragment {
    private static final int STATE_NONE = 0;
    private static final int STATE_REFRESH = 1;
    private static final int STATE_LOADMORE = 2;
    private static final int STATE_NOMORE = 3;
    private static final int STATE_PRESSNONE = 4;// 正在下拉但还没有到刷新的状态
    private static int mState = STATE_NONE;
    @Bind(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.order_list)
    RecyclerViewEmptySupport orderRecyclerView;
    @Bind(R.id.empty_view)
    TextView emptyView;
    private LinearLayoutManager linearLayoutManager;
    private SettingsOrderflowAdapter orderListAdapter;

    @Bind(R.id.spinner_tenant)
    Spinner spinnerTenant;
    @Bind(R.id.spinner_status)
    Spinner spinnerStatus;
    @Bind(R.id.insv_order_barcode)
    InputSearchView insvOrderBarcode;
    @Bind(R.id.goods_list)
    RecyclerView goodsRecyclerView;
    private LocalOrderflowGoodsAdapter goodsListAdapter;


    @Bind(R.id.tv_officename)
    TextView tvOfficeName;
    @Bind(R.id.tv_receipt_header)
    TextView tvReceiptHeader;
    @Bind(R.id.tv_receipt_tail)
    TextView tvReceiptTail;

    @Bind(R.id.fab_sync)
    FloatingActionButton fabSync;

    private boolean isLoadingMore;
    private static final int MAX_SYNC_PAGESIZE = 7;
    private PageInfo mPageInfo = new PageInfo(false, MAX_SYNC_PAGESIZE);//new PageInfo(PageInfo.PAGENO_NOTINIT, MAX_SYNC_PAGESIZE);
    private boolean bSyncInProgress = false;//是否正在同步

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settints_orderflow;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initProgressDialog("正在同步订单", " 同步成功", "同步失败");

        ArrayAdapter<CharSequence> priceTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.order_tenant_query, R.layout.mfh_spinner_item_text);
        priceTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTenant.setAdapter(priceTypeAdapter);
        spinnerTenant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reload();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerTenant.setSelection(0);

        ArrayAdapter<CharSequence> orderStatusAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.order_status_query, R.layout.mfh_spinner_item_text);
        orderStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(orderStatusAdapter);
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reload();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerStatus.setSelection(0);

        initOrderBarcodeView();
        setupSwipeRefresh();
        initOrderRecyclerView();
        initGoodsRecyclerView();

        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (orderRecyclerView != null) {
            orderRecyclerView.removeOnScrollListener(orderListScrollListener);
        }

        EventBus.getDefault().unregister(this);
    }

    private void initOrderBarcodeView() {
        insvOrderBarcode.setInputSubmitEnabled(true);
        insvOrderBarcode.setSoftKeyboardEnabled(false);
        insvOrderBarcode.config(InputSearchView.INPUT_TYPE_TEXT);
        insvOrderBarcode.setSearchButtonVisible(false);
//        inlvProductName.requestFocus();
        insvOrderBarcode.setOnInoutKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d("setOnKeyListener(CashierFragment.inlvBarcode):" + keyCode);
                //Press “Enter”
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    //条码枪扫描结束后会自动触发回车键
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        reload();
                    }

                    return true;
                }

                return (keyCode == KeyEvent.KEYCODE_TAB
                        || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
            }
        });
        insvOrderBarcode.setOnViewListener(new InputSearchView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                reload();
            }
        });
        insvOrderBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                reload();
            }
        });
//        labelShortcode.setOnViewListener(new InputSearchView.OnViewListener() {
//            @Override
//            public void onSubmit(String text) {
//                reload();
//            }
//        });
    }
    /**
     * 初始化订单列表
     */
    private void initOrderRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        orderRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        orderRecyclerView.setHasFixedSize(true);
        //添加分割线
        orderRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST, 8));
        //设置列表为空时显示的视图
        orderRecyclerView.setEmptyView(emptyView);
        orderRecyclerView.addOnScrollListener(orderListScrollListener);

        orderListAdapter = new SettingsOrderflowAdapter(getActivity(), null);
        orderListAdapter.setOnAdapterListener(new SettingsOrderflowAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                PosOrderEntity orderEntity = orderListAdapter.getCurPosOrder();
                loadReceipt(orderEntity);
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }

            @Override
            public void onDataSetChanged() {
                onLoadFinished();
                PosOrderEntity orderEntity = orderListAdapter.getCurPosOrder();
                loadReceipt(orderEntity);
            }
        });
        orderRecyclerView.setAdapter(orderListAdapter);
    }

    private RecyclerView.OnScrollListener orderListScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            int totalItemCount = linearLayoutManager.getItemCount();
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
    };

    private void initGoodsRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
        goodsRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        goodsListAdapter = new LocalOrderflowGoodsAdapter(CashierApp.getAppContext(), null);
        goodsListAdapter.setOnAdapterListener(new LocalOrderflowGoodsAdapter.OnAdapterListener() {

            @Override
            public void onDataSetChanged() {
            }
        });
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }

    /**
     * 打印订单
     * */
    @OnClick(R.id.fab_print)
    public void printOrder() {
        PosOrderEntity orderEntity = orderListAdapter.getCurPosOrder();
        if (orderEntity == null) {
            ZLogger.d("订单无效");
            DialogUtil.showHint("请先选择订单");
            return;
        }

        SerialManager.printPosOrder(orderEntity, true);
    }

    /**
     * 加载订单小票
     * @param orderEntity 订单
     * */
    private void loadReceipt(PosOrderEntity orderEntity){

        tvOfficeName.setText(MfhLoginService.get().getCurOfficeName());

        if (orderEntity == null){
            //头部：订单编号
            tvReceiptHeader.setText(String.format("%s NO.%s\n%s",
                    SharedPreferencesManager.getTerminalId(), "", ""));

            //明细：商品信息
            goodsListAdapter.setEntityList(null);

            //尾部:订单支付信息
            //        sbTail.append("--------------------------------\n");
            tvReceiptTail.setText(Html.fromHtml(String.format("<p><font color=#000000>应收：%.2f\n</font></p>", 0D)
                    + String.format("<p><font color=#000000>优惠：%.2f\n</font></p>", 0D)
                    + String.format("<p><font color=#000000>合计：%.2f\n</font></p>", 0D)
                    + String.format("<p><font color=#000000>付款：%.2f\n</font></p>", 0D)
                    + String.format("<p><font color=#32CD32>找零：%.2f\n</font></p>", 0D)));

            fabSync.setVisibility(View.GONE);
        }
        else{
            //头部：订单编号
            tvReceiptHeader.setText(String.format("%s NO.%s\n%s",
                    SharedPreferencesManager.getTerminalId(), orderEntity.getBarCode(),
                    TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(orderEntity.getUpdatedDate())));

            //明细：商品信息
            List<PosOrderItemEntity> itemEntityList = PosOrderItemService.get()
                    .queryAllBy(String.format("orderBarCode = '%s'", orderEntity.getBarCode()));
            goodsListAdapter.setEntityList(itemEntityList);

            //尾部:订单支付信息
            StringBuilder sbTail = new StringBuilder();
//        sbTail.append("--------------------------------\n");
            sbTail.append(String.format("<p><font color=#000000>应收：%.2f\n</font></p>",
                    orderEntity.getRetailAmount()));
            sbTail.append(String.format("<p><font color=#000000>优惠：%.2f\n</font></p>",
                    orderEntity.getDiscountAmount() + orderEntity.getCouponDiscountAmount()));
            sbTail.append(String.format("<p><font color=#000000>合计：%.2f\n</font></p>",
                    orderEntity.getRetailAmount() - orderEntity.getDiscountAmount() - orderEntity.getCouponDiscountAmount()));
            sbTail.append(String.format("<p><font color=#000000>付款：%.2f\n</font></p>",
                    orderEntity.getPaidMoney()));
            //支付记录
            List<PosOrderPayEntity> payEntityList = PosOrderPayService.get()
                    .queryAllBy(String.format("orderBarCode = '%s'", orderEntity.getBarCode()));
            for (PosOrderPayEntity payEntity : payEntityList){
                sbTail.append(String.format("<p><font color=#979797>\t%s：%.2f [%s]\n</font></p>",
                        WayType.name(payEntity.getPayType()), payEntity.getAmount(),
                        PosOrderPayEntity.getPayStatusDesc(payEntity.getPaystatus())));
            }
            if (orderEntity.getStatus() == PosOrderEntity.ORDER_STATUS_FINISH){
                sbTail.append(String.format("<p><font color=#32CD32>找零：%.2f\n</font></p>",
                        orderEntity.getCharge()));

                fabSync.setVisibility(View.VISIBLE);
            }
            else{
                fabSync.setVisibility(View.GONE);
            }
            tvReceiptTail.setText(Html.fromHtml(sbTail.toString()));
        }
    }

    /**
     * 同步订单
     * */
    @OnClick(R.id.fab_sync)
    public void syncOrder() {
        final PosOrderEntity orderEntity = orderListAdapter.getCurPosOrder();
        if (orderEntity == null) {
            ZLogger.d("订单无效");
            DialogUtil.showHint("请先选择订单");
            return;
        }

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            DialogUtil.showHint(R.string.toast_network_error);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING);

        JSONArray orders = new JSONArray();
        orders.add(OrderSyncManager.get().generateOrderJson(orderEntity));

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        ZLogger.d("DataSync--上传POS订单成功");

                        //修改订单同步状态
                        orderEntity.setSyncStatus(PosOrderEntity.SYNC_STATUS_SYNCED);
                        PosOrderService.get().saveOrUpdate(orderEntity);

                        showProgressDialog(ProgressDialog.STATUS_DONE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                            }
                        }, 1000);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("DataSync--上传订单失败: " + errMsg);
                        showProgressDialog(ProgressDialog.STATUS_ERROR, errMsg, true);

//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                hideProgressDialog();
//                            }
//                        }, 1000);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.batchInOrders(orders.toJSONString(), responseCallback);
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(OrderflowLocalWarehouseEvent event) {
        ZLogger.d(String.format("LocalOrderFlowFragment: OrderflowLocalWarehouseEvent(%d)", event.getAffairId()));
        if (event.getAffairId() == OrderflowLocalWarehouseEvent.EVENT_ID_RELOAD_DATA) {
            refresh();
            reload();
        }
    }

    private void refresh(){
        spinnerTenant.setSelection(0);
        spinnerStatus.setSelection(0);
        insvOrderBarcode.clear();
        insvOrderBarcode.requestFocus();
    }


    /**
     * 开始加载
     */
    private void onLoadStart() {
        isLoadingMore = true;
        bSyncInProgress = true;
        setRefreshing(true);
    }

    /**
     * 加载完成
     */
    private void onLoadFinished() {
        bSyncInProgress = false;
        isLoadingMore = false;
        setRefreshing(false);
    }

    /**
     * 重新加载数据
     */
    @OnClick(R.id.empty_view)
    public synchronized void reload() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载线下门店订单流水。");
//            onLoadFinished();
            return;
        }

        onLoadStart();

//        mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
        mPageInfo.reset();
//        if (orderListAdapter != null) {
//            orderListAdapter.setEntityList(null);
//        }

        if (orderListAdapter != null) {
            orderListAdapter.setEntityList(null);
        }

        mPageInfo.setPageNo(1);
        load(mPageInfo);
    }


    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载线下门店订单流水。");
//            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage()) {
            mPageInfo.moveToNext();

            onLoadStart();
            load(mPageInfo);
        } else {
            ZLogger.d("加载线下门店订单流水，已经是最后一页。");
            onLoadFinished();
        }
    }

    /**
     * 加载数据
     * */
    private void load(PageInfo pageInfo) {
        String barcode = insvOrderBarcode != null ? insvOrderBarcode.getInputString() : "";

        StringBuilder sbWhere = new StringBuilder();
        sbWhere.append(String.format("barCode like '%%%s%%'", barcode));

        String tenantStr = spinnerTenant != null ? spinnerTenant.getSelectedItem().toString() : "";
        if (tenantStr.equals("当前租户")){
            sbWhere.append(String.format(" and sellerId = '%d'", MfhLoginService.get().getSpid()));
        }

        String orderStatusCaption = spinnerStatus != null ? spinnerStatus.getSelectedItem().toString() : "";
        switch (orderStatusCaption){
            case "等待支付":{
                sbWhere.append(String.format(" and status = '%d'", PosOrderEntity.ORDER_STATUS_STAY_PAY));
            }
            break;
            case "挂单":{
                sbWhere.append(String.format(" and status = '%d'", PosOrderEntity.ORDER_STATUS_HANGUP));
            }
            break;
            case "支付处理中":{
                sbWhere.append(String.format(" and status = '%d'", PosOrderEntity.ORDER_STATUS_PROCESS));
            }
            break;
            case "异常":{
                sbWhere.append(String.format(" and status = '%d'", PosOrderEntity.ORDER_STATUS_EXCEPTION));
            }
            break;
            case "已结束":{
                sbWhere.append(String.format(" and status = '%d'", PosOrderEntity.ORDER_STATUS_FINISH));
            }
            break;
            default:{
            }
            break;
        }

        List<PosOrderEntity> entityList  = PosOrderService.get()
                .queryAllDesc(sbWhere.toString(), pageInfo);

        if (entityList == null || entityList.size() < 1) {
            ZLogger.d("没有找到订单。");

            onLoadFinished();
            return;
        }
        ZLogger.d(String.format("共找到%d条订单(%d/%d-%d)", entityList.size(), pageInfo.getPageNo(), pageInfo.getTotalPage(), pageInfo.getTotalCount()));

        if (orderListAdapter != null) {
            orderListAdapter.appendEntityList(entityList);
        }
        onLoadFinished();
    }

    /**
     * 设置刷新
     */
    private void setupSwipeRefresh() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(
                    R.color.swiperefresh_color1, R.color.swiperefresh_color2,
                    R.color.swiperefresh_color3, R.color.swiperefresh_color4);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (mState == STATE_REFRESH) {
                        return;
                    }

                    reload();
                }
            });
        }
        mState = STATE_NONE;
    }

    /**
     * 设置刷新状态
     */
    public void setRefreshing(boolean refreshing) {
        if (refreshing) {
            setSwipeRefreshLoadingState();
        } else {
            setSwipeRefreshLoadedState();
        }
    }

    /**
     * 设置顶部正在加载的状态
     */
    private void setSwipeRefreshLoadingState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
            // 防止多次重复刷新
            mSwipeRefreshLayout.setEnabled(false);


            mState = STATE_REFRESH;
        }
    }

    /**
     * 设置顶部加载完毕的状态
     */
    private void setSwipeRefreshLoadedState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(true);

            mState = STATE_NONE;
        }
    }

}
