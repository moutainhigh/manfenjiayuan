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
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mfh.comn.bean.EntityWrapper;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.comn.net.data.RspValue;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.api.impl.CashierApiImpl;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.DailysettleAccItem;
import com.mfh.litecashier.bean.DailysettleAggItem;
import com.mfh.litecashier.bean.wrapper.AccWrapper;
import com.mfh.litecashier.bean.wrapper.AggWrapper;
import com.mfh.litecashier.bean.wrapper.CashierOrderInfo;
import com.mfh.litecashier.com.SerialManager;
import com.mfh.litecashier.database.entity.DailysettleEntity;
import com.mfh.litecashier.database.entity.PosOrderPayEntity;
import com.mfh.litecashier.database.logic.DailysettleService;
import com.mfh.litecashier.database.logic.PosOrderPayService;
import com.mfh.litecashier.service.OrderSyncManager;
import com.mfh.litecashier.ui.adapter.AnalysisOrderAdapter;
import com.mfh.litecashier.ui.dialog.AlipayDialog;
import com.mfh.litecashier.utils.AnalysisHelper;
import com.mfh.litecashier.utils.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * <h1>日结</h1>
 * <p>首先判断是否已经日结过:<br>
 * 1.如果已经日结过,则不需要启动日结统计，可以直接查询统计数据。最后也不需要进行日结确认。<br>
 * 2.如果未日结，则需要启动日结统计，然后再查询统计数据，最后需要确认日结操作。</p>
 * <p/>
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class DailySettleFragment extends BaseProgressFragment {

    public static final String EXTRA_KEY_CANCELABLE = "cancelable";
    public static final String EXTRA_KEY_DATETIME = "datetime";

    @Bind(R.id.tv_header_title)
    TextView tvHeaderTitle;

    @Bind(R.id.tv_officename)
    TextView tvOfficeName;
    @Bind(R.id.tv_humanName)
    TextView tvHumanName;
    @Bind(R.id.tv_date)
    TextView tvDate;
    @Bind(R.id.tv_amount)
    TextView tvAmount;
    @Bind(R.id.tv_rate)
    TextView tvRate;
    @Bind(R.id.tv_compensation)
    TextView tvCompensation;
    @Bind(R.id.tv_not_cash)
    TextView tvNotCash;
    @Bind(R.id.tv_cash)
    TextView tvCash;

    @Bind(R.id.order_list)
    RecyclerViewEmptySupport aggRecyclerView;
    private AnalysisOrderAdapter aggListAdapter;
    @Bind(R.id.empty_view)
    TextView emptyView;

    @Bind(R.id.paytype_order_list)
    RecyclerViewEmptySupport accRecyclerView;
    private AnalysisOrderAdapter accListAdapter;
    @Bind(R.id.paytype_empty_view)
    TextView payTypeEmptyView;

    @Bind(R.id.button_header_close)
    ImageButton btnClose;
    @Bind(R.id.button_footer_positive)
    Button btnConfirm;

    private boolean cancelable = true;//是否可以关闭窗口
    private String dailySettleDatetime = null;//日结日期
    private DailysettleEntity dailysettleEntity = null;

    private AlipayDialog alipayDialog = null;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            cancelable = args.getBoolean(EXTRA_KEY_CANCELABLE, true);
            dailySettleDatetime = args.getString(EXTRA_KEY_DATETIME);
        }
        ZLogger.df(String.format("Dailysettle--cancelable＝%b, dailySettleDatetime=%s", cancelable, dailySettleDatetime));

        tvHeaderTitle.setText("日结");
        initAggRecyclerView();
        initAccRecyclerView();

        if (cancelable) {
            btnClose.setVisibility(View.VISIBLE);
        } else {
            btnClose.setVisibility(View.INVISIBLE);
        }

        refresh();

        btnConfirm.setEnabled(false);
        if (dailysettleEntity == null) {
            ZLogger.d("日结单创建失败");

            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        }
        //判断是否进行过日结，如果已经进行过日结，则不需要尽
        else if (dailysettleEntity.getConfirmStatus() == DailysettleEntity.CONFIRM_STATUS_YES) {
            DialogUtil.showHint("该网点已经日结过！");
            ZLogger.d("已经日结过，不需要再进行日结");
        } else {
            //TODO,先提交本地订单再进行日结
            OrderSyncManager.get().sync();

            //TODO 判断是否已经支付过,如果支付过则判断是否,由于支付状态只保存在本地，不能同步多台POS机，所以暂时不考虑。
        }

//        DialogUtil.showHint("开发君失踪了...");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
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

    /**
     * 日结确认(支付成功后才能确认)
     */
    @OnClick(R.id.button_footer_positive)
    public void doConfirmDailySettle() {
        btnConfirm.setEnabled(false);
        //现金收取金额
        Double cashAmount = dailysettleEntity.getAccCashAmount();
        if (cashAmount.compareTo(0D) <= 0) {
            ZLogger.df(String.format("日结确认－－现金收取金额为%.2f,不需要支付。", cashAmount));

            //确认日结单
            analysisAcDateDoEnd(null);
            return;
        } else {
            //TODO 判断是否已经支付过，如果已支付金额大于现金收取金额，则认为已经支付过，不需要支付
        }

        //设备号_支付业务类型号_本地数据库编号
//        String orderid = String.format("%s_%d_%d", SharedPreferencesManager.getTerminalId(), MUtils.PAY_BIZ_TYPE_DAILYSETTLE, dailysettleEntity.getId());
        String orderid = String.format("%s_%d", SharedPreferencesManager.getTerminalId(), dailysettleEntity.getId());

        CashierOrderInfo cashierOrderInfo = new CashierOrderInfo();
        cashierOrderInfo.initQuickPayment(BizType.DAILYSETTLE,
                "", orderid, "日结单支付", "", null, cashAmount, cashAmount);

        if (alipayDialog == null) {
            alipayDialog = new AlipayDialog(getActivity());
            alipayDialog.setCancelable(false);
            alipayDialog.setCanceledOnTouchOutside(false);
        }
        alipayDialog.init(cashierOrderInfo, new AlipayDialog.DialogClickListener() {
            @Override
            public void onPayProcess(Double amount, String outTradeNo) {
//                ZLogger.d("支付处理中");
                if (dailysettleEntity != null) {
                    dailysettleEntity.setPaystatus(DailysettleEntity.PAY_STATUS_PROCESS);
                    DailysettleService.get().saveOrUpdate(dailysettleEntity);

                    PosOrderPayService.get().pay(dailysettleEntity.getBarCode(),
                            WayType.ALI_F2F, outTradeNo,
                            amount, PosOrderPayEntity.PAY_STATUS_PROCESS, null);
                }
            }

            @Override
            public void onPaySucceed(Double amount, String outTradeNo) {
//                ZLogger.d("支付成功");
                if (dailysettleEntity != null) {
                    dailysettleEntity.setPaystatus(DailysettleEntity.PAY_STATUS_SUCCEED);
                    DailysettleService.get().saveOrUpdate(dailysettleEntity);

                    //保存支付记录
                    PosOrderPayService.get().pay(dailysettleEntity.getBarCode(),
                            WayType.ALI_F2F, outTradeNo,
                            amount, PosOrderPayEntity.PAY_STATUS_FINISH, null);
                }

                //确认日结单
                analysisAcDateDoEnd(outTradeNo);
            }

            @Override
            public void onPayException(Double amount, String outTradeNo) {
//                ZLogger.d("支付异常");
                if (dailysettleEntity != null) {
                    dailysettleEntity.setPaystatus(DailysettleEntity.PAY_STATUS_EXCEPTION);
                    DailysettleService.get().saveOrUpdate(dailysettleEntity);

                    PosOrderPayService.get().pay(dailysettleEntity.getBarCode(),
                            WayType.ALI_F2F, outTradeNo,
                            amount, PosOrderPayEntity.PAY_STATUS_EXCEPTION, null);
                }
            }

            @Override
            public void onPayFailed(Double amount, String outTradeNo) {
//                ZLogger.d("支付失败");
                if (dailysettleEntity != null) {
                    dailysettleEntity.setPaystatus(DailysettleEntity.PAY_STATUS_FAILED);
                    DailysettleService.get().saveOrUpdate(dailysettleEntity);

                    PosOrderPayService.get().pay(dailysettleEntity.getBarCode(),
                            WayType.ALI_F2F, outTradeNo,
                            amount, PosOrderPayEntity.PAY_STATUS_FAILED, null);
                }
            }

            @Override
            public void onPayCanceled() {
                btnConfirm.setEnabled(true);

//                if (dailysettleEntity != null) {
//                    dailysettleEntity.setPaystatus(DailysettleEntity.PAY_STATUS_FAILED);
//                    DailysettleService.get().saveOrUpdate(dailysettleEntity);
//
//                    PosOrderPayService.get().pay(dailysettleEntity.getBarCode(),
//                            WayType.ALI_F2F, outTradeNo,
//                            amount, PosOrderPayEntity.PAY_STATUS_FAILED, null);
//                }
            }

        });

        alipayDialog.show();
    }

    public void onEventMainThread(OrderSyncManager.OrderSyncManagerEvent event) {
        //有新订单
        if (event.getEventId() == OrderSyncManager.OrderSyncManagerEvent.EVENT_ID_SYNC_DATA_PROCESS) {
            onLoadProcess("正在同步订单流水");
        } else if (event.getEventId() == OrderSyncManager.OrderSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED) {
            autoDateEnd();
        } else if (event.getEventId() == OrderSyncManager.OrderSyncManagerEvent.EVENT_ID_SYNC_DATA_FAILED) {
            DialogUtil.showHint("同步订单流水失败");
            autoDateEnd();
        }
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
        try {
            if (dailysettleEntity == null) {
                dailysettleEntity = AnalysisHelper.createDailysettle(dailySettleDatetime);
            }

            if (dailysettleEntity == null) {
                ZLogger.d("日结单创建失败");
                return;
            }

//            ZLogger.d("显示门店");
            tvOfficeName.setText(String.format("门店：%s", dailysettleEntity.getOfficeName()));
//            ZLogger.d("显示结算人");
            tvHumanName.setText(String.format("结算人：%s", dailysettleEntity.getHumanName()));
//            ZLogger.d("显示日结时间");
            tvDate.setText(String.format("日结时间：%s",
                    (dailysettleEntity.getDailysettleDate() != null
                            ? TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(dailysettleEntity.getDailysettleDate())
                            : "")
            ));

//            ZLogger.d("显示营业额合计");
            Double turnover = dailysettleEntity.getTurnover();
            tvAmount.setText(String.format("营业额合计：%.2f", turnover));

//            ZLogger.d("计算毛利率");
            Double grossProfit = dailysettleEntity.getGrossProfit();
            Double grossMargin;
            if (grossProfit == 0D) {
                grossMargin = 0D;
            } else {
                if (turnover == 0D) {
                    grossMargin = Double.valueOf(String.valueOf(Integer.MAX_VALUE));
                } else {
                    grossMargin = grossProfit / turnover;
                }
            }
//            ZLogger.d("显示毛利率");
            tvRate.setText(String.format("毛利率：%.2f%%", 100 * grossMargin));
//            ZLogger.d("显示抵扣金额");
            tvCompensation.setText(String.format("抵扣金额：%.2f", 0D));
//            ZLogger.d("显示非现金收取");
            tvNotCash.setText(String.format("非现金收取：%.2f", dailysettleEntity.getTurnover() - dailysettleEntity.getAccCashAmount()));
//            ZLogger.d("显示现金收取");
            tvCash.setText(String.format("现金收取：%.2f", dailysettleEntity.getAccCashAmount()));

            //显示经营数据
//            ZLogger.d("显示经营数据");
            if (aggListAdapter != null) {
                aggListAdapter.setEntityList(AnalysisHelper.getAggItemsWrapper(dailysettleEntity));
            }

            //显示流水分析数据
//            ZLogger.d("显示流水分析");
            if (accListAdapter != null) {
                accListAdapter.setEntityList(AnalysisHelper.getAccItemsWrapper(dailysettleEntity));
            }

//            ZLogger.d("刷新日结数据成功");
        } catch (Exception ex) {
            ZLogger.e(String.format("刷新日结数据失败：%s", ex.toString()));
        }
    }

    @Override
    public void onLoadProcess(String description) {
        super.onLoadProcess(description);
        btnConfirm.setEnabled(false);
    }

    @Override
    public void onLoadFinished() {
        super.onLoadFinished();
        btnConfirm.setEnabled(true);
    }

    /**
     * 启动日结统计
     */
    private void autoDateEnd() {
        onLoadProcess("正在统计日结数据");

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            onLoadError("统计失败，网络未连接，请重新日结。");
            btnConfirm.setEnabled(false);
            return;
        }

        //回调
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":""}
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        ZLogger.d("启动日结统计成功:" + retValue.getValue());
//                            btnSubmit.setEnabled(true);
                        //TODO,开始查询统计数据
                        analysisAggShift();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //{"code":"1","msg":"指定网点已经日结过：132079","version":"1","data":null}
                        onLoadError("启动日结统计失败：" + errMsg);
                        btnConfirm.setEnabled(false);
                        //日结统计失败，仍然需要查询数据
//                        analysisAggShift();
//                        btnSubmit.setEnabled(true);
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.autoDateEnd(dailysettleEntity.getDailysettleDate(), responseCallback);
    }

    /**
     * 经营分析,查询业务类型日结数据
     */
    private void analysisAggShift() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            onLoadError("统计失败，网络未连接，暂停查询日结经营分析数据。");
            btnConfirm.setEnabled(false);
            return;
        }

        onLoadProcess("正在查询经营分析数据");
        NetCallBack.QueryRsCallBack responseCallback = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<DailysettleAggItem>(new PageInfo(1, 20)) {
            @Override
            public void processQueryResult(RspQueryResult<DailysettleAggItem> rs) {
                ZLogger.d("查询日结经营分析数据成功:");

                //保存日结数据
                saveAggData(rs);

                //查询支付类型数据
                analysisAccDateList();
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                onLoadError("查询日结经营分析数据失败：" + errMsg);
                btnConfirm.setEnabled(false);
//                saveAggData(null);
//                analysisAccDateList();
            }
        }, DailysettleAggItem.class, CashierApp.getAppContext());

        CashierApiImpl.analysisAggDateList(dailysettleEntity.getDailysettleDate(), responseCallback);
    }

    /**
     * 保存经营分析数据
     */
    private void saveAggData(RspQueryResult<DailysettleAggItem> rs) {
        try {
            List<DailysettleAggItem> entityList = new ArrayList<>();
            if (rs != null && rs.getReturnNum() > 0) {
                for (EntityWrapper<DailysettleAggItem> wrapper : rs.getRowDatas()) {
                    entityList.add(wrapper.getBean());
                }
            }
            AggWrapper aggWrapper = new AggWrapper();
            aggWrapper.initWithDailysettleAggItems(entityList);

            this.dailysettleEntity.setTurnover(aggWrapper.getTurnOver());
            this.dailysettleEntity.setGrossProfit(aggWrapper.getGrossProfit());
            this.dailysettleEntity.setAggPosAmount(aggWrapper.getPosAmount());
            this.dailysettleEntity.setAggPosOrderNum(aggWrapper.getPosQuantity());
            this.dailysettleEntity.setAccScAmount(aggWrapper.getScAmount());
            this.dailysettleEntity.setAccScOrderNum(aggWrapper.getScQuantity());
            this.dailysettleEntity.setAggLaundryAmount(aggWrapper.getLaundryAmount());
            this.dailysettleEntity.setAggLaundryOrderNum(aggWrapper.getLaundryQuantity());
            this.dailysettleEntity.setAggPijuAmount(aggWrapper.getPijuAmount());
            this.dailysettleEntity.setAggPijuOrderNum(aggWrapper.getPijuQuantity());
            this.dailysettleEntity.setAggCourierAmount(aggWrapper.getCourierAmount());
            this.dailysettleEntity.setAggCourierOrderNum(aggWrapper.getCourierQuantity());
            this.dailysettleEntity.setAggExpressAmount(aggWrapper.getScAmount());
            this.dailysettleEntity.setAggExpressOrderNum(aggWrapper.getScQuantity());
            this.dailysettleEntity.setAggRechargeAmount(aggWrapper.getRechargeAmount());
            this.dailysettleEntity.setAggRechargeOrderNum(aggWrapper.getRechargeQuantity());
            DailysettleService.get().saveOrUpdate(this.dailysettleEntity);

            refresh();
        } catch (Exception ex) {
            ZLogger.d("日结--保存流水分析数据失败:" + ex.toString());
        }

    }

    /**
     * 流水分析,查询支付方式日结数据
     * TODO,加载等待窗口
     */
    private void analysisAccDateList() {
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            onLoadError("统计失败，网络未连接，暂停查询日结流水分析数据。");
            btnConfirm.setEnabled(false);
            return;
        }

        NetCallBack.QueryRsCallBack responseCallback = new NetCallBack.QueryRsCallBack<>(new NetProcessor.QueryRsProcessor<DailysettleAccItem>(new PageInfo(1, 20)) {
            @Override
            public void processQueryResult(RspQueryResult<DailysettleAccItem> rs) {
                //保存日结数据
                saveAccData(rs);

                onLoadFinished();
            }

            @Override
            protected void processFailure(Throwable t, String errMsg) {
                super.processFailure(t, errMsg);
                onLoadError("查询日结流水分析数据失败：" + errMsg);
                btnConfirm.setEnabled(false);
            }
        }, DailysettleAccItem.class, CashierApp.getAppContext());

        onLoadProcess("正在查询流水分析数据");
        CashierApiImpl.analysisAccDateList(dailysettleEntity.getDailysettleDate(), responseCallback);
    }

    /**
     * 保存流水分析数据
     */
    private void saveAccData(RspQueryResult<DailysettleAccItem> rs) {
        ZLogger.df("日结--保存日结流水分析数据");

        try {
            List<DailysettleAccItem> entityList = new ArrayList<>();
            if (rs != null && rs.getReturnNum() > 0) {
                for (EntityWrapper<DailysettleAccItem> wrapper : rs.getRowDatas()) {
                    entityList.add(wrapper.getBean());
                }
            }
            AccWrapper accWrapper = new AccWrapper();
            accWrapper.initWithDailysettleAccItems(entityList);

            this.dailysettleEntity.setAccCashAmount(accWrapper.getCashAmount());
            this.dailysettleEntity.setAccCashOrderNum(accWrapper.getCashQuantity());
            this.dailysettleEntity.setAccAlipayAmount(accWrapper.getAlipayAmount());
            this.dailysettleEntity.setAccAlipayOrderNum(accWrapper.getAlipayQuantity());
            this.dailysettleEntity.setAccWxAmount(accWrapper.getWxAmount());
            this.dailysettleEntity.setAccWxOrderNum(accWrapper.getWxQuantity());
            this.dailysettleEntity.setAccMemberAccount(accWrapper.getMemberAccountAmount());
            this.dailysettleEntity.setAccMemberOrderNum(accWrapper.getMemberAccountQuantity());
//            this.dailysettleEntity.setAccScAmount(accWrapper.getScAmount());
//            this.dailysettleEntity.setAccScOrderNum(accWrapper.getScQuantity());
            this.dailysettleEntity.setAccBankcardAmount(accWrapper.getBankcardAmount());
            this.dailysettleEntity.setAccBankcardOrderNum(accWrapper.getBankcardQuantity());
            DailysettleService.get().saveOrUpdate(this.dailysettleEntity);

            refresh();
        } catch (Exception ex) {
            ZLogger.d("保存流水分析数据失败:" + ex.toString());
        }
    }

    /**
     * 确认日结统计
     */
    private void analysisAcDateDoEnd(String outTradeNo) {
        onLoadProcess("正在确认日结...");

        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            onLoadError("网络未连接，暂停确认日结。");
            btnConfirm.setEnabled(true);
            return;
        }

        //回调
        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<String,
                NetProcessor.Processor<String>>(
                new NetProcessor.Processor<String>() {
                    @Override
                    public void processResult(IResponseData rspData) {
                        //{"code":"0","msg":"操作成功!","version":"1","data":""}
                        RspValue<String> retValue = (RspValue<String>) rspData;
                        ZLogger.d("确认日结成功:" + retValue.getValue());
                        // 保存交接班时间和班次
                        SharedPreferencesHelper.setLastHandoverDateTime(dailysettleEntity.getDailysettleDate());
//                        SharedPreferencesHelper.setLastHandoverShiftId(dailySettleBill.getShiftId());

                        //确认日结
                        if (dailysettleEntity != null) {
                            dailysettleEntity.setConfirmStatus(DailysettleEntity.CONFIRM_STATUS_YES);
                            DailysettleService.get().saveOrUpdate(dailysettleEntity);
                        }

                        onLoadFinished();

                        DialogUtil.showHint("确认日结成功");

                        // 打印交接单
                        SerialManager.printDailySettleBill(dailysettleEntity);

                        //TODO,跳转至支付页面
                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        //{"code":"1","msg":"指定的日结流水已经日结过：17","version":"1","data":null}
                        onLoadError("确认日结失败：" + errMsg);
                        btnConfirm.setEnabled(true);

//                        getActivity().setResult(Activity.RESULT_OK);
//                        getActivity().finish();
                    }
                }
                , String.class
                , CashierApp.getAppContext()) {
        };

        CashierApiImpl.analysisAcDateDoEnd(dailysettleEntity.getDailysettleDate(),
                outTradeNo, responseCallback);
    }

}
