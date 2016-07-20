package com.manfenjiayuan.loveshopping.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import com.manfenjiayuan.loveshopping.R;
import com.manfenjiayuan.loveshopping.activity.HybridActivity;
import com.manfenjiayuan.loveshopping.widget.UserProfileView;
import com.mfh.framework.core.logger.ZLogger;
import com.mfh.framework.login.logic.MfhLoginService;
import com.mfh.framework.api.H5Api;
import com.mfh.framework.api.MfhApi;
import com.mfh.framework.net.URLHelper;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.widget.OnTabReselectListener;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainMineFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainMineFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMineFragment extends BaseFragment implements
        OnTabReselectListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    @Bind(R.id.userProfileView)
    UserProfileView mUserProfileView;

    public MainMineFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainMineFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainMineFragment newInstance(String param1, String param2) {
        MainMineFragment fragment = new MainMineFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_mine;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onTabReselect() {

        ZLogger.d("MainMineFragment.onTabReselect");
    }

    @OnClick(R.id.userProfileView)
    public void redirectToUserProfile() {
//        UIHelper.startActivity(getActivity(), UserProfileActivity.class);
    }

    @OnClick(R.id.label_balance)
    public void showBalance() {
        String url = URLHelper.append(H5Api.URL_ME_WALLET,
                String.format("humanid=%d",
                        MfhLoginService.get().getCurrentGuId()));
//                    NativeWebViewActivity.actionStart(getActivity(), url, true, false, false);
//        redirectToJBWebForResult(url, true, -1, Constants.ACTIVITY_REQUEST_SUBDIS_SELECT);
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
//        redirectToJBWebForResult(URLHelper.append(H5Api.URL_ME_ORDER_MALL,
//                String.format("status=%s&humanid=%d", "1",
//                        MfhLoginService.get().getCurrentGuId())),
//                true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
    }

    @OnClick(R.id.button_pending_receipt)
    public void showPendingReceipt() {
//        redirectToJBWebForResult(URLHelper.append(H5Api.URL_ME_ORDER_MALL,
//                String.format("status=%s&humanid=%d", "2", MfhLoginService.get().getCurrentGuId())),
//                true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
    }

    @OnClick(R.id.button_pending_evaluation)
    public void showPendingEvaluation() {
//        redirectToJBWebForResult(URLHelper.append(H5Api.URL_ME_ORDER_MALL,
//                String.format("status=%s&humanid=%d", "3", MfhLoginService.get().getCurrentGuId())),
//                true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
    }

    //跳转至订单
    @OnClick(R.id.item_1_0)
    public void showOrder() {
//        redirectToJBWebForResult(URLHelper.append(H5Api.URL_ME_ORDER_MALL,
//                String.format("humanid=%d", MfhLoginService.get().getCurrentGuId())),
//                true, -1, Constants.ACTIVITY_REQUEST_ME_ORDER);
    }//跳转至购物车

    @OnClick(R.id.item_1_1)
    public void showShopcart() {
//                    redirectToJBWebForResult(MobileURLConf.generateUrl(MobileURLConf.URL_ME_CART,
//                                    String.format("humanid=%s", SharedPreferencesHelper.getUserGuid())),
//                            true, -1, Constants.ACTIVITY_REQUEST_ME_CART);

//        ShoppingCartActivity.actionStart(getContext(), 0);
    }//跳转至钱包

    @OnClick(R.id.item_2_0)
    public void showWallet() {
        String url = URLHelper.append(H5Api.URL_ME_WALLET,
                String.format("humanid=%d",
                        MfhLoginService.get().getCurrentGuId()));
//                    NativeWebViewActivity.actionStart(getActivity(), url, true, false, false);
//        redirectToJBWebForResult(url, true, -1, Constants.ACTIVITY_REQUEST_SUBDIS_SELECT);
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

//        redirectToJBWebForResult(url, true, 0, Constants.ACTIVITY_REQUEST_SUBDIS_SELECT);
//
//                    UIHelper.redirectToActivity(getActivity(), H5CategoryActivity.class);
    }//反馈

    @OnClick(R.id.item_4_0)
    public void showFeedback() {
        String url = URLHelper.append(H5Api.URL_FEEDBACK,
                String.format("humanid=%d&channelid=%s",
                        MfhLoginService.get().getCurrentGuId(),
                        MfhApi.CHANNEL_ID));
//        redirectToJBWebForResult(url,
//                true, 0, Constants.ACTIVITY_REQUEST_SUBDIS_SELECT);
    }

    /**
     * 跳转至设置页面
     */
    @OnClick(R.id.item_settings)
    public void redirectToSettings() {
        //跳转至设置页面
//        Intent intent = new Intent(getActivity(), SettingsActivity.class);
////        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////        startActivity(intent);
//        //Nat@20150424 修复在设置页面点击退出账号进入登录页面，点击系统返回按键仍然显示之前的页面问题。
//        startActivityForResult(intent, Constants.ACTIVITY_REQUEST_CODE_SETTINGS);
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
