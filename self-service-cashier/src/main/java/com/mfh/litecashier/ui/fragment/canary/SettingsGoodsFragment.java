package com.mfh.litecashier.ui.fragment.canary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.category.CateApi;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.service.DataSyncManager;
import com.mfh.litecashier.ui.adapter.SettingsGoodsAdapter;
import com.mfh.litecashier.ui.fragment.settings.SettingsFragment;
import com.mfh.litecashier.ui.widget.InputSearchView;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 设置－－POS机本地商品库
 * 适用场景，查看本地商品库
 *
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public class SettingsGoodsFragment extends BaseListFragment<PosProductEntity> {

    @Bind(R.id.swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.order_list)
    RecyclerViewEmptySupport orderRecyclerView;
    @Bind(R.id.empty_view)
    TextView emptyView;
    private LinearLayoutManager linearLayoutManager;
    private SettingsGoodsAdapter orderListAdapter;

    @Bind(R.id.spinner_tenant)
    Spinner spinnerTenant;
    @Bind(R.id.spinner_catetype)
    Spinner spinnerCatetype;
    @Bind(R.id.spinner_status)
    Spinner spinnerStatus;
    @Bind(R.id.tv_brief)
    TextView tvBrief;
    @Bind(R.id.insv_order_barcode)
    InputSearchView insvOrderBarcode;


    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_settings_goods;
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

        ArrayAdapter<CharSequence> orderStatusAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.settings_goods_status, R.layout.mfh_spinner_item_text);
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

        ArrayAdapter<CharSequence> cateTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.settings_goods_catetype, R.layout.mfh_spinner_item_text);
        cateTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCatetype.setAdapter(cateTypeAdapter);
        spinnerCatetype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reload();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerCatetype.setSelection(0);

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
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
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
     * 初始化商品列表
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

        orderListAdapter = new SettingsGoodsAdapter(getActivity(), null);
        orderListAdapter.setOnAdapterListener(new SettingsGoodsAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
            }

            @Override
            public void onItemLongClick(View view, int position) {
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

    /**
     * 同步商品
     * */
    @OnClick(R.id.fab_sync)
    public void syncGoods() {
        DataSyncManager.get().sync(DataSyncManager.SYNC_STEP_PRODUCTS);
    }

    /**
     * 在主线程接收CashierEvent事件，必须是public void
     */
    public void onEventMainThread(SettingsFragment.SettingsGoodsEvent event) {
        ZLogger.d(String.format("SettingsGoodsFragment: SettingsGoodsEvent(%d)", event.getEventId()));
        if (event.getEventId() == SettingsFragment.SettingsGoodsEvent.EVENT_ID_RELOAD_DATA) {
            refresh();
            reload();
        }
    }

    private void refresh(){
        spinnerTenant.setSelection(0);
        spinnerStatus.setSelection(0);
        spinnerCatetype.setSelection(0);
        insvOrderBarcode.clear();
        insvOrderBarcode.requestFocus();
    }

    /**
     * 重新加载数据
     */
    @OnClick(R.id.empty_view)
    public synchronized void reload() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载本地商品库。");
//            onLoadFinished();
            return;
        }

        onLoadStart();
        if (orderListAdapter != null) {
            orderListAdapter.setEntityList(null);
        }

//        mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
        mPageInfo.reset();
        mPageInfo.setPageNo(1);
        load(mPageInfo);
    }


    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载本地商品库。");
//            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage()) {
            mPageInfo.moveToNext();

            onLoadStart();
            load(mPageInfo);
        } else {
            ZLogger.d("加载本地商品库，已经是最后一页。");
            onLoadFinished();
        }
    }

    /**
     * 加载数据
     * */
    private void load(PageInfo pageInfo) {
        String barcode = insvOrderBarcode != null ? insvOrderBarcode.getInputString() : "";

        StringBuilder sbWhere = new StringBuilder();
        sbWhere.append(String.format("barcode like '%%%s%%'", barcode));

        String tenantStr = spinnerTenant != null ? spinnerTenant.getSelectedItem().toString() : "";
        if (tenantStr.equals("当前租户")){
            sbWhere.append(String.format(" and tenantId = '%d'", MfhLoginService.get().getSpid()));
        }

        String orderStatusCaption = spinnerStatus != null ? spinnerStatus.getSelectedItem().toString() : "";
        switch (orderStatusCaption){
            case "出售中":{
                sbWhere.append(String.format(" and status = '%d'", 1));
            }
            break;
            case "已下架":{
                sbWhere.append(String.format(" and status = '%d'", 0));
            }
            break;
            default:{
            }
            break;
        }

        String cateTypeCaption = spinnerCatetype != null ? spinnerCatetype.getSelectedItem().toString() : "";
        switch (cateTypeCaption){
            case "普通商品":{
                sbWhere.append(String.format(" and cateType = '%d'", CateApi.BACKEND_CATE_BTYPE_NORMAL));
            }
            break;
            case "商品套餐":{
                sbWhere.append(String.format(" and cateType = '%d'", CateApi.BACKEND_CATE_BTYPE_PACKAGE));
            }
            break;
            case "生鲜":{
                sbWhere.append(String.format(" and cateType = '%d'", CateApi.BACKEND_CATE_BTYPE_FRESH));
            }
            break;
            case "香烟":{
                sbWhere.append(String.format(" and cateType = '%d'", CateApi.BACKEND_CATE_BTYPE_SMOKE));
            }
            break;
            case "烘培":{
                sbWhere.append(String.format(" and cateType = '%d'", CateApi.BACKEND_CATE_BTYPE_BAKING));
            }
            break;
            case "水果":{
                sbWhere.append(String.format(" and cateType = '%d'", CateApi.BACKEND_CATE_BTYPE_FRUIT));
            }
            break;
            default:{
            }
            break;
        }

        List<PosProductEntity> entities  = PosProductService.get()
                .queryAllBy(sbWhere.toString(), "updatedDate desc", pageInfo);

        if (entities == null || entities.size() < 1) {
            ZLogger.d("没有找到商品。");

            tvBrief.setText("商品总数：0");
            onLoadFinished();
            return;
        }
        ZLogger.d(String.format("共找到%d条商品(%d/%d-%d)", entities.size(),
                pageInfo.getPageNo(), pageInfo.getTotalPage(), pageInfo.getTotalCount()));

        tvBrief.setText(String.format("商品总数：%d", pageInfo.getTotalCount()));
        if (orderListAdapter != null) {
            orderListAdapter.appendEntityList(entities);
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
