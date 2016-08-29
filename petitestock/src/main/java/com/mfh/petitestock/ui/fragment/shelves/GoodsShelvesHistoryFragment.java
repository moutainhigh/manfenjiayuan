package com.mfh.petitestock.ui.fragment.shelves;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.petitestock.AppContext;
import com.mfh.petitestock.R;
import com.mfh.petitestock.database.entity.ShelveEntity;
import com.mfh.petitestock.database.logic.ShelveService;
import com.mfh.petitestock.ui.adapter.GoodsShelvesAdapter;
import com.mfh.petitestock.utils.ShelveSyncManager;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * 盘点记录
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class GoodsShelvesHistoryFragment extends BaseFragment {
    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport orderRecyclerView;
    private GoodsShelvesAdapter orderListAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Bind(R.id.empty_view)
    View emptyView;
    @Bind(R.id.button_clear)
    Button btnClear;

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
        return R.layout.fragment_shelves_history;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
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
        ZLogger.d(String.format("GoodsShelvesFragment: ShelveSyncManagerEvent(%d)", event.getEventId()));
        if (event.getEventId() == ShelveSyncManager.ShelveSyncManagerEvent.EVENT_ID_SYNC_DATA_FINISHED) {
            showProgressDialog(ProgressDialog.STATUS_DONE, "同步成功", true);
        } else if (event.getEventId() == ShelveSyncManager.ShelveSyncManagerEvent.EVENT_ID_SYNC_DATA_FAILED) {
            showProgressDialog(ProgressDialog.STATUS_DONE, "同步失败", true);
        }
    }

    @OnClick(R.id.button_sync)
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

                final ShelveEntity entity = orderListAdapter.getEntityList().get(position);
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
                if (orderListAdapter.getItemCount() > 0) {
                    btnClear.setEnabled(true);
                } else {
                    btnClear.setEnabled(false);
                }
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
