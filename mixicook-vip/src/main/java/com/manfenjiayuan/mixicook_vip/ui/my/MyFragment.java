package com.manfenjiayuan.mixicook_vip.ui.my;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.bingshanguxue.vector_uikit.ProfileView;
import com.bingshanguxue.vector_user.UserApiImpl;
import com.bingshanguxue.vector_user.bean.MyProfile;
import com.manfenjiayuan.business.ui.HybridActivity;
import com.manfenjiayuan.business.utils.MUtils;
import com.manfenjiayuan.mixicook_vip.AppContext;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.SimpleActivity;
import com.manfenjiayuan.mixicook_vip.utils.ACacheHelper;
import com.mfh.comn.net.data.IResponseData;
import com.mfh.comn.net.data.RspBean;
import com.mfh.framework.MfhApplication;
import com.mfh.framework.api.H5Api;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.core.utils.DialogUtil;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.net.NetCallBack;
import com.mfh.framework.net.NetProcessor;
import com.mfh.framework.net.URLHelper;
import com.mfh.framework.network.NetWorkUtil;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.compound.MultiLayerLabel;
import com.mfh.framework.uikit.compound.SettingsItem;
import com.mfh.framework.uikit.dialog.ProgressDialog;
import com.mfh.framework.uikit.widget.OnTabReselectListener;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * 我·
 *
 * @author Nat.ZZN(bingshanguxue) created on 2015-04-13
 * @since bingshanguxue
 */
public class MyFragment extends BaseFragment implements OnTabReselectListener {
    @Bind(R.id.app_bar)
    AppBarLayout mAppBarLayout;
    @Bind(R.id.collaps_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @Bind(R.id.profileView)
    ProfileView mProfileView;
    @Bind(R.id.item_balance)
    MultiLayerLabel itemBalance;
    @Bind(R.id.item_redpacket)
    MultiLayerLabel itemRedPacket;
    @Bind(R.id.item_score)
    MultiLayerLabel itemScore;
    @Bind(R.id.item_order)
    SettingsItem itemOrder;
    @Bind(R.id.item_topup)
    SettingsItem itemTopup;
    @Bind(R.id.item_card)
    SettingsItem itemCard;
    @Bind(R.id.item_store)
    SettingsItem itemStore;
    @Bind(R.id.item_feedback)
    SettingsItem itemFeedback;
    @Bind(R.id.item_parters)
    SettingsItem itemParters;


    private CollapsingToolbarLayoutState state;

    private enum CollapsingToolbarLayoutState {
        EXPANDED,
        COLLAPSED,
        INTERNEDIATE
    }

    public MyFragment() {
        super();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_my;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (verticalOffset == 0) {
                    if (state != CollapsingToolbarLayoutState.EXPANDED) {
                        state = CollapsingToolbarLayoutState.EXPANDED;//修改状态标记为展开
                        mCollapsingToolbarLayout.setTitle("EXPANDED");//设置title为EXPANDED
                    }
                } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
                    if (state != CollapsingToolbarLayoutState.COLLAPSED) {
                        mCollapsingToolbarLayout.setTitle("");//设置title不显示
//                        playButton.setVisibility(View.VISIBLE);//隐藏播放按钮
                        state = CollapsingToolbarLayoutState.COLLAPSED;//修改状态标记为折叠
                    }
                } else {
                    if (state != CollapsingToolbarLayoutState.INTERNEDIATE) {
                        if(state == CollapsingToolbarLayoutState.COLLAPSED){
//                            playButton.setVisibility(View.GONE);//由折叠变为中间状态时隐藏播放按钮
                        }
                        mCollapsingToolbarLayout.setTitle("INTERNEDIATE");//设置title为INTERNEDIATE
                        state = CollapsingToolbarLayoutState.INTERNEDIATE;//修改状态标记为中间
                    }
                }
            }
        });

        refresh(true);
        DialogUtil.showHint("我的");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ZLogger.d(String.format("MyFragment.onActivityResult.requestCode=%d, resultCode=%d", requestCode, resultCode));

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
//            case Constants.ACTIVITY_REQUEST_ME_ORDER:
//            case Constants.ACTIVITY_REQUEST_ME_PACKAGE:
//            case Constants.ACTIVITY_REQUEST_ME_CART:
//            case Constants.ACTIVITY_REQUEST_RECEIVE_STOCK:
//            case Constants.ACTIVITY_REQUEST_SUBDIS_SELECT:
//                loadData();//刷新数据
//                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @OnClick(R.id.iv_settings)
    public void redirectToSettings(){
        DialogUtil.showHint("设置");
        UIHelper.startActivity(getActivity(), GeneralSettingActivity.class);
    }

    @OnClick(R.id.profileView)
    public void redirectToProfile(){

        Bundle extras = new Bundle();
        extras.putString(SimpleActivity.EXTRA_TITLE, "个人资料");
        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(SimpleActivity.EXTRA_KEY_FRAGMENT_TYPE, SimpleActivity.FT_MYPROFILE);
        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    @OnClick(R.id.item_balance)
    public void showBalance() {
        String url = URLHelper.append(H5Api.URL_ME_WALLET,
                String.format("humanid=%d",
                        MfhLoginService.get().getCurrentGuId()));
//                    NativeWebViewActivity.actionStart(getActivity(), url, true, false, false);
        redirectToJBWebForResult(url, true, -1);
    }

    /**
     * 注意：不能使用getActivity()启动startActivityForResult，
     * 直接在fragment里面调用startActivityForResult，否则收不到返回的结果
     */
    private void redirectToJBWebForResult(String url, boolean bNeedSyncCookie, int animType) {
        Intent intent = new Intent(getActivity(), HybridActivity.class);
        intent.putExtra(HybridActivity.EXTRA_KEY_REDIRECT_URL, url);
        intent.putExtra(HybridActivity.EXTRA_KEY_SYNC_COOKIE, bNeedSyncCookie);
        intent.putExtra(HybridActivity.EXTRA_KEY_BACKASHOMEUP, false);
        intent.putExtra(HybridActivity.EXTRA_KEY_ANIM_TYPE, animType);
        startActivity(intent);
    }

    /**
     * 加载用户数据
     */
    private void refresh(boolean isAutoReload) {
        mProfileView.setAvatarUrl(MfhLoginService.get().getHeadimage());
        mProfileView.setPrimaryText(MfhLoginService.get().getHumanName());
        mProfileView.setSecondaryText(MfhLoginService.get().getTelephone());

        MyProfile myProfile = null;

        String profileCache = ACacheHelper.getAsString(ACacheHelper.CK_MYPROFILE);
        if (!StringUtils.isEmpty(profileCache)) {
            myProfile = JSONObject.toJavaObject(JSONObject.parseObject(profileCache),
                    MyProfile.class);
        }

        if (myProfile != null) {
            itemBalance.setTopText(MUtils.formatDouble(myProfile.getAmount(), ""));
            itemScore.setTopText(String.valueOf(myProfile.getScore()));
            itemRedPacket.setTopText(String.valueOf(myProfile.getCardCouponsNum()));
        } else {
            itemBalance.setTopText("");
            itemScore.setTopText("");
            itemRedPacket.setTopText("");
        }

        if (isAutoReload && NetWorkUtil.isConnect(AppContext.getAppContext())) {
            showProgressDialog(ProgressDialog.STATUS_PROCESSING, "加载中...", true);
            UserApiImpl.getMyProfile(responseCallback);
        }
    }

    //回调
    private NetCallBack.NetTaskCallBack responseCallback = new NetCallBack.NetTaskCallBack<MyProfile,
            NetProcessor.Processor<MyProfile>>(
            new NetProcessor.Processor<MyProfile>() {
                @Override
                public void processResult(IResponseData rspData) {
                    try {
                        RspBean<MyProfile> retValue = (RspBean<MyProfile>) rspData;
                        MyProfile myProfile = retValue.getValue();
//                        ZLogger.d(String.format("%d,%s", myProfile.getHumanId(), myProfile.toString()));
//                        ZLogger.d(JSONObject.toJSONString(myProfile));
//
//                        String temp = "{\"serviceOrderNum\":0,\"amount\":2852.73,\"score\":171,\"favoriteNum\":0,\"cardCouponsNum\":4,\"waitReceiveNum\":0,\"shoppingCartNum\":0,\"waitPraiseNum\":0,\"waitPayNum\":0,\"humanId\":136060}";
//                        MyProfile temp2 = JSONObject.toJavaObject(JSON.parseObject(temp), MyProfile.class);
//                        ZLogger.d(temp);
//                        ZLogger.d(JSONObject.toJSONString(temp2));

                        //保存用户信息
                        ACacheHelper.put(ACacheHelper.CK_MYPROFILE,
                                JSONObject.toJSONString(myProfile));

                        refresh(false);
                    } catch (Exception ex) {
                        ZLogger.e("parseUserProfile, " + ex.toString());
                    } finally {
                        hideProgressDialog();
                    }
                }

                @Override
                protected void processFailure(Throwable t, String errMsg) {
                    super.processFailure(t, errMsg);
                    hideProgressDialog();
                }
            }
            , MyProfile.class
            , MfhApplication.getAppContext()) {
    };

    @Override
    public void onTabReselect() {
        refresh(true);
    }

//    /**
//     * 显示VIP等级（目前开放 0~7）
//     */
//    private void showVip(int vipLevl) {
//        if (vipLevl < 0 || vipLevl > 7) {
//            ivVip.setImageResource(vipIcons[0]);
//            return;
//        }
//
//        ivVip.setImageResource(vipIcons[vipLevl]);
//    }
//
//    @OnClick(R.id.iv_header)
//    public void redirectToUserProfile() {
//        UIHelper.startActivity(getActivity(), UserProfileActivity.class);
//    }
//
//    @OnClick(R.id.label_balance)
//    public void showBalance() {
//        String url = URLHelper.append(H5Api.URL_ME_WALLET,
//                String.format("humanid=%d",
//                        MfhLoginService.get().getCurrentGuId()));
////                    NativeWebViewActivity.actionStart(getActivity(), url, true, false, false);
//        redirectToJBWebForResult(url, true, -1, Constants.ACTIVITY_REQUEST_SUBDIS_SELECT);
//    }
//
//    @OnClick(R.id.label_collection)
//    public void showCollection() {
//        String url = URLHelper.append(H5Api.URL_ME_FAVOR_COLLECTION,
//                String.format("humanid=%d", MfhLoginService.get().getCurrentGuId()));
//        HybridActivity.actionStart(getActivity(), url, true, false, -1);
//    }
//
//    //待支付
//    @OnClick(R.id.button_pending_payment)
//    public void showPendingPayment() {
//        redirectToJBWebForResult(URLHelper.append(H5Api.URL_ME_ORDER_MALL,
//                String.format("status=%s&humanid=%d", "1",
//                        MfhLoginService.get().getCurrentGuId())),
//                true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
//    }
//
//    @OnClick(R.id.button_pending_receipt)
//    public void showPendingReceipt() {
//        redirectToJBWebForResult(URLHelper.append(H5Api.URL_ME_ORDER_MALL,
//                String.format("status=%s&humanid=%d", "2", MfhLoginService.get().getCurrentGuId())),
//                true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
//    }
//
//    @OnClick(R.id.button_pending_evaluation)
//    public void showPendingEvaluation() {
//        redirectToJBWebForResult(URLHelper.append(H5Api.URL_ME_ORDER_MALL,
//                String.format("status=%s&humanid=%d", "3", MfhLoginService.get().getCurrentGuId())),
//                true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
//    }
//
//    //跳转至订单
//    @OnClick(R.id.item_1_0)
//    public void showOrder() {
//        redirectToJBWebForResult(URLHelper.append(H5Api.URL_ME_ORDER_MALL,
//                String.format("humanid=%d", MfhLoginService.get().getCurrentGuId())),
//                true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
//    }//跳转至购物车
//
//    @OnClick(R.id.item_1_1)
//    public void showShopcart() {
////                    redirectToJBWebForResult(MobileURLConf.generateUrl(MobileURLConf.URL_ME_CART,
////                                    String.format("humanid=%s", SharedPreferencesHelper.getUserGuid())),
////                            true, -1, Constants.ACTIVITY_REQUEST_ME_CART);
//
//        ShoppingCartActivity.actionStart(getContext(), 0);
//    }//跳转至钱包
//
//    @OnClick(R.id.item_2_0)
//    public void showWallet() {
//        String url = URLHelper.append(H5Api.URL_ME_WALLET,
//                String.format("humanid=%d",
//                        MfhLoginService.get().getCurrentGuId()));
////                    NativeWebViewActivity.actionStart(getActivity(), url, true, false, false);
//        redirectToJBWebForResult(url, true, -1, Constants.ACTIVITY_REQUEST_SUBDIS_SELECT);
//    }//跳转至卡包
//
//    @OnClick(R.id.item_2_1)
//    public void showCardpack() {
//        HybridActivity.actionStart(getActivity(),
//                URLHelper.append(H5Api.URL_ME_CARDPACK,
//                        String.format("humanid=%d",
//                                MfhLoginService.get().getCurrentGuId())), true, false, -1);
//    }//跳转至包裹
//
//    @OnClick(R.id.item_2_2)
//    public void showParcel() {
//        HybridActivity.actionStart(getActivity(),
//                URLHelper.append(H5Api.URL_ME_PARCEL,
//                        String.format("humanid=%d",
//                                MfhLoginService.get().getCurrentGuId())), true, false, -1);
//    }//满分小伙伴
//
//    @OnClick(R.id.item_3_0)
//    public void showMfParter() {
//        String url = URLHelper.append(H5Api.URL_ME_MFHPARTER,
//                String.format("humanid=%d", MfhLoginService.get().getCurrentGuId()));
//
//        redirectToJBWebForResult(url, true, 0, Constants.ACTIVITY_REQUEST_SUBDIS_SELECT);
////
////                    UIHelper.redirectToActivity(getActivity(), H5CategoryActivity.class);
//    }//反馈
//
//    @OnClick(R.id.item_4_0)
//    public void showFeedback() {
//        String url = URLHelper.append(H5Api.URL_FEEDBACK,
//                String.format("humanid=%d&channelid=%s",
//                        MfhLoginService.get().getCurrentGuId(),
//                        MfhApi.CHANNEL_ID));
//        redirectToJBWebForResult(url,
//                true, 0, Constants.ACTIVITY_REQUEST_SUBDIS_SELECT);
//    }
//
//
//    /**
//     * 注意：不能使用getActivity()启动startActivityForResult，
//     * 直接在fragment里面调用startActivityForResult，否则收不到返回的结果
//     */
//    private void redirectToNativeWebForResult(String url, boolean bNeedSyncCookie, int requestCode) {
//        Intent intent = new Intent(getActivity(), NativeWebViewActivity.class);
//        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_REDIRECT_URL, url);
//        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_SYNC_COOKIE, bNeedSyncCookie);
//        intent.putExtra(NativeWebViewActivity.EXTRA_KEY_BACKASHOMEUP, false);
//        startActivityForResult(intent, requestCode);
//    }
//
//    /**
//     * 注意：不能使用getActivity()启动startActivityForResult，
//     * 直接在fragment里面调用startActivityForResult，否则收不到返回的结果
//     */
//    private void redirectToJBWebForResult(String url, boolean bNeedSyncCookie, int animType, int requestCode) {
//        Intent intent = new Intent(getActivity(), HybridActivity.class);
//        intent.putExtra(HybridActivity.EXTRA_KEY_REDIRECT_URL, url);
//        intent.putExtra(HybridActivity.EXTRA_KEY_SYNC_COOKIE, bNeedSyncCookie);
//        intent.putExtra(HybridActivity.EXTRA_KEY_BACKASHOMEUP, false);
//        intent.putExtra(HybridActivity.EXTRA_KEY_ANIM_TYPE, animType);
//        startActivityForResult(intent, requestCode);
//    }

}
