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
import com.mfh.litecashier.database.entity.PosOrderPayEntity;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 支付记录
 * Created by Administrator on 2015/4/20.
 */
public class PayHistoryAdapter
        extends RegularAdapter<PosOrderPayEntity, PayHistoryAdapter.ProductViewHolder> {


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

        String payInfo = String.format("商户订单号：%s\n支付方式：%s\n支付金额：%.2f\n会员编号：%s\n" +
                        "备注：%s",
                entity.getOutTradeNo(), WayType.name(entity.getPayType()),
                entity.getAmount(), entity.getMemberGUID(), entity.getRemark());
        holder.tvPayInfo.setText(payInfo);
        holder.tvPayStatus.setText(PosOrderPayEntity.getPayStatusDesc(entity.getPaystatus()));

        // Populate the data into the template view using the data object
        holder.tvUpdateDate.setText(TimeCursor.FORMAT_YYYYMMDDHHMMSS.format(entity.getUpdatedDate()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_payInfo)
        TextView tvPayInfo;
        @Bind(R.id.tv_paystatus)
        TextView tvPayStatus;
        @Bind(R.id.tv_updatedate)
        TextView tvUpdateDate;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null){
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });
        }
    }
}
