package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.bean.InvTransOrder;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 库存－库存调拨
 * Created by bingshanguxue on 15/8/5.
 */
public class InvTransOrderAdapter
        extends RegularAdapter<InvTransOrder, InvTransOrderAdapter.ProductViewHolder> {

    private boolean netFlag;
    private InvTransOrder curOrder = null;

    public InvTransOrderAdapter(Context context, List<InvTransOrder> entityList) {
        super(context, entityList);
    }

    public InvTransOrderAdapter(Context context, List<InvTransOrder> entityList, boolean netFlag) {
        super(context, entityList);
        this.netFlag = netFlag;
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_inv_trans_order, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        InvTransOrder entity = entityList.get(position);

        if (curOrder != null && curOrder.getId().compareTo(entity.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }

        // 调入
        if (netFlag){
            holder.tvProviderName.setText(String.format("调出门店：%s", entity.getSendCompanyName()));
        }
        else{
            holder.tvProviderName.setText(String.format("调入门店：%s", entity.getReceiveNetName()));
        }
        holder.tvOrderNumber.setText(String.format("单据编号：%s", entity.getOrderName()));

        holder.tvCreateDate.setText(String.format("下单时间：%s",
                TimeUtil.format(entity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        holder.tvTransHumanName.setText(String.format("经手人：%s", entity.getAuditHumanName()));
        holder.tvStatus.setText(String.format("状态：%s", entity.getStatusCaption()));
//        if (entity.getPayStatus() == ReceivableOrder.PAY_STATUS_PAID) {
////            holder.ivStatus.setImageResource(R.mipmap.ic_marker_paid);
//            holder.ivStatus.setVisibility(View.GONE);
//        } else {
////            holder.ivStatus.setImageResource(R.mipmap.ic_marker_not_paid);
//            holder.ivStatus.setVisibility(View.VISIBLE);
//        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rootview)
        View rootView;
        @Bind(R.id.tv_net_name)
        TextView tvProviderName;
        @Bind(R.id.tv_order_name)
        TextView tvOrderNumber;
        @Bind(R.id.tv_createDate)
        TextView tvCreateDate;
        @Bind(R.id.tv_transHumanName)
        TextView tvTransHumanName;
        @Bind(R.id.tv_status)
        TextView tvStatus;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    getLayoutPosition()
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
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

    @Override
    public void setEntityList(List<InvTransOrder> entityList) {
        this.entityList = entityList;

        if (this.entityList != null && this.entityList.size() > 0){
            curOrder = this.entityList.get(0);
        }
        else{
            curOrder = null;
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public InvTransOrder getCurOrder() {
        return curOrder;
    }

    public void remove(InvTransOrder order) {
        if (order == null) {
            return;
        }

        if (entityList != null) {
            entityList.remove(order);
        }
        if (curOrder == order) {
            curOrder = null;
        }
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }
    public void remove(Long id) {
        remove(query(id));
    }

    private InvTransOrder query(Long id){
        if (id == null) {
            return null;
        }

        if (entityList != null && entityList.size() > 0){
            for (InvTransOrder entity : entityList){
                if (entity.getId().compareTo(id) == 0){
                    return entity;
                }
            }
        }

        return null;
    }

}
