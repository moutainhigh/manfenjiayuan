package com.mfh.litecashier.ui.fragment.purchase.manual;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.business.widget.NumberPickerView;
import com.mfh.framework.api.scGoodsSku.ScGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.database.entity.PurchaseGoodsEntity;
import com.mfh.litecashier.database.entity.PurchaseOrderEntity;
import com.mfh.litecashier.database.logic.PurchaseGoodsService;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 采购商品
 * Created by bingshanguxue on 16/07/20.
 */
public class ManualPurchaseGoodsAdapter
        extends RegularAdapter<ScGoodsSku, ManualPurchaseGoodsAdapter.ProductViewHolder> {

    public ManualPurchaseGoodsAdapter(Context context, List<ScGoodsSku> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void addToShopcart(ScGoodsSku goods, int quantity);

        void onShowDetail(ScGoodsSku goods);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_ordergoods_content,
                parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        ScGoodsSku goodsSku = entityList.get(position);

        Glide.with(mContext).load(goodsSku.getImgUrl())
                .error(R.mipmap.ic_image_error).into(holder.ivHeader);

        holder.tvName.setText(goodsSku.getSkuName());
        holder.tvDescription.setText(goodsSku.getBarcode());
        if (StringUtils.isEmpty(goodsSku.getBuyUnit())) {
            holder.tvPurchasePrice.setText(String.format("%.2f", goodsSku.getBuyPrice()));
        } else {
            holder.tvPurchasePrice.setText(String.format("%.2f／%s",
                    goodsSku.getBuyPrice(), goodsSku.getBuyUnit()));
        }
//        holder.tvMinimumOrderQuantity.setText(String.format("%.2f", entity.getStartNum()));
        holder.tvStockQuantity.setText(String.format("%.2f", goodsSku.getQuantity()));
        holder.tvPakcageNum.setText(String.format("%.2f", goodsSku.getPackageNum()));


//        PurchaseGoodsEntity entity = PurchaseGoodsService
//                .getInstance().fetchGoods(PurchaseOrderEntity.PURCHASE_TYPE_MANUAL,
//                        goodsSku.getProviderId(), goodsSku.getBarcode());

        String sqlWhere = String.format("purchaseType = '%d' and proSkuId = '%d' " +
                        "and barcode = '%s'", PurchaseOrderEntity.PURCHASE_TYPE_MANUAL,
                goodsSku.getProSkuId(), goodsSku.getBarcode());
        List<PurchaseGoodsEntity> entities = PurchaseGoodsService.getInstance().queryAllBy(sqlWhere);
        if (entities != null && entities.size() > 0) {
            PurchaseGoodsEntity entity = entities.get(0);
            holder.mNumberPickerView.setValue(String.format("%.0f", entity.getQuantityCheck()));
        } else {
            holder.mNumberPickerView.setValue(null);
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_description)
        TextView tvDescription;
        @Bind(R.id.tv_purchaseprice)
        TextView tvPurchasePrice;
        @Bind(R.id.tv_packageNum)
        TextView tvPakcageNum;
        @Bind(R.id.tv_stock_quantity)
        TextView tvStockQuantity;
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
                        ScGoodsSku goods = getEntity(position);
                        if (goods == null) {
                            return;
                        }
                        if (adapterListener != null) {
                            adapterListener.addToShopcart(goods, value);
                        }

                    } catch (Exception ex) {
                        ZLogger.e(ex.toString());
                    }
                }
            });
        }

        /**
         * 详情
         */
        @OnClick(R.id.iv_header)
        public void showDetailInfo() {
            int position = getAdapterPosition();
            final ScGoodsSku original = getEntity(position);
            if (original == null) {
                return;
            }

            if (adapterListener != null) {
                adapterListener.onShowDetail(original);
            }
        }
    }

    @Override
    public void setEntityList(List<ScGoodsSku> entityList) {
        super.setEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }
}
