package com.manfenjiayuan.pda_supermarket.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.bingshanguxue.pda.IData95Activity;
import com.manfenjiayuan.pda_supermarket.R;
import com.manfenjiayuan.pda_supermarket.ui.invcheck.InvCheckHistoryFragment;
import com.manfenjiayuan.pda_supermarket.ui.invcheck.InvCheckInspectFragment;

/**
 * 服务
 * Created by Nat.ZZN(bingshanguxue) on 15/8/30.
 */
public class SimpleActivity extends IData95Activity {
    public static final String EXTRA_KEY_SERVICE_TYPE = "EXTRA_KEY_SERVICE_TYPE";
    public static final String EXTRA_KEY_COURIER = "EXTRA_KEY_COURIER";
    public static final int FRAGMENT_TYPE_STOCKTAKE_LIST = 1;//盘点记录
    public static final int FRAGMENT_TYPE_STOCK_TAKE = 2;//盘点

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

//        hideSystemUI();

        handleIntent();

        super.onCreate(savedInstanceState);

//        startService(new Intent(this, Utf7ImeService.class));
        //hide soft input
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initFragments();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        hideSystemUI();
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
        if(serviceType == FRAGMENT_TYPE_STOCKTAKE_LIST){
            InvCheckHistoryFragment invCheckHistoryFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                invCheckHistoryFragment = InvCheckHistoryFragment.newInstance(intent.getExtras());
            }else{
                invCheckHistoryFragment = InvCheckHistoryFragment.newInstance(null);
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, invCheckHistoryFragment)
//                    .add(R.id.fragment_container, invCheckHistoryFragment).show(invCheckHistoryFragment)
                    .commit();
        }
        else if(serviceType == FRAGMENT_TYPE_STOCK_TAKE){
            InvCheckInspectFragment invCheckInspectFragment;
            Intent intent = this.getIntent();
            if (intent != null){
                invCheckInspectFragment = InvCheckInspectFragment.newInstance(intent.getExtras());
            }else{
                invCheckInspectFragment = InvCheckInspectFragment.newInstance(null);
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, invCheckInspectFragment)
//                    .add(R.id.fragment_container, invCheckInspectFragment).show(invCheckInspectFragment)
                    .commit();
        }
    }
}
