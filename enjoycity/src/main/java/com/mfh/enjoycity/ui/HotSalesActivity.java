package com.mfh.enjoycity.ui;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.adapter.HotsaleProductAdapter;
import com.mfh.enjoycity.bean.HotSaleProductBean;
import com.mfh.enjoycity.database.ShopEntity;
import com.mfh.enjoycity.database.ShopService;
import com.mfh.enjoycity.database.ShoppingCartEntity;
import com.mfh.enjoycity.database.ShoppingCartService;
import com.mfh.enjoycity.ui.activity.ShoppingCartActivity;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.enjoycity.view.FloatShopcartView;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.uikit.recyclerview.GridItemDecoration;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.TypeEvaluator;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


/**
 * 热卖商品
 * Created by Nat.ZZN(bingshanguxue) on 15/8/5.
 * */
public class HotSalesActivity extends BaseActivity {

    public static final String EXTRA_KEY_SHOP_ID = "shopId";

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.recycler_view_hot_sales)
    RecyclerView mRecyclerView;
    @Bind(R.id.fab_shopcart)
    FloatShopcartView fabShopcartView;
    @Bind(R.id.tv_empty)
    TextView tvEmpty;
    @Bind(R.id.animProgress)
    ProgressBar animProgress;

    private Long shopId;
    private ShopEntity shopEntity;
    private HotsaleProductAdapter productAdapter;


    private BroadcastReceiver receiver;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, HotSalesActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_hot_sale;
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(R.string.toolbar_title_hotsales);
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
                        HotSalesActivity.this.onBackPressed();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

        registerReceiver();

        initRecyclerView();

        refreshFloatShopcartView();

        load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFloatShopcartView();

        if (productAdapter != null){
            productAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);

        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.ACTIVITY_REQUEST_LOGIN_H5)
        {
            if(resultCode == Activity.RESULT_OK){
                load();
            }
            else{
                finish();
            }
        }
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
            shopEntity = ShopService.get().getEntityById(shopId);

            if (shopEntity == null){
                finish();
            }
        }
    }

    private void initRecyclerView() {
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setHasFixedSize(true);
//        mRecyclerView.setScrollViewCallbacks(mScrollViewScrollCallbacks);
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
        mRecyclerView.addItemDecoration(new GridItemDecoration(
                3, 2, false));
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

        productAdapter = new HotsaleProductAdapter(HotSalesActivity.this, null);
        productAdapter.setOnAdapterLitener(new HotsaleProductAdapter.AdapterListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void addToShopcart(HotSaleProductBean bean) {
                ShoppingCartService dbService = ShoppingCartService.get();
                dbService.addToShopcart(shopId, bean);

                fabShopcartView.setVisibility(View.VISIBLE);
                fabShopcartView.animate().translationY(0)
                        .setInterpolator(new AccelerateInterpolator(2)).start();
            }

            @Override
            public void addToShopcart(float x, float y, HotSaleProductBean bean) {
                ShoppingCartService dbService = ShoppingCartService.get();
                dbService.addToShopcart(shopId, bean);

                fabShopcartView.setVisibility(View.VISIBLE);
                fabShopcartView.animate().translationY(0)
                        .setInterpolator(new AccelerateInterpolator(2)).start();
                if (fabShopcartView.getTranslationY() == 0){
                    playAnimation(x, y, fabShopcartView);
                }
            }

            @Override
            public void showProductDetail(HotSaleProductBean bean) {
                Bundle extras = new Bundle();
                extras.putInt(ProductDetailActivity.EXTRA_KEY_ANIM_TYPE, 0);
                extras.putLong(ProductDetailActivity.EXTRA_KEY_PRODUCT_ID, bean.getProductId());
                extras.putLong(ProductDetailActivity.EXTRA_KEY_SHOP_ID, shopId);
                ProductDetailActivity.actionStart(HotSalesActivity.this, extras);
            }
        });
        mRecyclerView.setAdapter(productAdapter);
    }

//    private ObservableScrollViewCallbacks mScrollViewScrollCallbacks = new ObservableScrollViewCallbacks() {
//        @Override
//        public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
//        }
//
//        @Override
//        public void onDownMotionEvent() {
//        }
//
//        @Override
//        public void onUpOrCancelMotionEvent(ScrollState scrollState) {
////            mBaseTranslationY = 0;
//
//            int scrollY = mRecyclerView.getCurrentScrollY();
//
//            if (scrollState == ScrollState.DOWN) {
//                MLog.d(String.format("scrollState:down %d, topEdge %d", scrollY,
//                        mRecyclerView.getScrollY()));
////                if(scrollY <= 0){
////                    showBannersView();
////                }
//                fabShopcartView.setVisibility(View.VISIBLE);
//                fabShopcartView.animate().translationY(0)
//                        .setInterpolator(new AccelerateInterpolator(2)).start();
//            } else if (scrollState == ScrollState.UP) {
////                            fabShopcartView.setVisibility(View.GONE);
//                fabShopcartView.animate().translationY(fabShopcartView.getHeight() + DensityUtil.dip2px(HotSalesActivity.this, 16))
//                        .setInterpolator(new AccelerateInterpolator(2)).start();
//            } else {
//            }
//        }
//    };

    private void refreshEmptyText(){
        if (productAdapter != null && productAdapter.getItemCount() > 0){
            tvEmpty.setVisibility(View.GONE);
        }else{
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void load(){
        if(!NetworkUtils.isConnect(this)){
            animProgress.setVisibility(View.GONE);
            DialogUtil.showHint(R.string.toast_network_error);
            refreshEmptyText();
            return;
        }

        animProgress.setVisibility(View.VISIBLE);

        //回调
        NetCallBack.QueryRsCallBack queryResponseCallback = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<HotSaleProductBean>(new PageInfo(1, 100)) {
                    @Override
                    public void processQueryResult(RspQueryResult<HotSaleProductBean> rs) {//此处在主线程中执行。
                        try {
                            int retSize = rs.getReturnNum();
                            ZLogger.d(String.format("%d result, content:%s", retSize, rs.toString()));

                            List<HotSaleProductBean> result = new ArrayList<>();
                            for (int i = 0; i < retSize; i++) {
                                result.add(rs.getRowEntity(i));
                            }

                            EventBus.getDefault().post(
                                    new HotsaleProductsEvent(result));
                        }
                        catch(Throwable ex){
                            ZLogger.e(ex.toString());
                        }finally{
                            animProgress.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        ZLogger.d("processFailure: " + errMsg);

                        EventBus.getDefault().post(
                                new HotsaleProductsEvent(null));
                    }
                }
                , HotSaleProductBean.class
                , MfhApplication.getAppContext());

        EnjoycityApiProxy.queryShopHotSales(shopId, queryResponseCallback);
    }

    @OnClick(R.id.fab_shopcart)
    public void redirectToCart() {
        ShoppingCartActivity.actionStart(this, ANIM_TYPE_NEW_FLOW);
    }

    private void registerReceiver(){
        IntentFilter filter = new IntentFilter();//接收者只有在activity才起作用。
        filter.addAction(Constants.ACTION_TOGGLE_FLOAT);
        filter.addAction(Constants.BROADCAST_ACTION_SHOPCART_REFRESH);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action){
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
    }

    public class HotsaleProductsEvent {

        private List<HotSaleProductBean> beanList;
        public HotsaleProductsEvent(List<HotSaleProductBean> beanList) {
            // TODO Auto-generated constructor stub
            this.beanList = beanList;
        }

        public List<HotSaleProductBean> getBeanList() {
            return beanList;
        }
    }

    /**
     * 热卖商品列表
     * */
    public void onEventMainThread(HotsaleProductsEvent event) {
//        List<HotsaleProductBean> beanList = event.getBeanList();

        productAdapter.setProductBeans(shopId, event.getBeanList());

        animProgress.setVisibility(View.GONE);
        refreshEmptyText();
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
//        MLog.e(String.format("ValueAnimator：from(%f,%f) to(%f,%f) maxEval(%f) duration(%d) g(%f)",
//                point1.x, point1.y, point2.x, point2.y, maxEval, duration, g));

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
