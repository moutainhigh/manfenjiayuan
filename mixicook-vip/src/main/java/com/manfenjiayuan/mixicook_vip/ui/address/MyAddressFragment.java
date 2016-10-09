package com.manfenjiayuan.mixicook_vip.ui.address;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.ARCode;
import com.manfenjiayuan.mixicook_vip.ui.FragmentActivity;
import com.manfenjiayuan.mixicook_vip.ui.SimpleActivity;
import com.mfh.comn.bean.PageInfo;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.reciaddr.Reciaddr;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseListFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 我的收货地址
 * Created by bingshanguxue on 6/28/16.
 */
public class MyAddressFragment extends BaseListFragment<Reciaddr> implements IReciaddrView {
//    public static final String EXTRA_KEY_SHOP_ID = "shopId";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private MyAddressAdapter goodsListAdapter;
    private LinearLayoutManager mRLayoutManager;
    @Bind(R.id.empty_view)
    TextView emptyView;

    @Bind(R.id.tv_brief)
    TextView tvBrief;
    @Bind(R.id.button_add)
    Button btnConfirm;
    @Bind(R.id.noAddressView)
    View mNoAddressView;

    private ReciaddrPresenter mReciaddrPresenter;

    public static MyAddressFragment newInstance(Bundle args){
        MyAddressFragment fragment = new MyAddressFragment();

        if (args != null){
            fragment.setArguments(args);
        }
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EventBus.getDefault().register(this);

        mReciaddrPresenter = new ReciaddrPresenter(this);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_myaddress;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
//        Bundle args = getArguments();
//        if (args != null) {
//            shopId = args.getLong(EXTRA_KEY_SHOP_ID);
//        }

        toolbar.setTitle("收货地址");
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_manager) {
                    DialogUtil.showHint("管理收货地址");
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        toolbar.inflateMenu(R.menu.menu_myaddress);

        initGoodsRecyclerView();

        reload();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case ARCode.ARC_ADD_ADDRESS:{
                if (resultCode == Activity.RESULT_OK){
                    reload();
                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void reload() {
        super.reload();
        if (bSyncInProgress) {
            ZLogger.d("正在加载收货地址。");
//            onLoadFinished();
            return;
        }
        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            ZLogger.d("网络未连接，暂停加载订单流水。");
            onLoadFinished();
            return;
        }

        mPageInfo = new PageInfo(-1, MAX_SYNC_PAGESIZE);
        mReciaddrPresenter.getAllAddrsByHuman(MfhLoginService.get().getCurrentGuId());

        mPageInfo.setPageNo(1);
    }

    @Override
    public void loadMore() {
        super.loadMore();
        onLoadFinished();

//        if (bSyncInProgress) {
//            ZLogger.d("正在加载收货地址。");
////            onLoadFinished();
//            return;
//        }
//        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
//            ZLogger.d("网络未连接，暂停加载收货地址。");
//            onLoadFinished();
//            return;
//        }
//
//
//        if (mPageInfo.hasNextPage() && mPageInfo.getPageNo() <= MAX_PAGE) {
//            mPageInfo.moveToNext();
//
//            mReciaddrPresenter.getAllAddrsByHuman(MfhLoginService.get().getCurrentGuId(), mPageInfo);
//
//        } else {
//            ZLogger.d("加载收货地址，已经是最后一页。");
//            onLoadFinished();
//        }
    }

    @OnClick(R.id.button_add)
    public void addAddress() {
        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "收货地址");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_MYADDRESS);
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_ADD_ADDRESS);
    }


    private void initGoodsRecyclerView() {
        mRLayoutManager = new LinearLayoutManager(AppContext.getAppContext());
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
                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
                    if (!isLoadingMore) {
                        loadMore();
                    }
                } else if (dy < 0) {
                    isLoadingMore = false;
                }
            }
        });

        goodsListAdapter = new MyAddressAdapter(AppContext.getAppContext(), null);
        goodsListAdapter.setOnAdapterListsner(new MyAddressAdapter.OnAdapterListener() {
                                                  @Override
                                                  public void onItemClick(View view, int position) {

                                                      Reciaddr reciaddr = goodsListAdapter.getEntity(position);
                                                      if (reciaddr != null){
                                                          Intent data = new Intent();
                                                          data.putExtra("reciaddr", reciaddr);
                                                          getActivity().setResult(Activity.RESULT_OK, data);
                                                          getActivity().finish();
                                                      }
                                                      else{
                                                          getActivity().setResult(Activity.RESULT_CANCELED);
                                                          getActivity().finish();
                                                      }
                                                  }

                                                  @Override
                                                  public void onDataSetChanged() {
                                                      int count = goodsListAdapter.getItemCount();
                                                      if (count > 0) {
                                                          tvBrief.setText(String.format("商品数：%d", count));
                                                          btnConfirm.setEnabled(true);
                                                          mNoAddressView.setVisibility(View.GONE);
                                                      } else {
                                                          tvBrief.setText("暂无商品");
                                                          btnConfirm.setEnabled(false);
                                                          mNoAddressView.setVisibility(View.VISIBLE);
                                                      }
                                                  }
                                              }

        );
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }

    @Override
    public void onLoadFinished() {
        super.onLoadFinished();
        hideProgressDialog();
    }



    @Override
    public void onIReciaddrViewProcess() {
        showProgressDialog(ProgressDialog.STATUS_PROCESSING, "请稍候...", false);
        onLoadStart();
    }

    @Override
    public void onIReciaddrViewError(String errorMsg) {
        if (!StringUtils.isEmpty(errorMsg)){
            ZLogger.df(errorMsg);
        }

        onLoadFinished();
    }

    @Override
    public void onIReciaddrViewSuccess(PageInfo pageInfo, List<Reciaddr> dataList) {
        try {
            mPageInfo = pageInfo;
            if (goodsListAdapter != null) {
                goodsListAdapter.setEntityList(dataList);
            }

            onLoadFinished();
        } catch (Throwable ex) {
//            throw new RuntimeException(ex);
            ZLogger.e(String.format("加载收货地址失败: %s", ex.toString()));
            onLoadFinished();
        }
    }

    @Override
    public void onIReciaddrViewSuccess(Reciaddr data) {

    }
}
