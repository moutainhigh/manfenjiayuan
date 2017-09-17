package com.mfh.litecashier.components.pickup;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.rxapi.bean.GroupBuyOrder;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 团购订单列表
 * Created by bingshanguxue on 17/7/4.
 */
public class GroupBuyOrderAdapter extends RegularAdapter<GroupBuyOrder, GroupBuyOrderAdapter.ViewHolder> {

    public GroupBuyOrderAdapter(Context context, List<GroupBuyOrder> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.cardview_groupbuy_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        GroupBuyOrder entity = entityList.get(position);

        //"¥%.1f",优惠券金额不会有小数
        holder.tvDate.setText(TimeUtil.format(entity.getUpdatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS));
        holder.mAdapter.setEntityList(entity.getItems());
//        mWebview.loadDataWithBaseURL(null, s, "text/html", "UTF-8", null);//解决乱码问题

        if (entity.isSelected()) {
            holder.ibRatio.setVisibility(View.VISIBLE);
        } else {
            holder.ibRatio.setVisibility(View.GONE);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_date)
        TextView tvDate;
        @BindView(R.id.goods_list)
        RecyclerView mRecyclerView;
        @BindView(R.id.ib_ratio)
        ImageView ibRatio;
        GroupBuyOrderItemsAdapter mAdapter;


        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mAdapter = new GroupBuyOrderItemsAdapter(itemView.getContext(), null);
            mAdapter.setOnAdapterListener(new GroupBuyOrderItemsAdapter.OnAdapterListener() {
                @Override
                public void onDataSetChanged() {

                }
            });

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(itemView.getContext(),
                    LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            //enable optimizations if all item views are of the same height and width for
            //signficantly smoother scrolling
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.addItemDecoration(new LineItemDecoration(
                    itemView.getContext(), LineItemDecoration.VERTICAL_LIST));
            mRecyclerView.setAdapter(mAdapter);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    getLayoutPosition()
//                    int position = getAdapterPosition();
//                    if (entityList == null || position < 0 || position >= entityList.size()) {
////                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
//                        return;
//                    }
//
//                    GroupBuyOrder entity = entityList.get(position);
////                    if (entity.isSelected()) {
////                        entity.setSelected(false);
////                    } else {
//                        entity.setSelected(true);
////                    }
//                    notifyItemChanged(position);
//                }
//            });
        }

        @OnClick({R.id.maskView, R.id.ib_ratio})
        public void toggle() {
            try {
                int position = getAdapterPosition();
                if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                    return;
                }

                GroupBuyOrder item = entityList.get(position);
                if (item.isSelected()) {
                    item.setSelected(false);
                } else {
                    item.setSelected(true);
                }
                notifyItemChanged(position);
            } catch (Exception e) {
                ZLogger.e(e.toString());
            }
        }
    }

    public void setEntityList(List<GroupBuyOrder> entityList) {
        this.entityList = entityList;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }


    public List<GroupBuyOrder> getSelectedEntityList() {
        List<GroupBuyOrder> groupBuyOrders = new ArrayList<>();

        if (entityList != null) {
            for (GroupBuyOrder order :
                    entityList) {

                if (order != null && order.isSelected()) {
                    groupBuyOrders.add(order);
                }
//                ZLogger.d("isSelected=" + order.isSelected());
            }
        }

        return groupBuyOrders;
    }
}
