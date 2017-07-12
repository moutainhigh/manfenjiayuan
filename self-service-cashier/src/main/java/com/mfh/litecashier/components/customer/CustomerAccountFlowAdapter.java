package com.mfh.litecashier.components.customer;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.api.constant.BizType;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.rxapi.bean.CommonAccountFlow;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 订单流水
 * Created by bingshanguxue on 15/8/5.
 */
public class CustomerAccountFlowAdapter
        extends RegularAdapter<CommonAccountFlow, CustomerAccountFlowAdapter.ProductViewHolder> {
    private CommonAccountFlow curPosOrder = null;

    public CustomerAccountFlowAdapter(Context context, List<CommonAccountFlow> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_customer_accountflow, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        CommonAccountFlow entity = entityList.get(position);

        holder.tvDate.setText(TimeUtil.format(entity.getHappenDate(), TimeUtil.FORMAT_YYYYMMDDHHMMSS));

        holder.tvAmount.setText(MUtils.formatDouble(entity.getConCash()));
        if (entity.getConCash() < 0) {
            holder.tvAmount.setTextColor(ContextCompat.getColor(mContext, R.color.mf_red));
        } else {
            holder.tvAmount.setTextColor(ContextCompat.getColor(mContext, R.color.mfh_colorPrimary));
        }
        holder.tvBizType.setText(BizType.name(entity.getBizType()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_date)
        TextView tvDate;
        @BindView(R.id.tv_biztype)
        TextView tvBizType;
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
    public void setEntityList(List<CommonAccountFlow> entityList) {
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

    public CommonAccountFlow getCurPosOrder() {
        return curPosOrder;
    }

}
