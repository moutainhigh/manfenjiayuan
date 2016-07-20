package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.manfenjiayuan.business.bean.ScGoodsSku;
import com.manfenjiayuan.business.utils.MUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 商品－－库存商品
 * Created by bingshanguxue on 15/8/5.
 */
public class InventoryCostGoodsAdapter
        extends RegularAdapter<ScGoodsSku, InventoryCostGoodsAdapter.ProductViewHolder> {

    public InventoryCostGoodsAdapter(Context context, List<ScGoodsSku> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onDataSetChanged();
        void onUpdateCostPrice(ScGoodsSku goods);
        void onUpdateUpperLimit(ScGoodsSku goods);
        void onOrderItem(ScGoodsSku goods);
        void onShowDetail(ScGoodsSku goods);
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_stockcost_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        ScGoodsSku entity = entityList.get(position);

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error)
                .into(holder.ivHeader);

        holder.tvName.setText(entity.getSkuName());
        holder.tvBarcode.setText(entity.getBarcode());
        holder.tvUpperLimit.setText(MUtils.formatDouble(null, null, entity.getUpperLimit(), "无", null, null));
        holder.tvQuantity.setText(MUtils.formatDouble(entity.getQuantity(), "无"));
        holder.tvMonthlySales.setText(String.format("%.2f", entity.getSellMonthNum()));

        holder.tvBuyPrice.setText(MUtils.formatDouble(null, null, entity.getBuyPrice(), "无", "/", entity.getUnit()));
        holder.tvCostPrice.setText(MUtils.formatDouble(null, null, entity.getCostPrice(), "无", "/", entity.getUnit()));
//        holder.tvSuppliers.setText(entity.getSupplyName());
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_barcode)
        TextView tvBarcode;
        @Bind(R.id.tv_upperLimit)
        TextView tvUpperLimit;
        @Bind(R.id.tv_quantity)
        TextView tvQuantity;
        @Bind(R.id.tv_monthlysales)
        TextView tvMonthlySales;
        @Bind(R.id.tv_buyprice)
        TextView tvBuyPrice;
        @Bind(R.id.tv_costprice)
        TextView tvCostPrice;

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

        /**
         * 修改售价
         * */
        @OnClick(R.id.ll_costprice)
        public void changeCostPrice() {
            final int position = getAdapterPosition();
            if (entityList == null || position < 0 || position >= entityList.size()){
//                ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                return;
            }

            final ScGoodsSku original = entityList.get(position);
            if (original == null) {
                return;
            }

            if (adapterListener != null){
                adapterListener.onUpdateCostPrice(original);
            }
        }


        /**
         *修改排面库存
         * */
        @OnClick(R.id.ll_upperLimit)
        public void changeUpperLimit() {
            final int position = getAdapterPosition();
            if (entityList == null || position < 0 || position >= entityList.size()){
//                ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                return;
            }

            final ScGoodsSku original = entityList.get(position);
            if (original == null) {
                return;
            }

            if (adapterListener != null){
                adapterListener.onUpdateUpperLimit(original);
            }
        }

        /**
         * 订货
         * */
        @OnClick(R.id.ib_order_goods)
        public void orderGoods() {
            final int position = getAdapterPosition();
            if (entityList == null || position < 0 || position >= entityList.size()){
//                ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                return;
            }

            if (adapterListener != null) {
                adapterListener.onOrderItem(entityList.get(position));
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
