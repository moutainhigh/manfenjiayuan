package com.manfenjiayuan.pda_supermarket.ui.common;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.api.scOrder.ScOrderItem;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * <h1>收银商品适</h1>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class ScOrderItemAdapter
        extends SwipAdapter<ScOrderItem, ScOrderItemAdapter.CashierViewHolder> {

    public interface OnAdapterListener {

        void onDataSetChanged(boolean needScroll);
    }

    protected OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class CashierViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_amount)
        TextView tvAmount;
        @Bind(R.id.tv_bcount)
        TextView tvBcount;

        @Bind(R.id.tv_commitAmount)
        TextView tvCommitAmount;

        public CashierViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null){
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });
        }

    }

    public ScOrderItemAdapter(Context context, List<ScOrderItem> entityList) {
        super(context, entityList);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CashierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CashierViewHolder(mLayoutInflater.inflate(R.layout.itemview_scorder_item,
                parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final CashierViewHolder holder, final int position) {
        // - get element from your dataset at this position
        try {
            ScOrderItem entity = getEntity(position);
            if (entity != null){
                // - replace the contents of the view with that element
                holder.tvName.setText(entity.getProductName());
                //计件：整数；记重：3位小数
                if (entity.getPriceType() == PriceType.WEIGHT) {
                    holder.tvBcount.setText(String.format("%.3f", entity.getBcount()));
                } else {
                    holder.tvBcount.setText(String.format("%.2f", entity.getBcount()));
                }

                holder.tvAmount.setText(MUtils.formatDouble(entity.getAmount(), ""));
                holder.tvCommitAmount.setText(MUtils.formatDouble(entity.getCommitAmount(), ""));
            }

        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }
    }

    @Override
    public void setEntityList(List<ScOrderItem> entityList) {
        super.setEntityList(entityList);

//        sortByUpdateDate();
        notifyDataSetChanged(true);
    }

//    @Override
//    public void removeEntity(int position) {
//        ScOrderItem entity = getEntity(position);
//        if (entity == null){
//            return;
//        }
//
//        CashierShopcartService.getInstance().deleteById(String.valueOf(entity.getId()));
//        //刷新列表
//        entityList.remove(position);
//        notifyItemRemoved(position);
//
//        if (adapterListener != null) {
//            adapterListener.onDataSetChanged(false);
//        }
//    }

    public double getBcount() {
        double count = 0;
        if (entityList != null && entityList.size() > 0) {
            for (ScOrderItem entity : entityList) {
                count += entity.getBcount();
            }
        }

        return count;
    }



    public void notifyDataSetChanged(int position, boolean needScroll){
        notifyItemChanged(position);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(needScroll);
        }
    }

    public void notifyDataSetChanged(boolean needScroll){
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(needScroll);
        }
    }

}
