package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.business.utils.MUtils;
import com.bingshanguxue.vector_uikit.NumberPickerView;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.FreshScheduleGoods;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 生鲜预定－－订单明细
 * Created by bingshanguxue on 15/8/5.
 */
public class FreshScheduleGoodsAdapter
        extends RegularAdapter<FreshScheduleGoods, FreshScheduleGoodsAdapter.ProductViewHolder> {

    private boolean isNumberPickViewVisible = false;
    public FreshScheduleGoodsAdapter(Context context, List<FreshScheduleGoods> entityList) {
        super(context, entityList);
    }

    public FreshScheduleGoodsAdapter(Context context, List<FreshScheduleGoods> entityList, boolean isNumberPickViewVisible) {
        super(context, entityList);
        this.isNumberPickViewVisible = isNumberPickViewVisible;
    }

    public interface OnAdapterListener {
        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public void setNumberPickViewVisible(boolean numberPickViewVisible) {
        isNumberPickViewVisible = numberPickViewVisible;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater
                .inflate(R.layout.itemview_purchase_freshschedule_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        FreshScheduleGoods entity = entityList.get(position);

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error)
                .into(holder.ivHeader);

        holder.tvName.setText(entity.getProductName());
        holder.tvBarcode.setText(entity.getBarcode());
        holder.tvUnit.setText(entity.getBuyUnit());
        holder.tvQuantity.setText(MUtils.formatDouble(entity.getAskTotalCount(), ""));
        if (isNumberPickViewVisible){
            holder.mNumberPickerView.setVisibility(View.VISIBLE);
        }
        else{
            holder.mNumberPickerView.setVisibility(View.INVISIBLE);
        }
        holder.mNumberPickerView.setValue(String.format("%.0f", entity.getQuantityCheck()));
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
                        FreshScheduleGoods entity = getEntity(position);
                        if (entity == null) {
                            return;
                        }

                        entity.setQuantityCheck(Double.valueOf(String.valueOf(value)));

                        notifyDataSetChanged();
                        if (adapterListener != null) {
                            adapterListener.onDataSetChanged();
                        }

                    } catch (Exception ex) {
                        ZLogger.e(ex.toString());
                    }
                }
            });
        }

    }

    @Override
    public void setEntityList(List<FreshScheduleGoods> entityList) {
        super.setEntityList(entityList);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    @Override
    public void removeEntity(int position) {
        super.removeEntity(position);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }
}
