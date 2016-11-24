package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.bean.StockOutItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 线下门店订单流水－明细
 * Created by Nat.ZZN on 15/8/5.
 */
public class ExpressDeliveryOrderflowGoodsAdapter
        extends RegularAdapter<StockOutItem, ExpressDeliveryOrderflowGoodsAdapter.ProductViewHolder> {

    public ExpressDeliveryOrderflowGoodsAdapter(Context context, List<StockOutItem> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_orderflow_expressdelivery_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        StockOutItem entity = entityList.get(position);

        holder.tvReceiverName.setText(entity.getHumanName());
        holder.tvReceiverPhone.setText(entity.getHumanPhone());
        holder.tvBarcode.setText(entity.getBarcode());
        if (entity.getStatus() == 1){
            holder.tvStatus.setText("已取");
        }
        else{
            holder.tvStatus.setText("待取");
        }
        holder.tvDeliveryDate.setText(entity.getCreatedDate());

    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_receiverName)
        TextView tvReceiverName;
        @BindView(R.id.tv_receiverPhone)
        TextView tvReceiverPhone;
        @BindView(R.id.tv_barcode)
        TextView tvBarcode;
        @BindView(R.id.tv_status)
        TextView tvStatus;
        @BindView(R.id.tv_deliveryDate)
        TextView tvDeliveryDate;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    getLayoutPosition()
//                    int position = getAdapterPosition();
//                    if (position < 0 || position >= entityList.size()) {
////                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
//                        return;
//                    }
////                    notifyDataSetChanged();//getAdapterPosition() return -1.
////
////                    if (adapterListener != null){
////                        adapterListener.onItemClick(itemView, position);
////                    }
//                }
//            });
        }
    }

    @Override
    public void setEntityList(List<StockOutItem> entityList) {
        super.setEntityList(entityList);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
