package com.mfh.litecashier.ui.fragment.components;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.cashier.model.PayOrder;
import com.bingshanguxue.cashier.model.wrapper.QuickPayInfo;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.BizConfig;
import com.mfh.framework.api.cashier.CashierApiImpl;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.compound.MultiLayerLabel;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.alarm.AlarmManagerHelper;
import com.mfh.litecashier.bean.wrapper.CashQuotOrderInfo;
import com.mfh.litecashier.bean.wrapper.CashQuotaInfo;
import com.mfh.litecashier.com.PrintManager;
import com.mfh.litecashier.service.UploadSyncManager;
import com.mfh.litecashier.ui.adapter.CashQuotaAdapter;
import com.mfh.litecashier.ui.dialog.AlipayDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * <h1>日结</h1>
 * <p>首先判断是否已经日结过:<br>
 * 1.如果已经日结过,则不需要启动日结统计，可以直接查询统计数据。最后也不需要进行日结确认。<br>
 * 2.如果未日结，则需要启动日结统计，然后再查询统计数据，最后需要确认日结操作。</p>
 * <p/>
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class CashQuotaFragment extends BaseProgressFragment {

    public static final String EXTRA_KEY_CANCELABLE = "cancelable";
    public static final String EXTRA_KEY_DATETIME = "datetime";

    @Bind(R.id.tv_header_title)
    TextView tvHeaderTitle;


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
    private CashQuotaAdapter aggListAdapter;
    @Bind(R.id.empty_view)
    TextView emptyView;

    @Bind(R.id.button_header_close)
    ImageButton btnClose;
    @Bind(R.id.fab_print)
    FloatingActionButton fabPrint;

    private AlipayDialog alipayDialog = null;

    private boolean cancelable = true;//是否可以关闭窗口
    private CashQuotaInfo mCashQuotaInfo;


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
        Bundle args = getArguments();
        ZLogger.df(String.format(">>开始授权查询：%s", StringUtils.decodeBundle(args)));
        if (args != null) {
            cancelable = args.getBoolean(EXTRA_KEY_CANCELABLE, true);
//            dailySettleDatetime = args.getString(EXTRA_KEY_DATETIME);
        }

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

        if (cancelable) {
            btnClose.setVisibility(View.VISIBLE);
        } else {
            btnClose.setVisibility(View.INVISIBLE);
        }


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
        if (!cancelable) {
            DialogUtil.showHint("请先确认当前日结");
            return;
        }
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    private void initAggRecyclerView() {
        LinearLayoutManager mRLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
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
            tvQuota.setTopText(MUtils.formatDouble(mCashQuotaInfo.getLimit(), ""));
            tvOut.setTopText(MUtils.formatDouble(mCashQuotaInfo.getUnpaid(), ""));
            tvLeft.setTopText(MUtils.formatDouble(mCashQuotaInfo.getLimit() - mCashQuotaInfo.getUnpaid(), ""));

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

    @Override
    public void onLoadProcess(String description) {
        super.onLoadProcess(description);
    }

    @Override
    public void onLoadFinished() {
        super.onLoadFinished();
    }

    /**
     * 查询
     */
    private void queryLimitInfo() {
        onLoadProcess("正在统计日结数据");

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            onLoadError("统计失败，网络未连接，请重新日结。");
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
                        if (ret.length >=2){
//                                Boolean.parseBoolean()1
//                                boolean isNeedLock = Boolean.valueOf(ret[0]).booleanValue();
                            Double limitAmount = Double.valueOf(ret[0]);
                            Double amount = Double.valueOf(ret[1]);

                            mCashQuotaInfo.setLimit(limitAmount);
                            mCashQuotaInfo.setUnpaid(amount);
                            ZLogger.df(String.format("查询限额，limitAmount=%.2f, 未缴纳现金=%.2f",
                                    limitAmount, amount));

                        }
                        else{
                            ZLogger.df("查询限额:" + result);
                        }
                    } catch (NumberFormatException e) {
//                            e.printStackTrace();
                        ZLogger.ef(e.toString());
                    }
                    loadDetailList();
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    //{"code":"1","msg":"指定的日结流水已经日结过：17","version":"1","data":null}
                    onLoadError("查询限额失败：" + errMsg);
                }
            }
            , String.class
            , CashierApp.getAppContext()) {
    };

    private void loadDetailList(){
        String bizType = spinnerBiztype.getSelectedItem().toString();

        if (bizType.equals("现金订单")) {
            listOrderPayWay();
        } else if (bizType.equals("授信充值")) {
            listPayOrder();
        }

    }
    /**
     * 经营分析,查询业务类型日结数据
     */
    private void listOrderPayWay() {
        onLoadProcess("正在查询经营分析数据...");

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            onLoadError("网络未连接，暂停查询日结经营分析数据!");
            return;
        }

        CashierApiImpl.listOrderPayWay(WayType.CASH, orderPayWayRC);
    }

    NetCallBack.QueryRsCallBack orderPayWayRC = new NetCallBack.QueryRsCallBack<>(
            new NetProcessor.QueryRsProcessor<CashQuotOrderInfo>(new PageInfo(1, 20)) {
        @Override
        public void processQueryResult(RspQueryResult<CashQuotOrderInfo> rs) {
            //保存日结数据
            saveOrderPayWay(rs);
            onLoadFinished();
        }

        @Override
        protected void processFailure(Throwable t, String errMsg) {
            super.processFailure(t, errMsg);
            onLoadError("查询日结经营分析数据失败：" + errMsg);
        }
    }, CashQuotOrderInfo.class, CashierApp.getAppContext());

    /**
     * 保存经营分析数据
     */
    private void saveOrderPayWay(RspQueryResult<CashQuotOrderInfo> rs) {
        try {

            List<CashQuotOrderInfo> temp = mCashQuotaInfo.getOrderPayWays();
            if (temp == null){
                temp = new ArrayList<>();
            }

            if (rs != null && rs.getReturnNum() > 0) {
                for (EntityWrapper<CashQuotOrderInfo> wrapper : rs.getRowDatas()) {
                    CashQuotOrderInfo aggItem = wrapper.getBean();
//                    aggItem.setBizTypeCaption(wrapper.getPropCaption("bizType"));
//                    aggItem.setSubTypeCaption(wrapper.getPropCaption("subType"));
                    aggItem.setBizType(0);
                    aggItem.setBizTypeCaption("现金订单");
                    temp.add(aggItem);
                }
            }

            mCashQuotaInfo.setOrderPayWays(temp);

            refresh();
        } catch (Exception ex) {
            ZLogger.d("保存流水分析数据失败:" + ex.toString());
        }

    }

    /**
     * 流水分析,查询支付方式日结数据
     * TODO,加载等待窗口
     */
    private void listPayOrder() {
        onLoadProcess("正在查询流水分析数据");
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            onLoadError("统计失败，网络未连接，暂停查询日结流水分析数据。");
            return;
        }

        CashierApiImpl.listPayOrder(BizType.CASH_QUOTA, 2, payOrderRC);
    }

    NetCallBack.QueryRsCallBack payOrderRC = new NetCallBack.QueryRsCallBack<>(
            new NetProcessor.QueryRsProcessor<PayOrder>(new PageInfo(1, 20)) {
        @Override
        public void processQueryResult(RspQueryResult<PayOrder> rs) {
            //保存日结数据
            saveAccData(rs);

            onLoadFinished();
        }

        @Override
        protected void processFailure(Throwable t, String errMsg) {
            super.processFailure(t, errMsg);
            onLoadError("查询日结流水分析数据失败：" + errMsg);
        }
    }, PayOrder.class, CashierApp.getAppContext());

    /**
     * 保存流水分析数据
     */
    private void saveAccData(RspQueryResult<PayOrder> rs) {
        try {
            List<CashQuotOrderInfo> temp = mCashQuotaInfo.getPosOrders();
            if (temp == null){
                temp = new ArrayList<>();
            }

            if (rs != null && rs.getReturnNum() > 0) {
                for (EntityWrapper<PayOrder> wrapper : rs.getRowDatas()) {
                    PayOrder posOrder = wrapper.getBean();
                    CashQuotOrderInfo cashQuotaOrderInfo = new CashQuotOrderInfo();
                    cashQuotaOrderInfo.setAmount(posOrder.getTotalFee());
                    cashQuotaOrderInfo.setCreatedDate(posOrder.getUpdatedDate());
                    cashQuotaOrderInfo.setBizType(1);
                    cashQuotaOrderInfo.setBizTypeCaption("授权充值");
                    temp.add(cashQuotaOrderInfo);
                }
            }

            mCashQuotaInfo.setPosOrders(temp);

            refresh();
        } catch (Exception ex) {
            ZLogger.d("保存流水分析数据失败:" + ex.toString());
        }
    }

    /**
     * 针对当前用户所属网点提交营业现金，并触发一次日结操作
     */
    @OnClick(R.id.fab_print)
    public void commitCash() {
        final QuickPayInfo quickPayInfo = new QuickPayInfo();
        quickPayInfo.setBizType(BizType.CASH_QUOTA);
        quickPayInfo.setPayType(WayType.ALI_F2F);
        quickPayInfo.setSubject("提交营业现金");
        quickPayInfo.setBody("为了不影响您使用POS设备，请尽快充值！");
        if (!BizConfig.RELEASE){
            quickPayInfo.setAmount(100D);
            quickPayInfo.setMinAmount(0.01D);
        }
        else{
            quickPayInfo.setAmount(0.01D);
            quickPayInfo.setMinAmount(0.01D);
        }

        ZLogger.df(String.format(">>>准备提交营业现金: %s", JSONObject.toJSONString(quickPayInfo)));

        if (alipayDialog == null) {
            alipayDialog = new AlipayDialog(getActivity());
            alipayDialog.setCancelable(false);
            alipayDialog.setCanceledOnTouchOutside(false);
        }
        alipayDialog.initialize(quickPayInfo, true, new AlipayDialog.DialogClickListener() {
            @Override
            public void onPaySucceed(QuickPayInfo mQuickPayInfo, String outTradeNo) {
                PrintManager.printTopupReceipt(quickPayInfo, outTradeNo);

                UploadSyncManager.getInstance().sync();

                Calendar trigger = Calendar.getInstance();
                //第二天凌晨2点钟
                trigger.add(Calendar.DAY_OF_MONTH, 1);
                trigger.set(Calendar.HOUR_OF_DAY, 2);
                trigger.set(Calendar.MINUTE, 2);
                trigger.set(Calendar.SECOND, 0);
                AlarmManagerHelper.registerDailysettle(CashierApp.getAppContext(), trigger);
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
