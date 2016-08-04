package com.manfenjiayuan.pda_supermarket.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.business.bean.InvCheckOrder;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.utils.TimeUtil;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 库存－－库存盘点
 * Created by bingshanguxue on 15/8/5.
 */
public class StockCheckOrderAdapter extends RecyclerView.Adapter<StockCheckOrderAdapter.ProductViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<InvCheckOrder> entityList;

    private InvCheckOrder curOrder = null;

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public StockCheckOrderAdapter(Context context, List<InvCheckOrder> entityList) {
        this.entityList = entityList;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_stockcheck_order, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        InvCheckOrder entity = entityList.get(position);

        if (curOrder != null && curOrder.getId().compareTo(entity.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }

        holder.tvOrderName.setText(entity.getOrderName());
        holder.tvCreateDate.setText(String.format("盘点时间：%s", TimeUtil.format(entity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        if (entity.getStatus().equals(InvCheckOrder.INVCHECK_ORDERSTATUS_PROCESSING)) {
            holder.tvOrderStatus.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        }
        holder.tvNet.setText(String.format("网点: %s", entity.getNetCaption()));
        holder.tvOrderStatus.setText(String.format("状态: %s", entity.getStatusCaption()));
    }

    @Override
    public int getItemCount() {
        return (entityList == null ? 0 : entityList.size());
    }

    @Override
    public void onViewRecycled(ProductViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rootview)
        View rootView;
        @Bind(R.id.tv_orderName)
        TextView tvOrderName;
        @Bind(R.id.tv_createDate)
        TextView tvCreateDate;
        @Bind(R.id.tv_net)
        TextView tvNet;
        @Bind(R.id.tv_orderstatus)
        TextView tvOrderStatus;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    curOrder = entityList.get(position);
                    notifyDataSetChanged();

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

    public void setEntityList(List<InvCheckOrder> entityList) {
        this.entityList = entityList;

        if (this.entityList != null && this.entityList.size() > 0) {
            curOrder = this.entityList.get(0);
        } else {
            curOrder = null;
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public List<InvCheckOrder> getEntityList() {
        return entityList;
    }

    public InvCheckOrder getCurOrder() {
        return curOrder;
    }


}
