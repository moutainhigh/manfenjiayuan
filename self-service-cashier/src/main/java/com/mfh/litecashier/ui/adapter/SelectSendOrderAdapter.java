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
import com.mfh.framework.api.invSendOrder.InvSendOrder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 选择采购单
 * Created by Nat.ZZN on 15/8/5.
 */
public class SelectSendOrderAdapter
        extends RegularAdapter<InvSendOrder, SelectSendOrderAdapter.ProductViewHolder> {

    private InvSendOrder curPosOrder = null;

    public SelectSendOrderAdapter(Context context, List<InvSendOrder> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_select_sendorder, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        InvSendOrder entity = entityList.get(position);

        if (curPosOrder != null && curPosOrder.getId().compareTo(entity.getId()) == 0) {
            holder.rootView.setSelected(true);
        } else {
            holder.rootView.setSelected(false);
        }
        holder.tvBarcode.setText(entity.getName());
        holder.tvProviderName.setText(entity.getSendCompanyName());
        holder.tvStatus.setText(entity.getStatusCaption());
        holder.tvCreateDate.setText(TimeUtil.format(entity.getCreatedDate(), TimeCursor.FORMAT_YYYYMMDDHHMM));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.rootview)
        View rootView;
        @BindView(R.id.tv_barcode)
        TextView tvBarcode;
        @BindView(R.id.tv_provider_name)
        TextView tvProviderName;
        @BindView(R.id.tv_status)
        TextView tvStatus;
        @BindView(R.id.tv_createDate)
        TextView tvCreateDate;

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
//                    notifyDataSetChanged();
//                    notifyItemChanged(position);

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });
        }
    }

    @Override
    public void setEntityList(List<InvSendOrder> entityList) {
        this.entityList = entityList;
        this.curPosOrder = null;

        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public InvSendOrder getCurPosOrder() {
        return curPosOrder;
    }

}
