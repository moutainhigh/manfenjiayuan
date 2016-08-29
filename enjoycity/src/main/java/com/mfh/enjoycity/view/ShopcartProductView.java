package com.mfh.enjoycity.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bingshanguxue.vector_uikit.NumberPickerView;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.database.ShoppingCartService;

import butterknife.Bind;
import butterknife.ButterKnife;


/***
 * 店铺订单商品
 */
public class ShopcartProductView extends LinearLayout {
    @Bind(R.id.iv_product_pic)
    ImageView ivProductPic;
    @Bind(R.id.tv_product_name)
    TextView tvProductName;
    @Bind(R.id.tv_product_price)
    TextView tvProductPrice;
    @Bind(R.id.view_numberpicker)
    NumberPickerView numberPickerView;


    private ShoppingCartEntity entity;

    public interface onViewListener {
        void onDatasetChanged(ShoppingCartEntity entity);

        void onPreDelete();

        void showProductDetail(ShoppingCartEntity entity);
    }

    private onViewListener listener;

    public void setOnViewListener(onViewListener listener) {
        this.listener = listener;
    }

    public ShopcartProductView(Context context) {
        super(context);
        init();
    }

    public ShopcartProductView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        View rootView = View.inflate(getContext(), R.layout.view_shoppingcart_product, this);
        ButterKnife.bind(rootView);

        ivProductPic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.showProductDetail(entity);
                }
            }
        });
        numberPickerView.setZeroIntercept(true);
        numberPickerView.setonOptionListener(new NumberPickerView.onOptionListener() {
            @Override
            public void onPreIncrease() {
                int newNumber = entity.getProductCount() + 1;
                numberPickerView.setValue(newNumber);

                ShoppingCartService shoppingCartService = ShoppingCartService.get();
                entity.setProductCount(newNumber);
                shoppingCartService.saveOrUpdate(entity);

                if (listener != null) {
                    listener.onDatasetChanged(entity);
                }
            }

            @Override
            public void onPreDecrease() {
                int newNumber = entity.getProductCount() - 1;
                if (newNumber > 0) {
                    numberPickerView.setValue(newNumber);

                    ShoppingCartService shoppingCartService = ShoppingCartService.get();
                    entity.setProductCount(newNumber);
                    shoppingCartService.saveOrUpdate(entity);

                    if (listener != null) {
                        listener.onDatasetChanged(entity);
                    }
                } else {
                    if (listener != null) {
                        listener.onPreDelete();
                    }
                }
            }

            @Override
            public void onValueChanged(int value) {

            }
        });

    }

    /**
     * 设置商品信息
     */
    public void set(ShoppingCartEntity entity) {

        this.entity = entity;

        tvProductName.setText(entity.getProductName());

        tvProductPrice.setText(String.format("￥ %.2f", entity.getProductPrice()));

        Glide.with(getContext())
                .load(entity.getProductImageUrl())
                .error(R.mipmap.img_default).into(ivProductPic);

        numberPickerView.setValue(entity.getProductCount());
    }


}
