package com.mfh.litecashier.ui.fragment.canary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bingshanguxue.cashier.database.entity.DailysettleEntity;
import com.bingshanguxue.cashier.database.service.DailysettleService;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.helper.SharedPreferencesManager;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.AccItem;
import com.mfh.litecashier.bean.AggItem;
import com.mfh.litecashier.bean.wrapper.AccWrapper;
import com.mfh.litecashier.bean.wrapper.AggWrapper;
import com.mfh.litecashier.com.PrintManagerImpl;
import com.mfh.litecashier.event.SettingsDailysettleEvent;
import com.mfh.litecashier.ui.adapter.SettingsDailysettleAdapter;
import com.mfh.litecashier.ui.widget.InputSearchView;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 设置－－日结
 * 显示POS机本地的日结流水
 * <p/>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public class SettingsDailysettleFragment extends BaseFragment {
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
    private SettingsDailysettleAdapter orderListAdapter;

    @Bind(R.id.spinner_tenant)
    Spinner spinnerTenant;
    @Bind(R.id.insv_order_barcode)
    InputSearchView insvOrderBarcode;

    @Bind(R.id.tv_receipt_tail)
    TextView tvReceiptTail;

    private boolean isLoadingMore;
    private static final int MAX_SYNC_PAGESIZE = 7;
    private PageInfo mPageInfo = new PageInfo(false, MAX_SYNC_PAGESIZE);//new PageInfo(PageInfo.PAGENO_NOTINIT, MAX_SYNC_PAGESIZE);
    private boolean bSyncInProgress = false;//是否正在同步

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settints_dailysettle;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
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

        tvReceiptTail.setMovementMethod(ScrollingMovementMethod.getInstance());

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

        orderListAdapter = new SettingsDailysettleAdapter(getActivity(), null);
        orderListAdapter.setOnAdapterListener(new SettingsDailysettleAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                DailysettleEntity orderEntity = orderListAdapter.getCurPosOrder();
                loadReceipt(orderEntity);
            }

            @Override
            public void onItemLongClick(View view, int position) {
            }

            @Override
            public void onDataSetChanged() {
                onLoadFinished();
                DailysettleEntity orderEntity = orderListAdapter.getCurPosOrder();
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
            if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
                if (!isLoadingMore) {
                    loadMore();
                }
            } else if (dy < 0) {
                isLoadingMore = false;
            }
        }
    };


    /**
     * 打印订单
     */
    @OnClick(R.id.fab_print)
    public void printOrder() {
        DailysettleEntity orderEntity = orderListAdapter.getCurPosOrder();
        if (orderEntity == null) {
            DialogUtil.showHint("请先选择订单");
            return;
        }

        PrintManagerImpl.printDailySettleBill(orderEntity);
    }

    /**
     * 加载订单小票
     *
     * @param dailysettleEntity 订单
     */
    private void loadReceipt(DailysettleEntity dailysettleEntity) {

        StringBuilder sbTail = new StringBuilder();
        sbTail.append(String.format("<p><font color=#000000>%s</font></p>",
                MfhLoginService.get().getCurOfficeName()));

        if (dailysettleEntity != null) {
            sbTail.append(String.format("<p><font color=#000000>日结人:%s</font></p>",
                    dailysettleEntity.getHumanName()));
            sbTail.append(String.format("<p><font color=#000000>日结时间:%s</font></p>",
                    (dailysettleEntity.getDailysettleDate() != null
                            ? TimeCursor.InnerFormat.format(dailysettleEntity.getDailysettleDate())
                            : "")
            ));
            sbTail.append(String.format("<p><font color=#000000>设备编号:%s</font></p>",
                    SharedPreferencesManager.getTerminalId()));

            sbTail.append("--------------------------------\n");
            AggWrapper aggWrapper = JSON.toJavaObject(JSON.parseObject(dailysettleEntity.getAggData()),
                    AggWrapper.class);
            if (aggWrapper == null) {
                aggWrapper = new AggWrapper();
            }
            int index = 1;

            List<AggItem> posItems = aggWrapper.getPosItems();
            if (posItems != null && posItems.size() > 0) {
                for (AggItem aggItem : posItems) {
                    sbTail.append(String.format("<p><font color=#000000>%d.%s-%s\t\t%.2f\t%.2f</font></p>",
                            index, aggItem.getBizTypeCaption(), aggItem.getSubTypeCaption(),
                            aggItem.getOrderNum(),
                            aggItem.getTurnover()));
                    index++;
                }
            } else {
                sbTail.append(String.format("<p><font color=#000000>%d.社区超市\t\t%.2f\t%.2f</font></p>",
                        index, 0D,
                        0D));
                index++;
            }

            List<AggItem> scItems = aggWrapper.getScItems();
            if (scItems != null && scItems.size() > 0) {
                for (AggItem aggItem : scItems) {
                    sbTail.append(String.format("<p><font color=#000000>%d.%s-%s\t\t%.2f\t%.2f</font></p>",
                            index, aggItem.getBizTypeCaption(), aggItem.getSubTypeCaption(),
                            aggItem.getOrderNum(),
                            aggItem.getTurnover()));
                    index++;
                }
            } else {
                sbTail.append(String.format("<p><font color=#000000>%d.线上订单\t\t%.2f\t%.2f</font></p>",
                        index, 0D,
                        0D));
                index++;
            }

            List<AggItem> laundryItems = aggWrapper.getLaundryItems();
            if (laundryItems != null && laundryItems.size() > 0) {
                for (AggItem aggItem : laundryItems) {
                    sbTail.append(String.format("<p><font color=#000000>%d.%s-%s\t\t%.2f\t%.2f</font></p>",
                            index, aggItem.getBizTypeCaption(), aggItem.getSubTypeCaption(),
                            aggItem.getOrderNum(),
                            aggItem.getTurnover()));
                    index++;
                }
            } else {
                sbTail.append(String.format("<p><font color=#000000>%d.衣物洗护\t\t%.2f\t%.2f</font></p>",
                        index, 0D,
                        0D));
                index++;
            }
            List<AggItem> pijuItems = aggWrapper.getPijuItems();
            if (pijuItems != null && pijuItems.size() > 0) {
                for (AggItem aggItem : pijuItems) {
                    sbTail.append(String.format("<p><font color=#000000>%d.%s-%s\t\t%.2f\t%.2f</font></p>",
                            index, aggItem.getBizTypeCaption(), aggItem.getSubTypeCaption(),
                            aggItem.getOrderNum(),
                            aggItem.getTurnover()));
                    index++;
                }
            } else {
                sbTail.append(String.format("<p><font color=#000000>%d.皮具护理\t\t%.2f\t%.2f</font></p>",
                        index, 0D,
                        0D));
                index++;
            }

            List<AggItem> stockItems = aggWrapper.getStockItems();
            if (stockItems != null && stockItems.size() > 0) {
                for (AggItem aggItem : stockItems) {
                    sbTail.append(String.format("<p><font color=#000000>%d.%s-%s\t\t%.2f\t%.2f</font></p>",
                            index, aggItem.getBizTypeCaption(), aggItem.getSubTypeCaption(),
                            aggItem.getOrderNum(),
                            aggItem.getTurnover()));
                    index++;
                }
            } else {
                sbTail.append(String.format("<p><font color=#000000>%d.快递代收\t\t%.2f\t%.2f</font></p>",
                        index, 0D,
                        0D));
                index++;
            }

            List<AggItem> sendItems = aggWrapper.getSendItems();
            if (sendItems != null && sendItems.size() > 0) {
                for (AggItem aggItem : sendItems) {
                    sbTail.append(String.format("<p><font color=#000000>%d.%s-%s\t\t%.2f\t%.2f</font></p>",
                            index, aggItem.getBizTypeCaption(), aggItem.getSubTypeCaption(),
                            aggItem.getOrderNum(),
                            aggItem.getTurnover()));
                    index++;
                }
            } else {
                sbTail.append(String.format("<p><font color=#000000>%d.快递代揽\t\t%.2f\t%.2f</font></p>",
                        index, 0D,
                        0D));
                index++;
            }

            List<AggItem> rechargeItems = aggWrapper.getRechargeItems();
            if (rechargeItems != null && rechargeItems.size() > 0) {
                for (AggItem aggItem : rechargeItems) {
                    sbTail.append(String.format("<p><font color=#000000>%d.%s-%s\t\t%.2f\t%.2f\t%s</font></p>",
                            index, aggItem.getBizTypeCaption(), aggItem.getSubTypeCaption(),
                            aggItem.getOrderNum(),
                            aggItem.getTurnover(),
                            MUtils.retrieveFormatedGrossMargin(aggItem.getTurnover(),
                                    aggItem.getGrossProfit())));
                    index++;
                }
            } else {
                sbTail.append(String.format("<p><font color=#000000>%d.转账充值\t\t%.2f\t%.2f</font></p>",
                        index, 0D,
                        0D));
                index++;
            }

            sbTail.append("--------------------------------\n");
            AccWrapper accWrapper = JSON.toJavaObject(JSON.parseObject(dailysettleEntity.getAccData()),
                    AccWrapper.class);
            if (accWrapper == null) {
                accWrapper = new AccWrapper();
            }
            int accIndex = 1;

            AccItem cashItem = accWrapper.getCashItem();
            if (cashItem != null) {
                sbTail.append(String.format("<p><font color=#000000>%d.%s\t\t%.2f\t%.2f</font></p>",
                        accIndex, cashItem.getPayTypeCaption(),
                        cashItem.getOrderNum(),
                        cashItem.getAmount()));
            } else {
                sbTail.append(String.format("<p><font color=#000000>%d.现金\t\t%.2f\t%.2f</font></p>",
                        accIndex, 0D,
                        0D));
            }
            accIndex++;

            AccItem alipayItem = accWrapper.getAlipayItem();
            if (alipayItem != null) {
                sbTail.append(String.format("<p><font color=#000000>%d.%s\t\t%.2f\t%.2f</font></p>",
                        accIndex, alipayItem.getPayTypeCaption(),
                        alipayItem.getOrderNum(),
                        alipayItem.getAmount()));
            } else {
                sbTail.append(String.format("<p><font color=#000000>%d.支付宝\t\t%.2f\t%.2f</font></p>",
                        accIndex, 0D,
                        0D));
            }
            accIndex++;

            AccItem wxItem = accWrapper.getWxItem();
            if (wxItem != null) {
                sbTail.append(String.format("<p><font color=#000000>%d.%s\t\t%.2f\t%.2f</font></p>",
                        accIndex, wxItem.getPayTypeCaption(),
                        wxItem.getOrderNum(),
                        wxItem.getAmount()));
            } else {
                sbTail.append(String.format("<p><font color=#000000>%d.微信\t\t%.2f\t%.2f</font></p>",
                        accIndex, 0D,
                        0D));
            }
            accIndex++;

            AccItem accountItem = accWrapper.getAccountItem();
            if (accountItem != null) {
                sbTail.append(String.format("<p><font color=#000000>%d.%s\t\t%.2f\t%.2f</font></p>",
                        accIndex, accountItem.getPayTypeCaption(),
                        accountItem.getOrderNum(),
                        accountItem.getAmount()));
            } else {
                sbTail.append(String.format("<p><font color=#000000>%d.平台账户\t\t%.2f\t%.2f</font></p>",
                        accIndex, 0D,
                        0D));
            }
            accIndex++;

            AccItem bankItem = accWrapper.getBankItem();
            if (bankItem != null) {
                sbTail.append(String.format("<p><font color=#000000>%d.%s\t\t%.2f\t%.2f</font></p>",
                        accIndex, bankItem.getPayTypeCaption(),
                        bankItem.getOrderNum(),
                        bankItem.getAmount()));
            } else {
                sbTail.append(String.format("<p><font color=#000000>%d.现金\t\t%.2f\t%.2f</font></p>",
                        accIndex, 0D,
                        0D));
            }
            accIndex++;

            AccItem ruleItem = accWrapper.getRuleItem();
            if (cashItem != null) {
                sbTail.append(String.format("<p><font color=#000000>%d.%s\t\t%.2f\t%.2f</font></p>",
                        accIndex, cashItem.getPayTypeCaption(),
                        cashItem.getOrderNum(),
                        cashItem.getAmount()));
            } else {
                sbTail.append(String.format("<p><font color=#000000>%d.现金\t\t%.2f\t%.2f</font></p>",
                        accIndex, 0D,
                        0D));
            }
            accIndex++;

            sbTail.append("--------------------------------\n");
            sbTail.append(String.format("<p><font color=#000000>现金收取:%.2f</font></p>",
                    dailysettleEntity.getCash()));
            sbTail.append(String.format("<p><font color=#000000>非现金收取:%.2f</font></p>",
                    dailysettleEntity.getTurnover() - dailysettleEntity.getCash()));

            Double turnover = dailysettleEntity.getTurnover();
            sbTail.append(String.format("<p><font color=#000000>营业额合计:%.2f</font></p>", turnover));

//            //支付记录
//            List<PosOrderPayEntity> payEntityList = PosOrderPayService.get()
//                    .queryAllBy(String.format("orderBarCode = '%s'", orderEntity.getBarCode()));
//            for (PosOrderPayEntity payEntity : payEntityList){
//                sbTail.append(String.format("<p><font color=#979797>\t%s：%.2f [%s]\n</font></p>",
//                        MUtils.getPayTypeDesc(payEntity.getPayType()), payEntity.getAmount(),
//                        MUtils.getPayStatusDesc(payEntity.getPaystatus())));
//            }
//            if (orderEntity.getStatus() == PosOrderEntity.ORDER_STATUS_FINISH){
//                sbTail.append(String.format("<p><font color=#32CD32>找零：%.2f\n</font></p>",
//                        orderEntity.getCharge()));
//            }
//            else{
//            }
        }
        sbTail.append("辛苦了!\\n祝您生活愉快\\n");

        tvReceiptTail.setText(Html.fromHtml(sbTail.toString()));
    }


    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(SettingsDailysettleEvent event) {
        ZLogger.d(String.format("SettingsDailysettleEvent(%d)", event.getEventId()));
        if (event.getEventId() == SettingsDailysettleEvent.EVENT_ID_RELOAD_DATA) {
            refresh();
            reload();
        }
    }

    private void refresh() {
        spinnerTenant.setSelection(0);
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
            ZLogger.d("正在加载日结订单流水。");
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
            ZLogger.d("正在加载日结订单流水。");
//            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage()) {
            mPageInfo.moveToNext();

            onLoadStart();
            load(mPageInfo);
        } else {
            ZLogger.d("加载日结订单流水，已经是最后一页。");
            onLoadFinished();
        }
    }

    /**
     * 加载数据
     */
    private void load(PageInfo pageInfo) {
        String barcode = insvOrderBarcode != null ? insvOrderBarcode.getInputString() : "";

        StringBuilder sbWhere = new StringBuilder();
        sbWhere.append(String.format("barCode like '%%%s%%'", barcode));

        String tenantStr = spinnerTenant != null ? spinnerTenant.getSelectedItem().toString() : "";
        if (tenantStr.equals("当前租户")) {
            sbWhere.append(String.format(" and officeId = '%d'", MfhLoginService.get().getCurOfficeId()));
        }

        List<DailysettleEntity> entityList = DailysettleService.get()
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


}
