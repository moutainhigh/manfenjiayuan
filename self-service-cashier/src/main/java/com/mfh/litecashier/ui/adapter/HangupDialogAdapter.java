package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.HangupOrder;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 调单/挂单
 * Created by bingshanguxues on 2015/4/20.
 */
public class HangupDialogAdapter
        extends RegularAdapter<HangupOrder, HangupDialogAdapter.ProductViewHolder> {

    public HangupDialogAdapter(Context context, List<HangupOrder> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_hangup_order, parent, false));
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        HangupOrder entity = entityList.get(position);

        // Populate the data into the template view using the data object
        holder.tvCreateDate.setText(TimeUtil.format(entity.getUpdateDate(), TimeCursor.FORMAT_YYYYMMDDHHMMSS));
        holder.tvAmount.setText(String.format("%.2f 元", entity.getFinalAmount()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_createDate)
        TextView tvCreateDate;
        @Bind(R.id.tv_amount)
        TextView tvAmount;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    HangupOrder orderEntity = getEntity(position);
                    if (orderEntity == null){
                        return;
                    }

//                    notifyDataSetChanged();
                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }
}
