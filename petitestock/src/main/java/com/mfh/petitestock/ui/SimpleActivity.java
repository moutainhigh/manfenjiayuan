package com.mfh.petitestock.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import com.mfh.framework.uikit.base.BaseActivity;
import com.mfh.petitestock.R;
import com.mfh.petitestock.ui.fragment.shelves.GoodsShelvesHistoryFragment;
import com.mfh.petitestock.ui.fragment.stocktake.StockTakeFragment;
import com.mfh.petitestock.ui.fragment.stocktake.StockTakeHistoryFragment;
import com.mfh.petitestock.ui.fragment.stocktake.StockTakeSettingsFragment;

/**
 * 服务
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SimpleActivity extends BaseActivity {
    public static final String EXTRA_KEY_SERVICE_TYPE = "EXTRA_KEY_SERVICE_TYPE";
    public static final String EXTRA_KEY_COURIER = "EXTRA_KEY_COURIER";
    public static final int FRAGMENT_TYPE_STOCKTAKE_SETTINGS = 0;//盘点设置
    public static final int FRAGMENT_TYPE_STOCKTAKE_LIST = 1;//盘点记录
    public static final int FRAGMENT_TYPE_STOCK_TAKE = 2;//盘点
    public static final int FRAGMENT_TYPE_SHELVESBIND_HISTORY = 3;//商品绑定货架

    /**
     * 0: 快递代收
     * */
    private int serviceType = 0;

    public static void actionStart(Context context, Bundle extras) {
        Intent intent = new Intent(context, SimpleActivity.class);
        if (extras != null){
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

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

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

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK
//                && event.getAction() == KeyEvent.ACTION_DOWN) {
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    private void handleIntent(){
        Intent intent = this.getIntent();
        if(intent != null){
            int animType = intent.getIntExtra(EXTRA_KEY_ANIM_TYPE, ANIM_TYPE_NEW_NONE);
            //setTheme必须放在onCreate之前执行，后面执行是无效的
            if (animType == ANIM_TYPE_NEW_FLOW) {
                this.setTheme(R.style.NewFlow);
            }

            serviceType = intent.getIntExtra(EXTRA_KEY_SERVICE_TYPE, -1);
        }
    }

    /**
     * 初始化内容视图
     * Caused by: java.lang.IllegalStateException: commit already called
     * */
    private void initFragments(){
        if(serviceType == FRAGMENT_TYPE_STOCKTAKE_SETTINGS){
            StockTakeSettingsFragment stockTakeSettingsFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                stockTakeSettingsFragment = StockTakeSettingsFragment.newInstance(intent.getExtras());
            }else{
                stockTakeSettingsFragment = StockTakeSettingsFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, stockTakeSettingsFragment).show(stockTakeSettingsFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_STOCKTAKE_LIST){
            StockTakeHistoryFragment stockTakeHistoryFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                stockTakeHistoryFragment = StockTakeHistoryFragment.newInstance(intent.getExtras());
            }else{
                stockTakeHistoryFragment = StockTakeHistoryFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, stockTakeHistoryFragment).show(stockTakeHistoryFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_STOCK_TAKE){
            StockTakeFragment stockTakeFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                stockTakeFragment = StockTakeFragment.newInstance(intent.getExtras());
            }else{
                stockTakeFragment = StockTakeFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, stockTakeFragment).show(stockTakeFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_SHELVESBIND_HISTORY){
            GoodsShelvesHistoryFragment goodsShelvesHistoryFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                goodsShelvesHistoryFragment = GoodsShelvesHistoryFragment.newInstance(intent.getExtras());
            }else{
                goodsShelvesHistoryFragment = GoodsShelvesHistoryFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, goodsShelvesHistoryFragment).show(goodsShelvesHistoryFragment)
                    .commit();
        }
    }
}
