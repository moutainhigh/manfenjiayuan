package com.bingshanguxue.pda.bizz.invcheck;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.pda.R;
import com.manfenjiayuan.business.bean.InvCheckOrder;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

/**
 * 库存－－库存盘点
 * Created by bingshanguxue on 15/8/5.
 */
public class InvCheckOrderAdapter extends RegularAdapter<InvCheckOrder, InvCheckOrderAdapter.ProductViewHolder> {

    public InvCheckOrderAdapter(Context context, List<InvCheckOrder> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_invcheck_order, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        InvCheckOrder entity = entityList.get(position);

//        if (curOrder != null && curOrder.getId().compareTo(entity.getId()) == 0) {
//            holder.rootView.setSelected(true);
//        } else {
//            holder.rootView.setSelected(false);
//        }

        holder.tvOrderName.setText(entity.getOrderName());
        holder.tvCreateDate.setText(String.format("盘点时间：%s",
                TimeUtil.format(entity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        if (entity.getStatus().equals(InvCheckOrder.INVCHECK_ORDERSTATUS_PROCESSING)) {
            holder.tvOrderStatus.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }
        holder.tvNet.setText(String.format("网点: %s", entity.getNetCaption()));
        holder.tvOrderStatus.setText(String.format("状态: %s", entity.getStatusCaption()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
//        @Bind(R.id.rootview)
//        View rootView;
//        @Bind(R.id.tv_orderName)
        TextView tvOrderName;
//        @Bind(R.id.tv_createDate)
        TextView tvCreateDate;
//        @Bind(R.id.tv_net)
        TextView tvNet;
//        @Bind(R.id.tv_orderstatus)
        TextView tvOrderStatus;

        public ProductViewHolder(final View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);

            tvOrderName = (TextView) itemView.findViewById(R.id.tv_orderName);
            tvCreateDate = (TextView) itemView.findViewById(R.id.tv_createDate);
            tvNet = (TextView) itemView.findViewById(R.id.tv_net);
            tvOrderStatus = (TextView) itemView.findViewById(R.id.tv_orderstatus);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

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

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public List<InvCheckOrder> getEntityList() {
        return entityList;
    }

}
