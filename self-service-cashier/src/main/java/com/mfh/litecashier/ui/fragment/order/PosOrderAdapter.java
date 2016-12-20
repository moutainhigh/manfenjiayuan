package com.mfh.litecashier.ui.fragment.order;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PosType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;
import com.bingshanguxue.cashier.model.PosOrder;
import com.bingshanguxue.cashier.model.PosOrderItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 订单流水
 * Created by bingshanguxue on 15/8/5.
 */
public class PosOrderAdapter
        extends RegularAdapter<PosOrder, PosOrderAdapter.ProductViewHolder> {
    private PosOrder curPosOrder = null;

    public PosOrderAdapter(Context context, List<PosOrder> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_posorder, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        PosOrder entity = entityList.get(position);

        if (curPosOrder != null && curPosOrder.getId().compareTo(entity.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }
        holder.tvCreateDate.setText(String.format("下单时间：%s",
                TimeUtil.format(entity.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
        holder.tvAmount.setText(String.format("商品总价：%.2f", entity.getAmount()));

        if (BizType.POS.equals(entity.getBtype()) && !PosType.POS_STANDARD.equals(entity.getSubType())){
            holder.tvBarcode.setText(String.format("订单编号：%s", entity.getOuterNo()));
            holder.tvPayType.setText(String.format("平台：%s", PosType.name(entity.getSubType())));
        }
        else{
            holder.tvBarcode.setText(String.format("订单编号：%d", entity.getId()));
            holder.tvPayType.setText(String.format("支付方式：%s", WayType.name(entity.getPayType())));
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rootview)
        View rootView;
        @BindView(R.id.tv_barcode)
        TextView tvBarcode;
        @BindView(R.id.tv_createDate)
        TextView tvCreateDate;
        @BindView(R.id.tv_amount)
        TextView tvAmount;
        @BindView(R.id.tv_pay_type)
        TextView tvPayType;

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
