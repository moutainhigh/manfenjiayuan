package com.mfh.enjoycity.ui.fragments;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bingshanguxue.vector_uikit.SettingsItem;
import com.bingshanguxue.vector_uikit.widget.MultiLayerLabel;
import com.manfenjiayuan.business.ui.HybridActivity;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.enjoycity.R;
import com.mfh.enjoycity.bean.UserProfile;
import com.mfh.enjoycity.ui.SettingsActivity;
import com.mfh.enjoycity.ui.activity.NativeWebViewActivity;
import com.mfh.enjoycity.ui.activity.ShoppingCartActivity;
import com.mfh.enjoycity.ui.settings.UserProfileActivity;
import com.mfh.enjoycity.utils.Constants;
import com.mfh.enjoycity.utils.UserProfileHelper;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.H5Api;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.api.account.UserApiImpl;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.NetworkUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.network.NetCallBack;
import com.mfh.framework.network.NetProcessor;
import com.mfh.framework.network.URLHelper;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.compound.BadgeViewButton;
import com.mfh.framework.uikit.widget.AvatarView;
import com.mfh.framework.uikit.widget.EmptyLayout;
import com.mfh.framework.uikit.widget.LoadingImageView;
import com.mfh.framework.uikit.widget.ReboundScrollView;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 我·
 *
 * @author Nat.ZZN(bingshanguxue) created on 2015-04-13
 * @since bingshanguxue
 */
public class IndividualFragment extends BaseFragment {
    @Bind(R.id.reboundScrollView)
    ReboundScrollView scrollView;
    @Bind(R.id.iv_header)
    AvatarView ivHeader;
    @Bind(R.id.tv_username)
    TextView tvUsername;
    @Bind(R.id.iv_vip)
    ImageView ivVip;

    @Bind({R.id.label_balance, R.id.label_points, R.id.label_collection})
    List<MultiLayerLabel> llItems1;
    @Bind(R.id.button_pending_payment)
    BadgeViewButton btnPendingPayment;
    @Bind(R.id.button_pending_receipt)
    BadgeViewButton btnPendingReceipt;
    @Bind(R.id.button_pending_evaluation)
    BadgeViewButton btnPendingEvaluation;
    @Bind(R.id.item_1_1)
    SettingsItem shopcartSettingsItem;


    @Bind(R.id.animProgress)
    ProgressBar animProgress;
    @Bind(R.id.loadingImageView)
    LoadingImageView loadingImageView;
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
    protected int getLayoutResId() {
        return R.layout.fragment_individual;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        initViews();

        //TODO,监听网络变化，提示刷新 OR 自动刷新
        if (!MfhLoginService.get().haveLogined()) {
            //TODO，在当前页打开
            UIHelper.sendBroadcast(getActivity(), com.mfh.enjoycity.utils.UIHelper.ACTION_REDIRECT_TO_LOGIN_H5);

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

        switch (requestCode) {
//            case OwnerConstants.ACTIVITY_REQUEST_CODE_SETTINGS:
//                if(resultCode == Activity.RESULT_OK){
//                    if(data.getBooleanExtra(OwnerConstants.INTENT_KEY_IS_LOGOUT, false)){
//                        needLoadData = false;
//                        UIHelper.sendLoginBroadcast(getActivity());
//
//                        //在设置页面点击退出账号后需要将之前打开的页面关掉。
////                        getActivity().finish();
//                        break;
//                    }else{
//
//                    }
//                }
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

        //重复请求
        if (MfhLoginService.get().haveLogined()) {
            needLoadData = true;
        }
        loadData();
    }

    /**
     * initialize views
     */
    private void initViews() {
        scrollView.setSmoothScrollingEnabled(true);

        ivHeader.setBorderWidth(3);
        ivHeader.setBorderColor(Color.parseColor("#e8e8e8"));

//        loadingImageView.setBackgroundResource(com.mfh.comna.R.drawable.loading_anim);
        emptyView.setLoadingTheme(1);
    }

    /**
     * 加载数据
     */
    public void loadData() {
        if (needLoadData) {
            reloadData();
        }
    }

    public void reloadData() {
        needLoadData = true;

        try {
            if (scrollView != null) {
                scrollView.smoothScrollTo(0, 0);
            }

            loadDefaultData(UserProfileHelper.getUserProfile());

            String headerUrl = MfhLoginService.get().getHeadimage();
//            MLog.d("headerUrl = " + headerUrl);
            ivHeader.setAvatarUrl(headerUrl);
            tvUsername.setText(MfhLoginService.get().getHumanName());

            //TODO 测试数据
            showVip(0);//new Random().nextInt(9)
            if (NetworkUtils.isConnect(getActivity())) {
//                loadingImageView.toggle(true);
                animProgress.setVisibility(View.VISIBLE);
//                emptyView.setErrorType(EmptyLayout.BIZ_LOADING);
                UserApiImpl.getMyProfile(responseCallback);
            } else {
                DialogUtil.showHint(R.string.toast_network_error);
            }
        } catch (Exception e) {
            ZLogger.e(e.toString());
        }
    }

    /**
     * 加载用户数据
     */
    private void loadDefaultData(UserProfile userProfile) {
        if (userProfile == null) {
            return;
        }
        llItems1.get(0).setTopText(String.format(getString(R.string.format_balance_ammount),
                userProfile.getAmount()));
        llItems1.get(1).setTopText(userProfile.getScore());
        llItems1.get(2).setTopText(userProfile.getFavoriteNum());
        btnPendingPayment.setBadgeNumber(Integer.valueOf(userProfile.getWaitPayNum()));
        btnPendingReceipt.setBadgeNumber(Integer.valueOf(userProfile.getWaitReceiveNum()));
        btnPendingEvaluation.setBadgeNumber(Integer.valueOf(userProfile.getWaitPraiseNum()));

        shopcartSettingsItem.setSubTitle(getString(R.string.label_me_cart_description,
                userProfile.getShoppingCartNum()));
    }

    //回调
    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<UserProfile,
            NetProcessor.Processor<UserProfile>>(
            new NetProcessor.Processor<UserProfile>() {
                @Override
                public void processResult(IResponseData rspData) {
                    try {
                        RspBean<UserProfile> retValue = (RspBean<UserProfile>) rspData;
                        UserProfile userProfile = retValue.getValue();

                        //保存用户信息
                        UserProfileHelper.saveUserProfile(userProfile);

                        loadDefaultData(userProfile);
                    } catch (Exception ex) {
                        ZLogger.e("parseUserProfile, " + ex.toString());
                    } finally {
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
            , MfhApplication.getAppContext()) {
    };

    /**
     * 显示VIP等级（目前开放 0~7）
     */
    private void showVip(int vipLevl) {
        if (vipLevl < 0 || vipLevl > 7) {
            ivVip.setImageResource(vipIcons[0]);
            return;
        }

        ivVip.setImageResource(vipIcons[vipLevl]);
    }

    @OnClick(R.id.iv_header)
    public void redirectToUserProfile() {
        UIHelper.startActivity(getActivity(), UserProfileActivity.class);
    }

    @OnClick(R.id.label_balance)
    public void showBalance() {
        String url = URLHelper.append(H5Api.URL_ME_WALLET,
                String.format("humanid=%d",
                        MfhLoginService.get().getCurrentGuId()));
//                    NativeWebViewActivity.actionStart(getActivity(), url, true, false, false);
        redirectToJBWebForResult(url, true, -1, Constants.ACTIVITY_REQUEST_SUBDIS_SELECT);
    }

    @OnClick(R.id.label_collection)
    public void showCollection() {
        String url = URLHelper.append(H5Api.URL_ME_FAVOR_COLLECTION,
                String.format("humanid=%d", MfhLoginService.get().getCurrentGuId()));
        HybridActivity.actionStart(getActivity(), url, true, false, -1);
    }

    //待支付
    @OnClick(R.id.button_pending_payment)
    public void showPendingPayment() {
        redirectToJBWebForResult(URLHelper.append(H5Api.URL_ME_ORDER_MALL,
                String.format("status=%s&humanid=%d", "1",
                        MfhLoginService.get().getCurrentGuId())),
                true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
    }

    @OnClick(R.id.button_pending_receipt)
    public void showPendingReceipt() {
        redirectToJBWebForResult(URLHelper.append(H5Api.URL_ME_ORDER_MALL,
                String.format("status=%s&humanid=%d", "2", MfhLoginService.get().getCurrentGuId())),
                true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
    }

    @OnClick(R.id.button_pending_evaluation)
    public void showPendingEvaluation() {
        redirectToJBWebForResult(URLHelper.append(H5Api.URL_ME_ORDER_MALL,
                String.format("status=%s&humanid=%d", "3", MfhLoginService.get().getCurrentGuId())),
                true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
    }

    //跳转至订单
    @OnClick(R.id.item_1_0)
    public void showOrder() {
        redirectToJBWebForResult(URLHelper.append(H5Api.URL_ME_ORDER_MALL,
                String.format("humanid=%d", MfhLoginService.get().getCurrentGuId())),
                true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
    }//跳转至购物车

    @OnClick(R.id.item_1_1)
    public void showShopcart() {
//                    redirectToJBWebForResult(MobileURLConf.generateUrl(MobileURLConf.URL_ME_CART,
//                                    String.format("humanid=%s", SharedPreferencesHelper.getUserGuid())),
//                            true, -1, Constants.ACTIVITY_REQUEST_ME_CART);

        ShoppingCartActivity.actionStart(getContext(), 0);
    }//跳转至钱包

    @OnClick(R.id.item_2_0)
    public void showWallet() {
        String url = URLHelper.append(H5Api.URL_ME_WALLET,
                String.format("humanid=%d",
                        MfhLoginService.get().getCurrentGuId()));
//                    NativeWebViewActivity.actionStart(getActivity(), url, true, false, false);
        redirectToJBWebForResult(url, true, -1, Constants.ACTIVITY_REQUEST_SUBDIS_SELECT);
    }//跳转至卡包

    @OnClick(R.id.item_2_1)
    public void showCardpack() {
        HybridActivity.actionStart(getActivity(),
                URLHelper.append(H5Api.URL_ME_CARDPACK,
                        String.format("humanid=%d",
                                MfhLoginService.get().getCurrentGuId())), true, false, -1);
    }//跳转至包裹

    @OnClick(R.id.item_2_2)
    public void showParcel() {
        HybridActivity.actionStart(getActivity(),
                URLHelper.append(H5Api.URL_ME_PARCEL,
                        String.format("humanid=%d",
                                MfhLoginService.get().getCurrentGuId())), true, false, -1);
    }//满分小伙伴

    @OnClick(R.id.item_3_0)
    public void showMfParter() {
        String url = URLHelper.append(H5Api.URL_ME_MFHPARTER,
                String.format("humanid=%d", MfhLoginService.get().getCurrentGuId()));

        redirectToJBWebForResult(url, true, 0, Constants.ACTIVITY_REQUEST_SUBDIS_SELECT);
//
//                    UIHelper.redirectToActivity(getActivity(), H5CategoryActivity.class);
    }//反馈

    @OnClick(R.id.item_4_0)
    public void showFeedback() {
        String url = URLHelper.append(H5Api.URL_FEEDBACK,
                String.format("humanid=%d&channelid=%s",
                        MfhLoginService.get().getCurrentGuId(),
                        MfhApi.CHANNEL_ID));
        redirectToJBWebForResult(url,
                true, 0, Constants.ACTIVITY_REQUEST_SUBDIS_SELECT);
    }

    /**
     * 跳转至设置页面
     */
    @OnClick(R.id.button_settings)
    public void redirectToSettings() {
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
     */
    private void redirectToNativeWebForResult(String url, boolean bNeedSyncCookie, int requestCode) {
        Intent intent = new Intent(getActivity(), NativeWebViewActivity.class);
        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_REDIRECT_URL, url);
        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_SYNC_COOKIE, bNeedSyncCookie);
        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_BACKASHOMEUP, false);
        startActivityForResult(intent, requestCode);
    }

    /**
     * 注意：不能使用getActivity()启动startActivityForResult，
     * 直接在fragment里面调用startActivityForResult，否则收不到返回的结果
     */
    private void redirectToJBWebForResult(String url, boolean bNeedSyncCookie, int animType, int requestCode) {
        Intent intent = new Intent(getActivity(), HybridActivity.class);
        intent.putExtra(HybridActivity.EXTRA_KEY_REDIRECT_URL, url);
        intent.putExtra(HybridActivity.EXTRA_KEY_SYNC_COOKIE, bNeedSyncCookie);
        intent.putExtra(HybridActivity.EXTRA_KEY_BACKASHOMEUP, false);
        intent.putExtra(HybridActivity.EXTRA_KEY_ANIM_TYPE, animType);
        startActivityForResult(intent, requestCode);
    }

}
