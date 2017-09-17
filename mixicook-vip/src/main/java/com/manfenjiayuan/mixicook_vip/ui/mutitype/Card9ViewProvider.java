package com.manfenjiayuan.mixicook_vip.ui.mutitype;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.DividerGridItemDecoration;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.database.HomeGoodsTempEntity;
import com.manfenjiayuan.mixicook_vip.database.HomeGoodsTempService;
import com.manfenjiayuan.mixicook_vip.ui.ActivityRoute;
import com.manfenjiayuan.mixicook_vip.ui.home.ShopcartEvent;
import com.manfenjiayuan.mixicook_vip.utils.AddCartOptions;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.anon.sc.storeRack.CardProduct;
import com.mfh.framework.api.shoppingCart.ShoppingCartApi;
import com.mfh.framework.api.shoppingCart.ShoppingCartApiImpl;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import me.drakeet.multitype.ItemViewProvider;


/**
 * 商品前台类目卡片
 * Created by bingshanguxue on 09/10/2016.
 */

public class Card9ViewProvider extends ItemViewProvider<Card9,
        Card9ViewProvider.ViewHolder> {

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(
            @NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.itemview_card9, parent, false);
        return new ViewHolder(root);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull Card9 card) {
        holder.setData(card);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategoryName;
        private RecyclerView recyclerView;
        private Card9ViewAdapter mAdapter;
        private Card9 card;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = (TextView) itemView.findViewById(R.id.tv_categoryName);
//            btnMore = (Button) itemView.findViewById(R.id.button_more);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.goods_list);

            tvCategoryName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (card != null) {
                        ActivityRoute.redirect2CategoryGoods(v.getContext(),
                                card.getShopId(), card.getFrontCategoryId(), card.getCategoryName());
                    }
                }
            });

            mAdapter = new Card9ViewAdapter(itemView.getContext(), null);
            mAdapter.setOnAdapterLitener(new Card9ViewAdapter.OnAdapterListener() {
                @Override
                public void onAdd2Cart(View view, CardProduct product) {
                    add2Cart(view, product);
                }

                @Override
                public void onItemClick(View view, int position) {
//                    CardProduct product = mAdapter.getEntity(position);
//                    if (product != null){
////                        DialogUtil.showHint(String.format("选中商品[%s][%s]",
////                                product.getImageUrl(), product.getName()));
////                        ActivityRoute.redirect2Url2(AppContext.getAppContext(), cardItem.getImageUrl());
//                    }
                }

                @Override
                public void onItemLongClick(View view, int position) {

                }
            });

            GridLayoutManager mRLayoutManager = new GridLayoutManager(itemView.getContext(), 3);
            recyclerView.setLayoutManager(mRLayoutManager);
            //enable optimizations if all item views are of the same height and width for
            //signficantly smoother scrolling
            recyclerView.setHasFixedSize(true);
            recyclerView.addItemDecoration(new DividerGridItemDecoration(itemView.getContext(),
                    R.drawable.divider_gridview));
//            recyclerView.addItemDecoration(new DividerGridItemDecoration());
            recyclerView.setAdapter(mAdapter);
        }

        /**
         * 设置数据
         * */
        private void setData(Card9 card) {
            this.card = card;
            if (card != null) {
                String categoryName = card.getCategoryName();
                if (StringUtils.isEmpty(categoryName)){
                    tvCategoryName.setText("类目");
                }
                else{
                    this.tvCategoryName.setText(card.getCategoryName());
                }
                tvCategoryName.setEnabled(true);

                List<CardProduct> products = card.getProducts();
                if (products != null && products.size() > 0){
                    for (CardProduct product : products){
                        HomeGoodsTempEntity entity = HomeGoodsTempService.getInstance()
                                .getEntityBy(product.getSkuId());
                        if (entity != null){
                            product.setBuyUnit(entity.getBuyUnit());
                            product.setCostPrice(entity.getCostPrice());
                            product.setProductId(entity.getProductId());
                            product.setStatus(1);
                        }
                        else{
                            product.setStatus(0);//未查询到商品价格的商品都认为是售罄状态
                        }
                    }
                }
                this.mAdapter.setEntityList(products);

            } else {
                this.tvCategoryName.setText("");
                this.mAdapter.setEntityList(null);
                tvCategoryName.setEnabled(false);
            }
        }

        /**
         * 加入购物车
         * */
        private void add2Cart(View view, CardProduct product){
            if (product == null){
                return;
            }


            if (!NetworkUtils.isConnect(AppContext.getAppContext())) {
                ZLogger.d("网络未连接。");
                return;
            }

            EventBus.getDefault().post(new ShopcartEvent(ShopcartEvent.EVENT_ID_ADD2CART,
                    AddCartOptions.makeOptions(view)));

            NetCallBack.NetTaskCallBack responseC = new NetCallBack.NetTaskCallBack<String,
                    NetProcessor.Processor<String>>(
                    new NetProcessor.Processor<String>() {
                        @Override
                        public void processResult(IResponseData rspData) {
                            //{"code":"0","msg":"操作成功!","version":"1","data":""}
                            ZLogger.df("加入购物车成功");
//                            DialogUtil.showHint("加入购物车成功");
                            EventBus.getDefault().post(new ShopcartEvent(ShopcartEvent.EVENT_ID_DATASETCHANGED, new Bundle()));
                        }

                        @Override
                        protected void processFailure(Throwable t, String errMsg) {
                            super.processFailure(t, errMsg);
                            ZLogger.df("加入购物车失败: " + errMsg);
                        }
                    }
                    , String.class
                    , MfhApplication.getAppContext()) {
            };

            JSONObject cart = new JSONObject();
            cart.put("goodsId", product.getGoodsId());
            cart.put("bcount", 1);
            cart.put("productId", product.getProductId());
            cart.put("shopId", card.getShopId());
            cart.put("price", product.getCostPrice());
            cart.put("proSkuId", product.getSkuId());
            cart.put("bizType", ShoppingCartApi.BIZTYPE_BUY);
            cart.put("subType", ShoppingCartApi.SUBTYPE);

            JSONArray specItems = new JSONArray();
            ShoppingCartApiImpl.add2Cart(cart, specItems, responseC);
        }
    }
}
