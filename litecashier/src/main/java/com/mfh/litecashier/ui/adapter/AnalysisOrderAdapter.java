package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.bean.wrapper.AnalysisItemWrapper;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 日结/交接班 统计数据
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class AnalysisOrderAdapter
        extends RegularAdapter<AnalysisItemWrapper, AnalysisOrderAdapter.ProductViewHolder> {

    public interface OnAdapterListener {

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    public AnalysisOrderAdapter(Context context, List<AnalysisItemWrapper> entityList) {
        super(context, entityList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_analysis_order, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        AnalysisItemWrapper entity = entityList.get(position);

        if (entity.isShowIndex()){
            holder.tvIndex.setText(String.valueOf(position + 1));
        }
        else{
            holder.tvIndex.setText("");
        }
        holder.tvBizType.setText(entity.getCaption());
        holder.tvQuantity.setText(String.format("%.2f", entity.getOrderNum()));
        holder.tvAmount.setText(String.format("%.2f", entity.getTurnover()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_index)
        TextView tvIndex;
        @Bind(R.id.tv_biztype)
        TextView tvBizType;
        @Bind(R.id.tv_quantity)
        TextView tvQuantity;
        @Bind(R.id.tv_amount)
        TextView tvAmount;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

    @Override
    public void setEntityList(List<AnalysisItemWrapper> entityList) {
        super.setEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
