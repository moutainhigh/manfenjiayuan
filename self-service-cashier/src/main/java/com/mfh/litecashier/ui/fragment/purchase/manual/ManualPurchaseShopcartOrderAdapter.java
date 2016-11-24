package com.mfh.litecashier.ui.fragment.purchase.manual;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.core.utils.ObjectsCompact;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;
import com.mfh.litecashier.database.entity.PurchaseOrderEntity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 采购商品订单
 * Created by Nat.ZZN on 15/8/5.
 */
public class ManualPurchaseShopcartOrderAdapter
        extends RegularAdapter<PurchaseOrderEntity, ManualPurchaseShopcartOrderAdapter.ProductViewHolder> {

    private PurchaseOrderEntity curOrder = null;

    public ManualPurchaseShopcartOrderAdapter(Context context, List<PurchaseOrderEntity> entityList) {
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
        PurchaseOrderEntity entity = entityList.get(position);

        if (entity == curOrder) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }

        holder.tvCompanyName.setText(entity.getProviderName());
        holder.tvProviderContact.setText(String.format("%s/%s",
                "", ""));
        holder.tvAmount.setText(String.format("商品总价：%.2f", entity.getAmount()));
     }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rootview)
        View rootView;
        @BindView(R.id.tv_company_name)
        TextView tvCompanyName;
        @BindView(R.id.tv_provider_contact)
        TextView tvProviderContact;
        @BindView(R.id.tv_amount)
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
    public void setEntityList(List<PurchaseOrderEntity> entityList) {
        this.entityList = entityList;

        if (entityList != null && entityList.size() > 0){
            if (this.curOrder == null){
                this.curOrder = entityList.get(0);
            }
            else{
                boolean isReset = true;
                for (PurchaseOrderEntity entity : entityList){
                    if (ObjectsCompact.equals(entity.getId(), curOrder.getId())){
                        isReset = false;
                        break;
                    }
                }
                if (isReset){
                    curOrder = entityList.get(0);
                }
            }
        }
        else{
            this.curOrder = null;
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public PurchaseOrderEntity getCurOrder() {
        return curOrder;
    }
}
