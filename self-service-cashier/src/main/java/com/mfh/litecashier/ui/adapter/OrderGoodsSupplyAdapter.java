package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.framework.api.GoodsSupplyInfo;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 订货商品批发商信息
 * Created by Nat.ZZN on 15/8/5.
 */
public class OrderGoodsSupplyAdapter
        extends RegularAdapter<GoodsSupplyInfo, OrderGoodsSupplyAdapter.ProductViewHolder> {

    public OrderGoodsSupplyAdapter(Context context, List<GoodsSupplyInfo> entityList) {
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
        return new ProductViewHolder(mLayoutInflater.inflate(R.layout.itemview_ordergoods_supply, parent, false));
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        GoodsSupplyInfo entity = entityList.get(position);

        holder.tvName.setText(entity.getSupplyName());
        holder.tvPackageNum.setText(String.format("%.2f", entity.getPackageNum()));
        holder.tvStartnum.setText(String.format("%.2f", entity.getStartNum()));
        holder.tvBuyprice.setText(String.format("%.2f", entity.getBuyPrice()));
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_packageNum)
        TextView tvPackageNum;
        @Bind(R.id.tv_startnum)
        TextView tvStartnum;
        @Bind(R.id.tv_buyprice)
        TextView tvBuyprice;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterListener != null){
                        adapterListener.onItemClick(itemView, getAdapterPosition());
                    }
                }
            });
        }

    }

    @Override
    public void setEntityList(List<GoodsSupplyInfo> entityList) {
        super.setEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }
}
