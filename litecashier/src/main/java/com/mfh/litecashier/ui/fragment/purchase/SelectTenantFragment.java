package com.mfh.litecashier.ui.fragment.purchase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.manfenjiayuan.business.bean.CompanyInfo;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.constant.AbilityItem;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.manfenjiayuan.business.presenter.TenantPresenter;
import com.mfh.litecashier.ui.adapter.SelectPlatformProviderAdapter;
import com.manfenjiayuan.business.view.ITenantView;
import com.mfh.litecashier.ui.widget.InputSearchView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

import static com.mfh.litecashier.ui.fragment.purchase.SelectWholesalerWithTenantFragment.SelectWholesalerWithTenantEvent;

/**
 * 选择门店
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class SelectTenantFragment extends BaseFragment
        implements ITenantView{

    @Bind(R.id.inlv_shortCode)
    InputSearchView labelShortcode;
    @Bind(R.id.company_list)
    RecyclerViewEmptySupport mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    @Bind(R.id.empty_view)
    TextView emptyView;
    @Bind(R.id.animProgress)
    ProgressBar progressBar;

    private SelectPlatformProviderAdapter productAdapter;

    private boolean isLoadingMore;
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 20;
    private boolean bSyncInProgress = false;//是否正在同步
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private List<CompanyInfo> orderList = new ArrayList<>();

    private TenantPresenter tenantPresenter;

    public static SelectTenantFragment newInstance(Bundle args) {
        SelectTenantFragment fragment = new SelectTenantFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_select_wholesaler;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);
        tenantPresenter = new TenantPresenter(this);
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initShortcodeView();
        initRecyclerView();

        reload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        EventBus.getDefault().unregister(this);
    }

    private void initShortcodeView() {
        labelShortcode.setInputSubmitEnabled(true);
        labelShortcode.setSoftKeyboardEnabled(true);
        labelShortcode.config(InputSearchView.INPUT_TYPE_TEXT);
        labelShortcode.setHintText("门店名称");
//        inlvProductName.requestFocus();
        labelShortcode.setOnInoutKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                ZLogger.d("setOnKeyListener(SelectInvCompProviderDialog.labelShortcode):" + keyCode);
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

        labelShortcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                reload();
            }
        });
        labelShortcode.setOnViewListener(new InputSearchView.OnViewListener() {
            @Override
            public void onSubmit(String text) {
                reload();
            }
        });
    }

    private void initRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        mRecyclerView.setHasFixedSize(true);
        //设置列表为空时显示的视图
        mRecyclerView.setEmptyView(emptyView);
        //添加分割线
        mRecyclerView.addItemDecoration(new LineItemDecoration(
                getContext(), LineItemDecoration.VERTICAL_LIST));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        productAdapter = new SelectPlatformProviderAdapter(getContext(), null);
        productAdapter.setOnAdapterListener(new SelectPlatformProviderAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Bundle args = new Bundle();
                args.putSerializable("data", productAdapter.getEntity(position));
                EventBus.getDefault()
                        .post(new SelectWholesalerWithTenantEvent(SelectWholesalerWithTenantEvent.EVENT_ID_ITEM_SELECTED, args));
            }

            @Override
            public void onDataSetChanged() {
            }
        });
        mRecyclerView.setAdapter(productAdapter);
    }

    /**
     * 加载完成
     */
    private void onLoadFinished() {
        bSyncInProgress = false;
        isLoadingMore = false;
        progressBar.setVisibility(View.GONE);
    }

    /**
     * 重新加载数据
     */
    @OnClick(R.id.empty_view)
    public void reload() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载关联租户。");
//            onLoadFinished();
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载关联租户。");
            onLoadFinished();
            return;
        }


        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        if (orderList == null) {
            orderList = new ArrayList<>();
        } else {
            orderList.clear();
        }

        tenantPresenter.getTenants(mPageInfo, getNameLike(), AbilityItem.TENANT);
        mPageInfo.setPageNo(1);
    }

    /**
     * 翻页加载更多数据
     */
    public void loadMore() {
        if (bSyncInProgress) {
            ZLogger.d("正在加载关联租户。");
//            onLoadFinished();
            return;
        }
        if (!NetWorkUtil.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载关联租户。");
            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            tenantPresenter.getTenants(mPageInfo, getNameLike(), AbilityItem.TENANT);
        } else {
            ZLogger.d("加载关联租户，已经是最后一页。");
            onLoadFinished();
        }
    }

    @Override
    public String getNameLike() {
        return labelShortcode.getInputString();
    }

    @Override
    public void onProcess() {
        isLoadingMore = true;
        bSyncInProgress = true;
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onSuccess(PageInfo pageInfo, List<CompanyInfo> dataList) {
        mPageInfo = pageInfo;
        if (orderList == null) {
            orderList = new ArrayList<>();
        }
        orderList.addAll(dataList);
        if (productAdapter != null) {
            productAdapter.setEntityList(orderList);
        }
        onLoadFinished();
    }


}
