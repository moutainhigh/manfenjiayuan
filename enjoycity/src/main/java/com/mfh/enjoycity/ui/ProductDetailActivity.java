package com.mfh.enjoycity.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.BannerBean;
import com.mfh.enjoycity.bean.Product;
import com.mfh.enjoycity.bean.ProductAtt;
import com.mfh.enjoycity.bean.ProductDetail;
import com.mfh.enjoycity.database.ShopEntity;
import com.mfh.enjoycity.database.ShopService;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.enjoycity.ui.activity.MainActivity;
import com.mfh.enjoycity.ui.advertise.AdvertisementPagerAdapter;
import com.mfh.enjoycity.ui.advertise.AdvertisementViewPager;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.enjoycity.view.ProductDetailShopcartView;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * 我常买
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 * */
public class ProductDetailActivity extends BaseActivity {

    public static final String EXTRA_KEY_PRODUCT_ID = "productId";
    public static final String EXTRA_KEY_SHOP_ID = "shopId";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.viewpager_product)
    AdvertisementViewPager bannerViewPager;
    @Bind(R.id.ivAnimImage)
    ImageView ivAnimProductImage;
    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @Bind(R.id.tv_product_name)
    TextView tvProductName;
    @Bind(R.id.tv_product_cost_price) TextView tvCostPrice;
    @Bind(R.id.tv_product_old_price) TextView tvOldPrice;
    @Bind(R.id.iv_shop_icon) ImageView ivShopThumb;
    @Bind(R.id.tv_shop_name) TextView tvShopName;
    @Bind(R.id.view_footer)
    ProductDetailShopcartView shopcartView;

    private Long productId;
    private Long shopId;
    private ShopEntity shopEntity;
    private ProductDetail productDetail;

    public static void actionStart(Context context, Bundle extras){
        Intent intent = new Intent(context, ProductDetailActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_product_detail;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle("");
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
        setSupportActionBar(toolbar);
//        if(getSupportActionBar() != null){
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
//        toolbar.setBackgroundColor(this.getResources().getColor(R.color.transparent));
        toolbar.setNavigationIcon(R.drawable.icon_details_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ProductDetailActivity.this.onBackPressed();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

//        ProductDetailFragment productDetailFragment = new ProductDetailFragment();
//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.fragment_container, productDetailFragment)
//                .show(productDetailFragment)
//                .commit();

        shopcartView.setViewListener(new ProductDetailShopcartView.ViewListener() {
            @Override
            public void onFinsih() {
                setResult(RESULT_OK);
                finish();
            }
        });

        load();

//        collapsingToolbar.setTitle("商品详情");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.button_show_category)
    public void showCategory(){
        if (productDetail != null){
            Product product = productDetail.getProduct();
            if (product != null){
                ZLogger.d("category:procateId = " + String.valueOf(product.getProcateId()));

                Bundle extras = new Bundle();
                extras.putLong(CategoryTabActivity.EXTRA_KEY_SHOP_ID, shopId);
                extras.putString(CategoryTabActivity.EXTRA_KEY_CATEGORY_ID, String.valueOf(product.getProcateId()));
//                        AllProductActivity.actionStart(getContext(), extras);
                CategoryTabActivity.actionStart(this, extras);
            }
        }
    }

    @OnClick(R.id.button_enter_shop)
    public void enterShop(){
        Bundle extras = new Bundle();
        extras.putLong(MainActivity.EXTRA_KEY_SELECT_SHOP_ID, shopId);
        MainActivity.actionStart(this, extras);
//        UIHelper.redirectToActivity(ProductDetailActivity.this, MainActivity.class);
    }

    /**
     * */
    private void handleIntent(){
        Intent intent = this.getIntent();
        if(intent != null){
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, -1);

            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if(animType == ANIM_TYPE_NEW_FLOW){
                this.setTheme(R.style.AppTheme_NewFlow);
            }

            productId = intent.getLongExtra(EXTRA_KEY_PRODUCT_ID, 0);
            shopId = intent.getLongExtra(EXTRA_KEY_SHOP_ID, 0);

            shopEntity = ShopService.get().getEntityById(shopId);
        }
    }

    /**
     * 加载商品详情信息
     * */
    private void load(){
        if (productId == null){
            DialogUtil.showHint("商品编号无效");
            finish();
        }


        Glide.with(getApplicationContext()).load("http://p0.meituan.net/200.0/deal/6a126d04c009e0193d42d01834aeb0ae43804.jpg")
                .error(R.mipmap.img_default).into(ivAnimProductImage);

        if (shopEntity != null){
            tvShopName.setText(shopEntity.getShopName());
            Glide.with(getApplicationContext())
                    .load(shopEntity.getShopLogoUrl())
                    .error(R.mipmap.img_default).into(ivShopThumb);
        }

        if(!NetworkUtils.isConnect(this)){
//            animProgress.setVisibility(View.GONE);
            DialogUtil.showHint(getString(R.string.toast_network_error));
            return;
        }

        NetCallBack.NetTaskCallBack queryResponseCallback = new NetCallBack.NetTaskCallBack<ProductDetail,
                NetProcessor.Processor<ProductDetail>>(
                new NetProcessor.Processor<ProductDetail>() {
                    @Override
                    public void processResult(IResponseData rspData) {
//                    java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                        RspBean<ProductDetail> retValue = (RspBean<ProductDetail>) rspData;
//                        if(retValue != null){
//                            ReceiveAddressService.get().init(retValue.getValue());
//                        }
                        EventBus.getDefault().post(
                                new ProductDetailEvent(retValue.getValue()));
                    }

//                            @Override
//                            protected void processFailure(Throwable t, String errMsg) {
//                                super.processFailure(t, errMsg);
//                                Log.d("Nat: updateUserPassword.processFailure", errMsg);
//                                DialogUtil.showHint("修改登录密码失败");
//                            }
                }
                , ProductDetail.class
                , MfhApplication.getAppContext())
        {
        };

        EnjoycityApiProxy.queryProductDetail(productId, queryResponseCallback);
    }

    public class ProductDetailEvent {

        private ProductDetail bean;
        public ProductDetailEvent(ProductDetail bean) {
            // TODO Auto-generated constructor stub
            this.bean = bean;
        }

        public ProductDetail getBean() {
            return bean;
        }
    }


    /**
     * 商品详情
     * */
    public void onEventMainThread(ProductDetailEvent event) {
        productDetail = event.getBean();

        if (productDetail == null){
            DialogUtil.showHint("加载失败");
            return;
        }

        try{
            List<ProductAtt> attList = productDetail.getAttList();
            if (attList != null && attList.size() > 0){
                List<BannerBean> imageList = new ArrayList<>();

                for (ProductAtt att : attList){

                    BannerBean pic = new BannerBean();
                    pic.setImageUrl(att.getPathUrl());
                    imageList.add(pic);
                }
                AdvertisementPagerAdapter adapter = new AdvertisementPagerAdapter(this,
                        imageList, null);
                bannerViewPager.setAdapter(adapter);
            }

            Product product = productDetail.getProduct();
            ZLogger.d("procateId = " + String.valueOf(product.getProcateId()));

            tvProductName.setText(product.getName());
//                tvCostPrice.setText(String.format("￥ %.2f", bean.getCostPrice()));
            tvCostPrice.setText(String.format("￥ %s", productDetail.getCostPrice()));
            String oldPrice = productDetail.getOldPrice();
            if (!StringUtils.isEmpty(oldPrice)){
                SpannableString span = new SpannableString(String.format("￥ %s", oldPrice));
                span.setSpan(new StrikethroughSpan(), 2, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvOldPrice.setText(span);
            }else{
                tvOldPrice.setText("");
            }

            //刷新购物车
            ShoppingCartService dbService = ShoppingCartService.get();
            String id = String.valueOf(shopId) + String.valueOf(product.getId());
            ShoppingCartEntity entity;
            if (dbService.entityExistById(id)){
                entity = dbService.getEntityById(id);
            }
            else {
                entity= new ShoppingCartEntity();
                entity.setId(String.valueOf(product.getId()));
                entity.setCreatedDate(new Date());
                entity.setProductId(product.getId());
                entity.setProductName(product.getName());
                if (productDetail.getCostPrice() != null){
                    //TODO
//                    double discount = productDetail.getDiscount();
//                    if (discount > 0 && discount < 1){
//                        entity.setProductPrice(productDetail.getCostPrice() * discount);
//                    }else{
//                        entity.setProductPrice(productDetail.getCostPrice());
//                    }

                    entity.setProductPrice(Double.valueOf(productDetail.getCostPrice()));
                }else{
                    entity.setProductPrice(0);
                }
                entity.setProductImageUrl(productDetail.getThumbnail());
                entity.setProductCount(0);
                entity.setShopId(shopId);
            }

            shopcartView.init(entity);
        }
        catch (Exception ex){
            ZLogger.e(ex.toString());
        }

    }




}
