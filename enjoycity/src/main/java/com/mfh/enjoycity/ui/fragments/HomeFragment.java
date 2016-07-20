package com.mfh.enjoycity.ui.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;

import com.mfh.comn.bean.PageInfo;
import com.mfh.comn.net.data.RspQueryResult;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.ShopBean;
import com.mfh.enjoycity.database.ShopEntity;
import com.mfh.enjoycity.database.ShopService;
import com.mfh.enjoycity.events.HomeSubdisEvent;
import com.mfh.enjoycity.utils.EnjoycityApiProxy;
import com.mfh.enjoycity.view.HomeViewPageFragmentAdapter;
import com.mfh.enjoycity.view.NoShopView;
import com.mfh.enjoycity.widget.WebviewViewPager;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.H5Api;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.net.URLHelper;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.EmptyLayout;
import com.mfh.framework.uikit.widget.PagerSlidingTabStrip;

import java.util.Date;
import java.util.List;

import butterknife.Bind;
import de.greenrobot.event.EventBus;


/**
 * 首页·
 * 
 * @author Nat.ZZN created on 2015-04-13
 * @since Framework 1.0
 */
public class HomeFragment extends BaseFragment {
    @Bind(R.id.view_shop_container)
    View shopContainerView;
    @Bind(R.id.tab_layout)
    PagerSlidingTabStrip mTabStrip;
    @Bind(R.id.tab_viewpager)
    WebviewViewPager mViewPager;
    private HomeViewPageFragmentAdapter viewPagerAdapter;
    @Bind(R.id.view_noshop)
    NoShopView noShopView;
    @Bind(R.id.view_empty)
    EmptyLayout emptyView;

    private Long currentSubdisId;

    public HomeFragment() {
        super();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_home;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        //setupViewPager
        viewPagerAdapter = new HomeViewPageFragmentAdapter(getFragmentManager(), mTabStrip, mViewPager);
//        tabViewPager.setPageTransformer(true, new ZoomOutPageTransformer());//设置动画切换效果

        emptyView.setLoadingTheme(1);
        emptyView.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload(currentSubdisId);
            }
        });

        shopContainerView.setVisibility(View.GONE);
        noShopView.setVisibility(View.VISIBLE);
        noShopView.refresh();
        noShopView.setViewListener(new NoShopView.ViewListener() {
            @Override
            public void onSearch() {
                EventBus.getDefault().post(new HomeSubdisEvent());
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void reload(Long subdisId){
        currentSubdisId = subdisId;

        if (!NetWorkUtil.isConnect(getContext())){
            DialogUtil.showHint(R.string.tip_network_error);
            return;
        }

        emptyView.setErrorType(EmptyLayout.NETWORK_LOADING);

        //回调
        NetCallBack.QueryRsCallBack responseCallback = new NetCallBack.QueryRsCallBack<>(
                new NetProcessor.QueryRsProcessor<ShopBean>(new PageInfo(1, 100)) {
                    //                处理查询结果集，子类必须继承
                    @Override
                    public void processQueryResult(RspQueryResult<ShopBean> rs) {//此处在主线程中执行。
                        saveArroundMarketShops(rs);
                    }

                    @Override
                    protected void processFailure(Throwable t, String errMsg) {
                        ZLogger.d("processFailure: " + errMsg);
                        super.processFailure(t, errMsg);
                        Message message = new Message();
                        message.what =MSG_ERROR;
                        uiHandler.sendMessage(message);
                    }
                }
                , ShopBean.class
                , MfhApplication.getAppContext());

        EnjoycityApiProxy.findArroundMarketShops(subdisId, responseCallback);
    }


    private void saveArroundMarketShops(RspQueryResult<ShopBean> rs) {
        try {
            int retSize = rs.getReturnNum();
            ZLogger.d(String.format("%d result, content:%s", retSize, rs.toString()));

            ShopService dbService = ShopService.get();
            dbService.clear();

            if(retSize < 1){
                Message message = new Message();
                message.what = MSG_NONE;
                uiHandler.sendMessage(message);
                return;
            }
            else{
                //保存店铺信息到数据库
                for (int i = 0; i < retSize; i++) {
                    ShopBean bean = rs.getRowEntity(i);

                    ShopEntity entity = new ShopEntity();
                    entity.setId(bean.getId());
                    entity.setCreatedDate(new Date());
                    entity.setShopName(bean.getShopName());
                    entity.setShopLogoUrl(bean.getShopLogoUrl());
                    entity.setTenantId(bean.getTenantId());
                    dbService.save(entity);
                }

                Message message = new Message();
                message.what = MSG_SUCCESS;
                uiHandler.sendMessage(message);
            }
        }
        catch(Throwable ex){
            ZLogger.e(ex.toString());
            Message message = new Message();
            message.what = MSG_ERROR;
            uiHandler.sendMessage(message);
        }
    }

    private final static int MSG_NONE = 0;
    private final static int MSG_ERROR = 1;
    private final static int MSG_SUCCESS = 2;
    private Handler uiHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NONE:
                    DialogUtil.showHint("无结果，请重新再试一次");
                    emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
                    break;
                case MSG_ERROR:
                    emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
                    break;
                case MSG_SUCCESS:
                    emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
                    refresh();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void refresh(Long shopId){
        List<ShopEntity> entityList = ShopService.get().queryAll();
        if (entityList != null && entityList.size() > 0){
            noShopView.setVisibility(View.GONE);
            shopContainerView.setVisibility(View.VISIBLE);

            int index = 0;
            viewPagerAdapter.removeAll();
            for (int i = 0; i < entityList.size(); i++){
                ShopEntity entity = entityList.get(i);
//                if(i == 1){
//                    Bundle extras = new Bundle();
//                    extras.putLong(ShopHomeFragment.EXTRA_KEY_SHOP_ID, entity.getShopId());
//                    extras.putString(ShopHomeFragment.EXTRA_KEY_SHOP_NAME, entity.getShopName());
//                    extras.putInt(ShopHomeFragment.EXTRA_KEY_SHOP_POSITION, i);
//                    viewPagerAdapter.addTab("bingshanguxue", "bingshanguxue", ShopHomeFragment.class,
//                            extras);
////                    continue;
//                }

                Bundle args = new Bundle();
                args.putString(ShopHomeWebFragment.EXTRA_KEY_REDIRECT_URL,
                        URLHelper.append(H5Api.URL_HOME_SHOP, String.format("shopId=%d", entity.getId())));
                args.putLong(ShopHomeWebFragment.EXTRA_KEY_SHOP_ID, entity.getId());
                args.putInt(ShopHomeWebFragment.EXTRA_KEY_SHOP_POSITION, i);
                viewPagerAdapter.addTab(entity.getShopName(), entity.getShopName(), ShopHomeWebFragment.class,
                        args);

                if (shopId.compareTo(entity.getId()) == 0){
                    index = i;
                }
            }

            mViewPager.setOffscreenPageLimit(entityList.size());
            mViewPager.setCurrentItem(index, true);
        }else{
            noShopView.setVisibility(View.VISIBLE);
            noShopView.refresh();
            shopContainerView.setVisibility(View.GONE);
        }
    }
    /**
     * 刷新
     * */
    private void refresh(){
        List<ShopEntity> entityList = ShopService.get().queryAll();
        if (entityList != null && entityList.size() > 0){
            noShopView.setVisibility(View.GONE);
            shopContainerView.setVisibility(View.VISIBLE);

            viewPagerAdapter.removeAll();
            for (int i = 0; i < entityList.size(); i++){
                ShopEntity entity = entityList.get(i);
//                if(i == 1){
//                    Bundle extras = new Bundle();
//                    extras.putLong(ShopHomeFragment.EXTRA_KEY_SHOP_ID, entity.getShopId());
//                    extras.putString(ShopHomeFragment.EXTRA_KEY_SHOP_NAME, entity.getShopName());
//                    extras.putInt(ShopHomeFragment.EXTRA_KEY_SHOP_POSITION, i);
//                    viewPagerAdapter.addTab("bingshanguxue", "bingshanguxue", ShopHomeFragment.class,
//                            extras);
////                    continue;
//                }


                Bundle args = new Bundle();
                args.putString(ShopHomeWebFragment.EXTRA_KEY_REDIRECT_URL,
                        URLHelper.append(H5Api.URL_HOME_SHOP, String.format("shopId=%d", entity.getId())));
                args.putLong(ShopHomeWebFragment.EXTRA_KEY_SHOP_ID, entity.getId());
                args.putInt(ShopHomeWebFragment.EXTRA_KEY_SHOP_POSITION, i);
                viewPagerAdapter.addTab(entity.getShopName(), entity.getShopName(), ShopHomeWebFragment.class,
                        args);
            }

            mViewPager.setOffscreenPageLimit(entityList.size());
        }else{
            noShopView.setVisibility(View.VISIBLE);
            shopContainerView.setVisibility(View.GONE);
        }
    }

    public Long getCurrentSubdisId() {
        return currentSubdisId;
    }
}
