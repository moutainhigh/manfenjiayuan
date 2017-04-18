package com.manfenjiayuan.pda_supermarket.ui.convertRecv;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.WindowManager;

import com.bingshanguxue.pda.IData95Activity;
import com.bingshanguxue.pda.bizz.company.InvCompanyListFragment;
import com.bingshanguxue.pda.bizz.invrecv.InvRecvInspectFragment;
import com.manfenjiayuan.pda_supermarket.R;
import com.mfh.framework.core.utils.DeviceUtils;
import com.mfh.framework.uikit.BackHandledInterface;
import com.mfh.framework.uikit.base.BaseFragment;

import java.util.Observable;
import java.util.Observer;

/**
 * 转换收货
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class ConvertRecvActivity extends IData95Activity implements BackHandledInterface {

    private Fragment mInvCompaniesFragment;//选择批发商列表
    private Fragment mConvertRulesFragment;//选择转换模型
    private Fragment mInspectFragment;//验收商品
    private Fragment mTargetFragment;//目标商品页面


     public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, ConvertRecvActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_service;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        handleIntent();

        super.onCreate(savedInstanceState);

        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        if (savedInstanceState != null) {  // “内存重启”时调用
            mInvCompaniesFragment = getSupportFragmentManager().findFragmentByTag("mInvCompaniesFragment");
            mConvertRulesFragment = getSupportFragmentManager().findFragmentByTag("mConvertRulesFragment");
            mInspectFragment = getSupportFragmentManager().findFragmentByTag("mInspectFragment");
            mTargetFragment = getSupportFragmentManager().findFragmentByTag("mTargetFragment");
            // 解决重叠问题
            getSupportFragmentManager().beginTransaction()
                    .show(mInvCompaniesFragment)
                    .hide(mConvertRulesFragment)
                    .hide(mInspectFragment)
                    .hide(mTargetFragment)
                    .commit();
        } else {  // 正常时
            mInvCompaniesFragment = InvCompanyListFragment.newInstance(null);
            mConvertRulesFragment = InvCompanyListFragment.newInstance(null);
            mInspectFragment = InvRecvInspectFragment.newInstance(null);
            mTargetFragment = InvCompanyListFragment.newInstance(null);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mInvCompaniesFragment, "mInvCompaniesFragment")
                    .add(R.id.fragment_container,mConvertRulesFragment, "mConvertRulesFragment")
                    .add(R.id.fragment_container,mInspectFragment, "mInspectFragment")
                    .add(R.id.fragment_container,mTargetFragment, "mTargetFragment")
                    .hide(mConvertRulesFragment)
                    .hide(mInspectFragment)
                    .hide(mTargetFragment)
                    .commit();
        }

        ConvertRecvObservable.getInstance().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {

            }
        });
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        DeviceUtils.hideSoftInput(this);

        if (mBackHandedFragment == null || !mBackHandedFragment.onBackPressed()) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                super.onBackPressed();
            } else {
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    private BaseFragment mBackHandedFragment;

    @Override
    public void setSelectedFragment(BaseFragment selectedFragment) {
        this.mBackHandedFragment = selectedFragment;
    }

    private void handleIntent() {
        Intent intent = this.getIntent();
        if (intent != null) {
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if (animType == ANIM_TYPE_NEW_FLOW) {
                this.setTheme(R.style.NewFlow);
            }
//            else{
//                this.setTheme(R.style.AppTheme_NoTitleBar);
//            }

        }
    }

}
