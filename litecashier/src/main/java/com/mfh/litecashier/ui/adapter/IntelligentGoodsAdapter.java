package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.business.bean.InvSendOrderItem;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.dialog.ChangeQuantityDialog;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 智能订货
 * Created by bingshanguxue on 15/8/5.
 */
public class IntelligentGoodsAdapter
        extends RegularAdapter<InvSendOrderItem, IntelligentGoodsAdapter.ProductViewHolder> {


    public IntelligentGoodsAdapter(Context context, List<InvSendOrderItem> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onShowDetail(InvSendOrderItem goods);
        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    private ChangeQuantityDialog changeQuantityDialog = null;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_intelligent_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        InvSendOrderItem entity = entityList.get(position);

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error).into(holder.ivHeader);

        holder.tvName.setText(entity.getProductName());
        holder.tvDescription.setText(entity.getBarcode());
        holder.tvPurchasePrice.setText(MUtils.formatDouble(null, null, entity.getPrice(), "", "/", entity.getBuyUnit()));
        holder.tvTotalCount.setText(MUtils.formatDouble(entity.getTotalCount(), "0"));
//        holder.tvStockQuantity.setText(String.format("%.2f", entity.getq()));
//        holder.tvPakcageNum.setText(String.format("%.2f", entity.getPackageNum()));
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
        @Bind(R.id.tv_checksum)
        TextView tvTotalCount;

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

            final InvSendOrderItem original = entityList.get(position);
            if (original == null) {
                return;
            }

            if (adapterListener != null) {
                adapterListener.onShowDetail(original);
            }
        }

        /**
         * 修改商品数量
         * */
        @OnClick(R.id.ll_checksum)
        public void inputChecksum() {
            final int position = getAdapterPosition();

            final InvSendOrderItem original = entityList.get(position);
            if (original == null) {
                return;
            }

            if (changeQuantityDialog == null) {
                changeQuantityDialog = new ChangeQuantityDialog(mContext);
                changeQuantityDialog.setCancelable(true);
                changeQuantityDialog.setCanceledOnTouchOutside(true);
            }
            changeQuantityDialog.init("数量", 2, original.getTotalCount(), new ChangeQuantityDialog.OnResponseCallback() {
                @Override
                public void onQuantityChanged(Double quantity) {
                    try{
                        original.setTotalCount(quantity);
                        original.setAmount(original.getTotalCount() * original.getPrice());
                        // TODO: 4/29/16
//                    ShopcartService.get().saveOrUpdate(original);

                        notifyDataSetChanged();

                        if (adapterListener != null) {
                            adapterListener.onDataSetChanged();
                        }
                    }
                    catch (Exception e){
                        ZLogger.e(e.toString());
                    }

                }
            });
            changeQuantityDialog.show();
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
