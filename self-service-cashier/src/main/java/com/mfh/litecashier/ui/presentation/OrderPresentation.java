package com.mfh.litecashier.ui.presentation;

import android.app.Presentation;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import com.bingshanguxue.cashier.CashierBenchObservable;
import com.bingshanguxue.cashier.model.CashierOrderInfo;
import com.bingshanguxue.cashier.CashierProvider;
import com.bingshanguxue.vector_uikit.widget.AvatarView;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.account.Human;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.uikit.adv.AdvLocalPic;
import com.mfh.framework.uikit.adv.AdvLocalPicAdapter;
import com.mfh.framework.uikit.adv.AdvertisementViewPager;
import com.mfh.framework.uikit.recyclerview.RecyclerViewEmptySupport;
import com.mfh.litecashier.CashierApp;
import com.mfh.litecashier.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * The presentation to show on the secondary display.
 * <p>
 * Note that this display may have different metrics from the display on which
 * the main activity is showing so we must be careful to use the presentation's
 * own {@link Context} whenever we load resources.
 * </p>
 * <p>
 * Created by bingshanguxue on 04/02/2017.
 * <p>
 * 主屏幕分辨率：1366*720 1.000000
 * 副屏幕分辨率：1024*768 1.418750
 */

public class OrderPresentation extends Presentation {
    private AvatarView ivMemberHeader;
    private MultiLayerLabel labelOrderAmount;
    private MultiLayerLabel labelDiscount;
    private MultiLayerLabel labelPromotion;
    private MultiLayerLabel labelCouponDiscount;
    private MultiLayerLabel labelScore;
    private MultiLayerLabel labelActualAmount;
    private RecyclerViewEmptySupport goodsRecyclerView;
    private ImageView emptyView;
    private OrderPresentationGoodsAdapter mGoodsAdapter;

    AdvertisementViewPager advertiseViewPager;
    private AdvLocalPicAdapter mPictureAdvPagerAdapter;

    public OrderPresentation(Context context, Display display) {
        super(context, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        // Get the resources for the context of the presentation.
        // Notice that we are getting the resources from the context of the presentation.
//        Resources r = getContext().getResources();

        // Inflate the layout.
        setContentView(R.layout.presentation_order);

        ivMemberHeader = (AvatarView) findViewById(R.id.iv_vip_header);
        labelOrderAmount = (MultiLayerLabel) findViewById(R.id.label_orderamount);
        labelDiscount = (MultiLayerLabel) findViewById(R.id.label_discount);
        labelPromotion = (MultiLayerLabel) findViewById(R.id.label_promotion);
        labelCouponDiscount = (MultiLayerLabel) findViewById(R.id.label_coupon_discount);
        labelScore = (MultiLayerLabel) findViewById(R.id.label_score);
        labelActualAmount = (MultiLayerLabel) findViewById(R.id.label_actualamount);
        goodsRecyclerView = (RecyclerViewEmptySupport) findViewById(R.id.goods_list);
        emptyView = (ImageView) findViewById(R.id.empty_view);
        advertiseViewPager = (AdvertisementViewPager) findViewById(R.id.viewpager_adv);

        ivMemberHeader.setBorderWidth(3);
        ivMemberHeader.setBorderColor(Color.parseColor("#e8e8e8"));

        initGoodsRecyclerView();

        List<AdvLocalPic> localAdvList = new ArrayList<>();
        localAdvList.add(AdvLocalPic.newInstance(R.mipmap.img_presentation_hb1));
        localAdvList.add(AdvLocalPic.newInstance(R.mipmap.img_presentation_hb1));
//        localAdvList.add(AdvLocalPic.newInstance(R.mipmap.img_presentation_hb3));
        mPictureAdvPagerAdapter = new AdvLocalPicAdapter(getContext(), localAdvList, advertiseViewPager, null);

        advertiseViewPager.setShowPoint(false);
        advertiseViewPager.setAdapter(mPictureAdvPagerAdapter);
        //TODO,定时切换(每隔5秒切换一次)
//        advertiseViewPager.startSlide(5 * 1000);
        advertiseViewPager.setTimerEnabled(false);

        Resources resources = getContext().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        ZLogger.d(String.format("Presentation: %d*%d %f%navigation_bar_height:%d\n",
                resources.getDisplayMetrics().widthPixels,
                resources.getDisplayMetrics().heightPixels,
                resources.getDisplayMetrics().density,
                resources.getDimensionPixelSize(resourceId)));
//
//        getWindow().getWindowManager().getDefaultDisplay().getSize();
    }

    @Override
    protected void onStart() {
        super.onStart();

        CashierBenchObservable.getInstance().addObserver(mObserver);
    }

    @Override
    protected void onStop() {
        super.onStop();

        CashierBenchObservable.getInstance().deleteObserver(mObserver);
    }

    private void initGoodsRecyclerView() {
        try {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CashierApp.getAppContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            goodsRecyclerView.setLayoutManager(linearLayoutManager);
            //enable optimizations if all item views are of the same height and width for
            //signficantly smoother scrolling
            goodsRecyclerView.setHasFixedSize(true);
            //设置列表为空时显示的视图
            goodsRecyclerView.setEmptyView(emptyView);
            //分割线
//            goodsRecyclerView.addItemDecoration(new LineItemDecoration(
//                    getOwnerActivity(), LineItemDecoration.VERTICAL_LIST));

            mGoodsAdapter = new OrderPresentationGoodsAdapter(getContext(), null);
            mGoodsAdapter.setOnAdapterListener(new OrderPresentationGoodsAdapter.OnAdapterListener() {


                @Override
                public void onItemClick(View view, int position) {

                }

                @Override
                public void onDataSetChanged() {

                }

            });
            goodsRecyclerView.setAdapter(mGoodsAdapter);
        } catch (Exception e) {
            e.printStackTrace();
            ZLogger.e(e.toString());
        }

    }

    /**
     * 加载会员信息
     */
    private void refreshMemberInfo(Human memberInfo) {
        if (memberInfo != null) {
            ivMemberHeader.setAvatarUrl(memberInfo.getHeadimageUrl());
        } else {
            ivMemberHeader.setImageResource(R.drawable.chat_tmp_user_head);
        }
    }


    private Observer mObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            try {
                mGoodsAdapter.setEntityList(CashierBenchObservable.getInstance().getShopcartEntities());
                labelOrderAmount.setTopText(String.format("%.2f",
                        CashierBenchObservable.getInstance().getFinalAmount()));//成交价

                CashierOrderInfo cashierOrderInfo = CashierBenchObservable.getInstance().getCashierOrderInfo();
                if (cashierOrderInfo != null) {
                    refreshMemberInfo(cashierOrderInfo.getVipMember());

                    Double handleAmount = CashierProvider.getHandleAmount(cashierOrderInfo);
                    labelDiscount.setTopText(String.format("%.2f",
                            CashierBenchObservable.getInstance().getItemRuleAmount()));
                    labelPromotion.setTopText(String.format("%.2f",
                            CashierBenchObservable.getInstance().getPackRuleAmount()));
                    labelCouponDiscount.setTopText(String.format("%.2f",
                            CashierBenchObservable.getInstance().getCouponAmount()));
                    labelScore.setTopText(String.format("%.0f", Math.abs(handleAmount / 2)));
                    labelActualAmount.setTopText(String.format("%.2f", handleAmount));
                } else {
                    refreshMemberInfo(null);
                    labelDiscount.setTopText(String.format("%.2f", 0D));
                    labelPromotion.setTopText(String.format("%.2f", 0D));
                    labelCouponDiscount.setTopText(String.format("%.2f", 0D));
                    labelScore.setTopText(String.format("%.0f", 0D));
                    labelActualAmount.setTopText(String.format("%.2f", 0D));
                }

                //java.lang.IllegalMonitorStateException: object not locked by thread before notifyAll()
//                advertiseViewPager.notifyAll();
//        mPictureAdvPagerAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                ZLogger.ef(e.toString());
            }
        }
    };

    /**
     * 刷新订单信息
     */
    public void setText(String text) {
        DialogUtil.showHint(text);
        ZLogger.d(text);

    }
}
