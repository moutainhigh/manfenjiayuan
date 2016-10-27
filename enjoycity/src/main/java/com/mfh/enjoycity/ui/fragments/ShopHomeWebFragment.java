package com.mfh.enjoycity.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.enjoycity.ui.CategoryTabActivity;
import com.mfh.enjoycity.ui.HotSalesActivity;
import com.mfh.enjoycity.ui.OfenBuyActivity;
import com.mfh.enjoycity.ui.ProductDetailActivity;
import com.mfh.enjoycity.ui.activity.ShoppingCartActivity;
import com.mfh.enjoycity.ui.web.BrowserFragment;
import com.mfh.framework.hybrid.JBridgeConf;
import com.mfh.framework.hybrid.WebViewJavascriptBridge;


/**
 * 首页－－店铺
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class ShopHomeWebFragment extends BrowserFragment {

    public static final String EXTRA_KEY_SHOP_ID = "extra_key_shop_id";
    public static final String EXTRA_KEY_SHOP_POSITION = "extra_key_shop_position";

    private Long shopId;
    private int shopPosition;

    public static ShopHomeWebFragment newInstance(Long id, int position) {
        ShopHomeWebFragment fragment = new ShopHomeWebFragment();
        Bundle args = new Bundle();
        args.putLong(ShopHomeWebFragment.EXTRA_KEY_SHOP_ID, id);
        args.putInt(ShopHomeWebFragment.EXTRA_KEY_SHOP_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            shopId = intent.getLongExtra(EXTRA_KEY_SHOP_ID, 0);
            shopPosition = intent.getIntExtra(EXTRA_KEY_SHOP_POSITION, 0);
        }
        //for Fragment.instantiate
        Bundle args = getArguments();
        if (args != null) {
            shopId = args.getLong(EXTRA_KEY_SHOP_ID, 0);
            shopPosition = args.getInt(EXTRA_KEY_SHOP_POSITION, 0);
        }
        super.createViewInner(rootView, container, savedInstanceState);
    }

    /**
     * register native method
     */
    @Override
    protected void registerHandle() {
        super.registerHandle();
        //跳转至热卖商品
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_REDIRECT_TO_NATICE_HOTSALE,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        JSONObject jsonObject = JSON.parseObject(data);
                        Long shopId = jsonObject.getLong("shopId");
                        Bundle extras = new Bundle();
                        extras.putLong(HotSalesActivity.EXTRA_KEY_SHOP_ID, shopId);
                        HotSalesActivity.actionStart(getContext(), extras);
                    }
                });

        //跳转至我常买商品
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_REDIRECT_TO_NATICE_OFENBUY,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        JSONObject jsonObject = JSON.parseObject(data);
                        Long shopId = jsonObject.getLong("shopId");
                        Bundle extras = new Bundle();
                        extras.putLong(OfenBuyActivity.EXTRA_KEY_SHOP_ID, shopId);
                        OfenBuyActivity.actionStart(getContext(), extras);
                    }
                });

        //跳转至全部商品
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_REDIRECT_TO_NATICE_ALL,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
//                        {
//                            "shopId": 131228
//                              "categoryId": ""
//                        }
                        JSONObject jsonObject = JSON.parseObject(data);
                        Long shopId = jsonObject.getLong("shopId");
                        String categoryId = jsonObject.getString("categoryId");
                        Bundle extras = new Bundle();
                        extras.putLong(CategoryTabActivity.EXTRA_KEY_SHOP_ID, shopId);
                        extras.putString(CategoryTabActivity.EXTRA_KEY_CATEGORY_ID, categoryId);
//                        AllProductActivity.actionStart(getContext(), extras);
                        CategoryTabActivity.actionStart(getContext(), extras);
                    }
                });

        //跳转至购物车
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_REDIRECT_TO_NATIVE_SHOPCART,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
                        ShoppingCartActivity.actionStart(getContext(), 0);
                    }
                });


        //跳转至商品详情
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_REDIRECT_TO_NATIVE_PRODUCT_DETAIL,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
//                        {
//                                "productId": 456,
//                                "shopId": 789
//                        }
                        JSONObject jsonObject = JSON.parseObject(data);
                        Long productId = jsonObject.getLong("productId");
                        Long shopId = jsonObject.getLong("shopId");

                        Bundle extras = new Bundle();
                        extras.putInt(ProductDetailActivity.EXTRA_KEY_ANIM_TYPE, 0);
                        extras.putLong(ProductDetailActivity.EXTRA_KEY_PRODUCT_ID, productId);
                        extras.putLong(ProductDetailActivity.EXTRA_KEY_SHOP_ID, shopId);
                        ProductDetailActivity.actionStart(getContext(), extras);
                    }
                });

        //新增商品到购物车
        bridge.registerHandler(JBridgeConf.HANDLE_NAME_ADD2CART,
                new WebViewJavascriptBridge.WVJBHandler() {
                    @Override
                    public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback responseCallback) {
//                        {
//                                "productId": 456,
//                                "productName": "商品名",
//                                "productPrice": 88.88,
//                                "productImageUrl": "商品图片链接",
//                                "shopId": 789
//                        }
                        ShoppingCartService.get().addToShopcartFromHybird(data);
                    }
                });
    }


    /**
     * 刷新加载更多
     */
    public void refreshToLoadMore() {
        setRefreshing(false);
    }


}
