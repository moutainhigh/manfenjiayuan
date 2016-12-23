package com.mfh.litecashier.ui.fragment.goods.query;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.bingshanguxue.cashier.database.service.PosProductService;
import com.bingshanguxue.vector_uikit.DividerGridItemDecoration;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.widget.InputNumberLabelView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 商品查询MsgMgrAdapter
 * <p>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/31.
 */
public class QueryGoodsFragment extends BaseListFragment<PosProductEntity> {

//    @BindView(R.id.toolbar)
//    Toolbar mToolbar;
    @BindView(R.id.goodsRecyclerView)
    RecyclerViewEmptySupport goodsRecyclerView;
    @BindView(R.id.empty_view)
    View emptyView;
    private GridLayoutManager linearLayoutManager;
    private QueryGoodsAdapter goodsListAdapter;

    @BindView(R.id.inlv_barcode)
    InputNumberLabelView inlvBarcode;

    @BindView(R.id.letterRecyclerView)
    RecyclerViewEmptySupport letterRecyclerView;
    private LetterAdapter mLetterAdapter;

    private String keyword;

    public interface OnFragmentListener {
        void onClose();

        void onAddGoods(PosProductEntity productEntity);
    }

    private OnFragmentListener mOnFragmentListener;

    public void setOnFragmentListener(OnFragmentListener fragmentListener) {
        mOnFragmentListener = fragmentListener;
    }

    public static QueryGoodsFragment newInstance(Bundle args, OnFragmentListener onFragmentListener) {
        QueryGoodsFragment fragment = new QueryGoodsFragment();

        if (args != null) {
            fragment.setArguments(args);
            fragment.setOnFragmentListener(onFragmentListener);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_goods_query;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        mToolbar.setTitle("商品查询");
////        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
////        mToolbar.setNavigationOnClickListener(
////                new View.OnClickListener() {
////                    @Override
////                    public void onClick(View v) {
////                        getActivity().onBackPressed();
////                    }
////                });
//
//
//// Set an OnMenuItemClickListener to handle menu item clicks
//        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                // Handle the menu item
//                int id = item.getItemId();
//                if (id == R.id.action_reload) {
////                    DialogUtil.showHint("close");
//                    if (mOnFragmentListener != null) {
//                        mOnFragmentListener.onClose();
//                    }
//                }
//                return true;
//            }
//        });
//
//        // Inflate a menu to be displayed in the toolbar
//        mToolbar.inflateMenu(R.menu.menu_querygoods);


        initGoodsRecyclerView();
        initOrderBarcodeView();
        initLetterRecyclerView();

        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (goodsRecyclerView != null) {
            goodsRecyclerView.removeOnScrollListener(orderListScrollListener);
        }
    }

    private void initOrderBarcodeView() {
        inlvBarcode.registerIntercept(new int[]{KeyEvent.KEYCODE_ENTER},
                new InputNumberLabelView.OnInterceptListener() {
                    @Override
                    public void onKey(int keyCode, String text) {
                        //Press “Enter”
                        if (keyCode == KeyEvent.KEYCODE_ENTER) {
                            //条码枪扫描结束后会自动触发回车键
                            keyword = inlvBarcode.getInputString();
                            reload();
                        }

                    }
                });
        inlvBarcode.registerOnViewListener(new InputNumberLabelView.OnViewListener() {
            @Override
            public void onClickAction1(String text) {
                keyword = text;
                reload();
            }

            @Override
            public void onLongClickAction1(String text) {

            }
        });
//        inlvBarcode.requestFocusEnd();
//        inlvBarcode.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                    if (SharedPrefesManagerFactory.isSoftInputEnabled()
//                            || inlvBarcode.isSoftKeyboardEnabled()) {
////                        showBarcodeKeyboard();
//                    }
//                }
//
//                inlvBarcode.requestFocusEnd();
//                //返回true,不再继续传递事件
//                return true;
//            }
//        });
    }

    /**
     * 初始化商品列表
     */
    private void initGoodsRecyclerView() {
        linearLayoutManager = new GridLayoutManager(getActivity(), 1,
                GridLayoutManager.HORIZONTAL, false);

//        linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
//        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        goodsRecyclerView.setLayoutManager(linearLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        goodsRecyclerView.setHasFixedSize(true);
        //添加分割线
//        letterRecyclerView.addItemDecoration(new LineItemDecoration(
//                getActivity(), LineItemDecoration.HORIZONTAL_LIST));
        goodsRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));

        //设置列表为空时显示的视图
        goodsRecyclerView.setEmptyView(emptyView);
        goodsRecyclerView.addOnScrollListener(orderListScrollListener);

        goodsListAdapter = new QueryGoodsAdapter(getActivity(), null);
        goodsListAdapter.setOnAdapterLitener(new QueryGoodsAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                //加入购物车
                PosProductEntity productEntity = goodsListAdapter.getEntity(position);
                if (productEntity != null && mOnFragmentListener != null) {
                    mOnFragmentListener.onAddGoods(productEntity);
                }
            }
        });

        goodsRecyclerView.setAdapter(goodsListAdapter);
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

    private void initLetterRecyclerView() {
        GridLayoutManager mRLayoutManager = new GridLayoutManager(getActivity(), 6);
        letterRecyclerView.setLayoutManager(mRLayoutManager);
        //enable optimizations if all item views are of the same height and width for
        //signficantly smoother scrolling
        letterRecyclerView.setHasFixedSize(true);
//        menuRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        letterRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
        letterRecyclerView.addItemDecoration(new DividerGridItemDecoration(getActivity()));

        mLetterAdapter = new LetterAdapter(CashierApp.getAppContext(), null);
        mLetterAdapter.setOnAdapterLitener(new LetterAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {
                Letter entity = mLetterAdapter.getEntity(position);
                if (entity != null) {
//                    DialogUtil.showHint(String.format("选中%s(%s)",
//                            entity.getKey(), entity.getValue()));
                    inlvBarcode.appendInput(entity.getValue());

                }
            }
        });
        letterRecyclerView.setAdapter(mLetterAdapter);

        List<Letter> letters = new ArrayList<>();
        for (String az : Letter.A_Z_0_9) {
            Letter letter = new Letter();
            letter.setKey(az);
            letter.setValue(az);
            letters.add(letter);

        }
        mLetterAdapter.setEntityList(letters);
    }

    private void refresh() {
//        spinnerTenant.setSelection(0);
//        spinnerStatus.setSelection(0);
//        spinnerCatetype.setSelection(0);
//        insvOrderBarcode.clear();
//        insvOrderBarcode.requestFocus();
    }

    public synchronized void reload() {
        inlvBarcode.clear();
        inlvBarcode.requestFocusEnd();

        if (bSyncInProgress) {
            ZLogger.d("正在加载本地商品库。");
//            onLoadFinished();
            return;
        }

        onLoadStart();
//        mPageInfo = new PageInfo(1, MAX_SYNC_PAGESIZE);
        mPageInfo.reset();
        mPageInfo.setPageNo(1);
        load(keyword, mPageInfo);
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
            load(keyword, mPageInfo);
        } else {
            ZLogger.d("加载本地商品库，已经是最后一页。");
            onLoadFinished();
        }
    }

    /**
     * 加载数据
     * */
    /**
     * 分页查询数据
     */
    private void load(final String abbreviation, final PageInfo pageInfo) {
        onLoadStart();

        Observable.create(new Observable.OnSubscribe<List<PosProductEntity>>() {
            @Override
            public void call(Subscriber<? super List<PosProductEntity>> subscriber) {
                List<PosProductEntity> productEntities = new ArrayList<>();

                try {
                    String sqlWhere = String.format("abbreviation like '%%%s%%'", abbreviation);
                    productEntities = PosProductService.get()
                            .queryAll(sqlWhere, pageInfo);
                } catch (Throwable ex) {
                    ZLogger.ef(String.format("加载商品失败: %s", ex.toString()));
                }

                subscriber.onNext(productEntities);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<PosProductEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        onLoadFinished();

                    }

                    @Override
                    public void onNext(List<PosProductEntity> posProductEntities) {
                        if (mPageInfo.getPageNo() == 1) {
                            entityList.clear();
                        }
                        if (posProductEntities != null) {
                            entityList.addAll(posProductEntities);
                        }
                        ZLogger.d(String.format("共有 %d 个商品", entityList.size()));

                        if (goodsListAdapter != null) {
                            goodsListAdapter.setEntityList(entityList);
                        }

                        onLoadFinished();
                    }

                });
    }

}
