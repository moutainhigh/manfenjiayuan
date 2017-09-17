package com.mfh.litecashier.components.reconcile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bingshanguxue.cashier.hardware.printer.PrinterFactory;
import com.bingshanguxue.vector_uikit.OptionalLabel;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.ProductAggDate;
import com.mfh.framework.api.category.CategoryInfo;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetFactory;
import com.mfh.framework.rxapi.entity.MEntityWrapper;
import com.mfh.framework.rxapi.http.ProductAggDateHttpManager;
import com.mfh.framework.rxapi.subscriber.MQuerySubscriber;
import com.mfh.framework.uikit.base.BaseProgressFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.activity.SimpleDialogActivity;
import com.mfh.litecashier.ui.dialog.MyDatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * <h1>对账</h1>
 * Created by bingshanguxue on 17/02/25.
 */
public class ReconcileFragment extends BaseProgressFragment {
    @BindView(R.id.tv_header_title)
    TextView tvHeaderTitle;
    @BindView(R.id.button_header_close)
    ImageButton btnClose;

    @BindView(R.id.label_category)
    OptionalLabel labelCategory;
    @BindView(R.id.label_updatedate)
    OptionalLabel labelUpdateDate;
    @BindView(R.id.button_search)
    Button btnSearch;

    @BindView(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private LinearLayoutManager mRLayoutManager;
    private ProductAggDateAdapter goodsAdapter;
    @BindView(R.id.empty_view)
    TextView emptyView;

    @BindView(R.id.fab_print)
    ImageButton fabPrint;

    protected PageInfo mPageInfo = new PageInfo(PageInfo.PAGENO_NOTINIT, 30);
    private Calendar mCalendar;
    private CategoryInfo mCategoryInfo = null;
    private MyDatePickerDialog dateTimePickerDialog = null;

    public static ReconcileFragment newInstance(Bundle args) {
        ReconcileFragment fragment = new ReconcileFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_reconcile;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        tvHeaderTitle.setText("对账");
        initGoodsRecyclerView();

        mCalendar = Calendar.getInstance();
        mCalendar.setTime(TimeUtil.getCurrentDate());
        mCalendar.add(Calendar.DATE, 0 - 1);
//        calendar.set(Calendar.HOUR, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);

        refresh();
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
        PrinterFactory.getPrinterManager().printReconcile(mCategoryInfo.getNameCn(),
                TimeUtil.format(mCalendar.getTime(), TimeUtil.FORMAT_YYYYMMDD),
                goodsAdapter.getEntityList());
    }

    private void initGoodsRecyclerView() {
        mRLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
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
                if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        goodsAdapter = new ProductAggDateAdapter(CashierApp.getAppContext(), null);
        goodsAdapter.setOnAdapterListener(new ProductAggDateAdapter.OnAdapterListener() {

                                              @Override
                                              public void onDataSetChanged() {
//                                                      onLoadFinished();
                                                  if (goodsAdapter.getItemCount() > 0) {
                                                      fabPrint.setVisibility(View.VISIBLE);
                                                  } else {
                                                      fabPrint.setVisibility(View.GONE);
                                                  }

                                              }
                                          }

        );
        goodsRecyclerView.setAdapter(goodsAdapter);
    }

    /**
     * 重新加载数据
     */
    @OnClick({R.id.empty_view, R.id.button_search})
    public synchronized void reload() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载线上订单订单流水。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载订单流水。");
            onLoadFinished();
            return;
        }

        mPageInfo = new PageInfo(-1, 30);

        Map<String, String> rMaps = new HashMap<>();
        if (mCategoryInfo != null) {
            rMaps.put("subType", String.valueOf(mCategoryInfo.getId()));
        }
        rMaps.put("bizType", String.valueOf(BizType.POS));
        rMaps.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
        rMaps.put("aggDateStr", TimeUtil.format(mCalendar.getTime(), TimeUtil.FORMAT_YYYYMMDD));
        load(rMaps, mPageInfo);
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载线上订单订单流水。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载线上订单订单流水。");
            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage()) {
            mPageInfo.moveToNext();

            Map<String, String> rMaps = new HashMap<>();
            if (mCategoryInfo != null) {
                rMaps.put("subType", String.valueOf(mCategoryInfo.getId()));
            }
            rMaps.put("bizType", String.valueOf(BizType.POS));
            rMaps.put("officeId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
            rMaps.put("aggDateStr", TimeUtil.format(mCalendar.getTime(), TimeUtil.FORMAT_YYYYMMDD));
            load(rMaps, mPageInfo);
        } else {
            DialogUtil.showHint("已经是最后一页了");
            ZLogger.d("加载线上订单订单流水，已经是最后一页。");
            onLoadFinished();
        }
    }


    /**
     * 刷新数据
     */
    private void refresh() {
        labelUpdateDate.setLabelText(TimeUtil.format(mCalendar.getTime(), TimeUtil.FORMAT_YYYYMMDD));

        //显示经营数据
        if (goodsAdapter != null) {
            goodsAdapter.setEntityList(null);
        }

        if (mCategoryInfo == null) {
            selectCategory();
        } else {
            reload();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        ZLogger.d(String.format("requestCode=%d, resultCode=%d, intent=%s",
                requestCode,
                resultCode,
                StringUtils.decodeBundle(intent != null ? intent.getExtras() : null)));
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
//            case Constants.ACTIVITY_REQUEST_CHANGE_NICKNAME:
//                btnItems.get(0).setDetailText(MfhLoginService.get().getHumanName());
//                break;
            case 1://相册
                if (intent != null) {
                    mCategoryInfo = (CategoryInfo) intent.getSerializableExtra("categoryInfo");
                }
                if (mCategoryInfo != null) {
                    labelCategory.setLabelText(mCategoryInfo.getNameCn());
                }
                reload();
                break;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }


    @OnClick(R.id.label_category)
    public void selectCategory() {
        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_SERVICE_TYPE, SimpleDialogActivity.FT_PROD_LINE_LIST);
        extras.putInt(SimpleDialogActivity.EXTRA_KEY_DIALOG_TYPE, SimpleDialogActivity.DT_VERTICIAL_FULLSCREEN);
//        extras.putString(DailySettleFragment.EXTRA_KEY_DATETIME, datetime);
        Intent intent = new Intent(getActivity(), SimpleDialogActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, 1);
    }

    @OnClick(R.id.label_updatedate)
    public void changeDate() {
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

                if (mCalendar.get(Calendar.YEAR) != year
                        || mCalendar.get(Calendar.MONTH) != monthOfYear
                        || mCalendar.get(Calendar.DAY_OF_MONTH) != dayOfMonth) {
                    mCalendar = calendar;

                    refresh();
                }
            }
        });
        if (!dateTimePickerDialog.isShowing()) {
            dateTimePickerDialog.show();
        }
    }

    private void load(Map<String, String> rMaps, PageInfo pageInfo){

        onLoadProcess("加载中");

        Map<String, String> options = new HashMap<>();
        if (rMaps != null) {
            options.putAll(rMaps);
        }
        options.put("wrapper", "true");
//        options.put("netId", String.valueOf(MfhLoginService.get().getCurOfficeId()));
//        options.put("tenantId", String.valueOf(MfhLoginService.get().getSpid()));

        if (pageInfo != null){
            options.put("page", Integer.toString(pageInfo.getPageNo()));
            options.put("rows", Integer.toString(pageInfo.getPageSize()));
        }
        options.put(NetFactory.KEY_JSESSIONID, MfhLoginService.get().getCurrentSessionId());

        ProductAggDateHttpManager.getInstance().list(options,
                new MQuerySubscriber<MEntityWrapper<ProductAggDate>>(pageInfo) {

                    @Override
                    public void onQueryNext(PageInfo pageInfo, List<MEntityWrapper<ProductAggDate>> dataList) {
                        super.onQueryNext(pageInfo, dataList);

                        mPageInfo = pageInfo;

                        List<ProductAggDate> productAggDates = new ArrayList<>();
                        if (dataList != null) {
                            for (MEntityWrapper<ProductAggDate> wrapper : dataList) {
                                ProductAggDate productAggDate = wrapper.getBean();

                                Map<String, String> caption = wrapper.getCaption();
                                if (caption != null) {
                                    productAggDate.setTenantSkuIdWrapper(caption.get("tenantSkuId"));
                                }
                                productAggDates.add(productAggDate);
                            }
                        }
                        if (mPageInfo == null || mPageInfo.getPageNo() == 1) {
                            if (goodsAdapter != null) {
                                goodsAdapter.setEntityList(productAggDates);
                            }
                        } else {
                            if (productAggDates.size() > 0) {
                                if (goodsAdapter != null) {
                                    goodsAdapter.appendEntityList(productAggDates);
                                }
                            }
                        }

                        onLoadFinished();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        ZLogger.e("加载商品销量数据失败:" + e.toString());
                        onLoadFinished();
                    }
                });
    }

}
