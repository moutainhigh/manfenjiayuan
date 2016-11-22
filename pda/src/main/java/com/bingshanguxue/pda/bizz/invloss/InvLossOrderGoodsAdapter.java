package com.bingshanguxue.pda.bizz.invloss;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.pda.R;
import com.bingshanguxue.pda.database.entity.InvLossGoodsEntity;
import com.bingshanguxue.vector_uikit.widget.TextLabelView;
import com.manfenjiayuan.business.utils.MUtils;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 报损商品
 * Created by bingshanguxue on 15/8/5.
 */
public class InvLossOrderGoodsAdapter extends SwipAdapter<InvLossGoodsEntity,
        InvLossOrderGoodsAdapter.ProductViewHolder> {

    public InvLossOrderGoodsAdapter(Context context, List<InvLossGoodsEntity> entityList) {
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
        return new ProductViewHolder(mLayoutInflater
                .inflate(R.layout.itemview_invloss_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        InvLossGoodsEntity entity = entityList.get(position);

        holder.tvName.setEndText(entity.getProductName());
        holder.tvBarcode.setEndText(entity.getBarcode());
        holder.tvQuantity.setEndText(MUtils.formatDouble(entity.getQuantityCheck(), ""));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
//        @Bind(R.id.tv_name)
TextLabelView tvName;
//        @Bind(R.id.tv_barcode)
TextLabelView tvBarcode;
//        @Bind(R.id.tv_quantity)
TextLabelView tvQuantity;

        public ProductViewHolder(final View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
            tvName = (TextLabelView) itemView.findViewById(R.id.tv_name);
            tvBarcode = (TextLabelView) itemView.findViewById(R.id.tv_barcode);
            tvQuantity = (TextLabelView) itemView.findViewById(R.id.tv_quantity);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, getAdapterPosition());
                    }
                }
            });
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    int position = getAdapterPosition();
//                    if (entityList == null || position < 0 || position >= entityList.size()) {
////                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
//                        return false;
//                    }
////                    notifyDataSetChanged();//getAdapterPosition() return -1.
////
//                    if (adapterListener != null) {
//                        adapterListener.onItemLongClick(itemView, position);
//                    }
//                    return false;
//                }
//            });
        }
    }

    @Override
    public void setEntityList(List<InvLossGoodsEntity> entityList) {
//        super.setEntityList(entityList);
        this.entityList = entityList;
        sortByUpdateDate();
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    /**
     * 按时间排序
     */
    private void sortByUpdateDate() {
        if (entityList == null || entityList.size() < 1) {
            return;
        }

        Collections.sort(entityList, new Comparator<InvLossGoodsEntity>() {
            @Override
            public int compare(InvLossGoodsEntity order1, InvLossGoodsEntity order2) {
                return 0 - order1.getUpdatedDate().compareTo(order2.getUpdatedDate());
            }
        });
    }

}
