package com.mfh.litecashier.ui.fragment.components;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.entity.PosOrderEntity;
import com.bingshanguxue.cashier.database.service.PosOrderService;
import com.bingshanguxue.cashier.model.CashierOrderInfo;
import com.bingshanguxue.cashier.CashierProvider;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.event.OrderflowLocalWarehouseEvent;
import com.mfh.litecashier.service.DataUploadManager;
import com.mfh.litecashier.ui.adapter.ExceptionOrderflowAdapter;
import com.mfh.litecashier.ui.dialog.DailysettlePreviewDialog;
import com.mfh.litecashier.ui.dialog.MyDatePickerDialog;
import com.mfh.litecashier.ui.dialog.OrderPrintPreviewDialog;
import com.mfh.litecashier.ui.dialog.PosOrderDetailDialog;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.ui.widget.InputSearchView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 设置－－门店所有订单流水
 * 显示POS机本地的订单流水
 * <p/>
 * Created by bingshanguxue on 15/8/31.
 */
public class OrderFlowFragment extends BaseFragment {
    private static final int STATE_NONE = 0;
    private static final int STATE_REFRESH = 1;
    private static final int STATE_LOADMORE = 2;
    private static final int STATE_NOMORE = 3;
    private static final int STATE_PRESSNONE = 4;// 正在下拉但还没有到刷新的状态
    private static int mState = STATE_NONE;
    @BindView(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.order_list)
    RecyclerViewEmptySupport orderRecyclerView;
    @BindView(R.id.empty_view)
    TextView emptyView;
    private LinearLayoutManager linearLayoutManager;
    private ExceptionOrderflowAdapter orderListAdapter;

    @BindView(R.id.spinner_tenant)
    Spinner spinnerTenant;
    @BindView(R.id.spinner_status)
    Spinner spinnerStatus;
    @BindView(R.id.inlv_order_id)
    InputSearchView insvOrderId;
    @BindView(R.id.insv_order_barcode)
    InputSearchView insvOrderBarcode;

    private boolean isLoadingMore;
    private static final int MAX_SYNC_PAGESIZE = 7;
    private PageInfo mPageInfo = new PageInfo(false, MAX_SYNC_PAGESIZE);//new PageInfo(PageInfo.PAGENO_NOTINIT, MAX_SYNC_PAGESIZE);
    private boolean bSyncInProgress = false;//是否正在同步

    private OrderPrintPreviewDialog mOrderPrintPreviewDialog = null;
    private DailysettlePreviewDialog mDailysettlePreviewDialog = null;

    private MyDatePickerDialog dateTimePickerDialog = null;

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

        initOrderIdView();
        initOrderBarcodeView();
        setupSwipeRefresh();
        initOrderRecyclerView();

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

    private void initOrderIdView() {
        insvOrderId.config(InputSearchView.INPUT_TYPE_TEXT);
        insvOrderId.setSearchButtonVisible(false);
//        inlvProductName.requestFocus();
        insvOrderId.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER}, new InputNumberLabelView.OnInterceptListener() {
            @Override
            public void onKey(int keyCode, String text) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER){
                    reload();
                }
            }
        });
        insvOrderId.addTextChangedListener(new TextWatcher() {
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
    }

    private void initOrderBarcodeView() {
        insvOrderBarcode.config(InputSearchView.INPUT_TYPE_TEXT);
        insvOrderBarcode.setSearchButtonVisible(false);
//        inlvProductName.requestFocus();
        insvOrderBarcode.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER}, new InputNumberLabelView.OnInterceptListener() {
            @Override
            public void onKey(int keyCode, String text) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER){
                    reload();
                }
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
//        orderRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.VERTICAL_LIST, 8));
        //设置列表为空时显示的视图
        orderRecyclerView.setEmptyView(emptyView);
        orderRecyclerView.addOnScrollListener(orderListScrollListener);

        orderListAdapter = new ExceptionOrderflowAdapter(getActivity(), null);
        orderListAdapter.setOnAdapterListener(new ExceptionOrderflowAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }

            @Override
            public void onProcessClick(PosOrderEntity entity) {
                showOrderDetail(entity);
            }

            @Override
            public void onPrintPreview(PosOrderEntity entity) {
                printPreview(entity);
            }

            @Override
            public void onDataSetChanged() {
                onLoadFinished();
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
            if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
                if (!isLoadingMore) {
                    loadMore();
                }
            } else if (dy < 0) {
                isLoadingMore = false;
            }
        }
    };

    //异常订单，显示订单详情处理
    private PosOrderDetailDialog mPosOrderDetailDialog = null;
    private void showOrderDetail(PosOrderEntity orderEntity){
        if (orderEntity == null || orderEntity.getStatus() != PosOrderEntity.ORDER_STATUS_EXCEPTION){
            DialogUtil.showHint(R.string.coming_soon);
            return;
        }

        CashierOrderInfo cashierOrderInfo = CashierProvider.createCashierOrderInfo(orderEntity, null);
        if (cashierOrderInfo == null){
            return;
        }

        if (mPosOrderDetailDialog == null) {
            mPosOrderDetailDialog = new PosOrderDetailDialog(getActivity());
            mPosOrderDetailDialog.setCancelable(false);
            mPosOrderDetailDialog.setCanceledOnTouchOutside(false);
        }
//        CashierOrderInfo cashierOrderInfo = CashierHelper.getCashierOrderItemInfo()
        mPosOrderDetailDialog.init(cashierOrderInfo, new PosOrderDetailDialog.onDialogClickListener() {
            @Override
            public void onDatasetChanged() {
                reload();
                DataUploadManager.getInstance().sync(DataUploadManager.POS_ORDER);
            }
        });
        if (!mPosOrderDetailDialog.isShowing()) {
            mPosOrderDetailDialog.show();
        }
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OrderflowLocalWarehouseEvent event) {
        ZLogger.d(String.format("OrderflowLocalWarehouseEvent(%d)", event.getAffairId()));
        if (event.getAffairId() == OrderflowLocalWarehouseEvent.EVENT_ID_RELOAD_DATA) {
            refresh();
            reload();
        }
    }

    private void refresh() {
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
            DialogUtil.showHint("已经是最后一页了");
            ZLogger.d("加载线下门店订单流水，已经是最后一页。");
            onLoadFinished();
        }
    }

    /**
     * 加载数据
     */
    private void load(PageInfo pageInfo) {

        StringBuilder sbWhere = new StringBuilder();

        String orderBarCode = insvOrderBarcode != null ? insvOrderBarcode.getInputString() : "";
        sbWhere.append(String.format("barCode like '%%%s%%'", orderBarCode));

        String orderId = insvOrderId.getInputString();
        if (!StringUtils.isEmpty(orderId)){
            sbWhere.append(String.format(" and id = '%s'", orderId));
        }

        String tenantStr = spinnerTenant != null ? spinnerTenant.getSelectedItem().toString() : "";
        if (tenantStr.equals("当前租户")) {
            sbWhere.append(String.format(" and sellerId = '%d'", MfhLoginService.get().getSpid()));
        }

        String orderStatusCaption = spinnerStatus != null ? spinnerStatus.getSelectedItem().toString() : "";
        switch (orderStatusCaption) {
            case "等待支付": {
                sbWhere.append(String.format(" and status = '%d'", PosOrderEntity.ORDER_STATUS_STAY_PAY));
            }
            break;
            case "挂单": {
                sbWhere.append(String.format(" and status = '%d'", PosOrderEntity.ORDER_STATUS_HANGUP));
            }
            break;
            case "支付处理中": {
                sbWhere.append(String.format(" and status = '%d'", PosOrderEntity.ORDER_STATUS_PROCESS));
            }
            break;
            case "异常": {
                sbWhere.append(String.format(" and status = '%d'", PosOrderEntity.ORDER_STATUS_EXCEPTION));
            }
            break;
            case "已结束": {
                sbWhere.append(String.format(" and status = '%d'", PosOrderEntity.ORDER_STATUS_FINISH));
            }
            break;
            default: {
            }
            break;
        }

        List<PosOrderEntity> entityList = PosOrderService.get()
                .queryAllDesc(sbWhere.toString(), pageInfo);
        if (entityList == null || entityList.size() < 1) {
            ZLogger.d("没有找到订单。");

            onLoadFinished();
            return;
        }
        ZLogger.d(String.format("共找到%d条订单(%d/%d-%d)", entityList.size(),
                pageInfo.getPageNo(), pageInfo.getTotalPage(), pageInfo.getTotalCount()));

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


    /**
     * 打印预览
     * */
    private void printPreview(PosOrderEntity orderEntity) {
        if (orderEntity == null) {
            return;
        }

//        CashierOrderInfo cashierOrderInfo = CashierHelper
//                .makeCashierOrderInfo(orderEntity.getBizType(), orderEntity.getBarCode(),
//                        orderEntity.getStatus(), null);

        if (mOrderPrintPreviewDialog == null) {
            mOrderPrintPreviewDialog = new OrderPrintPreviewDialog(getActivity());
            mOrderPrintPreviewDialog.setCancelable(false);
            mOrderPrintPreviewDialog.setCanceledOnTouchOutside(false);
        }
//        CashierOrderInfo cashierOrderInfo = CashierHelper.getCashierOrderItemInfo()
        mOrderPrintPreviewDialog.initialize(orderEntity);
        if (!mOrderPrintPreviewDialog.isShowing()) {
            mOrderPrintPreviewDialog.show();
        }
    }

    @OnClick(R.id.button_dailysettle)
    public void dailysettlePreview1() {
//        Date date = new Date();
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(TimeUtil.getCurrentDate());

        if (dateTimePickerDialog == null) {
            dateTimePickerDialog = new MyDatePickerDialog(getActivity());
            dateTimePickerDialog.setCancelable(true);
            dateTimePickerDialog.setCanceledOnTouchOutside(true);
        }
        dateTimePickerDialog.init(calendar, new MyDatePickerDialog.OnDateTimeSetListener() {
            @Override
            public void onDateTimeSet(int year, int monthOfYear, int dayOfMonth) {

                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                dailysettlePreview2(calendar.getTime());
            }
        });
        if (!dateTimePickerDialog.isShowing()) {
            dateTimePickerDialog.show();
        }
    }

    /**
     * 日结预览
     * */
    private void dailysettlePreview2(Date date) {
//        CashierOrderInfo cashierOrderInfo = CashierHelper
//                .makeCashierOrderInfo(orderEntity.getBizType(), orderEntity.getBarCode(),
//                        orderEntity.getStatus(), null);

        if (mDailysettlePreviewDialog == null) {
            mDailysettlePreviewDialog = new DailysettlePreviewDialog(getActivity());
            mDailysettlePreviewDialog.setCancelable(false);
            mDailysettlePreviewDialog.setCanceledOnTouchOutside(false);
        }
//        CashierOrderInfo cashierOrderInfo = CashierHelper.getCashierOrderItemInfo()
        mDailysettlePreviewDialog.initialize(date);
        if (!mDailysettlePreviewDialog.isShowing()) {
            mDailysettlePreviewDialog.show();
        }
    }

}
