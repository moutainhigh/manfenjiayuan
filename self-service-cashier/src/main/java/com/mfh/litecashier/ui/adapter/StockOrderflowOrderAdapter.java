package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.core.utils.StringUtils;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.framework.api.pmcstock.PosOrder;
import com.mfh.framework.api.pmcstock.PosOrderItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 线上销售/衣服洗护/快递 订单流水
 * Created by Nat.ZZN on 15/8/5.
 */
public class StockOrderflowOrderAdapter
        extends RegularAdapter<PosOrder, StockOrderflowOrderAdapter.ProductViewHolder> {
    private PosOrder curPosOrder = null;

    public StockOrderflowOrderAdapter(Context context, List<PosOrder> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_orderflow_store_order, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        PosOrder entity = entityList.get(position);

        if (curPosOrder != null && curPosOrder.getId().compareTo(entity.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }

        holder.tvBarcode.setText(String.format("订单编号：%s", entity.getBarcode()));
        holder.tvContact.setText(String.format("%s/%s", entity.getReceiveName(), entity.getReceivePhone()));
        String address = entity.getAddress();
        if (StringUtils.isEmpty(address)) {
            holder.tvAddress.setText("未知");
        } else {
            holder.tvAddress.setText(address);
        }
        holder.tvAddress.setText(entity.getAddress());
        holder.tvAmount.setText(String.format("商品总价:%.2f", entity.getAmount()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rootview)
        View rootView;
        @BindView(R.id.tv_contact)
        TextView tvContact;
        @BindView(R.id.tv_address)
        TextView tvAddress;
        @BindView(R.id.tv_amount)
        TextView tvAmount;
        @BindView(R.id.tv_barcode)
        TextView tvBarcode;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }
                    curPosOrder = entityList.get(position);
                    notifyDataSetChanged();
//                    notifyItemChanged(position);

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

    @Override
    public void setEntityList(List<PosOrder> entityList) {
        this.entityList = entityList;
        this.curPosOrder = null;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public PosOrder getCurPosOrder() {
        return curPosOrder;
    }

    /**
     * 获取当前订单明细
     */
    public List<PosOrderItem> getCurrentOrderItems() {
        if (curPosOrder == null) {
            return null;
        }
        return curPosOrder.getItems();
    }
}
