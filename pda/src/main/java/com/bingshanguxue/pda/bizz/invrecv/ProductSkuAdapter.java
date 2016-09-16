package com.bingshanguxue.pda.bizz.invrecv;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.pda.R;
import com.bingshanguxue.pda.database.service.InvRecvGoodsService;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.anon.ProductSku;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;

import java.util.List;

/**
 * 平台商品档案
 * Created by bingshanguxue on 15/8/5.
 */
public class ProductSkuAdapter extends SwipAdapter<ProductSku, ProductSkuAdapter.ProductViewHolder> {

    public ProductSkuAdapter(Context context, List<ProductSku> entityList) {
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

        return new ProductViewHolder(mLayoutInflater
                .inflate(R.layout.cardview_productsku, parent, false));
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        try {
            ProductSku entity = entityList.get(position);

            holder.tvBarcode.setText(entity.getBarcode());
            holder.tvName.setText(entity.getName());
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
//        @Bind(R.id.tv_companyName)
        TextView tvBarcode;
//        @Bind(R.id.tv_hintPrice)
        TextView tvName;

        public ProductViewHolder(final View itemView) {
            super(itemView);
//            ButterKnife.bind(this, itemView);
            tvBarcode = (TextView) itemView.findViewById(R.id.tv_barcode);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    if (adapterListener != null) {
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
//                    notifyDataSetChanged();//getAdapterPosition() return -1.
//
                    if (adapterListener != null) {
                        adapterListener.onItemLongClick(itemView, position);
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void setEntityList(List<ProductSku> entityList) {
//        super.setEntityList(entityList);
        this.entityList = entityList;
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    @Override
    public void removeEntity(int position) {
        ProductSku entity = getEntity(position);
        if (entity == null){
            return;
        }

        InvRecvGoodsService.get().deleteById(String.valueOf(entity.getId()));

        //刷新列表
        entityList.remove(position);
        notifyItemRemoved(position);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
