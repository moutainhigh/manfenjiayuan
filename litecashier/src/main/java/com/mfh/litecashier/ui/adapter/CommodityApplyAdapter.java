package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.manfenjiayuan.business.bean.ScGoodsSku;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 商品中心－－采购商品
 * Created by bingshanguxue on 15/8/5.
 */
public class CommodityApplyAdapter
        extends RegularAdapter<ScGoodsSku, CommodityApplyAdapter.ProductViewHolder> {


    public CommodityApplyAdapter(Context context, List<ScGoodsSku> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void addToShopcart(ScGoodsSku goods);
        void onShowDetail(ScGoodsSku goods);
        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_ordergoods_content, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        ScGoodsSku entity = entityList.get(position);

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error).into(holder.ivHeader);

        holder.tvName.setText(entity.getSkuName());
        holder.tvDescription.setText(entity.getBarcode());
        if (StringUtils.isEmpty(entity.getBuyUnit())) {
            holder.tvPurchasePrice.setText(String.format("%.2f", entity.getBuyPrice()));
        }
        else{
            holder.tvPurchasePrice.setText(String.format("%.2f／%s", entity.getBuyPrice(), entity.getBuyUnit()));
        }
//        holder.tvMinimumOrderQuantity.setText(String.format("%.2f", entity.getStartNum()));
        holder.tvStockQuantity.setText(String.format("%.2f", entity.getQuantity()));
        holder.tvPakcageNum.setText(String.format("%.2f", entity.getPackageNum()));
//        holder.tvMonthlySales.setText(String.format("%.2f", entity.getSellNum()));
//        holder.tvDailySales.setText(String.format("%.2f", entity.getSellNum()));
//        holder.ibShopcart.setSelected(false);
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
//        @Bind(R.id.tv_minimum_order_quantity)
//        TextView tvMinimumOrderQuantity;
        @Bind(R.id.tv_stock_quantity)
        TextView tvStockQuantity;
//        @Bind(R.id.tv_monthly_sales)
//        TextView tvMonthlySales;
//        @Bind(R.id.tv_daily_sales)
//        TextView tvDailySales;
        @Bind(R.id.ib_shopcart)
        ImageButton ibShopcart;

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
         * 详情
         * */
        @OnClick(R.id.iv_header)
        public void showDetailInfo() {
            int position = getAdapterPosition();
            if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                return;
            }

            final ScGoodsSku original = entityList.get(position);
            if (original == null) {
                return;
            }

            if (adapterListener != null) {
                adapterListener.onShowDetail(original);
            }
        }

        @OnClick(R.id.ib_shopcart)
        public void addToShopcart() {
            int position = getAdapterPosition();
            if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                return;
            }

            if (adapterListener != null) {
                adapterListener.addToShopcart(entityList.get(position));
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
