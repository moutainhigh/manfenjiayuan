package com.manfenjiayuan.cashierdisplay.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.cashierdisplay.R;
import com.manfenjiayuan.cashierdisplay.bean.PosOrderItemEntity;
import com.manfenjiayuan.cashierdisplay.bean.PriceType;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * <h1>收银商品适配器</h1>
 * <ul>
 * <li>支持滑动删除</li>
 * </ul>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class OrderItemsAdapter
        extends RegularAdapter<PosOrderItemEntity, OrderItemsAdapter.CashierViewHolder> {

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
        @Bind(R.id.tv_finalPrice)
        TextView tvFinalPrice;
        @Bind(R.id.tv_quantity)
        TextView tvCount;
        @Bind(R.id.tv_amount)
        TextView tvAmount;

//        ShopcartEntity orderItemEntity;

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

    public OrderItemsAdapter(Context context, List<PosOrderItemEntity> entityList) {
        super(context, entityList);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CashierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CashierViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_cashier,
                parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final CashierViewHolder holder, final int position) {
        // - get element from your dataset at this position
        PosOrderItemEntity entity = entityList.get(position);

        // - replace the contents of the view with that element
        holder.tvName.setText(entity.getName());
        holder.tvFinalPrice.setText(String.format("%.2f", entity.getFinalPrice()));
        //计件：整数；记重：3位小数
        if (entity.getPriceType() == PriceType.WEIGHT) {
            holder.tvCount.setText(String.format("%.3f", entity.getBcount()));
        } else {
            holder.tvCount.setText(String.format("%.2f", entity.getBcount()));
        }
        holder.tvAmount.setText(String.format("%.2f", entity.getFinalAmount()));
    }

    @Override
    public void setEntityList(List<PosOrderItemEntity> entityList) {
        super.setEntityList(entityList);

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(true);
        }
    }


}
