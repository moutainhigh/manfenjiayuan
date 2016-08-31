package com.mfh.buyers.fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mfh.buyers.R;
import com.mfh.buyers.bean.UserProfile;
import com.mfh.buyers.ui.activity.NativeWebViewActivity;
import com.mfh.buyers.ui.settings.SettingsActivity;
import com.mfh.buyers.ui.settings.UserProfileActivity;
import com.mfh.buyers.ui.web.ComnJBH5Activity;
import com.mfh.buyers.utils.MobileURLConf;
import com.mfh.buyers.utils.NetProxy;
import com.mfh.buyers.utils.Constants;
import com.mfh.buyers.utils.UIHelper;
import com.mfh.buyers.utils.UserProfileHelper;
import com.mfh.buyers.view.BadgeViewButton;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetWorkUtil;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.uikit.widget.AvatarView;
import com.mfh.framework.uikit.widget.EmptyLayout;
import com.mfh.framework.uikit.widget.LoadingImageView;
import com.mfh.framework.uikit.widget.ReboundScrollView;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 我·
 * 
 * @author Nat.ZZN created on 2015-04-13
 * @since Framework 1.0
 */
public class IndividualFragment extends BaseFragment{
    private static final int MAX_INDEX          = 7;
    private static final int INDEX_ORDERS       = 0;//订单
    private static final int INDEX_CART         = 1;//购物车
    private static final int INDEX_WLLET        = 2;//钱包
    private static final int INDEX_CARDPACK     = 3;//卡包
    private static final int INDEX_PARCEL       = 4;//包裹
//    private static final int INDEX_MFH_PARTERS  = 5;//满分小伙伴
//    private static final int INDEX_FEEDBACK     = 6;//反馈

    @Bind(R.id.reboundScrollView)
    ReboundScrollView scrollView;
    @Bind(R.id.iv_header)
    AvatarView ivHeader;
    @Bind(R.id.tv_username) TextView tvUsername;
    @Bind(R.id.iv_vip) ImageView ivVip;

    @Bind({ R.id.ll_tab_balance, R.id.ll_tab_points, R.id.ll_tab_collection })
    List<LinearLayout> llItems1;
    @Bind(R.id.tv_balance) TextView tvBalance;
    @Bind(R.id.tv_points) TextView tvPoints;
    @Bind(R.id.tv_collection) TextView tvCollection;
    @Bind(R.id.button_pending_payment)
    BadgeViewButton btnPendingPayment;
    @Bind(R.id.button_pending_receipt) BadgeViewButton btnPendingReceipt;
    @Bind(R.id.button_pending_evaluation) BadgeViewButton btnPendingEvaluation;
    private SettingsItem[] groupItems;

    @Bind(R.id.loadingImageView)
    LoadingImageView loadingImageView;
    @Bind(R.id.animProgress) ProgressBar animProgress;
    @Bind(R.id.error_view)
    EmptyLayout emptyView;
    private boolean needLoadData = true;//是否需要加载数据

    //Vip Icons
    private int[] vipIcons = new int[]{
            R.drawable.vip_0, R.drawable.vip_1, R.drawable.vip_2,
            R.drawable.vip_3, R.drawable.vip_4, R.drawable.vip_5,
            R.drawable.vip_6, R.drawable.vip_7
    };

    public IndividualFragment() {
        super();
    }

    
    @Override
    public int getLayoutResId() {
        return R.layout.fragment_individual;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initViews();

        //TODO,监听网络变化，提示刷新 OR 自动刷新
        if (!MfhLoginService.get().haveLogined()){
            UIHelper.sendLoginBroadcast(getActivity());

            needLoadData = false;
            return;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ZLogger.d(String.format("IndividualFragment.onActivityResult.requestCode=%d, resultCode=%d", requestCode, resultCode));

        switch (requestCode){
            case Constants.ACTIVITY_REQUEST_CODE_SETTINGS:
                if(resultCode == Activity.RESULT_OK){
                    if(data.getBooleanExtra(Constants.INTENT_KEY_IS_LOGOUT, false)){
                        needLoadData = false;
                        UIHelper.sendLoginBroadcast(getActivity());

                        //在设置页面点击退出账号后需要将之前打开的页面关掉。
//                        getActivity().finish();
                        break;
                    }else{

                    }
                }
            case Constants.ACTIVITY_REQUEST_ME_ORDER:
            case Constants.ACTIVITY_REQUEST_ME_PACKAGE:
            case Constants.ACTIVITY_REQUEST_ME_CART:
            case Constants.ACTIVITY_REQUEST_RECEIVE_STOCK:
            case Constants.ACTIVITY_REQUEST_SUBDIS_SELECT:
                loadData();//刷新数据
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();

        com.mfh.buyers.utils.UIHelper.sendToggleTabbarBroadcast(getContext(), true);

        //重复请求
        if (MfhLoginService.get().haveLogined()){
            needLoadData = true;
        }
        loadData();
    }

    /**
     * initialize views
     * */
    private void initViews(){
//        ButterKnife.bind(this, rootView);
        scrollView.setSmoothScrollingEnabled(true);

        ivHeader.setOnClickListener(myOnClickListener);
        ivHeader.setBorderWidth(3);
        ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));

        for(LinearLayout layout : llItems1){
            layout.setOnClickListener(myOnClickListener);
        }

        //initialize badge buttons
        btnPendingPayment.init(R.drawable.icon_pending_payment, R.string.label_pending_payment);
        btnPendingPayment.setOnClickListener(myOnClickListener);
        btnPendingReceipt.init(R.drawable.icon_pending_receipt, R.string.label_pending_receipt);
        btnPendingReceipt.setOnClickListener(myOnClickListener);
        btnPendingEvaluation.init(R.drawable.icon_pending_evaluation, R.string.label_pending_evaluation);
        btnPendingEvaluation.setOnClickListener(myOnClickListener);

        initSettingList();

//        loadingImageView.setBackgroundResource(com.mfh.comna.R.drawable.loading_anim);
        emptyView.setLoadingTheme(1);
    }

    private void initSettingList(){
        groupItems = new SettingsItem[MAX_INDEX];
        groupItems[INDEX_ORDERS] = UIHelper.createSettingsItem(rootView, R.id.item_1_0,
                new SettingsItemData(R.drawable.icon_orders_normal,
                        getString(R.string.label_me_orders), getString(R.string.label_me_orders_description)),
                SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW,
                SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_TOP,
                myOnClickListener);
        groupItems[INDEX_CART] = UIHelper.createSettingsItem(rootView, R.id.item_1_1,
                new SettingsItemData(R.drawable.icon_cart_normal,
                        getString(R.string.label_me_cart), String.format(getString(R.string.label_me_cart_description), "0")),
                SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW,
                SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_BOTTOM,
                myOnClickListener);
        groupItems[INDEX_WLLET] = UIHelper.createSettingsItem(rootView, R.id.item_2_0,
                new SettingsItemData(R.drawable.icon_wallet_normal,
                        getString(R.string.label_me_wallet), getString(R.string.label_me_wallet_description)),
                SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW,
                SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_TOP,
                myOnClickListener);
        groupItems[INDEX_CARDPACK] = UIHelper.createSettingsItem(rootView, R.id.item_2_1,
                new SettingsItemData(R.drawable.icon_cardpack_normal,
                        getString(R.string.label_me_carkpack), getString(R.string.label_me_carkpack_description)),
                SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW,
                SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_CENTER,
                myOnClickListener);
        groupItems[INDEX_PARCEL] = UIHelper.createSettingsItem(rootView, R.id.item_2_2,
                new SettingsItemData(R.drawable.icon_parcel_normal,
                        getString(R.string.label_me_parcel), getString(R.string.label_me_parcel_description)),
                SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW,
                SettingsItem.SeperateLineType.SEPERATE_LINE_MULTI_BOTTOM,
                myOnClickListener);
//        groupItems[INDEX_MFH_PARTERS] = UIHelper.createSettingsItem(rootView, R.id.item_3_0,
//                new SettingsItemData(R.drawable.icon_mfhpartner_normal,
//                        getString(R.string.label_me_mfhpartner), getString(R.string.label_me_mfhpartner_description)),
//                SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW,
//                SettingsItem.SeperateLineType.SEPERATE_LINE_SINGLE,
//                myOnClickListener);
//        groupItems[INDEX_FEEDBACK] = UIHelper.createSettingsItem(rootView, R.id.item_4_0,
//                new SettingsItemData(R.drawable.icon_mfhpartner_normal,
//                        getString(R.string.label_me_feedback), ""),
//                SettingsItem.ThemeType.THEME_IMAGE_TEXT_TEXT_ARROW,
//                SettingsItem.SeperateLineType.SEPERATE_LINE_SINGLE,
//                myOnClickListener);
    }

    /**
     * 加载数据
     * */
    public void loadData(){
        if(needLoadData){
            reloadData();
        }
    }

    public void reloadData(){
        needLoadData = true;

        try{
            if(scrollView != null){
                scrollView.smoothScrollTo(0, 0);
            }

            loadDefaultData();

            ivHeader.setAvatarUrl(MfhLoginService.get().getHeadimage());
            tvUsername.setText(MfhLoginService.get().getHumanName());

            //TODO 测试数据
            showVip(0);//new Random().nextInt(9)
            if(NetWorkUtil.isConnect(getActivity())){
//                loadingImageView.toggle(true);
                animProgress.setVisibility(View.VISIBLE);
//                emptyView.setErrorType(EmptyLayout.BIZ_LOADING);
                NetProxy.getUserProfile(responseCallback);
            }else {
                DialogUtil.showHint(R.string.toast_network_error);
            }
        }

        catch(Exception e){
            ZLogger.e(e.toString());
        }
    }

    /**
     * 加载默认数据
     * */
    private void loadDefaultData(){
        UserProfile userProfile = UserProfileHelper.getUserProfile();
        tvBalance.setText(String.format(getString(R.string.format_balance_ammount),
                userProfile.getAmount()));
        tvPoints.setText(userProfile.getScore());
        tvCollection.setText(userProfile.getFavoriteNum());
        btnPendingPayment.setBadgeNumber(Integer.valueOf(userProfile.getWaitPayNum()));
        btnPendingReceipt.setBadgeNumber(Integer.valueOf(userProfile.getWaitReceiveNum()));
        btnPendingEvaluation.setBadgeNumber(Integer.valueOf(userProfile.getWaitPraiseNum()));

        groupItems[INDEX_CART].setDetailText(getString(R.string.label_me_cart_description,
                userProfile.getShoppingCartNum()));
    }

    //回调
    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<UserProfile,
            NetProcessor.Processor<UserProfile>>(
            new NetProcessor.Processor<UserProfile>() {
                @Override
                public void processResult(IResponseData rspData) {
                    try{
                        RspBean<UserProfile> retValue = (RspBean<UserProfile>) rspData;
                        UserProfile userProfile = retValue.getValue();

                        //保存用户信息
                        UserProfileHelper.saveUserProfile(userProfile);

                        if(userProfile != null){
                            tvBalance.setText(String.format(getString(R.string.format_balance_ammount),
                                    userProfile.getAmount()));
                            tvPoints.setText(userProfile.getScore());
                            tvCollection.setText(userProfile.getFavoriteNum());
                            btnPendingPayment.setBadgeNumber(Integer.valueOf(userProfile.getWaitPayNum()));
                            btnPendingReceipt.setBadgeNumber(Integer.valueOf(userProfile.getWaitReceiveNum()));
                            btnPendingEvaluation.setBadgeNumber(Integer.valueOf(userProfile.getWaitPraiseNum()));

                            groupItems[INDEX_CART].setDetailText(getString(R.string.label_me_cart_description,
                                    userProfile.getShoppingCartNum()));
                        }
                    }catch(Exception ex){
                        ZLogger.e("parseUserProfile, " + ex.toString());
                    }finally{
//                        loadingImageView.toggle(false);
                        animProgress.setVisibility(View.GONE);
//                        emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
//                    loadingImageView.toggle(false);
                    animProgress.setVisibility(View.GONE);
//                    emptyView.setErrorType(EmptyLayout.HIDE_LAYOUT);
                }
            }
            , UserProfile.class
            , MfhApplication.getAppContext())
    {
    };

    /**
     * 显示VIP等级（目前开放 0~7）
     * */
    private void showVip(int vipLevl){
        if(vipLevl < 0 || vipLevl > 7){
            ivVip.setImageResource(vipIcons[0]);
            return;
        }

        ivVip.setImageResource(vipIcons[vipLevl]);
    }


    private View.OnClickListener myOnClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.iv_header:{
                    UIHelper.redirectToActivity(getActivity(), UserProfileActivity.class);
                }
                break;
                case R.id.ll_tab_balance:{
                    String url = MobileURLConf.generateUrl(MobileURLConf.URL_ME_WALLET, String.format("humanid=%s",
                            MfhLoginService.get().getCurrentGuId()));
//                    NativeWebViewActivity.actionStart(getActivity(), url, true, false, false);
                    redirectToJBWebForResult(url, true, -1, Constants.ACTIVITY_REQUEST_SUBDIS_SELECT);
                }
                break;
                case R.id.ll_tab_points:{
                }
                break;
                case R.id.ll_tab_collection:{
                    String url =MobileURLConf.generateUrl(MobileURLConf.URL_ME_FAVOR_COLLECTION,
                            String.format("humanid=%d", MfhLoginService.get().getCurrentGuId()));
                    ComnJBH5Activity.actionStart(getActivity(), url, true, false, -1);
                }
                break;
                //待支付
                case R.id.button_pending_payment:{
                    redirectToJBWebForResult(MobileURLConf.generateUrl(MobileURLConf.URL_ME_ORDER_MARKET,
                                    String.format("status=%s&humanid=%d", "1", MfhLoginService.get().getCurrentGuId())),
                            true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
                }
                break;
                case R.id.button_pending_receipt:{
                    redirectToJBWebForResult(MobileURLConf.generateUrl(MobileURLConf.URL_ME_ORDER_MARKET,
                                    String.format("status=%s&humanid=%d", "2", MfhLoginService.get().getCurrentGuId())),
                            true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
                }
                break;
                case R.id.button_pending_evaluation:{
                    redirectToJBWebForResult(MobileURLConf.generateUrl(MobileURLConf.URL_ME_ORDER_MARKET,
                                    String.format("status=%s&humanid=%d", "3", MfhLoginService.get().getCurrentGuId())),
                            true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
                }
                break;
                //跳转至订单
                case R.id.item_1_0:{
                    redirectToJBWebForResult(MobileURLConf.generateUrl(MobileURLConf.URL_ME_ORDER_MARKET,
                                    String.format("humanid=%d", MfhLoginService.get().getCurrentGuId())),
                            true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
                }
                break;
                //跳转至购物车
                case R.id.item_1_1:{
                    redirectToJBWebForResult(MobileURLConf.generateUrl(MobileURLConf.URL_ME_CART,
                                    String.format("humanid=%d", MfhLoginService.get().getCurrentGuId())),
                            true, -1, Constants.ACTIVITY_REQUEST_ME_CART);
                }
                break;
                //跳转至钱包
                case R.id.item_2_0:{
                    String url = MobileURLConf.generateUrl(MobileURLConf.URL_ME_WALLET,
                            String.format("humanid=%d", MfhLoginService.get().getCurrentGuId()));
//                    NativeWebViewActivity.actionStart(getActivity(), url, true, false, false);
                    redirectToJBWebForResult(url, true, -1, Constants.ACTIVITY_REQUEST_SUBDIS_SELECT);
                }
                break;
                //跳转至卡包
                case R.id.item_2_1:{
                    ComnJBH5Activity.actionStart(getActivity(),
                            MobileURLConf.generateUrl(MobileURLConf.URL_ME_CARDPACK,
                                    String.format("humanid=%d", MfhLoginService.get().getCurrentGuId())), true, false, -1);
                }
                break;
                //跳转至包裹
                case R.id.item_2_2:{
                    ComnJBH5Activity.actionStart(getActivity(),
                            MobileURLConf.generateUrl(MobileURLConf.URL_ME_PARCEL,
                                    String.format("humanid=%d", MfhLoginService.get().getCurrentGuId())), true, false, -1);
                }
                break;
//                //跳转至任务
//                case R.id.item_2_3:{
//                    DialogUtil.showHint("暂未开放");
////                    NativeWebViewActivity.actionStart(getActivity(),
////                            UIHelper.generateUrl(MobileURLConf.URL_ME_PARCEL, String.format("humanid=%s",
////                                    SharedPreferencesHelper.getUserGuid())), true, true, false);
//                }
//                break;
//                //满分小伙伴
//                case R.id.item_3_0:{
//                    String url = MobileURLConf.generateUrl(MobileURLConf.URL_ME_MFHPARTER,
//                            String.format("humanid=%s", SharedPreferencesHelper.getUserGuid()));
//
//                    redirectToJBWebForResult(url, true, 0, OwnerConstants.ACTIVITY_REQUEST_SUBDIS_SELECT);
////
////                    UIHelper.redirectToActivity(getActivity(), H5CategoryActivity.class);
//                }
//                break;
//                //反馈
//                case R.id.item_4_0:{
//                    String url = MobileURLConf.generateUrl(MobileURLConf.URL_FEEDBACK,
//                            String.format("humanid=%s&channelid=%s",
//                                    SharedPreferencesHelper.getUserGuid(), URLConf.PARAM_VALUE_CHANNEL_ID_DEF));
//                    redirectToJBWebForResult(url,
//                            true, 0, OwnerConstants.ACTIVITY_REQUEST_SUBDIS_SELECT);
//                }
//                break;
//                //选择常住小区
//                case R.id.item_3_1:{
//                    redirectToNativeWebForResult(UIHelper.generateUrl(MobileURLConf.URL_ME_SUBDIS,
//                                    String.format("humanid=%s", SharedPreferencesHelper.getUserGuid())),
//                            true, OwnerConstants.ACTIVITY_REQUEST_SUBDIS_SELECT);
//                }
//                break;
//                case R.id.item_4_0:{
////                    redirectToNativeWebForResult(UIHelper.generateUrl(MobileURLConf.URL_ME_RECEIVESTOCK,
//// String.format("humanid=%s", loginService.getCurrentGuId())),
////                            true, OwnerConstants.ACTIVITY_REQUEST_RECEIVE_STOCK);
//                }
//                break;
            }
        }
    };

    /**
     * 跳转至设置页面
     * */
     @OnClick(R.id.button_settings)
     public void redirectToSettings(){
        //跳转至设置页面
        Intent intent = new Intent(getActivity(), SettingsActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
        //Nat@20150424 修复在设置页面点击退出账号进入登录页面，点击系统返回按键仍然显示之前的页面问题。
        startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_SETTINGS);
    }

    /**
     * 注意：不能使用getActivity()启动startActivityForResult，
     * 直接在fragment里面调用startActivityForResult，否则收不到返回的结果
     * */
    private void redirectToNativeWebForResult(String url, boolean bNeedSyncCookie, int requestCode){
        Intent intent = new Intent(getActivity(), NativeWebViewActivity.class);
        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_REDIRECT_URL, url);
        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_SYNC_COOKIE, bNeedSyncCookie);
        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_BACKASHOMEUP, false);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 注意：不能使用getActivity()启动startActivityForResult，
     * 直接在fragment里面调用startActivityForResult，否则收不到返回的结果
     * */
    private void redirectToJBWebForResult(String url, boolean bNeedSyncCookie, int animType, int requestCode){
        Intent intent = new Intent(getActivity(), ComnJBH5Activity.class);
        intent.putExtra(ComnJBH5Activity.EXTRA_KEY_REDIRECT_URL, url);
        intent.putExtra(ComnJBH5Activity.EXTRA_KEY_SYNC_COOKIE, bNeedSyncCookie);
        intent.putExtra(ComnJBH5Activity.EXTRA_KEY_BACKASHOMEUP, false);
        intent.putExtra(ComnJBH5Activity.EXTRA_KEY_ANIM_TYPE, animType);
        startActivityForResult(intent, requestCode);
    }

}
