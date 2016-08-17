package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.CashQuotOrderInfo;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 日结/交接班 经营分析统计数据
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class CashQuotaAdapter
        extends RegularAdapter<CashQuotOrderInfo, CashQuotaAdapter.ProductViewHolder> {

    public interface OnAdapterListener {

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    public CashQuotaAdapter(Context context, List<CashQuotOrderInfo> entityList) {
        super(context, entityList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_cashquota,
                parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        CashQuotOrderInfo entity = entityList.get(position);

        holder.tvDate.setText(TimeUtil.format(entity.getCreatedDate(),
                TimeCursor.FORMAT_YYYYMMDDHHMM));
        holder.tvAmount.setText(String.format("%.2f", entity.getAmount()));
        if (entity.getBizType() == 0){
            holder.tvAmount.setText(Html.fromHtml(String.format("<font color=#FE5000>－ </font><font color=#000000>%.2f</font>",
                    entity.getAmount())));
        }
        else{
            holder.tvAmount.setText(Html.fromHtml(String.format("<font color=#4CAF50>＋ </font><font color=#000000>%.2f</font>",
                    entity.getAmount())));
        }

        holder.tvBizType.setText(entity.getBizTypeCaption());
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_date)
        TextView tvDate;
        @Bind(R.id.tv_amount)
        TextView tvAmount;
        @Bind(R.id.tv_biztype)
        TextView tvBizType;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

    @Override
    public void setEntityList(List<CashQuotOrderInfo> entityList) {
        super.setEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
