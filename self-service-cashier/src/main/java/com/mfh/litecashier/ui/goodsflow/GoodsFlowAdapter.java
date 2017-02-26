package com.mfh.litecashier.ui.goodsflow;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.api.pmcstock.GoodsItem;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 商品流水
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class GoodsFlowAdapter
        extends RegularAdapter<GoodsItem, GoodsFlowAdapter.ProductViewHolder> {

    public interface OnAdapterListener {

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    public GoodsFlowAdapter(Context context, List<GoodsItem> entityList) {
        super(context, entityList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_goodsflow, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        GoodsItem entity = entityList.get(position);

        holder.tvName.setText(String.format("%s / %s", entity.getProductName(), entity.getBarcode()));
        holder.tvBcount.setText(String.format(Locale.getDefault(), "%.2f", entity.getBcount()));
        holder.tvAmount.setText(String.format(Locale.getDefault(), "%.2f", entity.getFactAmount()));

    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_bcount)
        TextView tvBcount;
        @BindView(R.id.tv_amount)
        TextView tvAmount;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void setEntityList(List<GoodsItem> entityList) {
        super.setEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
