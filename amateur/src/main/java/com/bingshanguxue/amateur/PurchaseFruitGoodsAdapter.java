package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.business.widget.NumberPickerView;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.database.entity.PurchaseShopcartEntity;
import com.mfh.litecashier.database.logic.PurchaseShopcartService;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 生鲜采购商品
 * Created by bingshanguxue on 15/8/5.
 */
public class PurchaseFruitGoodsAdapter
        extends RegularAdapter<ChainGoodsSku, PurchaseFruitGoodsAdapter.ProductViewHolder> {

    public PurchaseFruitGoodsAdapter(Context context, List<ChainGoodsSku> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onShowDetail(ChainGoodsSku goods);
        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_purchase_fruit_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        try{
            ChainGoodsSku entity = entityList.get(position);

            Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error)
                    .into(holder.ivHeader);

            holder.tvName.setText(entity.getSkuName());
            holder.tvBarcode.setText(entity.getBarcode());
            holder.tvUnit.setText(entity.getBuyUnit());
            holder.tvQuantity.setText(MUtils.formatDouble(entity.getQuantity(), "无"));
            holder.tvPrice.setText(MUtils.formatDouble(entity.getHintPrice(), "无"));

            PurchaseShopcartEntity purchaseShopcartEntity = PurchaseShopcartService
                    .getInstance().getFreshGoods(entity.getTenantId(), entity.getBarcode());
            if (purchaseShopcartEntity != null){
                holder.mNumberPickerView.setValue(String.format("%.0f", purchaseShopcartEntity.getQuantity()));
            }
            else{
                holder.mNumberPickerView.setValue(null);
            }
        }
        catch (Exception e){
            ZLogger.e(e.toString());
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_barcode)
        TextView tvBarcode;
        @Bind(R.id.tv_unit)
        TextView tvUnit;
        @Bind(R.id.tv_price)
        TextView tvPrice;
        @Bind(R.id.tv_quantity)
        TextView tvQuantity;
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
//                    final ChainGoodsSku original = getEntity(position);
//                    if (original == null) {
//                        return;
//                    }
//
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
                    try{
                        int position = getAdapterPosition();
                        ChainGoodsSku entity = getEntity(position);
                        if (entity == null){
                            return;
                        }
                        if (value == 0){
                            String sqlWhere = String.format("purchaseType = '%d' and providerId = '%d' and barcode = '%s'",
                                    PurchaseShopcartEntity.PURCHASE_TYPE_FRESH,
                                    entity.getTenantId(), entity.getBarcode());
                            PurchaseShopcartService.getInstance().deleteBy(sqlWhere);

                        }
                        else {
                            PurchaseShopcartService.getInstance()
                                    .saveOrUpdateFreshGoods(entity,
                                            Double.valueOf(String.valueOf(value)));
                        }

                        if (adapterListener != null) {
                            adapterListener.onDataSetChanged();
                        }
                    }
                    catch (Exception ex){
                        ZLogger.e(ex.toString());
                    }
                }
            });
        }

        /**
         * 详情
         * */
        @OnClick(R.id.iv_header)
        public void showDetailInfo() {
            int position = getAdapterPosition();
            final ChainGoodsSku original = getEntity(position);
            if (original == null) {
                return;
            }

            if (adapterListener != null) {
                adapterListener.onShowDetail(original);
            }
        }
    }

    @Override
    public void setEntityList(List<ChainGoodsSku> entityList) {
        super.setEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }
}
