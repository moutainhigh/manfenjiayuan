package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.bean.wrapper.PurchaseShopcartSplitOrder;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 商品申请－购物车商品明细
 * Created by Nat.ZZN on 15/8/5.
 */
public class PurchaseFruitOrderAdapter
        extends RegularAdapter<PurchaseShopcartSplitOrder, PurchaseFruitOrderAdapter.ProductViewHolder> {

    private PurchaseShopcartSplitOrder curOrder = null;

    public PurchaseFruitOrderAdapter(Context context, List<PurchaseShopcartSplitOrder> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_purchase_shopcart_order, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        PurchaseShopcartSplitOrder entity = entityList.get(position);

        if (PurchaseShopcartSplitOrder.isEqual(entity, curOrder)) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }

        holder.tvCompanyName.setText(entity.getProviderName());
        holder.tvProviderContact.setText(String.format("%s/%s",
                entity.getProviderContact(), entity.getProviderPhone()));
        holder.tvAmount.setText(String.format("商品总价：%.2f", entity.getOrderAmount()));
     }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.rootview)
        View rootView;
        @Bind(R.id.tv_company_name)
        TextView tvCompanyName;
        @Bind(R.id.tv_provider_contact)
        TextView tvProviderContact;
        @Bind(R.id.tv_amount)
        TextView tvAmount;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    getLayoutPosition()
                    int position = getAdapterPosition();
                    curOrder = getEntity(position);
                    if (curOrder == null){
                        return;
                    }
                    notifyDataSetChanged();
//
                    if (adapterListener != null){
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

    @Override
    public void setEntityList(List<PurchaseShopcartSplitOrder> entityList) {
        this.entityList = entityList;

        if (entityList != null && entityList.size() > 0){
            this.curOrder = entityList.get(0);
        }
        else{
            this.curOrder = null;
        }
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public void setEntityList(List<PurchaseShopcartSplitOrder> entityList, boolean isResetCurrent){
        this.entityList = entityList;

        if (isResetCurrent){
            if (entityList != null && entityList.size() > 0){
                this.curOrder = entityList.get(0);
            }
            else{
                this.curOrder = null;
            }
        }
        else{
            if (entityList != null && entityList.size() > 0 && !entityList.contains(curOrder)){
                this.curOrder = entityList.get(0);
            }
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public PurchaseShopcartSplitOrder getCurOrder() {
        return curOrder;
    }
}
