package com.mfh.litecashier.ui.presentation;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.entity.CashierShopcartEntity;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 订单流水
 * Created by bingshanguxue on 15/8/5.
 */
public class OrderPresentationGoodsAdapter
        extends RegularAdapter<CashierShopcartEntity, OrderPresentationGoodsAdapter.ProductViewHolder> {

    public OrderPresentationGoodsAdapter(Context context, List<CashierShopcartEntity> entityList) {
        super(context, entityList);
    }


    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(
                mLayoutInflater.inflate(R.layout.itemview_orderprentation_goods_header,
                parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        CashierShopcartEntity entity = entityList.get(position);

        holder.tvName.setText(entity.getSkuName());
        holder.tvPrice.setText(String.format("%.2f", entity.getFinalPrice()));
        holder.tvCustomerPrice.setText(MUtils.formatDouble(entity.getFinalCustomerPrice(), ""));
        //计件：整数；记重：3位小数
        if (entity.getPriceType() == PriceType.WEIGHT) {
            holder.tvBcount.setText(String.format("%.3f", entity.getBcount()));
        } else {
            holder.tvBcount.setText(String.format("%.2f", entity.getBcount()));
        }
        //显示会员价小计
        holder.tvAmount.setText(String.format("%.2f", MathCompact.mult(entity.getBcount(), entity.getFinalCustomerPrice())));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_price)
        TextView tvPrice;
        @BindView(R.id.tv_customer_price)
        TextView tvCustomerPrice;
        @BindView(R.id.tv_bcount)
        TextView tvBcount;
        @BindView(R.id.tv_amount)
        TextView tvAmount;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }
                    notifyDataSetChanged();
//                    notifyItemChanged(position);

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }


    @Override
    public void setEntityList(List<CashierShopcartEntity> entityList) {
        this.entityList = entityList;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    @Override
    public void appendEntityList(List<CashierShopcartEntity> entityList) {
        super.appendEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
