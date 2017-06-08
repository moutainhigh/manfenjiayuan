package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.AnalysisItemWrapper;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 日结/交接班 经营分析统计数据
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class AggAnalysisOrderAdapter
        extends RegularAdapter<AnalysisItemWrapper, AggAnalysisOrderAdapter.ProductViewHolder> {

    public interface OnAdapterListener {

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    public AggAnalysisOrderAdapter(Context context, List<AnalysisItemWrapper> entityList) {
        super(context, entityList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_analysis_agg, parent, false));
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
//        holder.tvQuantity.setText(String.format(Locale.getDefault(), "%.2f", entity.getOrderNum()));
        holder.tvTurnOver.setText(String.format(Locale.getDefault(), "%.2f", entity.getTurnover()));
//        holder.tvGrossMargin.setText(MUtils.retrieveFormatedGrossMargin(entity.getTurnover(),
//                entity.getGrossProfit()));
        holder.tvSalesBalance.setText(String.format(Locale.getDefault(), "%.2f", entity.getSalesBalance()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_index)
        TextView tvIndex;
        @BindView(R.id.tv_biztype)
        TextView tvBizType;
        @BindView(R.id.tv_turnover)
        TextView tvTurnOver;
//        @BindView(R.id.tv_grossProfitMargin)
//        TextView tvGrossMargin;
        @BindView(R.id.tv_salesBalance)
        TextView tvSalesBalance;

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
