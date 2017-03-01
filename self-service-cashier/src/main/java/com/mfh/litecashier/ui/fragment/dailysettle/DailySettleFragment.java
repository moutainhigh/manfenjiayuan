package com.mfh.litecashier.ui.fragment.dailysettle;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.cashier.hardware.printer.PrinterFactory;
import com.bingshanguxue.cashier.model.wrapper.DailysettleInfo;
import com.bingshanguxue.vector_uikit.OptionalLabel;
import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.analysis.AccItem;
import com.mfh.framework.api.analysis.AggItem;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.entity.MEntityWrapper;
import com.mfh.framework.rxapi.http.RxHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.AggAnalysisOrderAdapter;
import com.mfh.litecashier.ui.adapter.AnalysisOrderAdapter;
import com.mfh.litecashier.ui.dialog.MyDatePickerDialog;
import com.mfh.litecashier.utils.AnalysisHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Subscriber;

/**
 * <h1>日结</h1>
 * <p>首先判断是否已经日结过:<br>
 * 1.如果已经日结过,则不需要启动日结统计，可以直接查询统计数据。最后也不需要进行日结确认。<br>
 * 2.如果未日结，则需要启动日结统计，然后再查询统计数据，最后需要确认日结操作。</p>
 * <p/>
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class DailySettleFragment extends BaseProgressFragment {
    @BindView(R.id.tv_header_title)
    TextView tvHeaderTitle;

    @BindView(R.id.tv_officename)
    TextView tvOfficeName;
    @BindView(R.id.tv_humanName)
    TextView tvHumanName;
    @BindView(R.id.label_date)
    OptionalLabel labelDate;
    @BindView(R.id.tv_amount)
    TextView tvAmount;
    @BindView(R.id.tv_not_cash)
    TextView tvNotCash;
    @BindView(R.id.tv_cash)
    TextView tvCash;

    @BindView(R.id.order_list)
    RecyclerViewEmptySupport aggRecyclerView;
    private AggAnalysisOrderAdapter aggListAdapter;
    @BindView(R.id.empty_view)
    TextView emptyView;

    @BindView(R.id.paytype_order_list)
    RecyclerViewEmptySupport accRecyclerView;
    private AnalysisOrderAdapter accListAdapter;
    @BindView(R.id.paytype_empty_view)
    TextView payTypeEmptyView;

    @BindView(R.id.button_header_close)
    ImageButton btnClose;
    @BindView(R.id.fab_print)
    ImageButton fabPrint;

    private DailysettleInfo mDailysettleInfo;
    private MyDatePickerDialog dateTimePickerDialog = null;

    public static DailySettleFragment newInstance(Bundle args) {
        DailySettleFragment fragment = new DailySettleFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_components_dailysettle;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        ZLogger.df(String.format(">>开始日结：%s", StringUtils.decodeBundle(args)));
//        if (args != null) {
//            dailySettleDatetime = args.getString(EXTRA_KEY_DATETIME);
//        }

        tvHeaderTitle.setText("统计");
        initAggRecyclerView();
        initAccRecyclerView();

        mDailysettleInfo = AnalysisHelper.createDailysettle(new Date());
        refresh();

        autoDateEnd();
    }

    @OnClick(R.id.button_header_close)
    public void finishActivity() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    /**
     * 打印订单
     */
    @OnClick(R.id.fab_print)
    public void printOrder() {
        PrinterFactory.getPrinterManager().printDailySettleReceipt(mDailysettleInfo);
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
        aggListAdapter = new AggAnalysisOrderAdapter(CashierApp.getAppContext(), null);
        aggListAdapter.setOnAdapterListener(new AggAnalysisOrderAdapter.OnAdapterListener() {

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
        try {
            if (mDailysettleInfo == null) {
                ZLogger.d("日结单创建失败");
                fabPrint.setVisibility(View.GONE);
            } else {
                fabPrint.setVisibility(View.VISIBLE);

                tvOfficeName.setText(String.format("门店：%s",
                        mDailysettleInfo.getOfficeName()));
                tvHumanName.setText(String.format("结算人：%s",
                        mDailysettleInfo.getHumanName()));
                labelDate.setLabelText(TimeUtil.format(mDailysettleInfo.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDD));

                Double turnover = mDailysettleInfo.getTurnOver();
                tvAmount.setText(String.format("营业额合计：%.2f",
                        turnover));
                tvNotCash.setText(String.format("非现金收取：%.2f",
                        mDailysettleInfo.getTurnOver() - mDailysettleInfo.getCash()));
                tvCash.setText(String.format("现金收取：%.2f",
                        mDailysettleInfo.getCash()));

                //显示经营数据
                if (aggListAdapter != null) {
                    aggListAdapter.setEntityList(AnalysisHelper.getAggItemsWrapper(mDailysettleInfo));
                }

                //显示流水分析数据
                if (accListAdapter != null) {
                    accListAdapter.setEntityList(AnalysisHelper.getAccItemsWrapper(mDailysettleInfo));
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

    @OnClick(R.id.label_date)
    public void changeDate() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

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

                mDailysettleInfo = AnalysisHelper.createDailysettle(calendar.getTime());
                refresh();
                autoDateEnd();
            }
        });
        if (!dateTimePickerDialog.isShowing()) {
            dateTimePickerDialog.show();
        }
    }


    /**
     * 启动日结统计
     */
    private void autoDateEnd() {
        onLoadProcess("正在统计日结数据");

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onLoadError("统计失败，网络未连接，请重新日结。");
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("date", TimeUtil.format(mDailysettleInfo.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDD));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());
        RxHttpManager.getInstance().autoDateEnd(options,
                new Subscriber<String>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        onLoadError("启动日结统计失败：" + e.toString());
                    }

                    @Override
                    public void onNext(String s) {
                        ZLogger.d("启动日结统计成功:" + s);
                        //TODO,开始查询统计数据
                        analysisAggShift();
                    }
                });
    }

    /**
     * 经营分析,查询业务类型日结数据
     */
    private void analysisAggShift() {
        onLoadProcess("正在查询经营分析数据...");

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onLoadError("网络未连接，暂停查询日结经营分析数据!");
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("wrapper", "true");
        options.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        options.put("aggDate", TimeUtil.format(mDailysettleInfo.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDD));
//        params.put("createdBy", MfhLoginService.get().getCurrentGuId());
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());


        RxHttpManager.getInstance().analysisAggDateList(options,
                new MQuerySubscriber<MEntityWrapper<AggItem>>(new PageInfo(1, 50)) {

                    @Override
                    public void onError(Throwable e) {
                        onLoadError("查询日结经营分析数据失败：" + e.toString());
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<MEntityWrapper<AggItem>> dataList) {
                        super.onQueryNext(pageInfo, dataList);

                        //保存日结数据
                        saveAggData2(dataList);

                        //查询支付类型数据
                        analysisAccDateList();
                    }
                });
    }


    /**
     * 保存经营分析数据
     */
    private void saveAggData2(List<MEntityWrapper<AggItem>> dataList) {
        try {
            Double turnOver = 0D;
            Double grossProfit = 0D;
            List<AggItem> aggItems = new ArrayList<>();

            if (dataList != null && dataList.size() > 0){
                for (MEntityWrapper<AggItem> entityWrapper : dataList){
                    AggItem aggItem = entityWrapper.getBean();
                    Map<String, String> caption = entityWrapper.getCaption();
                    aggItem.setBizTypeCaption(caption.get("bizType"));
                    aggItem.setSubTypeCaption(caption.get("subType"));
                    aggItems.add(entityWrapper.getBean());

                    turnOver += aggItem.getTurnover();
                    grossProfit += aggItem.getGrossProfit();
                }
            }
            mDailysettleInfo.setAggItems(aggItems);
            mDailysettleInfo.setTurnOver(turnOver);
            mDailysettleInfo.setGrossProfit(grossProfit);
            mDailysettleInfo.setUpdatedDate(new Date());
            ZLogger.df(String.format("保存经营分析数据:\n%s", JSON.toJSONString(mDailysettleInfo)));

            refresh();
        } catch (Exception ex) {
            ZLogger.d("保存流水分析数据失败:" + ex.toString());
        }

    }

    /**
     * 流水分析,查询支付方式日结数据
     * TODO,加载等待窗口
     */
    private void analysisAccDateList() {
        onLoadProcess("正在查询流水分析数据");
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            onLoadError("统计失败，网络未连接，暂停查询日结流水分析数据。");
            return;
        }

        Map<String, String> options = new HashMap<>();
        options.put("wrapper", "true");
        options.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        options.put("aggDate", TimeUtil.format(mDailysettleInfo.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDD));
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        RxHttpManager.getInstance().analysisAccDateList(options,
                new MQuerySubscriber<MEntityWrapper<AccItem>>(new PageInfo(1, 50)) {

                    @Override
                    public void onError(Throwable e) {
                        onLoadError("查询日结流水分析数据失败：" + e.toString());
                    }

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<MEntityWrapper<AccItem>> dataList) {
                        super.onQueryNext(pageInfo, dataList);

                        saveAccData2(dataList);

                        onLoadFinished();
                    }
                });
    }


    /**
     * 保存流水分析数据
     */
    private void saveAccData(RspQueryResult<AccItem> rs) {
        try {
            Double cash = 0D;
            List<AccItem> accItems = new ArrayList<>();
            if (rs != null && rs.getReturnNum() > 0) {
                for (EntityWrapper<AccItem> wrapper : rs.getRowDatas()) {
                    AccItem accItem = wrapper.getBean();
                    accItem.setPayTypeCaption(wrapper.getPropCaption("payType"));
                    accItems.add(accItem);

                    if (accItem.getPayType().equals(WayType.CASH)) {
                        cash = accItem.getAmount();
                    }
                }
            }

            mDailysettleInfo.setAccItems(accItems);
            mDailysettleInfo.setCash(cash);
            mDailysettleInfo.setUpdatedDate(new Date());

            ZLogger.df(String.format("保存流水分析数据:\n%s", JSON.toJSONString(mDailysettleInfo)));

            refresh();
        } catch (Exception ex) {
            ZLogger.d("保存流水分析数据失败:" + ex.toString());
        }
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

            mDailysettleInfo.setAccItems(accItems);
            mDailysettleInfo.setCash(cash);
            mDailysettleInfo.setUpdatedDate(new Date());

            ZLogger.df(String.format("保存流水分析数据:\n%s", JSON.toJSONString(mDailysettleInfo)));

            refresh();
        } catch (Exception ex) {
            ZLogger.d("保存流水分析数据失败:" + ex.toString());
        }
    }
}
