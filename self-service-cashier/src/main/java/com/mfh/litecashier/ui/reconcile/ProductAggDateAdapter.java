package com.mfh.litecashier.ui.reconcile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.api.ProductAggDate;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 销售统计
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class ProductAggDateAdapter
        extends RegularAdapter<ProductAggDate, ProductAggDateAdapter.ProductViewHolder> {

    public interface OnAdapterListener {

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    public ProductAggDateAdapter(Context context, List<ProductAggDate> entityList) {
        super(context, entityList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_goodsflow, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        ProductAggDate entity = entityList.get(position);

        holder.tvName.setText(entity.getTenantSkuIdWrapper());
        holder.tvBcount.setText(String.format(Locale.getDefault(), "%.2f", entity.getProductNum()));
        holder.tvAmount.setText(String.format(Locale.getDefault(), "%.2f", entity.getTurnover()));

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
    public void setEntityList(List<ProductAggDate> entityList) {
        super.setEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
