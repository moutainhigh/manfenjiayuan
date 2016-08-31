package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.widget.NumberPickerView;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.database.entity.PurchaseShopcartEntity;
import com.mfh.litecashier.database.logic.PurchaseShopcartService;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 生鲜采购--购物车商品明细
 * Created by bingshanguxue on 15/8/5.
 */
public class PurchaseFruitShopcartGoodsAdapter
        extends SwipAdapter<PurchaseShopcartEntity, PurchaseFruitShopcartGoodsAdapter.ProductViewHolder> {

    public interface OnAdapterListener {
        void onDataSetChanged(boolean isNeedReloadOrder);
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public PurchaseFruitShopcartGoodsAdapter(Context context, List<PurchaseShopcartEntity> entityList) {
        super(context, entityList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_purchase_fruit_shopcart_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        PurchaseShopcartEntity entity = entityList.get(position);

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error)
                .into(holder.ivHeader);

        holder.tvName.setText(entity.getName());
        holder.tvBarcode.setText(entity.getBarcode());
        holder.tvBuyPrice.setText(MUtils.formatDouble(null, null,
                entity.getPrice(), "无", "/", entity.getUnit()));

        holder.mNumberPickerView.setValue(String.format("%.0f", entity.getQuantity()));
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
        @Bind(R.id.numberPickerView)
        NumberPickerView mNumberPickerView;

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

            mNumberPickerView.setonOptionListener(new NumberPickerView.onOptionListener() {
                @Override
                public void onPreIncrease() {

                }

                @Override
                public void onPreDecrease() {

                }

                @Override
                public void onValueChanged(int value) {
                    // TODO: 6/3/16
                    try {
                        int position = getAdapterPosition();
                        PurchaseShopcartEntity entity = getEntity(position);
                        if (entity == null) {
                            return;
                        }
                        if (value == 0) {
                            String sqlWhere = String.format("purchaseType = '%d' and providerId = '%d' and barcode = '%s'",
                                    PurchaseShopcartEntity.PURCHASE_TYPE_FRESH,
                                    entity.getProviderId(), entity.getBarcode());
                            PurchaseShopcartService.getInstance()
                                    .deleteBy(sqlWhere);
                        } else {
                            //注：这里不修改更新时间，避免刷新时顺序重新排序
                            PurchaseShopcartService.getInstance()
                                    .saveOrUpdateFreshGoods(entity,
                                            Double.valueOf(String.valueOf(value)), false);
                        }

                        if (adapterListener != null) {
                            adapterListener.onDataSetChanged(true);
                        }
                    } catch (Exception ex) {
                        ZLogger.e(ex.toString());
                    }
                }
            });
        }
    }


    @Override
    public void setEntityList(List<PurchaseShopcartEntity> entityList) {
        super.setEntityList(entityList);

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(false);
        }
    }

    @Override
    public void removeEntity(int position) {
        try {
            PurchaseShopcartEntity entity = getEntity(position);
            if (entity == null) {
                return;
            }

            //刷新列表
            entityList.remove(position);
            notifyItemRemoved(position);

            // TODO: 6/5/16 删除数据库数据
            String sqlWhere = String.format("purchaseType = '%d' and providerId = '%d' and barcode = '%s'",
                    PurchaseShopcartEntity.PURCHASE_TYPE_FRESH,
                    entity.getProviderId(), entity.getBarcode());
            PurchaseShopcartService.getInstance()
                    .deleteBy(sqlWhere);
//
            if (adapterListener != null) {
                adapterListener.onDataSetChanged(true);
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }
}
