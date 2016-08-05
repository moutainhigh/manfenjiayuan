package com.manfenjiayuan.pda_wholesaler.ui.fragment.shelves;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.manfenjiayuan.pda_wholesaler.AppContext;
import com.manfenjiayuan.pda_wholesaler.R;
import com.manfenjiayuan.pda_wholesaler.database.entity.ShelveEntity;
import com.manfenjiayuan.pda_wholesaler.database.logic.ShelveService;
import com.manfenjiayuan.pda_wholesaler.ui.adapter.GoodsShelvesAdapter;
import com.manfenjiayuan.pda_wholesaler.utils.ShelveSyncManager;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import butterknife.Bind;
import de.greenrobot.event.EventBus;


/**
 * 绑定记录
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class GoodsShelvesHistoryFragment extends BaseFragment {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport orderRecyclerView;
    private GoodsShelvesAdapter orderListAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Bind(R.id.empty_view)
    View emptyView;

    private CommonDialog operateDialog = null;

    public static GoodsShelvesHistoryFragment newInstance(Bundle args) {
        GoodsShelvesHistoryFragment fragment = new GoodsShelvesHistoryFragment();

        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        EventBus.getDefault().register(this);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_template_goods_list;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        mToolbar.setTitle("绑定记录");
        mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
        // Set an OnMenuItemClickListener to handle menu item clicks
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle the menu item
                int id = item.getItemId();
                if (id == R.id.action_sync) {
                    sync();
                }
                return true;
            }
        });
        // Inflate a menu to be displayed in the toolbar
        mToolbar.inflateMenu(R.menu.menu_bindshelves_history);

        initGoodsRecyclerView();

        load();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(ShelveSyncManager.ShelveSyncManagerEvent event) {
        ZLogger.d(String.format("BindGoods2ShelvesFragment: ShelveSyncManagerEvent(%d)", event.getEventId()));
        if (event.getEventId() == ShelveSyncManager.ShelveSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED) {
            showProgressDialog(ProgressDialog.STATUS_DONE, "同步成功", true);
        } else if (event.getEventId() == ShelveSyncManager.ShelveSyncManagerEvent.EVENT_ID_SYNC_DATA_FAILED) {
            showProgressDialog(ProgressDialog.STATUS_DONE, "同步失败", true);
        }
    }

    public void sync() {
        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.tip_network_error);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING);
        ShelveSyncManager.get().sync();
    }

    private void initGoodsRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(getActivity());
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

        orderListAdapter = new GoodsShelvesAdapter(getActivity(), null);
        orderListAdapter.setOnAdapterListener(new GoodsShelvesAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

//                final ShelveEntity entity = orderListAdapter.getEntityList().get(position);
//                if (entity.getStatus() == ShelveEntity.STATUS_CONFLICT) {
                //TODO,修改状态

//                }
            }

            @Override
            public void onItemLongClick(View view, final int position) {

                final ShelveEntity entity = orderListAdapter.getEntity(position);
                if (operateDialog == null){
                    operateDialog = new CommonDialog(getActivity());
                    operateDialog.setCancelable(true);
                }
                operateDialog.setMessage(String.format("确定要删除该商品 %s吗？", entity.getBarcode()));
                operateDialog.setPositiveButton("删除", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        orderListAdapter.removeEntity(position);
                    }
                });
                operateDialog.setNegativeButton("点错了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                if (!operateDialog.isShowing()){
                    operateDialog.show();
                }

            }


            @Override
            public void onDataSetChanged() {
            }
        });
        orderRecyclerView.setAdapter(orderListAdapter);
    }

    /**
     * 加载数据
     */
    private void load() {
        //按更新日期，降序排列
        if (orderListAdapter != null) {
            orderListAdapter.setEntityList(ShelveService.get().queryAllByDesc(""));
        }
    }


}
