package com.mfh.litecashier.ui.fragment.purchase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.companyInfo.CompanyInfoPresenter;
import com.mfh.framework.api.companyInfo.ICompanyInfoView;
import com.mfh.framework.api.constant.AbilityItem;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.SelectPlatformProviderAdapter;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;
import com.mfh.litecashier.ui.widget.InputSearchView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.mfh.litecashier.ui.fragment.purchase.SelectWholesalerWithTenantFragment.SelectWholesalerWithTenantEvent;

/**
 * 选择门店
 * Created by Nat.ZZN(bingshanguxue) on 15/12/15.
 */
public class SelectCompanyInfoFragment extends BaseFragment
        implements ICompanyInfoView {

    @BindView(R.id.inlv_shortCode)
    InputSearchView labelShortcode;
    @BindView(R.id.company_list)
    RecyclerViewEmptySupport mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    @BindView(R.id.empty_view)
    TextView emptyView;
    @BindView(R.id.animProgress)
    ProgressBar progressBar;

    private SelectPlatformProviderAdapter productAdapter;

    private boolean isLoadingMore;
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 20;
    private boolean bSyncInProgress = false;//是否正在同步
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
    private List<CompanyInfo> orderList = new ArrayList<>();

    private CompanyInfoPresenter mCompanyInfoPresenter;

    public static SelectCompanyInfoFragment newInstance(Bundle args) {
        SelectCompanyInfoFragment fragment = new SelectCompanyInfoFragment();

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
        mCompanyInfoPresenter = new CompanyInfoPresenter(this);
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
        labelShortcode.setSoftKeyboardEnabled(true);
        labelShortcode.config(InputSearchView.INPUT_TYPE_TEXT);
        labelShortcode.setHintText("门店名称");
//        inlvProductName.requestFocus();
        labelShortcode.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER}, new InputNumberLabelView.OnInterceptListener() {
            @Override
            public void onKey(int keyCode, String text) {
                if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER){
                    reload();
                }
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
                if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
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
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
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

        mCompanyInfoPresenter.findPublicCompanyInfo(mPageInfo, labelShortcode.getInputString(), AbilityItem.TENANT);
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
        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载关联租户。");
            onLoadFinished();
            return;
        }

        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
            mPageInfo.moveToNext();

            mCompanyInfoPresenter.findPublicCompanyInfo(mPageInfo, labelShortcode.toString(), AbilityItem.TENANT);
        } else {
            ZLogger.d("加载关联租户，已经是最后一页。");
            onLoadFinished();
        }
    }

    @Override
    public void onICompanyInfoViewProcess() {
        isLoadingMore = true;
        bSyncInProgress = true;
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onICompanyInfoViewError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onICompanyInfoViewSuccess(PageInfo pageInfo, List<CompanyInfo> dataList) {
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
