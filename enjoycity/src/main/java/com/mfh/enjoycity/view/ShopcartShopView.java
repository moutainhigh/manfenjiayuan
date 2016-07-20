package com.mfh.enjoycity.view;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.database.ShopEntity;
import com.mfh.enjoycity.database.ShopService;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.enjoycity.utils.ShopcartHelper;
import com.mfh.framework.uikit.dialog.CommonDialog;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 购物车～店铺
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class ShopcartShopView extends LinearLayout {
    @Bind(R.id.iv_shop_icon)
    ImageView ivShopIcon;
    @Bind(R.id.tv_shop_name)
    TextView tvShopName;
    @Bind(R.id.ll_products)
    LinearLayout llProducts;
    @Bind(R.id.ll_tip)
    LinearLayout llTip;
    @Bind(R.id.tv_tip_title)
    TextView tvTipTitle;
    @Bind(R.id.tv_tip_content)
    TextView tvTipContent;

    View rootView;

    private Long shopId;
    private List<ShoppingCartEntity> entityList;


    public interface onViewListener {
        void onDatasetChanged(List<ShoppingCartEntity> entityList);

        void showProductDetail(ShoppingCartEntity entity);

        void enterShop(Long shopId);
    }

    private onViewListener listener;

    public void setOnViewListener(onViewListener listener) {
        this.listener = listener;
    }

    public ShopcartShopView(Context context) {
        super(context);
        init();
    }

    public ShopcartShopView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        rootView = View.inflate(getContext(), R.layout.view_shoppingcart_shop, this);
        ButterKnife.bind(rootView);
    }

    public void set(Long shopId, final List<ShoppingCartEntity> entityList) {

        this.shopId = shopId;

        ShopEntity shopEntity = ShopService.get().getEntityById(shopId);
        if (shopEntity != null) {
            tvShopName.setText(shopEntity.getShopName());

            Glide.with(getContext())
                    .load(shopEntity.getShopLogoUrl())
                    .error(R.mipmap.img_default).into(ivShopIcon);
        }

        this.entityList = entityList;
        refreshProductData();

    }

    public List<ShoppingCartEntity> getEntityList() {
        return this.entityList;
    }

    private void refreshProductData() {
        if (entityList != null && entityList.size() > 0) {
            llProducts.removeAllViews();
            for (int i = 0; i < entityList.size(); i++) {
                final int position = i;
                final ShoppingCartEntity productEntity = entityList.get(i);
                ShopcartProductView productView = new ShopcartProductView(getContext());
                productView.set(productEntity);
                productView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showDeleteDialog(productEntity);

                        return true;
                    }
                });
                productView.setOnViewListener(new ShopcartProductView.onViewListener() {
                    @Override
                    public void onDatasetChanged(ShoppingCartEntity entity) {
                        entityList.set(position, entity);
                        if (listener != null) {
                            listener.onDatasetChanged(entityList);
                        }
                        refreshTip();
                    }

                    @Override
                    public void onPreDelete() {
                        showDeleteDialog(productEntity);
                    }

                    @Override
                    public void showProductDetail(ShoppingCartEntity entity) {
                        if (listener != null) {
                            listener.showProductDetail(entity);
                        }
                    }
                });
                llProducts.addView(productView);
            }
        }

        refreshTip();
    }

    private void showDeleteDialog(final ShoppingCartEntity entity) {
        CommonDialog dialog = new CommonDialog(getContext());
//		CommonDialog dialog = new CommonDialog(AppContext.getAppContext());
        dialog.setMessage("确定要删除商品吗？");
        dialog.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ShoppingCartService.get().deleteById(entity.getId());

                entityList.remove(entity);

                if (listener != null) {
                    listener.onDatasetChanged(entityList);
                }

                refreshTip();

                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 刷新提示信息
     */
    private void refreshTip() {
        if (entityList != null && entityList.size() > 0) {
            double totalAmount = 0;
            for (ShoppingCartEntity entity : entityList) {
                totalAmount += entity.getTotalAmount();
            }
            if (totalAmount < ShopcartHelper.MIN_DELIVER_PRICE) {
                tvTipTitle.setText(String.format("满￥%.2f起送", ShopcartHelper.MIN_DELIVER_PRICE));
                tvTipContent.setText(String.format("差￥%.2f起送 去凑单", ShopcartHelper.MIN_DELIVER_PRICE - totalAmount));
                tvTipContent.setTextColor(Color.parseColor("#ff0000"));
                llTip.setVisibility(VISIBLE);
                return;
            } else if (totalAmount >= ShopcartHelper.MIN_DELIVER_PRICE && totalAmount < ShopcartHelper.NO_FREIGHT_PRICE) {
                tvTipTitle.setText(String.format("满￥%.2f免配送费", ShopcartHelper.NO_FREIGHT_PRICE));
                tvTipContent.setText(String.format("差￥%.2f免邮 去凑单", ShopcartHelper.NO_FREIGHT_PRICE - totalAmount));
                tvTipContent.setTextColor(Color.parseColor("#000000"));
                llTip.setVisibility(VISIBLE);
                return;
            }
        }

        tvTipTitle.setText("");
        tvTipContent.setText("");
        llTip.setVisibility(GONE);
    }

    @OnClick(R.id.ll_tip)
    public void enterShop() {
        if (listener != null) {
            listener.enterShop(shopId);
        }
    }


}
