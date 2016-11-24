package com.manfenjiayuan.mixicook_vip.ui.order;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.framework.api.shoppingCart.Cart;
import com.mfh.framework.api.shoppingCart.CartPack;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 订单明细
 * Created by Nat.ZZN(bingshanguxue) on 15/6/5.
 */
public class OrderGoodsAdapter
        extends RegularAdapter<CartPack, OrderGoodsAdapter.CategoryViewHolder> {

    public OrderGoodsAdapter(Context context, List<CartPack> entityList) {
        super(context, entityList);
    }

    public interface OnAdapterListener {
        void onItemClick(View view, int position);
        void onDataSetChanged();
    }

    private OnAdapterListener adapterListener;

    public void setOnAdapterListsner(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryViewHolder(mLayoutInflater.inflate(R.layout.itemview_order_goods, parent, false));
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, final int position) {
        final CartPack entity = entityList.get(position);
        Cart cart = entity.getCart();

        Glide.with(mContext).load(entity.getImgUrl()).error(R.mipmap.ic_image_error)
                .into(holder.tvHeader);
        holder.tvName.setText(entity.getProductName());
        holder.tvPrice.setText(MUtils.formatDouble(null, null,
                cart.getPrice(), "", "/", entity.getUnitName()));
        holder.tvQuantity.setText(String.format("x %.0f", cart.getBcount()));
    }

    @Override
    public void setEntityList(List<CartPack> entityList) {
        super.setEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }


    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_header)
        ImageView tvHeader;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_price)
        TextView tvPrice;
        @BindView(R.id.tv_quantity)
        TextView tvQuantity;

        public CategoryViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position < 0 || position >= entityList.size()) {
//                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
                        return;
                    }

                    notifyDataSetChanged();

                    if (adapterListener != null) {
                        adapterListener.onItemClick(itemView, position);
                    }
                }
            });



        }
    }
}
