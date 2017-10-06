package com.mfh.litecashier.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.rxapi.bean.CompanyHuman;
import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.R;
import com.mfh.litecashier.components.OrderFlowFragment;
import com.mfh.litecashier.components.SettingsGoodsFragment;
import com.mfh.litecashier.components.company.CompanyHumanQueryDialog;
import com.mfh.litecashier.ui.fragment.inventory.InventoryCostFragment;
import com.mfh.litecashier.ui.fragment.purchase.PurchaseFragment;
import com.mfh.litecashier.ui.fragment.purchase.manual.ManualPurchaseFragment;
import com.mfh.litecashier.ui.fragment.purchase.manual.ManualPurchaseShopcartFragment;
import com.mfh.litecashier.ui.fragment.settings.SettingsFragment;

import butterknife.BindView;


/**
 * 服务
 * Created by bingshanguxue on 15/8/30.
 */
public class UnauthorizedActivity extends BaseActivity {
    public static final String EXTRA_KEY_LOGIN_TYPE = "loginType";

    public static final int LOGIN_TYPE_USERNAME_PASSWORD = 0x01;//用户名+密码登录
    public static final int LOGIN_TYPE_CARD = 0x02;//快捷登录-刷卡/扫码/手机号

    private int loginType = LOGIN_TYPE_USERNAME_PASSWORD;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, UnauthorizedActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutResId() {
        return R.layout.activity_unauthorized;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return false;
    }

    @Override
    protected boolean isFullscreenEnabled() {
        return true;
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

//        startService(new Intent(this, Utf7ImeService.class));
        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (loginType == LOGIN_TYPE_CARD) {
            showCompanyHumanQueryDialog(CompanyHumanQueryDialog.TARGET_CASHIER_SIGNIN);
        } else {
            ZLogger.w("not expert loginType: " + loginType);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    private void handleIntent() {
        Intent intent = this.getIntent();
        if (intent != null) {
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if (animType == ANIM_TYPE_NEW_FLOW) {
//                this.setTheme(R.style.NewFlow);
            }

            loginType = intent.getIntExtra(EXTRA_KEY_LOGIN_TYPE, LOGIN_TYPE_USERNAME_PASSWORD);
        }
    }

    private CompanyHumanQueryDialog mCompanyHumanQueryDialog;

    /**
     * 显示查询收银员对话框
     */
    private synchronized void showCompanyHumanQueryDialog(int target) {
        if (mCompanyHumanQueryDialog == null) {
            mCompanyHumanQueryDialog = new CompanyHumanQueryDialog();
        }
        mCompanyHumanQueryDialog.setTargetAndListener(target, mOnCompanyHumanQueryListener);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        mCompanyHumanQueryDialog.show(ft, CompanyHumanQueryDialog.TAG);
    }

    private CompanyHumanQueryDialog.OnCompanyHumanQueryListener mOnCompanyHumanQueryListener = new CompanyHumanQueryDialog.OnCompanyHumanQueryListener() {
        @Override
        public void onQuerySuccess(int target, CompanyHuman companyHuman) {
            switch (target) {
                case CompanyHumanQueryDialog.TARGET_CASHIER_SIGNIN:
                    // TODO: 16/09/2017
//                    settleStep3(type, human);
                    finish();
                    break;
                default:
                    break;
            }
        }
    };
}
