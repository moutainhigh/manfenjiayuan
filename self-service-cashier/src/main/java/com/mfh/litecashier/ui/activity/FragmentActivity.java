package com.mfh.litecashier.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.litecashier.R;
import com.mfh.litecashier.ui.fragment.GrouponFragment;
import com.mfh.litecashier.ui.fragment.cashier.StockDetailFragment;
import com.mfh.litecashier.ui.fragment.goods.FrontCategoryFragment;

/**
 * 服务
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class FragmentActivity extends BaseActivity {
    public static final String EXTRA_KEY_SERVICE_TYPE = "EXTRA_KEY_SERVICE_TYPE";

    public static final int FT_ADDMORE_LOCALFRONTGOODS = 0x01;//添加更多前台类目商品
    public static final int FT_STOCK_DETAIL = 0x32;
    public static final int FT_GROUPON_DETAIL = 0x33;  //团购详情页

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
    protected boolean isFullscreenEnabled() {
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
//            if (animType == ANIM_TYPE_NEW_FLOW) {
////                this.setTheme(R.style.NewFlow);
//            }

            fragmentType = intent.getIntExtra(EXTRA_KEY_SERVICE_TYPE, -1);
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     */
    private void initFragments() {
        switch (fragmentType) {
            case FT_ADDMORE_LOCALFRONTGOODS: {
                Intent intent = this.getIntent();
                FrontCategoryFragment fragment = FrontCategoryFragment.newInstance(intent.getExtras());
                getSupportFragmentManager().beginTransaction()
//                    .add(R.id.fragment_container, purchaseShopcartFragment).show(purchaseShopcartFragment)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            break;
            case FT_STOCK_DETAIL: {
                StockDetailFragment stockDetailFragment = new StockDetailFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, stockDetailFragment).show(stockDetailFragment)
                        .commit();
            }
            //团购详情页
            case FT_GROUPON_DETAIL: {
                GrouponFragment fragment;
                Intent intent = this.getIntent();
                if (intent != null) {
                    fragment = GrouponFragment.newInstance(intent.getExtras());
                } else {
                    fragment = GrouponFragment.newInstance(null);
                }
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment).show(fragment)
                        .commit();
            }
        }
    }

}
