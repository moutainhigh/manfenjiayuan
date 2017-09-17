package com.mfh.litecashier.ui.fragment.dailysettle;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.cashier.hardware.printer.PrinterFactory;
import com.bingshanguxue.cashier.model.wrapper.HandOverBill;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.analysis.AccItem;
import com.mfh.framework.api.analysis.AggItem;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.entity.MEntityWrapper;
import com.mfh.framework.rxapi.http.AnalysisHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.AnalysisOrderAdapter;
import com.mfh.litecashier.utils.AnalysisHelper;
import com.mfh.litecashier.utils.SharedPreferencesUltimate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;

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
    @BindView(R.id.tv_originAmount)
    TextView tvOrigin;
    @BindView(R.id.tv_turnover)
    TextView tvTurnover;
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
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
//        }

        tvHeaderTitle.setText("交接班");
        initAggRecyclerView();
        initAccRecyclerView();

        refresh();

        btnSubmit.setEnabled(false);

        autoShiftAnalyasis();
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
        if (handOverBill != null) {
            String cursor = TimeUtil.format(handOverBill.getEndDate(),
                    TimeCursor.FORMAT_YYYYMMDDHHMMSS);
            SharedPreferencesUltimate.set(SharedPreferencesUltimate.PK_LAST_HANDOVER_DATETIME, cursor);

            SharedPreferencesUltimate.setLastHandoverShiftId(handOverBill.getShiftId());
            //打印交接单单据
            PrinterFactory.getPrinterManager().printHandoverBill(handOverBill);
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

        tvHeaderTitle.setText(String.format("交接班 (第 %d 班)", handOverBill.getShiftId()));
        tvOfficeName.setText(String.format("门店：%s", handOverBill.getOfficeName()));
        tvHumanName.setText(String.format("帐号：%s", handOverBill.getHumanName()));
        tvHandoverDateTime.setText(String.format("交班时间：%s",
                TimeCursor.InnerFormat.format(handOverBill.getEndDate())));
        tvOrigin.setText(String.format("原价金额：%.2f", handOverBill.getOrigionAmount()));
        tvTurnover.setText(String.format("营业额：%.2f", handOverBill.getTurnover()));
        tvIncome.setText(String.format("账户新增：%.2f", handOverBill.getTurnover() - handOverBill.getCash()));
        tvCash.setText(String.format("现金收取：%.2f", handOverBill.getCash()));

        //显示数据
        if (aggListAdapter != null) {
            aggListAdapter.setEntityList(AnalysisHelper.wrapperAggItems(handOverBill.getAggItems()));
        }

        //显示数据
        if (accListAdapter != null) {
            accListAdapter.setEntityList(AnalysisHelper.wrapperAccItems(handOverBill.getAccItems()));
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

        Map<String, String> options = new HashMap<>();
        options.put("shiftId", String.valueOf(handOverBill.getShiftId()));
        options.put("startTime", TimeUtil.format(handOverBill.getStartDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS));
        options.put("endTime", TimeUtil.format(handOverBill.getEndDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        AnalysisHttpManager.getInstance().autoShiftAnalysis(options,
                new Subscriber<String>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        onLoadError(e.getMessage());
                        btnSubmit.setEnabled(false);
                    }

                    @Override
                    public void onNext(String s) {
                        ZLogger.d("启动交接班统计成功:" + s);
                        //TODO,开始查询统计数据
                        analysisAggShift();
                    }
                });
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

        Map<String, String> options = new HashMap<>();
        options.put("wrapper", "true");
        options.put("shiftId", String.valueOf(handOverBill.getShiftId()));
        options.put("aggDate", TimeUtil.format(handOverBill.getStartDate(), TimeUtil.FORMAT_YYYYMMDD));
        options.put("createdBy", String.valueOf(MfhLoginService.get().getHumanId()));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AnalysisHttpManager.getInstance().analysisAggShiftList(options,
                new MQuerySubscriber<MEntityWrapper<AggItem>>(new PageInfo(1, 50)) {

                    @Override
                    public void onError(Throwable e) {
                        onLoadError(e.getMessage());
                        btnSubmit.setEnabled(false);
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<MEntityWrapper<AggItem>> dataList) {
                        super.onQueryNext(pageInfo, dataList);

                        //保存日结数据
                        saveAggData2(dataList);

                        //查询经营分析数据
                        accAnalysisAggShift();
                    }
                });
    }

    private void saveAggData2(List<MEntityWrapper<AggItem>> dataList) {
        try {
            Double turnOver = 0D;
            Double origionAmount = 0D;

            List<AggItem> aggItems = new ArrayList<>();

            if (dataList != null && dataList.size() > 0) {
                for (MEntityWrapper<AggItem> entityWrapper : dataList) {
                    AggItem aggItem = entityWrapper.getBean();
                    Map<String, String> caption = entityWrapper.getCaption();
                    aggItem.setBizTypeCaption(caption.get("bizType"));
                    aggItem.setSubTypeCaption(caption.get("subType"));
                    aggItems.add(aggItem);

                    turnOver += aggItem.getTurnover();
                    origionAmount += aggItem.getOrigionAmount();
                }
            }
            handOverBill.setTurnover(turnOver);
            handOverBill.setOrigionAmount(origionAmount);
            handOverBill.setAggItems(aggItems);
            ZLogger.d(String.format("保存经营分析数据:\n%s", JSON.toJSONString(handOverBill)));

            refresh();
        } catch (Exception ex) {
            ZLogger.ef("保存流水分析数据失败:" + ex.toString());
        }
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

        Map<String, String> options = new HashMap<>();
        options.put("wrapper", "true");
        options.put("shiftId", String.valueOf(handOverBill.getShiftId()));
        options.put("aggDate", TimeUtil.format(handOverBill.getStartDate(), TimeUtil.FORMAT_YYYYMMDD));
        options.put("createdBy", String.valueOf(MfhLoginService.get().getHumanId()));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        AnalysisHttpManager.getInstance().accAnalysisAggShiftList(options,
                new MQuerySubscriber<MEntityWrapper<AccItem>>(new PageInfo(1, 50)) {

                    @Override
                    public void onError(Throwable e) {
                        onLoadError(e.getMessage());
                        btnSubmit.setEnabled(false);
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<MEntityWrapper<AccItem>> dataList) {
                        super.onQueryNext(pageInfo, dataList);

                        saveAccData2(dataList);

                        onLoadFinished();
                    }
                });
    }

    private void saveAccData2(List<MEntityWrapper<AccItem>> dataList) {
        try {
            Double cash = 0D;
            List<AccItem> accItems = new ArrayList<>();
            if (dataList != null && dataList.size() > 0) {
                for (MEntityWrapper<AccItem> wrapper : dataList) {
                    AccItem accItem = wrapper.getBean();
                    Map<String, String> caption = wrapper.getCaption();
                    accItem.setPayTypeCaption(caption.get("payType"));
                    accItems.add(accItem);

                    if (accItem.getPayType().equals(WayType.CASH)) {
                        cash = accItem.getAmount();
                    }
                }
            }

            handOverBill.setAccItems(accItems);
            handOverBill.setCash(cash);

            ZLogger.d(String.format("保存流水分析数据:\n%s", JSON.toJSONString(handOverBill)));

            refresh();
        } catch (Exception ex) {
            ZLogger.ef("保存流水分析数据失败:" + ex.toString());
        }
    }

}
