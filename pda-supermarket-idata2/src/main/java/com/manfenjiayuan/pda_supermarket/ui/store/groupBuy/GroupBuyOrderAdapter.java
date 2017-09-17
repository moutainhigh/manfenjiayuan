package com.manfenjiayuan.pda_supermarket.ui.store.groupBuy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.framework.rxapi.bean.GroupBuyOrder;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 团购活动
 * Created by bingshanguxue on 15/8/5.
 */
public class GroupBuyOrderAdapter extends RegularAdapter<GroupBuyOrder, GroupBuyOrderAdapter.ProductViewHolder> {

    public GroupBuyOrderAdapter(Context context, List<GroupBuyOrder> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_groupbuy_order, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        GroupBuyOrder entity = entityList.get(position);

        try {
            holder.tvPhonenumber.setText(entity.getReceivePhone());
            holder.tvBcount.setText(entity.getBuyerName());
            if (entity.getStatus().equals(4)) {
                holder.tvStatus.setText("已提");
                holder.tvPhonenumber.setEnabled(false);
                holder.tvBcount.setEnabled(false);
                holder.tvStatus.setEnabled(false);
            } else {
                holder.tvStatus.setText("待提");
                holder.tvPhonenumber.setEnabled(true);
                holder.tvBcount.setEnabled(true);
                holder.tvStatus.setEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        //        @Bind(R.id.rootview)
//        View rootView;
        @BindView(R.id.tv_phonenumber)
        TextView tvPhonenumber;
        @BindView(R.id.tv_bcount)
        TextView tvBcount;
        @BindView(R.id.tv_status)
        TextView tvStatus;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    GroupBuyOrder order = getEntity(position);
                    if (order == null || order.getStatus().equals(4)) {
                        return;
                    }

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

    public void setEntityList(List<GroupBuyOrder> entityList) {
        this.entityList = entityList;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public List<GroupBuyOrder> getEntityList() {
        return entityList;
    }

}
