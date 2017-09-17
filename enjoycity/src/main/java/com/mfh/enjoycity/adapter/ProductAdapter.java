package com.mfh.enjoycity.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.ProductBean;
import com.mfh.enjoycity.bean.PromoteLabelBean;
import com.mfh.enjoycity.bean.PromotePriceBean;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 热卖商品/我常买/所有商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 */
public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public enum ITEM_TYPE {
        ITEM_TYPE_PRODUCT
    }

    private final LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<ProductBean> productBeans;
    private Long shopId;

    int itemWidth;

    public interface AdapterListener
    {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
        void addToShopcart(ProductBean bean);
        void showProductDetail(ProductBean bean);
        void addToShopcart(float x, float y, ProductBean bean);
    }
    private AdapterListener adapterListener;

    public void setOnAdapterLitener(AdapterListener adapterListener)
    {
        this.adapterListener = adapterListener;
    }

    public ProductAdapter(Context context, List<ProductBean> productBeans) {
        this.productBeans = productBeans;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_PRODUCT.ordinal()){
            return new ProductViewHolder(mLayoutInflater.inflate(R.layout.view_productcard, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder.getItemViewType() == ITEM_TYPE.ITEM_TYPE_PRODUCT.ordinal()) {
            final ProductBean bean = productBeans.get(position);

            //显示商品图片
            Glide.with(mContext).load(bean.getThumbnail())
                    .error(R.mipmap.img_default).into(((ProductViewHolder) holder).ivProduct);

            ((ProductViewHolder) holder).ivProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterListener != null) {
                        adapterListener.showProductDetail(bean);
                    }
                }
            });

            //显示促销标签
            ((ProductViewHolder) holder).ivPromoteLabel.setVisibility(View.GONE);
            Long label = bean.getLabel();
            if (label != null){
                if (label == 1){
                    ((ProductViewHolder) holder).ivPromoteLabel.setImageResource(R.drawable.ic_tag_hot);
                    ((ProductViewHolder) holder).ivPromoteLabel.setVisibility(View.VISIBLE);
                }
                else if (label == 2){
                    ((ProductViewHolder) holder).ivPromoteLabel.setImageResource(R.drawable.ic_tag_import);
                    ((ProductViewHolder) holder).ivPromoteLabel.setVisibility(View.VISIBLE);
                }
            }

            //显示商品价格(&促销)
            double discount = bean.getDiscount();
            if (discount > 0 && discount < 10){
                ((ProductViewHolder) holder).tvProductPrice.setText(String.format("￥ %.2f", bean.getCostPrice() * discount / 10));
            }
            else{
                ((ProductViewHolder) holder).tvProductPrice.setText(String.format("￥ %.2f", bean.getCostPrice()));
            }

//            if (bean.getName().contains("火龙果")){
//                ((ProductViewHolder) holder).tvProductName.setText(bean.getName() + "换行换行换行换行换行换行换行换行换行换行换行换行换行换行换行换行");
//            }else
            {
                ((ProductViewHolder) holder).tvProductName.setText(bean.getName());
            }
            ((ProductViewHolder) holder).ibShopcart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (adapterListener != null){
//                        adapterListener.addToShopcart(bean);
                        int[] location2 = new int[2];
                        v.getLocationInWindow(location2);//跳到屏幕外面
                        adapterListener.addToShopcart(location2[0], location2[1], bean);
                    }
                    notifyItemChanged(position);
                }
            });

            //显示购物车数量
            String id = String.valueOf(shopId) + String.valueOf(bean.getId());
            ShoppingCartEntity entity = ShoppingCartService.get().getEntityById(id);
            if (entity != null && entity.getProductCount() > 0){
                ((ProductViewHolder) holder).tvBadgeNumber.setText(String.valueOf(entity.getProductCount()));
                ((ProductViewHolder) holder).tvBadgeNumber.setVisibility(View.VISIBLE);
            }else{
                ((ProductViewHolder) holder).tvBadgeNumber.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (productBeans == null ? 0 : productBeans.size());
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM_TYPE.ITEM_TYPE_PRODUCT.ordinal();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }


    public void setProductBeans(List<ProductBean> productBeans) {
        this.productBeans = productBeans;
        this.notifyDataSetChanged();
    }

    public void setProductBeans(Long shopId, List<ProductBean> productBeans) {
        this.shopId = shopId;
        this.productBeans = productBeans;
        this.notifyDataSetChanged();

        loadPromote();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_product)
        ImageView ivProduct;
        @BindView(R.id.iv_promote_label)
        ImageView ivPromoteLabel;
        @BindView(R.id.tv_discount) TextView tvDiscount;
        @BindView(R.id.tv_product_name) TextView tvProductName;
        @BindView(R.id.tv_product_price) TextView tvProductPrice;
        @BindView(R.id.ib_shopcart)
        ImageButton ibShopcart;
        @BindView(R.id.tv_badgeNumber) TextView tvBadgeNumber;

        public ProductViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (adapterListener != null) {
//                        adapterListener.onItemClick(itemView, getPosition());
//                    }
//                }
//            });

        }
    }

    private void loadPromote(){
        ZLogger.d("loadPromote");
        StringBuilder productIds = new StringBuilder();
        if (productBeans != null && productBeans.size() > 0) {
            int len = productBeans.size();
            for (int i = 0; i < len; i++){
                productIds.append(productBeans.get(i).getId());
                if (i < len - 1){
                    productIds.append(",");
                }
            }
        }

        loadPromoteLabel(shopId, productIds.toString());
        loadPromotePrice(shopId, productIds.toString());
    }
    /**
     * 加载促销标签
     * */
    private void loadPromoteLabel(Long shopId, String productIds){
        if (shopId == null || StringUtils.isEmpty(productIds)){
            return;
        }

        NetCallBack.QueryRsCallBack queryResponseCallback = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<PromoteLabelBean>(new PageInfo(1, 100)) {
                    //                处理查询结果集，子类必须继承
                    @Override
                    public void processQueryResult(RspQueryResult<PromoteLabelBean> rs) {//此处在主线程中执行。
                        refreshPromoteLabel(rs);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        ZLogger.d("processFailure: " + errMsg);
                    }
                }
                , PromoteLabelBean.class
                , MfhApplication.getAppContext());

        EnjoycityApiProxy.findPromoteLabels(shopId, productIds, queryResponseCallback);
    }

    /**
     * 加载促销价格
     * */
    private void loadPromotePrice(Long shopId, String productIds){
        if (shopId == null || StringUtils.isEmpty(productIds)){
            return;
        }

        NetCallBack.QueryRsCallBack queryResponseCallback = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<PromotePriceBean>(new PageInfo(1, 100)) {
                    //                处理查询结果集，子类必须继承
                    @Override
                    public void processQueryResult(RspQueryResult<PromotePriceBean> rs) {//此处在主线程中执行。
                        refreshPromotePrice(rs);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        ZLogger.d("processFailure: " + errMsg);
                    }
                }
                , PromotePriceBean.class
                , MfhApplication.getAppContext());

        EnjoycityApiProxy.findPromotePrice(shopId, productIds.toString(), queryResponseCallback);
    }

    private void refreshPromoteLabel(RspQueryResult<PromoteLabelBean> rs){
        try {
            int retSize = rs.getReturnNum();
            ZLogger.d(String.format("%d result, content:%s", retSize, rs.toString()));

            List<PromoteLabelBean> result = new ArrayList<>();
            if(retSize > 0){
                for (int i = 0; i < retSize; i++) {
                    result.add(rs.getRowEntity(i));
                }
            }

            //TODO,修改数据
            if (productBeans != null && productBeans.size() > 0){
                for (PromoteLabelBean bean : result){
                    for (ProductBean product : productBeans){
                        if (bean.getProId().compareTo(product.getId()) == 0){
                            product.setLabel(bean.getLabel());
                            break;
                        }
                    }
                }
                notifyDataSetChanged();
            }
        }
        catch(Throwable ex){
            ZLogger.e(ex.toString());
        }
    }

    private void refreshPromotePrice(RspQueryResult<PromotePriceBean> rs){
        try {
            int retSize = rs.getReturnNum();
            ZLogger.d(String.format("%d result, content:%s", retSize, rs.toString()));

            List<PromotePriceBean> result = new ArrayList<>();
            if(retSize > 0){
                for (int i = 0; i < retSize; i++) {
                    result.add(rs.getRowEntity(i));
                }
            }

            //TODO,修改数据
            if (productBeans != null && productBeans.size() > 0){
                for (PromotePriceBean bean : result){
                    for (ProductBean product : productBeans){
                        if (bean.getProductId().compareTo(product.getId()) == 0){
                            if (bean.getDiscount() != null){
                                product.setDiscount(Double.valueOf(bean.getDiscount()));
                            }else{
                                product.setDiscount(0);
                            }
                            break;
                        }
                    }
                }
                notifyDataSetChanged();
            }
        }
        catch(Throwable ex){
            ZLogger.e(ex.toString());
        }
    }

    public void setItemWidth(int itemWidth) {
        this.itemWidth = itemWidth;
    }
}
