package com.mfh.litecashier.ui.dialog;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.mfh.framework.api.companyInfo.CompanyInfo;
import com.mfh.framework.api.companyInfo.CompanyInfoPresenter;
import com.mfh.framework.api.companyInfo.ICompanyInfoView;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.api.constant.AbilityItem;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.adapter.SelectPlatformProviderAdapter;
import com.mfh.litecashier.ui.widget.InputSearchView;
import com.mfh.litecashier.utils.ACacheHelper;

import java.util.List;


/**
 * 选择门店
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SelectCompanyInfoDialog extends CommonDialog
        implements ICompanyInfoView {

    private View rootView;
    private ImageButton btnClose;
    private TextView tvTitle;
    private InputSearchView labelShortcode;
    private RecyclerViewEmptySupport mRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private TextView emptyView;
    private ProgressBar progressBar;

    private SelectPlatformProviderAdapter productAdapter;

    private boolean isLoadingMore;
    private static final int MAX_PAGE = 10;
    private static final int MAX_SYNC_PAGESIZE = 20;
    private boolean bSyncInProgress = false;//是否正在同步
    private PageInfo mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);

    private CompanyInfoPresenter mCompanyInfoPresenter;
    private Integer abilityItem = AbilityItem.TENANT;


    public interface OnDialogListener {
        void onItemSelected(CompanyInfo companyInfo);
    }

    private OnDialogListener listener;


    @Override
    public void onICompanyInfoViewProcess() {
        onLoadStart();
    }

    @Override
    public void onICompanyInfoViewError(String errorMsg) {
        onLoadFinished();
    }

    @Override
    public void onICompanyInfoViewSuccess(PageInfo pageInfo, List<CompanyInfo> dataList) {
        mPageInfo = pageInfo;

        //第一页，缓存数据
        if (mPageInfo.getPageNo() == 1) {
//                    ZLogger.d("缓存关联租户第一页数据");
            JSONArray cacheArrays = new JSONArray();
            if (dataList != null) {
                cacheArrays.addAll(dataList);
            }
            ACacheHelper.put(ACacheHelper.CK_RELATIVE_TENANT, cacheArrays.toJSONString());
            if (productAdapter != null) {
                productAdapter.setEntityList(dataList);
            }

        } else {
            if (productAdapter != null) {
                productAdapter.appendEntityList(dataList);
            }
        }

        ZLogger.d(String.format("保存关联租户:page=%d/%d(%d/%d)",
                mPageInfo.getPageNo(), mPageInfo.getTotalPage(),
                productAdapter.getItemCount(), mPageInfo.getTotalCount()));

        onLoadFinished();
    }


    private SelectCompanyInfoDialog(Context context, boolean flag, OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private SelectCompanyInfoDialog(Context context, int defStyle) {
        super(context, defStyle);
        rootView = getLayoutInflater().inflate(
                R.layout.dialogview_select_tenant, null);
//        ButterKnife.bind(rootView);

        mCompanyInfoPresenter = new CompanyInfoPresenter(this);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_header_title);
        btnClose = (ImageButton) rootView.findViewById(R.id.button_header_close);
        labelShortcode = (InputSearchView) rootView.findViewById(R.id.inlv_shortCode);
        mRecyclerView = (RecyclerViewEmptySupport) rootView.findViewById(R.id.company_list);
        emptyView = (TextView) rootView.findViewById(R.id.empty_view);
        progressBar = (ProgressBar) rootView.findViewById(R.id.animProgress);

        tvTitle.setText("选择门店");
        initShortcodeView();
        initRecyclerView();

        btnClose.setOnClickListener(dialogClickListener);
        emptyView.setOnClickListener(dialogClickListener);

        setContent(rootView, 0);
    }

    public SelectCompanyInfoDialog(Context context) {
        this(context, R.style.dialog_common);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().setGravity(Gravity.CENTER);

//        WindowManager m = getWindow().getWindowManager();
//        Display d = m.getDefaultDisplay();
//        WindowManager.LayoutParams p = getWindow().getAttributes();
////        p.width = d.getWidth() * 2 / 3;
////        p.y = DensityUtil.dip2px(getContext(), 44);
//
//        final TypedArray a = getContext().obtainStyledAttributes(ATTRS);
//        p.y = (int)a.getDimension(0, 44);
//        getWindow().setAttributes(p);


        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void show() {
        super.show();

        if (!NetworkUtils.isConnect(CashierApp.getAppContext())) {
            readCache();
        } else {
            reload();
        }
    }

    private View.OnClickListener dialogClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_header_close: {
                    dismiss();
                }
                break;
                case R.id.empty_view: {
                    reload();
                }
                break;
            }
        }
    };

    public void init(Integer abilityItem, OnDialogListener listener) {
        this.abilityItem = abilityItem;
        this.listener = listener;
        this.productAdapter.setEntityList(null);
    }

    private void initShortcodeView() {
        labelShortcode.setInputSubmitEnabled(true);
        labelShortcode.setSoftKeyboardEnabled(true);
        labelShortcode.config(InputSearchView.INPUT_TYPE_TEXT);
        labelShortcode.setHintText("名称");
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
                dismiss();

                if (listener != null) {
                    listener.onItemSelected(productAdapter.getEntity(position));
                }
            }

            @Override
            public void onDataSetChanged() {
            }
        });
        mRecyclerView.setAdapter(productAdapter);
    }

    /**
     * 开始加载
     */
    private void onLoadStart() {
        isLoadingMore = true;
        bSyncInProgress = true;
        progressBar.setVisibility(View.VISIBLE);
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
//        onLoadStart();
//        load(mPageInfo);
        mCompanyInfoPresenter.findPublicCompanyInfo(mPageInfo, labelShortcode.getInputString(), abilityItem);
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

//            onLoadStart();
//            load(mPageInfo);
            mCompanyInfoPresenter.findPublicCompanyInfo(mPageInfo,
                    labelShortcode.getInputString(), abilityItem);
        } else {
            ZLogger.d("加载关联租户，已经是最后一页。");
            onLoadFinished();
        }
    }

    /**
     * 读取缓存
     */
    public synchronized boolean readCache() {
        //读取缓存，如果有则加载缓存数据，否则重新加载
        String cacheStr = ACacheHelper.getAsString(ACacheHelper.CK_RELATIVE_TENANT);
        List<CompanyInfo> cacheData = JSONArray.parseArray(cacheStr, CompanyInfo.class);
        if (cacheData != null && cacheData.size() > 0) {
            ZLogger.d(String.format("加载缓存数据(%s): %d个关联租户", ACacheHelper.CK_RELATIVE_TENANT, cacheData.size()));
            if (productAdapter != null) {
                productAdapter.setEntityList(cacheData);
            }

            return true;
        }

        return false;
    }

}
