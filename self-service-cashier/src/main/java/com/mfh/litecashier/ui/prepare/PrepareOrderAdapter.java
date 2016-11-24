package com.mfh.litecashier.ui.prepare;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.api.scOrder.ScOrderItem;
import com.mfh.framework.core.utils.MathCompact;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * <h1>拣货</h1>
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class PrepareOrderAdapter
        extends SwipAdapter<ScOrderItem, PrepareOrderAdapter.CashierViewHolder> {

    public interface OnAdapterListener {
        void onDataSetChanged(boolean needScroll);
    }

    protected OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class CashierViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_price)
        TextView tvPrice;
        @BindView(R.id.tv_bcount)
        TextView tvBecount;
        @BindView(R.id.tv_quantitycheck)
        TextView tvQuantityCheck;
        @BindView(R.id.tv_amountcheck)
        TextView tvAmountCheck;
        @BindView(R.id.tv_diff)
        TextView tvDiff;

        public CashierViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null){
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });
        }
    }

    public PrepareOrderAdapter(Context context, List<ScOrderItem> entityList) {
        super(context, entityList);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CashierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CashierViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_prepareorder,
                parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final CashierViewHolder holder, final int position) {
        // - get element from your dataset at this position
        try {
            ScOrderItem entity = getEntity(position);
            if (entity != null) {
                // - replace the contents of the view with that element
                holder.tvName.setText(entity.getProductName());
                holder.tvPrice.setText(String.format("%.2f", entity.getPrice()));
                //计件：整数；记重：3位小数
                if (PriceType.WEIGHT.equals(entity.getPriceType())) {
                    holder.tvBecount.setText(String.format("%.3f", entity.getBcount()));
                    holder.tvQuantityCheck.setText(String.format("%.3f", entity.getQuantityCheck()));
                } else {
                    holder.tvBecount.setText(String.format("%.2f", entity.getBcount()));
                    holder.tvQuantityCheck.setText(String.format("%.2f", entity.getQuantityCheck()));
                }
                holder.tvAmountCheck.setText(String.format("%.2f",
                        MathCompact.mult(entity.getPrice(), entity.getQuantityCheck())));
                holder.tvDiff.setText(String.format("%.2f",
                        MathCompact.sub(MathCompact.mult(entity.getPrice(), entity.getQuantityCheck()),
                                MathCompact.mult(entity.getPrice(), entity.getBcount()))));
            }
        } catch (Exception e) {
            ZLogger.ef(e.toString());
        }
    }

    @Override
    public void setEntityList(List<ScOrderItem> entityList) {
        super.setEntityList(entityList);

//        sortByUpdateDate();
        notifyDataSetChanged(true);
    }


    public double getBcount() {
        double count = 0;
        if (entityList != null && entityList.size() > 0) {
            for (ScOrderItem entity : entityList) {
                count += entity.getBcount();
            }
        }

        return count;
    }



    public void notifyDataSetChanged(int position, boolean needScroll) {
        notifyItemChanged(position);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(needScroll);
        }
    }

    public void notifyDataSetChanged(boolean needScroll) {
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged(needScroll);
        }
    }


}
