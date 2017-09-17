package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.ShopProductBean;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.ui.activity.MainActivity;
import com.mfh.enjoycity.utils.ShopcartHelper;
import com.mfh.enjoycity.view.ShopcartShopView;
import com.mfh.framework.login.logic.MfhLoginService;

import java.util.List;

import butterknife.Bind;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 购物车
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class ShopCartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public enum ITEM_TYPE {
        ITEM_TYPE_ADDRESS,
        ITEM_TYPE_SHOP
    }

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<ShopProductBean> mList;

    public interface OnAdapterListener {
        void onChangeAddress();

        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onDataSetChanged();

        void showProductDetail(ShoppingCartEntity entity);
    }

    private OnAdapterListener adapterListener;

    public void setOnItemClickLitener(OnAdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }


    public ShopCartAdapter(Context context, List<ShopProductBean> mList) {
        this.mList = mList;
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_ADDRESS.ordinal()) {
            return new AddressViewHolder(mLayoutInflater.inflate(R.layout.view_address_item, parent, false));
        } else if (viewType == ITEM_TYPE.ITEM_TYPE_SHOP.ordinal()) {
            return new ShopViewHolder(mLayoutInflater.inflate(R.layout.view_item_shoppingcart_shop, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_ADDRESS.ordinal()) {
            if (MfhLoginService.get().haveLogined()) {
                ((AddressViewHolder) holder).ivMarker.setImageResource(R.mipmap.icon_address_gray);
                ((AddressViewHolder) holder).tvName.setText(ShopcartHelper.getInstance().getReceiver());
                ((AddressViewHolder) holder).tvTelephone.setText(ShopcartHelper.getInstance().getTelephone());

                ((AddressViewHolder) holder).tvAddress.setText(ShopcartHelper.getInstance().getSubName());
            } else {
                ((AddressViewHolder) holder).ivMarker.setImageResource(R.drawable.ic_shop);
                ((AddressViewHolder) holder).tvName.setText(ShopcartHelper.getInstance().getSubName());
                ((AddressViewHolder) holder).tvTelephone.setText("");

                ((AddressViewHolder) holder).tvAddress.setText(ShopcartHelper.getInstance().getAddrName());
            }
            ((AddressViewHolder) holder).ivArrow.setVisibility(View.VISIBLE);
        } else if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_SHOP.ordinal()) {

            final int index = position - 1;

            final ShopProductBean adapterData = mList.get(index);
            if (adapterData != null) {
                ((ShopViewHolder) holder).shopView.set(adapterData.getShopId(), adapterData.getEntityList());
                ((ShopViewHolder) holder).shopView.setOnViewListener(new ShopcartShopView.onViewListener() {
                    @Override
                    public void onDatasetChanged(List<ShoppingCartEntity> entityList) {
                        if (entityList != null && entityList.size() > 0) {
                            mList.get(index).setEntityList(((ShopViewHolder) holder).shopView.getEntityList());

                            notifyDataSetChanged();
                        } else {
                            mList.remove(index);

                            notifyItemRemoved(position);
                            notifyDataSetChanged();
                        }

                        if (adapterListener != null) {
                            adapterListener.onDataSetChanged();
                        }
                    }

                    @Override
                    public void showProductDetail(ShoppingCartEntity entity) {
                        if (adapterListener != null) {
                            adapterListener.showProductDetail(entity);
                        }
                    }

                    @Override
                    public void enterShop(Long shopId) {
                        Bundle extras = new Bundle();
                        extras.putLong(MainActivity.EXTRA_KEY_SELECT_SHOP_ID, shopId);
                        MainActivity.actionStart(mContext, extras);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return 1 + (mList == null ? 0 : mList.size());
    }

    //根据这个类型判断去创建不同item的ViewHolder
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_TYPE.ITEM_TYPE_ADDRESS.ordinal();
        }
        return ITEM_TYPE.ITEM_TYPE_SHOP.ordinal();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }


    public List<ShopProductBean> getData() {
        return mList;
    }

    public void setData(List<ShopProductBean> mList) {
        this.mList = mList;
        this.notifyDataSetChanged();
    }

    /**
     * 收货地址
     */
    public class AddressViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_marker)
        ImageView ivMarker;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_address)
        TextView tvAddress;
        @BindView(R.id.tv_telephone)
        TextView tvTelephone;

        @BindView(R.id.iv_arrow)
        ImageView ivArrow;

        public AddressViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterListener != null) {
                        adapterListener.onChangeAddress();
                    }
                }
            });
        }
    }

    /**
     * 店铺商品详情
     */
    public class ShopViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.shopView)
        ShopcartShopView shopView;

        public ShopViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null){
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });
        }
    }


}
