package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.bean.InvCheckOrderItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 库存盘点－－商品明细
 * Created by Nat.ZZN on 15/8/5.
 */
public class StockCheckGoodsAdapter
        extends RegularAdapter<InvCheckOrderItem, StockCheckGoodsAdapter.ProductViewHolder> {

    public StockCheckGoodsAdapter(Context context, List<InvCheckOrderItem> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_stockcheck_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        InvCheckOrderItem entity = entityList.get(position);

        holder.tvName.setText(entity.getProductName());
        holder.tvDescription.setText(entity.getBarcode());
        holder.tvCheckQuantity.setText(String.format("%.2f", entity.getQuantityCheck()));
        holder.tvSystemInventory.setText(String.format("%.2f", entity.getQuantityInv()));
        Double lossQuantity = entity.getQuantityCheck() - entity.getQuantityInv();
        holder.tvLossQuantity.setText(String.format("%.2f", lossQuantity));
        holder.tvCostPrice.setText(String.format("%.2f", entity.getCostPrice()));
        holder.tvLossAmount.setText(String.format("%.2f", entity.getCostPrice() * lossQuantity));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_header)
        ImageView ivHeader;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_description)
        TextView tvDescription;
        @BindView(R.id.tv_check_quantity)
        TextView tvCheckQuantity;
        @BindView(R.id.tv_system_inventory)
        TextView tvSystemInventory;
        @BindView(R.id.tv_loss_quantity)
        TextView tvLossQuantity;
        @BindView(R.id.tv_costprice)
        TextView tvCostPrice;
        @BindView(R.id.tv_loss_amount)
        TextView tvLossAmount;

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
    public void setEntityList(List<InvCheckOrderItem> entityList) {
        super.setEntityList(entityList);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }
}
