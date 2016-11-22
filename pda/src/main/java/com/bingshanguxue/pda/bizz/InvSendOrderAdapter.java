package com.bingshanguxue.pda.bizz;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.pda.R;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.comn.bean.TimeCursor;
import com.mfh.framework.api.invSendOrder.InvSendOrder;
import com.mfh.framework.core.utils.TimeUtil;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

/**
 * 采购订单列表
 * Created by bingshanguxue on 15/8/5.
 */
public class InvSendOrderAdapter extends RegularAdapter<InvSendOrder, InvSendOrderAdapter.ProductViewHolder> {

    public InvSendOrderAdapter(Context context, List<InvSendOrder> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }



    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_inv_sendorder, parent, false));
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        InvSendOrder entity = entityList.get(position);

        holder.tvSendCompanyName.setText(entity.getSendCompanyName());
        holder.tvBarCode.setEndText(entity.getName());
        holder.tvQuantity.setEndText(MUtils.formatDouble(null, null, entity.getAskTotalCount(), "0", "", "件"));
        holder.tvGoodsFee.setEndText(MUtils.formatDouble(null, null, entity.getGoodsFee(), "0", "", ""));
        holder.tvSendDate.setEndText(TimeUtil.format(entity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM));
        holder.tvTransHumanName.setEndText(entity.getContact());
    }


    @Override
    public int getItemCount() {
        return (entityList == null ? 0 : entityList.size());
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
//        @Bind(R.id.tv_send_companyname)
TextView tvSendCompanyName;
//        @Bind(R.id.tv_barcode)
TextLabelView tvBarCode;
//        @Bind(R.id.tv_quantity)
TextLabelView tvQuantity;
//        @Bind(R.id.tv_sendDate)
TextLabelView tvSendDate;
//        @Bind(R.id.tv_goodsFee)
TextLabelView tvGoodsFee;
//        @Bind(R.id.tv_transHumanName)
TextLabelView tvTransHumanName;

        public ProductViewHolder(final View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);

            tvSendCompanyName = (TextView) itemView.findViewById(R.id.tv_send_companyname);
            tvBarCode = (TextLabelView) itemView.findViewById(R.id.tv_barcode);
            tvQuantity = (TextLabelView) itemView.findViewById(R.id.tv_quantity);
            tvSendDate = (TextLabelView) itemView.findViewById(R.id.tv_sendDate);
            tvGoodsFee = (TextLabelView) itemView.findViewById(R.id.tv_goodsFee);
            tvTransHumanName = (TextLabelView) itemView.findViewById(R.id.tv_transHumanName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    notifyDataSetChanged();
//                    notifyItemChanged(position);

                    if (adapterListener != null){
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

    public void setEntityList(List<InvSendOrder> entityList) {
        this.entityList = entityList;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public List<InvSendOrder> getEntityList() {
        return entityList;
    }

}
