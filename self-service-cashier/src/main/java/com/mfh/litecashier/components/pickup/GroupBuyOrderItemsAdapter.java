package com.mfh.litecashier.components.pickup;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.rxapi.bean.GroupBuyOrderItem;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;



/**
 * 团购订单列表
 * Created by bingshanguxue on 17/7/4.
 */
public class GroupBuyOrderItemsAdapter extends RegularAdapter<GroupBuyOrderItem, GroupBuyOrderItemsAdapter.ViewHolder> {

    public GroupBuyOrderItemsAdapter(Context context, List<GroupBuyOrderItem> entityList) {
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
        View v = mLayoutInflater.inflate(R.layout.itemview_groupbuy_order_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        GroupBuyOrderItem entity = entityList.get(position);

        holder.tvName.setText(entity.getProductName());
        holder.tvBcount.setText(MUtils.formatDouble(entity.getBcount(), ""));
        holder.tvAmount.setText(MUtils.formatDouble(entity.getAmount(), ""));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_bcount)
        TextView tvBcount;
        @BindView(R.id.tv_amount)
        TextView tvAmount;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setEntityList(List<GroupBuyOrderItem> entityList) {
        this.entityList = entityList;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
