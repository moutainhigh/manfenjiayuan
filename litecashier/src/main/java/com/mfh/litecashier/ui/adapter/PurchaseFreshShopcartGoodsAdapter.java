package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.PurchaseShopcartGoodsWrapper;
import com.mfh.litecashier.ui.dialog.ChangeQuantityDialog;
import com.mfh.litecashier.utils.FreshShopcartHelper;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 生鲜采购--购物车商品明细
 * Created by bingshanguxue on 15/8/5.
 */
public class PurchaseFreshShopcartGoodsAdapter
        extends SwipAdapter<PurchaseShopcartGoodsWrapper, PurchaseFreshShopcartGoodsAdapter.ProductViewHolder> {

    private ChangeQuantityDialog changeQuantityDialog;

    public interface OnAdapterListener {
        void onDataSetChanged(boolean isNeedReloadOrder);
    }
    private OnAdapterListener adapterListener;
    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public PurchaseFreshShopcartGoodsAdapter(Context context, List<PurchaseShopcartGoodsWrapper> entityList) {
        super(context, entityList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_purchase_fresh_shopcart_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        PurchaseShopcartGoodsWrapper entity = entityList.get(position);

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error)
                .into(holder.ivHeader);

        holder.tvName.setText(entity.getProductName());
        holder.tvBarcode.setText(entity.getBarcode());
        holder.tvBuyPrice.setText(MUtils.formatDouble(null, null,
                entity.getBuyPrice(), "无", "/", entity.getUnit()));

        //TODO,注意采购量和库存区分
        holder.tvQuantity.setText(String.format("%.2f", entity.getQuantityCheck()));
     }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_barcode)
        TextView tvBarcode;
        @Bind(R.id.tv_buyprice)
        TextView tvBuyPrice;
        @Bind(R.id.tv_quantity)
        TextView tvQuantity;

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

        /**
         * 修改采购量
         * */
        @OnClick(R.id.ll_quantity)
        public void changeQuantity() {
            final int position = getAdapterPosition();
            if (entityList == null || position < 0 || position >= entityList.size()){
//                ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                return;
            }

            final PurchaseShopcartGoodsWrapper original = entityList.get(position);
            if (original == null) {
                return;
            }

            if (changeQuantityDialog == null) {
                changeQuantityDialog = new ChangeQuantityDialog(mContext);
                changeQuantityDialog.setCancelable(true);
                changeQuantityDialog.setCanceledOnTouchOutside(true);
            }
            changeQuantityDialog.init("采购量", 2, original.getQuantityCheck(), new ChangeQuantityDialog.OnResponseCallback() {
                @Override
                public void onQuantityChanged(Double quantity) {
                    if (quantity < 1D){
                        DialogUtil.showHint("采购量不能为空");
                        return;
                    }
                    if (quantity < original.getStartNum()){
                        DialogUtil.showHint("采购量不能低于起配量");
                        return;
                    }

                    original.setQuantityCheck(quantity);
                    notifyDataSetChanged();

                    boolean isNeedReloadOrder = FreshShopcartHelper.getInstance().onDataSetChanged();

                    if (adapterListener != null) {
                        adapterListener.onDataSetChanged(isNeedReloadOrder);
                    }
                }
            });
            changeQuantityDialog.show();
        }
    }

    @Override
    public void setEntityList(List<PurchaseShopcartGoodsWrapper> entityList) {
        super.setEntityList(entityList);

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(false);
        }
    }

    @Override
    public void removeEntity(int position) {
        try {
            if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                return;
            }
//            final PurchaseShopcartGoods original = entityList.get(position);

            //刷新列表
            entityList.remove(position);
            notifyItemRemoved(position);

//            PurchaseShopcartHelper.getInstance().remove(original);
            boolean isNeedReloadOrder = FreshShopcartHelper.getInstance().onDataSetChanged();

            if (adapterListener != null) {
                adapterListener.onDataSetChanged(isNeedReloadOrder);
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }
}
