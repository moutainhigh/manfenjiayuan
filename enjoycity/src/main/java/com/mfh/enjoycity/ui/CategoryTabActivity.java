package com.mfh.enjoycity.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.adapter.AllProductCategoryAdapter;
import com.mfh.enjoycity.bean.CategoryInfoBean;
import com.mfh.enjoycity.bean.CategoryOptionBean;
import com.mfh.enjoycity.database.ShopEntity;
import com.mfh.enjoycity.database.ShopService;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.enjoycity.ui.activity.ShoppingCartActivity;
import com.mfh.enjoycity.ui.fragments.CategoryFragment;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.enjoycity.view.CategoryFragmentPagerAdapter;
import com.mfh.enjoycity.view.CategorySlidingTabStrip;
import com.mfh.enjoycity.view.FloatShopcartView;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DensityUtil;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.recyclerview.LineItemDecoration;
import com.mfh.framework.uikit.widget.ViewPageInfo;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.TypeEvaluator;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * 类目
 * */
public class CategoryTabActivity extends BaseActivity {

    public static final String EXTRA_KEY_SHOP_ID = "EXTRA_KEY_SHOP_ID";
    public static final String EXTRA_KEY_CATEGORY_ID = "EXTRA_KEY_CATEGORY_ID";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.category_header)
    View categoryHeaderView;
    @Bind(R.id.tv_category_current)
    TextView tvCurrentCategory;
    @Bind(R.id.tv_category_all)
    TextView tvCategoryOptions;
    @Bind(R.id.sticky_category_tab)
    CategorySlidingTabStrip categorySlidingTabStrip;

    @Bind(R.id.tab_viewpager)
    ViewPager mViewPager;
    private CategoryFragmentPagerAdapter viewPagerAdapter;

    @Bind(R.id.ll_category) LinearLayout llCategory;
    @Bind(R.id.recycler_view_category)
    RecyclerView mCategoryRecyclerView;
    private AllProductCategoryAdapter categoryAdapter;


    @Bind(R.id.animProgress)
    ProgressBar animProgress;
    @Bind(R.id.fab_shopcart)
    FloatShopcartView fabShopcartView;

    private Long shopId;
    private ShopEntity shopEntity;
    private CategoryInfoBean rootCategory;
    private String curCategoryId;


    private BroadcastReceiver receiver;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, CategoryTabActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_catogory_tab;
    }

    @Override
    protected void initToolBar() {
        if (shopEntity != null){
            toolbar.setTitle(shopEntity.getShopName());
        }
        else{
            toolbar.setTitle("");
        }
        toolbar.setBackgroundColor(this.getResources().getColor(R.color.transparent));
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
                        CategoryTabActivity.this.onBackPressed();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        registerReceiver();

        //setupViewPager
        viewPagerAdapter = new CategoryFragmentPagerAdapter(getSupportFragmentManager(), categorySlidingTabStrip, mViewPager);
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果
        mViewPager.setOffscreenPageLimit(3);

        initCategoryRecyclerView();
        refreshFloatShopcartView();

        loadCategoryInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);

        //android.app.IntentReceiverLeaked: Activity com.mfh.enjoycity.ui.activity.UserActivity has leaked IntentReceiver com.mfh.enjoycity.ui.activity.UserActivity$3@443b09b8 that was originally registered here. Are you missing a call to unregisterReceiver()?
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
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
//                fabShopcartView.setVisibility(View.GONE);

                fabShopcartView.animate().translationY(fabShopcartView.getHeight() +
                        DensityUtil.dip2px(CategoryTabActivity.this, 16))
                        .setInterpolator(new AccelerateInterpolator(2)).start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        tvCurrentCategory.setVisibility(View.GONE);
        tvCategoryOptions.setVisibility(View.VISIBLE);
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
                tvCurrentCategory.setVisibility(View.VISIBLE);
                tvCategoryOptions.setVisibility(View.GONE);
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
            ZLogger.d(String.format("curCategoryId=%s", (curCategoryId == null ? "null" : curCategoryId)));
        }
    }

    /**
     * 初始化*/
    private void initCategoryRecyclerView(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mCategoryRecyclerView.setLayoutManager(linearLayoutManager);
        mCategoryRecyclerView.setHasFixedSize(true);
//        View headerView = LayoutInflater.from(getContext()).inflate(R.layout.view_item_home_recycler_header, null);
        //设置Item增加、移除动画
        mCategoryRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
        mCategoryRecyclerView.addItemDecoration(new LineItemDecoration(
                this, LineItemDecoration.VERTICAL_LIST));
        categoryAdapter = new AllProductCategoryAdapter(this, null);
        categoryAdapter.setOnAdapterListsner(new AllProductCategoryAdapter.AdapterListener() {
            @Override
            public void onSelectCategory(CategoryOptionBean bean) {
                hideCategory();

                //没有改变
//                if (bean != null && !StringUtils.isEmpty(bean.getCode())
//                        && !StringUtils.isEmpty(curCategoryId) && bean.getCode().equals(curCategoryId)){
//                    return;
//                }
                refresh(bean);
            }
        });
        mCategoryRecyclerView.setAdapter(categoryAdapter);
    }

    private void showHeaderView() {
        //TODO
//        float headerTranslationY = ViewHelper.getTranslationY(tvCurrentCategory);
//        if (headerTranslationY != 0) {
//            ViewPropertyAnimator.animate(tvCurrentCategory).cancel();
//            ViewPropertyAnimator.animate(tvCurrentCategory).translationY(0).setDuration(200).start();
//        }

        categoryHeaderView.setVisibility(View.VISIBLE);
    }

    private void hideHeaderView() {
        //TODO
//        float headerTranslationY = ViewHelper.getTranslationY(tvCurrentCategory);
//        int toolbarHeight = tvCurrentCategory.getHeight();
//        if (headerTranslationY != -toolbarHeight) {
//            ViewPropertyAnimator.animate(tvCurrentCategory).cancel();
//            ViewPropertyAnimator.animate(tvCurrentCategory).translationY(-toolbarHeight).setDuration(200).start();
//        }
        categoryHeaderView.setVisibility(View.GONE);
    }

    /**
     * 更新购物车信息
     * */
    private void refreshFloatShopcartView(){
        List<ShoppingCartEntity> entityList = ShoppingCartService.get().queryAll();
        if(entityList != null && entityList.size() > 0){
            fabShopcartView.setNumber(entityList.size());
//            fabShopcartView.setVisibility(View.VISIBLE);
        }
        else{
            fabShopcartView.setNumber(0);
//            fabShopcartView.setVisibility(View.GONE);
        }
        fabShopcartView.animate().translationY(0)
                .setInterpolator(new AccelerateInterpolator(2)).start();
    }

    /**
     * 加载类目信息
     * */
    private void loadCategoryInfo(){
        if (shopEntity == null){
            return;
        }

        if(!NetWorkUtil.isConnect(this)){
            DialogUtil.showHint(R.string.toast_network_error);
            animProgress.setVisibility(View.GONE);
            return;
        }

        animProgress.setVisibility(View.VISIBLE);

        NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<CategoryInfoBean,
                NetProcessor.Processor<CategoryInfoBean>>(
                new NetProcessor.Processor<CategoryInfoBean>() {
                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        super.processFailure(t, errMsg);
                        ZLogger.d("processFailure: " + errMsg);
                        animProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void processResult(IResponseData rspData) {
//                            java.lang.ClassCastException: com.mfh.comn.net.data.RspListBean cannot be cast to com.mfh.comn.net.data.RspValue
                        RspBean<CategoryInfoBean> retValue = (RspBean<CategoryInfoBean>) rspData;
                        if (retValue != null){
                            EventBus.getDefault().post(
                                    new CategoryListEvent(retValue.getValue()));
                        }
                        animProgress.setVisibility(View.GONE);
                    }
                }
                , CategoryInfoBean.class
                , MfhApplication.getAppContext())
        {
        };

        EnjoycityApiProxy.queryRootCategory(shopEntity.getTenantId(), responseCallback);
    }

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

    @OnClick(R.id.fab_shopcart)
    public void redirectToCart() {
        ShoppingCartActivity.actionStart(this, ANIM_TYPE_NEW_FLOW);
    }

    private void registerReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_TOGGLE_FLOAT);
        filter.addAction(Constants.ACTION_PLAY_SHOPCART_ANIM);
        filter.addAction(Constants.BROADCAST_ACTION_SHOPCART_REFRESH);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action){
                    case Constants.ACTION_TOGGLE_FLOAT:{
                        boolean enabled = intent.getBooleanExtra(Constants.EXTRA_NAME_FLOAT_ENABLED, true);
                        if(enabled){
                            fabShopcartView.setVisibility(View.VISIBLE);
                            fabShopcartView.animate().translationY(0)
                                    .setInterpolator(new AccelerateInterpolator(2)).start();
                            showHeaderView();
                        }
                        else{
//                            fabShopcartView.setVisibility(View.GONE);
                            fabShopcartView.animate().translationY(fabShopcartView.getHeight() + DensityUtil.dip2px(CategoryTabActivity.this, 16))
                                    .setInterpolator(new AccelerateInterpolator(2)).start();
                            hideHeaderView();
                        }
                    }
                    break;
                    case Constants.ACTION_PLAY_SHOPCART_ANIM:{
                        fabShopcartView.setVisibility(View.VISIBLE);
                        fabShopcartView.animate().translationY(0)
                                .setInterpolator(new AccelerateInterpolator(2)).start();

                        showHeaderView();

                        ZLogger.d(String.format("fabShopcartView:translationY = %f", fabShopcartView.getTranslationY()));
                        if (fabShopcartView.getTranslationY() == 0){
                            float x = intent.getFloatExtra(Constants.EXTRA_NAME_SHOPCART_ANIM_SX, 0);
                            float y = intent.getFloatExtra(Constants.EXTRA_NAME_SHOPCART_ANIM_SY, 0);
                            playAnimation(x, y, fabShopcartView);
                        }
                    }
                    break;
                    case Constants.BROADCAST_ACTION_SHOPCART_REFRESH:{
                        refreshFloatShopcartView();
                    }
                    break;
                }
            }
        };
        registerReceiver(receiver, filter);
    }

    /**
     * 刷新当前类目
     * */
    private void refresh(CategoryOptionBean bean){
        if (bean == null){
            tvCurrentCategory.setText("");

//            categorySlidingTabStrip.setVisibility(View.GONE);
            //加载当前类目
            viewPagerAdapter.removeAll();
            Bundle args = new Bundle();
            args.putLong(CategoryFragment.EXTRA_KEY_SHOP_ID, shopId);
            args.putString(CategoryFragment.EXTRA_KEY_CATEGORY_ID, curCategoryId);
            viewPagerAdapter.addTab(curCategoryId, curCategoryId, CategoryFragment.class,
                    args);
            mViewPager.setOffscreenPageLimit(1);
            return;
        }

        curCategoryId = bean.getCode();
        tvCurrentCategory.setText(bean.getValue());


        categorySlidingTabStrip.setVisibility(View.VISIBLE);

        viewPagerAdapter.removeAll();

        ArrayList<ViewPageInfo> mTabs = new ArrayList<>();

        Bundle args = new Bundle();
        args.putLong(CategoryFragment.EXTRA_KEY_SHOP_ID, shopId);
        args.putString(CategoryFragment.EXTRA_KEY_CATEGORY_ID, bean.getCode());
        mTabs.add(new ViewPageInfo("全部", bean.getCode(), CategoryFragment.class,
                args));
//        viewPagerAdapter.addTab("全部", bean.getCode(), CategoryFragment.class,
//                args);

        //加载子类目
        List<CategoryOptionBean> subItems = bean.getItems();
        if (subItems != null && subItems.size() > 0){
            for (int i = 0; i < subItems.size(); i++){
                CategoryOptionBean entity = subItems.get(i);

                Bundle subArgs = new Bundle();
                subArgs.putLong(CategoryFragment.EXTRA_KEY_SHOP_ID, shopId);
                subArgs.putString(CategoryFragment.EXTRA_KEY_CATEGORY_ID, entity.getCode());
                mTabs.add(new ViewPageInfo(entity.getValue(), entity.getCode(), CategoryFragment.class,
                        subArgs));
            }
        }
        viewPagerAdapter.addAllTab(mTabs);
        mViewPager.setOffscreenPageLimit(1 + mTabs.size());
    }

    public class CategoryListEvent {

        private CategoryInfoBean categoryInfoBean;
        public CategoryListEvent(CategoryInfoBean categoryInfoBean) {
            // TODO Auto-generated constructor stub
            this.categoryInfoBean = categoryInfoBean;
        }

        public CategoryInfoBean getCategoryInfoBean() {
            return categoryInfoBean;
        }
    }

    /**
     * 热卖商品列表
     * */
    public void onEventMainThread(CategoryListEvent event) {
//        List<HotsaleProductBean> beanList = event.getBeanList();

        rootCategory = event.getCategoryInfoBean();

        refresh(getBeanById(curCategoryId));

        if (rootCategory != null){
            categoryAdapter.setCategoryBeanList(rootCategory.getOptions());
        }else{
            categoryAdapter.setCategoryBeanList(null);
        }
    }

    /**
     * 播放购物车动画
     * */
    public void playAnimation(float x1, float y1, View destView){
        ZLogger.d(String.format("playAnimation from(%f,%f)", x1, y1));
        //create a ball
        final ImageView ballView = new ImageView(this);
        ballView.setBackgroundResource(R.drawable.ic_productcard_shopcart_green);
        ballView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ballView.setX(x1);
        ballView.setY(y1);
        this.addContentView(ballView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

//        int x1 = DensityUtils.dip2px(this, Float.valueOf(String.valueOf(new Random().nextInt(60))));
//        int y1 = DensityUtils.dip2px(this, Float.valueOf(String.valueOf(new Random().nextInt(80))));
        final PointF point1 = new PointF(x1, y1);
        //getLocationOnScreen 计算该视图在全局坐标系中的x，y值
        //getLocationInWindow 计算该视图在它所在的widnow的坐标x，y值
        //getLeft , getTop, getBottom, getRight, 这一组是获取相对在它父亲里的坐标
//        int[] location2 = new int[2];
//        destView.getLocationInWindow(location2);//跳到屏幕外面
//        destView.getLocationOnScreen(location2);//跳到屏幕外面
        Rect viewRect2 = new Rect();
        destView.getGlobalVisibleRect(viewRect2);
//        int x2 = DensityUtils.dip2px(this, viewRect2.centerX());
//        int y2 = DensityUtils.dip2px(this, viewRect2.centerY());
//        int x2 = viewRect2.centerX();
        float x2 = viewRect2.centerX();
        float y2 = viewRect2.top;//.centerY();
        final PointF point2 = new PointF(x2, y2);
        final float speed = 400;
        final float maxEval = Math.abs((x2-x1)/speed);
        //java.util.IllegalFormatConversionException: %f can't format java.lang.Integer arguments
        //java.util.UnknownFormatConversionException: Conversion: .String.format("%.f")
        //java.util.MissingFormatWidthException: -f String.format("%0f")
        //java.lang.NumberFormatException: Invalid int: "  1740" String.format("%6.0f")
//        final int duration = Integer.valueOf(String.format("%6.0f", maxEval * 1000));
        final int duration = Math.round(maxEval * 1000);
        //自由落体2h=g*t*t
        final float g = (y2-y1)*2/(maxEval*maxEval);
        //java.util.IllegalFormatConversionException: %f can't format java.lang.Integer arguments
        ZLogger.e(String.format("ValueAnimator：from(%f,%f) to(%f,%f) maxEval(%f) duration(%d) g(%f)",
                point1.x, point1.y, point2.x, point2.y, maxEval, duration, g));

        TypeEvaluator pointEvaluator = new TypeEvaluator<PointF>()
        {
            // fraction = t / duration
            @Override
            public PointF evaluate(float fraction, PointF startValue,
                                   PointF endValue)
            {
                // x方向200px/s ，则y方向0.5 * 10 * t
                PointF point = new PointF();
                point.x = startValue.x + speed * fraction * maxEval;
                point.y = startValue.y + 0.5f * g * (fraction * maxEval) * (fraction * maxEval);

//                if(fraction == 1.0){
//                    MLog.e(String.format("ValueAnimator：(%f,%f) startValue(%f,%f) endValue(%f,%f) update(%f,%f)",
//                            fraction, fraction * duration, startValue.x, startValue.y, endValue.x, endValue.y, point.x, point.y));
//                }

                return point;
            }
        };
        ValueAnimator valueAnimator = ValueAnimator.ofObject(pointEvaluator, point1, point2);
        valueAnimator.setDuration(duration);//动画的持续时间
        valueAnimator.setObjectValues(point1, point2);//设置起始位置
        valueAnimator.setInterpolator(new LinearInterpolator());
//        valueAnimator.setTarget(new PointF(120, 200));//设置目标位置
//        valueAnimator.setEvaluator(new TypeEvaluator<PointF>()
//        {
//            // fraction = t / duration
//            @Override
//            public PointF evaluate(float fraction, PointF startValue,
//                                   PointF endValue)
//            {
//                MLog.e(fraction * 3 + "");
//                // x方向200px/s ，则y方向0.5 * 10 * t
//                PointF point = new PointF();
//                point.x = 200 * fraction * 3;
//                point.y = 0.5f * 200 * (fraction * 3) * (fraction * 3);
//                return point;
//            }
//        });

        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF point = (PointF) animation.getAnimatedValue();
                ballView.setX(point.x);
                ballView.setY(point.y);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                // do something when the animation is done
//                MLog.e("onAnimationEnd");
                ViewGroup parent = (ViewGroup) ballView.getParent();
                if (parent != null)
                    parent.removeView(ballView);
            }

            public void onAnimationCancel(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }
        });
    }
}
