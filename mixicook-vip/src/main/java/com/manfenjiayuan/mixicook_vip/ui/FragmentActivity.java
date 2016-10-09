package com.manfenjiayuan.mixicook_vip.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import com.manfenjiayuan.mixicook_vip.R;
import com.manfenjiayuan.mixicook_vip.ui.address.AddAddressFragment;
import com.manfenjiayuan.mixicook_vip.ui.address.MyAddressFragment;
import com.manfenjiayuan.mixicook_vip.ui.home.QuickPayFragment;
import com.manfenjiayuan.mixicook_vip.ui.hybrid.HybridFragment;
import com.manfenjiayuan.mixicook_vip.ui.location.LocationFragment;
import com.manfenjiayuan.mixicook_vip.ui.my.ChangeLoginPwdFragment;
import com.manfenjiayuan.mixicook_vip.ui.my.ChangeNicknameFragment;
import com.manfenjiayuan.mixicook_vip.ui.my.ChangePayPwdFragment;
import com.manfenjiayuan.mixicook_vip.ui.my.MyFragment;
import com.manfenjiayuan.mixicook_vip.ui.my.SettingsFragment;
import com.manfenjiayuan.mixicook_vip.ui.order.ConfirmOrderFragment;
import com.manfenjiayuan.mixicook_vip.ui.reserve.ReserveFragment;
import com.manfenjiayuan.mixicook_vip.ui.shopcart.ShopcartFragment;
import com.manfenjiayuan.mixicook_vip.ui.topup.TopupFragment;
import com.mfh.framework.uikit.base.BaseActivity;

/**
 * 服务
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class FragmentActivity extends BaseActivity {
    public static final String EXTRA_KEY_FRAGMENT_TYPE = "EXTRA_KEY_FRAGMENT_TYPE";

    public static final int FT_CONFIRM_ORDER = 0x01;//确认订单
    public static final int FT_MY = 0x02;//我的
    public static final int FT_SHOPCART = 0x03;//购物车
    public static final int FT_RESERVE = 0x04;//预定
    public static final int FT_LOCATION = 0x05;//定位
    public static final int FT_HYBRID = 0x06;//Hybrid
    public static final int FT_SETTINGS = 0x07;//设置
    public static final int FT_LOGIN_PASSWORD = 0x08;//登录密码
    public static final int FT_PAY_PASSWORD = 0x09;//支付密码
    public static final int FT_CHANGE_NICKNAME = 0x0A;//昵称
    public static final int FT_MYADDRESS = 0x0B;//收货地址
    public static final int FT_ADD_ADDRESS = 0x0C;//添加收货地址
    public static final int FT_TOPUP = 0x0D;//充值
    public static final int FT_QUICK_PAY = 0x0E;//快捷支付


    private int fragmentType = 0;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, FragmentActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected boolean isBackKeyEnabled() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handleIntent();

        super.onCreate(savedInstanceState);

//        startService(new Intent(this, Utf7ImeService.class));
        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initFragments();
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

            fragmentType = intent.getIntExtra(EXTRA_KEY_FRAGMENT_TYPE, -1);
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     */
    private void initFragments() {
        switch (fragmentType) {
            case FT_CONFIRM_ORDER: {
                ConfirmOrderFragment fragment = new ConfirmOrderFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_MY: {
                MyFragment fragment = new MyFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_SHOPCART: {
                ShopcartFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = ShopcartFragment.newInstance(intent.getExtras());
                } else {
                    fragment = ShopcartFragment.newInstance(null);
                }

                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_RESERVE: {
                ReserveFragment fragment = new ReserveFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_LOCATION: {
                LocationFragment fragment = new LocationFragment();
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_HYBRID: {
                HybridFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = HybridFragment.newInstance(intent.getExtras());
                } else {
                    fragment = HybridFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_SETTINGS: {
                SettingsFragment fragment = new SettingsFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment).show(fragment)
                        .commit();
            }
            break;
            case FT_LOGIN_PASSWORD: {
                ChangeLoginPwdFragment fragment = new ChangeLoginPwdFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment).show(fragment)
                        .commit();
            }
            break;
            case FT_PAY_PASSWORD: {
                ChangePayPwdFragment fragment = new ChangePayPwdFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment).show(fragment)
                        .commit();
            }
            break;
            case FT_CHANGE_NICKNAME: {
                ChangeNicknameFragment fragment = new ChangeNicknameFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment).show(fragment)
                        .commit();
            }
            break;
            case FT_MYADDRESS: {
                MyAddressFragment fragment = new MyAddressFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment).show(fragment)
                        .commit();
            }
            break;
            case FT_ADD_ADDRESS: {
                AddAddressFragment fragment = new AddAddressFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment).show(fragment)
                        .commit();
            }
            break;
            case FT_TOPUP: {
                TopupFragment fragment = new TopupFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment).show(fragment)
                        .commit();
            }
            break;
            case FT_QUICK_PAY: {
                QuickPayFragment fragment = new QuickPayFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment).show(fragment)
                        .commit();
            }
            break;
            default:
                break;
        }
    }

}
