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
import com.manfenjiayuan.business.bean.InvSendIoOrderItem;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 采购退货－－订单明细
 * Created by Nat.ZZN on 15/8/5.
 */
public class PurchaseReturnGoodsAdapter
        extends RegularAdapter<InvSendIoOrderItem, PurchaseReturnGoodsAdapter.ProductViewHolder> {

    public PurchaseReturnGoodsAdapter(Context context, List<InvSendIoOrderItem> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_purchase_return_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        InvSendIoOrderItem entity = entityList.get(position);

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error)
                .into(holder.ivHeader);

        holder.tvName.setText(entity.getProductName());
        holder.tvBarcode.setText(entity.getBarcode());
        if (StringUtils.isEmpty(entity.getUnitSpec())){
            holder.tvBuyprice.setText(String.format("%.2f", entity.getPrice()));
        }
        else{
            holder.tvBuyprice.setText(String.format("%.2f/%s", entity.getPrice(), entity.getUnitSpec()));
        }
        holder.tvQuantity.setText(String.format("%.2f", entity.getQuantityCheck()));
        holder.tvAmount.setText(String.format("%.2f", entity.getPrice() * entity.getQuantityCheck()));
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
        @Bind(R.id.tv_amount)
        TextView tvAmount;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, getAdapterPosition());
                    }
                }
            });
        }

    }

    @Override
    public void setEntityList(List<InvSendIoOrderItem> entityList) {
        super.setEntityList(entityList);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
