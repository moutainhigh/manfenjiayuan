package com.mfh.litecashier.ui.fragment.goods;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bingshanguxue.cashier.database.entity.PosProductEntity;
import com.mfh.framework.api.constant.PriceType;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.litecashier.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 前台类目商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class BackendCategoryGoodsAdapter
        extends RegularAdapter<PosProductEntity, BackendCategoryGoodsAdapter.MenuOptioinViewHolder> {

    public BackendCategoryGoodsAdapter(Context context, List<PosProductEntity> entityList) {
        super(context, entityList);
    }

    public interface AdapterListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onDataSetChanged();
    }

    private AdapterListener adapterListener;

    public void setOnAdapterLitener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    @Override
    public MenuOptioinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MenuOptioinViewHolder(mLayoutInflater
                .inflate(R.layout.itemview_pos_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final MenuOptioinViewHolder holder, final int position) {
        final PosProductEntity entity = entityList.get(position);
//            holder.ivHeader.setLayoutParams(new ViewGroup.LayoutParams(DensityUtil.dip2px(mContext, 156), DensityUtil.dip2px(mContext, 156)));
        holder.tvName.setText(entity.getName());
        holder.tvBarcode.setText(entity.getBarcode());
        holder.tvPriceType.setText(PriceType.name(entity.getPriceType()));
        holder.tvCostPrice.setText(String.format("¥ %.2f", entity.getCostPrice()));
    }

    @Override
    public void setEntityList(List<PosProductEntity> entityList) {
        super.setEntityList(entityList);

        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_barcode)
        TextView tvBarcode;
        @Bind(R.id.tv_pricetype)
        TextView tvPriceType;
        @Bind(R.id.tv_costprice)
        TextView tvCostPrice;

        public MenuOptioinViewHolder(final View itemView) {
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

}
