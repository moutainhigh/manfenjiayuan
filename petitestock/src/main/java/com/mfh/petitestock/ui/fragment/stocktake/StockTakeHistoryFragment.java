package com.mfh.petitestock.ui.fragment.stocktake;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.CommonDialog;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.petitestock.AppContext;
import com.mfh.petitestock.R;
import com.mfh.petitestock.ui.adapter.StockTakeAdapter;
import com.mfh.petitestock.database.entity.StockTakeEntity;
import com.mfh.petitestock.database.logic.StockTakeService;
import com.mfh.petitestock.utils.DataSyncService;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * 盘点记录
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class StockTakeHistoryFragment extends BaseFragment {
    @Bind(R.id.goods_list)
    RecyclerViewEmptySupport orderRecyclerView;
    private StockTakeAdapter orderListAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Bind(R.id.empty_view) View emptyView;
    @Bind(R.id.button_clear)
    Button btnClear;

    public static StockTakeHistoryFragment newInstance(Bundle args) {
        StockTakeHistoryFragment fragment = new StockTakeHistoryFragment();

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
        return R.layout.fragment_stock_take_list;
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


    @OnClick(R.id.button_clear)
    public void removeAll(){
        orderListAdapter.removeAll();
    }

    @OnClick(R.id.button_sync)
    public void sync() {
        if (!NetWorkUtil.isConnect(AppContext.getAppContext())) {
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

        orderListAdapter = new StockTakeAdapter(getActivity(), null);
        orderListAdapter.setOnAdapterListener(new StockTakeAdapter.OnAdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

                final StockTakeEntity entity = orderListAdapter.getEntityList().get(position);
//                if (entity.getStatus() == StockTakeEntity.STATUS_CONFLICT) {
                    //TODO,修改状态

//                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                final StockTakeEntity entity = orderListAdapter.getEntityList().get(position);
                CommonDialog dialog = new CommonDialog(getActivity());
                dialog.setCancelable(true);
                dialog.setMessage("请选择提交方式");
                dialog.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        entity.setUpdateHint(StockTakeEntity.HINT_OVERRIDE);
//                            entity.setStatus(StockTakeEntity.STATUS_NONE);
                        StockTakeService.get().saveOrUpdate(entity);
                        orderListAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                dialog.setNegativeButton("合并", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        entity.setUpdateHint(StockTakeEntity.HINT_MERGER);
//                            entity.setStatus(StockTakeEntity.STATUS_NONE);
                        StockTakeService.get().saveOrUpdate(entity);
                        orderListAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }

            @Override
            public void onDataSetChanged() {
                if (orderListAdapter.getItemCount() > 0){
                    btnClear.setEnabled(true);
                }
                else{
                    btnClear.setEnabled(false);
                }
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
            orderListAdapter.setEntityList(StockTakeService.get()
                    .queryAllByDesc(String.format("status != '%d'", StockTakeEntity.STATUS_FINISHED)));
        }
    }
}
