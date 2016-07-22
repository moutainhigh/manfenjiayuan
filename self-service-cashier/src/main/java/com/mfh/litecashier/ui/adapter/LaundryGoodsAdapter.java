package com.mfh.litecashier.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.litecashier.R;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;
import com.mfh.framework.api.scChainGoodsSku.ChainGoodsSku;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 称重
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class LaundryGoodsAdapter
        extends RegularAdapter<ChainGoodsSku, LaundryGoodsAdapter.MenuOptioinViewHolder> {

    public LaundryGoodsAdapter(Context context, List<ChainGoodsSku> entityList) {
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
        return new MenuOptioinViewHolder(mLayoutInflater.inflate(R.layout.itemview_measure, parent, false));
    }

    @Override
    public void onBindViewHolder(final MenuOptioinViewHolder holder, final int position) {
        final ChainGoodsSku entity = entityList.get(position);
//            holder.ivHeader.setLayoutParams(new ViewGroup.LayoutParams(DensityUtil.dip2px(mContext, 156), DensityUtil.dip2px(mContext, 156)));

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error)
                .into(holder.ivHeader);


        holder.tvName.setText(entity.getSkuName());
        holder.tvPrice.setText(String.format("¥ %.2f", entity.getCostPrice()));
    }

    @Override
    public void setEntityList(List<ChainGoodsSku> entityList) {
        super.setEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_header)
        ImageView ivHeader;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_price)
        TextView tvPrice;

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
