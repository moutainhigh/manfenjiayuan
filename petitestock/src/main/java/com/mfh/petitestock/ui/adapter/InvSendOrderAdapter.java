package com.mfh.petitestock.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.business.bean.InvSendOrder;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.petitestock.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 采购订单列表
 * Created by bingshanguxue on 15/8/5.
 */
public class InvSendOrderAdapter extends RegularAdapter<InvSendOrder, InvSendOrderAdapter.ProductViewHolder> {

    private InvSendOrder curOrder = null;

    public InvSendOrderAdapter(Context context, List<InvSendOrder> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }



    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_commodity_distribution, parent, false));
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        InvSendOrder entity = entityList.get(position);

        if (curOrder != null && curOrder.getId().compareTo(entity.getId()) == 0){
            holder.rootView.setSelected(true);
        }else{
            holder.rootView.setSelected(false);
        }


        holder.tvSendCompanyName.setText(entity.getSendCompanyName());
        holder.tvBarCode.setText(String.format("订单号：%s", entity.getName()));
        holder.tvQuantity.setText(MUtils.formatDouble("商品数量", ":", entity.getAskTotalCount(), "0", "", "件"));
        holder.tvGoodsFee.setText(MUtils.formatDouble("商品金额", ":", entity.getGoodsFee(), "0", "", ""));
        holder.tvSendDate.setText(String.format("下单时间: %s", TimeUtil.format(entity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
        holder.tvTransHumanName.setText(String.format("经手人: %s", entity.getContact()));
    }


    @Override
    public int getItemCount() {
        return (entityList == null ? 0 : entityList.size());
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rootview) View rootView;
        @Bind(R.id.tv_send_companyname)
        TextView tvSendCompanyName;
        @Bind(R.id.tv_barcode)
        TextView tvBarCode;
        @Bind(R.id.tv_quantity)
        TextView tvQuantity;
        @Bind(R.id.tv_sendDate)
        TextView tvSendDate;
        @Bind(R.id.tv_goodsFee)
        TextView tvGoodsFee;
        @Bind(R.id.tv_transHumanName)
        TextView tvTransHumanName;

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
//                    notifyItemChanged(position);

                    if (adapterListener != null){
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

    public void setEntityList(List<InvSendOrder> entityList) {
        this.entityList = entityList;

        curOrder = null;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public List<InvSendOrder> getEntityList() {
        return entityList;
    }

    public InvSendOrder getCurOrder() {
        return curOrder;
    }

    public void remove(InvSendOrder order){
        if (order == null){
            return;
        }

        if (entityList != null){
            entityList.remove(order);
        }
        if (curOrder == order){
            curOrder = null;
        }
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public void remove(Long orderId){
        if (orderId == null){
            return;
        }

        if (entityList != null && entityList.size() > 0){
            for (InvSendOrder entity : entityList){
                if (entity.getId() != null && entity.getId().compareTo(orderId) == 0){
                    entityList.remove(entity);
                    break;
                }
            }
        }

        curOrder = null;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
