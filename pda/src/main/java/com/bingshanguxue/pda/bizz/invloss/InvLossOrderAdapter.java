package com.bingshanguxue.pda.bizz.invloss;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.pda.R;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.api.invLossOrder.InvLossOrder;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

/**
 * 库存－－库存盘点
 * Created by bingshanguxue on 15/8/5.
 */
public class InvLossOrderAdapter extends RegularAdapter<InvLossOrder, InvLossOrderAdapter.ProductViewHolder> {

    public InvLossOrderAdapter(Context context, List<InvLossOrder> entityList) {
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
        InvLossOrder entity = entityList.get(position);

//        if (curOrder != null && curOrder.getId().compareTo(entity.getId()) == 0) {
//            holder.rootView.setSelected(true);
//        } else {
//            holder.rootView.setSelected(false);
//        }

        holder.tvOrderName.setText(entity.getOrderName());
        holder.tvCreateDate.setEndText(TimeUtil.format(entity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM));
        if (entity.getStatus().equals(InvLossOrder.INVLOSS_ORDERSTATUS_PROCESSING)) {
            holder.tvOrderStatus.setEndTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }
//        holder.tvNet.setEndText(entity.getNetCaption());
        holder.tvOrderStatus.setEndText(entity.getStatusCaption());
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        //        @Bind(R.id.rootview)
//        View rootView;
//        @Bind(R.id.tv_orderName)
        TextView tvOrderName;
        //        @Bind(R.id.tv_createDate)
        TextLabelView tvCreateDate;
        //        @Bind(R.id.tv_net)
        TextLabelView tvNet;
        //        @Bind(R.id.tv_orderstatus)
        TextLabelView tvOrderStatus;

        public ProductViewHolder(final View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);

            tvOrderName = (TextView) itemView.findViewById(R.id.tv_orderName);
            tvCreateDate = (TextLabelView) itemView.findViewById(R.id.tv_createDate);
            tvNet = (TextLabelView) itemView.findViewById(R.id.tv_net);
            tvOrderStatus = (TextLabelView) itemView.findViewById(R.id.tv_orderstatus);

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

    public void setEntityList(List<InvLossOrder> entityList) {
        this.entityList = entityList;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public List<InvLossOrder> getEntityList() {
        return entityList;
    }

}
