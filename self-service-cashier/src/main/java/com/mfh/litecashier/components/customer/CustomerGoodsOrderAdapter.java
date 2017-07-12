package com.mfh.litecashier.components.customer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.api.constant.PosType;
import com.mfh.framework.api.constant.WayType;
import com.mfh.framework.rxapi.bean.GoodsOrder;
import com.mfh.framework.rxapi.bean.GoodsOrderItem;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 订单流水
 * Created by bingshanguxue on 15/8/5.
 */
public class CustomerGoodsOrderAdapter
        extends RegularAdapter<GoodsOrder, CustomerGoodsOrderAdapter.ProductViewHolder> {
    private GoodsOrder curPosOrder = null;

    public CustomerGoodsOrderAdapter(Context context, List<GoodsOrder> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_transaction_goodsorder, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        GoodsOrder entity = entityList.get(position);

        if (curPosOrder != null && curPosOrder.getId().compareTo(entity.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }
        holder.tvCreateDate.setText(TimeUtil.format(entity.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS));
        holder.tvAmount.setText(MUtils.formatDouble(entity.getAmount()));
        holder.tvPayType.setText(WayType.name(entity.getPayType()));
        if (BizType.POS.equals(entity.getBtype()) && !PosType.POS_STANDARD.equals(entity.getSubType())) {
            holder.tvBizType.setText(PosType.name(entity.getSubType()));
        } else {
            holder.tvBizType.setText(BizType.name(entity.getBtype()));
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rootview)
        View rootView;
        @BindView(R.id.tv_createDate)
        TextView tvCreateDate;
        @BindView(R.id.tv_biztype)
        TextView tvBizType;
        @BindView(R.id.tv_pay_type)
        TextView tvPayType;
        @BindView(R.id.tv_amount)
        TextView tvAmount;

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
    public void setEntityList(List<GoodsOrder> entityList) {
        this.entityList = entityList;
        if (this.entityList != null && this.entityList.size() > 0) {
            this.curPosOrder = this.entityList.get(0);
        } else {
            this.curPosOrder = null;
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public GoodsOrder getCurPosOrder() {
        return curPosOrder;
    }


    /**
     * 获取当前订单明细
     */
    public List<GoodsOrderItem> getCurrentOrderItems() {
        if (curPosOrder == null) {
            return null;
        }
        return curPosOrder.getItems();
    }
}
