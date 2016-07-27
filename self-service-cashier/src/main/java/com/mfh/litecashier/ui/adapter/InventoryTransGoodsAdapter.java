package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.framework.api.invSendIoOrder.InvSendIoOrderItem;
import com.mfh.litecashier.ui.dialog.DoubleInputDialog;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 库存－－库存调拨
 * Created by Nat.ZZN on 15/8/5.
 */
public class InventoryTransGoodsAdapter
        extends RegularAdapter<InvSendIoOrderItem, InventoryTransGoodsAdapter.ProductViewHolder> {

    private boolean isItemEditabled;//是否可以修改

    private DoubleInputDialog changeQuantityDialog;

    public InventoryTransGoodsAdapter(Context context, List<InvSendIoOrderItem> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_inventory_allocation_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        InvSendIoOrderItem entity = entityList.get(position);

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error)
                .into(holder.ivHeader);

        holder.tvName.setText(entity.getProductName());
        holder.tvBarcode.setText(entity.getBarcode());
        if (StringUtils.isEmpty(entity.getUnitSpec())) {
            holder.tvBuyprice.setText(String.format("%.2f", entity.getPrice()));
        }
        else{
            holder.tvBuyprice.setText(String.format("%.2f/%s", entity.getPrice(), entity.getUnitSpec()));
        }
        holder.tvQuantity.setText(String.format("%.2f", entity.getQuantityCheck()));
        if (isItemEditabled){
            Drawable drawable = ContextCompat.getDrawable(mContext, R.mipmap.ic_marker_edit);
            holder.tvQuantity.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
//            holder.tvQuantity.setCompoundDrawables(null, null, drawable, null);
//            holder.tvQuantity.setCompoundDrawablesRelative(null, null, drawable, null);
        }
        else{
//            holder.tvQuantity.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
//            holder.tvQuantity.setCompoundDrawables(null, null, null, null);
            holder.tvQuantity.setCompoundDrawablesRelative(null, null, null, null);
        }
        holder.tvAmount.setText(String.format("%.2f", entity.getAmount()));
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

        /**
         * 修改数目
         */
        @OnClick(R.id.ll_quantity)
        public void changeQuantity() {
            if (!isItemEditabled){
                return;
            }
            final int position = getAdapterPosition();

            final InvSendIoOrderItem original = entityList.get(position);
            if (original == null) {
                return;
            }

            if (changeQuantityDialog == null) {
                changeQuantityDialog = new DoubleInputDialog(mContext);
                changeQuantityDialog.setCancelable(true);
                changeQuantityDialog.setCanceledOnTouchOutside(true);
            }
            changeQuantityDialog.init("采购量", 2, original.getQuantityCheck(), new DoubleInputDialog.OnResponseCallback() {
                @Override
                public void onQuantityChanged(Double quantity) {
                    original.setQuantityCheck(quantity);
                    original.setAmount(original.getPrice() * original.getQuantityCheck());

                    notifyDataSetChanged();

                    if (adapterListener != null) {
                        adapterListener.onDataSetChanged();
                    }
                }
            });
            changeQuantityDialog.show();
        }
    }

    @Override
    public void setEntityList(List<InvSendIoOrderItem> entityList) {
        super.setEntityList(entityList);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public void setIsItemEditabled(boolean isItemEditabled) {
        this.isItemEditabled = isItemEditabled;
        notifyDataSetChanged();
    }
}
