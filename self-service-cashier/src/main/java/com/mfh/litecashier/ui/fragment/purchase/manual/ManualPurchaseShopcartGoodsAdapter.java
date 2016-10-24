package com.mfh.litecashier.ui.fragment.purchase.manual;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bingshanguxue.vector_uikit.NumberPickerView;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.database.entity.PurchaseGoodsEntity;
import com.mfh.litecashier.database.logic.PurchaseGoodsService;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 商品采购－购物车商品明细
 * Created by bingshanguxue on 16/07/20.
 */
public class ManualPurchaseShopcartGoodsAdapter
        extends SwipAdapter<PurchaseGoodsEntity, ManualPurchaseShopcartGoodsAdapter.ProductViewHolder> {

    public interface OnAdapterListener {
        void onDataSetChanged(boolean isNeedReloadOrder);
    }
    private OnAdapterListener adapterListener;
    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public ManualPurchaseShopcartGoodsAdapter(Context context, List<PurchaseGoodsEntity> entityList) {
        super(context, entityList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_purchase_shopcart_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        PurchaseGoodsEntity entity = entityList.get(position);

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error)
                .into(holder.ivHeader);

        holder.tvName.setText(entity.getProductName());
        holder.tvBarcode.setText(entity.getBarcode());
        if (StringUtils.isEmpty(entity.getUnit())) {
            holder.tvBuyPrice.setText(String.format("%.2f", entity.getBuyPrice()));
        }
        else{
            holder.tvBuyPrice.setText(String.format("%.2f/%s", entity.getBuyPrice(), entity.getUnit()));
        }
        holder.tvStartNum.setText(String.format("%.2f", 0D));

        //TODO,注意采购量和库存区分
        holder.mNumberPickerView.setValue(String.format("%.0f", entity.getQuantityCheck()));
        holder.tvAmount.setText(String.format("%.2f", entity.getQuantityCheck() * entity.getBuyPrice()));
     }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_barcode)
        TextView tvBarcode;
        @Bind(R.id.tv_startnum)
        TextView tvStartNum;
        @Bind(R.id.tv_buyprice)
        TextView tvBuyPrice;
        @Bind(R.id.numberPickerView)
        NumberPickerView mNumberPickerView;
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

            mNumberPickerView.setonOptionListener(new NumberPickerView.onOptionListener() {


                @Override
                public void onPreIncrease(int value) {

                }

                @Override
                public void onPreDecrease(int value) {

                }

                @Override
                public void onValueChanged(int value) {
                    // TODO: 6/3/16
                    try {
                        int position = getAdapterPosition();

                        if (value == 0) {
                            removeEntity(position);
                        }
                        else{
                            PurchaseGoodsEntity entity = getEntity(position);
                            if (entity == null) {
                                return;
                            }

                            entity.setQuantityCheck(Double.valueOf(String.valueOf(value)));
                            PurchaseGoodsService.getInstance().saveOrUpdate(entity);
                            PurchaseHelper.getInstance().arrange(entity.getPurchaseType(),
                                    entity.getProviderId());
                            notifyDataSetChanged();

                            if (adapterListener != null) {
                                adapterListener.onDataSetChanged(true);
                            }
                        }
                    } catch (Exception ex) {
                        ZLogger.e(ex.toString());
                    }
                }
            });
        }
    }

    @Override
    public void setEntityList(List<PurchaseGoodsEntity> entityList) {
        super.setEntityList(entityList);

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(false);
        }
    }

    @Override
    public void removeEntity(int position) {
        try {
            final PurchaseGoodsEntity original = getEntity(position);
            if (original == null) {
                return;
            }
            //刷新列表
            entityList.remove(position);
            notifyItemRemoved(position);

            PurchaseGoodsService.getInstance().deleteById(String.valueOf(original.getId()));
            PurchaseHelper.getInstance().arrange(original.getPurchaseType(),
                    original.getProviderId());

            if (adapterListener != null) {
                adapterListener.onDataSetChanged(true);
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }
}
