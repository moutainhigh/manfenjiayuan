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
import com.manfenjiayuan.business.bean.InvSendOrderItem;
import com.manfenjiayuan.business.utils.MUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 采购订单－－订单明细
 * Created by bingshanguxue on 15/8/5.
 */
public class PurchaseSendGoodsAdapter
        extends RegularAdapter<InvSendOrderItem, PurchaseSendGoodsAdapter.ProductViewHolder> {

    public PurchaseSendGoodsAdapter(Context context, List<InvSendOrderItem> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_purchase_send_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        InvSendOrderItem entity = entityList.get(position);

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error)
                .into(holder.ivHeader);

        holder.tvName.setText(entity.getProductName());
        holder.tvBarcode.setText(entity.getBarcode());
        if (StringUtils.isEmpty(entity.getBuyUnit())){
            holder.tvBuyprice.setText(MUtils.formatDouble(entity.getPrice(), "无"));
        }
        else{
            holder.tvBuyprice.setText(MUtils.formatDoubleWithSuffix(entity.getPrice(), "/", entity.getBuyUnit()));
        }
        holder.tvQuantity.setText(MUtils.formatDouble(entity.getAskTotalCount(), "无"));
        holder.tvReceiveCount.setText(MUtils.formatDouble(entity.getReceiveCount(), "无"));
        holder.tvAmount.setText(MUtils.formatDouble(entity.getAmount(), "无"));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_barcode)
        TextView tvBarcode;
        @Bind(R.id.tv_buyprice)
        TextView tvBuyprice;
        @Bind(R.id.tv_quantity)
        TextView tvQuantity;
        @Bind(R.id.tv_receiveCount)
        TextView tvReceiveCount;
        @Bind(R.id.tv_amount)
        TextView tvAmount;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int position = getAdapterPosition();
//                    if (position < 0 || position >= entityList.size()) {
////                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
//                        return;
//                    }
//
//                    if (adapterListener != null) {
//                        adapterListener.onItemClick(itemView, getAdapterPosition());
//                    }
//                }
//            });
        }
    }

    @Override
    public void setEntityList(List<InvSendOrderItem> entityList) {
        super.setEntityList(entityList);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }
}
