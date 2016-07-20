package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.database.entity.PosOrderItemEntity;


import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 线下门店订单流水－明细
 * Created by Nat.ZZN on 15/8/5.
 */
public class LocalOrderflowGoodsAdapter
        extends RegularAdapter<PosOrderItemEntity, LocalOrderflowGoodsAdapter.ProductViewHolder> {

    public LocalOrderflowGoodsAdapter(Context context, List<PosOrderItemEntity> entityList) {
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
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_orderflow_local_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        PosOrderItemEntity entity = entityList.get(position);

        holder.tvName.setText(String.format("%s\n%s", entity.getBarcode(), entity.getName()));
        holder.tvCostPrice.setText(String.format("%.2f", entity.getCostPrice()));
        holder.tvQuantity.setText(String.format("%.2f", entity.getBcount()));
        holder.tvAmount.setText(String.format("%.2f", entity.getBcount() * entity.getCostPrice()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_costprice)
        TextView tvCostPrice;
        @Bind(R.id.tv_quantity)
        TextView tvQuantity;
        @Bind(R.id.tv_amount)
        TextView tvAmount;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    getLayoutPosition()
//                    int position = getAdapterPosition();
//                    if (position < 0 || position >= entityList.size()) {
////                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
//                        return;
//                    }
////                    notifyDataSetChanged();//getAdapterPosition() return -1.
////
////                    if (adapterListener != null){
////                        adapterListener.onItemClick(itemView, position);
////                    }
//                }
//            });
        }
    }

    @Override
    public void setEntityList(List<PosOrderItemEntity> entityList) {
        super.setEntityList(entityList);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }
}
