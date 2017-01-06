package com.mfh.litecashier.ui.fragment.prepareorder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.framework.api.scOrder.ScOrder;
import com.mfh.framework.api.scOrder.ScOrderItem;
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
public class PrepareableOrderAdapter
        extends RegularAdapter<ScOrder, PrepareableOrderAdapter.ProductViewHolder> {
    private ScOrder curScOrder = null;

    public PrepareableOrderAdapter(Context context, List<ScOrder> entityList) {
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
        ScOrder entity = entityList.get(position);

        if (curScOrder != null && curScOrder.getId().compareTo(entity.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }
        holder.tvBarcode.setText(String.format("下单时间：%s",
                TimeUtil.format(entity.getCreatedDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS)));
        holder.tvCreateDate.setText(String.format("收货人：%s", entity.getReceiveName()));
        holder.tvAmount.setText(String.format("手机号：%s", entity.getReceivePhone()));
        holder.tvPayType.setVisibility(View.GONE);
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
                    curScOrder = entityList.get(position);
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
    public void setEntityList(List<ScOrder> entityList) {
        this.entityList = entityList;
        if (this.entityList != null && this.entityList.size() > 0){
            this.curScOrder = this.entityList.get(0);
        }
        else{
            this.curScOrder = null;
        }

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    @Override
    public void appendEntityList(List<ScOrder> entityList) {
        super.appendEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public ScOrder getCurScOrder() {
        return curScOrder;
    }


    /**
     * 获取当前订单明细
     */
    public List<ScOrderItem> getCurrentOrderItems() {
        if (curScOrder == null) {
            return null;
        }
        return curScOrder.getItems();
    }
}
