package com.mfh.litecashier.ui.fragment.online;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.api.invSendOrder.InvSendOrder;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 生鲜预定订单
 * Created by Nat.ZZN on 15/8/5.
 */
public class FreshScheduleOrderAdapter
        extends RegularAdapter<InvSendOrder, FreshScheduleOrderAdapter.ProductViewHolder> {

    private InvSendOrder curOrder = null;

    public FreshScheduleOrderAdapter(Context context, List<InvSendOrder> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_freshschedule_order,
                parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        InvSendOrder entity = entityList.get(position);

        if (curOrder != null && curOrder.getId().compareTo(entity.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }

        holder.tvTransHumanName.setText(String.format("%s/%s",
                entity.getContact(), entity.getReceiveMobile()));
        holder.tvCreateDate.setText(String.format("下单时间：%s",
                TimeUtil.format(entity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        holder.tvProviderName.setText(String.format("发货方: %s", entity.getSendCompanyName()));
        holder.tvOrderNumber.setText(String.format("单据编号：%s", entity.getName()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rootview)
        View rootView;
        @BindView(R.id.tv_provider_name)
        TextView tvProviderName;
        @BindView(R.id.tv_orderNumber)
        TextView tvOrderNumber;
        @BindView(R.id.tv_createDate)
        TextView tvCreateDate;
        @BindView(R.id.tv_transHumanName)
        TextView tvTransHumanName;

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
    public void setEntityList(List<InvSendOrder> entityList) {
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

    public InvSendOrder getCurOrder() {
        return curOrder;
    }

    public void remove(InvSendOrder order) {
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

    private InvSendOrder query(Long id){
        if (id == null) {
            return null;
        }

        if (entityList != null && entityList.size() > 0){
            for (InvSendOrder entity : entityList){
                if (entity.getId().compareTo(id) == 0){
                    return entity;
                }
            }
        }

        return null;
    }

}
