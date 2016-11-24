package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.comn.bean.TimeCursor;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.framework.api.constant.WayType;
import com.bingshanguxue.cashier.database.entity.PosOrderPayEntity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 支付记录
 * Created by Administrator on 2015/4/20.
 */
public class PayHistoryAdapter
        extends RegularAdapter<PosOrderPayEntity, PayHistoryAdapter.ProductViewHolder> {

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    public PayHistoryAdapter(Context context, List<PosOrderPayEntity> entityList) {
        super(context, entityList);
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_payhistory, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        PosOrderPayEntity entity = entityList.get(position);

        String payInfo;
        if (PosOrderPayEntity.AMOUNT_TYPE_IN.equals(entity.getAmountType())){
            payInfo = String.format("商户订单号：%s\n支付方式：%s\n收取金额：%.2f\n会员编号：%s\n",
                    entity.getOutTradeNo(), WayType.name(entity.getPayType()),
                    entity.getAmount(), entity.getCustomerHumanId());
        }
        else{
            payInfo = String.format("商户订单号：%s\n支付方式：%s\n找零金额：%.2f\n会员编号：%s\n",
                    entity.getOutTradeNo(), WayType.name(entity.getPayType()),
                    entity.getAmount(), entity.getCustomerHumanId());
        }

        holder.tvPayInfo.setText(payInfo);
        holder.tvPayStatus.setText(PosOrderPayEntity.getPayStatusDesc(entity.getPaystatus()));

        // Populate the data into the template view using the data object
        holder.tvUpdateDate.setText(TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(entity.getUpdatedDate()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_payInfo)
        TextView tvPayInfo;
        @BindView(R.id.tv_paystatus)
        TextView tvPayStatus;
        @BindView(R.id.tv_updatedate)
        TextView tvUpdateDate;

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
                    if (adapterListener != null){
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return false;
                    }

                    if (adapterListener != null) {
                        adapterListener.onItemLongClick(itemView, position);
                    }
                    return false;
                }
            });
        }
    }
}
