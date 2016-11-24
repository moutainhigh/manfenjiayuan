package com.manfenjiayuan.mixicook_vip.ui.my;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.bingshanguxue.vector_uikit.SettingsItem;
import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.ARCode;
import com.manfenjiayuan.mixicook_vip.ui.FragmentActivity;
import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.api.mobile.Mixicook;
import com.mfh.framework.core.utils.StringUtils;
import com.mfh.framework.uikit.UIHelper;
import com.mfh.framework.uikit.base.BaseFragment;
import com.mfh.framework.uikit.dialog.CommonDialog;

import butterknife.BindView;
import butterknife.OnClick;

import static com.manfenjiayuan.mixicook_vip.ui.InputTextFragment.EXTRA_KEY_HINT_TEXT;
import static com.manfenjiayuan.mixicook_vip.ui.InputTextFragment.EXTRA_KEY_TITLE;


/**
 * 客服中心
 *
 * @author Nat.ZZN(bingshanguxue) created on 2015-04-13
 * @since bingshanguxue
 */
public class CustomerServiceFragment extends BaseFragment{
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.item_call)
    SettingsItem itemCall;
    @BindView(R.id.item_feedback)
    SettingsItem itemFeedback;


    public CustomerServiceFragment() {
        super();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_my_customerservice;
    }

    @Override
    protected void createViewInner(View rootView, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args != null) {
            animType = args.getInt(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
        }
        mToolbar.setTitle("客服中心");
        if (animType == ANIM_TYPE_NEW_FLOW) {
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_close);
        }
        else{
            mToolbar.setNavigationIcon(R.drawable.ic_toolbar_back);
        }
        mToolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });
//        refresh(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        ZLogger.d(String.format("requestCode=%d, resultCode=%d, intent=%s",
                requestCode,
                resultCode,
                StringUtils.decodeBundle(intent != null ? intent.getExtras() : null)));
        if (resultCode != Activity.RESULT_OK){
            return;
        }

        switch (requestCode){
//            case Constants.ACTIVITY_REQUEST_CHANGE_NICKNAME:
//                btnItems.get(0).setDetailText(MfhLoginService.get().getHumanName());
//                break;
            case ARCode.ARC_INUT_TEXT:{
                if (resultCode == Activity.RESULT_OK){
                    // TODO: 12/10/2016 提交反馈信息

                }
            }
            break;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * 客服中心
     */
    @OnClick(R.id.item_call)
    public void customerService() {
        CommonDialog dialog = new CommonDialog(getActivity());
        dialog.setMessage(Mixicook.CUSTOMER_SERVICELCENTER);
        dialog.setPositiveButton("呼叫", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                UIHelper.callPhone(getActivity(), Mixicook.CUSTOMER_SERVICELCENTER);
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @OnClick(R.id.item_feedback)
    public void redirect2Feedback() {
        Bundle extras = new Bundle();
//        extras.putInt(BaseActivity.EXTRA_KEY_ANIM_TYPE, BaseActivity.ANIM_TYPE_NEW_FLOW);
        extras.putInt(FragmentActivity.EXTRA_KEY_FRAGMENT_TYPE, FragmentActivity.FT_INPUT_TEXT);
        extras.putString(EXTRA_KEY_TITLE, "反馈");
        extras.putString(EXTRA_KEY_HINT_TEXT, "随便吐槽400字以内");
//        extras.putString(InputTextFragment.EXTRA_KEY_RAW_TEXT, mOrderBrief.getRemark());
        Intent intent = new Intent(getActivity(), FragmentActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, ARCode.ARC_INUT_TEXT);
    }

}
