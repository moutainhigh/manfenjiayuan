package com.mfh.enjoycity.ui;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.adapter.AllProductCategoryAdapter;
import com.mfh.enjoycity.adapter.ProductAdapter;
import com.mfh.enjoycity.bean.CategoryInfoBean;
import com.mfh.enjoycity.bean.CategoryOptionBean;
import com.mfh.enjoycity.bean.ProductBean;
import com.mfh.enjoycity.database.ShopEntity;
import com.mfh.enjoycity.database.ShopService;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.enjoycity.ui.activity.ShoppingCartActivity;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.enjoycity.view.CategorySlidingTabStrip;
import com.mfh.enjoycity.view.FloatShopcartView;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 全部商品
 * */
public class AllProductActivity extends BaseActivity {
    public static final String EXTRA_KEY_SHOP_ID = "EXTRA_KEY_SHOP_ID";
    public static final String EXTRA_KEY_CATEGORY_ID = "EXTRA_KEY_CATEGORY_ID";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.tv_category_current)
    TextView tvCurrentCategory;
    @Bind(R.id.sticky_category_tab)
    CategorySlidingTabStrip categorySlidingTabStrip;
    @Bind(R.id.my_recycler_view)
    RecyclerView mRecyclerView;

    private int mBaseTranslationY;
    private ProductAdapter productAdapter;

    @Bind(R.id.ll_category) LinearLayout llCategory;
    @Bind(R.id.recycler_view_category)
    RecyclerView mCategoryRecyclerView;


    @Bind(R.id.fab_shopcart)
    FloatShopcartView fabShopcartView;

    private Long shopId;
    private ShopEntity shopEntity;
    private CategoryInfoBean rootCategory;
    private String curCategoryId;
    private CategoryOptionBean currentCategoryOption;

    public static void actionStart(Context context, int animationType) {
        Intent intent = new Intent(context, AllProductActivity.class);
        intent.putExtra(EXTRA_KEY_ANIM_TYPE, animationType);
        context.startActivity(intent);
    }

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, AllProductActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_all_product;
    }

    @Override
    protected void initToolBar() {
        if (shopEntity != null){
            toolbar.setTitle(shopEntity.getShopName());
        }
        else{
            toolbar.setTitle("");
        }
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
        setSupportActionBar(toolbar);
//        if(getSupportActionBar() != null){
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
//        toolbar.setBackgroundColor(this.getResources().getColor(R.color.transparent));
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AllProductActivity.this.onBackPressed();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

//        ProductDetailFragment productDetailFragment = new ProductDetailFragment();
//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.fragment_container, productDetailFragment)
//                .show(productDetailFragment)
//                .commit();


        ViewCompat.setElevation(tvCurrentCategory, getResources().getDimension(R.dimen.toolbar_elevation));

        categorySlidingTabStrip.setOnClickTabListener(new CategorySlidingTabStrip.OnClickTabListener() {
            @Override
            public void onClickTab(View tab, int index) {
                if (currentCategoryOption != null){
                    List<CategoryOptionBean> subItems = currentCategoryOption.getItems();
                    if (subItems != null && subItems.size() > index){
                        CategoryOptionBean selBean = subItems.get(index);
                        findProduct(selBean);
                    }
                }
            }
        });

        initRecyclerView();
        initCategoryRecyclerView();

        refreshFloatShopcartView();

        load();
    }

    @OnClick(R.id.tv_category_current)
    public void showCategory(){
        if (rootCategory == null){
            loadCategoryInfo();
        }

        TranslateAnimation anim1 = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF,
                0f);
        anim1.setDuration(200);
//        Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.view_top_in);
        anim1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                fabShopcartView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        llCategory.setVisibility(View.VISIBLE);
        mCategoryRecyclerView.startAnimation(anim1);
    }

    @OnClick(R.id.tv_category_all)
    public void hideCategory(){
        TranslateAnimation anim1 = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                -1.0f);
        anim1.setDuration(200);

//        Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.view_top_out);
        anim1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                llCategory.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mCategoryRecyclerView.startAnimation(anim1);
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

            shopId = intent.getLongExtra(EXTRA_KEY_SHOP_ID, 0);
            curCategoryId = intent.getStringExtra(EXTRA_KEY_CATEGORY_ID);
            shopEntity = ShopService.get().getEntityById(shopId);
        }
    }


    private void initRecyclerView() {
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setHasFixedSize(true);
//        View headerView = LayoutInflater.from(getContext()).inflate(R.layout.view_item_home_recycler_header, null);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//添加分割线
//        mRecyclerView.addItemDecoration(new LineItemDecoration(
//                this, LineItemDecoration.VERTICAL_LIST));
//        mRecyclerView.setOnTouchListener(
//                new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        return mIsRefreshing;
//                    }
//                }
//        );
//        mRecyclerView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                int position = mRecyclerView.getChildPosition(v);
//                DialogUtil.showHint("click " + position);
//                return false;
//            }
//        });
    }
    private void initCategoryRecyclerView(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mCategoryRecyclerView.setLayoutManager(linearLayoutManager);
        mCategoryRecyclerView.setHasFixedSize(true);
//        View headerView = LayoutInflater.from(getContext()).inflate(R.layout.view_item_home_recycler_header, null);
        //设置Item增加、移除动画
        mCategoryRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private boolean bannersViewIsShown() {
        return ViewHelper.getTranslationY(tvCurrentCategory) == 0;
    }

    private boolean bannersViewIsHidden() {
        return ViewHelper.getTranslationY(tvCurrentCategory) == -tvCurrentCategory.getHeight();
    }

    private void showBannersView() {
        float headerTranslationY = ViewHelper.getTranslationY(tvCurrentCategory);
        if (headerTranslationY != 0) {
            ViewPropertyAnimator.animate(tvCurrentCategory).cancel();
            ViewPropertyAnimator.animate(tvCurrentCategory).translationY(0).setDuration(200).start();
        }
    }

    private void hideBannersView() {
        float headerTranslationY = ViewHelper.getTranslationY(tvCurrentCategory);
        int toolbarHeight = tvCurrentCategory.getHeight();
        if (headerTranslationY != -toolbarHeight) {
            ViewPropertyAnimator.animate(tvCurrentCategory).cancel();
            ViewPropertyAnimator.animate(tvCurrentCategory).translationY(-toolbarHeight).setDuration(200).start();
        }
    }

    /**
     * 更新购物车信息
     * */
    private void refreshFloatShopcartView(){
        Map<Long, List<ShoppingCartEntity>> entityMap = ShoppingCartService.get().queryAllByGroup(new PageInfo(1, 100));
        if(entityMap != null && entityMap.size() > 0){
            fabShopcartView.setNumber(entityMap.size());
            fabShopcartView.setVisibility(View.VISIBLE);
        }
        else{
            fabShopcartView.setNumber(0);
            fabShopcartView.setVisibility(View.GONE);
        }
    }

    private void loadCategoryInfo(){
        if (shopEntity == null){
            return;
        }

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<CategoryInfoBean,
                NetProcessor.Processor<CategoryInfoBean>>(
                new NetProcessor.Processor<CategoryInfoBean>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("processFailure: " + errMsg);
//                        orderPayFailed(-1);
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                        RspBean<CategoryInfoBean> retValue = (RspBean<CategoryInfoBean>) rspData;
                        //商户网站唯一订单号
//                            String outTradeNo = retValue.getValue();
                        ZLogger.d("prePayResponse: " + retValue.getValue().toString());
                        rootCategory = retValue.getValue();
                        Message msg = new Message();
                        msg.what = REFRESH_CATEGORY_LIST;
                        mHandler.sendMessage(msg);
                    }
                }
                , CategoryInfoBean.class
                , MfhApplication.getAppContext())
        {
        };

        EnjoycityApiProxy.queryRootCategory(shopEntity.getTenantId(), responseCallback);
    }

    private static final int REFRESH_CATEGORY_LIST = 1;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_CATEGORY_LIST: {
                    setCategoryList();
                    break;
                }
                default:
                    break;
            }
        }
    };

    private CategoryOptionBean getBeanById(String id){
        if (rootCategory == null){
            return null;
        }

        List<CategoryOptionBean> optionBeans = rootCategory.getOptions();

        if (StringUtils.isEmpty(id)){
            return optionBeans.get(0);
        }

        for (CategoryOptionBean bean : optionBeans){

            if (bean.getCode().equals(id)){
                return bean;
            }
            else{
                CategoryOptionBean subBean = getSubBeanById(id, bean.getItems());
                if (subBean != null){
                    return subBean;
                }
            }
        }

        return null;
    }

    private CategoryOptionBean getSubBeanById(String id, List<CategoryOptionBean> subItems){
        if (StringUtils.isEmpty(id) || subItems == null){
            return null;
        }

        for (CategoryOptionBean bean : subItems){
            if (bean.getCode().equals(id)){
                return bean;
            }
            else{
                CategoryOptionBean subBean = getSubBeanById(id, bean.getItems());
                if (subBean != null){
                    return subBean;
                }
            }
        }

        return null;
    }


    private void setCategoryList(){
        List<CategoryOptionBean> categoryItemBeanList = new ArrayList<>();
        if (rootCategory != null){
            categoryItemBeanList = rootCategory.getOptions();

            if (StringUtils.isEmpty(curCategoryId)){
                for (CategoryOptionBean optionBean : categoryItemBeanList){

                    if (optionBean.getCode().equals(curCategoryId)){
                        currentCategoryOption = optionBean;
                        break;
                    }
                    else{
                        List<CategoryOptionBean> subItems = optionBean.getItems();
                        if (subItems != null && subItems.size() > 0){
                            for (CategoryOptionBean bean : subItems){
                                if (bean.getCode().equals(curCategoryId)){
                                    currentCategoryOption = bean;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            else{

            }

        }

        findProduct(getBeanById(curCategoryId));

        AllProductCategoryAdapter allProductCategoryAdapter = new AllProductCategoryAdapter(this, categoryItemBeanList);
        allProductCategoryAdapter.setOnAdapterListsner(new AllProductCategoryAdapter.AdapterListener() {
            @Override
            public void onSelectCategory(CategoryOptionBean bean) {
                hideCategory();
                findProduct(bean);
            }
        });
        mCategoryRecyclerView.setAdapter(allProductCategoryAdapter);

        productAdapter = new ProductAdapter(AllProductActivity.this, null);
        productAdapter.setOnAdapterLitener(new ProductAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void addToShopcart(ProductBean bean) {
                ShoppingCartService dbService = ShoppingCartService.get();
                dbService.addToShopcart(shopId, bean);
            }

            @Override
            public void showProductDetail(ProductBean bean) {
                Bundle extras = new Bundle();
                extras.putInt(ProductDetailActivity.EXTRA_KEY_ANIM_TYPE, 0);
                extras.putLong(ProductDetailActivity.EXTRA_KEY_PRODUCT_ID, bean.getId());
                extras.putLong(ProductDetailActivity.EXTRA_KEY_SHOP_ID, shopId);
                ProductDetailActivity.actionStart(AllProductActivity.this, extras);
            }

            @Override
            public void addToShopcart(float x, float y, ProductBean bean) {

            }
        });
        mRecyclerView.setAdapter(productAdapter);
    }

    private void load(){
        setCategoryList();
        loadCategoryInfo();
    }

    @OnClick(R.id.fab_shopcart)
    public void redirectToCart() {
        ShoppingCartActivity.actionStart(this, ANIM_TYPE_NEW_FLOW);
    }

    private void findProduct(CategoryOptionBean bean){
        currentCategoryOption = bean;

        if (bean == null){
            tvCurrentCategory.setText("");
            return;
        }

        tvCurrentCategory.setText(bean.getValue());

        StringBuilder sb = new StringBuilder();
        sb.append("select " + bean.getValue());

        List<CategoryOptionBean> subItems = bean.getItems();

        if (subItems != null && subItems.size() > 0) {
            categorySlidingTabStrip.removeAllTab();

            for (CategoryOptionBean item : subItems){
                TextView tv = new TextView(this);
                tv.setText(item.getValue());
                tv.setTextColor(Color.parseColor("#000000"));
                tv.setGravity(Gravity.CENTER);

                categorySlidingTabStrip.addTab(tv);
                sb.append("," + item.getValue());
            }

            DialogUtil.showHint(sb.toString());

            categorySlidingTabStrip.setVisibility(View.VISIBLE);
        }else{
            categorySlidingTabStrip.setVisibility(View.GONE);
        }

        NetCallBack.QueryRsCallBack queryResponseCallback = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ProductBean>(new PageInfo(1, 100)) {
                    //                处理查询结果集，子类必须继承
                    @Override
                    public void processQueryResult(RspQueryResult<ProductBean> rs) {//此处在主线程中执行。
                        try {
                            int retSize = rs.getReturnNum();
                            ZLogger.d(String.format("%d result, content:%s", retSize, rs.toString()));

                            List<ProductBean> result = new ArrayList<>();
                            if(retSize > 0){
                                for (int i = 0; i < retSize; i++) {
                                    result.add(rs.getRowEntity(i));
                                }
                            }
                            productAdapter.setProductBeans(result);
                        }
                        catch(Throwable ex){
                            ZLogger.e(ex.toString());
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        ZLogger.d("processFailure: " + errMsg);
                    }
                }
                , ProductBean.class
                , MfhApplication.getAppContext());

        EnjoycityApiProxy.findProduct(bean.getCode(), queryResponseCallback);
    }
}
