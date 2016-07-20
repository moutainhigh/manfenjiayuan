package com.manfenjiayuan.mixicook_vip.ui.shopcart;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.database.PurchaseShopcartEntity;
import com.manfenjiayuan.mixicook_vip.database.PurchaseShopcartService;
import com.manfenjiayuan.mixicook_vip.ui.SimpleActivity;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by bingshanguxue on 6/28/16.
 */
public class ShopcartFragment extends BaseFragment {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport goodsRecyclerView;
    private ShopcartGoodsAdapter goodsListAdapter;
    private LinearLayoutManager mRLayoutManager;
    @Bind(R.id.empty_view)
    TextView emptyView;

    @Bind(R.id.tv_brief)
    TextView tvBrief;
    @Bind(R.id.button_confirm)
    Button btnConfirm;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_shopcart;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        toolbar.setTitle("购物车");

        initGoodsRecyclerView();

        List<PurchaseShopcartEntity> entities = PurchaseShopcartService.getInstance().fetchFreshEntites();
        goodsListAdapter.setEntityList(entities);

        DialogUtil.showHint("购物车" + entities.size());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0: {
                if (resultCode == Activity.RESULT_OK) {
                    showProgressDialog(ProgressDialog.STATUS_DONE, "预定成功", true);
                }
                refresh();
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void refresh() {
        List<PurchaseShopcartEntity> entities = PurchaseShopcartService.getInstance().fetchFreshEntites();
        goodsListAdapter.setEntityList(entities);
    }

    @OnClick(R.id.button_confirm)
    public void redirect2OrderFragment() {
//        Bundle extras = new Bundle();
//        extras.putInt(SimpleActivity.EXTRA_KEY_FRAGMENT_TYPE, SimpleActivity.FT_CONFIRM_ORDER);
//        UIHelper.startActivity(getActivity(), SimpleActivity.class, extras);


        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "确认订单");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_FRAGMENT_TYPE, SimpleActivity.FT_CONFIRM_ORDER);
        Intent intent = new Intent(getActivity(), SimpleActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, 0);
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
//                int lastVisibleItem = mRLayoutManager.findLastVisibleItemPosition();
//                int totalItemCount = mRLayoutManager.getItemCount();
//                //lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载，各位自由选择
//                // dy>0 表示向下滑动
////                ZLogger.d(String.format("%s %d(%d)", (dy > 0 ? "向上滚动" : "向下滚动"), lastVisibleItem, totalItemCount));
//                if (lastVisibleItem >= totalItemCount - 1 && dy > 0) {
//                    if (!isLoadingMore) {
//                        loadMore();
//                    }
//                } else if (dy < 0) {
//                    isLoadingMore = false;
//                }
            }
        });

        goodsListAdapter = new ShopcartGoodsAdapter(AppContext.getAppContext(), null);
        goodsListAdapter.setOnAdapterListsner(new ShopcartGoodsAdapter.OnAdapterListener() {
                                                  @Override
                                                  public void onItemClick(View view, int position) {

                                                  }

                                                  @Override
                                                  public void onDeleteConfirm(final int position) {
                                                      final PurchaseShopcartEntity entity = goodsListAdapter.getEntity(position);
                                                      if (entity == null){
                                                          return;
                                                      }

                                                      showConfirmDialog(String.format("确定要删除 %s 吗？", entity.getName()),
                                                              "删除", new DialogInterface.OnClickListener() {

                                                                  @Override
                                                                  public void onClick(DialogInterface dialog, int which) {
                                                                      dialog.dismiss();
                                                                      goodsListAdapter.removeEntity(position);

                                                                  }
                                                              }, "点错了", new DialogInterface.OnClickListener() {

                                                                  @Override
                                                                  public void onClick(DialogInterface dialog, int which) {
                                                                      dialog.dismiss();
                                                                  }
                                                              });
                                                  }

                                                  @Override
                                                  public void onDataSetChanged() {
                                                      int count = goodsListAdapter.getItemCount();
                                                      if (count > 0) {
                                                          tvBrief.setText(String.format("商品数：%d", count));
                                                          btnConfirm.setEnabled(true);
                                                      } else {
                                                          tvBrief.setText("暂无商品");
                                                          btnConfirm.setEnabled(false);
                                                      }
                                                  }
                                              }

        );
        goodsRecyclerView.setAdapter(goodsListAdapter);
    }
}
