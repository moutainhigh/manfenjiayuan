package com.manfenjiayuan.pda_supermarket.ui.store.invcheck;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.Toolbar;

import com.bingshanguxue.pda.database.entity.InvCheckGoodsEntity;
import com.manfenjiayuan.pda_supermarket.AppContext;
import com.manfenjiayuan.pda_supermarket.R;
import com.bingshanguxue.pda.database.service.InvCheckGoodsService;
import com.bingshanguxue.pda.bizz.invcheck.InvCheckHistoryAdapter;
import com.manfenjiayuan.pda_supermarket.utils.DataSyncService;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;

import butterknife.Bind;
import de.greenrobot.event.EventBus;


/**
 * 盘点记录
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class InvCheckHistoryFragment extends BaseFragment {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport orderRecyclerView;
    private InvCheckHistoryAdapter orderListAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Bind(R.id.empty_view) View emptyView;

    public static InvCheckHistoryFragment newInstance(Bundle args) {
        InvCheckHistoryFragment fragment = new InvCheckHistoryFragment();

        if (args != null){
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
        mToolbar.setTitle("盘点记录");
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
        mToolbar.inflateMenu(R.menu.menu_inv_check);

        initGoodsRecyclerView();

        load();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inv_check, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }


    public void removeAll(){
        orderListAdapter.removeAll();
    }

    public void sync() {
        if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
            DialogUtil.showHint(R.string.tip_network_error);
            return;
        }

        showProgressDialog(ProgressDialog.STATUS_PROCESSING);
        DataSyncService.get().sync(DataSyncService.SYNC_STEP_UPLOAD_STOCKTAKE);
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

        orderListAdapter = new InvCheckHistoryAdapter(getActivity(), null);
        orderListAdapter.setOnAdapterListener(new InvCheckHistoryAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

//                final InvCheckGoodsEntity entity = orderListAdapter.getEntityList().get(position);
//                if (entity.getStatus() == InvCheckGoodsEntity.STATUS_CONFLICT) {
                    //TODO,修改状态

//                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                final InvCheckGoodsEntity entity = orderListAdapter.getEntityList().get(position);
                if (entity == null){
                    return;
                }
                CommonDialog dialog = new CommonDialog(getActivity());
                dialog.setCancelable(true);
                dialog.setMessage("请选择提交方式");
                dialog.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        entity.setUpdateHint(InvCheckGoodsEntity.HINT_OVERRIDE);
//                            entity.setStatus(InvCheckGoodsEntity.STATUS_NONE);
                        InvCheckGoodsService.get().saveOrUpdate(entity);
                        orderListAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                dialog.setNegativeButton("合并", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        entity.setUpdateHint(InvCheckGoodsEntity.HINT_MERGER);
//                            entity.setStatus(InvCheckGoodsEntity.STATUS_NONE);
                        InvCheckGoodsService.get().saveOrUpdate(entity);
                        orderListAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }

            @Override
            public void onDataSetChanged() {
            }

            @Override
            public void onConflictSolved() {
                load();
            }
        });
        orderRecyclerView.setAdapter(orderListAdapter);
    }

    /**
     * 加载数据
     * */
    private void load(){
        //按更新日期，降序排列
        if (orderListAdapter != null){
            orderListAdapter.setEntityList(InvCheckGoodsService.get()
                    .queryAllByDesc(String.format("status != '%d'", InvCheckGoodsEntity.STATUS_FINISHED)));
        }
    }

    public void onEventMainThread(DataSyncService.StockTakeSyncEvent event) {
        ZLogger.d(String.format("StockTakeSyncEvent(%d)", event.getEventId()));
        if (event.getEventId() == DataSyncService.StockTakeSyncEvent.EVENT_ID_SYNC_FINISHED) {
            showProgressDialog(ProgressDialog.STATUS_DONE, "同步成功", true);
        } else if (event.getEventId() == DataSyncService.StockTakeSyncEvent.EVENT_ID_SYNC_FAILED) {
            showProgressDialog(ProgressDialog.STATUS_DONE, "同步失败", true);
        }
    }
}
