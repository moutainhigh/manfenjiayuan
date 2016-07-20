package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.bean.InvLossOrderItem;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 库存批次－－商品明细
 * Created by Nat.ZZN on 15/8/5.
 */
public class StockLossGoodsAdapter
        extends RegularAdapter<InvLossOrderItem, StockLossGoodsAdapter.ProductViewHolder> {

    public StockLossGoodsAdapter(Context context, List<InvLossOrderItem> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_stockloss_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        InvLossOrderItem entity = entityList.get(position);

//        if (StringUtils.isEmpty(entity.getImgUrl())){
            holder.ivHeader.setImageResource(R.mipmap.ic_image_error);
//        }
//        else{
//            kjb.display(holder.ivHeader, entity.getImgUrl(), R.mipmap.ic_image_error, 0, 0,null);
//        }
        holder.tvName.setText(entity.getProductName());
        holder.tvDescription.setText(entity.getBarcode());
        holder.tvQuantity.setText(String.format("%.2f", entity.getQuantityCheck()));
        holder.tvBuyPrice.setText(String.format("%.2f", entity.getPrice()));
        holder.tvAmount.setText(String.format("%.2f", entity.getQuantityCheck() * entity.getPrice()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_description)
        TextView tvDescription;
        @Bind(R.id.tv_quantity)
        TextView tvQuantity;
        @Bind(R.id.tv_buyprice)
        TextView tvBuyPrice;
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
    public void setEntityList(List<InvLossOrderItem> entityList) {
        super.setEntityList(entityList);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }
}
