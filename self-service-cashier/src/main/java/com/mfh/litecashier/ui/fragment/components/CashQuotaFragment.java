package com.mfh.litecashier.ui.fragment.components;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.model.OrderPayWay;
import com.bingshanguxue.cashier.model.PayOrder;
import com.bingshanguxue.cashier.model.wrapper.QuickPayInfo;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.BizConfig;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.cashier.CashierApiImpl;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.alarm.AlarmManagerHelper;
import com.mfh.litecashier.bean.wrapper.CashQuotaInfo;
import com.mfh.litecashier.com.PrintManager;
import com.mfh.litecashier.service.UploadSyncManager;
import com.mfh.litecashier.ui.adapter.CashQuotaAdapter;
import com.mfh.litecashier.ui.dialog.AlipayDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * <h1>现金授权</h1>
 * <p>首先判断是否已经日结过:<br>
 * 1.如果已经日结过,则不需要启动日结统计，可以直接查询统计数据。最后也不需要进行日结确认。<br>
 * 2.如果未日结，则需要启动日结统计，然后再查询统计数据，最后需要确认日结操作。</p>
 * <p/>
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class CashQuotaFragment extends BaseProgressFragment {

//    public static final String EXTRA_KEY_CANCELABLE = "cancelable";
//    public static final String EXTRA_KEY_DATETIME = "datetime";

    @Bind(R.id.tv_header_title)
    TextView tvHeaderTitle;
    @Bind(R.id.button_header_reload)
    ImageButton btnReload;
    @Bind(R.id.button_header_close)
    ImageButton btnClose;


    @Bind(R.id.label_quota)
    MultiLayerLabel tvQuota;
    @Bind(R.id.label_out)
    MultiLayerLabel tvOut;
    @Bind(R.id.label_left)
    MultiLayerLabel tvLeft;

    @Bind(R.id.spinner_biztype)
    Spinner spinnerBiztype;
    @Bind(R.id.order_list)
    RecyclerViewEmptySupport aggRecyclerView;
    private LinearLayoutManager mRLayoutManager;
    private CashQuotaAdapter aggListAdapter;
    @Bind(R.id.empty_view)
    TextView emptyView;

    @Bind(R.id.fab_print)
    FloatingActionButton fabPrint;

    private AlipayDialog alipayDialog = null;

//    private boolean cancelable = true;//是否可以关闭窗口
    private CashQuotaInfo mCashQuotaInfo;

    protected boolean bSyncInProgress = false;//是否正在同步
    protected static final int MAX_SYNC_PAGESIZE = 20;
    protected PageInfo mPageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, MAX_SYNC_PAGESIZE);

    public static CashQuotaFragment newInstance(Bundle args) {
        CashQuotaFragment fragment = new CashQuotaFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_components_cashquota;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        ZLogger.df(String.format(">>开始授权查询：%s", StringUtils.decodeBundle(args)));
//        if (args != null) {
//            cancelable = args.getBoolean(EXTRA_KEY_CANCELABLE, true);
////            dailySettleDatetime = args.getString(EXTRA_KEY_DATETIME);
//        }

        tvHeaderTitle.setText("授权查询");
        ArrayAdapter<CharSequence> priceTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.biztype_cashquota, R.layout.mfh_spinner_item_text);
        priceTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBiztype.setAdapter(priceTypeAdapter);
        spinnerBiztype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                spinnerBiztype.getSelectedItem().toString();
                loadDetailList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerBiztype.setSelection(0);
        initAggRecyclerView();

//        if (cancelable) {
//            btnClose.setVisibility(View.VISIBLE);
//        } else {
//            btnClose.setVisibility(View.GONE);
//        }


        mCashQuotaInfo = new CashQuotaInfo();

        refresh();
        queryLimitInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.button_header_close)
    public void finishActivity() {
//        if (!cancelable) {
//            DialogUtil.showHint("请先确认当前日结");
//            return;
//        }
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    private void initAggRecyclerView() {
        mRLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        mRLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        aggRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        aggRecyclerView.setHasFixedSize(true);
//        //添加分割线
        aggRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        aggRecyclerView.setEmptyView(emptyView);
        aggRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
//                ZLogger.d(String.format("%s %d(%d)", (dy > 0 ? "向上滚动" : "向下滚动"),
//                        lastVisibleItem, totalItemCount));
                if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });
        aggListAdapter = new CashQuotaAdapter(CashierApp.getAppContext(), null);
        aggListAdapter.setOnAdapterListener(new CashQuotaAdapter.OnAdapterListener() {

                                                @Override
                                                public void onDataSetChanged() {
//                                                      onLoadFinished();
                                                }
                                            }

        );
        aggRecyclerView.setAdapter(aggListAdapter);
    }


    /**
     * 刷新数据
     */
    private void refresh() {
        try {
            tvOut.setTopText(MUtils.formatDouble(mCashQuotaInfo.getUnpaid(), ""));
            if (mCashQuotaInfo.getLimit() == null || mCashQuotaInfo.getLimit().compareTo(0D) == 0){
                tvQuota.setTopText("未设置");
                tvLeft.setTopText("无限");
            }
            else{
                tvQuota.setTopText(MUtils.formatDouble(mCashQuotaInfo.getLimit(), "未设置"));
                tvLeft.setTopText(MUtils.formatDouble(mCashQuotaInfo.getLimit() - mCashQuotaInfo.getUnpaid(), ""));
            }

            //显示经营数据
            if (aggListAdapter != null) {
                String bizType = spinnerBiztype.getSelectedItem().toString();
                if (bizType.equals("现金订单")) {
                    aggListAdapter.setEntityList(mCashQuotaInfo.getOrderPayWays());
                } else if (bizType.equals("授信充值")) {
                    aggListAdapter.setEntityList(mCashQuotaInfo.getPosOrders());
                }
            }

        } catch (Exception ex) {
            ZLogger.e(String.format("刷新日结数据失败：%s", ex.toString()));
        }
    }

    /**
     * 查询
     */
    @OnClick(R.id.button_header_reload)
    public void queryLimitInfo() {
        onLoadProcess("正在查询授权金额信息");

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onLoadError("网络未连接");
            return;
        }

        CashierApiImpl.queryLimitInfo(queryLimitInfoRC);
    }

    //回调
    NetCallBack.NetTaskCallBack queryLimitInfoRC = new NetCallBack.NetTaskCallBack<String,
            NetProcessor.Processor<String>>(
            new NetProcessor.Processor<String>() {
                @Override
                public void processResult(IResponseData rspData) {
//                        {"code":"0","msg":"查询成功!","version":"1","data":"false,8.99"}
                    try {
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        String result = retValue.getValue();
                        String[] ret = result.split(",");
                        if (ret.length >= 2) {
//                                Boolean.parseBoolean()1
//                                boolean isNeedLock = Boolean.valueOf(ret[0]).booleanValue();
                            Double limitAmount = Double.valueOf(ret[0]);
                            Double amount = Double.valueOf(ret[1]);

                            mCashQuotaInfo.setLimit(limitAmount);
                            mCashQuotaInfo.setUnpaid(amount);
                            ZLogger.df(String.format("查询授权限额成功，limitAmount=%.2f, 未缴纳现金=%.2f",
                                    limitAmount, amount));
                        } else {
                            ZLogger.df("解析授权限额数据失败:" + result);
                        }
                    } catch (NumberFormatException e) {
//                            e.printStackTrace();
                        ZLogger.ef("查询授权限额失败:" + e.toString());
                    }
                    loadDetailList();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //{"code":"1","msg":"指定的日结流水已经日结过：17","version":"1","data":null}
                    onLoadError("查询授权限额失败：" + errMsg);
                    loadDetailList();
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };

    private void loadDetailList() {
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onLoadError("网络未连接。");
            return;
        }

        onLoadProcess("加载中...");
        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        String bizType = spinnerBiztype.getSelectedItem().toString();
        if (bizType.equals("现金订单")) {
            listOrderPayWay(mPageInfo);
        } else if (bizType.equals("授信充值")) {
            listPayOrder(mPageInfo);
        }
        mPageInfo.setPageNo(1);
    }

    private void loadMore(){
        if (bSyncInProgress) {
            ZLogger.d("正在加载数据...");
            return;
        }
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onLoadError("网络未连接。");
            return;
        }

        if (mPageInfo.hasNextPage()) {
            mPageInfo.moveToNext();
            String bizType = spinnerBiztype.getSelectedItem().toString();
            if (bizType.equals("现金订单")) {
                listOrderPayWay(mPageInfo);
            } else if (bizType.equals("授信充值")) {
                listPayOrder(mPageInfo);
            }
        } else {
            ZLogger.d("加载类目商品，已经是最后一页。");
            onLoadFinished();
        }
    }

    /**
     * 查询现金订单
     */
    private void listOrderPayWay(PageInfo pageInfo) {
        onLoadProcess("正在查询现金订单...");

        ZLogger.d(String.format("保存现金订单流水, 请求%d/%d--%d",
                pageInfo.getPageNo(), pageInfo.getTotalPage(), pageInfo.getPageSize()));

        NetCallBack.QueryRsCallBack responseRC = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<OrderPayWay>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<OrderPayWay> rs) {
                        //保存日结数据
                        mPageInfo = pageInfo;
                        saveOrderPayWay(rs);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        onLoadError("查询查询现金订单失败：" + errMsg);
                    }
                }, OrderPayWay.class, CashierApp.getAppContext());

        CashierApiImpl.listOrderPayWay(WayType.CASH, pageInfo, responseRC);
    }

    /**
     * 查询授权充值
     */
    private void listPayOrder(PageInfo pageInfo) {
        onLoadProcess("正在查询授信充值");

        ZLogger.d(String.format("保存授信充值流水, 请求%d/%d--%d",
                pageInfo.getPageNo(), pageInfo.getTotalPage(), pageInfo.getPageSize()));

        NetCallBack.QueryRsCallBack responseRC = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<PayOrder>(pageInfo) {
                    @Override
                    public void processQueryResult(RspQueryResult<PayOrder> rs) {
                        //保存日结数据
                        mPageInfo = pageInfo;
                        saveAccData(rs);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        onLoadError("查询授信充值失败：" + errMsg);
                    }
                }, PayOrder.class, CashierApp.getAppContext());

        CashierApiImpl.listPayOrder(BizType.DAILYSETTLE, 2, pageInfo, responseRC);
    }


    /**
     * 保存经营分析数据
     */
    private void saveOrderPayWay(RspQueryResult<OrderPayWay> rs) {
        try {
            List<OrderPayWay> temp = mCashQuotaInfo.getOrderPayWays();
            if (temp == null) {
                temp = new ArrayList<>();
            }
            if (mPageInfo.getPageNo() == 1){
                temp.clear();
            }

            if (rs != null && rs.getReturnNum() > 0) {
                for (EntityWrapper<OrderPayWay> wrapper : rs.getRowDatas()) {
                    OrderPayWay aggItem = wrapper.getBean();
//                    aggItem.setBizTypeCaption(wrapper.getPropCaption("bizType"));
//                    aggItem.setSubTypeCaption(wrapper.getPropCaption("subType"));
                    aggItem.setBizType(0);
                    aggItem.setBizTypeCaption("现金订单");
                    temp.add(aggItem);
                }
            }

            mCashQuotaInfo.setOrderPayWays(temp);
            ZLogger.d(String.format("更新现金收银流水, 请求%d/%d ,已加载(%d)",
                    mPageInfo.getPageNo(), mPageInfo.getTotalPage(),mPageInfo.getTotalCount()));

            refresh();
        } catch (Exception ex) {
            ZLogger.d("保存查询现金订单失败:" + ex.toString());
        }

        onLoadFinished();
    }

    /**
     * 保存流水分析数据
     */
    private void saveAccData(RspQueryResult<PayOrder> rs) {
        try {
            List<OrderPayWay> temp = mCashQuotaInfo.getPosOrders();
            if (temp == null) {
                temp = new ArrayList<>();
            }
            if (mPageInfo.getPageNo() == 1){
                temp.clear();
            }

            if (rs != null && rs.getReturnNum() > 0) {
                for (EntityWrapper<PayOrder> wrapper : rs.getRowDatas()) {
                    PayOrder posOrder = wrapper.getBean();
                    OrderPayWay cashQuotaOrderInfo = new OrderPayWay();
                    //单位是分，转换成元。
                    cashQuotaOrderInfo.setAmount(posOrder.getTotalFee() / 100);
                    cashQuotaOrderInfo.setCreatedDate(posOrder.getUpdatedDate());
                    cashQuotaOrderInfo.setBizType(1);
                    cashQuotaOrderInfo.setBizTypeCaption("授权充值");
                    temp.add(cashQuotaOrderInfo);
                }
            }

            mCashQuotaInfo.setPosOrders(temp);
            ZLogger.d(String.format("更新授信充值流水, 请求%d/%d ,已加载(%d)",
                    mPageInfo.getPageNo(), mPageInfo.getTotalPage(),mPageInfo.getTotalCount()));
            refresh();
        } catch (Exception ex) {
            ZLogger.d("保存授信充值失败:" + ex.toString());
        }

        onLoadFinished();
    }

    /**
     * 针对当前用户所属网点提交营业现金，并触发一次日结操作
     */
    @OnClick(R.id.fab_print)
    public void commitCash() {
        final QuickPayInfo quickPayInfo = new QuickPayInfo();
        quickPayInfo.setBizType(BizType.DAILYSETTLE);
        quickPayInfo.setSubBizType(BizType.CASH_QUOTA);
        quickPayInfo.setPayType(WayType.ALI_F2F);
        quickPayInfo.setSubject("提交营业现金");
        quickPayInfo.setBody("为了不影响您使用POS设备，请及时提交营业现金！");
        if (!BizConfig.RELEASE) {
            quickPayInfo.setAmount(100D);
            quickPayInfo.setMinAmount(0.01D);
        } else {
            quickPayInfo.setAmount(0.01D);
            quickPayInfo.setMinAmount(0.01D);
        }

        ZLogger.df(String.format(">>>准备提交营业现金: %s", JSONObject.toJSONString(quickPayInfo)));

        if (alipayDialog == null) {
            alipayDialog = new AlipayDialog(getActivity());
            alipayDialog.setCancelable(false);
            alipayDialog.setCanceledOnTouchOutside(false);
        }
        alipayDialog.initialize(quickPayInfo, true, false, new AlipayDialog.DialogClickListener() {
            @Override
            public void onPaySucceed(QuickPayInfo mQuickPayInfo, String outTradeNo) {
                PrintManager.printTopupReceipt(quickPayInfo, outTradeNo);

                UploadSyncManager.getInstance().sync();

                AlarmManagerHelper.triggleNextDailysettle(0);
            }

            @Override
            public void onPayCanceled() {
            }

        });

        if (!alipayDialog.isShowing()) {
            alipayDialog.show();
        }
    }
}
