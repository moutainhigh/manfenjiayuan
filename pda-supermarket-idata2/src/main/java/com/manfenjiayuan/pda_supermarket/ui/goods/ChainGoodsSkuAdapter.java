package com.manfenjiayuan.pda_supermarket.ui.goods;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.database.logic.DistributionSignService;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.uikit.recyclerview.SwipAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 批发商商品
 * Created by bingshanguxue on 15/8/5.
 */
public class ChainGoodsSkuAdapter extends SwipAdapter<ChainGoodsSku, ChainGoodsSkuAdapter.ProductViewHolder> {

    public ChainGoodsSkuAdapter(Context context, List<ChainGoodsSku> entityList) {
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
                .inflate(R.layout.cardview_chain_goodssku, parent, false));
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        try {
            ChainGoodsSku entity = entityList.get(position);

            holder.tvCompanyName.setText(entity.getCompanyName());

            holder.tvBuyPrice.setText(MUtils.formatDouble("批发价:", "",
                    entity.getBuyPrice(), "无", "/", entity.getBuyUnit()));
            holder.tvHintPrice.setText(MUtils.formatDouble("建议零售价:", "",
                    entity.getHintPrice(), "无", "/", entity.getUnit()));
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_companyName)
        TextView tvCompanyName;
        @Bind(R.id.tv_hintPrice)
        TextView tvHintPrice;
        @Bind(R.id.tv_buyPrice)
        TextView tvBuyPrice;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

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
    public void setEntityList(List<ChainGoodsSku> entityList) {
//        super.setEntityList(entityList);
        this.entityList = entityList;
        notifyDataSetChanged();
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    @Override
    public void removeEntity(int position) {
        ChainGoodsSku entity = getEntity(position);
        if (entity == null){
            return;
        }

        DistributionSignService.get().deleteById(String.valueOf(entity.getId()));

        //刷新列表
        entityList.remove(position);
        notifyItemRemoved(position);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

}
