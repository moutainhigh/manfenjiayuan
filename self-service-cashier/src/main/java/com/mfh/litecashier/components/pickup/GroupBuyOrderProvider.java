package com.mfh.litecashier.components.pickup;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.DividerGridItemDecoration;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.rxapi.bean.GroupBuyOrder;
import com.mfh.framework.rxapi.bean.GroupBuyOrderItem;
import com.mfh.litecashier.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.drakeet.multitype.ItemViewProvider;


/**
 * 商品前台类目卡片
 * Created by bingshanguxue on 09/10/2016.
 */

public class GroupBuyOrderProvider extends ItemViewProvider<GroupBuyOrder,
        GroupBuyOrderProvider.ViewHolder> {

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(
            @NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.cardview_groupbuy_order, parent, false);
        return new ViewHolder(root);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull GroupBuyOrder card) {
        holder.setData(card);
        // TODO: 08/07/2017
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate;
        private RecyclerView recyclerView;
        private GroupBuyOrderItemsAdapter mAdapter;
        private ImageButton ibRatio;
        private GroupBuyOrder groupBuyOrder;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.goods_list);
            ibRatio = (ImageButton) itemView.findViewById(R.id.ib_ratio);
            mAdapter = new GroupBuyOrderItemsAdapter(itemView.getContext(), null);
            mAdapter.setOnAdapterListener(new GroupBuyOrderItemsAdapter.OnAdapterListener() {
                @Override
                public void onDataSetChanged() {

                }
            });

            LinearLayoutManager mRLayoutManager = new LinearLayoutManager(itemView.getContext());
            mRLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(mRLayoutManager);
            //enable optimizations if all item views are of the same height and width for
            //signficantly smoother scrolling
            recyclerView.setHasFixedSize(true);
            recyclerView.addItemDecoration(new DividerGridItemDecoration(itemView.getContext(),
                    R.drawable.divider_gridview));
//            recyclerView.addItemDecoration(new DividerGridItemDecoration());
            recyclerView.setAdapter(mAdapter);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    groupBuyOrder.setSelected(true);
                    ibRatio.setVisibility(View.VISIBLE);
                }
            });
            ibRatio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    groupBuyOrder.setSelected(false);
                    ibRatio.setVisibility(View.GONE);
                }
            });
        }

        /**
         * 设置数据
         * */
        private void setData(GroupBuyOrder groupBuyOrder) {
            this.groupBuyOrder = groupBuyOrder;
            if (groupBuyOrder != null) {
                tvDate.setText(TimeUtil.format(groupBuyOrder.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS));

                List<GroupBuyOrderItem> items = groupBuyOrder.getItems();
                if (items == null || items.size() <= 0){
                    items = new ArrayList<>();
                    GroupBuyOrderItem item = new GroupBuyOrderItem();
                    item.setCreatedDate(new Date());
                    item.setBcount(1.1D);
                    item.setAmount(11.12D);
                    items.add(item);
                }
                this.mAdapter.setEntityList(items);

            } else {
                tvDate.setText("ERROR");
                this.mAdapter.setEntityList(null);
            }
        }
    }
}
