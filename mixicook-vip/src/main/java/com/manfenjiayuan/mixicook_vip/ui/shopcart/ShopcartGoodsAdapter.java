package com.manfenjiayuan.mixicook_vip.ui.shopcart;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.NumberPickerView;
import com.bumptech.glide.Glide;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.mixicook_vip.R;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.shoppingCart.Cart;
import com.mfh.framework.api.shoppingCart.CartPack;
import com.mfh.framework.api.shoppingCart.ShoppingCartApiImpl;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.uikit.recyclerview.RegularAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 购物车
 * Created by Nat.ZZN(bingshanguxue) on 15/6/5.
 */
public class ShopcartGoodsAdapter
        extends RegularAdapter<CartPack, ShopcartGoodsAdapter.CategoryViewHolder> {

    public ShopcartGoodsAdapter(Context context, List<CartPack> entityList) {
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
        return new CategoryViewHolder(mLayoutInflater.inflate(R.layout.itemview_shopcart_goods,
                parent, false));
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, final int position) {
        final CartPack cartPack = entityList.get(position);
        Cart cart = cartPack.getCart();

        holder.mCheckBox.setChecked(cartPack.isChecked());
        Glide.with(mContext).load(cartPack.getImgUrl()).error(R.mipmap.ic_image_error)
                .into(holder.tvHeader);
        holder.tvName.setText(cartPack.getProductName());

        if (cart != null) {
            holder.tvPrice.setText(MUtils.formatDouble(null, null,
                    cart.getPrice(), "", "/", cartPack.getUnitName()));
            holder.mNumberPickerView.setValue(String.format("%.0f", cart.getBcount()));
        } else {
            holder.tvPrice.setText("");
            holder.mNumberPickerView.setValue(0);
        }
    }

    @Override
    public void setEntityList(List<CartPack> entityList) {
        super.setEntityList(entityList);
        if (adapterListener != null) {
            adapterListener.onDataSetChanged();
        }
    }

    /**
     * */
    public void adjustCart(final int position, final int quantity) {
        try {
            CartPack cartPack = getEntity(position);
            if (cartPack == null) {
                return;
            }
            final Cart cart = cartPack.getCart();

            NetCallBack.NetTaskCallBack responseC = new NetCallBack.NetTaskCallBack<String,
                    NetProcessor.Processor<String>>(
                    new NetProcessor.Processor<String>() {
                        @Override
                        public void processResult(IResponseData rspData) {
                            //{"code":"0","msg":"操作成功!","version":"1","data":""}
                            ZLogger.df("调整购物车商品数量: 操作成功");

                            cart.setBcount(Double.valueOf(String.valueOf(quantity)));
                            cart.setAmount(cart.getPrice() * cart.getBcount());

                            if (quantity <= 0) {
                                removeEntity(position);
                            } else {
                                notifyItemChanged(position);
                            }
                            if (adapterListener != null) {
                                adapterListener.onDataSetChanged();
                            }
                        }

                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);
                            ZLogger.df("调整购物车商品数量: " + errMsg);
                        }
                    }
                    , String.class
                    , MfhApplication.getAppContext()) {
            };

            ShoppingCartApiImpl.adjustCart(cart.getId(), quantity, responseC);
        } catch (Exception ex) {
            ZLogger.e("onValueChanged failed, " + ex.toString());
        }
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.checkbox)
        CheckBox mCheckBox;
        @BindView(R.id.iv_header)
        ImageView tvHeader;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_price)
        TextView tvPrice;
        @BindView(R.id.numberPickerView)
        NumberPickerView mNumberPickerView;

        public CategoryViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int position = getAdapterPosition();
//                    if (position < 0 || position >= entityList.size()) {
////                        ZLogger.d(String.format("do nothing because posiion is %d when dataset changed.", position));
//                        return;
//                    }
//
//                    notifyDataSetChanged();
//
//                    if (adapterListener != null) {
//                        adapterListener.onItemClick(itemView, position);
//                    }
//                }
//            });

            mNumberPickerView.setonOptionListener(new NumberPickerView.onOptionListener() {
                @Override
                public void onPreIncrease(int value) {
                    adjustCart(getAdapterPosition(), value);
                }

                @Override
                public void onPreDecrease(int value) {
                    adjustCart(getAdapterPosition(), value);
                }

                @Override
                public void onValueChanged(int value) {
                }
            });

            mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    ZLogger.d("check checkbox" + isChecked);

                    int position = getAdapterPosition();
                    CartPack cartPack = getEntity(position);
                    if (cartPack == null) {
                        return;
                    }
                    boolean isCheckChanged = false;
                    if (isChecked != cartPack.isChecked()){
                        isCheckChanged = true;
                    }
                    cartPack.setChecked(isChecked);
                    if (isCheckChanged && adapterListener != null) {
                        adapterListener.onDataSetChanged();
                    }
                }
            });

        }
    }

    /**
     * 全选or取消全选
     * */
    public void setChecked(boolean checked) {
        if (entityList != null && entityList.size() > 0) {
            for (CartPack pack : entityList) {
                pack.setChecked(checked);
            }

            notifyDataSetChanged();
            if (adapterListener != null) {
                adapterListener.onDataSetChanged();
            }
        }
    }

    /**
     * 获取选中的商品
     */
    public List<CartPack> retrieveSelectedPacks() {
        List<CartPack> packs = new ArrayList<>();
        if (entityList != null && entityList.size() > 0) {
            for (CartPack pack : entityList) {
                if (pack.isChecked()) {
                    packs.add(pack);
                }
            }
        }
        return packs;
    }
}
