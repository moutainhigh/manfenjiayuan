package com.mfh.litecashier.ui.fragment.dailysettle;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bingshanguxue.cashier.hardware.printer.PrinterAgent;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.analysis.AnalysisApiImpl;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.AccItem;
import com.mfh.litecashier.bean.AggItem;
import com.mfh.litecashier.com.EmbPrintManagerImpl;
import com.mfh.litecashier.com.PrintManagerImpl;
import com.mfh.litecashier.ui.adapter.AnalysisOrderAdapter;
import com.mfh.litecashier.utils.AnalysisHelper;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * <h>交接班</h><br>
 * {@link HandOverBill}<br>
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class HandoverFragment extends BaseProgressFragment {
    @BindView(R.id.tv_header_title)
    TextView tvHeaderTitle;
    @BindView(R.id.tv_officename)
    TextView tvOfficeName;
    @BindView(R.id.tv_humanName)
    TextView tvHumanName;
    @BindView(R.id.tv_handover_datetime)
    TextView tvHandoverDateTime;
    @BindView(R.id.tv_amount)
    TextView tvAmount;
    @BindView(R.id.tv_income)
    TextView tvIncome;
    @BindView(R.id.tv_cash)
    TextView tvCash;

    @BindView(R.id.order_list)
    RecyclerViewEmptySupport aggRecyclerView;
    private AnalysisOrderAdapter aggListAdapter;
    @BindView(R.id.empty_view)
    TextView emptyView;

    @BindView(R.id.paytype_order_list)
    RecyclerViewEmptySupport accRecyclerView;
    private AnalysisOrderAdapter accListAdapter;
    @BindView(R.id.paytype_empty_view)
    TextView payTypeEmptyView;

    @BindView(R.id.button_footer_positive)
    Button btnSubmit;

    private HandOverBill handOverBill = null;

    public static HandoverFragment newInstance(Bundle args) {
        HandoverFragment fragment = new HandoverFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_components_handover;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
//        }

        tvHeaderTitle.setText("交接班");
        initAggRecyclerView();
        initAccRecyclerView();

        refresh();

        btnSubmit.setEnabled(false);
        //TODO,先提交本地订单再进行日结
//        OrderSyncManager.get().sync();

        autoShiftAnalyasis();
//        DialogUtil.showHint("开发君失踪了...");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.button_header_close)
    public void finishActivity() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    /**
     * 确定交接班
     */
    @OnClick(R.id.button_footer_positive)
    public void doConfirmHandover() {
        // 保存交接班时间和班次
        String cursor = TimeUtil.format(handOverBill.getEndDate(),
                TimeCursor.FORMAT_YYYYMMDDHHMM);
        SharedPreferencesUltimate.set(SharedPreferencesUltimate.PK_LAST_HANDOVER_DATETIME, cursor);

        SharedPreferencesUltimate.setLastHandoverShiftId(handOverBill.getShiftId());
        //打印交接单单据
        if (PrinterAgent.getPrinterType() == PrinterAgent.PRINTER_TYPE_COMMON){
            PrintManagerImpl.printHandoverBill(handOverBill);
        }
        else{
            EmbPrintManagerImpl.printHandoverBill(handOverBill);
        }

        getActivity().setResult(Activity.RESULT_OK);
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
        aggListAdapter = new AnalysisOrderAdapter(CashierApp.getAppContext(), null);
        aggListAdapter.setOnAdapterListener(new AnalysisOrderAdapter.OnAdapterListener() {

                                                @Override
                                                public void onDataSetChanged() {
//                                                      onLoadFinished();
                                                }
                                            }

        );
        aggRecyclerView.setAdapter(aggListAdapter);
    }

    private void initAccRecyclerView() {
        LinearLayoutManager mRLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
        mRLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        accRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        accRecyclerView.setHasFixedSize(true);
//        //添加分割线
        accRecyclerView.addItemDecoration(new LineItemDecoration(
                getActivity(), LineItemDecoration.VERTICAL_LIST));
        //设置列表为空时显示的视图
        accRecyclerView.setEmptyView(payTypeEmptyView);
        accListAdapter = new AnalysisOrderAdapter(CashierApp.getAppContext(), null);
        accListAdapter.setOnAdapterListener(new AnalysisOrderAdapter.OnAdapterListener() {

                                                  @Override
                                                  public void onDataSetChanged() {
//                                                      onLoadFinished();
                                                  }
                                              }

        );
        accRecyclerView.setAdapter(accListAdapter);
    }

    /**
     * 刷新数据
     */
    private void refresh() {
        if (handOverBill == null) {
            handOverBill = AnalysisHelper.createHandOverBill();
        }

        tvHeaderTitle.setText(String.format("交接班 (班次：%d)", handOverBill.getShiftId()));
        tvOfficeName.setText(String.format("门店：%s", handOverBill.getOfficeName()));
        tvHumanName.setText(String.format("交班人：%s", handOverBill.getHumanName()));
        tvHandoverDateTime.setText(String.format("交班时间：%s",
                TimeCursor.InnerFormat.format(handOverBill.getEndDate())));
        tvAmount.setText(String.format("营业额合计：%.2f", handOverBill.getAmount()));
        tvIncome.setText(String.format("账户新增：%.2f", handOverBill.getAmount() - handOverBill.getCash()));
        tvCash.setText(String.format("现金收取：%.2f", handOverBill.getCash()));

        //显示数据
        if (aggListAdapter != null) {
            aggListAdapter.setEntityList(AnalysisHelper.getAggItemsWrapper(handOverBill.getAggItems()));
        }

        //显示数据
        if (accListAdapter != null) {
            accListAdapter.setEntityList(AnalysisHelper.getAccAnalysisList(handOverBill.getAccItems()));
        }

    }

    @Override
    public void onLoadProcess(String description) {
        super.onLoadProcess(description);
        btnSubmit.setEnabled(false);
    }

    /**
     * 加载完成
     */
    @Override
    public void onLoadFinished() {
        super.onLoadFinished();
        btnSubmit.setEnabled(true);
    }

//    public void onEventMainThread(OrderSyncManager.OrderSyncManagerEvent event) {
//        //有新订单
//        if (event.getEventId() == OrderSyncManager.OrderSyncManagerEvent.EVENT_ID_SYNC_DATA_PROCESS) {
//            onLoadProcess("正在同步订单流水");
//        } else if (event.getEventId() == OrderSyncManager.OrderSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED) {
//            autoShiftAnalyasis();
//        } else if (event.getEventId() == OrderSyncManager.OrderSyncManagerEvent.EVENT_ID_SYNC_DATA_FAILED) {
//            DialogUtil.showHint("同步订单流水失败");
//            autoShiftAnalyasis();
//        }
//    }

    /**
     * 启动交接班统计
     */
    private void autoShiftAnalyasis() {
        onLoadProcess("正在统计交接班数据");

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onLoadError("网络未连接，暂停启动交接班统计。");
            btnSubmit.setEnabled(false);
            return;
        }

        //回调
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        ZLogger.d("启动交接班统计成功:" + retValue.getValue());
//                            btnSubmit.setEnabled(true);
                        //TODO,开始查询统计数据
                        analysisAggShift();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //{"code":"1","msg":"交接班正在统计中，请稍候...","version":"1","data":null}
                        onLoadError("启动交接班统计失败：" + errMsg);
                        btnSubmit.setEnabled(false);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        AnalysisApiImpl.autoShiftAnalysic(handOverBill.getShiftId(),
                TimeCursor.InnerFormat.format(handOverBill.getStartDate()),
                TimeCursor.InnerFormat.format(handOverBill.getEndDate()), responseCallback);
    }

    /**
     * 经营分析,查询业务类型日结数据
     */
    private void analysisAggShift() {
        onLoadProcess("正在查询经营分析数据");

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onLoadError("网络未连接，暂停查询交接班经营分析数据。");
            btnSubmit.setEnabled(false);
            return;
        }

        NetCallBack.QueryRsCallBack responseCallback = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<AggItem>(new PageInfo(1, 20)) {
            @Override
            public void processQueryResult(RspQueryResult<AggItem> rs) {
                //保存日结数据
                saveAggData(rs);

                //查询经营分析数据
                accAnalysisAggShift();
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                onLoadError("查询日结经营分析数据失败:" + errMsg);
                btnSubmit.setEnabled(false);
            }
        }, AggItem.class, CashierApp.getAppContext());

        AnalysisApiImpl.analysisAggShift(handOverBill.getShiftId(),
                TimeCursor.FORMAT_YYYYMMDD.format(handOverBill.getStartDate()), responseCallback);
    }

    private void saveAggData(RspQueryResult<AggItem> rs){
        List<AggItem> entityList = new ArrayList<>();
        if (rs != null && rs.getReturnNum() > 0) {
            for (EntityWrapper<AggItem> wrapper : rs.getRowDatas()) {
                AggItem aggItem = wrapper.getBean();
                aggItem.setBizTypeCaption(wrapper.getPropCaption("bizType"));
                aggItem.setSubTypeCaption(wrapper.getPropCaption("subType"));
                entityList.add(wrapper.getBean());
            }
        }
        handOverBill.setAggItems(entityList);

        refresh();
    }

    /**
     * 查询交接班流水分析数据
     */
    private void accAnalysisAggShift() {
        onLoadProcess("正在查询流水分析数据");

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onLoadError("网络未连接，查询交接班流水分析数据。");
            btnSubmit.setEnabled(false);
            return;
        }

        NetCallBack.QueryRsCallBack responseCallback = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<AccItem>(new PageInfo(1, 20)) {
            @Override
            public void processQueryResult(RspQueryResult<AccItem> rs) {
                saveAccData(rs);

                onLoadFinished();
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                onLoadError("查询交接班流水分析数据失败:" + errMsg);
                btnSubmit.setEnabled(false);
            }
        }, AccItem.class, CashierApp.getAppContext());

        AnalysisApiImpl.accAnalysisAggShift(handOverBill.getShiftId(),
                TimeCursor.FORMAT_YYYYMMDD.format(handOverBill.getStartDate()), responseCallback);
    }

    private void saveAccData(RspQueryResult<AccItem> rs){
        ZLogger.d("保存交接班流水分析数据");
        Double cash = 0D;
        List<AccItem> entityList = new ArrayList<>();
        if (rs != null && rs.getReturnNum() > 0) {
            for (EntityWrapper<AccItem> wrapper : rs.getRowDatas()) {
                AccItem accItem = wrapper.getBean();
                accItem.setPayTypeCaption(wrapper.getPropCaption("payType"));
                entityList.add(accItem);
                if (accItem.getPayType().equals(WayType.CASH)){
                    cash = accItem.getAmount();
                }
            }
        }
        handOverBill.setCash(cash);
        handOverBill.setAccItems(entityList);

        refresh();
    }

}
