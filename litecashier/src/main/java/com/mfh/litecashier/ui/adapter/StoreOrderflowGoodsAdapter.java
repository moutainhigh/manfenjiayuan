package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.bean.PosOrderItem;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 线下门店订单流水－明细
 * Created by Nat.ZZN on 15/8/5.
 */
public class StoreOrderflowGoodsAdapter
        extends RegularAdapter<PosOrderItem, StoreOrderflowGoodsAdapter.ProductViewHolder> {

    public StoreOrderflowGoodsAdapter(Context context, List<PosOrderItem> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_orderflow_store_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        PosOrderItem entity = entityList.get(position);

//        ZLogger.d("entity = " + JSON.toJSONString(entity));
        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error)
                .into(holder.ivHeader);

        holder.tvName.setText(entity.getProductName());
        holder.tvBarcode.setText(entity.getBarcode());
        if (StringUtils.isEmpty(entity.getUnitName())) {
            holder.tvCostPrice.setText(String.format("%.2f", entity.getPrice()));
        } else {
            holder.tvCostPrice.setText(String.format("%.2f／%s", entity.getPrice(), entity.getUnitName()));
        }
        holder.tvQuantity.setText(String.format("%.2f", entity.getBcount()));
        holder.tvAmount.setText(String.format("%.2f", entity.getAmount()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_barcode)
        TextView tvBarcode;
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
    public void setEntityList(List<PosOrderItem> entityList) {
        super.setEntityList(entityList);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }
}
