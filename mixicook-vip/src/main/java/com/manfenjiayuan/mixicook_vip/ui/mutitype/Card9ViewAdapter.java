package com.manfenjiayuan.mixicook_vip.ui.mutitype;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.api.anon.sc.storeRack.CardProduct;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class Card9ViewAdapter extends RegularAdapter<CardProduct, Card9ViewAdapter.MenuOptioinViewHolder> {

    public Card9ViewAdapter(Context context, List<CardProduct> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onAdd2Cart(View view, CardProduct product);

        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterLitener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public MenuOptioinViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(
                R.layout.itemview_card9_adapter, null, false);

        return new MenuOptioinViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MenuOptioinViewHolder holder, int position) {
        final CardProduct product = entityList.get(position);
//        ZLogger.d(String.format("position=%d, imageUrl=%s", position, bean.getImageUrl()));

        Glide.with(AppContext.getAppContext()).load(product.getImageUrl())
                .error(R.mipmap.ic_image_error).into(holder.ivHeader);
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(MUtils.formatDouble("¥", "", product.getCostPrice(), "", null, null));

        Integer status = product.getStatus();
        if (status == null || status.equals(0)) {
            holder.overlayView.setVisibility(View.VISIBLE);
            holder.ibCart.setVisibility(View.GONE);
        } else {
            holder.overlayView.setVisibility(View.GONE);
            holder.ibCart.setVisibility(View.VISIBLE);
        }
    }

    public class MenuOptioinViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_header)
        ImageView ivHeader;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_price)
        TextView tvPrice;
        @BindView(R.id.overlay)
        View overlayView;
        @BindView(R.id.ib_cart)
        ImageButton ibCart;


        public MenuOptioinViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
//            ivHeader = (ImageView) itemView.findViewById(R.id.iv_header);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (entityList == null || position < 0 || position >= entityList.size()) {
//                        MLog.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }
//
                    if (adapterListener != null) {
                        adapterListener.onItemClick(v, position);
                    }
                }
            });
        }

        @OnClick(R.id.ib_cart)
        public void add2Cart() {
            int position = getAdapterPosition();
            CardProduct product = getEntity(position);
            if (product == null) {
                return;
            }

            Integer status = product.getStatus();
            if (status == null || status.equals(0)) {
//                ZLogger.d("商品已售罄");
                return;
            }
            if (adapterListener != null) {
                adapterListener.onAdd2Cart(ivHeader, product);
            }
        }
    }

    @Override
    public void setEntityList(List<CardProduct> entityList) {
        super.setEntityList(entityList);
//        ZLogger.d(String.format("共有%s个元素个元素\"", entityList != null ? entityList.size() : 0));
    }
}
