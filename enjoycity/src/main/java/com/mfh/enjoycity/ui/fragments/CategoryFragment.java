package com.mfh.enjoycity.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.adapter.ProductAdapter;
import com.mfh.enjoycity.bean.ProductBean;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.enjoycity.ui.ProductDetailActivity;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.recyclerview.GridItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * 类目·
 * 
 * @author Nat.ZZN(bingshanguxue) created on 2015-08-13
 */
public class CategoryFragment extends BaseFragment {

    public static final String EXTRA_KEY_SHOP_ID = "EXTRA_KEY_SHOP_ID";
    public static final String EXTRA_KEY_CATEGORY_ID = "EXTRA_KEY_CATEGORY_ID";

    @Bind(R.id.my_recycler_view)
    RecyclerView mRecyclerView;
    @Bind(R.id.tv_empty)
    TextView tvEmpty;
    @Bind(R.id.animProgress)
    ProgressBar animProgress;

    private ProductAdapter productAdapter;

    private GridLayoutManager mRLayoutManager;
    private int mBaseTranslationY;
    private boolean isLoadingMore;


    private Long shopId;
    private String categoryId;


    public CategoryFragment() {
        super();
    }

    public static CategoryFragment newInstance(Bundle args){
        CategoryFragment fragment = new CategoryFragment();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_category;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        if(intent != null){
            shopId = intent.getLongExtra(EXTRA_KEY_SHOP_ID, 0);
            categoryId = intent.getStringExtra(EXTRA_KEY_CATEGORY_ID);
        }
        //for Fragment.instantiate
        Bundle args = getArguments();
        if (args != null) {
            shopId = args.getLong(EXTRA_KEY_SHOP_ID, 0);
            categoryId = args.getString(EXTRA_KEY_CATEGORY_ID);
        }

        initRecyclerView();

//        loadData();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        new Thread(){
//            @Override
//            public void run() {
////                super.run();

                loadData();
//            }
//        }.start();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (productAdapter != null){
            productAdapter.notifyDataSetChanged();
        }
    }

    private void initRecyclerView() {
        mRLayoutManager = new GridLayoutManager(getContext(), 3);
        mRecyclerView.setLayoutManager(mRLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(orderListScrollListener);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
        mRecyclerView.addItemDecoration(new GridItemDecoration(
                3, 2, false));

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
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
//                MLog.d(String.format("%s %d(%d)", (dy > 0 ? "向上滚动" : "向下滚动"), lastVisibleItem, totalItemCount));
                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
                    if (isLoadingMore) {
//                        MLog.d("ignore manually update!");
                    } else {
//                        DialogUtil.showHint("向下加载更多");
//                        loadPage();//这里多线程也要手动控制isLoadingMore
                        isLoadingMore = true;
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        productAdapter = new ProductAdapter(getContext(), null);
        productAdapter.setOnAdapterLitener(new ProductAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void addToShopcart(ProductBean bean) {
                ShoppingCartService dbService = ShoppingCartService.get();
                dbService.addToShopcart(shopId, bean);
            }

            @Override
            public void showProductDetail(ProductBean bean) {
                Bundle extras = new Bundle();
                extras.putInt(ProductDetailActivity.EXTRA_KEY_ANIM_TYPE, 0);
                extras.putLong(ProductDetailActivity.EXTRA_KEY_PRODUCT_ID, bean.getId());
                extras.putLong(ProductDetailActivity.EXTRA_KEY_SHOP_ID, shopId);
                ProductDetailActivity.actionStart(getActivity(), extras);
            }

            @Override
            public void addToShopcart(float x, float y, ProductBean bean) {

                ShoppingCartService dbService = ShoppingCartService.get();
                dbService.addToShopcart(shopId, bean);

                Intent intent = new Intent(Constants.ACTION_PLAY_SHOPCART_ANIM);
                intent.putExtra(Constants.EXTRA_NAME_SHOPCART_ANIM_SX, x);
                intent.putExtra(Constants.EXTRA_NAME_SHOPCART_ANIM_SY, y);
                getActivity().sendBroadcast(intent);

            }
        });
        mRecyclerView.setAdapter(productAdapter);
        productAdapter.setItemWidth(mRecyclerView.getMeasuredWidth());
    }

    private RecyclerView.OnScrollListener orderListScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
//            int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
//            int totalItemCount = linearLayoutManager.getItemCount();
            //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
            // dy>0 表示向下滑动
//                ZLogger.d(String.format("%s %d(%d)", (dy > 0 ? "向上滚动" : "向下滚动"), lastVisibleItem, totalItemCount));
            if (dy > 0) {

                Intent intent = new Intent(Constants.ACTION_TOGGLE_FLOAT);
                intent.putExtra(Constants.EXTRA_NAME_FLOAT_ENABLED, false);
                getActivity().sendBroadcast(intent);
            } else if (dy < 0) {
                Intent intent = new Intent(Constants.ACTION_TOGGLE_FLOAT);
                intent.putExtra(Constants.EXTRA_NAME_FLOAT_ENABLED, true);
                getActivity().sendBroadcast(intent);
            }
        }
    };


    private void refreshEmptyText(){
        if (productAdapter != null && productAdapter.getItemCount() > 0){
            tvEmpty.setVisibility(View.GONE);
        }else{
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    public void loadData(){
        if (StringUtils.isEmpty(categoryId)){
            DialogUtil.showHint("加载失败，类目编号不能为空");
            Message msg = new Message();
            msg.what = MSG_LOAD_FINISHED;
            uiHandler.sendMessage(msg);
            return;
        }

        if(!NetWorkUtil.isConnect(getContext())){
            DialogUtil.showHint(R.string.toast_network_error);
            Message msg = new Message();
            msg.what = MSG_LOAD_FINISHED;
            uiHandler.sendMessage(msg);
            return;
        }

        Message msg = new Message();
        msg.what = MSG_LOADING;
        uiHandler.sendMessage(msg);

        NetCallBack.QueryRsCallBack queryResponseCallback = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ProductBean>(new PageInfo(1, 100)) {
                    //                处理查询结果集，子类必须继承
                    @Override
                    public void processQueryResult(RspQueryResult<ProductBean> rs) {//此处在主线程中执行。
                        try {
                            int retSize = rs.getReturnNum();
                            ZLogger.d(String.format("%d result, content:%s", retSize, rs.toString()));

                            List<ProductBean> result = new ArrayList<>();
                            if(retSize > 0){
                                for (int i = 0; i < retSize; i++) {
                                    result.add(rs.getRowEntity(i));
                                }
                            }
                            productAdapter.setProductBeans(shopId, result);

                            Message msg = new Message();
                            msg.what = MSG_LOAD_FINISHED;
                            uiHandler.sendMessage(msg);
                        }
                        catch(Throwable ex){
                            ZLogger.e(ex.toString());
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        ZLogger.d("processFailure: " + errMsg);
                        Message msg = new Message();
                        msg.what = MSG_LOAD_FINISHED;
                        uiHandler.sendMessage(msg);
                    }
                }
                , ProductBean.class
                , MfhApplication.getAppContext());

        EnjoycityApiProxy.findProduct(categoryId, queryResponseCallback);
    }

    private static final int MSG_LOADING = 0;
    private static final int MSG_LOAD_FINISHED = 1;
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_LOADING:{
                    animProgress.setVisibility(View.VISIBLE);
                }
                    break;
                case MSG_LOAD_FINISHED:{

                    animProgress.setVisibility(View.GONE);
                    refreshEmptyText();
                }
                    break;
            }
        }
    };

}
