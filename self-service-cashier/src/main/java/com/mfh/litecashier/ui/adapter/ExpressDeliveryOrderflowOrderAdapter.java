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
import com.mfh.litecashier.bean.ReceiveBatchItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 线下门店订单流水
 * Created by Nat.ZZN on 15/8/5.
 */
public class ExpressDeliveryOrderflowOrderAdapter
        extends RegularAdapter<ReceiveBatchItem, ExpressDeliveryOrderflowOrderAdapter.ProductViewHolder> {

    private ReceiveBatchItem curPosOrder = null;

    public ExpressDeliveryOrderflowOrderAdapter(Context context, List<ReceiveBatchItem> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_content_orderflow_expressdelivery_order, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        ReceiveBatchItem entity = entityList.get(position);

        if (curPosOrder != null && curPosOrder.getId().compareTo(entity.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }

        holder.tvCompany.setText(entity.getCompanyName());
        holder.tvCourier.setText(String.format("快递员:%s/%s", entity.getCourierName(), entity.getCourierPhone()));
        holder.tvReceiveDate.setText(String.format("收件时间：%s", TimeUtil.format(entity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM)));
//        ReceiveBatchItemBean bean = entity.getBean();
//        ReceiveBatchItemCaption caption = entity.getCaption();
//        if (caption != null){
//            holder.tvCompany.setText(caption.getCompanyId());
//            holder.tvCourier.setText(String.format("快递员:%s/%s", caption.getHumanId(), caption.getHumanId()));
//        }
//        else{
//            holder.tvCompany.setText("");
//            holder.tvCourier.setText("快递员：-/-");
//        }
//
//        if (bean != null){
//            holder.tvReceiveDate.setText(String.format("收件时间：%s", TimeCursor.TimeFormat.format(bean.getCreatedDate())));
//        }
//        else{
//            holder.tvReceiveDate.setText("收件时间：--");
//        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rootview)
        View rootView;
        @BindView(R.id.tv_company)
        TextView tvCompany;
        @BindView(R.id.tv_courier)
        TextView tvCourier;
        @BindView(R.id.tv_receiveDate)
        TextView tvReceiveDate;

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
    public void setEntityList(List<ReceiveBatchItem> entityList) {
//        super.setEntityList(entityList);
        this.entityList = entityList;
        if (this.entityList != null && this.entityList.size() > 0){
            this.curPosOrder = this.entityList.get(0);
        }
        else{
            this.curPosOrder = null;
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public ReceiveBatchItem getCurPosOrder() {
        return curPosOrder;
    }

}
